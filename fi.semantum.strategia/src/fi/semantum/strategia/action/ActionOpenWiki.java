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
import fi.semantum.strategia.Wiki;
import fi.semantum.strategy.db.MapBase;

public class ActionOpenWiki extends ActionBase<MapBase> {

	public ActionOpenWiki(Main main, MapBase base) {
		super("Tausta-asiakirja", main, base);
	}

	@Override
	public void run() {

		Wiki.openWiki(main, base);

	}

}
