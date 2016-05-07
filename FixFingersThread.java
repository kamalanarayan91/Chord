import java.util.*;
import java.io.*;

import java.util.concurrent.atomic.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;

/**
 * Periodically called so that a node can refresh its finger table
 * entries.
 */
public class FixFingersThread implements Runnable
{
	public ChordInterface thisNode;
	public int next;

	FixFingersThread(ChordInterface thisNode)
	{
		this.thisNode = thisNode;
		this.next = 0;
	}

	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(Utilities.fixFingersTimeout);
				synchronized(thisNode.getLock())
				{
					next = next + 1;

					if(next > Utilities.m)
					{
						next = 1;
					}

					int fingerId = thisNode.getId() +  (int)Math.pow(2, next- 1) ;
					
					fingerId = fingerId % Utilities.totalNodes;
				
					ArrayList<Integer> fList = thisNode.getFingerList();
					int newNode = thisNode.findSuccessor(fingerId);

					//System.out.println("next:"+next + " fingerId:"+fingerId +" newVal:"+newNode);

					synchronized(fList)
					{
						
						if(fList.size() == next && newNode!=thisNode.getId())
						{

							//System.out.println("next:"+next + " fingerId:"+fingerId +" newVal:"+newNode);
							fList.add(next ,  newNode);
						}
						else
						{

							if(fList.size() > next )
							{

								if( newNode != fList.get(next) && newNode != thisNode.getId())
								{
									//System.out.println("next:"+next+ " fingerId:"+fingerId  + " newVal:"+newNode);
									fList.set(next,newNode);
								}
							}
							
						}

					}
				}

				//System.err.println("fListSize:" + fList.size());


			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}