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
import java.util.List;

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.StrategyMap;

public class AccountFilter extends AbstractNodeFilter {

	private final Account account;
	private final Property owner;
	private final Property email;
	
	public AccountFilter(Main main, Account account) {
		super(main);
		final Database database = main.getDatabase();
		this.account = account;
		this.owner = Property.find(database, Property.OWNER);
		this.email = Property.find(database, Property.EMAIL);
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
		
		Base last = path.get(path.size()-1);
		Base b = (Base)last;
		
		String uuid = owner.getPropertyValue(b);
		if(account.uuid.equals(uuid)) {
			return new VisuSpec(main, path, "white", "white", "Vastuu");
		}
		
		String es = email.getPropertyValue(b);
		if(es.contains(account.getEmail())) {
			return new VisuSpec(main, path, "white", "white", "Seurannassa");
		}
		
		return null;
		
	}

	
	@Override
	public String toString() {
		return "Omat oliot";
	}
	
}
