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
		// Clear the data list
		theApp.clearIncomingDataList();
		return display;
	}
	
	// Decode a half rate packet
	public String[] decodeHalfRate (boolean bits[])	{
		Utilities utils=new Utilities();
		int a;
		for (a=0;a<bits.length;a=a+8)	{
			int td=utils.retEight(bits,a);
			theApp.addToIncomingDataList(td);
		}
		decodeList();
		return display;
	}
	
	// Unified Data Transport
	private void udt (boolean bits[])	{
		display[0]="Unified Data Transport";
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
		// Display this
		sb.append(Integer.toString(blocks)+" blocks follow : FSN="+Integer.toString(fsn));
		display[2]=sb.toString();
	}
	
	// Confirmed Data
	private void confirmedData (boolean bits[])	{
		display[0]="Confirmed Data";
	}
	
	// Defined Short Data
	private void definedShortData (boolean bits[])	{
		display[0]="Defined Short Data";
	}
	
	// Raw Short Data
	private void rawShortData (boolean bits[])	{
		display[0]="Raw or Status Short Data";
	}
	
	// Proprietary Data Packet
	private void propData (boolean bits[])	{
		Utilities utils=new Utilities();
		StringBuilder sa=new StringBuilder(250);
		int mfid=utils.retEight(bits,8);
		display[0]="Proprietary Data : MFID="+Integer.toString(mfid)+" ("+utils.returnMFIDName(mfid)+")";
		// Display proprietary data as binary
		int a;
		for (a=16;a<64;a++)	{
			if (bits[a]==true) sa.append("1");
			else sa.append("0");
		}
		display[1]=sa.toString();
	}
	
	
	// Unknown Data
	private void unknownData (boolean bits[],int dpf)	{
		display[0]="Unknown Data : DPF="+Integer.toString(dpf);
	}
	
	// Try to recognise the incoming data and decode it if possible 
	private void decodeList()	{
		
	}
		
}
