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
import java.util.List;

import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategy.db.MapBase;

public interface NodeFilter {

	void accept(List<MapBase> path, FilterState filterState);
	Collection<MapBase> traverse(List<MapBase> path, FilterState filterState);
	VisuSpec acceptNode(List<MapBase> path, FilterState filterState);
	public void report(Collection<List<MapBase>> paths, FilterState state);
	void refresh();
	boolean reject(List<MapBase> path);
	void reset(FilterState state);
	
}
