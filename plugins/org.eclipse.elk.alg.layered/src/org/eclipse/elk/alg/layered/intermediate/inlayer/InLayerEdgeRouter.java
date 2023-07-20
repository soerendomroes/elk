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

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.math.KVectorChain;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Routes the in-layer edges.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>a proper layered graph</dd>
 *     <dd>TODO</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>All in-layer edges are joined and routes.</dd>
 *   <dt>Slots:</dt>
 *     <dd>After phase 5.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Before long edge joiner</dd>
 * </dl>
 */
public class InLayerEdgeRouter implements ILayoutProcessor<LGraph> {

    @Override
    public void process(LGraph graph, IElkProgressMonitor progressMonitor) {
        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        if (inLayerEdges != null) {
            for (LEdge lEdge : inLayerEdges) {
                lEdge.setSource(lEdge.getSource());
                lEdge.setTarget(lEdge.getTarget());
                lEdge.getBendPoints().clear();
            }
        }
        for (LEdge lEdge : inLayerEdges) {
            // Route each edge part
            // First same y coordinate as dummy, then same x coordinate.
            LPort normal;
            LPort dummyPort;
            boolean normalToDummy = lEdge.getSource().getNode().getType() == NodeType.IN_LAYER
                    || lEdge.getSource().getNode().getType() == NodeType.IN_LAYER_LABEL;
            if (normalToDummy) {
                dummyPort = lEdge.getSource();
                normal = lEdge.getTarget();
            } else {
                normal = lEdge.getSource();
                dummyPort = lEdge.getTarget();
            }
            LNode normalNode = normal.getNode();
            LNode dummyNode = dummyPort.getNode();
            int normalNodeIndex = normalNode.getIndex();
            int dummyNodeIndex = dummyNode.getIndex();
            boolean forward = normalNodeIndex < dummyNodeIndex;
            double edgeNodeSpacing = graph.getProperty(LayeredOptions.SPACING_EDGE_NODE);
            
            if (forward && normal.getSide() == PortSide.SOUTH || !forward && normal.getSide() == PortSide.NORTH) {
                // Case both ports are directly next to each other
                lEdge.getBendPoints().add(normal.getAbsoluteAnchor().x, dummyPort.getAbsoluteAnchor().y);
            } else if (normal.getSide() == PortSide.EAST) {
                // Case we need to route from WEST to the dummy node.
                lEdge.getBendPoints().add(normal.getAbsoluteAnchor().x, dummyPort.getAbsoluteAnchor().y);
            } else if (normal.getSide() == PortSide.WEST) {
                // Case we need to route from EAST to the dummy node.
                lEdge.getBendPoints().add(normal.getAbsoluteAnchor().x, dummyPort.getAbsoluteAnchor().y);
                
            } else if (forward && normal.getSide() == PortSide.NORTH
                    || !forward && normal.getSide() == PortSide.SOUTH) {
                // Case we need to route from the opposite side to the dummy node
                KVectorChain bendpoints = lEdge.getBendPoints();
                bendpoints.add(normal.getAbsoluteAnchor().x,
                        normalNode.getPosition().y + normalNode.getSize().y + edgeNodeSpacing);
                bendpoints.add(normalNode.getPosition().x - edgeNodeSpacing,
                        normalNode.getPosition().y + normalNode.getSize().y + edgeNodeSpacing);
                bendpoints.add(normalNode.getPosition().x - edgeNodeSpacing,
                        dummyPort.getAbsoluteAnchor().y);
            }
            
            // Case we need to route from WEST to the dummy node.
            
            // Case we need to route from EAST to the dummy node.
            
            // Case we need to route from the opposite side to the dummy node
        }

        List<LNode> inLayerDummies = graph.getProperty(InternalProperties.IN_LAYER_DUMMIES);
        if (inLayerDummies != null) {
//          for (LNode lNode : inLayerDummies) {
//              lNode.set
//          }
        }
        
    }

}
