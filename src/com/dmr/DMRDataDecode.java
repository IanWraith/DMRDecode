package com.dmr;

public class DMRDataDecode {
	private int golayValue=-1,dataType=-1;
	private String line[]=new String[10];
	private boolean CACHres,SLOT_TYPEres,BPTCres;
	
	public String[] decode (DMRDecode theApp,byte[] dibit_buf)	{
		String cline;
		DecodeCACH cachdecode=new DecodeCACH();
		SlotType slottype=new SlotType();
		line[0]="<b>"+theApp.getTimeStamp()+" DMR Data Frame </b>";
		// CACH decode
		cline=cachdecode.decode(theApp,dibit_buf);
		CACHres=cachdecode.isPassErrorCheck();
		// Slot Type Decode
		if (CACHres==true)	{
			line[1]=cline;
			line[2]=slottype.decode(dibit_buf);
			SLOT_TYPEres=slottype.isPassErrorCheck();
			// If short LC data is available then display it
			if (cachdecode.getShortLC()==true)	{
				line[3]=cachdecode.getShortLCline();
				cachdecode.clearShortLC();
			}
			if (SLOT_TYPEres==false)	{
				golayValue=slottype.getGolayValue();
			}
			else	{
				// If no error then get the data type
				dataType=slottype.returnDataType();
				// Main section decode
				// CSBK
				if (dataType==3)	{
					BPTC19696 bptc19696=new BPTC19696();
					BPTCres=bptc19696.decode(dibit_buf);
				}

			}
			
		}
		
		theApp.frameCount++;
		return line;
	}

	public boolean isError() {
	  if ((SLOT_TYPEres==true)&&(CACHres==true)&&(BPTCres==true)) return true;
	  else return false;
	}
	
	public int getGolayValue()	{
		return golayValue;
	}
	
}
