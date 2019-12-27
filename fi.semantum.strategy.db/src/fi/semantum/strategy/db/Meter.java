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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Meter extends MapBase {

	private static final long serialVersionUID = 7697435820705311629L;

	public enum TrafficValuationEnum {
		Continuous,
		Enumerated,
		Number,
		String,
		Default
	}
	
	private TrafficValuationEnum trafficValuationEnum;
	
	@Deprecated
	final public double[] limits;
	@Deprecated
	private double userValue = 0;
	@Deprecated
	private boolean userDefined = false;
	
	private boolean showInMap = true;
	public boolean isPrincipal = false;
	
	public String link = "";
	
	private boolean isTransient = false;
	
	public static Meter transientMeter(String id, String link, double value) {
		Meter result = new Meter(id, "", TrafficValuationEnum.Continuous);
		result.userDefined = true;
		result.userValue = value;
		result.link = link;
		result.isTransient = true;
		return result;
	}
	
	public static Meter create(Database database, String id, String text,
			TrafficValuationEnum trafficValuationEnum) {
		Meter p = new Meter(id, text, trafficValuationEnum);
		database.register(p);
		return p;
	}

	public static Meter create(Database database, MapBase container, String id, String text,
			TrafficValuationEnum trafficValuationEnum) {
		Meter result = create(database, id, text, trafficValuationEnum);
		container.addMeter(result);
		return result;
	}

	private Meter(String id, String text, TrafficValuationEnum trafficValuationEnum) {
		super(UUID.randomUUID().toString(), id, text);
		this.trafficValuationEnum = trafficValuationEnum;
		limits = null;
	}
	
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public MapBase getOwner(Database database) {
		for(MapBase b : database.enumerateMapBase()) {
			List<Meter> ms = b.getMeters(database);
			if(ms == null) continue;
			if(ms.contains(this))
				return b;
		}
		return null;
	}
	
	public List<Meter> getSubmeters(Database database) {

		// User-defined meter
		if(userDefined) return Collections.emptyList();
		
		// Indicator-defined meter
		if(limits != null) return Collections.emptyList();

		MapBase owner = getOwner(database);
		if(owner == null) return Collections.emptyList();
		
		ArrayList<Meter> result = new ArrayList<Meter>();
		collectSubmeters(database, owner, result);
		
		return result;

	}
	
	public boolean isShowInMap() {
		return this.showInMap;
	}
	
	public TrafficValuationEnum getTrafficValuationEnum() {
		return this.trafficValuationEnum;
	}
	
	private static void collectSubmeters(Database db, MapBase b, List<Meter> result) {

		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
		for(MapBase implementor : db.getInverse(b, implementsRelation)) {
			Collection<Meter> meters = implementor.getMeters(db);
			if(meters.isEmpty()) {
				collectSubmeters(db, implementor, result);
			} else {
				result.addAll(meters);
			}
		}

	}

	public Indicator getPossibleIndicator(Database database) {

		Indicator result = null;
		Relation measures = Relation.find(database, Relation.MEASURES);
		for (Pair p : measures.getRelations(this)) {
			Base b = database.find(p.second);
			if (b instanceof Indicator) {
				if(result != null) return null;
				result = (Indicator) b;
			}
		}
		return result;

	}
	
	public static List<Meter> enumerate(Database database) {

		ArrayList<Meter> result = new ArrayList<Meter>();
		for (Base b : database.objects.values()) {
			if (b instanceof Meter)
				result.add((Meter) b);
		}
		return result;

	}
	
	public void setUserValue(Database database, Account account, double value) {
		userDefined = true;
		userValue = value;
		if(database != null) {
			Base b = getOwner(database);
			if(b != null) {
				b.modified(database, account);
			}
		}
	}
	
	public boolean isUserDefined() {
		return userDefined;
	}
	
	public double getUserValue() {
		return userValue;
	}
	
	@Override
	public boolean modifyId(Database database, Account account, String id) {

		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyId(database, account, id);
			}
		}
		return super.modifyId(database, account, id);
	}

	@Override
	public boolean modifyText(Database database, Account account, String text) {
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyText(database, account, text);
			}
		}
		return super.modifyText(database, account, text);
	}

	@Override
	public boolean modifyDescription(Database database, Account account, String text) {
		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			if(getOwner(database).equals(indicator.getOwner(database))) {
				indicator.modifyDescription(database, account, text);
			}
		}
		return super.modifyDescription(database, account, text);
	}

	@Override
	public boolean migrate(Database database) {
		
		boolean result = false;
		
		if(getOwner(database) == null) {
			remove(database);
			return true;
		}
		Account account = database.getDefaultAdminAccount();
		
		Property aika = Property.find(database, Property.AIKAVALI);

		Indicator indicator = getPossibleIndicator(database);
		if(indicator != null) {
			
			result |= indicator.migrate(database);

			if(trafficValuationEnum == null && limits != null) {
				trafficValuationEnum = TrafficValuationEnum.Number;
				result = true;
			}
			
		} else {
			
			if(userDefined) {
				
				Datatype dt = Datatype.find(database, Terminology.ACTUALIZATION);
				Indicator ind = Indicator.create(database, getId(database), dt);
				
				String year = aika.getPropertyValue(this);
				if(year == null) year = Property.AIKAVALI_KAIKKI;
				
				MapBase owner = getOwner(database);
				UtilsDB.addIndicatorMeter(database, account, owner, ind, year);
				owner.removeMeter(this);
				remove(database);
				result = true;
				
			}

		}
		
		if(description == null) {
			modifyDescription(database, account, getText(database));
			modifyText(database, account, getId(database));
			modifyId(database, account, "");
			result = true;
		}

		result |= super.migrate(database);
		
		return result;
		
	}
	
	public void setShowInMap(boolean value) {
		this.showInMap = value;
	}

	public String getBaseDescription(Database database) {
		return super.getDescription(database);
	}
	
	@Override
	public String getDescription(Database database) {
		throw new IllegalStateException("Failed to find description for meter! Use UtilsMeters instead!");
	}
	
	@Override
	public String clientIdentity() {
		return "Mittari";
	}

}
