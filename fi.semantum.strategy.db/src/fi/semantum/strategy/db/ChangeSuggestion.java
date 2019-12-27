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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A ChangeSuggestion to the current document and strategy element.
 * @author Miro Eklund
 *
 */
public class ChangeSuggestion extends GathersOpinions {

	private static final long serialVersionUID = 3381848583666335614L;
	
	public ChangeSuggestionState state;
	public String possibleBaseTargetUUID;
	public String possibleTargetString;
	public String newSuggestion;
	public String motivation;

	private ChangeSuggestion(String shortDescription,
			String possibleBaseTargetUUID,
			String possibleTargetString,
			String newSuggestion, String motivation) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "");
		
		this.description = shortDescription;
		this.possibleBaseTargetUUID = possibleBaseTargetUUID;
		this.possibleTargetString = possibleTargetString;
		this.newSuggestion = newSuggestion;
		this.motivation = motivation;
		this.state = ChangeSuggestionState.OPEN_ACTIVE;
	}

	/**
	 * 
	 * @param database
	 * @param text
	 * @param office
	 * @param account
	 * @param possibleBaseTargetUUID
	 * @param possibleTargetString
	 * @param newSuggestion
	 * @param motivation
	 * @return
	 */
	private static ChangeSuggestion createTransient(Database database, String shortDescription, String possibleBaseTargetUUID,
			String possibleTargetString, String newSuggestion, String motivation) {
		ChangeSuggestion cs = new ChangeSuggestion(shortDescription, possibleBaseTargetUUID, possibleTargetString, newSuggestion, motivation);
		return cs;
	}

	/**
	 * 
	 * @param database
	 * @param text
	 * @param office
	 * @param account
	 * @param possibleBaseTargetUUID
	 * @param possibleTargetString
	 * @param newSuggestion
	 * @param motivation
	 * @return
	 */
	public static ChangeSuggestion create(Database database, String shortDescription, Office office, Account account,
			String possibleBaseTargetUUID, String possibleTargetString,
			String newSuggestion, String motivation) {
		
		ChangeSuggestion cs = createTransient(database, shortDescription, possibleBaseTargetUUID, possibleTargetString, newSuggestion, motivation);
		
		database.register(cs);
		UtilsDBComments.claimOwnership(database, cs, office, account);
		
		return cs;
	}
	
	public void updateChangeSuggestion(Account account, Database database, String shortDescription,
			String possibleBaseTargetUUID, String possibleTargetString,
			String newSuggestion, String motivation) {
		this.description = shortDescription;
		this.possibleBaseTargetUUID = possibleBaseTargetUUID;
		this.possibleTargetString = possibleTargetString;
		this.newSuggestion = newSuggestion;
		this.motivation = motivation;
		
		UtilsDB.removeAllSeenByAccount(database, this);
		UtilsDB.setSeenByAccount(database, account, this);
	}
	
	public void updateState(Account account, Database database, ChangeSuggestionState state) {
		this.state = state;
		UtilsDB.removeAllSeenByAccount(database, this);
		UtilsDB.setSeenByAccount(database, account, this);
	}

	public ChangeSuggestionState getState() {
		return this.state;
	}
	
	public String getPossibleBaseTargetUUID() {
		return this.possibleBaseTargetUUID;
	}
	
	public String getPossibleTargetString() {
		return this.possibleTargetString;
	}
	
	public String getNewSuggestion() {
		return this.newSuggestion;
	}
	
	public String getMotivation() {
		return this.motivation;
	}
	
	
	@Override
	public Base getOwner(Database database) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return "Desc: " + this.description + ". UUID: " + this.uuid +
				". Motivation: " + this.motivation + ". Suggestion: " + this.newSuggestion + ". State: " + this.state.name();
	}
	
	public boolean isActive() {
		return this.state.equals(ChangeSuggestionState.OPEN_ACTIVE);
	}
	
	public boolean isOnHold() {
		return this.state.equals(ChangeSuggestionState.OPEN_ON_HOLD);
	}
	
	public boolean isClosed() {
		return this.state.equals(ChangeSuggestionState.CLOSED_ACCEPTED_AS_IS)
				|| this.state.equals(ChangeSuggestionState.CLOSED_ACCEPTED_WITH_CHANGES)
				|| this.state.equals(ChangeSuggestionState.CLOSED_DECLINED);
	}
	
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Possible states for a ChangeSuggestion.
	 * Active: Can be commented on and opinions given.
	 * On hold: Is not closed by comments and opinions cannot be given.
	 * Accepted as is: Closed and accepted without taking into account the comments and opinions.
	 * Accepted with changes: Closed and accepted with changes from comments and official statements
	 * Declined: Closed and not accepted at all
	 * @author Miro Eklund
	 *
	 */
	public static enum ChangeSuggestionState {
		OPEN_ACTIVE,
		OPEN_ON_HOLD,
		CLOSED_ACCEPTED_AS_IS,
		CLOSED_ACCEPTED_WITH_CHANGES,
		CLOSED_DECLINED;
	}
	
	public static final String activeStateTextUIText = "Avoin";
	public static final String onholdStateTextUIText = "Siirret‰‰n tulosneuvotteluihin";
	public static final String acceptedAsIsStateTextUIText = "Hyv‰ksytty sellaisenaan";
	public static final String acceptedWithChangesStateTextUIText = "Hyv‰ksytty muutoksilla";
	public static final String declinedStateTextUIText = "Hyl‰tty";
	
	public static String changeSuggestionStateEnumToUIText(ChangeSuggestionState state) {
		switch(state) {
			case OPEN_ACTIVE: return activeStateTextUIText;
			case OPEN_ON_HOLD: return onholdStateTextUIText;
			case CLOSED_ACCEPTED_AS_IS: return acceptedAsIsStateTextUIText;
			case CLOSED_ACCEPTED_WITH_CHANGES: return acceptedWithChangesStateTextUIText;
			case CLOSED_DECLINED: return declinedStateTextUIText;
			default: {
				System.err.println("Expected a ChangeSuggestionState, but failed to match! Returning empty");
				return "";
			}
		}
	}
	
	public static ChangeSuggestionState uiTextToChangeSuggestionState(String uiText) {
		switch(uiText) {
			case activeStateTextUIText: return ChangeSuggestionState.OPEN_ACTIVE;
			case onholdStateTextUIText: return ChangeSuggestionState.OPEN_ON_HOLD;
			case acceptedAsIsStateTextUIText: return ChangeSuggestionState.CLOSED_ACCEPTED_AS_IS;
			case acceptedWithChangesStateTextUIText: return ChangeSuggestionState.CLOSED_ACCEPTED_WITH_CHANGES;
			case declinedStateTextUIText: return ChangeSuggestionState.CLOSED_DECLINED;
			default: {
				System.err.println(uiText + " expected to match with a ChangeSuggestionState, but failed to match! Returning null");
				return null;
			}
		}
	}
	
	/**
	 * Find all ChangeSuggestions that target a specific base object
	 * @param database
	 * @param base
	 * @return
	 */
	public static List<ChangeSuggestion> changeSuggestionsForBase(Database database, Base base){
		String uuid = base.uuid;
		List<ChangeSuggestion> result = new ArrayList<>();
		
		for (Base b : database.enumerate()) {
			if (b instanceof ChangeSuggestion) {
				String possibleTargetUUID = ((ChangeSuggestion)b).possibleBaseTargetUUID;
				if(possibleTargetUUID != null && possibleTargetUUID.equals(uuid)) {
					result.add((ChangeSuggestion)b);
				}
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param database
	 * @return
	 */
	public static List<ChangeSuggestion> enumerate(Database database){
		List<ChangeSuggestion> result = new ArrayList<>();
		for (Base b : database.enumerate()) {
			if (b instanceof ChangeSuggestion) {
				result.add((ChangeSuggestion)b);
			}
		}
		return result;
	}
	
	/**
	 * Check if any of the UUIDs provided have a ChangeSuggestion in the database.
	 * @param database
	 * @param uuids
	 * @return
	 */
	public static Set<ChangeSuggestion> changeSuggestionsByTargetUUIDs(Database database, StrategyMap map, Set<String> uuids){
		Set<ChangeSuggestion> matchingSuggestions = new HashSet<>();
		for (ChangeSuggestion cs : UtilsDBComments.getPossibleLinkedChangeSuggestions(database, map)) {
			String possibleTargetUUID = cs.possibleBaseTargetUUID;
			if(possibleTargetUUID != null && uuids.contains(possibleTargetUUID)) {
				matchingSuggestions.add(cs);
			}
		}
		return matchingSuggestions;
	}
	
	
	@Override
	public String clientIdentity() {
		return "Muutosehdotus";
	}
}
