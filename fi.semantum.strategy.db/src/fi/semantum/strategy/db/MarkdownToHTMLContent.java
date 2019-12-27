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

/**
 * Abstract class for all markdown to HTML content type objects
 */
public abstract class MarkdownToHTMLContent extends Base {

	private static final long serialVersionUID = 2837266762042326672L;

	protected MarkdownToHTMLContent(String uuid, String id, String text) {
		super(uuid, id, text);
	}

	public String unformattedContent;
	public String html;
	
	public String getHTML() {
		return this.html;
	}
	
	public String getUnformattedContent() {
		return this.unformattedContent;
	}
	
	public boolean attemptSetContent(String unformattedContent) {
		this.unformattedContent = unformattedContent; //Content as unformatted
		this.html = UtilsDB.markdownToHTML(unformattedContent, false); //html conversion
		return true;
	}
}
