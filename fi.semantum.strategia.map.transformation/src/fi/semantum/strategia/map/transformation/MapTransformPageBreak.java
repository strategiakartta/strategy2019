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
package fi.semantum.strategia.map.transformation;

import java.util.Collections;
import java.util.Map;

import org.jsoup.nodes.Attributes;

import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

public class MapTransformPageBreak implements HTMLMapTransform {

	@Override
	public String tag() {
		return "pagebreak";
	}

	@Override
	public Map<String, String> possibleParametersGuide(Database database) {
		return Collections.emptyMap();
	}

	@Override
	public String replace(Database database, StrategyMap map, Attributes attributes) {
		return "<div class=\"page-break\"></div>";
	}
	
}
