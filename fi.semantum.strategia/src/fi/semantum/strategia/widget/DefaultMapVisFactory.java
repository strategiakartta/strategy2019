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
package fi.semantum.strategia.widget;

import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.contrib.VisRegistry;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.PathVis;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;	

public class DefaultMapVisFactory implements MapVisFactory {

	private int p = MapVisFactory.PRIORITY_MAP_NAVIGATION - 1;
	
	@Override
	public String getDescription() {
		return "Karttaolion tyhjä näkymä";
	}

	@Override
	public int getPriority() {
		return p;
	}

	@Override
	public boolean accept(Database database, MapBase base) {
		return true; //Accepts all
	}

	@Override
	public MapVis create(VisRegistry registry, Database database, Account account, UIState uistate, MapBase base) {
		MapVis result = new MapVis();
		result.uuid = base.uuid;
		result.id = base.getId(database);
		result.text = UtilsDB.mapBaseText(database, base);

		if(base instanceof StrategyMap) {
			result.path = UtilsDB.mapPath(database, (StrategyMap)base);
			result.text = result.path[result.path.length-1].text;
			result.path[result.path.length-1] = new PathVis("", result.text, "#000", "#eee");
			result.parents = ((StrategyMap)base).parents;	
		}
		
		return result;
	}

}
