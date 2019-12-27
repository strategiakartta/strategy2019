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
package fi.semantum.strategia;

import fi.semantum.strategy.db.MapBase;

public class LinkSpec {

	public MapBase source;
	public MapBase target;
	public String stroke;
	public String strokeWidth;
	
	public LinkSpec(MapBase source, MapBase target, String stroke, String strokeWidth) {
		this.source = source;
		this.target = target;
		this.stroke = stroke;
		this.strokeWidth = strokeWidth;
	}
	
}
