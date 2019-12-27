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

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.TulostavoiteToimenpideFilter;
import fi.semantum.strategy.db.MapBase;

public class ActionSwitchToBrowser extends ActionBase<MapBase> {

	public ActionSwitchToBrowser(Main main, MapBase base) {
		super("Strategian toteutus", HAKU, main, base);
	}

	@Override
	public void run() {

		main.getUIState().mapLevel = 3;
		main.setCurrentFilter(new TulostavoiteToimenpideFilter(main, base));
		
		main.setCurrentItem(base, database.getMap(base));
		main.switchToBrowser();

	}

}
