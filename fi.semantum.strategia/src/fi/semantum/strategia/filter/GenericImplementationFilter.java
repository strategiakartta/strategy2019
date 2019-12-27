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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.contrib.InnerBoxVis;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategia.widget.VisUtils;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class GenericImplementationFilter extends ImplementationFilter {

	private List<String> typeColumns;
	
	public GenericImplementationFilter(Main main, MapBase b, List<String> typeColumns) {
		super(main, b);
		this.typeColumns = typeColumns;
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState state) {
		super.accept(path, state);
	}
	
	@Override
	public List<MapBase> prune(List<MapBase> path) {
		List<MapBase> result = new ArrayList<MapBase>();
		for(int i=0;i<path.size();i++) {
			if(i == 0) result.add(path.get(i));
			else {
				MapBase previous = path.get(i-1);
				MapBase current = path.get(i);
				MapBase possibleCopy = current.getPossibleCopy(database);
				if(previous.equals(possibleCopy)) {
					result.remove(previous);
				}
				result.add(current);
			}
		}
		return result;
	}
	
	Map<String,Map<Base,ReportCell>> cellCache = new HashMap<String, Map<Base,ReportCell>>();
	
	private ReportCell computeCell(String currentType, MapBase b) {
		
		if(currentType.equals("$" + Terminology.METER)) {
			if(b instanceof InnerBox) {
				InnerBox p = (InnerBox)b;
				StrategyMap map = p.getMap(database);
				InnerBoxVis pv = VisUtils.defaultInnerBoxVis(main.getDatabase(), main.getAccountDefault(), main.getUIState(), map, p.getGoal(database), 0, false, 0, p, 0);
				if(pv.hasMeter) return new ReportCell(pv.leafMeterPct);
			}
		} else if(currentType.equals("$" + Terminology.MAP)) {
			StrategyMap map = b.getMap(database);
			return new ReportCell(map.getShortText(database), map.getText(database));
		} else if(currentType.equals(database.getType(b))) {
			return reportCell(database, b);
		}
		
		return null;
		
	}

	private ReportCell cacheCell(String currentType, MapBase b) {
		Map<Base,ReportCell> cs = cellCache.get(currentType);
		ReportCell cell = cs.get(b);
		if(cell == null) {
			cell = computeCell(currentType, b);
			cs.put(b, cell);
		}
		return cell;
	}
	
	@Override
	public void report(Collection<List<MapBase>> paths, FilterState state) {

		super.report(paths, state);

		Set<MapBase> internals = new HashSet<MapBase>();
		for(List<MapBase> path : paths) {
			for(int i=0;i<path.size()-1;i++)
				internals.add(path.get(i));
		}

		if(typeColumns.isEmpty()) return;
		
		for(List<MapBase> path : paths) {

			MapBase last = path.get(path.size()-1);
			if(internals.contains(last)) continue;
			
			int columnPosition = 0;
			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			for(int i=0;i<path.size();) {
				MapBase b = path.get(i);
				String currentType = typeColumns.get(columnPosition);
				ReportCell cell = cacheCell(currentType, b);
				if(cell != null) {
					r.put(currentType, cell);
					columnPosition++;
					if(columnPosition == typeColumns.size()) break;
					continue;
				}
				
				// Nothing was found from here - move on
				i++;
				
			}
			
			if(r.size() ==  typeColumns.size())
				state.report.add(r);
				
		}
		
	}

	private Map<Base,StrategyMap> maps = new HashMap<Base,StrategyMap>();

	private StrategyMap mapCache(Database database, MapBase b) {
		StrategyMap map = maps.get(b);
		if(map == null) {
			map = b.getMap(database);
			maps.put(b, map);
		}
		return map;
	}

	@Override
	public boolean reject(List<MapBase> path) {
		
		// Level
		if(super.reject(path)) return true;
		
		for(MapBase b : main.getUIState().requiredMapItems) {
			if(b instanceof StrategyMap) {
				StrategyMap map = (StrategyMap)b;
				boolean found = false;
				for(MapBase b2 : path) {
					if(map.equals(mapCache(database, b2))) {
						found = true;
						continue;
					}
				}
				if(!found) return true;
			} else {
				if(!path.contains(b))
					return true;
			}
		}
		
		return false;
		
	}
	
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		maps.clear();
		state.reportColumns.addAll(typeColumns);
		cellCache.clear();
		for(String col : typeColumns)
			cellCache.put(col, new HashMap<Base, ReportCell>());
	}

	@Override
	public String toString() {
		return "Toteutusraportti";
	}
	
}
