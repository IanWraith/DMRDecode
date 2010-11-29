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
	private JMenuItem save_to_file,inverted_item,debug_item;
	private JMenuItem view_voice_frames,view_data_frames,view_embedded_frames,error_rate;
	private JMenuItem exit_item,about_item,help_item;
	private JStatusBar status_bar=new JStatusBar();

	// Constructor
	public DisplayFrame(String title,DMRDecode theApp) {
		setTitle(title);
		this.theApp=theApp;
		status_bar.setTheApp(this.theApp);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		// Menu setup
		setJMenuBar(menuBar);
		// Main
		JMenu mainMenu=new JMenu("Main");
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
		getContentPane().add(status_bar, java.awt.BorderLayout.SOUTH);
		status_bar.setLoggingStatus("Not Logging");
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
			String line=theApp.program_version+"\r\n"+"by Ian Wraith (iwraith@gmail.com)\r\nwith code taken from the DSD program.";
			JOptionPane.showMessageDialog(null,line,"DMRDecode", JOptionPane.INFORMATION_MESSAGE);
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
				status_bar.setLoggingStatus("Logging");
			}
			 else	{
				 theApp.saveToFile=false;
				 status_bar.setLoggingStatus("Not Logging");
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
		
		// Exit 
		if (event_name=="Exit") {
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
		// Does the file name end in .txt ? //
		// If not then automatically add a .txt ending //
		int last_index=file_name.lastIndexOf(".txt");
		if (last_index!=(file_name.length() - 4))
			file_name=file_name + ".txt";
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
			// Write the program version as the first line of the log
			String fline=theApp.program_version+"\r\n";
			theApp.file.write(fline);
			
		} catch (Exception e) {
			System.out.println("\nError opening the logging file");
			return false;
		}
		theApp.logging=true;
		return true;
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
		// Divide by 2500 to get a value between 0 and 10
		int pval=val/2500;
		status_bar.setVolumeBar(pval);
	}
	
}