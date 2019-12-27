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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.BrowserNodeState;

public class BrowserNode implements Serializable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((circleFill == null) ? 0 : circleFill.hashCode());
		result = prime * result
				+ ((circleStroke == null) ? 0 : circleStroke.hashCode());
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + h;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + w;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BrowserNode other = (BrowserNode) obj;
		if (circleFill == null) {
			if (other.circleFill != null)
				return false;
		} else if (!circleFill.equals(other.circleFill))
			return false;
		if (circleStroke == null) {
			if (other.circleStroke != null)
				return false;
		} else if (!circleStroke.equals(other.circleStroke))
			return false;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (h != other.h)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		if (w != other.w)
			return false;
		return true;
	}

	private static final long serialVersionUID = 3272622564149705562L;
	
	public String description;
	public String name;
	public String uuid;
	public String color;
	public String circleStroke;
	public String circleFill;
	public double charge;
	public boolean emph;
	public boolean fixed;
	public boolean required = false;
	public int nameSize;
	public int descriptionSize;
	public int w;
	public int h;
	public double x;
	public double y;
	
	public transient Set<Base> children = new HashSet<Base>();
	
	public BrowserNode() {
		
	}
	
	public BrowserNode(String name, String uuid, BrowserNodeState state, boolean fixed, String color, int nameSize, int descriptionSize, int x, int y, int w, int h, double charge, VisuSpec spec) {
		this.name = name.trim();
		this.uuid = uuid;
		this.fixed = fixed;
		this.color = color;
		this.nameSize = nameSize;
		this.descriptionSize = descriptionSize;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.charge = charge;
		if(spec != null) {
			this.circleStroke = spec.circleStroke;
			this.circleFill = spec.circleFill;
			this.description = spec.description;
			this.emph = spec.emph;
		} else {
			this.circleStroke = "white";
			this.circleFill = "white";
			this.description = "";
			this.emph = false;
		}
		if(state != null) {
			this.x = state.x;
			this.y = state.y;
			this.fixed = state.fixed;
		}
	}
	
	public void addChild(Base base) {
		children.add(base);
	}
	
}
