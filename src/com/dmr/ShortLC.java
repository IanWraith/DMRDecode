package com.dmr;

public class ShortLC {
	private boolean dataReady;
	private String line;
	private boolean rawData[]=new boolean[69];
	private int rawCounter=0;
	private int blockCounter=0;
	
	// Add data to the Short LC data buffer
	// Type 0 if First fragment of LC
	// Type 1 if Continuation fragment of LC
	// Type 2 if Last fragment of LC
	public void addData (boolean[] CACHbuf,int type)	{
		int a,b=7;
		dataReady=false;
		// First fragment ?
		if (type==0)	{
			rawCounter=0;
			blockCounter=0;
		}
		// Add the data
		for (a=rawCounter;a<(rawCounter+17);a++)	{
			// Ignore the TACT
			
			// TODO : this line is causing a  java.lang.ArrayIndexOutOfBoundsException exception
			
			rawData[a]=CACHbuf[b];
			b++;
		}
		blockCounter++;
		rawCounter=rawCounter+17;
		// Has the fragment ended ?
		if (type==2)	{
			if (blockCounter==4) decode();
			else	{
				rawCounter=0;
				blockCounter=0;
			}
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
	
	private void decode()	{
		int a;
		line=" ";
		for (a=0;a<68;a++)	{
			if (rawData[a]==true) line=line+"1";
			else line=line+"0";
		}
		dataReady=true;
	}
	
}
