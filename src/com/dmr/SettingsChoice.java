package com.dmr;

import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Date;

public class SettingsChoice {
	private int bestMin=0;
	private int bestMax=0;
	private int bestScore=5;
	private boolean debug=false;
	
	// Return the best number of good data frames so far
	public int getBestScore()	{
		return bestScore;
	}
	
	public int getBestMax()	{
		return bestMax;
	}
	
	public int getBestMin()	{
		return bestMin;
	}
	
	public void setDebug (boolean d)	{
		debug=d;
	}
		
	// Set the best max , min and good frame score
	public void	setBestChoice (int tmax,int tmin,int tscore)	{
		bestMin=tmin;
		bestMax=tmax;
		bestScore=tscore;
		// If debugging record this
		if (debug==true)	{
			String l=getTimeStamp()+",Set,"+Integer.toString(tmax)+","+Integer.toString(tmin)+","+Integer.toString(tscore);
			debugDump(l);
		}
	}
	
	// See if the max and min measured are within 5% of the best so far
	// if it is return true and if not return false
	public boolean testChoice (int tmax,int tmin)	{
		boolean res=false;
		// If we haven't had 5 good frames always return true
		// This allows the program to hunt for the best settings
		if (bestScore==5) return true;
		// Calculate 10% of both the best max and mins so far
		int maxp=(int)(((float)bestMax/(float)100.0)*(float)10.0);
		int minp=(int)(((float)bestMin/(float)100.0)*(float)10.0);
		// Max //
		if ((tmax>(bestMax-maxp)&&(tmax<(bestMax+maxp)))) res=true;
		// Min //
		else if ((tmin>(bestMin-minp)&&(tmin<(bestMin+minp)))) res=true;
		// If debugging record this
		if (debug==true)	{
			String l=getTimeStamp();
			if (res==true) l=l+",OK,";
			else l=l+",FAIL,";
			l=l+Integer.toString(tmax)+","+Integer.toString(tmin);
			l=l+","+Integer.toString(bestMax)+","+Integer.toString(bestMin)+","+Integer.toString(bestScore);
			debugDump(l);
		}
		return res;
	}
	
	public void recordForce()	{
		if (debug==false) return;
		String l=getTimeStamp()+",Force,"+Integer.toString(bestMax)+","+Integer.toString(bestMin);
		debugDump(l);
	}
	
	public void badFrameRecord()	{
		if (debug==false) return;
		String l=getTimeStamp()+",Bad Frame";
		debugDump(l);
	}
	
	public void goodFrameRecord()	{
		if (debug==false) return;
		String l=getTimeStamp()+",Good Frame";
		debugDump(l);
	}
	
	// Write a line to the debug file
	private void debugDump (String line)	{
	    try	{
	    	FileWriter dfile=new FileWriter("settingschoice_debug.csv",true);
	    	dfile.write(line);
	    	dfile.write("\r\n");
	    	dfile.flush();  
	    	dfile.close();
	    	}catch (Exception e)	{
	    		System.err.println("Error: " + e.getMessage());
	    		}
		}
	// Return a time stamp
	private String getTimeStamp() {
		Date now=new Date();
		DateFormat df=DateFormat.getTimeInstance();
		return df.format(now);
	}
	
}
