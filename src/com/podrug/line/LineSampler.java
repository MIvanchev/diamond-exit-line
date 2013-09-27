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
public class LineSampler
{
    /**
     * TODO
     */
    Color strokeColor;

    BufferedImage buffer;
    int[] bufferData;

    /**
     * TODO
     */
    public LineSampler()
    {
        strokeColor = Color.BLACK;
    }

    /**
     * TODO
     */
    public void setBufferDimensions(int width, int height)
    {
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        Arrays.fill(bufferData, 0xFFFFFF00);
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
    public void sample(int x, int y)
    {
        int value = strokeColor.getRGB();
        bufferData[y * buffer.getWidth() + x] = value;
    }

    /***************************************************************************
     * PROPERTY ACCESSORS                                                      *
     **************************************************************************/

    public Color getStrokeColor()
    {
        return strokeColor;
    }

    public void setStrokeColor(Color strokeColor)
    {
        if (strokeColor == null)
            throw new IllegalArgumentException("The stroke color cannot be null.");

        this.strokeColor = strokeColor;
    }
}
