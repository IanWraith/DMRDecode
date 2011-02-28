// Please note much of the code in this program was taken from the DSD software
// and converted into Java. The author of this software is unknown but has the
// GPG Key ID below

// Copyright (C) 2010 DSD Author
// GPG Key ID: 0x3F1D7FD0 (74EF 430D F7F2 0A48 FCE6  F630 FAA2 635D 3F1D 7FD0)
// 
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND ISC DISCLAIMS ALL WARRANTIES WITH
// REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS.  IN NO EVENT SHALL ISC BE LIABLE FOR ANY SPECIAL, DIRECT,
// INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
// LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE
// OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
// PERFORMANCE OF THIS SOFTWARE.
//

package com.dmr;

import javax.swing.JComponent;
import java.util.Observer;
import java.util.Observable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class DisplayView extends JComponent implements Observer  {	
	public static final long serialVersionUID=1;
	private static final int DISPLAYCOUNT=150;
	private String display_string[]=new String[DISPLAYCOUNT];
	private Color displayColour[]=new Color[DISPLAYCOUNT];
	private Font displayFont[]=new Font[DISPLAYCOUNT];
	private int displayCounter=0;
	private DMRDecode theApp;	
	
	public DisplayView (DMRDecode theApp) {
		this.theApp=theApp;	
	}
			
	public void update (Observable o,Object rectangle)	{			
	}
			
	// Draw the main screen //
	public void paint (Graphics g) {
		int count=0,pos=20;
		int i=displayCounter;
		Graphics2D g2D=(Graphics2D)g;	
		// Draw in the lines on the screen
		// taking account of the fact that the data is stored in a circular buffer
		while(count<DISPLAYCOUNT)	{
			// Only display info if something is stored in the display string
			if (display_string[i]!=null)	{
				g.setColor(displayColour[i]);
				g.setFont(displayFont[i]);
				g2D.drawString(display_string[i],(5-theApp.horizontal_scrollbar_value),(pos-theApp.vertical_scrollbar_value));	
				pos=pos+20;
			}	
			i++;
			if (i==DISPLAYCOUNT) i=0;
			count++;
		}
	}
	
	// Add a line to the display circular buffer //
	public void add_line (String line,Color tcol,Font tfont) {
		display_string[displayCounter]=line;
		displayColour[displayCounter]=tcol;
		displayFont[displayCounter]=tfont;
		// Increment the circular buffer
		displayCounter++;
		// Check it hasn't reached its maximum size
		if (displayCounter==DISPLAYCOUNT) displayCounter=0;
		repaint();
	}

}