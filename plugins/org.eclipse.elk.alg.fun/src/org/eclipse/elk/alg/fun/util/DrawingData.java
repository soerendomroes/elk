package org.eclipse.elk.alg.fun.util;

import org.eclipse.elk.alg.fun.firstiteration.RectPackingCalculations;

/**
 * Class that offers instances to save information about a possible drawing. Can be used to return multiple values that
 * are calculated for a possible drawing in a method for example.
 * 
 * @author dalu
 */
public class DrawingData {
    //////////////////////////////////////////////////////////////////
    // FIELDS
    /** Scale measure of the given drawing. */
    private double scaleMeasure;
    /** Width of the drawing. */
    private double drawingWidth;
    /** Height of the drawing. */
    private double drawingHeight;
    /** Drawing area. */
    private double area;
    /** Aspect ratio of the drawing. */
    private double aspectRatio;
    /** Desired aspect ratio. */
    private double dar;
    /** Indicates what placement option this data belongs to or whether it belongs to a whole drawing. */
    private DrawingDataDescriptor plaOpt;
    /**
     * If a this object contains information about a possible drawing, this field contains the potential x coordinate
     * for the new rectangle.
     */
    private double nextXcoordinate;
    /**
     * If a this object contains information about a possible drawing, this field contains the potential y coordinate
     * for the new rectangle.
     */
    private double nextYcoordinate;

    //////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    /**
     * Creates a new object saving parameters of a drawing.
     * 
     * @param dar
     *            Desired aspect ratio.
     * @param drawingWidth
     *            Drawing width.
     * @param drawingHeight
     *            Drawing height.
     * @param placementOption
     *            Placement Option.
     */
    public DrawingData(final double dar, final double drawingWidth, final double drawingHeight,
            final DrawingDataDescriptor placementOption) {
        this.scaleMeasure = RectPackingCalculations.computeScaleMeasure(drawingWidth, drawingHeight, dar);
        this.dar = dar;
        this.drawingWidth = drawingWidth;
        this.drawingHeight = drawingHeight;
        this.plaOpt = placementOption;
        calcAreaAndAr();
    }

    /**
     * Creates a new object saving parameters of a drawing.
     * 
     * @param dar
     *            Desired aspect ratio.
     * @param drawingWidth
     *            Drawing width.
     * @param drawingHeight
     *            Drawing height.
     * @param placementOption
     *            Placement Option.
     * @param nextXcoord
     *            X value for rectangle to place.
     * @param nextYcoord
     *            Y value for rectangle to place.
     */
    public DrawingData(final double dar, final double drawingWidth, final double drawingHeight,
            final DrawingDataDescriptor placementOption, final double nextXcoord, final double nextYcoord) {
        this.scaleMeasure = RectPackingCalculations.computeScaleMeasure(drawingWidth, drawingHeight, dar);
        this.dar = dar;
        this.drawingWidth = drawingWidth;
        this.drawingHeight = drawingHeight;
        this.plaOpt = placementOption;
        this.nextXcoordinate = nextXcoord;
        this.nextYcoordinate = nextYcoord;
        calcAreaAndAr();

    }

    //////////////////////////////////////////////////////////////////
    // OTHER METHODS

    /**
     * Calculates the area A and aspect ratio AR variables and sets them, both according to the values set in width and
     * height.
     * 
     * @return Returns true, if AR and A were calculated successfully and false otherwise.
     */
    private boolean calcAreaAndAr() {
        if (drawingWidth > 0 && drawingHeight > 0) {
            this.area = this.drawingWidth * this.drawingHeight;
            this.aspectRatio = this.drawingWidth / this.drawingHeight;
            return true;
        } else {
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    /**
     * Returns the value of the scale measure.
     * 
     * @return scale measure
     */
    public double getScaleMeasure() {
        return scaleMeasure;
    }

    /**
     * Gets the value of the drawing width.
     * 
     * @return
     */
    public double getDrawingWidth() {
        return drawingWidth;
    }

    /**
     * Sets the value of the drawing width.
     * 
     * @param drawingWidth
     */
    public void setDrawingWidth(final double drawingWidth) {
        this.drawingWidth = drawingWidth;
        this.scaleMeasure = RectPackingCalculations.computeScaleMeasure(drawingWidth, this.drawingHeight, this.dar);
        calcAreaAndAr();
    }

    /**
     * Sets the value of the drawing height.
     * 
     * @return
     */
    public double getDrawingHeight() {
        return drawingHeight;
    }

    /**
     * Sets the value of the drawing height.
     * 
     * @param drawingHeight
     */
    public void setDrawingHeight(final double drawingHeight) {
        this.drawingHeight = drawingHeight;
        this.scaleMeasure = RectPackingCalculations.computeScaleMeasure(this.drawingWidth, drawingHeight, this.dar);
        calcAreaAndAr();
    }

    /**
     * Gets the value of the area variable.
     */
    public double getArea() {
        return area;
    }

    /**
     * Gets the value of the aspect ratio.
     * 
     * @return
     */
    public double getAspectRatio() {
        return aspectRatio;
    }

    /**
     * Gets placement option.
     */
    public DrawingDataDescriptor getPlaOpt() {
        return plaOpt;
    }

    /**
     * Sets placement option.
     */
    public void setPlaOpt(final DrawingDataDescriptor plaOpt) {
        this.plaOpt = plaOpt;
    }

    /**
     * Gets x value of rectangle to place.
     * 
     * @return
     */
    public double getNextXcoordinate() {
        return nextXcoordinate;
    }

    /**
     * Sets x value of rectangle to place.
     * 
     * @param potentialX
     */
    public void setNextXcoordinate(final double potentialX) {
        this.nextXcoordinate = potentialX;
    }

    /**
     * Gets y value of rectangle to place.
     */
    public double getNextYcoordinate() {
        return nextYcoordinate;
    }

    /**
     * Sets y value of rectangle to place.
     */
    public void setNextYcoordinate(final double potentialY) {
        this.nextYcoordinate = potentialY;
    }
}
