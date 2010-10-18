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
		boolean dataCACH1[]=new boolean[24];
		boolean dataCACH2[]=new boolean[24];
		boolean tact1[]=new boolean[7];
		boolean tact2[]=new boolean[7];
		final int[]interleaveCACH1={0,17,1,2,3,18,4,5,6,19,7,20,8,9,10,21,11,12,13,22,14,15,16,23};	
		final int[]interleaveCACH2={23,16,15,14,22,13,12,11,21,10,9,8,20,7,19,6,5,4,18,3,2,1,17,0};
		int a,b=0,r;
		// Convert from dibit into boolean
		for (a=0;a<12;a++)	{
			r=(1&(dibit_buf[a]>>1));
			if (r==0) rawdataCACH[b]=false;
			 else rawdataCACH[b]=true;
			b++;
			r=1&dibit_buf[a];
			if (r==0) rawdataCACH[b]=false;
			 else rawdataCACH[b]=true;
			b++;
		}
		// De-interleave
		for (a=0;a<24;a++)	{
			r=interleaveCACH1[a];
			dataCACH1[a]=rawdataCACH[r];
			r=interleaveCACH2[a];
			dataCACH2[a]=rawdataCACH[r];
		}
		
		// Display for diagnosic purposes
		//line[1]="CACH : ";
		//for (a=0;a<24;a++)	{
		//	if (dataCACH[a]==false) line[1]=line[1]+"0";
		//	 else line[1]=line[1]+"1";
		//}
		
		// TACT
		// Extract the TACT and error check it
		for (a=23;a>16;a--)	{
			tact1[a-17]=dataCACH1[a];	
			tact2[a-17]=dataCACH2[a];	
		}
		
		int t1=0;
		if (tact1[0]==true) t1=t1+64;
		if (tact1[1]==true) t1=t1+32;
		if (tact1[2]==true) t1=t1+16;
		if (tact1[3]==true) t1=t1+8;
		if (tact1[4]==true) t1=t1+4;
		if (tact1[5]==true) t1=t1+2;
		if (tact1[6]==true) t1=t1+1;
		boolean res=errorCheckHamming743(t1);
		
		line[2]="TACT1 : ";
		for (a=0;a<7;a++)	{
			if (tact1[a]==false) line[2]=line[2]+"0";
			 else  line[2]=line[2]+"1";
		}
		if (res==true) line[2]=line[2]+" PASS";
		 else line[2]=line[2]+" FAIL";
		
		t1=0;
		if (tact2[0]==true) t1=t1+64;
		if (tact2[1]==true) t1=t1+32;
		if (tact2[2]==true) t1=t1+16;
		if (tact2[3]==true) t1=t1+8;
		if (tact2[4]==true) t1=t1+4;
		if (tact2[5]==true) t1=t1+2;
		if (tact2[6]==true) t1=t1+1;
		res=errorCheckHamming743(t1);
		line[3]="TACT2 : ";
		for (a=0;a<7;a++)	{
			if (tact2[a]==false) line[3]=line[3]+"0";
			 else  line[3]=line[3]+"1";
		}
		if (res==true) line[3]=line[3]+" PASS";
		 else line[3]=line[3]+" FAIL";		
		
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
			
		}
		a++;
	}
	
}
