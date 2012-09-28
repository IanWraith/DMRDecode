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
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;

public class DisplayFrame extends JFrame implements ActionListener {
	private JMenuBar menuBar=new JMenuBar();
	private DMRDecode theApp;
	public static final long serialVersionUID=1;
	private JMenuItem save_to_file,inverted_item,debug_item,capture_item,quick_log,save_settings;
	private JMenuItem error_rate,exit_item,about_item,help_item,view_display_bar;
	private JMenuItem view_cach,view_idle,view_onlygood,view_voice;
	private JMenuItem clear_screen,copy_screen,twitter_item;
	private JStatusBar statusBar=new JStatusBar();
	private DisplayBar displayBar=new DisplayBar();
	public JScrollBar vscrollbar=new JScrollBar(JScrollBar.VERTICAL,0,1,0,2000);

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
		mainMenu.add(quick_log=new JRadioButtonMenuItem("Quick Log",theApp.isQuickLog()));
		quick_log.addActionListener(this);
		mainMenu.add(save_settings=new JMenuItem("Save Settings"));
		save_settings.addActionListener(this);
		mainMenu.add(save_to_file=new JRadioButtonMenuItem("Save to File",theApp.getLogging()));
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
		viewMenu.add(copy_screen=new JMenuItem("Copy All to the Clipboard"));
		copy_screen.addActionListener(this);
		viewMenu.add(clear_screen=new JMenuItem("Clear Screen"));
		clear_screen.addActionListener(this);
		viewMenu.addSeparator();
		viewMenu.add(view_cach=new JRadioButtonMenuItem("Display CACH",theApp.isDisplayCACH()));
		view_cach.addActionListener(this);
		viewMenu.add(view_onlygood=new JRadioButtonMenuItem("Display Good Frames Only",theApp.isDisplayOnlyGoodFrames()));
		view_onlygood.addActionListener(this);
		viewMenu.add(view_idle=new JRadioButtonMenuItem("Display Idle PDU",theApp.isDisplayIdlePDU()));
		view_idle.addActionListener(this);
		viewMenu.add(view_voice=new JRadioButtonMenuItem("Display Voice Frames",theApp.isDisplayVoiceFrames()));
		view_voice.addActionListener(this);
		menuBar.add(viewMenu);
		// Help
		JMenu helpMenu=new JMenu("Help");
		helpMenu.add(about_item=new JMenuItem("About"));		
		about_item.addActionListener(this);
		helpMenu.add(twitter_item=new JMenuItem("Follow DMRDecode Progress on Twitter"));		
		twitter_item.addActionListener(this);
		helpMenu.add(help_item=new JMenuItem("Help"));		
		help_item.addActionListener(this);		
		menuBar.add(helpMenu);
		// Setup the status bar
		getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
		statusBar.setLoggingStatus("Not Logging");
		statusBar.setApp(theApp);
		// Setup the display bar
		getContentPane().add(displayBar, java.awt.BorderLayout.WEST);
		// Add the vertical scrollbar
		add(vscrollbar,BorderLayout.EAST);
		// Add a listener for this
		vscrollbar.addAdjustmentListener(new MyAdjustmentListener());
		// Read in the default settings file
		try	{
			theApp.readDefaultSettings();
			// Update the menus
			menuItemUpdate();
		}
		catch (Exception e)	{
			String err=e.toString();
			// Can't find the default settings file //
			System.out.println("\nInformative : Unable to read the file DMRDecode_settings.xml "+err);
		}
			
		}

	
	// Handle messages from the scrollbars
	class MyAdjustmentListener implements AdjustmentListener  {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			// Vertical scrollbar
			if (e.getSource()==vscrollbar) {
				theApp.vertical_scrollbar_value=e.getValue();
				repaint();   
			}
			
		}
	 }


	// Handle all menu events
	public void actionPerformed (ActionEvent event) {
		String event_name=event.getActionCommand();
		
		// About
		if (event_name=="About")	{
			String line=theApp.program_version+"\r\n"+"Ian Wraith (ianwraith@gmail.com)\r\nalso big thanks to Chris Sams for the CPU utilization fix\r\nand with code taken from the DSD program.";
			JOptionPane.showMessageDialog(null,line,"DMRDecode", JOptionPane.INFORMATION_MESSAGE);
		}
		
		// Capture
		if (event_name=="Capture")	{
			if (theApp.isCapture()==false) theApp.setCapture(true);
			else theApp.setCapture(false);
		}
		
		// Clear Screen
		if (event_name=="Clear Screen") theApp.clearScreen();
		
		// Copy to the clip board
		if (event_name=="Copy All to the Clipboard") setClipboard(theApp.getAllText());
		
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
		
		// Quick Log
		if (event_name=="Quick Log")	{
			if (theApp.isQuickLog()==false)	{
				quickLogDialogBox();
			}
			else {
				closeQuickLogFile();
			}
		}
		
		// Save to File
		if (event_name=="Save to File")	{		
			if (theApp.getLogging()==false)	{
				if (saveDialogBox()==false)	{
					// Restart the audio in thread
					theApp.lineInThread.startAudio();
					menuItemUpdate();
					return;
				}
				theApp.setLogging(true);
				statusBar.setLoggingStatus("Logging");
			}
			 else	{
				 closeLogFile();
			 }
			// Restart the audio in thread
			theApp.lineInThread.startAudio();
		}	
		
		// Save the current settings
		if (event_name=="Save Settings") theApp.saveCurrentSettings();
		
		// Twitter
		if (event_name=="Follow DMRDecode Progress on Twitter")	{
			BareBonesBrowserLaunch.openURL("https://twitter.com/#!/IanWraith");
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
			if (theApp.getLogging()==true) closeLogFile();
			// Close the audio down //
			theApp.lineInThread.shutDownAudio();
			// Stop the program //
			System.exit(0);	
		}
		
		// Help
		if (event_name=="Help") {
			BareBonesBrowserLaunch.openURL("https://github.com/IanWraith/DMRDecode/wiki");
		}
		
		// Display CACH
		if (event_name=="Display CACH")	{
			if (theApp.isDisplayCACH()==true) theApp.setDisplayCACH(false);
			else theApp.setDisplayCACH(true);
			// If logging update the log of this change in filter settings
			if (theApp.getLogging()==true)	{
				if (theApp.isDisplayCACH()==false) theApp.fileWrite("Filter settings changed so CACH data isn't displayed");
				else theApp.fileWrite("Filter settings changed so that CACH data is displayed");
			}
		}
		
		// Display Idle PDU
		if (event_name=="Display Idle PDU")	{
			if (theApp.isDisplayIdlePDU()==true) theApp.setDisplayIdlePDU(false);
			else theApp.setDisplayIdlePDU(true);
			// If logging update the log of this change in filter settings
			if (theApp.getLogging()==true)	{
				if (theApp.isDisplayIdlePDU()==false) theApp.fileWrite("Filter settings changed so Idle PDUs aren't displayed");
				else theApp.fileWrite("Filter settings changed so that Idle PDUs are displayed");
			}
		}
		
		// Display only good frames
		if (event_name=="Display Good Frames Only")	{
			if (theApp.isDisplayOnlyGoodFrames()==true) theApp.setDisplayOnlyGoodFrames(false);
			else theApp.setDisplayOnlyGoodFrames(true);
			// If logging update the log of this change in filter settings
			if (theApp.getLogging()==true)	{
				if (theApp.isDisplayOnlyGoodFrames()==false) theApp.fileWrite("Filter settings changed so that frames with errors are displayed");
				else theApp.fileWrite("Filter settings changed so that only frames without errors are displayed");
			}

		}
		
		// Display Voice Frames
		if (event_name=="Display Voice Frames")	{
			if (theApp.isDisplayVoiceFrames()==true) theApp.setDisplayVoiceFrames(false) ;
			else theApp.setDisplayVoiceFrames(true);
			// If logging update the log of this change in filter settings
			if (theApp.getLogging()==true)	{
				if (theApp.isDisplayVoiceFrames()==false) theApp.fileWrite("Filter settings changed so voice frames aren't displayed");
				else theApp.fileWrite("Filter settings changed so that voice frames are displayed");
			}

		}

		menuItemUpdate();
	}

	// Update all the menu items 
	public void menuItemUpdate () {
		inverted_item.setSelected(theApp.inverted);
		debug_item.setSelected(theApp.isDebug());
		save_to_file.setSelected(theApp.getLogging());
		quick_log.setSelected(theApp.isQuickLog());
		view_cach.setSelected(theApp.isDisplayCACH());
		view_idle.setSelected(theApp.isDisplayIdlePDU());
		view_voice.setSelected(theApp.isDisplayVoiceFrames());
		view_onlygood.setSelected(theApp.isDisplayOnlyGoodFrames());
		view_display_bar.setSelected(theApp.isEnableDisplayBar());
		capture_item.setSelected(theApp.isCapture());
	}
	
	// Display a dialog box so the user can select a location and name for a log file
	public boolean saveDialogBox ()	{
		if (theApp.getLogging()==true) return false;
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
		// Does the file name end in .txt ? //
		// If not then automatically add a .txt ending //
		int last_index=file_name.lastIndexOf(".txt");
		if (last_index!=(file_name.length()-4)) file_name=file_name + ".txt";
		// Create a file with this name //
		File tfile=new File(file_name);
		// If the file exists ask the user if they want to overwrite it
		if (tfile.exists()) {
			int response = JOptionPane.showConfirmDialog(null,
					"Overwrite existing file?", "Confirm Overwrite",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (response==JOptionPane.CANCEL_OPTION) return false;
		}
		// Open the file
		try {
			theApp.file=new FileWriter(tfile);
			// Clear all logged info
			theApp.usersLogged.clearAll();
			// Write the program version as the first line of the log
			String fline=theApp.program_version+"\r\n";
			theApp.file.write(fline);
			// Display the state of the filters at the start of the log
			if (theApp.isDisplayCACH()==false) theApp.file.write("You have selected not to display CACH data\r\n");
			if (theApp.isDisplayOnlyGoodFrames()==true) theApp.file.write("You have selected only to display frames without errors\r\n");
			if (theApp.isDisplayIdlePDU()==false) theApp.file.write("You have selected not to display Idle PDUs\r\n");
			if (theApp.isDisplayVoiceFrames()==false) theApp.file.write("You have selected not to display voice frames\r\n");
			
		} catch (Exception e) {
			System.out.println("\nError opening the logging file");
			return false;
		}
		theApp.setLogging(true);
		return true;
	}
	
	// Close the log file
	public void closeLogFile()	{
		 int a,count;
		 String line;
		 theApp.setLogging(false);
		 statusBar.setLoggingStatus("Not Logging");
		 try	{
			 // Display users
			 count=theApp.usersLogged.returnUserCounter();
			 // No users
			 if (count==0)	{
				 theApp.file.write("\r\n\r\nNo users were logged");
			 }
			 else	{
				 line="\r\n\r\nThe following "+Integer.toString(count)+" users were logged ..";
				 theApp.file.write(line);
				 // Sort the users
				 theApp.usersLogged.sortByIdent();
				 // Run through each user
				 for (a=0;a<count;a++)	{
					 line="\r\n"+theApp.usersLogged.returnInfo(a);
					 theApp.file.write(line);
				 }
			 }
			 // Close the file
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
	
	// Display a dialog box so the user can select a location and name for a quick log file
	public boolean quickLogDialogBox ()	{
		if (theApp.isQuickLog()==true) return false;
		String file_name;
		// Bring up a dialog box that allows the user to select the name
		// of the saved file
		JFileChooser fc=new JFileChooser();
		// The dialog box title //
		fc.setDialogTitle("Select the quick log file name");
		// Start in current directory
		fc.setCurrentDirectory(new File("."));
		// Don't all types of file to be selected //
		fc.setAcceptAllFileFilterUsed(false);
		// Only show .csv files //
		fc.setFileFilter(new CSVFileFilter());
		// Show save dialog; this method does not return until the
		// dialog is closed
		int returnval=fc.showSaveDialog(this);
		// If the user has selected cancel then quit
		if (returnval==JFileChooser.CANCEL_OPTION) return false;
		// Get the file name an path of the selected file
		file_name=fc.getSelectedFile().getPath();
		// Does the file name end in .csv ? //
		// If not then automatically add a .csv ending //
		int last_index=file_name.lastIndexOf(".csv");
		if (last_index!=(file_name.length()-4)) file_name=file_name + ".csv";
		// Create a file with this name //
		File tfile=new File(file_name);
		// If the file exists ask the user if they want to overwrite it
		if (tfile.exists()) {
			int response = JOptionPane.showConfirmDialog(null,
					"Overwrite existing file?", "Confirm Overwrite",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (response==JOptionPane.CANCEL_OPTION) return false;
		}
		// Open the file
		try {
			theApp.quickLogFile=new FileWriter(tfile);
			
		} catch (Exception e) {
			System.out.println("\nError opening the quick log file");
			return false;
		}
		theApp.setQuickLog(true);
		return true;
	}
	
	public void closeQuickLogFile()	{
		try	{
			// Close the file
			 theApp.quickLogFile.flush();
			 theApp.quickLogFile.close();
		} catch (Exception e)	{
			JOptionPane.showMessageDialog(null,"Error closing Quick Log file","DMRDecode", JOptionPane.INFORMATION_MESSAGE);
		}
		theApp.setQuickLog(false);
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
	
	public void setCh1Label (String label,Color col)	{
		statusBar.setCh1Label(label,col);
	}
	
	public void setCh2Label (String label,Color col)	{
		statusBar.setCh2Label(label,col);
	}
	
	public void SetColourCodeLabel (int cc,Color col)	{
		statusBar.setColourCodeLabel(cc,col);
	}
	
	public void setSystemLabel (String txt,Color col)	{
		statusBar.setSystemLabel(txt,col);
	}
	
	// This sets the clipboard with a string passed to it
	private void setClipboard(String str) {
	    StringSelection ss=new StringSelection(str);
	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss,null);
	}
}