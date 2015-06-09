/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.layer;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sune.apps.ie.graphics.ImageUtils;

public class EmptyLayer extends ImageLayer
{
	public EmptyLayer(double x, double y, double width, double height)
	{
		super(new WritableImage((int) width, (int) height), x, y, width, height);
	}
	
	public void fill(Color color)
	{
		image = ImageUtils.fillImage(color, (int) width, (int) height);
	}
}