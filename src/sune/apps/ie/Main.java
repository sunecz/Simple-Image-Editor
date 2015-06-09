/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application
{
	public static final String VERSION 	 = "1.1";
	public static final String COPYRIGHT = "v" + VERSION + " © Petr Cipra (Sune)";
	
	@Override
	public void start(Stage stage) throws Exception
	{
		Platform.runLater(() ->
		{
			new WindowStartup();
		});
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
}