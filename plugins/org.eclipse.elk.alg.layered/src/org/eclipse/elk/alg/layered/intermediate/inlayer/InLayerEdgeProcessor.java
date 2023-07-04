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
import java.util.List;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.intermediate.loops.ordering.PortSideAssigner;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Readds all in-layer ports and assigns PORT_SIDEs to them and adds constraints
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
    
    private final PortSideAssigner portSideAssigner = new PortSideAssigner();
    
    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        progressMonitor.begin("Begin to readd in-layer edges", 1);
            
        boolean leftOrRight = graph.getProperty(LayeredOptions.DIRECTION).isHorizontal();
        List<LEdge> inLayerEdges = graph.getProperty(InternalProperties.IN_LAYER_EDGES);
        List<LPort> inLayerPorts = graph.getProperty(InternalProperties.IN_LAYER_PORTS);
        List<LEdge> dummies = new ArrayList<>();
        if (inLayerEdges != null) {
            // If we have in-layer constraints, constraints between nodes may be between non-dummies.
            graph.setProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS_BETWEEN_NON_DUMMIES, true);
            for (LEdge lEdge : inLayerEdges) {
                LNode dummy = createDummy(graph, lEdge, lEdge.getSource(), lEdge.getTarget(),
                        inLayerEdges, inLayerPorts, dummies);
                LNode sourceNode = lEdge.getSource().getNode();
                LNode targetNode = lEdge.getTarget().getNode();
                // Source should be next to dummy
                List<LNode> unit = sourceNode.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                unit.add(dummy);
                // Dummy should be next to target
                unit = dummy.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
                unit.add(targetNode);
                dummy.setLayer(sourceNode.getLayer());
            }
            inLayerEdges.addAll(dummies);
        }
        
        portSideAssigner.assignPortSides(inLayerEdges, leftOrRight);
        
        // Readd ports for node size calculation. Wrong, size calculation after CM
        for (LPort lPort : inLayerPorts) {
//            lPort.getNode().getPorts().add(lPort);
        }
        
        if (inLayerEdges != null) {
            // If we have in-layer constraints, constraints between nodes may be between non-dummies.
            graph.setProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS_BETWEEN_NON_DUMMIES, true);
//            
//            Direction layoutDirection = graph.getProperty(LayeredOptions.DIRECTION);
//            double labelLabelSpacing = graph.getProperty(LayeredOptions.SPACING_LABEL_LABEL);
//            double edgeLabelSpacing = graph.getProperty(LayeredOptions.SPACING_EDGE_LABEL);
//            List<LNode> newLabelDummies = Lists.newArrayList();
//            for (LEdge lEdge : inLayerEdges) {
////                
//                // Check whether label dummies need to be handled.
//                if (LabelDummyInserter.edgeNeedsToBeProcessed(lEdge)) {
//                    throw new UnsupportedConfigurationException("An in-layer edge cannot have a label.");
////                    // FIXME add support for labels
//                    LabelDummyInserter.processEdge(lEdge, graph, newLabelDummies, layoutDirection, labelLabelSpacing,
//////                            edgeLabelSpacing);
//////                    graph.getLayerlessNodes().addAll(newLabelDummies);
//////                    // Add constraint from source to label dummy and from dummy to target.
//////                    // The size of newLabelDummies should always be one.
//////                    assert newLabelDummies.size() == 1;
//                } else {
//                    // Add constraint for direct in-layer edge
//                    List<LNode> unit =
//                            lEdge.getSource().getNode().getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
//                    unit.add(lEdge.getTarget().getNode());
//                }
//            }
//            // FIXME add support for labels
////            if (!newLabelDummies.isEmpty()) {
////                LNode labelDummy = newLabelDummies.get(0);
////                LNode sourceNode = Iterables.getOnlyElement(labelDummy.getIncomingEdges()).getSource().getNode();
////                LNode targetNode = Iterables.getOnlyElement(labelDummy.getOutgoingEdges()).getTarget().getNode();
////                // Add constraint from source to dummy.
////                List<LNode> unit = sourceNode.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
////                unit.add(labelDummy);
////                // Add constraint from dummy to target.
////                unit = labelDummy.getProperty(InternalProperties.IN_LAYER_SUCCESSOR_CONSTRAINTS);
////                unit.add(targetNode);
////                // Add second edge from dummy to target to list of in-layer edges.
////                inLayerEdges.add(Iterables.getOnlyElement(labelDummy.getOutgoingEdges()));
////                // Add dummy to correct layer.
////                labelDummy.setLayer(sourceNode.getLayer());
////            }
        }
        
        progressMonitor.done();
    }
    
    private LNode createDummy(final LGraph layeredGraph, final LEdge edge, final LPort sourcePort,
            final LPort targetPort, List<LEdge> inLayerEdges, List<LPort> inLayerPorts, List<LEdge> dummies) {
        
        // Create a dummy node with an input port and an output port
        LNode dummyNode = new LNode(layeredGraph);
        dummyNode.setType(NodeType.IN_LAYER);
        
        dummyNode.setProperty(InternalProperties.ORIGIN, edge);
        dummyNode.setProperty(LayeredOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        dummyNode.setProperty(InternalProperties.LONG_EDGE_SOURCE, sourcePort);
        dummyNode.setProperty(InternalProperties.LONG_EDGE_TARGET, targetPort);
        dummyNode.setProperty(InternalProperties.ORIGINAL_PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        
        LPort dummyInput = new LPort();
        dummyInput.setSide(PortSide.WEST);
        dummyInput.setNode(dummyNode);
        inLayerPorts.add(dummyInput);
        
        LPort dummyOutput = new LPort();
        dummyOutput.setSide(PortSide.EAST);
        dummyOutput.setNode(dummyNode);
        inLayerPorts.add(dummyOutput);
        
        edge.setTarget(dummyInput);
        
        // Create a dummy edge
        LEdge dummyEdge = new LEdge();
        dummyEdge.copyProperties(edge);
        dummyEdge.setProperty(LayeredOptions.JUNCTION_POINTS, null);
        dummyEdge.setSource(dummyOutput);
        dummyEdge.setTarget(targetPort);
        dummies.add(dummyEdge);
        
        return dummyNode;
    }
    
}

