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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InnerBox extends MapBase implements Serializable, MapBox  {

	private static final long serialVersionUID = 9064076890935334644L;

	public boolean copy;

	public int columns = 2;

	public static InnerBox create(Database database, String currentTime,
			StrategyMap map, OuterBox goal, String text) {
		return create(database, currentTime, map, goal, "", text, null);
	}

	public static InnerBox create(Database database, String currentTime,
			StrategyMap map, OuterBox goal, String id, String text) {
		return create(database, currentTime, map, goal, id, text, null);
	}

	public static InnerBox create(Database database, String currentTime,
			StrategyMap map, OuterBox goal, String id, String text, ObjectType type) {
		
		InnerBox p = create(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addInnerBox(p);
		
		Property time = Property.find(database, Property.AIKAVALI);
		
		time.set(false, database, null, p, currentTime);
		
		return p;
		
	}

	public static InnerBox createTransient(Database database, String currentTime,
			StrategyMap map, OuterBox goal, String id, String text, ObjectType type) {
		
		InnerBox p = createTransient(database, map, id, text, type);
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		p.addRelation(implementsRelation, goal);
		goal.addInnerBox(p);
		
		Property time = Property.find(database, Property.AIKAVALI);
		time.set(false, database, null, p, currentTime);
		
		return p;
	}

	public static InnerBox createTransient(Database database, StrategyMap map, String id, String text, ObjectType type) {
		
		InnerBox p = new InnerBox(id, text);
		if(type != null) 
			p.properties.add(Pair.make(Property.find(database, Property.TYPE).uuid, type.uuid));
		
		UtilsDB.createProperties(database, map, p);

		return p;
		
	}

	/**
	 * @param database
	 * @param map
	 * @param id
	 * @param text
	 * @param type
	 * @return
	 */
	public static InnerBox create(Database database, StrategyMap map, String id, String text, ObjectType type) {
		
		InnerBox p = createTransient(database, map, id, text, type);
		database.register(p);
		return p;
		
	}
	
	protected InnerBox(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	public static List<InnerBox> enumerate(Database database) {
		ArrayList<InnerBox> result = new ArrayList<InnerBox>();
		for(OuterBox ob : OuterBox.enumerate(database)) {
			for(InnerBox ib : ob.innerboxes) {
				result.add(ib);
			}
		}
		return result;
	}

	@Override
	public String getDescription(Database database) {
		return getId(database) + " (" + database.getType(this) + ")";
	}
	
	@Override
	public MapBase getOwner(Database database) {
		return getGoal(database);
	}
	
	public OuterBox getGoal(Database database) {

		OuterBox result = null;
		for(StrategyMap map : StrategyMap.enumerate(database)) {
			for(OuterBox goal : map.outerBoxes) {
				for(InnerBox p : goal.innerboxes) {
					if(p.uuid.equals(uuid)) {
						if(result != null && result != goal) return null;
						result = goal;
					}
				}
			}
		}
		
		return result;
		
	}

	@Override
	public void remove(Database database) {
		OuterBox t = database.getTavoite(this);
		t.removePainopiste(database, this);
		super.remove(database);
	}
	
	public OuterBox getPossibleImplementationGoal(Database database) {
		Set<OuterBox> result = new HashSet<OuterBox>();
		for(Base b : UtilsDB.getDirectImplementors(database, this)) {
			if(b instanceof OuterBox) result.add((OuterBox)b);
		}
		if(result.size() == 1) return result.iterator().next();
		return null;
	}


	@Override
	public void moveUp(Database database) {
		OuterBox t = database.getTavoite(this);
		t.moveUp(this);
	}

	@Override
	public void moveDown(Database database) {
		OuterBox t = database.getTavoite(this);
		t.moveDown(this);
	}
	
	@Override
	public String getBoxDescription(Database database) {
		final StrategyMap map = database.getMap(this);
		return map.outerDescription;
	}
	
	@Override
	public String clientIdentity() {
		return "InnerBox";
	}
	
}
