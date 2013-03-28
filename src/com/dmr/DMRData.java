package com.dmr;

public class DMRData {
	private String display[]=new String[3];
	private DMRDecode theApp;
	
	public DMRData (DMRDecode tapp)	{
		theApp=tapp;
	}
	
	// The header decode method
	public String[] decodeHeader (boolean bits[])	{
		int dpf;
		// Data Packet Format
		if (bits[4]==true) dpf=8;
		else dpf=0;
		if (bits[5]==true) dpf=dpf+4;
		if (bits[6]==true) dpf=dpf+2;
		if (bits[7]==true) dpf++;
		// Types
		if (dpf==0) udt(bits);
		else if (dpf==1) responsePacket(bits);
		else if (dpf==2) unconfirmedData(bits);
		else if (dpf==3) confirmedData(bits);
		else if (dpf==13) definedShortData(bits);
		else if (dpf==14) rawShortData(bits);
		else if (dpf==15) propData(bits);
		else unknownData(bits,dpf);
		return display;
	}
	
	// Decode a half rate packet
	public String[] decodeHalfRate (boolean bits[])	{
		// Increment the blocks received counter
		theApp.incrementCurrentDataBlocksReceived();
		// Depending on the data type handle this in different ways
		if (theApp.getCurrentIncomingDataType()==2)	{
			handleConfirmedData(bits);
			return display;
		}
		
		// TODO : Handle other types of rate 1/2 data
		
		// Just display the rest as binary for now
		StringBuilder sb=new StringBuilder(250);
		int a;
		for (a=0;a<bits.length;a++)	{
			if (bits[a]==true) sb.append("1");
			else sb.append("0");
		}
		display[0]=sb.toString();
		
		return display;
	}
	
	// Decode a three quarter rate packet
	public String[] decodeThreeQuarterRate (boolean bits[])	{
		// Create a Trellis object
		Trellis trellis=new Trellis();
		boolean threeQuarterOut[]=trellis.decode(bits);
		// Increment the blocks received counter
		theApp.incrementCurrentDataBlocksReceived();
		// If this is null then there is an error so return null
		if (threeQuarterOut==null) return null;
		// Depending on the data type handle this in different ways
		if (theApp.getCurrentIncomingDataType()==2)	{
			handleConfirmedData(threeQuarterOut);
			return display;
		}
		
		// TODO : Handle other types of rate 3/4 data
		
		// Just display the rest as binary for now
		StringBuilder sb=new StringBuilder(250);
		int a;
		for (a=0;a<threeQuarterOut.length;a++)	{
			if (threeQuarterOut[a]==true) sb.append("1");
			else sb.append("0");
		}
		display[0]=sb.toString();
		return display;
	}
	
	// Unified Data Transport
	private void udt (boolean bits[])	{
		display[0]="Unified Data Transport";
		// Set the data type
		theApp.setCurrentIncomingDataType(8);
	}
	
	// Response Packet
	private void responsePacket (boolean bits[])	{
		int blocks,dclass,status,type;
		Utilities utils=new Utilities();
		StringBuilder sa=new StringBuilder(250);
		StringBuilder sb=new StringBuilder(250);
		display[0]="Response Packet";
		// Destination LLID
		int dllid=utils.retAddress(bits,16);
		// Source LLID
		int sllid=utils.retAddress(bits,40);
		sa.append("Destination Logical Link ID : "+Integer.toString(dllid));
		sa.append(" Source Logical Link ID : "+Integer.toString(sllid));
		display[1]=sa.toString();
		// Bit 64 is 0
		// Blocks to follow
		if (bits[65]==true) blocks=64;
		else blocks=0;
		if (bits[66]==true) blocks=blocks+32;
		if (bits[67]==true) blocks=blocks+16;
		if (bits[68]==true) blocks=blocks+8;
		if (bits[69]==true) blocks=blocks+4;
		if (bits[70]==true) blocks=blocks+2;
		if (bits[71]==true) blocks++;
		// Class
		if (bits[72]==true) dclass=2;
		else dclass=0;
		if (bits[73]==true) dclass++;
		// Type
		if (bits[74]==true) type=4;
		else type=0;
		if (bits[75]==true) type=type+2;
		if (bits[76]==true) type++;
		// Status
		if (bits[77]==true) status=4;
		else status=0;
		if (bits[78]==true) status=status+2;
		if (bits[79]==true) status++;
		// Set the data type
		theApp.setCurrentIncomingDataType(3);
		// Set the number of blocks to follow
		theApp.setCurrentDataBlocksToFollow(blocks);
		// Display this
		sb.append(Integer.toString(blocks)+" blocks follow : ");
		if ((dclass==0)&&(type==1)) sb.append("ACK");
		else if ((dclass==1)&&(type==0)) sb.append("NACK (Illegal Format)");
		else if ((dclass==1)&&(type==1)) sb.append("NACK (CRC Failed)");
		else if ((dclass==1)&&(type==2)) sb.append("NACK (Memory Full)");
		else if ((dclass==1)&&(type==4)) sb.append("NACK (Undeliverable)");
		else if ((dclass==2)&&(type==0)) sb.append("SACK");
		else sb.append(" Unknown C="+Integer.toString(dclass)+" T="+Integer.toString(type)+" S="+Integer.toString(status));
		display[2]=sb.toString();
	}
	
	// Unconfirmed Data
	private void unconfirmedData (boolean bits[])	{
		int blocks,fsn;
		Utilities utils=new Utilities();
		StringBuilder sa=new StringBuilder(250);
		StringBuilder sb=new StringBuilder(250);
		display[0]="Unconfirmed Data";
		// Destination LLID
		int dllid=utils.retAddress(bits,16);
		// Source LLID
		int sllid=utils.retAddress(bits,40);
		sa.append("Destination Logical Link ID : "+Integer.toString(dllid));
		sa.append(" Source Logical Link ID : "+Integer.toString(sllid));
		display[1]=sa.toString();
		// Bit 64 is 0
		// Blocks to follow
		if (bits[65]==true) blocks=64;
		else blocks=0;
		if (bits[66]==true) blocks=blocks+32;
		if (bits[67]==true) blocks=blocks+16;
		if (bits[68]==true) blocks=blocks+8;
		if (bits[69]==true) blocks=blocks+4;
		if (bits[70]==true) blocks=blocks+2;
		if (bits[71]==true) blocks++;
		// Bits 72,73,74 and 75 are 0
		// FSN
		if (bits[76]==true) fsn=8;
		else fsn=0;
		if (bits[77]==true) fsn=fsn+4;
		if (bits[78]==true) fsn=fsn+2;
		if (bits[79]==true) fsn++;
		// Set the data type
		theApp.setCurrentIncomingDataType(1);
		// Set the blocks to follow
		theApp.setCurrentDataBlocksToFollow(blocks);
		// Display this
		sb.append(Integer.toString(blocks)+" blocks follow : FSN="+Integer.toString(fsn));
		display[2]=sb.toString();
	}
	
	// Confirmed Data
	private void confirmedData (boolean bits[])	{
		int blocks,fsn,ns;
		Utilities utils=new Utilities();
		StringBuilder sa=new StringBuilder(250);
		StringBuilder sb=new StringBuilder(250);
		display[0]="Confirmed Data";
		// Destination LLID
		int dllid=utils.retAddress(bits,16);
		// Source LLID
		int sllid=utils.retAddress(bits,40);
		sa.append("Destination Logical Link ID : "+Integer.toString(dllid));
		sa.append(" Source Logical Link ID : "+Integer.toString(sllid));
		display[1]=sa.toString();
		// Bit 64 is F
		// Blocks to follow
		if (bits[65]==true) blocks=64;
		else blocks=0;
		if (bits[66]==true) blocks=blocks+32;
		if (bits[67]==true) blocks=blocks+16;
		if (bits[68]==true) blocks=blocks+8;
		if (bits[69]==true) blocks=blocks+4;
		if (bits[70]==true) blocks=blocks+2;
		if (bits[71]==true) blocks++;
		// Bits 72 is S
		// Bits 73,74,75 are N(S)
		if (bits[73]==true) ns=4;
		else ns=0;
		if (bits[74]==true) ns=ns+2;
		if (bits[75]==true) ns++;
		// FSN
		if (bits[76]==true) fsn=8;
		else fsn=0;
		if (bits[77]==true) fsn=fsn+4;
		if (bits[78]==true) fsn=fsn+2;
		if (bits[79]==true) fsn++;
		// Set the data type
		theApp.setCurrentIncomingDataType(2);
		// Set the blocks to follow
		theApp.setCurrentDataBlocksToFollow(blocks);
		// Display this
		sb.append(Integer.toString(blocks)+" blocks follow : FSN="+Integer.toString(fsn)+" N(S)="+Integer.toString(ns));
		display[2]=sb.toString();		
	}
	
	// Defined Short Data
	private void definedShortData (boolean bits[])	{
		display[0]="Defined Short Data";
		// Set the data type
		theApp.setCurrentIncomingDataType(7);
	}
	
	// Raw Short Data
	private void rawShortData (boolean bits[])	{
		display[0]="Raw or Status Short Data";
		// Set the data type
		theApp.setCurrentIncomingDataType(6);
	}
	
	// Proprietary Data Packet
	private void propData (boolean bits[])	{
		Utilities utils=new Utilities();
		StringBuilder sa=new StringBuilder(250);
		int mfid=utils.retEight(bits,8);
		display[0]="Proprietary Data : MFID="+Integer.toString(mfid)+" ("+utils.returnMFIDName(mfid)+")";
		// Display proprietary data as binary
		int a;
		for (a=16;a<80;a++)	{
			if (bits[a]==true) sa.append("1");
			else sa.append("0");
		}
		display[1]=sa.toString();
		// Set the data type
		theApp.setCurrentIncomingDataType(4);	
	}
	
	
	// Unknown Data
	private void unknownData (boolean bits[],int dpf)	{
		display[0]="Unknown Data : DPF="+Integer.toString(dpf);
	}
	
	// Handle confirmed data 
	private void handleConfirmedData (boolean bits[])	{
		int dbsn,crc;
		// Data block serial number 
		// bits 0,1,2,3,4,5,6
		if (bits[0]==true) dbsn=64;
		else dbsn=0;
		if (bits[1]==true) dbsn=dbsn+32;
		if (bits[2]==true) dbsn=dbsn+16;
		if (bits[3]==true) dbsn=dbsn+8;
		if (bits[4]==true) dbsn=dbsn+4;
		if (bits[5]==true) dbsn=dbsn+2;
		if (bits[6]==true) dbsn++;
		// 9 bit CRC
		// bits 7,8,9,10,11,12,13,14,15
		if (bits[7]==true) crc=256;
		else crc=0;
		if (bits[8]==true) crc=crc+128;
		if (bits[9]==true) crc=crc+64;
		if (bits[10]==true) crc=crc+32;
		if (bits[11]==true) crc=crc+16;
		if (bits[12]==true) crc=crc+8;
		if (bits[13]==true) crc=crc+4;
		if (bits[14]==true) crc=crc+2;
		if (bits[15]==true) crc++;
		// If 96 bits this is R_1_2_DATA
		if (bits.length==96) display[0]="R_1_2_DATA (data block serial number="+Integer.toString(dbsn)+")";
		else if (bits.length==144) display[0]="R_3_4_DATA (data block serial number="+Integer.toString(dbsn)+")";
		// Display the payload as hex for now
		Utilities utils=new Utilities();
		int a;
		for (a=16;a<bits.length;a=a+8)	{
			int td=utils.retEight(bits,a);
			if (a==16) display[1]="0x"+Integer.toHexString(td);
			else display[1]=display[1]+",0x"+Integer.toHexString(td);
		}
	}
		
}
