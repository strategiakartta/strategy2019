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
import java.util.List;
import java.util.Map;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;

public class SearchFilter extends AbstractNodeFilter {

	private final Map<String,String> hits;
	
	public SearchFilter(Main main, Map<String,String> hits) {
		super(main);
		this.hits = hits;
	}
	
	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		
		final Database database = main.getDatabase();

		Base last = path.get(path.size()-1);
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
		return FilterUtils.contains(path, main.uiState.currentMapPosition);
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState filterState) {
		
		if(filterState.countAccepted() > main.uiState.mapLevel * 3) return;
	
		if(!Utils.canRead(main, path)) return;

		MapBase last = path.get(path.size()-1);
		String content = hits.get(last.uuid);
		if(content == null) return;

		super.accept(path, filterState);
		
	}
	
	
	@Override
	public VisuSpec acceptNode(List<MapBase> path, FilterState state) {
		
		MapBase last = path.get(path.size()-1);
		String content = hits.get(last.uuid);
		if(content != null) {
			return new VisuSpec(main, path, "white", "white", content);
		}
		return new VisuSpec(main, path, "white", "white", content);
		
	}
	
	@Override
	public void report(Collection<List<MapBase>> paths, FilterState state) {

		for(List<MapBase> path : paths) {
			
			if(path.isEmpty()) continue;

			MapBase last = path.get(path.size()-1);
			
			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
			r.put("Teksti", new ReportCell(last.getShortText(main.getDatabase()), last.getText(main.getDatabase())));
			state.report.add(r);				
				
		}
		
	}

	@Override
	public void reset(FilterState state) {
		super.reset(state);
		state.reportColumns.add("Teksti");
	}

	@Override
	public String toString() {
		return  "Vapaasanahaku";
	}

}
