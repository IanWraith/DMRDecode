package com.dmr;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

public class JStatusBar extends JPanel {
	public static final long serialVersionUID = 1;
	private JLabel logMode=new JLabel();
	private JLabel syncLabel=new JLabel();
	private JLabel ch1Label=new JLabel();
	private JLabel ch2Label=new JLabel();
	private JLabel colourCodeLabel=new JLabel();
	private JLabel systemLabel=new JLabel();
	private JProgressBar volumeBar=new JProgressBar(0,100);
	private Border loweredbevel=BorderFactory.createLoweredBevelBorder();
	private JButton pauseButton=new JButton("Pause");
	private DMRDecode TtheApp;
	
	public JStatusBar() {
		logMode.setHorizontalAlignment(SwingConstants.LEFT);
		logMode.updateUI();
		logMode.setBorder(loweredbevel);
		syncLabel.setHorizontalAlignment(SwingConstants.LEFT);
		syncLabel.setBorder(loweredbevel);
		syncLabel.updateUI();
		ch1Label.setHorizontalAlignment(SwingConstants.LEFT);
		ch1Label.setBorder(loweredbevel);
		ch1Label.updateUI();
		ch2Label.setHorizontalAlignment(SwingConstants.LEFT);
		ch2Label.setBorder(loweredbevel);
		ch2Label.updateUI();
		colourCodeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		colourCodeLabel.setBorder(loweredbevel);
		colourCodeLabel.updateUI();
		systemLabel.setHorizontalAlignment(SwingConstants.LEFT);
		systemLabel.setBorder(loweredbevel);
		systemLabel.updateUI();
		pauseButton.addActionListener(new ButtonListener());
		// Give the volume progress bar a border //
		volumeBar.setBorder(loweredbevel);
		// Ensure the elements of the status bar are displayed from the left
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(pauseButton,BorderLayout.CENTER);
		this.add(volumeBar,BorderLayout.CENTER);
		this.add(syncLabel,BorderLayout.CENTER);
		this.add(logMode,BorderLayout.CENTER);
		this.add(colourCodeLabel,BorderLayout.CENTER);
		this.add(ch1Label,BorderLayout.CENTER);
		this.add(ch2Label,BorderLayout.CENTER);
		this.add(systemLabel,BorderLayout.CENTER);
	}
	
	// Sets the logging label text
	public void setLoggingStatus(String text) {
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		logMode.setText(text);
	}

	// Sets the sync mode label
	public void setSyncLabel (boolean syn)	{
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		// Have sync
		if (syn==true)	{
			syncLabel.setText("SYNC");
			syncLabel.setForeground(Color.GREEN);
		}
		else	{
			syncLabel.setText("NO SYNC");
			syncLabel.setForeground(Color.RED);
		}
	}
	
	// Set the volume bar display
	public void setVolumeBar(int val) {
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		if (val<40){
			volumeBar.setForeground(Color.yellow);
		}else if((val>40)&&(val<70)){
			volumeBar.setForeground(Color.green);
		}else {
			volumeBar.setForeground(Color.red);
			//greater 100 reset vol bar to 100
			val=100;
		}
		// Set the class value //
		volumeBar.setValue(val);
	}
	
	public void setCh1Label (String label,Color c)	{
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		label="Ch 1 : "+label;
		ch1Label.setText(label);
		ch1Label.setForeground(c);
	}
	
	public void setCh2Label (String label,Color c)	{
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		label="Ch 2 : "+label;
		ch2Label.setText(label);
		ch2Label.setForeground(c);
	}
	
	public void setColourCodeLabel (int cc,Color col)	{
		if (TtheApp!=null)	{
			if (TtheApp.isPauseScreen()==true) return;
		}
		String label;
		if (cc==-1) label="Colour Code : Unknown";
		else label="Colour Code : "+Integer.toString(cc);
		colourCodeLabel.setText(label);
		colourCodeLabel.setForeground(col);
	}
	
	public void setSystemLabel (String text,Color col)	{
		systemLabel.setText(text);
		systemLabel.setForeground(col);
	}
	
	public void setApp (DMRDecode theApp)	{
		TtheApp=theApp;
	}

	// This class listens for button events
	class ButtonListener implements ActionListener {
		  ButtonListener() {
		  }

		  public void actionPerformed(ActionEvent e) {
			// The user wants to pause the display
			if (e.getActionCommand().equals("Pause")) {
		    	pauseButton.setText("Restart");
		    	if (TtheApp!=null) TtheApp.setPauseScreen(true);
		    }
		    // The user wants to restart the display
		    if (e.getActionCommand().equals("Restart")) {
		    	pauseButton.setText("Pause");
			    if (TtheApp!=null) TtheApp.setPauseScreen(false);
			    }
		  }
		}
	
}


