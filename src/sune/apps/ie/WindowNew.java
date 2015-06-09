/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import sune.apps.ie.component.IScrollPane;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.graphics.Colored;
import sune.apps.ie.image.ColorConverter;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class WindowNew extends Window implements Colored
{
	private Editor editor;
	private Color backColor;
	
	private BorderPane pane;
	private GridPane paneMain;
	private HBox hboxButtons;
	
	private Label lblWidth;
	private Label lblHeight;
	private TextField txtWidth;
	private TextField txtHeight;
	private Label lblWidthUnits;
	private Label lblHeightUnits;
	private Button btnCreate;
	private Label lblBackColor;
	private ComboBox<BackColor> cmbBackColor;
	
	private enum BackColor
	{
		WHITE(Color.WHITE),
		BLACK(Color.BLACK),
		TRANSPARENT(Color.TRANSPARENT),
		CUSTOM;
		
		private Color color;
		
		private BackColor() {}
		private BackColor(Color color)
		{
			this.color = color;
		}
		
		public Color getColor() 
		{
			return color;
		}
		
		@Override
		public String toString()
		{
			return Translation.getTranslation("misc.backColor." + name());
		}
	}
	
	public WindowNew(Editor editor)
	{
		super();
		
		this.editor		= editor;
		this.backColor 	= BackColor.TRANSPARENT.getColor();
		
		this.pane 	= new BorderPane();
		this.scene 	= new Scene(pane, 300, 150);
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.setTitle(Translation.getTranslation("windows.createNewFile.title"));
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
		
		this.lblWidth  		= new Label(Translation.getTranslation("windows.createNewFile.labels.width"));
		this.lblHeight 		= new Label(Translation.getTranslation("windows.createNewFile.labels.height"));
		this.lblBackColor	= new Label(Translation.getTranslation("windows.createNewFile.labels.backColor"));
		
		this.txtWidth 	= new NumberTextField("1");
		this.txtHeight 	= new NumberTextField("1");
		
		this.lblWidthUnits 	= new Label("px");
		this.lblHeightUnits = new Label("px");
		
		this.cmbBackColor = new ComboBox<>();
		this.cmbBackColor.setPrefSize(150, 20);
		this.cmbBackColor.getItems().addAll(BackColor.values());
		this.cmbBackColor.getSelectionModel().select(BackColor.TRANSPARENT);
		
		this.cmbBackColor.valueProperty().addListener(new ChangeListener<BackColor>()
		{
			@Override
			public void changed(ObservableValue<? extends BackColor> observable, BackColor oldValue, BackColor newValue)
			{
				if(newValue == BackColor.CUSTOM)	(new WindowColorPalette(editor, WindowNew.this)).show();
				else								backColor = newValue.getColor();
			}
		});
		
		EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.getCode() == KeyCode.ENTER)
				{
					createNewCanvas();
				}
			}
		};
		
		this.txtWidth.addEventHandler(KeyEvent.KEY_PRESSED, handler);
		this.txtHeight.addEventHandler(KeyEvent.KEY_PRESSED, handler);

		this.btnCreate = new Button(Translation.getTranslation("windows.createNewFile.buttons.btnCreate"));
		this.btnCreate.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				createNewCanvas();
			}
		});

		Pane p2 = new Pane();
		HBox.setHgrow(p2, Priority.ALWAYS);
		this.hboxButtons.getChildren().add(p2);
		this.hboxButtons.getChildren().add(btnCreate);
		
		GridPane.setConstraints(lblWidth, 0, 0);
		GridPane.setConstraints(txtWidth, 1, 0);
		GridPane.setConstraints(lblWidthUnits, 2, 0);
		GridPane.setConstraints(lblHeight, 0, 1);
		GridPane.setConstraints(txtHeight, 1, 1);
		GridPane.setConstraints(lblHeightUnits, 2, 1);
		GridPane.setConstraints(lblBackColor, 0, 2);
		GridPane.setConstraints(cmbBackColor, 1, 2);
		
		GridPane.setHgrow(lblWidth, Priority.ALWAYS);
		GridPane.setHgrow(lblHeight, Priority.ALWAYS);
		GridPane.setHgrow(lblBackColor, Priority.ALWAYS);
		
		GridPane.setMargin(txtWidth, new Insets(0, 5, 0, 5));
		GridPane.setMargin(txtHeight, new Insets(5, 5, 0, 5));
		GridPane.setMargin(cmbBackColor, new Insets(5, 5, 0, 5));
		
		this.paneMain.getChildren().add(lblWidth);
		this.paneMain.getChildren().add(txtWidth);
		this.paneMain.getChildren().add(lblWidthUnits);
		
		this.paneMain.getChildren().add(lblHeight);
		this.paneMain.getChildren().add(txtHeight);
		this.paneMain.getChildren().add(lblHeightUnits);
		
		this.paneMain.getChildren().add(lblBackColor);
		this.paneMain.getChildren().add(cmbBackColor);
		
		this.pane.setCenter(paneMain);
		this.pane.setBottom(hboxButtons);
	}
	
	private void createNewCanvas()
	{
		String strWidth  = txtWidth.getText();
		String strHeight = txtHeight.getText();
		
		if(strWidth.trim().isEmpty() || strHeight.trim().isEmpty())
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("messages.createNewFile.emptySizes"));
			return;
		}

		double width  = Double.parseDouble(strWidth);
		double height = Double.parseDouble(strHeight);
		
		if(width <= 0 || height <= 0)
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("messages.createNewFile.positiveSizes"));
			return;
		}
		
		if(width > IScrollPane.MAX_WIDTH || height > IScrollPane.MAX_HEIGHT)
		{
			new Dialog(Translation.getTranslation("titles.error"),
					   Translation.getTranslation("messages.largeImage",
							  Integer.toString(IScrollPane.MAX_WIDTH),
							  Integer.toString(IScrollPane.MAX_HEIGHT)));
			return;
		}
		
		editor.getCanvas().create(width, height, backColor, true);
		editor.resizeCanvas();
		editor.resetOpenedFile();
		editor.getScrollPane().scrollToCenter();
		stage.close();
	}
	
	@Override
	public void show()
	{
		super.show();
		this.stage.sizeToScene();
		this.stage.centerOnScreen();
	}
	
	@Override
	public void setColor(Color color)
	{
		backColor = color;
	}

	@Override
	public void setColor(int argb)
	{
		setColor(ColorConverter.IntToFXColor(argb));
	}

	@Override
	public int getColor()
	{
		return ColorConverter.FXColorToInt(backColor);
	}
}