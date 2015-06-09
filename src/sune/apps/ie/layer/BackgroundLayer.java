/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.layer;

import sune.apps.ie.graphics.ImageUtils;
import sune.apps.ie.registry.ResourceLoader;

public class BackgroundLayer extends ImageLayer
{
	public static final String backgroundPath = "transparent_background.png";
	
	public BackgroundLayer(double x, double y, double width, double height)
	{
		super(ImageUtils.fillImage(
			ResourceLoader.loadImage(backgroundPath), (int) width, (int) height), 
			x, y, width, height);
	}
	
	@Override
	public void resize(double width, double height)
	{
		this.width  = width;
		this.height = height;
		
		image = ImageUtils.fillImage(
			ResourceLoader.loadImage(backgroundPath),
			(int) width, (int) height);
	}
	
	@Override
	public boolean canHaveMask()
	{
		return false;
	}
	
	@Override
	public boolean canBeTransformed()
	{
		return false;
	}
	
	@Override
	public boolean canBeLoaded()
	{
		return false;
	}
	
	@Override
	public boolean isMovable()
	{
		return false;
	}
	
	@Override
	public boolean isRotatable()
	{
		return false;
	}
	
	@Override
	public boolean isResizable()
	{
		return false;
	}
}