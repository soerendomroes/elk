package org.eclipse.elk.alg.fun.specialcasesingleexpandedregion;

import java.util.List;

import org.eclipse.elk.graph.ElkNode;

/**
 * Offers method to expand nodes in special case of one big rectangle and lots of smaller same-sized ones.
 * 
 * @author dalu
 */
public final class ExpandNodesSpecialCase {

    private ExpandNodesSpecialCase() {

    }

    /**
     * Expands nodes so the bounding box, given by width and height, is filled. The biggest rectangle does not need to
     * be resized.
     * 
     * @param before
     *            Rectangles that come before the biggest rectangle.
     * @param after
     *            Rectangles that come after the biggest rectangle.
     * @param height
     *            Height of the bounding box or the biggest rectangle.
     * @param howManyRows
     *            How many rows of rectangles there are before and after.
     * @param howManyColumnsAfter
     *            Amount of columns that come before the big rectangle.
     * @param howManyColumnsBefore
     *            Amount of columns that come after the big rectangle.
     */
    public static void expandNodes(final List<ElkNode> before, final List<ElkNode> after, final double height,
            final int howManyColumnsBefore, final int howManyColumnsAfter, final int howManyRows) {
        // find the bottom element of every column since the rectangles are placed in order. The last X rectangles are
        // the bottom rectangles of each column where X is the amount of columns.
        for(ElkNode rect : before.subList(before.size() - howManyColumnsBefore, before.size())){
            rect.setHeight(height - rect.getY());
        }
        for(ElkNode rect : after.subList(after.size() - howManyColumnsAfter, after.size())) {
            rect.setHeight(height - rect.getY());
        }
    }
}
