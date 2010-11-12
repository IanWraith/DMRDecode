package com.dmr;

public class DMRDataDecode {
	private byte dibit_buf[]=new byte[144];
	private int golayValue=-1;
	private String line[]=new String[10];
	private boolean CACHres,SLOT_TYPEres;
	
	public String[] decode (DMRDecode theApp,byte[] buf)	{
		String cline;
		DecodeCACH cachdecode=new DecodeCACH();
		SlotType slottype=new SlotType();
		dibit_buf=buf;
		line[0]="<b>"+theApp.getTimeStamp()+" DMR Data Frame </b>";
		// CACH decode
		cline=cachdecode.decode(theApp,dibit_buf);
		CACHres=cachdecode.isPassErrorCheck();
		if (CACHres==true) line[1]=cline;
		// Slot Type Decode
		if (CACHres==true)	{
			line[2]=slottype.decode(dibit_buf);
			SLOT_TYPEres=slottype.isPassErrorCheck();
			if (SLOT_TYPEres==false)	{
				golayValue=slottype.getGolayValue();
			}
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
