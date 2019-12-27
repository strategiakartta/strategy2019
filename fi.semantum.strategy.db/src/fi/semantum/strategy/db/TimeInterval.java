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

import java.util.Set;
import java.util.TreeSet;

public class TimeInterval {

	private static final TimeInterval ALWAYS = new TimeInterval(Integer.MIN_VALUE, Integer.MAX_VALUE);

	final public int startYear;
	final public int endYear;

	public TimeInterval(int start, int end) {
		if (start < 2000)
			start = 2000;
		if (end > 2100)
			end = 2100;
		startYear = start;
		endYear = end;
	}

	public boolean isSingle() {
		return startYear == endYear;
	}
	
	public static boolean isAlways(TimeInterval interval) {
		return ALWAYS == interval;
	}

	public Set<Integer> years() {
		Set<Integer> result = new TreeSet<Integer>();
		for (int i = startYear; i <= endYear; i++)
			result.add(i);
		return result;
	}

	public boolean contains(String ref) {
		try {
			int year = Integer.parseInt(ref);
			return (year >= startYear) && (year <= endYear);
		} catch (NumberFormatException e) {
			return true;
		}
	}

	public static TimeInterval parse(String s) {
		if (Property.AIKAVALI_KAIKKI.equals(s))
			return ALWAYS;
		if (s == null)
			return ALWAYS;
		int idx = s.lastIndexOf('-');
		if (idx == -1) {
			try {
				int startY = Integer.parseInt(s);
				return new TimeInterval(startY, startY);
			} catch (NumberFormatException e) {
				return ALWAYS;
			}
		}
		String start = s.substring(0, idx);
		String end = s.substring(idx + 1);
		try {
			int startY = Integer.parseInt(start);
			int endY = Integer.parseInt(end);
			return new TimeInterval(startY, endY);
		} catch (NumberFormatException e) {
			return ALWAYS;
		}
	}

	public boolean intersects(TimeInterval other) {
		if (other.endYear < startYear)
			return false;
		if (other.startYear > endYear)
			return false;
		return true;
	}

}

