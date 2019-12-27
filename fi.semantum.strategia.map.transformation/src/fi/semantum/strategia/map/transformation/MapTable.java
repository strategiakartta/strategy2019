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

import java.util.ArrayList;

public class MapTable {
	int columns = -1;
	ArrayList<Double> widths = new ArrayList<Double>();
	ArrayList<String> heights = new ArrayList<String>();
	String widthUnit = "mm";
	String height = "100%";
	ArrayList<Object> cells = new ArrayList<Object>();
}
