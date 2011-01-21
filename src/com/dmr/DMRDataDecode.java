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
				// Voice LC Header
				if (dataType==0)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
							int cc;
							line[3]="Voice LC Header : ";
							for (cc=0;cc<96;cc++)	{
								if (bits[cc]==false) line[3]=line[3]+"0";
								else line[3]=line[3]+"1";
							}
						}
				}
				
				// Terminator with LC
				if (dataType==2)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
							int cc;
							line[3]="Terminator with LC : ";
							for (cc=0;cc<96;cc++)	{
								if (bits[cc]==false) line[3]=line[3]+"0";
								else line[3]=line[3]+"1";
							}
						}
				}
				
				// CSBK
				if (dataType==3)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						crc tCRC=new crc();
						boolean bits[]=bptc19696.dataOut();
						// Does the CSBK pass its CRC test ?
						if (tCRC.crcCSBK(bits)==true)	{
							int cc;
							BPTCres=true;
							line[3]="CSBK : ";
							for (cc=0;cc<96;cc++)	{
								if (bits[cc]==false) line[3]=line[3]+"0";
								else line[3]=line[3]+"1";
							}
						}
					}
				}
				
				// Data Header
				if (dataType==6)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						crc tCRC=new crc();
						boolean bits[]=bptc19696.dataOut();
						// Does the Data Header pass its CRC test ?
						if (tCRC.crcDataHeader(bits)==true)	{
							int cc;
							BPTCres=true;
							line[3]="Data Header : ";
							for (cc=0;cc<96;cc++)	{
								if (bits[cc]==false) line[3]=line[3]+"0";
								else line[3]=line[3]+"1";
							}
						}	
					}
				}
				
				// Idle
				if (dataType==9)	{
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
