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

public class PathVis {

	public String uuid;
	public String text;
	public String textColor;
	public String backgroundColor;

	public PathVis(String uuid, String text, String textColor, String backgroundColor) {

		this.uuid = uuid;
		this.text = text;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		
	}
	
}
