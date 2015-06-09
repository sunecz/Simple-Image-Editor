/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import java.nio.IntBuffer;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import sune.apps.ie.image.ColorConverter;

public class ImageUtils
{
	public static WritableImage crop(Image image, int x, int y, int width, int height)
	{
		return new WritableImage(image.getPixelReader(), x, y, width, height);
	}
	
	public static WritableImage resize(Image image, int width, int height)
	{
		WritableImage wimg = new WritableImage(width, height);
		PixelWriter writer = wimg.getPixelWriter();
		PixelReader reader = image.getPixelReader();
		
		double pW = width / image.getWidth();
		double pH = height / image.getHeight();

		for(int y = 0; y < image.getHeight(); y++)
		{
			for(int x = 0; x < image.getWidth(); x++)
			{
				int argb = reader.getArgb(x, y);
				int nx = (int) (x*pW);
				int ny = (int) (y*pH);
				
				for(int h = 0; h < pH; h++)
					for(int w = 0; w < pW; w++)
						writer.setArgb(nx+w, ny+h, argb);
			}
		}
			
		return wimg;
	}
	
	public static WritableImage fillImage(Color color, int width, int height)
	{
		WritableImage image = new WritableImage(width, height);
		int[] pixels 		= new int[width*height];
		
		Arrays.fill(pixels, ColorConverter.FXColorToInt(color));
		image.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		
		return image;
	}
	
	public static WritableImage fillImage(Image image, int width, int height)
	{
		double backWidth 	= image.getWidth();
		double backHeight 	= image.getHeight();
		
		int rows = (int) Math.ceil(height / backHeight);
		int cols = (int) Math.ceil(width / backWidth);

		int rheight = (int) Math.ceil(height / (double) rows);
		int cwidth  = (int) Math.ceil(width / (double) cols);
		
		int intHeight 	= Math.max(1, (int) height);
		int intWidth	= Math.max(1, (int) width);
		
		WritableImage wimage = new WritableImage(intWidth, intHeight);
		PixelWriter writer	 = wimage.getPixelWriter();
		
		PixelFormat<IntBuffer> format	= PixelFormat.getIntArgbInstance();
		int[] buffer 		 			= new int[(int) (backWidth*backHeight*4)];

		image.getPixelReader().getPixels(0, 0, (int) backWidth, (int) backHeight, WritablePixelFormat.getIntArgbInstance(), buffer, 0, (int) backWidth);
		
		for(int y = 0; y < height; y += rheight)
			for(int x = 0; x < width; x += cwidth)
				writer.setPixels(x, y, (int) Math.ceil((x+cwidth) >= intWidth ? (intWidth-x) : cwidth),
								(int) Math.ceil((y+rheight) >= intHeight ? (intHeight-y) : rheight), format,
								buffer, 0, (int) backWidth);
		
		return wimage;
	}
	
	public static WritableImage copyImage(Image image)
	{
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		WritableImage wimg = new WritableImage(width, height);
		int[] pixels = new int[width*height];
		
		image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);
		wimg.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
		
		return wimg;
	}
	
	public static WritableImage flipImageHorizontally(Image image)
	{
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		WritableImage wimg = new WritableImage(width, height);
		int[] pixels = new int[width*height];
		int maxSize	 = pixels.length-1;
		
		image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);

		int[] flippedPixels = new int[pixels.length];
		for(int i = 0; i < height; i++)
			for(int k = 0; k < width; k++)
			{
				int index  = i*width+k;
				int index2 = Math.max(0, Math.min((i+1)*width-k-1, maxSize));
				
				flippedPixels[index] = pixels[index2];
			}
		
		wimg.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), flippedPixels, 0, width);
		return wimg;
	}
	
	public static WritableImage flipImageVertically(Image image)
	{
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		WritableImage wimg = new WritableImage(width, height);
		int[] pixels = new int[width*height];
		int maxSize	 = pixels.length-1;
		
		image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);

		int[] flippedPixels = new int[pixels.length];
		for(int k = 0; k < width; k++)
			for(int i = 0; i < height; i++)
			{
				int index  = i*width+k;
				int index2 = Math.max(0, Math.min((height-i-1)*width+k, maxSize));
				
				flippedPixels[index] = pixels[index2];
			}
		
		wimg.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), flippedPixels, 0, width);
		return wimg;
	}
	
	// Flip image anti-clockwise by 90° (to the left)
	public static WritableImage flipImageToLeft(Image image)
	{
		/*Width is height and height is width.*/
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		WritableImage wimg = new WritableImage(height, width);
		int[] pixels = new int[width*height];
		int maxSize	 = pixels.length-1;
		
		image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);

		int[] flippedPixels = new int[pixels.length];
		for(int k = 0; k < width; k++)
			for(int i = 0; i < height; i++)
			{
				int index  = i*width+k;
				int index2 = Math.max(0, Math.min(k*height+i, maxSize));
				
				flippedPixels[index2] = pixels[index];
			}
		
		wimg.getPixelWriter().setPixels(0, 0, height, width, PixelFormat.getIntArgbInstance(), flippedPixels, 0, height);
		return wimg;
	}
	
	// Flip image clockwise by 90° (to the right)
	public static WritableImage flipImageToRight(Image image)
	{
		/*Width is height and height is width.*/
		int width  = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		WritableImage wimg = new WritableImage(height, width);
		int[] pixels = new int[width*height];
		int maxSize	 = pixels.length-1;
		
		image.getPixelReader().getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);

		int[] flippedPixels = new int[pixels.length];
		for(int k = 0; k < width; k++)
			for(int i = 0; i < height; i++)
			{
				int index  = i*width+k;
				int index2 = Math.max(0, Math.min(k*height+(height-i-1), maxSize));
				
				flippedPixels[index2] = pixels[index];
			}
		
		wimg.getPixelWriter().setPixels(0, 0, height, width, PixelFormat.getIntArgbInstance(), flippedPixels, 0, height);
		return wimg;
	}
	
	public static WritableImage toWritableImage(Image image)
	{
		return copyImage(image);
	}
}