package com.dmr;

import java.awt.Color;
import java.awt.Font;

public class DMREmbedded {
	private int residueValue;
	private String line[]=new String[10];
	private Font fonts[]=new Font[10];
	private Color colours[]=new Color[10];
	private boolean resCACH,resEMB;
	private boolean shouldDisplay=true;
	private DMRDecode theApp;
	
	public String[] decode (DMRDecode TtheApp,byte[] dibit_buf)	{
		String cline;
		theApp=TtheApp;
		DecodeCACH cachdecode=new DecodeCACH();
		// CACH decode
		cline=cachdecode.decode(theApp,dibit_buf);
		resCACH=cachdecode.isPassErrorCheck();
		if (resCACH==true) {
			line[1]=cline;
			fonts[1]=theApp.italicFont;
			colours[1]=Color.BLACK;
			resEMB=EMBdecode(dibit_buf);
			// If short LC data is available then display it
			if (cachdecode.getShortLC()==true)	{
				line[7]=cachdecode.getShortLCline();
				fonts[7]=theApp.boldFont;
				if (cachdecode.getshortLCError()==true)	{
					colours[7]=Color.RED;
					if (theApp.isDisplayOnlyGoodFrames()==true) line[7]=null;
				}
				else colours[7]=Color.BLACK;
				cachdecode.clearShortLC();
			}
		}
		if ((resCACH==false)&&(resEMB==false)) theApp.embeddedFrameCount=8;
		theApp.frameCount++;
		return line;
	}

	// Has there been an error
	public boolean isError() {
		if ((resCACH==false)||(resEMB==false)) return false;
		else return true;
	}
	
	// Error check and decode the EMB
	private boolean EMBdecode(byte[] dibit_buf)	{
		int a,r,cc,lcss;
		boolean pi;
		boolean EMDdata[]=new boolean[16];
		// Convert from dibits into boolean
		// The EMB is broken into 2 parts either side of the embedded
		// these need reuniting into a single 20 bit boolean array
		r=0;
		for (a=66;a<70;a++)	{
			if (dibit_buf[a]==0)	{
				EMDdata[r]=false;
				EMDdata[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				EMDdata[r]=false;
				EMDdata[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				EMDdata[r]=true;
				EMDdata[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				EMDdata[r]=true;
				EMDdata[r+1]=true;
			}
			r=r+2;
		}
		for (a=86;a<90;a++)	{
			if (dibit_buf[a]==0)	{
				EMDdata[r]=false;
				EMDdata[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				EMDdata[r]=false;
				EMDdata[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				EMDdata[r]=true;
				EMDdata[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				EMDdata[r]=true;
				EMDdata[r+1]=true;
			}
			r=r+2;
		}
		// Error check the EMB
		// If it passes this is a Voice Burst with Embedded Signalling
		if (QuadResidue1676(EMDdata)==true)	{
			StringBuilder sb=new StringBuilder(250);
			line[0]=theApp.getTimeStamp()+" DMR Voice Frame with Embedded Signalling";
			colours[0]=Color.BLACK;
			fonts[0]=theApp.boldFont;
			// Colour code
			if (EMDdata[0]==true) cc=8;
			else cc=0;
			if (EMDdata[1]==true) cc=cc+4;
			if (EMDdata[2]==true) cc=cc+2;
			if (EMDdata[3]==true) cc=cc+1;
			// Update the colour code display
			if (theApp!=null) theApp.setColourCode(cc);
			// PI
			pi=EMDdata[4];
			// LCSS
			if (EMDdata[5]==true) lcss=2;
			else lcss=0;
			if (EMDdata[6]==true) lcss++;
			// Display the colour code
			sb.append("EMB : Colour Code "+Integer.toString(cc));
			// PI
			if (pi==true) sb.append(" : PI=1");
			// LCSS
			if (lcss==0) sb.append(" : Single fragment LC ");
			else if (lcss==1) sb.append(" : First fragment of LC ");
			else if (lcss==2) sb.append(" : Last fragment of LC");
			else if (lcss==3) sb.append(" : Continuation fragment of LC");
			line[2]=sb.toString();
			// Add this to the embedded data class
			theApp.embedded_lc.addData(dibit_buf,lcss);
			// Is embedded data ready
			if (theApp.embedded_lc.getDataReady()==true)	{
				String elines[]=theApp.embedded_lc.getLines();
				// Display the embedded LC along with a timestamp
				line[3]=theApp.getTimeStamp()+" "+elines[0];
			}
			// Pass on voice data
			VoiceData voicedata=new VoiceData();
			voicedata.handleVoice(theApp,dibit_buf);
			// If the user doesn't want to see voice frames set shouldDisplay to false
			if (theApp.isDisplayVoiceFrames()==false) shouldDisplay=false;
			// Return all done
			return true;
		}
		else	{
			// Is this a Data Frame with Embedded signalling
			// See if its has a slot type field that passes its error check
			SlotType slottype=new SlotType();
			boolean SLOT_TYPEres,BPTCres=false;
			line[0]=theApp.getTimeStamp()+" DMR Data Frame with Embedded Signalling";
			colours[0]=Color.BLACK;
			fonts[0]=theApp.boldFont;
			line[2]=slottype.decode(theApp,dibit_buf);
			SLOT_TYPEres=slottype.isPassErrorCheck();
			// If the slot type is OK try to decode the rest
			if (SLOT_TYPEres==true)	{
				int dataType=slottype.returnDataType();
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
				if (dataType==1)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Voice LC Headers in Embedded Data Frames pass the Reed Solomon (12,9) error check 
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
				if (dataType==2)	{
					BPTC19696 bptc19696=new BPTC19696();
					if (bptc19696.decode(dibit_buf)==true)	{
						BPTCres=true;
						boolean bits[]=bptc19696.dataOut();
						// TODO : Ensure the Terminator LCs in Embedded Data Frames pass the Reed Solomon (12,9) error check 
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
				if (dataType==6)	{
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
				// Rate ½ Data Continuation
				if (dataType==7) BPTCres=true;
				// Rate ¾ Data Continuation
				if (dataType==8) BPTCres=true;
				// Idle
				// Error check this to detect problems with the data stream
				if (dataType==9)	{
					BPTC19696 bptc19696=new BPTC19696();
					BPTCres=bptc19696.decode(dibit_buf);
					// If we don't want to display these then clear the lines
					if (theApp.isDisplayIdlePDU()==false) shouldDisplay=false;
				}				
			}
			if ((SLOT_TYPEres==true)&&(BPTCres==true)) return true;
			else return false;
		}
	}
	
	// Code to calculate all valid values for Quadratic residue (16,7,6)
	boolean calcQuadResidue1676 ()	{
		boolean d[]=new boolean[7];
		boolean p[]=new boolean[9];
		int value[]=new int[128];
		int a;
		// Run through all possible 7 bit values
		for (a=0;a<128;a++){
			// Convert to binary
			if ((a&64)>0) d[0]=true;
			else d[0]=false;
			if ((a&32)>0) d[1]=true;
			else d[1]=false;
			if ((a&16)>0) d[2]=true;
			else d[2]=false;
			if ((a&8)>0) d[3]=true;
			else d[3]=false;
			if ((a&4)>0) d[4]=true;
			else d[4]=false;
			if ((a&2)>0) d[5]=true;
			else d[5]=false;
			if ((a&1)>0) d[6]=true;
			else d[6]=false;
			// Shift the value 9 times to the left
			value[a]=a<<9;
			// Calculate the parity bits
			p[0]=d[1]^d[2]^d[3]^d[4];
			p[1]=d[2]^d[3]^d[4]^d[5];
			p[2]=d[0]^d[3]^d[4]^d[5]^d[6];
			p[3]=d[2]^d[3]^d[5]^d[6];
			p[4]=d[1]^d[2]^d[6];
			p[5]=d[0]^d[1]^d[4];
			p[6]=d[0]^d[1]^d[2]^d[5];
			p[7]=d[0]^d[1]^d[2]^d[3]^d[6];
			p[8]=d[0]^d[2]^d[4]^d[5]^d[6];
			// Add these to the lower bits of the valid words
			if (p[0]==true) value[a]=value[a]+256;
			if (p[1]==true) value[a]=value[a]+128;
			if (p[2]==true) value[a]=value[a]+64;
			if (p[3]==true) value[a]=value[a]+32;
			if (p[4]==true) value[a]=value[a]+16;
			if (p[5]==true) value[a]=value[a]+8;
			if (p[6]==true) value[a]=value[a]+4;
			if (p[7]==true) value[a]=value[a]+2;
			if (p[8]==true) value[a]=value[a]+1;

		}
		// Just something to break on !
		return true;
	}
	
	// Check the EMB against a precomputed list of correct words
	boolean QuadResidue1676 (boolean[] word)	{
		int a;
		// A complete list of valid slot type words
		// This was generated by the calcQuadResidue1676() method
		final int[]ResidueNums={0,627,1253,1686,2505,3002,3372,3935,4578,5009,5383,6004,6187,
				6744,7374,7869,8631,9156,9554,10017,10366,10765,11419,12008,12373,12838,13488, 
				14019,14748,15343,15737,16138,16670,17261,17915,18312,18647,19108,19506,20033, 
				20732,21135,21529,22122,22837,23366,24016,24483,24745,25306,25676,26175,26976, 
				27411,28037,28662,29003,29496,30126,30685,30850,31473,31847,32276,32847,33340, 
				33962,34521,35206,35829,36195,36624,37293,37854,38216,38715,39012,39447,40065, 
				40690,41464,41867,42269,42862,43057,43586,44244,44711,45082,45673,46335,46732, 
				47571,48032,48438,48965,49489,49954,50612,51143,51352,51947,52349,52750,53427, 
				53952,54358,54821,55674,56073,56735,57324,57574,58005,58371,58992,59695,60252, 
				60874,61369,61700,62327,62945,63378,63693,64190,64552,65115};
		// Convert the boolean array into an integer
		if (word[15]==true) residueValue=1;
		else residueValue=0;
		if (word[14]==true) residueValue=residueValue+2;
		if (word[13]==true) residueValue=residueValue+4;
		if (word[12]==true) residueValue=residueValue+8;
		if (word[11]==true) residueValue=residueValue+16;
		if (word[10]==true) residueValue=residueValue+32;
		if (word[9]==true) residueValue=residueValue+64;
		if (word[8]==true) residueValue=residueValue+128;
		if (word[7]==true) residueValue=residueValue+256;
		if (word[6]==true) residueValue=residueValue+512;
		if (word[5]==true) residueValue=residueValue+1024;
		if (word[4]==true) residueValue=residueValue+2048;
		if (word[3]==true) residueValue=residueValue+4096;
		if (word[2]==true) residueValue=residueValue+8192;
		if (word[1]==true) residueValue=residueValue+16384;
		if (word[0]==true) residueValue=residueValue+32768;
		// Run through the possible values and we have a match return true
		for (a=0;a<128;a++)	{
			if (residueValue==ResidueNums[a]) return true;
		}
		// No matches so we must have a problem and so should return false
		return false;
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
