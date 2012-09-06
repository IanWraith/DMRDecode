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
		else if (flco==4) big_m_flco4(theApp,bits);
		else if (flco==48) td_lc(theApp,bits);
		else unknown_flc(flco,fid,bits);
		return display;
	}
	
	// Group Voice Channer User LC
	void group_v_ch_usr (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb=new StringBuilder(250);
		display[0]="Group Voice Channel User LC";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Group address
		int group=utils.retAddress(bits,24);
		// Source address
		int source=utils.retAddress(bits,48);
		sb.append("Group Address : "+Integer.toString(group));
		sb.append(" Source Address : "+Integer.toString(source));
		display[2]=sb.toString();
		// Log these users
		// Group
		if (theApp.usersLogged.addUser(group)==true)	{
			index=theApp.usersLogged.findUserIndex(group);
			if (index!=-1)	{
				theApp.usersLogged.setAsGroup(index);
				theApp.usersLogged.setChannel(index,theApp.currentChannel);
			}
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroupUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("Group Call to Group ");
		lab.append(Integer.toString(group));
		lab.append(" from ");
		lab.append(Integer.toString(source));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Group Voice Call to Group",group,source,theApp.currentChannel,display[1]);
	}
	
	// Unit to Unit Voice Channel User LC
	void uu_v_ch_usr (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb=new StringBuilder(250);
		display[0]="Unit to Unit Voice Channel User LC";
		// Service Options
		display[1]=decodeServiceOptions(bits,16);
		// Target address
		int target=utils.retAddress(bits,24);
		// Source address
		int source=utils.retAddress(bits,48);
		sb.append("Target Address : "+Integer.toString(target));
		sb.append(" Source Address : "+Integer.toString(source));
		display[2]=sb.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("Unit to Unit Call from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Unit to Unit Voice Call",target,source,theApp.currentChannel,display[1]);
	}
	
	// Terminator Data Link Control PDU
	void td_lc (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb=new StringBuilder(250);
		display[0]="Terminator Data Link Control PDU";
		// Destination LLID
		int dllid=utils.retAddress(bits,16);
		// Source LLID
		int sllid=utils.retAddress(bits,40);
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
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Terminator Data Link Control PDU",dllid,sllid,theApp.currentChannel,"");
	}
	
	// CP FLCO=4
	void big_m_flco4 (DMRDecode theApp,boolean bits[])	{
		int group,source,a,lcn,index;
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Capacity Plus Full Link Control LC : FLCO=4";
        // Group
		if (bits[40]==true) group=127;
		else group=0;
		if (bits[41]==true) group=group+64;
		if (bits[42]==true) group=group+32;
		if (bits[43]==true) group=group+16;
		if (bits[44]==true) group=group+8;
		if (bits[45]==true) group=group+4;
		if (bits[46]==true) group=group+2;
		if (bits[47]==true) group++;
		// Bits 48 - 51 ??
		// LCN
		if (bits[52]==true) lcn=8;
		else lcn=0;
		if (bits[53]==true) lcn=lcn+4;
		if (bits[54]==true) lcn=lcn+2;
		if (bits[55]==true) lcn++;
		// Source
		if (bits[56]==true) source=32768;
		else source=0;
		if (bits[57]==true) source=source+16384;
		if (bits[58]==true) source=source+8192;
		if (bits[59]==true) source=source+4096;
		if (bits[60]==true) source=source+2048;
		if (bits[61]==true) source=source+1024;
		if (bits[62]==true) source=source+512;
		if (bits[63]==true) source=source+256;
		if (bits[64]==true) source=source+128;
		if (bits[65]==true) source=source+64;
		if (bits[66]==true) source=source+32;
		if (bits[67]==true) source=source+16;
		if (bits[68]==true) source=source+8;
		if (bits[69]==true) source=source+4;
		if (bits[70]==true) source=source+2;
		if (bits[71]==true) source++;
 		// Make up the 2nd line
		sb1.append("Group Address "+Integer.toString(group)+" Source Address "+Integer.toString(source)+" LCN "+Integer.toString(lcn));
		display[1]=sb1.toString();
		// Log these users
		// Group
		if (theApp.usersLogged.addUser(group)==true)	{
			index=theApp.usersLogged.findUserIndex(group);
			if (index!=-1)	{
				theApp.usersLogged.setAsGroup(index);
				theApp.usersLogged.setChannel(index,theApp.currentChannel);
			}
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroupUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
		// Display the full binary on the bottom line if in debug mode
		if (theApp.isDebug()==true)	{
			for (a=16;a<72;a++)	{
				if (bits[a]==true) sb2.append("1");
				else sb2.append("0");
			}
		display[2]=sb2.toString();
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("CP Group Call to Group ");
		lab.append(Integer.toString(group));
		lab.append(" from ");
		lab.append(Integer.toString(source));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Capacity Plus Full Link Control LC",group,source,theApp.currentChannel,"");
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
