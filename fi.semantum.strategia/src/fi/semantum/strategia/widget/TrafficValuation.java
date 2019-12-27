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

import com.vaadin.ui.VerticalLayout;

import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Meter;

public interface TrafficValuation {
	
	public static String RED = "#DA251D";
	public static String YELLOW = "#F4C000";
	public static String GREEN = "#00923F";

	String getTrafficValue(Object value);
	
	public Runnable getEditor(VerticalLayout layout, Main main, Meter meter);
	
}
