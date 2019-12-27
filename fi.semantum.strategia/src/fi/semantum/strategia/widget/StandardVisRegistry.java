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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.contrib.VisRegistry;
import fi.semantum.strategia.map.views.PainopisteMapVisFactory;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.UIState;

public class StandardVisRegistry implements VisRegistry {

	List<MapVisFactory> factories = new ArrayList<MapVisFactory>();
	
	private static StandardVisRegistry INSTANCE;
	
	public static StandardVisRegistry getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new StandardVisRegistry();
		}
		return INSTANCE;
	}

	void registerFactory(MapVisFactory factory) {
		factories.add(factory);
		Collections.sort(factories, new Comparator<MapVisFactory>() {

			@Override
			public int compare(MapVisFactory o1, MapVisFactory o2) {
				return Integer.compare(o2.getPriority(), o1.getPriority());
			}
		});
	}
	
	public boolean hasMapVis(Database database, Account account, UIState uistate, MapBase base) {
		return hasMapVis(database, account, uistate, base, MapVisFactory.PRIORITY_MAP_NAVIGATION);
	}
	
	public boolean hasMapVis(Database database, Account account, UIState uistate, MapBase base, int lowestAcceptablePriority) {

		for(MapVisFactory factory : factories) {
			if(factory.getPriority() >= lowestAcceptablePriority && factory.accept(database, base)) {
				return true;
			}
		}
		
		return false;
		
	}

	public MapVis getBestMapVis(Database database, Account account, UIState uistate, MapBase base) {
		return getBestMapVis(database, account, uistate, base, MapVisFactory.PRIORITY_MAP_NAVIGATION);
	}
	
	@Override
	public MapVis getBestMapVis(Database database, Account account, UIState uistate, MapBase base, int lowestAcceptablePriority) {
		for(MapVisFactory factory : factories) {
			if(factory.getPriority() >= lowestAcceptablePriority && factory.accept(database, base)) {
				return factory.create(this, database, account, uistate, base);
			}
		}
		
		return null;
		
	}
	
	public String getBestMapVisDescription(Database database, Account account, UIState uistate, MapBase base) {
		return getBestMapVisDescription(database, account, uistate, base, MapVisFactory.PRIORITY_MAP_NAVIGATION);
	}
	
	public String getBestMapVisDescription(Database database, Account account, UIState uistate, MapBase base, int lowestAcceptablePriority) {

		for(MapVisFactory factory : factories) {
			if(factory.getPriority() >= lowestAcceptablePriority && factory.accept(database, base)) {
				return factory.getDescription();
			}
		}
		
		return "Näkymä";

	}
	
	public static void init() {
		getInstance().registerFactory(new StrategyMapMapVisFactory());
		getInstance().registerFactory(new PainopisteMapVisFactory());
		getInstance().registerFactory(new DefaultMapVisFactory());
	}
	
}
