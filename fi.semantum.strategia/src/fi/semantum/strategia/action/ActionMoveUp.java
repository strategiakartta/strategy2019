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
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBox;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;

public class ActionMoveUp extends ActionBase<MapBox> {
	
	public ActionMoveUp(Main main, MapBox m) {
		super("Siirrä ylemmäs", main, m);
	}
	
	@Override
	public void run() {
		base.moveUp(main.getDatabase());
		Updates.update(main, true);
	}
	
	public boolean accept(OuterBox goal) {

		Database database = main.getDatabase();
		StrategyMap map = database.getMap(goal);
		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if(goal.hasRelatedTag(database, voimavarat)) return false;
		
		int pos = map.findTavoite(goal);
		return pos > 0;

	}

}
