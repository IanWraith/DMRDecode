package com.dmr;

public class FullLinkControl {
	boolean pf;
	private String display[]=new String[3];
	
	// The main decode method
	public String[] decode (boolean bits[]) 	{
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
		if (flco==0) group_v_ch_usr(bits);
		else if (flco==3) uu_v_ch_usr(bits);
		else if (flco==48) td_lc(bits);
		else unknown_flc(flco,fid,bits);
		return display;
	}
	
	// Group Voice Channer User LC
	void group_v_ch_usr (boolean bits[])	{
		display[0]="<b>Group Voice Channel User LC</b>";
		// Group address
		int group=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		display[1]="<b>Group Address : "+Integer.toString(group);
		display[1]=display[1]+" Source Address : "+Integer.toString(source)+"</b>";
	}
	
	// Unit to Unit Voice Channel User LC
	void uu_v_ch_usr (boolean bits[])	{
		display[0]="<b>Unit to Unit Voice Channel User LC</b>";
		// Target address
		int target=retAddress(bits,24);
		// Source address
		int source=retAddress(bits,48);
		display[1]="<b>Target Address : "+Integer.toString(target);
		display[1]=display[1]+" Source Address : "+Integer.toString(source)+"</b>";
	}
	
	// Terminator Data Link Control PDU
	void td_lc (boolean bits[])	{
		display[0]="<b>Terminator Data Link Control PDU</b>";
		// Destination LLID
		int dllid=retAddress(bits,16);
		// Source LLID
		int sllid=retAddress(bits,40);
		display[1]="<b>Destination Logical Link ID : "+Integer.toString(dllid);
		display[1]=display[1]+" Source Logical Link ID : "+Integer.toString(sllid)+"</b>";
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
}
