package com.dmr;

public class CSBK {
	boolean lb,pf;
	private String display[]=new String[3];
	
	// The main decode method
	public String[] decode (DMRDecode theApp,boolean bits[]) 	{
		int csbko,fid;
		// LB
		lb=bits[0];
		// PF
		pf=bits[1];
		// CSBKO
		if (bits[2]==true) csbko=32;
		else csbko=0;
		if (bits[3]==true) csbko=csbko+16;
		if (bits[4]==true) csbko=csbko+8;
		if (bits[5]==true) csbko=csbko+4;
		if (bits[6]==true) csbko=csbko+2;
		if (bits[7]==true) csbko++;
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
		// CSBK Types
		// 56 - BS_Dwn_Act
		if (csbko==56)	{
			bs_dwn_act(bits);
		}
		// 01 (FID 6) - Connect Plus
		else if ((csbko==1)&&(fid==6))	{
			big_m_csbko01(theApp,bits);
		}
		// 03 (FID 6) - Connect Plus
		else if ((csbko==3)&&(fid==6))	{
			big_m_csbko03(theApp,bits);
		}
		// 04 - UU_V_Reg
		else if (csbko==4)	{
			uu_v_reg(bits);
		}
		// 05 - UU_Ans_Rsp
		else if (csbko==5)	{
			uu_ans_rep(bits);
		}
		// 38 - NACK_Rsp
		else if (csbko==38)	{
			nack_rsp(bits);
		}
		// 61 - Pre_CSBK
		else if (csbko==61)	{
			preCSBK(theApp,bits);
		}
		// 62 - Capacity Plus
		else if (csbko==62)	{
			big_m_csbko62(theApp,bits);
		}	
		else	{
			unknownCSBK(csbko,fid,bits);
		}
		return display;
	}
	
	// Handle unknown CSBK types
	private void unknownCSBK (int csbko,int fid,boolean bits[])	{
		int a;
		StringBuilder sb=new StringBuilder(250);
		sb.append("Unknown CSBK : CSBKO="+Integer.toString(csbko)+" + FID="+Integer.toString(fid)+" ");
		// Display the binary
		for (a=16;a<80;a++)	{
			if (bits[a]==true) sb.append("1");
			else sb.append("0");
		}
		display[0]=sb.toString();
	}
	
	// Handle a Preamble CSBK
	private void preCSBK (DMRDecode theApp,boolean bits[])	{
		int index;
		StringBuilder sb=new StringBuilder(250);
		StringBuilder sc=new StringBuilder(250);
		Utilities utils=new Utilities();
		// 0 if CSBK , 1 if Data
		boolean dc=bits[16];
		// 0 if target is individual and 1 if group
		boolean gi=bits[17];
		// Bits 18,19,20,21,22,23 are reserved
		// Next 8 bits are the bits to follow
		int bfol=0;
		if (bits[24]==true) bfol=128;
		if (bits[25]==true) bfol=bfol+64;
		if (bits[26]==true) bfol=bfol+32;
		if (bits[27]==true) bfol=bfol+16;
		if (bits[28]==true) bfol=bfol+8;
		if (bits[29]==true) bfol=bfol+4;
		if (bits[30]==true) bfol=bfol+2;
		if (bits[31]==true) bfol++;
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		// Display this
		sb.append("Preamble CSBK : ");
		if (dc==false) sb.append(" CSBK content ");
		else sb.append(" Data content ");
		sb.append(Integer.toString(bfol)+" Blocks to follow");
		display[0]=sb.toString();		
		sc.append("Target Address : "+Integer.toString(target));
		if (gi==true) sc.append(" (Group)");
		sc.append(" Source Address : "+Integer.toString(source));
		display[1]=sc.toString();
		// Target
		theApp.usersLogged.addUser(target);
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsDataUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsDataUser(index);
			theApp.usersLogged.setChannel(index,theApp.currentChannel);
		}
	}
		
	// BS Outbound Activation CSBK
	private void bs_dwn_act (boolean bits[])	{
		// TODO : Full decoding of bs_dwn_act
		display[0]="BS Outbound Activation";
	}
	
	// Unit to Unit Voice Service Request CSBK
	private void uu_v_reg (boolean bits[])	{
		// TODO : Full decoding of UU_V_Req
		display[0]="Unit to Unit Voice Service Request";
	}
	
	// Unit to Unit Service Answer Response CSBK
	private void uu_ans_rep (boolean bits[])	{
		// TODO : Full decoding of UU_Ans_Rsp
		display[0]="Unit to Unit Service Answer Response";
	}
	
	// Negative Acknowledge Response CSBK
	private void nack_rsp (boolean bits[])	{
		// TODO : Full decoding of NACK_Rsp
		display[0]="Negative Acknowledge Response";
	}
	
	// Capacity Plus
	private void big_m_csbko62 (DMRDecode theApp,boolean bits[])	{
		int group,a,lcn;
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Capacity Plus CSBK : CSBKO=62";
		// LCN
		if (bits[20]==true) lcn=8;
		else lcn=0;
		if (bits[21]==true) lcn=lcn+4;
		if (bits[22]==true) lcn=lcn+2;
		if (bits[23]==true) lcn++;
		// Group ident
		if (bits[32]==true) group=128;
		else group=0;
		if (bits[33]==true) group=group+64;
		if (bits[34]==true) group=group+32;
		if (bits[35]==true) group=group+16;
		if (bits[36]==true) group=group+8;
		if (bits[37]==true) group=group+4;
		if (bits[38]==true) group=group+2;
		if (bits[39]==true) group++;
		// Only show more if we have any activity
		if (group==0)	{
			sb1.append("Activity Update : LCN "+Integer.toString(lcn)+" is the Rest Channel");
		} else {
			sb1.append("Activity Update : ");
			sb1.append("Group "+Integer.toString(group)+" call on LCN "+Integer.toString(lcn));
		}
		display[1]=sb1.toString();
		// Display the full binary if in debug mode
		if (theApp.isDebug()==true)	{
			for (a=16;a<80;a++)	{
				if (bits[a]==true) sb2.append("1");
				else sb2.append("0");
			}
			display[2]=sb2.toString();
		}
	}
	
	// Connect Plus - CSBKO 03 FID=6
	private void big_m_csbko03 (DMRDecode theApp,boolean bits[])	{
		int a,lcn;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Connect Plus CSBK : CSBKO=3";
		// Source ID
		int source=utils.retAddress(bits,16);
		// Group address
		int group=utils.retAddress(bits,40);
		// LCN
		if (bits[64]==true) lcn=8;
		else lcn=0;
		if (bits[65]==true) lcn=lcn+4;
		if (bits[66]==true) lcn=lcn+2;
		if (bits[67]==true) lcn++;
		sb1.append("Channel Grant : LCN "+Integer.toString(lcn));
		sb1.append(" Source "+Integer.toString(source));
		sb1.append(" Group "+Integer.toString(group));
		display[1]=sb1.toString();
		// Display the full binary if in debug mode
		if (theApp.isDebug()==true)	{
			for (a=16;a<80;a++)	{
				if (bits[a]==true) sb2.append("1");
				else sb2.append("0");
			}
			display[2]=sb2.toString();
		}
	}
	
	// Connect Plus - CSBKO 01 FID=6
	private void big_m_csbko01 (DMRDecode theApp,boolean bits[])	{
		int a;
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Connect Plus CSBK : CSBKO=1";
		sb1.append("System Message : ");
		// Can't decode this so show it as raw binary for now
		for (a=16;a<80;a++)	{
			if (bits[a]==true) sb1.append("1");
			else sb1.append("0");
		}
		display[1]=sb1.toString();
		// Display the full binary if in debug mode
		if (theApp.isDebug()==true)	{
			for (a=16;a<80;a++)	{
				if (bits[a]==true) sb2.append("1");
				else sb2.append("0");
			}
			display[2]=sb2.toString();
		}
	}



}
