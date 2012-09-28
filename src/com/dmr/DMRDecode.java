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
import java.io.FileWriter;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.text.DateFormat;
import java.util.Date;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class DMRDecode {
	private DisplayModel display_model;
	private DisplayView display_view;
	private static DMRDecode theApp;
	private static DisplayFrame window;
	public String program_version="DMR Decoder (Build 56)";
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private static boolean RUNNING=true;
	private final int SAMPLESPERSYMBOL=10;
	private final int SYMBOLCENTRE=4;
	private final int MAXSTARTVALUE=15000;
	private final int MINSTARTVALUE=-15000;
	private int max=MAXSTARTVALUE;
	private int min=MINSTARTVALUE;
	private int centre=0;
	private int lastsynctype=-1;
	private int symbolcnt=0;
	private final byte DMR_DATA_SYNC[]={3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1};
	private final byte DMR_VOICE_SYNC[]={1,3,1,1,1,1,3,3,3,1,1,3,3,1,3,3,1,3,1,1,3,3,1,3};
	private boolean carrier=false;
	public boolean inverted=true;
	private boolean firstframe=false;
	private int lmid=0;
	private int umid=0;
	private int synctype;
	private byte dibitCircularBuffer[]=new byte[144];
	private int dibitCircularBufferCounter=0;
	private byte dibitFrame[]=new byte[144];
	private boolean frameSync=false;
	public FileWriter file;
	public FileWriter captureFile;
	private boolean logging=false;
	public boolean pReady=false;
	private int symbolBuffer[]=new int[144];
	public AudioInThread lineInThread=new AudioInThread(this);
	private boolean debug=false;
	public int frameCount=0;
	public int badFrameCount=0;
	public ShortLC short_lc=new ShortLC();
	public EmbeddedLC embedded_lc=new EmbeddedLC();
	public int embeddedFrameCount=0;
	private int symbolBufferCounter=0;
	private int errorFreeFrameCount=0;
	private int continousBadFrameCount=0;
	private boolean captureMode=false;
	private long captureCount=0;
	private boolean enableDisplayBar=false;
	private final int SYMBOLSAHEAD=144;
	private final int SAMPLESAHEADSIZE=(SYMBOLSAHEAD*SAMPLESPERSYMBOL)+SAMPLESPERSYMBOL;
	private int samplesAheadBuffer[]=new int[SAMPLESAHEADSIZE];
	private int samplesAheadCounter=0;
	private int jitter=-1;
	private DataInputStream inPipeData;
	private PipedInputStream inPipe;
	private int lastSample=0;
	private final int JITTERFRAMEADJUST=1;
	private final int JITTERCOUNTERSIZE=(JITTERFRAMEADJUST*144);
	private int jitterCounter=0;
	private int jitterBuffer[]=new int[JITTERCOUNTERSIZE];
	private int syncHighLowlBuf[]=new int[24];
	public UsersLogged usersLogged=new UsersLogged();
	private final int MAXMINBUFSIZE=5;
	private int maxminBufferCounter=0;
	private int maxBuffer[]=new int[MAXMINBUFSIZE];
	private int minBuffer[]=new int[MAXMINBUFSIZE];
	public final Font plainFont=new Font("SanSerif",Font.PLAIN,12);
	public final Font boldFont=new Font("SanSerif",Font.BOLD,12);
	public final Font italicFont=new Font("SanSerif",Font.ITALIC,12);
	public SocketOut socketThread=new SocketOut(this);
	public int currentChannel=0;
	private boolean displayCACH=true;
	private boolean displayIdlePDU=true;
	private boolean displayOnlyGoodFrames=false;
	private boolean displayVoiceFrames=true;
	public final Color labelBusyColour=Color.BLACK;
	public final Color labelQuiteColour=Color.GRAY;
	private boolean pauseScreen=false;
	private boolean quickLog=false;
	public FileWriter quickLogFile;
	private int colourCode=0;
	private ArrayList<Integer> incomingDataList=new ArrayList<Integer>();  
    private int socketThreadPriority=3;
    private int audioInputPriority=3;
    private int mainThreadPriority=5;
    
    private ExecutorService socketExecutor = Executors.newSingleThreadExecutor(
        new ThreadFactory(){
            public Thread newThread(Runnable r){
                Thread thread=new Thread(r);
                thread.setName("DMRDecode Socket Thread");
                thread.setPriority(socketThreadPriority);
                return thread;
            }
        }
    );

    private ExecutorService mainExecutor = Executors.newSingleThreadExecutor(
        new ThreadFactory(){
            public Thread newThread(Runnable r){
                Thread thread=new Thread(r);
                thread.setName("DMRDecode Main Thread");
                thread.setPriority(mainThreadPriority);
                return thread;
            }
        }
    );

    private ExecutorService audioInputExecutor = Executors.newSingleThreadExecutor(
        new ThreadFactory(){
            public Thread newThread(Runnable r){
                Thread thread=new Thread(r);
                thread.setName("DMRDecode Audio Input Thread");
                thread.setPriority(audioInputPriority);
                return thread;
            }
        }
    );

	
	public static void main(String[] args) {
		// Setup the TCP/IP socket code
		try	{
            theApp=new DMRDecode();
            SwingUtilities.invokeAndWait(new Runnable(){public void run(){theApp.createGUI();}});
            theApp.short_lc.setApp(theApp);
            theApp.socketExecutor.submit(theApp.socketThread);
            //this returns a boolean...
			theApp.socketThread.setupSocket();			
		} catch (Exception e)	{
			JOptionPane.showMessageDialog(null,"Error in socket setup during main()","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		// Get data from the soundcard thread
		try	{
			// Start the audio thread
			theApp.lineInThread.startAudio();
			// Connected a piped input stream to the piped output stream in the thread
			theApp.inPipe=new PipedInputStream(theApp.lineInThread.getPipedWriter(), 16384);
			// Now connect a data input stream to the piped input stream
			theApp.inPipeData=new DataInputStream(theApp.inPipe);
        }
		catch (Exception e)	{
			JOptionPane.showMessageDialog(null,"Error in main()","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
        }

        theApp.audioInputExecutor.submit(theApp.lineInThread);
        
        theApp.mainExecutor.submit(new Runnable(){
            public void run(){
                while (RUNNING)	{
                    if ((theApp.lineInThread.getAudioReady()==true)&&(theApp.pReady==true)) theApp.decode();
                }
            }
        });

    }
	
	// Setup the window //
	public void createGUI() {
		window=new DisplayFrame(program_version,this);
		Toolkit theKit=window.getToolkit();
		Dimension wndsize=theKit.getScreenSize();
		window.setBounds(wndsize.width/6,wndsize.height/6,2*wndsize.width/3,2*wndsize.height/3);
		window.addWindowListener(new WindowHandler());
		display_model=new DisplayModel();
		display_view=new DisplayView(this);
		display_model.addObserver(display_view);
		window.getContentPane().add(display_view,BorderLayout.CENTER);
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
	      while (synctype!=-1)	{
	          processFrame();
	          synctype=getFrameSync(); 
	          createDibitFrame();
	        }  
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
		centre=(max+min)/2;
		umid=(int)((float)(max-centre)*(float)0.625)+centre;
	    lmid=(int)((float)(min-centre)*(float)0.625)+centre;		
	    // If debug enabled then record this
		if (debug==true)	{
			String l=getTimeStamp()+" Setting new params : centre="+Integer.toString(centre)+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
			addLine(l,Color.BLACK,plainFont);
			fileWrite(l);
			}
	    
	    // Pass these settings to the display bar
        window.displayBarParams(max,min,umid,lmid);
	}
	
	// This code lifted straight from the DSD source code converted to Java 
	// and tidied up removing non DMR code
	public int getSymbol(boolean have_sync)	{
		  int sample,i,sum=0,symbol,count=0;
		  for (i=0;i<SAMPLESPERSYMBOL;i++)	{
			  // Fall back or catch up
			  if ((i==0)&&(jitter>0))	{
				  
				  if ((frameSync==true)&&(debug==true))	{
					  String l=getTimeStamp()+" jitter change to "+Integer.toString(jitter);
					  addLine(l,Color.BLACK,plainFont);
					  fileWrite(l);
				  }
				  
				  if ((jitter>0)&&(jitter<=SYMBOLCENTRE)) i--;          
				  else if ((jitter>SYMBOLCENTRE)&&(jitter<SAMPLESPERSYMBOL)) i++;
				  jitter=-1;
				  }
		      // Get the sample from whatever source
			  sample=getSample(false);	
			  // Jitter adjust code
			  // Is this sample greater than the centre ?
			  if (sample>centre)	{
				  	  // Was the last sample less than the centre ?
					  if (lastSample<centre)	{
						  // Yes we have a zero crossing
						  if (frameSync==false) jitter=i;
						  else processJitter(i);
					  }
			  }
			  else	{
				  	  // If this sample is less than the centre then
				      // was the last sample greater than the centre
					  if (lastSample>centre)	{
						  // Yes we have a zero crossing
						  if (frameSync==false) jitter=i;
						  else processJitter(i);
					  }
			  }
			  // Sample the symbol from its centre 
			  if ((i>=SYMBOLCENTRE)&&(i<=SYMBOLCENTRE+1))	{
			  		  sum=sum+sample;
					  count++;
				  }
		      // Make copy of this sample for later comparison
		      lastSample=sample;
		    }
		  symbol=(sum/count);
		  symbolcnt++;		  
		  return symbol;
	  }
	
	// Add the calculated jitter value to the jitter circular buffer
	private void processJitter (int jit)	{
		jitterBuffer[jitterCounter]=jit;
		jitterCounter++;
		if (jitterCounter==JITTERCOUNTERSIZE)	{
			jitterCounter=0;
			// Set the jitter to the mode value of the jitter buffer
			jitter=calcJitterMode();			
		}
	}
	  
	// Calculate which jitter value occurs the most (the mode) and return it 
	private int calcJitterMode()	{
		int a,b,high=0,highMode=0,tmode;
		for (a=0;a<SAMPLESPERSYMBOL;a++)	{
			tmode=0;
			for (b=0;b<JITTERCOUNTERSIZE;b++)	{
				if (jitterBuffer[b]==a) tmode++;
			}
			if (tmode>highMode)	{
				high=a;
				highMode=tmode;
			}
		}
		return high;
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
			addToSymbolBuffer(symbol);
			// If needed pass the data to the display bar
			if (enableDisplayBar==true) window.displaySymbol(symbol);
			// Set the dibit state
			dibit=symboltoDibit(symbol);
			// Add the dibit to the circular dibit buffer
			addToDitbitBuf(dibit);
		    // If we have received 144 dibits then we can check for a valid sync sequence
			if (t>=144) {
				// If we don't have frame sync then rotate the symbol buffer
				// and also find the new minimum and maximum
				if ((frameSync==false)||((frameSync==true)&&(symbolcnt%144==0)))	{
					// Get the frames 24 sync symbols
					syncHighLowlBuf=getSyncSymbols();
					lmin=1;
					lmax=-1;
					for (a=0;a<24;a++)	{
						if (syncHighLowlBuf[a]<lmin) lmin=syncHighLowlBuf[a];
						if (syncHighLowlBuf[a]>lmax) lmax=syncHighLowlBuf[a];
					}
				}
				// Update the volume bar every 25 frames
				if ((t%3600)==0)	{
					highVol=lineInThread.returnVolumeAverage();
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
						else addToMinMaxBuffer(lmin,lmax);
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
						else addToMinMaxBuffer(lmin,lmax);
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
					StringBuilder l=new StringBuilder(250);
					l.append(getTimeStamp()+" Sync Lost");
					l.append(" : centre="+Integer.toString(centre));
					l.append(" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid));
					addLine(l.toString(),Color.BLACK,plainFont);
					fileWrite(l.toString());
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
		continousBadFrameCount=0;
		// Update the status bar
		window.updateSyncLabel(false);
		window.setCh1Label("Unused",labelQuiteColour);
		window.setCh2Label("Unused",labelQuiteColour);
		window.SetColourCodeLabel(-1,labelQuiteColour);
		window.setSystemLabel("System : Unknown",labelQuiteColour);
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
			// Increment the circular buffer counter
			circPos++;
			if (circPos==144) circPos=0;
		}
		if ((DMR_VOICE_SYNC.length-voiceSync)<=diff) return 1;
		else if ((DMR_DATA_SYNC.length-dataSync)<=diff)	return 2;
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
	  
	// Adds a line to the display as long as pause isn't enabled
	public void addLine(final String line, final Color col, final Font font) {
		if (pauseScreen==true) return;
		else display_view.add_line(line,col,font);
	}

	// Return a time stamp
	public String getTimeStamp() {
		Date now=new Date();
		DateFormat df=DateFormat.getTimeInstance();
		return df.format(now);
	}	
	
	// Return a date stamp
	public String getDateStamp()	{
		Date now=new Date();
		DateFormat df=DateFormat.getDateInstance();
		return df.format(now);
	}
	
	// Handle an incoming DMR Frame
	void processFrame ()	{
		if (firstframe==true)	{
			// Clear the max min buffer counter so we don't use old values
			maxminBufferCounter=0;
			// If debug enabled record obtaining sync
			if (debug==true)	{
				StringBuilder l=new StringBuilder(250);
				if (synctype==12) l.append(getTimeStamp()+" DMR Voice Sync Acquired");
				else l.append(getTimeStamp()+" DMR Data Sync Acquired : centre="+Integer.toString(centre)+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid));
				addLine(l.toString(),Color.BLACK,plainFont);
				fileWrite(l.toString());
				}
			return;
	    }
	    // Update the sync label
	    window.updateSyncLabel(frameSync);
	    // Deal with the frame
	    if (synctype==12) processDMRvoice ();
	    else if (synctype==10) processDMRdata ();
	    else if (synctype==13) processEmbedded ();
	}

	// Handle a DMR Voice Frame
	void processDMRvoice ()	{	
		DMRVoice DMRvoice=new DMRVoice();
		Font font[]=new Font[10];
		Color lcol[]=new Color[10];
		String line[]=new String[10];
		line=DMRvoice.decode(theApp,dibitFrame);
		font=DMRvoice.getFonts();
		lcol=DMRvoice.getColours();
		frameCount++;
		if (DMRvoice.isError()==false)	{
			badFrameCount++;
			continousBadFrameCount++;
			line[0]=getTimeStamp()+" DMR Voice Frame - Error !";
			lcol[0]=Color.RED;
		}
		else	{
			continousBadFrameCount=0;
		}
		if (debug==true)	{
			line[0]=line[0]+dispSymbolsSinceLastFrame();
			lcol[8]=Color.BLACK;
			lcol[9]=Color.BLACK;
			font[8]=plainFont;
			font[9]=plainFont;
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		// If this frame contains errors and the user wants to display only good ones
		// then stop them being shown
		if ((DMRvoice.isError()==false)&&(displayOnlyGoodFrames==true)) DMRvoice.setShouldDisplay(false);
		// Display the info
		if ((DMRvoice.getShouldDisplay()==true)&&(displayVoiceFrames==true)) displayLines(line,lcol,font);
	}
	
	// Handle a DMR Data Frame
	void processDMRdata ()	{
		DMRDataDecode DMRdata=new DMRDataDecode();
		Font font[]=new Font[10];
		Color lcol[]=new Color[10];
		String line[]=new String[10];
		line=DMRdata.decode(theApp,dibitFrame);
		font=DMRdata.getFonts();
		lcol=DMRdata.getColours();
		frameCount++;
		if (DMRdata.isError()==false)	{
			badFrameCount++;
			line[0]=getTimeStamp()+" DMR Data Frame - Error !";
			lcol[0]=Color.RED;
			font[0]=plainFont;
			line[2]=null;
			// Record that there has been a frame with an error
			errorFreeFrameCount=0;
			continousBadFrameCount++;
		}
		else	{
			// Record that there has been an error free frame
			errorFreeFrameCount++;
		}
		if (debug==true)	{
			line[0]=line[0]+dispSymbolsSinceLastFrame();
			lcol[8]=Color.BLACK;
			lcol[9]=Color.BLACK;
			font[8]=plainFont;
			font[9]=plainFont;
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		// If this frame contains errors and the user wants to display only good ones
		// then stop them being shown
		if ((DMRdata.isError()==false)&&(displayOnlyGoodFrames==true)) DMRdata.setShouldDisplay(false);
		// Display the info
		if (DMRdata.getShouldDisplay()==true) displayLines(line,lcol,font);
	}
	
	// Handle an embedded frame
	void processEmbedded ()	{
		DMREmbedded DMRembedded=new DMREmbedded();
		Color lcol[]=new Color[10];
		Font font[]=new Font[10];
		String line[]=new String[10];
		line=DMRembedded.decode(theApp,dibitFrame);
		font=DMRembedded.getFonts();
		lcol=DMRembedded.getColours();
		frameCount++;
		if (DMRembedded.isError()==false)	{
			badFrameCount++;
			line[0]=getTimeStamp()+" DMR Embedded Frame - Error !";
			lcol[0]=Color.RED;
			font[0]=plainFont;
			line[2]=null;
			// Record that there has been a frame with an error
			errorFreeFrameCount=0;
			continousBadFrameCount++;
		}
		else	{
			// Set last sync type to 14 to show this was a good embedded frame
			lastsynctype=14;
			continousBadFrameCount=0;
		}
		if (debug==true)	{
			line[0]=line[0]+dispSymbolsSinceLastFrame();
			lcol[8]=Color.BLACK;
			lcol[9]=Color.BLACK;
			font[8]=plainFont;
			font[9]=plainFont;
			line[8]=returnDibitBufferPercentages();
			line[9]=displayDibitBuffer();
		}
		// If this frame contains errors and the user wants to display only good ones
		// then stop them being shown
		if ((DMRembedded.isError()==false)&&(displayOnlyGoodFrames==true)) DMRembedded.setShouldDisplay(false);
		// Display the info
		if (DMRembedded.getShouldDisplay()==true) displayLines(line,lcol,font);
	}

	// Display a group of lines
	void displayLines (String line[],Color col[],Font font[])	{
		int a;
		int len=line.length;
		for (a=(len-1);a>=0;a--)	{
			if (line[a]!=null) addLine(line[a],col[a],font[a]);
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
	
	// Make up a string for the quick log file
	public void quickLogData(String line,int a,int b,int c,String extra)	{
		String tline=getDateStamp()+","+getTimeStamp()+","+Integer.toString(colourCode)+","+line+","+Integer.toString(a)+","+Integer.toString(b)+","+Integer.toString(c)+","+extra;
		quickLogWrite(tline);
	}
	
	// Write to a string to the logging file
	private boolean quickLogWrite(String fline) {
		// Add a CR to the end of each line
		fline=fline+"\r\n";
		// If we aren't logging don't try to do anything
		if (quickLog==false)
			return false;
		try {
			quickLogFile.write(fline);
			quickLogFile.flush();
		} catch (Exception e) {
			// Stop logging as we have a problem
			quickLog=false;
			System.out.println("\nError writing to the quick log file");
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
	
	// Grab a sample and write it to the capture file
	public void audioDump (int sample)	{
		try	{
			captureFile.write("\r\n");	
			captureFile.write(Integer.toString(sample));
			}
		catch (Exception e)	{
			System.err.println("Error: " + e.getMessage());
			captureMode=false;
		}
		captureCount++;
		if (captureCount>48000)	{
			closeCaptureFile();
			captureMode=false;
		}
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
		StringBuilder lb=new StringBuilder(500);
		int a;
		for (a=0;a<144;a++)	{
			lb.append(Integer.toString(dibitFrame[a]));
		}
		return lb.toString();
	}
	
	// Return a string showing the percentages of each dibit in the dibit buffer
	public String returnDibitBufferPercentages ()	{
		StringBuilder dline=new StringBuilder(500);
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
		dline.append("Dibit 0="+Integer.toString(c0)+"% ");	
		dline.append("Dibit 1="+Integer.toString(c1)+"% ");	
		dline.append("Dibit 2="+Integer.toString(c2)+"% ");	
		dline.append("Dibit 3="+Integer.toString(c3)+"% ");	
		return dline.toString();
	}
		
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug=debug;
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
	
	// Set the audio capture mode
	public void setCapture (boolean c)	{
		if ((captureMode==false)&&(c==true))	{
			openCaptureFile();
			captureCount=0;
		}
		else if ((captureMode==true)&&(c==false))	{
			closeCaptureFile();
			captureMode=false;
		}
	}
	
	// Tell the program if it is in audio capture mode
	public boolean isCapture (){
		return captureMode;
	}
	
	// Open the capture file
	private void openCaptureFile()	{
		try	{
			captureFile=new FileWriter("capture_dump.csv");
			captureMode=true;
		}
		catch (Exception e)	{
			captureMode=false;
		}
	}
	
	// Close the capture file
	private void closeCaptureFile()	{
		try	{
			captureFile.flush();
			captureFile.close();
		}
		catch (Exception e)	{
			JOptionPane.showMessageDialog(null,"Error closing the capture file","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
		}
		// We aren't in capture mode any longer
		captureMode=false;
	}

	
	// Enable or disable the symbol display bar
	public void setEnableDisplayBar(boolean enableDisplayBar) {
		this.enableDisplayBar=enableDisplayBar;
		window.switchDisplayBar(this.enableDisplayBar);
	}

	// Tell other classes if the symbol display bar is enabled or disabled 
	public boolean isEnableDisplayBar() {
		return enableDisplayBar;
	}
	
	// Add a sample to the samples ahead buffer
	private void addToSamplesAheadBuffer (int sam)	{
		samplesAheadBuffer[samplesAheadCounter]=sam;
		samplesAheadCounter++;
		if (samplesAheadCounter==SAMPLESAHEADSIZE) samplesAheadCounter=0;
	}
	
	// Get the oldest sample from the samples ahead buffer
	private int getOldestSample()	{
		return samplesAheadBuffer[samplesAheadCounter];
	}
	
	// Get a sample either from the sound card 
	private int getSample (boolean jitmode)	{
		int sample=0;
		// Get the sample from the sound card via the sound thread
		try	{
			sample=inPipeData.readInt();
			}
		catch (Exception e)	{
			JOptionPane.showMessageDialog(null,"Error in getSample()","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
			}
		// If in capture mode record the sample in the capture file
		// but don't do this in jitter adjust mode
		if ((captureMode==true)&&(jitmode==false)) audioDump(sample);
		// Add this to the circular samples ahead buffer
		addToSamplesAheadBuffer(sample);
		// Pull the oldest sample from the circular samples ahead buffer
		return getOldestSample();
	}
	
	// The the max and min values to a circular buffer of values
	private void addToMinMaxBuffer (int tmin,int tmax)	{
		maxBuffer[maxminBufferCounter]=tmax;
		minBuffer[maxminBufferCounter]=tmin;
		maxminBufferCounter++;
		// When the buffer reaches its maximum size use calculate new parameters
		if (maxminBufferCounter==MAXMINBUFSIZE)	{
			maxminBufferCounter=0;
			calcAverageMinMax();
		}
	}
	
	// Calculate new min and max parameters as averages from the circular buffer
	private void calcAverageMinMax()	{
		int a,totalmax=0,totalmin=0;
		for (a=0;a<MAXMINBUFSIZE;a++)	{
			totalmax=totalmax+maxBuffer[a];
			totalmin=totalmin+minBuffer[a];
		}
		totalmax=totalmax/MAXMINBUFSIZE;
		totalmin=totalmin/MAXMINBUFSIZE;
		jitterCounter=0;
		frameCalcs(totalmin,totalmax);
	}

	public void setDisplayCACH(boolean displayCACH) {
		this.displayCACH = displayCACH;
	}

	public boolean isDisplayCACH() {
		return displayCACH;
	}

	public void setDisplayIdlePDU(boolean displayIdlePDU) {
		this.displayIdlePDU = displayIdlePDU;
	}

	public boolean isDisplayIdlePDU() {
		return displayIdlePDU;
	}

	public void setDisplayOnlyGoodFrames(boolean displayOnlyGoodFrames) {
		this.displayOnlyGoodFrames = displayOnlyGoodFrames;
	}

	public boolean isDisplayOnlyGoodFrames() {
		return displayOnlyGoodFrames;
	}
	
	public void setCh1Label (String label,Color col)	{
		window.setCh1Label(label,col);
	}
	
	public void setCh2Label (String label,Color col)	{
		window.setCh2Label(label,col);
	}
	
	public boolean getLogging()	{
		return logging;
	}
	
	public void setLogging (boolean log)	{
		logging=log;
	}

	public void setPauseScreen(boolean pauseScreen) {
		this.pauseScreen=pauseScreen;
	}

	public boolean isPauseScreen() {
		return pauseScreen;
	}

	public void setQuickLog(boolean quickLog) {
		this.quickLog = quickLog;
	}

	public boolean isQuickLog() {
		return quickLog;
	}

	public void setColourCode(int cc) {
		this.colourCode=cc;
		window.SetColourCodeLabel(cc,labelBusyColour);
	}
	
	public void setSystemLabel(String txt)	{
		window.setSystemLabel(txt,labelBusyColour);
	}

	public int getColourCode() {
		return colourCode;
	}
	
	public void clearScreen()	{
		display_view.clearScreen();
	}
	
	// Gets all the text on the screen and returns it as a string
	public String getAllText()	{
		return	display_view.getText();
	}
	
	// Clear the data list
	public void clearIncomingDataList()	{
		incomingDataList.clear();
	}
	
	// Add an int to the data list
	public void addToIncomingDataList (int in)	{
		incomingDataList.add(in);
	}
	
	// Return the list length
	public int incomingDataListLengh()	{
		return incomingDataList.size();
	}
	
	// Return a copy of the list
	public ArrayList<Integer> getIncomingDataList()	{
		return incomingDataList;
	}
	
	// Save the current settings as DMRDecode_settings.xml
	public boolean saveCurrentSettings ()	{
		FileWriter xmlfile;
		String line;
		// Open the default file settings //
		try {
			xmlfile=new FileWriter("DMRDecode_settings.xml");
			// Start the XML file //
			line="<?xml version='1.0' encoding='utf-8' standalone='yes'?><settings>";
			xmlfile.write(line);
			// Debug mode
			line="<debug val='";
			if (debug==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);
			// Invert
			line="<invert val='";
			if (inverted==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);		
			// Enable Symbol Display
			line="<symbolDisplay val='";
			if (enableDisplayBar==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);			
			// Display CACH
			line="<displayCACH val='";
			if (displayCACH==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);
			// Only display good frames
			line="<goodFramesOnly val='";
			if (displayOnlyGoodFrames==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);
			// Display IDLE PDUs
			line="<idlePDU val='";
			if (displayIdlePDU==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);	
			// Display Voice Frames
			line="<voiceFrames val='";
			if (displayVoiceFrames==true) line=line+"TRUE";
			else line=line+"FALSE";
			line=line+"'/>";
			xmlfile.write(line);	
			// All done so close the root item //
			line="</settings>";
			xmlfile.write(line);
			// Flush and close the file //
			xmlfile.flush();
			xmlfile.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,"Error : Unable to create the file DMRDecode_settings.xml\n"+e.toString(),"Rivet", JOptionPane.ERROR_MESSAGE);
				return false;
			}	
		return true;
	}
	
	// Read in the DMRDecode_settings.xml file //
	public void readDefaultSettings() throws SAXException, IOException,ParserConfigurationException {
			// Create a parser factory and use it to create a parser
			SAXParserFactory parserFactory=SAXParserFactory.newInstance();
			SAXParser parser=parserFactory.newSAXParser();
			// This is the name of the file you're parsing
			String filename="DMRDecode_settings.xml";
			// Instantiate a DefaultHandler subclass to handle events
			saxHandler handler=new saxHandler();
			// Start the parser. It reads the file and calls methods of the handler.
			parser.parse(new File(filename),handler);
		}
	
	public boolean isDisplayVoiceFrames() {
		return displayVoiceFrames;
	}

	public void setDisplayVoiceFrames(boolean displayVoiceFrames) {
		this.displayVoiceFrames = displayVoiceFrames;
	}

	// This class handles the SAX events
	public class saxHandler extends DefaultHandler {
			String value;
			
			public void endElement(String namespaceURI,String localName,String qName) throws SAXException {	
			}

			public void characters(char[] ch,int start,int length) throws SAXException {
				// Extract the element value as a string //
				String tval=new String(ch);
				value=tval.substring(start,(start+length));
			}
			
			// Handle an XML start element //
			public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {
				// Check an element has a value //
				if (attributes.getLength()>0) {
					// Get the elements value //
					String aval=attributes.getValue(0);
					// Debug mode //
					if (qName.equals("debug")) {
						if (aval.equals("TRUE")) setDebug(true);
						else setDebug(false);	
					}
					// Invert
					if (qName.equals("invert")) {
						if (aval.equals("TRUE")) inverted=true;
						else inverted=false;	
					}			
					// Symbol display
					if (qName.equals("symbolDisplay")) {
						if (aval.equals("TRUE")) enableDisplayBar=true;
						else enableDisplayBar=false;	
					}
					// Display CACH
					if (qName.equals("displayCACH")) {
						if (aval.equals("TRUE")) displayCACH=true;
						else displayCACH=false;	
					}					
					// Display only good frames
					if (qName.equals("goodFramesOnly")) {
						if (aval.equals("TRUE")) displayOnlyGoodFrames=true;
						else displayOnlyGoodFrames=false;	
					}					
					// Display Idle PDUs
					if (qName.equals("idlePDU")) {
						if (aval.equals("TRUE")) displayIdlePDU=true;
						else displayIdlePDU=false;	
					}	
					// Display Voice Frames
					if (qName.equals("voiceFrames")) {
						if (aval.equals("TRUE")) displayVoiceFrames=true;
						else displayVoiceFrames=false;	
					}	
				}	
			}
		}
	
	

}
