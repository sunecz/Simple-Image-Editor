/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.translation.Translation;

public abstract class Tool
{
	protected Editor editor;
	protected CanvasPanel canvas;
	
	public Tool(Editor editor, CanvasPanel canvas)
	{
		this.editor = editor;
		this.canvas = canvas;
	}
	
	public abstract void draw(GraphicsContext gc);
	public abstract void reset();
	public abstract Pane init();
	public abstract void unload();
	
	public abstract void mouseMoved(MouseEvent event);
	public abstract void mouseDragged(MouseEvent event);
	public abstract void mouseDown(MouseEvent event);
	public abstract void mouseUp(MouseEvent event);
	public abstract void mouseEntered(MouseEvent event);
	public abstract void mouseExited(MouseEvent event);
	
	public abstract GraphicsCursor getCursor();
	public abstract String getName();
	public abstract Image getIcon();
	
	public String getTitle()
	{
		return Translation.getTranslation("tools." + getName());
	}
	
	public boolean canDraw(double x, double y, double stroke)
	{
		return false;
	}
	
	public boolean isEdgePoint(double x, double y, double stroke)
	{
		return false;
	}
	
	public boolean isEdgePoint2(double x, double y, double stroke, double distance)
	{
		return false;
	}
	
	public void setLayer(Layer layer) {}
	public void reload() {}
}