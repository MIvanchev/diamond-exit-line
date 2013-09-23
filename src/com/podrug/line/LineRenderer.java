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
package com.podrug.line;

import com.podrug.line.util.FPMath;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Arrays;

/**
 * This static class implements a rasterizer for aliased lines based on the
 * "diamond exit" rule; this strategy ensures that 2 coincident line segments
 * are rasterized using the same set of pixels in the common region unlike the
 * popular algorithm of Bresenham and its derivates.
 * The "diamond exit" rule is outlined here:
 * <a href="http://goo.gl/gsBQI">Direct3D 11 Rasterization Rules</a>
 * <p>
 * The implementation supports only lines of thickness 1 pixel, but it's trivial
 * to extend to arbitrary thickness, e.g. using the strategy outlined here:
 * <a href="http://goo.gl/lYtiO">OpenGL 1.1 Specification &ndash; Wide Lines</a>
 *
 * @author Mihail Ivanchev
 */
public final class LineRenderer
{
    /***************************************************************************
     * HELPER TYPES                                                            *
     **************************************************************************/

    /**
     * Encapsulates a point of the Euclidean Plane with coordinates encoded as
     * fixed-point numbers.
     */
    static class Point
    {
        long x;
        long y;

        public Point()
        {
        }

        public Point(long x, long y)
        {
            setLocation(x, y);
        }

        public void setLocation(long x, long y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object other)
        {
            if (this == other)
                return true;
            else if (!(other instanceof Point))
                return false;
            return x == ((Point) other).x && y == ((Point) other).y;
        }

        @Override
        public int hashCode()
        {
            return Long.valueOf(x ^ y).hashCode();
        }
    }

    /**
     * Encapsulates information about an intersection of the line with an edge
     * of a diamond region.
     */
    static class IntersectionInfo
    {
        long lineOffset;
        long edgeOffset;
        long denominator;
    }

    /***************************************************************************
     * MEMBERS                                                                 *
     **************************************************************************/

    static LineSampler sampler = new LineSampler();
    static final Point p1 = new Point();
    static final Point p2 = new Point();
    static int width;
    static boolean xMajor;
    static long length;
    static long a;
    static long b;
    static long c;
    static long pathLength;
    static final AffineTransform identity = new AffineTransform();

    /**
     * TODO
     */
    public static void render(Graphics2D graphics, Shape shape, float width, float[] dashArray, float dashPhase)
    {
	PathIterator iterator = shape.getPathIterator(null);
	double[] coords = new double[6];
	while (iterator.isDone())
	{
	    int type = iterator.currentSegment(coords);
	    if (type != PathIterator.SEG_MOVETO
		    && type != PathIterator.SEG_LINETO
		    && type != PathIterator.SEG_CLOSE)
	    {
		throw new IllegalArgumentException("The specified shape must consist only of straight line segments.");
	    }
	}
	double[] first = new double[2];
	double[] previous = new double[2];
	while (iterator.isDone())
	{
	    int type = iterator.currentSegment(coords);
	    switch (type)
	    {
	    case PathIterator.SEG_MOVETO:
                first[0] = coords[0];
                first[1] = coords[1];
                break;

            case PathIterator.SEG_CLOSE:
                coords[0] = first[0];
                coords[1] = first[1];
                //break;

            case PathIterator.SEG_LINETO:


                break;
	    }
	}
    }

    /**
     * Renders the line with the specified coordinates to the specified graphics
     * context.
     */
    public static void render(
	    		Graphics2D graphics,
	    		double x1,
	    		double y1,
	    		double x2,
	    		double y2,
	    		Color strokeColor,
	    		float strokeWidth,
	    		float[] dashArray,
	    		float dashPhase
	    		)
    {
        if (width < 0)
            throw new IllegalArgumentException("The width cannot be negative.");

	// Set an identity transform to the graphics context and transform the
        // end points manually.
        //

        AffineTransform transform = graphics.getTransform();
        graphics.setTransform(identity);

        double tx = x1 * transform.getScaleX() + y1 * transform.getShearX()
                    + transform.getTranslateX();
        double ty = x1 * transform.getShearY() + y1 * transform.getScaleY()
                    + transform.getTranslateY();
        x1 = tx;
        y1 = ty;

        tx = x2 * transform.getScaleX() + y2 * transform.getShearX()
                + transform.getTranslateX();
        ty = x2 * transform.getShearY() + y2 * transform.getScaleY()
                + transform.getTranslateY();
        x2 = tx;
        y2 = ty;

        // Classify, offset to account for the width and extract the bounding box.
        //

        xMajor = Math.abs(x2 - x1) >= Math.abs(y2 - y1);
 
        width = (int) Math.max(Math.round(strokeWidth), 1);

        if (xMajor)
        {
            y1 -= (width - 1) / 2;
            y2 -= (width - 1) / 2;
        }
        else
        {
            x1 -= (width - 1) / 2;
            x2 -= (width - 1) / 2;
        }

        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);

        // Offset the line end points half a pixel to the right and to the
        // bottom. Thus, a line coincident with the border between 2 pixel rows
        // will highly the bottom row.
        //

        double offset = 0.5;
        p1.setLocation(FPMath.toFixed(x1 + offset), FPMath.toFixed(y1 + offset));
        p2.setLocation(FPMath.toFixed(x2 + offset), FPMath.toFixed(y2 + offset));

        // Calculate the length and extract the standard form.
        //

        length = FPMath.sqrt(FPMath.sqr(p2.x - p1.x) + FPMath.sqr(p2.y - p1.y));

        if (p2.x != p1.x)
        {
            a = FPMath.div(p2.y - p1.y, p2.x - p1.x);
            b = -FPMath.ONE;
            c = p1.y - FPMath.mul(a, p1.x);
        }
        else
        {
            a = FPMath.ONE;
            b = 0;
            c = p1.x;
        }

        // Rasterize the line and then restore the graphics context transform.
        //

        int bufferWidth = (int) (Math.ceil(maxX) - Math.floor(minX)) + (!xMajor ? width : 1);
        int bufferHeight = (int) (Math.ceil(maxY) - Math.floor(minY)) + (xMajor ? width : 1);
        int positionX = (int) Math.floor(minX);
        int positionY = (int) Math.floor(minY);

        sampler.setColor(strokeColor);
        sampler.setDashArray(FPMath.toFixed(dashArray));
        sampler.setDashPhase(FPMath.toFixed(dashPhase));
        sampler.setBufferDimensions(bufferWidth, bufferHeight);
        for (int j = 0, y = positionY; j < bufferHeight; j++, y++)
        {
            for (int i = 0, x = positionX; i < bufferWidth; i++, x++)
            {
        	if (!belongsToRepresentation(x, y))
        	    continue;
        	
        	for (int index = 0; index < width; index++)
        	{
        	    if (xMajor)
        		sampler.sample(i, j + index, calculateDistance(x, y + index), 0);
        	    else
        		sampler.sample(i + index, j, calculateDistance(x + index, y), 0);
        	}
            }
        }

        sampler.drawBuffer(graphics, null, positionX, positionY);
        graphics.setTransform(transform);
    }

    /**
     * Returns true if the pixel whose top-left corner is given by the specified
     * coordinates is part of the line's representation, false otherwise.
     * 
     * The pixel will belong to the line's representation if and only if the
     * line has a common point with the diamond region around the pixel's center
     * as governed by implemented specification. 
     */
    static boolean belongsToRepresentation(int x, int y)
    {
        // Check whether the pixel is too far away from the line to be part of
        // it.
        //

        long centerX = FPMath.toFixed(x) + FPMath.HALF;
        long centerY = FPMath.toFixed(y) + FPMath.HALF;

        if (rejectPoint(centerX, centerY))
            return false;

        // Test for trivial cases:
        //
        // 1. If the 2nd end point is within the diamond area, discard the pixel
        // since the line is not exiting.
        //
        // 2. If the 1st end point is within the diamond area, accept the pixel
        // since the line is exiting.
        //

        Point[] points =
            {
                new Point(centerX - FPMath.HALF, centerY              ),    // 0
                new Point(centerX + FPMath.HALF, centerY              ),    // 1
                new Point(centerX              , centerY - FPMath.HALF),    // 2
                new Point(centerX              , centerY + FPMath.HALF)     // 3
            };

        Point[][] edges =
            {
                { points[0], points[3] },    // 0
                { points[3], points[1] },    // 1
                { points[1], points[2] },    // 2
                { points[2], points[0] }     // 3
            };

        if (p2.equals(points[3])
            || (!xMajor && p2.equals(points[1]))
            || (isOnLeftSide(edges[0], p2, false)
                && isOnLeftSide(edges[1], p2, false)
                && isOnLeftSide(edges[2], p2, true)
                && isOnLeftSide(edges[3], p2, true)))
        {
            return false;
        }

        if (p1.equals(points[3])
            || (!xMajor && p1.equals(points[1]))
            || (isOnLeftSide(edges[0], p1, false)
                && isOnLeftSide(edges[1], p1, false)
                && isOnLeftSide(edges[2], p1, true)
                && isOnLeftSide(edges[3], p1, true)))
        {
            return true;
        }

        // Accept the pixel if the line intersects the diamond area either at a
        // "hot" point or at 2 different points.
        //

        IntersectionInfo info = new IntersectionInfo();
        int numIntersections = 0;

        for (int index = 0; index < 4 && numIntersections < 2; index++)
        {
            if (findEdgeIntersection(edges[index], info))
            {
                if (info.edgeOffset == 0)
                {
                    if (edges[index][0] == points[3] || (!xMajor && edges[index][0] == points[1]))
                        return true;
                }
                if (info.edgeOffset == info.denominator)
                {
                    if (edges[index][1] == points[3] || (!xMajor && edges[index][1] == points[1]))
                        return true;
                }

                boolean edgeIntersect = (info.denominator < 0)
                                    ? (info.edgeOffset <= 0 && info.edgeOffset > info.denominator)
                                    : (info.edgeOffset >= 0 && info.edgeOffset < info.denominator);
                boolean lineIntersect = (info.denominator < 0)
                                    ? (info.lineOffset <= 0 && info.lineOffset >= info.denominator)
                                    : (info.lineOffset >= 0 && info.lineOffset <= info.denominator);

                if (edgeIntersect && lineIntersect)
                    numIntersections++;
            }
        }

        return numIntersections == 2;
    }

    /**
     * Returns false if the specified edge and the line are parallel, true
     * otherwise; If true is returned, the intersection offsets are stored
     * in the specified data object.
     */
    static boolean findEdgeIntersection(Point[] edge, IntersectionInfo info)
    {
        // Check if the lines are parallel.
        //

        long denominator = FPMath.mul(p2.x - p1.x, edge[1].y - edge[0].y)
                                - FPMath.mul(p2.y - p1.y, edge[1].x - edge[0].x);
        if (denominator == 0)
            return false;

        // Calculate the intersection points.
        //

        info.denominator = denominator;
        info.lineOffset = FPMath.mul(p1.y - edge[0].y, edge[1].x - edge[0].x)
                            - FPMath.mul(p1.x - edge[0].x, edge[1].y - edge[0].y);
        info.edgeOffset = FPMath.mul(p1.y - edge[0].y, p2.x - p1.x)
                            - FPMath.mul(p1.x - edge[0].x, p2.y - p1.y);

        return true;
    }

    /**
     * Returns true if the specified point is strictly on the left side of the
     * specified edge, false otherwise; if true is specified for the flag, the
     * method will also return true if the point is lying on the edge's
     * interior excluding the end points.
     */
    static boolean isOnLeftSide(Point[] edge, Point point, boolean strict)
    {
        long val = FPMath.mul(edge[1].x - edge[0].x, point.y - edge[0].y)
                    - FPMath.mul(edge[1].y - edge[0].y, point.x - edge[0].x);

        long numenator = point.x - edge[0].x;
        long denominator = edge[1].x - edge[0].x;
        boolean inBounds = (denominator < 0)
                                ? (numenator < 0 && numenator > denominator)
                                : (numenator > 0 && numenator < denominator);

        return val < 0 || (!strict && val == 0 && inBounds);
    }

    /**
     * Returns true if the point with the specified coordinates is too far from
     * the line to be considered, false otherwise.
     */
    static boolean rejectPoint(long x, long y)
    {
        if (a == 0)
            return FPMath.mul(y - c, y - c) > FPMath.QUARTER;
        else if (b == 0)
            return FPMath.mul(x - c, x - c) > FPMath.QUARTER;

        long numenator = FPMath.mul(a, x) + FPMath.mul(b, y) + c;
        long denominator = FPMath.mul(a, a) + FPMath.mul(b, b);

        return FPMath.mul(numenator, numenator) > FPMath.mul(FPMath.QUARTER, denominator);
    }

    /**
     * TODO
     */
    static long calculateDistance(int x, int y)
    {
	long cx = FPMath.toFixed(x) + FPMath.HALF;
	long cy = FPMath.toFixed(y) + FPMath.HALF;
	long numenator = FPMath.mul(cx - p1.x, p2.x - p1.x)
				+ FPMath.mul(cy - p1.y, p2.y - p1.y);
	return FPMath.div(numenator, length);
    }
}
