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

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class TimeSeries implements Serializable {

	private static final long serialVersionUID = -7523567241097594084L;

	private Datatype datatype;
	
	private TreeMap<Date,TimeSeriesEntry> values = new TreeMap<Date,TimeSeriesEntry>();
	
	public TimeSeries(Datatype datatype) {
		assert(datatype != null);
		this.datatype = datatype;
	}
	
	public Datatype getDatatype() {
		return datatype;
	}
	
	void addValue(Object value, Object estimate, Account account, String shortComment, String comment) {
		TimeSeriesEntry entry = new TimeSeriesEntry(value, estimate, account, shortComment, comment);
		values.put(new Date(), entry);
	}
	
	Object getLastValue() {
		
		Date now = new Date();
		Map.Entry<Date,TimeSeriesEntry> entry = values.floorEntry(now);
		if(entry == null) return null;
		return entry.getValue().getValue();

	}

	Object getLastForecast() {
		
		Date now = new Date();
		Map.Entry<Date,TimeSeriesEntry> entry = values.floorEntry(now);
		if(entry == null) return null;
		return entry.getValue().getForecast();

	}

	TimeSeriesEntry getLastValueEntry() {
		
		Date now = new Date();
		Map.Entry<Date,TimeSeriesEntry> entry = values.floorEntry(now);
		if(entry == null) return null;
		return entry.getValue();

	}

	public Set<Map.Entry<Date,TimeSeriesEntry>> list() {
		return values.descendingMap().entrySet();
	}
	
}
