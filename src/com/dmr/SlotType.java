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
		int a,r,t1,colourCode,dataType;
		boolean dataSLOT[]=new boolean[20];
		boolean res=false;
		// Convert from dibit into boolean
		// DATA SLOT is broken into 2 parts
		r=0;
		for (a=61;a<66;a++)	{
			if (dibit_buf[a]==0)	{
				dataSLOT[r]=false;
				dataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				dataSLOT[r]=false;
				dataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				dataSLOT[r]=true;
				dataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				dataSLOT[r]=true;
				dataSLOT[r+1]=true;
			}
			r=r+2;
		}
		for (a=90;a<95;a++)	{
			if (dibit_buf[a]==0)	{
				dataSLOT[r]=false;
				dataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				dataSLOT[r]=false;
				dataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				dataSLOT[r]=true;
				dataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				dataSLOT[r]=true;
				dataSLOT[r+1]=true;
			}
			r=r+2;
		}	
		
		calcGolay208();
		//checkGolay208(dataSLOT);
		
		// Colour code
		if (dataSLOT[0]==true) colourCode=8;
		else colourCode=0;
		if (dataSLOT[1]==true) colourCode=colourCode+4;
		if (dataSLOT[2]==true) colourCode=colourCode+2;
		if (dataSLOT[3]==true) colourCode++;
		// Data Type
		if (dataSLOT[4]==true) dataType=8;
		else dataType=0;
		if (dataSLOT[5]==true) dataType=dataType+4;
		if (dataSLOT[6]==true) dataType=dataType+2;
		if (dataSLOT[7]==true) dataType++;
		// Display this info
		line=line+"Colour Code "+Integer.toString(colourCode);
		if (dataType==0) line=line+" PI Header";
		else if (dataType==1) line=line+" Voice LC Header";
		else if (dataType==2) line=line+" Terminator with LC";
		else if (dataType==3) line=line+" CSBK";
		else if (dataType==4) line=line+" MBC Header";
		else if (dataType==5) line=line+" MBC Continuation";
		else if (dataType==6) line=line+" Data Header";
		else if (dataType==7) line=line+" Rate ½ Data Continuation";
		else if (dataType==8) line=line+" Rate ¾ Data Continuation";
		else if (dataType==9) line=line+" Idle";
		else line=line+" Reserved for future use";
		
		line=line+" ";
		for (a=0;a<20;a++)	{
			if (dataSLOT[a]==false) line=line+"0";
			else line=line+"1";	
		}
		
		return res;
	}
	
	// Code to calculate all valid values for Golay (20,8)
	boolean calcGolay208 ()	{
		final int POLY=0xAE3;
		int value[]=new int[256];
		int a,b,cw;
		// Run through all possible 8 bit values
		a=243;
		//for (a=0;a<256;a++){
			cw=a;
			for (b=0;b<8;b++)	{
				if ((cw&1)>0) cw^=POLY; 
				cw>>=1;
			}
		value[a]=cw;
		//}
		// Just something to break on !
		return true;
	}
	
	// Check if a 20 bit boolean array has the collect Golay (20,8) coding
	boolean checkGolay208 (boolean[] word)	{
		int a,val;
		final int[]GolayNums={};
		
		if (word[19]==true) val=1;
		else val=0;
		if (word[18]==true) val=val+2;
		if (word[17]==true) val=val+4;
		if (word[16]==true) val=val+8;
		if (word[15]==true) val=val+16;
		if (word[14]==true) val=val+32;
		if (word[13]==true) val=val+64;
		if (word[12]==true) val=val+128;
		if (word[11]==true) val=val+256;
		if (word[10]==true) val=val+512;
		if (word[9]==true) val=val+1024;
		if (word[8]==true) val=val+2048;
		if (word[7]==true) val=val+4096;
		if (word[6]==true) val=val+8192;
		if (word[5]==true) val=val+16384;
		if (word[4]==true) val=val+32768;
		if (word[3]==true) val=val+65536;
		if (word[2]==true) val=val+131072;
		if (word[1]==true) val=val+262144;
		if (word[0]==true) val=val+524288;
		// Run through the possible values
		for (a=0;a<256;a++)	{
			if (val==GolayNums[a]) return true;
		}
		return false;
	}
	
	
}
