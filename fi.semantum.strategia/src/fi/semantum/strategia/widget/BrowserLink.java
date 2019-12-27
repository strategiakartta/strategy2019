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

public class BrowserLink {

	public int source;
	public int target;
	public double weight;
	public double linkDistance;
	public double linkStrength;
	public String color;
	public String dash;
	
	public BrowserLink(String color, String dash, int source, int target, double weight, double linkDistance, double linkStrength) {
		this.color = color;
		this.dash = dash;
		this.source = source;
		this.target = target;
		this.weight = weight;
		this.linkDistance = linkDistance;
		this.linkStrength = linkStrength;
	}
	
}
