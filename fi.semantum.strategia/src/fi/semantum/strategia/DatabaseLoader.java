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
package fi.semantum.strategia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import fi.semantum.strategia.action.ActionCreatePrincipalMeter;
import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.configurations.DefaultInitialActiveYears;
import fi.semantum.strategia.widget.MapTypes;
import fi.semantum.strategy.db.AccessRight;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.AccountGroup;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.CommentToolAdminBase;
import fi.semantum.strategy.db.CommentToolBase;
import fi.semantum.strategy.db.DBConfiguration;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Datatype;
import fi.semantum.strategy.db.EnumerationDatatype;
import fi.semantum.strategy.db.Files;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.MapBox;
import fi.semantum.strategy.db.MapRight;
import fi.semantum.strategy.db.MdHtmlContent;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.ObjectType;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.ResultAgreementConfiguration;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.TimeConfiguration;
import fi.semantum.strategy.db.UtilsDB;

public class DatabaseLoader {

	private static final String startPageMDFileName = "startPage.md";
	private static final String faqMDFileName = "faq.md";
	private static final String databaseXLSXFileName = "database.xlsx";

	public static void validate(Main main) {
	}
	
	public static File bootstrapFile(String name) {
		return new File(Files.baseDirectory(), name);
	}

	public static File bootstrapFAQFile() {
		return bootstrapFile(faqMDFileName);
	}
	
	public static File bootstrapExcelFile() {
		return bootstrapFile(databaseXLSXFileName);
	}
		
	public static File bootstrapMarkdownFile() {
		return bootstrapFile(startPageMDFileName);
	}

	public static void migrate(Main main, Map<String, EnumerationDatatype> enumerations) {
		
		Database database = main.getDatabase();
		
		main.account = Account.find(database, DBConfiguration.getADMIN_ACCOUNT_NAME());
		
		ResultAgreementConfiguration.ensureExists(database);
		
		TimeConfiguration tc = TimeConfiguration.getInstance(database);
		if(tc == null) {
			TimeConfiguration.create(database, DefaultInitialActiveYears.getYears());
		}
		
		boolean migrated = false;

		Datatype actu = database.findByTypeAndId(Terminology.ACTUALIZATION, Datatype.class);
		if(actu == null) {
			List<String> values = new ArrayList<String>();
			values.add("Ei toteutunut");
			values.add("Osittain toteutunut");
			values.add("Toteutunut");
			String traffic = "pkv";
			enumerations.put(Terminology.ACTUALIZATION, new EnumerationDatatype(database, Terminology.ACTUALIZATION, values, traffic));
			migrated = true;
		}

		if(Relation.find(database, Relation.RESPONSIBILITY_INSTANCE) == null) {
			Relation.create(database, Relation.RESPONSIBILITY_INSTANCE);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.RESPONSIBILITY_MODEL) == null) {
			Relation.create(database, Relation.RESPONSIBILITY_MODEL);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.TAVOITE_SUBMAP) == null) {
			Relation.create(database, Relation.TAVOITE_SUBMAP);
			migrated = true;
		}

		if(Relation.find(database, Relation.LINKED_TO) == null) {
			Relation.create(database, Relation.LINKED_TO);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.HAS_CHANGE_SUGGESTION) == null) {
			Relation.create(database, Relation.HAS_CHANGE_SUGGESTION);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.PART_OF) == null) {
			Relation.create(database, Relation.PART_OF);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT) == null) {
			Relation.create(database, Relation.BASE_TO_OFFICE_ATTACHMENT);
			migrated = true;
		}

		if(Relation.find(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE) == null) {
			Relation.create(database, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.CONTAINS) == null) {
			Relation.create(database, Relation.CONTAINS);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.SEEN_BY_USER) == null) {
			Relation.create(database, Relation.SEEN_BY_USER);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.FROM_ACCOUNT) == null) {
			Relation.create(database, Relation.FROM_ACCOUNT);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.FROM_OFFICE) == null) {
			Relation.create(database, Relation.FROM_OFFICE);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.HAS_OPINION) == null) {
			Relation.create(database, Relation.HAS_OPINION);
		}
		
		if(Relation.find(database, Relation.CONTAINS_CHAPTER) == null) {
			Relation.create(database, Relation.CONTAINS_CHAPTER);
			migrated = true;
		}
		
		if(Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE) == null) {
			Relation.create(database, Relation.CONTAINS_CHAPTER_INVERSE);
			migrated = true;
		}

		if(Property.find(database, Property.CHARACTER_DESCRIPTION) == null) {
			Property.create(database, Property.CHARACTER_DESCRIPTION, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.GOAL_DESCRIPTION) == null) {
			Property.create(database, Property.GOAL_DESCRIPTION, null, false, Collections.<String>emptyList());
			migrated = true;
		}
		
		if(Property.find(database, Property.LINK_WITH_PARENT) == null) {
			Property.create(database, Property.LINK_WITH_PARENT, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.LINK_GOALS_AND_SUBMAPS) == null) {
			Property.create(database, Property.LINK_GOALS_AND_SUBMAPS, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.HAS_VISION) == null) {
			Property.create(database, Property.HAS_VISION, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.HAS_METER) == null) {
			Property.create(database, Property.HAS_METER, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.ALLOWS_OWN_OUTER_BOX) == null) {
			Property.create(database, Property.ALLOWS_OWN_OUTER_BOX, null, false, Collections.<String>emptyList());
			migrated = true;
		}

		if(Property.find(database, Property.DEFINES_OFFICES) == null) {
			Property.create(database, Property.DEFINES_OFFICES, null, false, Collections.<String>emptyList());
			migrated = true;
		}
		
		if(Property.find(database, Property.AMOUNT_OF_LEVELS) == null) {
			Property.create(database, Property.AMOUNT_OF_LEVELS, null, false, Collections.<String>emptyList());
			migrated = true;
		}
		
		if(Relation.find(database, Relation.RELATED_TO_TAG) == null) {
			Relation.create(database, Relation.RELATED_TO_TAG);
			migrated = true;
		}

		if(Property.find(database, Property.OWN_GOAL_TYPE) == null) {
			
			Property ownGoalTypeProperty = Property.create(database, Property.OWN_GOAL_TYPE, null, false, Collections.<String>emptyList());
			ObjectType painopiste = ObjectType.find(database, ObjectType.PAINOPISTE);
			migrated = true;
			
		}

		
		if(Relation.find(database, Relation.MONITORS_TAG) == null) {
			Relation.create(database, Relation.MONITORS_TAG);
			migrated = true;
		}
		
		if(Datatype.enumerate(database).isEmpty()) {
			Database.createDatatypes(database);
			migrated = true;
		}

		// Existing datatypes
        List<Datatype> types = Datatype.enumerate(database);
        for(Datatype dt : types) {
        	if(dt instanceof EnumerationDatatype) {
        		EnumerationDatatype edt = (EnumerationDatatype)dt; 
        		EnumerationDatatype vals = enumerations.get(dt.getId(database));
	        	if(vals != null) {
	        		if(!vals.getValues().equals(edt.getValues())) {
	        			edt.replace(vals);
	        			migrated = true;
	        		}
	        		enumerations.remove(dt.getId(database));
	        	}
        	}
        }
        
        for(String newEnumeration : enumerations.keySet()) {
        	
        	EnumerationDatatype edt = enumerations.get(newEnumeration);
    		database.register(edt);
			migrated = true;

        }
        
        for(MapBase b : MapBase.enumerateMapBase(database)) {
        	if(b.markup == null) {
        		System.err.println("Fixing markup for " + b.getDebugText(database));
        		b.markup = "";
				migrated = true;
        	}
        	if(b.meters == null) {
        		System.err.println("Fixing meters for " + b.getDebugText(database));
        		b.meters = new ArrayList<Meter>();
				migrated = true;
        	}
        	if(b.indicators == null) {
        		System.err.println("Fixing indicators for " + b.getDebugText(database));
        		b.indicators = new ArrayList<Indicator>();
				migrated = true;
        	}
        }
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		for(StrategyMap map : StrategyMap.enumerate(database)) {
			ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
			if(level == null) {
				System.err.println("No level for " + map.getDebugText(database));
				database.remove(map);
				migrated = true;
			}
		}
		
		
		boolean removed = true;
		while(removed) {

			removed = false;
			
			for(InnerBox b : InnerBox.enumerate(database)) {
				if(database.getPossibleMap(b) == null) {
					System.err.println("No map for " + b.getDebugText(database));
					database.remove(b);
					migrated = true;
					removed = true;
				}
			}
			
			for(OuterBox b : OuterBox.enumerate(database)) {
				if(database.getPossibleMap(b) == null) {
					System.err.println("No map for " + b.getDebugText(database));
					database.remove(b);
					migrated = true;
					removed = true;
				}
			}

		}
		
		for(StrategyMap map : StrategyMap.enumerate(database)) {
			if(!map.linkGoalsToSubmaps(database)) {
				if(map.pruneSubmapType(database)) {
					migrated = true;
				}
			}
		}

		for(StrategyMap map : StrategyMap.enumerate(database)) {
			for(OuterBox ob : map.outerBoxes) {
				if(ob.isRemoved(database)) {
					System.err.println("Prune outer box " + ob.getDebugText(database) + " from " + map.getDebugText(database));
					map.removeOuterBox(database, ob);
					migrated = true;
				}
			}
		}

		for(OuterBox ob : OuterBox.enumerate(database)) {
			for(InnerBox ib : ob.innerboxes) {
				if(ib.isRemoved(database)) {
					System.err.println("Prune inner box " + ib.getDebugText(database) + " from " + ob.getDebugText(database));
					ob.removePainopiste(database, ib);
					migrated = true;
				}
			}
		}

		for(StrategyMap map : StrategyMap.enumerate(database)) {
			
			List<Linkki> ls = new ArrayList<Linkki>();
			for(Linkki l : map.alikartat) {
				StrategyMap k = database.find(l.uuid);
				if(k == null) {
					ls.add(l);
				}
			}
			
			for(Linkki l : ls) {
				map.removeAlikartta(l.uuid);
				migrated = true;
			}

		}
		
		for(OuterBox t : OuterBox.enumerate(database)) {
			
			if(t.columns == 0) {
				t.columns = 2;
				migrated = true;
			}
			
			try {
				if(Utils.ensureImplementationMap(main, t))
					migrated = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		for(InnerBox i : InnerBox.enumerate(database)) {
			
			if(i.columns == 0) {
				i.columns = 2;
				migrated = true;
			}
			
		}

		for(StrategyMap map : StrategyMap.enumerate(database)) {
			try {
				Base sub = map.getPossibleSubmapType(database);
				if(sub != null) {
					Set<StrategyMap> proper = new HashSet<StrategyMap>();
					for(OuterBox t : map.outerBoxes) {
						try {
							StrategyMap imp = t.getPossibleImplementationMap(database);
							if(imp != null) proper.add(imp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					for(Linkki l : map.alikartat) {
						StrategyMap map2 = database.find(l.uuid);
						if(!proper.contains(map2)) {
							UtilsDB.removeMapAndRelatedObjects(database, map2);
							migrated = true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(InnerBox b : InnerBox.enumerate(database)) {
			StrategyMap map = b.getMap(database);
			CharacterInfo ci = map.getCharacterInfo(database);
			if(ci.hasMeter) {
				List<Meter> meters = b.getMeters(database);
				if(meters.size() == 1 && meters.get(0).isPrincipal) continue;
				ActionCreatePrincipalMeter.perform(main, b);
				migrated = true;
			}
		}

		// Migration
		Database mainDatabase = main.getDatabase();
		for(Base b : new ArrayList<Base>(database.enumerate())) {
			if(b.migrate(mainDatabase))
				migrated = true;
		}

		//Write md content from files if doesn't exist
		if(MdHtmlContent.getPossibleContentByID(database, MdHtmlContent.STARTPAGE) == null) {
			writeStartPageFromMarkdownFile(main, database);
			migrated = true;
		}
		
		if(MdHtmlContent.getPossibleContentByID(database, MdHtmlContent.FAQ) == null) {
			if(writeFAQFromMarkdownFile(main, database))
				migrated = true;
		}
		
		if(migrated) {
			database.save();
		}
		
		main.account = null;
		
	}
	
	/**
	 * Only allow admin accounts to login and rollback DB
	 * @param emergencyDatabase
	 * @param username
	 * @param passwordHash
	 * @return
	 */
	public static boolean emergencyLoginOK(Database emergencyDatabase, String username, String hash) {
		try {
			Account acc = Account.find(emergencyDatabase, username);
			if(acc != null) {
				boolean account_ok = acc.isAdmin(emergencyDatabase);
				if(account_ok) {
					if(hash == null || acc.getHash() == null) {
						return false;
					} else {
						return hash.equals(acc.getHash());
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			
		}
		
		return false;
	}
	
	/**
	 * Minimal load of database by id. Returns null if fails.
	 * @param databaseId
	 * @return
	 */
	public static Database emergencyLoad(String databaseId) {
		try {
			File db = Database.getDatabaseFile(databaseId);
			FileInputStream fileIn = new FileInputStream(db);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Database result = (Database) in.readObject();
			in.close();
			fileIn.close();
			result.setDatabaseId(databaseId);
			return result;
		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Database load(Main main, String databaseId) {

		Database result = null;

		synchronized(Database.class) {

			try {
				
				Map<String,EnumerationDatatype> enumerations = new HashMap<String, EnumerationDatatype>();
				
				try {
					File file = bootstrapExcelFile();
					FileInputStream fis = new FileInputStream(file);
					Workbook book = WorkbookFactory.create(fis);
					fis.close();
					Sheet sheet = book.getSheetAt(0);
					for(int rowN = sheet.getFirstRowNum();rowN<=sheet.getLastRowNum();rowN++) {
						Row row = sheet.getRow(rowN);
						Cell cell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
						if(cell != null) {
							if("Monivalinta".equals(cell.toString())) {
								Cell id = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
								if(id == null) continue;
								Cell traffic = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
								if(traffic == null) continue;
								int count = row.getLastCellNum()-3;
								if(traffic.toString().length() != count) continue;

								List<String> values = new ArrayList<String>();
								for(int i=0;i<count;i++) {
									Cell val = row.getCell(3+i, Row.RETURN_BLANK_AS_NULL);
									if(val != null)
										values.add(val.toString());
								}
								enumerations.put(id.toString(), new EnumerationDatatype(result, id.toString(), values, traffic.toString()));
							}
						}
					}
					
				} catch (Exception e) {
				}
				
				Database.makeDirectories(databaseId);
				
				File db = Database.getDatabaseFile(databaseId);
				
				FileInputStream fileIn = new FileInputStream(db);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				result = (Database) in.readObject();
				in.close();
				fileIn.close();

				result.setDatabaseId(databaseId);

				main.setDatabase(result);
				
				migrate(main, enumerations);
				validate(main);
				
				result.setLastModified(new Date(db.lastModified()));
			} catch (FileNotFoundException fnfe) {
			
				System.out.print("Creating new database, since expected DB didn't exist: ");
				System.out.println(fnfe.getMessage());
				result = create(main, databaseId);
			
			} catch(IOException i) {
				
				System.out.println("Creating new database, since expected DB didn't exist:");
				i.printStackTrace();
				result = create(main, databaseId);
				
			} catch(ClassNotFoundException c) {
				
				System.out.println("Database class not found");
				System.out.println("Creating new database, since expected DB didn't exist:");
				
				c.printStackTrace();
				result = create(main, databaseId);
				
			}
			
			result.touchBackup();

			result.updateTags();

			try {
			
				result.loadFromFile();
				
			} catch (Throwable t) {
				
				t.printStackTrace();
			
			}

		}
		
		return result;

	}

	private static Database create(Main main, String databaseId) {

		try {
			
			Database.startWrite(databaseId);

			StrategyMap rootMap;

			Database mainDatabase = new Database(databaseId);
			mainDatabase.setDatabaseId(databaseId);
			main.setDatabase(mainDatabase);

			ResultAgreementConfiguration resultAgreementConfiguration = ResultAgreementConfiguration.ensureExists(mainDatabase);
			
			TimeConfiguration tc = TimeConfiguration.getInstance(mainDatabase);
			if(tc == null) {
				TimeConfiguration.create(mainDatabase, DefaultInitialActiveYears.getYears());
			}

			Relation.create(mainDatabase, Relation.RELATED_TO_TAG);

			Relation.create(mainDatabase, Relation.IMPLEMENTS);
			Relation.create(mainDatabase, Relation.COPY);
			Relation.create(mainDatabase, Relation.MEASURES);
			Relation.create(mainDatabase, Relation.RESPONSIBILITY_INSTANCE);
			Relation.create(mainDatabase, Relation.RESPONSIBILITY_MODEL);
			Relation.create(mainDatabase, Relation.LINKED_TO);
			Relation.create(mainDatabase, Relation.HAS_CHANGE_SUGGESTION);
			Relation.create(mainDatabase, Relation.PART_OF);
			Relation.create(mainDatabase, Relation.BASE_TO_OFFICE_ATTACHMENT);
			Relation.create(mainDatabase, Relation.BASE_TO_OFFICE_ATTACHMENT_INVERSE);
			Relation.create(mainDatabase, Relation.CONTAINS);
			Relation.create(mainDatabase, Relation.CONTAINS_CHAPTER);
			Relation.create(mainDatabase, Relation.CONTAINS_CHAPTER_INVERSE);
			Relation.create(mainDatabase, Relation.FROM_ACCOUNT);
			Relation.create(mainDatabase, Relation.FROM_OFFICE);
			Relation.create(mainDatabase, Relation.HAS_OPINION);
			Relation.create(mainDatabase, Relation.SEEN_BY_USER);
			
			Relation allowsSubmap = Relation.create(mainDatabase, Relation.ALLOWS_SUBMAP);
			Relation.create(mainDatabase, Relation.MONITORS_TAG);
			
			Relation.create(mainDatabase, Relation.TAVOITE_SUBMAP);

			Property ownGoalTypeProperty = Property.create(mainDatabase, Property.OWN_GOAL_TYPE, null, false, Collections.<String>emptyList());
			Property characterColor = Property.create(mainDatabase, Property.CHARACTER_COLOR, null, false, Collections.<String>emptyList());
			Property characterTextColor = Property.create(mainDatabase, Property.CHARACTER_TEXT_COLOR, null, false, Collections.<String>emptyList());

			Property.create(mainDatabase, Property.CHARACTER_DESCRIPTION, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.GOAL_DESCRIPTION, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.LINK_WITH_PARENT, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.LINK_GOALS_AND_SUBMAPS, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.HAS_VISION, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.HAS_METER, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.ALLOWS_OWN_OUTER_BOX, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.DEFINES_OFFICES, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.AMOUNT_OF_LEVELS, null, false, Collections.<String>emptyList());			
			Property type = Property.create(mainDatabase, Property.TYPE, null, false, Collections.<String>emptyList());

			ObjectType levelType = ObjectType.create(mainDatabase, ObjectType.LEVEL_TYPE);
			
			ObjectType typeHallinnonala = ObjectType.create(mainDatabase, ObjectType.HALLINNONALA);
			typeHallinnonala.properties.add(type.make(levelType.uuid));
			typeHallinnonala.properties.add(characterColor.make("#3681D5"));
			typeHallinnonala.properties.add(characterTextColor.make("#000"));
			
			Property characterDescriptionP = Property.find(mainDatabase, Property.CHARACTER_DESCRIPTION);
			Property goalDescriptionP = Property.find(mainDatabase, Property.GOAL_DESCRIPTION);

			typeHallinnonala.properties.add(characterDescriptionP.make(Terminology.FOCUS_POINT));
			typeHallinnonala.properties.add(goalDescriptionP.make(Terminology.STRATEGIC_GOAL_OR_TARGET));

			Database.createDatatypes(mainDatabase);

			ObjectType accountType = ObjectType.create(mainDatabase, ObjectType.ACCOUNT);

			Property.create(mainDatabase, Property.LEVEL, levelType.uuid, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.AIKAVALI, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.EMAIL, null, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.OWNER, accountType.uuid, false, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.CHANGED_ON, null, true, Collections.<String>emptyList());
			Property.create(mainDatabase, Property.TTL, null, false, Collections.<String>emptyList());

			//Create default Guest Group user
			mainDatabase.guestGroup = AccountGroup.create(mainDatabase,
					Configuration.getGUEST_GROUP_NAME());
			
			//Create default System Group user
			mainDatabase.systemGroup = AccountGroup.create(mainDatabase,
					Configuration.getADMIN_GROUP_NAME());
			
			mainDatabase.guest = Account.create(mainDatabase,
					Configuration.getGUEST_ACCOUNT_NAME(),
					"",
					UtilsDB.hash(Configuration.getGUEST_ACCOUNT_PASSWORD()),
					mainDatabase.guestGroup);
			
			//Create default System user
			mainDatabase.system = Account.create(mainDatabase,
					DBConfiguration.getADMIN_ACCOUNT_NAME(),
					Configuration.getADMIN_ACCOUNT_EMAIL(),
					UtilsDB.hash(Configuration.getADMIN_ACCOUNT_PASSWORD()),
					mainDatabase.systemGroup);
			
			mainDatabase.systemGroup.setAdmin(true); //System group accounts are admins
			
			Tag LIIKENNE = mainDatabase.getOrCreateTag(Tag.LIIKENNE);
			LIIKENNE.modifyText(main.getDatabase(), mainDatabase.system, "Liikenne");
			
			Tag VIESTINTA = mainDatabase.getOrCreateTag(Tag.VIESTINTA);
			VIESTINTA.modifyText(main.getDatabase(), mainDatabase.system, "Viestintä");

			rootMap = StrategyMap.create(main.getDatabase(), "hallinnonala",
					"LVM:n hallinnonala",
					"Hyvinvointia ja kilpailukykyä hyvillä yhteyksillä", Database.EMPTY,
					Database.mapProperties(mainDatabase, typeHallinnonala));
			
			mainDatabase.systemGroup.addRightIfMissing(new MapRight(rootMap, true, true));
			//Guests cannot write to the strategy map
			mainDatabase.guestGroup.addRightIfMissing(new MapRight(rootMap, false, false));

			// To be able to create the data from configuration files, we must be admin:
			main.account = mainDatabase.system;
			CommentToolBase commentToolBase = CommentToolBase.fetchOrCreateSingleton(mainDatabase);
			CommentToolAdminBase commentToolAdminBase = CommentToolAdminBase.fetchOrCreateSingleton(mainDatabase);
			
			//Create the default controller account, office and account group.
			AccountGroup controllerAccountGroup = AccountGroup.create(mainDatabase, DBConfiguration.getCONTROLLER_ACCOUNT_NAME());
			controllerAccountGroup.addRightIfMissing(new AccessRight(commentToolBase, true));
			controllerAccountGroup.addRightIfMissing(new AccessRight(commentToolAdminBase, true));
			controllerAccountGroup.addRightIfMissing(new MapRight(rootMap, true, true));
			
			Office controllerOffice = Office.create(mainDatabase, DBConfiguration.getCONTROLLER_OFFICE_SHORT_NAME(),
					DBConfiguration.getCONTROLLER_ACCOUNT_NAME(), true);
			Account controllerAccount = Account.create(mainDatabase, DBConfiguration.getCONTROLLER_ACCOUNT_NAME(),
					Configuration.getADMIN_ACCOUNT_EMAIL(),
					UtilsDB.hash(Configuration.getADMIN_ACCOUNT_PASSWORD()),
					controllerAccountGroup);
			UtilsDB.associateAccountGroupWithOffice(mainDatabase, controllerOffice, controllerAccountGroup);
			UtilsDB.associateAccountWithOffice(mainDatabase, controllerOffice, controllerAccount);
			
			//System account is also part of the controllers:
			mainDatabase.system.addAccountGroup(mainDatabase, controllerAccountGroup);
			UtilsDB.associateAccountWithOffice(mainDatabase, controllerOffice, mainDatabase.system);
			
			writeStartPageFromMarkdownFile(main, mainDatabase);
			writeFAQFromMarkdownFile(main, mainDatabase);
			
			mainDatabase.systemGroup.addRightIfMissing(new AccessRight(commentToolBase, true));
			mainDatabase.systemGroup.addRightIfMissing(new AccessRight(commentToolAdminBase, true));
			
			try {
				AccountGroup commentToolBaseWriteGroup = AccountGroup.create(mainDatabase, "Comment Tool Write Group");
				commentToolBaseWriteGroup.addRightIfMissing(new AccessRight(CommentToolBase.fetchOrCreateSingleton(mainDatabase), true));

			} catch (IllegalArgumentException e) {
				System.err.println("Failed to read configuration xlsx. Skipping office creation step. Root cause for read failure:");
				e.printStackTrace();
			}
			
			
			if(resultAgreementConfiguration.getAllIndeces().length != 0) {
				ResultAgreementDocuments resultAgreementDocuments = ResultAgreementDocuments.create(mainDatabase, "Tulossopimus", "Tulossopimus");
				
				//Links the singleton ResultAgreementDocuments with the root map
				UtilsDB.linkMap(mainDatabase, resultAgreementDocuments, rootMap);
				
				//Shared chapters that aren't associated with any offices.
				Integer[] sharedChapters = resultAgreementConfiguration.getSharedIndeces();
				for(int i : sharedChapters) {
					TextChapter chapter = TextChapter.create(main.getDatabase(),
							resultAgreementConfiguration.getChapterDescription(i),
							resultAgreementConfiguration.getChapterLabel(i),
							i, "Auto-Generated Stub", null);
					boolean success = resultAgreementDocuments.registerChapter(mainDatabase, chapter);
					if(!success) {
						System.err.println("Failed to add chapter to ResultAgreementDocuments");
					}
				}
					
				mainDatabase.systemGroup.addRightIfMissing(new AccessRight(resultAgreementDocuments, true));
				controllerAccountGroup.addRightIfMissing(new AccessRight(resultAgreementDocuments, true));
					
				//Guests cannot write to the result agreement document
				mainDatabase.guestGroup.addRightIfMissing(new AccessRight(resultAgreementDocuments, false));
			} else {
				System.err.println("No chapter indeces defined, skipping office, text chapter and corresponding account group creation!");
			}
			
			writeDatabaseFromConfigurationFile(main, mainDatabase);
			//User should be logged out at start of session, since we logged in as admin earlier!
			main.account = null;
			
			mainDatabase.prepareAll(main.getDatabase());
			mainDatabase.save();

			Database.endWrite();

			return mainDatabase;

		} catch (IOException e) {
			//Ensure we close the writer when creating new DB fails
			try {
				Database.endWrite();
			} catch(NullPointerException npe) {
				npe.printStackTrace();
			}
			throw new Error(e);
			
		}

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
	
	private static void writeStartPageFromMarkdownFile(Main main, Database mainDatabase) {
		try {
			File file = bootstrapMarkdownFile();
			FileInputStream fis = new FileInputStream(file);
			String markdown = fileInputStreamToUTF8String(fis);
			fis.close();
			MdHtmlContent.create(mainDatabase, MdHtmlContent.STARTPAGE, markdown);
			MdHtmlContent link = MdHtmlContent.create(mainDatabase, MdHtmlContent.TARGET_WEBSITE_LINK, Configuration.getTARGET_WEBSITE_LINK());
			link.description = "Link";
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static boolean writeFAQFromMarkdownFile(Main main, Database mainDatabase) {
		try {
			File file = bootstrapFAQFile();
			FileInputStream fis = new FileInputStream(file);
			String markdown = fileInputStreamToUTF8String(fis);
			fis.close();
			MdHtmlContent content = MdHtmlContent.getOrCreateByID(mainDatabase, MdHtmlContent.FAQ);
			content.attemptSetContent(markdown);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Read database.xlsx and configure DB accordingly
	 * @param main
	 * @param mainDatabase
	 */
	private static void writeDatabaseFromConfigurationFile(Main main, Database mainDatabase) {
		try {
			Property characterDescriptionP = Property.find(mainDatabase, Property.CHARACTER_DESCRIPTION);
			Property goalDescriptionP = Property.find(mainDatabase, Property.GOAL_DESCRIPTION);
			
			File file = bootstrapExcelFile();
			FileInputStream fis = new FileInputStream(file);
			Workbook book = WorkbookFactory.create(fis);
			fis.close();
			Sheet sheet = book.getSheetAt(0);
			int last = sheet.getLastRowNum();

			String currentTime = main.getUIState().getTime();
			
			for(int rowN = sheet.getFirstRowNum();rowN<=last;rowN++) {
				Row row = sheet.getRow(rowN);
				Cell cell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
				if(cell != null) {
					if("Karttatyyppi".equals(cell.toString())) {
						
						Cell name = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
						if(name == null) continue;

						Cell ulompi = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
						Cell sisempi = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
						if(sisempi == null) continue;

						Cell lwp = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
						if(lwp == null) continue;
						Cell gs = row.getCell(5, Row.RETURN_BLANK_AS_NULL);
						if(gs == null) continue;

						Cell alityyppi = row.getCell(6, Row.RETURN_NULL_AND_BLANK);
						Cell definesOfficeCell = row.getCell(7, Row.RETURN_NULL_AND_BLANK);
						
						ObjectType newType = null;
						if(definesOfficeCell.getBooleanCellValue()) {
							newType = MapTypes.createMapTypeOfficeDefiner(main, mainDatabase, name.getStringCellValue(), lwp.getBooleanCellValue(), gs.getBooleanCellValue());
						} else {
							newType = MapTypes.createMapType(main, mainDatabase, name.getStringCellValue(), lwp.getBooleanCellValue(), gs.getBooleanCellValue());
						}
						if(ulompi != null && !ulompi.getStringCellValue().isEmpty()) {
							Utils.setProperty(goalDescriptionP, main, main.getDatabase(), newType, ulompi.getStringCellValue());
						}
						if(!sisempi.getStringCellValue().isEmpty()) {
							Utils.setProperty(characterDescriptionP, main, main.getDatabase(), newType, sisempi.getStringCellValue());
						}
						if(alityyppi != null && !alityyppi.getStringCellValue().isEmpty()) {
							
							Relation goalSubmapTypeR = Relation.find(mainDatabase, Relation.TAVOITE_SUBMAP);

							ObjectType b = mainDatabase.findByTypeAndId(alityyppi.getStringCellValue(), ObjectType.class);
							if(b != null) {
								newType.addRelation(goalSubmapTypeR, b);
							}
							
						}
						
					} else if("Kartta".equals(cell.toString())) {

						Cell mapType = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
						if(mapType == null) continue;
						Cell parent = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
						if(parent == null) continue;
						Cell id = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
						if(id == null) continue;
						Cell name = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
						if(name == null) continue;
						
						StrategyMap parentMap = mainDatabase.findByTypeAndId(parent.getStringCellValue(), StrategyMap.class);
						ObjectType typeB = mainDatabase.findByTypeAndId(mapType.getStringCellValue(), ObjectType.class);
						mainDatabase.newMap(main.getDatabase(), parentMap, id.getStringCellValue(), name.getStringCellValue(), typeB);
						
					} else if("Ulompi".equals(cell.toString())) {
						
						Cell time = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
						Cell mapId = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
						if(mapId == null) continue;
						Cell impl = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
						Cell id = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
						
						StrategyMap map = mainDatabase.findByTypeAndId(mapId.getStringCellValue(), StrategyMap.class);
						if(impl != null && !impl.getStringCellValue().isEmpty()) {
							InnerBox copy = mainDatabase.findByTypeAndId(impl.getStringCellValue(), InnerBox.class);
							OuterBox t = Utils.createOuterBoxCopy(main, map, copy);
							Utils.ensureImplementationMap(main, t);
							Utils.modifyValidity(main, t, parseTime(time));
						} else {
							OuterBox t = OuterBox.create(mainDatabase, map, id.getStringCellValue(), id.getStringCellValue());
							Utils.ensureImplementationMap(main, t);
							Utils.modifyValidity(main, t, parseTime(time));
						}
						
						
					} else if("Sisempi".equals(cell.toString())) {

						Cell time = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
						Cell mapId = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
						Cell impl = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
						Cell id = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
						if(id == null) continue;

						StrategyMap map = resolveExcelMap(mainDatabase, mapId, impl);

						MapBox box = mainDatabase.findByTypeAndId(impl.getStringCellValue(), MapBox.class);
						
						if(box instanceof InnerBox) {
							InnerBox p = (InnerBox)box;
							box = p.getPossibleImplementationGoal(mainDatabase); //This is an OuterBox?
						}
						
						OuterBox outerBox = (OuterBox)box;
						Utils.ensureImplementationMap(main, outerBox);
						InnerBox pp = InnerBox.create(main.getDatabase(), currentTime, map, outerBox, id.getStringCellValue(), id.getStringCellValue());
						try {
							StrategyMap submap = outerBox.getPossibleImplementationMap(mainDatabase);
							if(submap != null) {
								Utils.createOuterBoxCopy(main, submap, pp);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						Utils.modifyValidity(main, pp, parseTime(time));

					}
					
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private static String parseTime(Cell timeCell) throws Exception {
		if(timeCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			Double val = timeCell.getNumericCellValue();
			return Integer.toString(val.intValue());
		} else {
			return timeCell.getStringCellValue();
		}
		
	}

	private static StrategyMap resolveExcelMap(Database database, Cell mapCell, Cell outerCell) throws Exception {
		if(mapCell != null) {
			return database.findByTypeAndId(mapCell.getStringCellValue(), StrategyMap.class);
		} else {
			InnerBox p = database.findByTypeAndId(outerCell.getStringCellValue(), InnerBox.class);
			OuterBox t = p.getGoal(database);
			return t.getPossibleImplementationMap(database);
		}
	}

}
