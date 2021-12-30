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
import org.eclipse.elk.alg.rectpacking.options.InternalProperties;
import org.eclipse.elk.alg.rectpacking.options.OptimizationGoal;
import org.eclipse.elk.alg.rectpacking.options.RectPackingOptions;
import org.eclipse.elk.alg.rectpacking.seconditeration.RowFillingAndCompaction;
import org.eclipse.elk.alg.rectpacking.util.DrawingData;
import org.eclipse.elk.alg.rectpacking.util.DrawingDataDescriptor;
import org.eclipse.elk.alg.rectpacking.util.DrawingUtil;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.core.util.BoxLayoutProvider;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.util.ElkGraphUtil;

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
//            progressMonitor.logGraph(layoutGraph, "Input");
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
        // A target width for the algorithm. If this is set the width approximation step is skipped.
        double targetWidth = layoutGraph.getProperty(RectPackingOptions.TARGET_WIDTH);
        boolean secondIteration = layoutGraph.getProperty(RectPackingOptions.SECOND_ITERATION);
        boolean tryBox = layoutGraph.getProperty(RectPackingOptions.TRYBOX);

        List<ElkNode> rectangles = layoutGraph.getChildren();
        DrawingUtil.resetCoordinates(rectangles);
        DrawingData drawing = null;
        
        // Check whether regions are stackable and do box layout instead of they are not and secondIteration is enabled
        // TODO
        boolean stackable = false;
        if (tryBox && rectangles.size() >= 3) {
            ElkNode region1;
            ElkNode region2 = rectangles.get(0);
            ElkNode region3 = rectangles.get(1);
            int counter = 0;
            while (counter + 2 < rectangles.size()) {
                region1 = region2;
                region2 = region3;
                region3 = rectangles.get(counter + 2);
                if (region1.getHeight() >= region2.getHeight() + region3.getHeight() + nodeNodeSpacing
                        || region3.getHeight() >= region1.getHeight() + region2.getHeight() + nodeNodeSpacing) {
                    stackable = true;
                    break;
                } else {
                    counter++;
                }
            }
        }

        // Get minimum size of parent.
        KVector minSize = ElkUtil.effectiveMinSizeConstraintFor(layoutGraph);

        double minWidth = minSize.x;
        double maxWidth = 0;
        layoutGraph.setProperty(RectPackingOptions.STACKABLE, stackable);
        if (!stackable) {
            System.out.println("Not stackable");
            new BoxLayoutProvider().layout(layoutGraph, new BasicProgressMonitor());
        } else {

            
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
            // Remove padding to get the space the algorithm can use.
            minSize.x -= padding.getHorizontal();
            minSize.y -= padding.getVertical();
            maxWidth = minSize.x;
            if (targetWidth < 0 || targetWidth < minSize.x) {
                // Initial width approximation.
                AreaApproximation firstIt = new AreaApproximation(aspectRatio, goal, lastPlaceShift);
                drawing = firstIt.approxBoundingBox(rectangles, nodeNodeSpacing, padding);
                if (progressMonitor.isLoggingEnabled()) {
//                    progressMonitor.logGraph(layoutGraph, "After approximation");
                }
            } else {
                drawing = new DrawingData(aspectRatio, targetWidth, 0, DrawingDataDescriptor.WHOLE_DRAWING);
            }
            // Readd padding for next steps.
            minWidth = minSize.x;
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
                layoutGraph.setProperty(InternalProperties.MIN_ROW_INCREASE, secondIt.potentialRowWidthIncreaseMin);
                layoutGraph.setProperty(InternalProperties.MAX_ROW_INCREASE, secondIt.potentialRowWidthIncreaseMax);
                layoutGraph.setProperty(InternalProperties.MIN_ROW_DECREASE, secondIt.potentialRowWidthDecreaseMin);
                layoutGraph.setProperty(InternalProperties.MAX_ROW_DECREASE, secondIt.potentialRowWidthDecreaseMax);
            }
            
            // Final touch.
            if (!layoutGraph.getProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER)) {
                applyPadding(rectangles, padding);
            }
            
            ElkUtil.resizeNode(layoutGraph, drawing.getDrawingWidth() + padding.getHorizontal(),
                    drawing.getDrawingHeight() + padding.getVertical(), false, true);

            // if requested, compute nodes's dimensions, place node labels, ports, port labels, etc.
            if (!layoutGraph.getProperty(RectPackingOptions.OMIT_NODE_MICRO_LAYOUT)) {
                NodeMicroLayout.forGraph(layoutGraph).execute();
            }
            if (progressMonitor.isLoggingEnabled()) {
//                progressMonitor.logGraph(layoutGraph, "Output");
            }
        }

        if (secondIteration) {
            boolean onlyRectpacking = false;
            if (drawing == null && maxWidth == 0) {
                // No not set new width value, just do rectpacking, since first was box
                onlyRectpacking = true;
            }
            // Try to layout again if the aspect ratio seems to be bad
            if (onlyRectpacking && (layoutGraph.getWidth() / layoutGraph.getHeight()) < aspectRatio && layoutGraph.getChildren().size() > 1) {
                // The drawing is too high, this means the approximated target width is too low
                double oldSM = 0;
                if (onlyRectpacking) {
                    oldSM = Math.min(aspectRatio / layoutGraph.getWidth(), 1d / (layoutGraph.getHeight()));
                } else {
                    oldSM = Math.min(aspectRatio / (drawing.getDrawingWidth() + padding.getHorizontal()), 1d / (drawing.getDrawingHeight() + padding.getVertical()));
                }                ElkNode clone = clone(layoutGraph);
                clone.setProperty(RectPackingOptions.SECOND_ITERATION, false);
                
                clone.setProperty(RectPackingOptions.TARGET_WIDTH, maxWidth + layoutGraph.getProperty(InternalProperties.MIN_ROW_INCREASE));
                layout(clone, progressMonitor);
                double newSM = Math.min(aspectRatio / clone.getWidth(), 1d / clone.getHeight());
                if (newSM > oldSM) {
                    layoutGraph.setProperty(RectPackingOptions.REVISED_WIDTH, 1);
                    layoutGraph.setProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER, true);
                    for (int i = 0; i < clone.getChildren().size(); i++) {
                        copyPosition(clone.getChildren().get(i), layoutGraph.getChildren().get(i));
                    }
                    if (drawing == null) {
                        drawing = new DrawingData(aspectRatio, Math.max(minWidth, clone.getWidth()) - padding.getHorizontal(), clone.getHeight() - padding.getVertical(), null);
                    } else {
                        drawing.setDrawingWidth(clone.getWidth() - padding.getHorizontal());
                        drawing.setDrawingHeight(clone.getHeight() - padding.getVertical());
                    }
                    
                }
                
            } else if (onlyRectpacking && (layoutGraph.getWidth() / layoutGraph.getHeight()) > aspectRatio && layoutGraph.getChildren().size() > 1) {
                // The drawing is too high, this means the approximated target width is too high
                double oldSM = 0;
                if (onlyRectpacking) {
                    oldSM = Math.min(aspectRatio / layoutGraph.getWidth(), 1d / (layoutGraph.getHeight()));
                } else {
                    oldSM = Math.min(aspectRatio / (drawing.getDrawingWidth() + padding.getHorizontal()), 1d / (drawing.getDrawingHeight() + padding.getVertical()));
                }
                ElkNode clone = clone(layoutGraph);
                clone.setProperty(RectPackingOptions.SECOND_ITERATION, false);
                
                if (!onlyRectpacking) {
                    clone.setProperty(RectPackingOptions.TARGET_WIDTH, Math.max(ElkUtil.effectiveMinSizeConstraintFor(layoutGraph).x,
                            maxWidth - layoutGraph.getProperty(InternalProperties.MIN_ROW_DECREASE)));
                }
                layout(clone, progressMonitor);
                double newSM = Math.min(aspectRatio / clone.getWidth(), 1d / clone.getHeight());
                if (newSM > oldSM) {
                    layoutGraph.setProperty(RectPackingOptions.REVISED_WIDTH, -1);
                    layoutGraph.setProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER, true);
                    for (int i = 0; i < clone.getChildren().size(); i++) {
                        copyPosition(clone.getChildren().get(i), layoutGraph.getChildren().get(i));
                    }
                    if (drawing == null) {
                        drawing = new DrawingData(aspectRatio, Math.max(minWidth, clone.getWidth()) - padding.getHorizontal(), clone.getHeight() - padding.getVertical(), null);
                    } else {
                        drawing.setDrawingWidth(clone.getWidth() - padding.getHorizontal());
                        drawing.setDrawingHeight(clone.getHeight() - padding.getVertical());
                    }
                }
            } else if (layoutGraph.getChildren().size() > 1
                    && layoutGraph.getProperty(InternalProperties.MIN_ROW_INCREASE) != Double.POSITIVE_INFINITY
                    && (drawing.getDrawingWidth() + padding.getHorizontal()) / (drawing.getDrawingHeight() + padding.getVertical()) < aspectRatio) {
                // The drawing is too high, this means the approximated target width is too low
                double oldSM = Math.min(aspectRatio / (drawing.getDrawingWidth() + padding.getHorizontal()), 1d / (drawing.getDrawingHeight() + padding.getVertical()));
                ElkNode clone = clone(layoutGraph);
                clone.setProperty(RectPackingOptions.SECOND_ITERATION, false);
                
                clone.setProperty(RectPackingOptions.TARGET_WIDTH, maxWidth + layoutGraph.getProperty(InternalProperties.MIN_ROW_INCREASE));
                layout(clone, progressMonitor);
                double newSM = Math.min(aspectRatio / clone.getWidth(), 1d / clone.getHeight());
                if (newSM > oldSM) {
                    layoutGraph.setProperty(RectPackingOptions.REVISED_WIDTH, 1);
                    layoutGraph.setProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER, true);
                    for (int i = 0; i < clone.getChildren().size(); i++) {
                        copyPosition(clone.getChildren().get(i), layoutGraph.getChildren().get(i));
                    }
                    if (drawing == null) {
                        drawing = new DrawingData(aspectRatio, Math.max(minWidth, clone.getWidth()) - padding.getHorizontal(), clone.getHeight() - padding.getVertical(), null);
                    } else {
                        drawing.setDrawingWidth(clone.getWidth() - padding.getHorizontal());
                        drawing.setDrawingHeight(clone.getHeight() - padding.getVertical());
                    }
                    
                }
                
            } else if (layoutGraph.getChildren().size() > 1
                    && layoutGraph.getProperty(InternalProperties.MIN_ROW_DECREASE) != Double.POSITIVE_INFINITY 
                    && (drawing.getDrawingWidth() + padding.getHorizontal()) / (drawing.getDrawingHeight() + padding.getVertical()) > aspectRatio) {
                // The drawing is too high, this means the approximated target width is too high
                double oldSM = 0;
                if (onlyRectpacking) {
                    oldSM = Math.min(aspectRatio / layoutGraph.getWidth(), 1d / (layoutGraph.getHeight()));
                } else {
                    oldSM = Math.min(aspectRatio / (drawing.getDrawingWidth() + padding.getHorizontal()), 1d / (drawing.getDrawingHeight() + padding.getVertical()));
                }
                ElkNode clone = clone(layoutGraph);
                clone.setProperty(RectPackingOptions.SECOND_ITERATION, false);
                
                if (!onlyRectpacking) {
                    clone.setProperty(RectPackingOptions.TARGET_WIDTH, Math.max(ElkUtil.effectiveMinSizeConstraintFor(layoutGraph).x,
                            maxWidth - layoutGraph.getProperty(InternalProperties.MIN_ROW_DECREASE)));
                }
                layout(clone, progressMonitor);
                double newSM = Math.min(aspectRatio / clone.getWidth(), 1d / clone.getHeight());
                if (newSM > oldSM) {
                    layoutGraph.setProperty(RectPackingOptions.REVISED_WIDTH, -1);
                    layoutGraph.setProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER, true);
                    for (int i = 0; i < clone.getChildren().size(); i++) {
                        copyPosition(clone.getChildren().get(i), layoutGraph.getChildren().get(i));
                    }
                    if (drawing == null) {
                        drawing = new DrawingData(aspectRatio, Math.max(minWidth, clone.getWidth()) - padding.getHorizontal(), clone.getHeight() - padding.getVertical(), null);
                    } else {
                        drawing.setDrawingWidth(clone.getWidth() - padding.getHorizontal());
                        drawing.setDrawingHeight(clone.getHeight() - padding.getVertical());
                    }
                }
            }
            
            if (layoutGraph.getProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER)) {
                // Final touch.
                if (!layoutGraph.getProperty(InternalProperties.SECOND_ITERATION_WAS_BETTER)) {
                    applyPadding(rectangles, padding);
                }
                
                ElkUtil.resizeNode(layoutGraph, drawing.getDrawingWidth() + padding.getHorizontal(),
                        drawing.getDrawingHeight() + padding.getVertical(), false, true);

                // if requested, compute nodes's dimensions, place node labels, ports, port labels, etc.
                if (!layoutGraph.getProperty(RectPackingOptions.OMIT_NODE_MICRO_LAYOUT)) {
                    NodeMicroLayout.forGraph(layoutGraph).execute();
                }
                if (progressMonitor.isLoggingEnabled()) {
//                    progressMonitor.logGraph(layoutGraph, "Output");
                }
            }
        }
        progressMonitor.done();
    }
    
    private ElkNode clone(ElkNode node) {
        ElkNode clone = ElkGraphUtil.createNode(null);
        for (IProperty property : node.getAllProperties().keySet()) {
            clone.setProperty(property, node.getProperty(property));
        }
        for (ElkNode child : node.getChildren()) {
            ElkNode newChild = ElkGraphUtil.createNode(clone);
            newChild.setDimensions(child.getWidth(), child.getHeight());
            newChild.setIdentifier(child.getIdentifier());
            newChild.setLocation(child.getX(), child.getY());
            clone.getChildren().add(newChild);
            for (IProperty property : child.getAllProperties().keySet()) {
                newChild.setProperty(property, child.getProperty(property));
            }
        }
        return clone;
    }
    
    private void copyPosition(ElkNode clone, ElkNode original) {
        original.setDimensions(clone.getWidth(), clone.getHeight());
        original.setLocation(clone.getX(), clone.getY());
        for (int i = 0; i < clone.getChildren().size(); i++) {
            copyPosition(clone.getChildren().get(i), original.getChildren().get(i));
        }
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
