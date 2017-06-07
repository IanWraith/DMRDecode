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
	
	// Return an 16 bit byte from a boolean array
	public int retSixteen (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=32768;
		if (bits[offset+1]==true) b=b+16384;
		if (bits[offset+2]==true) b=b+8192;
		if (bits[offset+3]==true) b=b+4096;
		if (bits[offset+4]==true) b=b+2048;
		if (bits[offset+5]==true) b=b+1024;
		if (bits[offset+6]==true) b=b+512;
		if (bits[offset+7]==true) b=b+256;
		if (bits[offset+8]==true) b=b+128;
		if (bits[offset+9]==true) b=b+64;
		if (bits[offset+10]==true) b=b+32;
		if (bits[offset+11]==true) b=b+16;
		if (bits[offset+12]==true) b=b+8;
		if (bits[offset+13]==true) b=b+4;
		if (bits[offset+14]==true) b=b+2;
		if (bits[offset+15]==true) b++;
		return b;
	}	
	
	// Return an 12 bit byte from a boolean array
	public int retTwelve (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=2048;
		if (bits[offset+2]==true) b=b+1024;
		if (bits[offset+2]==true) b=b+512;
		if (bits[offset+3]==true) b=b+256;
		if (bits[offset+4]==true) b=b+128;
		if (bits[offset+5]==true) b=b+64;
		if (bits[offset+6]==true) b=b+32;
		if (bits[offset+7]==true) b=b+16;
		if (bits[offset+8]==true) b=b+8;
		if (bits[offset+9]==true) b=b+4;
		if (bits[offset+10]==true) b=b+2;
		if (bits[offset+11]==true) b++;
		return b;
	}		

	// Return an 9 bit byte from a boolean array
	public int retNine (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=256;
		if (bits[offset+1]==true) b=b+128;
		if (bits[offset+2]==true) b=b+64;
		if (bits[offset+3]==true) b=b+32;
		if (bits[offset+4]==true) b=b+16;
		if (bits[offset+5]==true) b=b+8;
		if (bits[offset+6]==true) b=b+4;
		if (bits[offset+7]==true) b=b+2;
		if (bits[offset+7]==true) b++;
		return b;
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
	
	// Return an 7 bit byte from a boolean array
	public int retSeven (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=64;
		if (bits[offset+1]==true) b=b+32;
		if (bits[offset+2]==true) b=b+16;
		if (bits[offset+3]==true) b=b+8;
		if (bits[offset+4]==true) b=b+4;
		if (bits[offset+5]==true) b=b+2;
		if (bits[offset+6]==true) b++;
		return b;
	}	
	
	// Return a 6 bit byte from a boolean array
	public int retSix (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=32;
		if (bits[offset+1]==true) b=b+16;
		if (bits[offset+2]==true) b=b+8;
		if (bits[offset+3]==true) b=b+4;
		if (bits[offset+4]==true) b=b+2;
		if (bits[offset+5]==true) b++;
		return b;
	}
	
	// Return a 5 bit byte from a boolean array
	public int retFive (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=16;
		if (bits[offset+1]==true) b=b+8;
		if (bits[offset+2]==true) b=b+4;
		if (bits[offset+3]==true) b=b+2;
		if (bits[offset+4]==true) b++;
		return b;
	}	
	
	// Return a 4 bit byte from a boolean array
	public int retFour (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=8;
		if (bits[offset+1]==true) b=b+4;
		if (bits[offset+2]==true) b=b+2;
		if (bits[offset+3]==true) b++;
		return b;
	}	
	
	// Return a 3 bit byte from a boolean array
	public int retThree (boolean bits[],int offset)	{
		int b=0;
		if (bits[offset]==true) b=4;
		if (bits[offset+1]==true) b=b+2;
		if (bits[offset+2]==true) b++;
		return b;
	}	
	
	// Decode and display Service Options
	public String decodeServiceOptions (boolean bits[],int offset)	{
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
