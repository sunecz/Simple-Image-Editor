/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.component.Dialog;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.event.EventListener;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.graphics.PixelHelper;
import sune.apps.ie.image.ColorConverter;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayerMask;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;
import sune.apps.ie.registry.ProgressBackups;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class ToolBrush extends Tool implements ColorTool, MaskTool
{
	public static final String NAME  = "TOOL_BRUSH";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_brush.png");

	protected LayersManager layersManager;
	protected Layer selectedLayer;
	
	protected GraphicsCursor cursor;
	protected PixelHelper helper;
	
	protected ToolType type;
	protected double size;

	protected int colorARGB;
	protected int maskColorARGB;
	protected int opacity = 255;
	
	protected boolean isInMask;
	protected boolean usedInMask;
	
	protected int prevX = -1;
	protected int prevY = -1;
	
	protected boolean mouseDown;
	
	/* ===== GUI ===== */
	protected Pane pane;
	protected HBox box;
	
	protected Label lblColor;
	protected Rectangle paneColor;
	
	protected Label lblSize;
	protected Slider sliderSize;
	protected NumberTextField txtSize;
	
	protected Separator sep0;
	protected Separator sep1;

	private Label lblOpacity;
	private Slider sliderOpacity;

	public ToolBrush(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);
		this.type = ToolType.CIRCLE;
		this.size = 20.0;
		
		this.colorARGB		= ColorConverter.FXColorToInt(Color.BLACK);
		this.maskColorARGB	= LayerMask.MASK_COLOR_EXCLUDE;
		this.layersManager 	= canvas.getLayersManager();
		this.usedInMask 	= true;
	}
	
	@Override
	public void setColor(int color)
	{
		colorARGB = color;
		
		if(helper != null)
		helper.setColor(isInMask ? maskColorARGB : colorARGB);
		
		if(!isInMask)
		paneColor.setFill(ColorConverter.IntToFXColor(colorARGB));
	}
	
	@Override
	public void setColor(Color color)
	{
		setColor(ColorConverter.FXColorToInt(color));
	}

	public void setStroke(double stroke)
	{
		size = stroke;
		
		if(helper != null)
		helper.setStroke(stroke);
	}
	
	@Override
	public int getColor()
	{
		return colorARGB;
	}
	
	@Override
	public void setMaskColor(int color)
	{
		maskColorARGB = color;
	}
	
	@Override
	public void setMaskColor(Color color)
	{
		setMaskColor(ColorConverter.FXColorToInt(color));
	}
	
	@Override
	public void setOpacity(int opacity)
	{
		this.opacity = opacity;
		int alphaVal = ((opacity & 0xff) << 24);
		
		setColor((colorARGB & 0x00ffffff) | alphaVal);
		setMaskColor((maskColorARGB & 0x00ffffff) | alphaVal);
	}
	
	@Override
	public int getMaskColor()
	{
		return maskColorARGB;
	}

	@Override
	public int getOpacity()
	{
		return opacity;
	}
	
	@Override
	public void setInMask(boolean flag)
	{
		isInMask = flag;
		editor.reloadToolPanel();
	}
	
	@Override
	public boolean isInMask()
	{
		return isInMask;
	}
	
	@Override
	public void draw(GraphicsContext gc)
	{
		if(cursor == null)
			return;
		
		cursor.draw(gc, null, canvas.getScale());
	}
	
	private void moveCursor(MouseEvent event)
	{
		if(cursor == null)
			return;
		
		int x  = (int) (event.getX());
		int y  = (int) (event.getY());
		
		double correctX = canvas.scalePosition(x) - canvas.leftOffset();
		double correctY = canvas.scalePosition(y) - canvas.topOffset();
		
		int ix = (int) correctX;
		int iy = (int) correctY;
		
		Color color = Color.WHITE;
		if(helper != null && selectedLayer.isVisible())
		{
			color = helper.getColor(ix, iy);
			
			if(!color.isOpaque())
				color = Color.WHITE;
		}

		cursor.setX(x);
		cursor.setY(y);
		
		boolean inCanvas = inLayerArea(x, y);
		if(inCanvas) editor.setToolInfo(
			Translation.getTranslation("tools.info.brush",
				Double.toString(Utils.round(correctX, 3)),
				Double.toString(Utils.round(correctY, 3)),
				ColorConverter.toReadableString(color),
				color.toString()));
		cursor.setColor(inCanvas ? color.invert() : Color.BLACK);
		canvas.redraw();
	}
	
	protected void drawPixels(int x, int y)
	{
		if(helper == null && !initHelper(true))
			return;
		
		if(selectedLayer.isVisible() && prevX > -1 && prevY > -1)
		helper.drawLine(prevX, prevY, x, y, helper.METHOD_BRUSH);
		
		prevX = x;
		prevY = y;
	}
	
	public boolean inLayerArea(int x, int y)
	{
		if(selectedLayer == null)
			return false;
		
		double scale  = canvas.getScale();
		double layerX = selectedLayer.getX(scale);
		double layerY = selectedLayer.getY(scale);
		double layerW = selectedLayer.getWidth(scale);
		double layerH = selectedLayer.getHeight(scale);
		
		return Utils.inRange(x, layerX, layerX+layerW) &&
			   Utils.inRange(y, layerY, layerY+layerH);
	}
	
	@Override
	public void unload()
	{
		selectedLayer = null;
	}
	
	@Override
	public void reset()
	{
		helper = null;
	}
	
	@Override
	public Pane init()
	{
		canvas.getColorChangeListeners().addListener(new EventListener<Color>()
		{
			@Override
			public void call(Color oldValue, Color newValue)
			{
				setColor((ColorConverter.FXColorToInt(newValue) & 0x00ffffff) |
						 ((opacity & 0xff) << 24));
			}
		});
		
		if(!canvas.getLayersManager().isEmpty())
		{
			selectedLayer 	= canvas.getLayersManager().getSelectedLayer();
			isInMask 		= selectedLayer.getMask() != null && selectedLayer.isMaskSelected();
		}
		
		pane = new Pane();
		box = new HBox(5);
		box.setPadding(new Insets(5));
		box.setAlignment(Pos.CENTER_LEFT);
		
		lblColor = new Label(Translation.getTranslation("tools.color"));
		box.getChildren().add(lblColor);

		paneColor = new Rectangle(20, 20);
		
		if(isInMask && usedInMask)
		{
			paneColor.setVisible(false);
			Rectangle maskColorExclude = new Rectangle(20, 20);
			Rectangle maskColorInclude = new Rectangle(20, 20);
			
			maskColorExclude.setStroke(maskColorARGB == LayerMask.MASK_COLOR_EXCLUDE ? Color.LIGHTBLUE : Color.BLACK);
			maskColorExclude.setFill(Color.BLACK);
			
			maskColorExclude.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					maskColorARGB 	= LayerMask.MASK_COLOR_EXCLUDE;
					opacity 		= 0;
					
					editor.reloadToolPanel();
				}
			});

			maskColorInclude.setStroke(maskColorARGB == LayerMask.MASK_COLOR_INCLUDE ? Color.LIGHTBLUE : Color.BLACK);
			maskColorInclude.setFill(Color.WHITE);
			
			maskColorInclude.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					maskColorARGB 	= LayerMask.MASK_COLOR_INCLUDE;
					opacity 		= 255;
					
					editor.reloadToolPanel();
				}
			});
			
			box.getChildren().add(maskColorExclude);
			box.getChildren().add(maskColorInclude);
		}
		else
		{
			Color globalColor 	= canvas.getGlobalColor();
			colorARGB 			= ColorConverter.FXColorToInt(globalColor);
			
			paneColor.setStroke(Color.BLACK);
			paneColor.setFill(globalColor);
		}
		
		box.getChildren().add(paneColor);
		
		sep0 = new Separator(Orientation.VERTICAL);
		box.getChildren().add(sep0);
		
		lblSize = new Label(Translation.getTranslation("tools.size"));
		box.getChildren().add(lblSize);
		
		sliderSize = new Slider(1, 200, size);
		sliderSize.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				txtSize.setText(numberToString(size = newValue.doubleValue()));
			}
		});
		
		box.getChildren().add(sliderSize);
		
		txtSize = new NumberTextField(numberToString(size));
		txtSize.setPadding(new Insets(2));
		txtSize.setPrefWidth(50);
		
		txtSize.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.getCode() == KeyCode.ENTER)
				{
					String text;
					if((text = txtSize.getText()).isEmpty())
						return;
					
					sliderSize.setValue(Double.parseDouble(text));
				}
			};
		});
		
		box.getChildren().add(txtSize);
		
		sep1 = new Separator(Orientation.VERTICAL);
		box.getChildren().add(sep1);
		
		lblOpacity 		= new Label(Translation.getTranslation("tools.opacity", Integer.toString(opacity)));
		sliderOpacity 	= new Slider(0, 255, opacity);
		sliderOpacity.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				int opacity = newValue.intValue();
				
				setOpacity(opacity);
				lblOpacity.setText(Translation.getTranslation("tools.opacity", Integer.toString(opacity)));
			}
		});
		
		box.getChildren().add(lblOpacity);
		box.getChildren().add(sliderOpacity);
		
		pane.getChildren().add(box);
		return pane;
	}
	
	private String numberToString(double value)
	{
		return Double.toString(Math.round(value * 1000) / 1000.0);
	}
	
	@Override
	public void mouseMoved(MouseEvent event)
	{
		if(canvas.isInitialized())
		{
			if(selectedLayer == null)
				selectedLayer = layersManager.getSelectedLayer();
			
			if(helper == null)
				initHelper(false);
		}
		
		moveCursor(event);
	}
	
	public boolean initHelper(boolean message)
	{
		if(selectedLayer == null)
			return false;
		
		if(selectedLayer instanceof ImageLayer)
		{
			if(helper == null)
				helper = new PixelHelper((ImageLayer) selectedLayer);
			
			// SET DEFAULT VALUES
			helper.setTool(this);
			helper.setStroke(size);
			
			setOpacity(opacity);
			helper.setColor(isInMask ? maskColorARGB : colorARGB);
			return true;
		}
		else
		{
			if(!Dialog.isShown() && message)
				new Dialog(Translation.getTranslation("titles.error"),
						   Translation.getTranslation("messages.canvas.notImageLayer"));
			
			return false;
		}
	}
	
	@Override
	public void setLayer(Layer layer)
	{
		selectedLayer = layer;
		initHelper(false);
	}
	
	@Override
	public void mouseDragged(MouseEvent event)
	{
		if(mouseDown)
		{
			double scale = canvas.getScale();
			int x = (int) ((event.getX() - selectedLayer.getX(scale)) / scale);
			int y = (int) ((event.getY() - selectedLayer.getY(scale)) / scale);
			
			if(Math.sqrt(Math.pow(x - prevX, 2) + Math.pow(y - prevY, 2)) >= size / 10.0)
				drawPixels(x, y);
		}
		
		moveCursor(event);
	}

	@Override
	public void mouseDown(MouseEvent event)
	{
		if(event.getButton() != MouseButton.PRIMARY ||
		   !canvas.isInitialized())
			return;
		if(!initHelper(true))
			return;

		if(!event.isShiftDown())
		{
			prevX = -1;
			prevY = -1;
		}
		
		selectedLayer = layersManager.getSelectedLayer();
		double scale  = canvas.getScale();
		int x = (int) ((event.getX() - selectedLayer.getX(scale)) / scale);
		int y = (int) ((event.getY() - selectedLayer.getY(scale)) / scale);
		
		if(event.isAltDown())
		{
			Color color = Color.WHITE;
			if(helper != null && selectedLayer.isVisible())
			{
				color = helper.getColor(x, y);
				
				if(!color.isOpaque())
					color = Color.WHITE;
			}
			
			canvas.setGlobalColor(color);
			return;
		}
		
		if(!selectedLayer.isEditable() || !selectedLayer.isVisible())
			return;
		
		mouseDown = true;
		if(prevX == -1 && prevY == -1)
		{
			drawPixels(prevX = x, prevY = y);
		}
		else
		{
			drawPixels(x, y);
			prevX = x;
			prevY = y;
		}
		
		canvas.redraw();
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		if(event.getButton() != MouseButton.PRIMARY)
			return;
		
		mouseDown = false;
		ProgressBackups.createBackup(editor);
		
		if(helper != null)
		helper.clear();
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		selectedLayer = layersManager.getSelectedLayer();
		
		if(helper == null)
			initHelper(false);
		
		double cursorSize = size/2;
		cursor = new GraphicsCursor(event.getX(), event.getY(), cursorSize, cursorSize);
		canvas.redraw();
	}

	@Override
	public void mouseExited(MouseEvent event)
	{
		cursor = null;
		canvas.redraw();
	}

	@Override
	public GraphicsCursor getCursor()
	{
		return cursor;
	}
	
	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Image getIcon()
	{
		return ICON;
	}
	
	@Override
	public boolean canDraw(double x, double y, double stroke)
	{
		return (type == ToolType.CIRCLE && Math.pow(x / stroke, 2) + Math.pow(y / stroke, 2) <= 1 || stroke < 1) ||
			   (type == ToolType.RECTANGLE);
	}
	
	@Override
	public boolean isEdgePoint(double x, double y, double stroke)
	{
		if(type == ToolType.CIRCLE)
		{
			double val = Math.pow(x / stroke, 2) + Math.pow(y / stroke, 2);
			// -0.05 because of the accuracy
			return val >= (1 - 0.05) && val <= 1 || stroke < 1;
		}
		
		return (type == ToolType.RECTANGLE);
	}
	
	@Override
	public boolean isEdgePoint2(double x, double y, double stroke, double factor)
	{
		if(type == ToolType.CIRCLE)
		{
			double val = Math.pow(x / stroke, 2) + Math.pow(y / stroke, 2);
			return val >= (1 - factor) && val <= 1 || stroke < 1;
		}
		
		return (type == ToolType.RECTANGLE);
	}
}