import java.rmi.*;
import java.util.*;
import java.io.*;


import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;

/**
 * Testing suite for the Chord system.
 */

public class TestSuite
{
	
	public static void main (String args[])
	{
		Console console = System.console();
		if (console == null) {
            System.err.println("No console.");
            System.exit(1);
        }
		System.out.print("Enter the command: ");
		String command = "";
		boolean debug = false;
		if(args.length!=0)
		{
			if(args[0].equals("-debug"))
			{
				debug=true;
			}
		}
		System.out.println("");
		
		while(!command.equals("9"))
		{
			help();
			command = console.readLine();
			
			
			if(command.equals("2"))
			{
				
					System.out.println("Deleting Node Command:"); 
					System.out.println("Enter an available nodeId in the chord system.");
					String nodeId = console.readLine();
					System.out.println("Deleting the node with ID "+Integer.valueOf(nodeId));

					try
					{
						ChordInterface baseNode =(ChordInterface) Naming.lookup("//127.0.0.1/BSNode");
						System.out.println("The Node responsible for this key:" + baseNode.findSuccessor(Integer.parseInt(hash)));
					}
					catch(Exception e)
					{
						System.out.println("Please initialize the Chord System and try again.");
					}
				
			}
			else if(command.equals("3"))
			{
				
					System.out.println("Download File Command:"); 
					System.out.println("Enter the Name of the file to be downloaded");
					String fileName = console.readLine();

					System.out.println("Searching the file .."+fileName);
					String hash = Hasher.getHashString(fileName,Utilities.m);
					System.out.println("The key of the file is:" + hash );

					try
					{
						ChordInterface baseNode =(ChordInterface) Naming.lookup("//127.0.0.1/BSNode");
						System.out.println("The Node responsible for this key:" + baseNode.findSuccessor(Integer.parseInt(hash)));
					}
					catch(Exception e)
					{
						System.out.println("Please initialize the Chord System and try again.");
					}

					System.out.println("Enter the id of the node where the file is to be downloaded to:");

					String fileId = console.readLine();

					System.out.println("Downloading the file "+ fileName + " on the node with ID "+Integer.valueOf(nodeId));

					try
					{
						ChordInterface downloadNode =(ChordInterface) Naming.lookup("//127.0.0.1/"+fileId);
					}
					catch(Exception e)
					{
						System.out.println("Please enter a valid Id and try again.");
					}




				
			}
			else if(command.equals("1"))
			{
				
					System.out.println("Search File Command:"); 
					System.out.println("Enter the file Name of the file to be searched for in the Chord System");
					String fileName = console.readLine();
					System.out.println("Searching the file .."+fileName);
					String hash = Hasher.getHashString(fileName,Utilities.m);
					System.out.println("The hash of the files is:" + hash );

					try
					{
						ChordInterface baseNode =(ChordInterface) Naming.lookup("//127.0.0.1/BSNode");
						System.out.println("The Node responsible for this key:" + baseNode.findSuccessor(Integer.parseInt(hash)));
					}
					catch(Exception e)
					{
						System.out.println("Please initialize the Chord System and try again.");
					}
				
			}
			else
			{
				System.out.println("command not valid. Possible commands are");
				help();
			}
		}
		
		//System.out.println(command);
	}

	private static void help() 
	{
		System.out.println("****************List of commands**************");
		System.out.println("Please ensure that your chord setup is up and running before running commands :)");
		System.out.println("Print Help command String: 0");
		System.out.println("Search for a file command String: 1");// <fileName>");
		System.out.println("Delete a node command String: 2");//<nodeId>");
		System.out.println("download a file command String: 3");// <fileName> <nodeId>");
		System.out.println("exit Test Suite command String: 9");
	}

	public findNodes()
	{
		
	}

}
