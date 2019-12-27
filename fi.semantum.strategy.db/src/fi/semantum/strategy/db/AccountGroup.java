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

public class AccountGroup extends Base {

	private static final long serialVersionUID = -8882871866781578456L;
	
	/**
	 * Is this group an admin group?
	 */
	private boolean admin = false;
	public List<AccessRight> rights = new ArrayList<AccessRight>();

	public static AccountGroup create(Database database, String groupName) {
		AccountGroup p = new AccountGroup(groupName);
		database.register(p);
		return p;
	}
	
	/**
	 * 
	 * @param database
	 * @param office
	 * @return
	 */
	public static AccountGroup ensureAccountGroupMappedToOffice(Database database, Office office) {
		String newText = office.getText(database);
		
		List<AccountGroup> groups = AccountGroup.enumerate(database);
		AccountGroup target = null;
		for(AccountGroup g : groups) {
			if(g.text.equals(newText)) {
				target = g;
				break;
			}
		}
		
		if(target == null) {
			target = new AccountGroup(newText);
			database.register(target);
			UtilsDB.associateAccountGroupWithOffice(database, office, target);
			return target;
		} else {
			UtilsDB.associateAccountGroupWithOffice(database, office, target);
			return target;
		}
	}
	
	public void addRightIfMissing(AccessRight right) {
		String uuid = right.base.uuid;
		boolean write = right.write;
		boolean exists = false;
		for(AccessRight existingRight : rights) {
			if(existingRight.write == write && existingRight.base.uuid.equals(uuid)) {
				exists = true;
				break;
			}
		}
		if(!exists) {
			rights.add(right);
		}
	}
	
	public void removeRightIfExists(Base target, boolean write) {
		for(int i = 0; i < rights.size(); i++) {
			AccessRight r = rights.get(i);
			if(r.write == write && r.base.uuid.equals(target.uuid)) {
				System.out.println("Removing AccessRight targeting base " + target.uuid + " from group " + this.text);
				rights.remove(i);
			}
		}
	}
	
	public void removeRight(AccessRight rightToRemove) {
		boolean ok = rights.remove(rightToRemove);
		if(!ok) {
			System.err.println("Failed to remove access right since no such object exists - trying to remove by matching UUID and write right");
			for(AccessRight existingRight : rights) {
				if(existingRight.write == rightToRemove.write && existingRight.base.uuid.equals(rightToRemove.base.uuid)) {
					System.out.println("Removing access right by matching base UUID and write right");
					rights.remove(existingRight);
				}
			}
		}
	}
	
	private AccountGroup(String groupNameAndID) {
		super(UUID.randomUUID().toString(), groupNameAndID, groupNameAndID);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean value) {
		this.admin = value;
	}
	
	public static AccountGroup find(Database database, String name) {
		
		for(Base b : database.enumerate()) {
			if(b instanceof AccountGroup) {
				AccountGroup p = (AccountGroup)b;
				if(name.equals(p.getText(database))) return p;
			}
		}
		return null;

	}

	public boolean canRead(Database database, Base b) {
		StrategyMap map = null;
		try {
			if(isMapType(b)) {
				map = database.getMap((MapBase)b);
			}
		} catch (RuntimeException rte) {
			//Inspecting a non-map entity
			map = null;
		}
		
		for(AccessRight r : rights) {
			if (r instanceof MapRight) {
				if(map != null) {
					MapRight mapR = (MapRight) r;
					if(mapR.recurse) {
						if(mapR.getMap().isUnder(database, map)) return true;
					} else {
						if(mapR.getMap().equals(map)) return true;
					}
				}
			} else {
				if(r.base.equals(b)) return true; //Ok to read since b matches the base of the right given to this account
			}

		}
		return false;
	}
	
	//TODO: Make Base two-layered where all of these are MapBase types!
	private boolean isMapType(Base b) {
		return b instanceof MapBase;
	}

	public boolean canWrite(Database database, Base b) {
		if(isAdmin()) return true;
		if(!TimeConfiguration.getInstance(database).canWrite(database, b)) return false;
		
		StrategyMap map = null;
		try {
			if(isMapType(b)) {
				map = database.getMap((MapBase)b);
			}
		} catch (RuntimeException rte) {
			//Inspecting a non-map entity
			map = null;
		}

		for(AccessRight r : rights) {
			//System.out.println("with " + r.base.uuid);
			
			if(r.write) {
				if(r instanceof MapRight) {
					if(map != null) {
						MapRight mapR = (MapRight)r;
						if(mapR.recurse) {
							if(mapR.getMap().isUnder(database, map)) return true;
						} else {
							if(mapR.getMap().equals(map)) return true;
						}
					}
				} else {
					if(r.base.equals(b)) return true;
				}
			}
		}
		return false;
	}

	public static List<AccountGroup> enumerate(Database database) {
		ArrayList<AccountGroup> result = new ArrayList<AccountGroup>();
		for(Base b : database.objects.values()) {
			if(b instanceof AccountGroup) result.add((AccountGroup)b);
		}
		return result;
	}
	
	@Override
	public boolean migrate(Database database) {
		boolean result = false;
		result |= super.migrate(database);
		return result;
	}
	
	@Override
	public String clientIdentity() {
		return "Käyttäjäryhmä";
	}
	
}
