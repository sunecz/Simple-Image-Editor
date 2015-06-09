/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.util;

import java.awt.Color;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.image.Image;
import sune.apps.ie.registry.ResourceLoader;

public class Utils
{
	public static final Image APP_ICON = getIcon("icon.png");
	public static final String WEB_URL = "http://java.suneweb.net/simple_image_editor/";
	
	public static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(Exception ex) {}
	}
	
	public static <E> List<E> toList(E[] array)
	{
		List<E> list = new ArrayList<>();
		for(E e : array)
			list.add(e);
		
		return list;
	}
	
	public static boolean inRange(int val, int min, int max)
	{
		return val >= min && val <= max;
	}
	
	public static boolean inRange(double val, double min, double max)
	{
		return val >= min && val <= max;
	}
	
	public static int minMax(int val, int min, int max)
	{
		return Math.max(min, Math.min(val, max));
	}
	
	public static double minMax(double val, double min, double max)
	{
		return Math.max(min, Math.min(val, max));
	}
	
	public static boolean inRangeTolerancy(int val, int val2, int tolerancy)
	{
		return inRange(val, Math.max(0, val2-tolerancy), Math.min(val2+tolerancy, 255));
	}
	
	public static boolean inRangeTolerancy(double val, double val2, double tolerancy)
	{
		return inRange(val, Math.max(0, val2-tolerancy), Math.min(val2+tolerancy, 255));
	}
	
	public static boolean inRangeTolerancy2(int val, int val2, int tolerancy)
	{
		return inRange(val, Math.max(Integer.MIN_VALUE, val2-tolerancy), Math.min(val2+tolerancy, Integer.MAX_VALUE));
	}
	
	public static boolean checkColor(Color color0, Color color1, int tolerancy)
	{
		return (tolerancy == 0 ? color0.equals(color1) : 
			   (inRangeTolerancy(color0.getRed(), 	color1.getRed(), 	tolerancy) &&
				inRangeTolerancy(color0.getGreen(), color1.getGreen(), 	tolerancy) &&
				inRangeTolerancy(color0.getBlue(), 	color1.getBlue(), 	tolerancy) &&
				inRangeTolerancy(color0.getAlpha(), color1.getAlpha(), 	tolerancy)));
	}
	
	public static boolean checkColor(int color0, int color1, int tolerancy)
	{
		return Utils.inRangeTolerancy2(color0, color1, tolerancy);
	}
	
	public static double round(double value, int decimals)
	{
		double tenths = Math.pow(10, decimals);
		return Math.round(value * tenths) / tenths;
	}
	
	public static void openURL(String url)
	{
		try
		{
			(Desktop.getDesktop()).browse(new URI(url));
		}
		catch(Exception ex) {}
	}
	
	public static Color normalizeColor(Color color)
	{
		return color == null ? Color.WHITE : color;
	}
	
	public static int range(int val, int min, int max)
	{
		return Math.max(min, Math.min(val, max));
	}
	
	public static void run(Runnable runnable)
	{
		Platform.runLater(runnable);
	}
	
	public static Image getIcon(String path)
	{
		return ResourceLoader.loadImage(path);
	}
	
	public static <T> String implode(T[] array, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < array.length; i++)
			sb.append(array[i].toString()).append(i == array.length-1 ? "" : delimiter);
		
		return sb.toString();
	}
	
	public static int[] IntegerToInt(Integer[] array)
	{
		int[] newArray = new int[array.length];
		for(int i = 0; i < array.length; i++)
			newArray[i] = array[i];
		
		return newArray;
	}
	
	public static String getCurrentGraphicsPipeline()
	{
	    return com.sun.prism.GraphicsPipeline.getPipeline().getClass().getName();
	}
	
	public static String getURLContent(String urlPath, int timeout)
	{
		try
		{
			URL url 			= new URL(urlPath);
			URLConnection conn 	= url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			
			BufferedReader br 	= new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb 	= new StringBuilder();
			
			String line = "";
			while((line = br.readLine()) != null)
				sb.append(line).append("\n");

			br.close();
			return sb.toString();
		}
		catch(Exception ex) {}
		
		return null;
	}
	
	public static String getURLContent(String urlPath)
	{
		try
		{
			URL url 			= new URL(urlPath);
			URLConnection conn 	= url.openConnection();
			BufferedReader br 	= new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb 	= new StringBuilder();
			
			String line = "";
			while((line = br.readLine()) != null)
				sb.append(line).append("\n");

			br.close();
			return sb.toString();
		}
		catch(Exception ex) {}
		
		return null;
	}
}