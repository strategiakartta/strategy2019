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
import fi.semantum.strategia.Utils;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Terminology;

public class ActionAddImplementationMap extends ActionBase<OuterBox> {

	public ActionAddImplementationMap(Main main, OuterBox goal) {
		super(Terminology.ADD_FULFILLING_LOWER_LEVEL_MAP_LABEL, main, goal);
	}

	@Override
	public void run() {

		Utils.addImplementationMap(main, base);

	}

}
