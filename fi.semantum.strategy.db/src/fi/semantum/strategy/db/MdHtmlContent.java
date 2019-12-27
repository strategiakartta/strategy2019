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
 * Markdown to HTML content.
 */
public class MdHtmlContent extends MarkdownToHTMLContent {

	private static final long serialVersionUID = -3826772286066789860L;

	public static final String STARTPAGE = "STARTPAGE";
	public static final String TARGET_WEBSITE_LINK = "TARGET_WEBSITE_LINK";
	public static final String FAQ = "FAQ";
	
	protected MdHtmlContent(String ID, String newContent) {
		super(UUID.randomUUID().toString(), ID, ID);
		this.html = "";
		this.unformattedContent = "";
		attemptSetContent(newContent);
	}

	/**
	 * 
	 * @param database
	 * @param ID
	 * @param newContent
	 * @return
	 */
	public static MdHtmlContent createTransient(Database database, String ID, String newContent) {
		MdHtmlContent spc = new MdHtmlContent(ID, newContent);
		return spc;
	}

	public static MdHtmlContent getOrCreateByID(Database database, String ID) {
		MdHtmlContent matching = getPossibleContentByID(database, ID);
		if(matching == null) {
			return create(database, ID, "");
		} else {
			return matching;
		}
	}
	
	/**
	 * 
	 * @param database
	 * @param ID - one of the static IDs in this Class
	 * @param newContent
	 * @return
	 */
	public static MdHtmlContent create(Database database, String ID, String newContent) {
		MdHtmlContent spc = createTransient(database, ID, newContent);
		database.register(spc);
		return spc;
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	/**
	 * Find all OfficialOpinion objects - this includes all objects across all documents, maps, etc.
	 * @param database
	 * @return
	 */
	public static List<MdHtmlContent> enumerate(Database database) {
		ArrayList<MdHtmlContent> result = new ArrayList<MdHtmlContent>();
		for (Base b : database.enumerate()) {
			if (b instanceof MdHtmlContent)
				result.add((MdHtmlContent) b);
		}
		return result;
	}
	
	public static MdHtmlContent getPossibleContentByID(Database database, String ID) {
		List<MdHtmlContent> all = enumerate(database);
		for(MdHtmlContent mhc : all) {
			if(mhc.id.equals(ID)) {
				return mhc;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Text: " + this.text + ". Desc: " + this.description + ". UUID: " + this.uuid;
	}

	@Override
	public String clientIdentity() {
		return "Aloitussivu";
	}

}
