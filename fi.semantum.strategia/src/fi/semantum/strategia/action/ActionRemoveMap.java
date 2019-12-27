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

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class ActionRemoveMap extends ActionBase<StrategyMap> {

	public ActionRemoveMap(Main main, StrategyMap base) {
		super("Poista", main, base);
	}

	@Override
	public void run() {

		if(base.outerBoxes.length > 0 && base.generators.isEmpty()) {

			VerticalLayout la = new VerticalLayout();
			Label l = new Label("Vain tyhjän kartan voi poistaa. Poista ensin jokainen " + base.outerDescription.toLowerCase() +  " ja yritä sen jälkeen uudestaan.");
			l.addStyleName(ValoTheme.LABEL_H3);
			la.addComponent(l);
			Dialogs.errorDialog(main, "Poisto estetty", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);
		
		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa kartan " + desc + " ?", "Poista", Terminology.CANCEL, new Runnable() {

			@Override
			public void run() {
				
				StrategyMap parent = UtilsDB.removeMapAndRelatedObjects(database, base);
				if(parent != null) {
					main.getUIState().currentMap = parent;
					main.getUIState().customVis = null;
				}
				
				Updates.updateJS(main, true);
			}
			
		});

	}

}
