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

import java.util.List;
import java.util.Set;

public class UtilsDBMapTypes {


	public static String canRemove(Database database, Base level) {
    	for(StrategyMap map : StrategyMap.enumerate(database)) {
    		ObjectType t = map.getLevelType(database);
    		if(t.equals(level)) return "Tyyppi on käytössä kartassa: " + map.getText(database);
    	}
    	return "";
	}
	
	
	public static ObjectType createMapType(boolean uiSessionExists, Database database, Account account, String name,
			boolean linkWithParent, boolean goalSubmap, boolean definesOffices) {
		
		Property typeProperty = Property.find(database, Property.TYPE);
        ObjectType levelType = ObjectType.find(database, ObjectType.LEVEL_TYPE);
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);
		
		ObjectType newType = ObjectType.create(database, name, name);
		newType.properties.add(typeProperty.make(levelType.uuid));
		characterDescriptionP.set(uiSessionExists, database, account, newType, Terminology.FOCUS_POINT);
		goalDescriptionP.set(uiSessionExists, database, account, newType, "");
		characterColorP.set(uiSessionExists, database, account, newType, database.createHue());
		characterTextColorP.set(uiSessionExists, database, account, newType, "#000");
		linkWithParentP.set(uiSessionExists, database, account, newType, linkWithParent ? "true" : "false");
		goalSubmapP.set(uiSessionExists, database, account, newType, goalSubmap ? "true" : "false");
		definesOfficesP.set(uiSessionExists, database, account, newType, "false");
		
		if(definesOffices) {
			updateOfficeMapTypeSelection(database,
					uiSessionExists,
					account,
					newType);
		}
		return newType;
	}
	
	/**
	 * 
	 * @param database
	 * @param uiSessionExists
	 * @param account
	 * @param newType
	 * @return success?
	 */
	public static boolean updateOfficeMapTypeSelection(final Database database,
			boolean uiSessionExists,
			Account account,
			Base newType){
		if(newType instanceof ObjectType) {
			updateOfficeMapTypeSelection(database, uiSessionExists, account, (ObjectType)newType);
			return true;
		} else {
			System.err.println("Error in updateOfficeMapTypeSelection. Unexpected class: " + newType.getClass().toString() + ". Expected: " + ObjectType.class.toString());
			return false;
		}
	}
	
	private static void updateOfficeMapTypeSelection(final Database database,
			boolean uiSessionExists,
			Account account,
			ObjectType newType){
		
		Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);

		List<ObjectType> types = ObjectType.enumerate(database);
		for(ObjectType t : types) {
			String oldValue = definesOfficesP.getPropertyValue(t);
			if(oldValue != null && !oldValue.equals("false")) {
				definesOfficesP.set(uiSessionExists, database, account, t, "false");
			}
		}
		System.out.println("ObjectType " + newType.id + " now defines offices!");
		definesOfficesP.set(uiSessionExists, database, account, newType, "true");
		
		//Delete old offices, result agreement documents, etc. Then create new offices and resultagreement documents for all new offices based on type.
		removeAllNonControllerOffices(database);
		createNewOfficesFromObjectType(database, newType);
	}
	
	private static void createNewOfficesFromObjectType(Database database, ObjectType newType) {
		Set<StrategyMap> matching = StrategyMap.getMapsByObjectType(database, newType);
		for(StrategyMap map : matching) {
			UtilsDB.createAndSynchronizeNewOffice(database, map, newType);
		}
	}
	
	public static void removeAllNonControllerOffices(final Database database) {
		System.out.println("Synchronizing DB to reflect chosen ObjectType as root for Offices!");
		List<Office> nonControllerOffices = Office.enumerate(database, false, false);
		for(Office office : nonControllerOffices) {
			UtilsDB.removeAllAssociatedObjectsByOffice(database, office);
			office.remove(database);
		}
	}
}
