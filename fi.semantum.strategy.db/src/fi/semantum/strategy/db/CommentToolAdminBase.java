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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author Miro Eklund
 *
 */
public class CommentToolAdminBase  extends Base implements Serializable{

	private static final long serialVersionUID = -4852437917973536449L;

	private CommentToolAdminBase() {
		super(UUID.randomUUID().toString(), "CommentToolAdminBase", "CommentToolAdminBase");
	}

	/**
	 * Create object and register it to the database
	 * @param database
	 * @param text
	 * @return
	 */
	public static CommentToolAdminBase fetchOrCreateSingleton(Database database) {
		List<CommentToolAdminBase> existing = enumerate(database);
		if(existing.size() == 0) {
			CommentToolAdminBase ctb = new CommentToolAdminBase();
			database.register(ctb);
			return ctb;
		} else {
			return existing.get(0);
		}
	}

	@Override
	public Base getOwner(Database database) {
		return null;
	}

	private static List<CommentToolAdminBase> enumerate(Database database) {
		ArrayList<CommentToolAdminBase> result = new ArrayList<CommentToolAdminBase>();
		for (Base b : database.enumerate()) {
			if (b instanceof CommentToolAdminBase)
				result.add((CommentToolAdminBase) b);
		}
		return result;
	}

	public static boolean canWrite(Database database, Account account) {
		return UtilsDB.canWrite(database, account, fetchOrCreateSingleton(database));
	}
	
	public static boolean canRead(Database database, Account account) {
		return UtilsDB.canRead(database, account, fetchOrCreateSingleton(database));
	}
	
	@Override
	public String clientIdentity() {
		return "Kommenttityökalun ylläpitäjä";
	}
	
}