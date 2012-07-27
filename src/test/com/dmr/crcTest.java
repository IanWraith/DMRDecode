package test.com.dmr;

import com.dmr.crc;

import junit.framework.TestCase;

public class crcTest extends TestCase {
	
	// Test the CRC8 code
	public void testCRC8 ()	{
		int a,returnCRC;
		crc crctest=new crc();
		boolean testBinaryPass[]={true,true,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false};
		boolean testBinaryFail[]={false,true,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,false,false};
		// Test for a pass
		crctest.setCrc8Value(0);
		for (a=0;a<testBinaryPass.length;a++)	{
			crctest.crc8(testBinaryPass[a]);
		}
		returnCRC=crctest.getCrc8Value();
		assertEquals(0,returnCRC);
		// Test for a failure
		crctest.setCrc8Value(0);
		for (a=0;a<testBinaryFail.length;a++)	{
			crctest.crc8(testBinaryFail[a]);
		}
		returnCRC=crctest.getCrc8Value();
		assertEquals(152,returnCRC);
	}
	
	// Test the CSBK CCITT CRC code
	public void testcrcCSBK ()	{
		boolean ok1,ok2,ok3;
		crc crctest=new crc();
		boolean testCSBK1[]={true,false,true,true,true,true,true,false,false,false,false,true,false,false,false,false,true,true,true,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false,false,true,true,false,true,true,false,true,true,false,false,true};
		boolean testCSBK2[]={true,false,true,true,true,true,true,false,false,false,false,true,false,false,false,false,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,false,true,true,true,false,false,false,false,true,true};
		boolean testCSBK3[]={true,false,true,true,true,true,true,false,false,false,false,true,false,false,false,false,true,true,false,false,false,false,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,false,true,true,true,false,false,false,false,false};
		ok1=crctest.crcCSBK(testCSBK1);
		ok2=crctest.crcCSBK(testCSBK2);
		ok3=crctest.crcCSBK(testCSBK3);
	    assertEquals(true,ok1);	
	    assertEquals(true,ok2);
	    assertEquals(true,ok3);	
	    
	    
	}
	
	// Test the Data Header CCITT CRC code
	public void testcrcDataHeader ()	{
		boolean ok1,ok2,ok3;
		crc crctest=new crc();
		boolean testDH1[]={false,false,false,true,true,true,true,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,true,false,false,true,false,false,false,true,false,false,false,false,false,false,false,false,false,false,true,false,false,false,true,true,true,false,false,false,false,false,false,false,false,false,true,true,true,false,false,true,false,false,false,false,false,true,true,true,true,true,false,false,false,true,false,true,true,false,false,false,false,false,false,false,false};
		boolean testDH2[]={false,false,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,false,true,false,true,true,true,true,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,false,true,false,false,true,false,true,true,true,false,true,false,false};
		boolean testDH3[]={false,true,false,false,false,false,false,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,false,false,false,false,false,false,false,false,true,true,true,true,true,false,true,false,true,true,true,true,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,true,true,false,true,false,false,true,false,true,true,true,false,true,false,false};
		ok1=crctest.crcDataHeader(testDH1);
		ok2=crctest.crcDataHeader(testDH2);
		ok3=crctest.crcDataHeader(testDH3);
	    assertEquals(true,ok1);	
	    assertEquals(true,ok2);
	    assertEquals(false,ok3);	
	}
	
	// Test the Reed-Solomon (12,9) code
	public void testRS129 ()	{
		boolean ok1;
		crc crctest=new crc();
		boolean testHeaderRS[]={false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,true,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,true,false,true,false,true,false,true,false,true,false,false,false,false,false,true,true,false,false,true,false,false,true,true,true,false,true,false};
		ok1=crctest.RS129(testHeaderRS);
		assertEquals(true,ok1);	
	}
	

}
