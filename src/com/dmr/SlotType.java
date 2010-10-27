package com.dmr;

public class SlotType {
	
	private int dibit_buf[]=new int[132];
	private String line;
	private boolean passErrorCheck;
	
	public String decode (int[] buf)	{
		dibit_buf=buf;
		line="Slot Type : ";
		passErrorCheck=mainDecode();
		return line;
	}
	

	private boolean mainDecode ()	{
		int a,r,t1;
		boolean rawdataSLOT[]=new boolean[20];
		boolean dataSLOT[]=new boolean[20];
		boolean res=false;
		// Convert from dibit into boolean
		// DATA SLOT is broken into 2 parts
		r=0;
		for (a=60;a<66;a++)	{
			if (dibit_buf[a]==0)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=true;
			}
			r=r+2;
		}
		r=10;
		for (a=90;a<95;a++)	{
			if (dibit_buf[a]==0)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=true;
			}
			r=r+2;
		}	
		
		for (a=0;a<20;a++)	{
			if (rawdataSLOT[a]==false) line=line+"0";
			else line=line+"1";
			if (a==9) line=line+" ";
			
		}
		
		return res;
	}
	
	// Code to calculate all valid values for Golay (20,8)
	void calcGolday208 ()	{
		boolean d[]=new boolean[8];
		boolean p[]=new boolean[12];
		int value[]=new int[256];
		int a;
		// Run through all possible 8 bit values
		for (a=0;a<256;a++){
			// Convert to binary
			if ((a&127)>0) d[0]=true;
			else d[0]=false;
			if ((a&64)>0) d[1]=true;
			else d[1]=false;
			if ((a&32)>0) d[2]=true;
			else d[2]=false;
			if ((a&16)>0) d[3]=true;
			else d[3]=false;
			if ((a&8)>0) d[4]=true;
			else d[4]=false;
			if ((a&4)>0) d[5]=true;
			else d[5]=false;
			if ((a&2)>0) d[6]=true;
			else d[6]=false;
			if ((a&1)>0) d[7]=true;
			else d[7]=false;
			// Calculate the parity bits
			p[0]=d[1]^d[4]^d[5]^d[6]^d[7];
			p[1]=d[1]^d[2]^d[4];
			p[2]=d[0]^d[2]^d[3]^d[5];
			p[3]=d[0]^d[1]^d[3]^d[4]^d[6];
			p[4]=d[0]^d[1]^d[2]^d[4]^d[5]^d[7];
			p[5]=d[0]^d[2]^d[3]^d[4]^d[7];
			p[6]=d[3]^d[6]^d[7];
			p[7]=d[0]^d[1]^d[5]^d[6];
			p[8]=d[0]^d[1]^d[2]^d[6]^d[7];
			p[9]=d[2]^d[3]^d[4]^d[5]^d[6];
			p[10]=d[0]^d[3]^d[4]^d[5]^d[6]^d[7];
			p[11]=d[1]^d[2]^d[3]^d[5]^d[7];
			// Shift the value 12 times to the left
			value[a]=a<<12;
			if (p[0]==true) value[a]=value[a]+2048;
			if (p[1]==true) value[a]=value[a]+1024;
			if (p[2]==true) value[a]=value[a]+512;
			if (p[3]==true) value[a]=value[a]+256;
			if (p[4]==true) value[a]=value[a]+128;
			if (p[5]==true) value[a]=value[a]+64;
			if (p[6]==true) value[a]=value[a]+32;
			if (p[7]==true) value[a]=value[a]+16;
			if (p[8]==true) value[a]=value[a]+8;
			if (p[9]==true) value[a]=value[a]+4;
			if (p[10]==true) value[a]=value[a]+2;
			if (p[11]==true) value[a]=value[a]+1;
		}
		// Just something to break on !
		a++;
	}
	
	
}
