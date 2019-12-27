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

import java.util.Collection;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class ActionRemoveInnerBox extends ActionBase<MapBase> {

	private Base container;
	
	public ActionRemoveInnerBox(Main main, MapBase base, MapBase container) {
		super("Poista", main, base);
		this.container = container;
	}

	@Override
	public void run() {
		
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<MapBase> implementors = database.getInverse(base, implementsRelation);
		if(!implementors.isEmpty()) {

			VerticalLayout la = new VerticalLayout();
			Label l = new Label("Poista ensin t‰h‰n viittaavat m‰‰ritykset:");
			l.addStyleName(ValoTheme.LABEL_H3);
			la.addComponent(l);
			for(MapBase b : implementors) {
				StrategyMap map = database.getMap(b);
				Label l2 = new Label("&nbsp;&nbsp;&nbsp;&nbsp;" + b.getId(database) + " - " + b.getText(database) + " (" + map.getId(database) + ")");
				l2.setContentMode(ContentMode.HTML);
				la.addComponent(l2);
			}
			Dialogs.errorDialog(main, "M‰‰ritys on k‰ytˆss‰, eik‰ sit‰ voida poistaa", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);

		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + desc + " ?", "Poista", Terminology.CANCEL, new Runnable() {

			@Override
			public void run() {
				
				InnerBox p = (InnerBox)base;
				StrategyMap map = database.getMap(base);
				int refs = 0;
				if(map != null) {
					for(OuterBox t : map.outerBoxes) {
						for(InnerBox p2 : t.innerboxes) {
							if(p2.equals(base)) refs++;
						}
					}
				}
				
				OuterBox t = (OuterBox)container;
				if(refs == 1) p.remove(database);
				else t.removePainopiste(database, p);
				if(map != null)
					map.fixRows();
				Updates.updateJS(main, true);
				
			}
			
		});


	}

}
