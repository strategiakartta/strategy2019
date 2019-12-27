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
import fi.semantum.strategy.db.Baseable;
import fi.semantum.strategy.db.Database;

public abstract class ActionBase<T extends Baseable> extends Action {

	public static String HAKU = "Siirry hakun‰kym‰‰n";
	public static String NAKYMA = "Muokkaa n‰kym‰‰";
	
	protected Main main;
	protected Database database;
	protected T base;
	
	public ActionBase(String caption, Main main, T base) {
		this(caption, null, main, base);
	}

	@SuppressWarnings("unchecked")
	public ActionBase(String caption, String category, Main main, T base) {
		super(category, caption);
		this.main = main;
		this.database = main.getDatabase();
		this.base = (T)base.getBase();
	}

}
