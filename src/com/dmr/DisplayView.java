package com.dmr;

import javax.swing.JComponent;
import java.util.Observer;
import java.util.Observable;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class DisplayView extends JComponent implements Observer  {
	private DMRDecode theApp;	
	public static final long serialVersionUID=1;
	static public String display_string[]=new String[100];
			
	public DisplayView (DMRDecode theApp) {
		this.theApp=theApp;	
	}
			
	public void update (Observable o,Object rectangle)	{			
	}
			
	// Draw the main screen //
	public void paint (Graphics g) {
		int i;
		int pos=20;
		Graphics2D g2D=(Graphics2D)g;	
		// Draw in the lines on the screen
		for (i=0;i<100;i++) {
			if (display_string[i]!=null) g2D.drawString(display_string[i],(5-theApp.horizontal_scrollbar_value),(pos-theApp.vertical_scrollbar_value));	
			pos=pos+20;
		}
	}

	// Add a line to the display //
	public void add_line (String line) {
		int i;
		for (i=99;i>0;i--) {
			display_string[i]=display_string[i-1];
		}
		display_string[0]=line;
		repaint();
	}
	

}