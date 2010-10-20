package com.dmr;

public class DMRDataDecode {
	private int dibit_buf[]=new int[132];
	private String line[]=new String[10];
	
	public String[] decode (String timeStamp,int[] buf)	{
		DecodeCACH cachdecode=new DecodeCACH();
		dibit_buf=buf;
		line[0]=timeStamp+" DMR Data Frame";
		// CACH decode
		line[1]=cachdecode.decode(dibit_buf);

		
		return line;
	}
	

}
