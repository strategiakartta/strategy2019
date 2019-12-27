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
import fi.semantum.strategia.UtilsComments;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.MapBase;

public class ActionCreateNewChangeSuggestion extends ActionBase<Base>{

	public ActionCreateNewChangeSuggestion(Main main, Base base) {
		super("Luo uusi Muutosehdotus", main, base);
	}
	
	@Override
	public void run() {
		if(UtilsComments.accountHasCommentToolWriteRights(main)) {
			if(base != null) {
				Base targetBase = base;
				if(base instanceof MapBase) {
					targetBase = MapBase.findTargetedMapBoxElement(main.getDatabase(), (MapBase)base);
				}
				main.getCommentLayout().openCreateNewChangeSuggestionDialog(targetBase);
			}
		} else {
			System.err.println("Account without right to write to comment tool tried to open create new change suggestion dialog!");
		}
	}

}
