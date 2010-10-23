package com.dmr;

public class DecodeCACH {

	private int dibit_buf[]=new int[132];
	private String line;
	private boolean at;
	private boolean channel;
	private int lcss;
	private boolean passErrorCheck=false;
	
	public String decode (int[] buf)	{
		dibit_buf=buf;
		line="CACH : TACT ";
		// CACH decode
		passErrorCheck=mainDecode();
		return line;
	}
	
	// De-interleave , CRC check and decode the CACH
	// With code added to work out which interleave sequence to use
	private boolean mainDecode ()	{
		int a,r,t1;
		boolean rawdataCACH[]=new boolean[24];
		boolean dataCACH[]=new boolean[24];
		boolean res;
		final int[]interleaveCACH={0,4,8,12,14,18,22,1,2,3,5,6,7,9,10,11,13,15,16,17,19,20,21,23};	
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
		//for (a=0;a<24;a++)	{
			//if (dataCACH[a]==false) line=line+"0";
			 //else line=line+"1";
			
			//if (a==3) line=line+" ";
			//if (a==6) line=line+" ";
		//}
		
		// Try the first and last 7 bits
		// First 7 bits straight
		if (dataCACH[0]==true) t1=64;
		else t1=0;
		if (dataCACH[1]==true) t1=t1+32;
		if (dataCACH[2]==true) t1=t1+16;
		if (dataCACH[3]==true) t1=t1+8;
		if (dataCACH[4]==true) t1=t1+4;
		if (dataCACH[5]==true) t1=t1+2;
		if (dataCACH[6]==true) t1=t1+1;
		res=errorCheckHamming743(t1);
		// Decode the TACT
		at=dataCACH[0];
		channel=dataCACH[1];
		if (dataCACH[2]==true) lcss=2;
		else lcss=0;
		if (dataCACH[3]==true) lcss++;
		// Display TACT info
		if (at==true) line=line+" AT=1";
		if (channel==false) line=line+" Ch 1";
		else line=line+" Ch 2";
		if (lcss==0) line=line+" First fragment of CBSK";
		else if (lcss==1) line=line+" First fragment of LC";
		else if (lcss==2) line=line+" Last fragment of LC or CSBK";
		else if (lcss==3) line=line+" Continuation fragment of LC or CSBK";
		
		return res;
	}
	
	// Error check the CACH TACT
	public boolean errorCheckHamming743(int tact)	{
		// An array of valid Hamming words
		final int[]Hamming743={0,11,22,29,39,44,49,58,69,78,83,88,98,105,116,127};
		int a;
		for (a=0;a<16;a++)	{
		 if (tact==Hamming743[a]) return true;	
		}
		return false;
	}
	
	// Generate a list of valid Hamming words
	// Isn't normally called but leave in for now
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
		
			h2=d1^d2^d3;
			h1=d2^d3^d4;
			h0=d1^d2^d4;
			
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
			if (h2==true) line=line+"1";
			 else line=line+"0";
			if (h1==true) line=line+"1";
			 else line=line+"0";
			if (h0==true) line=line+"1";
			 else line=line+"0";
			
			line=line+"\n";
		
		}
		a++;
	}

	// Let the main program now if there is an error in the frame
	public boolean isPassErrorCheck() {
		return passErrorCheck;
	}	
	
}
