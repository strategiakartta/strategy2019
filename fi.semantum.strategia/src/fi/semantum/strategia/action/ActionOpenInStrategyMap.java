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
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.StrategyMap;

public class ActionOpenInStrategyMap extends ActionBase<MapBase> {

	public ActionOpenInStrategyMap(Main main, MapBase base) {
		super("Avaa strategiakartassa", main, base);
	}

	@Override
	public void run() {

		StrategyMap map = database.getMap(base);
		
		main.getUIState().currentMap = map;
		main.getUIState().customVis = null;
		Updates.updateJS(main, false);
		main.switchToMapAndDocumentsView();
		
	}

	public boolean isNavigation() {
		return true;
	}

}
