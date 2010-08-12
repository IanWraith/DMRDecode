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

public class DMRDecode {
	private DisplayModel display_model;
	private DisplayView display_view;
	private static DMRDecode theApp;
	static DisplayFrame window;
	public String program_version="DMR Decoder V0.00 Build 0";
	public TargetDataLine Line;
	public AudioFormat format;
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private boolean audioReady=false;
	private static boolean RUNNING=true;
	private int have_sync=0;
	private int samplesPerSymbol=10;
	private int rf_mod=0;
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

	public static void main(String[] args) {
		theApp=new DMRDecode();
		SwingUtilities.invokeLater(new Runnable(){public void run(){theApp.createGUI();}});
		
		// Prepare the program //
		//theApp.prepare_variables();
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
		display_view=new DisplayView(this);
		display_model.addObserver(display_view);
		window.getContentPane().add(display_view,BorderLayout.CENTER);
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
			  format = new AudioFormat(48000, 16, 1, true, true);
			  DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			  Line = (TargetDataLine) AudioSystem.getLine(info);
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
		  int symbol;
		  symbol=getSymbol();
	
	  }
	  
	  // This code lifted straight from the DSD source code and needs a lot of tidying
	  public int getSymbol()	{
		  int sample,i,sum=0,symbol,count=0;
		  for (i = 0; i < samplesPerSymbol; i++)
		    {
		      // timing control
		      if ((i == 0) && (have_sync == 0))
		        {
		          if (rf_mod == 1)
		            {
		              if ((jitter >= 0) && (jitter < symbolCenter))
		                {
		                  i++;          // fall back
		                }
		              else if ((jitter > symbolCenter) && (jitter < 10))
		                {
		                  i--;          // catch up
		                }
		            }
		          else if (rf_mod == 2)
		            {
		              if ((jitter >= symbolCenter - 1) && (jitter <= symbolCenter))
		                {
		                  i--;
		                }
		              else if ((jitter >= symbolCenter + 1) && (jitter <= symbolCenter + 2))
		                {
		                  i++;
		                }
		            }
		          else if (rf_mod == 0)
		            {
		              if ((jitter > 0) && (jitter <= symbolCenter))
		                {
		                  i--;          // catch up
		                }
		              else if ((jitter > symbolCenter) && (jitter < samplesPerSymbol))
		                {
		                  i++;          // fall back
		                }
		            }
		          jitter = -1;
		        }

			      sample=getAudio();
		      
		      
		      if ((sample > max) && (have_sync == 1) && (rf_mod == 0))
		        {
		          sample = max;
		        }
		      else if ((sample < min) && (have_sync == 1) && (rf_mod == 0))
		        {
		          sample = min;
		        }

		      if (sample > center)
		        {
		          if (lastsample < center)
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
		              if ((symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("O");
		                }
		            }
		          else
		            {
		              if ((symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("+");
		                }
		              if ((jitter < 0) && (lastsample < center) && (rf_mod != 1))
		                {               // first transition edge
		                  jitter = i;
		                }
		            }
		        }
		      else
		        {                       // sample < 0
		          if (lastsample > center)
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
		              if ((symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("X");
		                }
		            }
		          else
		            {
		              if ((symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("-");
		                }
		              if ((jitter < 0) && (lastsample > center) && (rf_mod != 1))
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
		          if (((i >= symbolCenter - 1) && (i <= symbolCenter + 2) && (rf_mod == 0)) || (((i == symbolCenter) || (i == symbolCenter + 1)) && (rf_mod != 0)))
		            {
		              sum += sample;
		              count++;
		            }
		        }
		      lastsample = sample;
		    }
		  symbol = (sum / count);

		  if ((symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		    {
		      if (jitter >= 0)
		        {
		          //printf (" %i\n", jitter);
		        }
		      else
		        {
		          //printf ("\n");
		        }
		    }

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
		  sample=buffer[0];
		  return sample;
	  }
	  

}
