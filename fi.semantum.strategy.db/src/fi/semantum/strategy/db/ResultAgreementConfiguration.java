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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ResultAgreementConfiguration extends Base {
	
	private static final long serialVersionUID = 4549011274456890015L;
	
	public List<ResultAgreementConfigEntry> config;
	public Integer[] office_indeces_cache;
	public Integer[] shared_indeces_cache;
	public Integer[] all_indeces_cache;
	
	public static final String SHARED_LABEL_APPENDIX = "(yhteinen)";
	
	/**
	 * First creation of ResultAgreementConfiguration goes through the DBConfigurations.
	 */
	private ResultAgreementConfiguration(Database database) {
		super(UUID.randomUUID().toString(), "ResultAgreementConfiguration", "ResultAgreementConfiguration");
		
		int[] all = DBConfiguration.getDEFAULT_ALL_CHAPTER_INDECES();
		int[] shared_indeces_a = DBConfiguration.getDEFAULT_SHARED_CHAPTER_INDECES();
		
		config = new ArrayList<>(all.length);
		
		for(int i : all) {
			boolean is_shared  = false;
			for(int x : shared_indeces_a) {
				if(x == i) {
					is_shared = true;
					break;
				}
			}
			
			String label = getDefaultLabel(i);
			if(is_shared) {
				label = label + " " + SHARED_LABEL_APPENDIX;
			}
			
			String desc = getDefaultDescription(i);
			ResultAgreementConfigEntry entry = ResultAgreementConfigEntry.create(database, label, desc, is_shared);
			config.add(entry);
		}
		
		if(config.contains(null)) {
			for(ResultAgreementConfigEntry entry : config) {
				System.err.println(entry == null ? "null" : entry.toString());
			}
			throw new NullPointerException("Incorrectly defined offices and shared indeces, found null entry in configuration!");
		}
		
		syncCache();
	}
	
	/**
	 * Delete specified index and update the lists to reflect this change.
	 * Do not allow gaps to exist in the ALL shapters -> just remove the last from this
	 * @param i
	 * @return
	 */
	public boolean deleteEntry(int index) {
		if(config.size() > index) {
			//TODO: Delete entry and update indexes
			config.remove(index);
			syncCache();
			return true;
		} else {
			return false;
		}
	}
	
	public int addEntry(Database database, boolean shared) {
		if(this.getAllIndeces().length == 0) {
			return addEntry(database, 0, shared);
		} else {
			int new_index = (this.getAllIndeces()[(this.getAllIndeces().length - 1)]);
			return addEntry(database, new_index + 1, shared);
		}
	}
	
	private int addEntry(Database database, int index, boolean shared) {
		ResultAgreementConfigEntry entry = ResultAgreementConfigEntry.create(database,
				getDefaultLabel(index),
				getDefaultDescription(index), shared);
		if(this.config.size() < index) {
			this.config.add(index, entry);
		} else {
			this.config.add(entry);
		}
		syncCache();
		return index;
	}
	
	public static ResultAgreementConfiguration ensureExists(Database database) {
		ResultAgreementConfiguration old = getInstance(database);
		if(old != null) {
			return old;
		} else {
			ResultAgreementConfiguration rac = new ResultAgreementConfiguration(database);
			database.register(rac);
			return rac;
		}
	}
	
	public boolean isSharedIndex(int i) {
		ResultAgreementConfigEntry entry = this.config.get(i);
		return entry == null ? false : entry.isShared();
	}
	
	public boolean isOfficeIndex(int i) {
		ResultAgreementConfigEntry entry = this.config.get(i);
		return entry == null ? false : entry.isOffice();
	}
	
	/**
	 * Update cached values
	 * Call whenever changes happen to Entries
	 */
	private void syncCache() {
		List<Integer> office_indeces = new ArrayList<>();
		List<Integer> shared_indeces = new ArrayList<>();
		List<Integer> all_indeces = new ArrayList<>();
		for(int i = 0; i < this.config.size(); i++) {
			ResultAgreementConfigEntry entry = this.config.get(i);
			if(entry.isOffice()) {
				office_indeces.add(i);
			} else if(entry.isShared()) {
				shared_indeces.add(i);
			}
			all_indeces.add(i);
		}
		
		Collections.sort(office_indeces);
		Collections.sort(shared_indeces);
		Collections.sort(all_indeces);
		
		Integer[] arr = new Integer[office_indeces.size()];
		arr = office_indeces.toArray(new Integer[0]);
		this.office_indeces_cache = arr;
		
		Integer[] arr2 = new Integer[shared_indeces.size()];
		arr2 = shared_indeces.toArray(new Integer[0]);
		this.shared_indeces_cache = arr2;
		
		Integer[] arr3 = new Integer[all_indeces.size()];
		arr3 = all_indeces.toArray(new Integer[0]);
		this.all_indeces_cache = arr3;
	
		System.out.print("All:");
		for(Integer i : all_indeces_cache) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.print("Office:");
		for(Integer i : office_indeces_cache) {
			System.out.print(i + ", ");
		}
		System.out.println();
		System.out.print("Shared:");
		for(Integer i : shared_indeces_cache) {
			System.out.print(i + ", ");
		}
		System.out.println();
	}
	
	public Integer[] getOfficeIndeces(){
		return this.office_indeces_cache;
	}
	
	public Integer[] getAllIndeces(){
		return this.all_indeces_cache;
	}
	
	public Integer[] getSharedIndeces(){
		return this.shared_indeces_cache;
	}
	
	public String getChapterLabel(int x) {
		if(this.config.size() > x) {
			ResultAgreementConfigEntry entry = this.config.get(x);
			return entry == null ? "(no label)" : entry.getLabel();
		} else {
			return "Virhe: Tietokannassa ei ole kappaletta";
		}
	}
	
	public String getChapterDescription(int x) {
		if(this.config.size() > x) {
			ResultAgreementConfigEntry entry = this.config.get(x);
			return entry == null ? "(no description)" : entry.getDescription();
		} else {
			return "Virhe: Tietokannassa ei ole kappaletta";
		}
	}
	
	public static final int MAX_LABEL_LENGTH = 16;
	public static final int MAX_DESC_LENGTH = 80;
	
	public boolean renameLabel(int x, String label) {
		if(label.length() > MAX_LABEL_LENGTH) {
			return false;
		}
		
		ResultAgreementConfigEntry entry = this.config.get(x);
		if(entry != null) {
			entry.setLabel(label);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean renameDescription(int x, String desc) {
		if(desc.length() > MAX_DESC_LENGTH) {
			return false;
		}
		
		ResultAgreementConfigEntry entry = this.config.get(x);
		if(entry != null) {
			entry.setDescription(desc);
			return true;
		} else {
			return false;
		}
	}
	
	public void setIsShared(int i, boolean is_shared) {
		if(this.isSharedIndex(i) != is_shared) {
			ResultAgreementConfigEntry entry = this.config.get(i);
			System.out.println("Setting " + i + " from Shared: " + entry.isShared + " to Shared: " + is_shared);
			entry.isShared = is_shared;
			syncCache();
		}
	}
	
	/**
	 * Find the current ResultAgreementConfiguration
	 * @param database
	 * @return
	 */
	public static ResultAgreementConfiguration getInstance(Database database) {
		for (Base b : database.enumerate()) {
			if (b instanceof ResultAgreementConfiguration) {
				return((ResultAgreementConfiguration) b);
			}
		}
		return null;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	
	private static String getDefaultLabel(int x) {
		return "Luku " + x;
	}
	
	private static String getDefaultDescription(int x) {
		return "(ei kuvausta)";
	}
	
	@Override
	public String clientIdentity() {
		return "Tulossopimusasetus";
	}
}