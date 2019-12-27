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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.UtilsMeters;
import fi.semantum.strategia.UtilsTags;
import fi.semantum.strategia.contrib.ContribVisUtils;
import fi.semantum.strategia.contrib.ExtraVis;
import fi.semantum.strategia.contrib.InnerBoxVis;
import fi.semantum.strategia.contrib.MeterVis;
import fi.semantum.strategia.contrib.OuterBoxVis;
import fi.semantum.strategia.contrib.TagVis;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.MeterDescription;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Pair;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.TimeInterval;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDBComments;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;

public class VisUtils {


	
	public static OuterBoxVis defaultOuterBoxVis(Database database, Account account, UIState uistate, StrategyMap map, OuterBox outerBox, int index) {
		
		OuterBoxVis result = new OuterBoxVis();

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = outerBox.hasRelatedTag(database, voimavarat);
		
		result.columns = outerBox.columns;
		
		result.uuid = outerBox.uuid;
		
		result.id = map.outerDescription;
		TimeInterval ti = TimeInterval.parse(uistate.getTime());
		boolean showTimes = ti.startYear != ti.endYear;
		
		if(showTimes) {
			Property aika = Property.find(database, Property.AIKAVALI);
			String a = aika.getPropertyValue(outerBox);
			result.id += " " + a;
		}
		
		result.text = outerBox.text;
		result.color = map.outerColor;
		result.realIndex = index;
		
		if(isVoimavarat) {
			result.id = "";
			result.text = "Voimavarat";
		}
		
		try {
			StrategyMap k = outerBox.getPossibleImplementationMap(database);
			result.drill = (k != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<InnerBoxVis> childInnerBoxes = new ArrayList<InnerBoxVis>();
		
		Property aika = Property.find(database, Property.AIKAVALI);
		
		if(uistate.showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : uistate.shownTags) {
				double value = computeCoverage(database, uistate, monitorTag, outerBox);
				if(value > 0) tagNames.add(new TagVis(monitorTag, value));
			}
			result.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
		for(int i=0;i<outerBox.innerboxes.length;i++) {
			InnerBox innerBox = outerBox.innerboxes[i];
			String a = aika.getPropertyValue(innerBox);
			if(a != null) {
				if(uistate.acceptTime(a))
					childInnerBoxes.add(defaultInnerBoxVis(database, account, uistate, map, outerBox, 0, showTimes, result.realIndex, innerBox, i));
			} else {
				childInnerBoxes.add(defaultInnerBoxVis(database, account, uistate, map, outerBox, 0, showTimes, result.realIndex, innerBox, i));
			}
		}

		result.innerboxes = childInnerBoxes.toArray(new InnerBoxVis[childInnerBoxes.size()]);
		
    	Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
       	Pair p = implementsRelation.getPossibleRelation(outerBox);
       	result.copy = p != null;
       	
       	String content = outerBox.getText(database);
       	if(content.isEmpty()) content = outerBox.getId(database);

       	result.text = content;
       	
       	result.stripes = map.linkWithParent && !result.copy;
       	
       	if(database.getRoot().equals(map))
       		result.stripes = false;
       	
       	if(uistate.showDetailedMapMeters) {

           	boolean forecast = uistate.forecastMeters;

       		if(uistate.useImplementationMeters) {
       			
       			Meter m =  UtilsMeters.getPrincipalMeter(database, uistate, outerBox, "", forecast);
       			if(m.isPrincipal) {
       				result.meters = new MeterVis[2];
           			Meter imp = UtilsMeters.getImplementationMeter(database, uistate, outerBox, "", forecast);
           			double value = UtilsMeters.value(imp, database, forecast);
           			result.meters[0] = defaultMeterVis("Arvio", UtilsMeters.getTrafficColor(m, database), result.realIndex, -1, 0, "");
           			result.meters[1] = defaultMeterVis("" + (int)(100.0*value) + "%", UtilsMeters.getTrafficColor(imp, database), result.realIndex, -1, 1, "");
       			} else {
       				result.meters = new MeterVis[1];
           			double value = UtilsMeters.value(m, database, forecast);
           			String id = "" + (int)(100.0*value) + "%";
           			String color = UtilsMeters.getTrafficColor(m, database, forecast);
           			result.meters[0] = defaultMeterVis(id, color, result.realIndex, -1, 0, "");
       			}
       		} else {
       			List<MeterDescription> descs = UtilsMeters.makeMeterDescriptions(database, uistate, outerBox, true);
       			result.meters = new MeterVis[descs.size()];
        		for(int i=0;i<descs.size();i++) {
        			MeterDescription desc = descs.get(i);
        			Meter m = desc.meter;
        			String color = UtilsMeters.getTrafficColor(m, database, forecast);
        			String id = desc.meter.getId(database);
        			if(id.isEmpty()) id = desc.numbering;
        			result.meters[i] = defaultMeterVis(id, color, result.realIndex, -1, i, "");
        		}
       		}
			
       	}
       	
       	if(uistate.commentLayoutOpen) {
       		Tuple<Boolean, Boolean> changeSuggestionTuple = computeHasChangeSuggestions(database, account, uistate, outerBox);
       		result.childElementsHaveChangeSuggestions = changeSuggestionTuple.x;
       		result.hasChangeSuggestions = changeSuggestionTuple.y;
       	}
       	
		if(isVoimavarat) {
			result.startNewRow = true;
			result.xOffset = 0.5;
			result.yOffset = 10;
			result.color = "#11BB11";
			result.stripes = false;
		}
		
		Base possibleParent = UtilsDB.getPossibleRootParentImplementation(database, outerBox);
		if(possibleParent != null) {
			result.parentImplements = possibleParent.getText(database);
		}
		
		return result;
		
	}
	
	public static class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
	} 
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param uistate
	 * @param outerBox
	 * @return Tuple<B, B>. x = children have CS. y = current element has CS.
	 */
	private static Tuple<Boolean, Boolean> computeHasChangeSuggestions(Database database, Account account, UIState uistate, OuterBox outerBox) {
		
		Set<String> currentMapElementUUIDs = new HashSet<>();
		
		Base target = MapBase.findTargetedMapBoxElement(database, outerBox);
		if(target != null) {
			currentMapElementUUIDs.add(target.uuid);
		}
		currentMapElementUUIDs.add(outerBox.uuid);
		Set<ChangeSuggestion> currentMapElementChangeSuggestions = ChangeSuggestion.changeSuggestionsByTargetUUIDs(database,
				database.getRoot(),
				currentMapElementUUIDs);
		
		Set<ChangeSuggestion> changeSuggestionsByTargetUUIDsRecursive = UtilsDBComments.changeSuggestionsInOuterBoxRecursiveSearch(database,
				uistate,
				outerBox);
		
		//Ensure the two sets are unique by removing those CS targeting the current map element from the recursive child-set:
		changeSuggestionsByTargetUUIDsRecursive.removeAll(currentMapElementChangeSuggestions);
		
		List<ChangeSuggestion> allVisibleCurrentMapElement = UtilsDBComments.filterChangeSuggestionVisibleToAccount(database,
				account,
				currentMapElementChangeSuggestions,
				ChangeSuggestion.ChangeSuggestionState.OPEN_ACTIVE);

		List<ChangeSuggestion> allVisibleRecursive = UtilsDBComments.filterChangeSuggestionVisibleToAccount(database,
				account,
				changeSuggestionsByTargetUUIDsRecursive,
				ChangeSuggestion.ChangeSuggestionState.OPEN_ACTIVE);
		
		//If the filtered list of ChangeSuggestions that target the box or its immediate children isn't empty, return true
		Tuple<Boolean, Boolean> result = new Tuple<Boolean, Boolean>(!allVisibleRecursive.isEmpty(), !allVisibleCurrentMapElement.isEmpty());
		return result;
	}
	
	
	static ExtraVis defaultExtraVis(String id, String text, String desc, String color, int tavoite, int painopiste, int realIndex, String link) {
		ExtraVis result = new ExtraVis();
		result.id = id;
		result.text = text;
		result.desc = desc;
		result.color = color;
		result.tavoite = tavoite;
		result.painopiste = painopiste;
		result.realIndex = realIndex;
		result.link = link;
		return result;
	}


	
	static MeterVis defaultMeterVis(String text, String color, int tavoite, int painopiste, int realIndex, String link) {
		return defaultMeterVis(text, text, color, tavoite, painopiste, realIndex, link);
	}

	static MeterVis defaultMeterVis(String text, String desc, String color, int tavoite, int painopiste, int realIndex, String link) {
		MeterVis result = new MeterVis();
		result.text = text;
		result.desc = desc;
		result.color = color;
		result.tavoite = tavoite;
		result.painopiste = painopiste;
		result.realIndex = realIndex;
		result.link = link;
		return result;
	}
	
	public static MeterVis fromMeterVis(Database database, Meter m, boolean showDetailedMapMeters, boolean forecast, boolean allowLinks, int tavoite, int realIndex, int i) {
		String color = showDetailedMapMeters ? UtilsMeters.getTrafficColor(m, database, forecast) : "#aaaaaa";
		String possibleLink = allowLinks ? m.link : "";
		String id = m.getId(database);
		String desc = UtilsMeters.getVerboseDescription(m, database, forecast);
		if(id.isEmpty()) id = UtilsMeters.getDescription(m, database, forecast);
		if(id.isEmpty()) id = "" + (i+1);
		return defaultMeterVis(id, desc, color, tavoite, realIndex, i, possibleLink);
	}
	
	public static InnerBoxVis defaultInnerBoxVis(Database database, Account account, UIState uistate, StrategyMap map, OuterBox t, int level, boolean showTimes, int tavoiteIndex, InnerBox p, int realIndex) {
		
		InnerBoxVis result = new InnerBoxVis();
		CharacterInfo ci = map.getCharacterInfo(database);

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		boolean isVoimavarat = t.hasRelatedTag(database, voimavarat);

		Map<String,String> resp = UtilsDB.getResponsibilityMap(database, p);
		result.hasInfo = !resp.isEmpty();
		
		result.canDrill = StandardVisRegistry.getInstance().hasMapVis(database, account, uistate, p);
		
		result.columns = p.columns;
		
		result.hasMeter = ci.hasMeter && uistate.showDetailedMapMeters;
		
		result.uuid = p.uuid;
		
		result.id = map.innerDescription;

		if(showTimes) {
			Property aika = Property.find(database, Property.AIKAVALI);
			String a = aika.getPropertyValue(p);
			if(a != null && a.length() > 0)
				result.id = result.id + " " + a;
		}
		
		result.text = p.text;
		result.color = map.innerColor;
		result.tavoite = tavoiteIndex;
		result.realIndex = realIndex;
		
		if(uistate.showTags) {
			
			List<TagVis> tagNames = new ArrayList<TagVis>();
			for(Tag monitorTag : uistate.shownTags) {
				double value = computeCoverage(database, uistate, monitorTag, p);
				if(value > 0) tagNames.add(new TagVis(monitorTag, value));
			}
			result.tags = tagNames.toArray(new TagVis[tagNames.size()]);
			
		}
		
		boolean showDetailedMapMeters = uistate.showDetailedMapMeters;
		
		boolean meterForecast = uistate.forecastMeters;

		if(showDetailedMapMeters) {
			if(result.hasMeter) {
				result.meters = new MeterVis[0];
				if(showDetailedMapMeters) {
					MeterVis[] vis = computeImplementationMeters(database, uistate, t, p, showDetailedMapMeters, result.tavoite, realIndex);
					if(vis.length == 1) {
						result.leafMeterColor = vis[0].color;
						result.leafMeterDesc = vis[0].desc;
						result.leafMeterPct = vis[0].text;
					} else {
						result.leafMeterColor = "#0f0";
					}
				} else {
					result.leafMeterColor = InnerBoxVis.GREY;
				}
			} else if(uistate.useImplementationMeters) {
				Meter pm = UtilsMeters.getPossiblePrincipalMeterActive(database, uistate, p);
				if(pm != null) {
					String color = UtilsMeters.getTrafficColor(pm, database, meterForecast);
					double value = UtilsMeters.value(pm, database, meterForecast);
					String id = "Arvio: " + (int)(100.0*value) + "%";
					result.meters = new MeterVis[] { defaultMeterVis(id, id, color, result.tavoite, realIndex, 0, "") };
				} else {
					result.meters = computeImplementationMeters(database, uistate, t, p, showDetailedMapMeters, result.tavoite, realIndex);
				}

			} else {

				List<MeterDescription> descs = UtilsMeters.makeMeterDescriptions(database, uistate, p, true);
				result.meters = new MeterVis[descs.size()];
				for(int i=0;i<descs.size();i++) {
					MeterDescription desc = descs.get(i);
					Meter m = desc.meter;
					String color = UtilsMeters.getTrafficColor(m, database, meterForecast);
					String id = desc.meter.getId(database);
					if(id.isEmpty()) {
						double value = UtilsMeters.value(m, database, meterForecast);
						id = "" + (int)(100.0*value) + "%";
					}
					result.meters[i] = defaultMeterVis(id, id, color, result.tavoite, realIndex, i, m.link);
				}
			}
		}
		
		if(ci.amountOfLevels > 2 && level == 0) {

			Collection<MapBase> extra = UtilsDB.getDirectImplementors(database, p, uistate.getTime());
			if(extra.size() == 1) {
				Base e = extra.iterator().next();
				ArrayList<ExtraVis> es = new ArrayList<ExtraVis>();
				if(e instanceof OuterBox) {
					OuterBox o = (OuterBox)e;
					StrategyMap oMap = o.getMap(database);
					oMap.prepare(database);
					for(int index = 0; index < o.innerboxes.length; index++) {
						InnerBox i = o.innerboxes[index];
						if(UtilsDB.isActive(database, uistate.getTime(), i)) {
							InnerBoxVis v = defaultInnerBoxVis(database, account, uistate, oMap, o, level+1, showTimes, tavoiteIndex, i, realIndex);
							es.add(defaultExtraVis(v.id, v.text, "", v.color, v.tavoite, v.realIndex, index, ""));
						}
					}
					result.extras = es.toArray(new ExtraVis[es.size()]);
				}
			}

		}
       	
       	if(isVoimavarat)
       		result.color = "#44ee44";
       	
       	return result;

	}
	
	private static MeterVis[] computeImplementationMeters(Database database, UIState uistate, OuterBox t, InnerBox p, boolean showDetailedMapMeters, int tavoite, int realIndex) {
		
		boolean forecast = uistate.forecastMeters;

		boolean allowLinks = !t.hasImplementationSubmap(database);
		
		ArrayList<MeterVis> meterList = new ArrayList<MeterVis>();
		List<Meter> descs = UtilsMeters.getImplementationMeters(database, uistate, p, forecast);
		if(descs.isEmpty()) {
			Meter prin = UtilsMeters.getPrincipalMeter(database, uistate, p, "", forecast);
			if(showDetailedMapMeters || allowLinks) {
				meterList.add(fromMeterVis(database, prin, showDetailedMapMeters, forecast, allowLinks, tavoite, realIndex, 0));
			}
		} else {
			for(int i=0;i<descs.size();i++) {
				Meter m = descs.get(i);
				if(showDetailedMapMeters || allowLinks) {
					meterList.add(fromMeterVis(database, m, showDetailedMapMeters, forecast, allowLinks, tavoite, realIndex, i));
				}
			}
		}

		return meterList.toArray(new MeterVis[meterList.size()]);
		
	}
	
	public static double computeCoverage(Database database, UIState uistate, Tag monitorTag, MapBase b) {
		double value = UtilsTags.getCoverage(database, uistate, monitorTag, b);
		if(value > 0) {
			return 1.0;
		} else {
			for(MapBase b2 : ContribVisUtils.getImplementationSet(database, b)) {
				if(b2.getRelatedTags(database).contains(monitorTag)) {
					return 1.0;
				}
				if(b2 instanceof InnerBox) {
					OuterBox t2 = ((InnerBox)b2).getGoal(database);
					if(t2.getRelatedTags(database).contains(monitorTag)) {
						return 1.0;
					}
				}
			}
		}
		return 0.0;
	}
	
	
}
