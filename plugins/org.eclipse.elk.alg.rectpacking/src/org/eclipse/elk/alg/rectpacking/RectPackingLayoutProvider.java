/*******************************************************************************
 * Copyright (c) 2018, 2020 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.rectpacking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.elk.alg.rectpacking.firstiteration.AreaApproximation;
import org.eclipse.elk.alg.rectpacking.options.OptimizationGoal;
import org.eclipse.elk.alg.rectpacking.options.RectPackingOptions;
import org.eclipse.elk.alg.rectpacking.seconditeration.RowFillingAndCompaction;
import org.eclipse.elk.alg.rectpacking.util.DrawingData;
import org.eclipse.elk.alg.rectpacking.util.DrawingDataDescriptor;
import org.eclipse.elk.alg.rectpacking.util.DrawingUtil;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

import ilog.cp.*;
import ilog.cplex.IloCplex;
import ilog.concert.*;

/**
 * A layout algorithm that does not take edges into account, but treats all nodes as isolated boxes. This is useful for
 * parts of a diagram that consist of objects without connections, such as parallel regions in Statecharts.
 * <p>
 * Nodes are viewed as rectangles and so {@link ElkNode}s are referred to as rectangles in the comments.
 * </p>
 * <p>
 * Depending on the settings, checks for a specified special case, calculates a layout with a approximation algorithm or
 * uses that approximation algorithm for the needed area of the rectangles and places the rectangles nicely aligned on
 * the drawing area according to that approximation.
 * </p>
 */
public class RectPackingLayoutProvider extends AbstractLayoutProvider {
    /**
     * Calculating and applying layout to the model.
     */
    @Override
    public void layout(final ElkNode layoutGraph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Rectangle Packing", 1);

        if (progressMonitor.isLoggingEnabled()) {
            progressMonitor.logGraph(layoutGraph, "Input");
        }
        // The desired aspect ratio.
        double aspectRatio = layoutGraph.getProperty(RectPackingOptions.ASPECT_RATIO);
        // The strategy for the initial width approximation.
        OptimizationGoal goal = layoutGraph.getProperty(RectPackingOptions.OPTIMIZATION_GOAL);
        // Option for better width approximation.
        boolean lastPlaceShift = layoutGraph.getProperty(RectPackingOptions.LAST_PLACE_SHIFT);
        // Option to only do the initial width approximation.
        boolean onlyFirstIteration = layoutGraph.getProperty(RectPackingOptions.ONLY_FIRST_ITERATION);
        // Option whether the nodes should be expanded to fill the bounding rectangle.
        boolean expandNodes = layoutGraph.getProperty(RectPackingOptions.EXPAND_NODES);
        // The padding surrounding the drawing.
        ElkPadding padding = layoutGraph.getProperty(RectPackingOptions.PADDING);
        // The spacing between two nodes.
        double nodeNodeSpacing = layoutGraph.getProperty(RectPackingOptions.SPACING_NODE_NODE);
        // Whether the nodes are compacted after the initial placement.
        boolean compaction = layoutGraph.getProperty(RectPackingOptions.ROW_COMPACTION);
        // Whether the nodes should be expanded to fit the aspect ratio during node expansion.
        // Only effective if nodes are expanded.
        boolean expandToAspectRatio = layoutGraph.getProperty(RectPackingOptions.EXPAND_TO_ASPECT_RATIO);
        // Whether interactive layout is activ.
        boolean interactive = layoutGraph.getProperty(RectPackingOptions.INTERACTIVE);
        // A target width for the algorithm. If this is set the width approximation step is skipped.
        double targetWidth = -1;
        if (layoutGraph.hasProperty(RectPackingOptions.TARGET_WIDTH)) {
            targetWidth = layoutGraph.getProperty(RectPackingOptions.TARGET_WIDTH);
        }
        boolean cplexEnalbed = layoutGraph.getProperty(RectPackingOptions.CPLEX);
        boolean cplexEnalbed2 = layoutGraph.getProperty(RectPackingOptions.FUNCPLEX2);
        double cplexOptTolerance = -1;
        if (layoutGraph.hasProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE)) {
            cplexOptTolerance = layoutGraph.getProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE);
        }

        List<ElkNode> rectangles = layoutGraph.getChildren();
        DrawingUtil.resetCoordinates(rectangles);
        DrawingData drawing = null;

        if (interactive) {
            List<ElkNode> fixedNodes = new ArrayList<>();
            for (ElkNode elkNode : rectangles) {
                if (elkNode.hasProperty(RectPackingOptions.DESIRED_POSITION)) {
                    fixedNodes.add(elkNode);
                }
            }
            for (ElkNode elkNode : fixedNodes) {
                rectangles.remove(elkNode);
            }
            Collections.sort(fixedNodes, (a, b) -> {
                int positionA = a.getProperty(RectPackingOptions.DESIRED_POSITION);
                int positionB = b.getProperty(RectPackingOptions.DESIRED_POSITION);
                if (positionA == positionB) {
                    return -1;
                } else {
                    return Integer.compare(positionA, positionB);
                }
            });
            for (ElkNode elkNode : fixedNodes) {
                int position = elkNode.getProperty(RectPackingOptions.DESIRED_POSITION);
                position = Math.min(position, rectangles.size());
                rectangles.add(position, elkNode);
            }

            int index = 0;
            for (ElkNode elkNode : rectangles) {
                elkNode.setProperty(RectPackingOptions.CURRENT_POSITION, index);
                index++;
            }
        }
        // Get minimum size of parent.
        KVector minSize = ElkUtil.effectiveMinSizeConstraintFor(layoutGraph);
        // Remove padding to get the space the algorithm can use.
        minSize.x -= padding.getHorizontal();
        minSize.y -= padding.getVertical();
        if (cplexEnalbed) {
            // Begin cp optimizer solving.
            boolean logging = true;
            try {
                IloCP cp = new IloCP();
                if (cplexOptTolerance >= 0) {
                    cp.setParameter(IloCP.DoubleParam.OptimalityTolerance, cplexOptTolerance);
                }
                cp.setParameter(IloCP.DoubleParam.TimeLimit, 60 * 60);
                if (!logging) {
                    cp.setOut(null);
                    cp.setWarning(null);
                    cp.setOut(null);
                }
                int numberOfRects = rectangles.size();
                IloIntervalVar[] rectXs = new IloIntervalVar[numberOfRects];
                IloIntervalVar[] rectYs = new IloIntervalVar[numberOfRects];
                double totalWidth = 0;
                double totalHeight = 0;
                for (ElkNode rect : rectangles) {
                    totalWidth += rect.getWidth() + nodeNodeSpacing;
                    totalHeight += rect.getHeight() + nodeNodeSpacing;
                }
                if (logging) {
                    System.out.println("Total " + totalWidth + ", " + totalHeight);
                }
                // Set the length of the interval variable.
                for (int i = 0; i < numberOfRects; i++) {
                    rectXs[i] =
                            cp.intervalVar((int) rectangles.get(i).getWidth(), rectangles.get(i).getIdentifier() + "x");
                    rectXs[i].setStartMin(0);
                    rectXs[i].setStartMax((int) totalWidth);
                    rectXs[i].setEndMin(rectXs[i].getSizeMin());
                    rectXs[i].setEndMax((int) totalWidth);
                }
                for (int i = 0; i < numberOfRects; i++) {
                    rectYs[i] = cp.intervalVar((int) rectangles.get(i).getHeight(),
                            rectangles.get(i).getIdentifier() + "y");
                    rectYs[i].setStartMin(0);
                    rectYs[i].setStartMax((int) totalHeight);
                    rectYs[i].setEndMin(rectYs[i].getSizeMin());
                    rectYs[i].setEndMax((int) totalHeight);
                }

                // Define goal.
                // Max width
                IloNumExpr[] widths = new IloNumExpr[numberOfRects];
                for (int i = 0; i < numberOfRects; i++) {
                    widths[i] = cp.endOf(rectXs[i]);
                }
                IloNumExpr[] heights = new IloNumExpr[numberOfRects];
                for (int i = 0; i < numberOfRects; i++) {
                    heights[i] = cp.endOf(rectYs[i]);
                }

                IloNumExpr maxWidth = cp.max(widths);
                IloNumExpr maxHeight = cp.max(heights);
                IloNumExpr scaleMeasure = cp.min(cp.quot(aspectRatio, maxWidth), cp.quot(1, maxHeight));
                // Goal is to maximize the scale measure and minimize the area at the same time.
                IloNumExpr cpGoal = cp.sum(scaleMeasure, cp.quot(1, cp.prod(maxWidth, maxHeight)));
                cp.add(cp.maximize(cpGoal, "Scale measure goal"));
                // cp.addMinimize(cp.prod(maxWidth, maxHeight));
                // Define constraints
                for (int i = 0; i < numberOfRects; i++) {
                    IloIntervalVar rectX = rectXs[i];
                    IloIntervalVar rectY = rectYs[i];

                    // Define ordering.
                    if (i != 0) {
                        if (logging) {
                            System.out.println("Adding constraint for node" + i);
                        }
                        for (int j = 0; j < i; j++) {
                            cp.add(cp.or(cp.ge(cp.startOf(rectX), cp.sum((int) nodeNodeSpacing, cp.endOf(rectXs[j]))),
                                    cp.ge(cp.startOf(rectY), cp.sum((int) nodeNodeSpacing, cp.endOf(rectYs[j])))));
                        }
                    } else {
                        if (logging) {
                            System.out.println("First node");
                        }
                        cp.addEq(0, cp.startOf(rectXs[0]));
                        cp.addEq(0, cp.startOf(rectYs[0]));
                    }
                }

                if (cp.solve()) {

                    if (logging) {
                        System.out.println("Scale Measure " + cp.getValue(scaleMeasure));
                    }
                    for (IloIntervalVar rectX : rectXs) {
                        if (logging) {
                            System.out.println(rectX);
                            System.out.println(cp.getValue(cp.startOf(rectX)) + ", " + cp.getValue(cp.endOf(rectX))
                                    + ", " + cp.getLength(rectX));
                        }
                    }
                    for (IloIntervalVar rectY : rectYs) {
                        if (logging) {
                            System.out.println(rectY);
                            System.out.println(cp.getValue(cp.startOf(rectY)) + ", " + cp.getValue(cp.endOf(rectY))
                                    + ", " + cp.getLength(rectY));

                        }
                    }
                    int index = 0;
                    // Apply coordinates to rectangles.
                    for (ElkNode rect : rectangles) {
                        rect.setX(cp.getValue(cp.startOf(rectXs[index])));
                        rect.setY(cp.getValue(cp.startOf(rectYs[index])));
                        index++;
                    }
                    // Calculate drawing dimensions.
                    drawing = new DrawingData(aspectRatio, cp.getValue(maxWidth), cp.getValue(maxHeight),
                            DrawingDataDescriptor.WHOLE_DRAWING);
                    if (logging) {
                        System.out.println(cp.getValue(maxWidth));
                        System.out.println(cp.getValue(maxHeight));
                    }
                } else {
                    System.out.println("No solution found");
                    drawing = new DrawingData(aspectRatio, 100, 100, DrawingDataDescriptor.WHOLE_DRAWING);
                }
            } catch (IloException e) {
                System.err.println("Error " + e);
            }
        } else {
            double maxWidth = minSize.x;
            if (targetWidth < 0 || targetWidth < minSize.x) {
                // Initial width approximation.
                AreaApproximation firstIt = new AreaApproximation(aspectRatio, goal, lastPlaceShift);
                drawing = firstIt.approxBoundingBox(rectangles, nodeNodeSpacing);
                if (progressMonitor.isLoggingEnabled()) {
                    progressMonitor.logGraph(layoutGraph, "After approximation");
                }
            } else {
                drawing = new DrawingData(aspectRatio, targetWidth, 0, DrawingDataDescriptor.WHOLE_DRAWING);
            }
            // Placement according to approximated width.
            if (!onlyFirstIteration) {
                DrawingUtil.resetCoordinates(rectangles);
                if (cplexEnalbed2) {
                    IloCplex cp;
                    try {
                        cp = new IloCplex();
                        cp.setParam(IloCplex.DoubleParam.TimeLimit, 60 * 60);
                        boolean logging = true;
                        if (!logging) {
                            cp.setOut(null);
                            cp.setWarning(null);
                            cp.setOut(null);
                        }
                        // Create variables
                        IloNumVar[] x = new IloNumVar[rectangles.size()];
                        IloNumVar[] y = new IloNumVar[rectangles.size()];
                        IloNumVar[] width = new IloNumVar[rectangles.size()];
                        IloNumVar[] height = new IloNumVar[rectangles.size()];
                        int j = 0;
                        // Define width and height
                        for (ElkNode rect : rectangles) {
                            width[j] = cp.numVar(rect.getWidth(), rect.getWidth());
                            height[j] = cp.numVar(rect.getHeight(), rect.getHeight());
                            j++;
                        }
                        // Define endheight/endwidth of an element
                        IloNumExpr[] endHeight = new IloNumExpr[rectangles.size()];
                        IloNumExpr[] endWidth = new IloNumExpr[rectangles.size()];
                        IloNumExpr H = cp.numExpr();

                        minSize.x = Math.max(minSize.x, drawing.getDrawingWidth());
                        for (int i = 0; i < rectangles.size(); i++) {
                            x[i] = cp.numVar(0, 10000);
                            y[i] = cp.numVar(0, 10000);
                            endWidth[i] = cp.sum(x[i], width[i]);
                            endHeight[i] = cp.sum(y[i], height[i]);
                            cp.addLe(endWidth[i], minSize.x);
                        }
                        H = cp.max(endHeight);
                        for (int i = 0; i < rectangles.size(); i++) {
                            // Define ordering.
                            if (i != 0) {
                                if (logging) {
                                    System.out.println("Adding constraint for node" + i);
                                }
                                for (int k = 0; k < i; k++) {
                                    cp.add(cp.or(cp.ge(x[i], cp.sum((int) nodeNodeSpacing, endWidth[k])),
                                            cp.ge(y[i], cp.sum((int) nodeNodeSpacing, endHeight[k]))));
                                }
                            } else {
                                if (logging) {
                                    System.out.println("First node");
                                }
                                cp.addEq(0, x[0]);
                                cp.addEq(0, y[0]);
                            }
                        }
                        
                        // Define available position for a node based on their predecessors:
                        for (int i = 1; i < rectangles.size(); i++) {

                            IloNumVar maxXInRow = cp.numVar(0, 10000);
                            for (int k = 0; k < i; k++) {
                                cp.constraintGoal(arg0)
                                cp.add(cp.or(cp.le(y[i], y[k]), maxXinRow ));
                            }
                            cp.add(cp.or(cp.and(cp.eq(x[i], endWidth[i]), cp.eq(y[i], y[i-1])),
                                    cp.or(cp.and(cp.eq(x[i], arg1), arg1),
                                            cp.or()))));
                        }

                        cp.addMinimize(H);
                        if (cp.solve()) {
                            int index = 0;
                            // Apply coordinates to rectangles.
                            for (ElkNode rect : rectangles) {
                                if (logging) {
                                    System.out.println("X/Y (" + rect.getIdentifier() + ", " + cp.getValue(x[index])
                                            + "," + cp.getValue(y[index]) + ")");
                                }
                                rect.setX(cp.getValue(x[index]));
                                rect.setY(cp.getValue(y[index]));
                                index++;
                            }
                            // Calculate drawing dimensions.
                            drawing = new DrawingData(aspectRatio, minSize.x, cp.getValue(H),
                                    DrawingDataDescriptor.WHOLE_DRAWING);
                            if (logging) {
                                System.out.println(minSize.x);
                                System.out.println(cp.getValue(H));
                            }
                        } else {
                            System.out.println("No solution found");
                            drawing = new DrawingData(aspectRatio, 100, 100, DrawingDataDescriptor.WHOLE_DRAWING);
                        }

                    } catch (IloException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    RowFillingAndCompaction secondIt = new RowFillingAndCompaction(aspectRatio, expandNodes,
                            expandToAspectRatio, compaction, nodeNodeSpacing);
                    // Modify the initial approximation if necessary.
                    maxWidth = Math.max(minSize.x, drawing.getDrawingWidth());

                    drawing = secondIt.start(rectangles, maxWidth, minSize, progressMonitor, layoutGraph);
                }
            }
        }

        // Final touch.
        applyPadding(rectangles, padding);
        ElkUtil.resizeNode(layoutGraph, drawing.getDrawingWidth() + padding.getHorizontal(),
                drawing.getDrawingHeight() + padding.getVertical(), false, true);
        if (progressMonitor.isLoggingEnabled()) {
            progressMonitor.logGraph(layoutGraph, "Output");
        }
        progressMonitor.done();
    }

    /**
     * Shifts all rectangles to the right and bottom according to the specified padding.
     * 
     * @param rectangles
     *            list of rectangles that have been placed.
     */
    private static void applyPadding(final List<ElkNode> rectangles, ElkPadding padding) {
        for (ElkNode rect : rectangles) {
            rect.setLocation(rect.getX() + padding.getLeft(), rect.getY() + padding.getTop());
        }
    }
}
