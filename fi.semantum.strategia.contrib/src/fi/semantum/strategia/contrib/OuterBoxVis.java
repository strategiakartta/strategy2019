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
package fi.semantum.strategia.contrib;

/**
 * Cannot refactor this classes fields without changing JavaScript too
 * @author Antti Villberg
 *
 */
public class OuterBoxVis {

	public String uuid = "";
	public String id = "";
	public String parentText = "";
	public String text = "";
	public String color = "";
	public int columns = 1;
	public int realIndex = 0;
	public boolean startNewRow = false;
	public double xOffset = 0.0; // In %
	public double yOffset = 0.0; // In pixels
	public boolean copy = false;
	public boolean stripes = false;
	public boolean drill = false;
	public InnerBoxVis[] innerboxes = new InnerBoxVis[0];
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];
	public boolean hasChangeSuggestions = false;
	public boolean childElementsHaveChangeSuggestions = false;
	public String parentImplements = "";
	
}
