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

import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.utils.SwimlaneIndexUtil;
import org.eclipse.elk.graph.properties.IProperty;

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
        IProperty<Integer> laneIndex = LayeredOptions.NODE_PLACEMENT_SWIMLANE_LANE;
        if (node1.hasProperty(laneIndex) && node2.hasProperty(laneIndex)) {
            return node1.getProperty(laneIndex) != node1.getProperty(laneIndex);
        }
        // at least one node is not a normal node and will get a lane index later
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.elk.alg.layered.p3order.AbstractBarycenterHeuristicPreOrdered#compareNodeOrder(org.eclipse.elk.alg.layered.graph.LNode, org.eclipse.elk.alg.layered.graph.LNode)
     */
    @Override
    protected int compareNodeOrder(LNode node1, LNode node2) {
        final int firstNodeLane = SwimlaneIndexUtil.getLane(node1);
        final int secondNodeLane = SwimlaneIndexUtil.getLane(node2);

        return Integer.compare(firstNodeLane, secondNodeLane);
    }
}
