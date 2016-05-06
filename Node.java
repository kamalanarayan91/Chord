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

	public FileInputStream fin;
	public FileOutputStream fout;
	public String filesFolder;

	public ConcurrentHashMap<Integer,Integer> fingerTable;
	public ArrayList<String> fileList;
	public static Object lock = new Object();
	public ArrayList<Integer> fingerList; // 0 contains self, 1- immediate 2,3 .. next entries.

	public int myId;
	public static final int rmiRegistryPort = 5000;
	public ChordInterface firstNode;
	public Thread stabilizeThread;
	public Thread fixFingersThread;

	public boolean isBootStrap = false;
	public ArrayList<Integer> responsibleKeys;
	public ArrayList<Integer> fingerKeyList;

	public int counter = 1;
	public boolean flag = false;
	public int prevId = -1;

	/*Start Chord functions*/
	public void create() throws RemoteException
	{

		this.predecessorId = -1;
		this.successorId = myId;
		System.out.println("Chord Ring created");
		fingerTable.put(0,myId);
		fingerList.add(0,myId);
		stabilizeThread.start();
		fixFingersThread.start();
		this.isBootStrap = true;
		responsibleKeys();
	}

	public ArrayList<Integer> getResponsibleKeys() throws RemoteException
	{
		return this.responsibleKeys;
	}
	public Object getLock() throws RemoteException
	{
		return this.lock;
	}

	/**
	 *  Initiate Disconnection;
	 *  @param  anotherNode
	 *  @throws RemoteException
	 */
	public void disconnect() throws RemoteException
	{
		try
		{
			ChordInterface predecessorNode = (ChordInterface) Naming.lookup("//127.0.0.1/"+predecessorId);
			ChordInterface successorNode = (ChordInterface) Naming.lookup("//127.0.0.1/"+successorId);

			//file copy from this folder to successor's folder and then delete.
			successorNode.notify(predecessorNode);
			predecessorNode.setSuccessorId(successorId);
			predecessorNode.updateFingers(myId);
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
	}

	public void updateFingers(int disconnectedNodeId) throws RemoteException
	{
		synchronized(fingerList)
		{
			for(int i=1;i<fingerList.size();i++)
			{
				int val = fingerList.get(i);
				if(val == disconnectedNodeId)
				{
					fingerList.set(i,successorId);
				}
			}
		}
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
		

		this.fingerList.add(0,myId);

		setSuccessorId(tempVal);

		//this.successorId = tempVal;

		System.out.println("My Successor through join is:" + this.successorId);

		//notify the node that this is the predecessor.
		this.fingerTable.put(0,myId);

		try
		{
			ChordInterface successorNode= (ChordInterface) Naming.lookup("//127.0.0.1/"+this.successorId);
			successorNode.notify(this);

		}
		catch(Exception e)
		{
			System.out.println("Error in calling notify");
		}
			
		//move my files to successor
		redistributeKeys(successorId);
		stabilizeThread.start();
		fixFingersThread.start();

	}

	public void notify(ChordInterface possiblePredecessorNode) throws RemoteException
	{
		System.out.println("notify node called by" + possiblePredecessorNode.getId());

		if( predecessorId == -1 )
		{
			predecessorId = possiblePredecessorNode.getId();
			
			try
			{
				Thread.sleep(5000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
			responsibleKeys();
			try{
				Thread.sleep(5000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			redistributeKeys(predecessorId);

		}
		else if( possiblePredecessorNode.getId() > this.predecessorId && possiblePredecessorNode.getId()  < this.myId )
		{
			predecessorId = possiblePredecessorNode.getId();
			
			
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
			responsibleKeys();
			try{
				Thread.sleep(5000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			redistributeKeys(predecessorId);
		}
		else if(possiblePredecessorNode.getId() > this.myId && possiblePredecessorNode.getId() > this.successorId)
		{
			predecessorId = possiblePredecessorNode.getId();
			
			System.out.println("Predecessor Set!" + possiblePredecessorNode.getId());
			responsibleKeys();

			try{
				Thread.sleep(5000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			redistributeKeys(predecessorId);

		}

	}

	public void responsibleKeys()
	{
		synchronized(responsibleKeys)
		{
			responsibleKeys.clear();

			if(isBootStrap==true)
			{
				if(predecessorId==-1)
				{
					for(int i = 0; i < Utilities.totalNodes; i ++)
					{
						responsibleKeys.add(i);
					}
				}
				else
				{
					
					for(int i=predecessorId+1;;i++)
					{
						
						if(i%Utilities.totalNodes== myId)
						{
							responsibleKeys.add(i%Utilities.totalNodes);
							break;
						}

						responsibleKeys.add(i%Utilities.totalNodes);
					}					
				}
			}

			else
			{

				for(int i=predecessorId+1;;i++)
				{
					
					if(i%Utilities.totalNodes== myId)
					{
						responsibleKeys.add(i%Utilities.totalNodes);
						break;
					}

					responsibleKeys.add(i%Utilities.totalNodes);
				}

			}

		}

		synchronized(fingerList)
		{
			if(fingerKeyList.contains(predecessorId))
			{
				int index = fingerKeyList.indexOf(predecessorId);

				index++;

				if(fingerList.size() == index)
				{

				}
			}
		}
			
	}
	
	/*Keys In charge of this node*/
	public ArrayList<Integer> keysInCharge2()
	{
		ArrayList<Integer> result  = new ArrayList<Integer>();
		if(predecessorId!=-1)
		{

			for(int index=predecessorId+1; (index%Utilities.totalNodes) <= myId; index++)
			{
				result.add(index%Utilities.totalNodes);
				if(index%Utilities.totalNodes == myId)
				{
					return result;
				}
			}
			
		}

		return null;	
	}

	/*Find successor in chord ring*/
	public  int findSuccessor(int key) throws RemoteException
	{

		//System.out.println("finding successor for:" + key);
		ArrayList<Integer> result = new ArrayList<Integer>();

		synchronized(responsibleKeys)
		{
			if(responsibleKeys.contains(key))
			{
				return myId;
			}
		}

		if(successorId == predecessorId)
		{
			if(myId > successorId)
			{
				//4 - 0
				for(int index=myId+1; (index%Utilities.totalNodes) >= successorId; index++ )
				{
					result.add(index%Utilities.totalNodes);
					if(index%Utilities.totalNodes == successorId)
					{
						break;
					}
				}

				if(result.contains(key)==true)
				{
					//System.out.println("Case1: SuccId:" + successorId);
					return successorId;
				}

				// 0 - 4
				for(int index=successorId+1; (index%Utilities.totalNodes) <= myId; index++)
				{
					result.add(index%Utilities.totalNodes);
					if(index%Utilities.totalNodes == myId)
					{
						break;
					}	
				}

				if(result.contains(key)==true)
				{
					//System.out.println("Case1: myId:" + myId);
					return myId;
				}
			}
			else
			{
				
				for(int index=myId+1; (index%Utilities.totalNodes) <= successorId; index++ )
				{
					result.add(index%Utilities.totalNodes);
					if(index%Utilities.totalNodes == successorId)
					{
						break;
					}
				}

				if(result.contains(key)==true)
				{
					//System.out.println("Case2: successorId:" + successorId);
					return successorId;
				}

				// 0 - 4
				for(int index=successorId+1; (index%Utilities.totalNodes) >= myId; index++)
				{
					result.add(index%Utilities.totalNodes);
					if(index%Utilities.totalNodes == myId)
					{
						break;
					}	
				}

				if(result.contains(key)==true)
				{
					//System.out.println("Case2: myId:" + myId);
					return myId;
				}


			}
		}

		if(key > myId && key <= successorId)
		{
			
			return successorId;
		}
		
		ArrayList<Integer> result2 = new ArrayList<Integer>();
		if((result2 = keysInCharge2()) !=null)
		{
			if(result2.contains(key)== true)
			{
				
				return myId;
			}
		}
		
		int precedingNodeId = this.closestPrecedingNode(key);

		if(precedingNodeId != myId)
		{
			if(precedingNodeId != -1)
			{
				try
				{
					
					ChordInterface precedingNode =(ChordInterface) Naming.lookup("//127.0.0.1/"+precedingNodeId);
					//System.out.println("recursing:"+precedingNodeId + " for key:"+ key);
					return precedingNode.findSuccessor(key);
				}
				catch(Exception e)
				{
					System.exit(-1);
					System.out.println("error in find Successor!");
					e.printStackTrace();
				}
			}
		}

		return myId;
	}

	/**
	 * Finds the closest Preceding Node for the given Id
	 * @param  id [description]
	 * @return    [description]
	 */
	public  int closestPrecedingNode(int id) 
	{
		synchronized(fingerList)
		{
			if(fingerList.size()==0)
			{
				return -1;
			}
			for(int index=Utilities.m;index>=1;index--)
			{
				try
				{

					int value = fingerList.get(index);
					if(value==myId && responsibleKeys.contains(value) ==false)
					{
						continue;
					}
					if(Utilities.checkRange(value,myId,id))
					{				
						try
						{
							ChordInterface checkNode = (ChordInterface) Naming.lookup("//127.0.0.1/"+value);
						}
						catch(Exception e)
						{
							System.out.println("Node failed! - retrying.." + value);
							continue;
						}
						return value;
					}
					else if(id  > myId && value < id)
					{			
						return value;
						
					}
					else if( id < myId && id <= successorId)  // 7- 0 case
					{
						try
						{
							ChordInterface checkNode = (ChordInterface) Naming.lookup("//127.0.0.1/"+value);
						}
						catch(Exception e)
						{
							System.out.println("Node failed! - retrying.." + value);
							continue;
						}
						return value;
					}
				}
				catch(IndexOutOfBoundsException e)
				{
					continue;
				}
			}
		}
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
		synchronized(fingerList)
		{
			if(fingerList.size() > 1)
			{
				this.fingerList.set(1,id);
			}
			else
			{
				this.fingerList.add(1,id);
			}
		}

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
		
		stabilizeThread = new Thread(new StabilizationThread(this));
		fixFingersThread = new Thread(new FixFingersThread(this));
		responsibleKeys = new ArrayList<Integer>();
		fingerKeyList = new ArrayList<Integer>();

		
		for(int i=0;i<4;i++)
		{

			fingerKeyList.add((myId +  (int)Math.pow(2, i- 1))% Utilities.totalNodes );
			
		}

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

		if(port<0 || port >8)
		{
			System.out.println("Please enter an id number between 0 and 7");
		}
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
			System.out.println("Please enter a non-duplicate / unique node  Id between 0-7");
			System.exit(-1);
		}



		try {
			//File Services
			int nodeId = thisNode.getId();
			File file = new File(Integer.toString(nodeId));
			System.out.println(file.getAbsolutePath());

			if(file.exists()== true)
			{
				if(file.isDirectory())
				{
					thisNode.setFilesFolder(file.getAbsolutePath());
				}

			}
			else
			{
				file.mkdir();
			}
			//thisNode.redistributeKeys();
		} 
		catch (RemoteException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
				System.out.println("Some problem in finding BS Node");
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

		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<4;i++)
		{
			if(i==0)
			{
				list.add(thisNode.myId);
			}
			else
			{
				list.add((thisNode.myId +  (int)Math.pow(2, i- 1))% Utilities.totalNodes );
			}
		}

		while(true)
		{
			try
			{
				Thread.sleep(10000);
				//System.out.println("My successor is:" + thisNode.getSuccessorId());
				//System.out.println("My predecessor is:" + thisNode.getPredecessorId());
				System.out.println("ResponsibleKeys::: "+ thisNode.responsibleKeys);

				synchronized(thisNode.fingerList)
				{
					System.out.println("FingerKeyEntryList:::" + list);
					System.out.println("FingerList:::"+ thisNode.fingerList);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public String getFilesFolder()
	{
		return this.filesFolder;
	}
	
	public void setFilesFolder(String folderName)
	{
		this.filesFolder = folderName;
	}
	
	public boolean downloadFile(String fileName) throws IOException, RemoteException {
		
		File file = new File(filesFolder+"/"+fileName);
		fout =  new FileOutputStream(file);
		System.out.println("File Writer write "+ fileName);
		if(fout!=null)
		{
			return true;
		}
		return false;
		
		// TODO Auto-generated method stub
		
	}

	public boolean uploadFile(String fileName) throws IOException, RemoteException {
		// TODO Auto-generated method stub
		File file = new File(filesFolder+"/"+fileName);
		if(file.exists()&&file.isFile())
		{
			fin = new FileInputStream(file);
		}
		System.out.println("File Reader Read "+ fileName);
		if(fin!=null)
		{
			return true;
		}
		return false;
	}
	
	public int readFileContent() throws IOException, RemoteException {
		return fin.read();
	}

	public boolean writetoDownloadFile(byte[] buffer) throws IOException, RemoteException {
		fout.write(buffer);
		return true;
	}
	
	public long fetchFileLength(String fileName) throws IOException,RemoteException 
	{
		// TODO Auto-generated method stub
		File file = new File (filesFolder+"/"+fileName);
		if(file.exists()&&file.isFile())
		{
			return file.length();
		}
		return 0;
	}
	

	public void writerClose() throws IOException, RemoteException {
		fout.close();
	}

	public void readerClose() throws IOException, RemoteException {
		fin.close();
	}
	
	public void moveFiletoNode(String fileName,int NodeId) throws IOException, RemoteException
	{
	
		File file = new File(filesFolder+"/"+fileName);
		ChordInterface newNode;
		
		try
		{
			newNode= (ChordInterface) Naming.lookup("//127.0.0.1/"+NodeId);
			System.out.println("Node found "+NodeId);
		}
		catch(Exception e)
		{
			newNode = null;
		}
		if(newNode!=null && file.exists() && file.isFile())
		{
			try 
			{
				uploadFile(file.getName());
				newNode.downloadFile(file.getName());

				long offset = 0;
				long fileLength = fetchFileLength(file.getName());
				byte[] buffer = new byte[1000];

				if(fileLength!= 0 && fileLength > 1000)
				{
					for(long i=0;i<fileLength;i+=1000)
					{
						if(fileLength-i <1000)
						{
							byte[] buff = new byte[fin.available()];
							int offsetlength = (int) (fileLength-i);
							fin.read(buff);
							newNode.writetoDownloadFile(buff);
						}
						else
						{

							//fin.read(buffer, (int)i, 1000);
							fin.read(buffer, 0, 1000);
							newNode.writetoDownloadFile(buffer);
						}
							
					}
				}
				else if(fileLength!=0 && fileLength <=1000)
				{
					byte[] buffd = new byte[fin.available()];
					fin.read(buffd);
					newNode.writetoDownloadFile(buffd);
				}

				newNode.writerClose();
				readerClose();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Error encountered while downloading File");
			}
	
		}
		else
		{
			System.out.println("Node/File does not exist");
		}
	}
		
	public void redistributeKeys(int nodeId) throws RemoteException
	{
		File Folder = new File(filesFolder);
		
		String hash = "";
		String folderName = Folder.getAbsolutePath();
		ChordInterface destinationNode = null;

		if(nodeId == myId)
		{
			System.out.println("tried to move files. But not required");
			return;
		}
		try
		{
			 destinationNode = (ChordInterface) Naming.lookup("//127.0.0.1/"+nodeId);
		}
		catch(Exception e)
		{
			System.out.println("destinaation node doesn't exist");
			return;
		}

		ArrayList<Integer> keys = destinationNode.getResponsibleKeys();
		System.out.println("destination keys:"+ keys);
		if(Folder.exists() && Folder.isDirectory())
		{
			
			for (File file : Folder.listFiles()) 
			{
				hash = Hasher.getHashString(file.getName(),Utilities.totalNodes);
				int hashVal = Integer.parseInt(hash);

				if (keys.contains(hashVal))
				{
					System.out.println("Moving file : "+ file.getName()+" to:: "+ hash);
					try 
					{
						moveFiletoNode(file.getName(),nodeId);
						file.delete();
					} 
					catch (NumberFormatException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				else
				{
					System.out.println("Not Moving file to: "+ file.getName()+" : "+ hash + " to:" + destinationNode.getId());
				}	
			}
			
		}
	}
}