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

import java.io.File;

public class Files {

	private static File baseDirectory = null;

	public static File getDatabaseDirectory(String databaseId) {
		return new File(baseDirectory(), databaseId);
	}
	
	public static File baseDirectory() {
		String baseDirectoryPath = DBConfiguration.getBaseDirectoryPath();
		baseDirectory = new File(baseDirectoryPath);
		return (baseDirectory);
	}
	
	
}
