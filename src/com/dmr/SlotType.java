package com.dmr;

public class SlotType {
	
	private int dibit_buf[]=new int[132];
	private String line;
	private boolean passErrorCheck;
	
	public String decode (int[] buf)	{
		dibit_buf=buf;
		line="Slot Type : ";
		passErrorCheck=mainDecode();
		return line;
	}
	

	private boolean mainDecode ()	{
		int a,r,t1;
		boolean rawdataSLOT[]=new boolean[20];
		boolean dataSLOT[]=new boolean[20];
		boolean res=false;
		// Convert from dibit into boolean
		// DATA SLOT is broken into 2 parts
		r=0;
		for (a=61;a<66;a++)	{
			if (dibit_buf[a]==0)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=true;
			}
			r=r+2;
		}
		for (a=90;a<95;a++)	{
			if (dibit_buf[a]==0)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawdataSLOT[r]=false;
				rawdataSLOT[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawdataSLOT[r]=true;
				rawdataSLOT[r+1]=true;
			}
			r=r+2;
		}	
		
		for (a=0;a<20;a++)	{
			if (rawdataSLOT[a]==false) line=line+"0";
			else line=line+"1";
			if (a==9) line=line+" ";
			
		}
		
		return res;
	}
	
	// Code to calculate all valid values for Golay (20,8)
	void calcGolay208 ()	{
		boolean d[]=new boolean[8];
		boolean p[]=new boolean[12];
		int value[]=new int[256];
		int a;
		// Run through all possible 8 bit values
		for (a=0;a<256;a++){
			// Convert to binary
			if ((a&127)>0) d[0]=true;
			else d[0]=false;
			if ((a&64)>0) d[1]=true;
			else d[1]=false;
			if ((a&32)>0) d[2]=true;
			else d[2]=false;
			if ((a&16)>0) d[3]=true;
			else d[3]=false;
			if ((a&8)>0) d[4]=true;
			else d[4]=false;
			if ((a&4)>0) d[5]=true;
			else d[5]=false;
			if ((a&2)>0) d[6]=true;
			else d[6]=false;
			if ((a&1)>0) d[7]=true;
			else d[7]=false;
			// Calculate the parity bits
			p[0]=d[1]^d[4]^d[5]^d[6]^d[7];
			p[1]=d[1]^d[2]^d[4];
			p[2]=d[0]^d[2]^d[3]^d[5];
			p[3]=d[0]^d[1]^d[3]^d[4]^d[6];
			p[4]=d[0]^d[1]^d[2]^d[4]^d[5]^d[7];
			p[5]=d[0]^d[2]^d[3]^d[4]^d[7];
			p[6]=d[3]^d[6]^d[7];
			p[7]=d[0]^d[1]^d[5]^d[6];
			p[8]=d[0]^d[1]^d[2]^d[6]^d[7];
			p[9]=d[2]^d[3]^d[4]^d[5]^d[6];
			p[10]=d[0]^d[3]^d[4]^d[5]^d[6]^d[7];
			p[11]=d[1]^d[2]^d[3]^d[5]^d[7];
			// Shift the value 12 times to the left
			value[a]=a<<12;
			if (p[0]==true) value[a]=value[a]+2048;
			if (p[1]==true) value[a]=value[a]+1024;
			if (p[2]==true) value[a]=value[a]+512;
			if (p[3]==true) value[a]=value[a]+256;
			if (p[4]==true) value[a]=value[a]+128;
			if (p[5]==true) value[a]=value[a]+64;
			if (p[6]==true) value[a]=value[a]+32;
			if (p[7]==true) value[a]=value[a]+16;
			if (p[8]==true) value[a]=value[a]+8;
			if (p[9]==true) value[a]=value[a]+4;
			if (p[10]==true) value[a]=value[a]+2;
			if (p[11]==true) value[a]=value[a]+1;
		}
		// Just something to break on !
		a++;
	}
	
	// Check if a 20 bit boolean array has the collect Golay (20,8) coding
	boolean checkGolay208 (boolean[] word)	{
		int a,val;
		final int[]GolayNums={0, 6961, 10980, 12815, 18765, 20902, 24691, 30872, 
				36380, 38647, 42786, 49097, 50315, 56416, 60853, 62814, 65725, 
				71766, 76163, 78184, 84522, 86721, 90900, 97279, 101755, 103824, 
				107589, 113838, 116716, 122631, 126674, 128569, 132375, 138748, 
				142377, 144578, 151424, 153451, 157374, 163413, 166097, 167994, 
				172527, 178436, 180806, 187053, 191352, 193427, 198256, 204443, 
				208718, 210853, 216295, 218124, 222681, 228658, 232374, 234333,
				238216, 244323, 246049, 252362, 256031, 258292, 265795, 267944, 
				272253, 278422, 279764, 285759, 290282, 292097, 295813, 301934, 
				305851, 307792, 313618, 315897, 319532, 325831, 331044, 333263, 
				336922, 343281, 346035, 352088, 355981, 357990, 360674, 366601, 
				371164, 373047, 379509, 381598, 385867, 392096, 395406, 397413, 
				401840, 407899, 410137, 416498, 420647, 422860, 427336, 433571, 
				437366, 439453, 446431, 448308, 452321, 458250, 461801, 463618, 
				467671, 473660, 475518, 481685, 485440, 487595, 493103, 499396, 
				503569, 505850, 511160, 513107, 517510, 523629, 524288, 531249, 
				535268, 537103, 543053, 545190, 548979, 555160, 560668, 562935, 
				567074, 573385, 574603, 580704, 585141, 587102, 590013, 596054, 
				600451, 602472, 608810, 611009, 615188, 621567, 626043, 628112, 
				631877, 638126, 641004, 646919, 650962, 652857, 656663, 663036, 
				666665, 668866, 675712, 677739, 681662, 687701, 690385, 692282, 
				696815, 702724, 705094, 711341, 715640, 717715, 722544, 728731, 
				733006, 735141, 740583, 742412, 746969, 752946, 756662, 758621, 
				762504, 768611, 770337, 776650, 780319, 782580, 790083, 792232, 
				796541, 802710, 804052, 810047, 814570, 816385, 820101, 826222, 
				830139, 832080, 837906, 840185, 843820, 850119, 855332, 857551, 
				861210, 867569, 870323, 876376, 880269, 882278, 884962, 890889, 
				895452, 897335, 903797, 905886, 910155, 916384, 919694, 921701, 
				926128, 932187, 934425, 940786, 944935, 947148, 951624, 957859, 
				961654, 963741, 970719, 972596, 976609, 982538, 986089, 987906, 
				991959, 997948, 999806, 1005973, 1009728, 1011883, 1017391, 1023684, 
				1027857, 1030138, 1035448, 1037395, 1041798, 1047917};
		// Convert the boolean to a numerical value
		if (word[19]==true) val=1;
		else val=0;
		if (word[18]==true) val=val+2;
		if (word[17]==true) val=val+4;
		if (word[16]==true) val=val+8;
		if (word[15]==true) val=val+16;
		if (word[14]==true) val=val+32;
		if (word[13]==true) val=val+64;
		if (word[12]==true) val=val+128;
		if (word[11]==true) val=val+256;
		if (word[10]==true) val=val+512;
		if (word[9]==true) val=val+1024;
		if (word[8]==true) val=val+2048;
		if (word[7]==true) val=val+4096;
		if (word[6]==true) val=val+8192;
		if (word[5]==true) val=val+16384;
		if (word[4]==true) val=val+32768;
		if (word[3]==true) val=val+65536;
		if (word[2]==true) val=val+131072;
		if (word[1]==true) val=val+262144;
		if (word[0]==true) val=val+524288;
		// Run through the possible values
		for (a=0;a<256;a++)	{
			if (val==GolayNums[a]) return true;
		}
		return false;
	}
	
	
}
