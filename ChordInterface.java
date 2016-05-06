import java.util.*;
import java.io.*;
import java.rmi.*;

public interface ChordInterface extends Remote
{
	//Methods from the paper
	public void create() throws RemoteException;
	public void join(ChordInterface anotherNode) throws RemoteException;
	public void notify(ChordInterface possiblePredecessorNode) throws RemoteException;	
	public void updateFingers(int disconnectedNodeId) throws RemoteException;
	public void disconnect() throws RemoteException;
	
	public void redistributeKeys(int nodeId) throws RemoteException;
	// File transfer
	public void moveFiletoNode(String fileName,int NodeId) throws IOException, RemoteException;
	public int readFileContent() throws IOException, RemoteException;
	public boolean writetoDownloadFile(byte[] buffer) throws IOException, RemoteException;
	public boolean downloadFile(String fileName) throws IOException, RemoteException;
	public boolean uploadFile(String fileName) throws IOException, RemoteException;
	public long fetchFileLength(String fileName) throws IOException, RemoteException;
	public void writerClose() throws IOException, RemoteException;
	public void readerClose() throws IOException, RemoteException;
	

	// new methods
	public int getId() throws RemoteException;
	public void setPredecessorId(int id) throws RemoteException;
	public void setSuccessorId(int id) throws RemoteException;
	public int getPredecessorId() throws RemoteException;
	public int getSuccessorId() throws RemoteException;
	public int findSuccessor(int key) throws RemoteException;
	public ArrayList<Integer> getFingerList() throws RemoteException;
	public Object getLock() throws RemoteException;
	public ArrayList<Integer> getResponsibleKeys() throws RemoteException;
}
