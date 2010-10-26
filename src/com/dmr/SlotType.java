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
	
	
	
}
