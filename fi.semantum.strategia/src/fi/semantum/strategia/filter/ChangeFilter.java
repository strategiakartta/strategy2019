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
package fi.semantum.strategia.filter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDB;

public class ChangeFilter extends AbstractNodeFilter {

	private HashMap<MapBase,Date> bases;
	private Date now;
	
	public ChangeFilter(Main main) {
		super(main);
		now = new Date();
	}

	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		
		final Database database = main.getDatabase();
		MapBase last = path.get(path.size()-1);
		if(last instanceof StrategyMap) {
			ArrayList<MapBase> result = new ArrayList<MapBase>();
			StrategyMap map = (StrategyMap)last;
			for(OuterBox t : map.outerBoxes) {
				result.add(t);
			}
			for (Linkki l : map.alikartat) {
				StrategyMap child = database.find(l.uuid);
				result.add(child);
			}
			return result;
		} else if(last instanceof OuterBox) {
			ArrayList<MapBase> result = new ArrayList<MapBase>();
			OuterBox goal = (OuterBox)last;
			for(InnerBox p : goal.innerboxes) {
				result.add(p);
			}
			return result;
		}
		
		return super.traverse(path, filterState);
		
	}
	
	@Override
	public boolean filter(List<MapBase> path) {
		return FilterUtils.contains(path, main.getUIState().currentMapPosition);
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState filterState) {
		
		if(filterState.countAccepted() > main.uiState.mapLevel * 3) return;
		
		if(!Utils.canRead(main, path)) return;
		
		super.accept(path, filterState);
		
	}
	
	@Override
	public VisuSpec acceptNode(List<MapBase> path, FilterState state) {
		
		MapBase last = path.get(path.size()-1);
		
		if(bases.containsKey(last)) {
			Date d = bases.get(last);
			return new VisuSpec(main, path, "white", "white", Utils.describeDate(now, d));
		}
		else
			return null;
		
	}
	
	@Override
	public void refresh() {

		final Database database = main.getDatabase();
		TreeMap<Date, MapBase> changes = new TreeMap<Date, MapBase>();
		Property changed = Property.find(database, Property.CHANGED_ON);
		for(MapBase b : database.enumerateMapBase()) {
			String value = changed.getPropertyValue(b);
			if(value != null) {
				try {
					Date d = UtilsDB.simpleDateFormat.parse(value);
					changes.put(d, b);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		bases = new HashMap<MapBase,Date>();
		for(Map.Entry<Date,MapBase> b : changes.descendingMap().entrySet()) {
			bases.put(b.getValue(), b.getKey());
		}

	}
	
	@Override
	public String toString() {
		return "Viimeisimmät muutokset";
	}

}
