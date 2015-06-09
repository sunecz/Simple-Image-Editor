/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.util;

import java.util.List;

public class ByteHelper
{
	public static void appendBytes(List<Byte> list, byte[] array)
	{
		for(int k = 0; k < array.length; k++)
			list.add(array[k]);
	}
	
	public static byte[] toByteArray(List<Byte> list)
	{
		byte[] bytes = new byte[list.size()];
		for(int i = 0; i < list.size(); i++)
			bytes[i] = list.get(i);
		
		return bytes;
	}
	
	public static int indexOf(byte[] array, byte[] value)
	{
		return indexOf(array, value, 0);
	}
	
	public static int indexOf(byte[] array, byte[] value, int start)
	{
		byte firstByte = value[0];
		for(int i = start; i < array.length; i++)
		{
			byte arrVal = array[i];
			if(arrVal == firstByte)
			{
				boolean isValue = true;
				for(int k = 1; k < value.length; k++)
				{
					byte arrKVal = array[i+k];
					byte valVal  = value[k];
					
					if(arrKVal != valVal)
						isValue = false;
				}
				
				if(isValue) return i;
			}
		}
		
		return -1;
	}
	
	public static byte[] substring(byte[] array, int start)
	{
		return substring(array, start, array.length);
	}
	
	public static byte[] substring(byte[] array, int start, int end)
	{
		if(end <= start) return null;
		int length = (end - start);
		
		byte[] newArray = new byte[length];
		System.arraycopy(array, start, newArray, 0, length);
		return newArray;
	}
}