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
package fi.semantum.strategia.widget;

import java.util.List;

import com.vaadin.shared.ui.JavaScriptComponentState;

import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UtilsDB;
 
public class TextChapterCommentsJSState extends JavaScriptComponentState {

	private static final long serialVersionUID = 2549605994870976138L;

	public String[] clickableInnerHTMLs = new String[0];
	
	public TextChapterCommentsJSState() {
		super();
	}
	
	public void updateFromServer(Main main, TextChapter chapter) {
		if(chapter != null) {
			Database database = main.getDatabase();
			Account account = main.getAccountDefault();
			List<String> clickableInnerHTMLList = UtilsDB.subChapterHtmlsTargetedByChangeSuggestions(database, account, chapter);
			clickableInnerHTMLs = clickableInnerHTMLList.toArray(new String[clickableInnerHTMLList.size()]);
		} else {
			clickableInnerHTMLs = new String[0];
		}
	}
	
}