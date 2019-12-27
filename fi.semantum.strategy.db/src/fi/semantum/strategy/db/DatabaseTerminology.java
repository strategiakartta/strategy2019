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

import java.io.Serializable;

public class DatabaseTerminology implements Serializable {
	
	private static final long serialVersionUID = 3526447500595249632L;
	
	public final String FOCUS_DESCRIPTION;
	
	public DatabaseTerminology(String focus_description) {
		this.FOCUS_DESCRIPTION = focus_description;
	}
	
	
}
