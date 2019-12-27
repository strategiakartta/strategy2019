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
import fi.semantum.strategia.UtilsMeters;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class MeterFilter extends ImplementationFilter {

	public MeterFilter(Main main, MapBase target) {
		super(main, target);
	}
	
	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		
		MapBase last = path.get(path.size()-1);

		ArrayList<MapBase> result = new ArrayList<MapBase>();
		result.addAll(UtilsMeters.getMetersActive(main.getDatabase(), main.getUIState(), last));
		result.addAll(super.traverse(path, filterState));
		return result;
		
	}

	@Override
	public void report(Collection<List<MapBase>> paths, FilterState state) {
		
		final Database database = main.getDatabase();

		super.report(paths, state);
		
		for(List<MapBase> path : paths) {

			MapBase last = path.get(path.size()-1);

			Meter meter = (last instanceof Meter) ? (Meter)last : null;
			MapBase last2 = path.get(path.size()-2);
			
			StrategyMap map = database.getMap(last2);

//			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
//			List<Base> innerboxes = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);
//			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);
//			List<Base> toimenpiteet = FilterUtils.filterByType(database, path, ObjectType.TOIMENPIDE);

			List<Base> maps = map.getMaps(database);

//			List<Base> osastot = FilterUtils.filterByType(database, maps, ObjectType.LVM_OSASTO);
//			osastot.addAll(FilterUtils.filterByType(database, maps, ObjectType.VIRASTO_OSASTO));
//			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
//			r.put(Terminology.STRATEGIC_GOAL_OR_TARGET, firstReportCell(database, strategiset));
//			r.put(Terminology.TAG_FOCUS_POINT, firstReportCell(database, innerboxes));
//			r.put(Terminology.OFFICE_OR_BOARD, firstReportCell(database, virastot));
//			r.put(Terminology.RESULT_GOAL_OR_TARGET, firstReportCell(database, tulostavoitteet));
//			r.put(Terminology.ACTION_OR_STEP, firstReportCell(database, toimenpiteet));
//			r.put(Terminology.DEPARTMENT, firstReportCell(database, osastot));
			r.put(Terminology.DEFINITION_OR_GUIDE, meter == null ? ReportCell.EMPTY : new ReportCell(meter.getCaption(database)));
			r.put(Terminology.VALUE, meter == null ? ReportCell.EMPTY : new ReportCell(UtilsMeters.explain(meter, database)));
			state.report.add(r);
				
		}
		
	}
	
	@Override
	protected VisuSpec makeSpec(List<MapBase> path, FilterState state, MapBase base) {

		if(base instanceof Meter) {
			Meter m = (Meter)base;
			double value = UtilsMeters.value(m, database);
			String color = UtilsDB.trafficColor(value);
			return new VisuSpec(main, path, "black", color, UtilsMeters.describe(m, database, main.getUIState().forecastMeters));
		} else {
			
			double value = 0;
			List<Meter> meters = UtilsMeters.getMetersActive(main.getDatabase(), main.getUIState(), base);
			for(Meter m : meters) {
				value += UtilsMeters.value(m, database);
			}
			
			if(meters.isEmpty()) {
				String color = UtilsDB.trafficColor(value);
				return new VisuSpec(main, path, "black", color);
			} else {
				value /= meters.size();
				String color = UtilsDB.trafficColor(value);
				return new VisuSpec(main, path, "black", color);
			}
			
		}
		
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState state) {
		
		MapBase last = path.get(path.size()-1);
		if((last instanceof Meter) || !state.isPrefix(path)) {
			super.accept(path, state);	
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
		state.reportColumns.add(Terminology.DEFINITION_OR_GUIDE);
		state.reportColumns.add(Terminology.VALUE);
	}
	
	@Override
	public String toString() {
		return "Valmiusasteet";
	}

}
