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

public enum BarycenterHeuristicStrategy {
    /**
     * The node order given by the model does not change to produce a better layout. E.g. if node A is before node B in
     * the model this is not changed during crossing minimization. This assumes that the node model order is already
     * respected before crossing minimization. This can be achieved by setting considerModelOrder.strategy to
     * NODES_AND_EDGES.
     */
    MODEL_ORDER,
    /**
     * When employing the swimlane node placement strategy, predefined lanes inherently dictate a certain node order.
     * Within these lanes, nodes have the flexibility to be placed freely while adhering to their designated order.
     */
    SWIMLANE_ORDER,
    /**
     * Barycenter heuristic implementation.
     */
    NONE;

    public BarycenterHeuristic create(final ForsterConstraintResolver constraintResolver, final Random random,
            final AbstractBarycenterPortDistributor portDistributor, final LNode[][] graph) {
        switch (this) {
        case MODEL_ORDER:
            return new ModelOrderBarycenterHeuristic(constraintResolver, random, portDistributor, graph);
        case SWIMLANE_ORDER:
            return new SwimlaneBarycenterHeuristic(constraintResolver, random, portDistributor, graph);
        case NONE:
            return new BarycenterHeuristic(constraintResolver, random, portDistributor, graph);
        default:
            throw new IllegalArgumentException(
                    "No implementation is available for the Barycenter Heuristic option " + this.toString());
        }
    }
}
