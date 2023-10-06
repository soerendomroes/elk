package org.eclipse.elk.alg.fun.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.elk.graph.ElkNode;

/**
 * An object that abstracts a stack of rectangles (for example inside a row). This abstraction makes it easier to handle
 * the rectangles as one as well as expanding the nodes to its border.
 * <p>
 * Do not confuse this with the data type {@link Stack}. Here, Stack means vertically arranged rectangles.
 * 
 * @see RectRow
 * 
 * @author dalu
 */
public class RectStack {
    //////////////////////////////////////////////////////////////////
    // Fields
    /** Stack width, given by the widest child rectangle. */
    private double stackWidth;
    /** Stack height, given by the sum of the children's height. */
    private double stackHeight;
    /** Rectangles contained in this stack. */
    private final List<ElkNode> children = new ArrayList<ElkNode>();
    /** X coordinate of this stack. */
    private double xCoord;
    /** Y coordinate of this stack. */
    private double yCoord;
    /** The row this stack is assigned to. */
    private RectRow parentRow;

    //////////////////////////////////////////////////////////////////
    // Constructor
    /**
     * Creates a vertical rectangle stack with the given element being the first element. Adjusts height and width of
     * this stack accordingly. Sets parent row. Does not add itself as part of the parent row.
     */
    public RectStack(final ElkNode first, final double xcoordinate, final double ycoordinate, final RectRow parentRow) {
        this.parentRow = parentRow;
        this.children.add(first);
        this.xCoord = xcoordinate;
        this.yCoord = ycoordinate;

        this.stackWidth = findMaxWidth();
        this.stackHeight = findTotalHeight();
    }

    //////////////////////////////////////////////////////////////////
    // Special methods.
    /**
     * Adds given rectangle to this stack. Adjusts height and width of the stack accordingly. Does not set the
     * coordinates of the ElkNode. Informs parent about size change.
     */
    public void addChild(final ElkNode node) {
        this.children.add(node);
        adjustSizes();
    }

    /**
     * Removes element from bottom of the abstract stack. Sets height and width accordingly. Informs parent about size
     * change.
     */
    public void removeChild(final ElkNode rect) {
        this.children.remove(rect);
        adjustSizes();
    }

    /**
     * Returns the number of rectangles associated with this Stack, the amount of ElkNodes in this objects elements
     * list.
     */
    public int getNumberOfRectangles() {
        return this.children.size();
    }

    /**
     * Adjusts this stacks x value as well as its children's x values to the given x value.
     * 
     * @param x
     *            The new adjusted x value.
     */
    public void adjustXRecursively(final double x) {
        this.xCoord = x;
        for (ElkNode rect : this.children) {
            rect.setX(this.xCoord);
        }
    }

    /**
     * Shifts this stacks' children vertically upwards according to the parameter.
     * 
     * @param diff
     *            Difference that the rectangles are shifted upwards by. E.g. {@code i.Y = i.Y - diff}.
     */
    public void decreaseChildrensY(final double diff) {
        for (ElkNode rect : this.children) {
            rect.setY(rect.getY() - diff);
        }
    }

    /**
     * Shifts this stacks' children vertically downwards according to the parameter.
     * 
     * @param diff
     *            Difference that the rectangles are shifted upwards by. E.g. {@code i.Y = i.Y + diff}.
     */
    public void increaseChildrensY(final double diff) {
        for (ElkNode rect : this.children) {
            rect.setY(rect.getY() + diff);
        }
    }

    /**
     * Shifts this stacks' children vertically and horizontally according to this stack's coordinates. Needed when
     * rectangles and stack are out of sync (e.g. when a rectangle changes stacks.)
     */
    public void adjustChildrensXandY() {
        double currentYcoord = this.getY();
        for (ElkNode rect : this.children) {
            rect.setLocation(this.xCoord, currentYcoord);
            currentYcoord += rect.getHeight();
        }
    }

    //////////////////////////////////////////////////////////////////
    // Getters and setters.
    /**
     * Gets width of the stack.
     */
    public double getWidth() {
        return stackWidth;
    }

    /**
     * Sets width of the stack.
     */
    public void setWidth(final double stackWidth) {
        this.stackWidth = stackWidth;
    }

    /**
     * Gets height of the stack.
     */
    public double getHeight() {
        return stackHeight;
    }

    /**
     * Sets height of the stack.
     */
    public void setHeight(final double stackHeight) {
        this.stackHeight = stackHeight;
    }

    /**
     * Gets this stack's children.
     */
    public List<ElkNode> getChildren() {
        return children;
    }

    /**
     * Gets this stack's x coordinate.
     */
    public double getX() {
        return xCoord;
    }

    /**
     * Sets this stack's x coordinate.
     */
    public void setX(final double xcoordinate) {
        this.xCoord = xcoordinate;
    }

    /**
     * Gets this stack's y coordinate.
     */
    public double getY() {
        return yCoord;
    }

    /**
     * Sets this stack's y coordinate.
     */
    public void setY(final double ycoordinate) {
        this.yCoord = ycoordinate;
    }

    /**
     * Gets the row this stack belongs to.
     */
    public RectRow getParentRow() {
        return parentRow;
    }

    /**
     * Sets the row this stack belongs to.
     * 
     * @param parentRow
     */
    public void setParentRow(final RectRow parentRow) {
        this.parentRow = parentRow;
    }

    /**
     * Finds maximum width out of the given of this objects children.
     */
    private double findMaxWidth() {
        double maxStackWidth = Double.MIN_VALUE;
        for (ElkNode element : this.children) {
            maxStackWidth = Math.max(maxStackWidth, element.getWidth());
        }
        return maxStackWidth;
    }

    /**
     * Adjusts size of stack and notifies parent.
     */
    private void adjustSizes() {
        this.stackWidth = findMaxWidth();
        this.stackHeight = findTotalHeight();
        this.parentRow.notifyAboutNodeChange();
    }

    /**
     * Finds total height out of the given of this objects children.
     */
    private double findTotalHeight() {
        double height = 0;
        for (ElkNode element : this.children) {
            height += element.getHeight();
        }
        return height;
    }
}
