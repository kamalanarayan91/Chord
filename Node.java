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


	public int predecessorId;
	public int successorId;

	public ConcurrentHashMap<Integer,Integer> fingerTable;
	public ArrayList<String> fileList;

	public ArrayList<Integer> successorList;
	public ArrayList<Integer> fingerList; // 0 contains self, 1- immediate 2,3 .. next entries.

	public int myId;
	public static final int rmiRegistryPort = 5000;
	public ChordInterface firstNode;
	public Thread stabilizeThread;
	public Thread fixFingersThread;

	public boolean isBootStrap = false;


	/*Start Chord functions*/
	public void create() throws RemoteException
	{

		this.predecessorId = -1;
		this.successorId = myId;
		System.out.println("Chord Ring created");
		fingerTable.put(0,myId);
		fingerList.add(0,myId);
		stabilizeThread.start();
		this.isBootStrap = true;
	}

	// Find predecessor's successor and assign it as successor.
	public void join(ChordInterface anotherNode) throws RemoteException
	{
		this.isBootStrap = false;
		System.out.println("Join called!");
		this.predecessorId = -1;

		//this.successorId = anotherNode.findSuccessor(this.myId);
		//
		int tempVal = anotherNode.findSuccessor(this.myId);
		System.out.println("tempVAl:"+tempVal);

		// a->b c comes case
		if(tempVal < myId)
		{
			try
			{
				ChordInterface predecessorNode= (ChordInterface) Naming.lookup("//127.0.0.1/"+tempVal);
				this.successorId = predecessorNode.getSuccessorId();
			}
			catch(Exception e){

			}


		}
		else // a->c b comes case
		{
			this.successorId = tempVal;
		}

		System.out.println("My Successor through join is:" + this.successorId);
		//notify the node that this is the predecessor.
		this.fingerTable.put(0,myId);
		this.fingerList.add(0,myId);
		this.fingerList.add(1,successorId);

//		anotherNode.notify(this);
		try
		{
			ChordInterface successorNode= (ChordInterface) Naming.lookup("//127.0.0.1/"+this.successorId);
			successorNode.notify(this);

		}
		catch(Exception e)
		{
			System.err.println("Error in calling notify");
		}
		stabilizeThread.start();
	}

	public void notify(ChordInterface possiblePredecessorNode) throws RemoteException
	{
		System.out.println("notify node called by" + possiblePredecessorNode.getId());

		if( predecessorId == -1 )
		{
			predecessorId = possiblePredecessorNode.getId();
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
		}
		else if( possiblePredecessorNode.getId() > this.predecessorId && possiblePredecessorNode.getId()  < this.myId )
		{
			predecessorId = possiblePredecessorNode.getId();
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
		}
		else if(possiblePredecessorNode.getId() > this.myId && possiblePredecessorNode.getId() > this.successorId)
		{
			predecessorId = possiblePredecessorNode.getId();
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
		}

	}

	
	public void checkPredecessor() throws RemoteException
	{

	}
	public void redistributeKeys() throws RemoteException
	{

	}


	public int findSuccessor(int key) throws RemoteException
	{
		System.out.println("finding successor for:" + key);
		System.out.println("my Id is:" + myId);
		if(key > myId && key <= successorId)
		{
			return successorId;
		}
		else
		{
			int precedingNodeId = this.closestPrecedingNode(key);
			if(precedingNodeId != myId)
			{
				if(precedingNodeId!= -1)
				{
					try
					{
						
						ChordInterface precedingNode =(ChordInterface) Naming.lookup("//127.0.0.1/"+precedingNodeId);
						return precedingNode.findSuccessor(key);
					}
					catch(Exception e)
					{
						System.err.println("error in find Successor!");
						e.printStackTrace();
					}
				}
			}
			else if(precedingNodeId == myId)
			{
				return myId;
			}
		}

		//never happens
		return successorId;
	}

	/**
	 * Finds the closest Preceding Node for the given Id
	 * @param  id [description]
	 * @return    [description]
	 */
	public int closestPrecedingNode(int id) 
	{
		if(fingerList.size()==0)
		{
			return -1;
		}

		System.out.println("FingerList size:" + fingerList.size());

		for(int index=Utilities.m;index>=1;index--)
		{
			
			try
			{

				int value = fingerList.get(index);
				System.err.println("index:" + index + " for value:"+value);
				if(Utilities.checkRange(value,myId,id))
				{
					System.err.println("val retu:  " + value);
					return value;
				}
				/*else if () {
					
				}*/
			}
			catch(IndexOutOfBoundsException e)
			{
				continue;
			}
		}

		System.err.println("my Id returned");

		return myId;
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

	public ArrayList<Integer> getFingerList() throws RemoteException
	{
		return this.fingerList;
	}

	public Node(String id) throws RemoteException
	{
		myId = Integer.parseInt(id);
		System.out.println("Node Initialized");
		fingerTable = new ConcurrentHashMap<Integer,Integer>();
		fingerList = new ArrayList<Integer>();
		successorList = new ArrayList<Integer>();
		stabilizeThread = new Thread(new StabilizationThread(this));
		fixFingersThread = new Thread(new FixFingersThread(this));

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


		}	


		while(true)
		{
			try
			{
				Thread.sleep(10000);
				System.out.println("My successor is:" + thisNode.getSuccessorId());
				System.out.println("My predecessor is:" + thisNode.getPredecessorId());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}