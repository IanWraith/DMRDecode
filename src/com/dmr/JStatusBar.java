package com.dmr;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

public class JStatusBar extends JPanel {
	public static final long serialVersionUID = 1;
	private JLabel logMode=new JLabel();
	private JLabel syncLabel=new JLabel();
	private JProgressBar volumeBar=new JProgressBar(0,100);
	private Border loweredbevel=BorderFactory.createLoweredBevelBorder();
	
	public JStatusBar() {
		logMode.setHorizontalAlignment(SwingConstants.LEFT);
		logMode.updateUI();
		logMode.setBorder(loweredbevel);
		syncLabel.setHorizontalAlignment(SwingConstants.LEFT);
		syncLabel.setBorder(loweredbevel);
		syncLabel.updateUI();
		// Give the volume progress bar a border //
		volumeBar.setBorder(loweredbevel);
		// Ensure the elements of the status bar are displayed from the left
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(volumeBar,BorderLayout.CENTER);
		this.add(syncLabel,BorderLayout.CENTER);
		this.add(logMode,BorderLayout.CENTER);
	}
	
	// Sets the logging label text
	public void setLoggingStatus(String text) {
		logMode.setText(text);
	}

	// Sets the sync mode label
	public void setSyncLabel (boolean syn)	{
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
	

}
