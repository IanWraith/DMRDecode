package com.dmr;

public class Utilities {
	
	// Given a MFID as an int return the manufacturers name as a String
	public String returnMFIDName (int mfid)	{
		if (mfid==0x04) return "Fylde Micro";
		else if (mfid==0x05) return "PROD-EL SPA";
		else if (mfid==0x06) return "Trident Datacom";
		else if (mfid==0x07) return "RADIODATA";
		else if ((mfid==0x08)||(mfid==0x68)) return "HYT";
		else if (mfid==0x10) return "Motorola";
		else if ((mfid==0x13)||(mfid==0x1c)) return "EMC SPA";
		else if ((mfid==0x33)||(mfid==0x3c)) return "Radio Activity Srl";
		else if (mfid==0x58) return "Tait";
		else if (mfid==0x77) return "Vertex Standard";
		else return "Unknown";
	}
	
	// Return a 24 bit address 
	public int retAddress (boolean bits[],int offset)	{
		int addr=0,a,b,c;
		for (a=0;a<24;a++)	{
			b=(24-a)-1;
			c=(int)Math.pow(2.0,b);
			if (bits[a+offset]==true) addr=addr+c;
		}
		return addr;
	}
	
	// Return an 8 bit byte from a boolean array
	public int retEight (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=128;
		if (bits[offset+1]==true) b=b+64;
		if (bits[offset+2]==true) b=b+32;
		if (bits[offset+3]==true) b=b+16;
		if (bits[offset+4]==true) b=b+8;
		if (bits[offset+5]==true) b=b+4;
		if (bits[offset+6]==true) b=b+2;
		if (bits[offset+7]==true) b++;
		return b;
	}

}
