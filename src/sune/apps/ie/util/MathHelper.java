/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.util;

public class MathHelper
{
	/**
	 * Rychlej�� alternativa funkce Math.pow(a, b).
	 * Metoda je a� 23kr�t rychlej�� ne� z�kladn� funkce, a to s minim�ln� nep�esnost�.
	 * <br><br>
	 * Metoda byla p�evzata ze str�nky:<br>
	 * <a href="http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/">
	 * 		http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
	 * </a>
	 * 
	 * @param a z�klad
	 * @param b exponent
	 * 
	 * @return p�ibli�n� hodnota {@code a}<sup>{@code b}</sup>
	 * @author Martin Ankerl*/
	public static double fastPow(final double a, final double b)
	{
		final long tmp  = Double.doubleToLongBits(a);
		final long tmp2 = (long) (b * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		return Double.longBitsToDouble(tmp2);
	}
	
	public static double fastExp(double val)
	{
		final long tmp = (long) (1512775 * val + 1072632447);
		return Double.longBitsToDouble(tmp << 32);
	}
}