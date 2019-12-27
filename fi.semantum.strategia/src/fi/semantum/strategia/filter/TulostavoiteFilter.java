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
import fi.semantum.strategia.UtilsDataTypes;
import fi.semantum.strategia.UtilsMeters;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.configurations.DefaultInitialActiveYears;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TimeConfiguration;
import fi.semantum.strategy.db.UtilsDB;

public class TulostavoiteFilter extends ImplementationFilter {

	public TulostavoiteFilter(Main main, MapBase target) {
		super(main, target);
	}
	
	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		
		
		ArrayList<MapBase> result = new ArrayList<MapBase>();
		MapBase last = path.get(path.size()-1);
		
//		if(isTulostavoite(last)) {
//			return result; 
//		}
//		
//		if(isVirasto(last)) {
//			Strategiakartta map = (Strategiakartta)last;
//			for(Tavoite t : map.tavoitteet) {
//				if(!visited.contains(t))
//					result.add(t);
//			}
//		} else {
			result.addAll(super.traverse(path, filterState));
//		}
		
		return result;
		
	}
	
//	private boolean isTulostavoite(Base base) {
//		
//		ObjectType tp = ObjectType.find(database, ObjectType.TULOSTAVOITE);
//		String uuid = FilterUtils.getObjectTypeUUID(database, base);
//		return tp.uuid.equals(uuid);
//
//	}

//	private boolean isVirasto(Base base) {
//		
//		ObjectType tp = ObjectType.find(database, ObjectType.VIRASTO);
//		String uuid = FilterUtils.getObjectTypeUUID(database, base);
//		return tp.uuid.equals(uuid);
//
//	}

	@Override
	public void report(Collection<List<MapBase>> paths, FilterState state) {
		
		final Database database = main.getDatabase();

		super.report(paths, state);
		
		for(List<MapBase> path : paths) {

			MapBase last = path.get(path.size()-1);
			
//			if(!isTulostavoite(last)) continue;
			
			StrategyMap map = database.getMap(last);

//			List<Base> strategiset = FilterUtils.filterByType(database, path, ObjectType.STRATEGINEN_TAVOITE);
//			List<Base> innerboxes = FilterUtils.filterByType(database, path, ObjectType.PAINOPISTE);
//			List<Base> tulostavoitteet = FilterUtils.filterByType(database, path, ObjectType.TULOSTAVOITE);

			List<Base> maps = map.getMaps(database);
//			List<Base> virastot = FilterUtils.filterByType(database, maps, ObjectType.VIRASTO);
			
			Property aika = Property.find(database, Property.AIKAVALI);

			Map<String,ReportCell> r = new HashMap<String,ReportCell>();
//			r.put(Terminology.STRATEGIC_GOAL_OR_TARGET, firstReportCell(database, strategiset));
//			r.put(Terminology.TAG_FOCUS_POINT, firstReportCell(database, innerboxes));
//			r.put(Terminology.OFFICE_OR_BOARD, firstReportCell(database, virastot));
//			r.put(Terminology.RESULT_GOAL_OR_TARGET, firstReportCell(database, tulostavoitteet));
			
			r.put("Indikaattori", ReportCell.EMPTY);
			r.put(Terminology.VALUE, ReportCell.EMPTY);
			
			ArrayList<Integer> allYears = TimeConfiguration.getInstance(database).getAllConfiguredYears();
			for(Integer y : allYears) {
				r.put(""+y, ReportCell.EMPTY);
			}

			for(Meter m : last.getMeters(database)) {
				Indicator in = m.getPossibleIndicator(database);
				if(in != null) {
					String yearS = aika.getPropertyValue(m);
					if(DefaultInitialActiveYears.isAConfiguredYear(Integer.parseInt(yearS))) {
						r.put(yearS, new ReportCell(UtilsDataTypes.getTrafficValuationDescription(database, m)));
					}
					r.put(Terminology.VALUE, new ReportCell("" + in.getValue()));
					r.put("Indikaattori", new ReportCell(in.getCaption(database)));
				}
			}
			
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
		}
		
		return defaultSpec(path, true);
		
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState state) {
		
		Base last = path.get(path.size()-1);
//		if(!isTulostavoite(last)) return;
		super.accept(path, state);
		
	}
	
	@Override
	public void reset(FilterState state) {
		super.reset(state);
		state.reportColumns.add(Terminology.STRATEGIC_GOAL_OR_TARGET);
		state.reportColumns.add(Terminology.TAG_FOCUS_POINT);
		state.reportColumns.add(Terminology.OFFICE_OR_BOARD);
		state.reportColumns.add(Terminology.RESULT_GOAL_OR_TARGET);
		state.reportColumns.add("Indikaattori");
		state.reportColumns.add(Terminology.VALUE);
		
		ArrayList<Integer> years = TimeConfiguration.getInstance(database).getAllConfiguredYears();
		for(Integer y : years) {
			state.reportColumns.add(y.toString());
		}
	}
	
	@Override
	public String toString() {
		return "Tulostavoitteet";
	}

}
