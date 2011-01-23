package com.dmr;

import junit.framework.TestCase;

public class CSBKTest extends TestCase {
	
	public void testdecode ()	{
		String olines[]=new String[3];
		boolean csbk_preamble[]={true,false,true,true,true,true,false,true,false,false,false,false,false,false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false,true,true,true,true,true,false,true,false,true,true,true,true,true,false,true,false,true,true,true,true,true,false,true,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,true,true,true,false,true,false,true,true,true,false,true,true,false,true,true,false,false,false,true,false};
			
		CSBK csbktest=new CSBK();
		olines=csbktest.decode(csbk_preamble);
	
	}

}
