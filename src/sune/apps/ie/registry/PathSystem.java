/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.net.URLDecoder;
import java.util.Arrays;

import sune.apps.ie.util.Utils;

public class PathSystem
{
	public static final String DIRECTORY;
	public static final String RESOURCES_FOLDER = "/resources/";
	public static final String ICONS_FOLDER 	= "/resources/icons/";
	public static final String STYLES_FOLDER	= "/resources/styles/";
	
	static
	{
		String path	= PathSystem.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		path = path.trim().substring(1).trim();
		
		try
		{
			// Decodes the path
			path = URLDecoder.decode(path, "UTF-8");
		} 
		catch (Exception ex) {}
		
		String[] split	= path.split("/");
		path = Utils.implode(Arrays.copyOfRange(split, 0, split.length-1), "/") + "/";
		
		DIRECTORY = path;
	}
	
	public static String getFullPath(String path)
	{
		return (DIRECTORY + path).replace("//", "/");
	}
}