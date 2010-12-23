package com.dmr;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class DisplayBar extends JPanel {
	public static final long serialVersionUID=1;
	private Border loweredbevel=BorderFactory.createLoweredBevelBorder();
	static final int BUFFERMAX=75;
	private int bufferCounter=0;
	private int symbolBuffer[]=new int[BUFFERMAX];
	private int max,min,umid,lmid,fullRange;
	private boolean displayActive=false,enableDisplay=false;
	
	public DisplayBar () {
		this.setBorder(loweredbevel);
	}
	
	// Draw the display in the JPanel
	@Override public void paintComponent(Graphics g) {
		int a,val;
		int height=this.getHeight();
		int width=this.getWidth();
		double modFactor=((float)height/(float)fullRange);
		// Repaint the background
		super.paintComponent(g);  
		// If the display isn't active then don't go any further
		if ((displayActive==false)||(enableDisplay==false)) return;
        // Draw the centre line 
        g.drawLine(0,(height/2),width,(height/2));
        // Draw the lmid line
        val=lmid+Math.abs(min);
        val=(int)((float)val*modFactor);
        g.drawLine(0,val,width,val);
        // Draw the lmid line
        val=umid+Math.abs(min);
        val=(int)((float)val*modFactor);
        g.drawLine(0,val,width,val);
        // Draw the symbol points
        for (a=0;a<BUFFERMAX;a++)	{
        	val=symbolBuffer[a]+Math.abs(min);
        	val=(int)((float)val*modFactor);
          	g.fillRect((width/2)-2,val,5,5);
        }
   }
	
	// Add a symbol to a circular buffer which is displayed
	public void addToBuffer (int tsymbol)	{
		symbolBuffer[bufferCounter]=tsymbol;
		bufferCounter++;
		// Repaint every 25 samples
		if ((bufferCounter%25)==0) repaint();
		// Check if the circular buffer counter has reached its maximum
		if (bufferCounter==BUFFERMAX) bufferCounter=0;	
	}
	
	// Set the displays parameters
	public void setDisplayBarParams (int tmax,int tmin,int tumid,int tlmid)	{
		max=tmax;
		min=tmin;
		umid=tumid;
		lmid=tlmid;
		// Calculate the full range needed
		fullRange=Math.abs(max)+Math.abs(min);
		// Enable the display and do a repaint
		displayActive=true;
		repaint();
	}
	
	// Stop the display if sync is lost
	public void stopDisplay()	{
		displayActive=false;
		repaint();
	}
	
	// Enable or disable the display
	public void setEnableDisplay (boolean val)	{
		enableDisplay=val;
		repaint();
	}

	
	
}
