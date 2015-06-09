/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.plugin;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sune.apps.ie.Editor;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.translation.Translation;

public abstract class WindowPlugin extends Plugin
{
	protected Editor editor;
	
	protected Stage stage;
	protected BorderPane pane;
	protected Scene scene;
	
	public WindowPlugin(String name, Editor editor, double width, double height)
	{
		super(name);
		
		this.editor = editor;
		this.stage  = new Stage();
		this.pane   = new BorderPane();
		this.scene  = new Scene(pane, width, height);
		
		this.stage.setResizable(false);
		this.stage.setTitle(Translation.getTranslation(
			"plugins." + getName() + ".window.title"
		));
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.getIcons().add(ResourceLoader.loadImage("icon.png"));
		this.stage.initOwner(editor.getStage());
		this.stage.initModality(Modality.APPLICATION_MODAL);
		
		this.stage.setScene(scene);
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				unload();
			}
		});
	}
	
	@Override
	public abstract void load();
	
	@Override
	public abstract void unload();
	
	public void show()
	{
		stage.sizeToScene();
		stage.centerOnScreen();
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
}