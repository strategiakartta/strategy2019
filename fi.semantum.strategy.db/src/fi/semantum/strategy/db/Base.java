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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/*
 * Common base class for all objects in a database
 * 
 */
abstract public class Base implements Serializable, Baseable, Comparable<Base> {
	
	private static final long serialVersionUID = -4482518287842093370L;
	
	// GUID
	final public String uuid;
	// Short name for graphical visualization
	protected String id;
	// Full name for listing
	public String text;
	// A compact description (shown in text areas)
	public String description = "";
	
	public final Long creationTime;
	
	public List<Pair> relations = new ArrayList<Pair>();
	public List<Pair> properties = new ArrayList<Pair>();

	protected Base(String uuid, String id, String text) {
		this.uuid = uuid;
		this.id = id;
		this.text = text;
		this.creationTime = Calendar.getInstance().getTime().getTime(); //First getTime gives Date object, second gives Long since 1970 January 1st
	}
	
	public String showCreationTime(String form) {
		DateFormat dateFormat = new SimpleDateFormat(form);
		String formatted = dateFormat.format(new Date(this.creationTime)); 
		return formatted;
	}
	
	protected void setId(Database database, String id) {
		this.id = id;
	}

	abstract public Base getOwner(Database database);
	
	public void remove(Database database) {
		database.remove(this);
	}
	
	public boolean isRemoved(Database database) {
		return database.find(uuid) != this;
	}
	
	public String getShortText(Database database) {
		String id = getId(database);
		if(!id.isEmpty()) return id;
		String text = getText(database);
		if(text.length() > 30) text = text.substring(0, 30);
		return text;
	}
	
	public String getCaption(Database database) {
		String id = getId(database);
		String caption = getText(database);
		if(caption.isEmpty()) {
			return id;
		} else {
			if(id.isEmpty()) {
				return caption;
			} else {
				return caption + " (" + id + ")";
			}
		}
	}
	
	public String getLabel(Database database) {
		String label = getId(database);
		if(label.isEmpty()) label = getText(database);
		return label;
	}
	
	public String getDescription(Database database) {
		if(description == null || description.isEmpty()) return "";
		return description;
	}

	public static Comparator<Base> creationTimeComparator = new Comparator<Base>() {

		@Override
		public int compare(Base o1, Base o2) {
			Long l =  o1.creationTime - o2.creationTime;
			double d = (double)l;
			return (int)d;
		}
		
	};
	
	
	public String getText(Database database) {
		return text;
	}
	
	public void setText() {
		throw new IllegalStateException();
	}
	
	public boolean modified(final Database database, Account account) {
		if(!UtilsDB.canWrite(database, account, this))
			return false;

		Property changedOn = Property.find(database, Property.CHANGED_ON);
		String acc = account.getId(database);
		changedOn.set(false, database, account, this, UtilsDB.simpleDateFormat.format(new Date()) + " " + acc);
		
		return true;
		
	}

	public String getId(Database database) {
		return id;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Base> Collection<T> getRelatedObjects(Database database, Relation r) {
		Set<T> result = new TreeSet<T>();
		for(Pair p : r.getRelations(this)) {
			Base found = database.find(p.second);
			if(found != null)
				result.add((T)found);
		}
		return result;
	}
	
	public boolean hasRelation(Database database, Relation r, Base b) {
		for(Pair p : r.getRelations(this)) {
			Base found = database.find(p.second);
			if(found.equals(b)) return true;
		}
		return false;
	}
	
	public void addRelation(Relation r, Base b) {
		relations.add(new Pair(r.uuid, b.uuid));
	}

	public void assertRelation(Database database, Relation r, Base b) {
		if(!hasRelation(database, r, b)) addRelation(r, b);
	}

	public void denyRelation(Database database, Relation r) {
		List<Pair> toRemove = new ArrayList<Pair>();
		for(Pair p : relations)
			if(p.first.equals(r.uuid))
				toRemove.add(p);
		relations.removeAll(toRemove);
	}

	public void denyRelation(Database database, Relation r, Base b) {
		relations.remove(new Pair(r.uuid, b.uuid));
	}

	public String searchText(Database database){
		return "";
	}
	
	@Override
	public int compareTo(Base o) {
		return uuid.compareTo(o.uuid);
	}
	
	public boolean migrate(Database database) {
		Account account = database.getDefaultAdminAccount();
		
		boolean result = false;

		if(description == null) {
			description = "";
			result = true;
		}
		
		Property aika = Property.find(database, Property.AIKAVALI);
		aika.setEnumeration(Collections.<String>emptyList());
		String validity = aika.getPropertyValue(this);
		if(validity == null || "Kaikki".equals(validity)) {
			aika.set(false, database, account, this, Property.AIKAVALI_KAIKKI);
			result = true;
		}

		return result;
		
	}
	
	@Override
	public Base getBase() {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		Base other = (Base) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public Collection<Base> getImplementedSet(Database database) {
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		return getRelatedObjects(database, implementsRelation);
	}
	
	public Base getImplemented(Database database) {

		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> bases = getRelatedObjects(database, implementsRelation);
		if(bases.size() == 1) return bases.iterator().next();
		if(bases.size() > 1) throw new IllegalStateException("Implements multiple!");
		return null;
		
	}

	public Base getPossibleImplemented(Database database) {
		try {
			return getImplemented(database);
		} catch (IllegalStateException e) {
			return null;
		}
	}
	
	/**
	 * Show the identify of this Object as the client understands it.
	 * E.g. rather than saying class.toString, we show this value.
	 * @return
	 */
	abstract public String clientIdentity();
	
	public String getDebugText(Database database) {
		StringBuilder result = new StringBuilder();
		result.append(toString());
		result.append("\n");
		result.append("-uuid: " + uuid);
		result.append("\n");
		result.append("-id: " + getId(database));
		result.append("\n");
		result.append("-text: " + getText(database));
		result.append("\n");
		return result.toString();
	}
	
}
