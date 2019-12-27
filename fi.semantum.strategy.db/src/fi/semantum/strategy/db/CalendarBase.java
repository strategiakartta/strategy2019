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
import java.util.List;
import java.util.UUID;

public class CalendarBase extends Base {

	private static final long serialVersionUID = 6061272327980585762L;
	public boolean active;
	
	protected CalendarBase(String text) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), text);
		this.active = false;
	}

	private static CalendarBase createTransient(String text) {
		CalendarBase yc = new CalendarBase(text);
		return yc;
	}
	
	public static CalendarBase create(Database database, String text) {
		CalendarBase yc = createTransient(text);
		database.register(yc);
		return yc;
	}
	
	/**
	 * Find all YearCalendar objects
	 * @param database
	 * @return
	 */
	public static List<CalendarBase> enumerate(Database database) {
		ArrayList<CalendarBase> result = new ArrayList<CalendarBase>();
		for (Base b : database.enumerate()) {
			if (b instanceof CalendarBase)
				result.add((CalendarBase) b);
		}
		return result;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}

	@Override
	public String clientIdentity() {
		return "Vuosikello";
	}
	
	/**
	 * Adds an event to this Calendar
	 * @param database
	 * @param event
	 */
	public void addEvent(Database database, CalendarEventBase event) {
		this.addRelation(Relation.find(database, Relation.CONTAINS), event);
		event.addRelation(Relation.find(database, Relation.PART_OF), this);
	}
	
	/**
	 * Remove an event from the calendar
	 * @param database
	 * @param event
	 */
	public void removeEvent(Database database, CalendarEventBase event) {
		this.denyRelation(database, Relation.find(database, Relation.CONTAINS), event);
		event.denyRelation(database, Relation.find(database, Relation.PART_OF), this);
	}
	
	/**
	 * Find all Calendar objects that an event belongs to
	 * @param database
	 * @param event
	 * @return
	 */
	public static List<CalendarBase> calendarsFromEvent(Database database, CalendarEventBase event) {
		Collection<Base> bases = event.getRelatedObjects(database, Relation.find(database, Relation.PART_OF));
		List<CalendarBase> calendars = new ArrayList<>();
		for(Base b : bases) {
			if(b instanceof CalendarBase) {
				calendars.add((CalendarBase)b);
			}
		}
		return calendars;
	}
	
	/**
	 * Find all the events for this year
	 * @param database
	 * @return
	 */
	public List<CalendarEventBase> getEvents(Database database){
		Collection<Base> bases = this.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS));
		List<CalendarEventBase> events = new ArrayList<>();
		for(Base b : bases) {
			if(b instanceof CalendarEventBase) {
				events.add((CalendarEventBase)b);
			}
		}
		return events;
	}

}
