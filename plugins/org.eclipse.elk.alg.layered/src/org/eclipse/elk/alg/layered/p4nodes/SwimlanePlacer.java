/*******************************************************************************
 * Copyright (c) 2023 stu205834 and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0 
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p4nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.elk.alg.layered.JsonDebugUtil;
import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.intermediate.IntermediateProcessorStrategy;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.LayoutProcessorConfiguration;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.core.util.LoggedGraph;

/**
 * @author stu205834
 *
 */
public class SwimlanePlacer implements ILayoutPhase<LayeredPhases, LGraph> {

    double SPACEING_BETWEEN_LANES = 0.0;
    double SPACING_NODE_NODE = 0.0;
    double SPACING_EDGE_NODE = 0.0;
    double SPACING_EDGE_EDGE = 0.0;

    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final LayoutProcessorConfiguration<LayeredPhases, LGraph> HIERARCHY_PROCESSING_ADDITIONS =
            LayoutProcessorConfiguration.<LayeredPhases, LGraph> create().addBefore(LayeredPhases.P5_EDGE_ROUTING,
                    IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);
    
    /* (non-Javadoc)
     * @see org.eclipse.elk.core.alg.ILayoutPhase#getLayoutProcessorConfiguration(java.lang.Object)
     */
    @Override
    public LayoutProcessorConfiguration<LayeredPhases, LGraph> getLayoutProcessorConfiguration(final LGraph graph) {
        if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(GraphProperties.EXTERNAL_PORTS)) {
            return HIERARCHY_PROCESSING_ADDITIONS;
        } else {
            return null;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.elk.core.alg.ILayoutProcessor#process(java.lang.Object,
     * org.eclipse.elk.core.util.IElkProgressMonitor)
     */
    @Override
    public void process(LGraph graph, IElkProgressMonitor monitor) {
        monitor.begin("Swimlane node placement", 1);
        monitor.logGraph(graph, null, null);

        monitor.logGraph(JsonDebugUtil.createDebugGraph(graph), "swimlane graph [" + graph.id + "]",
                LoggedGraph.Type.JSON);

        SPACING_NODE_NODE = graph.getProperty(LayeredOptions.SPACING_NODE_NODE);
        SPACING_EDGE_EDGE = graph.getProperty(LayeredOptions.SPACING_EDGE_EDGE);
        SPACING_EDGE_NODE = graph.getProperty(LayeredOptions.SPACING_EDGE_NODE);
        SPACEING_BETWEEN_LANES = graph.getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE_SPACING);

        final List<Layer> layers = graph.getLayers();
        final Map<Integer, Map<Integer, List<LNode>>> lanes = new HashMap<Integer, Map<Integer, List<LNode>>>();

        int maxLaneIndex = 0;
        int maxLayerIndex = 0;

        for (Layer layer : layers) {
            final int layerIndex = layer.getIndex();
            maxLayerIndex = Math.max(maxLayerIndex, layerIndex);
            for (LNode node : layer.getNodes()) {
                if (node.getType() == NodeType.LONG_EDGE)
                    node.setProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE, getLaneIndexForLongEdgeNode(node));
                if (node.getType() == NodeType.LABEL) {
                    node.setProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE, getLaneIndexForEdgeLabelNode(node));
                }
                final int laneIndex = node.getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
                maxLaneIndex = Math.max(maxLaneIndex, laneIndex);
                if (lanes.get(laneIndex) != null) {
                    final Map<Integer, List<LNode>> lane = lanes.get(laneIndex);
                    if (lane.get(layerIndex) != null) {
                        lane.get(layerIndex).add(node);
                    } else {
                        final List<LNode> inLaneLayer = new ArrayList<LNode>();
                        inLaneLayer.add(node);
                        lane.put(layerIndex, inLaneLayer);
                    }
                } else {
                    final List<LNode> inLaneLayer = new ArrayList<LNode>();
                    inLaneLayer.add(node);
                    final Map<Integer, List<LNode>> lane = new HashMap<Integer, List<LNode>>();
                    lane.put(layerIndex, inLaneLayer);
                    lanes.put(laneIndex, lane);
                }
            }
        }

        double laneOffset = 0.0;
        for (int laneIndex = 0; laneIndex <= maxLaneIndex; laneIndex++) {
            final Map<Integer, List<LNode>> inLaneLayers = lanes.get(laneIndex);
            if (inLaneLayers != null) {
                double laneSize = 0.0;
                for (int layerIndex = 0; layerIndex <= maxLayerIndex; layerIndex++)
                    laneSize = Math.max(laneSize, getNodeHeight(inLaneLayers.get(layerIndex)));
                double maxLaneSizeAfterLongEdgePlacement = laneOffset + laneSize;
                for (int layerIndex = 0; layerIndex <= maxLayerIndex; layerIndex++) {
                    final List<LNode> nodes = inLaneLayers.get(layerIndex);
                    if (nodes != null) {
                        final double laneSizeAfterLongEdgePlacement = nodePlacement(nodes, laneOffset, laneSize);
                        maxLaneSizeAfterLongEdgePlacement =
                                Math.max(maxLaneSizeAfterLongEdgePlacement, laneSizeAfterLongEdgePlacement);
                    }
                }
                laneOffset = maxLaneSizeAfterLongEdgePlacement + SPACEING_BETWEEN_LANES;
            }
        }
        monitor.done();
    }

    private double nodePlacement(final List<LNode> nodes, final double offset, final double laneSize) {
        if (nodes == null || nodes.isEmpty())
            return 0.0;

        final double spaceNeeded = getNodeHeight(nodes);
        double nodeOffset = offset + (laneSize - spaceNeeded) * 0.5;
        nodeOffset = setNodePosition(nodes.get(0), nodeOffset);
        NodeType firstNodeType = nodes.get(0).getType();
        for (int i = 1; i < nodes.size(); i++) {
            NodeType secondNodeType = nodes.get(i).getType();
            nodeOffset = setNodePosition(nodes.get(i), nodeOffset + getSpacing(firstNodeType, secondNodeType));
            firstNodeType = secondNodeType;
        }
        return nodeOffset;
    }
    
    private double getSpacing(final NodeType firstType, final NodeType secondType) {
        if(firstType == NodeType.LONG_EDGE && secondType == NodeType.LONG_EDGE)
            return SPACING_EDGE_EDGE;
        else if(firstType != NodeType.LONG_EDGE && secondType != NodeType.LONG_EDGE)
            return SPACING_NODE_NODE;
        else
            return SPACING_EDGE_NODE;
    }

    private double setNodePosition(final LNode node, final double newPosition) {
        final double nodeHeigt = getNodeHeight(node);
        KVector position = node.getPosition();
        position.y = newPosition;
        return newPosition + nodeHeigt;
    }
    
    private int getLaneIndexForEdgeLabelNode(LNode edgeLabelNode) {
        return getActualSourceNode(edgeLabelNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
    }
    
    private int getLaneIndexForLongEdgeNode(LNode longEdgeNode) {
        int sourceLane = getActualSourceNode(longEdgeNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
        int targetLane = getActualTargetNode(longEdgeNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);

        if (sourceLane < targetLane)
            return targetLane;
        else
            return sourceLane;
    }

    private LNode getActualTargetNode(LNode node) {
        for (LEdge edge : node.getOutgoingEdges()) {
            final LNode target = edge.getTarget().getNode();
            if (target.getType() != NodeType.LONG_EDGE && target.getType() != NodeType.LABEL)
                return target;
            else
                return getActualTargetNode(target);
        }
        return null;
    }

    private LNode getActualSourceNode(LNode node) {
        for (LEdge edge : node.getIncomingEdges()) {
            final LNode source = edge.getSource().getNode();
            if (source.getType() != NodeType.LONG_EDGE && source.getType() != NodeType.LABEL)
                return source;
            else
                return getActualSourceNode(source);
        }
        return null;
    }

    private double getNodeHeight(LNode node) {
        if (node.getType() != NodeType.LONG_EDGE)
            return node.getMargin().top + node.getSize().y + node.getMargin().bottom + 1.0;
        else
            return node.getMargin().top + node.getSize().y + node.getMargin().bottom;
    }

    private double getNodeHeight(List<LNode> nodes) {
        if (nodes == null || nodes.isEmpty())
            return 0.0;
        double size = getNodeHeight(nodes.get(0));
        NodeType firstNodeType = nodes.get(0).getType();
        for (int i = 1; i < nodes.size(); i++) {
            NodeType secondNodeType = nodes.get(i).getType();
            size += getNodeHeight(nodes.get(i)) + getSpacing(firstNodeType, secondNodeType);
            firstNodeType = secondNodeType;
        }
        return size;
    }
}
