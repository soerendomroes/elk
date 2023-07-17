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
import java.util.Set;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.intermediate.LabelAndNodeSizeProcessor;
import org.eclipse.elk.alg.layered.intermediate.loops.ordering.PortSideAssigner;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Since the node order is now fix, we can assign ports sides to the in-layer edges (if they are free).
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>TODO</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Has be be before {@link LabelAndNodeSizeProcessor}
 * </dl>
 */
public class InLayerEdgePostCMProcessor implements ILayoutProcessor<LGraph> {
    
    private final PortSideAssigner portSideAssigner = new PortSideAssigner();

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("In-Layer Edge Post Crossing Minimization", 1);

        List<LPort> inLayerPorts = graph.getProperty(InternalProperties.IN_LAYER_PORTS);
        if (inLayerPorts != null) {
            for (LPort lPort : inLayerPorts) {
                lPort.setNode(lPort.getNode());
            }
        }

        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        boolean leftOrRight = graph.getProperty(LayeredOptions.DIRECTION).isHorizontal();
        // Use special method to assign in-layer edge ports to a side.
        portSideAssigner.assignPortSides(inLayerEdges, leftOrRight);
        
        progressMonitor.done();
    }
}

