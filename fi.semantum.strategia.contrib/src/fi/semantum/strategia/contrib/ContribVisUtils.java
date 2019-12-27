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
package fi.semantum.strategia.contrib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;

public class ContribVisUtils {

	public static Set<MapBase> getImplementationSet(Database database, MapBase b) {
		
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<MapBase> imp = b.getRelatedObjects(database, implementsRelation);
		if(imp.isEmpty()) return Collections.emptySet();
		Set<MapBase> result = new TreeSet<MapBase>();
		
		if(b instanceof InnerBox) {
			result.add(((InnerBox)b).getGoal(database));
		}
		
		for(MapBase b2 : imp) {
			result.add(b2);
			Set<MapBase> bs = getImplementationSet(database, b2);
			result.addAll(bs);
		}
		return result;
	}

	public static Linkki[] getSubmapLinks(Database database, Account account, UIState uistate, StrategyMap map, boolean filter) {

		ArrayList<Linkki> alikartta = new ArrayList<Linkki>();
		for(Linkki l : map.alikartat) {
			StrategyMap child = database.find(l.uuid);
			if(UtilsDB.canRead(database, account, child)) {
				// If implemented, navigation is possible from goal
				MapBase imp = child.getImplemented(database);
				if(filter && imp != null) continue;
				
				boolean found = false;
				
				if(filter) {
					// Otherwise it might be possible to navigate from meter
					if(uistate.showDetailedMapMeters) {
						for(OuterBox t : child.outerBoxes) {
							MapBase imp2 = t.getPossibleImplemented(database);
							if(imp2 != null) {
								if(imp2.getMap(database).equals(map)) {
									found = true;
									break;
								}
							}
						}
					}
				}

				if(!found)
					alikartta.add(l); 

			}

		}

		return alikartta.toArray(new Linkki[alikartta.size()]);

	}

}
