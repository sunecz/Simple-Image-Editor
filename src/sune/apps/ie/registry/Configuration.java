/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import sune.utils.ssdf.SSDFCore;

public class Configuration 
{
	public static String CONFIG_FILE_PATH = PathSystem.DIRECTORY + "resources/config.ssdf";
	private static SSDFCore config;
	
	public static void init()
	{
		File configFile = new File(CONFIG_FILE_PATH);
		
		// Creates the file if it does not exist
		if(!configFile.exists())
		{
			try
			{
				new File(configFile.getParent()).mkdirs();
				configFile.createNewFile();
			}
			catch(Exception ex) {}
		}
		
		config = new SSDFCore(configFile);
		
		// Default values
		if(!hasProperty("language"))		setStringProperty("language", "EN");
		if(!hasProperty("style"))			setStringProperty("style", "default.css");
		if(!hasProperty("checkUpdates"))	setStringProperty("checkUpdates", "true");
		
		save();
	}
	
	public static String getStringProperty(String name)
	{
		return config.getObject(name).getValue();
	}
	
	public static int getIntProperty(String name)
	{
		try
		{
			return Integer.parseInt(config.getObject(name).getValue());
		}
		catch(Exception ex) {}
		
		return Integer.MAX_VALUE;
	}
	
	public static double getDoubleProperty(String name)
	{
		try
		{
			return Double.parseDouble(config.getObject(name).getValue());
		}
		catch(Exception ex) {}
		
		return Double.MIN_VALUE;
	}
	
	public static boolean getBooleanProperty(String name)
	{
		try
		{
			return Boolean.parseBoolean(config.getObject(name).getValue());
		}
		catch(Exception ex) {}
		
		return false;
	}
	
	public static void setStringProperty(String name, String value)
	{
		config.setObject(name, value);
	}
	
	public static void setIntProperty(String name, int value)
	{
		config.setObject(name, value);
	}

	public static void setDoubleProperty(String name, double value)
	{
		config.setObject(name, value);
	}
	
	public static void setBooleanProperty(String name, boolean value)
	{
		config.setObject(name, value);
	}
	
	public static boolean hasProperty(String name)
	{
		return config.hasObject(name);
	}
	
	public static void save()
	{
		try
		{
			String content = config.getContentString();
			BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(new File(CONFIG_FILE_PATH)));
			
			byte[] bytes = content.getBytes();
			bos.write(bytes);
			
			bos.flush();
			bos.close();
		}
		catch(Exception ex) {}
		
	}
}