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
package fi.semantum.strategia;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.ChangeSuggestionOpinion;
import fi.semantum.strategy.db.Comment;
import fi.semantum.strategy.db.CommentToolAdminBase;
import fi.semantum.strategy.db.CommentToolBase;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.Opinion;
import fi.semantum.strategy.db.Opinion.OpinionState;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDBComments;

public class UtilsComments {

	/**
	 * Create a new change suggestion from the logged in account. Returns null if creation failed
	 * @param main
	 * @param documents
	 * @param displayText
	 * @param possibleBaseUUID
	 * @param possibleTargetString
	 * @param newSuggestion
	 * @param motivation
	 * @return
	 */
	public static ChangeSuggestion possiblyCreateChangeSuggestion(Main main,
			String displayText,
			String possibleBaseUUID, String possibleTargetString,
			String newSuggestion, String motivation) {
		
		Database database = main.getDatabase();
		
		Account account = main.account;
		if(account == null) return null;
		
		Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		if(office == null) return null;
		
		StrategyMap map =  main.getDatabase().getRoot();
		ResultAgreementDocuments doc = ResultAgreementDocuments.getInstance(database);
		
		return UtilsDBComments.possiblyCreateChangeSuggestion(database, map, doc, displayText,
				office, account,
				possibleBaseUUID, possibleTargetString,
				newSuggestion, motivation);
	}
	
	/**
	 * Currently logged in account creates a comment for a change suggestion, associating it with their office
	 * @param main
	 * @param text
	 * @param changeSuggestion
	 * @return
	 */
	public static Comment possiblyCreateComment(Main main, String text, ChangeSuggestion changeSuggestion) {
		Account account = main.account;
		if(account != null) {
			return(UtilsDBComments.possiblyCreateComment(main.getDatabase(), text, account, changeSuggestion));
		} else {
			return null;
		}
	}
	
	/**
 	 * Given a change suggestion and an office, updates that office's opinion.
	 * Also sets all other office's opinions on this offices opinion to NO_OPINION_GIVEN, requiring all other offices to give an opinion on this new opinion.
	 * @param main
	 * @param changeSuggestion
	 * @param office
	 * @param officialStatement
	 */
	public static void updateOfficialStatement(Main main, ChangeSuggestion changeSuggestion, Office office, String officialStatement) {
		ChangeSuggestionOpinion cos = UtilsDBComments.getPossibleChangeSuggestionOpinionByOffice(main.getDatabase(), changeSuggestion, office);
		cos.setOfficialStatement(officialStatement);
		
		List<Opinion> opinions = cos.getOpinions(main.getDatabase());
		for(Opinion opinion : opinions) {
			Office possibleOffice = opinion.possiblyGetOffice(main.getDatabase());
			if(possibleOffice != null) {
				if(possibleOffice.uuid.equals(office.uuid)) {
					opinion.setState(OpinionState.OK);
				} else {
					opinion.setState(OpinionState.NO_OPINION_GIVEN);
				}
			}
		}
		Updates.update(main, true);
	}
	
	/**
	 * 
	 * @param main
	 * @param cs
	 * @return
	 */
	public static boolean accountCanWriteToChangeSuggestion(Main main, ChangeSuggestion cs) {
		Account account = main.account;
		if(account != null) {
			Database database = main.getDatabase();
			return accountHasCommentToolAdminWriteRights(main) || UtilsDB.canWrite(database, account, cs);
		} else {
			return false;
		}
	}
	
	/**
	 * Check if the logged in user is allowed to read any comments at all
	 * @return
	 */
	public static boolean accountHasCommentToolReadRights(Main main) {
		Account acc = main.account;
		if(acc != null) {
			Database database = main.getDatabase();
			if(acc.isAdmin(database) || CommentToolAdminBase.canRead(database, acc)) {
				return true;
			} else {
				return CommentToolBase.canRead(database, acc);
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Check if the logged in user is allowed to write to admin-only rights in the comment tool
	 * @return
	 */
	public static boolean accountHasCommentToolAdminWriteRights(Main main) {
		Account acc = main.account;
		if(acc != null) {
			Database database = main.getDatabase();
			return acc.isAdmin(database) || CommentToolAdminBase.canWrite(database, acc);
		} else {
			return false;
		}
	}
	
	/**
	 * Check if the logged in user is allowed to write to non-admin settings.
	 * If they have access to admin settings, they also have access to non-admin settings.
	 * Visible ChangeSuggestions: Only those of the related office
	 * @return
	 */
	public static boolean accountHasCommentToolWriteRights(Main main) {
		Account acc = main.account;
		if(acc != null) {
			Database database = main.getDatabase();
			if(acc.isAdmin(database) || CommentToolAdminBase.canWrite(database, acc)) {
				return true;
			} else {
				return CommentToolBase.canWrite(database, acc);
			}
		} else {
			return false;
		}
	}
	
	/**
	 * A list of all the ChangeSuggestions created by the provided office
	 * @param main
	 * @param office
	 * @return
	 */
	public static List<ChangeSuggestion> findOfficeOwnedChangeSuggestions(Main main, Office office){
		if(office == null) {
			return Collections.emptyList();
		}
		else {
			List<ChangeSuggestion> all = UtilsDBComments.getPossibleLinkedChangeSuggestions(main.getDatabase(), main.getDatabase().getRoot());
			List<ChangeSuggestion>  accountOwned = all.stream()
				    .filter(cs -> UtilsDBComments.baseIsFromOffice(main.getDatabase(), cs, office)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	/**
	 * A list of all the ChangeSuggestionOpinionss created by the provided office
	 * @param main
	 * @param office
	 * @return
	 */
	public static List<ChangeSuggestionOpinion> findOfficeOwnedChangeSuggestionOpinions(Main main, Office office){
		if(office == null) {
			return Collections.emptyList();
		}
		else {
			List<ChangeSuggestionOpinion> all = ChangeSuggestionOpinion.enumerate(main.getDatabase());
			List<ChangeSuggestionOpinion>  accountOwned = all.stream()
				    .filter(cs -> UtilsDBComments.baseIsFromOffice(main.getDatabase(), cs, office)).collect(Collectors.toList());
			return accountOwned;
		}
	}
	
	/**
	 * A list of all the Comments created by the provided office
	 * @param main
	 * @param office
	 * @return
	 */
	public static List<Comment> findOfficeOwnedComments(Main main, Office office){
		if(office == null) {
			return Collections.emptyList();
		}
		else {
			List<Comment> all = Comment.enumerate(main.getDatabase());
			List<Comment>  accountOwned = all.stream()
				    .filter(cs -> UtilsDBComments.baseIsFromOffice(main.getDatabase(), cs, office)).collect(Collectors.toList());
			return accountOwned;
		}
	}

	/**
	 * Adds a Contains relation between the ChangeSuggestion and the base object. Base object is either ChangeSuggestionOpinion or Comment
	 * @param database
	 * @param cs
	 * @param cos
	 */
	public static void addToChangeSuggestion(Main main, ChangeSuggestion cs, Base b) {
		UtilsDBComments.addToChangeSuggestion(main.getDatabase(), cs, b);
		Updates.update(main, true);
	}
	
	
	
}
