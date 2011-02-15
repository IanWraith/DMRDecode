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
		display[0]="<b>Group Voice Channel User LC</b>";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Group address
		int group=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		display[2]="<b>Group Address : "+Integer.toString(group);
		display[2]=display[2]+" Source Address : "+Integer.toString(source)+"</b>";
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
		
	}
	
	// Unit to Unit Voice Channel User LC
	void uu_v_ch_usr (DMRDecode theApp,boolean bits[])	{
		int index;
		display[0]="<b>Unit to Unit Voice Channel User LC</b>";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Target address
		int target=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		display[2]="<b>Target Address : "+Integer.toString(target);
		display[2]=display[2]+" Source Address : "+Integer.toString(source)+"</b>";
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1) theApp.usersLogged.setAsUnitUser(index);
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1) theApp.usersLogged.setAsUnitUser(index);
	}
	
	// Terminator Data Link Control PDU
	void td_lc (DMRDecode theApp,boolean bits[])	{
		int index;
		display[0]="<b>Terminator Data Link Control PDU</b>";
		// Destination LLID
		int dllid=retAddress(bits,16);
		// Source LLID
		int sllid=retAddress(bits,40);
		display[1]="<b>Destination Logical Link ID : "+Integer.toString(dllid);
		display[1]=display[1]+" Source Logical Link ID : "+Integer.toString(sllid)+"</b>";
		// Log these users
		// Destination
		theApp.usersLogged.addUser(dllid);
		index=theApp.usersLogged.findUserIndex(dllid);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
		// Source
		theApp.usersLogged.addUser(sllid);
		index=theApp.usersLogged.findUserIndex(sllid);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
	}
	
	// Handle unknown Full Link Control types
	private void unknown_flc (int flco,int fid,boolean bits[])	{
		int a;
		display[0]="<b>Unknown Full Link Control LC : FLCO="+Integer.toString(flco)+" + FID="+Integer.toString(fid)+" ";
		// Display the binary
		for (a=16;a<72;a++)	{
			if (bits[a]==true) display[0]=display[0]+"1";
			else display[0]=display[0]+"0";
		}
		display[0]=display[0]+"</b>";
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
		String so="<b>Service Options : ";
		// Emergency
		if (bits[offset]==false) so=so+"Non-emergency";
		else so=so+"Emergency";
		// Privacy
		if (bits[offset+1]==true) so=so+"/Privacy Enabled";
		// +2 and +3 are reserved bits
		// +4 is Broadcast
		if (bits[offset+4]==true) so=so+"/Broadcast";
		// +5 is OVCM
		if (bits[offset+5]==true) so=so+"/OVCM Call";
		// 6 and 7 are priority
		if (bits[offset+6]==true) priority=2;
		else priority=0;
		if (bits[offset+7]==true) priority++;
		if (priority==0) so=so+"/No priority";
		else so=so+"/Priority "+Integer.toString(priority);
        so=so+"</b>";
		return so;
	}
}
