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
		// 25 (FID 00) - C_ALOHA (Tier III)
		else if ((csbko==25)&&(fid==0))	{
			csbko25fid0(theApp,bits);
		}
		// 31 (FID 16) - Call Alert 
		// Note in Tier III that CSBKO=31 is C_RAND but only inbound also FID=0 
		else if ((csbko==31)&&(fid==16))	{
			csbko31fid16(theApp,bits);
		}
		// 32 (FID 16) - Call Alert Ack
		// Note in Tier III that CSBKO=32 is C/P_ACKD but also FID=0
		else if ((csbko==32)&&(fid==16))	{
			csbko32fid16(theApp,bits);
		}
		// 36 (FID 16) - Radio Check
		else if ((csbko==36)&&(fid==16))	{
			csbko36fid16(theApp,bits);
		}
		// 38 - NACK_Rsp
		else if (csbko==38)	{
			nack_rsp(bits);
		}
		// 40 (FID 00) - C_BCAST (Tier III)
		else if ((csbko==40)&&(fid==0))	{
			csbko40fid0(theApp,bits);
		}
		// 46 (FID 00) - P_CLEAR (Tier III)
		else if ((csbko==46)&&(fid==0))	{
			csbko46fid0(theApp,bits);
		}
		// 48 (FID 00) - PV_GRANT (Tier III)
		else if ((csbko==48)&&(fid==0))	{
			csbko48fid0(theApp,bits);
		}
		// 49 (FID 00) - TV_GRANT (Tier III)
		else if ((csbko==49)&&(fid==0))	{
			csbko49fid0(theApp,bits);
		}	
		// 50 (FID 00) - BTV_GRANT (Tier III)
		else if ((csbko==50)&&(fid==0))	{
			csbko50fid0(theApp,bits);
		}		
		// 51 (FID 00) - PD_GRANT (Tier III)
		else if ((csbko==51)&&(fid==0))	{
			csbko51fid0(theApp,bits);
		}	
		// 52 (FID 00) - TD_GRANT (Tier III)
		else if ((csbko==52)&&(fid==0))	{
			csbko52fid0(theApp,bits);
		}	
		// 57 (FID 00) - C_MOVE (Tier III)
		else if ((csbko==57)&&(fid==0))	{
			csbko57fid0(theApp,bits);
		}			
		// 59 - Capacity Plus
		else if ((csbko==59)&&(fid==16))	{
			big_m_csbko59(theApp,bits);
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
    // A great deal of information on this type of packet was kindly provided by Eric Cottrell on the Radioreference forums	
	// see http://forums.radioreference.com/digital-voice-decoding-software/209318-understanding-capacity-plus-trunking-6.html#post2078924	
	private void big_m_csbko62 (DMRDecode theApp,boolean bits[])	{
		int group1,group2,group3,group4,group5,group6,a,lcn;
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Capacity Plus CSBK : CSBKO=62";
		// LCN
		if (bits[20]==true) lcn=8;
		else lcn=0;
		if (bits[21]==true) lcn=lcn+4;
		if (bits[22]==true) lcn=lcn+2;
		if (bits[23]==true) lcn++;
		// Group idents
		// Low group
		if (bits[32]==true) group1=128;
		else group1=0;
		if (bits[33]==true) group1=group1+64;
		if (bits[34]==true) group1=group1+32;
		if (bits[35]==true) group1=group1+16;
		if (bits[36]==true) group1=group1+8;
		if (bits[37]==true) group1=group1+4;
		if (bits[38]==true) group1=group1+2;
		if (bits[39]==true) group1++;
		// Group 2
		if (bits[40]==true) group2=128;
		else group2=0;
		if (bits[41]==true) group2=group2+64;
		if (bits[42]==true) group2=group2+32;
		if (bits[43]==true) group2=group2+16;
		if (bits[44]==true) group2=group2+8;
		if (bits[45]==true) group2=group2+4;
		if (bits[46]==true) group2=group2+2;
		if (bits[47]==true) group2++;
		// Group 3
		if (bits[48]==true) group3=128;
		else group3=0;
		if (bits[49]==true) group3=group3+64;
		if (bits[50]==true) group3=group3+32;
		if (bits[51]==true) group3=group3+16;
		if (bits[52]==true) group3=group3+8;
		if (bits[53]==true) group3=group3+4;
		if (bits[54]==true) group3=group3+2;
		if (bits[55]==true) group3++;
		// Group 4
		if (bits[56]==true) group4=128;
		else group4=0;
		if (bits[57]==true) group4=group4+64;
		if (bits[58]==true) group4=group4+32;
		if (bits[59]==true) group4=group4+16;
		if (bits[60]==true) group4=group4+8;
		if (bits[61]==true) group4=group4+4;
		if (bits[62]==true) group4=group4+2;
		if (bits[63]==true) group4++;
		// Group 5
		if (bits[64]==true) group5=128;
		else group5=0;
		if (bits[65]==true) group5=group5+64;
		if (bits[66]==true) group5=group5+32;
		if (bits[67]==true) group5=group5+16;
		if (bits[68]==true) group5=group5+8;
		if (bits[69]==true) group5=group5+4;
		if (bits[70]==true) group5=group5+2;
		if (bits[71]==true) group5++;		
		// Group 6
		if (bits[72]==true) group6=128;
		else group6=0;
		if (bits[73]==true) group6=group6+64;
		if (bits[74]==true) group6=group6+32;
		if (bits[75]==true) group6=group6+16;
		if (bits[76]==true) group6=group6+8;
		if (bits[77]==true) group6=group6+4;
		if (bits[78]==true) group6=group6+2;
		if (bits[79]==true) group6++;
		// Display all this 
		// Only show more if we have any activity
		if ((bits[24]==false)&&(bits[25]==false)&&(bits[26]==false)&&(bits[27]==false)&&(bits[28]==false)&&(bits[29]==false))	{
			sb1.append("Activity Update : LCN "+Integer.toString(lcn)+" is the Rest Channel");
		} else {
			boolean nf=false;
			sb1.append("Activity Update : LCN "+Integer.toString(lcn)+" is the rest channel (");
			if (bits[24]==true)	{
				if (group1>0) sb1.append("Group "+Integer.toString(group1)+" on LCN 1");
				else sb1.append("Activity on LCN 1");
				nf=true;
			}
			if (bits[25]==true)	{
				if (nf==true) sb1.append(",");
				if (group2>0) sb1.append("Group "+Integer.toString(group2)+" on LCN 2");
				else sb1.append("Activity on LCN 2");
				nf=true;
			}
			if (bits[26]==true)	{
				if (nf==true) sb1.append(",");
				if (group3>0) sb1.append("Group "+Integer.toString(group3)+" on LCN 3");
				else sb1.append("Activity on LCN 3");
				nf=true;
			}
			if (bits[27]==true)	{
				if (nf==true) sb1.append(",");
				if (group4>0) sb1.append("Group "+Integer.toString(group4)+" on LCN 4");
				else sb1.append("Activity on LCN 4");
				nf=true;
			}
			if (bits[28]==true)	{
				if (nf==true) sb1.append(",");
				if (group5>0) sb1.append("Group "+Integer.toString(group5)+" on LCN 5");
				else sb1.append("Activity on LCN 5");
				nf=true;
			}
			if (bits[29]==true)	{
				if (nf==true) sb1.append(",");
				if (group6>0) sb1.append("Group "+Integer.toString(group6)+" on LCN 6");
				else sb1.append("Activity on LCN 6");
				nf=true;
			}
			if (nf==true) sb1.append(")");
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
		// Time Slot
	    // The information on the time slot bit was kindly provided by W8EMX on the Radioreference forums
		// see http://forums.radioreference.com/digital-voice-decoding-software/213131-understanding-connect-plus-trunking-7.html#post1909226
		boolean timeSlot=bits[68];
		// Display this
		sb1.append("Channel Grant : LCN "+Integer.toString(lcn));
		if (timeSlot==false) sb1.append(" TS1");
		else sb1.append(" TS2");
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
		int a,nb1,nb2,nb3,nb4,nb5;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Connect Plus CSBK : CSBKO=1";
		sb1.append("Control Channel Neighbour List : ");
		// The information to decode these packets was kindly provided by inigo88 on the Radioreference forums
		// see http://forums.radioreference.com/digital-voice-decoding-software/213131-understanding-connect-plus-trunking-6.html#post1866950
		//                 67 890123 45 678901 23 456789 01 234567 89 012345 6789 0123 4567 8901 2345 6789
		// CSBKO=1 + FID=6 00 000001 00 000011 00 000100 00 000101 00 000110 0000 0000 0000 0000 0000 1110
		//                         1         3         4	     5         6	                      
		// bits 16,17 have an unknown purpose
		// bits 18,19,20,21,22,23 make up the first neighbour site ID
		nb1=utils.retSix(bits,18);
		// bits 24,25 have an unknown purpose
		// bits 26,27,28,29,30,31 make up the second neighbour site ID
		nb2=utils.retSix(bits,26);
		// bits 32,33 have an unknown purpose
		// bits 34,35,36,37,38,39 make up the third neighbour site ID
		nb3=utils.retSix(bits,34);
		// bits 40,41 have an unknown purpose
		// bits 42,43,44,45,46,47 make up the fourth neighbour site ID
		nb4=utils.retSix(bits,42);
		// bits 48,49 have an unknown purpose
		// bits 50,51,52,53,54,55 make up the fifth neighbour site ID
		nb5=utils.retSix(bits,50);
		// bits 56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79 have an unknown purpose
		// Display this info
		sb1.append(Integer.toString(nb1)+","+Integer.toString(nb2)+","+Integer.toString(nb3)+","+Integer.toString(nb4)+","+Integer.toString(nb5)+" (");
		// Also display as raw binary for now
		for (a=16;a<80;a++)	{
			if (bits[a]==true) sb1.append("1");
			else sb1.append("0");
		}
		sb1.append(")");
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
	
	// CSBKO 25 FID 00 C_ALOHA
	// Bits 16,17,18,19,20,21 Reserved
	// 22 Infill
	// 23 Active connection
	// 24,25,26,27,28 Mask
	// 29,30 Service Function
	// 31,32,33,34 NRand_Wait
	// 35 Reg
	// 36,37,38,39 Backoff
	// 40 - 55 System ID
	// 56 - 79 MS Individual addr
	private void csbko25fid0 (DMRDecode theApp,boolean bits[])	{
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		Utilities utils=new Utilities();
		display[0]="C_ALOHA : CSBKO=25 + FID=0";
		// Infill
		if (bits[22]==true) sb1.append("Infill Radio Site : ");
		else sb1.append("Not an Infill Radio Site : ");
		// Active_Connection
		if (bits[23]==true) sb1.append("TS has Network Connection : ");
		else sb1.append("TS doesn't have a Network Connection : ");
		// Mask
		int mask=0;
		if (bits[24]==true) mask=16;
		if (bits[25]==true) mask=mask+8;
		if (bits[26]==true) mask=mask+4;
		if (bits[27]==true) mask=mask+2;
		if (bits[28]==true) mask++;
		sb1.append("Mask="+Integer.toString(mask)+" : ");
		// Service Function
		int sf=0;
		if (bits[29]==true) sf=2;
		if (bits[30]==true) sf++;
		sb1.append("Service Function="+Integer.toString(sf)+" : ");
		// Reg
		if (bits[35]==true) sb1.append("TSCC demands MS must register");
		display[1]=sb1.toString();
		// System Identity Code
		int sysID=utils.retSixteen(bits,40);
		sb2.append("System Identity Code="+Integer.toString(sysID)+" : ");
		// MS Address
		int addr=utils.retAddress(bits,56);
		sb2.append("MS Individual Address="+Integer.toString(addr));
		display[2]=sb2.toString();
	}
	
	// CSBKO 40 FID 00 C_BCAST
	// Bits 16,17,18,19,20 Announcement type
	// 21 - 34 Broadcast Parms 1
	// 35 Reg
	// 36,37,38,39 Backoff
	// 40 - 55 System ID
	// 56 - 79 Broadcast Parms 2
	private void csbko40fid0 (DMRDecode theApp,boolean bits[])	{
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		Utilities utils=new Utilities();
		// Announcement Type
		int at=0;
		String aType;
		if (bits[16]==true) at=16;
		if (bits[17]==true) at=at+8;
		if (bits[18]==true) at=at+4;
		if (bits[19]==true) at=at+2;
		if (bits[20]==true) at++;
		if (at==0)	{
			aType="Ann-WD_TSCC (Announce/Withdraw TSCC)";
			// Parms 1
			// Bits 21,22,23,24 are reserved
			int col_ch1=utils.retFour(bits,25);
			int col_ch2=utils.retFour(bits,29);
			boolean aw_flag1=bits[33];
			boolean aw_flag2=bits[34];
			// Parms 2
			int bcast_ch1=utils.retTwelve(bits,56);
			// T_MS-LINE_TIMER
			int bcast_ch2=utils.retTwelve(bits,68);
			// Display
			if (aw_flag1==true)	sb1.append("Withdraw BCAST_CH1 (Colour Code "+Integer.toString(col_ch1)+") from the hunt list : ");
			else sb1.append("Add BCAST_CH1 (Colour Code "+Integer.toString(col_ch1)+") from the hunt list : ");
			if (aw_flag2==true)	sb1.append("Withdraw BCAST_CH2 (Colour Code "+Integer.toString(col_ch2)+") from the hunt list : ");
			else sb1.append("Add BCAST_CH2 (Colour Code "+Integer.toString(col_ch2)+") from the hunt list : ");
			sb2.append("BCAST_CH1="+Integer.toString(bcast_ch1));
			sb2.append(" : BCAST_CH2="+Integer.toString(bcast_ch2));
			display[1]=sb1.toString();
			display[2]=sb2.toString();
		}
		else if (at==1)	{
			aType="CallTimer_Parms (Specify Call Timer Parameters)";
			// Parms 1
			// T_EMERG_TIMER
			int t_emerg_timer=utils.retNine(bits,21);
			// T_PACKET_TIMER
			int t_packet_timer=utils.retFive(bits,30);
			// Parms 2
			// T_MS-MS_TIMER
			int t_ms_ms_timer=utils.retTwelve(bits,56);
			// T_MS-LINE_TIMER
			int t_ms_line_timer=utils.retTwelve(bits,68);
			// Display these
			if (t_emerg_timer==512)	{
				sb1.append("Emergency Call Timer is Infinity : ");
			}
			else	{
				sb1.append("T_EMERG_TIMER="+Integer.toString(t_emerg_timer)+" : ");
			}
			if (t_packet_timer==31)	{
				sb1.append("Packet Call Timer is Infinity : ");
			}
			else	{
				sb1.append("T_PACKET_TIMER="+Integer.toString(t_packet_timer)+" : ");
			}
			if (t_ms_ms_timer==4095)	{
				sb1.append("MS to MS Call Timer is Infinity : ");
			}
			else	{
				sb1.append("T_MS-MS_TIMER="+Integer.toString(t_ms_ms_timer)+" : ");
			}
			if (t_ms_line_timer==4095)	{
				sb1.append("Line Connected Call Timer is Infinity");
			}
			else	{
				sb1.append("T_MS-LINE_TIMER="+Integer.toString(t_ms_line_timer)+" : ");
			}			
			display[1]=sb1.toString();
		}
		else if (at==2)	{
			aType="Vote_Now (Vote Now Advice)";
			// Parms1 contains the most significant 14 bits of the TSCC system identity
			// Parms2
			// Bits 56,57 least significant 2 bits of TSCC system identity is 'Manufacturer Specific' VN_ACTION option selected
			// Bit 58 active connection
			// Bits 59,60,61,62,63,64 Reserved
			// Site_Strategy
			int site_strat=utils.retThree(bits,65);
			if (site_strat==0) sb1.append("Radio Site : ");
			else if (site_strat==1) sb1.append("Infill : ");
			else if (site_strat==2) sb1.append("Manufacturer specific strategy : ");
			else sb1.append("Reserved : ");
			// CH_VOTE
			int ch_vote=utils.retTwelve(bits,68);
			sb1.append("CH_VOTE is "+Integer.toString(ch_vote));
			display[1]=sb1.toString();
		}
		else if (at==3)	{
			aType="Local_Time (Broadcast Local Time)";
			// Parms 1
			// B_DAY 
			int b_day=utils.retFive(bits,21);
			// B_MONTH 
			int b_month=utils.retFour(bits,25);
			// UTC_OFFSET
			int utc_offset=utils.retFive(bits,30);
			// Check there is a date
			if ((b_day>0)&&(b_month>0))	{
				sb1.append("Date "+Integer.toString(b_day)+"/"+Integer.toString(b_month)+" ");
				if (utc_offset==31) sb1.append("UTC Offset is "+Integer.toString(utc_offset)+" hours ");
			}
			// Parms 2
			// B_HOURS
			int b_hours=utils.retFive(bits,56);
			// B_MINS
			int b_mins=utils.retSix(bits,61);
			// B_SECS
			int b_secs=utils.retSix(bits,67);
			// DAYOF_WEEK
			int dayof_week=utils.retThree(bits,73);
			// UTC_OFFSET_FRACTION
			int utc_offset_fraction=0;
			if (bits[74]==true) utc_offset_fraction=2;
			if (bits[75]==true) utc_offset_fraction++;
			// 76,77,78,79 Reserved
			if (dayof_week==1) sb1.append("Sunday ");
			else if (dayof_week==2) sb1.append("Monday ");
			else if (dayof_week==3) sb1.append("Tuesday ");
			else if (dayof_week==4) sb1.append("Wednesday ");
			else if (dayof_week==5) sb1.append("Thursday ");
			else if (dayof_week==6) sb1.append("Friday ");
			else if (dayof_week==7) sb1.append("Saturday ");
			if (b_hours<10) sb1.append("0");
			sb1.append(Integer.toString(b_hours)+":");
			if (b_mins<10) sb1.append("0");
			sb1.append(Integer.toString(b_mins)+":");
			if (b_secs<10) sb1.append("0");
			sb1.append(Integer.toString(b_secs));
			if (utc_offset_fraction==1) sb1.append(" (Add 15 mins)");
			else if (utc_offset_fraction==1) sb1.append(" (Add 30 mins)");
			else if (utc_offset_fraction==2) sb1.append(" (Add 45 mins)");
			display[1]=sb1.toString();
		}
		else if (at==4)	{
			aType="MassReg (Mass_Registration)";
			// Parms 1
			// 21,22,23,24,25 Reserved
			// Reg_Window
			int reg_window=utils.retFour(bits,26);
			// Aloha Mask
			int aloha_mask=utils.retFive(bits,30);
			// Parms 2
			int ms_individual_address=utils.retAddress(bits,26);
			// Display this
			sb1.append("Reg_Window="+Integer.toString(reg_window)+" : Aloha Mask="+Integer.toString(aloha_mask)+" : MS Individual Address "+Integer.toString(ms_individual_address));
			display[1]=sb1.toString();
		}
		else if (at==5)	{
			aType="Chan_Freq (Announce a logical channel/frequency relationship)";
			// Nothing describing this is in ETSI TS 102 361-4 V1.5.1 so just show binary parms 1 & 2 instead
			int a;
			for (a=21;a<35;a++)	{
				if (bits[a]==false) sb1.append("0");
				else sb1.append("1");
			}
			sb1.append(" ");
			for (a=56;a<80;a++)	{
				if (bits[a]==false) sb1.append("0");
				else sb1.append("1");
			}		
			display[1]=sb1.toString();
		}
		else if (at==6)	{
			aType="Adjacent_Site (Adjacent Site Information)";
			// Parms1 contains the most significant 14 bits of the TSCC system identity
			// Parms2
			// Bits 56,57 least significant 2 bits of TSCC system identity is 'Manufacturer Specific' VN_ACTION option selected
			// Bit 58 active connection
			// Bits 59,60,61,62,63,64 Reserved
			// Site_Strategy
			int site_strat=utils.retThree(bits,65);
			if (site_strat==0) sb1.append("Radio Site : ");
			else if (site_strat==1) sb1.append("Infill : ");
			else if (site_strat==2) sb1.append("Manufacturer specific strategy : ");
			else sb1.append("Reserved : ");
			// CH_ADJ
			int ch_adj=utils.retTwelve(bits,68);
			sb1.append("CH_ADJ "+Integer.toString(ch_adj));
			display[1]=sb1.toString();
		}
		else if ((at==30)||(at==31))	{
			aType="Manufacturer Specific ("+Integer.toString(at)+")";
			// Display the parms binary
			int a;
			for (a=21;a<35;a++)	{
				if (bits[a]==false) sb1.append("0");
				else sb1.append("1");
			}
			sb1.append(" ");
			for (a=56;a<80;a++)	{
				if (bits[a]==false) sb1.append("0");
				else sb1.append("1");
			}		
			display[1]=sb1.toString();			
		}
		else aType="Reserved ("+Integer.toString(at)+")";
		// System Identity Code
		int sysID=utils.retSixteen(bits,40);
		display[0]="C_BCAST : CSBKO=40 + FID=0 : System ID="+Integer.toString(sysID)+" : "+aType;
	}	
	

	// CSBKO 31 FID 16 Call Alert
	// The information to decode this was kindly provided by bben95 on the Radioreference forums
	// http://forums.radioreference.com/digital-voice-decoding-software/191957-java-program-decode-dmr-31.html#post2098983
	// 0000000000000000 000000000001011101110010 000000000001011101110001
	// 1111222222222233 333333334444444444555555 555566666666667777777777
	// 6789012345678901 234567890123456789012345 678901234567890123456789
	// is Call alert from 6001 to 6002
	private void csbko31fid16 (DMRDecode theApp,boolean bits[])	{
		int a;
		Utilities utils=new Utilities();
		int to=utils.retAddress(bits,32);
		int from=utils.retAddress(bits,56);
		StringBuilder sb1=new StringBuilder(300);
		display[0]="CSBK : CSBKO=31 + FID=16";
		sb1.append("Call Alert from "+Integer.toString(from)+" to "+Integer.toString(to)+" (");
		// Also display the unknown part as raw binary for now
		for (a=16;a<32;a++)	{
			if (bits[a]==true) sb1.append("1");
			else sb1.append("0");
			}
		sb1.append(")");
		display[1]=sb1.toString();	
	}
	
	// CSBKO 32 FID 16 Call Alert Ack
	// The information to decode this was kindly provided by bben95 on the Radioreference forums
	// http://forums.radioreference.com/digital-voice-decoding-software/191957-java-program-decode-dmr-31.html#post2098983
    // 1001111100000000 000000000001011101110001 000000000001011101110010
	// 1111222222222233 333333334444444444555555 555566666666667777777777
	// 6789012345678901 234567890123456789012345 678901234567890123456789
	// is Call alert from 6001 to 6002: acknowledged
	private void csbko32fid16 (DMRDecode theApp,boolean bits[])	{
		int a;
		Utilities utils=new Utilities();
		int to=utils.retAddress(bits,32);
		int from=utils.retAddress(bits,56);
		StringBuilder sb1=new StringBuilder(300);
		display[0]="CSBK : CSBKO=32 + FID=16";
		sb1.append("Call Alert ACK from "+Integer.toString(from)+" to "+Integer.toString(to)+" (");
		// Also display the unknown part as raw binary for now
		for (a=16;a<32;a++)	{
			if (bits[a]==true) sb1.append("1");
			else sb1.append("0");
			}
		sb1.append(")");
		display[1]=sb1.toString();		
	}
	
	// CSBKO 36 FID 16 Radio Check
	private void csbko36fid16 (DMRDecode theApp,boolean bits[])	{
		int a;
		Utilities utils=new Utilities();
		int from=utils.retAddress(bits,32);
		int to=utils.retAddress(bits,56);
		StringBuilder sb1=new StringBuilder(300);
		display[0]="CSBK : CSBKO=36 + FID=16";
		sb1.append("Radio Check from "+Integer.toString(from)+" to "+Integer.toString(to)+" (");
		// Also display the unknown part as raw binary for now
		for (a=16;a<32;a++)	{
			if (bits[a]==true) sb1.append("1");
			else sb1.append("0");
			}
		sb1.append(")");
		display[1]=sb1.toString();		
	}	
	
	// Capacity Plus
    // The information on this type of packet was kindly provided by Eric Cottrell on the Radioreference forums	
	// see http://forums.radioreference.com/digital-voice-decoding-software/209318-understanding-capacity-plus-trunking-6.html#post2106396
	private void big_m_csbko59 (DMRDecode theApp,boolean bits[])	{
		StringBuilder sb1=new StringBuilder(300);
		StringBuilder sb2=new StringBuilder(300);
		display[0]="Capacity Plus CSBK : CSBKO=59 + FID=16: SYSSITESTS";
		// Bits 16 & 17 First/Last Block
		int fl=0;
		if (bits[16]==true) fl=2;
		if (bits[17]==true) fl++;
		sb1.append("FL="+Integer.toString(fl));
		// Bit 18 slot
		if (bits[18]==false) sb1.append(" : TS1");
		else sb1.append(" : TS2");
		// Bits 19,20,21,22,23  Rest Channel ID
		int restCh=0;
		if (bits[19]==true) restCh=16;
		else if (bits[20]==true) restCh=restCh+8;
		else if (bits[21]==true) restCh=restCh+4;
		else if (bits[22]==true) restCh=restCh+2;
		else if (bits[23]==true) restCh++;
		sb1.append(" : Rest Channel ID "+Integer.toString(restCh));
		// Bit 24 ASYNC
		if (bits[24]==false) sb1.append(" : Periodic Beacons");
		else sb1.append(" : Asynchronous Beacons");
		// Bits 25,26,27,28 My Site ID
		int mySiteID=0;
		if (bits[25]==true) mySiteID=8;
		if (bits[26]==true) mySiteID=mySiteID+4;
		if (bits[27]==true) mySiteID=mySiteID+2;
		if (bits[28]==true) mySiteID++;
		sb1.append(" : This Site ID "+Integer.toString(mySiteID));
		// Display this
		display[1]=sb1.toString();
		// Bits 29.30,31 Number of neighbour sites
		int nNos=0;
		if (bits[29]==true) nNos=4;
		if (bits[30]==true) nNos=nNos+2;
		if (bits[31]==true) nNos++;
		// If more than 6 sites we have a problem that will cause an overflow
		if (nNos>6) nNos=6;
		// Display the neighbour site info
		Utilities utils=new Utilities();
		int a,pos=32,nsid,nrst;
		for (a=0;a<nNos;a++)	{
			nsid=utils.retFour(bits,pos);
			nrst=utils.retFour(bits,pos+4);
			// Display
			if (a>0) sb2.append(",");
			sb2.append("Site #"+Integer.toString(a+1)+" ID "+Integer.toString(nsid)+" Rest Ch "+Integer.toString(nrst));
			// Move along
			pos=pos+8;
		}
		// If there is any neighbour site info then display it
		if (nNos>0) display[2]=sb2.toString();
	}
	
	// P_CLEAR
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28,29,30 Reserved
	// 31 IG
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko46fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		// Logical channel number
		int lochan=utils.retTwelve(bits,16);
		display[0]="P_Clear from channel "+Integer.toString(lochan);
		// IG
		boolean ig=bits[31];
		// Target address
		int targetAddr=utils.retAddress(bits,32);
		// Display this
		// Is this an ALLMSI
		if (targetAddr==0xFFFED4)	{
			sb1.append("Target : ALLMSI");
		}
		else	{
			if (ig==true) sb1.append("Target TG :");
			else sb1.append("Target : ");
			sb1.append(Integer.toString(targetAddr));
		}
		display[1]=sb1.toString();
		// Source Address
		int sourceAddr=utils.retAddress(bits,56);
		sb2.append("Source Address "+Integer.toString(sourceAddr));
		// Record this
		// Log these users
		// Target
		if (targetAddr!=0xFFFED4)	{
			theApp.usersLogged.addUser(targetAddr);	
			index=theApp.usersLogged.findUserIndex(targetAddr);
			if (index!=-1)	{
				if (ig==true) theApp.usersLogged.setAsGroup(index);
				theApp.usersLogged.setChannel(index,lochan);
			}
		}
		// Source
		theApp.usersLogged.addUser(sourceAddr);
		index=theApp.usersLogged.findUserIndex(sourceAddr);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("P_CLEAR",targetAddr,sourceAddr,lochan,"");
	}
	
	
	// PV_GRANT
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28 TDMA Channel
	// 29 OVCM
	// 30 Emergency
	// 31 Offset
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko48fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="Private Voice Channel Grant";
		// Logical Physical Channel Number
		int lchannel=utils.retTwelve(bits,16);
		sb1.append("Payload Channel "+Integer.toString(lchannel));
		if (bits[28]==false) sb1.append(" TDMA ch1 ");
		else sb1.append(" TDMA ch2 ");
		if (bits[29]==true) sb1.append(": OVCM Call ");
		if (bits[30]==true) sb1.append(": Emergency Call ");
		if (bits[31]==false) sb1.append(": Aligned Timing");
		else sb1.append(": Offset Timing");
		display[1]=sb1.toString();
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		sb2.append("Target Address : "+Integer.toString(target));
		sb2.append(" Source Address : "+Integer.toString(source));
		display[2]=sb2.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("PV_GRANT from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Private Voice Channel Grant",target,source,lchannel,display[1]);
	}
	
	// TV_GRANT
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28 TDMA Channel
	// 29 OVCM
	// 30 Emergency
	// 31 Offset
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko49fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="Talkgroup Voice Channel Grant";
		// Logical Physical Channel Number
		int lchannel=utils.retTwelve(bits,16);
		sb1.append("Payload Channel "+Integer.toString(lchannel));
		if (bits[28]==false) sb1.append(" TDMA ch1 ");
		else sb1.append(" TDMA ch2 ");
		if (bits[29]==true) sb1.append(": OVCM Call ");
		if (bits[30]==true) sb1.append(": Emergency Call ");
		if (bits[31]==false) sb1.append(": Aligned Timing");
		else sb1.append(": Offset Timing");
		display[1]=sb1.toString();
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		sb2.append("Target Address : "+Integer.toString(target));
		sb2.append(" Source Address : "+Integer.toString(source));
		display[2]=sb2.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroup(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroupUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("TV_GRANT from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Talkgroup Voice Channel Grant",target,source,lchannel,display[1]);
	}	
	
	// BTV_GRANT
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28 TDMA Channel
	// 29 OVCM
	// 30 Emergency
	// 31 Offset
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko50fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="Broadcast Talkgroup Voice Channel Grant";
		// Logical Physical Channel Number
		int lchannel=utils.retTwelve(bits,16);
		sb1.append("Payload Channel "+Integer.toString(lchannel));
		if (bits[28]==false) sb1.append(" TDMA ch1 ");
		else sb1.append(" TDMA ch2 ");
		if (bits[29]==true) sb1.append(": OVCM Call ");
		if (bits[30]==true) sb1.append(": Emergency Call ");
		if (bits[31]==false) sb1.append(": Aligned Timing");
		else sb1.append(": Offset Timing");
		display[1]=sb1.toString();
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		sb2.append("Target Address : "+Integer.toString(target));
		sb2.append(" Source Address : "+Integer.toString(source));
		display[2]=sb2.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroup(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroupUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("BTV_GRANT from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Broadcast Talkgroup Voice Channel Grant",target,source,lchannel,display[1]);
	}		
	
	// PD_GRANT
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28 TDMA Channel
	// 29 Packet Mode
	// 30 Emergency
	// 31 Offset
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko51fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="Private Data Channel Grant";
		// Logical Physical Channel Number
		int lchannel=utils.retTwelve(bits,16);
		sb1.append("Payload Channel "+Integer.toString(lchannel));
		if (bits[28]==false) sb1.append(" TDMA ch1 ");
		else sb1.append(" TDMA ch2 ");
		if (bits[29]==true) sb1.append(": Payload Channel uses 1:1 mode ");
		else sb1.append(": Payload Channel uses 2:1 mode ");
		if (bits[30]==true) sb1.append(": Emergency Call ");
		if (bits[31]==false) sb1.append(": Aligned Timing");
		else sb1.append(": Offset Timing");
		display[1]=sb1.toString();
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		sb2.append("Target Address : "+Integer.toString(target));
		sb2.append(" Source Address : "+Integer.toString(source));
		display[2]=sb2.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsUnitUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("PD_GRANT from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Private Data Channel Grant",target,source,lchannel,display[1]);
	}
	
	// TD_GRANT
	// Bits ..
	// 16,17,18,19,20,21,22,23,24,25,26,27 Logical Physical Channel Number
	// 28 TDMA Channel
	// 29 Packet Mode
	// 30 Emergency
	// 31 Offset
	// 32 - 55 Target Address
	// 56 - 79 Source Address
	void csbko52fid0 (DMRDecode theApp,boolean bits[])	{
		int index;
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="Talkgroup Data Channel Grant";
		// Logical Physical Channel Number
		int lchannel=utils.retTwelve(bits,16);
		sb1.append("Payload Channel "+Integer.toString(lchannel));
		if (bits[28]==false) sb1.append(" TDMA ch1 ");
		else sb1.append(" TDMA ch2 ");
		if (bits[29]==true) sb1.append(": Payload Channel uses 1:1 mode ");
		else sb1.append(": Payload Channel uses 2:1 mode ");
		if (bits[30]==true) sb1.append(": Emergency Call ");
		if (bits[31]==false) sb1.append(": Aligned Timing");
		else sb1.append(": Offset Timing");
		display[1]=sb1.toString();
		// Target address
		int target=utils.retAddress(bits,32);
		// Source address
		int source=utils.retAddress(bits,56);
		sb2.append("Target Address : "+Integer.toString(target));
		sb2.append(" Source Address : "+Integer.toString(source));
		display[2]=sb2.toString();
		// Log these users
		// Target
		theApp.usersLogged.addUser(target);	
		index=theApp.usersLogged.findUserIndex(target);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroup(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Source
		theApp.usersLogged.addUser(source);
		index=theApp.usersLogged.findUserIndex(source);
		if (index!=-1)	{
			theApp.usersLogged.setAsGroupUser(index);
			theApp.usersLogged.setChannel(index,lchannel);
		}
		// Display this in a label on the status bar
		StringBuilder lab=new StringBuilder(250);
		lab.append("TD_GRANT from ");
		lab.append(Integer.toString(source));
		lab.append(" to ");
		lab.append(Integer.toString(target));
		if (theApp.currentChannel==1) theApp.setCh1Label(lab.toString(),theApp.labelBusyColour);
		else theApp.setCh2Label(lab.toString(),theApp.labelBusyColour);
		// Quick log
		if (theApp.isQuickLog()==true) theApp.quickLogData("Talkgroup Data Channel Grant",target,source,lchannel,display[1]);
	}	
	
	// C_MOVE
	// Bits ..
	// 16,17,18,19,20,21,22,23,24 Reserved
	// 25,26,27,28,29 Mask
	// 30,31,32,33,34 Reserved
	// 35 Reg
	// 36,37,38,39 Backoff
	// 40,41,42,43 Reserved
	// 44 - 55 Physical Channel Number
	// 56 - 79 MS Individual Address
	void csbko57fid0 (DMRDecode theApp,boolean bits[])	{
		Utilities utils=new Utilities();
		StringBuilder sb1=new StringBuilder(250);
		StringBuilder sb2=new StringBuilder(250);
		display[0]="C_MOVE : CSBKO=57 + FID=0";
		// Mask
		int mask=utils.retFive(bits,25);
		sb1.append("Mask="+Integer.toString(mask)+" : ");
		// Reg
		if (bits[35]==true) sb1.append("TSCC demands MS must register : ");
	    // Backoff
		int backoff=utils.retFour(bits,36);
		sb1.append("Backoff="+Integer.toString(backoff));
		display[1]=sb1.toString();
		// Physical Channel Number
		int chanNo=utils.retTwelve(bits,44);
		sb2.append("Physical Channel Number "+Integer.toString(chanNo)+" : ");
		// MS Individual Address
		int msi=utils.retAddress(bits,56);
		sb2.append("MS Individual Address "+Integer.toString(msi));
		display[2]=sb2.toString();
	}
		
	
}
