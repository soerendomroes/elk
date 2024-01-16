/*******************************************************************************
 * Copyright (c) 2024 stu205834 and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0 
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p3order;

import java.util.Random;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.options.LayeredOptions;

public class SwimlaneBarycenterHeuristic extends AbstractBarycenterHeuristicPreOrdered {
    
    /**
     * @param constraintResolver
     * @param random
     * @param portDistributor
     * @param graph
     */
    public SwimlaneBarycenterHeuristic(ForsterConstraintResolver constraintResolver, Random random,
            AbstractBarycenterPortDistributor portDistributor, LNode[][] graph) {
        super(constraintResolver, random, portDistributor, graph);
    }

    /* (non-Javadoc)
     * @see org.eclipse.elk.alg.layered.p3order.AbstractBarycenterHeuristicPreOrdered#areNodesOrdered(org.eclipse.elk.alg.layered.graph.LNode, org.eclipse.elk.alg.layered.graph.LNode)
     */
    @Override
    protected boolean areNodesOrdered(LNode node1, LNode node2) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.elk.alg.layered.p3order.AbstractBarycenterHeuristicPreOrdered#compareNodeOrder(org.eclipse.elk.alg.layered.graph.LNode, org.eclipse.elk.alg.layered.graph.LNode)
     */
    @Override
    protected int compareNodeOrder(LNode node1, LNode node2) {
        final int firstNodeLane = getLaneProperty(node1);
        final int secondNodeLane = getLaneProperty(node2);

        return Integer.compare(firstNodeLane, secondNodeLane);
    }
    
    private int getLaneProperty (final LNode node) {
        switch(node.getType()) {
        case NORMAL:
            return node.getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
        case LONG_EDGE:
            return getLaneIndexForLongEdgeNode(node);
        default:
            return getSourceNodeLaneIndex(node);
        }
    }
    
    private int getSourceNodeLaneIndex(LNode edgeLabelNode) {
        return getActualSourceNode(edgeLabelNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
    }
    
    private int getLaneIndexForLongEdgeNode(LNode longEdgeNode) {
        final int sourceLane = getActualSourceNode(longEdgeNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);
        final int targetLane = getActualTargetNode(longEdgeNode).getProperty(LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE);

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


}
