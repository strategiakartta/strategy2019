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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OuterBox extends MapBase implements MapBox {

	private static final long serialVersionUID = 2404810943743110180L;

	public InnerBox[] innerboxes = new InnerBox[0];
	public int columns = 2;
	public int rows = 0;
	public int extraRows = 0;
	public boolean copy;

	protected OuterBox(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}

	public static OuterBox create(Database database, StrategyMap map, String text) {
		return create(database, map, "", text);
	}

	public static OuterBox createTransient(Database database, StrategyMap map, String id, String text) {

		OuterBox p = new OuterBox(id, text);
		UtilsDB.createProperties(database, map, p);

//		Property levelProperty = Property.find(database, Property.LEVEL);
//		ObjectType level = database.find((String) levelProperty.getPropertyValue(map));

//		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
//		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);
//
//		ObjectType strateginen = ObjectType.find(database, ObjectType.STRATEGINEN_TAVOITE);

//		if (strateginen.uuid.equals(goalTypeUUID)) {
//			String pageName = Wiki.makeWikiPageName(database, p);
//			Wiki.edit(pageName,
//					"=Strategisen tavoitteen määritys=\n==Kuvaus tavoitetilasta==\n==Onnistumisen kriteerit==\n==Lähtöoletukset==\n==Riskit==\n=Voimavarat=\n==Henkiset voimavarat==\n==Fyysiset/aineelliset voimavarat==\n==Määrärahat==\n");
//		}

		map.addTavoite(p);

		return p;

	}

	public static OuterBox create(Database database, StrategyMap map, String id, String text) {

		OuterBox t = createTransient(database, map, id, text);
		database.register(t);
		return t;

	}

	@Override
	public MapBase getOwner(Database database) {
		return getMap(database);
	}

	public void add(Database database, InnerBox painopiste) {
		painopiste.addRelation(Relation.find(database, Relation.IMPLEMENTS), this);
		addInnerBox(painopiste);
	}

	public void moveUp(InnerBox t) {
		int pos = findPainopiste(t);
		if (pos != -1 && pos > 0) {
			InnerBox previous = innerboxes[pos - 1];
			innerboxes[pos - 1] = t;
			innerboxes[pos] = previous;
		}
	}

	public void moveDown(InnerBox t) {
		int pos = findPainopiste(t);
		if (pos != -1 && pos < innerboxes.length - 1) {
			InnerBox next = innerboxes[pos + 1];
			innerboxes[pos + 1] = t;
			innerboxes[pos] = next;
		}
	}

	public int findPainopiste(InnerBox t) {
		for (int i = 0; i < innerboxes.length; i++) {
			if (t.equals(innerboxes[i]))
				return i;
		}
		return -1;
	}

	public void addInnerBox(InnerBox p) {
		innerboxes = Arrays.copyOf(innerboxes, innerboxes.length + 1);
		innerboxes[innerboxes.length - 1] = p;
	}

	public void removePainopiste(Database database, InnerBox p) {
		int index = findPainopiste(p);
		if (index == -1)
			throw new IllegalArgumentException("Not found: " + p.getId(database));
		removePainopiste(index);
	}

	public void removePainopiste(int index) {
		InnerBox[] old = innerboxes;
		innerboxes = new InnerBox[innerboxes.length - 1];
		for (int i = 0, pos = 0; i < old.length; i++) {
			if (i == index)
				continue;
			innerboxes[pos++] = old[i];
		}
	}

	@Override
	public String getDescription(Database database) {
		return getId(database) + " (" + database.getType(this) + ")";
	}

	@Override
	public boolean canRemove(Database database) {
		return innerboxes.length == 0;
	}

	@Override
	public void remove(Database database) {
		try {
			StrategyMap implementationMap = getPossibleImplementationMap(database);
			if(implementationMap != null) {
				implementationMap.remove(database);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		StrategyMap map = database.getMap(this);
		map.removeOuterBox(database, this);
		super.remove(database);
	}

	/*public Strategiakartta getImplementingSubmap(Database database) {
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		for (Base b : database.getInverse(this, implementsRelation)) {
			if (b instanceof Strategiakartta) {
				return (Strategiakartta) b;
			}
		}
		return null;
	}*/
	
	public Base getPossibleSubmapType(Database database) throws Exception {
		StrategyMap map = getPossibleMap(database);
		if(map == null)return null;
		return map.getPossibleSubmapType(database);
	}
	
	public boolean hasImplementationSubmap(Database database) {
		try {
			return getPossibleSubmapType(database) != null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static List<OuterBox> enumerate(Database database) {
		ArrayList<OuterBox> result = new ArrayList<OuterBox>();
		for(Base b : database.enumerate()) {
			if(b instanceof OuterBox) result.add((OuterBox)b);
		}
		return result;
	}

	public Set<StrategyMap> getImplementationMaps(Database database) {
		HashSet<StrategyMap> result = new HashSet<StrategyMap>();
		for(Base b : UtilsDB.getDirectImplementors(database, this, false, Property.AIKAVALI_KAIKKI)) {
			if(b instanceof StrategyMap) result.add((StrategyMap)b);
		}
		return result;
	}
	
	public StrategyMap getPossibleImplementationMap(Database database) throws Exception {
		Set<StrategyMap> maps = getImplementationMaps(database);
		if(maps.size() == 1) return maps.iterator().next();
		else if(maps.size() == 0) return null;
		else throw new Exception("Multiple implementation maps.");
	}

	/**
	 * 
	 * @param database
	 * @param account
	 * @param currentTime
	 * @return
	 * @throws Exception
	 */
	public boolean ensureImplementationMap(final Database database, Account account, String currentTime) throws Exception {
	
		boolean didSomething = false;
		
		if("Voimavarat".equals(id)) return false;
		
		Base subType = getPossibleSubmapType(database);
		if(subType != null) {
			StrategyMap implementationMap = getPossibleImplementationMap(database);
			if(implementationMap == null) {
				implementationMap = getPossibleImplementationMap(database);
				StrategyMap parent = getMap(database) ;
				implementationMap = database.newMap(database, parent, "", "", subType);
				implementationMap.addRelation(Relation.find(database, Relation.IMPLEMENTS), this);
				didSomething = true;
			}
			for(InnerBox pp : innerboxes) {
				OuterBox goal = pp.getPossibleImplementationGoal(database);
				if(goal == null) {
					UtilsDB.createOuterBoxCopy(database, account, implementationMap, pp, currentTime);
					didSomething = true;
				}
			}
		}
		
		return didSomething;
		
	}


	@Override
	public void moveUp(Database database) {
		StrategyMap map = database.getMap(this);
		map.moveUp(this);
	}

	@Override
	public void moveDown(Database database) {
		StrategyMap map = database.getMap(this);
		map.moveDown(this);
	}
	
	@Override
	public String getBoxDescription(Database database) {

		Tag voimavarat = database.getOrCreateTag(Tag.VOIMAVARAT);
		if (hasRelatedTag(database, voimavarat)) {
			return "Keino";
		} else {
			final StrategyMap map = database.getMap(this);
			return map.innerDescription;
		}

	}
	
	@Override
	public String clientIdentity() {
		return "OuterBox";
	}

}
