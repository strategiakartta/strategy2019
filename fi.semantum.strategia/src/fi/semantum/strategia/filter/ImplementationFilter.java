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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;

public class ImplementationFilter extends AbstractNodeFilter {

	protected Database database;
	//protected Strategiakartta scope;
	private Relation implementsRelation;
	protected Set<MapBase> visited = new HashSet<MapBase>();
	
	public ImplementationFilter(Main main, MapBase target) {
		super(main);
		this.database = main.getDatabase();
//		this.scope = scope;
		this.implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
//		Set<Base> tgts = new HashSet<Base>();
//		if(target instanceof Strategiakartta) {
//			Strategiakartta map = (Strategiakartta)target;
//			tgts = new HashSet<Base>();
//			for(Tavoite t : map.tavoitteet) {
//				tgts.add(t);
//			}
//			
//		} else {
//			tgts = Collections.singleton(target);
//		}
//		setTargets(tgts);
	}
	
	@Override
	public Collection<MapBase> traverse(List<MapBase> path, FilterState filterState) {
		MapBase last = path.get(path.size()-1);
		visited.add(last);
		if(last instanceof StrategyMap) {
			ArrayList<MapBase> result = new ArrayList<MapBase>();
			StrategyMap map = (StrategyMap)last;
			for(OuterBox t : map.outerBoxes) {
				if(!visited.contains(t))
					result.add(t);
			}
			for (Linkki l : map.alikartat) {
				StrategyMap child = database.find(l.uuid);
				result.add(child);
			}
			return result;
		} else {
			return database.getInverse(last, implementsRelation);
		}
	}
	
	
	public Base getLastMapObject(List<Base> path) {
		for(int i=0;i<path.size();i++) {
			Base b = path.get(path.size()-1-i);
			if(b instanceof StrategyMap) return b;
			else if(b instanceof OuterBox) return b;
			else if(b instanceof InnerBox) return b;
		}
		return null;
	}
	
//	public boolean hasTargets(List<Base> path) {
//		for(Base target : getTargets()) {
//			for(int i=0;i<path.size();i++) {
//				if(path.get(i).equals(target))
//					continue;
//			}
//			return false;
//		}
//		return true;
//	}
	
	public List<MapBase> prune(List<MapBase> path) {
		return path;
	}
	
	@Override
	public void accept(List<MapBase> path_, FilterState state) {
		List<MapBase> path = prune(path_);
		//if(!FilterUtils.inScope(database, path, scope)) return;
		if(!Utils.canRead(main, path)) return;
		//if(!hasTargets(path)) return;
		//if(path.size() - pos <= main.getUIState().level) {
			state.accept(this, path);
		//}
	}
	
	@Override
	public boolean reject(List<MapBase> path) {
		if(path.size() > main.getUIState().mapLevel) return true;
		return false;
	}
	
	@Override
	public VisuSpec acceptNode(List<MapBase> path, FilterState state) {
		
		MapBase last = path.get(path.size()-1);
		return makeSpec(path, state, last);
		
	}
	
	protected VisuSpec makeSpec(List<MapBase> path, FilterState state, MapBase base) {
		return defaultSpec(path, true);
	}
	
	@Override
	public String toString() {
		return "Toteutushierarkia";
	}
	
	@Override
	public void reset(FilterState state) {
		refresh();
		visited.clear();
	}

}
