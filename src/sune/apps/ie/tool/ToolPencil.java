/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.scene.image.Image;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;

public class ToolPencil extends ToolBrush
{
	public static final String NAME  = "TOOL_PENCIL";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_pencil.png");

	public ToolPencil(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
	}
	
	@Override
	protected void drawPixels(int x, int y)
	{
		if(helper == null && !initHelper(true))
			return;
		
		if(selectedLayer.isVisible() && prevX > -1 && prevY > -1)
		helper.drawLine(prevX, prevY, x, y, helper.METHOD_PENCIL);
		
		prevX = x;
		prevY = y;
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