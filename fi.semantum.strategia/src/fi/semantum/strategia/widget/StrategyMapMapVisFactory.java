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
package fi.semantum.strategia.widget;

import java.util.ArrayList;

import fi.semantum.strategia.contrib.ContribVisUtils;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.contrib.OuterBoxVis;
import fi.semantum.strategia.contrib.VisRegistry;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.PathVis;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;

public class StrategyMapMapVisFactory implements MapVisFactory {

	private int p = MapVisFactory.PRIORITY_MAP_NAVIGATION + 2;
	
	@Override
	public int getPriority() {
		return p;
	}

	@Override
	public boolean accept(Database database, MapBase base) {
		return base instanceof StrategyMap;
	}

	@Override
	public MapVis create(VisRegistry registry, Database database, Account account, UIState uistate, MapBase base) {

		MapVis result = new MapVis();
		StrategyMap map = (StrategyMap)base;

		result.columns = map.columns;

		result.showVision = map.showVision;
		result.commentToolVisible = uistate.commentLayoutOpen;
		
		CharacterInfo ci = map.getCharacterInfo(database);
		if(!ci.hasVision) result.showVision = false;
		
		result.amountOfLevels = ci.amountOfLevels;

		if(uistate.showDetailedMapMeters) {
			if(uistate.forecastMeters) {
				result.meterStatus = "Ennusteet";
			} else {
				result.meterStatus = "Toteumat";
			}
		} else {
			result.meterStatus = "";
		}

		result.uuid = map.uuid;
		result.id = map.getId(database);
		result.text = UtilsDB.mapBaseText(database, map);
		if(result.text.isEmpty()) result.text = "<anna teksti tai lyhytnimi kartalle>";

		result.visio = map.vision;

		result.path = UtilsDB.mapPath(database, map);
	
		result.text = result.path[result.path.length-1].text;

		result.path[result.path.length-1] = new PathVis("", result.text, "#000", "#eee");

		ArrayList<OuterBoxVis> pps = new ArrayList<OuterBoxVis>();

		Property aika = Property.find(database, Property.AIKAVALI);

		for(int i=0;i<map.outerBoxes.length;i++) {
			OuterBox p = map.outerBoxes[i];
			String a = aika.getPropertyValue(p);
			if(a != null) {
				if(uistate.acceptTime(a))
					pps.add(VisUtils.defaultOuterBoxVis(database, account, uistate, map, p, i));
			} else {
				pps.add(VisUtils.defaultOuterBoxVis(database, account, uistate, map, p, i));
			}
		}

		if(map.voimavarat != null && uistate.showMapVoimavarat)
			pps.add(VisUtils.defaultOuterBoxVis(database, account, uistate, map, map.voimavarat, map.outerBoxes.length));

		result.tavoitteet = pps.toArray(new OuterBoxVis[pps.size()]);

		result.parents = map.parents;
		
		result.alikartat = ContribVisUtils.getSubmapLinks(database, account, uistate, map, true);

		result.tavoiteDescription = map.outerDescription;
		result.painopisteDescription = map.innerDescription;

		result.tavoiteColor = map.outerColor;
		result.painopisteColor = map.innerColor;

		result.tavoiteTextColor = map.outerTextColor;
		result.painopisteTextColor = map.innerTextColor;

		result.scrollFocus = 2;
		
		return result;

	}

	@Override
	public String getDescription() {
		return "Strategiakartta";
	}

}
