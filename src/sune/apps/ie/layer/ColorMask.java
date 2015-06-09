/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.layer;

public class ColorMask
{
	private int maskValue;

	public ColorMask(int maskValue)
	{
		this.maskValue = maskValue;
	}
	
	public void applyMask(int[] pixels, int[] output)
	{
		for(int i = 0; i < pixels.length; i++)
			output[i] = pixels[i] & maskValue;
	}
	
	public void setMaskValue(int maskValue)
	{
		this.maskValue = maskValue;
	}
	
	public int getMaskValue()
	{
		return maskValue;
	}
}