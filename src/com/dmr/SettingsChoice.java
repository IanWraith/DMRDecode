package com.dmr;

public class SettingsChoice {
	private int bestMin=0;
	private int bestMax=0;
	private int bestScore=5;
	
	// Return the best number of good data frames so far
	public int getBestScore()	{
		return bestScore;
	}
		
	// Set the best max , min and good frame score
	public void	setBestChoice (int tmax,int tmin,int tscore)	{
		bestMin=tmin;
		bestMax=tmax;
		bestScore=tscore;
	}
	
	// See if the max and min measured are within 5% of the best so far
	// if it is return true and if not return false
	public boolean testChoice (int tmax,int tmin)	{
		// If we haven't had 5 good frames always return true
		// This allows the program to hunt for the best settings
		if (bestScore==5) return true;
		// Calculate 5% of both the best max and mins so far
		int maxp=(int)(((float)bestMax/(float)100.0)*(float)5.0);
		int minp=(int)(((float)bestMin/(float)100.0)*(float)5.0);
		// Max //
		if ((tmax>(bestMax-maxp)&&(tmax<(bestMax+maxp)))) return true;
		// Min //
		else if ((tmin>(bestMin-minp)&&(tmin<(bestMin+minp)))) return true;
		else return false;
	}
	
}
