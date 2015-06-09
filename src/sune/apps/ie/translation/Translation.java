/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.translation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sune.apps.ie.registry.Registry;
import sune.utils.ssdf.SSDArray;
import sune.utils.ssdf.SSDFCore;
import sune.utils.ssdf.SSDObject;

public class Translation
{
	private static String MARK = "UNKNOWN";
	private static String NAME = "UNKNOWN";
	private static String DEFV = "UNKNOWN";

	private static Map<String, String> translations
		= new HashMap<>();
	
	public static void loadTranslation(File file)
	{
		SSDFCore reader = new SSDFCore(file);

		translations.clear();
		for(SSDObject object : reader.getAll())
			translations.put(object.getName(), object.getValue());
		
		MARK = getTranslation("LANG_MARK");
		NAME = getTranslation("LANG_NAME");
		DEFV = getTranslation("LANG_DEFV");
	}
	
	public static void registerTranslation(File file)
	{
		SSDFCore reader = new SSDFCore(file);
		Registry.LANGUAGES.register(
			new TranslationObject(
				reader.getObject("LANG_MARK").getValue(),
				reader.getObject("LANG_NAME").getValue()));
	}
	
	public static String getTranslation(String name)
	{
		return translations.getOrDefault(name, DEFV);
	}
	
	public static List<SSDArray> getTranslationArray(String name)
	{
		Map<Integer, SSDArray> map = new HashMap<>();
		for(Entry<String, String> entry : translations.entrySet())
		{
			String objectName  = entry.getKey();
			String objectValue = entry.getValue();
			
			if(objectName.startsWith(name))
			{
				String itemName = objectName.substring(name.length()+1);
				int index 		= Integer.parseInt(itemName.substring(0, itemName.indexOf(".")));
				String keyName	= itemName.substring(itemName.indexOf(".")+1);
				
				SSDArray array = null;
				if(map.containsKey(index))
					array = map.get(index);
				if(array == null)
					array = new SSDArray("");
				
				array.setObject(keyName, objectValue);
				map.put(index, array);
			}
		}
		
		List<SSDArray> list = new ArrayList<>();
		for(Entry<Integer, SSDArray> entry : map.entrySet())
			list.add(entry.getKey(), entry.getValue());
		
		return list;
	}
	
	public static String getTranslation(String name, String... params)
	{
		String value = translations.getOrDefault(name, DEFV);
		
		char chItem = '%';
		char chBrc0	= '{';
		char chBrc1 = '}';
		
		boolean isChItem	= false;
		boolean inBrackets 	= false;
		
		String tempString 	= "";
		String valueString 	= "";
		
		for(int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);
			
			if(c == chItem)
			{
				isChItem = true;
				continue;
			}
			
			if(isChItem && c == chBrc0)
			{
				inBrackets 	= true;
				isChItem 	= false;
				continue;
			}
			
			if(inBrackets && c == chBrc1)
			{
				inBrackets 	= false;
				
				if(!tempString.isEmpty())
				{
					int index = Integer.parseInt(tempString);
					
					if(params.length > index)
						valueString += params[index];
					
					tempString = "";
				}

				continue;
			}
			
			if(inBrackets)
			{
				if(Character.isDigit(c))
					tempString += c;
				
				continue;
			}
			
			valueString += c;
		}

		return valueString;
	}

	public static String getMark()
	{
		return MARK;
	}

	public static String getName()
	{
		return NAME;
	}
	
	public static class TranslationObject
	{
		public final String mark;
		public final String name;
		
		public TranslationObject(String mark, String name)
		{
			this.mark = mark;
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}