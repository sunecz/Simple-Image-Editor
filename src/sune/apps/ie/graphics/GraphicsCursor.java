/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sune.apps.ie.image.ColorConverter;
import sune.apps.ie.layer.Layer;

public class GraphicsCursor implements Drawable, Colored
{
	protected double x;
	protected double y;
	protected double width;
	protected double height;
	protected Color color;

	public GraphicsCursor(double x, double y, double width, double height)
	{
		this.x 		= x;
		this.y 		= y;
		this.width 	= width;
		this.height = height;
		this.color 	= Color.BLACK;
	}
	
	@Override
	public void draw(GraphicsContext gc, Layer layer, double scale)
	{
		double w = Math.max(4, width/2) * scale;
		double h = Math.max(4, height/2) * scale;
		
		gc.setStroke(color);
		gc.strokeOval(x-w, y-h, 2*w, 2*h);
		
		gc.strokeLine(x-w/2, y, x+w/2, y);
		gc.strokeLine(x, y-h/2, x, y+h/2);
	}
	
	@Override
	public double getX()
	{
		return x;
	}
	
	@Override
	public double getY()
	{
		return y;
	}
	
	@Override
	public double getWidth()
	{
		return width;
	}
	
	@Override
	public double getHeight()
	{
		return height;
	}
	
	@Override
	public void setX(double x)
	{
		this.x = x;
	}
	
	@Override
	public void setY(double y)
	{
		this.y = y;
	}
	
	@Override
	public void setWidth(double width)
	{
		this.width = width;
	}
	
	@Override
	public void setHeight(double height)
	{
		this.height = height;
	}
	
	@Override
	public int getColor()
	{
		return ColorConverter.FXColorToInt(color);
	}
	
	@Override
	public void setColor(Color color)
	{
		this.color = color;
	}
	
	@Override
	public void setColor(int argb)
	{
		this.color = ColorConverter.IntToFXColor(argb);
	}
}