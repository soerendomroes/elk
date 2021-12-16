/*******************************************************************************
 * Copyright (c) 2021 sdo and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0 
 *******************************************************************************/
package org.eclipse.elk.alg.rectpacking.options;

import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

/**
 * Container for property definitions for internal use.
 */
public final class InternalProperties {
    public static final IProperty<Double> MIN_ROW_INCREASE = new Property<Double>("minRowIncrease", 0.0);
    public static final IProperty<Double> MAX_ROW_INCREASE = new Property<Double>("maxRowIncrease", 0.0);
    public static final IProperty<Double> MIN_ROW_DECREASE = new Property<Double>("minRowDecrease", 0.0);
    public static final IProperty<Double> MAX_ROW_DECREASE = new Property<Double>("maxRowDecrease", 0.0);
    public static final IProperty<Boolean> SECOND_ITERATION = new Property<Boolean>("secondIteration", false);
    public static final IProperty<Boolean> SECOND_ITERATION_WAS_BETTER = new Property<Boolean>("secondIterationWasBetter", false);
    
    /**
     * hidden default constructor.
     */
    private InternalProperties() {
        
    }
}
