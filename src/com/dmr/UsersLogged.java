package com.dmr;

public class UsersLogged {
	private static final int MAX=8192;
	private int userCounter=0;
	private int ident[]=new int[MAX];
	private boolean group[]=new boolean[MAX];
	private boolean dataUser[]=new boolean[MAX];
	private boolean groupCallUser[]=new boolean[MAX];
	private boolean unitCallUser[]=new boolean[MAX];
	private boolean usedChannel1[]=new boolean[MAX];
	private boolean usedChannel2[]=new boolean[MAX];
	
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
		usedChannel1[userCounter]=false;
		usedChannel2[userCounter]=false;
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
	
	// Sets an ident as being a group call user
	public void setAsGroupUser (int tident)		{
		groupCallUser[tident]=true;
	}
	
	// Sets an ident as being a unit to unit caller
	public void setAsUnitUser (int tident)		{
		unitCallUser[tident]=true;
	}
	
	// Shows how many users have been logged
	public int returnUserCounter ()	{
		return userCounter;
	}
	
	// Records which channels as radio has used
	public void setChannel (int tident,int channel)	{
		if (channel==1) usedChannel1[tident]=true;
		else usedChannel2[tident]=true;
	}
	
	// Sort the users by mobile ident //
	// This code is standard bubble sort taken from the book
	// "Learning to Program in C" by N.Kantaris //
	public void sortByIdent() {
		int i,j,temp,max;
		boolean flag,btemp;
		max=userCounter;
		for (i=0;i<userCounter-1;i++) {
			max--;
			flag=false;
			for (j=0;j<max;j++)
				if (ident[j]>ident[j+1]) {
					// Ident //
					temp=ident[j];
					ident[j]=ident[j+1];
					ident[j+1]=temp;
					// Group/User
					btemp=group[j];
					group[j]=group[j+1];
					group[j+1]=btemp;
					// Data User
					btemp=dataUser[j];
					dataUser[j]=dataUser[j+1];
					dataUser[j+1]=btemp;
					// Group Call User
					btemp=groupCallUser[j];
					groupCallUser[j]=groupCallUser[j+1];
					groupCallUser[j+1]=btemp;
					// Unit Call User
					btemp=unitCallUser[j];
					unitCallUser[j]=unitCallUser[j+1];
					unitCallUser[j+1]=btemp;
					// Used Channel 1
					btemp=usedChannel1[j];
					usedChannel1[j]=usedChannel1[j+1];
					usedChannel1[j+1]=btemp;
					// Used Channel 2
					btemp=usedChannel2[j];
					usedChannel2[j]=usedChannel2[j+1];
					usedChannel2[j+1]=btemp;
					flag=true;
				}
			if (flag==false) break;
		}
	}
	
	// Return a formatted info line
	public String returnInfo (int index)	{
		int items=0;
		String l=Integer.toString(ident[index]);
		if (group[index]==true) l=l+" GROUP";
		if (dataUser[index]==true)	{
			items++;
			l=l+" Data ";
		}
		if (groupCallUser[index]==true)	{
			if (items>0) l=l+"+";
			items++;
			l=l+" Group Calls ";
		}
		if (unitCallUser[index]==true)	{
			if (items>0) l=l+"+";
			items++;
			l=l+" Unit to Unit Calls ";
		}
		if (items==1) l=l+"only";
		// Channels
		if ((usedChannel1[index]==true)&&(usedChannel2[index]==false))	l=l+" (Only used channel 1)";
		else if ((usedChannel2[index]==true)&&(usedChannel1[index]==false))	l=l+" (Only used channel 2)";
		else if ((usedChannel1[index]==true)&&(usedChannel2[index]==true))	l=l+" (Used both channels)";
		// All done
		return l;
	}
	
	// Clear all stored records
	public void clearAll()	{
		userCounter=0;
	}
	
}
