/*******************************************************************************
 * Copyright (c) 2010, 2015 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate;

import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LGraphAdapters;
import org.eclipse.elk.alg.layered.graph.LLabel;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LNode.NodeType;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.math.ElkRectangle;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.PortLabelPlacement;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.core.util.nodespacing.NodeDimensionCalculation;

/**
 * Calculates node sizes, places ports, and places node and port labels.
 * 
 * <p><i>Note:</i> Regarding port placement, this processor now does what the old
 * {@code PortPositionProcessor} did and thus replaces it.</p>
 * 
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>The graph is layered.</dd>
 *     <dd>Crossing minimization is finished.</dd>
 *     <dd>Port constraints are at least at {@code FIXED_ORDER}.</dd>
 *     <dd>Port lists are properly sorted going clockwise, starting at the leftmost northern port.</dd>
 *     <dd>External port dummies have the label-port and label-label spacing set that should apply to them.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>Port positions are fixed.</dd>
 *     <dd>Port labels are placed.</dd>
 *     <dd>Node labels are placed.</dd>
 *     <dd>Node sizes are set.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 4.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>{@link LabelSideSelector}</dd>
 * </dl>
 * 
 * @see LabelSideSelector
 * @author cds
 */
public final class LabelAndNodeSizeProcessor implements ILayoutProcessor<LGraph> {
    
    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IElkProgressMonitor monitor) {
        monitor.begin("Node and Port Label Placement and Node Sizing", 1);
        
        NodeDimensionCalculation.calculateLabelAndNodeSizes(LGraphAdapters.adapt(
                layeredGraph,
                false,
                node -> node.getType() == NodeType.NORMAL || node.getType() == NodeType.BIG_NODE));
        
        // If the graph has external ports, we need to treat labels of external port dummies a bit differently,
        // which is the reason why we haven't handed them to the label and node size processing code
        if (layeredGraph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(GraphProperties.EXTERNAL_PORTS)) {
            PortLabelPlacement portLabelPlacement = layeredGraph.getProperty(LayeredOptions.PORT_LABELS_PLACEMENT);
            
            for (Layer layer : layeredGraph.getLayers()) {
                layer.getNodes().stream()
                        .filter(node -> node.getType() == NodeType.EXTERNAL_PORT)
                        .forEach(dummy -> placeExternalPortDummyLabels(dummy, portLabelPlacement));
            }
        }
        
        monitor.done();
    }
    
    /**
     * Places the labels of the given external port dummy such that it results in correct node margins later on that
     * will reserve enough space for the labels to be placed once label and node placement is called on the graph.
     */
    private void placeExternalPortDummyLabels(final LNode dummy, final PortLabelPlacement graphPortLabelPlacement) {
        double labelPortSpacing = dummy.getProperty(LayeredOptions.SPACING_LABEL_PORT);
        double labelLabelSpacing = dummy.getProperty(LayeredOptions.SPACING_LABEL_LABEL);
        
        KVector dummySize = dummy.getSize();
        
        // External port dummies have exactly one port (see ElkGraphImporter)
        LPort dummyPort = dummy.getPorts().get(0);
        KVector dummyPortPos = dummyPort.getPosition();
        
        ElkRectangle portLabelBox = computePortLabelBox(dummyPort, labelLabelSpacing);
        if (portLabelBox == null) {
            return;
        }
        
        // Determine the position of the box
        // TODO We could handle FIXED here as well
        if (graphPortLabelPlacement == PortLabelPlacement.INSIDE) {
            // TODO We could also handle the case where an external port does not have any connections
            // (port label placement has to support this case first, though)
            switch (dummy.getProperty(InternalProperties.EXT_PORT_SIDE)) {
            case NORTH:
//                portLabelBox.x = (dummySize.x - portLabelBox.width) / 2 - dummyPortPos.x;
                portLabelBox.x = dummySize.x + labelPortSpacing - dummyPortPos.x;
                portLabelBox.y = labelPortSpacing;
                break;
                
            case SOUTH:
//                portLabelBox.x = (dummySize.x - portLabelBox.width) / 2 - dummyPortPos.x;
                portLabelBox.x = dummySize.x + labelPortSpacing - dummyPortPos.x;
                portLabelBox.y = -labelPortSpacing - portLabelBox.height;
                break;
                
            case EAST:
                portLabelBox.x = -labelPortSpacing - portLabelBox.width;
//                portLabelBox.y = (dummySize.y - portLabelBox.height) / 2 - dummyPortPos.y;
                portLabelBox.y = dummySize.y + labelPortSpacing - dummyPortPos.y;
                break;
                
            case WEST:
                portLabelBox.x = labelPortSpacing;
//                portLabelBox.y = (dummySize.y - portLabelBox.height) / 2 - dummyPortPos.y;
                portLabelBox.y = dummySize.y + labelPortSpacing - dummyPortPos.y;
                break;
            }
        } else if (graphPortLabelPlacement == PortLabelPlacement.OUTSIDE) {
            switch (dummy.getProperty(InternalProperties.EXT_PORT_SIDE)) {
            case NORTH:
            case SOUTH:
                portLabelBox.x = dummyPortPos.x + labelPortSpacing;
                break;
                
            case EAST:
            case WEST:
                portLabelBox.y = dummyPortPos.y + labelPortSpacing;
                break;
            }
        }
        
        // Place the labels
        double currentY = portLabelBox.y;
        for (LLabel label : dummyPort.getLabels()) {
            KVector labelPos = label.getPosition();
            
            labelPos.x = portLabelBox.x;
            labelPos.y = currentY;
            
            currentY += label.getSize().y + labelLabelSpacing;
        }
    }
    
    /**
     * Returns the amount of space required to place the labels later, or {@code null} if there are no labels.
     */
    private ElkRectangle computePortLabelBox(final LPort dummyPort, final double labelLabelSpacing) {
        if (dummyPort.getLabels().isEmpty()) {
            return null;
        } else {
            ElkRectangle result = new ElkRectangle();
            
            for (LLabel label : dummyPort.getLabels()) {
                KVector labelSize = label.getSize();
                
                result.width = Math.max(result.width, labelSize.x);
                result.height += labelSize.y;
            }
            
            result.height += (dummyPort.getLabels().size() - 1) * labelLabelSpacing;
            
            return result;
        }
    }
    
}
