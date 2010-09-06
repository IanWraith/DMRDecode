package com.dmr;

public class DMRDataDecode {
	private int dibit_buf[]=new int[132];
	private String line[]=new String[10];
	private Boolean inverted_dmr;
	
	public String[] decode (String timeStamp,int[] buf,boolean inv)	{
		int i,dibit,currentslot;
		int cachdata[]=new int[13];
		dibit_buf=buf;
		line[0]=timeStamp+" DMR Data Frame";
		inverted_dmr=inv;
		line[1]="CACH ";
		
		// CACH decode
		for (i=0;i<12;i++)	{
			dibit=dibit_buf[i];
		    if (inverted_dmr==true) dibit=(dibit^2);
		    cachdata[i]=dibit;
		    if (i==2) currentslot=(1&(dibit>>1));    
		    }
		  cachdata[12]=0;

		
		return line;
	}
	
}
