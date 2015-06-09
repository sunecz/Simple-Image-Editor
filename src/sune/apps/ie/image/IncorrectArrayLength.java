/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.image;

/**
 * Exception class used when user parse an
 * array that has incorrect length (size).
 * This exception could be used anywhere, where
 * is important to handle the array length (size).*/
public class IncorrectArrayLength extends Exception
{
	/**
	 * The serial version unique ID*/
	private static final long serialVersionUID = 6L;

	/**
	 * Creates new instance of IncorrectArrayLength
	 * exception.
	 * @param message - the message to output*/
	public IncorrectArrayLength(String message)
	{
		super(message);
	}
}