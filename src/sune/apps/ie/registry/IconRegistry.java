/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.registry;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;

public class IconRegistry
{
	public static class Icon extends Image
	{
		private String path;
		
		public Icon(String path)
		{
			this(PathSystem.ICONS_FOLDER + path, false);
		}
		
		public Icon(String path, boolean customFolder)
		{
			super(path);
			this.path = path;
		}
		
		public Background getBackground()
		{
			return new Background(new BackgroundImage(this, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null));
		}
		
		public String getPath()
		{
			return path;
		}
	}

	public static final Icon ICON_LAYER_VISIBLE 		= loadIcon("layer_visible.png");
	public static final Icon ICON_LAYER_INVISIBLE 		= loadIcon("layer_invisible.png");
	public static final Icon ICON_LAYER_CCVISIBILITY 	= loadIcon("layer_cannot_change_visiblity.png");
	
	public static Icon loadIcon(String path)
	{
		return new Icon(path);
	}
}