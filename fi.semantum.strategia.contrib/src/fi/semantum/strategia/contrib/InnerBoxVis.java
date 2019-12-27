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

public class InnerBoxVis {

	public String uuid;
	public String id;
	public String text;
	public String color;
	public int tavoite;
	public int realIndex;
	public int columns;
	public boolean hasMeter;
	public boolean hasInfo;
	public boolean canDrill;
	public String leafMeterColor = "#000";
	public String leafMeterDesc = "";
	public String leafMeterPct = "";
	public InnerBoxVis[] innerboxes = new InnerBoxVis[0];
	
	public TagVis[] tags = new TagVis[0];
	public MeterVis[] meters = new MeterVis[0];
	public ExtraVis[] extras = new ExtraVis[0];

	public static final String GREY = "#aaaaaa";
	
	
}
