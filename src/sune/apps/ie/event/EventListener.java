/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.event;

public interface EventListener<T>
{
	public void call(T oldValue, T newValue);
}