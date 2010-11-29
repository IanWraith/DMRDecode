// Please note much of the code in this program was taken from the DSD software
// and converted into Java. The author of this software is unknown but has the
// GPG Key ID below

// Copyright (C) 2010 DSD Author
// GPG Key ID: 0x3F1D7FD0 (74EF 430D F7F2 0A48 FCE6  F630 FAA2 635D 3F1D 7FD0)
// 
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND ISC DISCLAIMS ALL WARRANTIES WITH
// REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS.  IN NO EVENT SHALL ISC BE LIABLE FOR ANY SPECIAL, DIRECT,
// INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
// LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
// OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
// PERFORMANCE OF THIS SOFTWARE.
//

package com.dmr;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.StyleConstants;
import javax.swing.JEditorPane;
import java.text.DateFormat;
import java.util.Date;

public class DMRDecode {
	private DisplayModel display_model;
	private DisplayView display_view;
	private static DMRDecode theApp;
	static DisplayFrame window;
	public String program_version="DMR Decoder V0.00 Build 12";
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private static boolean RUNNING=true;
	private static final int SAMPLESPERSYMBOL=10;
	private int jitter=-1;
	private static final int SYMBOLCENTRE=4;
	private static final int MAXSTARTVALUE=15000;
	private static final int MINSTARTVALUE=-15000;
	private int max=MAXSTARTVALUE;
	private int min=MINSTARTVALUE;
	private int centre=0;
	private int lastsample=0;
	private int maxref=12000;
	private int minref=-12000;
	private int lastsynctype=-1;
	private int symbolcnt=0;
	private static final byte DMR_DATA_SYNC[]={3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1};
	private static final byte DMR_VOICE_SYNC[]={1,3,1,1,1,1,3,3,3,1,1,3,3,1,3,3,1,3,1,1,3,3,1,3};
	private boolean carrier=false;
	public boolean inverted=true;
	private boolean firstframe=false;
	public JEditorPane editorPane;
	public HTMLDocument doc;
	public Element el;
	private int lmid=0;
	private int umid=0;
	private int synctype;
	private BufferedReader br;
	private byte dibitCircularBuffer[]=new byte[144];
	private int dibitCircularBufferCounter=0;
	private byte dibitFrame[]=new byte[144];
	private boolean frameSync=false;
	public boolean saveToFile=false;
	public FileWriter file;
	public boolean logging=false;
	public boolean pReady=false;
	private boolean audioSuck=false;
	private int symbolBuffer[]=new int[144];
	public AudioInThread lineInThread=new AudioInThread(this);
	private boolean debug=false;
	private boolean viewVoiceFrames=true;
	private boolean viewDataFrames=true;
	private boolean viewEmbeddedFrames=true;
	public int frameCount=0;
	public int badFrameCount=0;
	public ShortLC short_lc=new ShortLC();
	public int embeddedFrameCount=0;
	private int symbolBufferCounter=0;
	private int errorFreeFrameCount=0;
	private SettingsChoice settingsChoice=new SettingsChoice();
	
	public static void main(String[] args) {
		theApp=new DMRDecode();
		SwingUtilities.invokeLater(new Runnable(){public void run(){theApp.createGUI();}});
		// If sucking in test data then open the file
		if (theApp.audioSuck==true) theApp.prepareAudioSuck("aor3000_audiodump.csv");
		 else theApp.lineInThread.startAudio();
		// Set the debug mode in the settings choice object
		theApp.settingsChoice.setDebug(true);
		// The main routine
		while (RUNNING)	{
			if ((theApp.lineInThread.getAudioReady()==true)&&(theApp.pReady==true)) theApp.decode();
		}

		}
	
	// Setup the window //
	public void createGUI() {
		window=new DisplayFrame(program_version,this);	
		Toolkit theKit=window.getToolkit();
		Dimension wndsize=theKit.getScreenSize();
		window.setBounds(wndsize.width/6,wndsize.height/6,2*wndsize.width/3,2*wndsize.height/3);
		window.addWindowListener(new WindowHandler());
		display_model=new DisplayModel();
		editorPane=new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setText("<html><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"></table></html>");
	    doc=(HTMLDocument)editorPane.getDocument();
		el=doc.getElement(doc.getDefaultRootElement(),StyleConstants.NameAttribute,HTML.Tag.TABLE);
		JScrollPane scrollPane=new JScrollPane(editorPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		window.getContentPane().add(scrollPane,BorderLayout.CENTER);
		window.setVisible(true);
		// Make certain the program knows the GUI is ready
		pReady=true;
		}

	class WindowHandler extends WindowAdapter {
		public void windowClosing(WindowEvent e) {	
			}
		}

	public DisplayFrame getWindow()	{
		return window;	
		}

	public DisplayModel getModel() {
		return display_model;
		}

	public DisplayView getView() {
		return display_view;	
		}
	
  
	// The main routine for decoding DMR data
	public void decode()	{
		  noCarrier();
		  synctype=getFrameSync();
		  calcMids();
	      while (synctype!=-1)	{
	          processFrame();
	          synctype=getFrameSync(); 
	          createDibitFrame();
	        }  
	  }
	
	// Calculate the waveform centre and mid points
	public void calcMids()	{
			centre=(max+min)/2;
			umid=((max-centre)*5/8)+centre;
		    lmid=((min-centre)*5/8)+centre;		
	}
	
	// A function containing the calculations required when a frame is detected
	private void frameCalcs (int lmin,int lmax)	{
		// The code required below appears to depend on the soundcard
		// Viglen PC code
		//max=(lmax+max)/2;
		//min=(lmin+min)/2;	
		// Acer PC Code 
		max=lmax;
		min=lmin;
		///////////////////
		maxref=(int)((float)max*(float)1.25);
		minref=(int)((float)min*(float)1.25);;
	}
	
	
	// This code lifted straight from the DSD source code converted to Java 
	// and tidied up removing non DMR code
	public int getSymbol(boolean have_sync)	{
		  int sample,i,sum=0,symbol,count=0;
		  for (i=0;i<SAMPLESPERSYMBOL;i++)	{
		      if ((i==0)&&(have_sync==false))	{
		        if ((jitter>0)&&(jitter<=SYMBOLCENTRE)) i--;          
		         else if ((jitter>SYMBOLCENTRE)&&(jitter<SAMPLESPERSYMBOL)) i++;          
		        jitter=-1;
		       }
			  if (audioSuck==false)	{ 
				  // Loop until a sample is ready
				  while (lineInThread.sampleReady()==false)	{}
				  // Get the sample from the sound card via the sound thread
				  sample=lineInThread.returnSample();
			  }
			  else	{
				  // Get the data from the suck file
				  int fsample=getSuckData();
				  // Push this through a root raised filter
				  sample=lineInThread.rootRaisedFilter(fsample);
			  }
			  if ((sample>max)&&(have_sync==true)) sample=max;  
			    else if ((sample<min)&&(have_sync==true)) sample=min;
		      if (sample>centre)	{
		    	  if ((jitter<0)&&(lastsample<centre)&&(sample<maxref)) jitter=i;   
		        }
		      else if ((sample>minref)&&(jitter<0)&&(lastsample>centre)) jitter=i;
      
		      if ((i>=SYMBOLCENTRE-1)&&(i<=SYMBOLCENTRE+2)) {
		    	  sum=sum+sample;
		          count++;
		          }
		      lastsample=sample;
		    }
		  symbol=(sum/count);
		  symbolcnt++;		  
		  return symbol;
	  }
	  

	// Grab 144 dibits then check if they have a sync pattern and if they do then process 
	// them accordingly
	public int getFrameSync ()	{
		int t=0,dibit,symbol,synctest_pos=0,syncType;
		int lmin=0,lmax=0,a,highVol;
		// Clear the symbol counter
		symbolcnt=0;

		while (true) {
			t++;
			// Get a symbol from the soundcard
			symbol=getSymbol(frameSync);
			// Store this in the rotating symbol buffer
			// Only needed if we don't have frame sync
			if (frameSync==false) addToSymbolBuffer(symbol);
			// Set the dibit state
			dibit=symboltoDibit(symbol);
			// Add the dibit to the circular dibit buffer
			addToDitbitBuf(dibit);
		    // If we have received 144 dibits then we can check for a valid sync sequence
			if (t>=144) {
				// If we don't have frame sync then rotate the symbol buffer
				// and also find the new minimum and maximum
				if (frameSync==false)	{
					// Get the frames 24 sync symbols
					int lbuf2[]=getSyncSymbols();
					lmin=1;
					lmax=-1;
					for (a=0;a<24;a++)	{
						if (lbuf2[a]<lmin) lmin=lbuf2[a];
						if (lbuf2[a]>lmax) lmax=lbuf2[a];
					}
					maxref=max;
					minref=min;
				}
				// Update the volume bar every 6 frames
				if ((t%864)==0)	{
					highVol=lineInThread.getHighest();
					window.updateVolumeBar(highVol);
				}
				// Check if a frame has a voice or data sync
				// If no frame sync do this at any time but if we do have
				// frame sync then only do this every 144 bits
				if ((frameSync==false)||((frameSync==true)&&(symbolcnt%144==0)))	{
					// Identify the frame sync type which returns
					// 0 if unknown
					// 1 if voice
					// 2 if data
					syncType=syncCompare(frameSync);
					// Embedded signalling frame
					if ((frameSync==true)&&(syncType==0)&&(firstframe==false)&&(embeddedFrameCount<7))	{
						// Increment the embedded frame counter
						embeddedFrameCount++;
						lastsynctype=13;
						return (13);
					}					
					// Data frame
					if (syncType==2) {
						// Clear the embedded frame counter
						embeddedFrameCount=0;
						carrier=true;
						if (frameSync==false)	{
							frameCalcs(lmin,lmax);
							frameSync=true;
						}
						if (lastsynctype==-1) firstframe=true;
						else firstframe=false;
						lastsynctype=10;
						return (10);
					}
					// Voice frame
					if (syncType==1) {
						// Clear the embedded frame counter
						embeddedFrameCount=0;
						carrier=true;
						if (frameSync==false)	{
							frameCalcs(lmin,lmax);
							frameSync=true;
						}
						if (lastsynctype==-1) firstframe=true;
						else firstframe=false;
						lastsynctype=12;
						return (12);
					}
				
				}
		}					
		// We had a signal but appear to have lost it
		if (carrier==true) {
			// If we have missed 12 frames then something is wrong
			if (synctest_pos>=1728) {
				// If in debug mode show that sync has been lost
				if (debug==true)	{
					String l=getTimeStamp()+" Sync Lost";
					l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter);
					l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
					addLine(l);
					fileWrite(l);
				}
				frameSync=false;
				noCarrier();
				return (-1);
				}
			}
		// If the hunt has gone on for a while then reset everything
		if (t>32000) {
			t=0;
			synctest_pos=0;
			}
		else synctest_pos++;
		}
	  }
	  
	// Add a dibit to the circular dibit buffer
	void addToDitbitBuf (int dibit)	{
		dibitCircularBuffer[dibitCircularBufferCounter]=(byte)dibit;
		dibitCircularBufferCounter++;
		if (dibitCircularBufferCounter==144) dibitCircularBufferCounter=0;
	}
	
	// Add a symbol to the circular symbol buffer
	void addToSymbolBuffer (int symbol)	{
		symbolBuffer[symbolBufferCounter]=symbol;
		symbolBufferCounter++;
		if (symbolBufferCounter==144) symbolBufferCounter=0;
	}
	
	// No carrier or carrier lost so clear the variables
	public void noCarrier ()	{
		jitter=-1;
		lastsynctype=-1;
		carrier=false;
		max=MAXSTARTVALUE;
		min=MINSTARTVALUE;
		centre=0;
		firstframe=false;
		errorFreeFrameCount=0;
		// Update the sync label
		window.updateSyncLabel(false);
	  	}
	
	// Given a symbol return a dibit
	int symboltoDibit (int symbol)	{
		// With Sync
		if (frameSync==true)	{
			if (inverted==false)	{
				// Normal
				if (symbol>centre) {
					if (symbol>umid) return 1;
					else return 0;
				}
				else {
					if (symbol<lmid) return 3;
					else return 2;
				}
			} else	{	
				// Inverted
				if (symbol>centre) {
					if (symbol>umid) return 3;
					else return 2;
				}
				else {
					if (symbol<lmid) return 1;
					else return 0;
				}
			}
		} else	{
				// No Sync
				// Normal
				if (inverted==false)	{
					if (symbol>0) return 1;
					else return 3;
				}
				// Inverted
				else	{
					if (symbol>0) return 3;
					else return 1;
				}
			}
	}
	  	
	// Compare the sync sequences held in global arrays with the contents of the dibit circular buffer
	// Returns ..
	// 0 if unknown
	// 1 if voice
	// 2 if data
	private int syncCompare(boolean sync)	{
		int i,dataSync=0,voiceSync=0,diff,circPos;
		// Allow 5 dibits to be incorrect when syncronised and set the offset
		if (sync==true)	diff=5;
		else diff=0;
		circPos=dibitCircularBufferCounter+66;
		if (circPos>=144) circPos=circPos-144;
		for (i=0;i<24;i++)	{
			if (dibitCircularBuffer[circPos]==DMR_VOICE_SYNC[i]) voiceSync++;
			if (dibitCircularBuffer[circPos]==DMR_DATA_SYNC[i]) dataSync++;
			circPos++;
			if (circPos==144) circPos=0;
		}
		if ((DMR_VOICE_SYNC.length-voiceSync)<=diff) return 1;
		else if ((DMR_DATA_SYNC.length-dataSync)<=diff) return 2;
		else return 0;	
	}
	
	// Extract just the 24 symbols of the sync sequence and return them in an array
	private int[] getSyncSymbols()	{
		int i,circPos;
		int syms[]=new int[24];
		circPos=symbolBufferCounter+66;
		if (circPos>=144) circPos=circPos-144;
		for (i=0;i<24;i++)	{
			syms[i]=symbolBuffer[circPos];
			circPos++;
			if (circPos==144) circPos=0;
		}
		return syms;	
	}
	  
	// Adds a line to the display
	public void addLine(String line) {
		  try {
			  doc.insertAfterStart(el,"<tr>"+line +"</tr>");
		  }
		  catch (Exception e) {
			  System.out.println("Exception:" + e.getMessage());
		  }		
	}

	// Return a time stamp
	public String getTimeStamp() {
		Date now=new Date();
		DateFormat df=DateFormat.getTimeInstance();
		return df.format(now);
	}	
	
	// Handle an incoming DMR Frame
	void processFrame ()	{
		String l;
	    maxref=max;
	    minref=min;
	    if (firstframe==true)	{	
	    	// Check if these settings are any good
	    	if (settingsChoice.testChoice(max,min)==false)	{
	    		settingsChoice.recordForce();
	    		max=settingsChoice.getBestMax();
	    		min=settingsChoice.getBestMin();
	    		calcMids();
	    		}
	    	// If debug enabled record obtaining sync
			if (debug==true)	{
				if (synctype==12) l=getTimeStamp()+" DMR Voice Sync Acquired";
				else l=getTimeStamp()+" DMR Data Sync Acquired";
				l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter);
				l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
				addLine(l);
				fileWrite(l);
			}
			return;
	    }
	    // Update the sync label
	    window.updateSyncLabel(frameSync);
	    // Deal with the frame
	    if ((synctype==12)&&(viewVoiceFrames==true)) processDMRvoice ();
	    else if ((synctype==10)&&(viewDataFrames==true)) processDMRdata ();
	    else if ((synctype==13)&&(viewEmbeddedFrames==true)) processEmbedded ();
	}

	// Handle a DMR Voice Frame
	void processDMRvoice ()	{	
		DMRVoice DMRvoice=new DMRVoice();
		String line[]=new String[10];
		line=DMRvoice.decode(theApp,dibitFrame);
		line[0]=line[0]+dispSymbolsSinceLastFrame();
		if (debug==true)	{
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		frameCount++;
		if (DMRvoice.isError()==false)	{
			badFrameCount++;
			line[0]=getTimeStamp()+" DMR Voice Frame - Error ! ";
			line[0]=line[0]+dispSymbolsSinceLastFrame();	
		}
		displayLines(line);
	}
	
	// Handle a DMR Data Frame
	void processDMRdata ()	{
		DMRDataDecode DMRdata=new DMRDataDecode();
		String line[]=new String[10];
		line=DMRdata.decode(theApp,dibitFrame);
		line[0]=line[0]+dispSymbolsSinceLastFrame();		
		if (debug==true)	{
			line[0]=line[0]+" jitter="+Integer.toString(jitter);
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		frameCount++;
		if (DMRdata.isError()==false)	{
			badFrameCount++;
			line[0]=getTimeStamp()+" DMR Data Frame - Error ! ";
			line[0]=line[0]+dispSymbolsSinceLastFrame();	
			int gval=DMRdata.getGolayValue();
			if (gval!=-1) line[0]=line[0]+" ("+Integer.toString(gval)+")";
			if (debug==true) line[0]=line[0]+" jitter="+Integer.toString(jitter);
			// Record that there has been a frame with an error
			errorFreeFrameCount=0;
			settingsChoice.badFrameRecord();
		}
		else	{
			// Record that there has been an error free frame
			errorFreeFrameCount++;
			if (errorFreeFrameCount>settingsChoice.getBestScore())	{
				if (debug==true) addLine(getTimeStamp()+" Best Score so far");
				settingsChoice.setBestChoice(max,min,errorFreeFrameCount);
			}
			settingsChoice.goodFrameRecord();
		}
		// Display the info
		displayLines(line);
	}
	
	// Handle an embedded frame
	void processEmbedded ()	{
		DMREmbedded DMRembedded=new DMREmbedded();
		String line[]=new String[10];
		line=DMRembedded.decode(theApp,dibitFrame);
		line[0]=line[0]+dispSymbolsSinceLastFrame();
		if (debug==true)	{
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		frameCount++;
		if (DMRembedded.isError()==false)	{
			badFrameCount++;
			line[0]=getTimeStamp()+" DMR Embedded Frame - Error ! ";
			line[0]=line[0]+dispSymbolsSinceLastFrame();	
			// Record that there has been a frame with an error
			errorFreeFrameCount=0;
		}
		else	{
			// Set last sync type to 14 to show this was a good embedded frame
			lastsynctype=14;
		}
		// Display the info
		displayLines(line);
	}

	// Display a group of lines
	void displayLines (String line[])	{
		int a;
		int len=line.length;
		for (a=(len-1);a>=0;a--)	{
			if (line[a]!=null) addLine(line[a]);
		}
		// Log to disk if needed
		if (logging==true)	{
			for (a=0;a<len;a++)	{
				if (line[a]!=null) fileWrite(line[a]);
			}
		}
	}
	
	// Write to a string to the logging file
	public boolean fileWrite(String fline) {
		// Add a CR to the end of each line
		fline=fline+"\r\n";
		// If we aren't logging don't try to do anything
		if (logging==false)
			return false;
		try {
			file.write(fline);
			file.flush();
		} catch (Exception e) {
			// Stop logging as we have a problem
			logging=false;
			System.out.println("\nError writing to the logging file");
			return false;
		}
		return true;
	}
	
	// Display the number of symbols since the last frame with a valid sync
	public String dispSymbolsSinceLastFrame ()	{
		// Don't display anything if 144 symbols since the last frame.
		if (symbolcnt!=144)	{
			String l=" (Symbols="+Integer.toString(symbolcnt)+")";
			return l;
		}
		else return "";
	}
	
	// Grab 5 seconds worth of audio and write to the file "audiodump_out.csv"
	public void audioDump ()	{
		long a;
		final long sample_max=48000*5;
		int samples[]=new int[48000*5];
		for (a=0;a<sample_max;a++)	{
			samples[(int)a]=lineInThread.returnSample();
		}	
	    try	{
	    	FileWriter dfile=new FileWriter("audiodump_out.csv");
			for (a=0;a<sample_max;a++)	{
				dfile.write(Integer.toString(samples[(int)a]));
				dfile.write("\r\n");
			}
	    	dfile.flush();  
	    	dfile.close();
	    	}catch (Exception e)	{
	    		System.err.println("Error: " + e.getMessage());
	    		}
	    // Saved everything so shut down the program
	    System.exit(0);
		}
	
	// Write a line to the debug file
	public void debugDump (String line)	{
	    try	{
	    	FileWriter dfile=new FileWriter("debug.csv",true);
	    	dfile.write(line);
	    	dfile.write("\r\n");
	    	dfile.flush();  
	    	dfile.close();
	    	}catch (Exception e)	{
	    		System.err.println("Error: " + e.getMessage());
	    		}
		}
		
	// Display the dibit buffer as a string
	public String displayDibitBuffer ()	{
		String lb="";
		int a;
		for (a=0;a<144;a++)	{
			lb=lb+Integer.toString(dibitFrame[a]);
		}
		return lb;
	}
	
	// Return a string showing the percentages of each dibit in the dibit buffer
	public String returnDibitBufferPercentages ()	{
		String dline;
		int a,c0=0,c1=0,c2=0,c3=0;
		for (a=0;a<144;a++)	{
			// Exclude the sync burst from the percentages 
			if ((a<66)||(a>89))	{
			if (dibitFrame[a]==0) c0++;
			if (dibitFrame[a]==1) c1++;
			if (dibitFrame[a]==2) c2++;
			if (dibitFrame[a]==3) c3++;
			}
		}
		c0=(int)(((float)c0/(float)120.0)*(float)100);
		c1=(int)(((float)c1/(float)120.0)*(float)100);
		c2=(int)(((float)c2/(float)120.0)*(float)100);
		c3=(int)(((float)c3/(float)120.0)*(float)100);
		// Write this to a line
		dline="Dibit 0="+Integer.toString(c0)+"% ";	
		dline=dline+"Dibit 1="+Integer.toString(c1)+"% ";	
		dline=dline+"Dibit 2="+Integer.toString(c2)+"% ";	
		dline=dline+"Dibit 3="+Integer.toString(c3)+"% ";	
		return dline;
	}
	
	// Open a file which contains data that can be sucked in
	public void prepareAudioSuck (String fn)	{
		try	{
			br=new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
		} catch (Exception e)	{
			e.printStackTrace();
			audioSuck=false;
		}
		audioSuck=true;
	}

	// Read in a line from the suck file and return the int it contains
	private int getSuckData ()	{
		int data=0;
		String line;
		try	{
			line=br.readLine();
			data=Integer.parseInt(line);
		} catch (Exception e)	{
			// We have a problem so stop sucking
			audioSuck=false;
		}
		return data;
	}

	public void setViewVoiceFrames(boolean viewVoiceFrames) {
		this.viewVoiceFrames=viewVoiceFrames;
	}

	public boolean isViewVoiceFrames() {
		return viewVoiceFrames;
	}
		
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug=debug;
	}

	public void setViewDataFrames(boolean viewDataFrames) {
		this.viewDataFrames = viewDataFrames;
	}

	public boolean isViewDataFrames() {
		return viewDataFrames;
	}

	public void setViewEmbeddedFrames(boolean viewEmbeddedFrames) {
		this.viewEmbeddedFrames = viewEmbeddedFrames;
	}

	public boolean isViewEmbeddedFrames() {
		return viewEmbeddedFrames;
	}
	
	// Put the dibits into dibitFrame in the correct order from the circular dibit buffer
	private void createDibitFrame()	{
		int i,circPos;
		circPos=dibitCircularBufferCounter-144;
		if (circPos<0) circPos=144+circPos;
		for (i=0;i<144;i++)	{
			dibitFrame[i]=dibitCircularBuffer[circPos];
			circPos++;
			if (circPos==144) circPos=0;
		}
	}
	
	
}
