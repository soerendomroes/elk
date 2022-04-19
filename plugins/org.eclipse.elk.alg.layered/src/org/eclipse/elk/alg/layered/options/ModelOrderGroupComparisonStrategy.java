/*******************************************************************************
 * Copyright (c) 2022 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.alg.layered.options;

/**
 * Strategy to order nodes and ports before crossing minimization.
 */
public enum ModelOrderGroupComparisonStrategy {
    /**
     * A randomized solution that still respects the specified ordering is applied.
     */
    RANDOMIZATION,
    /**
     * Infer a total order by the first occurrence of a model order group.
     */
    INFER_ORDER;

}
