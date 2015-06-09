/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.util.ArrayList;
import java.util.List;

import sune.apps.ie.util.Utils;

public class ThreadRegistry
{
	private static List<Thread> threads;
	private static Thread threadCheck;
	private static boolean running;
	
	public static boolean isRunning;
	
	static
	{
		threads		= new ArrayList<>();
		threadCheck = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(running)
				{
					for(int i = 0; i < threads.size(); i++)
					{
						Thread thread = threads.get(i);
						if(!thread.isAlive() || thread.isInterrupted())
							threads.remove(i--);
					}
					
					Utils.sleep(1);
				}
			}
		});
		
		isRunning = true;
		threadCheck.start();
	}

	public static void registerThread(Thread thread)
	{
		threads.add(thread);
	}
	
	public static void registerThreadAndStart(Thread thread)
	{
		threads.add(thread);
		thread.start();
	}
	
	public static void closeAllThreads()
	{
		isRunning = false;
		while(threads.size() > 0)
			for(int i = 0; i < threads.size(); i++)
				try
				{
					Thread thread = threads.get(i);
					while(thread.isAlive())
						thread.interrupt();
					
					threads.remove(i);
				}
				catch(Exception ex) { }

		running = false;
		while(threadCheck.isAlive())
			threadCheck.interrupt();
	}
}