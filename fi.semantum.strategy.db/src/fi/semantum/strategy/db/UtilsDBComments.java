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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fi.semantum.strategy.db.Opinion.OpinionState;
import fi.semantum.strategy.db.UtilsDB;

public class UtilsDBComments {

	public static Set<ChangeSuggestion> changeSuggestionsInOuterBoxRecursiveSearch(Database database, UIState uistate, OuterBox outerBox){
		Set<String> allUUIDs = findAllSubLeafUUIDs(database, uistate, outerBox);

		StrategyMap map = database.getRoot();
		Set<ChangeSuggestion> changeSuggestionsByTargetUUIDs = ChangeSuggestion.changeSuggestionsByTargetUUIDs(database, map, allUUIDs);
		return changeSuggestionsByTargetUUIDs;
	}
	
	public static Set<Base> findAllSubLeafBases(Database database, UIState uiState, OuterBox outerBox){
		Set<Base> allBases = new HashSet<>();
		if(outerBox != null) {
			Base target = MapBase.findTargetedMapBoxElement(database, outerBox);
			if(target != null) {
				allBases.add(target);
			}
			allBases.add(outerBox);
		}
		Base leafOuter = outerBox.hasLeaf(database, uiState.getTime());
		if(leafOuter != null) allBases.add(leafOuter);
		
		for(InnerBox ib : outerBox.innerboxes) {
			if(ib != null) allBases.add(ib);
			Base leafInner = ib.hasLeaf(database, uiState.getTime());
			if(leafInner != null) allBases.add(leafInner);
		}
		return allBases;
	}
	
	private static Set<String> findAllSubLeafUUIDs(Database database, UIState uiState, OuterBox outerBox){
		Set<Base> bases = findAllSubLeafBases(database, uiState, outerBox);
		Set<String> allUUIDs = new HashSet<>();
		for(Base b : bases) {
			allUUIDs.add(b.uuid);
		}
		return allUUIDs;
	}
	
	/**
	 * Change the office and account associated with the opinionGatherer object
	 * @param database
	 * @param opinionGatherer
	 * @param office
	 * @param account
	 */
	public static void claimOwnership(Database database, Base b, Office office, Account account) {
		if(office != null) {
			b.addRelation(Relation.find(database, Relation.FROM_OFFICE), office);
		}
		if(account != null) {
			b.addRelation(Relation.find(database, Relation.FROM_ACCOUNT), account);
		}
	}
	
	
	/**
	 * If the account has read access to all change suggestions
	 * @return
	 */
	public static boolean accountCanReadAllChangeSuggestions(Database database, Account acc) {
		if(acc != null) {
			return acc.isAdmin(database) || CommentToolAdminBase.canRead(database, acc);
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param all
	 * @param stateFilter
	 * @return
	 */
	public static List<ChangeSuggestion> filterChangeSuggestionVisibleToAccount(Database database, Account account, Set<ChangeSuggestion> all,
			ChangeSuggestion.ChangeSuggestionState stateFilter){
		List<ChangeSuggestion> allAsList = new ArrayList<>(all);
		return filterChangeSuggestionVisibleToAccount(database, account, allAsList, stateFilter);
	}
	
	/**
	 * Provided a list of ChangeSuggestions, only returns those actually visible to the account
	 * @param database
	 * @param all
	 * @param account
	 * @return
	 */
	public static List<ChangeSuggestion> filterChangeSuggestionVisibleToAccount(Database database, Account account, List<ChangeSuggestion> all,
			ChangeSuggestion.ChangeSuggestionState stateFilter){
		List<ChangeSuggestion> result = new ArrayList<>();
		if(account != null) {
			if(accountCanReadAllChangeSuggestions(database, account)) {
				result = all;
			} else {
				List<ChangeSuggestion>  accountOwned = all.stream()
						    .filter(cs -> UtilsDB.canRead(database, account, cs)).collect(Collectors.toList());
				result = accountOwned;
			}
		}
		if(stateFilter != null) {
			List<ChangeSuggestion> filteredByState = result.stream()
			    .filter(cs -> cs.getState().equals(stateFilter) && UtilsDB.canRead(database, account, cs)).collect(Collectors.toList());
			result = filteredByState;
		}
		return result;
	}
	
	public static List<ChangeSuggestion> filterChangeSuggestionVisibleToAccount(Database database, Account account, List<ChangeSuggestion> all){
		return filterChangeSuggestionVisibleToAccount(database, account, all, null);
	}
	
	/**
	 * Find the Change Suggestions
	 * @param database
	 * @param map or other base object with HAS_CHANGE_SUGGESTION relation
	 * @return
	 */
	public static List<ChangeSuggestion> getPossibleLinkedChangeSuggestions(Database database, Base base) {
		Collection<Base> linkedChangeSuggestions = base.getRelatedObjects(database, Relation.find(database, Relation.HAS_CHANGE_SUGGESTION));
		List<ChangeSuggestion> result = new ArrayList<>();
		for(Base b : linkedChangeSuggestions) {
			if(b instanceof ChangeSuggestion) {
				result.add((ChangeSuggestion)b);
			}
		}
		return result;
	}
	
	
	/**
	 * Similar to linkToChangeSuggestion but doesn't update Main
	 * @param database
	 * @param cs
	 * @param b
	 */
	public static void addToChangeSuggestion(Database database, ChangeSuggestion cs, Base b) {
		cs.addRelation(Relation.find(database, Relation.CONTAINS), b);
	}
	
	/**
	 * Adds a linked to statement between map and change suggestion
	 * @param database
	 * @param changeSuggestion
	 * @param base
	 */
	public static void addChangeSuggestionToBase(Database database, ChangeSuggestion changeSuggestion, Base base) {
		if(base != null) {
			base.addRelation(Relation.find(database, Relation.HAS_CHANGE_SUGGESTION), changeSuggestion);
			changeSuggestion.addRelation(Relation.find(database, Relation.PART_OF), base);
		} else {
			System.err.println("Cannot add CS to null base");
		}
	}
	
	public static ResultAgreementDocuments getPossibleParentDocs(Database database, ChangeSuggestion changeSuggestion) {
		Collection<Base> bases = changeSuggestion.getRelatedObjects(database, Relation.find(database, Relation.PART_OF));
		for(Base b : bases) {
			if(b instanceof ResultAgreementDocuments) {
				return (ResultAgreementDocuments)b;
			}
		}
		return null;
	}
	
	public static StrategyMap getPossibleParentMap(Database database, ChangeSuggestion changeSuggestion) {
		Collection<Base> bases = changeSuggestion.getRelatedObjects(database, Relation.find(database, Relation.PART_OF));
		for(Base b : bases) {
			if(b instanceof StrategyMap) {
				return (StrategyMap)b;
			}
		}
		return null;
	}
	
	/**
	 * Find objects linked with linkToChangeSuggestion. E.g. ChangeSuggestionOpinions and Comments
	 * @param database
	 * @param cs
	 * @return
	 */
	public static List<Base> findContainsObjects(Database database, ChangeSuggestion cs){
		Collection<Base> bases = cs.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS));
		List<Base> result = new ArrayList<>(bases.size());
		for(Base b : bases) {
			result.add(b);
		}
		return result;
	}
	
	public static Set<Office> findRelatedOffices(Database database, ChangeSuggestion cs){
		Set<Office> offices = new HashSet<>();
		for(Base base : findContainsObjects(database, cs)) {
			Office office = UtilsDB.getPossibleAssociatedOffice(database, base);
			if(office != null) {
				offices.add(office);
			}
		}
		return offices;
	}
	
	/**
	 * The account that owns the opinion, or null
	 * @param database
	 * @param opinionGatherer
	 * @return
	 */
	public static Account getPossibleAssociatedAccount(Database database, Base opinionGatherer) {
		Collection<Base> linkedMaps = opinionGatherer.getRelatedObjects(database, Relation.find(database, Relation.FROM_ACCOUNT));
		if(linkedMaps.size() == 0) {
			return null;
		}
		else if(linkedMaps.size() > 1) {
			System.err.println("Multiple accounts linked to GathersOpinions!");
			return(null);
		} else {
			return (Account)linkedMaps.iterator().next();
		}
	}
	
	/**
	 * Provided a changesuggestion, finds all opinions it contains and returns those matching the office
	 * @param database
	 * @param cs
	 * @param office
	 * @return
	 */
	public static ChangeSuggestionOpinion getPossibleChangeSuggestionOpinionByOffice(Database database, ChangeSuggestion cs, Office office) {
		Collection<Base> contains = cs.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS));
		List<ChangeSuggestionOpinion> result = new ArrayList<>();
		for(Base b : contains) {
			if(b instanceof ChangeSuggestionOpinion) {
				if(baseIsFromOffice(database, b, office)) {
					result.add((ChangeSuggestionOpinion)b);
				}
			}
		}
		if(result.size() == 1) {
			return result.get(0);
		} else {
			System.err.println("Incorrect number of changesuggestionopinions for changesuggestion " + cs.uuid
					+ " from office "+ office.uuid + ". Expected exactly 1, got: " + result.size());
			return null;
		}
	}
	
	/**
	 * True if provided base has Relation.FROM_ACCOUNT and that Account matches the provided account
	 * @param database
	 * @param b
	 * @param account
	 * @return
	 */
	public static boolean baseIsFromAccount(Database database, Base b, Account account) {
		Account baseAccount = getPossibleAssociatedAccount(database, b);
		if(baseAccount == null || account == null) {
			return(false);
		}
    	return baseAccount.uuid.equals(account.uuid);
	}
	
	/**
	 * True if provided base has Relation.FROM_OFFICE and that Office matches the provided office
	 * @param database
	 * @param b
	 * @param office
	 * @return
	 */
	public static boolean baseIsFromOffice(Database database, Base b, Office office) {
		Office baseOffice = UtilsDB.getPossibleAssociatedOffice(database, b);
		if(baseOffice == null || office == null) {
			return(false);
		}
    	return baseOffice.uuid.equals(office.uuid);
	}

	
	private static String maxXChars(String s, int x) {
		if(s.length() <= x) return s;
		return s.substring(0, x) + "..";
	}
	
	/**
	 * 
	 * @param box
	 * @return
	 */
	public static String findRecursiveParentPaths(Database database, Base base, boolean fullLength, String separator) {
		String parentPath = "";
		Base parent = base.getOwner(database);
		if(parent != null) {
			String grandParentInfo = findRecursiveParentPaths(database, parent, fullLength, separator);
			String parentInfo = getPossibleBaseSummary(database, parent, fullLength, false, separator);
			if(grandParentInfo != null && !grandParentInfo.equals("")) {
				parentPath = grandParentInfo + separator + parentInfo;
			} else {
				parentPath = parentInfo;
			}
		}
		//System.out.println("Partial parentPath: " + parentPath);
		return parentPath;
	}
	
	public static String getPossibleBaseSummary(Database database, Base base, boolean fullLength, boolean recursive, String separator) {
		String parentPath = "";
		if(recursive) {
			parentPath = findRecursiveParentPaths(database, base, fullLength, separator);
			if(!parentPath.equals("")) {
				parentPath += separator;
			}
		}
		
		String result = "[";
		if(base instanceof TextChapter) {
			result += fullLength ? base.getText(database) : maxXChars(base.getText(database), 60);
			
			TextChapter chapter = (TextChapter)base;
			Office office = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
			if(office != null) {
				String shortID = fullLength ? office.getShortId(database) : maxXChars(office.getShortId(database), 20);
				result += " | " + shortID;
			}
			String desc = fullLength ? base.getDescription(database) : maxXChars(base.getDescription(database), 80);
			result += "] - " + desc;
		} else if(base instanceof ResultAgreementDocuments){
			ResultAgreementDocuments rad = (ResultAgreementDocuments)base;
			String identity = rad.clientIdentity();
			result += fullLength ? identity : maxXChars(identity, 100);
			result += "]";
		} else {
			String identity = base.clientIdentity();
			
			int idPrefixLength = 40;
			int textPrefixLength = 60;
			int descPrefixLength = 80;
			if(base instanceof MapBase){ //Outer and Inner boxes are both MapBases and MapBoxes.
				identity = database.getType((MapBase)base);
				idPrefixLength = 120;
			} else {
				idPrefixLength = 60;
			}
			
			result += fullLength ? identity + "]" : maxXChars(identity, 100) + "]";
			
			String id = fullLength ? base.getId(database) : maxXChars(base.getId(database), idPrefixLength);
			String idPrefix = id.equals("") ? "" : id;
			String text = fullLength ? base.getText(database) : maxXChars(base.getText(database), textPrefixLength);
			String textPrefix = text.equals("") ? "" : " | " + text;
			String desc = fullLength ? base.getDescription(database) : maxXChars(base.getDescription(database), descPrefixLength);
			String descPrefix = desc.equals("") ? "" : " | " + desc;
			
			result += " - " + idPrefix + textPrefix + descPrefix;
		}
		return parentPath + result;
	}
	
	public static String textChapterToSummary(Database database, TextChapter chapter) {
		Office office = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
		String shortID = office != null ? office.getShortId(database) : "";
		return shortID + " " + chapter.clientIdentity() + ", " + chapter.getText(database) + ": " + chapter.getDescription(database);
	}
	
	public static String getChangeSuggestionTargetBaseSummary(Database database, ChangeSuggestion cs,
			boolean fullLength, boolean recursive, String separator) {
		String uuid = cs.getPossibleBaseTargetUUID();
		if(uuid != null && !uuid.equals("")) {
			Base b = database.find(uuid);
			if(b != null) {
				return getPossibleBaseSummary(database, b, fullLength, recursive, separator);
			} else {
				System.err.println("Failed to read the Base target for uuid " + uuid);
				return "(kohdetta ei löydetty. kohde on voitu poistaa.)";
			}
		} else {
			return "(ei tarkkaa kohdetta)";	
		}
	}
	
	
	/**
	 * Provided a list of ChangeSuggestionOpinions, only returns those actually visible to the account
	 * @param database
	 * @param all
	 * @param account
	 * @return
	 */
	public static List<ChangeSuggestionOpinion> filterChangeSuggestionOpinionVisibleToAccount(Database database, List<ChangeSuggestionOpinion> all, Account account){
		if(account == null) {
			return Collections.emptyList();
		} else {
			List<ChangeSuggestionOpinion>  accountOwned = all.stream()
				    .filter(cso -> UtilsDB.canRead(database, account, cso)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	/**
	 * A list of all the Comments visible to the currently logged in account
	 * @param main
	 * @return
	 */
	public static List<Comment> filterCommentsVisibleToAccount(Database database, List<Comment> all, Account account){
		if(account == null) {
			return Collections.emptyList();
		}
		else {
			List<Comment>  accountOwned = all.stream()
				    .filter(comment -> UtilsDB.canRead(database, account, comment)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	
	/**
	 * Create a new change suggestion and give access rights to the office's group. Returns null if creation failed
	 * @param database
	 * @param documents
	 * @param displayText
	 * @param office
	 * @param account
	 * @param possibleBaseUUID
	 * @param possibleTargetString
	 * @param newSuggestion
	 * @param motivation
	 * @return possibly null, or ChangeSuggestion
	 */
	public static ChangeSuggestion possiblyCreateChangeSuggestion(Database database,
			StrategyMap possibleMap, ResultAgreementDocuments possibleDoc,
			String displayText,
			Office byOffice, Account byAccount,
			String possibleBaseUUID, String possibleTargetString,
			String newSuggestion, String motivation) {
		
		AccountGroup group = UtilsDB.getPossibleAccountGroupByOffice(database, byOffice);
		AccountGroup controllerGroup = database.getDefaultControllerAccountGroup();
		
		if(group == null) {
			System.err.println("Failed to find AccountGroup to associate ChangeSuggestion with.");
			return null;
		}
		
		ChangeSuggestion changeSuggestion = ChangeSuggestion.create(database, displayText,
				byOffice, byAccount,
				possibleBaseUUID, possibleTargetString,
				newSuggestion, motivation);
		
		addChangeSuggestionToBase(database, changeSuggestion, possibleMap);
		addChangeSuggestionToBase(database, changeSuggestion, possibleDoc);
		
		group.addRightIfMissing(new AccessRight(changeSuggestion, false)); //Give read access to group that created the ChangeSuggestion
		if(controllerGroup != null) {
			controllerGroup.addRightIfMissing(new AccessRight(changeSuggestion, true));
		}
		
		Map<Office, AccountGroup> officeAccountGroupMap = new HashMap<>();
		officeAccountGroupMap.put(byOffice, database.getDefaultAdminAccountGroup());
		officeAccountGroupMap.put(findPossibleResultControllerOffice(database), database.getDefaultControllerAccountGroup());
		
		if(possibleBaseUUID != null) {
			Base b = database.find(possibleBaseUUID);
			if(b != null && b instanceof TextChapter) {
				TextChapter chapter = (TextChapter)b;
				Office baseTargetOffice = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
				if(baseTargetOffice != null) {
					officeAccountGroupMap.put(baseTargetOffice, UtilsDB.getPossibleAccountGroupByOffice(database, baseTargetOffice));
				}
			}
		}
		
		possiblyCreateDefaultOpinions(database, changeSuggestion, byOffice, officeAccountGroupMap);
		
		return changeSuggestion;
	}
	
	/**
	 * Find the first Office that is a controller.
	 * @param database
	 * @return
	 */
	public static Office findPossibleResultControllerOffice(Database database) {
		for(Office office : Office.enumerate(database, false, true)){
			if(office.isController) {
				return office;
			}
		}
		return null;
	}
	
	/**
	 * Give full write access to all AccountGroups associated with the Offices provided to the ChangeSuggestion.
	 * @param database
	 * @param changeSuggestion
	 * @param byOffice
	 * @param officeAccountMap
	 * @return
	 */
	private static List<ChangeSuggestionOpinion> possiblyCreateDefaultOpinions(Database database, ChangeSuggestion changeSuggestion, Office byOffice, Map<Office, AccountGroup> officeAccountMap){
		List<ChangeSuggestionOpinion> result = new ArrayList<>();
		for(Office office : officeAccountMap.keySet()) {
			AccountGroup accountGroup = officeAccountMap.get(office);
			if(accountGroup != null && office != null) {
				//System.out.println("Creating CSO for " + office.text);
				ChangeSuggestionOpinion cso = possiblyCreateChangeSuggestionOpinionAndGiveAccessRight(database, changeSuggestion, office, accountGroup);
				if(cso != null) {
					result.add(cso);
				} else {
					System.err.println("FATAL ERROR - Failed to create ChangeSuggestionOpinion!");
				}
			} else {
				System.err.println("FATAL ERROR - Office or Account was null in possiblyCreateDefaultOpinions!");
			}
		}
		changeSuggestion.addOrUpdateOpinion(database, byOffice, OpinionState.OK);
		
		return result;
	}
	
	/**
	 * Return null if fails. Links a new opinion to the ChangeSuggestion and gives access rights to the office to read the ChangeSuggestion and write to the Opinion
	 * Currently logged in user should never be able to do this! Instead, only the current owner of a ChangeSuggestion (or admin) should be able to create this connection.
	 * @param database
	 * @param changeSuggestion
	 * @param text
	 * @param office
	 * @param account
	 * @return
	 */
	public static ChangeSuggestionOpinion possiblyCreateChangeSuggestionOpinion(Database database, ChangeSuggestion changeSuggestion, String text, Office office, Account account) {
		if(!UtilsDB.canWrite(database, account, changeSuggestion)) {
			System.err.println("Account " + account.text + " cannot write to changesuggestion " + changeSuggestion.uuid);
			return null;
		}
		
		AccountGroup group = UtilsDB.getPossibleAccountGroupByOffice(database, office);
		if(group == null) {
			System.err.println("Failed to find AccountGroup to associate ChangeSuggestion with.");
			return null;
		}
		
		ChangeSuggestionOpinion cso = ChangeSuggestionOpinion.create(database, text, office, account);
		
		UtilsDBComments.addToChangeSuggestion(database, changeSuggestion, cso);
		group.addRightIfMissing(new AccessRight(cso, true)); //Give write access for this Opinion to the provided Office
		group.addRightIfMissing(new AccessRight(changeSuggestion, false)); //Give read access to the ChangeSuggestion to the office that gave the opinion
		
		//Gather all other opinions.
		List<ChangeSuggestionOpinion> allOtherOpinions = new ArrayList<>();
		for(Base b : findContainsObjects(database, changeSuggestion)) {
			if(b instanceof ChangeSuggestionOpinion) {
				allOtherOpinions.add((ChangeSuggestionOpinion)b);
			}
		}
				
		//Add a new OpinionState for all opinions since this new ChangeSuggestionOpinion appeared (they previously have had none from this office!)
		for(ChangeSuggestionOpinion alreadyExistingOpinion : allOtherOpinions) {
			Office opinionOwnerOffice = UtilsDB.getPossibleAssociatedOffice(database, alreadyExistingOpinion);
			if(opinionOwnerOffice != null) {
				cso.addOpinionIfMissing(database, opinionOwnerOffice, OpinionState.NO_OPINION_GIVEN);
				//System.out.println("Attempting to give read right to office" + opinionOwnerOffice.text + " to CSO " + cso.uuid);
				UtilsDB.addReadRightToOffice(database, cso, opinionOwnerOffice);
				UtilsDB.addReadRightToOffice(database, alreadyExistingOpinion, office); //This Office must be given read access to all existing ChangeSuggestionOpinions
			} else {
				System.err.println("Failed to find an associated office from opinion " + alreadyExistingOpinion.uuid);
			}
			alreadyExistingOpinion.addOpinionIfMissing(database, office, OpinionState.NO_OPINION_GIVEN); //Default to show no opinion given once an Office joins a ChangeSuggestion
		}
		
		cso.addOrUpdateOpinion(database, office, OpinionState.OK); //Set the opinion of this office for its own CSO to be OK
		changeSuggestion.addOpinionIfMissing(database, office, OpinionState.NO_OPINION_GIVEN);
		
		return cso;
	}
	
	
	/**
	 * Give access right to the Office's account group to the ChangeSuggestion, and create a new ChangeSuggestionOpinion with default values from this Office to the ChangeSuggestion
	 * @param database
	 * @param changeSuggestion
	 * @param account
	 * @param office
	 * @return
	 */
	public static ChangeSuggestionOpinion possiblyCreateChangeSuggestionOpinionAndGiveAccessRight(Database database, ChangeSuggestion changeSuggestion,  Office office, AccountGroup accountGroup) {
		AccountGroup group = UtilsDB.getPossibleAccountGroupByOffice(database, office);
		if(group != null) {
			group.addRightIfMissing(new AccessRight(changeSuggestion, true));
		}
		
		if(accountGroup != null) {
			if(group == null || !group.uuid.equals(accountGroup.uuid)) {
				accountGroup.addRightIfMissing(new AccessRight(changeSuggestion, true));
			}
		}
		
		ChangeSuggestionOpinion cso = possiblyCreateChangeSuggestionOpinion(database, changeSuggestion, "(virallista kantaa ei annettu)", office, database.getDefaultAdminAccount());
		//Don't check access right - just add automatically as system
		return cso;
	}
	
	public static Comment possiblyCreateComment(Database database, String text, Account account, ChangeSuggestion changeSuggestion) {
		
		Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		return possiblyCreateComment(database, text, account, office, changeSuggestion);
	}
	
	/**
	 * 
	 * @param database
	 * @param comment - comment to remove
	 * @param cs - belonging to CS
	 * @param account - deleted by account
	 */
	public static void deleteComment(Database database, Comment comment, ChangeSuggestion cs, Account account) {
		if(comment != null) {
			comment.remove(database);
		}
		
		if(cs != null) {
			UtilsDB.removeAllSeenByAccount(database, cs);
			if(account != null) {
				UtilsDB.setSeenByAccount(database, account, cs);
			}
		}
	}
	
	
	/**
	 * Can return null if comment creation failed!
	 * @param database
	 * @param text
	 * @param account
	 * @param changeSuggestion
	 * @return
	 */
	public static Comment possiblyCreateComment(Database database, String text, Account account, Office office, ChangeSuggestion changeSuggestion) {
		Comment c = Comment.create(database, text, account, office);
		UtilsDBComments.addToChangeSuggestion(database, changeSuggestion, c);
		
		UtilsDB.removeAllSeenByAccount(database, changeSuggestion);
		UtilsDB.setSeenByAccount(database, account, changeSuggestion);
		
		return c;
	}
	
	/**
	 * Provided a list of ChangeSuggestions, only returns those actually owned by the account
	 * @param database
	 * @param all
	 * @param account
	 * @return
	 */
	public static List<ChangeSuggestion> filterChangeSuggestionOwnedByAccount(Database database, List<ChangeSuggestion> all, Account account){
		if(account == null) {
			return Collections.emptyList();
		} else {
			List<ChangeSuggestion>  accountOwned = all.stream()
				    .filter(cs -> baseIsFromAccount(database, cs, account)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	/**
	 * Provided a list of ChangeSuggestionOpinions, only returns those actually owned by the account
	 * @param database
	 * @param all
	 * @param account
	 * @return
	 */
	public static List<ChangeSuggestionOpinion> filterChangeSuggestionOpinionOwnedByAccount(Database database, List<ChangeSuggestionOpinion> all, Account account){
		if(account == null) {
			return Collections.emptyList();
		} else {
			List<ChangeSuggestionOpinion>  accountOwned = all.stream()
				    .filter(cs -> baseIsFromAccount(database, cs, account)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	/**
	 * A list of all the Comments created by the currently logged in account
	 * @param main
	 * @return
	 */
	public static List<Comment> filterCommentsOwnedByAccount(Database database, List<Comment> all, Account account){
		if(account == null) {
			return Collections.emptyList();
		}
		else {
			List<Comment>  accountOwned = all.stream()
				    .filter(cs -> baseIsFromAccount(database, cs, account)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	
}
