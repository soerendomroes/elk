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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.elk.alg.layered.JsonDebugUtil;
import org.eclipse.elk.alg.layered.LayeredPhases;
import org.eclipse.elk.alg.layered.graph.LEdge;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.intermediate.IntermediateProcessorStrategy;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutPhase;
import org.eclipse.elk.core.alg.LayoutProcessorConfiguration;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.core.util.LoggedGraph;

public class TreelikeCenter implements ILayoutPhase<LayeredPhases, LGraph> {

    double BETWEEN_LANE_EDGE_SPACEING = 0.0;
    double MIN_NODE_SPACING = 0.0;
    double EDGE_NODE_SPACING = 0.0;
    double[] MIN_POSITION;
    Map<LNode, Node> NODES;

    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final LayoutProcessorConfiguration<LayeredPhases, LGraph> HIERARCHY_PROCESSING_ADDITIONS =
            LayoutProcessorConfiguration.<LayeredPhases, LGraph> create().addBefore(LayeredPhases.P5_EDGE_ROUTING,
                    IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.elk.core.alg.ILayoutPhase#getLayoutProcessorConfiguration(java.lang.Object)
     */
    @Override
    public LayoutProcessorConfiguration<LayeredPhases, LGraph> getLayoutProcessorConfiguration(LGraph graph) {
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
        monitor.begin("Treelike node placement", 1);
        monitor.logGraph(graph, null, null);

        monitor.logGraph(JsonDebugUtil.createDebugGraph(graph), "treelike graph [" + graph.id + "]",
                LoggedGraph.Type.JSON);

        MIN_NODE_SPACING = graph.getProperty(LayeredOptions.SPACING_NODE_NODE);
        EDGE_NODE_SPACING = graph.getProperty(LayeredOptions.SPACING_EDGE_NODE);
        NODES = new HashMap<LNode, Node>();

        final List<Layer> layers = graph.getLayers();
        MIN_POSITION = new double[layers.size()];
        Arrays.fill(MIN_POSITION, 0.0);

        
        
        final List<Node> trees = new ArrayList<Node>();

        for (Layer layer : layers) {
            int treeIndex = 0;
            for (LNode lNode : layer.getNodes()) {
                final boolean isLongEdge = lNode.getType() == NodeType.LONG_EDGE;
                if (!isLongEdge) {
                    if (NODES.containsKey(lNode)) {
                        treeIndex = -1;
                    } else {
                        Node node = new Node(lNode);
                        NODES.put(lNode, node);
                        node.buildTree();
                        node.setTreeHeigth();
                        if (treeIndex < 0) {
                            trees.add(node);
                        } else {
                            trees.add(treeIndex, node);
                            treeIndex++;
                        }
                    }
                }
            }
        }
        
        final boolean useSubtreeSeparation =
                graph.getProperty(LayeredOptions.NODE_PLACEMENT_TREELIKE_CENTER_SUBTREE_SEPARATION);
        
        for (Node tree : trees) {
            final double minPos = MIN_POSITION[0];
            final double maxPox = minPos + tree.treeHeigth;
            placeTree(tree, minPos, maxPox);
            Arrays.fill(MIN_POSITION, Arrays.stream(MIN_POSITION).max().getAsDouble() + MIN_NODE_SPACING);
        }

        monitor.done();
    }

    private void placeTree(final Node treeNode, final double minPosition, final double maxPosition) {

        final int layerIndex = treeNode.getLNode().getLayer().getIndex();
        MIN_POSITION[layerIndex] = Math.max(MIN_POSITION[layerIndex], placeNode(treeNode, minPosition, maxPosition));
        if (treeNode.getChilds().size() > 0) {
            final double spaceWithoutSubtree = maxPosition - minPosition - treeNode.treeHeigth;
            final double treeHeightWithoutNodeSpacing =
                    treeNode.getTreeHeight() - MIN_NODE_SPACING * (treeNode.getChilds().size() - 1);
            final double childSpace = treeHeightWithoutNodeSpacing / treeNode.getChilds().size();
            double nextMin = minPosition + spaceWithoutSubtree * 0.5;
            double nextMax = nextMin + childSpace;
            for (int i = 0; i < treeNode.getChilds().size(); i++) {
                final Node subtree = treeNode.getChilds().get(i);
                placeTree(subtree, nextMin, nextMax);
                nextMin = nextMax + MIN_NODE_SPACING;
                nextMax = nextMin + childSpace;
            }
            // for(Node longEdge : treeNode.getLongEdgeChilds()) placeTree(longEdge);
        }
    }

    public double placeNode(final Node node, final double minPosition, final double maxPosition) {
        KVector nodePosition = node.getLNode().getPosition();
        final double spaceWithoutSubtree = maxPosition - minPosition - node.treeHeigth;
        double min = minPosition + spaceWithoutSubtree * 0.5;
        double max = maxPosition - spaceWithoutSubtree * 0.5;
        nodePosition.y = (min + max) * 0.5 - (node.nodeHeight) * 0.5;
        return nodePosition.y + node.nodeHeight;
    }

    private class Node {
        private LNode node;
        private List<Node> childs = new ArrayList<Node>();
        private double treeHeigth;
        private double nodeHeight;
        // private boolean isLongEdge;

        public Node(LNode node) {
            this.node = node;
            // this.isLongEdge = node.getType() == NodeType.LONG_EDGE;
            this.nodeHeight = node.getMargin().top + node.getSize().y + node.getMargin().bottom;
            this.treeHeigth = this.nodeHeight;
        }

        public LNode getLNode() {
            return node;
        }

        public void buildTree() {
            for (LEdge outgoingedge : node.getOutgoingEdges()) {
                LNode targetNode = outgoingedge.getTarget().getNode();
                if (!NODES.containsKey(targetNode)) {
                    Node child = new Node(targetNode);
                    // if (child.isLongEdge) longEdgeChilds.add(child);
                    // else nonLongEdgeChilds.add(child);
                    childs.add(child);
                    NODES.put(targetNode, child);
                    child.buildTree();
                }
            }
        }

        public List<Node> getChilds() {
            return childs;
        }

        public double getTreeHeight() {
            return this.treeHeigth;
        }

        public void setTreeHeigth() {
            treeHeigth = getRecursiveTreeHeight();
        }

        private double getRecursiveTreeHeight() {
            double min = nodeHeight;
            if (childs.isEmpty())
                return min;
            double max = 0.0;
            for (Node child : childs) {
                child.setTreeHeigth();
                max = Math.max(child.treeHeigth, max);
            }
            if (childs.size() > 1)
                max = max * childs.size() + (childs.size() - 1) * MIN_NODE_SPACING;
            return Math.max(min, max);
        }

    }

}
