/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import sune.apps.ie.component.ColorPalettePanel;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.graphics.Colored;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class WindowColorPalette extends Window
{
	private Colored coloredNode;
	
	private BorderPane pane;
	private ColorPalettePanel panelCP;
	private VBox vboxMain;
	private HBox hboxInfo;
	private GridPane paneInfo;
	private HBox hboxButtons;
	
	private Label lblRed;
	private Label lblGreen;
	private Label lblBlue;
	private Label lblHue;
	private Label lblSaturation;
	private Label lblLightness;
	
	private TextField txtRed;
	private TextField txtGreen;
	private TextField txtBlue;
	private TextField txtHue;
	private TextField txtSaturation;
	private TextField txtLightness;
	
	private Button btnSelect;
	private boolean inited;
	
	public WindowColorPalette(Editor editor, Colored coloredNode)
	{
		super();
		
		this.coloredNode = coloredNode;
		this.pane 		 = new BorderPane();
		this.scene 		 = new Scene(pane, 300, 400);
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.setTitle(Translation.getTranslation("windows.windowCP.title"));
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
		
		this.vboxMain = new VBox(5);
		this.hboxInfo = new HBox(5);
		this.paneInfo = new GridPane();
		this.hboxButtons = new HBox(5);
		
		this.hboxInfo.setPadding(new Insets(0, 10, 5, 10));
		this.hboxButtons.setPadding(new Insets(10));
		
		this.panelCP = new ColorPalettePanel(editor, 261, 30, 250)
		{
			@Override
			protected void setColor()
			{
				if(!inited) return;
				txtRed.setText(Integer.toString((int) (color.getRed() * 255)));
				txtGreen.setText(Integer.toString((int) (color.getGreen() * 255)));
				txtBlue.setText(Integer.toString((int) (color.getBlue() * 255)));
				txtHue.setText(Integer.toString((int) color.getHue()));
				txtSaturation.setText(Integer.toString((int) (color.getSaturation() * 100)));
				txtLightness.setText(Integer.toString((int) (color.getBrightness() * 100)));
			}
		};
		
		this.lblRed 		= new Label(Translation.getTranslation("windows.windowCP.labels.red"));
		this.lblGreen 		= new Label(Translation.getTranslation("windows.windowCP.labels.green"));
		this.lblBlue 		= new Label(Translation.getTranslation("windows.windowCP.labels.blue"));
		this.lblHue 		= new Label(Translation.getTranslation("windows.windowCP.labels.hue"));
		this.lblSaturation 	= new Label(Translation.getTranslation("windows.windowCP.labels.saturation"));
		this.lblLightness 	= new Label(Translation.getTranslation("windows.windowCP.labels.lightness"));
		
		this.txtRed 		= new NumberTextField("255");
		this.txtGreen 		= new NumberTextField("255");
		this.txtBlue 		= new NumberTextField("255");
		this.txtHue 		= new NumberTextField("0");
		this.txtSaturation 	= new NumberTextField("0");
		this.txtLightness 	= new NumberTextField("100");
		
		this.txtRed.setPrefColumnCount(3);
		this.txtGreen.setPrefColumnCount(3);
		this.txtBlue.setPrefColumnCount(3);
		this.txtHue.setPrefColumnCount(3);
		this.txtSaturation.setPrefColumnCount(3);
		this.txtLightness.setPrefColumnCount(3);

		ChangeListener<Boolean> listenerFocus = new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				String strRed 	= txtRed.getText();
				String strGreen = txtGreen.getText();
				String strBlue 	= txtBlue.getText();
				
				if(!strRed.isEmpty() && !strGreen.isEmpty() && !strBlue.isEmpty())
				{
					int intRed 	 = Math.max(0, Math.min(Integer.parseInt(strRed), 255));
					int intGreen = Math.max(0, Math.min(Integer.parseInt(strGreen), 255));
					int intBlue  = Math.max(0, Math.min(Integer.parseInt(strBlue), 255));
					
					panelCP.setPickerColor(Color.rgb(intRed, intGreen, intBlue));
				}
			}
		};

		this.txtRed.focusedProperty().addListener(listenerFocus);
		this.txtGreen.focusedProperty().addListener(listenerFocus);
		this.txtBlue.focusedProperty().addListener(listenerFocus);
		
		this.txtHue.setEditable(false);
		this.txtSaturation.setEditable(false);
		this.txtLightness.setEditable(false);
		
		this.btnSelect = new Button(Translation.getTranslation("windows.windowCP.buttons.btnSelect"));
		this.btnSelect.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				selectColor();
			}
		});
		
		Pane p2 = new Pane();
		HBox.setHgrow(p2, Priority.ALWAYS);
		this.hboxButtons.getChildren().add(p2);
		this.hboxButtons.getChildren().add(btnSelect);
		
		this.paneInfo.getChildren().add(lblRed);
		this.paneInfo.getChildren().add(txtRed);
		GridPane.setConstraints(lblRed, 0, 0);
		GridPane.setConstraints(txtRed, 1, 0);
		GridPane.setMargin(lblRed, new Insets(0, 0, 0, 5));
		GridPane.setMargin(txtRed, new Insets(0, 50, 0, 20));

		this.paneInfo.getChildren().add(lblGreen);
		this.paneInfo.getChildren().add(txtGreen);
		GridPane.setConstraints(lblGreen, 0, 1);
		GridPane.setConstraints(txtGreen, 1, 1);
		GridPane.setMargin(lblGreen, new Insets(0, 0, 0, 5));
		GridPane.setMargin(txtGreen, new Insets(5, 50, 0, 20));
		
		this.paneInfo.getChildren().add(lblBlue);
		this.paneInfo.getChildren().add(txtBlue);
		GridPane.setConstraints(lblBlue, 0, 2);
		GridPane.setConstraints(txtBlue, 1, 2);
		GridPane.setMargin(lblBlue, new Insets(0, 0, 0, 5));
		GridPane.setMargin(txtBlue, new Insets(5, 50, 0, 20));
		
		this.paneInfo.getChildren().add(lblHue);
		this.paneInfo.getChildren().add(txtHue);
		GridPane.setConstraints(lblHue, 2, 0);
		GridPane.setConstraints(txtHue, 3, 0);
		GridPane.setMargin(txtHue, new Insets(5, 0, 0, 20));
		
		this.paneInfo.getChildren().add(lblSaturation);
		this.paneInfo.getChildren().add(txtSaturation);
		GridPane.setConstraints(lblSaturation, 2, 1);
		GridPane.setConstraints(txtSaturation, 3, 1);
		GridPane.setMargin(txtSaturation, new Insets(5, 0, 0, 20));
		
		this.paneInfo.getChildren().add(lblLightness);
		this.paneInfo.getChildren().add(txtLightness);
		GridPane.setConstraints(lblLightness, 2, 2);
		GridPane.setConstraints(txtLightness, 3, 2);
		GridPane.setMargin(txtLightness, new Insets(5, 0, 0, 20));
		
		this.hboxInfo.getChildren().add(paneInfo);
		HBox.setHgrow(paneInfo, Priority.ALWAYS);
		
		this.vboxMain.getChildren().add(panelCP);
		this.vboxMain.getChildren().add(hboxInfo);
		
		this.pane.setCenter(vboxMain);
		this.pane.setBottom(hboxButtons);
		inited = true;
	}
	
	public WindowColorPalette setDefaultColor(Color color)
	{
		panelCP.setColor(color);
		return this;
	}
	
	private void selectColor()
	{
		coloredNode.setColor(panelCP.getPickerColor());
		stage.close();
	}
}