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

public class Relation extends Base {

	private static final long serialVersionUID = -1598499411379047877L;
	
	//Don't move these to Terminology - needed for database and Relation!
	public static final String IMPLEMENTS = "Toteuttaa";
	public static final String COPY = "Kopio";
	public static final String MEASURES = "Mittaa";
	public static final String RESPONSIBILITY_MODEL = "Vastuumalli";
	public static final String RESPONSIBILITY_INSTANCE = "Vastuut";
	
	@Deprecated
	public static final String ALLOWS_SUBMAP = "Sallittu alikartta";

	public static final String RELATED_TO_TAG = "Liittyy aihetunnisteeseen";
	public static final String MONITORS_TAG = "Monitoroi aihetunnistetta";

	public static final String TAVOITE_SUBMAP = "Tavoitteen alikarttatyyppi";
	
	/**
	 * Relation indicating which account created the base object
	 */
	public static final String FROM_ACCOUNT = "From Account";
	
	/**
	 * Relation indicating which office created the base object
	 */
	public static final String FROM_OFFICE = "From Office";
	
	/**
	 * Used by GathersOptions objects to store Opinion objects
	 */
	public static final String HAS_OPINION = "Has Opinion";
	
	/**
	 * Used by StrategiaKartta and ResultAgreementDocuments to indicate linkage.
	 */
	public static final String LINKED_TO = "Linked to";
	
	/**
	 * StrategyMaps have change suggestions. ResultAgreementDocuments have change suggestions.
	 * Reverse: PART_OF
	 */
	public static final String HAS_CHANGE_SUGGESTION = "Has Change Suggestion";
	
	/**
	 * Used by chapters to indicate which office it is part of.
	 * Also used to associate Account with AccountGroups. An Account can be part of many AccountGroups.
	 */
	public static final String PART_OF = "Part of";
	
	/**
	 * Used by Offices to indicate which StrategyMap element they were created from
	 */
	public static final String BASE_TO_OFFICE_ATTACHMENT = "Base-to-Office Attachment";
	public static final String BASE_TO_OFFICE_ATTACHMENT_INVERSE = "Office-to-Base Attachment";
	
	/**
	 * A Base-to-Account Relation that keeps track of which users have seen an element.
	 * If all accounts have seen a Base, then all Accounts are found with this relation.
	 * If no accounts have seen a Base, this Relation returns an empty collection.
	 * 
	 */
	public static final String SEEN_BY_USER = "Seen By User";
	
	/**
	 * ChangeSuggestions contain a set of ChangeSuggestionOpinions and Comments
	 * CalendarYear contains CalendarEvents
	 */
	public static final String CONTAINS = "Contains";
	
	/**
	 * Used by ResultAgreementDocument to indicated chapters
	 */
	public static final String CONTAINS_CHAPTER = "Containers Chapter";
	public static final String CONTAINS_CHAPTER_INVERSE = "Contains Chapter Inverse";
	
	public static Relation create(Database database, String name) {
		Relation p = new Relation(name);
		database.register(p);
		return p;
	}
	
	private Relation(String name) {
		super(UUID.randomUUID().toString(), name, name);
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	public static Relation find(Database database, String name) {
		
		for(Base b : database.objects.values()) {
			if(b instanceof Relation) {
				Relation p = (Relation)b;
				if(name.equals(p.text)) return p;
			}
		}
		return null;

	}
	
	public List<Pair> getRelations(Base b) {
		ArrayList<Pair> result = new ArrayList<Pair>();
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) {
				result.add(p);
			}
		}
		return result;
	}

	public Pair getPossibleRelation(Base b) {
		Pair result = null;
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) {
				if(result != null) return null;
				result = p;
			}
		}
		return result;
	}

	public <T extends Base> T getPossibleRelationObject(Database database, Base b) {
		Pair p = getPossibleRelation(b);
		if(p == null) return null;
		else return database.find(p.second);
	}

	public boolean hasRelations(Base b) {
		for(Pair p : b.relations) {
			if(uuid.equals(p.first)) return true;
		}
		return false;
	}
	
	@Override
	public String clientIdentity() {
		return "Relaatio";
	}

}
