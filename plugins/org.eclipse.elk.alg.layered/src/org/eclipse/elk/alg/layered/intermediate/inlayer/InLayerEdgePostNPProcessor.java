/*******************************************************************************
 * Copyright (c) 2023 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate.inlayer;

import java.util.List;

import org.eclipse.elk.alg.layered.DebugUtil;
import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.intermediate.LabelAndNodeSizeProcessor;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Adds all in-layer edges by connecting their ports
 * and setting source and targets of the in-layer edges.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>In-layer edges and ports are not in the graph.</dd>
 *     <dd>with positions for nodes and ports</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>In-layer edges and ports are again in the graph.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 * </dl>
 */
public class InLayerEdgePostNPProcessor implements ILayoutProcessor<LGraph> {

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("In-layer Edges Post Node Placement", 1);
        DebugUtil.logLGraphNodesAndPorts(progressMonitor, graph, 1, "Before");
        // Readd the edges.
        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        if (inLayerEdges != null) {
            for (LEdge lEdge : inLayerEdges) {
                lEdge.setSource(lEdge.getSource());
                lEdge.setTarget(lEdge.getTarget());
            }
        }
        
        // Readd the ports.
        List<LPort> inLayerPorts = graph.getProperty(InternalProperties.IN_LAYER_PORTS);
        if (inLayerPorts != null) {
            for (LPort lPort : inLayerPorts) {
                lPort.setNode(lPort.getNode());
                // Also readd the ports to the cache.
                lPort.getNode().cachePortSides();
            }
        }

        progressMonitor.done();
    }
}

