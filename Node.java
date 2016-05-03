import java.rmi.*;
import java.util.*;
import java.io.*;


import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;

/**
 * Simulate an 8 node chord structure
 */
public class Node extends UnicastRemoteObject implements ChordInterface
{

	/*Start Chord functions*/

	public void create() throws RemoteException
	{

		this.predecessorId = -1;
		this.successorId = myId;

		System.out.println("Chord Ring created");

	}
	public void join(ChordInterface anotherNode) throws RemoteException
	{
		System.out.println("Join called!");
		this.predecessorId = -1;
		System.out.println(anotherNode.getSuccessorId());
		this.successorId = anotherNode.findSuccessor(this.myId);
		System.out.println("Join:"+ this.successorId);

	}
	public void notify(Node node) throws RemoteException
	{

	}
	public void fixFingers() throws RemoteException
	{

	}
	public void checkPredecessor() throws RemoteException
	{

	}
	public void redistributeKeys() throws RemoteException
	{

	}


	public int findSuccessor(int key) throws RemoteException
	{
		if(key > myId && key <= successorId)
		{
			return successorId;
		}
		else
		{
			//ChordInterface precedingNode = closestPrecedingNode(id);
			//return precedingNode.findSuccessor(key);
		}

		return successorId;
	}

	/*End chord functions*/

	public int getId() throws RemoteException
	{
		return this.myId;
	}

	public int getSuccessorId() throws RemoteException
	{
		return this.successorId;
	}

	public int getPredecessorId() throws RemoteException
	{
		return this.predecessorId;
	}

	public void setSuccessorId(int id) throws RemoteException
	{
		this.successorId = id;
	}

	public void setPredecessorId(int id) throws RemoteException
	{
		this.predecessorId = id;
	}



	public int predecessorId;
	public int successorId;
	public ConcurrentHashMap<String,String> FingerTable;
	public int myId;
	public static final int rmiRegistryPort = 5000;
	public ChordInterface firstNode;


	public Node(String id) throws RemoteException
	{
		myId = Integer.parseInt(id);
		System.out.println("Node Initialized");
	}

	/**
	 * Creates an RMI registry on the specified port and registers itself as the bootstrapNode.
	 * 
	 */
	public static void main(String[] args)
	{

		if(args.length==0)
		{
			System.out.println("Usage: java Node <NodeID>");
			System.exit(-1);
		}

		int port = Integer.parseInt(args[0]);

		Node thisNode = null;
		

		boolean isFirstNode = true;

		try
		{
			thisNode = new Node(args[0]);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		try
		{
			LocateRegistry.createRegistry(1099);	
		}
		catch(Exception e)
		{
			
		}

		try
		{
			//Naming.rebind("//127.0.0.1:"+args[0]+"/BSNode",thisNode);
			//If the bs node wants to leave and then a new node is added?
			//need a seperate thread to  bind the object all the time. for every 1 minute orr so. T
			//thats for later.
			Naming.bind("//127.0.0.1/BSNode",thisNode);

		}
		catch(Exception e)
		{
			isFirstNode = false;
		}


		System.out.println("IsFirstNode:"+ isFirstNode);

		/**
		 * Bind it using the port number as the id again,
		 */		
		try
		{
			Naming.bind("//127.0.0.1/"+args[0], thisNode);
			
		}
		catch(Exception e)
		{
			
		}
		

		if(isFirstNode == true)
		{
			//true;
			thisNode.firstNode = thisNode;
			//create the chord ring.
			try
			{
				thisNode.create();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{	
			try
			{
				thisNode.firstNode = (ChordInterface) Naming.lookup("//127.0.0.1/BSNode"); 
			}
			catch(Exception e)
			{
				System.err.println("Some problem in finding BS Node");
				e.printStackTrace();
			}

			try
			{
				System.out.println("BSNode id "+ thisNode.firstNode.getId());
				thisNode.join(thisNode.firstNode);
			}
			catch(Exception e)
			{

			}

			//  join the ring
			

		}	


	}
}