/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.util.ArrayList;
import java.util.List;

import sune.apps.ie.event.EventListener;

public class ListenersRegistry<T>
{
	private List<EventListener<T>> listeners = new ArrayList<>();
	
	public void addListener(EventListener<T> listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(EventListener<T> listener)
	{
		listeners.remove(listener);
	}
	
	public void callAll(T oldValue, T newValue)
	{
		synchronized(listeners)
		{
			for(EventListener<T> listener : listeners)
				listener.call(oldValue, newValue);
		}
	}
}