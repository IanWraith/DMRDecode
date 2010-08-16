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
	  
	  int getFrameSync ()
	  {
	    /* detects frame sync and returns frame type
	     * 0 = +p25p1
	     * 1 = -p25p1
	     * 2 = +X2-TDMA (non inverted signal data frame)
	     * 3 = +X2-TDMA (inverted signal voice frame)
	     * 4 = -X2-TDMA (non inverted signal voice frame)
	     * 5 = -X2-TDMA (inverted signal data frame)
	     * 6 = +D-STAR
	     * 7 = -D-STAR
	     * 8 = +NXDN96
	     * 9 = -NXDN96
	     * 10 = +DMR (non inverted singlan data frame)
	     * 11 = -DMR (inverted signal voice frame)
	     * 12 = +DMR (non inverted signal voice frame)
	     * 13 = -DMR (inverted signal data frame)
	     * 14 = +ProVoice
	     * 15 = -ProVoice
	     */

	    int i, j, t, o, dibit, sync, symbol, synctest_pos, lastt;
	    char synctest[25];
	    char synctest18[19];
	    char synctest32[33];
	    char modulation[8];
	    char *synctest_p;
	    char synctest_buf[10240];
	    int lmin, lmax, lidx;
	    int lbuf[24], lbuf2[24];
	    int lsum;
	    char spectrum[64];

	    // detect frame sync
	    t = 0;
	    synctest[24] = 0;
	    synctest18[18] = 0;
	    synctest32[32] = 0;
	    synctest_pos = 0;
	    synctest_p = synctest_buf;
	    sync = 0;
	    lmin = 0;
	    lmax = 0;
	    lidx = 0;
	    lastt = 0;
	    numflips = 0;

	    while (sync == 0)
	      {
	        t++;
	        symbol=getSymbol();
	        lbuf[lidx] = symbol;
	        state->sbuf[state->sidx] = symbol;
	        if (lidx == 23)
	          {
	            lidx = 0;
	          }
	        else
	          {
	            lidx++;
	          }
	        if (state->sidx == (opts->ssize - 1))
	          {
	            state->sidx = 0;
	          }
	        else
	          {
	            state->sidx++;
	          }

	        if (lastt == 23)
	          {
	            lastt = 0;
	            if (state->numflips > opts->mod_threshold)
	              {
	                if (opts->mod_qpsk == 1)
	                  {
	                    state->rf_mod = 1;
	                  }
	              }
	            else if (state->numflips > 18)
	              {
	                if (opts->mod_gfsk == 1)
	                  {
	                    state->rf_mod = 2;
	                  }
	              }
	            else
	              {
	                if (opts->mod_c4fm == 1)
	                  {
	                    state->rf_mod = 0;
	                  }
	              }
	            state->numflips = 0;
	          }
	        else
	          {
	            lastt++;
	          }

	        //determine dibit state
	        if (symbol > 0)
	          {
	            *state->dibit_buf_p = 1;
	            state->dibit_buf_p++;
	            dibit = 49;
	          }
	        else
	          {
	            *state->dibit_buf_p = 3;
	            state->dibit_buf_p++;
	            dibit = 51;
	          }

	        *synctest_p = dibit;
	        if (t >= 24)
	          {
	            for (i = 0; i < 24; i++)
	              {
	                lbuf2[i] = lbuf[i];
	              }
	            qsort (lbuf2, 24, sizeof (int), comp);
	            lmin = (lbuf2[2] + lbuf2[3] + lbuf2[4]) / 3;
	            lmax = (lbuf2[21] + lbuf2[20] + lbuf2[19]) / 3;

	            if (state->rf_mod == 1)
	              {
	                state->minbuf[state->midx] = lmin;
	                state->maxbuf[state->midx] = lmax;
	                if (state->midx == (opts->msize - 1))
	                  {
	                    state->midx = 0;
	                  }
	                else
	                  {
	                    state->midx++;
	                  }
	                lsum = 0;
	                for (i = 0; i < opts->msize; i++)
	                  {
	                    lsum += state->minbuf[i];
	                  }
	                state->min = lsum / opts->msize;
	                lsum = 0;
	                for (i = 0; i < opts->msize; i++)
	                  {
	                    lsum += state->maxbuf[i];
	                  }
	                state->max = lsum / opts->msize;
	                state->center = ((state->max) + (state->min)) / 2;
	                state->maxref = ((state->max) * 0.80);
	                state->minref = ((state->min) * 0.80);
	              }
	            else
	              {
	                state->maxref = state->max;
	                state->minref = state->min;
	              }

	            if (state->rf_mod == 0)
	              {
	                sprintf (modulation, "C4FM");
	              }
	            else if (state->rf_mod == 1)
	              {
	                sprintf (modulation, "QPSK");
	              }
	            else if (state->rf_mod == 2)
	              {
	                sprintf (modulation, "GFSK");
	              }

	            if (opts->datascope == 1)
	              {
	                if (lidx == 0)
	                  {
	                    for (i = 0; i < 64; i++)
	                      {
	                        spectrum[i] = 0;
	                      }
	                    for (i = 0; i < 24; i++)
	                      {
	                        o = (lbuf2[i] + 32768) / 1024;
	                        spectrum[o]++;
	                      }
	                    if (state->symbolcnt > (4800 / opts->scoperate))
	                      {
	                        state->symbolcnt = 0;
	                        printf ("\n");
	                        printf ("Demod mode:     %s                Nac:                     %4X\n", modulation, state->nac);
	                        printf ("Frame Type:    %s        Talkgroup:            %7i\n", state->ftype, state->lasttg);
	                        printf ("Frame Subtype: %s       Source:          %12i\n", state->fsubtype, state->lastsrc);
	                        printf ("TDMA activity:  %s %s     Voice errors: %s\n", state->slot0light, state->slot1light, state->err_str);
	                        printf ("+----------------------------------------------------------------+\n");
	                        for (i = 0; i < 10; i++)
	                          {
	                            printf ("|");
	                            for (j = 0; j < 64; j++)
	                              {
	                                if (i == 0)
	                                  {
	                                    if ((j == ((state->min) + 32768) / 1024) || (j == ((state->max) + 32768) / 1024))
	                                      {
	                                        printf ("#");
	                                      }
	                                    else if (j == (state->center + 32768) / 1024)
	                                      {
	                                        printf ("!");
	                                      }
	                                    else
	                                      {
	                                        if (j == 32)
	                                          {
	                                            printf ("|");
	                                          }
	                                        else
	                                          {
	                                            printf (" ");
	                                          }
	                                      }
	                                  }
	                                else
	                                  {
	                                    if (spectrum[j] > 9 - i)
	                                      {
	                                        printf ("*");
	                                      }
	                                    else
	                                      {
	                                        if (j == 32)
	                                          {
	                                            printf ("|");
	                                          }
	                                        else
	                                          {
	                                            printf (" ");
	                                          }
	                                      }
	                                  }
	                              }
	                            printf ("|\n");
	                          }
	                        printf ("+----------------------------------------------------------------+\n");
	                      }
	                  }
	              }

	            strncpy (synctest, (synctest_p - 23), 24);
	            if (opts->frame_p25p1 == 1)
	              {
	                if (strcmp (synctest, FRAME_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " P25 Phase 1 ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " +p25p1    ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 0;
	                    return (0);
	                  }
	                if (strcmp (synctest, INV_FRAME_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " P25 Phase 1 ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " -p25p1    ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 1;
	                    return (1);
	                  }
	              }
	            if (opts->frame_x2tdma == 1)
	              {
	                if (strcmp (synctest, X2TDMA_DATA_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + (lmax)) / 2;
	                    state->min = ((state->min) + (lmin)) / 2;
	                    if (opts->inverted_x2tdma == 0)
	                      {
	                        // data frame
	                        sprintf (state->ftype, " X2-TDMA     ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " +X2-TDMA  ", synctest_pos + 1, modulation);
	                          }
	                        state->lastsynctype = 2;
	                        return (2);
	                      }
	                    else
	                      {
	                        // inverted voice frame
	                        sprintf (state->ftype, " X2-TDMA     ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " -X2-TDMA  ", synctest_pos + 1, modulation);
	                          }
	                        if (state->lastsynctype != 3)
	                          {
	                            state->firstframe = 1;
	                          }
	                        state->lastsynctype = 3;
	                        return (3);
	                      }
	                  }
	                if (strcmp (synctest, X2TDMA_VOICE_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    if (opts->inverted_x2tdma == 0)
	                      {
	                        // voice frame
	                        sprintf (state->ftype, " X2-TDMA     ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " +X2-TDMA  ", synctest_pos + 1, modulation);
	                          }
	                        if (state->lastsynctype != 4)
	                          {
	                            state->firstframe = 1;
	                          }
	                        state->lastsynctype = 4;
	                        return (4);
	                      }
	                    else
	                      {
	                        // inverted data frame
	                        sprintf (state->ftype, " X2-TDMA     ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " -X2-TDMA  ", synctest_pos + 1, modulation);
	                          }
	                        state->lastsynctype = 5;
	                        return (5);
	                      }
	                  }
	              }
	            if (opts->frame_dmr == 1)
	              {
	                if (strcmp (synctest, DMR_DATA_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + (lmax)) / 2;
	                    state->min = ((state->min) + (lmin)) / 2;
	                    if (opts->inverted_dmr == 0)
	                      {
	                        // data frame
	                        sprintf (state->ftype, " DMR         ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " +DMR      ", synctest_pos + 1, modulation);
	                          }
	                        state->lastsynctype = 10;
	                        return (10);
	                      }
	                    else
	                      {
	                        // inverted voice frame
	                        sprintf (state->ftype, " DMR         ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " -DMR      ", synctest_pos + 1, modulation);
	                          }
	                        if (state->lastsynctype != 11)
	                          {
	                            state->firstframe = 1;
	                          }
	                        state->lastsynctype = 11;
	                        return (11);
	                      }
	                  }
	                if (strcmp (synctest, DMR_VOICE_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    if (opts->inverted_dmr == 0)
	                      {
	                        // voice frame
	                        sprintf (state->ftype, " DMR         ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " +DMR      ", synctest_pos + 1, modulation);
	                          }
	                        if (state->lastsynctype != 12)
	                          {
	                            state->firstframe = 1;
	                          }
	                        state->lastsynctype = 12;
	                        return (12);
	                      }
	                    else
	                      {
	                        // inverted data frame
	                        sprintf (state->ftype, " DMR         ");
	                        if (opts->errorbars == 1)
	                          {
	                            printFrameSync (opts, state, " -DMR      ", synctest_pos + 1, modulation);
	                          }
	                        state->lastsynctype = 13;
	                        return (13);
	                      }
	                  }
	              }
	            if (opts->frame_provoice == 1)
	              {
	                strncpy (synctest32, (synctest_p - 31), 32);
	                if ((strcmp (synctest32, PROVOICE_SYNC) == 0) || (strcmp (synctest32, PROVOICE_EA_SYNC) == 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " ProVoice    ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " -ProVoice ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 14;
	                    return (14);
	                  }
	                else if ((strcmp (synctest32, INV_PROVOICE_SYNC) == 0) || (strcmp (synctest32, INV_PROVOICE_EA_SYNC) == 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " ProVoice    ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " -ProVoice ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 15;
	                    return (15);
	                  }

	              }
	            if (opts->frame_nxdn == 1)
	              {
	                strncpy (synctest18, (synctest_p - 17), 18);
	                if (strcmp (synctest18, NXDN96_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " NXDN96      ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " +NXDN96   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 8;
	                    return (8);
	                  }
	                if (strcmp (synctest18, INV_NXDN96_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " NXDN96      ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " -NXDN96   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 9;
	                    return (9);
	                  }
	              }
	            if (opts->frame_dstar == 1)
	              {
	                if (strcmp (synctest, DSTAR_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " D-STAR      ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " +D-STAR   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 6;
	                    return (6);
	                  }
	                if (strcmp (synctest, INV_DSTAR_SYNC) == 0)
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, " D-STAR      ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, " -D-STAR   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = 7;
	                    return (7);
	                  }
	              }

	            if ((t == 24) && (state->lastsynctype != -1))
	              {
	                if ((state->lastsynctype == 0) && ((state->lastp25type == 1) || (state->lastp25type == 2)))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + (lmax)) / 2;
	                    state->min = ((state->min) + (lmin)) / 2;
	                    sprintf (state->ftype, "(P25 Phase 1)");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(+p25p1)   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (0);
	                  }
	                else if ((state->lastsynctype == 1) && ((state->lastp25type == 1) || (state->lastp25type == 2)))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, "(P25 Phase 1)");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(-p25p1)   ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (1);
	                  }
	                else if ((state->lastsynctype == 3) && (strcmp (synctest, X2TDMA_VOICE_SYNC) != 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, "(X2-TDMA)    ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(-X2-TDMA) ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (3);
	                  }
	                else if ((state->lastsynctype == 4) && (strcmp (synctest, X2TDMA_DATA_SYNC) != 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, "(X2-TDMA)    ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(+X2-TDMA) ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (4);
	                  }
	                else if ((state->lastsynctype == 11) && (strcmp (synctest, DMR_VOICE_SYNC) != 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, "(DMR)        ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(-DMR)     ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (11);
	                  }
	                else if ((state->lastsynctype == 12) && (strcmp (synctest, DMR_DATA_SYNC) != 0))
	                  {
	                    state->carrier = 1;
	                    state->offset = synctest_pos;
	                    state->max = ((state->max) + lmax) / 2;
	                    state->min = ((state->min) + lmin) / 2;
	                    sprintf (state->ftype, "(DMR)        ");
	                    if (opts->errorbars == 1)
	                      {
	                        printFrameSync (opts, state, "(+DMR)     ", synctest_pos + 1, modulation);
	                      }
	                    state->lastsynctype = -1;
	                    return (12);
	                  }
	              }
	          }

	        if (exitflag == 1)
	          {
	            cleanupAndExit (opts, state);
	          }

	        if (synctest_pos < 10200)
	          {
	            synctest_pos++;
	            synctest_p++;
	          }
	        else
	          {
	            // buffer reset
	            synctest_pos = 0;
	            synctest_p = synctest_buf;
	            noCarrier (opts, state);
	          }

	        if (state->carrier == 1)
	          {
	            if (synctest_pos >= 1800)
	              {
	                if (opts->errorbars == 1)
	                  {
	                    if (opts->verbose > 1)
	                      {
	                        printf ("Sync: no sync\n");
	                      }
	                  }
	                noCarrier (opts, state);
	                return (-1);
	              }
	          }
	      }

	    return (-1);
	  }
	  

}
