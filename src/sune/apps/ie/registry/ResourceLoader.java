/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

public class ResourceLoader
{
	public static URL getResource(String path)
	{
		return ResourceLoader.class.getResource(PathSystem.RESOURCES_FOLDER + path);
	}
	
	public static InputStream getResourceAsInputStream(String path)
	{
		return ResourceLoader.class.getResourceAsStream(PathSystem.RESOURCES_FOLDER + path);
	}
	
	public static WritableImage loadImage(String path)
	{
		try
		{
			BufferedImage bimg = ImageIO.read(getResource(path));
			WritableImage wimg = new WritableImage(bimg.getWidth(), bimg.getHeight());
			
			return SwingFXUtils.toFXImage(bimg, wimg);
		}
		catch(Exception ex) {}
		
		return null;
	}
	
	public static File loadFile(String path)
	{
		try
		{
			File file = File.createTempFile("sie_", ".tmp");
			file.deleteOnExit();
			
			InputStream is 		= getResourceAsInputStream(path);
			FileOutputStream os = new FileOutputStream(file);
			byte[] buffer 		= new byte[4096];
			
			int read;
			while((read = is.read(buffer)) != -1)
				os.write(buffer, 0, read);
			
			os.close();
			is.close();
			
			return file;
		}
		catch(Exception ex) {}
		
		return null;
	}
	
	public static String loadStylesheet(String path)
	{
		return getResource(path).toExternalForm();
	}
}