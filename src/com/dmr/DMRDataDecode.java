package com.dmr;

public class DMRDataDecode {
	private int dibit_buf[]=new int[132];
	private String line[]=new String[10];
	private boolean error;
	
	public String[] decode (String timeStamp,int[] buf)	{
		DecodeCACH cachdecode=new DecodeCACH();
		dibit_buf=buf;
		line[0]=timeStamp+" DMR Data Frame ";
		// CACH decode
		line[0]=line[0]+" "+cachdecode.decode(dibit_buf);
		error=cachdecode.isPassErrorCheck();
		return line;
	}

	public boolean isError() {
		return error;
	}
	

}
