package com.dmr;

public class ShortLC {
	private boolean dataReady;
	private StringBuilder sb=new StringBuilder(250);
	private boolean rawData[]=new boolean[69];
	private boolean crcResult=false;
	private int currentState=-1;
	private crc tCRC=new crc();
	
	// Add data to the Short LC data buffer
	// Type 0 if First fragment of LC
	// Type 1 if Continuation fragment of LC
	// Type 2 if Last fragment of LC
	public void addData (boolean[] CACHbuf,int type)	{
		int a,b=7,rawCounter=0;
		dataReady=false;
		// First fragment ?
		// If so reset the the counters 
		if (type==0)	{
			rawCounter=0;
			// Ensure nothing else has arrived before
			if (currentState!=-1) return;
			// Set the current state to 0 to indicate a first fragment has arrived
			currentState=0;
			// Clear the display line
			sb.delete(0,sb.length());
		}
		// Continuation fragments
		else if (type==1)	{
			if (currentState==0) {
				rawCounter=17;
				currentState=1;
			}
			else if (currentState==1)	{
				rawCounter=34;
				currentState=2;
			}
			else if (currentState==-1)	{
				rawCounter=0;
				return;
			}
		}
		// Last fragment
		else if (type==2)	{
			// Ensure that a first fragment and two continuation fragments have arrived
			if (currentState!=2)	{
				currentState=-1;
				rawCounter=0;
				return;
			}
			else rawCounter=51;
		}
		// Add the data
		for (a=rawCounter;a<(rawCounter+17);a++)	{
			// Ignore the TACT
			rawData[a]=CACHbuf[b];
			b++;
		}
		// Has the fragment ended ?
		if (type==2)	{
			decode();
			currentState=-1;
			rawCounter=0;
		}
	}
	
	// Make the text string available
	public String getLine()	{
		return sb.toString();
	}
	
	// Tell the main object if decoded data is available
	public boolean isDataReady()	{
		return dataReady;
	}
	
	// Tell the main object if the CRC and Hamming checks are OK
	public boolean isCRCgood()	{
		return crcResult;
	}
	
	// Clear the data ready boolean
	public void clrDataReady()	{
		sb.delete(0,sb.length());
		dataReady=false;
	}
	
	// Deinterleave and error check the short LC
	public void decode()	{
		crcResult=false;
		if (shortLCHamming(rawData)==true)	{
			boolean shortLC[]=deInterleaveShortLC(rawData);
			if (shortLCcrc(shortLC)==true)	{
				sb=decodeShortLC(shortLC);
				crcResult=true;
			}
		}
		else sb.delete(0,sb.length());
		dataReady=true;
	}
	
	// Deinterleave a Short LC from 4 CACH bursts
	private boolean[] deInterleaveShortLC (boolean raw[])	{
		int a,pos;
		final int sequence[]={
				0,4,8,12,16,20,24,28,32,36,40,44,
				1,5,9,13,17,21,25,29,33,37,41,45,
				2,6,10,14,18,22,26,30,34,38,42,46};
		boolean[] deinter=new boolean[36];
		for (a=0;a<36;a++)	{
			pos=sequence[a];
			deinter[a]=raw[pos];
		}
		return deinter;
	}
	
	// Hamming check 3 of the 4 CACH rows
	private boolean shortLCHamming (boolean raw[])	{
		int a,pos;
		final int sequence1[]={0,4,8,12,16,20,24,28,32,36,40,44};
		final int sequence2[]={1,5,9,13,17,21,25,29,33,37,41,45};
		final int sequence3[]={2,6,10,14,18,22,26,30,34,38,42,46};
		final int ham1[]={48,52,56,60,64};
		final int ham2[]={49,53,57,61,65};
		final int ham3[]={50,54,58,62,66};
		boolean[] d=new boolean[12];
		boolean[] p=new boolean[5];
		boolean[] c=new boolean[5];
		// Row 1
		for (a=0;a<12;a++)	{
			pos=sequence1[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham1[a];
				p[a]=raw[pos];
			}
		}
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}
		// Row 2
		for (a=0;a<12;a++)	{
			pos=sequence2[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham2[a];
				p[a]=raw[pos];
			}
		}
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}
		// Row 3
		for (a=0;a<12;a++)	{
			pos=sequence3[a];
			d[a]=raw[pos];
			if (a<5)	{
				pos=ham3[a];
				p[a]=raw[pos];
			}
		}
		c[0]=d[0]^d[1]^d[2]^d[3]^d[6]^d[7]^d[9];
		c[1]=d[0]^d[1]^d[2]^d[3]^d[4]^d[7]^d[8]^d[10];
		c[2]=d[1]^d[2]^d[3]^d[4]^d[5]^d[8]^d[9]^d[11];
		c[3]=d[0]^d[1]^d[4]^d[5]^d[7]^d[10];
		c[4]=d[0]^d[1]^d[2]^d[5]^d[6]^d[8]^d[11];
		for (a=0;a<5;a++)	{
			if (c[a]!=p[a]) return false;
		}
		// All done so must have passed
		return true;
	}
	
	// Test if the short LC passes its CRC8 test
	private boolean shortLCcrc (boolean dataBits[])	{
		int a;
		tCRC.setCrc8Value(0);
		for (a=0;a<dataBits.length;a++)	{
			tCRC.crc8(dataBits[a]);
		}
		if (tCRC.getCrc8Value()==0) return true;
		else return false;
	}
	
	// Decode and display the info in SHORT LC PDUs
	private StringBuilder decodeShortLC (boolean db[])	{
		int slco,a;
		StringBuilder tsb=new StringBuilder(250);
		// Calculate the SLCO
		if (db[0]==true) slco=8;
		else slco=0;
		if (db[1]==true) slco=slco+4;
		if (db[2]==true) slco=slco+2;
		if (db[3]==true) slco++;
		// Short LC Types
		if (slco==0)	{
			tsb.append("Nul_Msg");
		}
		else if (slco==1)	{
			int addr1,addr2,inf;
			tsb.append("Act_Updt - ");
			// Slot 1
			if (db[4]==true) inf=8;
			else inf=0;
			if (db[5]==true) inf=inf+4;
			if (db[6]==true) inf=inf+2;
			if (db[7]==true) inf++;
			tsb.append(decodeAct_Updt(inf,1));
			// Hashed Address
			if (inf!=0)	{
				if (db[12]==true) addr1=128;
				else addr1=0;
				if (db[13]==true) addr1=addr1+64;
				if (db[14]==true) addr1=addr1+32;
				if (db[15]==true) addr1=addr1+16;
				if (db[16]==true) addr1=addr1+8;
				if (db[17]==true) addr1=addr1+4;
				if (db[18]==true) addr1=addr1+2;
				if (db[19]==true) addr1++;
				tsb.append(" Hashed Addr "+Integer.toString(addr1));
			}
			tsb.append(" : ");
			// Slot 2
			if (db[8]==true) inf=8;
			else inf=0;
			if (db[9]==true) inf=inf+4;
			if (db[10]==true) inf=inf+2;
			if (db[11]==true) inf++;
			tsb.append(decodeAct_Updt(inf,2));
			if (inf!=0)	{
				// Hashed Address
				if (db[20]==true) addr2=128;
				else addr2=0;
				if (db[21]==true) addr2=addr2+64;
				if (db[22]==true) addr2=addr2+32;
				if (db[23]==true) addr2=addr2+16;
				if (db[24]==true) addr2=addr2+8;
				if (db[25]==true) addr2=addr2+4;
				if (db[26]==true) addr2=addr2+2;
				if (db[27]==true) addr2++;
				tsb.append(" Hashed Addr "+Integer.toString(addr2));
			}
		}
		else	{
			tsb.append("Unknown SLCO="+Integer.toString(slco)+" ");
			for (a=4;a<28;a++)	{
				if (db[a]==true) tsb.append("1");
				tsb.append("0");
			}
		}
		
		return tsb;
	}
	
	// Decode a 4 bit section of an Act_Updt
	private String decodeAct_Updt (int inf,int channel)	{
		String l="Reserved";
		if (inf==0) l="No activity on BS time slot "+Integer.toString(channel);
		if (inf==2) l="Group CSBK activity on BS time slot "+Integer.toString(channel);
		if (inf==3) l="Individual CSBK activity on BS time slot "+Integer.toString(channel);
		if (inf==8) l="Group voice activity on BS time slot "+Integer.toString(channel);
		if (inf==9) l="Individual voice activity on BS time slot "+Integer.toString(channel);
		if (inf==10) l="Individual data activity on BS time slot "+Integer.toString(channel);
		if (inf==11) l="Group data activity on BS time slot "+Integer.toString(channel);
		if (inf==12) l="Emergency group activity on BS time slot "+Integer.toString(channel);
		if (inf==13) l="Emergency individual voice activity on BS time slot "+Integer.toString(channel);
		return l;
	}
	
}
