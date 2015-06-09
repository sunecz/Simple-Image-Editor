/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.filter;

import sune.apps.ie.Editor;
import sune.apps.ie.plugin.PluginWindow;

public class FilterPressUp extends PluginWindow
{	
	public FilterPressUp(Editor editor)
	{
		super("filters.pressUp", editor, 250, 120);
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
		image.filters.pressUp();
	}
}