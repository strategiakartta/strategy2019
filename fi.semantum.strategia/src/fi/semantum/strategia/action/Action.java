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

public abstract class Action implements Runnable {
	
	protected String caption;
	protected String category;
	
	public Action(String caption) {
		this(null, caption);
	}
	
	public Action(String category, String caption) {
		this.category = category;
		this.caption = caption;
	}

	public boolean accept() {
		return true;
	}

	public String getCaption() {
		return caption;
	}
	
	public boolean isNavigation() {
		return false;
	}
	
}