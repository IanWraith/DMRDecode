package com.dmr;

public class DMRVoice {
	private String line[]=new String[10];
	private boolean res;
	private DecodeCACH cachdecode=new DecodeCACH();
	
	public String[] decode (DMRDecode theApp,byte[] dibit_buf)	{
		String cline;
		line[0]="<b>"+theApp.getTimeStamp()+" DMR Voice Frame </b>";
		// CACH decode
		cline=cachdecode.decode(theApp,dibit_buf);
		res=cachdecode.isPassErrorCheck();
		if (res==true)	{
			line[1]=cline;
			// If short LC data is available then display it
			if (cachdecode.getShortLC()==true)	{
				line[7]=cachdecode.getShortLCline();
				cachdecode.clearShortLC();
			}
		}
		theApp.frameCount++;
		return line;
	}

	public boolean isError() {
		return res;
	}

}
