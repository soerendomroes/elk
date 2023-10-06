package org.eclipse.elk.alg.fun.seconditeration;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.elk.alg.fun.util.DrawingData;
import org.eclipse.elk.alg.fun.util.DrawingDataDescriptor;
import org.eclipse.elk.alg.fun.util.RectRow;
import org.eclipse.elk.alg.fun.util.RectStack;
import org.eclipse.elk.graph.ElkNode;

/**
 * Second iteration of the algorithm. Actual placement of the boxes inside the approximated bounding box. Sequential
 * layer based packing.
 * 
 * @author dalu
 */
public class SecondIteration {
    /** Current drawing width. */
    private double drawingWidth;
    /** Current drawing height. */
    private double drawingHeight;
    /** Indicates that something was optimized while compacting. */
    private boolean improvedCompacting;
    /** Indicates that something was optimized while row filling. */
    private boolean improvedRowFilling;
    /** Indicates that something was optimized while row filling. */
    private boolean improvedRowFillingSingleRects;
    /** Desired aspect ratio */
    private double dar;
    /** Indicates whether to expand the nodes in the end. */
    private boolean expandNodes;

    /**
     * Creates an object to execute the second iteration on.
     * 
     * @param desiredAr
     *            Desired aspect ratio.
     * @param expandNodes
     *            Indicates whether to expand the nodes in the end.
     */
    public SecondIteration(final double desiredAr, final boolean expandNodes) {
        this.dar = desiredAr;
        this.expandNodes = expandNodes;
    }

    /**
     * Placement of the rectangles inside the given bounding box.
     * 
     * @param rectangles
     *            Given set of rectangles to be placed inside the bounding box.
     * @param boundingBoxHeight
     *            Height of the given bounding box.
     * @param boundingBoxWidth
     *            Width of the given bounding box.
     * @param dar
     *            Desired aspect ratio.
     * @param expandNodes
     *            Indicates whether the nodes should be expanded to fill the bounding box.
     * @return Drawing data for a produced drawing.
     */
    public DrawingData start(final List<ElkNode> rectangles, final double boundingBoxWidth,
            final double boundingBoxHeight) {
        this.improvedCompacting = true;
        this.improvedRowFilling = true;
        this.improvedRowFillingSingleRects = true;

        // first and fast placement do this every time
        List<RectRow> rows = placeRectanglesInitially(rectangles, boundingBoxWidth);

        while (this.improvedCompacting || this.improvedRowFilling || this.improvedRowFillingSingleRects) {
            // COMPACTION
            rows = compaction(rows);

            // ROW FILLING first try to take whole stack, then single rectangles.
            rows = rowFilling(rows, boundingBoxWidth);
            rows = rowFillingSingleRects(rows, boundingBoxWidth);
            // calculate dimensions of drawing
            calculateDimensions(rows);
            // search for empty rows
            deleteEmptyRows(rows);
        }

        // expand notes if configured.
        if (this.expandNodes) {
            ExpandNodesSecondIteration.expand(rows, this.drawingWidth);
        }

        // RETURN
        DrawingData values =
                new DrawingData(this.dar, this.drawingWidth, this.drawingHeight, DrawingDataDescriptor.WHOLE_DRAWING);
        return values;
    }

    // Placement and arrangement methods.
    /**
     * Simply places the rectangles as rows onto the drawing area, bounded by the calculated bounding box width. When a
     * rectangle can fit beneath the lastly placed stack according to the current row's height. the rectangle is placed
     * in said stack.
     * 
     * @param rectangles
     *            Rectangles to be placed.
     * @param boundingBoxWidth
     *            Bounding box width.
     * @return Returns the rows in which the rectangles were placed.
     */
    private List<RectRow> placeRectanglesInitially(final List<ElkNode> rectangles, final double boundingBoxWidth) {
        List<RectRow> rows = new ArrayList<RectRow>();
        RectRow row = new RectRow(0);
        double currDrawingWidth = 0;
        double currDrawingHeight = 0;
        RectStack currentStack = null;

        // first placement in rows.
        for (ElkNode rect : rectangles) {
            // first iteration: initialization
            if (currentStack == null) {
                currentStack = createNewStackAndAddToRow(rect, row);
                continue;
            }

            // fit rectangle beneath current stack, if it is smaller than current row height and less wide than bounding
            // box
            double newStackHeight = currentStack.getHeight() + rect.getHeight();
            double newRowWidth = currentStack.getX() + rect.getWidth();

            if (newStackHeight <= row.getHeight() && newRowWidth < boundingBoxWidth) {
                double yCoord = currentStack.getY() + currentStack.getHeight();
                rect.setLocation(currentStack.getX(), yCoord);
                currentStack.addChild(rect);
            } else {
                // rectangle does not fit in stack, we need to create a new stack.
                // does the new stack exceed the bounding box?
                if (row.getWidth() + rect.getWidth() > boundingBoxWidth) {
                    // a row is finished, calculate variables.
                    currDrawingHeight += row.getHeight();
                    currDrawingWidth = Math.max(currDrawingWidth, row.getWidth());

                    rows.add(row);
                    row = new RectRow(currDrawingHeight);
                }
                // add rectangle and set its location.
                currentStack = createNewStackAndAddToRow(rect, row);
            }
        }
        rows.add(row);
        // last row finished, calculate variables.
        this.drawingWidth = currDrawingWidth + row.getHeight();
        this.drawingHeight = Math.max(currDrawingWidth, row.getWidth());

        return rows;
    }

    /**
     * Compacts the given rows by putting elements of the row beneath each other if possible and shifting left if an
     * element was rearranged. Calculates the drawing's width and height accordingly.
     * 
     * @param rows
     *            Given rows with placed rectangles.
     * @return Returns the updated rows.
     */
    private List<RectRow> compaction(List<RectRow> rows) {
        boolean somethingWasChanged = false;

        // stack to be filled with rectangles.
        RectStack stackToFill;
        double takingStackPreviousWidth = 0;
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            List<RectStack> stacksToRemove = new ArrayList<RectStack>();
            RectRow currentRow = rows.get(rowIdx);
            List<RectStack> stacks = currentRow.getChildren();
            double originalRowHeight = currentRow.getHeight();
            // if there is only one stack, there is nothing to compact.
            if (stacks.size() < 2) {
                break;
            }
            for (int stackIdx = 1; stackIdx < stacks.size(); stackIdx++) {
                RectStack yieldingStack = stacks.get(stackIdx);
                stackToFill = stacks.get(stackIdx - 1);
                takingStackPreviousWidth = stackToFill.getWidth();
                ElkNode movingRect = yieldingStack.getChildren().get(0);

                if (!stacksToRemove.contains(stackToFill)) {
                    // can we fit the moving rectangle underneath the taking stack?
                    if (stackToFill.getHeight() + movingRect.getHeight() <= currentRow.getHeight()) {
                        somethingWasChanged = true;
                        stacksToRemove = reassignRectangleAndShiftAccorindgly(movingRect, stackToFill, yieldingStack,
                                stacks, stacksToRemove, stackIdx, takingStackPreviousWidth);
                    }
                }
            }
            // remove stacks from row
            for (RectStack r : stacksToRemove) {
                currentRow.removeStack(r);
            }

            // adjust following rows vertically - shift is positive if shift has to be upwards for following rows
            double verticalShift = originalRowHeight - currentRow.getHeight();
            if (verticalShift != 0) {
                for (int rowsToShiftIdx = rowIdx + 1; rowsToShiftIdx < rows.size(); rowsToShiftIdx++) {
                    rows.get(rowsToShiftIdx).decreaseYRecursively(verticalShift);
                }
            }
        }

        this.improvedCompacting = somethingWasChanged;
        return rows;
    }

    /**
     * This method tries to sequentially fill a row as much as the bounding box width allows. It takes stacks from the
     * next row as long as the rows width is smaller than the bounding box width. Since the row's height might increase,
     * every row below has to be shifted according to the height difference. The row the stacks have been taken from has
     * to be shifted left, according to how many stacks were taken.
     * 
     * @param rows
     *            Rows containing stacks and rectangles that are to be filled.
     * @param boundingBoxWidth
     *            Width of the bounding box that should not be exceeded.
     * @return Updated rows.F
     */
    private List<RectRow> rowFilling(List<RectRow> rows, final double boundingBoxWidth) {
        boolean somethingWasChanged = false;
        for (int rowIdx = 0; rowIdx < rows.size() - 1; rowIdx++) {
            RectRow rowToFill = rows.get(rowIdx);
            RectRow yieldingRow = rows.get(rowIdx + 1);
            while (yieldingRow.numberOfStacks() > 0
                    && rowToFill.getWidth() + yieldingRow.firstStack().getWidth() < boundingBoxWidth) {
                somethingWasChanged = true;
                // put stack in the current row
                RectStack movingStack = yieldingRow.firstStack();
                double takingRowPreviousWidth = rowToFill.getWidth();
                double takingRowPreviousHeight = rowToFill.getHeight();
                rowToFill.addStack(movingStack);
                movingStack.setParentRow(rowToFill);
                yieldingRow.removeStack(movingStack);

                // adjust stacks' rectangles coordinates to new row as well as stacks coordinates.
                movingStack.setX(takingRowPreviousWidth);
                movingStack.setY(rowToFill.getY());
                movingStack.adjustChildrensXandY();

                // calculate vertical shift caused by the stack if it has a bigger height than the row it is added to.
                double verticalShiftOne = 0;
                if (movingStack.getHeight() > takingRowPreviousHeight) {
                    verticalShiftOne = movingStack.getHeight() - takingRowPreviousHeight;
                }

                // if the taken stack was the biggest stack in the yielding row, the following rows after the yielding
                // rows have to be adjusted as well.
                double verticalShiftTwo = 0;
                if (movingStack.getHeight() > yieldingRow.getHeight()) {
                    verticalShiftTwo = movingStack.getHeight() - yieldingRow.getHeight();
                }

                // actually adjusting y coordinates of all following rows according to the calculations. The yielding
                // row is only affected by the first vertical shift.
                if (verticalShiftOne > 0 || verticalShiftTwo > 0) {
                    for (int rowToShiftIdx = rowIdx + 1; rowToShiftIdx < rows.size(); rowToShiftIdx++) {
                        RectRow adjustingRow = rows.get(rowToShiftIdx);
                        // yielding row
                        if (rowToShiftIdx == rowIdx + 1) {
                            adjustingRow.increaseYRecursively(verticalShiftOne);
                            // all other rows are affected by both shifts.
                        } else {
                            adjustingRow.increaseYRecursively(verticalShiftOne - verticalShiftTwo);
                        }
                    }
                }

                // shift yielding row to the left
                double horizontalShift = movingStack.getWidth();
                yieldingRow.decreaseXRecursively(horizontalShift);

                // check whether we have taken all stacks from yieldingRow and if so, increase yieldingRow.
                // if there is no other yielding row method terminates.
                if (yieldingRow.getChildren().size() == 0) {
                    break;
                }
            }
        }

        deleteEmptyRows(rows);
        this.improvedRowFilling = somethingWasChanged;
        return rows;
    }

    /**
     * In case taking a whole stack does not work. This method tries to sequentially fill a row as much as the bounding
     * box width allows. It takes the first rectangle from the stack from the next row and adds it to the last stack of
     * the current row as long as the rows width is smaller than the bounding box width. Since the row's height might
     * increase, every row below has to be shifted according to the height difference. The row the rectangles have been
     * taken from has to be shifted left, according to the width of the stacks, that were taken.
     * 
     * @param rows
     *            Rows containing stacks and rectangles that are to be filled.
     * @param boundingBoxWidth
     *            Width of the bounding box that should not be exceeded.
     * @return Updated rows.
     */
    private List<RectRow> rowFillingSingleRects(List<RectRow> rows, final double boundingBoxWidth) {
        boolean somethingWasChanged = false;
        for (int rowIdx = 0; rowIdx < rows.size() - 1; rowIdx++) {
            RectRow currentRow = rows.get(rowIdx);
            RectRow yieldingRow = rows.get(rowIdx + 1);

            // skip empty row, which will be deleted in the loop.
            if (currentRow.numberOfStacks() == 0) {
                continue;
            }

            // initial calculation of potential current stack width and height with new rectangle from next row added
            double potentialRowWidth = currentRow.getWidth();

            boolean movingRectWidthBiggerThanLastStackWidth =
                    yieldingRow.firstRectFirstStack().getWidth() > currentRow.lastStack().getWidth();

            if (yieldingRow.numberOfStacks() > 0 && movingRectWidthBiggerThanLastStackWidth) {
                potentialRowWidth = (yieldingRow.firstRectFirstStack().getWidth() - currentRow.lastStack().getWidth())
                        + currentRow.getWidth();
            }

            double potentialHeightLastStack =
                    currentRow.lastStack().getHeight() + yieldingRow.firstRectFirstStack().getHeight();
            boolean potentialWidthFeasible = potentialRowWidth <= boundingBoxWidth;
            boolean potentialHeightFeasible = potentialHeightLastStack <= currentRow.getHeight();

            // if first rectangle of new row fits in the last stack of current row without increasing size.
            while (yieldingRow.numberOfStacks() > 0 && potentialWidthFeasible && potentialHeightFeasible) {
                somethingWasChanged = true;

                // actual reassigning and shifts
                rowFillingSingleRectReassigningAndShifting(rows, currentRow, yieldingRow, rowIdx);

                // check whether we have taken all stacks from yieldingRow and if so, increase yieldingRow.
                // if there is no other yielding row method terminates.
                if (yieldingRow.numberOfStacks() == 0) {
                    break;
                }

                // calculate new width of the row with the next rectangle in line.
                potentialRowWidth = currentRow.getWidth();
                if (yieldingRow.numberOfStacks() > 0
                        && yieldingRow.firstRectFirstStack().getWidth() > currentRow.lastStack().getWidth()) {
                    potentialRowWidth =
                            (yieldingRow.firstRectFirstStack().getWidth() - currentRow.lastStack().getWidth())
                                    + currentRow.getWidth();
                    // yielding stack is empty, we need a compaction before we can continue.
                } else if (yieldingRow.firstStack().getNumberOfRectangles() > 0) {
                    break;
                }

                potentialHeightLastStack =
                        currentRow.lastStack().getHeight() + yieldingRow.firstRectFirstStack().getHeight();
                potentialWidthFeasible = potentialRowWidth <= boundingBoxWidth;
                potentialHeightFeasible = potentialHeightLastStack <= currentRow.getHeight();
            }
        }

        this.improvedRowFillingSingleRects = somethingWasChanged;
        return rows;
    }

    /**
     * Reassigns a rectangle according to the compaction algorithm and takes care of all shifts.
     * 
     * @param movingRect
     *            Rectangle to be reassigned.
     * @param stackToFill
     *            New stack of the rectangle.
     * @param yieldingStack
     *            Current stack of the rectangle.
     * @param stacks
     *            Stacks in the row of the yielding stack.
     * @param stacksToRemove
     *            List of empty stacks that are to be removed later.
     * @param stackIdx
     *            Index of current stack that is being filled with rectangles.
     * @param takingStackPreviousWidth
     *            Width of the stack before assigning a new rectangle to it.
     * @return The list of rectangles to be removed with the additions of the stacks in this method.
     */
    private List<RectStack> reassignRectangleAndShiftAccorindgly(final ElkNode movingRect, final RectStack stackToFill,
            final RectStack yieldingStack, final List<RectStack> stacks, final List<RectStack> stacksToRemove,
            final int stackIdx, final double takingStackPreviousWidth) {
        // reassign stack
        movingRect.setLocation(stackToFill.getX(), stackToFill.getY() + stackToFill.getHeight());
        stackToFill.addChild(movingRect);
        yieldingStack.removeChild(movingRect);

        // if stack is now empty, shift all other stacks accordingly
        if (yieldingStack.getNumberOfRectangles() == 0) {
            stacksToRemove.add(yieldingStack);
            // shift all coming stacks in that row to the left accordingly.
            double distanceToShift = 0;
            if (takingStackPreviousWidth >= movingRect.getWidth()) {
                distanceToShift = movingRect.getWidth();
            } else {
                distanceToShift = takingStackPreviousWidth;
            }
            for (int stackToShiftIdx = stackIdx + 1; stackToShiftIdx < stacks.size(); stackToShiftIdx++) {
                RectStack stackToShift = stacks.get(stackToShiftIdx);
                stackToShift.adjustXRecursively(stackToShift.getX() - distanceToShift);
            }
        } else {
            // now shift all stacks according to the change of width in current stack and stack to
            // fill. Shift yielding stack and every following stack according to widths.
            double distanceToShiftFollowingStacks = 0;
            double distanceToShiftYieldingStack = 0;
            // these cases are relevant.
            if (movingRect.getWidth() > yieldingStack.getWidth() && movingRect.getWidth() > takingStackPreviousWidth) {

                distanceToShiftFollowingStacks = (yieldingStack.getWidth() - movingRect.getWidth())
                        + (movingRect.getWidth() - takingStackPreviousWidth);
                distanceToShiftYieldingStack = movingRect.getWidth() - takingStackPreviousWidth;

            } else if (movingRect.getWidth() <= yieldingStack.getWidth()
                    && movingRect.getWidth() > takingStackPreviousWidth) {

                distanceToShiftYieldingStack = movingRect.getWidth() - takingStackPreviousWidth;
                distanceToShiftFollowingStacks = distanceToShiftYieldingStack;

            } else if (movingRect.getWidth() > yieldingStack.getWidth()
                    && movingRect.getWidth() <= takingStackPreviousWidth) {

                distanceToShiftFollowingStacks = yieldingStack.getWidth() - movingRect.getWidth();
            }

            // shift all rectangles from the yielding stack to the top and shift them horizontally
            // according to calculations
            yieldingStack.adjustXRecursively(yieldingStack.getX() + distanceToShiftYieldingStack);
            yieldingStack.decreaseChildrensY(movingRect.getHeight());

            // shift all following stacks (after the yielding stack) according to the calculations.
            for (int stackToShiftIdx = stackIdx + 1; stackToShiftIdx < stacks.size(); stackToShiftIdx++) {
                RectStack stackToShift = stacks.get(stackToShiftIdx);
                stackToShift.adjustXRecursively(stackToShift.getX() + distanceToShiftFollowingStacks);
            }

        }

        return stacksToRemove;
    }

    /**
     * Actually assigns a rectangle from the yielding row to the current row. Takes care of all necessary shifts.
     * 
     * @param rows
     *            All rows of the drawing. Needed for the shifts.
     * @param currentRow
     *            Row that a rectangle is being assigned to.
     * @param yieldingRow
     *            Row that is yielding the rectangle to be reassigned.
     * @param rowIdx
     *            Index of the current row.
     */
    private void rowFillingSingleRectReassigningAndShifting(List<RectRow> rows, RectRow currentRow, RectRow yieldingRow,
            int rowIdx) {
        // put rectangle in the last stack of the current row.
        RectStack yieldingStack = yieldingRow.firstStack();
        RectStack currentStack = currentRow.lastStack();
        ElkNode movingRect = yieldingStack.getChildren().get(0);
        yieldingStack.removeChild(movingRect);
        currentStack.addChild(movingRect);

        // adjust rectangles coordinates to new stacks coordinates. Stack's height already considers the new
        // rectangle.
        movingRect.setLocation(currentStack.getX(),
                currentStack.getY() + currentStack.getHeight() - movingRect.getHeight());

        // adjust y value of yielding stacks rectangles
        yieldingStack.decreaseChildrensY(movingRect.getHeight());

        // y shift of following rows, if row size decreased due to stack size decrease.
        if (movingRect.getHeight() + yieldingStack.getHeight() > yieldingRow.getHeight()) {
            double verticalShift = movingRect.getHeight() + yieldingStack.getHeight() - yieldingRow.getHeight();
            for (int rowShiftIdx = rowIdx + 2; rowShiftIdx < rows.size(); rowShiftIdx++) {
                rows.get(rowShiftIdx).decreaseYRecursively(verticalShift);
            }
        }

        // shift yielding row to the left if necessary
        if (yieldingStack.getWidth() < movingRect.getWidth()) {
            double horizontalShift = movingRect.getWidth() - yieldingStack.getWidth();
            List<RectStack> yieldingRowStacks = yieldingRow.getChildren();
            for (int stackIdx = 1; stackIdx < yieldingRow.numberOfStacks(); stackIdx++) {
                RectStack stack = yieldingRowStacks.get(stackIdx);
                stack.adjustXRecursively(stack.getX() - horizontalShift);
            }
        }

        // check whether we have taken all rectangles from yieldingStack and if so, remove yieldingStack.
        // if there is no other yielding row method terminates.
        if (yieldingStack.getNumberOfRectangles() == 0) {
            yieldingRow.removeStack(yieldingStack);
        }
    }

    /**
     * Method that creates a new {@link RectStack}, adds the given {@link ElkNode} to the {@link RectStack}, and adds
     * the created {@link RectStack} to the given {@link RectRow}. The new {@link RectStack} and given {@link ElkNode}
     * will be at the end of the {@link RectRow}.
     * 
     * @param rect
     *            Rectangle to add to a {@link RectStack}.
     * @param row
     *            The {@link ElkNode} the new {@link RectStack} is added to.
     * @return returns the newly created {@link RectStack}.
     */
    private RectStack createNewStackAndAddToRow(final ElkNode rect, final RectRow row) {
        rect.setLocation(row.getWidth(), row.getY());
        RectStack newStack = new RectStack(rect, rect.getX(), rect.getY(), row);
        row.addStack(newStack);
        return newStack;
    }

    // Utility methods.
    /**
     * Sometimes, rows are fully cleared of stacks, but remain rows in the list. This method removes all empty rows.
     */
    private void deleteEmptyRows(final List<RectRow> rows) {
        ListIterator<RectRow> iter = rows.listIterator();
        while (iter.hasNext()) {
            if (iter.next().numberOfStacks() == 0) {
                iter.remove();
            }
        }
    }

    /**
     * Calculates the maximum width and height for the given List of rows.
     */
    private void calculateDimensions(final List<RectRow> rows) {
        // new calculation of drawings dimensions.
        double maxWidth = Double.MIN_VALUE;
        double newHeight = 0;
        for (RectRow row : rows) {
            maxWidth = Math.max(maxWidth, row.getWidth());
            newHeight += row.getHeight();
        }

        this.drawingHeight = newHeight;
        this.drawingWidth = maxWidth;
    }
}
