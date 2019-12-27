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
package fi.semantum.strategy.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UIState implements Serializable {

	private static final long serialVersionUID = -5997513543643591132L;
	
	public static final int MAP = 0;
	public static final int BROWSER = 1;
	public static final int WIKI = 2;
	public static final int PROPERTIES = 3;

	public String name;
	public String time;
	
	public boolean showSelectedDocuments;
	
	public MapBase customVis;
	public StrategyMap currentMap;
	public StrategyMap referenceMap;
	public MapBase currentMapItem;
	public Set<MapBase> requiredMapItems;
	public StrategyMap currentMapPosition;
	public int mapTabState;
	public boolean showTags = false;
	public List<Tag> shownTags = new ArrayList<Tag>();
	public boolean showDetailedMapMeters = false;
	public boolean useImplementationMeters = true;
	public boolean showMapVoimavarat = false;
	public boolean input = false;
	public boolean forecastMeters = true;
	public int mapLevel = 1;
	public String currentFilterName;
	public Map<String,BrowserNodeState> browserStates = new HashMap<String, BrowserNodeState>();
	public boolean reportAll = false;
	public Set<String> treeExpansion = new HashSet<String>();

	public boolean startPageVisible = false;
	public Office currentOffice;
	
	public boolean showEditor = true;
	public boolean showFullDocumentHTML = false;
	public boolean commentLayoutOpen = false;
	public boolean commentLayoutFullWindow = false;
	public boolean commentLayoutFilterMode = false;
	public boolean showDocumentPrintWindow = false;
	public boolean commentsOrderAscending = false;
	
	public UIState() {
		name = "";
		time = "-";
	}
	
	public void setTime(List<Integer> years) {
		if(years != null && years.size() != 0) {
			time = years.get(0).toString();
		}
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public void setCurrentOffice(Office office) {
		this.currentOffice = office;
	}
	
	public String getTime() {
		return time;
	}
	
	public Office getCurrentOffice() {
		return this.currentOffice;
	}
	
	public void setForecastMeters() {
		forecastMeters = true;
	}
	
	public void setActualMeters() {
		forecastMeters = false;
	}
	
	public StrategyMap getCurrentMap() {
		return currentMap;
	}
	
	public void setCurrentMap(StrategyMap map) {
		this.currentMap = map;
		customVis = null;
	}

	public void setCustomVis(MapBase base) {
		this.customVis = base;
	}
	
	public UIState duplicateI(int counter) {
		return duplicateS("s"+counter);
	}

	/**
	 * Remember to update this function if adding new fields to the UIState object!
	 * @param name
	 * @return
	 */
	public UIState duplicateS(String name) {
		UIState result = new UIState();
		result.name = name;
		result.customVis = customVis;
		result.currentMap = currentMap;
		result.forecastMeters = forecastMeters;
		result.referenceMap = referenceMap;
		result.time = time;
		result.currentMapItem = currentMapItem;
		result.requiredMapItems = requiredMapItems;
		result.currentMapPosition = currentMapPosition;
		result.mapTabState = mapTabState;
		result.mapLevel = mapLevel;
		result.currentFilterName = currentFilterName;
		result.showTags = showTags;
		result.shownTags = new ArrayList<Tag>(shownTags);
		result.showDetailedMapMeters = showDetailedMapMeters;
		result.showMapVoimavarat = showMapVoimavarat;
		result.browserStates = browserStates != null ? new HashMap<String, BrowserNodeState>(browserStates) : new HashMap<String, BrowserNodeState>();
		result.useImplementationMeters = useImplementationMeters;
		result.input = input;
		result.reportAll = reportAll;
		result.currentOffice = currentOffice;
		result.showEditor = showEditor;
		result.startPageVisible = startPageVisible;
		result.showFullDocumentHTML = showFullDocumentHTML;
		result.commentLayoutOpen = commentLayoutOpen;
		result.commentLayoutFullWindow = commentLayoutFullWindow;
		result.commentLayoutFilterMode = commentLayoutFilterMode;
		result.showDocumentPrintWindow = showDocumentPrintWindow;
		result.showSelectedDocuments = showSelectedDocuments;
		result.treeExpansion = treeExpansion;
		return result;
	}
	
	public boolean acceptTime(String t) {
		String ref = getTime();
		return UtilsDB.acceptTime(t, ref);
	}
	
	public boolean migrate() {
		if(shownTags == null) {
			shownTags = new ArrayList<Tag>();
			return true;
		}
		return false;
	}
	
}
