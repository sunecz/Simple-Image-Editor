/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import javafx.scene.paint.Color;

public interface Colored
{
	public void setColor(Color color);
	public void setColor(int argb);
	public int getColor();
}