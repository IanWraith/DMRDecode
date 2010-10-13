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
	private static final int BUFFERSIZE=512;
	private int audioBuffer[]=new int [BUFFERSIZE];
	private TargetDataLine Line;
	private AudioFormat format;
	private boolean gettingAudio;
	// Filter details ..
	// filtertype	 =	 Raised Cosine
	// samplerate	 =	 48000
	// corner	 =	 2400
	// beta	 =	 0.7
	// impulselen	 =	 21
	// racos	 =	 sqrt
	// comp	 =	 no
	// bits	 =	
	// logmin	 =	
	private static final int NZEROS=20;
	private static final double GAIN=1.063197639e+01;
	private double xv[]=new double [NZEROS+1];
	private static final double XCOEFFS[]=
	  { -0.1142415065, -0.0652615756, +0.0266625160, +0.1613371679,
	    +0.3321186878, +0.5261565098, +0.7257137259, +0.9104055994,
	    +1.0600181526, +1.1574475610, +1.1912627108, +1.1574475610,
	    +1.0600181526, +0.9104055994, +0.7257137259, +0.5261565098,
	    +0.3321186878, +0.1613371679, +0.0266625160, -0.0652615756,
	    -0.1142415065,
	  };
	
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
    		// Sleep for 10 ns
    		//try	{
    		//Thread.sleep(0,10);
    		//} catch (Exception e)	{}
    		
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
    	int sample,count,total=0,fsample;
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
		// Put this through a root raised filter
		fsample=rootRaisedFilter(sample);
		// Put the new sample in the buffer
		audioBuffer[writePos]=fsample;
		// Increment the write buffer pos
		writePos++;
		// If the write buffer pointer has reached maximum then reset it to zero
		if (writePos==BUFFERSIZE) writePos=0;		
		gettingAudio=false;	
    }
    
    // Return the next integer from the sound buffer
    // then increment the read buffer counter
    public int returnSample ()	{
    	if (run==false) return -1;
    	// If the writePos hasn't changed since the last time then there is nothing to return
    	if (writePos==lastWritePos)	{
    		String err="AudioInThread returnSample() has caught up with itself !";
    		JOptionPane.showMessageDialog(null,err,"DMRDecode", JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    	int sample=audioBuffer[readPos];
    	lastWritePos=writePos;
    	// Increment the read buffer counter
    	readPos++;
    	// If the read buffer pointer has reached maximum then reset it to zero
    	if (readPos==BUFFERSIZE) readPos=0;
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
    
    // A root raised pulse shaping filter
    public int rootRaisedFilter (int sample)	{
    	int i;
    	double sum=0.0;
    	double in=(double) sample;
    	for (i=0;i<NZEROS;i++)	{
    		xv[i]=xv[i+1];
    	}
    	xv[NZEROS]=in/GAIN;
    	for (i=0;i<=NZEROS;i++)	{
    		sum=sum+(XCOEFFS[i]*xv[i]);
    	}
    	return (int)sum;
    }

    // Return true only if there are samples waiting
    public boolean sampleReady ()	{
    	if (writePos==lastWritePos) return false;
    	 else return true;
    }
	

    
	
}
