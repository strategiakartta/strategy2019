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

import java.util.ArrayList;
import java.util.List;

public class DocumentLayout {

	public int defaultMaxColumn;
	public List<String> widths;

	public DocumentLayout() {
		this.defaultMaxColumn = 8;
		this.widths = new ArrayList<String>();
		widths.add("30%");
		widths.add("8%");
		widths.add("10%");
		widths.add("10%");
		widths.add("10%");
		widths.add("10%");
		widths.add("10%");
		widths.add("10%");
	}

}
