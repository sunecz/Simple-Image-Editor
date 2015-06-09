/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.layer;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import sune.apps.ie.graphics.Drawable;

public class VectorLayer extends Layer
{
	private List<Drawable> objects;
	
	public VectorLayer(double x, double y, double width, double height)
	{
		super(x, y, width, height);	
		this.objects = new ArrayList<>();
	}
	
	public List<Drawable> getObjects()
	{
		return objects;
	}
	
	@Override
	public void render(GraphicsContext gc)
	{
		for(Drawable object : objects)
			object.draw(gc, this, 1);
	}
}