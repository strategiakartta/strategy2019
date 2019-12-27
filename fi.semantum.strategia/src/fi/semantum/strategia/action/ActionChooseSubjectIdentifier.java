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
package fi.semantum.strategia.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.DialogCallback;
import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UIState;

public class ActionChooseSubjectIdentifier extends ActionBase<StrategyMap> {

	public ActionChooseSubjectIdentifier(Main main, String caption, StrategyMap base) {
		super(caption, NAKYMA, main, base);
	}

	@Override
	public void run() {
		selectMonitorTagsDialog(main, base, new DialogCallback<Collection<Tag>>() {

			@Override
			public void finished(Collection<Tag> result) {

				UIState s = main.duplicateUIState();
				s.showTags = true;
				s.shownTags = new ArrayList<Tag>(result);
				main.setFragment(s, true);

				Updates.update(main, true);

			}

			@Override
			public void canceled() {
			}

		});	
	}

	void selectMonitorTagsDialog(final Main main, StrategyMap map, final DialogCallback<Collection<Tag>> callback) {

		final Database database = main.getDatabase();
		
		HashSet<Tag> monitorTagsSet = new HashSet<Tag>();
		for(MapBase b : map.getOwners(database)) {
			monitorTagsSet.addAll(b.getMonitorTags(database));
		}
		
		ArrayList<Tag> monitorTags = new ArrayList<Tag>(monitorTagsSet);
		Collections.sort(monitorTags, MapBase.tagComparator);
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

        final OptionGroup table = new OptionGroup();
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setWidth("100%");
        
        for(Tag t : monitorTags) {
        	table.addItem(t);
        	table.setItemCaption(t, t.getId(database));
        }
        
        content.addComponent(table);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        
		Button ok = new Button("Valitse");
        
        if(monitorTags.isEmpty()) {
        	table.setVisible(false);
        	Label emptyLabel = new Label();
        	emptyLabel.setContentMode(ContentMode.HTML);
        	emptyLabel.setValue("<div>Yht‰‰n aihetunnistetta ei ole m‰‰ritetty.</div></br>"
        			+ "<div>Yll‰pit‰j‰ voi luoda uusia aihem‰‰rityksi‰ kartan muokkaustilassa.</div></br>"
        			+ "<div>(Valitse \"Ominaisuudet\" -> \"Aihetunnisteet\").</br>"
        			+ "Sen j‰lkeen m‰‰rit‰ \"Seurataan toteutuksessa\" valituille aihetunnisteille.</div>");
        	content.addComponent(emptyLabel);
        	ok.setEnabled(false);
        }
        
		ok.setStyleName(ValoTheme.BUTTON_SMALL);
        buttons.addComponent(ok);
        buttons.setExpandRatio(ok, 0.0f);

        final Window dialog = Dialogs.makeDialog(main, "300px", "600px", "Valitse n‰ytett‰v‰t", Terminology.CANCEL, content, buttons);
        
        ok.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1657687721482107951L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
            	main.removeWindow(dialog);
            	callback.finished((Collection<Tag>)table.getValue());
            }
            
        });

	}
	
}
