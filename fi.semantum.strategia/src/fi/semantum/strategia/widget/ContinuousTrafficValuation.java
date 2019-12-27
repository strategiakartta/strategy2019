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
import fi.semantum.strategy.db.UtilsDB;

public class ContinuousTrafficValuation implements TrafficValuation {
	
	public static ContinuousTrafficValuation INSTANCE = new ContinuousTrafficValuation();

	@Override
	public String getTrafficValue(Object value) {
		Double d = (Double)value;
		return UtilsDB.trafficColor(d);
	}

	@Override
	public Runnable getEditor(VerticalLayout layout, Main main, Meter meter) {
		return null;
	}
	
}
