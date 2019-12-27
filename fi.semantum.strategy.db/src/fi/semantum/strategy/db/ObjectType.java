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
import java.util.List;
import java.util.UUID;

public class ObjectType extends Base {

	/*
	 * LVM
	 * -osasto   tulostavoite/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * LIVI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * -osasto     toimialan toimenpide/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * VIVI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * (-yksikkö)     toimialan toimenpide/toimenpide
	 * -ryhmä  toimenpide/vastuuhenkilö
	 * 
	 * TRAFI
	 * -toimiala   tulostavoite/toimialan toimenpide
	 * -osasto     toimialan toimenpide/toimenpide
	 * -yksikkö  toimenpide/vastuuhenkilö
	 * 
	 * 
	 */
	
	public static final String HALLINNONALA = "Hallinnonala";
	
	public static final String RYHMA = "Ryhmä";
	
	public static final String ACCOUNT = "Account";
	public static final String LEVEL_TYPE = "LevelType";
	public static final String FOCUS_TYPE = "FocusType";
	public static final String GOAL_TYPE = "GoalType";

	public static final String STRATEGINEN_TAVOITE = Terminology.STRATEGIC_GOAL_OR_TARGET;
	public static final String PAINOPISTE = Terminology.FOCUS_POINT;
	
	private static final long serialVersionUID = -1598499411379047877L;

	public static ObjectType create(Database database, String name) {
		ObjectType p = new ObjectType(name, name);
		database.register(p);
		return p;
	}
	
	public static ObjectType create(Database database, String id, String text) {
		ObjectType p = new ObjectType(id, text);
		database.register(p);
		return p;
	}

	private ObjectType(String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public static ObjectType find(Database database, String id) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof ObjectType) {
				ObjectType p = (ObjectType)b;
				if(id.equals(p.getId(database))) return p;
			}
		}
		return null;

	}
	
	public static List<ObjectType> enumerate(Database database){
		List<ObjectType> types = new ArrayList<>();
		for(Base b : database.enumerate()) {
			if(b instanceof ObjectType) {
				types.add((ObjectType)b);
			}
		}
		return types;
	}
	
	public List<Pair> getRelations(Base b) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) {
				result.add(p);
			}
		}
		return result;
	}
	
	@Override
	public String clientIdentity() {
		return "Olio tietotyyppi";
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

	public boolean modifyText(Database database, Account account, String text) {

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
	
}
