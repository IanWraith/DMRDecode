package com.dmr;

public class UsersLogged {
	private static final int MAX=8192;
	private int userCounter=0;
	private int ident[]=new int[MAX];
	private boolean group[]=new boolean[MAX];
	private boolean dataUser[]=new boolean[MAX];
	private boolean groupCallUser[]=new boolean[MAX];
	private boolean unitCallUser[]=new boolean[MAX];
	
	// Adds a user and returns TRUE if this has been done
	public boolean addUser (int tident)	{
		int a;
		// Check the buffer isn't full
		if (userCounter==(MAX-1)) return false;
		// Check if the user already exists
		for (a=0;a<userCounter;a++)	{
			if (ident[a]==tident) return false;
		}
		// No add them
		ident[userCounter]=tident;
		group[userCounter]=false;
		dataUser[userCounter]=false;
		groupCallUser[userCounter]=false;
		unitCallUser[userCounter]=false;
		// Increment index
		userCounter++;
		return true;
	}
	
	// Returns a users index number or -1 if not found
	public int findUserIndex (int tident)	{
		int a;
		// Check if the user already exists
		for (a=0;a<userCounter;a++)	{
			if (ident[a]==tident) return a;
		}
		// Return -1 as nothing found
		return -1;
	}
	
	// Sets an ident as being a group
	public void setAsGroup (int tident)		{
		group[tident]=true;
	}
	
	// Sets an ident as being a data user
	public void setAsDataUser (int tident)		{
		dataUser[tident]=true;
	}
	
}
