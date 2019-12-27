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
import java.util.List;
import java.util.UUID;

public class Property extends Base {

	private static final long serialVersionUID = -1598499411379047877L;

	public static final String LEVEL = "Organisaatiotaso";
	public static final String AIKAVALI = "Voimassaolo";
	public static final String OWNER = "Vastuuhenkilö";
	public static final String EMAIL = "Seuraajat (email)";
	public static final String CHANGED_ON = "Viimeisin muutos";
	public static final String TTL = "Päivitysvaatimus (päivää)";

	public static final String OWN_GOAL_TYPE = "Own Goal Type";
	//@Deprecated
	//public static final String GOAL_TYPE = "Goal Type";
	//@Deprecated
	//public static final String FOCUS_TYPE = "Focus Type";
	public static final String CHARACTER_DESCRIPTION = "Characteristic Description";
	public static final String CHARACTER_COLOR = "Characteristic Color";
	public static final String CHARACTER_TEXT_COLOR = "Characteristic Text Color";
	public static final String GOAL_DESCRIPTION = "Goal Description";
	public static final String LINK_WITH_PARENT = "Link With Parent";
	public static final String LINK_GOALS_AND_SUBMAPS = "Link Goals And Submaps";
	public static final String AMOUNT_OF_LEVELS = "The Amount Of Box Levels In This Map";
	public static final String HAS_VISION = "Do The Maps Define Visions";
	public static final String HAS_METER = "Do The Inner Boxes Contain Meter Values";
	public static final String ALLOWS_OWN_OUTER_BOX = "Are Own Outer Boxes Allowed";

	public static final String DEFINES_OFFICES = "Defines Office";
	
//	public static final String MANY_IMPLEMENTOR = "Many Implementor";
//	public static final String MANY_IMPLEMENTS = "Many Implements";

	public static final String TYPE = "Tyyppi";

	public static final String AIKAVALI_KAIKKI = "-";

	final public boolean readOnly;
	final private String objectType;
	private List<String> enumeration;

	public static Property create(Database database, String id, String text, String objectType, boolean readOnly, List<String> enumeration) {
		Property p = new Property(id, text, objectType, readOnly, enumeration);
		database.register(p);
		return p;
	}

	public static Property create(Database database, String name, String objectType, boolean readOnly, List<String> enumeration) {
		Property p = new Property(name, name, objectType, readOnly, enumeration);
		database.register(p);
		return p;
	}

	private Property(String id, String text, String objectType, boolean readOnly, List<String> enumeration) {
		super(UUID.randomUUID().toString(), id, text);
		this.objectType = objectType;
		this.readOnly = readOnly;
		this.enumeration = enumeration;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}

	private List<Pair> getObjectEnumeration(Database database) {
		Property type = Property.find(database, Property.TYPE);
		ArrayList<Pair> result = new ArrayList<Pair>();
		for(Base b : database.objects.values()) {
			String uuid = type.getPropertyValue(b);
			if(uuid == null) continue;
			if(objectType.equals(uuid))
				result.add(Pair.make(b.uuid, b.getId(database)));
		}
		return result;

	}

	public List<String> getEnumeration(Database database) {
		if(objectType != null) {
			ArrayList<String> result = new ArrayList<String>();
			for(Pair p : getObjectEnumeration(database)) {
				result.add(p.second);
			}
			return result;
		}
		return enumeration;
	}

	public String getEnumerationValue(Database database, String value) {
		if(objectType != null) {
			Base b = database.find(value);
			if(b == null) return "Invalid enumeration value " + value;
			return b.getId(database);
		} else {
			return value;
		}
	}

	public static Property find(Database database, String name) {

		for(Base b : database.objects.values()) {
			if(b instanceof Property) {
				Property p = (Property)b;
				if(name.equals(p.getText(database))) return p;
			}
		}
		return null;

	}

	public boolean hasProperty(Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) return true;
		}
		return false;
	}

	public boolean hasPropertyValue(Base b, String value) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				if(value.equals(p.second))
					return true;
			}
		}
		return false;
	}

	public String getPropertyValue(Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				return p.second;
			}
		}
		return null;
	}

	public <T extends Base> T getPropertyValueObject(Database database, Base b) {
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				return database.find(p.second);
			}
		}
		return null;
	}

	/*
	 * TODO: Enumerated value can be uuid or it can be id
	 */
	public boolean set(boolean uiSessionExists, Database database, Account account, Base b, String value) {
		
		Pair exist = null;
		for(Pair p : b.properties) {
			if(uuid.equals(p.first)) {
				if(exist != null) {
					System.err.println("Multiple values for property " + uuid);
				} else {
					exist = p;
				}
			}
		}

		if(exist != null) {
			if(exist.equals(value)) return false;
			if(exist.second.equals(value)) return false;
		}

		if(uiSessionExists) {
			if(!b.modified(database, account)) return false;
		}

		if(exist != null) b.properties.remove(exist);
		if(objectType != null) {
			for(Pair p : getObjectEnumeration(database)) {
				if(p.first.equals(value)) {
					// UUID
					b.properties.add(Pair.make(uuid, p.first));
					return true;
				} else if(p.second.equals(value)) {
					// id
					b.properties.add(Pair.make(uuid, p.first));
					return true;
				}
			}
		} else {
			b.properties.add(Pair.make(uuid, value));
		}

		if(AIKAVALI.equals(getId(database)) && b instanceof MapBase) {
			MapBase mb = (MapBase)b;
			for(MapBase imp : UtilsDB.getDirectImplementors(database, mb, Property.AIKAVALI_KAIKKI)) {
				set(uiSessionExists, database, account, imp, value);
			}
			MapBase copy = mb.getPossibleCopy(database);
			if(copy != null) {
				set(uiSessionExists, database, account, copy, value);
			}
		}

		return true;

	}

	public Pair make(String value) {
		return Pair.make(uuid, value);
	}

	public void setEnumeration(List<String> values) {
		enumeration = values;
	}

		
	@Override
	public String clientIdentity() {
		return "Ominaisuus";
	}

}
