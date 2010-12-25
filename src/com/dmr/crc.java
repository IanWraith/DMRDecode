package com.dmr;

public class crc {
	private int crc8Value;

	public void setCrc8Value(int crc8Value) {
		this.crc8Value = crc8Value;
	}

	public int getCrc8Value() {
		return crc8Value;
	}

	// The CRC8 routine //
	public void crc8(boolean bit) {
        boolean shiftBit;
        if ((crc8Value&0x01)>0) shiftBit=true;
        else shiftBit=false;
        crc8Value=crc8Value>>1; 
        if ((bit^shiftBit)==true) crc8Value=crc8Value^0xe0; 
	}
	
}
