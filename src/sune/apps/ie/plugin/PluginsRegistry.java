/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginsRegistry
{
	private static List<Plugin32> plugins
		= new ArrayList<>();
	
	public static void registerPlugin(Plugin32 item)
	{
		plugins.add(item);
	}

	public static void unregisterPlugin(Plugin32 item)
	{
		plugins.remove(item);
	}

	public static Plugin32 getPluginByName(String name)
	{
		for(Plugin32 plugin : plugins)
			if(plugin.getName().equals(name))
				return plugin;
		
		return null;
	}
	
	public static List<Plugin32> getRegisteredPlugins()
	{
		return plugins;
	}
	
	public static void clear()
	{
		plugins.clear();
	}
}