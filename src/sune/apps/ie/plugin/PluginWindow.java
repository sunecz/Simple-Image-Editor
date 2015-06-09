/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.plugin;

import java.awt.event.ActionListener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javax.swing.Timer;

import sune.apps.ie.Editor;
import sune.apps.ie.graphics.ImageUtils;
import sune.apps.ie.image.IImage;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.ProgressBackups;
import sune.apps.ie.registry.ThreadRegistry;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public abstract class PluginWindow extends WindowPlugin
{
	protected WritableImage imageCopy;
	protected IImage image;
	
	protected boolean applied;
	protected boolean preview;
	
	protected VBox box;
	protected CheckBox chbPreview;
	protected Button btnOK;
	
	private Thread threadApply;
	protected volatile boolean forceApply;
	protected volatile boolean isApplied;
	protected boolean running;
	
	public PluginWindow(String name, Editor editor, double width, double height)
	{
		super(name, editor, width, height);

		this.applied 	= false;
		this.preview 	= true;
		this.isApplied	= false;
		this.running	= true;
		
		this.timerApply.setRepeats(false);
	}
	
	@Override
	public void load()
	{
		getLayerImage();

		box  		= new VBox(5);
		btnOK		= new Button(Translation.getTranslation("general.texts.ok"));
		chbPreview	= new CheckBox(Translation.getTranslation("general.texts.preview"));
		
		pane.setPadding(new Insets(10));
		btnOK.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				applied = true;
				apply();
				
				while(!isApplied) {}
				unload();
			};
		});
		
		chbPreview.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				preview = newValue.booleanValue();
				apply();
			}
		});
		
		chbPreview.setSelected(preview);
		
		HBox hbox = new HBox();
		Pane p1 = new Pane();
		HBox.setHgrow(p1, Priority.ALWAYS);
		
		hbox.getChildren().add(chbPreview);
		hbox.getChildren().add(p1);
		hbox.getChildren().add(btnOK);
		
		pane.setBottom(hbox);
		pane.setCenter(box);
		
		startThreadApply();
	}
	
	@Override
	public void unload()
	{
		if(!applied)	reset();
		else			ProgressBackups.createBackup(editor);
		
		redraw();
		imageCopy = null;
		image 	  = null;
		applied	  = false;
		preview   = true;
		running	  = false;
		
		threadApply.interrupt();
		editor.reloadLayers();
	}
	
	public void getLayerImage()
	{
		LayersManager layersManager = editor.getCanvas().getLayersManager();
		if(layersManager.getLayers().size() > 0)
		{
			Layer selectedLayer = layersManager.getSelectedLayer();
			if(selectedLayer instanceof ImageLayer)
			{
				ImageLayer imageLayer 	= (ImageLayer) selectedLayer;
				Image layerImage 		= imageLayer.getImage();
				
				if(imageLayer.getMask() != null)
					layerImage = imageLayer.getMask().getMaskedImage();
				
				imageCopy = ImageUtils.copyImage(layerImage);
				image 	  = new IImage(layerImage);
			}
		}
	}
	
	private Timer timerApply = new Timer(300, new ActionListener()
	{
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			forceApply = true;
		}
	});
	
	private void startThreadApply()
	{
		running 	= true;
		threadApply = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(running)
				{
					if(forceApply)
					{
						isApplied  = false;
						
						reset();
						if(preview)
						{
							applyToImage();
							
							/*Fixes bug with no preview at layers with mask*/
							Layer layer = editor.getCanvas().getLayersManager().getSelectedLayer();
							if(layer instanceof ImageLayer)
							{
								ImageLayer imageLayer = (ImageLayer) layer;
								
								if(imageLayer.getMask() != null)
								{
									imageLayer.setImage(image.getImage());
									imageLayer.getMask().setMaskedImage(image.getImage());
								}
							}
						}
						redraw();
						
						isApplied  = true;
						forceApply = false;
					}
					
					Utils.sleep(1);
				}
			}
		});
		
		ThreadRegistry.registerThreadAndStart(threadApply);
	}
	
	public void redraw()
	{
		editor.getCanvas().redraw();
	}
	
	public void apply()
	{
		if(timerApply.isRunning())	timerApply.restart();
		else						timerApply.start();
	}
	
	public abstract void applyToImage();
	
	public void reset()
	{
		if(imageCopy == null)
			return;
		
		LayersManager layersManager = editor.getCanvas().getLayersManager();
		if(layersManager.getLayers().size() > 0)
		{
			Layer selectedLayer = layersManager.getSelectedLayer();
			if(selectedLayer instanceof ImageLayer)
			{
				ImageLayer imageLayer = (ImageLayer) selectedLayer;

				image = new IImage(ImageUtils.copyImage(imageCopy));
				imageLayer.setImage(image.getImage());
				
				if(imageLayer.getMask() != null)
					imageLayer.getMask().setMaskedImage(image.getImage());
			}
		}
		
		editor.getCanvas().getTool().unload();
		editor.getCanvas().getTool().reset();
	}
}