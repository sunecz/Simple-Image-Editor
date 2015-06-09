/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.layer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Rotate;
import sune.apps.ie.graphics.ImageUtils;

public class ImageLayer extends Layer
{
	protected WritableImage image;
	
	public ImageLayer(double x, double y, double width, double height)
	{
		this(null, x, y, width, height);
	}
	
	public ImageLayer(WritableImage image, double x, double y, double width, double height)
	{
		super(x, y, width, height);
		this.image = image;
		this.setEditable(true);
	}
	
	@Override
	public void render(GraphicsContext gc)
	{
		if(image == null)
			return;
		
		if(angle > 0)
		{
			gc.save();
			
			Rotate rotate = new Rotate(angle, x+width/2, y+height/2);
			gc.transform(rotate.getMxx(), rotate.getMyx(),
						 rotate.getMxy(), rotate.getMyy(),
						 rotate.getTx(), rotate.getTy());
			
			gc.drawImage(getImageInUse(), (int) x, (int) y, (int) width, (int) height);
			gc.restore();
		}
		else
		{
			gc.drawImage(getImageInUse(), (int) x, (int) y, (int) width, (int) height);
		}
	}
	
	@Override
	public void resize(double width, double height)
	{
		super.setSize(width, height);
		image = ImageUtils.resize(image, (int) width, (int) height);
	}
	
	@Override
	public void scale(double scaleX, double scaleY)
	{
		super.scale(scaleX, scaleY);
		image = ImageUtils.resize(image, (int) width, (int) height);
	}
	
	@Override
	public void setMask(LayerMask mask)
	{
		super.setMask(mask);
		this.mask.setMaskedImage(image);
	}
	
	public void setImage(WritableImage image)
	{
		if(image == null)
			return;
		
		this.image = image;
		if(this.getMask() != null)
			this.getMask().setMaskedImage(image);
		
		this.setSize(image.getWidth(), image.getHeight());
	}
	
	public WritableImage getImage()
	{
		return image;
	}
	
	public WritableImage getImageInUse()
	{
		return mask != null ? mask.getMaskedImage() : image;
	}
	
	public void flipHorizontally()
	{
		setImage(ImageUtils.flipImageHorizontally(image));
		
		if(mask != null)
		{
			mask.setMaskImage(
				ImageUtils.flipImageHorizontally(mask.getMaskImage()));
			mask.setMaskedImage(
				ImageUtils.flipImageHorizontally(mask.getMaskedImage()));
		}
	}
	
	public void flipVertically()
	{
		setImage(ImageUtils.flipImageVertically(image));
		
		if(mask != null)
		{
			mask.setMaskImage(
				ImageUtils.flipImageVertically(mask.getMaskImage()));
			mask.setMaskedImage(
				ImageUtils.flipImageVertically(mask.getMaskedImage()));
		}
	}
	
	public void flipToLeft()
	{
		setImage(ImageUtils.flipImageToLeft(image));
		
		if(mask != null)
		{
			mask.setMaskImage(
				ImageUtils.flipImageToLeft(mask.getMaskImage()));
			mask.setMaskedImage(
				ImageUtils.flipImageToLeft(mask.getMaskedImage()));
		}
	}
	
	public void flipToRight()
	{
		setImage(ImageUtils.flipImageToRight(image));
		
		if(mask != null)
		{
			mask.setMaskImage(
				ImageUtils.flipImageToRight(mask.getMaskImage()));
			mask.setMaskedImage(
				ImageUtils.flipImageToRight(mask.getMaskedImage()));
		}
	}
}