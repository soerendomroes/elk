package org.eclipse.elk.alg.fun.specialcasesingleexpandedregion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.elk.alg.fun.util.DrawingData;
import org.eclipse.elk.alg.fun.util.DrawingDataDescriptor;
import org.eclipse.elk.graph.ElkNode;

/**
 * Class that offers methods for the special case that one big rectangle exists while all other rectangles are smaller
 * and the same size.
 * 
 * @author dalu
 */
public class SingleExpandedRegionPlacer {

    /**
     * Places the rectangles smartly and compactly.
     * 
     * @param rectangles
     *            Rectangles in order they are supposed to be placed.
     * @param dar
     *            Desired Aspect Ratio.
     * @param expandNodes
     *            Indicates whether the nodes should be expanded to fill the bounding box.
     * @return Values of the drawing (width, height, scale measure, area.
     */
    public static DrawingData placeSpecialCase(List<ElkNode> rectangles, double dar, boolean expandNodes) {
        ElkNode biggestRect = findBiggestRectangle(rectangles);
        ElkNode smallerRect = findOneOfTheSmallerRectangles(rectangles, biggestRect);

        // figure out how many rectangles can be fit to fill the height of the biggest rectangle
        int howManyRows = (int) (biggestRect.getHeight() / smallerRect.getHeight());
        // find out, which rectangles have to be placed "before" and "after" the biggest rectangle.
        List<ElkNode> before = new ArrayList<ElkNode>();
        List<ElkNode> after = new ArrayList<ElkNode>();
        boolean beforeAfterSwitch = true;

        // checking which rectangles come before and after, respectively.
        for (ElkNode rect : rectangles) {
            if (rect.equals(biggestRect)) {
                beforeAfterSwitch = false;
            } else {
                if (beforeAfterSwitch) {
                    before.add(rect);
                } else {
                    after.add(rect);
                }
            }
        }

        /*
         * by having the amount of rectangles to be placed before and after and how many rectangles fit inside the
         * height, we can now determine how many columns of rectangles we'll have to place. Add plus one, since integer
         * makes 2 out of 2.5. And if we need 2.5 columns to place the rectangles, two will not be enough. But if we
         * need an integer amount of columns, adding one is problematic (e.g. 2 + 1 = 3 one column too many). So round
         * up.
         */
        int howManyColumnsBefore = (int) Math.ceil(1.0 * before.size() / howManyRows);
        int howManyColumnsAfter = (int) Math.ceil(1.0 * after.size() / howManyRows);
        double currWidth = 0;

        // Place before
        placeRects(currWidth, before, smallerRect.getWidth(), smallerRect.getHeight(), howManyColumnsBefore,
                howManyRows);

        // calculate width of rectangles placed before biggest rectangle and biggest rectangles' x coordinate.
        currWidth = howManyColumnsBefore * smallerRect.getWidth();

        // place BiggestRect
        biggestRect.setLocation(currWidth, 0);
        currWidth += biggestRect.getWidth();

        // Place after
        placeRects(currWidth, after, smallerRect.getWidth(), smallerRect.getHeight(), howManyColumnsAfter, howManyRows);
        currWidth += howManyColumnsAfter * smallerRect.getWidth();

        // expand nodes
        if (expandNodes) {
            ExpandNodesSpecialCase.expandNodes(before, after, biggestRect.getHeight(), howManyColumnsBefore,
                    howManyColumnsAfter, howManyRows);
        }

        // prepare return object.
        DrawingData returner = new DrawingData(dar, currWidth, biggestRect.getHeight(), DrawingDataDescriptor.WHOLE_DRAWING);
        return returner;
    }

    /**
     * Checks, whether the given rectangles meet the criteria for being packed in the special case: One big rectangle
     * with a set of smaller rectangles of the same size.
     * 
     * @param rectangles
     *            Rectangles to be packed.
     * @return True, if special case is met, false otherwise.
     */
    public static boolean checkSpecialCase(List<ElkNode> rectangles) {
        List<ArrayList<ElkNode>> sizes = new ArrayList<ArrayList<ElkNode>>();
        boolean foundListWithSameSize = false;
        boolean firstIteration = true;

        for (ElkNode rect : rectangles) {
            // add initial rectangle size
            if (sizes.size() == 0 && firstIteration) {
                List<ElkNode> first = new ArrayList<ElkNode>();
                first.add(rect);
                sizes.add((ArrayList<ElkNode>) first);
                firstIteration = false;
            }

            // check whether there has already been a rectangle of this size.
            foundListWithSameSize = false;
            for (List<ElkNode> list : sizes) {
                if (list.get(0).getWidth() == rect.getWidth() && list.get(0).getHeight() == rect.getHeight()) {
                    list.add(rect);
                    foundListWithSameSize = true;
                    break;
                }
            }

            // there has not been a rectangle of this size. Make an entry for this size.
            if (foundListWithSameSize == false) {
                List<ElkNode> newSize = new ArrayList<ElkNode>();
                newSize.add(rect);
                sizes.add((ArrayList<ElkNode>) newSize);
            }
        }

        // only two sizes are there. the bigger one must have a quantity of one whereas the smaller one has to fit
        // inside the bigger ones dimensions.
        if (sizes.size() == 2) {
            List<ElkNode> sizeOne = sizes.get(0);
            List<ElkNode> sizeTwo = sizes.get(1);
            if (sizeOne.size() >= sizeTwo.size() && sizeTwo.size() == 1
                    && sizeOne.get(0).getWidth() < sizeTwo.get(0).getWidth()
                    && sizeOne.get(0).getHeight() < sizeTwo.get(0).getHeight()) {
                return true;
            } else if (sizeOne.size() <= sizeTwo.size() && sizeOne.size() == 1
                    && sizeOne.get(0).getWidth() > sizeTwo.get(0).getWidth()
                    && sizeOne.get(0).getHeight() > sizeTwo.get(0).getHeight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Places rectangles in list according to starting x position in the given amount of columns and rows and the given
     * dimensions.
     * 
     * @param startX
     *            Start x coordinate.
     * @param rectList
     *            List of rectangles to be placed.
     * @param smallRectWidth
     *            Width of the rectangles to be placed.
     * @param smallRectHeight
     *            Height of the rectangles to be placed.
     * @param howManyColumns
     *            Number of columns.
     * @param howManyRows
     *            Number of rows.
     */
    private static void placeRects(double startX, List<ElkNode> rectList, double smallRectWidth, double smallRectHeight,
            int howManyColumns, int howManyRows) {
        double currentX = startX;
        double currentY = 0;
        int columnCounter = 1;
        int rowCounter = 1;

        // place rectangles sequentially according to given rows and columns.
        for (ElkNode rect : rectList) {
            rect.setLocation(currentX, currentY);
            columnCounter++;
            currentX += smallRectWidth;
            if (columnCounter > howManyColumns) {
                columnCounter = 1;
                rowCounter++;
                if (rowCounter > howManyRows) {
                    break;
                }
                currentX = startX;
                currentY += smallRectHeight;
            }
        }
    }

    // Helping methods.
    /**
     * Finds and returns the biggest rectangle in the list.
     */
    private static ElkNode findBiggestRectangle(List<ElkNode> rectangles) {
        ElkNode biggest = rectangles.get(0);

        for (ElkNode rect : rectangles) {
            if (rect.getWidth() * rect.getHeight() > biggest.getWidth() * biggest.getHeight()) {
                biggest = rect;
            }
        }
        return biggest;
    }

    /**
     * Finds and returns one of the rectangles that is smaller than the biggest one.
     */
    private static ElkNode findOneOfTheSmallerRectangles(List<ElkNode> rectangles, ElkNode biggest) {
        for (ElkNode rect : rectangles) {
            if (rect.getWidth() * rect.getHeight() < biggest.getWidth() * biggest.getHeight()) {
                return rect;
            }
        }
        return null;
    }
}
