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

import org.eclipse.elk.alg.common.NodeMicroLayout;
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
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

import com.google.common.collect.Lists;

import ilog.cp.*;
//import ilog.cplex.IloCplex;
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
        int intSpacing = (int) nodeNodeSpacing;
        // Whether the nodes are compacted after the initial placement.
        boolean compaction = layoutGraph.getProperty(RectPackingOptions.ROW_COMPACTION);
        // Whether the nodes should be expanded to fit the aspect ratio during node expansion.
        // Only effective if nodes are expanded.
        boolean expandToAspectRatio = layoutGraph.getProperty(RectPackingOptions.EXPAND_TO_ASPECT_RATIO);
        // Whether interactive layout is activ.
        boolean cplexEnalbed = layoutGraph.getProperty(RectPackingOptions.CPLEX);
        // cplexEnalbed = false;
        boolean cplexEnalbed2 = layoutGraph.getProperty(RectPackingOptions.FUNCPLEX2);
        double cplexOptTolerance = -1;
        if (layoutGraph.hasProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE)) {
            cplexOptTolerance = layoutGraph.getProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE);
        }
        boolean interactive = layoutGraph.getProperty(RectPackingOptions.INTERACTIVE);
        // A target width for the algorithm. If this is set the width approximation step is skipped.
        double targetWidth = layoutGraph.getProperty(RectPackingOptions.TARGET_WIDTH);

        List<ElkNode> rectangles = layoutGraph.getChildren();
        DrawingUtil.resetCoordinates(rectangles);
        DrawingData drawing = null;
        // XXX Do not commit Log input model
        if (true) {
            System.out.println("\n Graph " + layoutGraph);
            // Log important properties
            System.out.println(CoreOptions.ALGORITHM + ": " + RectPackingOptions.ALGORITHM_ID);
            System.out.println(RectPackingOptions.SPACING_NODE_NODE + ": " + nodeNodeSpacing);
            System.out.println(RectPackingOptions.EXPAND_NODES + ": " + expandNodes);
            System.out.println(RectPackingOptions.ASPECT_RATIO + ": " + aspectRatio);
            
            for (ElkNode elkNode : rectangles) {
                System.out.println("node " + elkNode.getIdentifier() + " {layout [size: " + elkNode.getWidth()  + ", " + elkNode.getHeight() + "]}"); 
            }
        }
        // XXX

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
            boolean logging = false;
            try {
                IloCP cp = new IloCP();
                if (cplexOptTolerance >= 0) {
                    cp.setParameter(IloCP.DoubleParam.OptimalityTolerance, cplexOptTolerance);
                }
                cp.setParameter(IloCP.DoubleParam.TimeLimit, 60 * 60);
//                cp.setParameter(IloCP.IntParam.LogVerbosity, IloCP.ParameterValues.Quiet);
//                cp.setParameter(IloCP.IntParam.ConflictRefinerOnVariables, IloCP.ParameterValues.On);
//                cp.setParameter(IloCP.DoubleParam.RelativeOptimalityTolerance,0.01);
                if (!logging) {
                    cp.setOut(null);
                    cp.setWarning(null);
                    cp.setOut(null);
                }
                int numberOfRects = rectangles.size();
                int[] rectWidth = new int[numberOfRects];
                int[] rectHeight = new int[numberOfRects];
                IloIntervalVar[] rectXs = new IloIntervalVar[numberOfRects];
                IloIntervalVar[] rectYs = new IloIntervalVar[numberOfRects];
                double totalWidth = 0;
                int totalHeight = 0;
                for (ElkNode rect : rectangles) {
                    totalWidth += (int) rect.getWidth() + intSpacing;
                    totalHeight += (int) rect.getHeight() + intSpacing;
                }
                if (logging) {
                    System.out.println("Total " + totalWidth + ", " + totalHeight);
                }
                // Set the length of the interval variable.
                for (int i = 0; i < numberOfRects; i++) {
                    rectWidth[i] = (int) rectangles.get(i).getWidth();
                    rectXs[i] =
                            cp.intervalVar(rectWidth[i], rectangles.get(i).getIdentifier() + "x");
                    rectXs[i].setStartMin(0);
                    rectXs[i].setStartMax((int) totalWidth);
                    rectXs[i].setEndMin(rectXs[i].getSizeMin());
                    rectXs[i].setEndMax((int) totalWidth);
                }
                for (int i = 0; i < numberOfRects; i++) {
                    rectHeight[i] = (int) rectangles.get(i).getHeight();
                    rectYs[i] = cp.intervalVar(rectHeight[i],
                            rectangles.get(i).getIdentifier() + "y");
                    rectYs[i].setStartMin(0);
                    rectYs[i].setStartMax((int) totalHeight);
                    rectYs[i].setEndMin(rectYs[i].getSizeMin());
                    rectYs[i].setEndMax((int) totalHeight);
                }

                // Define goal.
                // Max width
                IloNumExpr[] widths = new IloNumExpr[numberOfRects];
//                IloNumExpr[] rectWidths = new IloNumExpr[numberOfRects];
                for (int i = 0; i < numberOfRects; i++) {
                    widths[i] = cp.endOf(rectXs[i]);
//                    rectWidths[i] = cp.constant((int) rectangles.get(i).getWidth());
                }
                IloNumExpr[] heights = new IloNumExpr[numberOfRects];
//                IloNumExpr[] rectHeights = new IloNumExpr[numberOfRects];
                for (int i = 0; i < numberOfRects; i++) {
                    heights[i] = cp.endOf(rectYs[i]);
//                    rectHeights[i] = cp.constant((int) rectangles.get(i).getHeight());
                }

                IloNumExpr maxWidth2 = cp.max(widths);
                IloNumExpr maxHeight = cp.max(heights);
                IloNumExpr scaleMeasure = cp.min(cp.quot(aspectRatio, maxWidth2), cp.quot(1, maxHeight));
                // Goal is to maximize the scale measure and minimize the area at the same time.
                IloNumExpr cpGoal = cp.sum(scaleMeasure, cp.quot(1, cp.prod(maxWidth2, maxHeight)));
                cp.add(cp.maximize(cpGoal, "Scale measure goal"));
                // cp.addMinimize(cp.prod(maxWidth, maxHeight));
                // Define constraints
                IloNumExpr[] currentMaxHeight = new IloNumExpr[numberOfRects];
//                IloNumVar[] lastStackWidth = new IloNumVar[numberOfRects];
//                IloNumVar[] currentRowLevel = new IloNumVar[numberOfRects];
//                IloNumVar[] currentStackWidth = new IloNumVar[numberOfRects];
//                IloNumVar[] currentSubRowEnd = new IloNumVar[numberOfRects];
                
                IloIntVar[] lastStackEnd = new IloIntVar[numberOfRects];
                IloIntVar[] currentRowLevel = new IloIntVar[numberOfRects];
                IloIntVar[] currentStackEnd = new IloIntVar[numberOfRects];
                IloIntVar[] currentSubRowEnd = new IloIntVar[numberOfRects];
//                IloIntVar[] choosenPosition = new IloIntVar[numberOfRects];
//                IloNumExpr[] currentSubRowLevel = new IloNumExpr[numberOfRects];
                for (int i = 0; i < numberOfRects; i++) {
                    IloIntervalVar rectX = rectXs[i];
                    IloIntervalVar rectY = rectYs[i];
//                    cp.add(cp.eq(cp.sum(cp.startOf(rectX), rectWidths[i]), cp.endOf(rectX)));
//                    cp.add(cp.eq(cp.sum(cp.startOf(rectY), rectHeights[i]), cp.endOf(rectY)));
                    // Define ordering.
                    if (i != 0) {
                        if (logging) {
                            System.out.println("Adding constraint for node" + i);
                        }
                        currentMaxHeight[i] = cp.max(currentMaxHeight[i - 1], cp.sum(cp.endOf(rectY), intSpacing));
//                        lastStackWidth[i] = cp.numVar(0, (int) totalWidth);
//                        currentRowLevel[i] = cp.numVar(0, (int) totalHeight);
//                        currentStackWidth[i] = cp.numVar(0, (int) totalWidth);
//                        currentSubRowEnd[i] = cp.numVar(0, (int) totalHeight);
                        
                        // Constrain helper
                        currentStackEnd[i] = cp.intVar(0, (int) totalWidth);
                        // currentStackWidth[i - 1] + spacing + width >= currentStackWidth, otherwise space is wasted
                        cp.addGe(cp.sum(currentStackEnd[i - 1],  intSpacing + rectWidth[i]), currentStackEnd[i]);
                        lastStackEnd[i] = cp.intVar(0, (int) totalWidth);
                        // The lastStackWidth is at most the width of the current stack, if a new stack is created.
                        // Otherwise it does not change or is set to zero
                        cp.addGe(currentStackEnd[i], lastStackEnd[i]);
                        currentRowLevel[i] = cp.intVar(0, (int) totalHeight);
                        // rowlvel[i-1] <= rowLevel[i] <= maxHeight[i-1]
                        cp.addLe(currentRowLevel[i - 1], currentRowLevel[i]);
                        cp.addGe(currentMaxHeight[i - 1], currentRowLevel[i]);
                        currentSubRowEnd[i] = cp.intVar(0, (int) totalHeight);
                        // subrowEnd[i - 1] <= subrow end <= maxHeight
                        cp.addLe(cp.sum(currentRowLevel[i],  intSpacing + rectHeight[i]), currentSubRowEnd[i]);
                        cp.addGe(currentMaxHeight[i], currentSubRowEnd[i]);
                        
                        // Constrain position
                        // New rectanbgle can at most be placed right of the previous stack by either
                        // forming their own stack
                        // or being placed right of the last rect in the same subrow
                        cp.addGe(currentStackEnd[i - 1], cp.startOf(rectX));
                        // A new rectangle can at most be placed on below all existing rectangles. (as new row or new subrow)
                        cp.addGe(currentMaxHeight[i - 1], cp.startOf(rectY));
                        
//                        currentSubRowLevel[i] = cp.numExpr();
//                        choosenPosition[i] = cp.intVar(1, 4);
                        if (logging) {
                            System.out.println("FUn");
                        }
                        IloConstraint[] constraint = new IloConstraint[4];

                        if (logging) {
                            System.out.println("FUn");
                        }
                        // Case: In new row
                        constraint[3] = // cp.and(cp.eq(0, cp.startOf(rectX)), cp.eq(currentMaxHeight[i - 1], cp.startOf(rectY)));
                                cp.and(cp.eq(0, cp.startOf(rectX)), // Bind x-coordinate
//                                cp.and(cp.eq(currentSubRowLevel[i], cp.startOf(rectY)), // Bind subrow level
                                cp.and(cp.eq(cp.constant(intSpacing + rectWidth[i]), currentStackEnd[i]), // Bind currentStackWidth
                                cp.and(cp.eq(cp.constant(0), lastStackEnd[i]), // Bind lastStackWidth
                                cp.and(cp.eq(currentMaxHeight[i - 1], currentRowLevel[i]), // Bind row level
                                cp.and(cp.eq(cp.sum(intSpacing + rectHeight[i], currentRowLevel[i]), currentSubRowEnd[i]), // Bind subrow end height
                                cp.and(cp.ge(cp.startOf(rectXs[i - 1]), cp.startOf(rectX)), // I do not really ned this??
                                cp.and(cp.lt(cp.endOf(rectYs[i - 1]), cp.startOf(rectY)), // why no spacing ehre for <=??
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(1)),
                                        cp.eq(currentMaxHeight[i - 1], cp.startOf(rectY))))))))); // // Bind y-coordinate, same as currentRowLevel
                        
                        // Case left of current one in same subrow
                        constraint[0] =
                                cp.and(cp.eq(cp.sum(intSpacing, cp.endOf(rectXs[i-1])), cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(lastStackEnd[i-1], lastStackEnd[i]), // Bind lastStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
                                cp.and(cp.eq(currentStackEnd[i], cp.max(currentStackEnd[i - 1], cp.sum(intSpacing, cp.endOf(rectX)))), // Bind currentStackWidth
//                                cp.and(cp.eq(currentSubRowLevel[i], currentSubRowLevel[i - 1]), // Bind subrowlevel
                                cp.and(cp.eq(currentSubRowEnd[i], cp.max(currentSubRowEnd[i-1], cp.sum(intSpacing, cp.endOf(rectY)))), // Bind subrow end height
                                cp.and(cp.lt(cp.endOf(rectXs[i - 1]), cp.startOf(rectX)), // This is the reason why spacing 0 does not work!!!! FIXME
                                cp.and(cp.eq(cp.startOf(rectYs[i - 1]), cp.startOf(rectY)), // Doubled with binding y FIXME
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(2)),
                                        cp.eq(cp.startOf(rectY), cp.startOf(rectYs[i - 1]))))))))); // Bind y
                        
                        // Case right of last one in new stack
                        constraint[1] =
                                cp.and(cp.eq(currentStackEnd[i - 1], cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(currentStackEnd[i], cp.sum(intSpacing, cp.endOf(rectX))), // Bind currentStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
//                                cp.and(cp.eq(currentRowLevel[i-1], currentSubRowLevel[i]), // Bind subrowlevel
                                cp.and(cp.eq(lastStackEnd[i], currentStackEnd[i-1]), // Bind lastStackWidth
                                cp.and(cp.eq(currentSubRowEnd[i], cp.sum(intSpacing + rectHeight[i], currentRowLevel[i])), // Bind subrow end height
                                cp.and(cp.lt(cp.endOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.ge(cp.startOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(3)),
                                        cp.eq(currentRowLevel[i], cp.startOf(rectY))))))))); // Bind y
                        
                        // Case in new subrow
                        constraint[2] = 
                                cp.and(cp.eq(lastStackEnd[i], cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(currentStackEnd[i], cp.max(currentStackEnd[i-1], cp.sum(cp.endOf(rectX), intSpacing))), // Bind currentStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
//                                cp.and(cp.eq(currentSubRowEnd[i-1], currentSubRowLevel[i]), // Bind subrowlevel
                                cp.and(cp.eq(lastStackEnd[i], lastStackEnd[i-1]), // Bind lastStackWidth
                                cp.and(cp.eq(currentSubRowEnd[i], cp.sum(intSpacing + rectHeight[i], currentSubRowEnd[i - 1])), // Bind subrow end height
                                cp.and(cp.ge(cp.startOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.lt(cp.endOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(4)),
                                        cp.eq(currentSubRowEnd[i - 1], cp.startOf(rectY))))))))); //Bind y
                        
                        // DOMINANT ELEMENT
                        // FIXME maybe this constraint has to be added before the others to constrain positions and stuff.
                        // One of the previous nodes with the same row level as the last one has a row height equal
                        // to the difference of the new row level and the last one.
                        
                        // On every row change, there exists a previous element that begins at the previous row level
                        // and reaches to the current row level.
                        if (i > 2) {
                            IloConstraint[] dominantElement = new IloConstraint[i];
                            for (int index = 0; index < i; index++) {
                                dominantElement[index] = 
                                    cp.and(
                                            cp.eq(cp.startOf(rectYs[index]), currentRowLevel[i-1]),
                                            cp.eq(currentRowLevel[i], cp.sum(cp.endOf(rectYs[index]), intSpacing))
                                    );
                            }
                            cp.add(cp.imply(
                                    cp.neq(currentRowLevel[i-1], currentRowLevel[i]),
                                        cp.or(dominantElement)
                                )
                            );                                
                        }
                        
                        
                        

                        if (logging) {
                            System.out.println("Added dominant");
                        }
                        

                        if (logging) {
                            System.out.println("FUn");
                        }
                        cp.add(cp.or(constraint));
                        for (int j = 0; j < i; j++) {
                            cp.add(cp.or(cp.ge(cp.startOf(rectX), cp.sum((int) nodeNodeSpacing, cp.endOf(rectXs[j]))),
                                    cp.ge(cp.startOf(rectY),
                                            cp.sum((int) nodeNodeSpacing, cp.endOf(rectYs[j])))));
                        }
                    } else {
                        if (logging) {
                            System.out.println("First node");
                        }
//                        choosenPosition[0] = cp.intVar(0,  0);
                        cp.addEq(0, cp.startOf(rectXs[0]));
                        cp.addEq(0, cp.startOf(rectYs[0]));
                        cp.addEq(cp.sum(cp.startOf(rectX), rectWidth[i]), cp.endOf(rectX));
                        cp.addEq(cp.sum(cp.startOf(rectY), rectHeight[i]), cp.endOf(rectY));
                        currentMaxHeight[i] = cp.constant(intSpacing + rectHeight[i]);
//                        lastStackWidth[i] = cp.numVar(0, 0);
//                        currentRowLevel[i] = cp.numVar(0, 0);
//                        currentStackWidth[i] = cp.numVar(intSpacing + rectWidth[i], intSpacing + rectWidth[i]);
//                        currentSubRowEnd[i] = cp.numVar(intSpacing + rectHeight[i], intSpacing + rectHeight[i]);

                        lastStackEnd[i] = cp.intVar(0, 0);
                        currentRowLevel[i] = cp.intVar(0, 0);
                        currentStackEnd[i] = cp.intVar(intSpacing + rectWidth[i], intSpacing + rectWidth[i]);
                        currentSubRowEnd[i] = cp.intVar(intSpacing + rectHeight[i], intSpacing + rectHeight[i]);
//                        currentSubRowLevel[i] = cp.constant(0);
                    }
                    
                }

                // DOMINANT ELEMENT for last row
                // Add the same constraint for the last row level and maxheight
                IloConstraint[] dominantElementForLastRow = new IloConstraint[numberOfRects];
                for (int index = 0; index < numberOfRects; index++) {
                    dominantElementForLastRow[index] = 
                        cp.and(
                                cp.eq(cp.startOf(rectYs[index]), currentRowLevel[numberOfRects - 1]),
                                cp.le( // FIXME maybe this should be eq?
                                    maxHeight,
                                    cp.sum(
                                        cp.endOf(rectYs[index]), // maxHeight does not include the spacing
                                        1 // + 1 since the maxHeight always includes the spacing?
                                    )
                                )
                        );
                }
                cp.add(cp.or(dominantElementForLastRow));

                if (logging) {
                    System.out.println("Trying to solve");
                }
                if (cp.solve()) {
                    if (logging) {
                        System.out.println("Scale Measure " + cp.getValue(scaleMeasure));
                    }
//                    int counter = 0;
//                    for (IloIntVar rowLevel : currentRowLevel) {
//                        if (true) {
//                            System.out.println("Row level of " + counter + ":");
//                            System.out.println(cp.getValue(rowLevel));
//                        }
//                        counter++;
//                    }
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
                    drawing = new DrawingData(aspectRatio, cp.getValue(maxWidth2), cp.getValue(maxHeight),
                            DrawingDataDescriptor.WHOLE_DRAWING);
                    if (logging) {
                        System.out.println(cp.getValue(maxWidth2));
                        System.out.println(cp.getValue(maxHeight));
                    }

                    if (progressMonitor.isLoggingEnabled()) {
                        progressMonitor.logGraph(layoutGraph, "Output");
                    }
                    
                    // Expand nodes by iterating over all other nodes and check whether they are "visible" from the
                    // right or down border. Use this to calculate the width and height increase.
                    // Nodes are looked at in the reverse order to make sure that the whitespace elimination works.
                    if (expandNodes) {
                        for (ElkNode rect : Lists.reverse(rectangles)) {
                            double increaseRight = drawing.getDrawingWidth();
                            double increaseDown = drawing.getDrawingHeight();
                            for (ElkNode otherRect : rectangles) {
                                if (rect == otherRect) continue;
                                
                                // Case visible by right border
                                if (otherRect.getX() >= rect.getX() + rect.getWidth() + nodeNodeSpacing
                                        && ((rect.getY() <= otherRect.getY() && otherRect.getY() <= rect.getY() + rect.getHeight())
                                            || (rect.getY() <= otherRect.getY() + otherRect.getHeight() && otherRect.getY() + otherRect.getHeight() <= rect.getY() + rect.getHeight())
                                            || (otherRect.getY() <= rect.getY() && rect.getY() <= otherRect.getY() + otherRect.getHeight())
                                            || (otherRect.getY() <= rect.getY() + rect.getHeight() && rect.getY() + rect.getHeight() <= otherRect.getY() + otherRect.getHeight()))) {
                                    increaseRight = Math.min(increaseRight, otherRect.getX() - nodeNodeSpacing);
                                    if (increaseRight - rect.getX() < rect.getWidth()) {
                                        System.out.println("ERROR");
                                    }
                                }
                                // Case visible by bottom border
                                if (otherRect.getY() >= rect.getY() + rect.getHeight() + nodeNodeSpacing
                                        && ((rect.getX() <= otherRect.getX() && otherRect.getX() <= rect.getX() + rect.getWidth())
                                            || (rect.getX() <= otherRect.getX() + otherRect.getWidth() && otherRect.getX() + otherRect.getWidth() <= rect.getX() + rect.getWidth())
                                            || (otherRect.getX() <= rect.getX() && rect.getX() <= otherRect.getX() + otherRect.getWidth())
                                            || (otherRect.getX() <= rect.getX() + rect.getWidth() && rect.getX() + rect.getWidth() <= otherRect.getX() + otherRect.getWidth()))) {
                                    increaseDown = Math.min(increaseDown, otherRect.getY() - nodeNodeSpacing);
                                    if (increaseDown - rect.getY() < rect.getHeight()) {
                                        System.out.println("ERROR");
                                    }
                                }
                            }
                            rect.setWidth(increaseRight - rect.getX());
                            rect.setHeight(increaseDown - rect.getY());
                            ElkUtil.translate(rect, new KVector(increaseRight - rect.getX(), increaseDown - rect.getY()), new KVector(rect.getWidth(), rect.getHeight()));
                        }

                        if (progressMonitor.isLoggingEnabled()) {
                            progressMonitor.logGraph(layoutGraph, "Output after whitespace elim");
                        }
                    }
                    
                    applyPadding(rectangles, padding);
                    ElkUtil.resizeNode(layoutGraph, drawing.getDrawingWidth() + padding.getHorizontal(),
                            drawing.getDrawingHeight() + padding.getVertical(), false, true);

                    if (progressMonitor.isLoggingEnabled()) {
                        progressMonitor.logGraph(layoutGraph, "After padding");
                    }
                    
                    if (logging) {
                        System.out.println("");
                        for (IloNumVar choosen : currentSubRowEnd) {
                            if (cp.isFixed(choosen)) {
                                System.out.print(cp.getValue(choosen));
                            } else {
                                System.out.print("W");
                            }
                        }

                        System.out.println("");
                        for (IloNumVar choosen : currentRowLevel) {
                            if (cp.isFixed(choosen)) {
                                System.out.print(cp.getValue(choosen));
                            } else {
                                System.out.print("W");
                            }
                        }
                        

                        System.out.println("");
                        for (IloNumVar choosen : currentStackEnd) {
                            if (cp.isFixed(choosen)) {
                                System.out.print(cp.getValue(choosen));
                            } else {
                                System.out.print("W");
                            }
                        }
                        

                        System.out.println("");
                        for (IloNumVar choosen : lastStackEnd) {
                            if (cp.isFixed(choosen)) {
                                System.out.print(cp.getValue(choosen));
                            } else {
                                System.out.print("W");
                            }
                        }
                    }
                } else {
                    System.out.println("No solution found");
                    drawing = new DrawingData(aspectRatio, 100, 100, DrawingDataDescriptor.WHOLE_DRAWING);
                }
                cp.end();
            } catch (IloException e) {
                System.err.println("Error " + e);
            }
        } else {
            double maxWidth = minSize.x;
            if (targetWidth < 0 || targetWidth < minSize.x) {
                // Initial width approximation.
                AreaApproximation firstIt = new AreaApproximation(aspectRatio, goal, lastPlaceShift);
                drawing = firstIt.approxBoundingBox(rectangles, nodeNodeSpacing, padding);
                if (progressMonitor.isLoggingEnabled()) {
                    progressMonitor.logGraph(layoutGraph, "After approximation");
                }
            } else {
                drawing = new DrawingData(aspectRatio, targetWidth, 0, DrawingDataDescriptor.WHOLE_DRAWING);
            }
            // Readd padding for next steps.
            minSize.x += padding.getHorizontal();
            minSize.y += padding.getVertical();
            // Placement according to approximated width.
            if (!onlyFirstIteration) {
                DrawingUtil.resetCoordinates(rectangles);
                RowFillingAndCompaction secondIt = new RowFillingAndCompaction(aspectRatio, expandNodes, expandToAspectRatio, compaction, nodeNodeSpacing);
                // Modify the initial approximation if necessary.
                maxWidth = Math.max(minSize.x, drawing.getDrawingWidth());

                minSize.x -= padding.getHorizontal();
                minSize.y -= padding.getVertical();
                // Run placement, compaction, and expansion (if enabled).
                drawing = secondIt.start(rectangles, maxWidth, minSize, progressMonitor, layoutGraph);
                minSize.x += padding.getHorizontal();
                minSize.y += padding.getVertical();
            }
    
            // Final touch.
            applyPadding(rectangles, padding);
            ElkUtil.resizeNode(layoutGraph, drawing.getDrawingWidth() + padding.getHorizontal(),
                    drawing.getDrawingHeight() + padding.getVertical(), false, true);
            if (progressMonitor.isLoggingEnabled()) {
                progressMonitor.logGraph(layoutGraph, "Output");
            }
        }

        // if requested, compute nodes's dimensions, place node labels, ports, port labels, etc.
        if (!layoutGraph.getProperty(RectPackingOptions.OMIT_NODE_MICRO_LAYOUT)) {
            NodeMicroLayout.forGraph(layoutGraph)
                           .execute();
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
