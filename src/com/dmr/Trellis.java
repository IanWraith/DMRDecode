package com.dmr;

public class Trellis {
	
	private final int INTERLEAVE[]={
			0,1,8,9,16,17,24,25,32,33,40,41,48,49,56,57,64,65,72,73,80,81,88,89,96,97,
			2,3,10,11,18,19,26,27,34,35,42,43,50,51,58,59,66,67,74,75,82,83,90,91,
			4,5,12,13,20,21,28,29,36,37,44,45,52,53,60,61,68,69,76,77,84,85,92,93,
			6,7,14,15,22,23,30,31,38,39,46,47,54,55,62,63,70,71,78,79,86,87,94,95};
	
	private final byte STATETABLE[]={
			0,8,4,12,2,10,6,14,
			4,12,2,10,6,14,0,8,
			1,9,5,13,3,11,7,15,
			5,13,3,11,7,15,1,9,
			3,11,7,15,1,9,5,13,
			7,15,1,9,5,13,3,11,
			2,10,6,14,0,8,4,12,
			6,14,0,8,4,12,2,10};
	
	private byte trellisTable[][]=new byte[8][8];
	
	private boolean errorState=false;
	
	public Trellis()	{
		// Load the trellis table from the state table
		int a,b,c=0;
		for (a=0;a<8;a++)	{
			for (b=0;b<8;b++)	{
				trellisTable[a][b]=STATETABLE[c];
				c++;
			}
		}
	}
	
	
	// 
	public boolean[] decode (boolean r[])	{
		boolean out[]=new boolean[144];
		
		byte dibits[]=extractDibits(r);
		byte cons[]=constellationOut(dibits);
		
		
		return out;
	}
	
	// Extract and deinterleave the dibits
	private byte[] extractDibits (boolean[] raw)	{
		int a,index=97;
		byte rawDibits[]=new byte[98];
		byte dibits[]=new byte[98];
		for (a=0;a<196;a=a+2)	{
			// Set the dibits
			// 01 = +3
			// 00 = +1
			// 10 = -1
			// 11 = -3
			if ((raw[a]==false)&&(raw[a+1]==true)) rawDibits[index]=+3;
			else if ((raw[a]==false)&&(raw[a+1]==false)) rawDibits[index]=+1;
			else if ((raw[a]==true)&&(raw[a+1]==false)) rawDibits[index]=-1;
			else if ((raw[a]==true)&&(raw[a+1]==true)) rawDibits[index]=-3;
			// Reduce the index
			index--;
		}
		// Now deinterleave the dibits
		for (a=0;a<98;a++)	{
			index=INTERLEAVE[a];
			dibits[index]=rawDibits[a];
		}
		return dibits;
	}
	
	// Extract the constellation points
	private byte[] constellationOut (byte[] diBits)	{
		byte constellationPoints[]=new byte[49];
		int a,i=0;
		for (a=0;a<98;a=a+2)	{
			if ((diBits[a]==+1)&&(diBits[a+1]==-1)) constellationPoints[i]=0;
			else if ((diBits[a]==-1)&&(diBits[a+1]==-1)) constellationPoints[i]=1;
			else if ((diBits[a]==+3)&&(diBits[a+1]==-3)) constellationPoints[i]=2;
			else if ((diBits[a]==-3)&&(diBits[a+1]==-3)) constellationPoints[i]=3;
			else if ((diBits[a]==-3)&&(diBits[a+1]==-1)) constellationPoints[i]=4;
			else if ((diBits[a]==+3)&&(diBits[a+1]==-1)) constellationPoints[i]=5;
			else if ((diBits[a]==-1)&&(diBits[a+1]==-3)) constellationPoints[i]=6;
			else if ((diBits[a]==+1)&&(diBits[a+1]==-3)) constellationPoints[i]=7;
			else if ((diBits[a]==-3)&&(diBits[a+1]==+3)) constellationPoints[i]=8;
			else if ((diBits[a]==+3)&&(diBits[a+1]==+3)) constellationPoints[i]=9;
			else if ((diBits[a]==-1)&&(diBits[a+1]==+1)) constellationPoints[i]=10;
			else if ((diBits[a]==+1)&&(diBits[a+1]==+1)) constellationPoints[i]=11;
			else if ((diBits[a]==+1)&&(diBits[a+1]==+3)) constellationPoints[i]=12;
			else if ((diBits[a]==-1)&&(diBits[a+1]==+3)) constellationPoints[i]=13;
			else if ((diBits[a]==+3)&&(diBits[a+1]==+1)) constellationPoints[i]=14;
			else if ((diBits[a]==-3)&&(diBits[a+1]==+1)) constellationPoints[i]=15;
			else constellationPoints[i]=-1;
			i++;
		}
		
		return constellationPoints;
	}


	public boolean isErrorState() {
		return errorState;
	}


}
