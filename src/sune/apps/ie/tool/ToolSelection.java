/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.graphics.GraphicsImageCursor;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.selection.Selection;
import sune.apps.ie.translation.Translation;

public class ToolSelection extends Tool
{
	public static final String NAME  = "TOOL_SELECTION";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_selection.png");
	
	private LayersManager layersManager;
	private Layer selectedLayer;
	
	private WritableImage cursorImage;
	private GraphicsCursor cursor;
	
	private double x;
	private double y;
	
	private double mx;
	private double my;
	
	private boolean dragged;
	private boolean dragging;
	
	public ToolSelection(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
		this.layersManager 	= canvas.getLayersManager();
		this.cursorImage 	= ResourceLoader.loadImage("cursors/cursor_selection.png");
	}

	@Override
	public void draw(GraphicsContext gc)
	{
		if(cursor == null)
			return;
		
		if(dragging)
		{
			gc.save();
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(5);
			
			double rx = Math.min(x, mx);
			double ry = Math.min(y, my);
			double rw = Math.abs(x-mx);
			double rh = Math.abs(y-my);
			
			gc.setLineDashOffset(5);
			gc.strokeRect(rx, ry, rw, rh);
			gc.restore();
		}
		
		cursor.draw(gc, null, 1);
	}
	
	@Override
	public void unload()
	{
		selectedLayer = null;
		canvas.setTToolsVisible(true);
	}
	
	@Override
	public void reset() {}

	@Override
	public Pane init()
	{		
		Pane pane = new Pane();
		pane.setPadding(new Insets(15, 15, 16, 15));
		return pane;
	}
	
	private void moveCursor(MouseEvent event)
	{
		if(cursor == null)
			return;

		cursor.setX((int) (event.getX() - cursorImage.getWidth()/2));
		cursor.setY((int) (event.getY() - cursorImage.getHeight()/2));
		canvas.redraw();
	}
	@Override
	public void mouseMoved(MouseEvent event) 
	{
		moveCursor(event);
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if(dragged)
		{
			x = event.getX();
			y = event.getY();
			dragging = true;
			
			double lo = canvas.leftOffset();
			double to = canvas.topOffset();
			int sx = (int) Math.max(0, Math.min(x-lo, mx-lo));
			int sy = (int) Math.max(0, Math.min(y-to, my-to));
			int sw = (int) Math.min(Math.abs(x-mx), selectedLayer.getWidth(1)-1);
			int sh = (int) Math.min(Math.abs(y-my), selectedLayer.getHeight(1)-1);
			
			editor.setToolInfo(Translation.getTranslation("tools.info.selection",
				Double.toString(sx),
				Double.toString(sy),
				Double.toString(sw),
				Double.toString(sh)));
			canvas.setTToolsVisible(false);
		}
		
		moveCursor(event);
	}

	@Override
	public void mouseDown(MouseEvent event)
	{
		if(event.getButton() != MouseButton.PRIMARY ||
		   !canvas.isInitialized())
			return;
		
		selectedLayer = layersManager.getSelectedLayer();
		if(!selectedLayer.isMovable())
			return;
		
		mx = event.getX();
		my = event.getY();
		
		dragged = true;		
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		if(selectedLayer instanceof ImageLayer)
		{
			double lo = selectedLayer.getX(1);
			double to = selectedLayer.getY(1);
			double lw = selectedLayer.getWidth(1);
			double lh = selectedLayer.getHeight(1);
			
			double x = canvas.scalePosition(this.x);
			double y = canvas.scalePosition(this.y);
			double mx = canvas.scalePosition(this.mx);
			double my = canvas.scalePosition(this.my);
			
			double shiftW = mx < lo ? lo-mx : 0;
			double shiftH = my < to ? to-my : 0;
			
			int sx = (int) Math.max(0, Math.min(Math.min(x-lo, mx-lo), lw));
			int sy = (int) Math.max(0, Math.min(Math.min(y-to, my-to), lh));
			int sw = (int) Math.max(0, Math.min(Math.abs(x-mx-shiftW), lw));
			int sh = (int) Math.max(0, Math.min(Math.abs(y-my-shiftH), lh));
			
			if(mx+sw < lo || my+sh < to)
				sw = sh = 0;
			
			if(selectedLayer != null)
			{
				Selection selection = selectedLayer.getSelection();
				if((selection == null || !(event.isShiftDown() || event.isControlDown())) &&
				   (sw > 0 && sh > 0))
					selection = selectedLayer.createSelection();
				
				if(selection != null)
				{
					for(int j = sy; j <= sy+sh; j++)
						for(int k = sx; k <= sx+sw; k++)
						{
							if(event.isControlDown())	selection.remove(k, j);
							else						selection.add(k, j);
						}
					
					selection.prepare();
				}
				
				canvas.setTToolsVisible(selection.isEmpty());
			}
		}
		
		mx = 0;
		my = 0;
		dragged  = false;
		dragging = false;
		
		canvas.redraw();
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		double w = cursorImage.getWidth();
		double h = cursorImage.getHeight();
		cursor = new GraphicsImageCursor(cursorImage, event.getX() - w/2, event.getY() - h/2, w, h);
		canvas.redraw();
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		if(!dragging)
			cursor = null;
		
		canvas.redraw();
	}

	@Override
	public GraphicsCursor getCursor()
	{
		return cursor;
	}
	
	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Image getIcon()
	{
		return ICON;
	}
}