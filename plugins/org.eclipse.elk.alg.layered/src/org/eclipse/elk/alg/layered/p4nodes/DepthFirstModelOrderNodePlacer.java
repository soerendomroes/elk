/*******************************************************************************
 * Copyright (c) 2023 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p4nodes;

import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.intermediate.IntermediateProcessorStrategy;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.Spacings;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.LayoutProcessorConfiguration;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Node placement implementation that assumes a depth-first layering, which means if a node with model order i has a
 * node with model order i+1 in the next non-dummy layer, the two nodes should be aligned.
 * This creates a node placement such as this:
 * 1 -> 2 -> 3 -> 4 -> 5
 *   -> 6 -> 7 -> 8 ->
 *   -> 9 -> 10
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>The graph has a proper layering with optimized nodes ordering</dd>
 *     <dd>Ports are properly arranged</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>Each node is assigned a vertical coordinate such that no two nodes overlap</dd>
 *     <dd>The size of each layer is set according to the area occupied by its nodes</dd>
 * </dl>
 */
public final class DepthFirstModelOrderNodePlacer implements ILayoutPhase<LayeredPhases, LGraph> {

    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final LayoutProcessorConfiguration<LayeredPhases, LGraph> HIERARCHY_PROCESSING_ADDITIONS =
        LayoutProcessorConfiguration.<LayeredPhases, LGraph>create()
            .addBefore(LayeredPhases.P5_EDGE_ROUTING,
                    IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

    @Override
    public LayoutProcessorConfiguration<LayeredPhases, LGraph> getLayoutProcessorConfiguration(final LGraph graph) {
        if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(GraphProperties.EXTERNAL_PORTS)) {
            return HIERARCHY_PROCESSING_ADDITIONS;
        } else {
            return null;
        }
    }
    
    private LGraph graph;

    @Override
    public void process(final LGraph layeredGraph, final IElkProgressMonitor monitor) {
        monitor.begin("Depth-first model order node placement", 1);
        this.graph = layeredGraph;
        Spacings spacings = layeredGraph.getProperty(InternalProperties.SPACINGS);
        
        // Begin with the first node of the first layer.
        // Try to find a node with model order + 1 in the next layer of real nodes.
        // Continue until none can be found.
        // Next start node is the first real node in the first layer that was not already handled.
        Layer lastLayer = null;
        for (Layer layer : layeredGraph) {
            
        }
        
        LNode node = getNextNode();
        while (node != null) {
            
            node = getNextNode();
        }
        
        monitor.done();
    }
    
    private LNode getNextNode() {
        for (Layer layer : graph) {
            for (LNode node : layer) {
                if (node.getType() == NodeType.NORMAL && node.getPosition().y != 0) {
                    return node;
                }
            }
        }
        return null;
    }

}
