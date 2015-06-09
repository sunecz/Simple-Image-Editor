/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.registry.ToolsRegistry;
import sune.apps.ie.tool.Tool;
import sune.apps.ie.tool.ToolViewMove;
import sune.apps.ie.translation.Translation;

public class IScrollPane extends ScrollPane
{
	private Editor editor;
	private IScrollPaneController controller;
	
	private double scrollV;
	private double scrollH;
	private double scrollVSpeed;
	private double scrollHSpeed;
	private boolean scrollHSet;
	private boolean scrollVSet;
	private boolean scrollWheel;
	
	private double mouseX;
	private double mouseY;
	
	private double currentX;
	private double currentY;
	
	public static final int MAX_WIDTH  = 4000;
	public static final int MAX_HEIGHT = 4000;

	public IScrollPane(Editor editor)
	{
		super();
		
		this.editor 	= editor;
		this.controller = new IScrollPaneController();
		
		this.scrollVSpeed = 2;
		this.scrollHSpeed = 2;
		
		this.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle(ScrollEvent event)
			{
				scrollWheel = true;
				double delta = event.getDeltaY();
				
				if(event.isAltDown())
				{
					// ZOOMING (SCALING)
					CanvasPanel canvas = editor.getCanvas();
					double scale  = canvas.getScale();
					double factor = 0.1;
					double svalue = delta / event.getMultiplierY() * factor;
					
					double tscale = Math.max(0.1, Math.min(scale+svalue, 5));
					if(tscale * canvas.getWidth()  > MAX_WIDTH ||
					   tscale * canvas.getHeight() > MAX_HEIGHT)
					{
						event.consume();
						return;
					}
					
					canvas.scale(tscale);
					
					Rectangle2D bounds = getViewBounds();					
					double posX = currentX / bounds.getWidth();
					double posY = currentY / bounds.getHeight();
					scrollHTo(posX);
					scrollVTo(posY);
					
					editor.setStatus(Translation.getTranslation("general.texts.zoom", Double.toString(Math.round(tscale * 100) / 100.0)));
				}
				else
				{
					if(event.isControlDown()) scrollH(delta);
					else					  scrollV(delta);
				}
				
				event.consume();
			}
		});
		
		this.vvalueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				scrollV = newValue.doubleValue();
			}
		});
		
		this.hvalueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				scrollH = newValue.doubleValue();
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				currentX = event.getX();
				currentY = event.getY();
			}
		});
	}
	
	public class IScrollPaneController
	{
		private KeyCode moveKey;
		private boolean isDown;
		
		public IScrollPaneController()
		{
			moveKey	= KeyCode.SPACE;
			
			addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					if(event.getButton() == MouseButton.PRIMARY)
					{
						mouseX = event.getX();
						mouseY = event.getY();
						
						if(isDown)
							event.consume();
					}
				}
			});
			
			addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					if(isDown)
					{
						double x = event.getX();
						double y = event.getY();
						
						double dx = x - mouseX;
						double dy = y - mouseY;

						scrollVTo(scrollV - (dy / getHeight()));
						scrollHTo(scrollH - (dx / getWidth()));
						
						mouseX = x;
						mouseY = y;
						
						event.consume();
					}
				}
			});

			addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(KeyEvent event)
				{
					if(event.getCode() == moveKey)
					{
						ToolViewMove toolVM = (ToolViewMove) editor.getCanvas().getTool();
						editor.getCanvas().setTool(toolVM.getPreviousTool());
						isDown = false;
					}
				}
			});
			
			addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(KeyEvent event)
				{
					if(event.getCode() == moveKey)
					{
						if(!isDown)
						{
							Tool canvasTool 	= editor.getCanvas().getTool();
							ToolViewMove toolVM = (ToolViewMove) ToolsRegistry.getToolByName(ToolViewMove.NAME);
							
							if(!(canvasTool instanceof ToolViewMove))
								toolVM.setPreviousTool(canvasTool);
							
							editor.getCanvas().setTool(toolVM);
							editor.getCanvas().redraw();
							isDown = true;
						}
						
						event.consume();
					}
				}
			});
		}
	
		public void setKey(KeyCode key)
		{
			moveKey = key;
		}
		
		public void setIsDown(boolean flag)
		{
			isDown = flag;
		}
	}
	
	public IScrollPaneController getController()
	{
		return controller;
	}
	
	public void scrollV(double delta)
	{
		if(!scrollWheel)
			return;
		
		if(scrollVSet)
		{
			scrollVSet = false;
			return;
		}

		double cval = scrollV;
		double sval = -delta/getBoundsInLocal().getHeight();
		double fval = cval+(sval*scrollVSpeed);

		scrollVSet = true;
		setVvalue(scrollV = Math.max(0, Math.min(fval, 1)));
		
		scrollWheel = false;
		editor.getCanvas().redraw();
	}
	
	public void scrollH(double delta)
	{
		if(!scrollWheel)
			return;
		
		if(scrollHSet)
		{
			scrollHSet = false;
			return;
		}

		double cval = scrollH;
		double sval = -delta/getBoundsInLocal().getWidth();
		double fval = cval+(sval*scrollHSpeed);

		scrollHSet = true;
		setHvalue(scrollH = Math.max(0, Math.min(fval, 1)));
		
		scrollWheel = false;
		editor.getCanvas().redraw();
	}
	
	public void scrollVTo(double val)
	{
		setVvalue(scrollV = Math.max(0, Math.min(val, 1)));
	}
	
	public void scrollHTo(double val)
	{
		setHvalue(scrollH = Math.max(0, Math.min(val, 1)));
	}
	
	public void scrollToCenterV()
	{
		setVvalue(scrollV = 0.5);
	}
	
	public void scrollToCenterH()
	{
		setHvalue(scrollH = 0.5);
	}
	
	public void scrollToCenter()
	{
		scrollToCenterV();
		scrollToCenterH();
	}
	
	public void setVerticalScrollSpeed(double speed)
	{
		this.scrollVSpeed = speed;
	}
	
	public void setHorizontalScrollSpeed(double speed)
	{
		this.scrollHSpeed = speed;
	}
	
	public double getVerticalPosition()
	{
		return scrollV;
	}
	
	public double getHorizontalPosition()
	{
		return scrollH;
	}
	
	public double getVerticalScrollSpeed()
	{
		return scrollVSpeed;
	}
	
	public double getHorizontalScrollSpeed()
	{
		return scrollHSpeed;
	}
	
	public Rectangle2D getViewBounds()
	{
		Node content;
		if((content = getContent()) instanceof CanvasPanel)
		{
			CanvasPanel panel 	= (CanvasPanel) content;
			Bounds bounds  		= getViewportBounds();
			double scrollX 		= getHorizontalPosition();
			double scrollY 		= getVerticalPosition();
			
			double viewW = bounds.getWidth();
			double viewH = bounds.getHeight();
			double viewX = scrollX * (panel.getWidth() - viewW);
			double viewY = scrollY * (panel.getHeight() - viewH);
			
			return new Rectangle2D(viewX, viewY, viewW, viewH);
		}
		
		return null;
	}
}