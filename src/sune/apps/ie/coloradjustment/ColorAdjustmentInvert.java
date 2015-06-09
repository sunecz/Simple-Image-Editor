/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.coloradjustment;

import sune.apps.ie.Editor;
import sune.apps.ie.plugin.PluginWindow;

public class ColorAdjustmentInvert extends PluginWindow
{	
	public ColorAdjustmentInvert(Editor editor)
	{
		super("colorAdjustments.invert", editor, 250, 120);
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
		image.invert();
	}
}