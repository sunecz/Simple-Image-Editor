/**
 * Copyright (c) 2015, Sune.
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
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;
import sune.apps.ie.registry.ProgressBackups;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class ToolMove extends Tool
{
	public static final String NAME  = "TOOL_MOVE";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_move.png");

	private LayersManager layersManager;
	private Layer selectedLayer;
	
	private WritableImage cursorImage;
	private GraphicsCursor cursor;
	
	private double mx;
	private double my;
	
	private double lx;
	private double ly;
	
	private boolean dragged;
	
	public ToolMove(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
		this.layersManager 	= canvas.getLayersManager();
		this.cursorImage 	= ResourceLoader.loadImage("cursors/cursor_move.png");
	}
	
	@Override
	public void draw(GraphicsContext gc)
	{
		if(cursor == null)
			return;
		
		if(selectedLayer != null && selectedLayer.isVisible())
		{
			double scale = canvas.getScale();
			double lx = selectedLayer.getX(scale);
			double ly = selectedLayer.getY(scale);
			double lw = selectedLayer.getWidth(scale);
			double lh = selectedLayer.getHeight(scale);
			
			gc.save();
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(5);
			gc.strokeRect(lx, ly, lw, lh);
			gc.restore();
		}
		
		cursor.draw(gc, null, 1);
	}

	@Override
	public void unload() {}
	
	@Override
	public void reset()
	{
		selectedLayer = null;
	}

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
			double scale = canvas.getScale();
			double x = canvas.scalePosition(event.getX() - mx);
			double y = canvas.scalePosition(event.getY() - my);
			
			selectedLayer.move(lx / scale + x, ly / scale + y);
			editor.setToolInfo(
				Translation.getTranslation("tools.info.move",
					Double.toString(Utils.round(selectedLayer.getX(1) - canvas.getBackgroundLayer().getX(1), 3)),
					Double.toString(Utils.round(selectedLayer.getY(1) - canvas.getBackgroundLayer().getY(1), 3))));
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
		if(!selectedLayer.isMovable() || !selectedLayer.isVisible())
			return;
		
		double scale = canvas.getScale();
		
		mx = event.getX();
		my = event.getY();
		lx = selectedLayer.getX(scale);
		ly = selectedLayer.getY(scale);

		double lw = selectedLayer.getWidth(scale);
		double lh = selectedLayer.getHeight(scale);
		
		if(Utils.inRange(mx, lx, lx+lw) &&
		   Utils.inRange(my, ly, ly+lh))
			dragged = true;		
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		mx = 0;
		my = 0;
		lx = 0;
		ly = 0;
		dragged = false;
		
		ProgressBackups.createBackup(editor);
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		selectedLayer = layersManager.getSelectedLayer();
		
		double w = cursorImage.getWidth();
		double h = cursorImage.getHeight();
		cursor = new GraphicsImageCursor(cursorImage, event.getX() - w/2, event.getY() - h/2, w, h);
		canvas.redraw();
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
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
	
	@Override
	public boolean canDraw(double x, double y, double stroke)
	{
		return false;
	}
	
	@Override
	public boolean isEdgePoint(double x, double y, double stroke)
	{
		return false;
	}
}