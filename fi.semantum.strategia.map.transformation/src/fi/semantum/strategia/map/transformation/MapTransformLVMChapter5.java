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
package fi.semantum.strategia.map.transformation;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Attributes;

import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBox;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

public class MapTransformLVMChapter5 implements HTMLMapTransform {

	public static final String OFFICE_NAME = "Liikenne- ja viestintäministeriö";

	@Override
	public String tag() {
		return "lvm-luku-5";
	}

	@Override
	public Map<String, String> possibleParametersGuide(Database database){
		Map<String, String> allowedParameters = new HashMap<>();
		allowedParameters.put(MapTransformationUtils.YEAR, "Anna tulossopimuksen vuosi.\n"
				+ "Esim: " + MapTransformationUtils.YEAR + "=\"2019\".");
		return allowedParameters;
	}
	
	MapTable buildTable(Database database, StrategyMap map, Attributes attributes) {
		
		String year = MapTransformationUtils.extractYear(database, attributes);

		StrategyMap possibleOfficeMap = MapTransformationUtils.child(database, map, OFFICE_NAME);

		MapTable result = new MapTable();
		result.widths.add(MapTransformationUtils.TABLE_MAX_WIDTH);
		result.columns = 1;
		result.height = "";

		if(possibleOfficeMap == null) {
			result.cells.add(Terminology.OFFICE_MISSING);
		} else {
			result.cells.add("<div style=\"font-weight: 600;margin: 1mm\">Toiminnalliset tulostavoitteet</div");
			result.heights.add("min-content");
			int counter = 1;
			for(OuterBox t : possibleOfficeMap.outerBoxes) {
				
				if(UtilsDB.isActive(database, year, t)) {
					MapBox b = UtilsDB.getPossibleImplemented(database, t, "Strateginen tavoite");
					if(b != null && b instanceof InnerBox) {
						
						InnerBox ib = (InnerBox)b;
						OuterBox st = ib.getGoal(database);
						
						result.cells.add("<div style=\"font-weight: 600; margin: 1mm; margin-left: 2mm\">" + counter + ". " + st.getBase().getText(database) + " / " + t.getId(database) + "</div>");
						result.heights.add("min-content");
						for(InnerBox i : t.innerboxes) {
							result.cells.add("<div style=\"margin: 1mm; margin-left: 5mm\">" + i.getId(database) + "</div>");
							result.heights.add("min-content");
						}
						counter++;

					}
				}

				
			}
		}
		return result;

	}

	@Override
	public String replace(Database database, StrategyMap map, Attributes attributes) {
		
		return MapTransformationUtils.createTable(buildTable(database, map, attributes), false);

	}

}
