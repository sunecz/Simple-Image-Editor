/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.plugin;

public abstract class Plugin
{
	private String name;
	
	public Plugin(String name)
	{
		this.name = name;
	}
	
	public abstract void load();
	public abstract void unload();
	
	public String getName()
	{
		return name;
	}
}