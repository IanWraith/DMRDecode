package com.dmr;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

public class AudioInThread extends Thread {

	private boolean run;
	private boolean audioReady;
	private int writePos;
	private int readPos;
	private int lastWritePos;
	private static final int bufferSize=512;
	private int audioBuffer[]=new int [bufferSize];
	private TargetDataLine Line;
	private AudioFormat format;
	private boolean gettingAudio;
	
    public AudioInThread (DMRDecode theApp) {
    	run=false;
    	audioReady=false;
    	writePos=0;
    	readPos=0;
    	lastWritePos=-1;
    	gettingAudio=false;
        start();
        Thread.yield();
      }
    
    public void run()	{
    	for (;;)	{
    		if (audioReady==false) setupAudio();
    		if ((audioReady==true)&&(run==true)&&(gettingAudio==false)) getSample();
    		
    	}
    }
	
    private void setupAudio ()	{
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
    
    private void getSample ()	{
    	gettingAudio=true;
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
		  // Put the new sample in the buffer
		  audioBuffer[writePos]=sample;
		  // Increment the write buffer pos
		  writePos++;
		  if (writePos==bufferSize) writePos=0;
		  gettingAudio=false;
    }
    
    public int returnSample ()	{
    	// If the writePos hasn't changed since the last time then there is nothing to return
    	if (writePos==lastWritePos) Thread.yield();
    	int sample=audioBuffer[readPos];
    	lastWritePos=writePos;
    	// Increment the read buffer counter
    	readPos++;
    	if (readPos==bufferSize) readPos=0;
    	return sample;
    }
    
    public void stopAudio ()	{
    	run=false;
    }
    
    public void startAudio ()	{
    	run=true;
    }
    
    public boolean getAudioReady ()	{
    	return audioReady;
    }
    
    public void shutDownAudio ()	{
    	run=false;
    	Line.close();
    }
    
    
	
}
