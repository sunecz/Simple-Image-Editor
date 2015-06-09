/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.layer;

import java.util.ArrayList;
import java.util.List;

public class LayersManager
{
	private List<Layer> layers;
	private Selection selection;
	
	public static class Selection
	{
		private List<Integer> items
			= new ArrayList<>();
		
		public void only(int index)
		{
			clear();
			add(index);
		}
		
		public void add(int index)
		{
			items.add(index);
		}
		
		public void remove(int index)
		{
			if(index > -1 && index < items.size())
			items.remove(index);
		}
		
		public boolean has(int index)
		{
			return items.contains(index);
		}
		
		public int getFirst()
		{
			return !isEmpty() ? items.get(0) : -1;
		}
		
		public int getLast()
		{
			return items.get(items.size()-1);
		}
		
		public void clear()
		{
			items.clear();
		}
		
		public boolean isEmpty()
		{
			return items.isEmpty();
		}
	}
	
	public LayersManager()
	{
		layers 		= new ArrayList<>();
		selection	= new Selection();
	}
	
	public void selectLayer(int index)
	{
		selection.only(index);
	}
	
	public void selectLastLayer()
	{
		selection.only(layers.size()-1);
	}
	
	public void moveLayer(int from, int to)
	{
		Layer layer = layers.get(from);
		
		layers.remove(from);
		layers.add(to, layer);
	}
	
	public void moveLayers(int[] from, int to)
	{
		List<Layer> listLayers = new ArrayList<>(from.length);
		for(int index : from) listLayers.add(layers.get(index));
		for(int index : from) layers.remove(index);
		
		for(int i = listLayers.size()-1; i > -1; i--)
			layers.add(to-i, listLayers.get(i));
	}
	
	public void addLayer(Layer layer)
	{
		layers.add(layer);
		selection.only(layers.size()-1);
	}
	
	public void removeLayer(int index)
	{
		selection.remove(index);
		layers.remove(index);
	}
	
	public void removeLayer(Layer layer)
	{
		int index = layers.indexOf(layer);
		
		layers.remove(layer);
		if(selection.has(index))
			selection.remove(index);
	}
	
	public Layer getLayer(int index)
	{
		return layers.get(index);
	}
	
	public Layer getSelectedLayer()
	{
		int first;
		if(!(!selection.isEmpty() && !layers.isEmpty() &&
		   (first = selection.getFirst()) > -1 && layers.size() > first))
			return null;
		
		return layers.get(first);
	}
	
	public int getSelectedLayerIndex()
	{
		return selection.getFirst();
	}
	
	public List<Layer> getLayers()
	{
		return layers;
	}
	
	public void clear()
	{
		layers.clear();
		selection.clear();
	}

	public boolean isEmpty()
	{
		return layers.isEmpty();
	}
	
	public Selection getSelection()
	{
		return selection;
	}
}