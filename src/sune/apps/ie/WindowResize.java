/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import sune.apps.ie.component.Dialog;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class WindowResize extends Window
{
	private Editor editor;
	
	private BorderPane pane;
	private GridPane paneMain;
	private HBox hboxButtons;
	
	private Label lblWidth;
	private Label lblHeight;
	private TextField txtWidth;
	private TextField txtHeight;
	private Label lblWidthUnits;
	private Label lblHeightUnits;
	private Button btnResize;
	
	public WindowResize(Editor editor)
	{
		super();
		
		this.editor	= editor;
		this.pane 	= new BorderPane();
		this.scene 	= new Scene(pane, 300, 150);
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.setTitle(Translation.getTranslation("windows.windowResize.title"));
		this.stage.getIcons().add(Utils.APP_ICON);
		this.stage.initOwner(editor.getStage());
		this.stage.initModality(Modality.APPLICATION_MODAL);
		this.stage.setResizable(false);
		this.stage.setScene(scene);
		
		this.scene.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.getCode() == KeyCode.ESCAPE)
					close();
			}
		});
		
		this.paneMain = new GridPane();
		this.paneMain.setPadding(new Insets(10));

		this.hboxButtons = new HBox(5);
		this.hboxButtons.setPadding(new Insets(10));
		
		this.lblWidth  		= new Label(Translation.getTranslation("windows.windowResize.labels.width"));
		this.lblHeight 		= new Label(Translation.getTranslation("windows.windowResize.labels.height"));
		
		this.txtWidth 	= new NumberTextField(Integer.toString((int) editor.getCanvas().getCanvasWidth()));
		this.txtHeight 	= new NumberTextField(Integer.toString((int) editor.getCanvas().getCanvasHeight()));
		
		this.lblWidthUnits 	= new Label("px");
		this.lblHeightUnits = new Label("px");
		
		EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.getCode() == KeyCode.ENTER)
				{
					resizeCanvas();
				}
			}
		};
		
		this.txtWidth.addEventHandler(KeyEvent.KEY_PRESSED, handler);
		this.txtHeight.addEventHandler(KeyEvent.KEY_PRESSED, handler);

		this.btnResize = new Button(Translation.getTranslation("windows.windowResize.buttons.btnResize"));
		this.btnResize.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				resizeCanvas();
			}
		});

		Pane p2 = new Pane();
		HBox.setHgrow(p2, Priority.ALWAYS);
		this.hboxButtons.getChildren().add(p2);
		this.hboxButtons.getChildren().add(btnResize);
		
		GridPane.setConstraints(lblWidth, 0, 0);
		GridPane.setConstraints(txtWidth, 1, 0);
		GridPane.setConstraints(lblWidthUnits, 2, 0);
		GridPane.setConstraints(lblHeight, 0, 1);
		GridPane.setConstraints(txtHeight, 1, 1);
		GridPane.setConstraints(lblHeightUnits, 2, 1);
		
		GridPane.setHgrow(lblWidth, Priority.ALWAYS);
		GridPane.setHgrow(lblHeight, Priority.ALWAYS);
		
		GridPane.setMargin(txtWidth, new Insets(0, 5, 0, 5));
		GridPane.setMargin(txtHeight, new Insets(5, 5, 0, 5));
		
		this.paneMain.getChildren().add(lblWidth);
		this.paneMain.getChildren().add(txtWidth);
		this.paneMain.getChildren().add(lblWidthUnits);
		
		this.paneMain.getChildren().add(lblHeight);
		this.paneMain.getChildren().add(txtHeight);
		this.paneMain.getChildren().add(lblHeightUnits);
		
		this.pane.setCenter(paneMain);
		this.pane.setBottom(hboxButtons);
	}
	
	private void resizeCanvas()
	{
		String strWidth  = txtWidth.getText();
		String strHeight = txtHeight.getText();
		
		if(strWidth.trim().isEmpty() || strHeight.trim().isEmpty())
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("messages.resizeImage.emptySizes"));
			return;
		}

		double width  = Double.parseDouble(strWidth);
		double height = Double.parseDouble(strHeight);
		
		if(width <= 0 || height <= 0)
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("messages.resizeImage.positiveSizes"));
			return;
		}
		
		CanvasPanel canvas 	  = editor.getCanvas();
		Layer backgroundLayer = canvas.getBackgroundLayer();
		double backWidth  	  = backgroundLayer.getWidth(1);
		double backHeight 	  = backgroundLayer.getHeight(1);
		
		double ratioWidth  = width / backWidth;
		double ratioHeight = height / backHeight;
		
		LayersManager layersManager = canvas.getLayersManager();
		List<Layer> layers 			= new ArrayList<>();
		
		for(Layer layer : layersManager.getLayers())
		{
			if(layer instanceof ImageLayer)
			{
				ImageLayer imageLayer 	= (ImageLayer) layer;
				double layerWidth		= imageLayer.getWidth(1);
				double layerHeight 		= imageLayer.getHeight(1);
				
				imageLayer.resize(layerWidth * ratioWidth, layerHeight * ratioHeight);
			}
			
			layers.add(layer);
		}
		
		canvas.create(width, height, Color.TRANSPARENT, false);
		for(Layer layer : layers)
		{
			layer.move(0, 0);
			canvas.getLayersManager().addLayer(layer);
		}
		
		editor.resizeCanvas();
		editor.resetOpenedFile();
		editor.getScrollPane().scrollToCenter();
		editor.reloadLayers();
		canvas.redraw();
		
		stage.close();
	}
	
	@Override
	public void show()
	{
		super.show();
		this.stage.sizeToScene();
		this.stage.centerOnScreen();
	}
}