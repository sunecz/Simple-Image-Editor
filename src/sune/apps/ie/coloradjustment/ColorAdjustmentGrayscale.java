/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.coloradjustment;

import sune.apps.ie.Editor;
import sune.apps.ie.plugin.PluginWindow;

public class ColorAdjustmentGrayscale extends PluginWindow
{	
	public ColorAdjustmentGrayscale(Editor editor)
	{
		super("colorAdjustments.grayscale", editor, 250, 120);
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
		image.grayscale();
	}
}