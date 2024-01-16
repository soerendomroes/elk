/*******************************************************************************
 * Copyright (c) 2021 Kiel University and others.
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
import org.eclipse.elk.alg.layered.options.InternalProperties;

/**
 * Minimizes crossing with the barycenter method. However, the node order given by the order in the model does not
 * change. I.e. only the dummy nodes are sorted in the already sorted real nodes.
 */
public class ModelOrderBarycenterHeuristic extends AbstractBarycenterHeuristicPreOrdered {

    /**
     * Constructs a model order barycenter heuristic for crossing minimization. After sorting
     * {@link #clearTransitiveOrdering()} should be called.
     * 
     * @param constraintResolver
     *            the constraint resolver
     * @param random
     *            the random number generator
     * @param portDistributor
     *            calculates the port ranks for the barycenter heuristic.
     * @param graph
     *            current node order
     */
    public ModelOrderBarycenterHeuristic(final ForsterConstraintResolver constraintResolver, final Random random,
            final AbstractBarycenterPortDistributor portDistributor, final LNode[][] graph) {
        super(constraintResolver, random, portDistributor, graph);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.elk.alg.layered.p3order.AbstractBarycenterHeuristicPreOrdered#areNodesOrdered(org.eclipse.elk.alg.layered.graph.LNode, org.eclipse.elk.alg.layered.graph.LNode)
     */
    @Override
    protected boolean areNodesOrdered(LNode node1, LNode node2) {
        return node1.hasProperty(InternalProperties.MODEL_ORDER) && node2.hasProperty(InternalProperties.MODEL_ORDER);
    }

    /* (non-Javadoc)
     * @see org.eclipse.elk.alg.layered.p3order.AbstractBarycenterHeuristicPreOrdered#compareNodeOrder(org.eclipse.elk.alg.layered.graph.LNode, org.eclipse.elk.alg.layered.graph.LNode)
     */
    @Override
    protected int compareNodeOrder(LNode node1, LNode node2) {
        return Integer.compare(node1.getProperty(InternalProperties.MODEL_ORDER),
                node2.getProperty(InternalProperties.MODEL_ORDER));
    }

}
