/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.util.ArrayList;
import java.util.List;

import sune.apps.ie.tool.Tool;

public class ToolsRegistry
{
	private static final List<Tool> tools
		= new ArrayList<>();
	
	public static void registerTool(Tool tool)
	{
		tools.add(tool);
	}
	
	public static void unregisterTool(Tool tool)
	{
		tools.remove(tool);
	}
	
	public static Tool getToolByName(String name)
	{
		for(Tool tool : tools)
			if(tool.getName().equals(name))
				return tool;
		
		return null;
	}
	
	public static List<Tool> getRegisteredTools()
	{
		return tools;
	}
}