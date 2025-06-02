/*******************************************************************************
 * Copyright (c) 2022, 2025 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0 
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p1cycles;

import java.util.List;

import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.options.GroupOrderStrategy;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;

/**
 * Greedy Cycle Breaker that behaves the same as {@link GreedyCycleBreaker} but does not randomly choose an edge to
 * reverse if multiple candidates exist but does so by model order.
 */
public final class GreedyModelOrderCycleBreaker extends GreedyCycleBreaker {

    private int firstSeparateModelOrder;
    private int lastSeparateModelOrder;
    
    /**
     * Choose the node with the minimum model order.
     */
    @Override
    protected LNode chooseNodeWithMaxOutflow(final List<LNode> nodes) {
        LNode returnNode = null;
        int minimumModelOrder = Integer.MAX_VALUE;
        int offset = Math.max(layeredGraph.getLayerlessNodes().size(), layeredGraph.getProperty(InternalProperties.MAX_MODEL_ORDER_NODES));
        int bigOffset = offset * layeredGraph.getProperty(InternalProperties.CB_NUM_MODEL_ORDER_GROUPS);
        boolean enforceGroupModelOrder = layeredGraph.getProperty(
                LayeredOptions.CONSIDER_MODEL_ORDER_GROUP_MODEL_ORDER_CB_GROUP_ORDER_STRATEGY) == GroupOrderStrategy.ENFORCED;
        for (LNode node : nodes) {
            // In this step nodes without a model order are disregarded.
            // One could of course think of a different strategy regarding this aspect.
            if (node.hasProperty(InternalProperties.MODEL_ORDER)) {
                int modelOrder = enforceGroupModelOrder
                ? computeConstraintGroupModelOrder(node, bigOffset, offset)
                : computeConstraintModelOrder(node, offset);
                if (minimumModelOrder > modelOrder) {                    
                    minimumModelOrder = modelOrder;
                    returnNode = node;
                }
            }
        }
        if (returnNode == null) {
            return super.chooseNodeWithMaxOutflow(nodes);
        }
        return returnNode;
    }


    /**
     * Set model order to a value such that the constraint is respected and the ordering between nodes with
     * the same constraint is preserved.
     * The order should be FIRST_SEPARATE < FIRST < NORMAL < LAST < LAST_SEPARATE. The offset is used to make sure the 
     * all nodes have unique model orders.
     * @param node The LNode
     * @param offset The offset between FIRST, FIRST_SEPARATE, NORMAL, LAST_SEPARATE, and LAST nodes for unique order
     * @return A unique model order
     */
    private int computeConstraintModelOrder(final LNode node, final int offset) {
        int modelOrder = 0;
        switch (node.getProperty(LayeredOptions.LAYERING_LAYER_CONSTRAINT)) {
        case FIRST_SEPARATE:
            modelOrder = 2 * -offset + firstSeparateModelOrder;
            firstSeparateModelOrder++;
            break;
        case FIRST:
            modelOrder = -offset;
            break;
        case LAST:
            modelOrder = offset;
            break;
        case LAST_SEPARATE:
            modelOrder = 2 * offset + lastSeparateModelOrder;
            lastSeparateModelOrder++;
            break;
        default:
            break;
        }
        if (node.hasProperty(InternalProperties.MODEL_ORDER)) {
            modelOrder += node.getProperty(InternalProperties.MODEL_ORDER);
        }
        return modelOrder;
    }


    /**
     * Set group model order to a value such that the constraint is respected and the ordering between nodes with
     * the same constraint is preserved.
     * The order should be FIRST_SEPARATE < FIRST < NORMAL < LAST < LAST_SEPARATE. The offset is used to make sure the 
     * all nodes have unique group model orders. We calculate this offset by "highest model order * number of model order
     * groups" and the small offset by using only the highest model order.
     * @param node The LNode
     * @param offset The offset between FIRST, FIRST_SEPARATE, NORMAL, LAST_SEPARATE, and LAST nodes for unique order
     * @param smallOffset The offset between each model order group.
     * @return A unique group model order
     */
    protected int computeConstraintGroupModelOrder(final LNode node, final int offset, final int smallOffset) {
        int modelOrder = 0;
        switch (node.getProperty(LayeredOptions.LAYERING_LAYER_CONSTRAINT)) {
        case FIRST_SEPARATE:
            modelOrder = 2 * -offset + firstSeparateModelOrder;
            firstSeparateModelOrder++;
            break;
        case FIRST:
            modelOrder = -offset;
            break;
        case LAST:
            modelOrder = offset;
            break;
        case LAST_SEPARATE:
            modelOrder = 2 * offset + lastSeparateModelOrder;
            lastSeparateModelOrder++;
            break;
        default:
            break;
        }
        if (node.hasProperty(InternalProperties.MODEL_ORDER)) {
            modelOrder += node.getProperty(LayeredOptions.CONSIDER_MODEL_ORDER_GROUP_MODEL_ORDER_CYCLE_BREAKING_ID)
                    * smallOffset + node.getProperty(InternalProperties.MODEL_ORDER);
            
        }
        return modelOrder;
    }

}
