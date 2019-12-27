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

import fi.semantum.strategy.db.Meter.TrafficValuationEnum;

public class StringDatatype extends Datatype {

	private static final long serialVersionUID = -5442621428107892964L;

	protected StringDatatype(Database database) {
		super(database, "Vapaateksti", "Vapaamuotoinen kuvaus");
	}
	
	@Override
	public Object getDefaultValue() {
		return "";
	}
	
	@Override
	public Object getDefaultForecast() {
		return "";
	}
	
	@Override
	public String format(Object value) {
		return value.toString();
	}
	
	@Override
	public String clientIdentity() {
		return "Teksti datatyyppi";
	}
	
	@Override
	public TrafficValuationEnum getTrafficValuationEnum() {
		return Meter.TrafficValuationEnum.String;
	}
	
}
