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

    double BETWEEN_LANE_EDGE_SPACEING = 0.0;
    double MIN_NODE_SPACING = 0.0;
    double EDGE_NODE_SPACING = 0.0;

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

        MIN_NODE_SPACING = graph.getProperty(LayeredOptions.SPACING_NODE_NODE);
        BETWEEN_LANE_EDGE_SPACEING = graph.getProperty(LayeredOptions.SPACING_EDGE_EDGE);
        EDGE_NODE_SPACING = graph.getProperty(LayeredOptions.SPACING_EDGE_NODE);

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
                laneOffset = maxLaneSizeAfterLongEdgePlacement + MIN_NODE_SPACING;
            }
        }
        monitor.done();
    }

    private double nodePlacement(final List<LNode> nodes, final double offset, final double laneSize) {
        if (nodes == null || nodes.isEmpty())
            return 0.0;

        List<LNode> nonLongEdgeNodes = new ArrayList<LNode>();
        List<LNode> longEdgeNodes = new ArrayList<LNode>();

        for (LNode node : nodes) {
            if (node.getType() == NodeType.LONG_EDGE)
                longEdgeNodes.add(node);
            else
                nonLongEdgeNodes.add(node);
        }

        final double spaceNeeded = getNodeHeight(nonLongEdgeNodes);
        double nodeOffset = offset + (laneSize - spaceNeeded) * 0.5;
        if (!nonLongEdgeNodes.isEmpty())
            nodeOffset = setNodePosition(nonLongEdgeNodes.get(0), nodeOffset);
        for (int i = 1; i < nonLongEdgeNodes.size(); i++)
            nodeOffset = setNodePosition(nonLongEdgeNodes.get(i), nodeOffset + MIN_NODE_SPACING);

        if (!nonLongEdgeNodes.isEmpty())
            nodeOffset += EDGE_NODE_SPACING;
        if (!longEdgeNodes.isEmpty())
            nodeOffset = setNodePosition(longEdgeNodes.get(0), nodeOffset);
        for (int i = 1; i < longEdgeNodes.size(); i++)
            nodeOffset = setNodePosition(longEdgeNodes.get(i), nodeOffset + BETWEEN_LANE_EDGE_SPACEING);
        return nodeOffset;
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
        return node.getMargin().top + node.getSize().y + node.getMargin().bottom;
    }

    private double getNodeHeight(List<LNode> nodes) {
        if (nodes == null || nodes.isEmpty())
            return 0.0;
        double size = getNodeHeight(nodes.get(0));
        for (int i = 1; i < nodes.size(); i++)
            size += getNodeHeight(nodes.get(i)) + MIN_NODE_SPACING;
        return size;
    }
}
