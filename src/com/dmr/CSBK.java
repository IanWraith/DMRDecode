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
		int target=retAddress(bits,32);
		// Source address
		int source=retAddress(bits,56);
		// Display this
		sb.append("Preamble CSBK : ");
		if (dc==false) sb.append(" CSBK content ");
		else sb.append(" Data content ");
		sb.append(Integer.toString(bfol)+" Blocks to follow</b>");
		display[0]=sb.toString();		
		sc.append("<b>Target Address : "+Integer.toString(target));
		if (gi==true) sc.append(" (Group)");
		sc.append(" Source Address : "+Integer.toString(source));
		display[1]=sc.toString();
		// Target
		theApp.usersLogged.addUser(target);
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1) theApp.usersLogged.setAsDataUser(index);
		
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


}
