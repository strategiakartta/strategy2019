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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Database implements Serializable {

	public static void endWrite() {
		Lucene.endWrite();
	}
	
	public List<String> searchFileSystem(String input) throws IOException {
		return Lucene.search(getDatabaseId(), input);
	}
	
	public static void startWrite(String databaseId) throws IOException {
		Lucene.startWrite(databaseId);
	}
	
	public Account getDefaultAdminAccount() {
		if(system != null) return system;
		else return Account.find(this, DBConfiguration.getADMIN_ACCOUNT_NAME());
	}
	
	public AccountGroup getDefaultAdminAccountGroup() {
		if(systemGroup != null) return systemGroup;
		else return AccountGroup.find(this, DBConfiguration.getADMIN_ACCOUNT_NAME());
	}
	
	public Account getDefaultControllerAccount() {
		return Account.find(this, DBConfiguration.getCONTROLLER_ACCOUNT_NAME());
	}
	
	public AccountGroup getDefaultControllerAccountGroup() {
		return AccountGroup.find(this, DBConfiguration.getCONTROLLER_ACCOUNT_NAME());
	}
	
	/**
	 * Loads the Database from file according to ID and configured path
	 * @throws IOException
	 */
	public void loadFromFile() throws IOException {
		if(!Lucene.indexExists(databaseId)) {
			
	    	getLuceneDirectory(databaseId).mkdirs();

			System.err.println("Reset Lucene index for " + databaseId);

			Lucene.startWrite(databaseId);
			for(Base b : this.enumerate()) {
				Lucene.set(databaseId, b.uuid, b.searchText(this));
			}
			Lucene.endWrite();
			
		}
	}
	
	public static final String[] hues = new String[] {

		"#E03774",
		"#7BEB35",
		"#5CDBE8",
		"#323517",
		"#7A6CE7",
		"#EE9626",
		"#E4B8AF",
		"#4A214B",
		"#E5E67C",
		"#597EAE",
		"#792518",
		"#3FA348",
		"#DA3EE1",
		"#E44125",
		"#A761AB",
		"#87E5AC",
		"#838B23",
		"#BD8456",
		"#74625F",
		"#CAB9E5",
		"#C6E536",
		"#57999A",
		"#8AA978",
		"#D885A3",
		"#D1E2C5",
		"#4C5CAD",
		"#78B224",
		"#61E485",
		"#47705A",
		"#A03167",
		"#D67064",
		"#45262E",
		"#5D5A1A",
		"#E038B9",
		"#406578",
		"#A4D881",
		"#AE4F21",
		"#BB9223",
		"#86513B",
		"#9E9F90",
		"#A13EAD",
		"#9E87DC",
		"#3B762F",
		"#887B95",
		"#80734E",
		"#37C033",
		"#47A77B",
		"#C07AE9",
		"#E23697",
		"#51AAD8",
		"#DBD9DE",
		"#2E314C",
		"#273733",
		"#5882EA",
		"#DE6CCB",
		"#822738",
		"#895264",
		"#D63853",
		"#374F82",
		"#DBDCA0",
		"#D1AD83",
		"#B8E163",
		"#DB6094",
		"#2D5121",
		"#815C8D",
		"#372C68",
		"#936123",
		"#E85E4F",
		"#95CFCC",
		"#6C4EB3",
		"#D9C06E",
		"#B1AD26",
		"#89914E",
		"#962B7D",
		"#64DDC3",
		"#A858E2",
		"#E37A39",
		"#D89084",
		"#7097DE",
		"#A82B26",
		"#E9DE3A",
		"#491C14",
		"#CE6477",
		"#9FBCDB",
		"#703C88",
		"#EAAB67",
		"#A68D3E",
		"#EDBA38",
		"#CF98DD",
		"#E1A9CC",
		"#689A38",
		"#B6949C",
		"#7DE45E",
		"#641C42",
		"#E066EA",
		"#6C3F11",
		"#C6873D",
		"#DE73B4",
		"#C8C454",
		"#B97A1B"

	};

	private static final long serialVersionUID = 7219126520246069099L;

	public int tagNumber = 0;

	public Map<String,Base> objects = new HashMap<String,Base>();

	transient private Date lastModified;
	transient private Map<String,Tag> tagMap;
	transient private String databaseId = DBConfiguration.getDATABASE_ID(); //Default value from config defaults

	public AccountGroup guestGroup;
	public Account guest;
	
	public AccountGroup systemGroup;
	public Account system;
	
	public Database(String databaseId) {
		this.databaseId = databaseId;
	}

	public String getDatabaseId() {
		return databaseId;
	}
	
	public void updateTags() {
		tagMap = new HashMap<String, Tag>();
		for(Tag t : Tag.enumerate(this)) tagMap.put(t.getId(this), t);
	}

	public void setDatabaseId(String databaseId) {
		this.databaseId = databaseId;
	}
	
	public String createHue() {
		String color = hues[tagNumber % hues.length];
		tagNumber++;
		return color;
	}
	
	public Tag getOrCreateTag(String id) {
		updateTags();
		Tag result = tagMap.get(id);
		if(result == null) {
			String color = createHue();
			result = Tag.create(this, id, id, color);
			updateTags();
		}
		return result;
	}
	
	private static final String DATABASE_FILE_NAME = "database";
	static final String BACKUP_FILE_PREFIX = Database.DATABASE_FILE_NAME + "-";
	static final String HOURLY_DIRECTORY_PREFIX = "hourly-";
    private static final String LUCENE_DIRECTORY_NAME = "lucene";
    private static final String PRINTING_DIRECTORY_NAME = "printing";

	public static File getDatabaseFile(String databaseId) {
		return new File(Files.getDatabaseDirectory(databaseId), DATABASE_FILE_NAME);
	}

	public File getDatabaseFile() {
		return getDatabaseFile(databaseId);
	}

	public static File getDatabaseDirectory(String databaseId) {
		return Files.getDatabaseDirectory(databaseId);
	}

	public File getDatabaseDirectory() {
		return getDatabaseDirectory(databaseId);
	}

	public File getDatabaseBackupFilePlain() {
		return new File(getDatabaseDirectory(), "backup-" + DATABASE_FILE_NAME);
	}

	public File getDatabaseBackupFile(Date date) {
		String suffix = UtilsDB.backupDateFormat.format(date);
		return new File(getDatabaseDirectory(), BACKUP_FILE_PREFIX + suffix);
	}

	static File getPrintingDirectory(String databaseId) {
		return new File(Files.getDatabaseDirectory(databaseId), PRINTING_DIRECTORY_NAME);
	}

	public File getPrintingDirectory() {
		return getPrintingDirectory(databaseId);
	}

	static File getLuceneDirectory(String databaseId) {
    	return new File(Files.getDatabaseDirectory(databaseId), LUCENE_DIRECTORY_NAME);
    }
    
	File getBackupDirectory(String dirName) {
    	return new File(Files.getDatabaseDirectory(databaseId), dirName);
    }

	public static void makeDirectories(String databaseId) {
    	getDatabaseDirectory(databaseId).mkdirs();
    	getPrintingDirectory(databaseId).mkdirs();
    }
	
	public void save() {
		synchronized(Database.class) {
			try {
				File db = getDatabaseFile();
				FileOutputStream fileOut = new FileOutputStream(db);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this);
				out.close();
				fileOut.close();
				lastModified = new Date(db.lastModified());
				System.out.println("Serialized data is saved in " + db.getAbsolutePath() + " " + lastModified);
				
				touchBackup();
				
			} catch(IOException i) {
				i.printStackTrace();
			}
		}
	}
	
	// 5 min backup
	private static final long BACKUP_CYCLE_MS = 5*60*1000;

	private static final int WARP_SPEED = 1;
	
	/*
	 * This can be used to speed up the passage of time for testing backups
	 */
	public Date warp(Date now) {
		if(WARP_SPEED > 1) {
			try {
				Date baseline = UtilsDB.dailyDateFormat.parse("2019-05-16");
				long diff = now.getTime() - baseline.getTime();
				return new Date(baseline.getTime() + WARP_SPEED*diff);
			} catch (ParseException e) {
			}
		}
		return now;
	}
	
	public Date now() {
		return warp(new Date());
	}
	
	public long lastModified(File f) {
		return f.lastModified();
	}
	
	public void backupCurrentDatabase() {

		try {
			
			java.nio.file.Files.copy( 
					getDatabaseFile().toPath(), 
					getDatabaseBackupFile(now()).toPath(),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING,
					java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
					java.nio.file.LinkOption.NOFOLLOW_LINKS );

		} catch (IOException e) {

			e.printStackTrace();

		}
		
	}

	public void touchBackup() {
	
		try {
			
			File f = getDatabaseBackupFilePlain();
			if(!f.exists()) {
				f.createNewFile();
			}

			Date nowDate = now();
			long now = nowDate.getTime();
			long last = lastModified(f);
			
			if(now-last > BACKUP_CYCLE_MS) {

				f.setLastModified(now);

				DatabaseManager.getInstance().reorganizeBackups(this, nowDate);
				
				System.err.println("save database as " + getDatabaseBackupFile(nowDate).getName());

				backupCurrentDatabase();

			}
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} catch (Throwable t) {

			t.printStackTrace();

		}

	}
	
	public boolean checkChanges() {
		
		File f = getDatabaseFile();
		Date d = new Date(f.lastModified());
		
		int comp = d.compareTo(lastModified);
		
		if(comp > 0) {
			//System.err.println("Changed: " + lastModified + " vs. " + d);
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T find(String uuid) {
		Base b = objects.get(uuid);
		if(b != null) return (T)b;
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T findByTypeAndId(String id, Class<T> clazz) {
		for(Base b : objects.values()) {
			if(clazz.isInstance(b)) {
				if(id.equals(b.getId(this))) return (T)b;
			}
		}
		return null;
	}

	public Collection<Base> instances(ObjectType type) {
		Property typeProperty = Property.find(this, Property.TYPE);
		ArrayList<Base> result = new ArrayList<Base>();
		for(Base b : objects.values()) {
			Base t = typeProperty.getPropertyValueObject(this, b);
			if(t != null) {
				if(type.uuid == t.uuid) {
					result.add(b);
				}
			}
		}
		return result;
	}
	
	public Collection<Base> enumerate() {
		return objects.values();
	}
	
	public Collection<MapBase> enumerateMapBase(){
		Collection<Base> all = enumerate();
		Collection<MapBase> result = new HashSet<>();
		for(Base b : all) {
			if(b instanceof MapBase) {
				result.add((MapBase)b);
			}
		}
		return result;
	}

	public StrategyMap getRoot() {
		for(StrategyMap map : StrategyMap.enumerate(this)) {
			if(map.parents.length == 0) return map;
		}
		return null;
	}

	public static final Collection<Pair> EMPTY = Collections.emptyList();

	public static Pair[] mapProperties(Database database, Base level) {

		Account owner = Account.find(database, DBConfiguration.getADMIN_ACCOUNT_NAME());

		Pair p1 = Pair.make(Property.find(database, Property.LEVEL).uuid, level.uuid);
		Pair p3 = Pair.make(Property.find(database, Property.OWNER).uuid, owner.getId(database));
		Pair p4 = Pair.make(Property.find(database, Property.EMAIL).uuid, "");
		Pair p5 = Pair.make(Property.find(database, Property.CHANGED_ON).uuid, UtilsDB.simpleDateFormat.format(new Date()));
		Pair p6 = Pair.make(Property.find(database, Property.TTL).uuid, "30");

		return new Pair[] { p1, p3, p4, p5, p6 };

	}

	public void setLastModified(Date d) {
		this.lastModified = d;
	}
	
	public static Pair[] goalProperties(Database database, StrategyMap map) {

		Account owner = Account.find(database, DBConfiguration.getADMIN_ACCOUNT_NAME());
		Property levelProperty = Property.find(database, Property.LEVEL);

		ArrayList<Pair> result = new ArrayList<Pair>();
		
    	result.add(Pair.make(Property.find(database, Property.AIKAVALI).uuid, "Kaikki"));
		result.add(Pair.make(Property.find(database, Property.OWNER).uuid, owner.uuid));
		result.add(Pair.make(Property.find(database, Property.EMAIL).uuid, ""));
		result.add(Pair.make(Property.find(database, Property.CHANGED_ON).uuid, UtilsDB.simpleDateFormat.format(new Date())));
		result.add(Pair.make(Property.find(database, Property.TTL).uuid, "30"));

		return result.toArray(new Pair[result.size()]);
		
	}

	public static Pair[] focusProperties(Database database, StrategyMap map) {

		Account owner = Account.find(database, DBConfiguration.getADMIN_ACCOUNT_NAME());

		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		ArrayList<Pair> result = new ArrayList<Pair>();

		result.add(Pair.make(Property.find(database, Property.AIKAVALI).uuid, Property.AIKAVALI_KAIKKI));
		result.add(Pair.make(Property.find(database, Property.OWNER).uuid, owner.uuid));
		result.add(Pair.make(Property.find(database, Property.EMAIL).uuid, ""));
		result.add(Pair.make(Property.find(database, Property.CHANGED_ON).uuid, UtilsDB.simpleDateFormat.format(new Date())));
		result.add(Pair.make(Property.find(database, Property.TTL).uuid, "30"));

		return result.toArray(new Pair[result.size()]);

	}
	
	public static void createDatatypes(Database database) {
		database.register(new NumberDatatype(database));
	}

	public static String fileInputStreamToUTF8String(FileInputStream fis) {
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			
			while((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static class TagMatch {
		public StrategyMap kartta;
		public OuterBox tavoite;
		public InnerBox painopiste;
		public Set<String> tags;
	} 

	private Set<String> matchTags(Set<String> any, Collection<String> all, MapBase container) {

		// First check all required tags
		Set<String> contained = new HashSet<String>();
		for(Tag t : container.getRelatedTags(this))
			contained.add(t.uuid);

		contained.retainAll(all);
		if(contained.size() != all.size()) return Collections.emptySet();

		// Then collect overlapping tags
		Set<String> overlap = new HashSet<String>();
		for(Tag t : container.getRelatedTags(this)) {
			if(any.contains(t.uuid)) overlap.add(t.uuid);
		}
		return overlap;

	}

	public Collection<TagMatch> findByTags(Collection<String> atLeastOne, Collection<String> all) {

		Set<String> tagSet = new HashSet<String>();
		for(String tag : atLeastOne) tagSet.add(tag);
		List<TagMatch> result = new ArrayList<TagMatch>();
		for(StrategyMap map : StrategyMap.enumerate(this)) {

			for(OuterBox t : map.outerBoxes) {

				Set<String> overlap = matchTags(tagSet, all, t);
				if(!overlap.isEmpty()) {
					TagMatch m = new TagMatch();
					m.kartta = map;
					m.tavoite = t;
					m.painopiste = null;
					m.tags = overlap;
					result.add(m);
				}

				for(InnerBox p : t.innerboxes) {

					overlap = matchTags(tagSet, all, p);
					if(!overlap.isEmpty()) {
						TagMatch m = new TagMatch();
						m.kartta = map;
						m.tavoite = t;
						m.painopiste = p;
						m.tags = overlap;
						result.add(m);
					}

				}
			}
		}

		return result;

	}

	public StrategyMap newMap(Database originDatabase, StrategyMap parent, String id, String name, Base level) {
		StrategyMap newMap = StrategyMap.create(originDatabase, id, name, "", EMPTY, mapProperties(this, level));
		if(parent != null) {
			parent.addAlikartta(newMap);
			newMap.showVision = parent.showVision; 
		}
		

		if(newMap != null && level instanceof ObjectType) {
			ObjectType objectType = (ObjectType)level;
			Property definesOfficesP = Property.find(originDatabase, Property.DEFINES_OFFICES);
			String v = definesOfficesP.getPropertyValue(objectType);
			if(v != null && v.equals("true")) {
				UtilsDB.createAndSynchronizeNewOffice(originDatabase, newMap, objectType);
			}
		}
		
		return newMap;

	}
	
	public StrategyMap newMap(StrategyMap parent, String id, String name, Base level) {
		return newMap(this, parent, id, name, level);
	}

	public void remove(StrategyMap map) {
		if(map.parents.length == 1) {
			StrategyMap parent = find(map.parents[0].uuid);
			parent.removeAlikartta(map.uuid);
		}
		for(Linkki l : map.alikartat) {
			StrategyMap child = find(l.uuid);
			remove(child);
		}
		objects.remove(map.uuid);
	}
	
	public void remove(Base base) {
		objects.remove(base.uuid);
	}

	public void assertTags(Collection<String> tagName) {
		for(String tag : tagName) getOrCreateTag(tag);
	}

	public void register(Base b) {
		
		String className = b.getClass().getSimpleName();
		ObjectType t = ObjectType.find(this, className);
		if(t != null)
			b.properties.add(Pair.make(Property.find(this, Property.TYPE).uuid, t.uuid));

		Property aika = Property.find(this, Property.AIKAVALI);
		if(aika != null) {
			String validity = aika.getPropertyValue(b);
			if(validity == null)
				aika.set(false, this, this.getDefaultAdminAccount(), b, Property.AIKAVALI_KAIKKI);
		} else {
			System.out.println("Time is null in register(Base b)");
		}
		
		objects.put(b.uuid, b);
		
		try {
			Lucene.set(databaseId, b.uuid, b.searchText(this));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void prepareAll(Database database) {
		for(StrategyMap map : StrategyMap.enumerate(this)) {
			map.prepare(database);
		}
	}

	public OuterBox getTavoite(InnerBox b) {
		for(StrategyMap map : StrategyMap.enumerate(this)) {
			for(OuterBox t : map.outerBoxes) {
				for(InnerBox p : t.innerboxes) {
					if(p.uuid.equals(b.uuid)) {
						return t;
					}
				}
			}
			if(map.voimavarat != null) {
				for(InnerBox p : map.voimavarat.innerboxes) {
					if(p.uuid.equals(b.uuid)) {
						return map.voimavarat;
					}
				}
			}
		}
		return null;
	}
	
	public StrategyMap getPossibleMap(MapBase b) throws RuntimeException {
		try {
			return getMap(b);
		} catch (RuntimeException e) {
			System.err.println("getPossibleMap returns null for " + b.getDebugText(this));
			return null;
		}
	}

	public StrategyMap getMap(MapBase b) throws RuntimeException {
		if (b instanceof StrategyMap) {
			return (StrategyMap)b;
		}
		if (b instanceof OuterBox) {
			for(StrategyMap map : StrategyMap.enumerate(this)) {
				for(OuterBox t : map.outerBoxes) {
					if(t.uuid.equals(b.uuid))
						return map;
				}
				if(map.voimavarat != null && map.voimavarat.uuid.equals(b.uuid))
					return map;
			}
		} else if (b instanceof InnerBox) {
			StrategyMap result = null;
			for(StrategyMap map : StrategyMap.enumerate(this)) {
				for(OuterBox t : map.outerBoxes) {
					for(InnerBox p : t.innerboxes) {
						if(p.uuid.equals(b.uuid)) {
							if(result != null && result != map) return null;
							result = map;
						}
					}
				}
				if(map.voimavarat != null) {
					for(InnerBox p : map.voimavarat.innerboxes) {
						if(p.uuid.equals(b.uuid)) {
							if(result != null && result != map) return null;
							result = map;
						}
					}
				}
			}
			return result;
		} else if (b instanceof Meter) {
			for(MapBase b2 : enumerateMapBase()) {
				for(Meter m : b2.getMeters(this)) {
					if(m.equals(b))
						return getMap(b2);
				}
			}
		} else if (b instanceof Indicator) {
			for(MapBase b2 : enumerateMapBase()) {
				for(Indicator i : b2.getIndicators(this)) {
					if(i.equals(b)) {
						return getMap(b2);
					}
				}
			}
			for(Meter m : Meter.enumerate(this)) {
				Indicator i = m.getPossibleIndicator(this);
				if(b.equals(i)) {
					return getMap(m);
				}
			}
		}
		throw new RuntimeException("No map for " + b + ". " + b.getText(this));
	}

	public String getType(MapBase b) {
		if(b instanceof StrategyMap) {
			return "Strategiakartta";
		} else if (b instanceof OuterBox) {
			StrategyMap map = getMap(b);
			return map.outerDescription;
		} else if (b instanceof InnerBox) {
			StrategyMap map = getMap(b);
			return map.innerDescription;
		} else {
			return b.getClass().getSimpleName(); 
		}
	}

	public Collection<MapBase> getInverse(MapBase b, Relation r) {
		Set<MapBase> result = new HashSet<MapBase>();
		for(MapBase b2 : enumerateMapBase()) {
			for(Base b3 : b2.getRelatedObjects(this, r))
				if(b3.equals(b))
					result.add(b2);
		}
		return result;
	}
	
	public MapBase getDefaultParent(MapBase b) {
		if(b instanceof StrategyMap) {
			StrategyMap s = (StrategyMap)b;
			return s.getPossibleParent(this);
		} else if (b instanceof OuterBox) {
			return getMap(b);
		} else if (b instanceof InnerBox) {
			return getTavoite((InnerBox)b);
		} else {
			return null;
		}
	}
	
	public List<MapBase> getDefaultPath(MapBase b) {
		MapBase parent = getDefaultParent(b);
		if(parent == null) {
			ArrayList<MapBase> result = new ArrayList<MapBase>();
			result.add(b);
			return result;
		} else {
			List<MapBase> parentPath = getDefaultPath(parent);
			parentPath.add(b);
			return parentPath;
		}
	}

}
