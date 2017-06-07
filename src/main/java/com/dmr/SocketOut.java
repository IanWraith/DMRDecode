package com.dmr;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketOut implements Runnable {
	private boolean ready;
	private static int PORT=17887;
	private static int MAXCONNS=10;
	private ServerSocket serversocket;
	private Socket socket[]=new Socket[MAXCONNS];
	private boolean socketStatus[]=new boolean[MAXCONNS];
	private PrintWriter socketPrintWriter[]=new PrintWriter[MAXCONNS];

	public SocketOut (DMRDecode theApp) {
    	ready=false;
      }
	
	// Main
    public void run()	{
    	int next;
    	// Run continously
    	for (;;)	{
    		// Wait for a socket connection if the listening socket has been setup
    		// and there is a free socket available
            if(ready == false){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException ie){
                }
                continue;
            }
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
    		// Wait for the connection
    		socket[n]=serversocket.accept();
    		// Assign a PrintWriter to this socket
    		socketPrintWriter[n]=new PrintWriter(new OutputStreamWriter(socket[n].getOutputStream(),"8859_1"));
    		// Send "OK" to the connected client
    		socketPrintWriter[n].println("OK");
    		socketPrintWriter[n].flush();
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
    
    // Send voice data to connected clients
    public void sendVoiceViaSocket (int vdata[],int channel)	{
    	int a,b;
    	for (a=0;a<vdata.length;a++)	{
    		// Run through all the possible sockets
    		for (b=0;b<MAXCONNS;b++)	{
    			try	{
    				if (socketStatus[b]==true)	{
    					if (a==0)	{
    						// Send a # to show the start of the voice frame
    						socketPrintWriter[b].println("#");
    						// Send the channel number at the start of the frame
    						socketPrintWriter[b].println(channel);
    					}
    					socketPrintWriter[b].println(vdata[a]);
    					// If this is the last int of the voice frame flush the stream
    					// this way there is no delay in sending the data
    					if (a==(vdata.length-1)) socketPrintWriter[b].flush();
    				}
    			} catch (Exception e)	{
    				// If we have a problem this socket can't be valid anymore
    				socketStatus[b]=false;
    			}
    		}
    	}
    }
    

}
