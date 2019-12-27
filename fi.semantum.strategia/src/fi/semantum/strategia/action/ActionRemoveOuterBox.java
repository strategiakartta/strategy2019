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
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;

public class ActionRemoveOuterBox extends ActionBase<MapBase> {

	private String desc;
	
	public ActionRemoveOuterBox(String desc, Main main, MapBase base) {
		super("Poista", main, base);
		this.desc = desc;
	}

	@Override
	public void run() {
		
		final StrategyMap map = database.getMap(base);

		OuterBox t = (OuterBox)base;
		if(t.innerboxes.length > 0) {

			VerticalLayout la = new VerticalLayout();
			Label l = new Label("Vain tyhj‰n m‰‰rityksen voi poistaa. Poista ensin jokainen " + desc.toLowerCase() +  " ja yrit‰ sen j‰lkeen uudestaan.");
			l.addStyleName(ValoTheme.LABEL_H3);
			la.addComponent(l);
			Dialogs.errorDialog(main, "Poisto estetty", la);
			return;
			
		}
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);

		Dialogs.confirmDialog(main, "Haluatko varmasti poistaa m‰‰rityksen " + desc + " ?", "Poista", Terminology.CANCEL, new Runnable() {

			@Override
			public void run() {
				
				OuterBox t = (OuterBox)base;
				t.remove(database);
				map.fixRows();
				Updates.updateJS(main, true);
			}
			
		});


	}

}
