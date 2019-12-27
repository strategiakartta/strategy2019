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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import fi.semantum.strategy.db.Meter.TrafficValuationEnum;

public class NumberDatatype extends Datatype {

	private static final long serialVersionUID = -9212610888611727972L;

    private static DecimalFormat format = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US)); 
	
	public static final String ID = "Lukuarvo";

	protected NumberDatatype(Database database) {
		super(database, ID, "Kokonaisluku tai desimaaliluku");
	}

	@Override
	public Object getDefaultValue() {
		return BigDecimal.valueOf(0);
	}
	
	@Override
	public Object getDefaultForecast() {
		return BigDecimal.valueOf(1);
	}

	@Override
	public String format(Object value) {
		if(value == null) return "<arvoa ei ole asetettu>";
		if(!(value instanceof BigDecimal)) return "invalid value (" + value.getClass().getSimpleName() + ")";
		BigDecimal bd = (BigDecimal)value;
		return format.format(bd.doubleValue());
	}
	
	@Override
	public String clientIdentity() {
		return "Numeerinen tietotyyppi";
	}

	@Override
	public TrafficValuationEnum getTrafficValuationEnum() {
		return Meter.TrafficValuationEnum.Number;
	}
	
}
