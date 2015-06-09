/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.util;

import java.io.File;

public class FileSave
{
	private File file;
	private String extension;

	public FileSave(File file, String extension)
	{
		this.file 		= file;
		this.extension 	= extension;
	}

	public File getFile()
	{
		return file;
	}
	
	public String getExtension()
	{
		return extension;
	}
}