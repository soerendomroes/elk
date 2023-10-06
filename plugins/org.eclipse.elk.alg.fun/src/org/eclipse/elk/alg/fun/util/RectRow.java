package org.eclipse.elk.alg.fun.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.elk.graph.ElkNode;

/**
 * A row of rectangles inside a given bounding box. Tracks the rectangles inside the row and tracks width and height.
 * 
 * @see RectStack
 * 
 * @author dalu
 */
public class RectRow {
    //////////////////////////////////////////////////////////////////
    // Fields
    /** Height of row, given by the highest stack. */
    private double height = 0;
    /** Sum of this row's stack's widths. */
    private double width = 0;
    /** Y coordinate of this row. */
    private double yCoord;
    /** This row's stacks. */
    private final List<RectStack> children = new ArrayList<RectStack>();

    //////////////////////////////////////////////////////////////////
    // Constructors.
    /**
     * Creates a row object.
     */
    public RectRow() {
        this(0);
    }

    /**
     * Creates a row object.
     * 
     * @param yCoordinate
     *            Y coordinate of the row
     */
    public RectRow(final double yCoordinate) {
        this.yCoord = yCoordinate;
    }

    //////////////////////////////////////////////////////////////////
    // Special methods.
    /**
     * Decreases the y value of this row and its children (stacks and their respective rectangles.)
     * 
     * @param verticalDecrease
     *            Amount by which this Row's children's y coordinates should be decreased.
     */
    public void decreaseYRecursively(final double verticalDecrease) {
        this.yCoord -= verticalDecrease;
        for (RectStack stack : this.children) {
            stack.setY(stack.getY() - verticalDecrease);
            stack.decreaseChildrensY(verticalDecrease);
        }
    }

    /**
     * Increases the y value of this row and its children (stacks and their respective rectangles.)
     * 
     * @param verticalIncrease
     *            Amount by which this Row's children's y coordinates should be increased.
     */
    public void increaseYRecursively(final double verticalIncrease) {
        this.yCoord += verticalIncrease;
        for (RectStack stack : this.children) {
            stack.setY(stack.getY() + verticalIncrease);
            stack.increaseChildrensY(verticalIncrease);
        }
    }

    /**
     * Decreases the x values of all children (stacks and their respective rectangles) by the given amount.
     * 
     * @param horizontalDecrease
     *            Amount by which this Row's children's y coordinates should be decreased.
     */
    public void decreaseXRecursively(final double horizontalDecrease) {
        for (RectStack stack : this.children) {
            stack.adjustXRecursively(stack.getX() - horizontalDecrease);
        }
    }

    /**
     * Gets the first rectangle of the first stack in the row. (Left to right).
     * 
     * @return
     */
    public ElkNode firstRectFirstStack() {
        return this.getChildren().get(0).getChildren().get(0);
    }

    /**
     * Gets the first stack of this this row (the leftest).
     * 
     * @return
     */
    public RectStack firstStack() {
        return this.getChildren().get(0);
    }

    /**
     * Gets the last stack of this this row (the rightest).
     * 
     * @return
     */
    public RectStack lastStack() {
        return this.getChildren().get(this.children.size() - 1);
    }

    /**
     * Amount of stacks of this row.
     * 
     * @return
     */
    public int numberOfStacks() {
        return this.getChildren().size();
    }

    //////////////////////////////////////////////////////////////////
    // Add and remove methods.
    /**
     * Adds a children to the row. Sets last added variable. Automatically adjusts height and width.
     * 
     * @param stack
     *            Stack to add.
     */
    public void addStack(final RectStack stack) {
        this.children.add(stack);

        // adjust height and width.
        this.height = Math.max(this.height, stack.getHeight());
        this.width += stack.getWidth();
    }

    /**
     * Removes specified stack from this row. Adjusts width and height.
     * 
     * @param stack
     *            Stack to remove.
     */
    public void removeStack(final RectStack stack) {
        this.children.remove(stack);
        this.width -= stack.getWidth();

        double newMaxHeight = Double.MIN_VALUE;
        for (RectStack child : this.children) {
            newMaxHeight = Math.max(newMaxHeight, child.getHeight());
        }
        this.height = newMaxHeight;
    }

    //////////////////////////////////////////////////////////////////
    // Getters and setters.
    /**
     * Sets height.
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets height.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Gets width.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets width.
     */
    public void setWidth(final double width) {
        this.width = width;
    }

    /**
     * Sets y coordinate.
     */
    public double getY() {
        return yCoord;
    }

    /**
     * Sets y coordinate.
     */
    public void setY(final double yCoord) {
        this.yCoord = yCoord;
    }

    /**
     * Gets children.
     */
    public List<RectStack> getChildren() {
        return children;
    }

    /**
     * Called by one of its children stacks when a change was made to the children stacks like removing or adding a
     * rectangle. By removing or adding a rectangle, the stacks dimensions change which affects this rows dimensions.
     * Calculates new dimensions.
     */
    protected void notifyAboutNodeChange() {
        double totalStackWidth = 0;
        for (RectStack child : this.children) {
            totalStackWidth += child.getWidth();
        }
        this.width = totalStackWidth;

        double newMaxHeight = Double.MIN_VALUE;
        for (RectStack child : this.children) {
            newMaxHeight = Math.max(newMaxHeight, child.getHeight());
        }
        this.height = newMaxHeight;
    }

}
