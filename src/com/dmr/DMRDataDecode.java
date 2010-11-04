package com.dmr;

public class DMRDataDecode {
	private int dibit_buf[]=new int[144];
	private int golayValue=-1;
	private String line[]=new String[10];
	private boolean CACHres,SLOT_TYPEres;
	
	public String[] decode (DMRDecode theApp,int[] buf)	{
		String cline;
		DecodeCACH cachdecode=new DecodeCACH();
		SlotType slottype=new SlotType();
		dibit_buf=buf;
		line[0]=theApp.getTimeStamp()+" DMR Data Frame ";
		// CACH decode
		cline=cachdecode.decode(dibit_buf);
		CACHres=cachdecode.isPassErrorCheck();
		if (CACHres==true) line[1]=cline;
		// Slot Type Decode
		if (CACHres==true)	{
			line[2]=slottype.decode(dibit_buf);
			SLOT_TYPEres=slottype.isPassErrorCheck();
			if (SLOT_TYPEres==false) golayValue=slottype.getGolayValue();
		}
		theApp.frameCount++;
		return line;
	}

	public boolean isError() {
	  if ((SLOT_TYPEres==true)&&(CACHres==true)) return true;
	  else return false;
	}
	
	public int getGolayValue()	{
		return golayValue;
	}
	
}
