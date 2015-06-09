/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.image;

import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import sune.apps.ie.image.ColorConverter.HSLAColor;
import sune.apps.ie.image.ColorConverter.RGBAColor;
import sune.apps.ie.util.MathHelper;
import sune.apps.ie.util.Utils;

/**
 * IImage (or Improved Image) is a class that contains useful
 * methods and functions for manipulating with an image. It
 * provides universal functiontality for all the 1-32 bit images.
 * <br><br>
 * It offers functions like fast setting/getting image's pixels,
 * removing color channels, applying color adjustments (contrast,
 * brightness, hue, saturation, ...) and more.
 * <br><br>
 * When the object of this class is created, it automatically converts
 * the given image to the BufferedImage. Then it converts the image
 * of whatever type it is or whatever bit depth it has, to the array
 * of ints, where one item in that array represents one pixel on the
 * image in the ARGB format (8-bits for alpha channel, 8-bits for red
 * channel, 8-bits for green channel and 8-bits for blue channel).
 * <br><br>
 * <b>Version for WritableImage (JavaFX)</b>
 * 
 * @see WritableImage*/
public class IImage
{
	/**
	 * Buffered image object*/
	private WritableImage image;
	
	/**
	 * Width of the image*/
	private int width;
	/**
	 * Height of the image*/
	private int height;
	
	/**
	 * Stores all image's pixels*/
	private int[] pixels;
	
	private PixelReader reader;
	private PixelWriter writer;
	
	// MASKS
	private static final int MASK_CHANNEL_ARGB 	= 0x00000000;
	private static final int MASK_CHANNEL_ALPHA = 0xff000000;
	private static final int MASK_CHANNEL_RED 	= 0x00ff0000;
	private static final int MASK_CHANNEL_GREEN = 0x0000ff00;
	private static final int MASK_CHANNEL_BLUE 	= 0x000000ff;
	private static final int MASK_CHANNEL_RGB 	= 0x00ffffff;
	private static final int MASK_CHANNEL_AGB 	= 0xff00ffff;
	private static final int MASK_CHANNEL_ARB 	= 0xffff00ff;
	private static final int MASK_CHANNEL_ARG 	= 0xffffff00;
	private static final int MASK_CHANNEL_NO 	= 0xffffffff;
	
	/**
	 * Creates new instance of IImage
	 * @param image - the image object*/
	public IImage(Image image)
	{
		this.image 	= convertToWritableImage(image);
		this.width 	= (int) this.image.getWidth();
		this.height = (int) this.image.getHeight();
		
		this.reader = this.image.getPixelReader();
		this.writer = this.image.getPixelWriter();
	}

	/**
	 * Converts the given image to writable image
	 * @param image - the image object to convert
	 * @return The writable image object*/
	private WritableImage convertToWritableImage(Image image)
	{
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		pixels = new int[width*height];
		
		if(!(image instanceof WritableImage))
		{
			WritableImage wimg = new WritableImage(width, height);
			image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);
			wimg.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
			return wimg;
		}
		
		WritableImage wimg = (WritableImage) image;
		wimg.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);
		return wimg;
	}
	
	/**
	 * Formats the given value to fit into
	 * RGBA values' range (0-255).
	 * @param val - value to format
	 * @return The formatted value*/
	private int rangeRGBA(double val)
	{
		return (int) Math.max(0, Math.min(val, 255));
	}
	
	/**
	 * Formats the given value to fit into
	 * HSLA values' range (0-1).
	 * @param val - value to format
	 * @return The formatted value*/
	private float rangeHSLA(double val)
	{
		return (float) Math.max(0, Math.min(val, 1));
	}
	
	/**
	 * Resets the pixel into the image. When any change was
	 * done, the pixels will be rewritten in the image. The
	 * image then will contain all the changes made to it.*/
	public void resetPixels(boolean prealpha)
	{
		Utils.run(() ->
		{
			writer.setPixels(0, 0, width, height,
				prealpha ? PixelFormat.getIntArgbPreInstance() : PixelFormat.getIntArgbInstance(),
				pixels, 0, width);
		});
	}
	
	public final Filters filters = new Filters();
	
	public abstract class FilterAction
	{
		protected int index;
		public abstract void action(int[] pixels, int width, int height, int x, int y, int red, int green, int blue, int alpha);
		
		public void setIndex(int val)
		{
			index = val;
		}
		
		public void increase()
		{
			index++;
		}
	}
	
	public class Filters
	{
		public void filter(FilterAction filterAction)
		{
			for(int pixel = 0; pixel < pixels.length; pixel++)
			{
				int argb 	= pixels[pixel];
				int alpha 	= ((argb & MASK_CHANNEL_ALPHA) >> 24) & 0xff;
				int red 	= ((argb & MASK_CHANNEL_RED)   >> 16) & 0xff;
				int green 	= ((argb & MASK_CHANNEL_GREEN) >> 8)  & 0xff;
				int blue 	= ((argb & MASK_CHANNEL_BLUE)) 		  & 0xff;
				
				filterAction.action(pixels, width, height,
									pixel % width, (int) Math.floor(pixel / width),
									red, green, blue, alpha);
			}
		}
		
		public void boxBlur(int value)
		{
			float kernelValue = 1f / 9f;
			float[] kernel = new float[9];
			Arrays.fill(kernel, kernelValue);
			
			for(int i = 0; i < value; i++)
				kernelFilter(kernel, true);
		}
		
		public void motionBlur(int angle, int value)
		{
			angle %= 180;
			float n0 = (angle >= 0 	 && angle < 45  ? 1f / (45  - angle) : 0);
			float n1 = (angle >= 45  && angle < 90  ? 1f / (90  - angle) : 0);
			float n2 = (angle >= 90  && angle < 135 ? 1f / (135 - angle) : 0);
			float n3 = (angle >= 135 && angle < 180 ? 1f / (180 - angle) : 0);
			
			float[] kernel = new float[]
			{
				n3, n0, n1,
				n2,  0, n2,
				n1, n0, n3
			};
			
			for(int i = 0; i < value; i++)
				kernelFilter(kernel, true);
		}
		
		public void smooth()
		{
			kernelFilter(new float[]
			{
				1, 	1, 	1,
				1, 	2,  1,
				1, 	1, 	1
			}, true);
		}
		
		public void sharpen(int value)
		{
			float[] kernel = new float[]
			{
				0, -1, 0,
				-1, +5, -1,
				0, -1, 0
			};
			
			for(int i = 0; i < value; i++)
				kernelFilter(kernel, true);
		}
		
		public void unsharpMask()
		{
			float modifier = -1 / 256f;
			float n0 = 1 * modifier;
			float n1 = 4 * modifier;
			float n2 = 6 * modifier;
			float n3 = 16 * modifier;
			float n4 = 24 * modifier;
			float n5 = -476 * modifier;
			
			float[] kernel = new float[]
			{
				n0, n1, n2, n1, n0,
				n1, n3, n4, n3, n1,
				n2, n4, n5, n4, n2,
				n1, n3, n4, n3, n1,
				n0, n1, n2, n1, n0
			};
			
			kernelFilter(kernel, true);
		}
		
		public void edgeDetection()
		{
			kernelFilter(new float[]
			{
				-1, -1, -1,
				-1, +8, -1,
				-1, -1, -1
			}, false);
		}
		
		public void gaussianBlur2(int value)
		{
			float[] kernel = new float[]
			{
				1/16f, 2/16f, 1/16f,
				2/16f, 4/16f, 2/16f,
				1/16f, 2/16f, 1/16f
			};
			
			for(int i = 0; i < value; i++)
				kernelFilter(kernel, true);
		}
		
		public void pressUp()
		{
			kernelFilter(new float[]
			{
				-1, -1, -1,
				-1, +1, -1,
				-1, +3, +3
			}, true);
		}
		
		public void pressUpAndSmooth()
		{
			kernelFilter(new float[]
			{
				-1, -1, -1, -1, +2,
				-1, -1, -1, -1, +2,
				-1, +3, -1, +2, -1,
				+3, -2, -1, -1, +1,
				+3, +3, +3, -1, -1
			}, true);
		}
		
		// @author: Petr Cipra (Sune)
		public void kernelFilter(float[] kernel, boolean alphaChannel)
		{
			int[] temp  = new int[width*height];
			int ksize	= kernel.length;
			
			// KERNEL INDEXES
			int[] indexes 	= new int[ksize];
			int kernelRows 	= (int) Math.sqrt(ksize);
			int kernelCols 	= kernelRows;
			
			/*The middle row is the row, where the pixel for what
			 *we want to calculate the value, is. Even number of rows
			 *does not have only one middle row, but two. So the
			 *number of rows should not be even. In the case of even
			 *size of kernel, just resize the kernel array and set
			 *the values to 1, if the index of the value is outside
			 *of the previous kernel.*/
			if(kernelRows % 2 == 0)
			{
				kernelRows++;
				kernelCols++;
				
				float[] fKernel = new float[kernelRows*kernelCols];
				System.arraycopy(kernel, 0, fKernel, 0, kernel.length);
				
				for(int i = (fKernel.length - kernel.length); i < fKernel.length; i++)
					fKernel[i] = 1;
				
				kernel  = fKernel;
				indexes = new int[kernel.length];
			}
			
			/*These two numbers should not be full numbers (integers),
			 *but always decimal numbers, such as 1.5, 4.5, and so on.
			 *Also they should equal each other.*/
			double halfRowsLength = kernelRows / 2.0;
			double halfColsLength = kernelCols / 2.0;
			
			for(int i = 0, r = 0, c = 0; i < indexes.length; i++)
			{
				if(i != 0 && i % kernelCols == 0) r++;
				c = i % kernelCols;
				
				/*Creates the indexes of the kernel, so the final index of the pixel
				 *to take from, will be: px = index + kernel[i].
				 *
				 *The kernel of size 3x3 should look like this:
				 *
				 *	-width-1	-width	-width+1
				 *	-1			0		+1
				 *	+width-1	+width	+width+1
				 *
				 *So then, the pixels values (seperated in all channels RGB and A)
				 *will be taken from the indexes:
				 *	index-width-1, index-width, index-width+1, index-1, ...
				 *
				 *The final value of added values divided by the value of the kernel
				 *will be the value of the pixel on the given index.*/
				indexes[i] = (int) (Math.ceil(r - halfRowsLength)*width + Math.ceil(c - halfColsLength));
			}
			
			/*The kernel value that should be used for all the pixels
			 *in the image. The value is just a sum of all kernel's values.*/
			float kernelValue = 0;
			for(float val : kernel)
				kernelValue += val;
			
			if(kernelValue == 0)
				kernelValue = 1f;
			
			int size = pixels.length;
			for(int i = 0; i < size; i++)
			{
				/*The kernel value to be used on the current pixel.
				 *The value can change! When the pixel is located near
				 *the image's edges, the value can be smaller because
				 *it might take values from less pixels.*/
				float kVal = kernelValue;
				
				float alpha = 0, red = 0, green = 0, blue = 0;
				for(int k = 0; k < kernel.length; k++)
				{
					float kernelVal = kernel[k];
					int kIndex		= i + indexes[k];
					
					if(kIndex < 0 || kIndex >= size)
					{
						kVal -= kernelVal;
						continue;
					}
					
					int argb = pixels[kIndex];
					alpha 	+= ((argb >> 24) & 0xff) * kernelVal;
					red 	+= ((argb >> 16) & 0xff) * kernelVal;
					green 	+= ((argb >> 8)  & 0xff) * kernelVal;
					blue 	+= ((argb) 		 & 0xff) * kernelVal;
				}
				
				int ialpha 	= (int) (alpha 	/ kVal),
					ired 	= (int) (red 	/ kVal),
					igreen 	= (int) (green 	/ kVal),
					iblue 	= (int) (blue 	/ kVal);
				
				if(!alphaChannel)
					ialpha = 0xff;
				
				if(ialpha	> 0xff) ialpha 	= 0xff; else if(ialpha 	< 0x0) ialpha 	= 0x0;
				if(ired 	> 0xff) ired 	= 0xff; else if(ired 	< 0x0) ired 	= 0x0;
				if(igreen 	> 0xff) igreen 	= 0xff; else if(igreen 	< 0x0) igreen 	= 0x0;
				if(iblue 	> 0xff) iblue 	= 0xff; else if(iblue 	< 0x0) iblue 	= 0x0;
				
				temp[i] = ((ialpha 	& 0xff) << 24) |
						  ((ired 	& 0xff) << 16) |
						  ((igreen 	& 0xff) << 8)  |
						  ((iblue 	& 0xff));
			}
			
			pixels = temp;
			resetPixels(true);
		}
		
		public void gaussianBlur(int kernelSize)
		{
			if(kernelSize <= 0) return;
			int[] pxarr = new int[pixels.length];
			int ksize	= (int) Math.pow(2*kernelSize+1, 2);
			
			filter(new FilterAction()
			{
				@Override
				public void action(int[] pixels, int width, int height, int x, int y, int red, int green, int blue, int alpha)
				{
					int avgA = 0, avgR = 0, avgG = 0, avgB = 0;
					
					for(int j = -kernelSize; j <= kernelSize; j++)
					{
						for(int k = -kernelSize; k <= kernelSize; k++)
						{
							if(j == 0 && k == 0)
								continue;
							
							int ny = Math.max(0, Math.min(y+j, height-1));
							int nx = Math.max(0, Math.min(x+k, width-1));
							int in = ny*width+nx;
							
							int val = pixels[in];
							avgA += (val >> 24) & 0xff;
							avgR += (val >> 16) & 0xff;
							avgG += (val >> 8) 	& 0xff;
							avgB += (val) 		& 0xff;
						}
					}
					
					avgA = (int) (avgA /= ksize) & 0xff;
					avgR = (int) (avgR /= ksize) & 0xff;
					avgG = (int) (avgG /= ksize) & 0xff;
					avgB = (int) (avgB /= ksize) & 0xff;
					
					pxarr[index] = ((avgA) << 24) |	
								   ((avgR) << 16) |
					   			   ((avgG) << 8)  |
					   			   ((avgB));
					increase();
				}
			});
			
			pixels = pxarr;
			resetPixels(true);
		}
	}
	
	/**
	 * Applies a Image Matrix to a pixel's RGB values.
	 * @param matrix - the Image Matrix to apply*/
	public void applyRGBMatrix(ColorMatrix matrix)
	{
		for(int pixel = 0; pixel < pixels.length; pixel++)
		{
			int argb 	= pixels[pixel];
			int alpha 	= (argb >> 24) & 0xff;
			int red 	= (argb >> 16) & 0xff;
			int green 	= (argb >> 8)  & 0xff;
			int blue 	= (argb) 	   & 0xff;
			
			pixels[pixel] = ColorConverter.RGBAToInt(
				rangeRGBA(red*matrix.m00 + green*matrix.m01 + blue*matrix.m02 + alpha*matrix.m03 + matrix.m04),
				rangeRGBA(red*matrix.m10 + green*matrix.m11 + blue*matrix.m12 + alpha*matrix.m13 + matrix.m14),
				rangeRGBA(red*matrix.m20 + green*matrix.m21 + blue*matrix.m22 + alpha*matrix.m23 + matrix.m24),
				rangeRGBA(red*matrix.m30 + green*matrix.m31 + blue*matrix.m32 + alpha*matrix.m33 + matrix.m34)
			);
		}
		
		resetPixels(false);
	}
	
	/**
	 * Applies a Image Matrix to a pixel's HSL values.
	 * @param matrix - the Image Matrix to apply*/
	public void applyHSLMatrix(ColorMatrix matrix)
	{
		for(int pixel = 0; pixel < pixels.length; pixel++)
		{
			int argb 	= pixels[pixel];
			int alpha 	= (argb >> 24) & 0xff;
			int red 	= (argb >> 16) & 0xff;
			int green 	= (argb >> 8)  & 0xff;
			int blue 	= (argb) 	   & 0xff;

			HSLAColor color 	= ColorConverter.RGBAToHSLA(red, green, blue, alpha);
			float hue 			= color.hue;
			float saturation 	= color.saturation;
			float lightness 	= color.lightness;
			float falpha		= alpha / 255f;
			
			pixels[pixel] = ColorConverter.HSLAToInt(
				rangeHSLA(hue*matrix.m00 + saturation*matrix.m01 + lightness*matrix.m02 + falpha*matrix.m03 + matrix.m04),
				rangeHSLA(hue*matrix.m10 + saturation*matrix.m11 + lightness*matrix.m12 + falpha*matrix.m13 + matrix.m14),
				rangeHSLA(hue*matrix.m20 + saturation*matrix.m21 + lightness*matrix.m22 + falpha*matrix.m23 + matrix.m24),
				rangeHSLA(hue*matrix.m30 + saturation*matrix.m31 + lightness*matrix.m32 + falpha*matrix.m33 + matrix.m34)
			);
		}
		
		resetPixels(false);
	}
	
	/**
	 * Inverts the image.*/
	public void invert()
	{
		applyRGBMatrix(ColorMatrix.createMatrix
		(
			-1, 0, 	0, 	0, 	255,
			0, 	-1, 0, 	0, 	255,
			0, 	0, 	-1, 0, 	255,
			0, 	0, 	0, 	1, 	0
		));
	}
	
	/**
	 * Grayscale the image.*/
	public void grayscale()
	{
		applyRGBMatrix(ColorMatrix.createMatrix
		(
			0.299, 	0.587, 	0.114, 	0,	0,
			0.299, 	0.587, 	0.114, 	0,	0,
			0.299, 	0.587, 	0.114, 	0,	0,
			0, 		0, 		0, 		1, 	0
		));
	}
	
	/**
	 * Sets brightness of the image.
	 * @param brightness - the brightness value*/
	public void setBrightness(double brightness)
	{
		applyRGBMatrix(ColorMatrix.createMatrix
		(
			1, 	0, 	0, 	0, 	brightness,
			0, 	1, 	0, 	0, 	brightness,
			0, 	0, 	1, 	0, 	brightness,
			0, 	0, 	0, 	1, 	0
		));
	}
	
	/**
	 * Sets contrast of the image.
	 * @param contrast - the contrast value*/
	public void setContrast(double contrast)
	{
		for(int pixel = 0; pixel < pixels.length; pixel++)
		{
			int argb 	= pixels[pixel];
			int alpha 	= ((argb & MASK_CHANNEL_ALPHA) >> 24) & 0xff;
			int red 	= ((argb & MASK_CHANNEL_RED)   >> 16) & 0xff;
			int green 	= ((argb & MASK_CHANNEL_GREEN) >> 8)  & 0xff;
			int blue 	= ((argb & MASK_CHANNEL_BLUE)) 		  & 0xff;
			
			pixels[pixel] = ColorConverter.RGBAToInt(rangeRGBA(contrast*(red 	- 128) + 128),
									  				 rangeRGBA(contrast*(green 	- 128) + 128),
									  				 rangeRGBA(contrast*(blue 	- 128) + 128),
									  				 rangeRGBA(alpha));
		}
		
		resetPixels(false);
	}
	
	/**
	 * Sets gamma of the image.
	 * @param gamma - the gamma value*/
	public void setGamma(double gamma)
	{
		double factor = 1.0 / gamma;
		
		for(int pixel = 0; pixel < pixels.length; pixel++)
		{
			int argb 	= pixels[pixel];
			int alpha 	= ((argb & MASK_CHANNEL_ALPHA) >> 24) & 0xff;
			int red 	= ((argb & MASK_CHANNEL_RED)   >> 16) & 0xff;
			int green 	= ((argb & MASK_CHANNEL_GREEN) >> 8)  & 0xff;
			int blue 	= ((argb & MASK_CHANNEL_BLUE)) 		  & 0xff;
			
			pixels[pixel] = ColorConverter.RGBAToInt(rangeRGBA(MathHelper.fastPow(red 	/ 255.0, factor) * 255),
					  				  				 rangeRGBA(MathHelper.fastPow(green / 255.0, factor) * 255),
					  				  				 rangeRGBA(MathHelper.fastPow(blue  / 255.0, factor) * 255),
					  				  				 rangeRGBA(alpha));
		}
		
		resetPixels(false);
	}
	
	/**
	 * Sets alpha of the image.
	 * @param alpha - the alpha value*/
	public void setAlpha(int alpha)
	{
		for(int pixel = 0; pixel < pixels.length; pixel++)
			pixels[pixel] = pixels[pixel] & MASK_CHANNEL_RGB | (alpha << 24);
		
		resetPixels(false);
	}
	
	/**
	 * Sets transparency of the image.
	 * @param transparency - the transparency value*/
	public void setTransparency(int transparency)
	{
		double factor = 1 - (transparency / 255.0);
		for(int pixel = 0; pixel < pixels.length; pixel++)
		{
			int argb;
			if((argb = pixels[pixel]) != MASK_CHANNEL_ARGB)
				pixels[pixel] = (argb & MASK_CHANNEL_RGB) |
					((int) ((((argb & MASK_CHANNEL_ALPHA) >> 24) & 0xff) * factor) << 24);
		}
		
		resetPixels(false);
	}
	
	/**
	 * Sets hue of the image.
	 * @param value - the hue value*/
	public void setHue(float value)
	{
		applyHSLMatrix(ColorMatrix.createMatrix
		(
			value, 	0, 	0, 	0, 	0,
			0, 		1, 	0, 	0, 	0,
			0, 		0, 	1, 	0, 	0,
			0, 		0, 	0, 	1, 	0
		));
	}
	
	/**
	 * Sets saturation of the image.
	 * @param value - the saturation value*/
	public void setSaturation(float value)
	{
		applyHSLMatrix(ColorMatrix.createMatrix
		(
			1, 	0, 		0, 	0, 	0,
			0, 	value, 	0, 	0, 	0,
			0, 	0, 		1, 	0, 	0,
			0, 	0, 		0, 	1, 	0
		));
	}

	/**
	 * Sets lightness of the image.
	 * @param value - the lightness value*/
	public void setLightness(float value)
	{
		applyHSLMatrix(ColorMatrix.createMatrix
		(
			1, 	0, 	0, 		0, 	0,
			0, 	1, 	0, 		0, 	0,
			0, 	0, 	value, 	0, 	0,
			0, 	0, 	0, 		1, 	0
		));
	}
	
	/**
	 * Adds RGB values to the image
	 * @param red 	- the red value
	 * @param green - the green value
	 * @param blue 	- the blue value*/
	public void addRGB(int red, int green, int blue)
	{
		applyRGBMatrix(ColorMatrix.createMatrix
		(
			1, 	0, 	0, 	0, 	red,
			0, 	1, 	0, 	0, 	green,
			0, 	0, 	1, 	0, 	blue,
			0, 	0,	0,	1,	0
		));
	}
	
	/**
	 * Adds HSL values to the image
	 * @param hue 			- the hue value
	 * @param saturation 	- the saturation value
	 * @param lightness 	- the lightness value*/
	public void addHSL(float hue, float saturation, float lightness)
	{
		applyHSLMatrix(ColorMatrix.createMatrix
		(
			1, 	0, 	0, 	0, 	hue,
			0, 	1, 	0, 	0, 	saturation,
			0, 	0, 	1, 	0, 	lightness,
			0, 	0,	0,	1,	0
		));
	}
	
	public void applyMask(int mask)
	{
		for(int pixel = 0; pixel < pixels.length; pixel++)
			pixels[pixel] = pixels[pixel] & mask;	
		
		resetPixels(false);
	}

	/**
	 * Removes a color channel from the image
	 * @param channel - the channel to remove*/
	public void removeChannel(ColorChannel channel)
	{
		applyMask((channel == ColorChannel.ALPHA  ? MASK_CHANNEL_RGB 	:
				  (channel == ColorChannel.RED 	  ? MASK_CHANNEL_AGB 	:
				  (channel == ColorChannel.GREEN  ? MASK_CHANNEL_ARB 	:
				  (channel == ColorChannel.BLUE   ? MASK_CHANNEL_ARG 	:
				  (channel == ColorChannel.RGB 	  ? MASK_CHANNEL_ALPHA 	:
				  (channel == ColorChannel.RGBA   ? MASK_CHANNEL_ARGB 	:
				  (MASK_CHANNEL_NO))))))));
	}
	
	/**
	 * Sets pixel on the given coordinates
	 * @param x 	- the x-coordinates
	 * @param y 	- the y-coordinates
	 * @param color - the color object to set*/
	public void setPixel(int x, int y, Color color)
	{
		setPixel(x, y, rangeRGBA(color.getRed()*255), rangeRGBA(color.getGreen()*255),
				 rangeRGBA(color.getBlue()*255), rangeRGBA(color.getOpacity()*255));
	}
	
	/**
	 * Sets pixel on the given coordinates
	 * @param x 	- the x-coordinates
	 * @param y 	- the y-coordinates
	 * @param red 	- the red value
	 * @param green - the green value
	 * @param blue 	- the blue value*/
	public void setPixel(int x, int y, int red, int green, int blue)
	{
		setPixel(x, y, red, green, blue, 255);
	}
	
	/**
	 * Sets pixel on the given coordinates
	 * @param x 			- the x-coordinates
	 * @param y 			- the y-coordinates
	 * @param hue 			- the hue value
	 * @param saturation 	- the saturation value
	 * @param lightness 	- the lightness value*/
	public void setPixel(int x, int y, float hue, float saturation, float lightness)
	{
		setPixel(x, y, hue, saturation, lightness, 1f);
	}
	
	/**
	 * Sets pixel on the given coordinates
	 * @param x 			- the x-coordinates
	 * @param y 			- the y-coordinates
	 * @param hue 			- the hue value
	 * @param saturation 	- the saturation value
	 * @param lightness 	- the lightness value
	 * @param alpha			- the alpha value*/
	public void setPixel(int x, int y, float hue, float saturation, float lightness, float alpha)
	{
		RGBAColor color = ColorConverter.HSLAToRGBA(hue, saturation, lightness, alpha);
		setPixel(x, y, color.red, color.green, color.blue, color.alpha);
	}
	
	public int getPixelIndex(int x, int y)
	{
		return y*width + x;
	}
	
	/**
	 * Sets pixel on the given coordinates
	 * @param x 	- the x-coordinates
	 * @param y 	- the y-coordinates
	 * @param red 	- the red value
	 * @param green - the green value
	 * @param blue 	- the blue value
	 * @param alpha	- the alpha value*/
	public void setPixel(int x, int y, int red, int green, int blue, int alpha)
	{
		setPixel(x, y, ColorConverter.RGBAToInt(red, green, blue, alpha));
	}
	
	public void setPixel(int x, int y, int argb)
	{
		writer.setArgb(x, y, pixels[getPixelIndex(x, y)] = argb);
	}

	/**
	 * Gets ARGB value of the pixel on the given coordinates
	 * @param x - the x-coordinates
	 * @param y - the y-coordinates
	 * @return The ARGB value of the pixel*/
	public int getPixelInt(int x, int y)
	{
		return reader.getArgb(x, y);
	}
	
	/**
	 * Gets color of the pixel on the given coordinates
	 * @param x - the x-coordinates
	 * @param y - the y-coordinates
	 * @return The color of the pixel*/
	public Color getPixelColor(int x, int y)
	{
		return reader.getColor(x, y);
	}
	
	/**
	 * Gets the array of integers (in ARGB format)
	 * of all pixels.
	 * @return The array of integers*/
	public int[] getPixels()
	{
		return pixels;
	}
	
	/**
	 * Gets the width of the image
	 * @return The width of the image*/
	public int getWidth()
	{
		return width;
	}
	
	/**
	 * Gets the height of the image
	 * @return The height of the image*/
	public int getHeight()
	{
		return height;
	}
	
	/**
	 * Gets the writable image object
	 * @return The writable image object*/
	public WritableImage getImage()
	{
		return image;
	}
}
