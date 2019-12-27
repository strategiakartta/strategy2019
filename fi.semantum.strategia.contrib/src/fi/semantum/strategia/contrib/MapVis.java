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

import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.PathVis;

public class MapVis {

	public String uuid = "";
	public int width = 100;
	public String visio = "";
	public String id = "";
	public String elementId = "";
	public String text = "";
	public boolean showNavigation = false;
	public boolean showVision = false;
	public String meterStatus = "";
	public int scrollFocus = 0;
	public boolean commentToolVisible = false;
	
	public PathVis[] path = new PathVis[0];
	public OuterBoxVis[] tavoitteet = new OuterBoxVis[0];
	public Linkki[] parents = new Linkki[0];
	public Linkki[] alikartat = new Linkki[0];
	
	public String[] meterData = new String[0];

	public String tavoiteDescription = "";
	public String painopisteDescription = "";
	public String tavoiteColor = "";
	public String painopisteColor = "";

	public String tavoiteTextColor = "";
	public String painopisteTextColor = "";
	
	public int columns = 2;
	public int amountOfLevels = 3;

	public void fixRows() {
		
		for(int i=0;i<tavoitteet.length;i++) {
			OuterBoxVis t = tavoitteet[i];
			if((i%columns) == 0) t.startNewRow = true;
		}
			
	}
	
}
