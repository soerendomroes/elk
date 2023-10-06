package org.eclipse.elk.alg.fun.seconditeration;

import java.util.List;
import org.eclipse.elk.alg.fun.util.RectRow;
import org.eclipse.elk.alg.fun.util.RectStack;
import org.eclipse.elk.graph.ElkNode;

/**
 * Expands rectangles after the calculations of the second iteration.
 * 
 * @author dalu
 */
public final class ExpandNodesSecondIteration {

    private ExpandNodesSecondIteration() {

    }

    /**
     * Expands rectangles after the calculations of the second iteration, so that the bounding box is filled without
     * whitespace. First rows are enlarged to fit the drawings dimensions. Then, stacks are enlarged to fit the rows.
     * Lastly, rectangles are enlarged to fill the stacks.
     * 
     * @param rows
     *            Rows containing the rectangles given by {@link RectStack} classes.
     * @param drawingWidth
     *            Width of the drawing (width of the widest {@link RectRow}).
     */
    public static void expand(final List<RectRow> rows, final double drawingWidth) {
        for (RectRow row : rows) {
            // resize row to drawing width.
            double additionalWidthPerStack = (drawingWidth - row.getWidth()) / row.getChildren().size();
            row.setWidth(drawingWidth);
            
            for (int rowIdx = 0; rowIdx < row.getChildren().size(); rowIdx++) {
                RectStack stack = row.getChildren().get(rowIdx);
                // resize stack to row height.
                stack.setHeight(row.getHeight());
                double stackRightBorder = stack.getX() + stack.getWidth();
                // also, if row was enlarged before (width), the last stack needs to be enlarged as well.
                
//                if (rowIdx == row.getChildren().size() - 1 && stackRightBorder < row.getWidth()) {
                stack.setWidth(additionalWidthPerStack + stack.getWidth());
                stack.setX(stack.getX() + additionalWidthPerStack * rowIdx);
//                }

                // adjust rectangle sizes of each stack.
                for (int j = 0; j < stack.getChildren().size(); j++) {
                    ElkNode rect = stack.getChildren().get(j);
                    // resize rectangle width.
                    rect.setX(stack.getX());
                    rect.setWidth(stack.getWidth());
                    // if rectangle is the bottom rectangle of a stack, enlarge its height, if necessary.
                    if (j == stack.getChildren().size() - 1
                            && (stack.getY() + stack.getHeight()) > (rect.getY() + rect.getHeight())) {
                        rect.setHeight((stack.getY() + stack.getHeight()) - rect.getY());
                    }
                }
            }
        }
    }
}
