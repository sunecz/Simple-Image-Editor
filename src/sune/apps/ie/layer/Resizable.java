/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.layer;

public interface Resizable
{
	public void resize(double width, double height);
	public double getWidth(double scale);
	public double getHeight(double scale);
}