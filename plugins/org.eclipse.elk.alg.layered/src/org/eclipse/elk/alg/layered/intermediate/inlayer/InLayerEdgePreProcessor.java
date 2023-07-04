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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Remove all in-layer edges by disconnecting their ports. If their ports have no further connections also remove them.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 1 or before phase 4?.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>NONE</dd>
 * </dl>
 */
public class InLayerEdgePreProcessor implements ILayoutProcessor<LGraph> {
    
    public final static int NOT_ADDED = 0;
    public final static int ADDED = 1;
    

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Remove in-layer edges", 1);
        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        if (inLayerEdges == null) {
            inLayerEdges = new ArrayList<>();
            graph.setProperty(InternalProperties.IN_LAYER_EDGES, inLayerEdges);
        }
        List<LPort> inLayerPorts = graph.getProperty(InternalProperties.IN_LAYER_PORTS);
        if (inLayerPorts == null) {
            inLayerPorts = new ArrayList<>();
            graph.setProperty(InternalProperties.IN_LAYER_PORTS, inLayerPorts);
        }
        for (LNode node : graph.getLayerlessNodes()) {
            for (LPort port : node.getPorts()) {
                for (LEdge edge : port.getConnectedEdges()) {
                    edge.id = NOT_ADDED;
                }
            }
        }
        // All nodes are layerless before phase 1
        for (LNode node : graph.getLayerlessNodes()) {
            for (LPort port : node.getPorts()) {
                boolean normallyConnected = false;
                for (LEdge edge : port.getConnectedEdges()) {
                    // Find edges to remove
                    if (edge.getProperty(LayeredOptions.IN_LAYER_EDGE)) {
                        if (edge.id == NOT_ADDED) {
                            inLayerEdges.add(edge);
                            edge.id = ADDED;
                        }
                    } else {
                        normallyConnected = true;
                    }
                }
                if (!normallyConnected) {
                    // Also remove the ports
                    inLayerPorts.add(port);
                    
                }
            }
        }
        // There should be a better way to remove all of them, this seems slow.
        for (LEdge lEdge : inLayerEdges) {
            lEdge.getSource().getOutgoingEdges().remove(lEdge);
            lEdge.getTarget().getIncomingEdges().remove(lEdge);
        }
        for (LPort lPort : inLayerPorts) {
            lPort.getNode().getPorts().remove(lPort);
        }
        
        progressMonitor.done();
    }
}

