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
package fi.semantum.strategy.db;

public class ImplementationMeter extends Base {
	
	private static final long serialVersionUID = -3261111895103525818L;

	protected ImplementationMeter() {
		super("", "Toteutuksen tila", "");
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}
	
	@Override
	public String clientIdentity() {
		return "Mittari implementaatio";
	}
}
