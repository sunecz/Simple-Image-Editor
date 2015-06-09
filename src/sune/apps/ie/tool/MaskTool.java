/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.scene.paint.Color;

public interface MaskTool
{
	public void setMaskColor(Color color);
	public void setMaskColor(int color);
	public void setOpacity(int opacity);
	public int getMaskColor();
	public int getOpacity();
	
	public void setInMask(boolean flag);
	public boolean isInMask();
}