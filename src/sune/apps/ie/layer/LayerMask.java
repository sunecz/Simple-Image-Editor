/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.layer;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sune.apps.ie.graphics.ImageUtils;

public class LayerMask
{
	public static final int MASK_COLOR_INCLUDE = 0xffffffff;
	public static final int MASK_COLOR_EXCLUDE = 0x00000000;
	
	protected ImageLayer layer;
	protected int width;
	protected int height;
	
	protected WritableImage image;
	protected WritableImage maskedImage;

	public LayerMask(ImageLayer layer)
	{
		this.width  = (int) layer.getWidth(1);
		this.height = (int) layer.getHeight(1);
		this.image  = new WritableImage(width, height);
		
		this.image = ImageUtils.fillImage(Color.WHITE, width, height);
		setMaskedImage(layer.getImage());
	}
	
	public void setMaskImage(WritableImage image)
	{
		this.image 	= ImageUtils.copyImage(image);
		this.width 	= (int) image.getWidth();
		this.height = (int) image.getHeight();
	}
	
	public void setMaskedImage(WritableImage image)
	{
		this.maskedImage = ImageUtils.copyImage(image);
		this.width 		 = (int) image.getWidth();
		this.height 	 = (int) image.getHeight();
	}

	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}

	public WritableImage getMaskImage()
	{
		return image;
	}
	
	public WritableImage getMaskedImage()
	{
		return maskedImage;
	}
}