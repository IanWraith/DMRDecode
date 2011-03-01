package com.dmr;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketOut extends Thread {
	private boolean ready;
	private static int PORT=17887;
	private static int MAXCONNS=10;
	private ServerSocket serversocket;
	private Socket socket[]=new Socket[MAXCONNS];
	private boolean socketStatus[]=new boolean[MAXCONNS];

	public SocketOut (DMRDecode theApp) {
    	ready=false;
    	setPriority(Thread.MIN_PRIORITY);
        start();
        Thread.yield();
      }
	
	// Main
    public void run()	{
    	int next;
    	// Run continously
    	for (;;)	{
    		// Wait for a socket connection if the listening socket has been setup
    		// and there is a free socket available
    		if ((ready==true)&&(checkForFreeSockets()==true))	{
    			// Get the index of the next available socket
    			next=nextFreeSocket();
    			// Wait for a connection and when it arrives use the free socket
        		waitForConnection(next);
     		}
     	}
    }
    
    // Setup a listening TCP/IP socket
    public boolean setupSocket()	{
    	try	{
    		serversocket=new ServerSocket(PORT);
    	} catch (Exception e)	{
    		return false;
    	}
    	ready=true;
    	return true;
    }
    
    // Wait for a connection to the next free socket
    private void waitForConnection(int n)	{
    	try	{
    		socket[n]=serversocket.accept();
    	} catch (Exception e)	{
    		return;
    	}
    	socketStatus[n]=true;
    }
    
    // Return the next free socket
    private int nextFreeSocket()	{
    	int a;
    	for (a=0;a<MAXCONNS;a++)	{
    		if (socketStatus[a]==false) return a;
    	}
    	// Return -1 if no sockets are free
    	return -1;
    }
    
    // Check if there are any sockets free
    private boolean checkForFreeSockets()	{
    	int a;
    	for (a=0;a<MAXCONNS;a++)	{
    		if (socketStatus[a]==false) return true;
    	}
    	// Return false if no sockets are free
    	return false;
    }
    

}
