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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Pair;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class ActionSetToImplement extends ActionBase<MapBase> {

	public ActionSetToImplement(Main main, MapBase base) {
		super("Aseta toteuttamaan", main, base);
	}

	@Override
	public void run() {
		
		StrategyMap map = database.getMap(base);
		selectGoalType(main, map, new Consumer<String>() {
			
			@Override
			public void accept(String uuid) {
				if(uuid != null) {
					Base painopiste = database.find(uuid);
					base.addRelation(Relation.find(database, Relation.IMPLEMENTS), painopiste);
				}
				Updates.updateJS(main, true);
			}
			
		});


	}

	public static void selectGoalType(final Main main, StrategyMap map, final Consumer<String> callback) {

		final Database database = main.getDatabase();

		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(true);

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

        final Window dialog = Dialogs.makeDialog(main, "Valitse toteutettava aihe", Terminology.CANCEL, content, buttons);

        if(parent_ != null) {
        
	        final ListSelect table = new ListSelect();
	        table.setSizeFull();
	        table.setNullSelectionAllowed(false);
	        table.setMultiSelect(false);
	        table.setCaption("Valitse listasta:");
	        for(Linkki l : map.parents) {
	            StrategyMap parent = database.find(l.uuid);
	            for(OuterBox t : parent.outerBoxes) {
	            	for(InnerBox p : t.innerboxes) {
	            		if(alreadyImplemented.contains(p)) continue;
	            		table.addItem(p.uuid);
	            		table.setItemCaption(p.uuid, p.getId(database) + ": " + p.getText(database));
	            	}
	            }
	        }
	        
	        final Button copy = new Button("M‰‰rit‰ toteuttajaksi", new Button.ClickListener() {
	        	
				private static final long serialVersionUID = 1L;
	
				// inline click-listener
	            public void buttonClick(ClickEvent event) {
	            	Object selected = table.getValue();
	            	if(selected == null) return;
	            	main.removeWindow(dialog);
	            	callback.accept((String)selected);
	            }
	            
	        });
	        
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

        }

	}
	
}
