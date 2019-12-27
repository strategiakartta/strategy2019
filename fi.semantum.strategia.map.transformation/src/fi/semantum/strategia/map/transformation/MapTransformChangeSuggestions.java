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
package fi.semantum.strategia.map.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Attributes;

import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;
import fi.semantum.strategy.db.UtilsDBComments;

public class MapTransformChangeSuggestions implements HTMLMapTransform {

	@Override
	public String tag() {
		return "muutosehdotukset";
	}
	
	private static final String FROM_OFFICE = "virastolta";
	private static final String OFFICE_RESULT_AGREEMENT_DOCUMENT = "virastonTulossopimus";
	
	@Override
	public Map<String, String> possibleParametersGuide(Database database){
		List<Office> offices = Office.enumerate(database, true, false);
		List<String> officeIDs = new ArrayList<>(offices.size());
		for(Office o : offices) {
			officeIDs.add(o.getShortId(database));
		}
		
		Map<String, String> allowedParameters = new HashMap<>();
		allowedParameters.put(FROM_OFFICE, "Ota vain muutosehdotukset mukaan, jotka tulevat virastolta.\n"
				+ "Esim: " + FROM_OFFICE + "=\"LVM\".\n"
						+ "Virastojen lyhytnimet:" + officeIDs);
		allowedParameters.put(OFFICE_RESULT_AGREEMENT_DOCUMENT, "Ota vain muutosehdotukset mukaan, jotka kohdistuvat kyseisen viraston tulossopimukseen.\n"
				+ "Esim: " + OFFICE_RESULT_AGREEMENT_DOCUMENT + "=\"LVM\".\n"
						+ "Virastojen lyhytnimet: " + officeIDs);
		return allowedParameters;
	}
	
	void changeSuggestionToRow(Database database, ChangeSuggestion cs, MapTable result, int rowId) {
		String why = cs.getMotivation() == null ? "" : cs.getMotivation();
		String how = cs.getNewSuggestion() == null ? "" : cs.getNewSuggestion();

		String possibleTargetStringCaption = cs.getPossibleTargetString();
		if(possibleTargetStringCaption == null || possibleTargetStringCaption.equals("")) {
			possibleTargetStringCaption = "(muutosehdotuksella ei ole tekstikohdetta)";
		}

		String what = possibleTargetStringCaption; 

		List<String> parents = UtilsDB.changeSuggestionTargetPathAsList(database, cs);
		String targetBaseSummary = "";
		for(int i = 0; i < parents.size(); i++) {
			int mm = 7 + i*3;
			targetBaseSummary += "<div style=\"margin-left: " + mm + "mm;\">" + parents.get(i) + "</div>";
		}
		
		result.cells.add("<div style=\"margin-left: 1mm; font-weight: bold;\">"
				+ rowId + ". Muutosehdotuksen kohde: " +"</div>" + targetBaseSummary);
		
		result.cells.add("<div style=\"margin-left: 7mm; font-weight: bold;\">Miten muokataan:</div><div style=\"margin-left: 12mm;\">" + how + "</div>");
		result.cells.add("<div style=\"margin-left: 7mm; font-weight: bold;\">Mitä muokataan:</div><div style=\"margin-left: 12mm;\">" + what + "</div>");
		result.cells.add("<div style=\"margin-left: 7mm; font-weight: bold;\">Miksi muokataan:</div><div style=\"margin-left: 12mm;\">" + why + "</div>");
	}
	
	boolean canAdd(Database database, ChangeSuggestion cs, Office mustBeFromOffice,
			ResultAgreementDocuments mustTargetDocuments, Office mustTargetOfficeChapters) {
		if(!cs.isActive()) {
			return false;
		}
		
		if(mustBeFromOffice != null) {
			//System.out.println("Filtering by office " + mustBeFromOffice.shortID);
			Office csFromOffice = UtilsDB.getPossibleAssociatedOffice(database, cs);
			if(csFromOffice == null) {
				//System.out.println("ChangeSuggestion has no office, cannot match");
				return false; //Cannot be from Office mustBeFromOffice, since CS doesn't have office
			} else {
				if(!mustBeFromOffice.uuid.equals(csFromOffice.uuid)) {
					//System.out.println("Offices did not match. Filtered by: " + mustBeFromOffice.shortID + ", got " + csFromOffice.shortID);
					return false;
				}
			}
		}
		
		if(mustTargetDocuments != null) {
			//System.out.println("Filtering by document " + mustTargetDocuments.year);
			String uuid = cs.getPossibleBaseTargetUUID();
			if(uuid == null) {
				return false; //Cannot target the document or chapters, since the target is null
			} else {
				Base targeting = database.find(uuid);
				if(targeting != null && targeting instanceof TextChapter) {
					TextChapter targetedTextChapter = (TextChapter)targeting;
					Base parent = targetedTextChapter.getOwner(database);
					if(parent != null && parent instanceof ResultAgreementDocuments) {
						if(mustTargetDocuments.uuid.equals(parent.uuid)) {
							if(mustTargetOfficeChapters != null) {
								Office targetedTextChapterOffice = UtilsDB.getTextChaptersPossibleRelatedOffice(database, targetedTextChapter);
								if(targetedTextChapterOffice != null) {
									return mustTargetOfficeChapters.uuid.equals(targetedTextChapterOffice.uuid);
								} else {
									return true; //The targeted text chapter is a shared text chapter, accept this ChangeSuggestion
								}
							} else {
								return true; //Targeted the same documents as expected
							}
						} else {
							return false; //Target documents and parent documents do not match
						}
					} else {
						return false; //Invalid parent for this targeted TextChapter
					}
				} else {
					return false; //Doesn't target a valid chapter
				}
			}
		}
		
		return true;
		
	}
	
	MapTable errorTable(String message) {
		MapTable error = new MapTable();
		error.columns = 1;
		error.widths.add(MapTransformationUtils.TABLE_MAX_WIDTH);
		error.cells.add("<div>" + message + "</div>");
		return error;
	}
	
	MapTable buildTable(Database database, StrategyMap map, Attributes attributes) {
		//ChangeSuggestion must have been given by this office:
		Office mustBeFromOffice = null;
		//ChangeSuggestion must target a TextChapter in this documents:
		ResultAgreementDocuments mustTargetDocuments = null;
		//ChangeSuggestion must target a shared TextChapter, or an Office TextChapter from the above document, matching this office:
		Office mustTargetOfficeChapters = null;
		
		if(attributes.hasKey(FROM_OFFICE)) {
			//System.out.println("FROM OFFICE Attribute found. Value: " + attributes.get(FROM_OFFICE));
			mustBeFromOffice = Office.possiblyFindByShortID(database, attributes.get(FROM_OFFICE));
			if(mustBeFromOffice == null) {
				return errorTable("Virastoa ei löytynyt: " + attributes.get(FROM_OFFICE));
			}
		}
		
		if(attributes.hasKey(OFFICE_RESULT_AGREEMENT_DOCUMENT)) {
			//System.out.println("OFFICE_RESULT_AGREEMENT_DOCUMENT Attribute found. Value: " + attributes.get(OFFICE_RESULT_AGREEMENT_DOCUMENT));
			mustTargetOfficeChapters = Office.possiblyFindByShortID(database, attributes.get(OFFICE_RESULT_AGREEMENT_DOCUMENT));
			if(mustTargetOfficeChapters == null) {
				return errorTable("Virastoa ei löytynyt: " + attributes.get(OFFICE_RESULT_AGREEMENT_DOCUMENT));
			}
			mustTargetDocuments = UtilsDB.getPossibleLinkedResultAgreementDocuments(database, map);
		}
		
		MapTable result = new MapTable();
		List<ChangeSuggestion> changeSuggestionsAll = UtilsDBComments.getPossibleLinkedChangeSuggestions(database, map);
		List<ChangeSuggestion> changeSuggestions = new ArrayList<>();
		for(ChangeSuggestion cs : changeSuggestionsAll) {
			if(canAdd(database, cs, mustBeFromOffice, mustTargetDocuments, mustTargetOfficeChapters)) {
				changeSuggestions.add(cs);
			}
		}
		
		Collections.sort(changeSuggestions, Base.creationTimeComparator);
		
		result.columns = 1;
		result.widths.add(MapTransformationUtils.TABLE_MAX_WIDTH);
		result.widthUnit = "mm";
		result.cells.add("<div style=\"font-size: 16px; font-weight: bold; text-align: center;\">Muutosehdotukset</div>");
		int counter = 1;
		
		for(ChangeSuggestion cs : changeSuggestions) {
			changeSuggestionToRow(database, cs, result, counter);
			counter++;
		}
		
		return result;

	}

	@Override
	public String replace(Database database, StrategyMap map, Attributes attributes) {

		return MapTransformationUtils.createTable(buildTable(database, map, attributes));

	}


}