/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.Utils;

public class Dialog
{
	private static boolean isShown 		 = false;
	private static boolean emergencyMode = false;
	
	private String title;
	private String message;
	
	private double width;
	private double height;
	
	public static void setEmergencyMode(boolean flag)
	{
		emergencyMode = flag;
	}
	
	public Dialog(String title, String message)
	{
		this(title, message, 300, 80);
	}
	
	public Dialog(String title, String message, double width, double height)
	{
		this.title 	 = title;
		this.message = message;
		this.width	 = width;
		this.height  = height;
		this.init();
	}
	
	public double calculateApproximateLabelHeight(Label label)
	{
		double fontSize = label.getFont().getSize();
		double spacing	= 5;
		int length 		= message.length();
		int maxOnLine	= (int) Math.floor(width / (fontSize / Math.sqrt(3)));
		int lines		= (int) Math.ceil(length / maxOnLine);
		
		return (lines+1)*(fontSize+spacing);
	}
	
	public void autosizeLabel(Label label, double width, double height)
	{
		if(width <= 0) return;
		
		double fontSize = label.getFont().getSize();
		double spacing	= 5;
		int length 		= message.length();
		int maxOnLine	= (int) Math.floor(width / (fontSize / Math.sqrt(3)));
		int lines		= (int) Math.ceil(length / maxOnLine);

		StringBuilder sb = new StringBuilder();
		for(int line = 0; line <= lines; line++)
		sb.append(message.substring(line*maxOnLine, 
				Math.min((line+1)*maxOnLine, length)))
		  .append(line == lines ? "" : "\n");
		
		double lblHeight = (lines+1)*(fontSize+spacing);
		label.setText(sb.toString());
		label.setPrefWidth(width);
		label.setPrefHeight(lblHeight);
		label.setMinWidth(width);
		label.setMinHeight(lblHeight);
	}
	
	private void init()
	{
		if(isShown)
			return;
			
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.getIcons().add(Utils.APP_ICON);
		stage.setResizable(false);
		stage.initModality(Modality.APPLICATION_MODAL);
		
		BorderPane pane = new BorderPane();
		
		VBox lblBox = new VBox();
		lblBox.setPadding(new Insets(10));
		lblBox.setAlignment(Pos.CENTER_LEFT);
		
		Label label = new Label(message);
		label.setWrapText(true);
		
		lblBox.getChildren().add(label);
		pane.setCenter(lblBox);
		
		HBox btnBox = new HBox();
		btnBox.setPadding(new Insets(10));
		btnBox.setAlignment(Pos.CENTER_RIGHT);
		
		Button button = new Button(
			emergencyMode ? "Ok" : Translation.getTranslation("general.texts.ok"));
		btnBox.getChildren().add(button);
		pane.setBottom(btnBox);
		
		button.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				isShown = false;
				stage.close();
			}
		});
		
		Scene scene = new Scene(pane, width, height + calculateApproximateLabelHeight(label));
		
		if(!emergencyMode)
		{
			scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		}
		
		stage.setScene(scene);
		stage.sizeToScene();
		stage.centerOnScreen();

		isShown = true;
		stage.showAndWait();
	}
	
	public static boolean isShown()
	{
		return isShown;
	}
}