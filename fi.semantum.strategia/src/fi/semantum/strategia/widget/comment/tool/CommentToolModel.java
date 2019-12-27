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
package fi.semantum.strategia.widget.comment.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDBComments;

public class CommentToolModel {

	/**
	 * All change suggestions this account is allowed to see and active
	 */
	public List<ChangeSuggestion> visibleChangeSuggestionsActive = new ArrayList<>();
	
	/**
	 * All change suggestions this account is allowed to see and on hold
	 */
	public List<ChangeSuggestion> visibleChangeSuggestionsOnHold = new ArrayList<>();

	/**
	 * All change suggestions this account is allowed to see and closed
	 */
	public List<ChangeSuggestion> visibleChangeSuggestionsClosed = new ArrayList<>();

	private String resultsFrom = null;
	
	public CommentToolModel() {
		
	}
	
	private void clearAll() {
		visibleChangeSuggestionsOnHold.clear();
		visibleChangeSuggestionsOnHold = new ArrayList<>();
		visibleChangeSuggestionsActive.clear();
		visibleChangeSuggestionsActive = new ArrayList<>();
		resultsFrom = null;
	}
	
	public String getResultsFrom() {
		return resultsFrom;
	}
	
	private void update(Main main) {
		resultsFrom = null;
		
		Account account = main.account;
		if(account == null) {
			clearAll();
		} else {
			Database database = main.getDatabase();
			StrategyMap selectedMap = database.getRoot();
			
			if(selectedMap == null) {
				System.err.println("No StrategyMap selected - cannot fetch ChangeSuggestions!");
				clearAll();
			} else {
				//If we have a year active and there's a ResultAgreementDocument, then only show the results for that.
				//Otherwise, show all those belonging to map.

				List<ChangeSuggestion> allSuggestionsForSelection;
				ResultAgreementDocuments currentDocs = main.getUIState().showSelectedDocuments ? ResultAgreementDocuments.getInstance(database) : null;
				if(currentDocs != null) {
					resultsFrom = "tulossopimukseen";
					allSuggestionsForSelection = UtilsDBComments.getPossibleLinkedChangeSuggestions(database, currentDocs);
				} else {
					resultsFrom = "karttaan";
					allSuggestionsForSelection = UtilsDBComments.getPossibleLinkedChangeSuggestions(database, selectedMap);
				}

				List<ChangeSuggestion> changeSuggestionsVisibleToAccount = UtilsDBComments.filterChangeSuggestionVisibleToAccount(database, main.getAccountDefault(),
						allSuggestionsForSelection);
				//Visible active
				visibleChangeSuggestionsActive = changeSuggestionsVisibleToAccount.stream().filter(cs -> cs.isActive()).collect(Collectors.toList());
				//Visible closed
				visibleChangeSuggestionsClosed = changeSuggestionsVisibleToAccount.stream().filter(cs -> cs.isClosed()).collect(Collectors.toList());
				//Visible on hold
				visibleChangeSuggestionsOnHold = changeSuggestionsVisibleToAccount.stream().filter(cs -> cs.isOnHold()).collect(Collectors.toList());
				
				//Sort after initialization
				Collections.sort(visibleChangeSuggestionsActive, ChangeSuggestion.creationTimeComparator);
				Collections.reverse(visibleChangeSuggestionsActive);
				
				Collections.sort(visibleChangeSuggestionsClosed, ChangeSuggestion.creationTimeComparator);
				Collections.reverse(visibleChangeSuggestionsClosed);
				
				Collections.sort(visibleChangeSuggestionsOnHold, ChangeSuggestion.creationTimeComparator);
				Collections.reverse(visibleChangeSuggestionsOnHold);
			}
		}
	}
	
	/**
	 * Update to be up-to-date with Server
	 * @param main
	 */
	public void updateFromServer(Main main) {
		update(main);
	}
	
}
