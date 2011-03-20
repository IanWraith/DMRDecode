package com.dmr;

public class FullLinkControl {
	boolean pf;
	private String display[]=new String[3];
	
	// The main decode method
	public String[] decode (DMRDecode theApp,boolean bits[]) 	{
		int flco,fid,service;
		// PF
		pf=bits[0];
		// Bit 1 is reserved
		pf=bits[1];
		// FLCO
		if (bits[2]==true) flco=32;
		else flco=0;
		if (bits[3]==true) flco=flco+16;
		if (bits[4]==true) flco=flco+8;
		if (bits[5]==true) flco=flco+4;
		if (bits[6]==true) flco=flco+2;
		if (bits[7]==true) flco++;
		// FID
		if (bits[8]==true) fid=128;
		else fid=0;
		if (bits[9]==true) fid=fid+64;
		if (bits[10]==true) fid=fid+32;
		if (bits[11]==true) fid=fid+16;
		if (bits[12]==true) fid=fid+8;
		if (bits[13]==true) fid=fid+4;
		if (bits[14]==true) fid=fid+2;
		if (bits[15]==true) fid++;
		// Service options
		if (bits[16]==true) service=128;
		else service=0;
		if (bits[17]==true) service=service+64;
		if (bits[18]==true) service=service+32;
		if (bits[19]==true) service=service+16;
		if (bits[20]==true) service=service+8;
		if (bits[21]==true) service=service+4;
		if (bits[22]==true) service=service+2;
		if (bits[23]==true) service++;
		// PDU types 
		if (flco==0) group_v_ch_usr(theApp,bits);
		else if (flco==3) uu_v_ch_usr(theApp,bits);
		else if (flco==48) td_lc(theApp,bits);
		else unknown_flc(flco,fid,bits);
		return display;
	}
	
	// Group Voice Channer User LC
	void group_v_ch_usr (DMRDecode theApp,boolean bits[])	{
		int index;
		StringBuilder sb=new StringBuilder(250);
		display[0]="Group Voice Channel User LC";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Group address
		int group=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		sb.append("Group Address : "+Integer.toString(group));
		sb.append(" Source Address : "+Integer.toString(source));
		display[2]=sb.toString();
		// Log these users
		// Group
		if (theApp.usersLogged.addUser(group)==true)	{
			index=theApp.usersLogged.findUserIndex(group);
			if (index!=-1) theApp.usersLogged.setAsGroup(index);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1) theApp.usersLogged.setAsGroupUser(index);
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("Group Call to Group ");
		lab.append(Integer.toString(group));
		lab.append(" from ");
		lab.append(Integer.toString(source));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
	}
	
	// Unit to Unit Voice Channel User LC
	void uu_v_ch_usr (DMRDecode theApp,boolean bits[])	{
		int index;
		StringBuilder sb=new StringBuilder(250);
		display[0]="Unit to Unit Voice Channel User LC";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Target address
		int target=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		sb.append("Target Address : "+Integer.toString(target));
		sb.append(" Source Address : "+Integer.toString(source));
		display[2]=sb.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1) theApp.usersLogged.setAsUnitUser(index);
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1) theApp.usersLogged.setAsUnitUser(index);
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("Unit to Unit Call from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
	}
	
	// Terminator Data Link Control PDU
	void td_lc (DMRDecode theApp,boolean bits[])	{
		int index;
		StringBuilder sb=new StringBuilder(250);
		display[0]="Terminator Data Link Control PDU";
		// Destination LLID
		int dllid=retAddress(bits,16);
		// Source LLID
		int sllid=retAddress(bits,40);
		sb.append("Destination Logical Link ID : "+Integer.toString(dllid));
		sb.append(" Source Logical Link ID : "+Integer.toString(sllid));
		display[1]=sb.toString();
		// Log these users
		// Destination
		theApp.usersLogged.addUser(dllid);
		index=theApp.usersLogged.findUserIndex(dllid);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
		// Source
		theApp.usersLogged.addUser(sllid);
		index=theApp.usersLogged.findUserIndex(sllid);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("Data Call from ");
		lab.append(Integer.toString(sllid));
		lab.append(" to ");
		lab.append(Integer.toString(dllid));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
	}
	
	// Handle unknown Full Link Control types
	private void unknown_flc (int flco,int fid,boolean bits[])	{
		int a;
		StringBuilder sb=new StringBuilder(300);
	    sb.append("Unknown Full Link Control LC : FLCO="+Integer.toString(flco)+" + FID="+Integer.toString(fid)+" ");
		// Display the binary
		for (a=16;a<72;a++)	{
			if (bits[a]==true) sb.append("1");
			else sb.append("0");
		}
		display[0]=sb.toString();
	}
	
	// Return a 24 bit address 
	private int retAddress (boolean bits[],int offset)	{
		int addr=0,a,b,c;
		for (a=0;a<24;a++)	{
			b=(24-a)-1;
			c=(int)Math.pow(2.0,b);
			if (bits[a+offset]==true) addr=addr+c;
		}
		return addr;
	}
	
	// Decode and display Service Options
	private String decodeServiceOptions (boolean bits[],int offset)	{
		int priority;
		StringBuilder so=new StringBuilder(300);
		so.append("Service Options : ");
		// Emergency
		if (bits[offset]==false) so.append("Non-emergency");
		else so.append("Emergency");
		// Privacy
		if (bits[offset+1]==true) so.append("/Privacy Enabled");
		// +2 and +3 are reserved bits
		// +4 is Broadcast
		if (bits[offset+4]==true) so.append("/Broadcast");
		// +5 is OVCM
		if (bits[offset+5]==true) so.append("/OVCM Call");
		// 6 and 7 are priority
		if (bits[offset+6]==true) priority=2;
		else priority=0;
		if (bits[offset+7]==true) priority++;
		if (priority==0) so.append("/No priority");
		else so.append("/Priority "+Integer.toString(priority));
		return so.toString();
	}
}
