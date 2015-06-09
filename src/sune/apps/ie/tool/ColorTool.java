/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.scene.paint.Color;

public interface ColorTool
{
	public void setColor(Color color);
	public void setColor(int argb);
	public int getColor();
}