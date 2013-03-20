package com.dmr;

public class Trellis {
	
	final int INTERLEAVE[]={
			0,1,8,9,16,17,24,25,32,33,40,41,48,49,56,57,64,65,72,73,80,81,88,89,96,97,
			2,3,10,11,18,19,26,27,34,35,42,43,50,51,58,59,66,67,74,75,82,83,90,91,
			4,5,12,13,20,21,28,29,36,37,44,45,52,53,60,61,68,69,76,77,84,85,92,93,
			6,7,14,15,22,23,30,31,38,39,46,47,54,55,62,63,70,71,78,79,86,87,94,95};
	
	
	// Extract and deinterleave the dibits
	private byte[] extractDibits (boolean[] raw)	{
		int a,index=97;
		byte rawDibits[]=new byte[98];
		byte dibits[]=new byte[98];
		for (a=0;a<196;a=a+2)	{
			// Set the dibits
			if (raw[a]==true) rawDibits[index]=2;
			else rawDibits[index]=0;
			if (raw[a+1]==true) rawDibits[index]++;
			// Reduce the index
			index--;
		}
		// Now deinterleave the dibits
		for (a=0;a<98;a++)	{
			index=INTERLEAVE[a];
			dibits[a]=rawDibits[index];
		}
		return dibits;
	}
	

}
