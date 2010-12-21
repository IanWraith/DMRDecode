package com.dmr;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class DisplayBar extends JPanel {
	public static final long serialVersionUID = 1;
	private Border loweredbevel=BorderFactory.createLoweredBevelBorder();
	static final int BUFFERMAX=200;
	private int bufferCounter=0;
	private int symbolBuffer[]=new int[BUFFERMAX];
	private int max,min,centre,umid,lmid;
	
	public DisplayBar () {
		this.setBorder(loweredbevel);
	}
	
	@Override public void paintComponent(Graphics g) {
        super.paintComponent(g);    // paints background
        int a;
        if (bufferCounter==0) return;
        for (a=0;a<BUFFERMAX;a++)	{
        	g.drawRect(5,symbolBuffer[a],2,2);
        }
   }
	
	public void addToBuffer (int tsymbol)	{
		symbolBuffer[bufferCounter]=tsymbol+300;
		bufferCounter++;
		if (bufferCounter==BUFFERMAX)	{
			repaint();
			bufferCounter=0;
		}
	}
	
	public void setDisplayBarParams (int tmax,int tmin,int tcentre,int tumid,int tlmid)	{
		max=tmax;
		min=tmin;
		centre=tcentre;
		umid=tumid;
		lmid=tlmid;
	}

}
