/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import javafx.scene.canvas.GraphicsContext;
import sune.apps.ie.layer.Layer;

public interface Drawable extends Shape2D
{
	public void draw(GraphicsContext gc, Layer layer, double scale);
}