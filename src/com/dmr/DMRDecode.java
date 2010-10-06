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

import javax.sound.sampled.*;
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
	public String program_version="DMR Decoder V0.00 Build 3";
	public TargetDataLine Line;
	public AudioFormat format;
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private boolean audioReady=false;
	private static boolean RUNNING=true;
	private final int samplesPerSymbol=10;
	private int jitter=-1;
	private final int symbolCentre=4;
	private int max=15000;
	private int min=-15000;
	private int centre=0;
	private int lastsample=0;
	private int maxref=12000;
	private int minref=-12000;
	private int lastsynctype=-1;
	private int symbolcnt=0;
	private static final int DMR_DATA_SYNC[]={3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1};
	private static final int DMR_VOICE_SYNC[]={1,3,1,1,1,1,3,3,3,1,1,3,3,1,3,3,1,3,1,1,3,3,1,3};
	private boolean carrier=false;
	public boolean inverted=true;
	private boolean firstframe=false;
	public JEditorPane editorPane;
	public HTMLDocument doc;
	public Element el;
	private int lmid=0;
	private int umid=0;
	private int synctype;
	private int dibit_buf[]=new int[144];
	private boolean frameSync=false;
	public boolean saveToFile=false;
	public FileWriter file;
	public boolean logging=false;
	public boolean pReady=false;
	private boolean audioSuck=false;
	private boolean debug=false;
	private BufferedReader br;
	private int symbolBuffer[]=new int[24];
	
	
	public static void main(String[] args) {
		theApp=new DMRDecode();
		SwingUtilities.invokeLater(new Runnable(){public void run(){theApp.createGUI();}});
		theApp.prepare_audio();
		// If sucking in test data then open the file
		if (theApp.audioSuck==true) theApp.prepareAudioSuck("audiodump_test.csv");
		// The main routine
		while (RUNNING)	{
			if ((theApp.audioReady==true)&&(theApp.pReady==true)) theApp.decode();
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
	

	// Setup the audio interface
	public void prepare_audio() {
		  try {
			  // Sample at 48000 Hz , 16 bit samples , 1 channel , signed with bigendian numbers
			  format=new AudioFormat(48000,16,1,true,true);
			  DataLine.Info info=new DataLine.Info(TargetDataLine.class,format);
			  Line=(TargetDataLine) AudioSystem.getLine(info);
			  Line.open(format);
			  Line.start();
			  audioReady=true;
		  } catch (Exception e) {
			  JOptionPane.showMessageDialog(null, "Fatal error in prepare_audio","DMRdecoder", JOptionPane.ERROR_MESSAGE);
			  System.exit(0);
	   		}
	   	}
	  
	// The main routine for decoding DMR data
	public void decode()	{
		  noCarrier();
		  synctype=getFrameSync();
		  calcMids();
	      while (synctype!=-1)	{
	          processFrame();
	          synctype=getFrameSync(); 
	          if (synctype!=-1) calcMids();
	        }  
	  }
	
	// Calculate the waveform centre and mid points
	public void calcMids()	{
			centre=(max+min)/2;
			umid=((max-centre)*5/8)+centre;
		    lmid=((min-centre)*5/8)+centre;		
	}
	
	
	// This code lifted straight from the DSD source code converted to Java and tidied up removing non DMR code
	public int getSymbol(boolean have_sync)	{
		  int sample,i,sum=0,symbol,count=0;
		  for (i=0;i<samplesPerSymbol;i++)	{
		      if ((i==0)&&(have_sync==false))	{
		        if ((jitter>0)&&(jitter<=symbolCentre)) i--;          
		         else if ((jitter>symbolCentre)&&(jitter<samplesPerSymbol)) i++;          
		        jitter=-1;
		       }
			  if (audioSuck==false) sample=getAudio();
			   else sample=getSuckData();
			  if ((sample>max)&&(have_sync==true)) sample=max;  
			    else if ((sample<min)&&(have_sync==true)) sample=min;
		      if (sample>centre)	{
		    	  if ((jitter<0)&&(lastsample<centre)&&(sample<(maxref*1.25))) jitter=i;   
		        }
		      else if ((sample>(minref*1.25))&&(jitter<0)&&(lastsample>centre)) jitter=i;
      
		      if ((i>=symbolCentre-1)&&(i<=symbolCentre+2)) {
		    	  sum=sum+sample;
		          count++;
		          }
		      lastsample=sample;
		    }
		  symbol=(sum/count);
		  symbolcnt++;		  
		  return symbol;
	  }
	  
	  // Grab and return a single byte from the audio line
	  public int getAudio ()	{
		  int sample,count,total=0;
		  byte buffer[]=new byte[2];
		  try	{
			  while (total<1)	{
				  count=Line.read(buffer,0,2);
				  total=total+count;
			  	}
			  } catch (Exception e)	{
			  String err=e.getMessage();
			  JOptionPane.showMessageDialog(null,err,"DMRDecode", JOptionPane.ERROR_MESSAGE);
		  }
		  sample=(buffer[0]<<8)+buffer[1];
		  return sample;
	  }
	  
	// Grab either 24 or 144 dibits depending on if you have sync
	// Check if they have a sync pattern and if they do then process them accordingly
	public int getFrameSync ()	{
		int i,t=0,dibit,symbol,synctest_pos=0;
		int lmin=0,lmax=0;
		int lbufCount;
		boolean dataSync=false,voiceSync=false;
		Quicksort qsort=new Quicksort();
		// Clear the symbol counter
		symbolcnt=0;
		// Buffer size
		if (frameSync==true) lbufCount=144;
		 else lbufCount=23;
		int lbuf2[]=new int[24];
		
		while (true) {
			t++;
			symbol=getSymbol(frameSync);
			// Store this in the rotating symbol buffer
			addToSymbolBuffer(symbol);
			// Set the dibit state
			dibit=symboltoDibit(symbol);
			// Add the dibit to the rotating dibit buffer
			addToDitbitBuf(dibit,frameSync);
		    // If we have received either 24 or 144 dibits (depending if we have sync)
			// then check for a valid sync sequence
			if (t>=lbufCount) {
				
				if (frameSync==false)	{
					for (i=0;i<24;i++) {
						lbuf2[i]=symbolBuffer[i];
					}
					qsort.sort(lbuf2);
					lmin=(lbuf2[2]+lbuf2[3]+lbuf2[4])/3;
					lmax=(lbuf2[18]+lbuf2[19]+lbuf2[20])/3;
					maxref=max;
					minref=min;
				} 

				// Check if this has a valid voice or data frame sync
				dataSync=syncCompare(DMR_DATA_SYNC,frameSync);
				voiceSync=syncCompare(DMR_VOICE_SYNC,frameSync);
				
				// Data frame
				if (dataSync==true) {
					carrier=true;
					frameSync=true;
					max=(max+lmax)/2;
					min=(min+lmin)/2;
					if (lastsynctype==-1) firstframe=true;
					 else firstframe=false;
					lastsynctype=10;
					return (10);
				}
				// Voice frame
				if (voiceSync==true) {
					carrier=true;
					frameSync=true;
					max=(max+lmax)/2;
					min=(min+lmin)/2;
					if (lastsynctype==-1) firstframe=true;
					 else firstframe=false;
					lastsynctype=12;
					return (12);
				}
		}					
		// We had a signal but appear to have lost it
		if (carrier==true) {
			// If we have missed 12 frames then something is wrong
			if (synctest_pos>=1728) {
				int level=(int)(((float)max/(float)32768)*(float)100);
				String l=getTimeStamp()+" Sync Lost";
		    	l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter)+" level="+Integer.toString(level)+"%";
				l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
		    	addLine(l);
				fileWrite(l);
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
	  
	// Add a dibit to the dibit buffer
	void addToDitbitBuf (int dibit,boolean sync)	{
		int a;
		int max;
		if (sync==false) max=23;
		 else max=143;
		// Rotate the dibit buffer to the left
		for (a=0;a<max;a++)	{
			dibit_buf[a]=dibit_buf[a+1];
		}
		dibit_buf[max]=dibit;
	}
	
	// Add a symbol to the symbol buffer
	void addToSymbolBuffer (int symbol)	{
		int a;
		for (a=0;a<23;a++)	{
			symbolBuffer[a]=symbolBuffer[a+1];
		}
		symbolBuffer[23]=symbol;
	}
	
	// No carrier or carrier lost so clear the variables
	void noCarrier ()	{
		jitter=-1;
		lastsynctype=-1;
		carrier=false;
		max=15000;
		min=-15000;
		centre=0;
		firstframe=false;
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
	  
	// Compare the sync sequence held in an array with the contents of the dibit_buf
	public boolean syncCompare(int c[],boolean sync)	{
		int i;
		int offset;
		if (sync==true) offset=66;
		 else offset=0;
		for (i=0;i<24;i++)	{
			if (dibit_buf[i+offset]!=c[i]) return false;
		}
		return true;
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
	    	int level=(int)(((float)max/(float)32768)*(float)100);
			// As we now have sync then skip the next 77 dibits as we can't do anything with them
			skipDibit(77);
	    	if (synctype==12) l=getTimeStamp()+" DMR Voice Sync Acquired";
	    	 else l=getTimeStamp()+" DMR Data Sync Acquired";
	    	l=l+" : centre="+Integer.toString(centre)+" jitter="+Integer.toString(jitter)+" level="+Integer.toString(level)+"%";
			l=l+" max="+Integer.toString(max)+" min="+Integer.toString(min)+" umid="+Integer.toString(umid)+" lmid="+Integer.toString(lmid);
	    	addLine(l);
			fileWrite(l);
			return;
	    }
	    if (synctype==12) processDMRvoice ();
	     else processDMRdata ();
	    }

	// Handle a DMR Voice Frame
	void processDMRvoice ()	{	
		String line[]=new String[10];
		line[0]=getTimeStamp()+" DMR Voice Frame";
		line[0]=line[0]+dispSymbolsSinceLastFrame();
		line[9]=displayDibitBuffer();
		displayLines(line);
	}
	
	// Handle a DMR Data Frame
	void processDMRdata ()	{
		DMRDataDecode DMRdata=new DMRDataDecode();
		String line[]=new String[10];
		line=DMRdata.decode(getTimeStamp(),dibit_buf,inverted);
		if (frameSync==true) line[0]=line[0]+" (Sync)";
		line[0]=line[0]+dispSymbolsSinceLastFrame();
		line[9]=displayDibitBuffer();
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
		String l=" (Symbols="+Integer.toString(symbolcnt)+")";
		return l;
	}
	
	// Grab 5 seconds worth of audio and write to the file "audiodump_out.csv"
	public void audioDump ()	{
		long a;
		final long sample_max=48000*5;
		int samples[]=new int[48000*5];
		for (a=0;a<sample_max;a++)	{
			if (audioSuck==false) samples[(int)a]=getAudio();
			   else samples[(int)a]=getSuckData();
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
		
	// Open a file which contains data that can be sucked in
	public void prepareAudioSuck (String fn)	{
		try	{
			br=new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
		} catch (Exception e)	{
			e.printStackTrace();
			audioSuck=false;
		}
	}
	
	// Read in a line from the suck file and return the int it contains
	public int getSuckData ()	{
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
	
	// Display the dibit buffer as a string
	public String displayDibitBuffer ()	{
		String lb="";
		int a;
		for (a=0;a<144;a++)	{
			lb=lb+Integer.toString(dibit_buf[a]);
		}
		return lb;
	}
	
	// Grab a certain number of symbols but ignore their content
	public void skipDibit (int count)
	{
	  int i;
	  for (i=0;i<count;i++)
	    {
		getSymbol(true);
	    }
	}
	
	// The original DSD getSymbol in here to help me debug
	int ORIGINAL_getSymbol (boolean have_sync)
	{

	  int sample;
	  int i, sum, symbol, count;
	  int rf_mod=0,numflips=0;
	  int symboltiming=0;
	  
	  sum = 0;
	  count = 0;
	  for (i = 0; i < samplesPerSymbol; i++)
	    {
	      // timing control
	      if ((i == 0) && (have_sync == false))
	        {
	          if (rf_mod == 1)
	            {
	              if ((jitter >= 0) && (jitter < symbolCentre))
	                {
	                  i++;          // fall back
	                }
	              else if ((jitter > symbolCentre) && (jitter < 10))
	                {
	                  i--;          // catch up
	                }
	            }
	          else if (rf_mod == 2)
	            {
	              if ((jitter >= symbolCentre - 1) && (jitter <= symbolCentre))
	                {
	                  i--;
	                }
	              else if ((jitter >= symbolCentre + 1) && (jitter <= symbolCentre + 2))
	                {
	                  i++;
	                }
	            }
	          else if (rf_mod == 0)
	            {
	              if ((jitter > 0) && (jitter <= symbolCentre))
	                {
	                  i--;          // catch up
	                }
	              else if ((jitter > symbolCentre) && (jitter < samplesPerSymbol))
	                {
	                  i++;          // fall back
	                }
	            }
	          jitter = -1;
	        }

	      //result = read (audio_in_fd, &sample, 2);
		  if (audioSuck==false) sample=getAudio();
		   else sample=getSuckData();
	      
	      if ((sample > max) && (have_sync == true) && (rf_mod == 0))
	        {
	          sample = max;
	        }
	      else if ((sample < min) && (have_sync == true) && (rf_mod == 0))
	        {
	          sample = min;
	        }

	      if (sample > centre)
	        {
	          if (lastsample < centre)
	            {
	              numflips += 1;
	            }
	          if (sample > (maxref * 1.25))
	            {
	              if (lastsample < (maxref * 1.25))
	                {
	                  numflips += 1;
	                }
	              if ((jitter < 0) && (rf_mod == 1))
	                {               // first spike out of place
	                  jitter = i;
	                }
	              if ((symboltiming == 1) && (have_sync == false) && (lastsynctype != -1))
	                {
	            	  System.out.printf ("O");
	                }
	            }
	          else
	            {
	              if ((symboltiming == 1) && (have_sync == false) && (lastsynctype != -1))
	                {
	            	  System.out.printf ("+");
	                }
	              if ((jitter < 0) && (lastsample < centre) && (rf_mod != 1))
	                {               // first transition edge
	                  jitter = i;
	                }
	            }
	        }
	      else
	        {                       // sample < 0
	          if (lastsample > centre)
	            {
	              numflips += 1;
	            }
	          if (sample < (minref * 1.25))
	            {
	              if (lastsample > (minref * 1.25))
	                {
	                  numflips += 1;
	                }
	              if ((jitter < 0) && (rf_mod == 1))
	                {               // first spike out of place
	                  jitter = i;
	                }
	              if ((symboltiming == 1) && (have_sync == false) && (lastsynctype != -1))
	                {
	            	  System.out.printf ("X");
	                }
	            }
	          else
	            {
	              if ((symboltiming == 1) && (have_sync == false) && (lastsynctype != -1))
	                {
	            	  System.out.printf ("-");
	                }
	              if ((jitter < 0) && (lastsample > centre) && (rf_mod != 1))
	                {               // first transition edge
	                  jitter = i;
	                }
	            }
	        }
	      if (samplesPerSymbol == 5)
	        {
	          if ((i >= 2) && (i <= 2))
	            {
	              sum += sample;
	              count++;
	            }
	        }
	      else
	        {
	          if (((i >= symbolCentre - 1) && (i <= symbolCentre + 2) && (rf_mod == 0)) || (((i == symbolCentre) || (i == symbolCentre + 1)) && (rf_mod != 0)))
	            {
	              sum += sample;
	              count++;
	            }
	        }
	      lastsample = sample;
	    }
	  symbol = (sum / count);

	  if ((symboltiming == 1) && (have_sync == false) && (lastsynctype != -1))
	    {
	      if (jitter >= 0)
	        {
	    	  //System.out.printf (" %i\n", jitter);
	        }
	      else
	        {
	    	  System.out.printf ("\n");
	        }
	    }

	  symbolcnt++;
	  return (symbol);
	}
	
	
}
