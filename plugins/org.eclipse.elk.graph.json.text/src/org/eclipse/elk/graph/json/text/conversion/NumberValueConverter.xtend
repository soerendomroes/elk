/*******************************************************************************
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.elk.graph.json.text.conversion

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractValueConverter
import org.eclipse.xtext.nodemodel.INode

class NumberValueConverter extends AbstractValueConverter<Double> {

    override toString(Double value) throws ValueConverterException {
        if (value === null)
            throw new ValueConverterException("Double value may not be null.", null, null)
        else if (Math.floor(value) == value)
            return Integer.toString(value.intValue)
        else
            return value.toString
    }

    override toValue(String string, INode node) throws ValueConverterException {
        if (string.nullOrEmpty)
            throw new ValueConverterException("Cannot convert empty string to a double value.", node, null)
        try {
            return Double.valueOf(string)
        } catch (NumberFormatException e) {
            throw new ValueConverterException("Cannot convert '" + string + "' to a double value.", node, e)
        }
    }

}
