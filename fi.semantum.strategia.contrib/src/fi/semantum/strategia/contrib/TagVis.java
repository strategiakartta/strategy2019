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

import fi.semantum.strategy.db.Tag;

public class TagVis {

	public String text;
	public String color;
	public double fillRatio;

	public TagVis(Tag tag, double coverage) {
		int pct = (int)(100.0*coverage);
		text = tag.text;
		if(pct < 100) text += " " + pct;
		color = tag.color;
		fillRatio = coverage;
	}

}
