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
import fi.semantum.strategia.widget.TableInputDialog;
import fi.semantum.strategy.db.MapBase;

public class ActionInputMapElementsAsTable extends ActionBase<MapBase> {

	public ActionInputMapElementsAsTable(Main main, MapBase p) {
		super("Syötä tiedot taulukkona", main, p);
	}

	@Override
	public void run() {

		new TableInputDialog(main, base);

	}

}
