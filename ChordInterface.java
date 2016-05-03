import java.util.*;
import java.io.*;
import java.rmi.*;


public interface ChordInterface extends Remote
{
	//Methods from the paper
	public void create() throws RemoteException;
	public void join(ChordInterface anotherNode) throws RemoteException;
	public void notify(Node node) throws RemoteException;
	public void fixFingers() throws RemoteException;
	public void checkPredecessor() throws RemoteException;
	public void redistributeKeys() throws RemoteException;
	

	// new methods
	public int getId() throws RemoteException;
	public void setPredecessorId(int id) throws RemoteException;
	public void setSuccessorId(int id) throws RemoteException;
	public int getPredecessorId() throws RemoteException;
	public int getSuccessorId() throws RemoteException;
	public int findSuccessor(int key) throws RemoteException;
}
