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
package fi.semantum.strategia.map.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fi.semantum.strategia.contrib.ContribVisUtils;
import fi.semantum.strategia.contrib.InnerBoxVis;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.contrib.OuterBoxVis;
import fi.semantum.strategia.contrib.VisRegistry;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.PathVis;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;

public class PainopisteMapVisFactory implements MapVisFactory {

	private int p = MapVisFactory.PRIORITY_MAP_NAVIGATION + 1;
	
	@Override
	public int getPriority() {
		return p;
	}
	
	@Override
	public String getDescription() {
		return "Toteutus Virastoittain";
	}

	@Override
	public boolean accept(Database database, MapBase base) {
		if(base instanceof InnerBox) {
			StrategyMap map = base.getMap(database);
			CharacterInfo ci = map.getCharacterInfo(database);
			if("Hallinnonala".equals(ci.mapType.getId(database)))
				return true;
		}
		return false;
	}

	@Override
	public MapVis create(VisRegistry registry, Database database, Account account, UIState uistate, MapBase base) {

		StrategyMap hallinnonala = base.getMap(database);
		
		MapVis result = new MapVis();
		
		result.parents = hallinnonala.parents;

		StrategyMap someOffice = null;

		List<OuterBoxVis> outers = new ArrayList<OuterBoxVis>();
		for(Linkki l : ContribVisUtils.getSubmapLinks(database, account, uistate, hallinnonala, false)) {
			
			StrategyMap office = database.find(l.uuid);
			someOffice = office;
			OuterBoxVis outer = new OuterBoxVis();
			outer.uuid = office.uuid;
			outer.id = office.getId(database);
			outer.text = office.getText(database);
			outers.add(outer);
			
			ArrayList<InnerBox> tulostavoitteet = new ArrayList<InnerBox>();
			for(OuterBox ob : office.outerBoxes) {
				Set<MapBase> is = ContribVisUtils.getImplementationSet(database, ob);
				if(is.contains(base)) {
					for(InnerBox i : ob.innerboxes)
						tulostavoitteet.add(i);
				}
			}
			
			InnerBoxVis[] inners = new InnerBoxVis[tulostavoitteet.size()];
			for(int i=0;i<tulostavoitteet.size();i++) {
				
				InnerBox tt = tulostavoitteet.get(i);
				StrategyMap map = tt.getMap(database);
				CharacterInfo ci = map.getCharacterInfo(database);
				
				InnerBoxVis inner = new InnerBoxVis();
				inner.uuid = tt.uuid;
				inner.id = "Tulostavoite";
				inner.text = tt.getText(database);
				inner.color = ci.color;
				inners[i] = inner;
				
			}
			
			outer.innerboxes = inners;
			
		}
		
		result.tavoitteet = outers.toArray(new OuterBoxVis[outers.size()]);
		
		if(someOffice != null) {
			result.path = UtilsDB.mapPath(database, someOffice);
		} else {
			result.path = new PathVis[2];
			result.path[0] = UtilsDB.makePathVis(database, hallinnonala);
			result.path[1] = new PathVis("", result.text, "#000", "#eee");
		}

		CharacterInfo info = hallinnonala.getCharacterInfo(database);
		
		result.text = info.goalDescription + " : " + base.getText(database);
		result.path[result.path.length-1] = new PathVis("", base.getText(database), "#000", "#eee");

		return result;
		
	}

}
