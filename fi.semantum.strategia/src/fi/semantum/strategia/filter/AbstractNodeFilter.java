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
import java.util.Collections;
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;

abstract public class AbstractNodeFilter implements NodeFilter {

	protected Main main;
	
	AbstractNodeFilter(Main main) {
		this.main = main;
	}
	
	
	protected VisuSpec defaultSpec(List<MapBase> path, boolean condition) {
		return VisuSpec.getDefault(main, path, condition);
	}
	
	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		return Collections.emptyList();
	}
	
	public boolean filter(List<MapBase> path) {
		return true;
	}
	
	@Override
	public void accept(List<MapBase> path, FilterState filterState) {

		if(!filter(path)) return;
		
		VisuSpec spec = acceptNode(path, filterState);
		if(spec != null) {
			filterState.accept(this, path);
		}
		
	}
	
	@Override
	public void report(Collection<List<MapBase>> path, FilterState state) {
		
	}
	
	@Override
	public void refresh() {
		
	}
	
	@Override
	public void reset(FilterState state) {
		refresh();
	}
	
	protected ReportCell reportCell(Database database, Base base) {
		return new ReportCell(base.getId(database), base.getShortText(database), base.getText(database));
	}

	protected ReportCell firstReportCell(Database database, List<Base> bases) {
		if(!bases.isEmpty()) {
			return reportCell(database, bases.get(0));
		} else {
			return ReportCell.EMPTY;
		}
	}
	
	@Override
	public boolean reject(List<MapBase> path) {
		return false;
	}
	
}
