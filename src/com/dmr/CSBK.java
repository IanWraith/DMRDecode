package com.dmr;

public class CSBK {
	boolean lb,pf;
	private String display[]=new String[3];
	
	// The main decode method
	public String[] decode (boolean bits[]) 	{
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
			preCSBK(bits);
		}
		else	{
			unknownCSBK(csbko,fid,bits);
		}
		
		
		return display;
	}
	
	// Handle unknown CSBK types
	private void unknownCSBK (int csbko,int fid,boolean bits[])	{
		int a;
		display[0]="<b>Unknown CSBK : CSBKO="+Integer.toString(csbko)+"+ FID="+Integer.toString(fid)+"</b>";
		// Display the binary
		display[1]="<b>";
		for (a=16;a<80;a++)	{
			if (bits[a]==true) display[1]=display[1]+"1";
			else display[1]=display[1]+"0";
		}
		display[1]=display[1]+"</b>";
	}
	
	// Handle a Preamble CSBK
	private void preCSBK (boolean bits[])	{
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
		display[0]="<b>Preamble CSBK : ";
		if (dc==false) display[0]=display[0]+" CSBK content ";
		else display[0]=display[0]+" Data content ";
		display[0]=display[0]+Integer.toString(bfol)+" Blocks to follow</b>";
		display[1]="<b>Target Address : "+Integer.toString(target);
		if (gi==true) display[1]=display[1]+" (Group)";
		display[1]=display[1]+" Source Address : "+Integer.toString(source)+"</b>";
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
		display[0]="<b>BS Outbound Activation</b>";
	}
	
	// Unit to Unit Voice Service Request CSBK
	private void uu_v_reg (boolean bits[])	{
		// TODO : Full decoding of UU_V_Req
		display[0]="<b>Unit to Unit Voice Service Request</b>";
	}
	
	// Unit to Unit Service Answer Response CSBK
	private void uu_ans_rep (boolean bits[])	{
		// TODO : Full decoding of UU_Ans_Rsp
		display[0]="<b>Unit to Unit Service Answer Response</b>";
	}
	
	// Negative Acknowledge Response CSBK
	private void nack_rsp (boolean bits[])	{
		// TODO : Full decoding of NACK_Rsp
		display[0]="<b>Negative Acknowledge Response</b>";
	}


}
