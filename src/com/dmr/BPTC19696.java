package com.dmr;

public class BPTC19696 {
	private boolean rawData[]=new boolean[196];
	private boolean deInterData[]=new boolean[196];
	private boolean outData[]=new boolean[96];
	
	// The main decode function
	public boolean decode (byte[] dibit_buf)	{
		// Get the raw binary
		extractBinary(dibit_buf);
		// Deinterleave
		deInterleave();
		
		recordData();
		
		// Error check
		if (errorCheck()==true)	{
			// Extract Data
			extractData();
			return true;
		}
		else return false;
	}
	
	// Extract the binary from the dibit data
	private	void extractBinary (byte[] dibit_buf)	{
		int a,r=0;
		// First block
		for (a=12;a<61;a++)	{
			if (dibit_buf[a]==0)	{
				rawData[r]=false;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawData[r]=false;
				rawData[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawData[r]=true;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawData[r]=true;
				rawData[r+1]=true;
			}
			r=r+2;
		}
		// Second block
		for (a=95;a<144;a++)	{
			if (dibit_buf[a]==0)	{
				rawData[r]=false;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==1)	{
				rawData[r]=false;
				rawData[r+1]=true;
			}
			else if (dibit_buf[a]==2)	{
				rawData[r]=true;
				rawData[r+1]=false;
			}
			else if (dibit_buf[a]==3)	{
				rawData[r]=true;
				rawData[r+1]=true;
			}
			r=r+2;
		}
	}
	
	// Deinterleave the raw data
	private void deInterleave ()	{
		int a,interleaveSequence;
		// The first bit is R(3) which is not used so can be ignored
		for (a=0;a<196;a++)	{
			// Calculate the interleave sequence
			interleaveSequence=(a*181)%196;
			// Shuffle the data
			deInterData[a]=rawData[interleaveSequence];
		}
	}
	
	// Check each row with a Hamming (15,11,3) code
	// Return false if there is a problem
	private boolean errorCheck ()	{
		int a,r,c,pos,rowCount=0,colCount=0;
		boolean row[]=new boolean[15];
		boolean col[]=new boolean[13];
		// Run through each of the 9 rows containing data
		for (r=0;r<9;r++)	{
			pos=r+1;
			for (a=0;a<15;a++)	{
				row[a]=deInterData[pos];
				pos=pos+13;
			}
			if (hamming15113(row)==true) rowCount++;
		}
		// Run through each of the 15 columns
		pos=1;
		for (c=0;c<15;c++)	{
			for (a=0;a<13;a++){
				col[a]=deInterData[pos];
				pos++;
			}
			if (hamming1393(col)==true) colCount++;
		}
		
	if ((rowCount==9)&&(colCount==15)) return true;
	else return false;
	}
	
	// Hamming (15,11,3) check a boolean data array
	private boolean hamming15113 (boolean d[])	{
		boolean c[]=new boolean[4];
		// Calculate the checksum this row should have
		c[0]=d[0]^d[1]^d[2]^d[3]^d[5]^d[7]^d[8];
		c[1]=d[1]^d[2]^d[3]^d[4]^d[6]^d[8]^d[9];
		c[2]=d[2]^d[3]^d[4]^d[5]^d[7]^d[9]^d[10];
		c[3]=d[0]^d[1]^d[2]^d[4]^d[6]^d[7]^d[10];
		// Compare these with the actual bits
		if ((c[0]==d[11])&&(c[1]==d[12])&&(c[2]==d[13])&&(c[3]==d[14])) return true;
		else return false;
	}
	
	// Hamming (13,9,3) check a boolean data array
	private boolean hamming1393 (boolean d[])	{
		boolean c[]=new boolean[4];
		// Calculate the checksum this column should have
		c[0]=d[0]^d[1]^d[3]^d[5]^d[6];
		c[1]=d[0]^d[1]^d[2]^d[4]^d[6]^d[7];
		c[2]=d[0]^d[1]^d[2]^d[3]^d[5]^d[7]^d[8];
		c[3]=d[0]^d[2]^d[4]^d[5]^d[8];
		// Compare these with the actual bits
		if ((c[0]==d[9])&&(c[1]==d[10])&&(c[2]==d[11])&&(c[3]==d[12])) return true;
		else return false;
	}
	
	// Extract the 96 bits of payload
	private void extractData()	{
		
		// TODO : Extract the 96 bits of payload data from the BPTC
		
	}
	
	private void recordData()	{
		int a,pos=1;
		String l="";
	
		for (a=1;a<196;a++)	{
			if (deInterData[pos]==false) l=l+"0";
			else l=l+"1";
			pos=pos+13;
			if (pos>195)	{
				l=l+"\n";
				pos=pos-194;
			}	
		}
		l=l+"\nRaw\n";
		for (a=0;a<196;a++)	{
			if (rawData[a]==false) l=l+"0";
			else l=l+"1";
		}
		l=l+"\nDeinterleaved\n";
		for (a=0;a<196;a++)	{
			if (deInterData[a]==false) l=l+"0";
			else l=l+"1";
		}
		String r=binaryDisp(1);
		r=r+binaryDisp(14);
		r=r+binaryDisp(27);
		r=r+binaryDisp(40);
		r=r+binaryDisp(53);
		r=r+binaryDisp(66);
		r=r+binaryDisp(79);
		r=r+binaryDisp(92);
		r=r+binaryDisp(105);
		r=r+binaryDisp(118);
		r=r+binaryDisp(131);
		r=r+binaryDisp(144);
		r=r+binaryDisp(157);
		r=r+binaryDisp(170);
		r=r+binaryDisp(183);
		
		
		r=r+" ";
		
	}
	
	private String binaryDisp(int t)	{
		if (deInterData[t]==false) return "0";
		else return "1";
	}
	

}
