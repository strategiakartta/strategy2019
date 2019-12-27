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
package fi.semantum.strategia.contrib;

import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.UIState;

public interface MapVisFactory {

	public static final int PRIORITY_MAP_NAVIGATION = 0;
	public static final int PRORITY_ALL = Integer.MIN_VALUE;
	
	String getDescription();
	int getPriority();
	boolean accept(Database database, MapBase base);
	MapVis create(VisRegistry registry, Database database, Account account, UIState uistate, MapBase base);

}
