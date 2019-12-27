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
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class DBConfiguration {
	
	//Default values
	private static final String DEFAULT_DATABASE_ID = "root";
	private static final String DEFAULT_BASE_DIRECTORY_PATH = "./strategia-db";
	private static final String DEFAULT_ADMIN_ACCOUNT_NAME = "System";
	
	/**
	 * Less powerful account than System, that is not able to create new accounts and groups, but is allowed to edit most other things.
	 */
	private static final String DEFAULT_CONTOLLER_ACCOUNT_NAME = "Tulosohjaajat";
	
	/**
	 * Chapter config as: TOTAL:SHARED
	 * E.g: 9:1,2,3 means 9 chapters, indeces 1 2 and 3 are shared
	 * Replaced SHARED_CHAPTER_INDECES; OFFICE_CHAPTER_INDECES.
	 */
	private static final String DEFAULT_CHAPTER_CONFIG = "9:1,2,3";
	
	//Environment variables
	private static final String ENV_DATABASE_ID 			= "strategia_kartta_database_id";
	private static final String ENV_KEY_BASE_DIRECTORY_PATH = "strategia_kartta_base_directory";
	private static final String ENV_ADMIN_ACCOUNT_NAME 		= "strategia_kartta_admin_account_name";
	private static final String ENV_CHAPTER_CONFIG			= "strategia_kartta_chapter_config";
	private static final String ENV_CONTROLLER_ACCOUNT_NAME = "strategia_kartta_controller_account_name";
	
	//Changeable variables
	private static String DATABASE_ID = null;
	private static String BASE_DIRECTORY_PATH = null;
	private static String ADMIN_ACCOUNT_NAME = null;
	private static String CONTROLLER_ACCOUNT_NAME = null;
	//Configurations for chapter definitions and where the map comes.
	private static int[] SHARED_CHAPTER_INDECES = null;
	private static int[] OFFICE_CHAPTER_INDECES = null;
	private static int[] ALL_CHAPTER_INDECES = null;
	
	public static String getCONTROLLER_OFFICE_SHORT_NAME() {
		String full = getCONTROLLER_ACCOUNT_NAME();
		if(full.length() <= 3) {
			return full.toUpperCase();
		} else {
			String id = full.substring(0, 2);	
			return id.toUpperCase();
		}
	}
	public static String getDATABASE_ID()			{ return DATABASE_ID; }
	public static String getBaseDirectoryPath()		{ return BASE_DIRECTORY_PATH; }
	public static String getADMIN_ACCOUNT_NAME()	{ return ADMIN_ACCOUNT_NAME; }
	public static String getCONTROLLER_ACCOUNT_NAME() { return CONTROLLER_ACCOUNT_NAME; }
	
	/**
	 * Should be used by Database class only, during creation of DB
	 * @return
	 */
	public static int[] getDEFAULT_OFFICE_CHAPTER_INDECES() 		{ return OFFICE_CHAPTER_INDECES; }
	
	/**
	 * Should be used by Database class only, during creation of DB
	 * @return
	 */
	public static int[] getDEFAULT_ALL_CHAPTER_INDECES() 	{ return ALL_CHAPTER_INDECES; }
	
	/**
	 * Should be used by Database class only, during creation of DB
	 * @return
	 */
	public static int[] getDEFAULT_SHARED_CHAPTER_INDECES() { return SHARED_CHAPTER_INDECES; }
	
	public static void init() {
		updateCHAPTER_CONFIG();
		updateDATABASE_ID();
		updateBaseDirectoryPath();
		updateADMIN_ACCOUNT_NAME();
		updateCONTROLLER_ACCOUNT_NAME();
	}
	
	/**
	 * Example:  9:1,2,3:5
	 */
	private static void updateCHAPTER_CONFIG() {
		String chapterConfig = System.getenv(ENV_CHAPTER_CONFIG);
		if(chapterConfig == null) {
			chapterConfig = DEFAULT_CHAPTER_CONFIG;
		}
		System.out.println("Using CHAPTER_CONFIG " + chapterConfig);
		String[] options = chapterConfig.split(":");
		
		if(options.length != 2) {
			System.err.println("Incorrect CHAPTER_CONFIG!  Disabling chapters. Not enough parameters (expected 2)");
			disableChapters();
		} else {
			try {
				boolean allok = true;
				
				int total = Integer.parseInt(options[0]);
				String[] shared_temp = options[1].split(",");
				
				int[] shared = new int[shared_temp.length];
			
				for(int i = 0; i < shared_temp.length; i++) {
					int v = Integer.parseInt(shared_temp[i]);
					shared[i] = v;
					if(v >= total) {
						allok = false;
					}
				}
								
				if(allok) {
					SHARED_CHAPTER_INDECES = shared;
					ALL_CHAPTER_INDECES = IntStream.rangeClosed(0, (total-1)).toArray();
					List<Integer> officeIndeces = new ArrayList<>();
					
					for(int i = 0; i < ALL_CHAPTER_INDECES.length; i++) {
						final int t = i;
						boolean contains = IntStream.of(SHARED_CHAPTER_INDECES).anyMatch(x -> x == t);
						if(!contains) {
							officeIndeces.add(i);
						}
					}
					int[] primitiveOfficeIndeces = new int[officeIndeces.size()];
					for(int i = 0; i < officeIndeces.size(); i++) {
						primitiveOfficeIndeces[i] = officeIndeces.get(i);
					}
					OFFICE_CHAPTER_INDECES = primitiveOfficeIndeces;
					System.out.println("Default SHARED_CHAPTER_INDECES " + Arrays.toString(SHARED_CHAPTER_INDECES));
					System.out.println("Default OFFICE_CHAPTER_INDECES " + Arrays.toString(OFFICE_CHAPTER_INDECES));
					System.out.println("Default ALL_CHAPTER_INDECES " + Arrays.toString(ALL_CHAPTER_INDECES));
				} else {
					System.err.println("Incorrect CHAPTER_CONFIG! Disabling chapters. Indexes were not consistent.");
					disableChapters();
				}
				
			} catch(ArrayIndexOutOfBoundsException | ClassCastException e) {
				System.err.println("Incorrect CHAPTER_CONFIG!  Disabling chapters. Incorrect numbers:");
				e.printStackTrace();
				disableChapters();
			}
		}
	}
	
	private static void disableChapters() {
		SHARED_CHAPTER_INDECES = new int[] {};
		ALL_CHAPTER_INDECES = new int[] {};
		OFFICE_CHAPTER_INDECES = new int[] {};
	}
	
	private static void updateCONTROLLER_ACCOUNT_NAME(){
		String systemValue = System.getenv(ENV_CONTROLLER_ACCOUNT_NAME);
		if(systemValue == null) {
			CONTROLLER_ACCOUNT_NAME = DEFAULT_CONTOLLER_ACCOUNT_NAME;
		} else {
			CONTROLLER_ACCOUNT_NAME = systemValue;
		}
		System.out.println("Using CONTROLLER_ACCOUNT_NAME " + CONTROLLER_ACCOUNT_NAME);
	}
	
	private static void updateADMIN_ACCOUNT_NAME() {
		String systemValue = System.getenv(ENV_ADMIN_ACCOUNT_NAME);
		if(systemValue == null) {
			ADMIN_ACCOUNT_NAME = DEFAULT_ADMIN_ACCOUNT_NAME;
		} else {
			ADMIN_ACCOUNT_NAME = systemValue;
		}
		System.out.println("Using ADMIN_ACCOUNT_NAME " + ADMIN_ACCOUNT_NAME);
	}
	
	private static void updateDATABASE_ID() {
		String systemValue = System.getenv(ENV_DATABASE_ID);
		if(systemValue == null) {
			DATABASE_ID = DEFAULT_DATABASE_ID;
		} else {
			DATABASE_ID = systemValue;
		}
		System.out.println("Using DATABASE_ID " + DATABASE_ID);
	}
	
	
	private static void updateBaseDirectoryPath() {
		String baseEnvDirectoryPath = System.getenv(ENV_KEY_BASE_DIRECTORY_PATH);
		if(baseEnvDirectoryPath == null) {
			BASE_DIRECTORY_PATH = DEFAULT_BASE_DIRECTORY_PATH;
		} else {
			BASE_DIRECTORY_PATH = baseEnvDirectoryPath;
		}
	}
	
}
