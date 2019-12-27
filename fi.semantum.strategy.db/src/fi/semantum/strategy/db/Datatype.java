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
import java.util.List;
import java.util.UUID;

abstract public class Datatype extends Base {

	public static String RED = "#DA251D";
	public static String YELLOW = "#F4C000";
	public static String GREEN = "#00923F";
	
	private static final long serialVersionUID = 3032947469375442364L;

	protected Datatype(Database database, String id, String text) {
		super(UUID.randomUUID().toString(), id, text);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	public static Datatype find(Database database, String id) {
		for(Datatype dt : enumerate(database)) 
			if(id.equals(dt.getId(database)))
				return dt;
		return null;
	}
	
	public static List<Datatype> enumerate(Database database) {
		ArrayList<Datatype> result = new ArrayList<Datatype>();
		for(Base b : database.objects.values()) {
			if(b instanceof Datatype) result.add((Datatype)b);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public static interface ValueChanged {
		
		public void run(Object newValue);
		
	}
	
	abstract public Meter.TrafficValuationEnum getTrafficValuationEnum();
	abstract public Object getDefaultValue();
	abstract public Object getDefaultForecast();
	
	abstract public String format(Object value);
	
}
