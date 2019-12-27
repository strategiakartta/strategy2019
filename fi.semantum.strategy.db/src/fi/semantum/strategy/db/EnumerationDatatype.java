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

import java.util.List;
import java.util.TreeMap;

import fi.semantum.strategy.db.Meter.TrafficValuationEnum;

public class EnumerationDatatype extends Datatype {

	private static final long serialVersionUID = -9212610888611727972L;
	
	private List<String> enumeration;
	private TreeMap<Object,String> treeMapValues;

	public EnumerationDatatype(Database database, String id, List<String> enumeration, String traffic) {
		super(database, id, id);
		this.enumeration = enumeration;
		treeMapValues = new TreeMap<Object,String>();
		assert(enumeration.size() == traffic.length());
		for(int i=0;i<enumeration.size();i++) {
			String value = enumeration.get(i);
			char c = traffic.charAt(i);
			if(c == 'p') treeMapValues.put(value, Datatype.RED);
			else if(c == 'k') treeMapValues.put(value, Datatype.YELLOW);
			else if(c == 'v') treeMapValues.put(value, Datatype.GREEN);
			else throw new IllegalArgumentException("traffic=" + traffic);
		}
	}
	
	public List<String> getValues() {
		return enumeration;
	}
	
	public TreeMap<Object, String> getTreeMapValues(){
		return this.treeMapValues;
	}
	
	public void replace(EnumerationDatatype other) {
		this.enumeration = other.enumeration;
		this.treeMapValues = other.treeMapValues;
	}
	
	@Override
	public Object getDefaultValue() {
		return enumeration.get(0);
	}
	
	@Override
	public Object getDefaultForecast() {
		return enumeration.get(0);
	}
	
	@Override
	public String format(Object value) {
		if(value == null) return "<arvoa ei ole asetettu>";
		return value.toString();
	}
	
	@Override
	public String clientIdentity() {
		return "Enumeraatio tietotyyppi";
	}
	
	@Override
	public TrafficValuationEnum getTrafficValuationEnum() {
		return Meter.TrafficValuationEnum.Enumerated;
	}
	
}
