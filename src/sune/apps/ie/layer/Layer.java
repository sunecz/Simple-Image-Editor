/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.layer;

import javafx.scene.canvas.GraphicsContext;
import sune.apps.ie.graphics.LayerBlendMode;
import sune.apps.ie.selection.Selection;

public abstract class Layer implements Rotatable, Movable, Resizable
{
	private static int id = 0;
	protected String name;
	
	protected double x;
	protected double y;
	protected double width;
	protected double height;
	protected double angle;
	
	protected double originalWidth;
	protected double originalHeight;
	
	protected boolean changeVisibility;
	protected boolean visible;
	
	protected LayerMask mask;
	protected boolean maskSelected;
	
	protected boolean editable;
	protected LayerBlendMode blendMode;
	
	protected Selection selection;
	
	public Layer(double x, double y, double width, double height)
	{
		this.x 		= x;
		this.y 		= y;
		this.width 	= width;
		this.height = height;

		this.originalWidth  = width;
		this.originalHeight = height;
		
		this.name    			= "Layer " + (id++);
		this.changeVisibility 	= true;
		this.visible 			= true;
		this.blendMode			= LayerBlendMode.NORMAL;
	}
	
	public abstract void render(GraphicsContext gc);
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setChangeVisibility(boolean flag)
	{
		changeVisibility = flag;
	}
	
	public void translate(double x, double y)
	{
		this.x += x;
		this.y += y;
	}
	
	@Override
	public void move(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	@Deprecated
	public void resize(double width, double height)
	{
		this.width  = width;
		this.height = height;
	}
	
	public void scale(double scaleX, double scaleY)
	{
		this.width  = originalWidth*scaleX;
		this.height = originalHeight*scaleY;
	}
	
	@Override
	public void rotate(double angle)
	{
		this.angle = angle;
	}
	
	public void setVisibility(boolean flag)
	{
		visible = flag;
	}
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public double getX(double scale)
	{
		return x * scale;
	}

	@Override
	public double getY(double scale)
	{
		return y * scale;
	}

	@Override
	public double getWidth(double scale)
	{
		return width * scale;
	}

	@Override
	public double getHeight(double scale)
	{
		return height * scale;
	}
	
	@Override
	public double getAngle()
	{
		return angle;
	}
	
	public void setSize(double width, double height)
	{
		this.width  = width;
		this.height = height;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	public boolean canChangeVisibility()
	{
		return changeVisibility;
	}
	
	public void setMask(LayerMask mask)
	{
		this.mask = mask;
	}
	
	public void setEditable(boolean flag)
	{
		this.editable = flag;
	}
	
	public LayerMask getMask()
	{
		return mask;
	}
	
	public void selectMask(boolean flag)
	{
		maskSelected = flag;
	}
	
	public boolean isMaskSelected()
	{
		return maskSelected;
	}
	
	public boolean canHaveMask()
	{
		return true;
	}
	
	public boolean canBeTransformed()
	{
		return true;
	}
	
	public boolean canBeLoaded()
	{
		return true;
	}
	
	public boolean isEditable()
	{
		return editable;
	}
	
	public boolean isMovable()
	{
		return true;
	}
	
	public boolean isRotatable()
	{
		return true;
	}
	
	public boolean isResizable()
	{
		return true;
	}
	
	public void setBlendMode(LayerBlendMode mode)
	{
		blendMode = mode;
	}
	
	public LayerBlendMode getBlendMode()
	{
		return blendMode;
	}
	
	public Selection createSelection()
	{
		return selection = new Selection(this);
	}
	
	public void removeSelection()
	{
		selection = null;
	}
	
	public Selection getSelection()
	{
		return selection;
	}
}