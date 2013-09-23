package com.podrug.line;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import com.podrug.line.util.IllegalOperationException;

/**
 * TODO
 */
public class LineSampler {

    /**
     * TODO
     */
    long[] dashArray;

    /**
     * TODO
     */
    long dashPhase;

    /**
     * TODO
     */
    Color color;

    BufferedImage buffer;
    int[] bufferData;

    /**
     * TODO
     */
    public LineSampler()  {
	color = Color.BLACK;
    }

    /**
     * TODO
     */
    public void setBufferDimensions(int width, int height)
    {
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        Arrays.fill(bufferData, 0);
    }

    /**
     * TODO
     */
    public void drawBuffer(Graphics2D graphics, BufferedImageOp op, int x, int y)
    {
	if (buffer == null)
	    throw new IllegalOperationException("The buffer has not yet been initialized");
	
	graphics.drawImage(buffer, op, x, y);
    }

    /**
     * TODO
     */
    public void sample(int x, int y, long distance, long dashPhaseOffset)
    {
        if (belongsToVisibleDash(distance, dashPhaseOffset))
        {
            int value = color.getRGB();
            bufferData[y * buffer.getWidth() + x] = value;
        }
    }

    /**
     * TODO
     */
    boolean belongsToVisibleDash(long distance, long phaseOffset)
    {
	if (dashArray == null)
	    return true;

	distance += dashPhase;
	distance += phaseOffset;

        int index = 0;
        long total = 0;

        while (distance < total || distance >= total + dashArray[index])
        {
            total += dashArray[index];
            index = ++index % dashArray.length;
        }

        return (index % 2) == 0;
    }
 
    /***************************************************************************
     * PROPERTY ACCESSORS                                                      *
     **************************************************************************/

    public long[] getDashArray() {
	return dashArray;
    }

    public void setDashArray(long[] dashArray) {
	if (dashArray == null)
	{
	    this.dashArray = null;
	    return;
	}

	if (dashArray.length == 0)
	    throw new IllegalArgumentException("The dash array cannot be empty.");
	
	boolean allZero = true;
	for (long element : dashArray)
	{
	    if (element < 0)
		throw new IllegalArgumentException("The dash lengths cannot be negative.");
	    else if (element > 0)
		allZero = false;
	}
	
	if (allZero)
	    throw new IllegalArgumentException("At least 1 dash length must greater than 0.");

	this.dashArray = dashArray;
    }

    public long getDashPhase() {
	return dashPhase;
    }

    public void setDashPhase(long dashPhase) {
	if (dashPhase < 0)
	    throw new IllegalArgumentException("The phase must be positive or 0.");

	this.dashPhase = dashPhase;
    }

    public Color getColor() {
	return color;
    }

    public void setColor(Color color) {
	if (color == null)
	    throw new IllegalArgumentException("The color cannot be null.");

	this.color = color;
    }
}
