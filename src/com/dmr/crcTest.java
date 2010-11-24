package com.dmr;

import junit.framework.TestCase;

public class crcTest extends TestCase {
	
	// Test the CRC8 code
	public void testCRC8 ()	{
		int a,returnCRC;
		crc crctest=new crc();
		//boolean testBinary[]={true,true,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,true,false,false,false,false,false,false,false,false,false,true,false,false,true,true,true,true};
		boolean testBinary[]={true,true,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false};
		//boolean testBinary[]={false,false,false,true,true,false,false,false,false,false,false,false,true,false,true,false,true,false,false,true,false,false,false,false,false,false,false,false,true,false,true,true,true,false,false,false};
		//boolean testBinary[]={false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
		crctest.setCrc8Value(0);
		for (a=0;a<testBinary.length;a++)	{
			crctest.crc8(testBinary[a]);
		}
		returnCRC=crctest.getCrc8Value();
		assertEquals(0,returnCRC);
	}

}
