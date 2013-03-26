package test.com.dmr;

import junit.framework.TestCase;
import com.dmr.Trellis;

public class TrellisTest extends TestCase {


	public void testTrellis()	{
		
		// A couple of sample 3/4 rate frames
		
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
		
		Trellis trellis=new Trellis();
		
		trellis.decode(threequarterData1);
		
		trellis.decode(threequarterData2);
		
		trellis.decode(threequarterData3);
		
		trellis.decode(threequarterBadData1);
		
	}
	

}
