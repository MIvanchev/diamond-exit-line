/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Mihail Ivanchev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.podrug.line.util;

/**
 * This static class encapsulates tools for manipulating fixed-point numbers.
 *
 * @author Mihail Ivanchev
 */
public final class FPMath
{
    /***************************************************************************
     * CONSTANTS                                                               *
     **************************************************************************/

    /**
     * The number of bits used for the integer part of fixed-point numbers.
     */
    public static final int INTEGER_BITS = 34;

    /**
     * The number of bits used for the fraction of fixed-point numbers.
     */
    public static final int FRACTION_BITS = Long.SIZE - INTEGER_BITS;

    /**
     * The multiplier used to convert a floating point number to a fixed-point
     * number.
     */
    public static final long FRACTION_MULTIPLIER = 1L << FRACTION_BITS;

    /**
     * The representation of the constant 1.
     */
    public static final long ONE = 1L << FRACTION_BITS;

    /**
     * The representation of the constant 0.5.
     */
    public static final long HALF = ONE >> 1;

    /**
     * The representation of the constant 0.25.
     */
    public static final long QUARTER = ONE >> 2;

    /***************************************************************************
     * MEMBERS                                                                 *
     **************************************************************************/

    /**
     * The constructor of this class is private, because it cannot be
     * instantiated.
     */
    private FPMath()
    {
    }

    /**
     * Multiplies two fixed point values and returns the result.
     */
    public static long mul(long a, long b)
    {
        long intA = a / FRACTION_MULTIPLIER;
        long fraA = a - (intA << FRACTION_BITS);
        long intB = b / FRACTION_MULTIPLIER;
        long fraB = b -  (intB << FRACTION_BITS);
        return ((intA * intB) << FRACTION_BITS)
                + intA * fraB
                + intB * fraA
                + ((fraA * fraB) >> FRACTION_BITS);
    }

    /**
     * Divides two fixed point values and returns the result.
     */
    public static long div(long a, long b)
    {
        long intA = a / FRACTION_MULTIPLIER;
        long fraA = a - (intA << FRACTION_BITS);
        long intResult = ((intA != 0) ? ((ONE << FRACTION_BITS) / (b / intA)) : 0);
        long fraResult = (fraA << FRACTION_BITS) / b;
        return intResult + fraResult;
    }

    /**
     * Returns the squares of the specified number. 
     */
    public static long sqr(long number)
    {
	return FPMath.mul(number, number);
    }
    
    /**
     * Returns the square root of the specified number.
     * 
     * For the moment, the method converts the argument to its floating-point
     * representation, calculates the square root using the
     * {@link Math.sqrt(double) Math.sqrt} method and then returns the
     * fixed-point equivalent of this value. This implementation is only a
     * placeholder and will be changed as soon as a more suitable algorithm has
     * been selected. 
     */
    public static long sqrt(long number)
    {
	if (number < 0)
	    throw new IllegalArgumentException("Number must be positive or 0.");

	return toFixed(Math.sqrt(toDouble(number)));
    }

    /***************************************************************************
     * CONVERSION FUNCTIONS                                                    *
     **************************************************************************/

    /**
     * Converts the specified floating point number to its fixed-point
     * representation.
     */
    public static long toFixed(double number)
    {
        long intResult = (long) number << FRACTION_BITS;
        long fraResult = (long) ((number - (long) number) * FRACTION_MULTIPLIER);
        return intResult + fraResult;
    }

    /**
     * Converts the specified array of floating point numbers to an array of
     * fixed-point numbers by converting each number separately; the
     * function returns null if the argument is null.
     */
    public static long[] toFixed(float[] numbers)
    {
	if (numbers == null)
	    return null;
	long[] result = new long[numbers.length];
	for (int index = 0; index < numbers.length; index++)
	    result[index] = toFixed(numbers[index]);
	return result;
    }

    /**
     * Converts the specified array of floating point numbers to an array of
     * fixed-point numbers by converting each number separately; the
     * function returns null if the argument is null.
     */
    public static long[] toFixed(double[] numbers)
    {
	if (numbers == null)
	    return null;
	long[] result = new long[numbers.length];
	for (int index = 0; index < numbers.length; index++)
	    result[index] = toFixed(numbers[index]);
	return result;
    }

    /**
     * Converts the specified fixed-point number to its floating point
     * representation.
     */
    public static double toDouble(long number)
    {
        return (double) number / FRACTION_MULTIPLIER;
    }

    /**
     * Converts the specified array of fixed-point numbers to an array of
     * floating point numbers by converting each number separately; the
     * function returns null if the argument is null.
     */
    public static double[] toDouble(long[] numbers)
    {
	if (numbers == null)
	    return null;
	double[] result = new double[numbers.length];
	for (int index = 0; index < numbers.length; index++)
	    result[index] = toDouble(numbers[index]);
	return result;
    }

    /**
     * Converts the specified fixed-point number to its floating point
     * representation.
     */
    public static float toFloat(long number)
    {
        return (float) toDouble(number);
    }

    /**
     * Converts the specified array of fixed-point numbers to an array of
     * floating point numbers by converting each number separately; the
     * function returns null if the argument is null.
     */
    public static float[] toFloat(long[] numbers)
    {
	if (numbers == null)
	    return null;
	float[] result = new float[numbers.length];
	for (int index = 0; index < numbers.length; index++)
	    result[index] = toFloat(numbers[index]);
	return result;
    }
}
