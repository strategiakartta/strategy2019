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

import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.widget.StandardVisRegistry;
import fi.semantum.strategy.db.MapBase;

public class ActionOpenCustomMapVis extends ActionBase<MapBase> {
	
	public ActionOpenCustomMapVis(Main main, MapBase base) {
		super(StandardVisRegistry.getInstance().getBestMapVisDescription(main.getDatabase(), main.getAccountDefault(), main.getUIState(), base), main, base);
	}

	@Override
	public void run() {

		main.getUIState().customVis = base;
		Updates.updateJS(main, false);
		main.switchToMapAndDocumentsView();
		
	}
	
	public boolean isNavigation() {
		return true;
	}

}
