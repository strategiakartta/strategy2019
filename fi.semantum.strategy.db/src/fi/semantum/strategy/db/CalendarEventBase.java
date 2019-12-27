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

import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CalendarEventBase extends Base {

	private static final long serialVersionUID = 6061272327980585762L;
	public Date startDate;
	public Date endDate;
	public EventColor eventColor;
	public boolean includesTime;
	public String tag;
	
	public static enum EventColor {
		RED,
		GREEN,
		BLUE,
		GREY,
		PURPLE,
		ORANGE,
		YELLOW,
		NONE
	}
	
	protected CalendarEventBase(String text, Date startDate, Date endDate, EventColor color, boolean includesTime, String tag) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), text);
		this.startDate = startDate;
		this.endDate = endDate;
		this.eventColor = color;
		this.includesTime = includesTime;
		this.tag = tag;
	}

	public static CalendarEventBase createTransient(String text, Date startDate, Date endDate, EventColor color, boolean includesTime, String tag) {
		CalendarEventBase yc = new CalendarEventBase(text, startDate, endDate, color, includesTime, tag);
		return yc;
	}
	
	/**
	 * Without color
	 * @param database
	 * @param text
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static CalendarEventBase create(Database database, String text, Date startDate, Date endDate, boolean includesTime, String tag) {
		return create(database, text, startDate, endDate, EventColor.NONE, includesTime, tag);
	}
	
	/**
	 * With color
	 * @param database
	 * @param text
	 * @param startDate
	 * @param endDate
	 * @param color
	 * @return
	 */
	public static CalendarEventBase create(Database database, String text, Date startDate, Date endDate, EventColor color, boolean includesTime, String tag) {
		CalendarEventBase yc = createTransient(text, startDate, endDate, color, includesTime, tag);
		database.register(yc);
		return yc;
	}

	/**
	 * Without color
	 * @param database
	 * @param text
	 * @param date
	 * @return
	 */
	public static CalendarEventBase create(Database database, String text, Date date, boolean includesTime, String tag) {
		return create(database, text, date, EventColor.NONE, includesTime, tag);
	}
	
	/**
	 * With color
	 * @param database
	 * @param text
	 * @param date
	 * @param color
	 * @return
	 */
	public static CalendarEventBase create(Database database, String text, Date date, EventColor color, boolean includesTime, String tag) {
		CalendarEventBase yc = createTransient(text, date, date, color, includesTime, tag);
		database.register(yc);
		return yc;
	}


	
	public Date getStartDate() {
		return this.startDate;
	}
	
	public Date getEndDate() {
		return this.endDate;
	}
	
	/**
	 * Find all YearCalendar objects
	 * @param database
	 * @return
	 */
	public static List<CalendarEventBase> enumerate(Database database) {
		ArrayList<CalendarEventBase> result = new ArrayList<CalendarEventBase>();
		for (Base b : database.enumerate()) {
			if (b instanceof CalendarEventBase)
				result.add((CalendarEventBase) b);
		}
		return result;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static Comparator<CalendarEventBase> dateComparator = new Comparator<CalendarEventBase>() {

		@Override
		public int compare(CalendarEventBase o1, CalendarEventBase o2) {
			return o1.getStartDate().compareTo(o2.getStartDate());
		}
		
	};
	
	@Override
	public String clientIdentity() {
		return "Tapahtuma";
	}
}
