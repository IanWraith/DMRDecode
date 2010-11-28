package com.dmr;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class JStatusBar extends JPanel {
	public static final long serialVersionUID = 1;
	private DMRDecode theApp;
	private JLabel logMode=new JLabel();
	private JProgressBar volumeBar=new JProgressBar(0, 10);
	private Border loweredbevel=BorderFactory.createLoweredBevelBorder();
	
	public JStatusBar() {
		logMode.setHorizontalAlignment(SwingConstants.LEFT);
		logMode.updateUI();
		// Give the volume progress bar a border //
		volumeBar.setBorder(loweredbevel);
		
		// Ensure the elements of the status bar are displayed from the left
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(volumeBar,BorderLayout.CENTER);
		this.add(logMode,BorderLayout.CENTER);
	}
	
	public void setTheApp(DMRDecode x){
		this.theApp=x;
	}
	
	public void setLoggingStatus(String text) {
		logMode.setText(text);
	}

	public void setVolumeBar(int val) {
		if (val<2){
			//0<->2 so set to YELLOW
			volumeBar.setForeground(Color.yellow);
		}else if((val>2)&&(val<7)){
			//2<->7 so set to GREEN
			volumeBar.setForeground(Color.green);
		}else if ((val>7)&&(val<10)){
			//7<->10 so set to RED
			volumeBar.setForeground(Color.red);
		}else{
			//greater 10 reset vol bar to 10
			val=10;
		}
		// Set the class value //
		volumeBar.setValue(val);
	}
	

}
