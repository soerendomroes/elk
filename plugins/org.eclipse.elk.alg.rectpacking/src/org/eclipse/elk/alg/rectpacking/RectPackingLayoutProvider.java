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
import org.eclipse.elk.alg.rectpacking.options.RectPackingOptions;
import org.eclipse.elk.alg.rectpacking.seconditeration.RowFillingAndCompaction;
import org.eclipse.elk.alg.rectpacking.util.DrawingData;
import org.eclipse.elk.alg.rectpacking.util.DrawingDataDescriptor;
import org.eclipse.elk.alg.rectpacking.util.DrawingUtil;
import org.eclipse.elk.alg.rectpacking.util.OptimizationGoal;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

import ilog.cp.*;
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
        //  The spacing between two nodes.
        double nodeNodeSpacing = layoutGraph.getProperty(RectPackingOptions.SPACING_NODE_NODE);
        // Whether the nodes are compacted after the initial placement.
        boolean compaction = layoutGraph.getProperty(RectPackingOptions.ROW_COMPACTION);
        // Whether the nodes should be expanded to fit the aspect ratio during node expansion.
        // Only effective if nodes are expanded.
        boolean expandToAspectRatio = layoutGraph.getProperty(RectPackingOptions.EXPAND_TO_ASPECT_RATIO);
        // Whether interactive layout is activ.
        boolean interactive = layoutGraph.getProperty(RectPackingOptions.INTERACTIVE);
        boolean cplexEnalbed = layoutGraph.getProperty(RectPackingOptions.CPLEX);

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
                rectangles.add(position, elkNode);
            }

            int index = 0;
            for (ElkNode elkNode: rectangles) {
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
            try {
                IloCP cp = new IloCP();
                int numberOfRects = rectangles.size();
                IloIntervalVar[] rectXs = new IloIntervalVar[numberOfRects];
                IloIntervalVar[] rectYs = new IloIntervalVar[numberOfRects];
                
                // Set the length of the interval variable.
                for (int i = 0; i < numberOfRects; i++) {
                    rectXs[i] = cp.intervalVar((int) rectangles.get(i).getWidth(), rectangles.get(i).getIdentifier() + "x");
                    rectXs[i].setStartMin(0);
                    rectXs[i].setStartMax(10000);
                    rectXs[i].setEndMin(0);
                    rectXs[i].setEndMax(10000);
                }
                for (int i = 0; i < numberOfRects; i++) {
                    rectYs[i] = cp.intervalVar((int) rectangles.get(i).getHeight(), rectangles.get(i).getIdentifier() + "y");
                    rectYs[i].setStartMin(0);
                    rectYs[i].setStartMax(10000);
                    rectYs[i].setEndMin(0);
                    rectYs[i].setEndMax(10000);
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
                IloNumExpr scaleMeasure = cp.min(cp.quot(1, maxWidth), cp.quot(1 / aspectRatio, maxHeight));
                // Goal is to maximize the scale measure and minimize the area at the same time.
                IloNumExpr cpGoal = cp.sum(cp.prod(100000.0, scaleMeasure), cp.quot(1, cp.prod(maxWidth, maxHeight)));
                cp.add(cp.maximize(cpGoal, "Scale measure goal"));
                cp.addEq(cp.startOf(rectXs[0]), 0);
                cp.addEq(cp.startOf(rectYs[0]), 0);
                // Define constraints
                for (int i = 0; i < numberOfRects; i++) {
                    IloIntervalVar rectX = rectXs[i];
                    IloIntervalVar rectY = rectYs[i];
                    // Define maximum width and maximum height.
                    cp.add(cp.ge(maxWidth, cp.endOf(rectX)));
                    cp.add(cp.ge(maxHeight, cp.endOf(rectY)));
                    
                    // Define ordering.
                    if (i != numberOfRects - 1) {
                        cp.add(cp.or(
                                cp.gt(cp.startOf(rectXs[i+1]), cp.startOf(rectX)),
                                cp.gt(cp.startOf(rectYs[i+1]), cp.startOf(rectY))));
                    }
                    
                    // Define no overlaps.
                    for (int j = 0; j < numberOfRects; j++) {
                        IloIntervalVar rectXj = rectXs[i];
                        IloIntervalVar rectYj = rectYs[i];
                        IloConstraint firstExpr = cp.imply(
                                cp.and(
                                        cp.ge(cp.startOf(rectX), cp.startOf(rectXj)),
                                        cp.lt(cp.startOf(rectX), cp.endOf(rectXj))),
                                cp.or(
                                        cp.le(cp.endOf(rectY), cp.startOf(rectYj)),
                                        cp.ge(cp.startOf(rectY), cp.endOf(rectYj))));
                        
                        IloConstraint secondExpr = cp.imply(
                                cp.and(
                                        cp.ge(cp.endOf(rectX), cp.startOf(rectXj)),
                                        cp.lt(cp.endOf(rectX), cp.endOf(rectXj))),
                                cp.or(
                                        cp.le(cp.endOf(rectY), cp.startOf(rectYj)),
                                        cp.ge(cp.startOf(rectY), cp.endOf(rectYj))));

                        IloConstraint thirdExpr = cp.imply(
                                cp.and(
                                        cp.ge(cp.startOf(rectY), cp.startOf(rectYj)),
                                        cp.lt(cp.endOf(rectY), cp.endOf(rectYj))),
                                cp.or(
                                        cp.le(cp.endOf(rectX), cp.startOf(rectXj)),
                                        cp.ge(cp.startOf(rectX), cp.endOf(rectXj))));

                        IloConstraint fourthExpr = cp.imply(
                                cp.and(
                                        cp.ge(cp.endOf(rectY), cp.startOf(rectYj)),
                                        cp.lt(cp.endOf(rectY), cp.endOf(rectYj))),
                                cp.or(
                                        cp.le(cp.endOf(rectX), cp.startOf(rectXj)),
                                        cp.ge(cp.startOf(rectX), cp.endOf(rectXj))));
                        if (i != j) {
//                            cp.add(firstExpr);
//                            cp.add(secondExpr);
//                            cp.add(thirdExpr);
//                            cp.add(fourthExpr);
//                            cp.add(cp.and(
//                                    firstExpr
//                                    , cp.and(secondExpr
//                                            , cp.and(thirdExpr
//                                                    , fourthExpr))));
                        }
                    }
                }
                
                
                // Set up search
                IloSearchPhase[] phases = new IloSearchPhase[2];
                phases[0] = cp.searchPhase(rectXs);
                phases[1] = cp.searchPhase(rectYs);
                cp.setSearchPhases(phases);
                if (cp.solve()) {
                    System.out.println("Scale Measure " + cp.getValue(scaleMeasure));
                    for (IloIntervalVar rectX : rectXs) {
                        System.out.println(rectX);
                        System.out.println(cp.getValue(cp.startOf(rectX)) + ", " + cp.getValue(cp.endOf(rectX)) + ", " + cp.getLength(rectX));
                    }
                    for (IloIntervalVar rectY : rectYs) {
                        System.out.println(rectY);
                        System.out.println(cp.getValue(cp.startOf(rectY)) + ", " + cp.getValue(cp.endOf(rectY)) + ", " + cp.getLength(rectY));
                    }
                    int index = 0;
                    // Apply coordinates to rectangles.
                    for (ElkNode rect : rectangles) {
                        rect.setX(cp.getValue(cp.startOf(rectXs[index])));
                        rect.setY(cp.getValue(cp.startOf(rectYs[index])));
                        index++;
                    }
                    // Calculate drawing dimensions.
                    drawing = new DrawingData(aspectRatio, cp.getValue(maxWidth) / 1000, cp.getValue(maxHeight) / 1000, DrawingDataDescriptor.WHOLE_DRAWING);
                    System.out.println(cp.getValue(maxWidth) / 1000);
                    System.out.println(cp.getValue(maxHeight) / 1000);
                } else {
                    System.out.println("No solution found");
                    drawing = new DrawingData(aspectRatio, 100, 100, DrawingDataDescriptor.WHOLE_DRAWING);
                }
            } catch (IloException e) {
                System.err.println("Error " + e);
            }
//            IloCplex cplex;
//            try {
//                cplex = new IloCplex();
//                cplex.setOut(null);
//                cplex.setWarning(null);
//
//                IloIntVar[] rectX = new IloIntVar[rectangles.size()];
//                IloIntVar[] rectY = new IloIntVar[rectangles.size()];
//                int index = 0;
//                for (IloIntVar x : rectX) {
//                    x = cplex.intVar(0, 10000, rectangles.get(index).getIdentifier());
//                    index++;
//                }
//                index = 0;
//                for (IloIntVar y : rectY) {
//                    y = cplex.intVar(0, 10000, rectangles.get(index).getIdentifier());
//                    index++;
//                }
//                // Minimization expression
//                IloLinearNumExpr maximize = cplex.linearNumExpr();
//                maximize.addTerm(var, 1000);
//                cplex.addMinimize(maximize);
//            } catch (ilog.concert.IloException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            System.out.println("Fun");
//                IntParameter numerOfRects = new IntParameter("", rectangles.size());
//                cplex.add
        
//                cplex.minimize()
            // TODO set drawing width and so on.
        } else {
            // Initial width approximation.
            AreaApproximation firstIt = new AreaApproximation(aspectRatio, goal, lastPlaceShift);
            drawing = firstIt.approxBoundingBox(rectangles, nodeNodeSpacing);
            if (progressMonitor.isLoggingEnabled()) {
                progressMonitor.logGraph(layoutGraph, "After approximation");
            }
            // Placement according to approximated width.
            if (!onlyFirstIteration) {
                DrawingUtil.resetCoordinates(rectangles);
                RowFillingAndCompaction secondIt = new RowFillingAndCompaction(aspectRatio, expandNodes, expandToAspectRatio, compaction, nodeNodeSpacing);
                // Modify the initial approximation if necessary.
                minSize.x = Math.max(minSize.x, drawing.getDrawingWidth());
                
                drawing = secondIt.start(rectangles, minSize);
            }
            if (progressMonitor.isLoggingEnabled()) {
                progressMonitor.logGraph(layoutGraph, "After compaction");
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
