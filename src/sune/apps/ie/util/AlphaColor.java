/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.util;

import javafx.scene.paint.Color;

public class AlphaColor
{
	public static Color create(Color color, int alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha / 255.0);
	}
	
	public static Color create(Color color, double alpha)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}