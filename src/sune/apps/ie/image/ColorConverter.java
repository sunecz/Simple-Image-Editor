/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.image;

import java.awt.Color;

/**
 * Helps with converting colors and contains
 * some useful classes and methods.*/
public class ColorConverter
{
	/**
	 * Represents RGBA color and contains all
	 * RGBA values.*/
	public static class RGBAColor
	{
		/**
		 * Red value of color*/
		public final int red;
		/**
		 * Green value of color*/
		public final int green;
		/**
		 * Blue value of color*/
		public final int blue;
		/**
		 * Alpha value of color*/
		public final int alpha;
		
		/**
		 * Creates new instance of RGBA Color
		 * @param red 	- red value of color
		 * @param green - green value of color
		 * @param blue 	- blue value of color
		 * @param alpha - alpha value of color*/
		public RGBAColor(int red, int green, int blue, int alpha)
		{
			this.red 	= red;
			this.green 	= green;
			this.blue 	= blue;
			this.alpha 	= alpha;
		}
	}
	
	/**
	 * Represents HSLA color and contains all
	 * HSLA values.*/
	public static class HSLAColor
	{
		/**
		 * Hue value of color*/
		public final float hue;
		/**
		 * Saturation value of color*/
		public final float saturation;
		/**
		 * Lightness value of color*/
		public final float lightness;
		/**
		 * Alpha value of color*/
		public final float alpha;
		
		/**
		 * Creates new instance of HSLA Color
		 * @param hue 			- hue value of color
		 * @param saturation 	- saturation value of color
		 * @param lightness 	- lightness value of color
		 * @param alpha 		- alpha value of color*/
		public HSLAColor(float hue, float saturation, float lightness, float alpha)
		{
			this.hue 		= hue;
			this.saturation = saturation;
			this.lightness 	= lightness;
			this.alpha 		= alpha;
		}
	}

	/**
	 * Converts RGBA color to integer
	 * @param red 	- the red value of color
	 * @param green - the green value of color
	 * @param blue 	- the blue value of color
	 * @param alpha	- the alpha value of color
	 * @return Integer value of RGBA color*/
	public static int RGBAToInt(int red, int green, int blue, int alpha)
	{
        return ((alpha 	& 0xff) << 24) 	|
        	   ((red 	& 0xff) << 16) 	|
        	   ((green 	& 0xff) << 8) 	|
        	   ((blue 	& 0xff));
	}
	
	/**
	 * Converts HSLA color to integer
	 * @param hue 			- the hue value of color
	 * @param saturation 	- the saturation value of color
	 * @param lightness 	- the lightness value of color
	 * @param alpha			- the alpha value of color
	 * @return Integer value of HSLA color*/
	public static int HSLAToInt(float hue, float saturation, float lightness, float alpha)
	{
		RGBAColor color = HSLAToRGBA(hue, saturation, lightness, alpha);
		return RGBAToInt(color.red, color.green, color.blue, color.alpha);
	}
	
	/**
	 * Converts RGBA color to HSLA Color
	 * @param red 	- the red value of color
	 * @param green - the green value of color
	 * @param blue 	- the blue value of color
	 * @param alpha	- the alpha value of color
	 * @return Converted HSLA Color object*/
	public static HSLAColor RGBAToHSLA(int red, int green, int blue, int alpha)
	{
		float r = red 	/ 255f;
		float g = green / 255f;
		float b = blue 	/ 255f;
		float a = alpha / 255f;

		float min = Math.min(r, Math.min(g, b));
		float max = Math.max(r, Math.max(g, b));

		float h = 0;
		float l = (max + min) / 2;
		float s = 0;
		
		if(max == min) 		h = 0;
		else if(max == r) 	h = (((g - b) / (max - min) / 6f) + 1) % 1;
		else if(max == g) 	h = (((b - r) / (max - min) / 6f) + 1f/3f);
		else if(max == b) 	h = (((r - g) / (max - min) / 6f) + 2f/3f);

		if(max == min) 		s = 0;
		else if (l <= .5f)	s = (max - min) / (max + min);
		else 				s = (max - min) / (2 - max - min);

		return new HSLAColor(h, s, l, a);
	}

	/**
	 * Converts HSLA color to RGBA Color
	 * @param hue 			- the hue value of color
	 * @param saturation 	- the saturation value of color
	 * @param lightness 	- the lightness value of color
	 * @param alpha			- the alpha value of color
	 * @return Converted RGBA Color object*/
	public static RGBAColor HSLAToRGBA(float hue, float saturation, float lightness, float alpha)
	{
		float q = 0;

		if(lightness < 0.5) q = lightness * (1 + saturation);
		else 				q = (lightness + saturation) - (saturation * lightness);

		float p = 2 * lightness - q;
		int r = (int) (255 * Math.max(0, HueToRGB(p, q, hue + (1.0f / 3.0f))));
		int g = (int) (255 * Math.max(0, HueToRGB(p, q, hue)));
		int b = (int) (255 * Math.max(0, HueToRGB(p, q, hue - (1.0f / 3.0f))));

		return new RGBAColor(r, g, b, (int) (alpha * 255));
	}

	/**
	 * Converts hue value to one of RGB value
	 * @param p - modified lightness value
	 * @param q - modified saturation value
	 * @param h - modified hue value
	 * @return One of RGB value as float in range 0-1.*/
	private static float HueToRGB(float p, float q, float h)
	{
		if (h < 0) 		h++;
		if (h > 1) 		h--;
		
		if (6 * h < 1)	return p + ((q - p) * 6 * h);
		if (2 * h < 1)	return q;
		if (3 * h < 2)	return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));

   		return p;
	}
	
	public static HSLAColor createHSLAColor(int hue, int saturation, int lightness, int alpha)
	{
		return new HSLAColor(hue / 360f, saturation / 100f, lightness / 100f, alpha / 255f);
	}
	
	public static Color HSLAToColor(HSLAColor color)
	{
		return new Color(HSLAToInt(color.hue, color.saturation, color.lightness, color.alpha));
	}
	
	public static Color RGBAToColor(RGBAColor color)
	{
		return new Color(color.red, color.green, color.blue, color.alpha);
	}
	
	public static RGBAColor IntToRGBA(int argb)
	{
		return new RGBAColor((argb >> 16) & 0xff,
							 (argb >> 8)  & 0xff,
							 (argb) 	  & 0xff,
							 (argb >> 24) & 0xff);
	}
	
	public static HSLAColor IntToHSLA(int argb)
	{
		RGBAColor color = IntToRGBA(argb);
		return RGBAToHSLA(color.red, color.green, color.blue, color.alpha);
	}
	
	public static RGBAColor ColorToRGBA(Color color)
	{
		return IntToRGBA(ColorToInt(color));
	}
	
	public static HSLAColor ColorToHSLA(Color color)
	{
		return IntToHSLA(ColorToInt(color));
	}
	
	public static int ColorToInt(Color color)
	{
		return color.getRGB();
	}
	
	public static javafx.scene.paint.Color IntToFXColor(int argb)
	{
		RGBAColor color = IntToRGBA(argb);
		return new javafx.scene.paint.Color(
			color.red / 255.0, color.green / 255.0,
			color.blue / 255.0, color.alpha / 255.0
		);
	}
	
	public static int FXColorToInt(javafx.scene.paint.Color color)
	{
		return RGBAToInt(
			(int) (color.getRed()*255),  (int) (color.getGreen()*255),
			(int) (color.getBlue()*255), (int) (color.getOpacity()*255)
		);
	}

	public static String toReadableString(javafx.scene.paint.Color color)
	{
		StringBuilder sb = new StringBuilder();
		return sb.append("R: ").append((int) (color.getRed()*255)).append(", G: ")
			   .append((int) (color.getGreen()*255)).append(", B: ").append((int) (color.getBlue()*255))
			   .append(", A: ").append((int) (color.getOpacity()*255)).toString();
	}
}
