/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.alg.layered.compaction.components;

import java.util.List;
import java.util.Set;

import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.util.nodespacing.Rectangle;

/**
 * A component represents, for instance, a set of nodes that are fully connected. In other words,
 * between any pair of nodes of the component exists an undirected path.
 * 
 * @param <N>
 *            the type of the contained nodes
 * @param <E>
 *            the type of the contained edges
 */
public interface IComponent<N, E> {

    /**
     * @return a hull the nodes of this connected component. This, for instance, can be a
     *         rectilinear convex hull or a polyomino.
     */
    List<Rectangle> getHull();

    /**
     * @return a list with external extensions of this component.
     */
    List<IExternalExtension<E>> getExternalExtensions();
    
    /**
     * @return a set of the port sides of this component's external ports (if there are any,
     *         otherwise an empty set).
     */
    Set<PortSide> getExternalExtensionSides();

}
