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

/**
 * 
 * @author Miro Eklund
 *
 */
public class ChangeSuggestionOpinion extends GathersOpinions {

	private static final long serialVersionUID = -3826772286066789860L;
	
	public Boolean matterResolved;
	
	protected ChangeSuggestionOpinion(String officialStatement) {
		super(UUID.randomUUID().toString(), officialStatement, officialStatement);
		this.matterResolved = false;
	}

	/**
	 * 
	 * @param database
	 * @param officialStatement
	 * @return
	 */
	public static ChangeSuggestionOpinion createTransient(Database database, String officialStatement) {
		ChangeSuggestionOpinion cso = new ChangeSuggestionOpinion(officialStatement);
		return cso;
	}
	
	/**
	 * s
	 * @param database
	 * @param officialStatement
	 * @param office
	 * @param account
	 * @return
	 */
	public static ChangeSuggestionOpinion create(Database database, String officialStatement, Office office, Account account) {
		ChangeSuggestionOpinion cso = createTransient(database, officialStatement);
		database.register(cso);
		
		UtilsDBComments.claimOwnership(database, cso, office, account);
		
		return cso;
	}
	
	@Override
	public Base getOwner(Database database) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void resolveMatter() {
		this.matterResolved = true;
	}
	
	public void unresolveMatter() {
		this.matterResolved = false;
	}
	
	public String getOfficialStatement() {
		return text;
	}
	
	public void setOfficialStatement(String officialStatement) {
		this.text = officialStatement;
	}
	
	/**
	 * Find all OfficialOpinion objects - this includes all objects across all documents, maps, etc.
	 * @param database
	 * @return
	 */
	public static List<ChangeSuggestionOpinion> enumerate(Database database) {
		ArrayList<ChangeSuggestionOpinion> result = new ArrayList<ChangeSuggestionOpinion>();
		for (Base b : database.enumerate()) {
			if (b instanceof ChangeSuggestionOpinion)
				result.add((ChangeSuggestionOpinion) b);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Text: " + this.text + ". Desc: " + this.description + ". UUID: " + this.uuid + ". Resolved: " + this.matterResolved;
	}

	@Override
	public String clientIdentity() {
		return "Muutosehdotus mielipide";
	}
	
}
