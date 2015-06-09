/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.image;

/**
 * Used for manipulating RGB and HSL
 * values in an image.*/
public class ColorMatrix
{
	/**
	 * RED channel: Red value*/
	public final double m00;
	/**
	 * RED channel: Green value*/
	public final double m01;
	/**
	 * RED channel: Blue value*/
	public final double m02;
	/**
	 * RED channel: Alpha value*/
	public final double m03;
	/**
	 * RED channel: Additional value*/
	public final double m04;
	
	/**
	 * GREEN channel: Red value*/
	public final double m10;
	/**
	 * GREEN channel: Green value*/
	public final double m11;
	/**
	 * GREEN channel: Blue value*/
	public final double m12;
	/**
	 * GREEN channel: Alpha value*/
	public final double m13;
	/**
	 * GREEN channel: Additional value*/
	public final double m14;
	
	/**
	 * BLUE channel: Red value*/
	public final double m20;
	/**
	 * BLUE channel: Green value*/
	public final double m21;
	/**
	 * BLUE channel: Blue value*/
	public final double m22;
	/**
	 * BLUE channel: Alpha value*/
	public final double m23;
	/**
	 * BLUE channel: Additional value*/
	public final double m24;
	
	/**
	 * ALPHA channel: Red value*/
	public final double m30;
	/**
	 * ALPHA channel: Green value*/
	public final double m31;
	/**
	 * ALPHA channel: Blue value*/
	public final double m32;
	/**
	 * ALPHA channel: Alpha value*/
	public final double m33;
	/**
	 * ALPHA channel: Additional value*/
	public final double m34;
	
	/**
	 * Creates new instance of Color Matrix.
	 * @param data - the data array of doubles that will be
	 * 				 applied to the RGB/HSL values in an image.
	 * 				 <b>The array size has to equals to 20</b>
	 * 				 and can contains any real number. Default
	 * 				 array looks like this:
	 * <pre>
	 *     new double[]
	 *     {
	 *         1, 0, 0, 0, 0,
	 *         0, 1, 0, 0, 0,
	 *         0, 0, 1, 0, 0,
	 *         0, 0, 0, 1, 0
	 *     }
	 * </pre>
	 * 
	 * You can also write all the numbers seperated with 
	 * just the comma (e.g. <code>1, 0, 1, 1, 0, 0, ...</code>).
	 * 
	 * @throws NullPointerException
	 * 
	 * 			   When the matrix array equals to null.
	 * 
	 * @throws IncorrectArrayLength
	 * 			
	 * 		       When the length of the matrix array does not
	 * 		       equal to 20.*/
	private ColorMatrix(double... data) throws NullPointerException, IncorrectArrayLength
	{
		if(data == null)
			throw new NullPointerException
			("The Color Matrix array should not equals to null!");
		
		if(data.length != 20)
			throw new IncorrectArrayLength
			("Wrong length of Color Matrix array! The correct length is 20.");
			
		this.m00 = data[0];
		this.m01 = data[1];
		this.m02 = data[2];
		this.m03 = data[3];
		this.m04 = data[4];
		this.m10 = data[5];
		this.m11 = data[6];
		this.m12 = data[7];
		this.m13 = data[8];
		this.m14 = data[9];
		this.m20 = data[10];
		this.m21 = data[11];
		this.m22 = data[12];
		this.m23 = data[13];
		this.m24 = data[14];
		this.m30 = data[15];
		this.m31 = data[16];
		this.m32 = data[17];
		this.m33 = data[18];
		this.m34 = data[19];
	}
	
	/**
	 * Creates new Image Matrix with the given data.
	 * @param data - the data array of doubles that will be
	 * 				 applied to the RGB/HSL values in an image.
	 * 				 <b>The array size has to equals to 20</b>
	 * 				 and can contains any real number. Default
	 * 				 array looks like this:
	 * <pre>
	 *     new double[]
	 *     {
	 *         1, 0, 0, 0, 0,
	 *         0, 1, 0, 0, 0,
	 *         0, 0, 1, 0, 0,
	 *         0, 0, 0, 1, 0
	 *     }
	 * </pre>
	 * 
	 * You can also write all the numbers seperated with 
	 * just the comma (e.g. <code>1, 0, 1, 1, 0, 0, ...</code>).
	 * @return Created Image Matrix object*/
	public static ColorMatrix createMatrix(double... data)
	{
		try
		{
			return new ColorMatrix(data);
		}
		catch(Exception ex) { ex.printStackTrace(); }
	
		return null;
	}
}