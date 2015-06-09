/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.graphics.GraphicsImageCursor;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;
import sune.apps.ie.registry.ResourceLoader;

public class ToolViewMove extends Tool
{
	public static final String NAME  = "TOOL_VIEW_MOVE";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_view_move.png");
	
	private WritableImage cursorImage;
	private GraphicsCursor cursor;
	
	private Tool previousTool;
	
	public ToolViewMove(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
		this.cursorImage = ResourceLoader.loadImage("cursors/cursor_view_move.png");
	}

	public void setPreviousTool(Tool tool)
	{
		this.previousTool = tool;
	}
	
	public Tool getPreviousTool()
	{
		return previousTool;
	}
	
	private void moveCursor(MouseEvent event)
	{
		if(cursor == null)
			return;

		cursor.setX((int) (event.getX() - cursorImage.getWidth()/2));
		cursor.setY((int) (event.getY() - cursorImage.getHeight()/2));
		canvas.redraw();
	}
	
	private void createCursor(MouseEvent event)
	{
		double w = cursorImage.getWidth();
		double h = cursorImage.getHeight();
		cursor = new GraphicsImageCursor(cursorImage, event.getX() - w/2, event.getY() - h/2, w, h);
		canvas.redraw();
	}
	
	@Override
	public void draw(GraphicsContext gc)
	{
		if(cursor == null)
			return;
		
		cursor.draw(gc, null, 1);
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

	@Override
	public void unload() {}

	@Override
	public void mouseMoved(MouseEvent event)
	{
		moveCursor(event);
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		moveCursor(event);
	}
	
	@Override
	public void mouseDown(MouseEvent event)
	{
		if(cursor == null) createCursor(event);
		editor.getScrollPane().getController().setIsDown(true);
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		editor.getScrollPane().getController().setIsDown(false);
	}
	
	@Override
	public void mouseEntered(MouseEvent event)
	{
		if(cursor == null)
			createCursor(event);
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
}