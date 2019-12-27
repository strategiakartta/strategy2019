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

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.UnsupportedFilterException;

import fi.semantum.strategia.Utils.TagCombo;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Tag;

class CustomLazyContainer extends IndexedContainer {
	
	private static final long serialVersionUID = -4520139213434183947L;
	
	private Database database;
	private String filterString;
	private TagCombo combo;
	
	final private List<Tag> tags;

	public CustomLazyContainer(Database database, TagCombo combo, List<Tag> tags) {
		this.database = database;
		this.tags = tags;
		this.combo = combo;
		addContainerProperty("id", String.class, "");
		doFilter();
	}
	
	public String getFilterString() {
		return filterString;
	}

	@Override
	public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
		if (filter == null)
		{
			removeAllItems();
			filterString = null;
			return;
		}

		removeAllItems();

		if (filter instanceof SimpleStringFilter)
		{
			String newFilterString = combo.customFilterString;

			if (newFilterString == null)
				return;

			if (newFilterString.equals(filterString))
				return;

			filterString = newFilterString;

			if (filterString.length() < 1)
				return;

			doFilter();
			super.addContainerFilter(filter);
		}
	}

	@SuppressWarnings("unchecked")
	private void doFilter() {
		for(Tag t : tags) {
			Item item = addItem(t.getId(database));
			item.getItemProperty("id").setValue(t.getId(database));
		}
		if(filterString != null) {
			Item item = addItem(filterString);
			if(item != null)
				item.getItemProperty("id").setValue(filterString);
		}
	}

}