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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Office extends Base implements Serializable{

	private static final long serialVersionUID = 8286057130354351374L;
	public boolean isController;
	
	private Office(String id, String text, boolean isController) {
		super(UUID.randomUUID().toString(), id, text);
		this.isController = isController;
	}
	
	/**
	 * 
	 * @param isController
	 * @return
	 */
	public static Office createTransient(String id, String text, boolean isController) {
		Office tc = new Office(id, text, isController);
		return tc;
	}
	
	/**
	 * 
	 * @param database
	 * @param attachedMap
	 * @param isController
	 * @return
	 */
	public static Office create(Database database, StrategyMap attachedMap, boolean isController) {
		String id = "(virasto ilman karttaa. ID: " + UUID.randomUUID().toString() + ")";
		String text = attachedMap.getText(database);
		if(text == null) {
			text = id;
		} else if(text.equals("")) {
			text = id;
		}
		Office tc = createTransient(id, text, isController);
		database.register(tc);

		tc.attachToMap(database, attachedMap);
		
		return tc;
	}
	
	public void detachFromAllMaps(Database database) {
		Collection<Base> maps = this.getRelatedObjects(database, Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE));
		for(Base b : maps) {
			if(b instanceof StrategyMap) {
				((StrategyMap)b).denyRelation(database, Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT));
			}
		}
		this.denyRelation(database, Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE));
	}
	
	public void attachToMap(Database database, StrategyMap map) {
		this.addRelation(Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE), map);
		map.addRelation(Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT), this);
	}
	
	public static Office create(Database database, String id, String text, boolean isController) {
		Office tc = createTransient(id, text, isController);
		database.register(tc);
		return tc;
	}
	
	public String getShortId(Database database) {
		StrategyMap map = this.getPossibleAttachedMap(database);
		if(map != null) {
			if(!map.id.equals("")) {
				return map.id;
			} else {
				return getText(database);
			}
		} else {
			return this.id;
		}
	}
	
	@Override
	public String getText(Database database) {
		StrategyMap map = this.getPossibleAttachedMap(database);
		if(map != null) {
			return map.getText(database);
		} else {
			return this.text;
		}
	}
	
	public StrategyMap getPossibleAttachedMap(Database database) {
		Collection<Base> relatedMaps = this.getRelatedObjects(database, Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE));
		if(relatedMaps.size() == 0) return null;
		else if(relatedMaps.size() == 1) {
			Base b = relatedMaps.iterator().next();
			if(b instanceof StrategyMap) {
				return (StrategyMap)b;
			} else {
				return null;
			}
		}
		else {
			System.err.println("Multiple maps attached to an Office!");
			return null;
		}
	}
	
	public static Office getPossibleOfficeByMap(Database database, StrategyMap map) {
		Collection<Base> relatedOffices = map.getRelatedObjects(database, Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT));
		if(relatedOffices.size() == 0) return null;
		else if(relatedOffices.size() == 1) {
			Base b = relatedOffices.iterator().next();
			if(b instanceof Office) {
				return (Office)b;
			} else {
				return null;
			}
		}
		else {
			System.err.println("Multiple offices attached to a map!");
			return null;
		}
	}
	
	/**
	 * Returns the attached map
	 */
	@Override
	public Base getOwner(Database database) {
		return getPossibleAttachedMap(database);
	}
	
	/**
	 * Find all office objects
	 * @param database
	 * @return
	 */
	public static List<Office> enumerate(Database database, boolean sort, boolean includeControllers) {
		ArrayList<Office> result = new ArrayList<Office>();
		for (Base b : database.enumerate()) {
			if (b instanceof Office) {
				Office o = (Office)b;
				//Add regardless if they're controllers or not
				if(includeControllers) {
					result.add(o);
				} else {
					//Add only non-controllers
					if(!o.isController) {
						result.add(o);
					}
				}
			}
		}
		
		if(sort) {
			Comparator<Office> nameComparator = new Comparator<Office>() {
				@Override
				public int compare(Office o1, Office o2) {
					int l =  o1.getText(database).compareTo(o2.getText(database));
					double d = (double)l;
					return (int)d;
				}
			};
			
			Collections.sort(result, nameComparator);
		}
		return result;
	}
	
	/**
	 * Find by UUID
	 * @param database
	 * @param uuid
	 * @return
	 */
	public static Office possiblyFindByUUID(Database database, String uuid) {
		List<Office> all = enumerate(database, false, true);
		for(Office o : all) {
			if(o.uuid.equals(uuid)) {
				return o;
			}
		}
		return null;
	}
	
	public static Office possiblyFindByShortID(Database database, String shortId) {
		List<Office> all = enumerate(database, false, true);
		for(Office o : all) {
			if(o.getShortId(database).equals(shortId)) {
				return o;
			}
		}
		return null;
	}
	
	/**
	 * Find by name
	 * @param database
	 * @param uuid
	 * @return
	 */
	public static Office possiblyFindByName(Database database, String text) {
		List<Office> all = enumerate(database, false, true);
		for(Office o : all) {
			if(o.getText(database).equals(text)) {
				return o;
			}
		}
		return null;
	}
	
	@Override
	public String clientIdentity() {
		return "Virasto";
	}
	
}
