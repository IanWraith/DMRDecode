package com.dmr;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.FileWriter;
import java.util.Scanner;
import javax.swing.*;
import java.text.DecimalFormat;

// Test comment

public class DMRDecode {
	private DisplayModel display_model;
	private DisplayView display_view;
	private static DMRdecode theApp;
	static DisplayFrame window;
	public String program_version="DMR Decoder V0.00 Build 1 - Ian Wraith 2010";
	public TargetDataLine Line;
	public AudioFormat format;
	public int vertical_scrollbar_value=0;
	public int horizontal_scrollbar_value=0;
	private boolean audioReady=false;
	private static boolean RUNNING=true;
	private int have_sync=0;
	private int samplesPerSymbol=10;

	public static void main(String[] args) {
		theApp=new DMRdecode();
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
		  getSymbol();
	  }
	  
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

		      result = read (opts->audio_in_fd, &sample, 2);
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
		              if ((opts->symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("O");
		                }
		            }
		          else
		            {
		              if ((opts->symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
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
		              if ((opts->symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  //printf ("X");
		                }
		            }
		          else
		            {
		              if ((opts->symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		                {
		                  printf ("-");
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

		  if ((opts->symboltiming == 1) && (have_sync == 0) && (lastsynctype != -1))
		    {
		      if (jitter >= 0)
		        {
		          printf (" %i\n", jitter);
		        }
		      else
		        {
		          printf ("\n");
		        }
		    }

		  symbolcnt++;
		  return symbol;
	  }

}
