/*******************************************************************************
 * Copyright (c) 2022 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.intermediate.preserveorder.ModelOrderNodeComparator;
import org.eclipse.elk.alg.layered.intermediate.preserveorder.ModelOrderPortComparator;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.LongEdgeOrderingStrategy;
import org.eclipse.elk.alg.layered.options.OrderingStrategy;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Checks how all edges are sorted in each layer.
 * <dl><dl>
 *   <dt>Precondition:</dt>
 *     <dd>a proper layered graph</dd>
 *     <dd>nodes have fixed port sides.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>Nodes and ports (edges) are sorted to respect the order in the input graph</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>Is first only for debugging/dd>
 * </dl>
 */
public class GetModelOrderViolations implements ILayoutProcessor<LGraph> {

    @Override
    public void process(final LGraph graph, final IElkProgressMonitor progressMonitor) {
        OrderingStrategy strategy = graph.getProperty(LayeredOptions.CONSIDER_MODEL_ORDER_STRATEGY);
        if (strategy == OrderingStrategy.NONE) {
            strategy = OrderingStrategy.NODES_AND_EDGES;
        }

        int modelOrderNodeChanges = countModelOrderNodeChanges(graph,
                strategy);
        int modelOrderPortsChanges = countModelOrderPortChanges(graph);
        // XXX log to some file
        if (graph.hasProperty(InternalProperties.MODEL_NAME)) {
            System.out.println(graph.getProperty(InternalProperties.MODEL_NAME)
                    + ": " + modelOrderNodeChanges + " . " + modelOrderPortsChanges);
        } else {
            System.out.println(": " + modelOrderNodeChanges + " . " + modelOrderPortsChanges);
        }
    }

    /**
     * Compares all nodes in a each layer and counts how often they are not in model order.
     * This requires that the {@code SortByInputModelProcessor} ran previously.
     * @param layers layers to check
     * @param strategy the ordering strategy to compare the nodes
     * @return The number of model order conflicts
     */
    private int countModelOrderNodeChanges(final LGraph graph, final OrderingStrategy strategy) {
        int previousLayer = -1;
        int wrongModelOrder = 0;
        for (Layer layer : graph.getLayers()) {
            ModelOrderNodeComparator comp = new ModelOrderNodeComparator(
                    previousLayer == -1 ? graph.getLayers().get(0) : graph.getLayers().get(previousLayer)
                            , strategy, LongEdgeOrderingStrategy.EQUAL);
            for (int i = 0; i < layer.getNodes().size(); i++) {
                for (int j = i + 1; j < layer.getNodes().size(); j++) {
                    if (comp.compare(layer.getNodes().get(i), layer.getNodes().get(j)) > 0) {
                        wrongModelOrder++;
                    }
                }
            }
            previousLayer++;
        }
        return wrongModelOrder;
    }

    /**
     * Compares all ports in a each layer and counts how often they are not in model order.
     * This requires that the {@code SortByInputModelProcessor} ran previously.
     * @param layers layers to check
     * @return The number of model order conflicts
     */
    private int countModelOrderPortChanges(final LGraph graph) {
        int previousLayer = -1;
        int wrongModelOrder = 0;
        for (Layer layer : graph.getLayers()) {
            for (LNode lNode : layer) {
                Comparator<LPort> comp = new ModelOrderPortComparator(
                        previousLayer == -1 ? graph.getLayers().get(0) : graph.getLayers().get(previousLayer),
                                graph.getProperty(LayeredOptions.CONSIDER_MODEL_ORDER_STRATEGY),
                                SortByInputModelProcessor.longEdgeTargetNodePreprocessing(lNode));
                for (int i = 0; i < lNode.getPorts().size(); i++) {
                    for (int j = i + 1; j < lNode.getPorts().size(); j++) {
                        if (comp.compare(lNode.getPorts().get(i), lNode.getPorts().get(j)) > 0) {
                            wrongModelOrder++;
                        }
                    }
                }
            }
            previousLayer++;
        }
        return wrongModelOrder;
    }
}