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
public class Comment extends Base implements Serializable{

	private static final long serialVersionUID = -28099962563791099L;
	
	private Comment(String text) {
		super(UUID.randomUUID().toString(), UUID.randomUUID().toString(), text);
	}

	/**
	 * Create object
	 * Transient does not register the object to the database
	 * @param database
	 * @param text
	 * @return
	 */
	public static Comment createTransient(Database database, String text) {
		Comment cs = new Comment(text);
		return cs;
	}
	
	/**
	 * Create object and register it to the database
	 * @param database
	 * @param text
	 * @return
	 */
	public static Comment create(Database database, String text, Account account, Office office) {
		Comment c = createTransient(database, text);
		database.register(c);
		UtilsDBComments.claimOwnership(database, c, office, account);

		return c;
	}
	
	@Override
	public Base getOwner(Database database) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Find all Comment objects
	 * @param database
	 * @return
	 */
	public static List<Comment> enumerate(Database database) {
		ArrayList<Comment> result = new ArrayList<Comment>();
		for (Base b : database.enumerate()) {
			if (b instanceof Comment)
				result.add((Comment) b);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Text: " + this.text + ". Desc: " + this.description + ". UUID: " + this.uuid;
	}
	
	@Override
	public String clientIdentity() {
		return "Kommentti";
	}

}
