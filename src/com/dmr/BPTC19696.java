package com.dmr;

import javax.swing.JOptionPane;

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
		int a,interleaveSequence,pos=0;
		for (a=0;a<196;a++)	{
			// Calculate the interleave sequence
			interleaveSequence=(a*13)%196;
			// Ignore the first bit as this is R(3) which is not used
			if (interleaveSequence>0)	{
				// Shuffle the data
				deInterData[pos]=rawData[interleaveSequence];
				// Data fills the array in columns
				pos=pos+15;
				if (pos>194) pos=pos-194;
			}
		}
	}
	
	private boolean testHorizontalTheory ()	{
		boolean row[]=new boolean[15];
		// R2
		row[0]=rawData[13];
		// R1
		row[1]=rawData[182];
		// R0
		row[2]=rawData[155];
		// I95
		row[3]=rawData[128];
		// I94
		row[4]=rawData[101];
		// I93
		row[5]=rawData[74];
		// I92
		row[6]=rawData[47];
		// I91
		row[7]=rawData[20];
		// I90
		row[8]=rawData[189];
		// I89
		row[9]=rawData[162];
		// I88
		row[10]=rawData[135];
		// H_R1(3)
		row[11]=rawData[108];
		// H_R1(2)
		row[12]=rawData[81];
		// H_R1(1)
		row[13]=rawData[54];
		// H_R1(0)
		row[14]=rawData[27];
		
		boolean tst=hamming15113(row);
		
		return tst;
	}
	
	private boolean testVerticalTheory ()	{
		boolean col[]=new boolean[13];
		col[0]=rawData[13];
		col[1]=rawData[26];
		col[2]=rawData[39];
		col[3]=rawData[52];
		col[4]=rawData[65];
		col[5]=rawData[78];
		col[6]=rawData[91];
		col[7]=rawData[104];
		col[8]=rawData[117];
		col[9]=rawData[130];
		col[10]=rawData[143];
		col[11]=rawData[156];
		col[12]=rawData[169];
		
		boolean tst=hamming1393(col);
		
		return tst;
		
	}
	
	// Check each row with a Hamming (15,11,3) code
	// Return false if there is a problem
	private boolean errorCheck ()	{
		int a,r,c,pos,rowCount=0,colCount=0;
		boolean row[]=new boolean[15];
		boolean col[]=new boolean[13];
		// Run through each of the 9 rows containing data
		for (r=0;r<9;r++)	{
			for (a=0;a<15;a++)	{
				pos=(r*15)+a;
				row[a]=deInterData[pos];
			}
			if (hamming15113(row)==true) rowCount++;
		}
		
		// Run through each of the 15 columns
		for (c=0;c<15;c++)	{
			for (a=0;a<13;a++){
				pos=(c*13)+a;
				col[a]=deInterData[pos];
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

}
