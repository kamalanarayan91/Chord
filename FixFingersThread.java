import java.util.*;
import java.io.*;

import java.util.concurrent.atomic.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;


public class FixFingersThread implements Runnable
{
	public ChordInterface thisNode;
	public AtomicInteger next;

	FixFingersThread(ChordInterface thisNode)
	{
		this.thisNode = thisNode;
		next = new AtomicInteger(0);
	}

	@Override
	public void run()
	{
		try{
		Thread.sleep(Utilities.fixFingersTimeout);

		int nextInt = next.incrementAndGet();
		if(nextInt > Utilities.m )
		{
			next.set(0);
		}

			int fingerId = thisNode.getId() +  (int)Math.pow(2, nextInt - 1);
		//	thisNode.getFingerList().put(nextInt, thisNode.findSuccessor(fingerId));
			ArrayList<Integer> fList = thisNode.getFingerList();

			synchronized(fList)
			{
				System.err.println("fListSize:" + fList.size());
				System.err.println("nextInt:"+ nextInt);
				if(fList.size() == nextInt)
				{
					fList.add(nextInt , thisNode.findSuccessor(fingerId) );
				}
				else
				{
					fList.set(nextInt,thisNode.findSuccessor(fingerId));
				}	
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}