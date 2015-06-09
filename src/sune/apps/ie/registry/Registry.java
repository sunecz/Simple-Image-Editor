/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.registry;

import java.util.ArrayList;
import java.util.List;

import sune.apps.ie.plugin.Plugin;
import sune.apps.ie.translation.Translation.TranslationObject;

public class Registry<T>
{
	public static Registry<Plugin> COLOR_ADJUSTMENTS;
	public static Registry<Plugin> FILTERS;
	public static Registry<TranslationObject> LANGUAGES;
	
	static
	{
		COLOR_ADJUSTMENTS = new Registry<>();
		FILTERS			  = new Registry<>();
		LANGUAGES		  = new Registry<>();
	}
	
	private List<T> list = new ArrayList<>();
	
	public void register(T object)
	{
		list.add(object);
	}
	
	public void unregister(T object)
	{
		list.remove(object);
	}
	
	public List<T> getAll()
	{
		return list;
	}
}