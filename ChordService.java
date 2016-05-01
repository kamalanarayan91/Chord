import java.util.*;
import java.io.*;



public class ChordService extends UnicastRemoteObject implements ChordInterface
{

	//need a list of nodes.
	

	/*Start RMI definitions*/
	
	/**
	 * [nodeJoin description]
	 * @param  nodeId          [description]
	 * @throws RemoteException [description]
	 */
	public void nodeJoin(int nodeId) throws RemoteException
	{
		//set predecessor
		//set successor.
		//redistribute keys
	}

	/**
	 * [nodeLeaves description]
	 * @param  nodeId          [description]
	 * @throws RemoteException [description]
	 */
	public void nodeLeaves(int nodeId) throws RemoteException
	{

		//transfer key objects to node's successor.

	}

	/*End RMI definitions*/

	public ChordService()
	{

	}





}