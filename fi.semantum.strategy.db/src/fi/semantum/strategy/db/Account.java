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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Account extends Base {

	private static final long serialVersionUID = -8882871866781578456L;
	
	public List<UIState> uiStates = new ArrayList<UIState>();
	public String hash;
	public String email;
	public String zone;

	public static Account create(Database database, String usernameAndID, String email, String hash, AccountGroup group) {
		Account p = new Account(usernameAndID, email, hash, group);
		p.addAccountGroup(database, group);
		database.register(p);
		return p;
	}
	
	private Account(String nameAndID, String email, String hash, AccountGroup group) {
		super(UUID.randomUUID().toString(), nameAndID, nameAndID);
		this.hash = hash;
		this.email = email;
		this.zone = "Europe/Helsinki";
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public boolean isAdmin(Database database) {
		for(AccountGroup group : this.getAccountGroups(database)) {
			if(group.isAdmin()) return true;
		}
		return false;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getEmail() {
		return email;
	}
	
	public static Account find(Database database, String idOrName) {
		
		for(Base b : database.enumerate()) {
			if(b instanceof Account) {
				Account p = (Account)b;
				if(idOrName.equals(p.getText(database))) return p;
			}
		}
		return null;

	}

	public static List<Account> enumerate(Database database) {
		ArrayList<Account> result = new ArrayList<Account>();
		for(Base b : database.objects.values()) {
			if(b instanceof Account)
				result.add((Account)b);
		}
		return result;
	}
	
	@Override
	public boolean migrate(Database database) {
		boolean result = false;
		for(UIState s : uiStates) {
			result |= s.migrate();
		}
		result |= super.migrate(database);
		return result;
	}
	
	public Set<AccountGroup> getAccountGroups(Database database){
		Set<AccountGroup> groups = new HashSet<>();
		
		Collection<Base> accountGroups = this.getRelatedObjects(database, Relation.find(database, Relation.PART_OF));
		for(Base groupB : accountGroups) {
			if(groupB instanceof AccountGroup) {
				groups.add((AccountGroup)groupB);
			}
		}
		
		return groups;
	}
	
	public void addAccountGroup(Database database, AccountGroup group) {
		this.addRelation(Relation.find(database, Relation.PART_OF), group);
	}
	
	public void denyAccountGroup(Database database, AccountGroup group) {
		this.denyRelation(database, Relation.find(database, Relation.PART_OF), group);
	}

	public String getZone() {
		return this.zone;
	}
	
	@Override
	public String clientIdentity() {
		return "Käyttäjä";
	}
	
}
