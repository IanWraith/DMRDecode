package com.dmr;

import java.io.FileWriter;

public class VoiceData {
	
	// Handle incoming voice data
	public void handleVoice (DMRDecode tTheApp,byte[] dibit_buf)	{
		boolean bits[]=new boolean[216];
		int vdata[]=new int[27];
		// Extract the bits
		bits=extractVoiceBits(dibit_buf);
		// Pack the bits into an int array
		vdata=packBits(bits);
		// Send the data via the sockets
		tTheApp.socketThread.sendVoiceViaSocket(vdata,tTheApp.currentChannel);
	}
	
	// Get the voice data bits
	private boolean[] extractVoiceBits (byte dibit_buf[])	{
		int a,r=0;
		boolean rawData[]=new boolean[216];
		// First block
		for (a=12;a<66;a++)	{
			if (dibit_buf[a]==0)	{
				rawData[r]=false;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawData[r]=false;
				rawData[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawData[r]=true;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawData[r]=true;
				rawData[r+1]=true;
			}
			r=r+2;
		}
		// Second block
		for (a=90;a<144;a++)	{
			if (dibit_buf[a]==0)	{
				rawData[r]=false;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawData[r]=false;
				rawData[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawData[r]=true;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawData[r]=true;
				rawData[r+1]=true;
			}
			r=r+2;
		}
		return rawData;
	}
	
	// Pack the bits into an int array
	private int[] packBits(boolean bts[])	{
		int a,c=0;
		int by[]=new int[27];
		for (a=0;a<216;a=a+8)	{
			by[c]=convertByte(bts,a);
			c++;
		}
		return by;
	}
	
	// Get a single byte from the boolean array
	private int convertByte (boolean b[],int offset)	{
		int dby=0;
		if (b[offset]==true) dby=127;
		if (b[offset+1]==true) dby=dby+64;
		if (b[offset+2]==true) dby=dby+32;
		if (b[offset+3]==true) dby=dby+16;
		if (b[offset+4]==true) dby=dby+8;
		if (b[offset+5]==true) dby=dby+4;
		if (b[offset+6]==true) dby=dby+2;
		if (b[offset+7]==true) dby++;
		return dby;
	}
	
	// A function to save voice data to enable debugging
	private void voiceDump (int vdata[])	{
	    try	{
	    	int a;
	    	StringBuilder vline=new StringBuilder(500);
	    	FileWriter vfile=new FileWriter("voice.csv",true);
	    	// Run through all 27 ints
	    	for (a=0;a<27;a++)	{
	    		if (a>0) vline.append(",");
	    		vline.append(Integer.toString(vdata[a]));
	    	}
	    	vline.append("\r\n");
	    	vfile.write(vline.toString());
	    	vfile.flush();  
	    	vfile.close();
	    	}catch (Exception e)	{
	    		System.err.println("Error: " + e.getMessage());
	    		}
		}
	

}
