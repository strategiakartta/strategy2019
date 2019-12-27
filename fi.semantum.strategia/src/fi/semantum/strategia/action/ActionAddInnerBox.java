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
import fi.semantum.strategia.Utils;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class ActionAddInnerBox extends ActionBase<MapBase> {
	
	private String desc;
	
	public ActionAddInnerBox(String desc, Main main, MapBase base) {
		super(Terminology.ADD + " " + desc.toLowerCase(), main, base);
		this.desc = desc;
	}

	@Override
	public void run() {

		final StrategyMap map = database.getMap(base);

		OuterBox goal = (OuterBox)base;
		InnerBox pp = InnerBox.create(main.getDatabase(), main.getUIState().getTime(), map, goal, "Uusi " + desc);
		Updates.updateJS(main, true);
		
		try {
			StrategyMap submap = goal.getPossibleImplementationMap(database);
			if(submap != null) {
				Utils.createOuterBoxCopy(main, submap, pp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
}
