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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ResponsibilityInstance extends Base {

	private static final long serialVersionUID = 7674413338143454559L;
	
	private Map<String,String> values = new HashMap<String, String>();
	
	public ResponsibilityInstance(Database database) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "");
		database.register(this);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static List<ResponsibilityInstance> enumerate(Database database) {

		ArrayList<ResponsibilityInstance> result = new ArrayList<ResponsibilityInstance>();
		for (Base b : database.objects.values()) {
			if (b instanceof ResponsibilityInstance)
				result.add((ResponsibilityInstance) b);
		}
		return result;

	}
	
	public boolean containsField(String field) {
		return values.containsKey(field);
	}
	
	public String removeField(String field) {
		return values.remove(field);
	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	public void setValue(String key, String value) {
		values.put(key, value);
	}
	
	@Override
	public String clientIdentity() {
		return "Vastuu";
	}
	
}
