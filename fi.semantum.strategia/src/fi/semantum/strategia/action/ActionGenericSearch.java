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

import java.util.ArrayList;
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.GenericImplementationFilter;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Terminology;

public class ActionGenericSearch extends ActionBase<MapBase> {

	public ActionGenericSearch(Main main, MapBase base) {
		super("Yleinen haku", HAKU, main, base);
	}

	@Override
	public void run() {

		List<String> cols = new ArrayList<String>();
		cols.add(Terminology.STRATEGIC_GOAL_OR_TARGET);
		cols.add(Terminology.TAG_FOCUS_POINT);
		
		main.getUIState().mapLevel = 3;
		main.setCurrentFilter(new GenericImplementationFilter(main, base, cols));
		
		main.setCurrentItem(base, database.getMap(base));
		main.switchToBrowser();

	}

}
