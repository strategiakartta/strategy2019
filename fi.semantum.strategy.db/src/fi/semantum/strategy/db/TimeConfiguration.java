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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TimeConfiguration extends Base {

	private static final long serialVersionUID = 713732830288790423L;
	
	private String timeRange;
	private Set<Integer> frozen = new HashSet<Integer>();
	private ArrayList<Integer> activeYears = null;
	
	public ArrayList<Integer> getAllConfiguredYears() {
		if(activeYears == null) {
			//Backwards compatibility must be maintained - update DB now according to timeRange
			System.out.println("Old DB detected - update configured years for TimeConfiguration from range!");
			if(timeRange != null) {
				setRange(timeRange);
				System.out.println("Returning calculated years " + activeYears);
				return activeYears;
			} else {
				System.err.println("No timeRange defined for TimeConfiguration! Cannot update years!");
				return (new ArrayList<Integer>());
			}
		}
		
		return activeYears;
	}
	
	public String getRange() {
		return timeRange;
	}
	
	/**
	 * One way to update years and range. Another is setRange.
	 * @param years
	 */
	public void setYears(ArrayList<Integer> years) {
		Collections.sort(years);
		this.activeYears = years;
		
		String yearsAsString = "";
		for(Integer y : years) {
			yearsAsString += " " + y;
		}
		
		System.out.println("Years configured to: " + yearsAsString);
		
		if(years.size() >= 2) {
			timeRange = years.get(0) + "-" + years.get(years.size() - 1);
		} else if(years.size() == 1) {
			timeRange = years.get(0) + "-" + years.get(0);
		} else {
			timeRange = "";
			System.err.println("Not enough years configured for a time range! Expected at least 1 year, got 0!");
		}
	}
	
	public boolean isFrozen(int year) {
		return frozen.contains(year);
	}
	
	public void freeze(int year) {
		frozen.add(year);
	}

	public void unfreeze(int year) {
		frozen.remove(year);
	}

	public static TimeConfiguration create(Database database, ArrayList<Integer> years) {
		TimeConfiguration p = new TimeConfiguration(years);
		database.register(p);
		return p;
	}
	
	private TimeConfiguration(ArrayList<Integer> years) {
		super(UUID.randomUUID().toString(), "TimeConfiguration", "TimeConfiguration");
		setYears(years);
	}
	
	/**
	 * One way to update years and range. Another is setYears.
	 * @param range
	 * @return succeeded to set or not
	 */
	public boolean setRange(String range) {
		TimeInterval ti = TimeInterval.parse(range);
		if(ti != null) {
			int start = ti.startYear;
			int end = ti.endYear;
			
			if(end > start) {
				ArrayList<Integer> newYears = new ArrayList<Integer>();
				for(int i = start; i <= end; i++) {
					newYears.add(i);
				}
				Collections.sort(newYears);
				this.activeYears = newYears;
				this.timeRange = range;
			} else {
				System.err.println("Invalid range!");
			}
		}
		
		return true;
	}
	
	public static TimeConfiguration getInstance(Database database) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof TimeConfiguration) {
				return (TimeConfiguration)b;
			}
		}
		
		return null;

	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public boolean canWrite(Database database, Base b) {
		Property aika = Property.find(database, Property.AIKAVALI);
		String a = aika.getPropertyValue(b);
		if(a == null) return true;
		return canWrite(a);
	}

	public boolean canWrite(String a) {
		if(a == null) return true;
		if(Property.AIKAVALI_KAIKKI.equals(a)) return true;
		TimeInterval ti = TimeInterval.parse(a);
		for(int year : ti.years()) {
			if(isFrozen(year)) return false;
		}
		return true;
	}
	
	@Override
	public boolean migrate(Database database) {
		if(frozen == null) {
			frozen = new HashSet<Integer>();
			super.migrate(database);
			return true;
		}
		return super.migrate(database);
	}
	
	
	@Override
	public String clientIdentity() {
		return "Aikaasetus";
	}
	
}
