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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;

public class DisplayFrame extends JFrame implements ActionListener {
	private JMenuBar menuBar=new JMenuBar();
	private DMRDecode theApp;
	public static final long serialVersionUID=1;
	private JMenuItem save_to_file,inverted_item,debug_item,capture_item;
	private JMenuItem view_voice_frames,view_data_frames,view_embedded_frames,error_rate;
	private JMenuItem exit_item,about_item,help_item,view_display_bar;
	private JStatusBar statusBar=new JStatusBar();
	private DisplayBar displayBar=new DisplayBar();

	// Constructor
	public DisplayFrame(String title,DMRDecode theApp) {
		setTitle(title);
		this.theApp=theApp;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		// Menu setup
		setJMenuBar(menuBar);
		// Main
		JMenu mainMenu=new JMenu("Main");
		mainMenu.add(capture_item=new JRadioButtonMenuItem("Capture",theApp.isCapture()));
		capture_item.addActionListener(this);
		mainMenu.add(debug_item=new JRadioButtonMenuItem("Debug Mode",theApp.isDebug()));
		debug_item.addActionListener(this);
		mainMenu.add(inverted_item=new JRadioButtonMenuItem("Invert Signal",theApp.inverted));
		inverted_item.addActionListener(this);
		mainMenu.add(save_to_file=new JRadioButtonMenuItem("Save to File",theApp.saveToFile));
		save_to_file.addActionListener(this);
		mainMenu.add(exit_item=new JMenuItem("Exit"));		
		exit_item.addActionListener(this);
		menuBar.add(mainMenu);
		// Info
		JMenu infoMenu=new JMenu("Info");
		infoMenu.add(view_display_bar=new JRadioButtonMenuItem("Enable Symbol Display",theApp.isEnableDisplayBar()));
		view_display_bar.addActionListener(this);
		infoMenu.add(error_rate=new JMenuItem("Error Check Info"));		
		error_rate.addActionListener(this);
		menuBar.add(infoMenu);
		// View
		JMenu viewMenu=new JMenu("View");
		viewMenu.add(view_data_frames=new JRadioButtonMenuItem("View Data Frames",theApp.isViewDataFrames()));
		view_data_frames.addActionListener(this);		
		viewMenu.add(view_embedded_frames=new JRadioButtonMenuItem("View Embedded Frames",theApp.isViewEmbeddedFrames()));
		view_embedded_frames.addActionListener(this);		
		viewMenu.add(view_voice_frames=new JRadioButtonMenuItem("View Voice Frames",theApp.isViewVoiceFrames()));
		view_voice_frames.addActionListener(this);
		menuBar.add(viewMenu);
		// Help
		JMenu helpMenu=new JMenu("Help");
		helpMenu.add(about_item=new JMenuItem("About"));		
		about_item.addActionListener(this);
		helpMenu.add(help_item=new JMenuItem("Help"));		
		help_item.addActionListener(this);		
		menuBar.add(helpMenu);
		// Setup the status bar
		getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
		statusBar.setLoggingStatus("Not Logging");
		// Setup the display bar
		getContentPane().add(displayBar, java.awt.BorderLayout.WEST);
		}

	// Handle messages from the scrollbars
	class MyAdjustmentListener implements AdjustmentListener  {
		public void adjustmentValueChanged(AdjustmentEvent e) {

		}
	 }

	// Handle all menu events
	public void actionPerformed (ActionEvent event) {
		String event_name=event.getActionCommand();
		
		// About
		if (event_name=="About")	{
			String line=theApp.program_version+"\r\n"+"by Ian Wraith (ianwraith@gmail.com)\r\nwith code taken from the DSD program.";
			JOptionPane.showMessageDialog(null,line,"DMRDecode", JOptionPane.INFORMATION_MESSAGE);
		}
		
		// Capture
		if (event_name=="Capture")	{
			if (theApp.isCapture()==false) theApp.setCapture(true);
			else theApp.setCapture(false);
		}
		
		// Debug Mode
		if (event_name=="Debug Mode")	{
			if (theApp.isDebug()==false) theApp.setDebug(true);
			 else theApp.setDebug(false);
		}		

		// Invert signal
		if (event_name=="Invert Signal")	{
			if (theApp.inverted==false) theApp.inverted=true;
			 else theApp.inverted=false;
		}
		
		// Save to File
		if (event_name=="Save to File")	{		
			if (theApp.saveToFile==false)	{
				if (saveDialogBox()==false)	{
					// Restart the audio in thread
					theApp.lineInThread.startAudio();
					return;
				}
				theApp.saveToFile=true;
				statusBar.setLoggingStatus("Logging");
			}
			 else	{
				 closeLogFile();
			 }
			// Restart the audio in thread
			theApp.lineInThread.startAudio();
		}	
		
		// View data frames
		if (event_name=="View Data Frames")	{
			boolean cstate=theApp.isViewDataFrames();
			if (cstate==true) cstate=false;
			else cstate=true;
			theApp.setViewDataFrames(cstate);
		}
		
		// View embedded frames
		if (event_name=="View Embedded Frames")	{
			boolean cstate=theApp.isViewEmbeddedFrames();
			if (cstate==true) cstate=false;
			else cstate=true;
			theApp.setViewEmbeddedFrames(cstate);
		}
		
		// View voice frames
		if (event_name=="View Voice Frames")	{
			boolean cstate=theApp.isViewVoiceFrames();
			if (cstate==true) cstate=false;
			else cstate=true;
			theApp.setViewVoiceFrames(cstate);
		}
		
		// Error rate info
		if (event_name=="Error Check Info")	{
			errorDialogBox();
		}
		
		// Enable/Disable the symbol display
		if (event_name=="Enable Symbol Display")	{
			boolean estate=theApp.isEnableDisplayBar();
			if (estate==true) estate=false;
			else estate=true;
			theApp.setEnableDisplayBar(estate);
		}
		
		// Exit 
		if (event_name=="Exit") {
			// If logging close the file
			if (theApp.saveToFile==true) closeLogFile();
			// Close the audio down //
			theApp.lineInThread.shutDownAudio();
			// Stop the program //
			System.exit(0);	
		}
		
		// Help
		if (event_name=="Help") {
			BareBonesBrowserLaunch.openURL("https://github.com/IanWraith/DMRDecode/wiki");
		}
		
		menuItemUpdate();
	}

	// Update all the menu items 
	public void menuItemUpdate () {
		inverted_item.setSelected(theApp.inverted);
		debug_item.setSelected(theApp.isDebug());
	}
	
	// Display a dialog box so the user can select a location and name for a log file
	public boolean saveDialogBox ()	{
		if (theApp.logging==true) return false;
		String file_name;
		// Bring up a dialog box that allows the user to select the name
		// of the saved file
		JFileChooser fc=new JFileChooser();
		// The dialog box title //
		fc.setDialogTitle("Select the log file name");
		// Start in current directory
		fc.setCurrentDirectory(new File("."));
		// Don't all types of file to be selected //
		fc.setAcceptAllFileFilterUsed(false);
		// Only show .txt files //
		fc.setFileFilter(new TextfileFilter());
		// Show save dialog; this method does not return until the
		// dialog is closed
		int returnval=fc.showSaveDialog(this);
		// If the user has selected cancel then quit
		if (returnval==JFileChooser.CANCEL_OPTION) return false;
		// Get the file name an path of the selected file
		file_name=fc.getSelectedFile().getPath();
		// Does the file name end in .html ? //
		// If not then automatically add a .html ending //
		int last_index=file_name.lastIndexOf(".html");
		if (last_index!=(file_name.length()-5))
			file_name=file_name + ".html";
		// Create a file with this name //
		File tfile = new File(file_name);
		// If the file exists ask the user if they want to overwrite it
		if (tfile.exists()) {
			int response = JOptionPane.showConfirmDialog(null,
					"Overwrite existing file?", "Confirm Overwrite",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.CANCEL_OPTION) return false;
		}
		// Open the file
		try {
			theApp.file=new FileWriter(tfile);
			// Clear all logged info
			theApp.usersLogged.clearAll();
			// Write the program version as the first line of the log
			String fline="<HTML>"+theApp.program_version+"<br>\r\n";
			theApp.file.write(fline);
			
		} catch (Exception e) {
			System.out.println("\nError opening the logging file");
			return false;
		}
		theApp.logging=true;
		return true;
	}
	
	// Close the log file
	public void closeLogFile()	{
		 int a,count;
		 String line;
		 theApp.saveToFile=false;
		 statusBar.setLoggingStatus("Not Logging");
		 try	{
			 // Display users
			 count=theApp.usersLogged.returnUserCounter();
			 // No users
			 if (count==0)	{
				 theApp.file.write("<br><br><b>No users were logged");
			 }
			 else	{
				 line="<br><br><b>The folowing "+Integer.toString(count)+" users were logged ..</b>";
				 theApp.file.write(line);
				 // Sort the users
				 theApp.usersLogged.sortByIdent();
				 // Run through each user
				 for (a=0;a<count;a++)	{
					 line="<br>"+theApp.usersLogged.returnInfo(a);
					 theApp.file.write(line);
				 }
			 }
			 // Close the file
			 theApp.file.write("</HTML>");
			 theApp.file.flush();
			 theApp.file.close();
		 }
		 catch (Exception e)	{
			 JOptionPane.showMessageDialog(null,"Error closing Log file","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
		 }
	}
	
	// Display the percentage of bad frames received
	public void errorDialogBox()	{
		String line;
		if (theApp.frameCount==0)	{
			line="No frames received yet !";
		}
		else	{
			DecimalFormat df=new DecimalFormat("#.#");
			double err=((double)theApp.badFrameCount/(double)theApp.frameCount)*100.0;
			line=df.format(err)+"% of frames were bad.";
		}
		JOptionPane.showMessageDialog(null,line,"DMRDecode", JOptionPane.INFORMATION_MESSAGE);
	}
	
	// Set the volume indicating progress bar //
	public void updateVolumeBar(int val) {
		// Calculate as a percentage of 18000 (the max value)
		int pval=(int)(((float)val/(float)18000.0)*(float)100);
		statusBar.setVolumeBar(pval);
	}
	
	// Update the sync label
	public void updateSyncLabel (boolean sync)	{
		statusBar.setSyncLabel(sync);
	}
	
	// Pass a symbol to the display bar symbol buffer
	public void displaySymbol (int tsymb)	{
		displayBar.addToBuffer(tsymb);
	}
	
	// Set the display bar parameters
	public void displayBarParams (int tmax,int tmin,int tumid,int tlmid)	{
		displayBar.setDisplayBarParams(tmax,tmin,tumid,tlmid);
	}
	
	// Stop the display bar 
	public void stopDisplayBar()	{
		displayBar.stopDisplay();
	}
	
	// Enable or disable the display bar
	public void switchDisplayBar (boolean st)	{
		displayBar.setEnableDisplay(st);
	}
	
}