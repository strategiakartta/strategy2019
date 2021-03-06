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
import fi.semantum.strategia.widget.ResponsibilitiesDialog;
import fi.semantum.strategy.db.MapBase;

public class ActionOpenResponsibilitiesDialog extends ActionBase<MapBase> {

	public ActionOpenResponsibilitiesDialog(Main main, MapBase p) {
		super("M��rit� vastuut", main, p);
	}

	@Override
	public void run() {
		new ResponsibilitiesDialog(main, base);
	}

}
