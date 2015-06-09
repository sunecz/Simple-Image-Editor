/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.filter;

import sune.apps.ie.Editor;
import sune.apps.ie.plugin.PluginWindow;

public class FilterSmoothing extends PluginWindow
{	
	public FilterSmoothing(Editor editor)
	{
		super("filters.smoothing", editor, 250, 120);
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
		image.filters.smooth();
	}
}