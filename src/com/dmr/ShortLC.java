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
		line="";
		for (a=0;a<68;a++)	{
			if (rawData[a]==true) line=line+"1";
			else line=line+"0";
		}
		
		// TODO: Deinterleave the Short LC
		
		dataReady=true;
	}
	
}
