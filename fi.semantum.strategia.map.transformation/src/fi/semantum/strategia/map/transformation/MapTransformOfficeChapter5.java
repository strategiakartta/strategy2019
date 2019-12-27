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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Attributes;

import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

public class MapTransformOfficeChapter5 implements HTMLMapTransform {
	
	private static final String TAVOITE_TASO_VUODET = "tavoiteTasoVuodet";
	
	@Override
	public String tag() {
		return "virasto-luku-5";
	}

	@Override
	public Map<String, String> possibleParametersGuide(Database database){
		List<Office> offices = Office.enumerate(database, true, false);
		List<String> officeIDs = new ArrayList<>(offices.size());
		for(Office o : offices) {
			officeIDs.add(o.getShortId(database));
		}
		
		Map<String, String> allowedParameters = new HashMap<>();
		allowedParameters.put(MapTransformationUtils.ID, "Anna viraston lyhytnimi. Hakee vain kyseisen viraston tiedot.\n"
				+ "Esim: " + MapTransformationUtils.ID + "=\"LVM\".\n"
				+ "Virastojen lyhytnimet:" + officeIDs);
		allowedParameters.put(TAVOITE_TASO_VUODET,
				"Ota taulukkoon mukaan n‰iden vuosien tavoitetasot.\n"
				+ "Esim: " + TAVOITE_TASO_VUODET + "=\"2017,2018,2019\".\n");
		return allowedParameters;
	}
	
	MapTable header(double[] widths, List<String> years) {

		MapTable result = new MapTable();
		result.columns = 3 + years.size();
		for(int i=0; i<(result.columns); i++) {
			result.widths.add(widths[i]);
		}
		
		result.cells.add("Painopiste");
		result.cells.add("Tulostavoite");
		result.cells.add("Mittari");
		
		for(String y : years) {
			result.cells.add("Tavoitetaso " + y);
		}

		return result;

	}

	MapTable tavoitetasot(Database database, InnerBox pp, double[] widths, List<String> years) {
		MapTable result = new MapTable();
		result.columns = years.size();
		Map<String, ArrayList<InnerBox>> innerBoxMapList = new HashMap<>();
		for(int i = 0; i < years.size(); i++) {
			double w = widths[3 + i];
			result.widths.add(w);
			innerBoxMapList.put(years.get(i), new ArrayList<InnerBox>());
		}

		OuterBox t  = pp.getGoal(database);

		try {
			StrategyMap imp = t.getPossibleImplementationMap(database);
			for(OuterBox t2 : imp.outerBoxes) {
				if(UtilsDB.doesImplement(database, t2, pp)) {
					for(InnerBox pp2 : t2.innerboxes) {
						String validity = UtilsDB.getValidity(database, pp2);
						ArrayList<InnerBox> innerBoxesForYear = innerBoxMapList.get(validity);
						if(innerBoxesForYear != null) {
							innerBoxesForYear.add(pp2);
							innerBoxMapList.put(validity, innerBoxesForYear);
						}
					}
				}
			}

			for(String y : years) {
				ArrayList<InnerBox> innerBoxesForYear = innerBoxMapList.get(y);
				if(innerBoxesForYear != null) {
					String newEntry = "<ul style=\"padding-inline-start:3mm\">";
					for(InnerBox p : innerBoxesForYear) {
						newEntry += "<li>" + p.getText(database) + "</li>";
					}
					newEntry += "</ul>";
					result.cells.add(newEntry);
				} else {
					System.err.println("No list for year " + y);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	MapTable mittari(Database database, InnerBox pp, double[] widths, List<String> years) {

		MapTable result = new MapTable();
		result.widths.add(widths[2]);
		result.widths.add(MapTransformationUtils.widthRange(widths, 3));
		result.columns = 2;
		OuterBox t  = pp.getGoal(database);
		try {
			StrategyMap imp = t.getPossibleImplementationMap(database);
			if(imp != null) {
				for(OuterBox t2 : imp.outerBoxes) {
					if(UtilsDB.doesImplement(database, t2, pp)) {
						for(InnerBox pp2 : t2.innerboxes) {
							result.cells.add(pp2.getText(database));
							result.cells.add(tavoitetasot(database, pp2, widths, years));
						}
					}
				}
			} else {
				System.err.println("Null implementationMap for OuterBox (uuid: " + t.uuid + ") text: " + t.getText(database));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	MapTable tulostavoite(Database database, StrategyMap map, InnerBox pp, double[] widths, List<String> years) {

		MapTable result = new MapTable();
		result.widths.add(widths[1]);
		result.widths.add(MapTransformationUtils.widthRange(widths, 2));
		result.columns = 2;
		for(OuterBox t : map.outerBoxes) {
			if(UtilsDB.doesImplement(database, t, pp)) {
				for(InnerBox pp2 : t.innerboxes) {
					result.cells.add(pp2.getText(database));
					result.cells.add(mittari(database, pp2, widths, years));
				}
			}
		}
		return result;

	}

	MapTable strateginen(Database database, OuterBox t, StrategyMap map, double[] widths, List<String> years) {

		MapTable result = new MapTable();
		result.widths.add(widths[0]);
		result.widths.add(MapTransformationUtils.widthRange(widths, 1));
		result.columns = 2;
		for(InnerBox pp : t.innerboxes) {
			result.cells.add(pp.getText(database));
			result.cells.add(tulostavoite(database, map, pp, widths, years));
		}
		return result;

	}

	MapTable buildTable(Database database, StrategyMap map, Attributes attributes) {
		List<String> years = new ArrayList<>();
		
		if(attributes.hasKey(TAVOITE_TASO_VUODET)) {
			String value = attributes.get(TAVOITE_TASO_VUODET);
			String[] possibleYears = value.split(",");
			for(String y : possibleYears) {
				try {
					if((Integer.parseInt(y)+"").equals(y)){
						years.add(y);
					} else {
						throw new MapTransformException("V‰‰rin m‰‰riteltyj‰ vuosia!");
					}
				} catch (NumberFormatException e) {
					throw new MapTransformException("V‰‰rin m‰‰riteltyj‰ vuosia!");
				}
			}
		}
		
		StrategyMap possibleOfficeMap = MapTransformationUtils.extractOfficeMap(database, attributes);

		double[] widths = MapTransformationUtils.parseWidths(attributes, 3 + years.size());

		MapTable result = new MapTable();
		result.widths.add(MapTransformationUtils.widthRange(widths));
		result.columns = 1;

		if(possibleOfficeMap == null) {
			result.cells.add(Terminology.OFFICE_MISSING);
		} else {
			result.cells.add(header(widths, years));
			for(OuterBox t : map.outerBoxes) {
				result.cells.add("STRATEGINEN TAVOITE: " + t.getText(database));
				result.cells.add(strateginen(database, t, possibleOfficeMap, widths, years));
			}
		}
		return result;

	}

	@Override
	public String replace(Database database, StrategyMap map, Attributes attributes) {
		
		return MapTransformationUtils.createTable(buildTable(database, map, attributes), false);

	}

}