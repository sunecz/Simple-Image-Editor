/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import sune.apps.ie.api.CustomPlugin;
import sune.apps.ie.component.Dialog;
import sune.apps.ie.translation.Translation;

public class PluginLoader
{
	public static void registerAllPlugins(String path)
	{
		PluginsRegistry.clear();
		for(Plugin32 plugin : getAllPlugins(path))
			PluginsRegistry.registerPlugin(plugin);
	}
	
	public static List<Plugin32> getAllPlugins(String path)
	{
		File folder;
		if(!(folder = new File(path)).exists())
			return new ArrayList<>();
		
		List<Plugin32> plugins 	= new ArrayList<>();
		File[] files			= folder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				String[] split 	= name.split("\\.");
				String type  	= split[split.length - 1];

				return type.equals("jar");
			}
		});
		
		if(files != null)
		{
			for(File file : files)
				plugins.add(new Plugin32(file.getAbsolutePath()));
		}
		
		return plugins;
	}
	
	public static Plugin32 loadPlugin(String path)
	{
		try
		{
			File file = new File(path);
			ClassLoader loader = new URLClassLoader(
				new URL[] { file.toURI().toURL() },
				PluginLoader.class.getClassLoader());

			Class<?> cls 								= Class.forName("Main", true, loader);
			Class<? extends CustomPlugin> pluginClass 	= cls.asSubclass(CustomPlugin.class);
			Constructor<? extends CustomPlugin> con		= pluginClass.getConstructor();
			
			CustomPlugin customPlugin = con.newInstance();
			customPlugin.load();
		}
		catch(Exception ex)
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("pluginAPI.cannotLoadPlugin"));
		}
			
		return null;
	}
}