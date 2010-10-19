package com.dmr;

public class DMRDataDecode {
	private int dibit_buf[]=new int[132];
	private String line[]=new String[10];
	private boolean inverted;
	private boolean at;
	private boolean channel;
	private int lcss;
	
	public String[] decode (String timeStamp,int[] buf,boolean inv)	{
		int i,dibit,currentslot;
		int cachdata[]=new int[13];
		dibit_buf=buf;
		line[0]=timeStamp+" DMR Data Frame";
		inverted=inv;
		// CACH decode
		decodeCACH();

		
		return line;
	}
	
	// De-interleave , CRC check and decode the CACH
	// With code added to work out which interleave sequence to use
	private boolean decodeCACH ()	{
		boolean rawdataCACH[]=new boolean[24];
		boolean dataCACH[]=new boolean[24];
		boolean tact[]=new boolean[7];
		final int[]interleaveCACH={0,4,8,12,14,18,22,1,2,3,5,6,7,9,10,11,13,15,16,17,19,20,21,23};	
		
		int a,r;
		// Convert from dibit into boolean
		r=0;
		for (a=0;a<12;a++)	{
			if (dibit_buf[a]==0)	{
				rawdataCACH[r]=false;
				rawdataCACH[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdataCACH[r]=false;
				rawdataCACH[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdataCACH[r]=true;
				rawdataCACH[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdataCACH[r]=true;
				rawdataCACH[r+1]=true;
			}
			r=r+2;
		}
		// De-interleave
		for (a=0;a<24;a++)	{
			r=interleaveCACH[a];
			dataCACH[a]=rawdataCACH[r];
		}
		
		// Display for diagnosic purposes
		line[1]="CACH : ";
		for (a=0;a<24;a++)	{
			if (dataCACH[a]==false) line[1]=line[1]+"0";
			 else line[1]=line[1]+"1";
		}

		// Try the first and last 7 bits
		int t1=0;
		boolean res;
		// First 7 bits straight
		if (dataCACH[0]==true) t1=t1+64;
		if (dataCACH[1]==true) t1=t1+32;
		if (dataCACH[2]==true) t1=t1+16;
		if (dataCACH[3]==true) t1=t1+8;
		if (dataCACH[4]==true) t1=t1+4;
		if (dataCACH[5]==true) t1=t1+2;
		if (dataCACH[6]==true) t1=t1+1;
		res=errorCheckHamming743(t1);
		line[2]="Test ";
		if (res==true) line[2]=line[2]+"PASS ";
		else line[2]=line[2]+"FAIL ";
		line[2]=line[2]+Integer.toString(t1);
		return true;
	}
	
	// Error check the CACH TACT
	public boolean errorCheckHamming743(int tact)	{
		// An array of valid Hamming words
		final int[]Hamming743={0,14,21,27,35,45,54,56,71,73,82,92,100,106,113,127};
		int a;
		for (a=0;a<16;a++)	{
		 if (tact==Hamming743[a]) return true;	
		}
		return false;
	}
	
	// Generate a list of valid Hammind words
	public void calcHamming ()	{
		boolean d1,d2,d3,d4,h2,h1,h0;
		int a;
		int valid[]=new int[16];
		
		String line="";
		
		for (a=0;a<16;a++)	{
			
			if ((a&8)>0) d1=true;
			else d1=false;
			if ((a&4)>0) d2=true;
			else d2=false;
			if ((a&2)>0) d3=true;
			else d3=false;
			if ((a&1)>0) d4=true;
			else d4=false;
		
			h2=d1^d3^d4;
			h1=d1^d2^d4;
			h0=d1^d2^d3;
	
			valid[a]=0;
			if (d1==true) valid[a]=valid[a]+64;
			if (d2==true) valid[a]=valid[a]+32;
			if (d3==true) valid[a]=valid[a]+16;
			if (d4==true) valid[a]=valid[a]+8;
			if (h2==true) valid[a]=valid[a]+4;
			if (h1==true) valid[a]=valid[a]+2;
			if (h0==true) valid[a]=valid[a]+1;
			
			if (d1==true) line=line+"1";
			 else line=line+"0";
			if (d2==true) line=line+"1";
			 else line=line+"0";
			if (d3==true) line=line+"1";
			 else line=line+"0";
			if (d4==true) line=line+"1";
			 else line=line+"0";
			line=line+" ";
			if (h0==true) line=line+"1";
			 else line=line+"0";
			if (h1==true) line=line+"1";
			 else line=line+"0";
			if (h2==true) line=line+"1";
			 else line=line+"0";
			
			line=line+"\n";
		
		}
		a++;
	}
	
}
