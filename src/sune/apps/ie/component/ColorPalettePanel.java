/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.component;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import sune.apps.ie.Editor;
import sune.apps.ie.WindowColorPalette;
import sune.apps.ie.graphics.Colored;
import sune.apps.ie.util.Utils;

public class ColorPalettePanel extends VBox implements Colored
{
	private Editor editor;
	
	private double pickerWidth;
	private double pickerHeight;
	private double hueWidth;
	private double hueHeight;
	
	private WritableImage imagePicker;
	private WritableImage imageHue;
	
	private Canvas canvasPicker;
	private Canvas canvasHue;
	
	private double hueY;
	private double pickerX;
	private double pickerY;
	private double pickerHandleW = 10;
	private double pickerHandleH = 10;
	
	protected double hue;
	protected Color color;
	
	private HBox boxCanvases;
	private HBox boxCanvasPicker;
	private HBox boxCanvasHue;

	public ColorPalettePanel(Editor editor)
	{
		this(editor, 200, 30, 200);
	}
	
	public ColorPalettePanel(Editor editor, double widthPicker, double widthHue, double height)
	{
		this.editor = editor;
		this.color	= Color.WHITE;
		
		this.pickerWidth 	= widthPicker;
		this.pickerHeight 	= height;
		this.hueWidth 		= widthHue;
		this.hueHeight		= height;
		
		this.boxCanvases	 = new HBox(5);
		this.boxCanvasPicker = new HBox();
		this.boxCanvasHue 	 = new HBox();
		
		this.boxCanvasPicker.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
		this.boxCanvasHue.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

		this.canvasPicker 	= new Canvas(pickerWidth, pickerHeight);
		this.canvasHue 		= new Canvas(hueWidth, hueHeight);

		EventHandler<MouseEvent> pickerHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				if(event.getButton() == MouseButton.PRIMARY &&
				   event.getClickCount() == 2)
				{
					new WindowColorPalette(editor, ColorPalettePanel.this).setDefaultColor(color).show();
					return;
				}
				
				setColorByXY((int) (pickerX = Math.max(0, Math.min(event.getX(), canvasPicker.getWidth()-1))),
						     (int) (pickerY = Math.max(0, Math.min(event.getY(), canvasPicker.getHeight()-1))));
				redraw(true);
			}
		};
		
		this.canvasPicker.setOnMouseClicked(pickerHandler);
		this.canvasPicker.setOnMouseDragged(pickerHandler);
		
		EventHandler<MouseEvent> hueHandler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				setHueByXY((int) Math.max(0, Math.min(event.getX(), canvasHue.getWidth()-1)),
						   (int) (hueY = Math.max(0, Math.min(event.getY(), canvasHue.getHeight()-1))));
				
				preparePicker();
				redraw(false);
			}
		};
		
		this.canvasHue.setOnMouseClicked(hueHandler);
		this.canvasHue.setOnMouseDragged(hueHandler);
		
		preparePicker();
		prepareHue();
		
		this.boxCanvasPicker.getChildren().add(canvasPicker);
		this.boxCanvasHue.getChildren().add(canvasHue);

		this.boxCanvases.getChildren().add(boxCanvasPicker);
		this.boxCanvases.getChildren().add(boxCanvasHue);
		
		this.setSpacing(5);
		this.setPadding(new Insets(5, 5, 0, 5));
		this.getChildren().add(boxCanvases);
		
		setColorByXY((int) pickerX, (int) pickerY);
		setColor();
		
		redraw(true);
	}
	
	protected void setColor()
	{
		editor.getCanvas().setGlobalColor(color, false);
	}
	
	private void setColorByXY(int x, int y)
	{
		if(Utils.inRange(x, 0, imagePicker.getWidth()-1) &&
		   Utils.inRange(y, 0, imagePicker.getHeight()-1))
		{
			Color clr 	= imagePicker.getPixelReader().getColor(x, y);
			color 		= new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 1.0);
			
			setColor();
		}
	}
	
	private void setHueByXY(int x, int y)
	{
		if(Utils.inRange(x, 0, imageHue.getWidth()-1) &&
		   Utils.inRange(y, 0, imageHue.getHeight()-1))
		{
			hue = Math.floor(imageHue.getPixelReader().getColor(x, y).getHue());
			preparePicker();
			
			setColorByXY((int) pickerX, (int) pickerY);
		}
	}

	private void preparePicker()
	{
		GraphicsContext gc 	= canvasPicker.getGraphicsContext2D();
		double width 		= canvasPicker.getWidth();
		double height 		= canvasPicker.getHeight();

		gc.setFill(new LinearGradient(1, 0, width, 0, false, CycleMethod.NO_CYCLE,
				new Stop(0, Color.hsb(0, 0, 1.0)),
				new Stop(1, Color.hsb(hue, 1.0, 1.0)))
		);
		gc.fillRect(0, 0, width, height);
		
		gc.setFill(new LinearGradient(0, 1, 0, height-1, false, CycleMethod.NO_CYCLE,
				new Stop(0, Color.rgb(0, 0, 0, 0)),
				new Stop(1, Color.rgb(0, 0, 0, 1)))
		);
		gc.fillRect(0, 0, width, height);
		
		imagePicker = canvasPicker.snapshot(null, new WritableImage((int) width, (int) height));
		drawPickerHandle(gc);
	}
	
	private void prepareHue()
	{
		GraphicsContext gc 	= canvasHue.getGraphicsContext2D();
		double width 		= canvasHue.getWidth();
		double height 		= canvasHue.getHeight();
		
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);
		
		double size = height / 36;
		for(int i = 0; i < 36; i++)
		{
			gc.setFill(new LinearGradient(0, Math.floor(i*size), 0, Math.floor((i+1)*size), false, CycleMethod.NO_CYCLE,
					new Stop(0, Color.hsb(i*10, 1.0, 1.0)),
					new Stop(1, Color.hsb((i+1)*10, 1.0, 1.0)))
			);

			gc.fillRect(0, Math.floor(i*size), width, Math.ceil(size));
		}
		
		imageHue = canvasHue.snapshot(null, new WritableImage((int) width, (int) height));
	}
	
	public synchronized void draw(boolean drawPickerImage)
	{
		GraphicsContext gcPicker = canvasPicker.getGraphicsContext2D();
		GraphicsContext gcHue 	 = canvasHue.getGraphicsContext2D();

		double pickerW 	= canvasPicker.getWidth();
		double pickerH 	= canvasPicker.getHeight();
		
		double hueW = canvasHue.getWidth();
		double hueH = canvasHue.getHeight();
		
		if(drawPickerImage)
		gcPicker.drawImage(imagePicker, 0, 0, pickerW, pickerH);
		drawPickerHandle(gcPicker);
		
		gcHue.drawImage(imageHue, 0, 0, hueW, hueH);
		drawHueHandle(gcHue);
	}
	
	public synchronized void drawPickerHandle(GraphicsContext gc)
	{
		gc.setLineWidth(1.3);
		gc.setStroke(imagePicker.getPixelReader().getColor((int) pickerX, (int) pickerY).invert());
		gc.strokeOval(pickerX-pickerHandleW/2, pickerY-pickerHandleH/2, pickerHandleW, pickerHandleH);
		gc.strokeLine(pickerX-pickerHandleW/4, pickerY, pickerX+pickerHandleW/4, pickerY);
		gc.strokeLine(pickerX, pickerY-pickerHandleH/4, pickerX, pickerY+pickerHandleH/4);
	}
	
	public synchronized void drawHueHandle(GraphicsContext gc)
	{
		gc.setStroke(imageHue.getPixelReader().getColor(0, (int) hueY).invert());
		gc.strokeLine(0, hueY, canvasHue.getWidth(), hueY);
	}
	
	public void redraw(boolean drawPickerImage)
	{
		Utils.run(() ->
		{
			draw(drawPickerImage);
		});
	}
	
	public void setXYByColor(Color color)
	{
		setHueByXY(0,(int) (hueY 	= Math.max(0, Math.min(pickerHeight*(color.getHue() / 360.0), hueHeight-1))));
		setColorByXY((int) (pickerX = Math.max(0, Math.min(pickerWidth*color.getSaturation(), pickerWidth-1))),
					 (int) (pickerY = Math.max(0, Math.min(pickerHeight*(1.0-color.getBrightness()), pickerHeight-1))));
		redraw(true);
	}
	
	public void setPickerColor(Color color)
	{
		this.color 	= color;
		this.hue 	= color.getHue();
		preparePicker();

		setXYByColor(color);
	}

	public Color getPickerColor()
	{
		return color;
	}
	
	@Override
	public void setColor(Color color)
	{
		setPickerColor(color);
	}
	
	@Override
	public void setColor(int argb)
	{

	}
	
	@Override
	public int getColor()
	{
		return 0;
	}
}