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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class contains all the Result agreement documents for all the offices
 * Some of the chapters in the result agreements are shared by the offices
 * These are listed in the sharedChapters.
 * The other chapters are unique to the offices.
 * The Database implements a relation between a Strategymap and a ResultAgreementDocument object.
 * From the Strategymap, you'll be able to find the objects related to a specific office.
 * Use the toHTML functions to generate a complete ResultAgreementDocument for each of the offices.
 * @author Miro Eklund
 *
 */
public class ResultAgreementDocuments extends Base implements Serializable {
	
	private static final long serialVersionUID = 3083352363161799265L;
	
	/**
	 * Deprecated field. Use global ResultAgreementConfiguration getAllIndeces() instead.
	 * Keep this field for backwards compatibility with old Databases.
	 */
	@Deprecated
	public int[] expectedIndexes;
	
	private ResultAgreementDocuments(String description, String text) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), text);
		this.description = description;
	}
	
	/**
	 * Create a ResultAgreementDocument
	 * Transient does not register the object to the database
	 * @param database
	 * @param map
	 * @param id
	 * @param text
	 * @param chapterIndex
	 * @return
	 */
	public static ResultAgreementDocuments createTransient(Database database, String description, String text) {
		ResultAgreementDocuments rad = new ResultAgreementDocuments(description, text);
		return rad;
	}
	
	/**
	 * Create a ResultAgreementDocument object and register it to the database
	 * @param database
	 * @param map
	 * @param id
	 * @param text
	 * @param chapterIndex
	 * @return
	 */
	public static ResultAgreementDocuments create(Database database, String description, String text) {
		ResultAgreementDocuments rad = createTransient(database, description, text);
		database.register(rad);
		return rad;
	}
	
	public static ResultAgreementDocuments getInstance(Database database) {
		for (Base b : database.enumerate()) {
			if (b instanceof ResultAgreementDocuments)
				return ((ResultAgreementDocuments) b);
		}
		
		return null;
	}
	
	private boolean isExpected(Database database, TextChapter chapter) {
		for(int index : (ResultAgreementConfiguration.getInstance(database).getAllIndeces())) {
			if(chapter.chapterIndex == index) {
				return true;
			}
		}
		return false;
	}
	
	public void unregisterChapter(Database database, TextChapter chapter) {
		this.denyRelation(database, Relation.find(database, Relation.CONTAINS_CHAPTER), chapter);
		chapter.denyRelation(database, Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE), this);
	}
	
	/**
	 * Same as transient version but also writes the DB relation CONTAINS_CHAPTER
	 * Also writes an inverse relation to the TextChapter that points to this Documents objects
	 * @param database
	 * @param chapter
	 */
	public boolean registerChapter(Database database, TextChapter chapter) {
		if(isExpected(database, chapter)) {
			this.addRelation(Relation.find(database, Relation.CONTAINS_CHAPTER), chapter);
			chapter.addRelation(Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE), this); //Inverse that points from Chapter to Documents
			return true;
		} else {
			System.err.println("Unexpected TextChapter index " + chapter.chapterIndex + " added to ResultAgreementDocument.");
			return false;
		}
	}

	/**
	 * Purge the ResultAgreements of the provide Office's chapters. Only relevant if Office is removed from database.
	 * @param database
	 * @return
	 */
	public boolean denyAllChaptersForOffice(Database database, Office office) {
		Set<TextChapter> chapters = getOfficeChapterSet(database, office);
		for(TextChapter chapter : chapters) {
			this.denyRelation(database, Relation.find(database, Relation.CONTAINS_CHAPTER), chapter);
			chapter.denyRelation(database, Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE), this);
			chapter.remove(database);
		}
		return true;
	}
	
	@Override
	public Base getOwner(Database database) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * List all HTML versions of the ResultAgreementDocument, one for each office
	 * @param database
	 * @return
	 */
	public List<String> allHTMLs(Database database){
		List<String> result = new ArrayList<>();
		for(Office office : Office.enumerate(database, true, false)) {
			result.add(this.toHTML(database, office));
		}
		return(result);
	}
	
	/**
	 * Show entire document with all chapters and strategymap as HTML
	 * @param database
	 * @param office
	 * @return
	 */
	public String toHTML(Database database, Office office) {
		return(toHTMLByUUID(database, office.uuid));
	}
	
	public List<String> pureChapterHTMLs(Database database, Office office){
		return pureChapterHTMLs(database, office.uuid);
	}	
	
	public List<String> pureChapterHTMLs(Database database, String officeUUID){
		List<String> result = new ArrayList<>();
		
		for(int i : getAllIndeces(database)) {
			String html = chapterHTML(database, i, officeUUID);
			result.add(html);
		}
		
		return(result);
	}
	
	/**
	 * Find the office-specific chapters and all the shared chapters and return them in an ordered list
	 * @param database
	 * @param office
	 * @return
	 */
	public List<TextChapter> getOfficeAndSharedChapters(Database database, Office office){
		List<TextChapter> result = new ArrayList<>();
		Map<Integer, TextChapter> sharedChapters = getSharedChapters(database);
		Map<Integer, List<TextChapter>> officeChapters = getOfficeChapters(database);
		
		for(int i : getAllIndeces(database)) {
			TextChapter chapter = getPossibleChapterByOfficeUUID(sharedChapters, officeChapters, database, i, office.uuid);
			result.add(chapter);
		}
		
		return(result);
	}
	
	/**
	 * 
	 * @param database
	 * @param office
	 * @return
	 */
	private Set<TextChapter> getOfficeChapterSet(Database database, Office office){
		Set<TextChapter> result = new HashSet<>();
		for(int chapterIndex : getOfficeChapterIndeces(database)) {
			TextChapter chapter = this.getPossibleChapterByOffice(database, chapterIndex, office);
			if(chapter != null) {
				result.add(chapter);
			}
		}
		return result;
	}
	
	
	
	/**
	 * 
	 * @param database
	 * @return
	 */
	public List<TextChapter> getAllChapters(Database database) {
		Collection<Base> chapterBases = this.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS_CHAPTER));
		List<TextChapter> result = new ArrayList<>();
		for(Base b : chapterBases) {
			if(b instanceof TextChapter) {
				result.add((TextChapter)b);
			}
		}
		return result;
	}
	
	public Map<Integer, TextChapter> getSharedChapters(Database database){
		Map<Integer, TextChapter> sharedChapters = new HashMap<>();
		for(TextChapter tc : getAllChapters(database)) {
			if(UtilsDB.getTextChaptersPossibleRelatedOffice(database, tc) == null) {
				if(sharedChapters.containsKey(tc.chapterIndex)) {
					System.err.println("Duplicate keys found when creating getSharedChapters map! Key was: " + tc.chapterIndex);
				}
				sharedChapters.put(tc.chapterIndex, tc);
			}
		}
		return sharedChapters;
	}
	
	public Map<Integer, List<TextChapter>> getOfficeChapters(Database database){
		Map<Integer, List<TextChapter>> officeChapters = new HashMap<>();
		for(TextChapter tc : getAllChapters(database)) {
			if(UtilsDB.getTextChaptersPossibleRelatedOffice(database, tc) != null) {
				putInMapList(officeChapters, tc);
			}
		}
		return officeChapters;
	}
	
	/**
	 * Put the chapter in the map's index chapterIndex by updating the list
	 * @param targetMap
	 * @param chapter
	 */
	private void putInMapList(Map<Integer, List<TextChapter>> targetMap, TextChapter chapter) {
		int chapterIndex = chapter.getChapterIndex();
		
		List<TextChapter> subChapters = targetMap.get(chapterIndex);
		if(subChapters != null) {
			subChapters.add(chapter);
			targetMap.put(chapterIndex, subChapters);
		} else {
			List<TextChapter> asList = new ArrayList<TextChapter>();
			asList.add(chapter);
			targetMap.put(chapterIndex, asList);
		}
	}
	
	public String chapterHTML(Database database, int i, String officeUUID) {
		Map<Integer, TextChapter> sharedChapters = getSharedChapters(database);
		Map<Integer, List<TextChapter>> officeChapters = getOfficeChapters(database);
		
		if(sharedChapters.containsKey(i)) {
			TextChapter sharedChapter = sharedChapters.get(i);
			String html = sharedChapter.getHTML(); //Get unformatted styling
			if(html.equals("")) {
				return "<h3>Placeholder chapter title</h3>";
			} else {
				return(html);
			}
		} else if(officeChapters.containsKey(i)) {
			TextChapter possibleOfficeChapter = getPossibleChapterByOfficeUUID(sharedChapters, officeChapters, database, i, officeUUID);
			if(possibleOfficeChapter != null) {
				String html = possibleOfficeChapter.getHTML();
				if(html.equals("")) {
					return ("<h3>Placeholder chapter title</h3>");
				} else {
					return (html);
				}
				
			} else {
				return ("<p>Virhe - Tietokannasta ei löytynyt viraston tietoa!</p>");
			}
		} else {
			throw(new NullPointerException("Incorrectly defined indeces for chapters!"));
		}
	}
	
	/**
	 * Same as toHTML but takes the uuid of the office rather than the Office object
	 * @param database
	 * @param officeUUID
	 * @return
	 */
	public String toHTMLByUUID(Database database, String officeUUID) {
		String result = "";
		List<String> htmls = this.pureChapterHTMLs(database, officeUUID);
		for(String html : htmls) {
			result += html;
		}
		return(result);
	}
	
	/**
	 * Optional office parameter for choosing which office chapter to use
	 * If chapterIndex points on a shared chapter, returns that. Otherwise tries to find by office
	 * @param chapterIndex
	 * @param office
	 * @return
	 */
	public TextChapter getPossibleChapterByOffice(Database database, int chapterIndex, Office office) {
		Map<Integer, TextChapter> sharedChapters = getSharedChapters(database);
		Map<Integer, List<TextChapter>> officeChapters = getOfficeChapters(database);
		TextChapter chapter = getPossibleChapterByOfficeUUID(sharedChapters, officeChapters, database, chapterIndex, office.uuid);
		if(chapter == null) {
			System.err.println("Chapter was null for office " + office.getText(database) + " and index: " + chapterIndex);
		}
		return chapter;
	}
	
	/**
	 * Find the indeces for shared chapters
	 * @param database
	 * @return
	 */
	public Set<Integer> getSharedChapterIndeces(Database database){
		Map<Integer, TextChapter> sharedChapters = getSharedChapters(database);
		if(sharedChapters == null) {
			return Collections.emptySet();
		}
		return sharedChapters.keySet();
	}
	
	public Set<Integer> getAllIndeces(Database database){
		Set<Integer> indexSet = new HashSet<>();
		indexSet.addAll(getSharedChapterIndeces(database));
		indexSet.addAll(getOfficeChapterIndeces(database));
		return indexSet;
	}
	
	/**
	 * Find indeces for office chapters
	 * @param database
	 * @return
	 */
	public Set<Integer> getOfficeChapterIndeces(Database database){
		Map<Integer, List<TextChapter>> officeChapters = getOfficeChapters(database);
		if(officeChapters == null) {
			return Collections.emptySet();
		}
		return officeChapters.keySet();
	}
	
	/**
	 * Returns the chapter matching the chapter index. If it is an office chapter, returns the chapter belonging to the office.
	 * If it is a sharedChapter, returns the chapter.
	 * @param chapterIndex
	 * @param officeUUID
	 * @return null if no chapter found
	 */
	private TextChapter getPossibleChapterByOfficeUUID(Map<Integer, TextChapter> sharedChapters, Map<Integer, List<TextChapter>> officeChapters,
			Database database, int chapterIndex, String officeUUID) {
		//Attempt to find from shared chapters first
		TextChapter result = sharedChapters.get(chapterIndex);
		
		if(result == null) {
			List<TextChapter> officeChaptersInIndex = officeChapters.get(chapterIndex);
			if(officeChaptersInIndex != null) {
				for(int i = 0; i < officeChaptersInIndex.size(); i++) {
					TextChapter chapter = officeChaptersInIndex.get(i);
					Office office = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
					if(office.uuid.equals(officeUUID)) {
						result = officeChaptersInIndex.get(i);
						break;
					}
				}
			}
		}
		
		return (result);
	}
	
	@Override
	public String clientIdentity() {
		return "Tulostavoitedokumentti";
	}
	
}
