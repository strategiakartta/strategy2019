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
import fi.semantum.strategia.filter.TulostavoiteFilter;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.StrategyMap;

public class ActionShowGoals extends ActionBase<MapBase> {

	public ActionShowGoals(Main main, MapBase base) {
		super("Tulostavoitteet", HAKU, main, base);
	}

	@Override
	public void run() {

		StrategyMap map = database.getMap(base);
		main.getUIState().currentMapPosition = map;

		main.setCurrentFilter(new TulostavoiteFilter(main, base));
		main.getUIState().mapLevel = 10;
		
		main.setCurrentItem(base, main.getUIState().currentMapPosition);
		main.switchToBrowser();
		
	}

}
