package com.dmr;

public class DMRDataDecode {
	private int dataType=-1;
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
				line[7]=cachdecode.getShortLCline();
				cachdecode.clearShortLC();
			}
			if (SLOT_TYPEres==true)	{
				// If no error then get the data type
				dataType=slottype.returnDataType();
				// Main section decode
				// Voice LC Header
				if (dataType==1)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Voice LC Headers in Data Frames pass the Reed Solomon (12,9) error check 
						FullLinkControl flc=new FullLinkControl();
						String clines[]=new String[3];
						clines=flc.decode(bits);
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
						}
				}
				
				// Terminator with LC
				if (dataType==2)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Terminator LCs in Data Frames pass the Reed Solomon (12,9) error check 
						FullLinkControl flc=new FullLinkControl();
						String clines[]=new String[3];
						clines=flc.decode(bits);
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
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
							CSBK csbk=new CSBK();
							String clines[]=new String[3];
							BPTCres=true;
							clines=csbk.decode(bits);
							line[3]=clines[0];
							line[4]=clines[1];
							line[5]=clines[2];
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
							// TODO : Decode Data Headers
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
				
				// TODO : Decode Rate ½ Data Continuation frames
				// Rate ½ Data Continuation
				if (dataType==7) BPTCres=true;
				// TODO : Decode Rate ¾ Data Continuation frames
				// Rate ¾ Data Continuation
				if (dataType==8) BPTCres=true;
				// Idle
				// Don't waste time BPTC testing Idle data
				if (dataType==9) BPTCres=true;
				
			}
			
		}
		
		theApp.frameCount++;
		return line;
	}

	public boolean isError() {
	  if ((SLOT_TYPEres==true)&&(CACHres==true)&&(BPTCres==true)) return true;
	  else return false;
	}
	
}
