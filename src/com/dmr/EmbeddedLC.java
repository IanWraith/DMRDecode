package com.dmr;

public class EmbeddedLC {
	private boolean rawLC[]=new boolean[128];
	private int currentState=-1;
	private boolean dataReady=false;
	private String lines[]=new String[3];
	
	// Add LC data (which may consist of 4 blocks) to the data store
	public void addData (byte[] dibit_buf,int type)	{
		int a,r=0;
		boolean rawdata[]=new boolean[32];
		// Convert from dibits into boolean
		for (a=70;a<86;a++)	{
			if (dibit_buf[a]==0)	{
				rawdata[r]=false;
				rawdata[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdata[r]=false;
				rawdata[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdata[r]=true;
				rawdata[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdata[r]=true;
				rawdata[r+1]=true;
			}
			r=r+2;
		}
		// Is this the first block of a 4 block embedded LC ?
		if (type==1)	{
			for (a=0;a<32;a++)	{
				rawLC[a]=rawdata[a];
			}
			// Show we are ready for the next LC block
			currentState=0;
		}
		// Is this the 2nd block of a 4 block embedded LC ?
		else if ((type==3)&&(currentState==0))	{
			for (a=0;a<32;a++)	{
				rawLC[a+32]=rawdata[a];
			}
			// Show we are ready for the next LC block
			currentState=1;
		}
		// Is this the 3rd block of a 4 block embedded LC ?
		else if ((type==3)&&(currentState==1))	{
			for (a=0;a<32;a++)	{
				rawLC[a+64]=rawdata[a];
			}
			// Show we are ready for the final LC block
			currentState=2;
		}
		// Is this the final block of a 4 block embedded LC ?
		else if ((type==2)&&(currentState==2))	{
			for (a=0;a<32;a++)	{
				rawLC[a+96]=rawdata[a];
			}
			processMultiBlockEmbeddedLC();
		}
		// Is this a single block embedded LC
		else if (type==0) processSingleBlockEmbeddedLC(rawdata);
	}
	
	// Unpack and error check a embedded LC
	private void processMultiBlockEmbeddedLC()	{
		int a;
		lines[0]="Embedded Multi Block LC : ";
		for (a=0;a<128;a++)	{
			if (rawLC[a]==false) lines[0]=lines[0]+"0"; 
			else lines[0]=lines[0]+"1";
		}
		dataReady=true;
	}
	
	// Deal with a single block embedded LC
	private void processSingleBlockEmbeddedLC (boolean data[])	{
		boolean isnull=true;
		String tline="";
		int a;
		lines[0]="<b>Embedded Single Block LC : ";
		// Check if this message is all 0's as if it is then it is a null
		for (a=0;a<32;a++)	{
			if (data[a]==true)	{
				tline=tline+"1";
				isnull=false;
			}
			else 	{
				tline=tline+"0";
			}
		}
		// Is this message a null short LC
		if (isnull==true)	{
			lines[0]=lines[0]+"Null";
		}
		else	{
			lines[0]=lines[0]+tline;
		}
		lines[0]=lines[0]+"</b>";
		dataReady=true;
	}
	
	// Tell the main program if we have data to return
	public boolean getDataReady	()	{
		return dataReady;
	}
	
	// Return the display lines to the main program
	public String[] getLines ()	{
		dataReady=false;
		currentState=-1;
		return lines;
	}

}
