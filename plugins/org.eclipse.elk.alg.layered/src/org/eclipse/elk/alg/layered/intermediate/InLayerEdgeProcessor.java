/*******************************************************************************
 * Copyright (c) 2023 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate;

import java.util.List;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.util.IElkProgressMonitor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Readds all in-layer edges that where removed during graph import to their corresponding ports and adds constraints
 * such that the can be next to each other.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>a proper layered graph</dd>
 *     <dd>TODO</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Before model order processors</dd>
 * </dl>
 */
public class InLayerEdgeProcessor implements ILayoutProcessor<LGraph> {

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Begin to readd in-layer edges", 1);
        
        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        if (inLayerEdges != null) {
            // If we have in-layer constraints, constraints between nodes may be between non-dummies.
            graph.setProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS_BETWEEN_NON_DUMMIES, true);
            
            Direction layoutDirection = graph.getProperty(LayeredOptions.DIRECTION);
            double labelLabelSpacing = graph.getProperty(LayeredOptions.SPACING_LABEL_LABEL);
            double edgeLabelSpacing = graph.getProperty(LayeredOptions.SPACING_EDGE_LABEL);
            List<LNode> newLabelDummies = Lists.newArrayList();
            // Readd the in-layer edges while splitting for label dummies.
            for (LEdge lEdge : inLayerEdges) {
                lEdge.setSource(lEdge.getSource());
                lEdge.setTarget(lEdge.getTarget());
                
                // Check whether label dummies need to be handled.
                if (LabelDummyInserter.edgeNeedsToBeProcessed(lEdge)) {
                    LabelDummyInserter.processEdge(lEdge, graph, newLabelDummies, layoutDirection, labelLabelSpacing,
                            edgeLabelSpacing);
                    graph.getLayerlessNodes().addAll(newLabelDummies);
                    // Add constraint from source to label dummy and from dummy to target.
                    // The size of newLabelDummies should always be one.
                    assert newLabelDummies.size() == 1;
                } else {
                    // Add constraint for direct in-layer edge
                    List<LNode> unit =
                            lEdge.getSource().getNode().getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                    unit.add(lEdge.getTarget().getNode());
                }
            }
            if (!newLabelDummies.isEmpty()) {
                LNode labelDummy = newLabelDummies.get(0);
                LNode sourceNode = Iterables.getOnlyElement(labelDummy.getIncomingEdges()).getSource().getNode();
                LNode targetNode = Iterables.getOnlyElement(labelDummy.getOutgoingEdges()).getTarget().getNode();
                // Add constraint from source to dummy.
                List<LNode> unit = sourceNode.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                unit.add(labelDummy);
                // Add constraint from dummy to target.
                unit = labelDummy.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                unit.add(targetNode);
                // Add second edge from dummy to target to list of in-layer edges.
                inLayerEdges.add(Iterables.getOnlyElement(labelDummy.getOutgoingEdges()));
                // Add dummy to correct layer.
                labelDummy.setLayer(sourceNode.getLayer());
            }
        }
        
        progressMonitor.done();
    }
}

