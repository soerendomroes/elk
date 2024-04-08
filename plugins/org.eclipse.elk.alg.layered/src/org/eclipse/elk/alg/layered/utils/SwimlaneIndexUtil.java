/*******************************************************************************
 * Copyright (c) 2024 stu205834 and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0 
 *******************************************************************************/
package org.eclipse.elk.alg.layered.utils;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.graph.properties.IProperty;

/**
 * @author stu205834
 *
 */
public class SwimlaneIndexUtil {
    
    /**
     * Property identifier for swimlane lane index option <br>
     * shorter than 'LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE'
     */
    private static IProperty<Integer> LANE_INDEX = LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE;
    
    private SwimlaneIndexUtil () {}
    
    /**
     * Sets the lane index property for the given node.
     *  
     * @param node
     * @param laneIndex
     */
    public static void setLane (final LNode node, final int laneIndex) {
        node.setProperty(LANE_INDEX, laneIndex);
    }
    
    /**
     * Gets the lane index property for the given node.
     * If the lane index is not set, the implicit value will be returned.
     * 
     * @param node
     * @return lane index
     */
    public static int getLane (final LNode node) {
        if(node.hasProperty(LANE_INDEX))
            return node.getProperty(LANE_INDEX);
        
        // implicitly created nodes
        return getLaneIndexByType(node);
    }
    
    /**
     * Gets the lane index property for the given node.
     * If the lane index is not set, the implicit value will be set and returned.
     * 
     * @param node
     * @return
     */
    public static int setAndGetLaneIndex(final LNode node) {
        if(node.hasProperty(LANE_INDEX))
            return node.getProperty(LANE_INDEX);
        
        // implicitly created nodes
        final int laneIndex =  getLaneIndexByType(node);
        setLane(node, laneIndex);
        return laneIndex;
    }
    
    /**
     * Determines the implicit lane index value for the given node.
     * 
     * @param node
     * @return lane index
     */
    private static int getLaneIndexByType(final LNode node) {
        switch(node.getType()) {
        case NORMAL:
            return node.getProperty(LANE_INDEX);
        case LONG_EDGE:
            return getLaneIndexForLongEdgeNode(node);
        default:
            return getSourceNodeLaneIndex(node);
        }
    }
    
    /**
     * Returns the lane index of the source node.
     * 
     * @param node
     * @return lane index
     */
    private static int getSourceNodeLaneIndex(LNode node) {
        return getActualSourceNode(node).getProperty(LANE_INDEX);
    }
    
    /**
     * Returns the lane index of the actual source node if the source lane index
     * is smaller than the target lane index and the target lane index otherwise. 
     * 
     * @param longEdgeNode
     * @return lane index for long edge node
     */
    private static int getLaneIndexForLongEdgeNode(LNode longEdgeNode) {
        final int sourceLane = getActualSourceNode(longEdgeNode).getProperty(LANE_INDEX);
        final int targetLane = getActualTargetNode(longEdgeNode).getProperty(LANE_INDEX);

        if (sourceLane < targetLane)
            return targetLane;
        else
            return sourceLane;
    }

    /**
     * Recursively search through target nodes until a normal node is found.
     * 
     * @param node
     * @return the first normal node
     */
    private static LNode getActualTargetNode(LNode node) {
        for (LEdge edge : node.getOutgoingEdges()) {
            final LNode target = edge.getTarget().getNode();
            if (target.getType() != NodeType.LONG_EDGE && target.getType() != NodeType.LABEL)
                // actual target found
                return target;
            else
                // look at the next node
                return getActualTargetNode(target);
        }
        return null;
    }
    
    /**
     * Recursively search through source nodes until a normal node is found.
     * 
     * @param node
     * @return the first normal node
     */
    private static LNode getActualSourceNode(LNode node) {
        for (LEdge edge : node.getIncomingEdges()) {
            final LNode source = edge.getSource().getNode();
            if (source.getType() != NodeType.LONG_EDGE && source.getType() != NodeType.LABEL)
                // actual source found
                return source;
            else
                // look at the next node
                return getActualSourceNode(source);
        }
        return null;
    }
}
