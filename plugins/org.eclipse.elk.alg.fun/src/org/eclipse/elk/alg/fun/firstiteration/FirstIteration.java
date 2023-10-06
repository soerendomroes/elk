package org.eclipse.elk.alg.fun.firstiteration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.elk.alg.fun.util.DrawingData;
import org.eclipse.elk.alg.fun.util.PackingStrategy;
import org.eclipse.elk.alg.fun.util.DrawingDataDescriptor;
import org.eclipse.elk.graph.ElkNode;

/**
 * Class that handles the first iteration of the algorithm, producing an approximated bounded box for the packing.
 * 
 * @author dalu
 */
public class FirstIteration {
    /** Desired aspect ratio. */
    private double dar;
    /** Packing Strategy. */
    private PackingStrategy packingStrat;
    /** Shift when placing behind or below the last placed rectangle. */
    private boolean lpShift;
    /** Absolute aspect ratio calculation */
    private boolean absoluteAR;

    /**
     * Constructs an object that the first iteration can be executed on.
     * 
     * @param desiredAr
     *            Desired aspect ratio.
     * @param packStrat
     *            Given packing strategy.
     * @param absAR
     *            Indicating the way the aspect ratio is calculated.
     * @param lpShifting
     *            Indicating whether a shift should happen after placing near the last placed node.
     */
    public FirstIteration(final double desiredAr, final PackingStrategy packStrat, final boolean absAR,
            final boolean lpShifting) {
        this.dar = desiredAr;
        this.packingStrat = packStrat;
        this.lpShift = lpShifting;
        this.absoluteAR = absAR;
    }

    /**
     * Calculates a drawing for the given rectangles according to the set options. Rectangles have coordinates set after
     * this method.
     * 
     * @param rectangles
     *            The rectangles to place.
     * @return A drawing calculated by this methods algorithm.
     */
    public DrawingData approxBoundingBox(final List<ElkNode> rectangles) {
        // init variables
        ElkNode lastPlaced = null;
        List<ElkNode> placedRects = new ArrayList<>();
        ElkNode toPlace = null;
        DrawingData opt1;
        DrawingData opt2;
        DrawingData opt3;
        DrawingData opt4;
        DrawingData currentValues;
        DrawingData bestOpt;

        // place first box, box with order position one will always be in the top left corner
        ElkNode firstRect = rectangles.get(0);
        firstRect.setX(0);
        firstRect.setY(0);
        placedRects.add(0, firstRect);
        lastPlaced = firstRect;
        currentValues = new DrawingData(this.dar, firstRect.getWidth(), firstRect.getHeight(),
                DrawingDataDescriptor.WHOLE_DRAWING);

        // place the other boxes.
        for (int i = 1; i < rectangles.size(); i++) {
            toPlace = rectangles.get(i);
            
            // CHECKING SCALE MEASURES OF DIFFERENT OPTIONS
            // placing it right of last placed rectangle
            opt1 = calcValuesForOpt(DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT, toPlace, lastPlaced,
                    currentValues.getDrawingWidth(), currentValues.getDrawingHeight(), placedRects);
            // placing it below last placed rectangle
            opt2 = calcValuesForOpt(DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW, toPlace, lastPlaced,
                    currentValues.getDrawingWidth(), currentValues.getDrawingHeight(), placedRects);
            // placing it right of current drawing
            opt3 = calcValuesForOpt(DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT, toPlace, lastPlaced,
                    currentValues.getDrawingWidth(), currentValues.getDrawingHeight(), placedRects);
            // placing it below current drawing
            opt4 = calcValuesForOpt(DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_BELOW, toPlace, lastPlaced,
                    currentValues.getDrawingWidth(), currentValues.getDrawingHeight(), placedRects);

            // actually place rectangles according to best scale measure
            bestOpt = checkOptions(opt1, opt2, opt3, opt4, toPlace, lastPlaced);

            toPlace.setY(bestOpt.getNextYcoordinate());
            toPlace.setX(bestOpt.getNextXcoordinate());
            bestOpt.setPlaOpt(DrawingDataDescriptor.WHOLE_DRAWING);
            currentValues = bestOpt;
            lastPlaced = toPlace;
            placedRects.add(i, toPlace);
        }

        return currentValues;
    }

    // HELPING METHODS

    /**
     * Determines according to the selected packing strategy, which option out of the given ones is the best.
     * 
     * @param opt1
     *            option lastPlaced right
     * @param opt2
     *            option lastPlaced bottom
     * @param opt3
     *            option complete drawing right
     * @param opt4
     *            option complete drawing below
     * @param toPlace
     *            rectangle to be placed on drawing area
     * @param lastPlaced
     *            rectangle that was placed last on the drawing area
     * @return Returns the best option out of the given ones or null, if there is no best option found.
     */
    private DrawingData checkOptions(final DrawingData opt1, final DrawingData opt2, final DrawingData opt3,
            final DrawingData opt4, final ElkNode toPlace, final ElkNode lastPlaced) {
        List<DrawingData> candidates = new ArrayList<>();
        candidates.add(opt1);
        candidates.add(opt2);
        candidates.add(opt3);
        candidates.add(opt4);
        
        switch (packingStrat) {
        // scans the candidates according to the packing strategy.
        case MAX_SCALE_DRIVEN:
            candidates = BestOptionFinder.findMaxScale(candidates);
            if (candidates.size() > 1) {
                candidates = BestOptionFinder.findMinArea(candidates);
                if (candidates.size() > 1) {
                    candidates = BestOptionFinder.findBestAspectRatio(candidates, dar, absoluteAR);
                }
            }
            break;
        case ASPECT_RATIO_DRIVEN:
            candidates = BestOptionFinder.findBestAspectRatio(candidates, dar, absoluteAR);
            if (candidates.size() > 1) {
                candidates = BestOptionFinder.findMinArea(candidates);
                if (candidates.size() > 1) {
                    candidates = BestOptionFinder.findMaxScale(candidates);
                }
            }

            break;
        case AREA_DRIVEN:
            candidates = BestOptionFinder.findMinArea(candidates);
            if (candidates.size() > 1) {
                candidates = BestOptionFinder.findMaxScale(candidates);
                if (candidates.size() > 1) {
                    candidates = BestOptionFinder.findBestAspectRatio(candidates, dar, absoluteAR);
                }
            }

            break;
        default:
            break;
        }
        // only one candidate remains
        if (candidates.size() == 1) {
            return candidates.get(candidates.size() - 1);
        }

        // Multiple options have the same value for every benchmark. These special cases are caught in the following.
        if (candidates.size() == 2) {
            return checkSpecialPairs(candidates.get(0), candidates.get(1), lastPlaced, toPlace);
        }

        return null;
    }

    /**
     * Checks the two options and their possible combinations after being sorted out be the packing strategies.
     * Determines the best option depending on the options given.
     * 
     * @param drawing1
     *            A drawing to compare.
     * @param drawing2
     *            A drawing to compare
     * @param lastPlaced
     *            The rectangle that was last placed before the two drawings were calculated.
     * @param toPlace
     *            The rectangle that was newly placed in the two drawings.
     * @return The better drawing out of the two.
     */
    private DrawingData checkSpecialPairs(DrawingData drawing1, DrawingData drawing2, ElkNode lastPlaced,
            ElkNode toPlace) {
        DrawingDataDescriptor firstOpt = drawing1.getPlaOpt();
        DrawingDataDescriptor secondOpt = drawing2.getPlaOpt();

        boolean firstOptLPBorCDB = (firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW
                || firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_BELOW);
        boolean secondOptLPBorCDB = (secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW
                || secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_BELOW);

        boolean firstOptLPRorCDR = (firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT
                || firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT);
        boolean secondOptLPRorCDR = (secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT
                || secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT);

        boolean firstOptLPRorLPB = (firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT
                || firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW);
        boolean secondOptLPRorLPB = (secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT
                || secondOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW);

        if (firstOptLPBorCDB && secondOptLPBorCDB) {
            // If placing it below last Placed and below drawing produces the same values. Take CDB.
            if (drawing1.getPlaOpt() == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_BELOW) {
                return drawing1;
            } else {
                return drawing2;
            }
        } else if (firstOptLPRorCDR && secondOptLPRorCDR) {
            // If placing it right of last Placed and right of drawing produces the same values. Take CDR.
            if (drawing1.getPlaOpt() == DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT) {
                return drawing1;
            } else {
                return drawing2;
            }
        } else if (firstOptLPRorLPB && secondOptLPRorLPB) {
            // If placing it to the right or to the bottom of the last places produces the same value. Take the
            // option producing less area (area calculated with the brick to place and the last placed brick).
            // determine which one exactly is LPR/LPB
            DrawingData lprOpt;
            DrawingData lpbOpt;
            if (firstOpt == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT) {
                lprOpt = drawing1;
                lpbOpt = drawing2;
            } else {
                lprOpt = drawing2;
                lpbOpt = drawing1;
            }

            double areaLPR = (lprOpt.getNextXcoordinate() + toPlace.getWidth() - lastPlaced.getX()) * (Math
                    .max(lastPlaced.getY() + lastPlaced.getHeight(), lprOpt.getNextYcoordinate() + toPlace.getHeight())
                    - Math.min(lastPlaced.getY(), lprOpt.getNextYcoordinate()));
            double areaLPB = (Math.max(lastPlaced.getX() + lastPlaced.getWidth(),
                    lpbOpt.getNextXcoordinate() + toPlace.getWidth())
                    - Math.min(lastPlaced.getX(), lpbOpt.getNextXcoordinate()))
                    * (lpbOpt.getNextYcoordinate() + toPlace.getHeight() - lastPlaced.getY());

            if (areaLPR <= areaLPB) {
                if (drawing1.getPlaOpt() == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT) {
                    return drawing1;
                } else {
                    return drawing2;
                }
                // both areas are the same, take LPB
            } else {
                if (drawing1.getPlaOpt() == DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW) {
                    return drawing1;
                } else {
                    return drawing2;
                }
            }
        }

        return drawing1;
    }

    /**
     * Calculates the new drawing values for the given parameters including x and y coordinate for the rectangle
     * toPlace.
     * 
     * @param option
     *            The placement option the values are calculated for.
     * @param toPlace
     *            The rectangle to be placed.
     * @param lastPlaced
     *            The rectangle that was placed last.
     * @param currDrawingWidth
     *            The width of the current drawing before placing the new rectangle.
     * @param currDrawingHeight
     * 
     *            The height of the current drawing before placing the new rectangle.
     * @param placedRects
     *            Already placed rectangles.
     * @return An DrawingValues object containing the values after the rectangle would be placed.
     */
    private DrawingData calcValuesForOpt(final DrawingDataDescriptor option, final ElkNode toPlace,
            final ElkNode lastPlaced, final double currDrawingWidth, final double currDrawingHeight,
            final List<ElkNode> placedRects) {
        DrawingData newDrawing;
        double width;
        double height;
        double potentialXvalue = 0;
        double potentialYvalue = 0;
        DrawingDataDescriptor placementOpt;

        // width and height are calculated according to the placement option. LPR, and LPB also calculate the x and y
        // coordinates because they are not obvious beforehand.
        switch (option) {
        case PLACEMENT_OPTION_LAST_PLACED_RIGHT:
            // calculate coordinates for the rectangle to be placed
            // shift rectangle as far as possible if enabled.
            if (lpShift) {
                potentialXvalue = lastPlaced.getX() + lastPlaced.getWidth();
                potentialYvalue = RectPackingCalculations.calculateYforLPR(potentialXvalue, placedRects, lastPlaced);
            } else {
                potentialXvalue = lastPlaced.getX() + lastPlaced.getWidth();
                potentialYvalue = lastPlaced.getY();
            }

            width = getWidthLPRorLPB(currDrawingWidth, potentialXvalue, toPlace.getWidth());
            height = getHeightLPRorLPB(currDrawingHeight, potentialYvalue, toPlace.getHeight());
            placementOpt = DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_RIGHT;
            break;
        case PLACEMENT_OPTION_LAST_PLACED_BELOW:
            // calculate coordinates for the rectangle to be placed
            // shift rectangle as far as possible if enabled.
            if (lpShift) {
                potentialYvalue = lastPlaced.getY() + lastPlaced.getHeight();
                potentialXvalue = RectPackingCalculations.calculateXforLPB(potentialYvalue, placedRects, lastPlaced);
            } else {
                potentialXvalue = lastPlaced.getX();
                potentialYvalue = lastPlaced.getY() + lastPlaced.getHeight();
            }

            width = getWidthLPRorLPB(currDrawingWidth, potentialXvalue, toPlace.getWidth());
            height = getHeightLPRorLPB(currDrawingHeight, potentialYvalue, toPlace.getHeight());
            placementOpt = DrawingDataDescriptor.PLACEMENT_OPTION_LAST_PLACED_BELOW;
            break;
        case PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT:
            potentialXvalue = currDrawingWidth;
            potentialYvalue = 0;

            width = currDrawingWidth + toPlace.getWidth();
            height = Math.max(currDrawingHeight, toPlace.getHeight());
            placementOpt = DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_RIGHT;
            break;
        case PLACEMENT_OPTION_WHOLE_DRAWING_BELOW:
            potentialXvalue = 0;
            potentialYvalue = currDrawingHeight;

            width = Math.max(currDrawingWidth, toPlace.getWidth());
            height = currDrawingHeight + toPlace.getHeight();
            placementOpt = DrawingDataDescriptor.PLACEMENT_OPTION_WHOLE_DRAWING_BELOW;
            break;
        default:
            throw new IllegalArgumentException("IllegalPlacementOption.");
        }

        // LPR and LPB have more values to be set in newDrawing.
        newDrawing = new DrawingData(dar, width, height, placementOpt, potentialXvalue, potentialYvalue);
        return newDrawing;
    }

    /**
     * Calculates the drawing width for placement options LPR and LPB.
     * 
     * @param drawingWidth
     *            Width of drawing before placement of new rectangle.
     * @param xCoord
     *            Potential x coordinate of new rectangle.
     * @param toPlaceWidth
     *            Width of new rectangle.
     * @return Width of drawing including new rectangle in case LPR and LPB.
     */
    private double getWidthLPRorLPB(double drawingWidth, double xCoord, double toPlaceWidth) {
        return Math.max(drawingWidth, xCoord + toPlaceWidth);
    }

    /**
     * Calculates the drawing height for placement options LPR and LPB.
     * 
     * @param drawingHeight
     *            Height of drawing before placement of new rectangle.
     * @param yCoord
     *            Potential y coordinate of new rectangle.
     * @param toPlaceHeight
     *            Height of new rectangle.
     * @return Height of drawing including new rectangle in case LPR and LPB.
     */
    private double getHeightLPRorLPB(double drawingHeight, double yCoord, double toPlaceHeight) {
        return Math.max(drawingHeight, yCoord + toPlaceHeight);
    }
}
