/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import sune.apps.ie.layer.Layer;

public class GraphicsImageCursor extends GraphicsCursor implements Drawable
{
	protected Image image;
	
	public GraphicsImageCursor(Image image, double x, double y, double width, double height)
	{
		super(x, y, width, height);
		this.image = image;
	}
	
	@Override
	public void draw(GraphicsContext gc, Layer layer, double scale)
	{
		gc.drawImage(image, x, y, width * scale, height * scale);
	}
}