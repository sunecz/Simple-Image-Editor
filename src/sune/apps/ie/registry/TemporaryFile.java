/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TemporaryFile
{
	private File file;
	private String path;
	private TemporaryFileType type;
	
	private BufferedOutputStream outputStream;
	private boolean isOpened;
	
	public enum TemporaryFileType
	{
		PROGRESS_BACKUP_FILE;
	}
	
	public TemporaryFile(String path, TemporaryFileType type)
	{
		this.file = new File(path);
		this.path = path;
		this.type = type;
	}
	
	public boolean open()
	{
		try
		{
			outputStream = new BufferedOutputStream(
				new FileOutputStream(file));
			isOpened = true;
			return true;
		}
		catch(FileNotFoundException ex) {}
		return false;
	}
	
	public void write(String string)
	{
		write(string.getBytes());
	}
	
	public void write(byte[] bytes)
	{
		try
		{
			if(!isOpened) return;
			outputStream.write(bytes);
		}
		catch(IOException ex) {}
	}
	
	public boolean close()
	{
		try
		{
			if(!isOpened)
				return false;
			
			outputStream.flush();
			outputStream.close();
			isOpened = false;
			return true;
		}
		catch(IOException ex) {}
		return false;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public TemporaryFileType getType()
	{
		return type;
	}
}