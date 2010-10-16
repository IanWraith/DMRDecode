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
	private boolean decodeCACH ()	{
		boolean rawdataCACH[]=new boolean[24];
		boolean dataCACH[]=new boolean[24];
		boolean tact[]=new boolean[7];
		//final int[]interleaveCACH={0,17,1,2,3,18,4,5,6,19,7,20,8,9,10,21,11,12,13,22,14,15,16,23};	
		final int[]interleaveCACH={23,16,15,14,22,13,12,11,21,10,9,8,20,7,19,6,5,4,18,3,2,1,17,0};
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
			r=interleaveCACH[a];
			dataCACH[a]=rawdataCACH[r];
		}
		
		// Display for diagnosic purposes
		line[1]="CACH : ";
		for (a=0;a<24;a++)	{
			if (dataCACH[a]==false) line[1]=line[1]+"0";
			 else line[1]=line[1]+"1";
		}
		
		// TACT
		// Extract the TACT and error check it
		for (a=23;a>16;a--)	{
			tact[a-17]=rawdataCACH[a];			
		}
		
		line[2]="TACT : ";
		for (a=0;a<7;a++)	{
			if (tact[a]==false) line[2]=line[2]+"0";
			 else  line[2]=line[2]+"1";
		}
				
		return true;
	}
	
	// Error check the CACH TACT
	public boolean errorCheckHamming743(boolean in[])	{
		
		return true;
	}
	
}
