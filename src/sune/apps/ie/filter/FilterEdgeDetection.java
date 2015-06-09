/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.filter;

import sune.apps.ie.Editor;
import sune.apps.ie.plugin.PluginWindow;

public class FilterEdgeDetection extends PluginWindow
{	
	public FilterEdgeDetection(Editor editor)
	{
		super("filters.edgeDetection", editor, 250, 120);
	}
	
	@Override
	public void load()
	{
		super.load();
		apply();
		show();
	}
	
	@Override
	public void unload()
	{
		super.unload();
		close();
	}

	@Override
	public synchronized void applyToImage()
	{
		image.filters.edgeDetection();
	}
}