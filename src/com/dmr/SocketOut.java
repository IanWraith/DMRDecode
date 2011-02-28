package com.dmr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketOut extends Thread {
	private boolean run;
	private boolean connected;
	private static int PORT=17887;
	private ServerSocket serversocket;
	private Socket clientSocket=null;
	private PrintWriter socketOut;
	private BufferedReader socketIn;
	
	public SocketOut (DMRDecode theApp) {
    	run=false;
		connected=false;
    	setPriority(Thread.MIN_PRIORITY);
        start();
        Thread.yield();
      }
	
	// Main
    public void run()	{
    	// Run continously
    	for (;;)	{
    		if (connected==false) waitForConnection();
    		
     	}
    }
    
    // Setup a listening TCP/IP socket
    public boolean setupSocket()	{
    	try	{
    		serversocket=new ServerSocket(PORT);
    	} catch (Exception e)	{
    		connected=false;
    		return false;
    	}
    	connected=true;
    	return true;
    }
    
    private void waitForConnection()	{
    	try	{
    		clientSocket=serversocket.accept();
    		socketOut=new PrintWriter(clientSocket.getOutputStream(),true);
            socketIn=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    	} catch (Exception e)	{
    		connected=false;
    		return;
    	}
    	connected=true;
    }
    

}
