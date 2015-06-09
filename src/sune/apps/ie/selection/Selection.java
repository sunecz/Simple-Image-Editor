/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sune.apps.ie.layer.Layer;
import sune.apps.ie.util.Utils;

public class Selection
{
	private Set<Integer> pixels  = new HashSet<>();
	private Map<Integer, List<LineSegment>> segments
		= new HashMap<>();
	private Layer layer;

	public Selection() {}
	public Selection(Layer layer)
	{
		this.layer = layer;
	}
	
	public Layer getBoundLayer()
	{
		return layer;
	}
	
	public void bindToLayer(Layer layer)
	{
		this.layer = layer;
	}
	
	private boolean checkPositions(int x, int y)
	{
		return x >= 0 && x < layer.getWidth(1) &&
			   y >= 0 && y < layer.getHeight(1);
	}
	
	private int getPixelIndex(int x, int y)
	{
		return (int) (y*layer.getWidth(1)+x);
	}
	
	public void add(int x, int y)
	{
		if(!checkPositions(x, y))
			return;
		
		pixels.add(getPixelIndex(x, y));
	}
	
	public void add(int index)
	{
		if(!(index > 0 && index < layer.getWidth(1)*layer.getHeight(1)))
			return;
		
		pixels.add(index);
	}
	
	public void addRegion(int x, int y, int width, int height)
	{
		for(int j = y; j <= y+height; j++)
			for(int k = x; k <= x+width; k++)
				add(k, j);
	}
	
	public void remove(int x, int y)
	{
		if(!checkPositions(x, y))
			return;
		
		pixels.remove(new Integer(getPixelIndex(x, y)));
	}
	
	@Deprecated
	public boolean has(int index)
	{
		return pixels.contains(index);
	}
	
	public boolean has(int x, int y)
	{
		return pixels.contains(getPixelIndex(x, y));
	}
	
	public void invert()
	{
		Set<Integer> newSet = new HashSet<>();
		for(int y = 0; y < layer.getHeight(1); y++)
			for(int x = 0; x < layer.getWidth(1); x++)
			{
				int index = getPixelIndex(x, y);
				if(!pixels.contains(index))
					newSet.add(index);
			}
		
		pixels = newSet;
	}
	
	public void clear()
	{
		pixels.clear();
		segments.clear();
	}
	
	public void completeClear()
	{
		clear();
		layer = null;
	}
	
	public boolean isEmpty()
	{
		return pixels.isEmpty();
	}
	
	public int[] getData()
	{
		return Utils.IntegerToInt(pixels.toArray(new Integer[pixels.size()]));
	}
	
	public static class LineSegment
	{
		public int x;
		public int length;
		
		public LineSegment(int x, int length)
		{
			this.x 		= x;
			this.length = length;
		}
	}
	
	public void prepare()
	{
		if(pixels.isEmpty())
			return;
		
		double layerWidth  = layer.getWidth(1);
		double layerHeight = layer.getHeight(1);
		
		int width  = (int) layerWidth;
		int height = (int) layerHeight;
		segments = new HashMap<>();

		int[] data 		  = getData();
		int[][] tempLines = new int[height][width];
		
		for(int i = 0; i < data.length; i++)
		{
			int index 	= data[i];
			int x 		= index % width;
			int y 		= (int) Math.floor(index / width);
			
			tempLines[y][x] = 1;
		}
		
		int segmentIndex  = -1;
		int segmentLength = -1;
		
		for(int y = 0; y < tempLines.length; y++)
		{
			int[] tempCols = tempLines[y];
			for(int x = 0; x < tempCols.length; x++)
			{
				if(tempCols[x] == 1 && x != tempCols.length-1)
				{
					if(segmentIndex == -1)
					{
						segmentIndex  = x;
						segmentLength = 1;
					}
					else
					{
						segmentLength++;
					}
				}
				else
				{
					if(segmentIndex != -1)
					{
						// CREATE THE LINE SEGMENT
						LineSegment segment = new LineSegment(
							segmentIndex, segmentLength);
						
						segmentIndex  = -1;
						segmentLength = -1;
						
						List<LineSegment> lineSegments = segments.get(y);
						if(lineSegments == null)
							lineSegments = new ArrayList<>();
						
						lineSegments.add(segment);
						segments.put(y, lineSegments);
					}
				}
			}
		}
	}
	
	public boolean contains(int x, int y)
	{
		return pixels.contains(getPixelIndex(x, y));
	}
	
	public Map<Integer, List<LineSegment>> getLineSegments()
	{
		return segments;
	}
}