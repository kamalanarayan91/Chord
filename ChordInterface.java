import java.util.*;
import java.io.*;

public interface ChordInterface extends Remote
{
	//new node joins
	public void nodeJoin(int nodeId) throws RemoteException;
	public void nodeLeaves(int nodeId) throws RemoteException;
	public void keyRestore() throws RemoteException;
	

}
