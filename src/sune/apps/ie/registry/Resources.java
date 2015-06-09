/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sune.apps.ie.registry.TemporaryFile.TemporaryFileType;

public class Resources
{
	public static final String PATH_PLUGINS_FOLDER;
	public static final String PATH_LANGUAGES_FOLDER;
	public static final String PATH_STYLES_FOLDER;
	public static final String PATH_TEMPORARY_FOLDER;
	
	private static final String EXTENSION_PLUGINS 	= "jar";
	private static final String EXTENSION_LANGUAGES = "ssdf";
	private static final String EXTENSION_STYLES 	= "css";
	
	public static final List<String> FOLDERS;
	public static final List<File> PLUGINS;
	public static final List<File> LANGUAGES;
	public static final List<File> STYLES;
	
	private static final List<TemporaryFile> TEMPORARY_FILES;
	private static final char[] tempFileNameChars;
	private static final int tempFileNameLength;
	
	static
	{
		PATH_PLUGINS_FOLDER 	= PathSystem.getFullPath(PathSystem.RESOURCES_FOLDER + "plugins/");
		PATH_LANGUAGES_FOLDER 	= PathSystem.getFullPath(PathSystem.RESOURCES_FOLDER + "languages/");
		PATH_STYLES_FOLDER 		= PathSystem.getFullPath(PathSystem.RESOURCES_FOLDER + "styles/");
		PATH_TEMPORARY_FOLDER 	= PathSystem.getFullPath(PathSystem.RESOURCES_FOLDER + "temporary/");
		
		FOLDERS 	= Arrays.asList(
			PATH_PLUGINS_FOLDER,
			PATH_LANGUAGES_FOLDER,
			PATH_STYLES_FOLDER,
			PATH_TEMPORARY_FOLDER
		);
		
		PLUGINS 		= new ArrayList<>();
		LANGUAGES 		= new ArrayList<>();
		STYLES 			= new ArrayList<>();
		TEMPORARY_FILES = new ArrayList<>();
		
		// Creates an array of all the possible characters
		List<Character> listChars = new ArrayList<>();
		
		/*Add a sequence of numbers for numbers 0-9.
		 *In ASCII table numbers are defined in indexes
		 *from 48 to 57.*/
		for(int i = 48; i <= 57; i++)
			listChars.add((char) i);
		
		/*Add a sequence of numbers for upper-case
		 *characters A-Z. In ASCII table upper-case
		 *characters are defined in indexes from 65 to 90.*/
		for(int i = 65; i <= 90; i++)
			listChars.add((char) i);
		
		/*Add a sequence of numbers for lower-case
		 *characters a-z. In ASCII table lower-case
		 *characters are defined in indexes from 97 to 122.*/
		for(int i = 97; i <= 122; i++)
			listChars.add((char) i);
		
		tempFileNameChars = new char[listChars.size()];
		for(int k = 0; k < tempFileNameChars.length; k++)
			tempFileNameChars[k] = listChars.get(k);
		
		/*32-characters long string should be enough for
		 *temporary file name.*/
		tempFileNameLength = 32;
	}
	
	private static class FileExtensionFilter implements FilenameFilter
	{
		private String neededExtension;
		
		public FileExtensionFilter(String extension)
		{
			neededExtension = extension;
		}
		
		@Override
		public boolean accept(File folder, String name)
		{
			String[] splitName 	= name.split("\\.");
			String extension 	= splitName[splitName.length-1].trim();
			return extension.equalsIgnoreCase(neededExtension);
		}
	}
	
	public static void initialize()
	{
		for(String path : FOLDERS)
		{
			File folder = new File(path);
			if(folder != null && !folder.exists())
				folder.mkdirs();
		}
		
		File folderPlugins 		= new File(PATH_PLUGINS_FOLDER);
		File folderLanguages 	= new File(PATH_LANGUAGES_FOLDER);
		File folderStyles 		= new File(PATH_STYLES_FOLDER);
		
		File[] filesPlugins 	= folderPlugins.listFiles(new FileExtensionFilter(EXTENSION_PLUGINS));
		File[] filesLanguages 	= folderLanguages.listFiles(new FileExtensionFilter(EXTENSION_LANGUAGES));
		File[] filesStyles		= folderStyles.listFiles(new FileExtensionFilter(EXTENSION_STYLES));
		
		if(filesPlugins != null && filesPlugins.length > 0)
			PLUGINS.addAll(Arrays.asList(filesPlugins));
		if(filesLanguages != null && filesLanguages.length > 0)
			LANGUAGES.addAll(Arrays.asList(filesLanguages));
		if(filesStyles != null && filesStyles.length > 0)
			STYLES.addAll(Arrays.asList(filesStyles));
	}
	
	public static TemporaryFile createTemporaryFile(TemporaryFileType type)
	{
		StringBuilder builder = new StringBuilder();
		
		/*Generates random numbers that will be used as indexes for the
		 *temporary file name characters. Random name, including characters
		 *A-Z, a-z, and numbers is better than checking and numbering the
		 *file names (e.g. temp_0, temp_1, ...).*/
		Random rand = new Random();
		for(int i = 0; i < tempFileNameLength; i++)
			builder.append(
				tempFileNameChars[rand.nextInt(tempFileNameChars.length)]
			);
		
		builder.append(".temp");
		TemporaryFile tempFile = new TemporaryFile(
			PATH_TEMPORARY_FOLDER + "/" + builder.toString(),
			TemporaryFileType.PROGRESS_BACKUP_FILE);
		
		try
		{
			TEMPORARY_FILES.add(tempFile);
			File file = tempFile.getFile();
			
			/*It is unlikely that the file name will be same sometime,
			 *but there is still a chance that it can happen.*/
			if(file.exists()) return createTemporaryFile(type);
			
			file.createNewFile();
			file.deleteOnExit();
			return tempFile;
		}
		catch(Exception ex) {}
		
		return null;
	}
	
	public static TemporaryFile getLastTemporaryFileByType(TemporaryFileType type)
	{
		for(int i = TEMPORARY_FILES.size()-1; i > -1; i--)
		{
			TemporaryFile tempFile = TEMPORARY_FILES.get(i);
			if(tempFile.getType() == type)
				return tempFile;
		}
		
		return null;
	}
}