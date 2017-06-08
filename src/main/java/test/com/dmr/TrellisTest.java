package test.com.dmr;

import junit.framework.TestCase;
import com.dmr.Trellis;

public class TrellisTest extends TestCase {


	public void testTrellis()	{
		
		// Three good sample 3/4 rate frames
		final boolean threequarterData1[]={
				false,false,true,false,true,true,true,false,true,false, 
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				true,true,true,false,true,false,false,true,false,true,
				true,false,false,false,false,true,false,true,true,true,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,true,false,false,true,false,true,true,
				false,false,true,true,false,false,false,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,true,true,false,true,
				true,false,false,true,false,true,true,true,false,false,
				false,false,false,true,true,true,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,true,false,
				true,true,true,true,false,false};
		final boolean threequarterData2[]={
				false,false,true,false,true,false,true,true,false,false, 
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,true,true,true,false,true,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,true,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,true,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,false,false,true,false,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false};
		final boolean threequarterData3[]={
				false,false,true,false,true,false,false,false,false,true, 
				true,false,true,true,true,false,false,false,true,false,
				false,false,true,false,true,false,false,true,true,true,
				false,true,false,false,true,false,true,true,false,false,
				true,true,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,true,true,true,true,
				false,false,true,false,true,true,true,true,false,false,
				true,false,false,false,true,false,false,false,true,false,
				true,true,true,false,false,false,true,false,true,true,
				true,true,false,false,true,false,false,false,true,false,
				false,false,true,false,true,true,false,true,false,false,
				true,false,true,true,true,true,true,true,true,false,
				false,false,true,false,false,false,false,true,true,true,
				true,true,true,true,true,false,false,true,true,true,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,true,false,true,false,false,true,false,
				false,false,true,false,true,false,true,false,false,false,
				false,true,false,true,false,false,false,false,true,false,
				true,false,false,true,true,true,false,true,false,false,
				true,false,false,false,true,false};
		// and a bad frame
		final boolean threequarterBadData1[]={
				false,false,true,false,false,false,false,false,false,true, 
				true,false,true,true,true,false,false,false,true,false,
				false,false,true,false,true,false,false,true,true,true,
				false,true,false,false,true,false,true,true,false,false,
				true,true,true,false,false,false,true,false,false,false,
				true,false,false,false,true,false,true,true,true,true,
				false,false,true,false,true,true,true,true,false,false,
				true,false,false,false,true,false,false,false,true,false,
				true,true,true,true,false,false,true,false,true,true,
				true,true,false,false,true,false,false,false,true,false,
				false,false,true,false,true,true,false,true,false,false,
				true,false,true,true,true,true,true,true,true,false,
				false,false,true,false,false,false,false,true,true,true,
				true,true,true,true,true,false,false,true,true,true,
				false,false,true,false,false,false,true,false,false,false,
				true,false,false,true,false,true,false,false,true,false,
				false,false,true,false,true,false,true,false,false,false,
				false,true,true,false,false,false,false,false,true,false,
				true,false,false,true,true,true,false,true,false,false,
				true,false,false,false,true,false};
		
		// Now to test the Trellis class
		Trellis trellis=new Trellis();
		// Good frame 1
		boolean tst1[]=trellis.decode(threequarterData1);
		assertNotNull(tst1);
		// Good frame 2
		boolean tst2[]=trellis.decode(threequarterData2);
		assertNotNull(tst2);
		// Good frame 3
		boolean tst3[]=trellis.decode(threequarterData3);
		assertNotNull(tst3);
		// Bad frame
		boolean tst4[]=trellis.decode(threequarterBadData1);
		assertNull(tst4);
	}
	

}
