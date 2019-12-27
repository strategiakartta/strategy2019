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

public class MapRight extends AccessRight implements Serializable {

	private static final long serialVersionUID = 3246509646431571477L;
	
	final public boolean recurse;
	
	/**
	 * Creates a new AccessRight for the base map with write/read properties.
	 * Additionally provides a recurse field for granual access control within a map.
	 * @param map
	 * @param write
	 * @param recurse
	 */
	public MapRight(StrategyMap map, boolean write, boolean recurse) {
		super((Base)map, write); //Place map as Base
		this.recurse = recurse;
	}
	
	public StrategyMap getMap() {
		return (StrategyMap)this.base; //Type case base back to map
	}
	
}
