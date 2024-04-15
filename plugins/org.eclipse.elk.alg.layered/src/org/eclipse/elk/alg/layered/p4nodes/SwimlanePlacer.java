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

import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.intermediate.IntermediateProcessorStrategy;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.Spacings;
import org.eclipse.elk.alg.layered.utils.SwimlaneIndexUtil;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.LayoutProcessorConfiguration;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.IElkProgressMonitor;

public class SwimlanePlacer implements ILayoutPhase<LayeredPhases, LGraph> {
    
    /** a map of lanes containing a map to the layers **/
    private Map<Integer, Map<Integer, List<LNode>>> lanes;
    /** highest layer index in the graph **/
    private int maxLayerIndex = 0;
    /** highest lane index in the graph **/
    private int maxLaneIndex = 0;
    
    /** gets spacing defined in the graph **/
    private Spacings spacingUtility;
    /** spacing between lanes **/
    private double spacingLaneLane = 0.0;

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
        
        spacingUtility = new Spacings(graph);
        spacingLaneLane = graph.getProperty(
                LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE_SPACING);

        buildLanes(graph.getLayers());
        
        nodePlacement();
        
        monitor.done();
    }
    
    /**
     * Splits the given layers by lane index and stores the highest values 
     * of layer and lane index for easier access while placing the nodes.
     * 
     * @param layers
     *      list of {@link Layers} ordered by hierarchy, containing nodes.
     */
    private void buildLanes(final List<Layer> layers) {
        // Initialize instance variables needed for node placement
        lanes = new HashMap<Integer, Map<Integer, List<LNode>>>();
        maxLayerIndex = 0;
        maxLaneIndex = 0;

        for (Layer layer : layers) {
            final int layerIndex = layer.getIndex();
            maxLayerIndex = Math.max(maxLayerIndex, layerIndex);
            
            for (LNode node : layer.getNodes()) {
                // implicitly created nodes have no lane index
                final int laneIndex = SwimlaneIndexUtil.setAndGetLaneIndex(node);
                maxLaneIndex = Math.max(maxLaneIndex, laneIndex);
                
                // putting the node into the lanes mapping
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
    }
    
    /** 
     * Iterates through the lane mapping and places each node lane by lane and layer by layer.
     */
    private void nodePlacement() {
        // smallest possible position for a vertex in a lane
        double laneOffset = 0.0;
        
        for (int laneIndex = 0; laneIndex <= maxLaneIndex; laneIndex++) {
            // get all layers within a lane
            final Map<Integer, List<LNode>> inLaneLayers = lanes.get(laneIndex);
            if (inLaneLayers != null) {
                // calculate the layer size within the lane and the maximum 
                // of it for center alignment within the lane
                final double[] laneLayerSize = new double[maxLayerIndex + 1];
                double laneSize = 0.0;
                for (int layerIndex = 0; layerIndex <= maxLayerIndex; layerIndex++) {
                    laneLayerSize[layerIndex] = getHeightForNodes(inLaneLayers.get(layerIndex));
                    laneSize = Math.max(laneSize, laneLayerSize[layerIndex]);
                }
            
                // place each layer centered within the lane
                for (int layerIndex = 0; layerIndex <= maxLayerIndex; layerIndex++) {
                    final List<LNode> nodes = inLaneLayers.get(layerIndex);
                    if (nodes != null) {
                        laneLayerPlacement(nodes, laneOffset, laneSize, laneLayerSize[layerIndex]);
                    }
                }
                
                // set the offset for the next lane 
                laneOffset += laneSize + spacingLaneLane;
            }
        }
    }
    
    /**
     * Positions the given nodes centered within the lane, starting at the 
     * specified offset.
     * 
     * @param nodes
     *          list of nodes getting placed
     * @param offset
     *          smallest possible global y-position
     * @param laneSize
     *          maximum space available for node placement
     * @param laneLayerSize
     *          actual space needed for the given nodes        
     */
    private void laneLayerPlacement(final List<LNode> nodes, final double laneOffset, 
            final double laneSize, final double laneLayerSize) {
        if (nodes == null || nodes.isEmpty())
            return;
        
        // actual position of the node to place next
        double nodeOffset = laneOffset + (laneSize - laneLayerSize) * 0.5;
        NodeType firstNodeType = nodes.get(0).getType();
        
        // align single longe edge node with source node for a straight edge
        if (nodes.size() == 1 && firstNodeType == NodeType.LONG_EDGE) {
            singleLongeEdgePlacement(nodes.get(0), nodeOffset);
            return;
        }
        
        nodeOffset = setNodePosition(nodes.get(0), nodeOffset);
        for (int i = 1; i < nodes.size(); i++) {
            // determine spacing between nodes
            NodeType secondNodeType = nodes.get(i).getType();
            nodeOffset += spacingUtility.getVerticalSpacing(firstNodeType, secondNodeType);
            
            nodeOffset = setNodePosition(nodes.get(i), nodeOffset);
            firstNodeType = secondNodeType;
        }
       
        return;
    }
    
    /**
     * When the source node resides on the same lane and possesses multiple 
     * outgoing ports, the single long edge node placed on the lane layer 
     * becomes misaligned, positioned in the center instead of aligning with 
     * the source node's outgoing port. This method addresses this issue 
     * by ensuring the long edge node aligns correctly with the source node's 
     * outgoing port.
     * 
     * @param longEdgeNode
     * @param offset
     */
    private void singleLongeEdgePlacement(final LNode longEdgeNode, final double offset) {
        final KVector position = longEdgeNode.getPosition();
        final int laneIndex = SwimlaneIndexUtil.getLane(longEdgeNode);
        
        for (LEdge incommingEdge : longEdgeNode.getIncomingEdges()) {
            final LNode sourceNode = incommingEdge.getSource().getNode();
            final int sourceLaneIndex = SwimlaneIndexUtil.getLane(sourceNode); 
            if (sourceLaneIndex == laneIndex) {
                NodeType sourceType = sourceNode.getType();
                
                if (sourceType == NodeType.NORMAL) {
                    // align with the origin port
                    LPort sourcePort = incommingEdge.getSource();
                    position.y = sourceNode.getPosition().y + sourcePort.getPosition().y;
                } else {
                    // align with the previous node
                    position.y = sourceNode.getPosition().y;
                }
                return;
            }
        }
        
        // source node is not on the same lane => center in lane
        position.y = offset;
        return;
    }

    /**
     * Sets the y-coordinate of the node at the given position and returns the
     * next empty y-coordinate.
     * 
     * @param node
     * @param newPosition
     * @return next free space
     */
    private double setNodePosition(final LNode node, final double newPosition) {
        final double nodeHeigt = getNodeHeight(node);
        KVector position = node.getPosition();
        position.y = newPosition;
        return newPosition + nodeHeigt;
    }   
    
    private double getNodeHeight(LNode node) {
        if (node.getType() == NodeType.LONG_EDGE)
            return 0;
        
        return node.getMargin().top + node.getSize().y + node.getMargin().bottom;            
    }
    
    /**
     * Adds up all node heights and spacings between each node.
     * 
     * @param nodes
     *          list of nodes
     * @return total space occupied by given nodes
     */
    private double getHeightForNodes(final List<LNode> nodes) {
        if (nodes == null || nodes.isEmpty())
            return 0.0;
        double size = getNodeHeight(nodes.get(0));
        NodeType firstNodeType = nodes.get(0).getType();
        for (int i = 1; i < nodes.size(); i++) {
            final NodeType secondNodeType = nodes.get(i).getType();
            final double spacing = spacingUtility.getVerticalSpacing(firstNodeType, secondNodeType);
            size += getNodeHeight(nodes.get(i)) + spacing;
            firstNodeType = secondNodeType;
        }
        return size;
    }
}
