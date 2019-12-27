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
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TimeInterval;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

/**
 * Make this its own service available at runtime at some point
 * Currently importable directly to the main project and used as an external project
 * @author Miro Eklund
 *
 */
public class MapTransformationUtils {

	private static ArrayList<HTMLMapTransform> mapTransforms = new ArrayList<HTMLMapTransform>();
	
	public static final double TABLE_MAX_WIDTH = 170; //mm
	
	public static final String YEAR = "vuosi";
	public static final String ID = "id";
	public static final String COLUMNS ="sarakkeet";
	
	public static List<HTMLMapTransform> getAvailableMapTransforms(){
		return mapTransforms;
	}
	
	/**
	 * Find all tags available by this map transformer
	 * @return
	 */
	public static List<String> getAvailableTags(){
		List<String> result = new ArrayList<>();
		for(HTMLMapTransform transform : mapTransforms) {
			result.add(transform.tag());
		}
		return result;
	}
	
	static {

		mapTransforms.add(new MapTransformPageBreak());
		
		mapTransforms.add(new MapTransformLVMChapter5());

		mapTransforms.add(new MapTransformLVMAppendix1());

		mapTransforms.add(new MapTransformChangeSuggestions());

		mapTransforms.add(new MapTransformOfficeChapter5());

		mapTransforms.add(new MapTransformOfficeAppendix1());
		
		mapTransforms.add(new MapTransformEntireMap());

	}

	static String createTable(MapTable t) {
		return createTable(t, true);
	}
	
	public static String mapToCSV(Database database, StrategyMap map, String year, String separator) {
		Attributes attributes = new Attributes();
		attributes.put(MapTransformationUtils.YEAR, year);
		MapTable table = MapTransformEntireMap.buildTable(database, map, attributes);
		return tableToCSV(table, separator); 
	}
	
	public static String tableToCSV(MapTable t, String separator) {
		return tableToCSV(t, separator, 0);
	}
	
	private static String tableToCSV(MapTable t, String separator,  int level) {
		int nextLevel = level + 1;
		String result = "";
		int rows = t.cells.size();
		if(t.columns != 0) {
			rows = rows / t.columns;
		}
		
		int index = 0;
		
		for(int r = 0; r < rows; r++) {
			for(int i=0;i<t.columns;i++) {
				Object cell = t.cells.get(index++);
				if(cell instanceof String) {
					String prefix = "";
					for(int p = 0; p < level; p++) {
						prefix += separator + " ";
					}
					result += prefix + cell.toString().replaceAll(separator, "    ");
					result += "\n";
				} else {
					result += tableToCSV((MapTable)cell, separator, nextLevel);
				}
			}
		}
		return result;
	}
	
	static String createTable(MapTable t, boolean hideBorders) {

		double width = 0;
		for(double w : t.widths)
			width += w;
		
		String possibleHeight = "";
		if(!"".equals(t.height))
			possibleHeight = "height:" + t.height + ";";

		String possibleHiddenBorders = hideBorders ? "border-style:hidden;" : "";
		String result = "<table style=\"" + possibleHiddenBorders + possibleHeight + "border-collapse:collapse;border-spacing:0px;width:" + width + t.widthUnit + "\">";
		for(double w : t.widths) {
			result += "<col width=\"" + w + t.widthUnit + "\">";
		}
		
		int rows = t.cells.size();
		if(t.columns != 0) {
			rows = rows / t.columns;
		}
		
		int index = 0;
		
		String paddingRules = "padding-left: 0mm; padding-right: 3mm;";
		
		for(int r = 0; r < rows; r++) {
			if(r < t.heights.size())
				result += "<tr style=\"height:" + t.heights.get(r) + ";\">";
			else
				result += "<tr>";
			for(int i=0;i<t.columns;i++) {

				Object cell = t.cells.get(index++);
				if(cell instanceof String) {
					result += "<td style=\"transform:translateX(2mm);padding: 0px; " + paddingRules + "\">";
					result += cell.toString();
				} else {
					result += "<td style=\"padding: 0px; border: 1px solid;\">";
					result += createTable((MapTable)cell, true);
				}
				result += "</td>";
			}
			result += "</tr>";
		}
		result += "</table>";
		return result;
	}



	
	/*static String createTable(MapTable t) {

		int width = 0;
		for(Integer w : t.widths)
			width += w;

		String widthUnit = t.widthUnit;
		String result = "<table style=\"height:100%;border-collapse:collapse;border-spacing:0px;width:" + width + widthUnit + "\">";
		for(Integer w : t.widths)
			result += "<col width=\"" + w + widthUnit + "\">";
		int rows = t.cells.size() / t.columns;
		int index = 0;
		for(int r = 0; r < rows; r++) {
			result += "<tr>";
			for(int i=0;i<t.columns;i++) {
				result += "<td style=\"padding: 0px; border: 1px solid\">";
				Object cell = t.cells.get(index++);
				if(cell instanceof String) {
					result += cell.toString();
				} else {
					result += createTable((MapTable)cell);
				}
				result += "</td>";
			}
			result += "</tr>";
		}
		result += "</table>";
		return "<div>" +  result + "</div>";
	}*/

	/**
	 * 
	 * @param html
	 * @param main
	 * @param office
	 * @return
	 */
	public static String htmlTagReplacement(String html, Database database) {
		Document doc = Jsoup.parse(html);
		for(HTMLMapTransform t : mapTransforms) {
			String tag = t.tag();
			
			//Find all elements with a tag that matches
			for(Element element : doc.body().getAllElements()) {
				String prefix = element.tagName();
				if(prefix.equals(tag)) {
					Attributes attributes = element.attributes();
					try {
						String replacement = t.replace(database, database.getRoot(), attributes);
						 //Replace the entire tag and key-value pairs with the replacement HTML String
						html = html.replace(element.toString(), replacement);
					} catch (MapTransformException e) {
						html = html.replace(element.toString(), e.getMessage());
					}
				}
			}
		}
		return html;
	}

	public static StrategyMap child(Database database, StrategyMap parent, String name) {
		for(Linkki l : parent.alikartat) {
			StrategyMap child = database.find(l.uuid);
			if(child.getText(database).equals(name)) return child;
		}
		return null;
	}


	public static StrategyMap extractOfficeMap(Database database, Attributes attributes) {
		String id = attributes.get(ID);
		if(id != null && (!id.equals(""))) {
			StrategyMap result = database.findByTypeAndId(id, StrategyMap.class);
			if(result != null) {
				return result;
			} else {
				List<StrategyMap> maps = StrategyMap.enumerate(database);
				List<StrategyMap> matching = new ArrayList<>();
				for(StrategyMap map : maps) {
					if(map.getText(database).equals(id)) {
						matching.add(map);
					}
				}
				
				if(matching.size() == 1) {
					return matching.get(0);
				}
				else {
					System.err.println("Failed to find unique Office StrategyMaps by matching ID: [" + id + "] with Map IDs and Text contents. Found " + matching.size() + " matching StrategyMaps.");
				}
			}
		}
		return child(database, database.getRoot(), Terminology.ROOT_OFFICE_NAME);
	}

	public static String extractYear(Database database, Attributes attributes) {
		String year = attributes.get(YEAR);
		if(year != null) {
			try {
				TimeInterval i = TimeInterval.parse(year);
				if(!TimeInterval.isAlways(i))
					return year;
			} catch (NumberFormatException e) {
				
			}
		}
		throw new MapTransformException("Vuosim‰‰ritys puuttuu esim. vuosi=\"2019\"");
	}
	
	static double[] parseWeights(Attributes attributes, int columns) {
		
		double[] result = new double[columns];
		for(int i=0;i<columns;i++) result[i] = 1.0;

		String spec = attributes.get(COLUMNS);
		if(spec != null) {
			String[] parts = spec.split(",");
			if(parts.length == columns) {
				for(int i=0;i<columns;i++) {
					try {
						double d = Double.parseDouble(parts[i]);
						if(d>0) result[i] = d;
						else result[i] = 1.0;
					} catch (NumberFormatException e) {
						result[i] = 1.0;
					}
				}
			}
		}

		return result;
		
	}

	public static double[] parseWidths(Attributes attributes, int columns) {
		
		double[] weights = parseWeights(attributes, columns);
		
		double total = 0;
		for(double d : weights) total += d;
		
		double[] widths = new double[weights.length];

		for(int i=0;i<widths.length;i++) {
			double w = weights[i];
			widths[i] = MapTransformationUtils.TABLE_MAX_WIDTH * w / total;
		}
		
		return widths;
		
	}
	
	public static double widthRange(double[] widths, int start, int end) {
		double result = 0;
		for(int i=start;i<end;i++) {
			result += widths[i];
		}
		return result;
	}

	public static double widthRange(double[] widths, int start) {
		return widthRange(widths, start, widths.length);
	}
	
	public static double widthRange(double[] widths) {
		return widthRange(widths, 0, widths.length);
	}

}
