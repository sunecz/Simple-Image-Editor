/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import sune.apps.ie.graphics.ImageUtils;
import sune.apps.ie.layer.BackgroundLayer;
import sune.apps.ie.layer.EmptyLayer;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.ListenersRegistry;
import sune.apps.ie.registry.ThreadRegistry;
import sune.apps.ie.selection.Selection;
import sune.apps.ie.selection.Selection.LineSegment;
import sune.apps.ie.tool.Tool;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.AlphaColor;
import sune.apps.ie.util.Utils;

public class CanvasPanel extends Canvas
{
	private Editor editor;
	private GraphicsContext gc;
	
	private double width;
	private double height;

	private double canvasWidth;
	private double canvasHeight;

	private Insets padding = new Insets(500);
	private LayersManager layersManager;
	
	private Tool tool;
	private boolean viewTTools;
	
	private double leftOffset;
	private double topOffset;
	
	private boolean inited;
	private boolean additionalGraphics;
	
	private WritableImage imageSnapshot;
	
	private double scale = 1;
	private Color globalColor;
	private Layer backgroundLayer;
	
	public CanvasPanel(Editor editor)
	{
		super();
		
		this.editor = editor;
		this.gc 	= getGraphicsContext2D();
		
		this.layersManager = new LayersManager();
		
		this.viewTTools 		= true;
		this.additionalGraphics = true;
		this.globalColor		= Color.WHITE;

		this.setCursor(Cursor.NONE);
		this.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseDragged(event);
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseDown(event);
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseUp(event);
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseMoved(event);
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseEntered(event);
			}
		});
		
		this.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				mouseExited(event);
			}
		});
		
		this.setOnDragOver(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(DragEvent event)
			{
				if(event.getDragboard().hasFiles()) event.acceptTransferModes(TransferMode.COPY);
				else 								event.consume();
			}
		});
		
		this.setOnDragDropped(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(DragEvent event)
			{
				List<File> files = event.getDragboard().getFiles();
				
				for(File file : files)
					if(file.isFile() && file.canRead())
						editor.addFile(file);
				
				event.setDropCompleted(true);
				event.consume();
			}
		});
		
		ThreadRegistry.registerThreadAndStart(threadRedraw);
	}
	
	public WritableImage save(Color background)
	{
		additionalGraphics = false;
		Canvas canvas = new Canvas(width, height);
		draw(canvas.getGraphicsContext2D(), true);
		
		SnapshotParameters params = new SnapshotParameters();
		params.setViewport(new Rectangle2D(leftOffset, topOffset, canvasWidth, canvasHeight));
		params.setFill(background);
		
		return canvas.snapshot(params, new WritableImage((int) canvasWidth, (int) canvasHeight));
	}

	public void requestSave(Color background)
	{
		imageSnapshot = null;
		Utils.run(() -> { imageSnapshot = save(background); });
	}
	
	public WritableImage getImageSnapshot()
	{
		return imageSnapshot;
	}
	
	public void reset()
	{
		additionalGraphics = true;
	}
	
	public void addLayer(File file)
	{
		try
		{
			FileInputStream stream = new FileInputStream(file);
			WritableImage image = ImageUtils.toWritableImage(new Image(stream));

			ImageLayer layer = new ImageLayer(image, leftOffset, topOffset, image.getWidth(), image.getHeight());
			layer.setName(file.getName());
			layersManager.addLayer(layer);
			
			redraw();
			reloadLayers();
		}
		catch(Exception ex) {}
	}
	
	private void mouseDragged(MouseEvent event)
	{
		tool.mouseDragged(event);
	}
	
	private void mouseDown(MouseEvent event)
	{
		tool.mouseDown(event);
	}
	
	private void mouseUp(MouseEvent event)
	{
		tool.mouseUp(event);
		reloadLayers();
	}
	
	private void mouseEntered(MouseEvent event)
	{
		tool.mouseEntered(event);
	}
	
	private void mouseExited(MouseEvent event)
	{
		tool.mouseExited(event);
	}

	private void mouseMoved(MouseEvent event)
	{
		tool.mouseMoved(event);
	}
	
	public double leftOffset()
	{
		return leftOffset;
	}
	
	public double topOffset()
	{
		return topOffset;
	}
	
	public void create(double width, double height)
	{
		create(width, height, Color.TRANSPARENT, true);
	}
	
	public void create(double width, double height, Color backgroundColor, boolean emptyLayer)
	{
		this.canvasWidth  	= width;
		this.canvasHeight 	= height;
		this.leftOffset 	= 0;
		this.topOffset 		= 0;
		this.scale			= 1;
		layersManager.clear();
		
		// CREATE THE BACKGROUND LAYER
		backgroundLayer = new BackgroundLayer(0, 0, canvasWidth, canvasHeight);
		
		if(emptyLayer)
		{
			EmptyLayer baseLayer = new EmptyLayer(0, 0, canvasWidth, canvasHeight);
			baseLayer.setName(Translation.getTranslation("misc.baseLayer"));
			baseLayer.fill(backgroundColor);
			layersManager.addLayer(baseLayer);
		}
		
		editor.recalculatePreviewsSize();
		editor.canvasInit();
		tool.reset();
		
		reloadLayers();
		inited = true;
	}

	private int redrawCounter 	= 0;
	private Thread threadRedraw = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			while(ThreadRegistry.isRunning)
			{
				if(redrawCounter > 0)
				{
					Utils.run(() -> { draw(gc, false); });
					redrawCounter = 0;
				}
				
				// aprox. 60 times per second
				Utils.sleep(16);
			}
		}
	});
	
	public void redraw()
	{
		redrawCounter++;
	}
	
	public synchronized void draw(GraphicsContext gc, boolean saveMode)
	{
		gc.save();
		if(!saveMode)
		{
			double viewWidth  = Math.max(width, editor.getScrollPane().getWidth());
			double viewHeight = Math.max(height, editor.getScrollPane().getHeight());
			
			gc.setFill(Color.GRAY);
			gc.fillRect(0, 0, viewWidth, viewHeight);
		}
		
		gc.scale(scale, scale);
		// RENDER THE BACKGROUND LAYER
		if(backgroundLayer != null && !saveMode)
			backgroundLayer.render(gc);
		
		List<Layer> layers;
		if((layers = layersManager.getLayers()).size() > 0)
		{
			synchronized(layers)
			{
				for(int i = 0; i < layers.size(); i++)
				{
					Layer layer = layers.get(i);
					if(layer.isVisible() && !(saveMode && layer instanceof BackgroundLayer))
					{
						gc.save();
						
						if(layers.size() != 1)
						// BLENDS THE LAYER BY ITS BLEDING MODE
						gc.setGlobalBlendMode(
							layer.getBlendMode().getBlendMode());
						
						// RENDERS THE LAYER
						layer.render(gc);
						
						if(!saveMode)
						{
							Selection selection = layer.getSelection();
							if(selection != null && !selection.isEmpty())
							{
								gc.setStroke(AlphaColor.create(Color.GRAY, 80));
								
								Layer boundLayer = selection.getBoundLayer();
								double layerX 	 = boundLayer.getX(1);
								double layerY 	 = boundLayer.getY(1);
								
								Map<Integer, List<LineSegment>> segments = selection.getLineSegments();
								for(Entry<Integer, List<LineSegment>> entry : segments.entrySet())
								{
									int y 							= entry.getKey();
									List<LineSegment> lineSegments 	= entry.getValue();
									
									if(lineSegments.size() > 0)
									{
										for(LineSegment segment : lineSegments)
										{
											int ln = segment.length;
											int px = (int) (layerX+segment.x);
											int py = (int) (layerY+y);
											
											gc.strokeRect(px, py, ln, 1);
										}
									}
								}
							}
							
							if(additionalGraphics && viewTTools &&
							   selection != null && selection.isEmpty() && 
							   layersManager.getSelectedLayer() == layer &&
							   layer.canBeTransformed())
							{
								double x = layer.getX(1);
								double y = layer.getY(1);
								double w = layer.getWidth(1);
								double h = layer.getHeight(1);
								double a = layer.getAngle();
								
								if(a > 0)
								{
									Rotate rotate = new Rotate(a, x+w/2, y+h/2);
									gc.transform(rotate.getMxx(), rotate.getMyx(), rotate.getMxy(),
												 rotate.getMyy(), rotate.getTx(), rotate.getTy());
								}
								
								gc.setStroke(Color.BLACK);
								gc.setLineWidth(1.3);
								gc.setLineDashes(5);
								gc.strokeRect(x, y, w, h);
							}
						}
						
						gc.restore();
					}
				}
			}
		}
		
		gc.restore();
		if(additionalGraphics)
			tool.draw(gc);
	}
	
	public void reload(double width, double height)
	{
		this.width 	= width * scale;
		this.height = height * scale;
		
		Rectangle2D rect  = editor.getScrollPane().getViewBounds();
		double viewWidth  = Math.max(this.width, rect.getWidth());
		double viewHeight = Math.max(this.height, rect.getHeight());
		
		this.setWidth(viewWidth);
		this.setHeight(viewHeight);
		
		leftOffset = Math.max(padding.getLeft(), (width - canvasWidth) / 2);
		topOffset  = Math.max(padding.getTop(), (height - canvasHeight) / 2);
		
		List<Layer> layers = layersManager.getLayers();
		Map<Layer, double[]> positions = new HashMap<>();
		double backX = backgroundLayer.getX(1);
		double backY = backgroundLayer.getY(1);
		
		synchronized(layers)
		{
			for(Layer layer : layers)
			{
				positions.put(layer, new double[]
				{
					layer.getX(1) - backX,
					layer.getY(1) - backY
				});
			}
		}
		
		if(backgroundLayer != null)
		backgroundLayer.move(
			(viewWidth - backgroundLayer.getWidth(scale)) / (2*scale),
			(viewHeight - backgroundLayer.getHeight(scale)) / (2*scale)
		);
		
		backX = backgroundLayer.getX(1);
		backY = backgroundLayer.getY(1);
		
		synchronized(layers)
		{
			for(Layer layer : layers)
			{
				double[] offsets = positions.get(layer);
				layer.move(backX + offsets[0], backY + offsets[1]);
			}
		}
	}
	
	public double scalePosition(double position)
	{
		return position / scale;
	}
	
	public void centerLayer(Layer layer)
	{
		Rectangle2D rect  = editor.getScrollPane().getViewBounds();
		double viewWidth  = Math.max(this.width, rect.getWidth());
		double viewHeight = Math.max(this.height, rect.getHeight());
		
		layer.move((viewWidth - layer.getWidth(scale)) / (2*scale),
				   (viewHeight - layer.getHeight(scale)) / (2*scale));
	}
	
	public void setSize(double width, double height)
	{
		this.setWidth(this.width = width);
		this.setHeight(this.height = height);
	}
	
	public void setTToolsVisible(boolean flag)
	{
		viewTTools = flag;
	}
	
	public void deselectLayersSelections()
	{
		List<Layer> layers = layersManager.getLayers();
		if(layers != null && layers.size() > 0)
		{
			synchronized(layers)
			{
				for(Layer layer : layers)
					layer.removeSelection();
			}
		}
	}
	
	public double getCanvasWidth()
	{
		return canvasWidth;
	}
	
	public double getCanvasHeight()
	{
		return canvasHeight;
	}
	
	public Insets getPadding()
	{
		return padding;
	}
	
	public void setLayersManager(LayersManager layersManager)
	{
		this.layersManager = layersManager;
	}

	public LayersManager getLayersManager()
	{
		return layersManager;
	}
	
	public void setTool(Tool tool)
	{
		this.tool = tool;
		
		editor.reloadToolPanel();
		editor.reloadTools();
	}
	
	public Tool getTool()
	{
		return tool;
	}
	
	private void reloadLayers()
	{
		editor.reloadLayers();
	}
	
	public boolean isInitialized()
	{
		return inited;
	}
	
	public void clear()
	{
		layersManager.clear();
		inited = false;
	}
	
	public void scale(double scale)
	{
		this.scale = scale;
		editor.resizeCanvas();
		tool.reload();
	}
	
	public double getScale()
	{
		return scale;
	}
	
	private ListenersRegistry<Color> colorChangeListeners
		= new ListenersRegistry<>();

	public ListenersRegistry<Color> getColorChangeListeners()
	{
		return colorChangeListeners;
	}
	
	public void setGlobalColor(Color color)
	{
		setGlobalColor(color, true);
	}
	
	public void setGlobalColor(Color color, boolean changePicker)
	{
		if(inited && changePicker)
			editor.getColorPicker().setColor(color);
		
		colorChangeListeners.callAll(globalColor, globalColor = color);
	}
	
	public Color getGlobalColor()
	{
		return globalColor;
	}
	
	public void setBackgroundLayer(Layer layer)
	{
		this.backgroundLayer = layer;
	}
	
	public Layer getBackgroundLayer()
	{
		return backgroundLayer;
	}
}