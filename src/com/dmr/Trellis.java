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
			
	// Converts the 3/4 rate trellis encoded bits to plain binary
	public boolean[] decode (boolean r[])	{
		byte dibits[]=extractDibits(r);
		byte cons[]=constellationOut(dibits);
		int tri[]=tribitExtract(cons);
		// If the output of tribitExtract() is null then we have an error so return null
		if (tri==null) return null;
		boolean out[]=binaryConvert(tri);
		return out;
	}
	
	// Extract and deinterleave the dibits
	private byte[] extractDibits (boolean[] rawBits)	{
		int a,index=0,deinterleave;
		byte trellisDibit=0;
		byte encDibit[]=new byte[98];
		for (a=0;a<196;a=a+2)	{
			// Set the dibits
			// 01 = +3
			// 00 = +1
			// 10 = -1
			// 11 = -3
			if ((rawBits[a]==false)&&(rawBits[a+1]==true)) trellisDibit=+3;
			else if ((rawBits[a]==false)&&(rawBits[a+1]==false)) trellisDibit=+1;
			else if ((rawBits[a]==true)&&(rawBits[a+1]==false)) trellisDibit=-1;
			else if ((rawBits[a]==true)&&(rawBits[a+1]==true)) trellisDibit=-3;
			// Deinterleave
			deinterleave=INTERLEAVE[index];
			encDibit[deinterleave]=trellisDibit;
			// Increase the index
			index++;
		}
		return encDibit;
	}
	
	// Extract the constellation points
	private byte[] constellationOut (byte[] encDibit)	{
		byte constellationPoints[]=new byte[49];
		int a,index=0;
		for (a=0;a<98;a=a+2)	{
			if ((encDibit[a]==+1)&&(encDibit[a+1]==-1)) constellationPoints[index]=0;
			else if ((encDibit[a]==-1)&&(encDibit[a+1]==-1)) constellationPoints[index]=1;
			else if ((encDibit[a]==+3)&&(encDibit[a+1]==-3)) constellationPoints[index]=2;
			else if ((encDibit[a]==-3)&&(encDibit[a+1]==-3)) constellationPoints[index]=3;
			else if ((encDibit[a]==-3)&&(encDibit[a+1]==-1)) constellationPoints[index]=4;
			else if ((encDibit[a]==+3)&&(encDibit[a+1]==-1)) constellationPoints[index]=5;
			else if ((encDibit[a]==-1)&&(encDibit[a+1]==-3)) constellationPoints[index]=6;
			else if ((encDibit[a]==+1)&&(encDibit[a+1]==-3)) constellationPoints[index]=7;
			else if ((encDibit[a]==-3)&&(encDibit[a+1]==+3)) constellationPoints[index]=8;
			else if ((encDibit[a]==+3)&&(encDibit[a+1]==+3)) constellationPoints[index]=9;
			else if ((encDibit[a]==-1)&&(encDibit[a+1]==+1)) constellationPoints[index]=10;
			else if ((encDibit[a]==+1)&&(encDibit[a+1]==+1)) constellationPoints[index]=11;
			else if ((encDibit[a]==+1)&&(encDibit[a+1]==+3)) constellationPoints[index]=12;
			else if ((encDibit[a]==-1)&&(encDibit[a+1]==+3)) constellationPoints[index]=13;
			else if ((encDibit[a]==+3)&&(encDibit[a+1]==+1)) constellationPoints[index]=14;
			else if ((encDibit[a]==-3)&&(encDibit[a+1]==+1)) constellationPoints[index]=15;
			index++;
		}
		return constellationPoints;
	}

	// Extract tribits (as ints) from the constellation points
	private int[] tribitExtract (byte cons[])	{
		int a,b,rowStart,lastState=0;
		int tribit[]=new int[49];
		for (a=0;a<cons.length;a++)	{
			// The lastState variable decides which row of STATETABLE we should use
			rowStart=lastState*8;
			boolean match=false;
			for (b=rowStart;b<(rowStart+8);b++)	{
				// Check if this constellation point matches an element of this row of STATETABLE
				if (cons[a]==STATETABLE[b])	{
					// Yes it does
					match=true;
					lastState=b-rowStart;
					tribit[a]=lastState;
				}
			}
			// If no match found then we have a problem
			if (match==false) return null;
		}
		return tribit;
	}
	
	// Extract the 144 binary bits from the dibits
	private boolean[] binaryConvert (int tribit[])	{
		int a,b=0;
		boolean out[]=new boolean[144];
		for (a=0;a<144;a=a+3)	{
			// Convert three bits at a time
			if ((tribit[b]&4)>0) out[a]=true;
			else  out[a]=false;
			if ((tribit[b]&2)>0) out[a+1]=true;
			else  out[a+1]=false;
			if ((tribit[b]&1)>0) out[a+2]=true;
			else  out[a+2]=false;
			// Increment the bit counter
			b++;
		}
		return out;
	}


}
