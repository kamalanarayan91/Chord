import java.util.*;
import java.io.*;

import java.util.concurrent.atomic.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;

public class StabilizationThread implements Runnable
{
	public ChordInterface node;
	
	StabilizationThread(ChordInterface node)
	{
		this.node = node;		
	}
	public void run()
	{

		while(true)
		{
			

			try
			{
				Thread.sleep(Utilities.stabilizationTimeout);
				System.out.println("Stabilize Thread:");

				
				if(node.getSuccessorId() == node.getId()) // Bootstrap node case
				{
					if(node.getPredecessorId() !=-1)
					{
						node.setSuccessorId(node.getPredecessorId());
						System.out.println("Stabilize Thread: BSNode case");

						ChordInterface successor = (ChordInterface) Naming.lookup("//127.0.0.1/"+ node.getSuccessorId());
						successor.notify(node);
						

						ArrayList<Integer> fList = node.getFingerList();

						synchronized(fList)
						{
							
							if(fList.size() == 1)
							{
								fList.add(1 , node.getSuccessorId() );
							}
							else
							{
								fList.set(1 , node.getSuccessorId());
							}	
						}

					}
				}
				else
				{
					System.out.println("Stabilize Thread: Nalla thaan iruku case");
					ChordInterface successor = (ChordInterface)  Naming.lookup("//127.0.0.1/"+node.getSuccessorId());
					int successorPredecessorId = successor.getPredecessorId();

					System.out.println("Stabilize Thread: Successor:" + successor.getId());
					System.out.println("Stabilize Thread: Suc-Predecessor:"+ successorPredecessorId);

					if(Utilities.checkRange(successorPredecessorId,node.getId(),node.getSuccessorId()) == true)
					{
						ChordInterface successorPredecessor = (ChordInterface) Naming.lookup("//127.0.0.1/"+ node.getSuccessorId());
						successorPredecessor.notify(node);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	
	}
}