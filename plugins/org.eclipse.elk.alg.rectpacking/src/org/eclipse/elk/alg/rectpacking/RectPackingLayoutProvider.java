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
        // The spacing between two nodes.
        double nodeNodeSpacing = layoutGraph.getProperty(RectPackingOptions.SPACING_NODE_NODE);
        int intSpacing = (int) nodeNodeSpacing;
        // Whether the nodes are compacted after the initial placement.
        boolean compaction = layoutGraph.getProperty(RectPackingOptions.ROW_COMPACTION);
        // Whether the nodes should be expanded to fit the aspect ratio during node expansion.
        // Only effective if nodes are expanded.
        boolean expandToAspectRatio = layoutGraph.getProperty(RectPackingOptions.EXPAND_TO_ASPECT_RATIO);
        // Whether interactive layout is activ.
        boolean interactive = layoutGraph.getProperty(RectPackingOptions.INTERACTIVE);

        boolean cplexEnalbed = layoutGraph.getProperty(RectPackingOptions.CPLEX);
//        cplexEnalbed = false;
        boolean cplexEnalbed2 = layoutGraph.getProperty(RectPackingOptions.FUNCPLEX2);
        double cplexOptTolerance = -1;
        if (layoutGraph.hasProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE)) {
            cplexOptTolerance = layoutGraph.getProperty(RectPackingOptions.CPLEX_OPT_TOLERANCE);
        }
        // A target width for the algorithm. If this is set the width approximation step is skipped.
        double targetWidth = -1;
        if (layoutGraph.hasProperty(RectPackingOptions.TARGET_WIDTH)) {
            targetWidth = layoutGraph.getProperty(RectPackingOptions.TARGET_WIDTH);
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
                cp.setParameter(IloCP.DoubleParam.TimeLimit, 60 * 10);
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

                IloNumExpr maxWidth = cp.max(widths);
                IloNumExpr maxHeight = cp.max(heights);
                IloNumExpr scaleMeasure = cp.min(cp.quot(aspectRatio, maxWidth), cp.quot(1, maxHeight));
                // Goal is to maximize the scale measure and minimize the area at the same time.
                IloNumExpr cpGoal = scaleMeasure; // , cp.quot(1, cp.prod(maxWidth, maxHeight)));
                cp.add(cp.maximize(cpGoal, "Scale measure goal"));
                // cp.addMinimize(cp.prod(maxWidth, maxHeight));
                // Define constraints
                IloNumExpr[] currentMaxHeight = new IloNumExpr[numberOfRects];
//                IloNumVar[] lastStackWidth = new IloNumVar[numberOfRects];
//                IloNumVar[] currentRowLevel = new IloNumVar[numberOfRects];
//                IloNumVar[] currentStackWidth = new IloNumVar[numberOfRects];
//                IloNumVar[] currentSubRowEnd = new IloNumVar[numberOfRects];
                
                IloIntVar[] lastStackWidth = new IloIntVar[numberOfRects];
                IloIntVar[] currentRowLevel = new IloIntVar[numberOfRects];
                IloIntVar[] currentStackWidth = new IloIntVar[numberOfRects];
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
                        
                        currentStackWidth[i] = cp.intVar(0, (int) totalWidth);
                        cp.addGe(cp.sum(currentStackWidth[i - 1],  intSpacing + rectWidth[i]), currentStackWidth[i]);
                        lastStackWidth[i] = cp.intVar(0, (int) totalWidth);
                        cp.addGe(currentStackWidth[i], lastStackWidth[i]);
                        currentRowLevel[i] = cp.intVar(0, (int) totalHeight);
                        cp.addLe(currentRowLevel[i - 1], currentRowLevel[i]);
                        cp.addGe(cp.sum(currentMaxHeight[i - 1], intSpacing + rectHeight[i]), currentRowLevel[i]);
                        currentSubRowEnd[i] = cp.intVar(0, (int) totalHeight);
                        cp.addLe(cp.sum(currentRowLevel[i],  intSpacing + rectHeight[i]), currentSubRowEnd[i]);
                        cp.addGe(currentMaxHeight[i], currentSubRowEnd[i]);
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
                                cp.and(cp.eq(cp.constant(intSpacing + rectWidth[i]), currentStackWidth[i]), // Bind currentStackWidth
                                cp.and(cp.eq(cp.constant(0), lastStackWidth[i]), // Bind lastStackWidth
                                cp.and(cp.eq(currentMaxHeight[i - 1], currentRowLevel[i]), // Bind row level
                                cp.and(cp.eq(cp.sum(intSpacing + rectHeight[i], currentRowLevel[i]), currentSubRowEnd[i]), // Bind subrow end height
                                cp.and(cp.ge(cp.startOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.lt(cp.endOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(1)),
                                        cp.eq(currentMaxHeight[i - 1], cp.startOf(rectY))))))))); // // Bind y-coordinate

                        // Case left of current one in same subrow
                        constraint[0] =
                                cp.and(cp.eq(cp.sum(intSpacing, cp.endOf(rectXs[i-1])), cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(lastStackWidth[i-1], lastStackWidth[i]), // Bind lastStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
                                cp.and(cp.eq(currentStackWidth[i], cp.max(currentStackWidth[i - 1], cp.sum(intSpacing, cp.endOf(rectX)))), // Bind currentStackWidth
//                                cp.and(cp.eq(currentSubRowLevel[i], currentSubRowLevel[i - 1]), // Bind subrowlevel
                                cp.and(cp.eq(currentSubRowEnd[i], cp.max(currentSubRowEnd[i-1], cp.sum(intSpacing, cp.endOf(rectY)))), // Bind subrow end height
                                cp.and(cp.lt(cp.endOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.eq(cp.startOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(2)),
                                        cp.eq(cp.startOf(rectY), cp.startOf(rectYs[i - 1]))))))))); // Bind y
                        
                        // Case right of last one in new stack
                        constraint[1] =
                                cp.and(cp.eq(currentStackWidth[i - 1], cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(currentStackWidth[i], cp.sum(intSpacing, cp.endOf(rectX))), // Bind currentStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
//                                cp.and(cp.eq(currentRowLevel[i-1], currentSubRowLevel[i]), // Bind subrowlevel
                                cp.and(cp.eq(lastStackWidth[i], currentStackWidth[i-1]), // Bind lastStackWidth
                                cp.and(cp.eq(currentSubRowEnd[i], cp.sum(intSpacing + rectHeight[i], currentRowLevel[i])), // Bind subrow end height
                                cp.and(cp.lt(cp.endOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.ge(cp.startOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(3)),
                                        cp.eq(currentRowLevel[i], cp.startOf(rectY))))))))); // Bind y
                        
                        // Case in new subrow
                        constraint[2] = 
                                cp.and(cp.eq(lastStackWidth[i], cp.startOf(rectX)), // Bind x
                                cp.and(cp.eq(currentStackWidth[i], cp.max(currentStackWidth[i-1], cp.sum(cp.endOf(rectX), intSpacing))), // Bind currentStackWidth
                                cp.and(cp.eq(currentRowLevel[i], currentRowLevel[i-1]), // Bind row level
//                                cp.and(cp.eq(currentSubRowEnd[i-1], currentSubRowLevel[i]), // Bind subrowlevel
                                cp.and(cp.eq(lastStackWidth[i], lastStackWidth[i-1]), // Bind lastStackWidth
                                cp.and(cp.eq(currentSubRowEnd[i], cp.sum(intSpacing + rectHeight[i], currentSubRowEnd[i - 1])), // Bind subrow end height
                                cp.and(cp.ge(cp.startOf(rectXs[i - 1]), cp.startOf(rectX)),
                                cp.and(cp.lt(cp.endOf(rectYs[i - 1]), cp.startOf(rectY)),
//                                cp.and(cp.eq(choosenPosition[i], cp.constant(4)),
                                        cp.eq(currentSubRowEnd[i - 1], cp.startOf(rectY))))))))); //Bind y
                        

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

                        lastStackWidth[i] = cp.intVar(0, 0);
                        currentRowLevel[i] = cp.intVar(0, 0);
                        currentStackWidth[i] = cp.intVar(intSpacing + rectWidth[i], intSpacing + rectWidth[i]);
                        currentSubRowEnd[i] = cp.intVar(intSpacing + rectHeight[i], intSpacing + rectHeight[i]);
//                        currentSubRowLevel[i] = cp.constant(0);
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
                        for (IloNumVar choosen : currentStackWidth) {
                            if (cp.isFixed(choosen)) {
                                System.out.print(cp.getValue(choosen));
                            } else {
                                System.out.print("W");
                            }
                        }
                        

                        System.out.println("");
                        for (IloNumVar choosen : lastStackWidth) {
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
            // // Initial width approximation.
            // AreaApproximation firstIt = new AreaApproximation(aspectRatio, goal, lastPlaceShift);
            // drawing = firstIt.approxBoundingBox(rectangles, nodeNodeSpacing);
            // if (progressMonitor.isLoggingEnabled()) {
            // progressMonitor.logGraph(layoutGraph, "After approximation");
            // }
            // // Placement according to approximated width.
            // if (!onlyFirstIteration) {
            // DrawingUtil.resetCoordinates(rectangles);
            // if (cplexEnalbed2) {
            // IloCplex cp;
            // try {
            // cp = new IloCplex();
            // cp.setParam(IloCplex.DoubleParam.TimeLimit, 60 * 60);
            // boolean logging = true;
            // if (!logging) {
            // cp.setOut(null);
            // cp.setWarning(null);
            // cp.setOut(null);
            // }
            // // Create variables
            // IloNumVar[] x = new IloNumVar[rectangles.size()];
            // IloNumVar[] y = new IloNumVar[rectangles.size()];
            // IloNumVar[] width = new IloNumVar[rectangles.size()];
            // IloNumVar[] height = new IloNumVar[rectangles.size()];
            // int j = 0;
            // // Define width and height
            // for (ElkNode rect : rectangles) {
            // width[j] = cp.numVar(rect.getWidth(), rect.getWidth());
            // height[j] = cp.numVar(rect.getHeight(),rect.getHeight());
            // j++;
            // }
            // // Define endheight of an element
            // IloNumExpr[] endHeight = new IloNumExpr[rectangles.size()];
            // IloNumExpr[] endWidth = new IloNumExpr[rectangles.size()];
            // IloNumExpr H = cp.numExpr();
            //
            // minSize.x = Math.max(minSize.x, drawing.getDrawingWidth());
            // for (int i = 0; i < rectangles.size(); i++) {
            // x[i] = cp.numVar(0, 10000);
            // y[i] = cp.numVar(0, 10000);
            // endWidth[i] = cp.sum(x[i], width[i]);
            // endHeight[i] = cp.sum(y[i], height[i]);
            // cp.addLe(endWidth[i], minSize.x);
            // }
            // H = cp.max(endHeight);
            // for (int i = 0; i < rectangles.size(); i++) {
            // // Define ordering.
            // if (i != 0) {
            // if (logging) {
            // System.out.println("Adding constraint for node" + i);
            // }
            // for (int k = 0; k < i; k++) {
            // cp.add(cp.or(
            // cp.ge(x[i], cp.sum((int) nodeNodeSpacing, endWidth[k])),
            // cp.ge(y[i], cp.sum((int) nodeNodeSpacing, endHeight[k]))));
            // }
            // } else {
            // if (logging) {
            // System.out.println("First node");
            // }
            // cp.addEq(0, x[0]);
            // cp.addEq(0, y[0]);
            // }
            // }
            //
            // cp.addMinimize(H);
            // if (cp.solve()) {
            // int index = 0;
            // // Apply coordinates to rectangles.
            // for (ElkNode rect : rectangles) {
            // if (logging) {
            // System.out.println("X/Y (" + rect.getIdentifier() + ", " + cp.getValue(x[index]) + "," +
            // cp.getValue(y[index]) + ")");
            // }
            // rect.setX(cp.getValue(x[index]));
            // rect.setY(cp.getValue(y[index]));
            // index++;
            // }
            // // Calculate drawing dimensions.
            // drawing = new DrawingData(aspectRatio, minSize.x, cp.getValue(H),
            // DrawingDataDescriptor.WHOLE_DRAWING);
            // if (logging) {
            // System.out.println(minSize.x);
            // System.out.println(cp.getValue(H));
            // }
            // } else {
            // System.out.println("No solution found");
            // drawing = new DrawingData(aspectRatio, 100, 100, DrawingDataDescriptor.WHOLE_DRAWING);
            // }
            //
            // } catch (IloException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // } else {
            // RowFillingAndCompaction secondIt = new RowFillingAndCompaction(aspectRatio, expandNodes,
            // expandToAspectRatio, compaction, nodeNodeSpacing);
            // // Modify the initial approximation if necessary.
            // minSize.x = Math.max(minSize.x, drawing.getDrawingWidth());
            //
            // drawing = secondIt.start(rectangles, minSize);
            // }
            // }
            // if (progressMonitor.isLoggingEnabled()) {
            // progressMonitor.logGraph(layoutGraph, "After compaction");
            // }

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
                RowFillingAndCompaction secondIt = new RowFillingAndCompaction(aspectRatio, expandNodes,
                        expandToAspectRatio, compaction, nodeNodeSpacing);
                // Modify the initial approximation if necessary.
                maxWidth = Math.max(minSize.x, drawing.getDrawingWidth());

                drawing = secondIt.start(rectangles, maxWidth, minSize, progressMonitor, layoutGraph);
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
