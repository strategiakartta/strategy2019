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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Opinion extends Base implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7226882552547570272L;
	public OpinionState state;
	
	private Opinion(String id, OpinionState state) {
		super(UUID.randomUUID().toString(), id, id);
		this.state = state;
	}

	public static Opinion create(Database database, Office office, OpinionState state) {
		Opinion b = new Opinion(office.getText(database), state);
		b.addRelation(Relation.find(database, Relation.FROM_OFFICE), office);
		database.register(b);
		return b;
	}
	
	public static Opinion create(Database database, Office office) {
		Opinion b = new Opinion(office.getText(database), OpinionState.NO_OPINION_GIVEN);
		b.addRelation(Relation.find(database, Relation.FROM_OFFICE), office);
		database.register(b);
		return b;
	}
	
	public void setState(OpinionState state) {
		this.state = state;
	}
	
	public OpinionState getState() {
		return this.state;
	}
	
	/**
	 * Find all ChangeSuggestion objects
	 * @param database
	 * @return
	 */
	public static List<Opinion> enumerate(Database database) {
		ArrayList<Opinion> result = new ArrayList<Opinion>();
		for (Base b : database.enumerate()) {
			if (b instanceof Opinion)
				result.add((Opinion) b);
		}
		return result;
	}
	
	public Office possiblyGetOffice(Database database) {
		Collection<Base> possibleOffices = this.getRelatedObjects(database, Relation.find(database, Relation.FROM_OFFICE));
		if(possibleOffices.size() > 1) {
			System.err.println("FATAL ERROR - Expected a single office relation to opinion " + this.uuid + ", got " + possibleOffices.size());
			return null;
		}
		
		for(Base b : possibleOffices) {
			if(b instanceof Office) {
				return (Office)b;
			}
		}
		System.err.println("FATAL ERROR - Found no office relations from opinion " + this.uuid);
		return null;
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public static enum OpinionState {
		NO_OPINION_GIVEN,
		OK,
		NOT_OK,
		EXPLANATION_NEEDED;
	}
	
	@Override
	public String clientIdentity() {
		return "Mielipide";
	}
	
	
}
