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
package fi.semantum.strategia.widget;

import com.vaadin.shared.ui.JavaScriptComponentState;

public class BrowserState extends JavaScriptComponentState {

	private static final long serialVersionUID = -8517992190611794207L;

	public int w;
	public int h;
	public BrowserNode[] nodes;
	public BrowserLink[] links;
	public boolean setPositions;
	
}
