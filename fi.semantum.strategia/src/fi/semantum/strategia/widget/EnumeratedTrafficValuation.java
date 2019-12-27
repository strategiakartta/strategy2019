/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia.widget;

import java.io.Serializable;
import java.util.TreeMap;

import com.vaadin.ui.VerticalLayout;

import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Meter;

public class EnumeratedTrafficValuation implements TrafficValuation, Serializable {

	private static final long serialVersionUID = 4463910301472508822L;
	
	final protected TreeMap<Object,String> values;
	
	public EnumeratedTrafficValuation(TreeMap<Object,String> values) {
		this.values = values;
		if(values == null)
			System.err.println("Null values in EnumeratedTrafficValuation");
	}

	@Override
	public String getTrafficValue(Object value) {
		if(values == null)
			return TrafficValuation.RED;
		String result = values.get(value);
		if(result == null)
			return TrafficValuation.RED;
		return result;
	}
	
	@Override
	public Runnable getEditor(VerticalLayout layout, Main main, Meter meter) {
		
		return null;
		
	}	
}
