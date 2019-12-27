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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/*
 * Common base class for all objects in a database
 * 
 */
abstract public class MapBase extends Base {
	
	private static final long serialVersionUID = -4482518287842093370L;
	
	public String markup = "";
	
	public ArrayList<Indicator> indicators = new ArrayList<Indicator>();
	public List<Meter> meters = new ArrayList<Meter>();

	protected MapBase(String uuid, String id, String text) {
		super(uuid, id, text);
	}
	
	
	/**
	 * Find the map box element that should be targeted for a map-element.
	 * E.g: Pressing an OuterBox should target the implementing InnerBox, and pressing
	 * an innerbox should target it as-is.
	 * @param database
	 * @param base
	 * @return
	 */
	public static MapBase findTargetedMapBoxElement(Database database, MapBase base) {
		if(base instanceof OuterBox) {
			OuterBox ib = (OuterBox)base;
			Set<MapBase> impl = UtilsDB.getImmediateParentImplementationSet(database, ib);
			if(impl.isEmpty()) {
				return base;
			}
			else if (impl.size() == 1) {
				MapBase b = impl.iterator().next();
				return b;
			}
			else {
				System.err.println("More than one implementation as parent! Returning base as-is from findFirstMapBoxParent");
				return base;
			}
		} else {
			return base;
		}
	}
	
	public static List<MapBase> enumerateMapBase(Database database) {
		ArrayList<MapBase> result = new ArrayList<MapBase>();
		for(Base b : database.enumerate()) {
			if(b instanceof MapBase) result.add((MapBase)b);
		}
		return result;
	}

	public boolean canRemove(Database database) {
		Collection<MapBase> implementors = database.getInverse(this, Relation.find(database, Relation.IMPLEMENTS));
		return implementors.isEmpty();
	}

	@Override
	public void remove(Database database) {
		if(meters != null)
			for(Meter m : meters) m.remove(database);
		if(indicators != null)
			for(Indicator i : indicators) i.remove(database);
		for(StrategyMap map : StrategyMap.enumerate(database)) {
			boolean exists = false;
			for(Linkki l : map.alikartat) {
				if(l.uuid.equals(this.uuid)) {
					exists = true;
					break;
				}
			}
			if(exists) {
				map.removeAlikartta(this.uuid);
			}
		}
		database.remove(this);
	}
	
	public Map<String,String> searchMap(Database database) {
		HashMap<String,String> result = new HashMap<String,String>();
		result.put("Wiki", markup.toLowerCase());
		StringBuilder tagString = new StringBuilder();
		for(Tag t : getRelatedTags(database)) {
			tagString.append(" ");
			tagString.append(t.getId(database));
		}
		result.put("Aihetunnisteet", tagString.toString().toLowerCase());
		result.put("Sovellus", text.toLowerCase() + " " + id.toLowerCase());
		return result;
	}

	@Override
	public String searchText(Database database) {
		
		Map<String,String> map = searchMap(database);
		
		StringBuilder b = new StringBuilder();
		for(Map.Entry<String, String> entry : map.entrySet()) {
			b.append(" ");
			b.append(entry.getValue());
		}
		return b.toString();
		
	}
	
	public List<Tag> getRelatedTags(Database database) {
		Relation r = Relation.find(database, Relation.RELATED_TO_TAG);
		if(r == null) return Collections.emptyList();
		List<Tag> result = new ArrayList<Tag>(this.<Tag>getRelatedObjects(database, r));
		Collections.sort(result, tagComparator);
		return result; 
	}

	public List<Tag> getMonitorTags(Database database) {
		Relation r = Relation.find(database, Relation.MONITORS_TAG);
		if(r == null) return Collections.emptyList();
		List<Tag> result = new ArrayList<Tag>(this.<Tag>getRelatedObjects(database, r));
		Collections.sort(result, tagComparator);
		return result; 
	}
	
	public void removeRelatedTags(Database database, Tag ...tags) {
		for(Tag t : tags) {
			denyRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
			denyRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
		}
	}

	public List<MapBase> getOwners(Database database) {
		ArrayList<MapBase> result = new ArrayList<MapBase>();
		Base owner = this;
		while(owner != null && owner instanceof MapBase) {
			result.add((MapBase)owner);
			owner = owner.getOwner(database);
		}
		return result;
	}

	
	public void removeMonitorTags(Database database, Tag ...tags) {
		for(Tag t : tags)
			denyRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}
	
	public static Comparator<Tag> tagComparator = new Comparator<Tag>() {

		@Override
		public int compare(Tag o1, Tag o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.id, o2.id);
		}
		
	};
	
	
	public void setRelatedTags(Database database, Collection<Tag> newTags) {
		List<Tag> existing = getRelatedTags(database);
		for(Tag exist : existing)
			if(!newTags.contains(exist))
				removeRelatedTags(database, exist);
		assertRelatedTags(database, newTags);
	}
	
	public void assertRelatedTags(Database database, Collection<Tag> newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void assertRelatedTags(Database database, Tag ...newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), t);
	}

	public void assertMonitorTags(Database database, Collection<Tag> newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}
	
	public void assertMonitorTags(Database database, Tag ... newTags) {
		for(Tag t : newTags)
			assertRelation(database, Relation.find(database, Relation.MONITORS_TAG), t);
	}

	public boolean hasRelatedTag(Database database, Tag tag) {
		return hasRelation(database, Relation.find(database, Relation.RELATED_TO_TAG), tag);
	}

	public boolean hasMonitorTag(Database database, Tag tag) {
		return hasRelation(database, Relation.find(database, Relation.MONITORS_TAG), tag);
	}

	
	public List<Indicator> getIndicators(Database database) {
		return new ArrayList<Indicator>(indicators);
	}
	
	public void moveMetersUp(Collection<Meter> selectedMeters) {
		TreeMap<Integer,Meter> map = sortMeters(selectedMeters, 1);
		meters = new ArrayList<Meter>(map.values());
	}
	
	public void moveMetersDown(Collection<Meter> selectedMeters) {
		ArrayList<Meter> sel = new ArrayList<Meter>(selectedMeters);
		Collections.reverse(sel);
		TreeMap<Integer,Meter> map = sortMeters(sel, -1);
		meters = new ArrayList<Meter>(map.descendingMap().values());
	}
	
	private TreeMap<Integer,Meter> sortMeters(Collection<Meter> selectedMeters, int direction) {
		TreeMap<Integer,Meter> map = new TreeMap<Integer,Meter>();
		Map<Meter,Integer> map2 = new HashMap<Meter,Integer>();
		int index = 0;
		for(Meter m : meters) {
			map.put(index, m);
			map2.put(m, index);
			index += direction;
		}
		for(Meter m : selectedMeters) {
			Integer currentIndex = map2.get(m);
			if(currentIndex != null) {
				Meter m2 = map.get(currentIndex-1);
				if(m2 != null) {
					map.put(currentIndex, m2);
					map2.put(m2, currentIndex);
				} else {
					map.remove(currentIndex);
				}
				map.put(currentIndex-1, m);
				map2.put(m, currentIndex-1);
			}
		}
		return map;
	}

	public void moveIndicatorsUp(Collection<Indicator> selectedIndicators) {
		TreeMap<Integer,Indicator> map = sortIndicators(selectedIndicators, 1);
		indicators = new ArrayList<Indicator>(map.values());
	}
	
	public void moveIndicatorsDown(Collection<Indicator> selectedIndicators) {
		ArrayList<Indicator> sel = new ArrayList<Indicator>(selectedIndicators);
		Collections.reverse(sel);
		TreeMap<Integer,Indicator> map = sortIndicators(sel, -1);
		indicators = new ArrayList<Indicator>(map.descendingMap().values());
	}
	
	private TreeMap<Integer,Indicator> sortIndicators(Collection<Indicator> selectedIndicators, int direction) {
		TreeMap<Integer,Indicator> map = new TreeMap<Integer,Indicator>();
		Map<Indicator,Integer> map2 = new HashMap<Indicator,Integer>();
		int index = 0;
		for(Indicator m : indicators) {
			map.put(index, m);
			map2.put(m, index);
			index += direction;
		}
		for(Indicator m : selectedIndicators) {
			Integer currentIndex = map2.get(m);
			if(currentIndex != null) {
				Indicator m2 = map.get(currentIndex-1);
				if(m2 != null) {
					map.put(currentIndex, m2);
					map2.put(m2, currentIndex);
				} else {
					map.remove(currentIndex);
				}
				map.put(currentIndex-1, m);
				map2.put(m, currentIndex-1);
			}
		}
		return map;
	}

	public void addIndicator(Indicator indicator) {
		indicators.add(indicator);
	}
	
	public void removeIndicator(Indicator indicator) {
		indicators.remove(indicator);
	}
	
	public void addMeter(Meter meter) {
		meters.add(meter);
	}
	
	public void removeMeter(Meter meter) {
		meters.remove(meter);
	}

	public List<Meter> getMeters(Database database) {
		if(meters == null) {
			System.err.println("Meters is null for: " + getDebugText(database));
			return Collections.emptyList();
		}
		return new ArrayList<Meter>(meters);
	}
	
	public boolean modifyText(Database database, Account account, String text) {

		MapBase copy = getPossibleCopy(database);
		if(copy != null)
			return copy.modifyText(database, account, text);
		
		assert(text != null);

		if(!UtilsDB.canWrite(database, account, this))
			return false;
		
		if(!text.equals(this.text)) {
			modified(database, account);
			this.text = text;
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}

	public boolean modifyDescription(Database database, Account account, String text) {
		
		assert(text != null);
		

		if(!UtilsDB.canWrite(database, account, this))
			return false;
		
		if(!text.equals(this.description)) {
			modified(database, account);
			this.description = text;
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}

	public boolean modifyId(final Database database, Account account, String id) {
		
		assert(id != null);
		
		if(!UtilsDB.canWrite(database, account, this))
			return false;
		
		if(!id.equals(getId(database))) {
			modified(database, account);
			setId(database, id);
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
		
	}
	
	public boolean modifyMarkup(final Database database, Account account, String markup) {
		
		if(!UtilsDB.canWrite(database, account, this))
			return false;
		
		if(!this.markup.equals(markup)) {
			modified(database, account);
			this.markup = markup;
			try {
				Lucene.set(database.getDatabaseId(), uuid, searchText(database));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public String getId(Database database) {
		return id;
	}
	
	@Override
	public String getText(Database database) {
		Base copy = getPossibleCopy(database);
		if(copy != null) return copy.getText(database);
		return text;
	}
	
	public MapBase getPossibleCopy(Database database) {
		Relation copy = Relation.find(database, Relation.COPY);
		Pair p = copy.getPossibleRelation(this);
		if(p == null) return null;
		return database.find(p.second);
	}
	
	public boolean isCopy(Database database) {
		Relation copy = Relation.find(database, Relation.COPY);
		return copy.hasRelations(this);
	}
	
	public void synchronizeCopy(final Database database) {
		MapBase copy = getPossibleCopy(database);
		if(copy == null) return;
		copy.synchronizeCopy(database);
		setId(database, copy.getId(database));
		modifyText(database, database.getDefaultAdminAccount(), copy.getText(database));
	}

	public StrategyMap getMap(Database database) throws RuntimeException {
		return database.getMap(this);
	}
	
	public StrategyMap getPossibleMap(Database database) {
		return database.getPossibleMap(this);
	}
	

	public void denyRelation(Database database, Relation r, Base b) {
		relations.remove(new Pair(r.uuid, b.uuid));
	}
	
	final public void accept(MapVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public Base getBase() {
		return this;
	}

	public boolean isLeaf(Database database, String requiredValidityPeriod) {
		Collection<MapBase> imps = UtilsDB.getDirectImplementors(database, this, requiredValidityPeriod);
		return imps.isEmpty();
	}
	
	public MapBase hasLeaf(Database database, String requiredValidityPeriod) {

		Collection<MapBase> imps = UtilsDB.getDirectImplementors(database, this, requiredValidityPeriod);
		if(imps.isEmpty()) return this;
		if(imps.size() > 1) return null;
		MapBase imp = imps.iterator().next();
		
		return imp.hasLeaf(database, requiredValidityPeriod);
		
	}
	
	public MapBase getImplemented(Database database) {

		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> bases = getRelatedObjects(database, implementsRelation);
		if(bases.size() == 1) {
			Base b = bases.iterator().next();
			if(b instanceof MapBase) {
				return (MapBase)b;
			} else {
				System.err.println("Got non-MapBase type in getImplemented. Expected MapBase!");
				return null;
			}
		}
		if(bases.size() > 1) throw new IllegalStateException("Implements multiple!");
		return null;
		
	}
	
	public MapBase getPossibleImplemented(Database database) {
		try {
			return getImplemented(database);
		} catch (IllegalStateException e) {
			return null;
		}
	}
	
	public void removeRecursive(Database database) {
		for(MapBase imp : UtilsDB.getDirectImplementors(database, this)) {
			imp.removeRecursive(database);
		}
		remove(database);
	}
	
}

