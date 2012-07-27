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
		int a,r,c,pos;
		boolean row[]=new boolean[15];
		boolean col[]=new boolean[13];
		// Run through each of the 9 rows containing data
		for (r=0;r<9;r++)	{
			pos=(r*15)+1;
			for (a=0;a<15;a++)	{
				row[a]=deInterData[pos];
				pos++;
			}
			if (hamming15113(row)==false) return false;
		}
		// Run through each of the 15 columns
		for (c=0;c<15;c++)	{
			pos=c+1;
			for (a=0;a<13;a++){
				col[a]=deInterData[pos];
				pos=pos+15;
			}
			if (hamming1393(col)==false) return false;
		}
		
	return true;
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
		int a,pos=0;
		for (a=4;a<=11;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=16;a<=26;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=31;a<=41;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=46;a<=56;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=61;a<=71;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=76;a<=86;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=91;a<=101;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=106;a<=116;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
		for (a=121;a<=131;a++)	{
			outData[pos]=deInterData[a];
			pos++;
		}
	}
	
	// Pass the data output on
	public boolean[] dataOut()	{
		return outData;
	}
	
}
