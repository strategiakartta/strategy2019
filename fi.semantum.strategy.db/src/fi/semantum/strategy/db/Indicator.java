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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Indicator extends MapBase {
	
	private static final long serialVersionUID = -5122160460824254403L;

	private double value = 0;
	public TimeSeries values;
	
	public String unit = "";
	
	public static Indicator create(Database database, String text, Datatype datatype) {
		Indicator p = new Indicator("", text, datatype);
		database.register(p);
		return p;
	}
	
	private Indicator(String id, String text, Datatype datatype) {
		super(UUID.randomUUID().toString(), id, text);
		this.value = Double.NaN;
		values = new TimeSeries(datatype);
	}
	
	public Object getValue(boolean forecast) {
		if(forecast) return getForecast();
		else return getValue();
	}
	
	public Object getValue() {
		if(values != null) {
			return values.getLastValue();
		}
		else return value;
	}
	
	public Object getForecast() {
		if(values != null) {
			return values.getLastForecast();
		}
		else return value;
	}

	public String getValueShortComment() {
		if(values != null) {
			TimeSeriesEntry entry = values.getLastValueEntry();
			if(entry == null) return "";
			String shortComment = entry.getShortComment();
			return shortComment != null ? shortComment : "";
		}
		else return "";
	}
	
	public String getUnitAndComment() {
		return getUnit() + " " + getValueShortComment();
	}
	
	public String getUnit() {
		return unit;
	}
	
	public static List<Indicator> enumerate(Database database) {
		
		ArrayList<Indicator> result = new ArrayList<Indicator>();
		for(Base b : database.objects.values()) {
			if(b instanceof Indicator) result.add((Indicator)b);
		}
		return result;

	}
	
	public void update(final Database database, Account account, Base owner, Object value, boolean forecast, String shortComment, String comment) {
		if(!value.equals(forecast ? getForecast() : getValue())) {
			modified(database, account);
			values.addValue(forecast ? getValue() : value, forecast ? value : getForecast(), account, shortComment, comment);
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	public Datatype getDatatype(Database database) {
		return values.getDatatype();
	}
	
	public void setDatatype(Datatype datatype) {
		if(!datatype.equals(values.getDatatype()))
			values = new TimeSeries(datatype);
	}

	public void modifyUnit(final Database database, Account account, String text) {

		if(!this.unit.equals(text)) {
			modified(database, account);
			this.unit = text;
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public boolean migrate(Database database) {

		boolean result = false;
		
		Account account = database.getDefaultAdminAccount();
		if(description == null) {
			modifyDescription(database, account, getText(database));
			modifyText(database, account, getId(database));
			modifyId(database, account, "");
			result = true;
		}

		if(unit == null) {
			unit = "";
			result = true;
		}
		
		if(values == null && value != Double.NaN) {
			Datatype datatype = Datatype.find(database, NumberDatatype.ID);
			values = new TimeSeries(datatype);
			values.addValue(BigDecimal.valueOf(value), BigDecimal.valueOf(value), null, null, null);
			value = Double.NaN;
			result = true;
		}
		
		result |= super.migrate(database);

		return result;

	}

	@Override
	public MapBase getOwner(Database database) {
		for(MapBase b : database.enumerateMapBase()) {
			if(b.getIndicators(database).contains(this))
				return b;
		}
		return null;
	}

	@Override
	public String clientIdentity() {
		return "Indikaattori";
	}
	
}
