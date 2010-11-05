package com.dmr;

public class DMRVoice {
	private int dibit_buf[]=new int[144];
	private String line[]=new String[10];
	private boolean res;
	
	public String[] decode (DMRDecode theApp,int[] buf)	{
		String cline;
		DecodeCACH cachdecode=new DecodeCACH();
		dibit_buf=buf;
		line[0]=theApp.getTimeStamp()+" DMR Voice Frame ";
		// CACH decode
		cline=cachdecode.decode(theApp,dibit_buf);
		res=cachdecode.isPassErrorCheck();
		if (res==true) line[1]=cline;
		theApp.frameCount++;
		return line;
	}

	public boolean isError() {
		return res;
	}

}
