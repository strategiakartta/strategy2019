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
package fi.semantum.strategia.action;

import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.UtilsIndicators;
import fi.semantum.strategia.UtilsMeters;
import fi.semantum.strategy.db.Datatype;
import fi.semantum.strategy.db.EnumerationDatatype;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class ActionCreatePrincipalMeter extends ActionBase<MapBase> {

	public ActionCreatePrincipalMeter(Main main, MapBase base) {
		super("Aseta arvio", main, base);
	}

	private Datatype findType() {
        List<Datatype> types = Datatype.enumerate(database);
        for(Datatype dt : types) {
        	if(dt instanceof EnumerationDatatype) {
        		String id = dt.getId(database);
        		if(Terminology.ACTUALIZATION.equals(id)) return dt;
        	}
        }
        return null;
	}
	
	public static void perform(Main main, MapBase base) {
		new ActionCreatePrincipalMeter(main, base).run(); 
	}
	
	@Override
	public void run() {

		List<Meter> meters = base.getMeters(database);
		if(meters.size() == 1 && meters.get(0).isPrincipal) return;
		
		Datatype dt = findType();
		if(dt == null) return;
		
		Indicator ind = Indicator.create(database, "", dt);
		UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultValue(), false, "", "Alkuarvo");
		UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultForecast(), true, "", "Alkuarvo");
		Meter m = UtilsMeters.addIndicatorMeter(main, base, ind, UtilsDB.getValidity(database, base));
		m.isPrincipal = true;
		
		Updates.updateJS(main, true);
		
	}

}
