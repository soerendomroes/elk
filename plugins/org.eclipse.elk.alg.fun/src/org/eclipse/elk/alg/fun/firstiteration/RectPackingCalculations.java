package org.eclipse.elk.alg.fun.firstiteration;

import java.util.List;

import org.eclipse.elk.graph.ElkNode;

/**
 * Class that offers methods for small operations that are often used. This way, code is more compact.
 * 
 * @author dalu
 */
public final class RectPackingCalculations {

    private RectPackingCalculations() {

    }

    /**
     * Computes the scale measure for a given height and width according to the desired aspect ratio.
     * 
     * @param width
     *            given width
     * @param height
     *            given height
     * @param dar
     *            desired aspect ratio
     * @return Scale measure for the given parameters.
     */
    public static double computeScaleMeasure(final double width, final double height, final double dar) {
        return Math.min(1d / width, (1d / dar) / height);
    }

    /**
     * Calculates the y coordinate of the rectangle to be placed right of lastPlaced. Sometimes, when placing it right,
     * there is still unused space to the top. We want to shift as far to the top as possible.
     * 
     * @param toPlace
     *            Rectangle to be placed. X-coordinate has to be set.
     * @param placedRects
     *            A list of already placed rectangles.
     * @param xCoordRectToPlace
     *            X value of the rectangle to be placed.
     * @return Y value of the rectangle to place in the LPR option.
     */
    public static double calculateYforLPR(final double xCoordRectToPlace, final List<ElkNode> placedRects,
            final ElkNode lastPlaced) {
        ElkNode closestUpperNeighbor = null;
        double closestNeighborBottomBorder = 0;
        // find neighbors that lay between the upper and lower border of the tobePlaced rectangle
        for (ElkNode placedRect : placedRects) {
            double placedRectBottomBorder = placedRect.getY() + placedRect.getHeight();
            if (verticalOrderConstraint(placedRect, xCoordRectToPlace)) {
                // is closest neighbor?
                if (closestUpperNeighbor == null) {
                    closestUpperNeighbor = placedRect;
                } else if (lastPlaced.getY() - placedRectBottomBorder < lastPlaced.getY()
                        - closestNeighborBottomBorder) {
                    closestUpperNeighbor = placedRect;
                }
                closestNeighborBottomBorder = closestUpperNeighbor.getY() + closestUpperNeighbor.getHeight();
            }
        }

        // no neighbor yet
        if (closestUpperNeighbor == null) {
            return 0;
        } else {
            // else, choose closest neighbors bottom border
            return closestNeighborBottomBorder;
        }
    }

    /**
     * Calculates the x coordinate of the rectangle to be placed below lastPlaced. Sometimes, when placing it below,
     * there is still unused space to the left. We want to shift as far to the left as possible.
     * 
     * @param toPlace
     *            Rectangle to be placed. Y coordinate has to be set.
     * @param placedRects
     *            A list of already placed rectangles.
     * @param yCoordRectToPlace
     *            Y value of the rectangle to be placed.
     * @return X value of the rectangle to place in the LPB option.
     */
    public static double calculateXforLPB(final double yCoordRectToPlace, final List<ElkNode> placedRects,
            final ElkNode lastPlaced) {
        ElkNode closestLeftNeighbour = null;
        double closestNeighborRightBorder = 0;
        // find neighbors that lay in between the height of the tobePlaced rectangle
        for (ElkNode placedRect : placedRects) {
            double placedRectRightBorder = placedRect.getX() + placedRect.getWidth();
            if (horizontalOrderConstraint(placedRect, yCoordRectToPlace)) {
                // is closest neighbor?
                if (closestLeftNeighbour == null) {
                    closestLeftNeighbour = placedRect;
                } else if (lastPlaced.getX() - placedRectRightBorder < lastPlaced.getX() - closestNeighborRightBorder) {
                    closestLeftNeighbour = placedRect;
                }
                closestNeighborRightBorder = closestLeftNeighbour.getX() + closestLeftNeighbour.getWidth();
            }
        }

        // no neighbor yet
        if (closestLeftNeighbour == null) {
            return 0;
        } else {
            // else, choose closest neighbors right border
            return closestNeighborRightBorder;
        }
    }

    /**
     * Checks whether the placedRect produces an vertical order constraint regarding the order. If toPlace is placed
     * left of placedRect, toPlace can at most be placed at the bottom border of placedRect.
     * 
     * @param placedRect
     *            The already placed rectangle.
     * @param xCoordRectToPlace
     *            X value of the rectangle to be placed.
     * @return True, if the placedRect produces a constraint caused by the given order.
     */
    private static boolean verticalOrderConstraint(final ElkNode placedRect, final double xCoordRectToPlace) {
        return xCoordRectToPlace < placedRect.getX() + placedRect.getWidth();
    }

    /**
     * Checks whether the placedRect produces an horizontal order constraint regarding the order. If toPlace is placed
     * above of placedRect, toPlace can at most be placed at the right border of placedRect.
     * 
     * @param placedRect
     *            The already placed rectangle.
     * @param yCoordRectToPlace
     *            Y value of the rectangle to be placed.
     * @return True, if the placedRect produces a constraint caused by the given order.
     */
    private static boolean horizontalOrderConstraint(final ElkNode placedRect, final double yCoordRectToPlace) {
        return yCoordRectToPlace < placedRect.getY() + placedRect.getHeight();
    }
}
