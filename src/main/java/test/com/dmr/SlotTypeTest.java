package test.com.dmr;

import com.dmr.SlotType;

import junit.framework.TestCase;

public class SlotTypeTest extends TestCase {
	
	public void testDecode ()	{
		byte uniFrame[]={2,0,2,0,2,0,0,2,0,0,0,0,1,1,0,3,3,0,0,2,1,1,3,2,2,2,2,3,2,2,2,0,1,2,1,3,0,1,3,1,3,0,1,3,0,3,2,0,0,3,2,3,3,1,2,1,0,3,1,2,0,1,1,2,1,2,3,1,3,3,3,3,1,1,1,3,3,1,1,3,1,1,3,1,3,3,1,1,3,1,3,3,1,0,3,3,3,3,1,2,3,2,1,0,1,2,1,1,0,1,1,3,0,1,2,3,1,0,2,0,3,0,2,2,1,2,3,1,1,0,3,3,3,0,1,2,0,1,0,0,2,3,1,0};
		SlotType slottype=new SlotType();
		String sret=slottype.decode(null, uniFrame);
		assertEquals("Slot Type : Colour Code 5 Idle",sret);
	}

}
