/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.graphics.GraphicsImageCursor;
import sune.apps.ie.graphics.ImageUtils;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.IconRegistry.Icon;
import sune.apps.ie.registry.ProgressBackups;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.AlphaColor;
import sune.apps.ie.util.Utils;

public class ToolResize extends Tool
{
	public static final String NAME  = "TOOL_RESIZE";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_resize.png");

	private LayersManager layersManager;
	private Layer selectedLayer;
	
	private double defX;
	private double defY;
	private double defWidth;
	private double defHeight;
	private boolean defaultSet;
	
	private WritableImage cursorImageNW;
	private WritableImage cursorImageNE;
	private WritableImage cursorImageSW;
	private WritableImage cursorImageSE;
	private WritableImage cursorImageCN;
	private WritableImage cursorImageCW;
	private WritableImage cursorImageCE;
	private WritableImage cursorImageCS;
	private WritableImage cursorImageDF;
	private WritableImage cursorImage;
	
	private GraphicsCursor cursorNW;
	private GraphicsCursor cursorNE;
	private GraphicsCursor cursorSW;
	private GraphicsCursor cursorSE;
	private GraphicsCursor cursorCN;
	private GraphicsCursor cursorCW;
	private GraphicsCursor cursorCE;
	private GraphicsCursor cursorCS;
	private GraphicsCursor cursorDF;
	private GraphicsCursor cursor;

	private double mx;
	private double my;
	
	private boolean dragged;
	private int selectedDirection = 0;
	
	private double layerX;
	private double layerY;
	private double width;
	private double height;
	
	private double areaWidth;
	private double areaHeight;
	
	private Point2D p0;
	private Point2D p1;
	private Point2D p2;
	private Point2D p3;	
	private Point2D p4;
	private Point2D p5;
	private Point2D p6;
	private Point2D p7;
	
	private EventHandler<KeyEvent> listener;
	private boolean changed;
	
	public ToolResize(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);

		this.layersManager 	= canvas.getLayersManager();
		this.cursorImageNW 	= ResourceLoader.loadImage("cursors/cursor_resize_nw.png");
		this.cursorImageNE 	= ResourceLoader.loadImage("cursors/cursor_resize_ne.png");
		this.cursorImageSW 	= ResourceLoader.loadImage("cursors/cursor_resize_sw.png");
		this.cursorImageSE 	= ResourceLoader.loadImage("cursors/cursor_resize_se.png");
		this.cursorImageCN 	= ResourceLoader.loadImage("cursors/cursor_resize_cn.png");
		this.cursorImageCW 	= ResourceLoader.loadImage("cursors/cursor_resize_cw.png");
		this.cursorImageCE 	= ResourceLoader.loadImage("cursors/cursor_resize_ce.png");
		this.cursorImageCS 	= ResourceLoader.loadImage("cursors/cursor_resize_cs.png");
		this.cursorImageDF 	= ResourceLoader.loadImage("cursors/cursor_resize_default.png");
		
		this.cursorNW = new GraphicsImageCursor(cursorImageNW, 0, 0, cursorImageNW.getWidth(), cursorImageNW.getHeight());
		this.cursorNE = new GraphicsImageCursor(cursorImageNE, 0, 0, cursorImageNE.getWidth(), cursorImageNE.getHeight());
		this.cursorSW = new GraphicsImageCursor(cursorImageSW, 0, 0, cursorImageSW.getWidth(), cursorImageSW.getHeight());
		this.cursorSE = new GraphicsImageCursor(cursorImageSE, 0, 0, cursorImageSE.getWidth(), cursorImageSE.getHeight());
		this.cursorCN = new GraphicsImageCursor(cursorImageCN, 0, 0, cursorImageCN.getWidth(), cursorImageCN.getHeight());
		this.cursorCW = new GraphicsImageCursor(cursorImageCW, 0, 0, cursorImageCW.getWidth(), cursorImageCW.getHeight());
		this.cursorCE = new GraphicsImageCursor(cursorImageCE, 0, 0, cursorImageCE.getWidth(), cursorImageCE.getHeight());
		this.cursorCS = new GraphicsImageCursor(cursorImageCS, 0, 0, cursorImageCS.getWidth(), cursorImageCS.getHeight());
		this.cursorDF = new GraphicsImageCursor(cursorImageDF, 0, 0, cursorImageDF.getWidth(), cursorImageDF.getHeight());
	}
	
	public void saveImageWithScaling()
	{
		if(selectedLayer == null)
			return;
		
		double lw = selectedLayer.getWidth(1);
		double lh = selectedLayer.getHeight(1);

		ImageLayer layer 	= (ImageLayer) selectedLayer;
		WritableImage img 	= layer.getImage();
		
		if(layer.getMask() != null)
			img = layer.getMask().getMaskedImage();

		if(lw < 0) img = ImageUtils.flipImageHorizontally(img);
		if(lh < 0) img = ImageUtils.flipImageVertically(img);
		
		double lx = selectedLayer.getX(1);
		double ly = selectedLayer.getY(1);
		
		if(lw < 0) lx += lw;
		if(lh < 0) ly += lh;
		
		layer.move(lx, ly);
		layer.setImage(ImageUtils.resize(img, (int) Math.abs(lw), (int) Math.abs(lh)));
		reloadPoints();
		
		changed = true;
		canvas.redraw();
		editor.reloadLayers();
		
		ProgressBackups.createBackup(editor);
	}
	
	@Override
	public void unload()
	{
		editor.removeKeyListener(listener);
		
		if(!changed)
		{
			if(selectedLayer != null)
			{
				Layer backgroundLayer = canvas.getBackgroundLayer();
				double backgroundX 	  = backgroundLayer.getX(1);
				double backgroundY	  = backgroundLayer.getY(1);
				
				selectedLayer.move(layerX = backgroundX + defX,
								   layerY = backgroundY + defY);
				selectedLayer.setSize(width = defWidth, height = defHeight);
			}
			
			reloadPoints();
			canvas.redraw();
		}
		
		selectedLayer = null;
		changed 	  = false;
	}
	
	@Override
	public void draw(GraphicsContext gc)
	{
		if(cursor == null)
			return;
		
		if(p0 != null && p1 != null && p2 != null && p3 != null && 
		   p4 != null && p5 != null && p6 != null && p7 != null &&
		   selectedLayer != null && selectedLayer.isVisible() && selectedLayer.isResizable())
		{
			double scale = canvas.getScale();
			double lx = selectedLayer.getX(scale);
			double ly = selectedLayer.getY(scale);
			double lw = selectedLayer.getWidth(scale);
			double lh = selectedLayer.getHeight(scale);
			
			gc.save();
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(5);
			gc.strokeRect(lx, ly, lw, lh);
			gc.restore();
			
			gc.setFill(AlphaColor.create(Color.LIGHTBLUE, 150));
			gc.fillRect(p0.getX(), p0.getY(), areaWidth, areaHeight);
			gc.fillRect(p1.getX()-areaWidth, p1.getY(), areaWidth, areaHeight);
			gc.fillRect(p2.getX(), p2.getY()-areaHeight, areaWidth, areaHeight);
			gc.fillRect(p3.getX()-areaWidth, p3.getY()-areaHeight, areaWidth, areaHeight);
			gc.fillRect(p4.getX(), p4.getY(), areaWidth, areaHeight);
			gc.fillRect(p5.getX(), p5.getY(), areaWidth, areaHeight);
			gc.fillRect(p6.getX()-areaWidth, p6.getY(), areaWidth, areaHeight);
			gc.fillRect(p7.getX(), p7.getY()-areaHeight, areaWidth, areaHeight);
			
			double ovalSize  = 4;
			double ovalShift = ovalSize/2;
			
			gc.setFill(Color.BLACK);
			gc.fillOval(p0.getX()-ovalShift, p0.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p1.getX()-ovalShift, p1.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p2.getX()-ovalShift, p2.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p3.getX()-ovalShift, p3.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p4.getX()+areaWidth/2-ovalShift, p4.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p5.getX()-ovalShift, p5.getY()+areaHeight/2-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p6.getX()-ovalShift, p6.getY()+areaHeight/2-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p7.getX()+areaWidth/2-ovalShift, p7.getY()-ovalShift, ovalSize, ovalSize);
		}
		
		cursor.draw(gc, null, 1);
	}

	@Override
	public void reset()
	{
		defaultSet = false;
	}
	
	@Override
	public void reload()
	{
		reloadPoints();
	}

	@Override
	public Pane init()
	{
		editor.addKeyListener(listener = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.getCode() == KeyCode.ENTER)
					saveImageWithScaling();
			}
		});
		
		Pane pane = new Pane();
		pane.setPadding(new Insets(15, 15, 16, 15));
		return pane;
	}

	private void moveCursor(MouseEvent event)
	{
		if(cursor == null)
			return;

		cursor.setX((int) (event.getX() - cursorImage.getWidth()/2));
		cursor.setY((int) (event.getY() - cursorImage.getHeight()/2));
		canvas.redraw();
	}
	
	public void reloadPoints()
	{
		if(selectedLayer == null)
			return;
		
		double scale = canvas.getScale();
		double lx = selectedLayer.getX(scale);
		double ly = selectedLayer.getY(scale);
		double lw = selectedLayer.getWidth(scale);
		double lh = selectedLayer.getHeight(scale);
		
		areaWidth  = Math.min(Math.abs(lw) / 3, 50);
		areaHeight = Math.min(Math.abs(lh) / 3, 50);
		
		// Top-left point
		p0 = new Point2D(lx, ly);
		// Top-right point
		p1 = new Point2D(lx+lw, ly);
		// Bottom-left point
		p2 = new Point2D(lx, ly+lh);
		// Bottom-right point
		p3 = new Point2D(lx+lw, ly+lh);
		// Top-center point
		p4 = new Point2D(lx+(lw-areaWidth)/2, ly);
		// Left-center point
		p5 = new Point2D(lx, ly+(lh-areaHeight)/2);
		// Right-center point
		p6 = new Point2D(lx+lw, ly+(lh-areaHeight)/2);
		// Bottom-center point
		p7 = new Point2D(lx+(lw-areaWidth)/2, ly+lh);
	}

	@Override
	public void mouseMoved(MouseEvent event) 
	{
		if(canvas.isInitialized() && selectedLayer != null && selectedLayer.isVisible())
		{
			double x = event.getX();
			double y = event.getY();
			
			if(Utils.inRange(x, p0.getX(), p0.getX()+areaWidth) && Utils.inRange(y, p0.getY(), p0.getY()+areaHeight))
			{
				cursorImage = cursorImageNW;
				cursor 		= cursorNW;
				selectedDirection = 0;
			} 
			else if(Utils.inRange(x, p1.getX()-areaWidth, p1.getX()) && Utils.inRange(y, p1.getY(), p1.getY()+areaHeight))
			{
				cursorImage = cursorImageNE;
				cursor 		= cursorNE;
				selectedDirection = 1;
			}
			else if(Utils.inRange(x, p2.getX(), p2.getX()+areaWidth) && Utils.inRange(y, p2.getY()-areaHeight, p2.getY()))
			{
				cursorImage = cursorImageSW;
				cursor 		= cursorSW;
				selectedDirection = 2;
			}
			else if(Utils.inRange(x, p3.getX()-areaWidth, p3.getX()) && Utils.inRange(y, p3.getY()-areaHeight, p3.getY()))
			{
				cursorImage = cursorImageSE;
				cursor 		= cursorSE;
				selectedDirection = 3;
			}
			else if(Utils.inRange(x, p4.getX(), p4.getX()+areaWidth) && Utils.inRange(y, p4.getY(), p4.getY()+areaHeight))
			{
				cursorImage = cursorImageCN;
				cursor 		= cursorCN;
				selectedDirection = 4;
			}
			else if(Utils.inRange(x, p5.getX(), p5.getX()+areaWidth) && Utils.inRange(y, p5.getY(), p5.getY()+areaHeight))
			{
				cursorImage = cursorImageCW;
				cursor 		= cursorCW;
				selectedDirection = 5;
			}
			else if(Utils.inRange(x, p6.getX()-areaWidth, p6.getX()) && Utils.inRange(y, p6.getY(), p6.getY()+areaHeight))
			{
				cursorImage = cursorImageCE;
				cursor 		= cursorCE;
				selectedDirection = 6;
			}
			else if(Utils.inRange(x, p7.getX(), p7.getX()+areaWidth) && Utils.inRange(y, p7.getY()-areaHeight, p7.getY()))
			{
				cursorImage = cursorImageCS;
				cursor 		= cursorCS;
				selectedDirection = 7;
			}
			else
			{
				cursorImage = cursorImageDF;
				cursor 		= cursorDF;
			}
		}
		
		moveCursor(event);
	}
	
	@Override
	public void mouseDragged(MouseEvent event)
	{
		if(dragged)
		{
			double scale = canvas.getScale();
			
			double x = event.getX();
			double y = event.getY();
			
			double dx = canvas.scalePosition(x - mx);
			double dy = canvas.scalePosition(y - my);
			
			double layerX = this.layerX / scale;
			double layerY = this.layerY / scale;
			double width  = this.width  / scale;
			double height = this.height / scale;
			
			switch(selectedDirection)
			{
				case 0:
					selectedLayer.move(layerX+dx, layerY+dy);
					selectedLayer.setSize(width-dx, height-dy);
					break;
				case 1:
					selectedLayer.move(layerX, layerY+dy);
					selectedLayer.setSize(width+dx, height-dy);
					break;
				case 2:
					selectedLayer.move(layerX+dx, layerY);
					selectedLayer.setSize(width-dx, height+dy);
					break;
				case 3:
					selectedLayer.setSize(width+dx, height+dy);
					break;
				case 4:
					selectedLayer.move(layerX, layerY+dy);
					selectedLayer.setSize(width, height-dy);
					break;
				case 5:
					selectedLayer.move(layerX+dx, layerY);
					selectedLayer.setSize(width-dx, height);
					break;
				case 6:
					selectedLayer.setSize(width+dx, height);
					break;
				case 7:
					selectedLayer.setSize(width, height+dy);
					break;
			}
			
			reloadPoints();
			editor.setToolInfo(
				Translation.getTranslation("tools.info.resize",
					Double.toString(Utils.round(selectedLayer.getX(scale) - canvas.getBackgroundLayer().getX(1), 3)),
					Double.toString(Utils.round(selectedLayer.getY(scale) - canvas.getBackgroundLayer().getY(1), 3)),
					Double.toString(Utils.round(selectedLayer.getWidth(scale), 3)),
					Double.toString(Utils.round(selectedLayer.getHeight(scale), 3))));
		}
		
		moveCursor(event);
	}

	@Override
	public void mouseDown(MouseEvent event)
	{
		if(event.getButton() != MouseButton.PRIMARY ||
		   !canvas.isInitialized())
			return;
		
		selectedLayer = layersManager.getSelectedLayer();
		if(!selectedLayer.isResizable() || !selectedLayer.isVisible())
			return;
		
		setDefaults();
		mx = event.getX();
		my = event.getY();

		double scale = canvas.getScale();
		double lx = selectedLayer.getX(scale);
		double ly = selectedLayer.getY(scale);
		double lw = selectedLayer.getWidth(scale);
		double lh = selectedLayer.getHeight(scale);
		double aw = Math.min(lw / 2, 50);
		double ah = Math.min(lh / 2, 50);

		layerX = lx;
		layerY = ly;
		width  = lw;
		height = lh;
		
		if((Utils.inRange(mx, lx, lx+aw) 				&& Utils.inRange(my, ly, ly+ah)) 				||
		   (Utils.inRange(mx, lx+lw-aw, lx+lw) 			&& Utils.inRange(my, ly, ly+ah)) 				||
		   (Utils.inRange(mx, lx, lx+aw) 				&& Utils.inRange(my, ly+lh-ah, ly+lh)) 			||
		   (Utils.inRange(mx, lx+lw-aw, lx+lw) 			&& Utils.inRange(my, ly+lh-ah, ly+lh)) 			||
		   (Utils.inRange(mx, lx+lw/2-aw, lx+lw/2+aw) 	&& Utils.inRange(my, ly, ly+ah)) 				||
		   (Utils.inRange(mx, lx, lx+aw) 				&& Utils.inRange(my, ly+lh/2-ah, ly+lh/2+ah)) 	||
		   (Utils.inRange(mx, lx+lw-aw, lx+lw) 			&& Utils.inRange(my, ly+lh/2-ah, ly+lh/2+ah)) 	||
		   (Utils.inRange(mx, lx+lw/2-aw, lx+lw/2+aw) 	&& Utils.inRange(my, ly+lh-ah, ly+lh)))
			dragged = true;
	}
	
	private void setDefaults()
	{
		if(!defaultSet)
		{
			Layer backgroundLayer = canvas.getBackgroundLayer();
			double backgroundX 	  = backgroundLayer.getX(1);
			double backgroundY	  = backgroundLayer.getY(1);
			
			defX 		= (selectedLayer.getX(1) - backgroundX);
			defY 		= (selectedLayer.getY(1) - backgroundY);
			defWidth 	= selectedLayer.getWidth(1);
			defHeight 	= selectedLayer.getHeight(1);
			defaultSet  = true;
		}
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		mx = 0;
		my = 0;
		dragged = false;
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		cursorImage   = cursorImageDF;
		cursor 		  = cursorDF;
		selectedLayer = layersManager.getSelectedLayer();
		
		setDefaults();
		reloadPoints();
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
		return false;
	}
	
	@Override
	public boolean isEdgePoint(double x, double y, double stroke)
	{
		return false;
	}
}