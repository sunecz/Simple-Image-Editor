/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sune.apps.ie.component.Dialog;
import sune.apps.ie.plugin.PluginLoader;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.registry.Resources;
import sune.apps.ie.registry.ThreadRegistry;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.AlphaColor;
import sune.apps.ie.util.Utils;
import sune.utils.ssdf.SSDFCore;

public class WindowStartup extends Window
{
	private Pane pane;
	private Canvas canvasLogo;
	private Label lblVersion;
	private GridPane bottomBox;
	private Label lblStatus;
	private ProgressBar progressBar;
	
	private boolean running 	= true;
	private boolean actionDone 	= true;
	private int totalActions	= 4;
	private int actionCount 	= 0;
	
	private int logoWidth  = 600;
	private int logoHeight = 400;
	
	private final String[] FILES_TO_COPY =
	{
		"languages/CZ.ssdf",
		"languages/EN.ssdf",
		"styles/default.css"
	};
	
	private static double[] getVersion()
	{
	    String version  = System.getProperty("java.version");
	    Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)");
	    Matcher matcher = pattern.matcher(version);
	    double[] data	= new double[4];
	    
	    if(matcher.matches())
	    {
	    	for(int i = 0; i < data.length; i++)
	    		data[i] = Double.parseDouble(matcher.group(i+1));
	    }
	    
	    return data;
	}
	
	public WindowStartup()
	{
		double[] versionData = getVersion();
		/*Check the Java version because some features are for Java 1.8u45+.*/
		if(versionData[0] < 1 || versionData[1] < 8 || versionData[3] < 45)
		{
			Dialog.setEmergencyMode(true);
			new Dialog("Warning",
				"Java version is older than the supported version (Java 1.8u45).\nSome features may not work properly.",
				350, 80);
			Dialog.setEmergencyMode(false);
		}
		
		for(String path : FILES_TO_COPY)
		{
			try
			{
				File outputFile = new File(
					PathSystem.getFullPath(PathSystem.RESOURCES_FOLDER + path));
				
				if(!outputFile.exists())
				{
					new File(outputFile.getParent()).mkdirs();
					outputFile.createNewFile();
					
					InputStream originalFile = ResourceLoader.getResourceAsInputStream(path);
					BufferedInputStream originalStream = new BufferedInputStream(originalFile);
					BufferedOutputStream outputStream  = new BufferedOutputStream(
						new FileOutputStream(outputFile));
					
					byte[] buffer = new byte[4096];
					int readBytes = 0;
					
					while((readBytes = originalStream.read(buffer)) != -1)
						outputStream.write(buffer, 0, readBytes);
					
					originalStream.close();
					outputStream.flush();
					outputStream.close();
				}
			}
			catch(Exception ex) { ex.printStackTrace(); }
		}
		
		Configuration.init();
		Resources.initialize();
		
		for(File file : Resources.LANGUAGES)
			Translation.registerTranslation(file);
		
		Translation.loadTranslation(new File(
			Resources.PATH_LANGUAGES_FOLDER +
			Configuration.getStringProperty("language") + ".ssdf"));
		
		this.stage.initStyle(StageStyle.TRANSPARENT);
		
		this.pane  = new Pane();
		this.scene = new Scene(pane, logoWidth, logoHeight);
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.scene.setCursor(Cursor.WAIT);
		
		this.stage.setScene(scene);
		this.stage.getIcons().add(Utils.APP_ICON);
		this.stage.setTitle(Translation.getTranslation("APP_TITLE", Main.VERSION));
		this.stage.setResizable(false);
		
		this.canvasLogo  = new Canvas(logoWidth, logoHeight);
		this.lblVersion	 = new Label(Main.COPYRIGHT);
		this.bottomBox 	 = new GridPane();
		this.lblStatus 	 = new Label(Translation.getTranslation("windows.windowStartup.loading"));
		this.progressBar = new ProgressBar();

		this.bottomBox.setPadding(new Insets(0, 5, 0, 5));
		GridPane.setConstraints(lblStatus, 0, 0);
		GridPane.setConstraints(progressBar, 0, 1);
		GridPane.setMargin(progressBar, new Insets(1, 0, 5, 0));
		
		this.bottomBox.setBackground(new Background(new BackgroundFill(
				AlphaColor.create(Color.WHITE, 0), CornerRadii.EMPTY, new Insets(0))));
		this.progressBar.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
		this.progressBar.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
		this.progressBar.setPrefSize(logoWidth-10, 10);
		this.bottomBox.setVisible(false);
		GridPane.setFillWidth(progressBar, true);
		
		this.lblVersion.setFont(new Font(12));
		this.lblVersion.setId("label-version");
		this.lblVersion.setPadding(new Insets(4, 6, 4, 6));
		
		this.bottomBox.getChildren().add(lblStatus);
		this.bottomBox.getChildren().add(progressBar);
		
		this.pane.getChildren().add(canvasLogo);
		this.pane.getChildren().add(bottomBox);
		this.pane.getChildren().add(lblVersion);
		
		double canvasWidth = canvasLogo.getWidth();
		double canvasHeight = canvasLogo.getHeight();
		
		GraphicsContext gc = canvasLogo.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvasWidth, canvasHeight);
		gc.drawImage(ResourceLoader.loadImage("logo.png"), 0, 0);
		gc.setStroke(Color.BLACK);
		gc.strokeRect(0, 0, canvasWidth, canvasHeight);
		
		show();
		// Set the positions after all the sizes is calculated
		this.bottomBox.setLayoutY(logoHeight - bottomBox.getHeight());
		this.lblVersion.setLayoutX(logoWidth - lblVersion.getWidth());
		
		this.stage.sizeToScene();
		this.stage.centerOnScreen();
		
		ThreadRegistry.registerThreadAndStart(thread);
	}
	
	private Thread thread = new Thread(() ->
	{
		while(running && ThreadRegistry.isRunning)
		{
			if(actionDone)
			{
				nextAction();
				actionCount++;
			}
			
			Utils.sleep(1);
		}
	});
	
	private void setStatus(String text)
	{
		Utils.run(() -> { lblStatus.setText(text); });
	}
	
	private void setProgressBarValue()
	{
		double val = (double) (actionCount+1) / totalActions;
		Utils.run(() -> { progressBar.setProgress(val); });
	}
	
	private void nextAction()
	{
		actionDone = false;
		switch(actionCount)
		{
			case 0:
				Utils.run(() -> { bottomBox.setVisible(true); });
				setStatus(Translation.getTranslation("windows.windowStartup.loadingApp"));
				setProgressBarValue();
				actionDone = true;
				break;
			case 1:
				if(Boolean.valueOf(Configuration.getStringProperty("checkUpdates")))
				{
					setStatus(Translation.getTranslation("windows.windowStartup.checkingNewVersion"));
					
					String webContent;
					if((webContent = Utils.getURLContent(Utils.WEB_URL + "info.ssdf", 5000)) != null)
					{
						SSDFCore reader 		= new SSDFCore(webContent);
						String latestVersion 	= reader.getObject("version.latest").getValue();
						String currentVersion 	= Main.VERSION;
						
						try
						{
							double valLatest  = Double.parseDouble(latestVersion);
							double valCurrent = Double.parseDouble(currentVersion);
							
							if(valLatest > valCurrent)
							{
								Utils.run(() ->
								{
									Alert alert = new Alert(AlertType.CONFIRMATION,
											Translation.getTranslation("windows.windowStartup.confirmNewVersion"),
										ButtonType.YES, ButtonType.NO);
									
									Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
									stage.getIcons().add(Utils.APP_ICON);
									alert.setTitle(Translation.getTranslation("windows.windowStartup.title"));
									alert.setHeaderText(Translation.getTranslation("windows.windowStartup.headerText"));
									alert.initOwner(WindowStartup.this.stage);
									alert.showAndWait();
									
									ButtonType result = alert.getResult();
									if(result == ButtonType.YES) goToWeb();
									else						 actionDone = true;
								});
							}
							else
							{
								actionDone = true;
							}
						}
						catch(Exception ex)
						{
							actionDone = true;
						}
					}
					else
					{
						actionDone = true;
					}
					
					setProgressBarValue();
				}
				else
				{
					setProgressBarValue();
					actionDone = true;
				}
				
				break;
			case 2:
				setStatus(Translation.getTranslation("windows.windowStartup.loadingPlugins"));
				setProgressBarValue();
				
				File pluginFolder = new File(PathSystem.DIRECTORY + "resources/plugins");
				if(!pluginFolder.exists()) pluginFolder.mkdirs();
				PluginLoader.registerAllPlugins(pluginFolder.getAbsolutePath());
				
				actionDone = true;
				break;
			case 3:
				setStatus(Translation.getTranslation("windows.windowStartup.loadingApp"));
				setProgressBarValue();
				Utils.sleep(500);
				showApplication();
				actionDone = true;
				break;
		}
	}
	
	private void showApplication()
	{
		Utils.run(() ->
		{
			new Editor().show();
			close();
		});
	}
	
	private void goToWeb()
	{
		try
		{
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(new URI(Utils.WEB_URL));
			actionDone = true;
		}
		catch (Exception ex) {}
	}
}