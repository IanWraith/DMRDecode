package com.dmr;

public class ShortLC {
	private boolean dataReady;
	private String line;
	private boolean rawData[]=new boolean[69];
	private int currentState=-1;
	
	// Add data to the Short LC data buffer
	// Type 0 if First fragment of LC
	// Type 1 if Continuation fragment of LC
	// Type 2 if Last fragment of LC
	public void addData (boolean[] CACHbuf,int type)	{
		int a,b=7,rawCounter=0;
		dataReady=false;
		// First fragment ?
		// If so reset the the counters 
		if (type==0)	{
			rawCounter=0;
			// Ensure nothing else has arrived before
			if (currentState!=-1) return;
			// Set the current state to 0 to indicate a first fragment has arrived
			currentState=0;
			// Clear the display line
			line="";
		}
		// Continuation fragments
		else if (type==1)	{
			if (currentState==0) {
				rawCounter=17;
				currentState=1;
			}
			else if (currentState==1)	{
				rawCounter=34;
				currentState=2;
			}
			else if (currentState==-1)	{
				rawCounter=0;
				return;
			}
		}
		// Last fragment
		else if (type==2)	{
			// Ensure that a first fragment and two continuation fragments have arrived
			if (currentState!=2)	{
				currentState=-1;
				rawCounter=0;
				return;
			}
			else rawCounter=51;
		}
		// Add the data
		for (a=rawCounter;a<(rawCounter+17);a++)	{
			// Ignore the TACT
			try	{
				rawData[a]=CACHbuf[b];
			} catch (Exception e)	{
				String err=e.getMessage();
			}
			b++;
		}
		// Has the fragment ended ?
		if (type==2)	{
			decode();
			currentState=-1;
			rawCounter=0;
		}
	}
	
	// Make the text string available
	public String getLine()	{
		return line;
	}
	
	// The main object if decoded data is available
	public boolean isDataReady()	{
		return dataReady;
	}
	
	// Clear the data ready boolean
	public void clrDataReady()	{
		dataReady=false;
	}
	
	// Deinterleave and error check the short LC
	private void decode()	{
		int a;
		
		if (shortLCHamming(rawData)==true)	{
			boolean shortLC[]=deInterleaveShortLC(rawData);
		}
		else line="";
		
		
		dataReady=true;
	}
	
	// Deinterleave a Short LC from 4 CACH bursts
	private boolean[] deInterleaveShortLC (boolean raw[])	{
		int a,pos;
		final int sequence[]={
				0,4,8,12,16,20,24,28,32,36,40,44,
				1,5,9,13,17,21,25,29,33,37,41,45,
				2,6,10,14,18,22,26,30,34,38,42,46};
		boolean[] deinter=new boolean[36];
		for (a=0;a<36;a++)	{
			pos=sequence[a];
			deinter[a]=raw[pos];
		}
		
		line=" Verify ";
		for (a=0;a<36;a++)	{
			if (deinter[a]==true) line=line+"1";
			else line=line+"0";
			if (a==27) line=line+" ";
		}
		
		return deinter;
	}
	
	// Hamming check 3 of the 4 CACH rows
	private boolean shortLCHamming (boolean raw[])	{
		int a,pos;
		final int sequence1[]={0,4,8,12,16,20,24,28,32,36,40,44};
		final int sequence2[]={1,5,9,13,17,21,25,29,33,37,41,45};
		final int sequence3[]={2,6,10,14,18,22,26,30,34,38,42,46};
		final int ham1[]={48,52,56,60,64};
		final int ham2[]={49,53,57,61,65};
		final int ham3[]={50,54,58,62,66};
		boolean[] d=new boolean[12];
		boolean[] p=new boolean[5];
		boolean[] c=new boolean[5];
		
		for (a=0;a<12;a++)	{
			pos=sequence1[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham1[a];
				p[a]=raw[pos];
			}
		}
		
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}
		
		for (a=0;a<12;a++)	{
			pos=sequence2[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham2[a];
				p[a]=raw[pos];
			}
		}
		
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}

		for (a=0;a<12;a++)	{
			pos=sequence3[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham3[a];
				p[a]=raw[pos];
			}
		}
		
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}
		
		return true;
	}
	
	
}
