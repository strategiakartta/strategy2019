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

import java.util.UUID;

public class ResultAgreementConfigEntry extends Base {

	private static final long serialVersionUID = 2938612842585982434L;
	
	public boolean isShared;
	
	public static ResultAgreementConfigEntry create(Database database, String label, String desc, boolean shared) {
		ResultAgreementConfigEntry p = new ResultAgreementConfigEntry(label, desc, shared);
		database.register(p);
		return p;
	}
	
	private ResultAgreementConfigEntry(String label, String desc, boolean shared) {
		super(UUID.randomUUID().toString(), label, desc);
		setLabel(label);
		setDescription(desc);
		this.isShared = shared;
	}
	
	public void setLabel(String label) {
		this.text = label;
	}
	
	public String getLabel() {
		return this.text;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public boolean isShared() {
		return this.isShared;
	}
	
	public boolean isOffice() {
		return !this.isShared;
	}
	
	@Override
	public Base getOwner(Database database) {
		return null;
	}

	@Override
	public String clientIdentity() {
		return "Tulossopimusasetuksen olio";
	}
	
	@Override
	public String toString() {
		return this.text + " " + this.description + " is shared: " + isShared;
	}
	
}
