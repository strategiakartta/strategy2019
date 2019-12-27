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
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;

public class ActionRemovePrincipalMeter extends ActionBase<MapBase> {

	public ActionRemovePrincipalMeter(Main main, MapBase base) {
		super("Poista arvio", main, base);
	}
	
	public static void perform(Main main, MapBase base) {
		new ActionRemovePrincipalMeter(main, base).run(); 
	}
	
	@Override
	public void run() {

		List<Meter> meters = base.getMeters(database);
		if(meters.size() == 1 && meters.get(0).isPrincipal) {
			base.removeMeter(meters.get(0));
		}
		
		Updates.updateJS(main, true);
		
	}

}
