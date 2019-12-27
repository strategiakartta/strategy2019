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
package fi.semantum.strategia;

import java.util.List;

import fi.semantum.strategy.db.MapBase;

public class VisuSpec {

	public String circleStroke;
	public String circleFill;
	public String description;
	public int pathLength;
	public boolean emph;
	
	public VisuSpec(Main main, List<MapBase> path, String circleStroke, String circleFill, String description) {
		MapBase base = path.get(path.size()-1);
		this.pathLength = base.getOwners(main.getDatabase()).size();
		this.circleStroke = circleStroke;
		this.circleFill = circleFill;
		this.description = description;
		if((base == main.getUIState().currentMapItem || base == main.getUIState().currentMapPosition))
			this.emph = true;
		else
			this.emph = false;
	}
	
	public VisuSpec(Main main, List<MapBase> path, String circleStroke, String circleFill) {
		MapBase base = path.get(path.size()-1);
		this.pathLength = base.getOwners(main.getDatabase()).size();
		this.circleStroke = circleStroke;
		this.circleFill = circleFill;
		description = "";
		String type = main.getDatabase().getType(base);
		if(type != null) description = type;
		if((base == main.getUIState().currentMapItem || base == main.getUIState().currentMapPosition))
			this.emph = true;
		else
			this.emph = false;
	}
	
	public static VisuSpec getDefault(Main main, List<MapBase> path, boolean condition) {
		
		MapBase last = path.get(path.size()-1);
		String description = "";
		String type = main.getDatabase().getType(last);
		if(type != null) description = type;

		VisuSpec spec = condition ? new VisuSpec(main, path,"white", "white", description) : null;
		if((last == main.getUIState().currentMapItem || last == main.getUIState().currentMapPosition)) {
			if(spec == null) spec = new VisuSpec(main, path,"white", "white", description);
		}

		return spec;
				
	}
	
}
