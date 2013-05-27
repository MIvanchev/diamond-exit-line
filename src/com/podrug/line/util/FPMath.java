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
     * The number of bits used for the fraction of fixed-point numbers.
     *
     * This constant should in the range [0; 31].
     */
    public static final int FRACTION_BITS = 30;

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
     * Converts the specified fixed-point number to its floating point
     * representation.
     */
    public static double toDouble(long number)
    {
        return (double) number / FRACTION_MULTIPLIER;
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
}
