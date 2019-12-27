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
package fi.semantum.strategy.db;

import fi.semantum.strategy.db.ImplementationMeter;

public class MeterSpec {
	public static Base IMPLEMENTATION = new ImplementationMeter();
	
	private Object source;
	private String text;
	
	public MeterSpec(Database database, Base source) {
		this.source = source;
		text = source.getText(database);
		if(source instanceof Indicator) text = text + " [Indikaattori]"; 
		else if(source instanceof EnumerationDatatype) text = text + " [Monivalinta]"; 
	}
	
	public String toString() {
		return text;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSource() {
		return (T)source;
	}
}
