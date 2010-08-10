import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class DisplayFrame extends JFrame implements ActionListener {
	private JMenuBar menuBar=new JMenuBar();
	private DMRDecode theApp;
	public static final long serialVersionUID=1;
	public JScrollBar vscrollbar=new JScrollBar(JScrollBar.VERTICAL,0,1,0,500);
	public JScrollBar hscrollbar=new JScrollBar(JScrollBar.HORIZONTAL,0,1,0,1000);
	
	private JMenuItem exit_item;

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
		mainMenu.add(exit_item=new JMenuItem("Exit"));
		exit_item.addActionListener(this);
		menuBar.add(mainMenu);
		// View
		JMenu viewMenu=new JMenu("View");
		menuBar.add(viewMenu);
		// Add the vertical scrollbar
		add(vscrollbar,BorderLayout.EAST);
		// Add a listener for this
		vscrollbar.addAdjustmentListener(new MyAdjustmentListener());
		// Add the horizontal scrollbar
		add(hscrollbar,BorderLayout.SOUTH);
		// Add a listener for this
		hscrollbar.addAdjustmentListener(new MyAdjustmentListener());
		}

	// Handle messages from the scrollbars
	class MyAdjustmentListener implements AdjustmentListener  {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			// Vertical scrollbar
			if (e.getSource()==vscrollbar) {
				theApp.vertical_scrollbar_value=e.getValue();
				repaint();   
			}
			// Horizontal scrollbar
			if (e.getSource()==hscrollbar) {
				theApp.horizontal_scrollbar_value=e.getValue();
				repaint();   
			}	 
		}
	 }

	// Handle all menu events
	public void actionPerformed (ActionEvent event) {
		String event_name=event.getActionCommand();

		// Exit 
		if (event_name=="Exit") {
			// Close the audio down //
			//theApp.Line.close();
			// Stop the program //
			System.exit(0);	
		}
	}

	// Update all the menu items 
	public void menu_item_update () {
	}

}