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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Pair;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;
import fi.semantum.strategy.db.Terminology;

public class ActionAddOuterBox extends ActionBase<StrategyMap> {
	
	public ActionAddOuterBox(Main main, StrategyMap base) {
		super(Terminology.ADD + " " + base.outerDescription.toLowerCase(), main, base);
	}

	@Override
	public void run() {

		selectGoalType(main, base, new Consumer<String>() {
			
			OuterBox create(String uuid) {
				if(uuid != null) {
					Base copy = database.find(uuid);
					return Utils.createOuterBoxCopy(main, main.uiState.currentMap, copy);
				} else {
					return OuterBox.create(database, main.uiState.currentMap, "Oma " + base.outerDescription.toLowerCase());
				}
			}
			
			@Override
			public void accept(String uuid) {
				OuterBox ob = create(uuid);
				try {
					Utils.ensureImplementationMap(main, ob);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Updates.updateJS(main, true);
			}
			
		});
		
	}

	public static void selectGoalType(final Main main, StrategyMap map, final Consumer<String> callback) {

		final Database database = main.getDatabase();

		CharacterInfo ci = map.getCharacterInfo(database);

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		content.setMargin(true);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(true);

		Button ok = new Button(Terminology.ADD_YOUR_OWN + " " + map.outerDescription.toLowerCase());
		ok.addStyleName(ValoTheme.BUTTON_SMALL);

		if(ci.allowsOwnOuterBox) {

			hl1.addComponent(ok);
			hl1.setExpandRatio(ok, 0.0f);

			Label uusiOma = new Label("Omat m‰‰ritykset eiv‰t perustu ylempien tasojen m‰‰rityksiin");
			uusiOma.addStyleName(ValoTheme.LABEL_SMALL);

			hl1.addComponent(uusiOma);
			hl1.setComponentAlignment(uusiOma, Alignment.MIDDLE_LEFT);
			hl1.setExpandRatio(uusiOma, 1.0f);

		}

		content.addComponent(hl1);
		content.setExpandRatio(hl1, 0.0f);

		Set<Base> alreadyImplemented = new HashSet<Base>();
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		for(OuterBox t : map.outerBoxes) {
			Pair p = implementsRelation.getPossibleRelation(t);
			if(p != null) {
				Base targetPP = database.find(p.second);
				alreadyImplemented.add(targetPP);
			}
		}

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		StrategyMap parent_ = map.getPossibleParent(database);

		final Window dialog = Dialogs.makeDialog(main, "M‰‰rit‰ uusi " + map.outerDescription.toLowerCase(), Terminology.CANCEL, content, buttons);

		if(parent_ != null) {

			Base implementedGoal = map.getImplemented(database);

			final ListSelect table = new ListSelect();
			table.setSizeFull();
			table.setNullSelectionAllowed(false);
			table.setMultiSelect(false);
			table.setCaption(Terminology.ADD + " " + map.outerDescription.toLowerCase() + " perustuen ylemm‰n tason (" + parent_.getId(database) + ") m‰‰ritykseen. Valitse m‰‰ritys listasta:");
			for(Linkki l : map.parents) {
				StrategyMap parent = database.find(l.uuid);
				for(OuterBox t : parent.outerBoxes) {

					if(implementedGoal != null) {
						if(!implementedGoal.equals(t))
							continue;
					}

					for(InnerBox p : t.innerboxes) {
						if(alreadyImplemented.contains(p)) continue;
						table.addItem(p.uuid);
						String desc = p.getText(database);
						String id = p.getId(database);
						if(desc.isEmpty()) desc = id;
						else if(!id.isEmpty() && !id.equals(desc)) desc = id + " : " + desc;
						table.setItemCaption(p.uuid, desc);
					}
				}
			}

			final Button copy = new Button(Terminology.ADD_FULFILLING_LABEL + " " + map.outerDescription.toLowerCase());
			copy.addStyleName(ValoTheme.BUTTON_SMALL);

			ValueChangeListener l = new ValueChangeListener() {

				private static final long serialVersionUID = 192004471077387400L;

				@Override
				public void valueChange(ValueChangeEvent event) {

					Object selection = table.getValue();
					if(selection == null) {
						copy.setEnabled(false);
					} else {
						copy.setEnabled(true);
					}

				}

			};

			l.valueChange(null);

			table.addValueChangeListener(l);

			content.addComponent(table);
			content.setExpandRatio(table, 1.0f);

			copy.setEnabled(false);

			buttons.addComponent(copy);

			copy.addClickListener(new Button.ClickListener() {

				private static final long serialVersionUID = 7193540167093776902L;

				// inline click-listener
				public void buttonClick(ClickEvent event) {
					Object selected = table.getValue();
					if(selected == null) return;
					main.removeWindow(dialog);
					callback.accept((String)selected);
				}

			});

		}

		ok.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 6054297133724400131L;

			// inline click-listener
			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
				callback.accept(null);
			}

		});

	}

}
