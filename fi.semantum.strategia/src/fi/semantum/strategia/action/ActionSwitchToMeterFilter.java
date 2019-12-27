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
import fi.semantum.strategia.filter.MeterFilter;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;

public class ActionSwitchToMeterFilter extends ActionBase<MapBase> {

	public ActionSwitchToMeterFilter(Main main, MapBase base) {
		super("Valmiusasteet", HAKU, main, base);
	}

	@Override
	public void run() {

		StrategyMap map = database.getMap(base);
		main.getUIState().currentMapPosition = map;

		main.setCurrentFilter(new MeterFilter(main, base));
		if(base instanceof OuterBox) {
			main.getUIState().mapLevel = 4;
			main.less.setEnabled(true);
		} else {
			main.getUIState().mapLevel = 2;
			main.less.setEnabled(false);
		}
		
		main.setCurrentItem(base, main.getUIState().currentMapPosition);
		main.switchToBrowser();
	}

}
