package com.dmr;

import java.awt.Color;
import java.awt.Font;

public class DMRDataDecode {
	private int dataType=-1;
	private String line[]=new String[10];
	private Font fonts[]=new Font[10];
	private Color colours[]=new Color[10];
	private boolean CACHres,SLOT_TYPEres,BPTCres;
	private boolean shouldDisplay=true;
	
	public String[] decode (DMRDecode theApp,byte[] dibit_buf)	{
		String cline;
		SlotType slottype=new SlotType();
		int mode=theApp.getMode();
		DecodeCACH cachdecode=new DecodeCACH();
		if (theApp.getSyncType()==40) line[0]=theApp.getTimeStamp()+" DMR Data Rest Frame";
		else line[0]=theApp.getTimeStamp()+" DMR Data Frame";
		if (mode==0) line[0]=line[0]+" (BS)";
		else if (mode==1) line[0]=line[0]+" (MS)";
		else if (mode==2) line[0]=line[0]+" (Direct)";
		colours[0]=Color.BLACK;
		fonts[0]=theApp.boldFont;
		// CACH decode
		if (mode==0)	{
			cline=cachdecode.decode(theApp,dibit_buf);
			CACHres=cachdecode.isPassErrorCheck();
		}
		else	{
			CACHres=true;
			cline=null;
		}
		// Slot Type Decode
		if (CACHres==true)	{
			if (mode==0)	{
				line[1]=cline;
				fonts[1]=theApp.italicFont;
				colours[1]=Color.BLACK;
			}
			line[2]=slottype.decode(theApp,dibit_buf);
			SLOT_TYPEres=slottype.isPassErrorCheck();
			// If short LC data is available then display it
			if ((cachdecode.getShortLC()==true)&&(mode==0))	{
				line[7]=cachdecode.getShortLCline();
				fonts[7]=theApp.boldFont;
				if (cachdecode.getshortLCError()==true)	{
					colours[7]=Color.RED;
					if (theApp.isDisplayOnlyGoodFrames()==true) line[7]=null;
				}
				else colours[7]=Color.BLACK;
				cachdecode.clearShortLC();
			}
			if (SLOT_TYPEres==true)	{
				fonts[2]=theApp.boldFont;
				colours[2]=Color.BLACK;
				// If no error then get the data type
				dataType=slottype.returnDataType();
				// Main section decode
				// PI Header
				if (dataType==0)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// Display the PI header bits as raw binary
						StringBuilder sb=new StringBuilder();
						int ai;
						for (ai=0;ai<bits.length;ai++)	{
							if (bits[ai]==true) sb.append("1");
							else sb.append("0");
						}
						line[3]=sb.toString();
						fonts[3]=theApp.boldFont;
						colours[3]=Color.BLACK;
					}
				}
				// Voice LC Header
				else if (dataType==1)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Voice LC Headers in Data Frames pass the Reed Solomon (12,9) error check 
						FullLinkControl flc=new FullLinkControl();
						String clines[]=new String[3];
						clines=flc.decode(theApp,bits);
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
						fonts[3]=theApp.boldFont;
						colours[3]=Color.BLACK;
						fonts[4]=theApp.boldFont;
						colours[4]=Color.BLACK;
						fonts[5]=theApp.boldFont;
						colours[5]=Color.BLACK;
						}
				}
				// Terminator with LC
				else if (dataType==2)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Terminator LCs in Data Frames pass the Reed Solomon (12,9) error check 
						FullLinkControl flc=new FullLinkControl();
						String clines[]=new String[3];
						clines=flc.decode(theApp,bits);
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
						fonts[3]=theApp.boldFont;
						colours[3]=Color.BLACK;
						fonts[4]=theApp.boldFont;
						colours[4]=Color.BLACK;
						fonts[5]=theApp.boldFont;
						colours[5]=Color.BLACK;
						}
				}
				// CSBK
				else if (dataType==3)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						crc tCRC=new crc();
						boolean bits[]=bptc19696.dataOut();
						// Does the CSBK pass its CRC test ?
						if (tCRC.crcCSBK(bits)==true)	{
							CSBK csbk=new CSBK();
							String clines[]=new String[3];
							BPTCres=true;
							clines=csbk.decode(theApp,bits);
							line[3]=clines[0];
							line[4]=clines[1];
							line[5]=clines[2];
							fonts[3]=theApp.boldFont;
							colours[3]=Color.BLACK;
							fonts[4]=theApp.boldFont;
							colours[4]=Color.BLACK;
							fonts[5]=theApp.boldFont;
							colours[5]=Color.BLACK;
						}
					}
				}
				// Data Header
				else if (dataType==6)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						crc tCRC=new crc();
						boolean bits[]=bptc19696.dataOut();
						// Does the Data Header pass its CRC test ?
						if (tCRC.crcDataHeader(bits)==true)	{
							String clines[]=new String[3];
							BPTCres=true;
							DMRData data=new DMRData(theApp);
							clines=data.decodeHeader(bits);
							line[3]=clines[0];
							line[4]=clines[1];
							line[5]=clines[2];
							fonts[3]=theApp.boldFont;
							colours[3]=Color.BLACK;
							fonts[4]=theApp.boldFont;
							colours[4]=Color.BLACK;
							fonts[5]=theApp.boldFont;
							colours[5]=Color.BLACK;
						}	
					}
				}
				// Rate � Data Continuation
				else if (dataType==7) {
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						boolean bits[]=bptc19696.dataOut();
						BPTCres=true;
						String clines[]=new String[3];
						DMRData data=new DMRData(theApp);
						clines=data.decodeHalfRate(bits);
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
						fonts[3]=theApp.boldFont;
						colours[3]=Color.BLACK;
						fonts[4]=theApp.boldFont;
						colours[4]=Color.BLACK;
						fonts[5]=theApp.boldFont;
						colours[5]=Color.BLACK;
					}
				}
				// Rate � Data Continuation
				else if (dataType==8)	{
					// Just use the BPTC19696 class to extract the raw binary from the dibit buffer
					BPTC19696 bptc19696=new BPTC19696();
					boolean bits[]=bptc19696.rawOut(dibit_buf);
					String clines[]=new String[3];
					DMRData data=new DMRData(theApp);
					clines=data.decodeThreeQuarterRate(bits);
					// If clines is null then we have an error
					if (clines==null)	{
						BPTCres=false;
					}
					else {
						BPTCres=true;
						line[3]=clines[0];
						line[4]=clines[1];
						line[5]=clines[2];
						fonts[3]=theApp.boldFont;
						colours[3]=Color.BLACK;
						fonts[4]=theApp.boldFont;
						colours[4]=Color.BLACK;
						fonts[5]=theApp.boldFont;
						colours[5]=Color.BLACK;
					}
				}
				// Idle
				// Error check this to detect problems with the data stream
				else if (dataType==9)	{
					BPTC19696 bptc19696=new BPTC19696();
					BPTCres=bptc19696.decode(dibit_buf);
					// If we don't want to display these then clear the lines
					if (theApp.isDisplayIdlePDU()==false) shouldDisplay=false;
				}
				
			}
			
		}
		
		theApp.frameCount++;
		return line;
	}

	// Inform the main class that there has been an error
	public boolean isError() {
	  if ((SLOT_TYPEres==true)&&(CACHres==true)&&(BPTCres==true)) return true;
	  else return false;
	}
	
	// Return the fonts in use
	public Font[] getFonts()	{
		return fonts;
	}
	
	// Return the colours in use
	public Color[] getColours()	{
		return colours;
	}
	
	// Tells the main program if this PDU should be displayed
	public boolean getShouldDisplay()	{
		return shouldDisplay;
	}
	
	public void setShouldDisplay (boolean b)	{
		shouldDisplay=b;
	}
	
}
