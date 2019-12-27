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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Attributes;

import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

public class MapTransformEntireMap implements HTMLMapTransform {

	@Override
	public String tag() {
		return "koko-kartta-taulukkona";
	}

	@Override
	public Map<String, String> possibleParametersGuide(Database database) {
		Map<String, String> allowedParameters = new HashMap<>();
		
		allowedParameters.put(MapTransformationUtils.YEAR,
				"Mit‰ vuotta k‰ytet‰‰n. Jos tyhj‰, k‰ytet‰‰n kaikkia.\n"
				+ "Esim: " + MapTransformationUtils.YEAR + "=\"2019\".");
		
		return allowedParameters;
	}
	
	private static MapTable nextLevelTable(String year, Database database, OuterBox parent, double[] widths, int currentLevel) {
		MapTable result = new MapTable();
		int nextLevel = currentLevel + 1;
		if(currentLevel < widths.length) {
			result.widths.add(widths[currentLevel]);
			if(widths.length == (nextLevel)) {
				//This is the last level. Next level doesn't exist.
				result.columns = 1;
				for(InnerBox innerBox : parent.innerboxes) {
					result.cells.add(innerBox.getText(database));
				}
			} else {
				//Add next level too
				result.widths.add(MapTransformationUtils.widthRange(widths, nextLevel));
				result.columns = 2;
				for(InnerBox innerBox : parent.innerboxes) {
					result.cells.add(innerBox.getText(database));
					
					Collection<MapBase> extra = UtilsDB.getDirectImplementors(database, innerBox, year);
					if(extra.size() == 1) {
						Base ob = extra.iterator().next();
						if(ob != null && ob instanceof OuterBox) {
							result.cells.add(nextLevelTable(year, database, (OuterBox)ob, widths, nextLevel));
						} else {
							System.err.println("Null on non-outerbox");
						}
					}
				}
			}
		} else {
			System.err.println("IndexOutOfBounds in nextLevelTable! " + nextLevel);
		}
		
		return result;
	}
	

	public static MapTable buildTable(Database database, StrategyMap map, Attributes attributes) {
		
		String year = "";
		
		if(attributes.hasKey(MapTransformationUtils.YEAR)) {
			String value = attributes.get(MapTransformationUtils.YEAR);
			try {
				if((Integer.parseInt(value)+"").equals(value)){
					year = value;
				} else {
					throw new MapTransformException("V‰‰rin m‰‰riteltyj‰ vuosi!");
				}
			} catch (NumberFormatException e) {
				throw new MapTransformException("V‰‰rin m‰‰riteltyj‰ vuosi!");
			}

		}
		
		Collection<Base> levels = StrategyMap.availableLevels(database);
		double[] widths = MapTransformationUtils.parseWidths(attributes, levels.size());

		MapTable result = new MapTable();
		result.widths.add(widths[0]);
		result.widths.add(MapTransformationUtils.widthRange(widths, 1));
		
		result.columns = 2;
		for(OuterBox t : map.outerBoxes) {
			result.cells.add(map.currentLevel(database).text + ": " + t.getText(database));
			result.cells.add(nextLevelTable(year, database, t, widths, 1));
		}
		
		return result;

	}

	@Override
	public String replace(Database database, StrategyMap map, Attributes attributes) {
		MapTable table = buildTable(database, map, attributes);
		String tableHTML = MapTransformationUtils.createTable(table, false);
		//System.out.println(MapTransformationUtils.tableToCSV(table));
		return tableHTML;
	}

}
