package org.eclipse.elk.alg.fun;

import java.util.List;

//import org.eclipse.elk.alg.rectPacking.binarysearch.BinarySearch;
import org.eclipse.elk.alg.fun.firstiteration.FirstIteration;
//import org.eclipse.elk.alg.rectPacking.lp.LinearProgramRectPacking;
import org.eclipse.elk.alg.fun.seconditeration.SecondIteration;
import org.eclipse.elk.alg.fun.specialcasesingleexpandedregion.SingleExpandedRegionPlacer;
import org.eclipse.elk.alg.fun.util.DrawingData;
import org.eclipse.elk.alg.fun.util.PackingStrategy;
import org.eclipse.elk.alg.fun.util.CoordinatesUtil;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

/**
 * <p>
 * A layout algorithm that does not take edges into account, but treats all nodes as isolated boxes. This is useful for
 * parts of a diagram that consist of objects without connections, such as parallel regions in Statecharts.
 * 
 * We view nodes as rectangles and so {@link ElkNode}s are referred to as rectangles in the comments.
 * </p>
 * 
 * @author dalu
 */
public class FunLayoutProvider extends AbstractLayoutProvider {
    /** Desired aspect ratio. */
    private double dar;
    /** Packing Strategy. */
    private PackingStrategy packingStrat;
    /** Shift when placing behind or below the last placed rectangle. */
    private boolean lpShift;
    /** Absolute aspect ratio calculation */
    private boolean absoluteAR;
    /** Only first iteration. */
    private boolean onlyFirstIteration;
    /** Check for special case */
    private boolean checkSpecialCase;
    /** Expand nodes to fill the bounding box. */
    private boolean expandNodes;
    /** Padding surrounding the drawing. */
    private ElkPadding padding;
    /** Solve by using linear programming and CPLEX. */
    private boolean lp;
    /** Solve by using LP in combination with binary search. */
    private boolean binarySearchLP;

    @Override
    public void layout(final ElkNode layoutGraph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Rect Packing", 1);
        // INIT
        // get information about graph and options
        // @formatter:off
        dar = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.ASPECT_RATIO);
        packingStrat = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_STRATEGY);
        lpShift = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_LP_SHIFT);
        absoluteAR = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_AR_CALC_ABS);
        onlyFirstIteration = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_ONLY_FIRST_ITERATION);
        checkSpecialCase = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_CHECK_FOR_SPECIAL_CASE);
        expandNodes = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.EXPAND_NODES);
        padding = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.PADDING);
        lp = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_L_P);
        binarySearchLP = layoutGraph.getProperty(
                org.eclipse.elk.alg.fun.options.FunOptions.RECT_PACKING_OPTIONS_BINARY_SEARCH_L_P);
        // @formatter:on

        List<ElkNode> rectangles = layoutGraph.getChildren();
        CoordinatesUtil.resetCoordinates(rectangles);
        DrawingData drawing;

        // PLACEMENT ACCORDING TO SETTINGS
//        if (lp) {
////            drawing = LinearProgramRectPacking.computeOptimalSolutionWithLP(rectangles, dar);
//        } else if (binarySearchLP) {
////            drawing = BinarySearch.computeSolutionWithBinarySearchLP(rectangles, dar);
//        } else {
            // intercepting a special case: one big rectangle with all other rectangles being the same size.
            if (checkSpecialCase && SingleExpandedRegionPlacer.checkSpecialCase(rectangles)) {
                drawing = SingleExpandedRegionPlacer.placeSpecialCase(rectangles, dar, expandNodes);
            } else {
                // approximate bounding box.
                FirstIteration firstIt = new FirstIteration(dar, packingStrat, absoluteAR, lpShift);
                drawing = firstIt.approxBoundingBox(rectangles);
                if (!onlyFirstIteration) {
                    CoordinatesUtil.resetCoordinates(rectangles);
                    SecondIteration secondIt = new SecondIteration(dar, expandNodes);
                    drawing = secondIt.start(rectangles, drawing.getDrawingWidth(), drawing.getDrawingHeight());
                }
            }
//        }

        // FINAL TOUCH
        // shift all rectangles according to the padding.
        enforcePadding(rectangles);
        // set bounds. Drawings dimensions plus padding.
        ElkUtil.resizeNode(layoutGraph,
                drawing.getDrawingWidth() + padding.getHorizontal(),
                drawing.getDrawingHeight() + padding.getVertical(), false, true);
        
//        layoutGraph.setDimensions( + padding.getHorizontal(),
//                drawing.getDrawingHeight() + padding.getVertical());

        progressMonitor.done();
    }

    /**
     * Shifts all rectangles to the right and bottom according to the specified padding.
     * 
     * @param rectangles
     *            List of rectangles that have been placed.
     */
    private void enforcePadding(final List<ElkNode> rectangles) {
        for (ElkNode rect : rectangles) {
            rect.setLocation(rect.getX() + padding.getLeft(), rect.getY() + padding.getTop());
        }
    }
}
