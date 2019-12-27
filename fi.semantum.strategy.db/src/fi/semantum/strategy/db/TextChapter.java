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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A text chapter that is part of generating a final HTML document.
 * Default use-case: Chapters 1-3 are shared, chapter 4-7 are office specific and chapter 5 is a non-text, pure map chapter.
 * The chapters are used to generate a HTML, then a PDF document.
 * Each map contains knowledge of the surrounding chapters.
 * @author Miro Eklund
 *
 */
public class TextChapter extends MarkdownToHTMLContent implements Serializable {
	
	private static final long serialVersionUID = -679612296779731234L;

	public int chapterIndex;
	
	private TextChapter(String desc, String text, int chapterIndex, String unformattedContent) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), text);
		boolean ok = attemptSetContent(unformattedContent);
		if(!ok) {
			this.html = "";
			this.unformattedContent = "";
		}
		
		this.description = desc;
		this.chapterIndex = chapterIndex;
	}
	
	/**
	 * Create a TextChapter object to the current StrategyMap map.
	 * Transient does not register the object to the database
	 * @param database
	 * @param map
	 * @param id
	 * @param text
	 * @param chapterIndex
	 * @return
	 */
	public static TextChapter createTransient(String description, String text, int chapterIndex, String unformattedContent) {
		TextChapter tc = new TextChapter(description, text, chapterIndex, unformattedContent);
		return tc;
	}
	
	/**
	 * Create a text chapter object and register it to the database
	 * Add the new TextChapter relation PART OF to the office provided as input
	 * @param database
	 * @param map
	 * @param id
	 * @param text
	 * @param chapterIndex
	 * @param unformattedContent
	 * @param office
	 * @return
	 */
	public static TextChapter create(Database database, String description, String text, int chapterIndex, String unformattedContent, Office office) {
		TextChapter tc = createTransient(description, text, chapterIndex, unformattedContent);
		database.register(tc);
		
		//Can accept null offices, but in that case do not create a relation
		if(office != null) {
			tc.addRelation(Relation.find(database, Relation.PART_OF), office);
		}
		
		return tc;
	}
	
	public void setChapterIndex(int index) {
		this.chapterIndex = index;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	
	/**
	 * Attempt to update the content.
	 * Validation must pass in order for the value to be updated.
	 * Returns if the update succeeded.
	 * @param unformattedContent
	 * @return
	 */
	@Override
	public boolean attemptSetContent(String unformattedContent) {
		if(validateUnformattedContent(unformattedContent)) {
			this.unformattedContent = unformattedContent;
			this.html = UtilsDB.markdownToHTML(unformattedContent, true);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Unformatted cached HTML
	 * @return
	 */
	public String getHTML() {
		return this.html;
	}
	
	public String getUnformattedContent() {
		return this.unformattedContent;
	}
	
	/**
	 * This should check that the unformatted content is OK
	 * @param unformattedContent
	 * @return
	 */
	public static boolean validateUnformattedContent(String unformattedContent) {
		return true;
	}

	public int getChapterIndex() {
		return this.chapterIndex;
	}
	
	/**
	 * Get the ResultAgreementDocument (if any)
	 */
	@Override
	public Base getOwner(Database database) {
		Collection<Base> parents = this.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE));
		if(parents.isEmpty()) {
			return null;
		} else if(parents.size() == 1) {
			return parents.iterator().next();
		}
		else {
			System.err.println("Multiple parents for TextChapter!");
			return null;
		}
	}
	
	/**
	 * Find all TextChapter objects
	 * @param database
	 * @return
	 */
	public static List<TextChapter> enumerate(Database database) {
		ArrayList<TextChapter> result = new ArrayList<TextChapter>();
		for (Base b : database.enumerate()) {
			if (b instanceof TextChapter)
				result.add((TextChapter) b);
		}
		return result;
	}
	
	public static Comparator<TextChapter> chapterIndexComparator = new Comparator<TextChapter>() {

		@Override
		public int compare(TextChapter o1, TextChapter o2) {
			return o1.chapterIndex - o2.chapterIndex;
		}
		
	};
	
	@Override
	public String clientIdentity() {
		return "Kappale";
	}
	
}
