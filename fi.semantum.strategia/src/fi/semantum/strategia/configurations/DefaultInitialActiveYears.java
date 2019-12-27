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
package fi.semantum.strategia.configurations;

import java.util.ArrayList;

public class DefaultInitialActiveYears {

	private static ArrayList<Integer> years = new ArrayList<>();
	private static boolean initialized = false;
	
	public static void init() {
		if(!initialized) {
			initialized = true;
			updateYears();
		}
	}
	
	private static final String ENV_KEY_ACTIVE_YEARS = "strategia_active_years";
	
	private static void updateYears() {
		String activeYearsCommaSeparated = System.getenv(ENV_KEY_ACTIVE_YEARS);
		if(activeYearsCommaSeparated == null) {
			useDefaultYears();
		} else {
			String[] split = activeYearsCommaSeparated.split(",");
			boolean allValid = true;
			years = new ArrayList<Integer>();
			for(String yearString : split) {
				try {
					//Check if year is valid. If it is, add to list.
					years.add(Integer.parseInt(yearString));
				} catch (NumberFormatException e) {
					e.printStackTrace();
					allValid = false;
					break; //Break out of the for-loop, pointless to continue. Use default years.
				}
			}
			
			if(!allValid) {
				System.err.println("Failed to validate all years as comma separated. Expected format: 2016,2017,2018. Got: " + activeYearsCommaSeparated);
				System.err.print("Reverting to default years.");
				useDefaultYears(); //Override years with default years
			}
			
		}
	}
	
	/**
	 * Get the default configured years from configurations
	 * @return
	 */
	public static ArrayList<Integer> getYears(){
		return years;
	}
	
	private static void useDefaultYears() {
		years = new ArrayList<Integer>();
		years.add(2016);
		years.add(2017);
		years.add(2018);
		years.add(2019);
	}
	
	public static boolean isAConfiguredYear(Integer year) {
		return years.contains(year);
	}
	
}
