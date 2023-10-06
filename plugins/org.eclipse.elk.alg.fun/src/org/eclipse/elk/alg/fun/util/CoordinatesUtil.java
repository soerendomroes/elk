package org.eclipse.elk.alg.fun.util;

import java.util.List;

import org.eclipse.elk.graph.ElkNode;

/**
 * Class that offers a different methods related to coordinates.
 * 
 * @author dalu
 */
public final class CoordinatesUtil {

    // empty private constructor
    private CoordinatesUtil() {

    }

    /**
     * Resets coordinates of every element to (0,0).
     * 
     * @param graph
     *            Given graph to reset the coordinates from.
     */
    public static void resetCoordinates(List<ElkNode> graph) {
        for (ElkNode node : graph) {
            node.setLocation(0, 0);
        }
    }

    /**
     * Finds the drawing width of the given rectangles.
     * 
     * @param rectangles
     *            Placed rectangles whose drawing width is to be determined.
     * @return {@code Maximum e.width + e.X}
     */
    public static double findDrawingWidth(List<ElkNode> rectangles) {
        double maxWidth = Double.MIN_VALUE;
        for (ElkNode element : rectangles) {
            maxWidth = Math.max(maxWidth, element.getWidth() + element.getX());
        }
        return maxWidth;
    }

    /**
     * Finds the drawing height of the given rectangles.
     * 
     * @param rectangles
     *            Placed rectangles whose drawing width is to be determined.
     * @return {@code Maximum e.height + e.Y}
     */
    public static double findDrawingHeight(List<ElkNode> rectangles) {
        double maxHeight = Double.MIN_VALUE;
        for (ElkNode element : rectangles) {
            maxHeight = Math.max(maxHeight, element.getHeight() + element.getY());
        }
        return maxHeight;
    }
}
