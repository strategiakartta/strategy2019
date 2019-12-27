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

public class Tag extends MapBase {
	
	private static final long serialVersionUID = 844900680130018383L;
	
	public static final String LIIKENNE = "Liikenne";
	public static final String VIESTINTA = "Viestintä";
	public static final String VOIMAVARAT = "Voimavarat";
	
	public String color;

	public static Tag create(Database database, String id, String text, String color) {
		Tag p = new Tag(id, text, color);
		database.register(p);
		return p;
	}
	
	private Tag(String id, String text, String color) {
		super(UUID.randomUUID().toString(), id, text);
		this.color = color;
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static List<Tag> enumerate(Database database) {
		
		ArrayList<Tag> result = new ArrayList<Tag>();
		for(Base b : database.objects.values()) {
			if(b instanceof Tag) result.add((Tag)b);
		}
		return result;

	}
	
	@Override
	public String getDescription(Database database) {
		return getId(database) + " (Aihetunniste)";
	}
	
	public double getCoverage(Database database, String currentTime, MapBase base) {

		if(base.hasRelatedTag(database, this)) { 
			return 1.0;
		}
		
		Collection<MapBase> imp = UtilsDB.getDirectImplementors(database, base, currentTime);
		if(imp.isEmpty()) return 0.0;
		
		double result = 0.0;
		double coeff = 1.0 / imp.size();
		for(MapBase b : imp) {
			result += coeff*getCoverage(database, currentTime, b);
		}
		
		return result;
		
	}
	
	@Override
	public String clientIdentity() {
		return "Tunniste";
	}
	
}
