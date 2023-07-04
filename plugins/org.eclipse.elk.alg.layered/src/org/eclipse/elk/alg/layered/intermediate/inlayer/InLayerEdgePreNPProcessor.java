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

import java.util.Set;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.intermediate.loops.ordering.PortSideAssigner;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Remove all in-layer edges by disconnecting their ports.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Maybe Center Label Management Processor</dd>
 * </dl>
 */
public class InLayerEdgePreNPProcessor implements ILayoutProcessor<LGraph> {
    
//    private final PortSideAssigner portSideAssigner = new PortSideAssigner();

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Remove in-layer edges", 1);
        
//        boolean leftOrRight = graph.getProperty(LayeredOptions.DIRECTION).isHorizontal();
//        Set<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
//        
//        portSideAssigner.assignPortSides(inLayerEdges, leftOrRight);
//        
//        // Readd ports for node size calculation.
        
        
        
//        if (inLayerEdges != null) {
//            for (LEdge lEdge : inLayerEdges) {
//                lEdge.getSource().getOutgoingEdges().remove(lEdge);
//                lEdge.getTarget().getIncomingEdges().remove(lEdge);
//            }
//        }
        
        progressMonitor.done();
    }
}

