/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.LayerMask;
import sune.apps.ie.selection.Selection;
import sune.apps.ie.tool.Tool;
import sune.apps.ie.util.Utils;

public class PixelHelper
{
	private ImageLayer layer;

	private WritableImage image;
	private PixelWriter writer;
	private PixelReader reader;
	
	private WritableImage mask;
	private PixelWriter maskPW;
	private PixelReader maskPR;

	private WritableImage maskedImage;
	private PixelWriter maskedImagePW;
	private PixelReader maskedImagePR;

	private Map<Integer, Integer> brushMap;
	
	private double width;
	private double height;
	
	private boolean rubberMode;
	private Set<Integer> drawn = new HashSet<>();
	
	private WritableImage originalImage;
	private PixelReader originalImagePR;
	
	private int colorARGB;
	private double stroke;
	private Tool tool;
	
	private int[][] alphaValues;
	private Selection selection;
	
	public PixelHelper(ImageLayer layer)
	{
		this.layer  = layer;
		
		this.image 	= layer.getImage();
		this.writer = image.getPixelWriter();
		this.reader = image.getPixelReader();

		this.width  = image.getWidth();
		this.height = image.getHeight();
		
		if(layer.getMask() != null)
		{
			LayerMask mask = layer.getMask();
			
			this.mask 	= mask.getMaskImage();
			this.maskPW = this.mask.getPixelWriter();
			this.maskPR = this.mask.getPixelReader();

			this.maskedImage 	= mask.getMaskedImage();
			this.maskedImagePW 	= maskedImage.getPixelWriter();
			this.maskedImagePR 	= maskedImage.getPixelReader();
		}
		
		this.originalImage 	 = ImageUtils.copyImage(image);
		this.originalImagePR = originalImage.getPixelReader();
		
		this.brushMap  = new HashMap<>();
		this.selection = layer.getSelection();
	}
	
	public void setMask(LayerMask mask)
	{
		if(mask != null)
		{
			this.mask 	= mask.getMaskImage();
			this.maskPW = this.mask.getPixelWriter();
			this.maskPR = this.mask.getPixelReader();
			
			this.maskedImage 	= mask.getMaskedImage();
			this.maskedImagePW 	= maskedImage.getPixelWriter();
		}
	}
	
	public void setRubberMode(boolean flag)
	{
		rubberMode = flag;
	}
	
	public void setTool(Tool tool)
	{
		this.tool = tool;
	}
	
	public void setColor(int argb)
	{
		this.colorARGB = argb;
		recalculateAlphaValues();
	}
	
	public void setStroke(double stroke)
	{
		this.stroke = stroke / 4;
		recalculateAlphaValues();
	}
	
	private void recalculateAlphaValues()
	{
		// ALPHA VALUE OF THE COLOR
		int caVal = (colorARGB >> 24) & 0xff; 
		int size  = (int) (2*stroke+1);
		
		alphaValues = new int[size][size];
		for(double j = -stroke; j <= stroke; j++)
		{
			for(double k = -stroke; k <= stroke; k++)
			{
				alphaValues[(int) (j+stroke)][(int) (k+stroke)] = (int) ((caVal *
					(1 - ((Math.sqrt(Math.pow(j, 2) + Math.pow(k, 2)) / stroke)))
				));
			}
		}
	}
	
	private double checkX(double x)
	{
		return Utils.minMax(x, 0, width-1);
	}
	
	private double checkY(double y)
	{
		return Utils.minMax(y, 0, height-1);
	}
	
	private boolean checkPositions(double x, double y)
	{
		return (Utils.inRange(x, 0, width) && Utils.inRange(y, 0, height)) &&
			   (selection == null || (selection != null && selection.contains((int) x, (int) y)));
	}
	
	public interface PixelDrawMethod
	{
		public void draw(double x, double y);
	}
	
	public void drawLine(double x0, double y0, double x1, double y1, PixelDrawMethod method)
	{
		// Width of the line
		double dx = Math.abs(x0 - x1);
		// Height of the line
		double dy = Math.abs(y0 - y1);
		
		/*If the line has no width and height,
		 *then just draw a dot at that place.*/
		if(dx == 0 && dy == 0)
		{
			method.draw(x0, y0);
			return;
		}
		
		/*If the line has no width or height,
		 *then just draw in one of the directions.*/
		if(dx == 0 || dy == 0)
		{
			for(double y = 0; dx == 0 ? y < dy : y <= dy; y++)
			{
				double vy = y0 < y1 ? y : -y;
				double pY = y0 + vy;
				
				for(double x = 0; dy == 0 ? x < dx : x <= dx; x++)
				{
					double vx = x0 < x1 ? x : -x;
					double pX = x0 + vx;
					
					method.draw(pX, pY);
				}
			}
			
			return;
		}
		
		double d = dx > dy ? dx / dy : dy / dx;
		/*If the line has some width and height,
		 *then draw in both directions.*/
		for(double y = 0; y <= dy; y++)
		{
			double vy = y0 < y1 ? y : -y;
			double pY = y0 + vy;
			
			for(double x = 0; x <= dx; x++)
			{
				double vx = x0 < x1 ? x : -x;
				double pX = x0 + vx;
				
				if((dx > dy && (x >= ((y - 1) * d) && x < (y * d))) ||
				   (dy > dx && (y >= ((x - 1) * d) && y < (x * d))) ||
				   (x == (y * d) && y == (x * d)))
				{
					method.draw(pX, pY);
				}
			}
		}
	}
	
	public PixelDrawMethod METHOD_BRUSH = new PixelDrawMethod()
	{
		@Override
		public void draw(double x, double y)
		{
			boolean hs = layer.getMask() != null;
			boolean ms = layer.isMaskSelected();
			selection  = layer.getSelection();
			
			for(double j = -stroke; j <= stroke; j++)
				for(double k = -stroke; k <= stroke; k++)
					if(tool.canDraw(k, j, stroke) && checkPositions(x+k, y+j))
					{
						int px = (int) checkX(x+k);
						int py = (int) checkY(y+j);
						int in = (int) (py*width+px);
						
						int rv 		= brushMap.getOrDefault(in, 0);
						int alpha 	= alphaValues[(int) (j+stroke)][(int) (k+stroke)];
						
						int finalAlpha = Math.max(alpha, (rv >> 24) & 0xff);
						int finalVargb = (colorARGB & 0x00ffffff) | (alpha << 24);
						int finalVal   = finalVargb;
						
						if(!drawn.contains(in))
						{
							finalVal = alphaCorrection(finalVargb, rv);
							drawn.add(in);
						}
						
						int valArgb = (finalVal & 0x00ffffff) | (finalAlpha << 24);
						rv = reader.getArgb(px, py);
						
						if(hs)
						{
							if(ms)
							{
								// ALPHA VALUE OF THE ORIGINAL PIXEL
								int av = (rv >> 24) & 0xff;
								// ALPHA VALUE TO SET
								int al = Math.min(av, alpha);
								
								maskPW.setArgb(px, py, (colorARGB & 0xff000000) | 0x00ffffff);
								maskedImagePW.setArgb(px, py, (rv & 0x00ffffff) | ((al & 0xff) << 24));
							}
							else
							{
								if(rubberMode)
								{
									int miRV = maskedImagePR.getArgb(px, py);
									
									// ALPHA VALUE OF THE ORIGINAL PIXEL
									int av = (miRV >> 24) & 0xff;
									// ALPHA VALUE TO SET
									int al = Math.min(av, alpha) << 24;
									
									// ALPHA-CORRECTED VALUE
									int val 	= alphaCorrection(al, 0x0);
									// THE FINAL VALUE TO SET
									int argbVal = (rv & 0x00ffffff) | (val & 0xff000000);
									
									writer.setArgb(px, py, argbVal);
									maskedImagePW.setArgb(px, py, argbVal);
								}
								else
								{
									// MASK PIXEL
									int mv  = maskPR.getArgb(px, py);
									// ALPHA VALUE OF THE MASK PIXEL
									int av 	= (mv >> 24) & 0xff;
									// ALPHA-CORRECTED VALUE
									int val = alphaCorrection(colorARGB, rv);
									
									writer.setArgb(px, py, val);
									maskedImagePW.setArgb(px, py, (val & 0x00ffffff) | (av << 24));
								}
							}
						}
						else
						{
							if(!(rubberMode && rv == 0x0))
							{
								if(rubberMode)
								{
									// ALPHA VALUE OF THE ORIGINAL PIXEL
									int av = (rv >> 24) & 0xff;
									// ALPHA VALUE TO SET
									int al = Math.min(av, alpha) << 24;
									
									// ALPHA-CORRECTED VALUE
									int val = alphaCorrection(al, 0x0);
									
									writer.setArgb(px, py, (rv & 0x00ffffff) | (val & 0xff000000));
								}
								else
								{
									brushMap.put(in, valArgb);
									writer.setArgb(px, py, alphaCorrection(
										valArgb, originalImagePR.getArgb(px, py)
									));
								}
							}
						}						
					}
		}
	};
	
	public PixelDrawMethod METHOD_PENCIL = new PixelDrawMethod()
	{
		@Override
		public void draw(double x, double y)
		{
			boolean hs = layer.getMask() != null;
			boolean ms = layer.isMaskSelected();
			selection  = layer.getSelection();
			
			for(double j = -stroke; j <= stroke; j++)
				for(double k = -stroke; k <= stroke; k++)
					if(tool.canDraw(k, j, stroke) && checkPositions(x+k, y+j))
					{
						int px = (int) checkX(x+k);
						int py = (int) checkY(y+j);
						int in = (int) (py*width+px);
						
						if(drawn.contains(in))
							continue;
						
						int rv = reader.getArgb(px, py);
						drawn.add(in);
						
						if(hs)
						{
							if(ms)
							{
								// ALPHA VALUE OF THE ORIGINAL PIXEL
								int av = (rv >> 24) & 0xff;
								// ALPHA VALUE OF THE COLOR
								int ca = (colorARGB >> 24) & 0xff;
								
								// ALPHA VALUE TO SET
								int alpha = Math.min(av, ca);
								
								maskPW.setArgb(px, py, (colorARGB & 0xff000000) | 0x00ffffff);
								maskedImagePW.setArgb(px, py, (rv & 0x00ffffff) | ((alpha & 0xff) << 24));
							}
							else
							{
								if(rubberMode)
								{
									int miRV = maskedImagePR.getArgb(px, py);
									
									// ALPHA VALUE OF THE ORIGINAL PIXEL
									int av = (miRV >> 24) & 0xff;
									// ALPHA VALUE OF THE COLOR
									int ca = (colorARGB >> 24) & 0xff;
									
									// ALPHA VALUE TO SET
									int alpha = Math.min(av, ca) << 24;
									
									// ALPHA-CORRECTED VALUE
									int val 	= alphaCorrection(alpha, 0x0);
									// THE FINAL VALUE TO SET
									int argbVal = (rv & 0x00ffffff) | (val & 0xff000000);
									
									writer.setArgb(px, py, argbVal);
									maskedImagePW.setArgb(px, py, argbVal);
								}
								else
								{
									// MASK PIXEL
									int mv  = maskPR.getArgb(px, py);
									// ALPHA VALUE OF THE MASK PIXEL
									int av 	= (mv >> 24) & 0xff;
									// ALPHA-CORRECTED VALUE
									int val = alphaCorrection(colorARGB, rv);
									
									writer.setArgb(px, py, val);
									maskedImagePW.setArgb(px, py, (val & 0x00ffffff) | (av << 24));
								}
							}
						}
						else
						{
							if(!(rubberMode && rv == 0x0))
							{
								if(rubberMode)
								{
									// ALPHA VALUE OF THE ORIGINAL PIXEL
									int av = (rv >> 24) & 0xff;
									// ALPHA VALUE OF THE COLOR
									int ca = (colorARGB >> 24) & 0xff;
									
									// ALPHA VALUE TO SET
									int alpha = Math.min(av, ca) << 24;
									
									// ALPHA-CORRECTED VALUE
									int val = alphaCorrection(alpha, 0x0);
									
									writer.setArgb(px, py, (rv & 0x00ffffff) | (val & 0xff000000));
								}
								else
								{
									writer.setArgb(px, py, alphaCorrection(colorARGB, rv));
								}
							}
						}
					}
		}
	};
	
	public void clear()
	{
		drawn.clear();
		brushMap.clear();
		
		// Reset buffers
		originalImage 	= ImageUtils.copyImage(image);
		originalImagePR = originalImage.getPixelReader();
	}

	public int alphaCorrection(int foreg, int backg)
	{
		double fa = ((foreg >> 24) & 0xff) / 255.0;
		if(fa >= 1.0) return foreg;
		double fr = ((foreg >> 16) & 0xff) / 255.0;
		double fg = ((foreg >> 8)  & 0xff) / 255.0;
		double fb = ((foreg)	   & 0xff) / 255.0;
		
		double ba = ((backg >> 24) & 0xff) / 255.0;
		if(ba <= 0.0) return foreg;
		double br = ((backg >> 16) & 0xff) / 255.0;
		double bg = ((backg >> 8)  & 0xff) / 255.0;
		double bb = ((backg)	   & 0xff) / 255.0;
		
		double r, g, b, a;
		int ia, ir, ig, ib;
		
		a = 1 - (1 - fa) * (1 - ba);
		r = fr * fa / a + br * ba * (1 - fa) / a;
		g = fg * fa / a + bg * ba * (1 - fa) / a;
		b = fb * fa / a + bb * ba * (1 - fa) / a;
		
		ia = (int) (a * 255) & 0xff;
		ir = (int) (r * 255) & 0xff;
		ig = (int) (g * 255) & 0xff;
		ib = (int) (b * 255) & 0xff;
		
		return (ia << 24) | (ir << 16) | (ig << 8) | ib;
	}
	
	public int averageColors(int... colors)
	{
		int avgA = 0,
			avgR = 0,
			avgG = 0,
			avgB = 0;
		
		boolean opaque = false;
		for(int i = 0; i < colors.length; i++)
		{
			int color = colors[i];
			
			int a = (color >> 24) & 0xff;
			int r = (color >> 16) & 0xff;
			int g = (color >> 8)  & 0xff;
			int b = (color) 	  & 0xff;

			if(i == 0 && a == 255)
				opaque = true;
			
			avgA += a;
			avgR += r;
			avgG += g;
			avgB += b;
		}

		int l = colors.length;
		return ((opaque ? 255 : (avgA / l)) << 24) |
			   ((avgR / l) << 16) |
			   ((avgG / l) << 8)  |
			   ((avgB / l));
	}
	
	public void setArgb(double x, double y, int argb)
	{
		writer.setArgb((int) checkX(x), (int) checkY(y), argb);
	}
	
	public int getArgb(double x, double y)
	{
		return reader.getArgb((int) checkX(x), (int) checkY(y));
	}
	
	public Color getColor(double x, double y)
	{
		int ix = (int) checkX(x);
		int iy = (int) checkY(y);
		
		return layer.getMask() != null ? maskedImagePR.getColor(ix, iy) : reader.getColor(ix, iy);
	}
	
	public WritableImage getImage()
	{
		return image;
	}
}