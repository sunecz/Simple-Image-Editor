/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.component;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sune.apps.ie.Editor;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.util.Utils;

public class DraggableVBox extends VBox
{
	private double mouseX;
	private double mouseY;
	
	private int current  = -1;
	private int selected = -1;
	private int smallest = Integer.MAX_VALUE;
	private int biggest  = Integer.MIN_VALUE;
	private boolean dragged;
	
	private List<Integer> selection = new ArrayList<Integer>()
	{
		private static final long serialVersionUID = 120L;

		@Override
		public boolean add(Integer e)
		{
			if(!contains(e))
				return super.add(e);
			
			return false;
		}
	};
	
	public DraggableVBox(Editor editor)
	{
		super();
		
		addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseX 			= event.getX();
				mouseY 			= event.getY();
				
				int index = getSelectedNodeIndex(mouseX, mouseY);
				if(index < smallest) smallest = index;
				if(index > biggest)  biggest  = index;
				if(selected == -1)	 selected = index;
				
				if(event.isControlDown())
				{
					if(!selection.contains(index)) 	selection.add(index);
					else							selection.remove(new Integer(index));
				}
				
				if(event.isShiftDown())
				{
					for(int i = Math.min(selected, index); i <= Math.max(selected, index); i++)
					{
						selection.add(i);
						Node node = getChildren().get(i);
						VBox vbox = (VBox) node;
						
						vbox.setBackground(new Background(new BackgroundFill(
							Color.LIGHTBLUE, CornerRadii.EMPTY, new Insets(0))));
					}
				}
				
				if(!selection.contains(index) && !event.isControlDown() && !event.isShiftDown())
				{
					smallest = Integer.MAX_VALUE;
					biggest  = Integer.MIN_VALUE;

					selection.clear();
					selection.add(index);
				}

				selected = index;
				dragged  = false;
			}
		});
		
		addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				int layersCount = editor.getCanvas().getLayersManager().getLayers().size();
				int currentNode = getSelectedNodeIndex(event.getX(), event.getY());
				resetChildren(editor);

				current = Math.min(currentNode, layersCount-selection.size());
				for(int i = 0; i < selection.size(); i++)
				{
					Node node = getChildren().get(current+i);
					VBox vbox = (VBox) node;
					
					vbox.setBackground(new Background(new BackgroundFill(
						Color.LIGHTGREEN, CornerRadii.EMPTY, new Insets(0))));
				}
				
				dragged = true;
			}
		});
		
		addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				LayersManager layersManager = editor.getCanvas().getLayersManager();
				int layersCount 			= layersManager.getLayers().size();
				int index					= getSelectedNodeIndex(event.getX(), event.getY());
				
				if(!dragged && !event.isControlDown() && !event.isShiftDown())
				{
					smallest = Integer.MAX_VALUE;
					biggest  = Integer.MIN_VALUE;
					
					selection.clear();
					selection.add(index);
				}
				
				layersManager.getSelection().clear();
				
				for(int i : selection)
				layersManager.getSelection().add(layersCount-i-1);
 
				if(dragged && !selection.isEmpty())
				{
					int currentInd 	= layersCount-current-1;
					int[] layersInt	= new int[selection.size()];
					
					for(int i = 0; i < layersInt.length; i++)
					layersInt[i] = layersCount-selection.get(i)-1;
					
					layersManager.moveLayers(layersInt, currentInd);
					layersManager.selectLayer(currentInd);

					selection.clear();
					selection.add(current);
					
					current  = -1;
					smallest = Integer.MAX_VALUE;
					biggest  = Integer.MIN_VALUE;
					dragged  = false;
					
					editor.getCanvas().redraw();
					editor.reloadLayers();
				}
				
				resetChildren(editor);
			}
		});
	}
	
	private void resetChildren(Editor editor)
	{
		int layersCount = editor.getCanvas().getLayersManager().getLayers().size();
		
		for(int i = 0; i < getChildren().size(); i++)
		{
			Node node = getChildren().get(i);
			VBox vbox = (VBox) node;
			
			boolean layerSelected = editor.getCanvas().getLayersManager().getSelection().has(layersCount-i-1);
			Color backColor = layerSelected ? Color.LIGHTBLUE.brighter().brighter() : Color.WHITE;
			
			vbox.setBackground(new Background(new BackgroundFill(
				backColor, CornerRadii.EMPTY, new Insets(0))));
		}
	}
	
	private int getSelectedNodeIndex(double x, double y)
	{
		if(y < 0) return 0;

		int counter = 0;
		for(Node node : getChildren())
		{
			double nodeY = node.getLayoutY();
			double nodeH = ((VBox) node).getHeight();
			
			if(Utils.inRange(y, nodeY, nodeY+nodeH))
				break;
			
			counter++;
		}

		return Math.min(counter, getChildren().size()-1);
	}
	
	public List<Integer> getSelection()
	{
		return selection;
	}
}