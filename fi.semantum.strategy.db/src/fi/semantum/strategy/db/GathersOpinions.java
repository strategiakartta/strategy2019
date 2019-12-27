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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.semantum.strategy.db.Opinion.OpinionState;

public abstract class GathersOpinions extends Base implements Serializable {

	private static final long serialVersionUID = -8633329945484618159L;

	protected GathersOpinions(String uuid, String id, String text) {
		super(uuid, id, text);
	}

	@Override
	public Base getOwner(Database database) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get all opinions given to this opinion gatherer as a map from which office they belong to.
	 * Key: office text. Value: Opinion state
	 * @return
	 */
	public Map<String, OpinionState> getOpinionsAsMap(Database database){
		List<Opinion> opinions = getOpinions(database);
		Map<String, OpinionState> map = new HashMap<>();
		for(Opinion opinion : opinions) {
			Office possibleOffice = opinion.possiblyGetOffice(database);
			if(possibleOffice != null) {
				map.put(possibleOffice.getText(database), opinion.state);
			} else {
				System.err.println("FATAL ERROR - Got null office when creating opinions map from GathersOpinion object " + this.uuid);
			}
		}
		return map;
	}
	
	/**
	 * Find all the opinions given to this opinion gatherer
	 * @param database
	 * @return
	 */
	public List<Opinion> getOpinions(Database database){
		Collection<Base> bases = this.getRelatedObjects(database, Relation.find(database, Relation.HAS_OPINION));
		List<Opinion> opinions = new ArrayList<>(bases.size());
		for(Base b : bases) {
			if(b instanceof Opinion) {
				opinions.add((Opinion)b);
			}
		}
		return opinions;
	}
	
	private void addOpinion(Database database, Office office, OpinionState state) {
		Opinion opinion = Opinion.create(database, office, state);
		this.addRelation(Relation.find(database, Relation.HAS_OPINION), opinion);
	}
	
	public boolean addOpinionIfMissing(Database database, Office office, OpinionState state) {
		Opinion old = getPossibleOfficeOpinion(database, office);
		if(old == null) {
			addOpinion(database, office, state);
			return true;
		}
		return false;
	}
	
	public void addOrUpdateOpinion(Database database, Office office, OpinionState state) {
		if(!addOpinionIfMissing(database, office, state)) {
			updateOpinion(database, office, state);
		}
	}
	
	public void updateOpinion(Database database, Office office, OpinionState state) {
		Opinion old = getPossibleOfficeOpinion(database, office);
		old.setState(state);
	}
	
	/**
	 * Given an office, tries to find from this GathersOpinion object an Opinion from that Office
	 * @param database
	 * @param office
	 * @return
	 */
	public Opinion getPossibleOfficeOpinion(Database database, Office office) {
		List<Opinion> allOpinions = getOpinions(database);
		for(Opinion o : allOpinions) {
			Office possibleOffice = o.possiblyGetOffice(database);
			if(possibleOffice != null) {
				if(possibleOffice.uuid.equals(office.uuid)) {
					return o;
				}
			}
		}
		return null;
	}
}
