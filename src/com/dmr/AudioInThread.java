package com.dmr;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
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
    
    // Main
    public void run()	{
    	// Run continously
    	for (;;)	{
    		// If it hasn't been already then setup the audio device
    		if (audioReady==false) setupAudio();
    		// If the audio device is ready , the program wants to and we aren't already then
    		// get data from the audio device.
    		if ((audioReady==true)&&(run==true)&&(gettingAudio==false)) getSample();
    		
    	}
    }
	
    // Prepare the input audio device
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
    
    // Read in 2 bytes from the audio source combine them together into a single integer
    // then write that into the sound buffer
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
		  // If the write buffer pointer has reached maximum then reset it to zero
		  if (writePos==bufferSize) writePos=0;
		  gettingAudio=false;
    }
    
    // Return the next integer from the sound buffer
    // then increment the read buffer counter
    public int returnSample ()	{
    	// If the writePos hasn't changed since the last time then there is nothing to return
    	if (writePos==lastWritePos) Thread.yield();
    	int sample=audioBuffer[readPos];
    	lastWritePos=writePos;
    	// Increment the read buffer counter
    	readPos++;
    	// If the read buffer pointer has reached maximum then reset it to zero
    	if (readPos==bufferSize) readPos=0;
    	return sample;
    }
    
    // Called when the main program wants to suspend receiving audio
    public void suspendAudio ()	{
    	run=false;
    }
    
    // Called when the main program wants to start receiving audio
    public void startAudio ()	{
    	run=true;
    }
    
    // Getter tells the main program if the audio device is ready
    public boolean getAudioReady ()	{
    	return audioReady;
    }
    
    // When called this closes the audio device
    public void shutDownAudio ()	{
    	run=false;
    	Line.close();
    }
    
    
	
}
