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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class TulostavoiteToimenpideFilter extends ImplementationFilter {

	public TulostavoiteToimenpideFilter(Main main, MapBase b) {
		super(main, b);
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState state) {
		super.accept(path, state);
	}
	
	@Override
	public void report(Collection<List<MapBase>> paths, FilterState state) {

		final Database database = main.getDatabase();

		super.report(paths, state);

		Set<Base> internals = new HashSet<Base>();
		for(List<MapBase> path : paths) {
			for(int i=0;i<path.size()-1;i++)
				internals.add(path.get(i));
		}
		
		for(List<MapBase> path : paths) {

			MapBase last = path.get(path.size()-1);
			if(internals.contains(last)) continue;

			StrategyMap map = database.getMap(last);

//			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
//			List<Base> innerboxes = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);

//			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);
//			List<Base> toimenpiteet = FilterUtils.filterByType(database, path, ObjectType.TOIMENPIDE);

			List<Base> maps = map.getMaps(database);

//			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);
//			List<Base> osastot = FilterUtils.filterByType(database, maps, ObjectType.LVM_OSASTO);
//			osastot.addAll(FilterUtils.filterByType(database, maps, ObjectType.VIRASTO_OSASTO));
//			List<Base> yksikot = FilterUtils.filterByType(database, maps, ObjectType.KOKOAVA_YKSIKKO);
//			yksikot.addAll(FilterUtils.filterByType(database, maps, ObjectType.TOTEUTTAVA_YKSIKKO));
//			List<Base> vastuuhenkilot = FilterUtils.filterByType(database, path, ObjectType.VASTUUHENKILO);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
//			r.put(Terminology.STRATEGIC_GOAL_OR_TARGET, firstReportCell(database, strategiset));
//			r.put(Terminology.TAG_FOCUS_POINT, firstReportCell(database, innerboxes));
//			r.put(Terminology.OFFICE_OR_BOARD, firstReportCell(database, virastot));
//			r.put(Terminology.RESULT_GOAL_OR_TARGET, firstReportCell(database, tulostavoitteet));
//			r.put(Terminology.DEPARTMENT, firstReportCell(database, osastot));
//			r.put("Yksikkö", firstReportCell(database, yksikot));
//			r.put(Terminology.ACTION_OR_STEP, firstReportCell(database, toimenpiteet));
//			r.put("Vastuuhenkilö", firstReportCell(database, vastuuhenkilot));
			state.report.add(r);
				
		}
		
	}
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		state.reportColumns.add(Terminology.STRATEGIC_GOAL_OR_TARGET);
		state.reportColumns.add(Terminology.TAG_FOCUS_POINT);
		state.reportColumns.add(Terminology.OFFICE_OR_BOARD);
		state.reportColumns.add(Terminology.RESULT_GOAL_OR_TARGET);
		state.reportColumns.add(Terminology.ACTION_OR_STEP);
		state.reportColumns.add(Terminology.DEPARTMENT);
		state.reportColumns.add("Yksikkö");
		state.reportColumns.add("Vastuuhenkilö");
	}

	@Override
	public String toString() {
		return "Strategian toteutus";
	}
	
}
