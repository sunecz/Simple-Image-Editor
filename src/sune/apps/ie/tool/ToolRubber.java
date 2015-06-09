/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;

public class ToolRubber extends ToolBrush
{
	public static final String NAME  = "TOOL_RUBBER";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_rubber.png");

	public ToolRubber(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
		this.opacity	= 0;
		this.usedInMask = false;
	}
	
	@Override
	public void setColor(int color)
	{
		colorARGB = opacity << 24;
		
		if(helper != null)
		helper.setColor(colorARGB);
	}
	
	@Override
	protected void drawPixels(int x, int y)
	{
		if(helper == null && !initHelper(true))
			return;
		
		helper.setRubberMode(true);
		if(selectedLayer.isVisible() && prevX > -1 && prevY > -1 && !isInMask)
		helper.drawLine(prevX, prevY, x, y, helper.METHOD_PENCIL);
		
		prevX = x;
		prevY = y;
	}

	@Override
	public Pane init()
	{
		Pane pane = super.init();
		setOpacity(0);
		return pane;
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