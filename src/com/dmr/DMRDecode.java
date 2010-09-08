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
	public String program_version="DMR Decoder V0.00 Build 1";
	public TargetDataLine Line;
	public AudioFormat format;
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private boolean audioReady=false;
	private static boolean RUNNING=true;
	private int have_sync=0;
	private final int samplesPerSymbol=10;
	private int jitter=-1;
	private int symbolCenter=4;
	private int max=15000;
	private int min=-15000;
	private int center=0;
	private int lastsample=0;
	private int numflips=0;
	private int maxref=12000;
	private int minref=-12000;
	private int lastsynctype=-1;
	private int symboltiming=0;
	private int symbolcnt=0;
	private int ssize=36;
	private int mod_threshold=26;
	private static final int DMR_DATA_SYNC[]={3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1};
	private static final int DMR_VOICE_SYNC[]={1,3,1,1,1,1,3,3,3,1,1,3,3,1,3,3,1,3,1,1,3,3,1,3};
	private int carrier=0;
	public boolean inverted_dmr=false;
	private boolean firstframe=false;
	public JEditorPane editorPane;
	public HTMLDocument doc;
	public Element el;
	private int lmid=0;
	private int umid=0;
	private int synctype;
	private int dibit_buf[]=new int[144];
	private boolean lastwithSync;
	
	public static void main(String[] args) {
		theApp=new DMRDecode();
		SwingUtilities.invokeLater(new Runnable(){public void run(){theApp.createGUI();}});
		theApp.prepare_audio();
		while (RUNNING)	{
			if (theApp.audioReady==true) theApp.decode();
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
	      center=((max)+(min))/2;
	      umid=(((max)-center)*5/8)+center;
	      lmid=(((min)-center)*5/8)+center;
	      while (synctype!=-1)	{
	          processFrame();
	          synctype=getFrameSync();
		      center=((max)+(min))/2;
		      umid=(((max)-center)*5/8)+center;
		      lmid=(((min)-center)*5/8)+center;    
	        }  
	  }
	  
	// This code lifted straight from the DSD source code converted to Java and tidied up removing non DMR code
	public int getSymbol()	{
		  int sample,i,sum=0,symbol,count=0;
		  for (i=0;i<samplesPerSymbol;i++)	{
		      // timing control
		      if ((i==0)&&(have_sync==0))	{
		        if ((jitter>0)&&(jitter<=symbolCenter)) i--;          // catch up  
		         else if ((jitter>symbolCenter)&&(jitter<samplesPerSymbol)) i++;          // fall back   
		        jitter=-1;
		       }
			  sample=getAudio();
			  if ((sample>max)&&(have_sync==1)) sample=max;  
			   else if ((sample<min)&&(have_sync==1)) sample=min;
		      if (sample>center)	{
		        if (lastsample<center) numflips+=1;
		        if (sample>(maxref*1.25))	{
		        	if (lastsample<(maxref*1.25)) numflips+=1;
		          }
		          else if ((jitter<0)&&(lastsample<center)) jitter=i;   
		        }
		      else	{                       
		        if (lastsample>center) numflips+=1;
		        if (sample<(minref*1.25))	{
		        	if (lastsample>(minref*1.25)) numflips+=1;
		            if (jitter<0) jitter=i;
		            }
		          else	{
		            if ((jitter<0)&&(lastsample>center)) jitter=i;   
		            }
		        } 
		      if (((i>=symbolCenter-1)&&(i<=symbolCenter+2)))	{
		    	  sum+=sample;
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
	  
	public int getFrameSync ()	{
	    // detects frame sync and returns frame type
	    // 10 = +DMR (non inverted signal data frame)
	    // 11 = -DMR (inverted signal voice frame)
	    // 12 = +DMR (non inverted signal voice frame)
	    // 13 = -DMR (inverted signal data frame)
		
	    int i,t,dibit,sync,symbol,synctest_pos,lastt;
	    int lmin, lmax, lidx;
	    int lbuf[]=new int[24]; 
	    int lbuf2[]=new int[24];
	    boolean dataSync=false;
	    boolean voiceSync=false;
	    Quicksort qsort=new Quicksort();

	    // detect frame sync
	    t=0;
	    synctest_pos=0;	   
	    sync=0;
	    lmin=0;
	    lmax=0;
	    lidx=0;
	    lastt=0;
	    numflips=0;
	    dibit_buf[55]=0;
	    dibit_buf[56]=0;
	    dibit_buf[57]=0;
	    
	    while (sync==0)	{
	        t++;
	        symbol=getSymbol();
	        
	        lbuf[lidx]=symbol;
	        
	        if (lidx==23) lidx=0;
	         else lidx++;
	        
	        if (lastt==23)	{
	            lastt=0;
	            numflips=0;
	          }
	        else lastt++;
	          

	        // Get the dibit state
	        if (lastsynctype==-1)	{
		     if (symbol>0) dibit=1;
	          else dibit=3;
	         }
	         else	{
	        	 if (symbol>center)	{
	        		 if (symbol>umid) dibit=1;
	        		  else dibit=0;
	        	 }
	        	 else	{
	        		 if (symbol<lmid) dibit=3;
	        		  else dibit=2;	        	
	        	 }
	         }
	        
	        
		    addToDitbitBuf(dibit);
		    
		    
	        if (t>=24) {
	            for (i=0;i<24;i++) {
	              lbuf2[i]=lbuf[i];
	              }
	            qsort.sort(lbuf2);
	            lmin=(lbuf2[2]+lbuf2[3]+lbuf2[4])/3;
	            lmax=(lbuf2[21]+lbuf2[20]+lbuf2[19])/3;
	            maxref=max;
	            minref=min;
	                    
	            dataSync=syncCompare(DMR_DATA_SYNC);
	            voiceSync=syncCompare(DMR_VOICE_SYNC);
	            
	            	if (dataSync==true)	{
	                    carrier=1;	                 
	                    lastwithSync=true;
	                    max=((max)+(lmax))/2;
	                    min=((min)+(lmin))/2;
	                    if (inverted_dmr==false)	{
	                        lastsynctype=10;
	                        return (10);
	                      }
	                    else	{
	                        if (lastsynctype!=11) firstframe=true;
	                        lastsynctype=11;
	                        return (11);
	                      }
	                  }
	                if (voiceSync==true)	{
	                    carrier=1;
	                    lastwithSync=true;
	                    max=((max)+lmax)/2;
	                    min=((min)+lmin)/2;
	                    if (inverted_dmr==false)	{
	                        if (lastsynctype!=12) firstframe=true;
	                        lastsynctype=12;
	                        return (12);
	                      }
	                    else	{
	                    	lastsynctype=13;
	                        return (13);
	                      }
	                  }
	              }

	        if ((synctest_pos==144)&&(lastsynctype!=-1))	{
	        	if ((lastsynctype==11)&&(voiceSync==false))	{
	        		carrier=1;
	                lastwithSync=false;
	                max=((max)+lmax)/2;
	                min=((min)+lmin)/2;
	                lastsynctype=-1;
	                return (11);
	               	}
	             else if ((lastsynctype==12)&&(dataSync==false))	{
	                carrier=1;
	                lastwithSync=false;
	                max=((max)+lmax)/2;
	                min=((min)+lmin)/2;
	                lastsynctype=-1;
	                return (12);
	                }
	              }
	          

	        if (t>12000)	{
	            // buffer reset
	            t=0;
	            noCarrier();
	        }
	        synctest_pos++;

	        if (carrier==1)	{
	            if (synctest_pos>=1800)	{
	                noCarrier();
	                return (-1);
	              }
	          }
	      
	    }
	        
	    return (-1);
	  }
	  
	// Add a dibit to the dibit buffer
	void addToDitbitBuf (int dibit)	{
		int a;
		// Rotate the dibit buffer to the left
		for (a=0;a<143;a++)	{
			dibit_buf[a]=dibit_buf[a+1];
		}
		dibit_buf[143]=dibit;
	}
	
	void noCarrier ()	{
		jitter=-1;
		lastsynctype=-1;
		carrier=0;
		max=15000;
		min=-15000;
		center=0;
		// TODO : Need to work out the purpose of firstframe
		firstframe=false;
	  	}
	  
	// Compare the sync held in an array with the contents of the dibit_buf
	public boolean syncCompare(int c[])	{
		int i;
		for (i=0;i<24;i++)	{
			if (dibit_buf[i+66]!=c[i]) return false;
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
	    maxref=max;
	    minref=min;
	    if ((synctype==11)||(synctype==12)) processDMRvoice ();
	     else processDMRdata ();
	    }

	// Handle a DMR Voice Frame
	void processDMRvoice ()	{	
		String l=getTimeStamp()+" DMR Voice Frame";
		if (lastwithSync==true) l=l+" (S)";
		addLine (l);
	}
	
	// Handle a DMR Data Frame
	void processDMRdata ()	{
		DMRDataDecode DMRdata=new DMRDataDecode();
		String line[]=new String[10];
		line=DMRdata.decode(getTimeStamp(),dibit_buf,inverted_dmr);
		if (lastwithSync==true) line[0]=line[0]+" (S)";
		displayLines(line);
	}

	// Display a group of lines
	void displayLines (String line[])	{
		int a;
		int len=line.length;
		for (a=(len-1);a>=0;a--)	{
			if (line[a]!=null) addLine(line[a]);
		}
	}
	
	// TODO : Need to add code to save decoded data to a file
	
}
