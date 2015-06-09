/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;

import sune.apps.ie.Editor;
import sune.apps.ie.registry.TemporaryFile.TemporaryFileType;
import sune.apps.ie.util.ByteHelper;
import sune.apps.ie.util.Utils;
import sune.utils.ssdf.SSDArray;
import sune.utils.ssdf.SSDFCore;

public class ProgressBackups
{
	private static final List<String> BACKUP_FILES = new ArrayList<>();
	private static int CURRENT_POSITION = -1;
	
	public static final byte[] HEADER_DELIMITER
		= { (byte) 0xff, (byte) 0xd7, (byte) 0xe7, (byte) 0xf7 };
	public static final byte[] IMAGE_START_DELIMITER
		= { (byte) 0xff, (byte) 0xd8, (byte) 0xe8, (byte) 0xf8 };
	public static final byte[] IMAGE_END_DELIMITER
		= { (byte) 0xff, (byte) 0xd9, (byte) 0xe9, (byte) 0xf9 };
	
	private static boolean createBackup = false;
	private static Editor editorObject  = null;
	
	private static Thread threadCreate  = new Thread(() ->
	{
		while(ThreadRegistry.isRunning)
		{
			if(createBackup)
			{
				try
				{
					TemporaryFile tempFile = Resources.createTemporaryFile(
						TemporaryFileType.PROGRESS_BACKUP_FILE);
					
					if(tempFile.open())
					{
						tempFile.write(
							editorObject.buildProgressBackupFileContent());
						
						if(tempFile.close())
						{
							// It is not the last backup, but a change was made
							if(CURRENT_POSITION < BACKUP_FILES.size()-1)
							{
								for(int i = BACKUP_FILES.size()-1; i > CURRENT_POSITION; i--)
									BACKUP_FILES.remove(i);
							}
							
							BACKUP_FILES.add(tempFile.getPath());
							CURRENT_POSITION++;
						}
					}
				}
				catch(Exception ex) {}
				createBackup = false;
			}
			
			Utils.sleep(1);
		}
	});
	
	static
	{
		ThreadRegistry.registerThreadAndStart(threadCreate);
	}
	
	public static void createBackup(Editor editor)
	{
		if(editorObject == null)
			editorObject = editor;
		
		createBackup = true;
	}
	
	public static class ProgressBackup
	{
		public final String path;
		public final SSDArray data;
		public final List<WritableImage> images;
		
		protected ProgressBackup(String path, SSDArray data, List<WritableImage> images)
		{
			this.path 	= path;
			this.data 	= data;
			this.images = images;
		}
	}
	
	public static boolean hasNextBackup()
	{
		return CURRENT_POSITION+1 < BACKUP_FILES.size();
	}
	
	public static boolean hasPreviousBackup()
	{
		return BACKUP_FILES.size() > 0 && CURRENT_POSITION-1 >= 0;
	}
	
	public static int getCurrentPosition()
	{
		return CURRENT_POSITION;
	}
	
	public static ProgressBackup getNextBackup()
	{
		return getBackup(CURRENT_POSITION = CURRENT_POSITION+1);
	}
	
	public static ProgressBackup getPreviousBackup()
	{
		return getBackup(CURRENT_POSITION = CURRENT_POSITION-1);
	}
	
	public static ProgressBackup getBackup(int index)
	{
		return loadBackup(BACKUP_FILES.get(index));
	}
	
	public static ProgressBackup loadBackup(String path)
	{
		try
		{
			File file 				= new File(path);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			
			byte[] data = new byte[(int) file.length()];
			bis.read(data);
			bis.close();
			
			int headerDelIndex 	  = ByteHelper.indexOf(data, HEADER_DELIMITER);
			ProgressBackup backup = null;
			
			if(headerDelIndex > -1)
			{
				byte[] header  = ByteHelper.substring(data, 0, headerDelIndex);
				byte[] content = ByteHelper.substring(data, headerDelIndex);
				
				if(content.length > 0)
				{
					List<WritableImage> images = new ArrayList<>();
					int startIndex 	= ByteHelper.indexOf(data, IMAGE_START_DELIMITER);
					int endIndex	= ByteHelper.indexOf(data, IMAGE_END_DELIMITER); 
					int current		= 0;
					
					do
					{
						byte[] bytes = ByteHelper.substring(data, 
							startIndex+IMAGE_START_DELIMITER.length, endIndex);
						
						InputStream stream 	 = new ByteArrayInputStream(bytes);
						BufferedImage image  = ImageIO.read(stream);
						WritableImage wimage = SwingFXUtils.toFXImage(image, null);
						images.add(wimage);
						
						current 	= endIndex+IMAGE_END_DELIMITER.length;
						startIndex 	= ByteHelper.indexOf(data, IMAGE_START_DELIMITER, current);
						endIndex	= ByteHelper.indexOf(data, IMAGE_END_DELIMITER, current); 
					}
					while(startIndex > -1);
					
					SSDFCore ssdf  = new SSDFCore(new String(header));
					SSDArray array = ssdf.getAll();
					
					backup = new ProgressBackup(path, array, images);
				}
			}
			
			return backup;
		}
		catch(Exception ex) { ex.printStackTrace(); }
		return null;
	}
}