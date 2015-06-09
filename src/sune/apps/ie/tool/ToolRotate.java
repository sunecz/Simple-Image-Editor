/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.tool;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import sune.apps.ie.CanvasPanel;
import sune.apps.ie.Editor;
import sune.apps.ie.graphics.GraphicsCursor;
import sune.apps.ie.graphics.GraphicsImageCursor;
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

public class ToolRotate extends Tool
{
	public static final String NAME  = "TOOL_ROTATE";
	public static final Icon   ICON	 = IconRegistry.loadIcon("tools/tool_rotate.png");

	private LayersManager layersManager;
	private Layer selectedLayer;
	
	private WritableImage cursorImageNW;
	private WritableImage cursorImageNE;
	private WritableImage cursorImageSW;
	private WritableImage cursorImageSE;
	private WritableImage cursorImageDF;
	private WritableImage cursorImage;
	
	private GraphicsCursor cursorNW;
	private GraphicsCursor cursorNE;
	private GraphicsCursor cursorSW;
	private GraphicsCursor cursorSE;
	private GraphicsCursor cursorDF;
	private GraphicsCursor cursor;
	
	private double angle;
	private boolean dragged;
	
	private Point2D p0;
	private Point2D p1;
	private Point2D p2;
	private Point2D p3;
	private int addAngle;
	
	private EventHandler<KeyEvent> listener;
	private boolean changed;

	public ToolRotate(Editor editor, CanvasPanel canvas)
	{
		super(editor, canvas);

		this.layersManager 	= canvas.getLayersManager();
		this.cursorImageNW 	= ResourceLoader.loadImage("cursors/cursor_rotate_nw.png");
		this.cursorImageNE 	= ResourceLoader.loadImage("cursors/cursor_rotate_ne.png");
		this.cursorImageSW 	= ResourceLoader.loadImage("cursors/cursor_rotate_sw.png");
		this.cursorImageSE 	= ResourceLoader.loadImage("cursors/cursor_rotate_se.png");
		this.cursorImageDF 	= ResourceLoader.loadImage("cursors/cursor_rotate_default.png");
		
		this.cursorNW = new GraphicsImageCursor(cursorImageNW, 0, 0, cursorImageNW.getWidth(), cursorImageNW.getHeight());
		this.cursorNE = new GraphicsImageCursor(cursorImageNE, 0, 0, cursorImageNE.getWidth(), cursorImageNE.getHeight());
		this.cursorSW = new GraphicsImageCursor(cursorImageSW, 0, 0, cursorImageSW.getWidth(), cursorImageSW.getHeight());
		this.cursorSE = new GraphicsImageCursor(cursorImageSE, 0, 0, cursorImageSE.getWidth(), cursorImageSE.getHeight());
		this.cursorDF = new GraphicsImageCursor(cursorImageDF, 0, 0, cursorImageDF.getWidth(), cursorImageDF.getHeight());
	}
	
	public void saveImageWithRotation()
	{
		if(selectedLayer == null)
			return;
		
		double lw = selectedLayer.getWidth(1);
		double lh = selectedLayer.getHeight(1);
		
		double a = 0, b = 0;
		if(Utils.inRange(angle, 0, 90) || Utils.inRange(angle, 180, 270))	b = 90 - (a = angle % 90);
		else 																a = 90 - (b = angle % 90);
		
		double ra = Math.toRadians(a);
		double rb = Math.toRadians(b);

		double w = Math.abs(lh*Math.cos(rb) + lw*Math.cos(ra));
		double h = Math.abs(lw*Math.sin(ra) + lh*Math.sin(rb));
		
		double sx = (w - lw) / 2;
		double sy = (h - lh) / 2;
		
		Canvas c = new Canvas(w, h);
		GraphicsContext gc = c.getGraphicsContext2D();

		Rotate r = new Rotate(angle, w/2, h/2);
		gc.transform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

		ImageLayer layer 	= (ImageLayer) selectedLayer;
		WritableImage img 	= layer.getImage();
		
		if(layer.getMask() != null)
			img = layer.getMask().getMaskedImage();
		
		gc.drawImage(img, sx, sy, img.getWidth(), img.getHeight());
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		
		WritableImage snap = c.snapshot(params, new WritableImage((int) w, (int) h));
		
		layer.rotate(0);
		layer.setImage(snap);
		layer.setSize(snap.getWidth(), snap.getHeight());
		layer.move(layer.getX(1) - sx, layer.getY(1) - sy);
		
		angle 	= 0;
		changed = true;
		
		reloadPoints();
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
				selectedLayer.rotate(angle = 0);
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
		   selectedLayer != null && selectedLayer.isVisible() && selectedLayer.isRotatable())
		{
			double scale = canvas.getScale();
			double lx = selectedLayer.getX(scale);
			double ly = selectedLayer.getY(scale);
			double lw = selectedLayer.getWidth(scale);
			double lh = selectedLayer.getHeight(scale);
			
			gc.save();
			gc.setStroke(Color.BLACK);
			gc.setLineDashes(5);
			
			Rotate rotate = new Rotate(angle, lx+lw/2, ly+lh/2);
			gc.transform(rotate.getMxx(), rotate.getMyx(),
						 rotate.getMxy(), rotate.getMyy(),
						 rotate.getTx(), rotate.getTy());
			
			gc.strokeRect(lx, ly, lw, lh);
			gc.restore();
			
			double backSize  = 40;
			double backShift = backSize/2;
			
			gc.save();
			gc.setFill(AlphaColor.create(Color.LIGHTBLUE, 100));
			gc.fillOval(p0.getX()-backShift, p0.getY()-backShift, backSize, backSize);
			gc.fillOval(p1.getX()-backShift, p1.getY()-backShift, backSize, backSize);
			gc.fillOval(p2.getX()-backShift, p2.getY()-backShift, backSize, backSize);
			gc.fillOval(p3.getX()-backShift, p3.getY()-backShift, backSize, backSize);
			
			double ovalSize  = 4;
			double ovalShift = ovalSize/2;
			
			gc.setFill(Color.BLACK);
			gc.fillOval(p0.getX()-ovalShift, p0.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p1.getX()-ovalShift, p1.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p2.getX()-ovalShift, p2.getY()-ovalShift, ovalSize, ovalSize);
			gc.fillOval(p3.getX()-ovalShift, p3.getY()-ovalShift, ovalSize, ovalSize);
			
			double ns = Math.sqrt(Math.pow(lw, 2) + Math.pow(lh, 2));
			
			gc.setLineDashes(5);
			gc.setStroke(Color.BLACK);
			gc.strokeOval(lx-(ns-lw)/2, ly-(ns-lh)/2, ns, ns);
			gc.restore();
		}

		cursor.draw(gc, null, 1);
	}

	@Override
	public void reset() {}
	
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
					saveImageWithRotation();
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
	
	@Override
	public void mouseMoved(MouseEvent event) 
	{
		if(canvas.isInitialized() && selectedLayer.isVisible())
		{
			double x = event.getX();
			double y = event.getY();
	
			double scale = canvas.getScale();
			double lw = selectedLayer.getWidth(scale);
			double lh = selectedLayer.getHeight(scale);
			double aw = Math.min(lw / 2, 50);
			double ah = Math.min(lh / 2, 50);
	
			int imgAngle = (int) Math.toDegrees(Math.atan((lh/2) / (lw/2)));
			if(Utils.inRange(x, p0.getX()-aw, p0.getX()) && Utils.inRange(y, p0.getY()-ah, p0.getY()))
			{
				cursorImage = cursorImageNW;
				cursor 		= cursorNW;
				addAngle	= 180-imgAngle;
			} 
			else if(Utils.inRange(x, p1.getX(), p1.getX()+aw) && Utils.inRange(y, p1.getY()-ah, p1.getY()))
			{
				cursorImage = cursorImageNE;
				cursor 		= cursorNE;
				addAngle	= imgAngle;
			}
			else if(Utils.inRange(x, p2.getX(), p2.getX()+aw) && Utils.inRange(y, p2.getY(), p2.getY()+ah))
			{
				cursorImage = cursorImageSW;
				cursor 		= cursorSW;
				addAngle	= 180+imgAngle;
			}
			else if(Utils.inRange(x, p3.getX()-aw, p3.getX()) && Utils.inRange(y, p3.getY(), p3.getY()+ah))
			{
				cursorImage = cursorImageSE;
				cursor 		= cursorSE;
				addAngle	= -imgAngle;
			}
			else
			{
				cursorImage = cursorImageDF;
				cursor 		= cursorDF;
			}
		}
		
		moveCursor(event);
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

		// Radius of the circle
		double ns = Math.sqrt(Math.pow(lw/2, 2) + Math.pow(lh/2, 2));
		// Origin X
		double ox = lx + lw/2;
		// Origin Y
		double oy = ly + lh/2;

		double imgAngle = Math.toDegrees(Math.atan((lh/2) / (lw/2)));
		double angle0 	= Math.toRadians(angle+180+imgAngle);
		double angle1	= Math.toRadians(angle-imgAngle);
		double angle2	= Math.toRadians(angle+180-imgAngle);
		double angle3	= Math.toRadians(angle+imgAngle);

		// Top-left point
		p0 = new Point2D(ox + ns*Math.cos(angle0), oy + ns*Math.sin(angle0));
		// Top-right point
		p1 = new Point2D(ox + ns*Math.cos(angle1), oy + ns*Math.sin(angle1));
		// Bottom-right point
		p2 = new Point2D(ox + ns*Math.cos(angle2), oy + ns*Math.sin(angle2));
		// Bottom-left point
		p3 = new Point2D(ox + ns*Math.cos(angle3), oy + ns*Math.sin(angle3));
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if(dragged)
		{
			double x = event.getX();
			double y = event.getY();
			
			double scale = canvas.getScale();
			double lx = selectedLayer.getX(scale);
			double ly = selectedLayer.getY(scale);
			double lw = selectedLayer.getWidth(scale);
			double lh = selectedLayer.getHeight(scale);

			double ox = lx + lw/2;
			double oy = ly + lh/2;

			double kx = x - ox;
			double ky = y - oy;

			double fi = Math.toDegrees(Math.atan2(ky, kx)) + addAngle;
			if(fi < 0) fi += 360;
			
			selectedLayer.rotate(angle = fi);
			reloadPoints();
			
			editor.setToolInfo(Translation.getTranslation("tools.info.rotate",
					Double.toString(Math.round(angle * 1000.0) / 1000.0)));
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
		if(!selectedLayer.isRotatable() || !selectedLayer.isVisible())
			return;
		
		double x = event.getX();
		double y = event.getY();

		double scale = canvas.getScale();
		double lw = selectedLayer.getWidth(scale);
		double lh = selectedLayer.getHeight(scale);
		double aw = Math.min(lw / 2, 50);
		double ah = Math.min(lh / 2, 50);

		if((Utils.inRange(x, p0.getX()-aw, p0.getX()) && Utils.inRange(y, p0.getY()-ah, p0.getY())) ||
		   (Utils.inRange(x, p1.getX(), p1.getX()+aw) && Utils.inRange(y, p1.getY()-ah, p1.getY())) ||
		   (Utils.inRange(x, p2.getX(), p2.getX()+aw) && Utils.inRange(y, p2.getY(), p2.getY()+ah)) ||
		   (Utils.inRange(x, p3.getX()-aw, p3.getX()) && Utils.inRange(y, p3.getY(), p3.getY()+ah)))
			dragged = true;
	}

	@Override
	public void mouseUp(MouseEvent event)
	{
		dragged = false;
	}

	@Override
	public void mouseEntered(MouseEvent event)
	{
		cursorImage = cursorImageDF;
		cursor 		= cursorDF;
		
		selectedLayer = layersManager.getSelectedLayer();
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