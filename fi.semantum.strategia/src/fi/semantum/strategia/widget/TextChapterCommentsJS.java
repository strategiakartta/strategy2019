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

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;
import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.TextChapter;

@JavaScript(value = {
		"app://VAADIN/js/textchapter.nocache.js"
})
public class TextChapterCommentsJS extends AbstractJavaScriptComponent{

	private static final long serialVersionUID = -6252495656888652098L;

	private Main main;
	private TextChapter content;
	
	private static Integer asInteger(JsonArray arguments, int index) {
		return Integer.parseInt(arguments.get(index).asString());
	}
	
	@Override
	public TextChapterCommentsJSState getState() {
		return (TextChapterCommentsJSState) super.getState();
	}
	
	public TextChapterCommentsJS(Main main_, TextChapter content_) {
		this.main = main_;
		this.content = content_;
		this.getState().updateFromServer(this.main, this.content); //Initialize state
		
		addFunction("clickEvent", new JavaScriptFunction() {

			private static final long serialVersionUID = 6054928350336922997L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				String innerHTML = arguments.get(0).asString();
				int index = asInteger(arguments, 1);
				if(main.getUIState().commentLayoutOpen && main.getCommentLayout() != null) {
					//System.out.println(innerHTML);
					Set<Base> targets = new HashSet<>();
					targets.add(content);
					main.getCommentLayout().openRelatedChangeSuggestions(targets, innerHTML);
				} else {
					System.err.println("CommentLayout is null - Cannot filter openRelatedChangeSuggestions!");
				}
			}

		});
	}

}
