/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Window
{
	protected Stage stage;
	protected Pane pane;
	protected Scene scene;
	
	public Window()
	{
		this(new Stage());
	}
	
	public Window(Stage stage)
	{
		this.stage = stage;
		this.pane  = new StackPane();
		this.scene = new Scene(pane, 500, 500);
		this.stage.setScene(scene);
	}
	
	public void show()
	{
		stage.show();
	}
	
	public void hide()
	{
		stage.hide();
	}
	
	public void close()
	{
		stage.close();
	}
	
	public Stage getStage()
	{
		return stage;
	}
	
	public Scene getScene()
	{
		return scene;
	}
	
	public Pane getMainPane()
	{
		return pane;
	}
}