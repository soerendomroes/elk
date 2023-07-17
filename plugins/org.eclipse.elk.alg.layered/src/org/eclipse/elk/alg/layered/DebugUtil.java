/*******************************************************************************
 * Copyright (c) 2015, 2019 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered;

import java.util.List;

import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.p4nodes.LinearSegmentsNodePlacer.LinearSegment;
import org.eclipse.elk.alg.layered.p5edges.orthogonal.HyperEdgeSegment;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.core.util.LoggedGraph;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.util.ElkGraphUtil;

/**
 * A utility class for debug output of ELK Layered. Currently it delegates to both {@link JsonDebugUtil} and
 * {@link DotDebugUtil}.
 */
public final class DebugUtil {

    private DebugUtil() {
    };
    
    /**
     * Logs a representation of the given graph in dot and JSON format.
     * 
     * @param monitor
     *            the progress monitor to log to.
     * @param lgraph
     *            the layered graph
     * @param slotIndex
     *            the slot before whose execution the graph is written.
     * @param name
     *            the name the slot before whose execution the graph is written.
     * @see {@link DotDebugUtil#createDebugGraph(LGraph, int)}
     * @see {@link JsonDebugUtil#createDebugGraph(LGraph, int)}
     */
    public static void logDebugGraph(final IElkProgressMonitor monitor, final LGraph lgraph, final int slotIndex,
            final String name) {
        
        String tag = slotIndex + "-" + name;
        monitor.logGraph(
                DotDebugUtil.createDebugGraph(lgraph),
                tag,
                LoggedGraph.Type.DOT);
        monitor.logGraph(
                JsonDebugUtil.createDebugGraph(lgraph),
                tag,
                LoggedGraph.Type.JSON);
    }

    /**
     * Logs a debug graph for the given linear segments and their dependencies.
     * 
     * @param monitor
     *            the progress monitor to log to.
     * @param layeredGraph
     *            the layered graph.
     * @param segmentList
     *            the list of linear segments.
     * @param outgoingList
     *            the list of successors for each linear segment.
     * @see {@link DotDebugUtil#createDebugGraph(LGraph, List, List)}
     * @see {@link JsonDebugUtil#createDebugGraph(LGraph, List, List)}
     */
    public static void logDebugGraph(final IElkProgressMonitor monitor, final LGraph layeredGraph,
            final List<LinearSegment> segmentList, final List<List<LinearSegment>> outgoingList) {

        String tag = "segment graph";
        monitor.logGraph(
                DotDebugUtil.createDebugGraph(layeredGraph, segmentList, outgoingList),
                tag,
                LoggedGraph.Type.DOT);
        monitor.logGraph(
                JsonDebugUtil.createDebugGraph(layeredGraph, segmentList, outgoingList),
                tag,
                LoggedGraph.Type.JSON);
    }

    /**
     * Logs a debug graph for the given list of hypernodes.
     * 
     * @param monitor
     *            the progress monitor to log to.
     * @param layeredGraph
     *            the layered graph
     * @param layerIndex
     *            the currently processed layer's index
     * @param hypernodes
     *            a list of hypernodes
     * @param debugPrefix
     *            prefix of debug output files
     * @param label
     *            a label to append to the output files
     * @see {@link DotDebugUtil#createDebugGraph(LGraph, int, List, String, String)}
     * @see {@link JsonDebugUtil#createDebugGraph(LGraph, int, List, String, String)}
     */
    public static void logDebugGraph(final IElkProgressMonitor monitor, final LGraph layeredGraph,
            final int layerIndex, final List<HyperEdgeSegment> hypernodes, final String debugPrefix,
            final String label) {

        String tag = debugPrefix + " - " + label;
        monitor.logGraph(
                DotDebugUtil.createDebugGraph(layeredGraph, hypernodes),
                tag,
                LoggedGraph.Type.DOT);
        monitor.logGraph(
                JsonDebugUtil.createDebugGraph(layeredGraph, hypernodes),
                tag,
                LoggedGraph.Type.JSON);
    }
    
    /**
     * Log an LGraph in the debug view by creating a new Elkgraph by creating nodes and ports
     * with the corresponding positions for the top level hierarchy.
     * @param monitor The progress monitor to log to.
     * @param lgraph The layered graph.
     * @param slotIndex The slot before whose execution the graph is written.
     * @param name The name of the slot before whose execution the graph is written.
     */
    public static void logLGraphNodesAndPorts(final IElkProgressMonitor monitor, final LGraph lgraph, final int slotIndex,
            final String name) {
        ElkNode elkGraph = ElkGraphUtil.createGraph();
        elkGraph.setLocation(lgraph.getOffset().x, lgraph.getOffset().y);
        elkGraph.setDimensions(lgraph.getSize().x, lgraph.getSize().y);
//        HashMap<LGraphElement, Integer> idMap = new HashMap<>();
//        for (Layer layer : lgraph) {
//            for (LNode node : layer.getNodes()) {
//                idMap.put(node, node.id);
//                node.id = 0;
//                for (LPort port : node.getPorts()) {
//                    idMap.put(port, port.id);
//                    port.id = 0;
//                    for (LEdge edge : port.getOutgoingEdges()) {
//                        idMap.put(edge, edge.id);
//                        edge.id = 0;
//                    }
//                }
//            }
//        }
//        List<ElkPort> dummyTargetPorts = new ArrayList<>();
        for (Layer layer : lgraph) {
            for (LNode node : layer.getNodes()) {
                ElkNode elkNode = ElkGraphUtil.createNode(elkGraph);
                elkNode.setIdentifier(node.toString());
                elkNode.setX(node.getPosition().x);
                elkNode.setY(node.getPosition().y);
                elkNode.setWidth(node.getSize().x);
                elkNode.setHeight(node.getSize().y);
                
                for (LPort port : node.getPorts()) {
                    ElkPort elkPort = ElkGraphUtil.createPort(elkNode);
                    elkPort.setIdentifier(port.toString());
                    elkPort.setX(port.getPosition().x);
                    elkPort.setY(port.getPosition().y);
                    elkPort.setWidth(port.getSize().x);
                    elkPort.setHeight(port.getSize().y);
//                    for (LEdge edge : port.getOutgoingEdges()) {
//                        ElkPort dummyTarget = ElkGraphUtil.createPort(elkNode);
//                        dummyTarget.setIdentifier(edge.getTarget().toString());
//                        ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(elkPort, dummyTarget);
//                        elkEdge.setIdentifier(elkEdge.toString());
//                        
//                        // Find target port by identifier.
//                        
//                    }
                }
            }
        }
//        for (ElkPort elkPort : dummyTargetPorts) {
//            for(ElkNode elkNode : elkGraph.getChildren()) {
//                for (ElkPort targetPort : elkNode.getPorts().stream().filter(p -> !p.getIncomingEdges().isEmpty()).toList()) {
//                    if (targetPort.getIdentifier().equals(elkPort.getIdentifier())) {
//                        targetPort.getIncomingEdges().addAll(elkPort.getIncomingEdges());
//                    }
//                }
//            }
//        }

//        for (LGraphElement e : idMap.keySet()) {
//            e.id = idMap.get(e);
//        }
        
        monitor.logGraph(elkGraph, slotIndex + "-" + name);
    }

}
