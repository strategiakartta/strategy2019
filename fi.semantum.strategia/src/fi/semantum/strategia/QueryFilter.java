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

import com.vaadin.data.Item;
import com.vaadin.data.Container.Filter;

public class QueryFilter implements Filter {

	private static final long serialVersionUID = 972098505930347322L;
	
	final String filterString;
    final boolean ignoreCase;
    final boolean onlyMatchPrefix;

    public QueryFilter(String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        this.filterString = ignoreCase ? filterString.toLowerCase()
                : filterString;
        this.ignoreCase = ignoreCase;
        this.onlyMatchPrefix = onlyMatchPrefix;
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) {
    	
    	for(Object id : item.getItemPropertyIds()) {

	        final com.vaadin.data.Property<?> p = item.getItemProperty(id);
	        if (p == null) {
	            continue;
	        }
	        Object propertyValue = p.getValue();
	        if (propertyValue == null) {
	            continue;
	        }
	        String value = ignoreCase ? propertyValue.toString()
	        		.toLowerCase() : propertyValue.toString();
	        		if (value.contains(filterString)) {
	        			return true;
	        		}
    	}
    	
    	return false;
    	
    }

    @Override
    public boolean appliesToProperty(Object propertyId) {
    	return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        // Only ones of the objects of the same class can be equal
        if (!(obj instanceof QueryFilter)) {
            return false;
        }
        final QueryFilter o = (QueryFilter) obj;

        // Checks the properties one by one
        if (filterString != o.filterString && o.filterString != null
                && !o.filterString.equals(filterString)) {
            return false;
        }
        if (ignoreCase != o.ignoreCase) {
            return false;
        }
        if (onlyMatchPrefix != o.onlyMatchPrefix) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (filterString != null ? filterString.hashCode() : 0);
    }

    /**
     * Returns the property identifier to which this filter applies.
     * 
     * @return property id
     */
    public Object getPropertyId() {
        return null;
    }

    /**
     * Returns the filter string.
     * 
     * Note: this method is intended only for implementations of lazy string
     * filters and may change in the future.
     * 
     * @return filter string given to the constructor
     */
    public String getFilterString() {
        return filterString;
    }

    /**
     * Returns whether the filter is case-insensitive or case-sensitive.
     * 
     * Note: this method is intended only for implementations of lazy string
     * filters and may change in the future.
     * 
     * @return true if performing case-insensitive filtering, false for
     *         case-sensitive
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Returns true if the filter only applies to the beginning of the value
     * string, false for any location in the value.
     * 
     * Note: this method is intended only for implementations of lazy string
     * filters and may change in the future.
     * 
     * @return true if checking for matches at the beginning of the value only,
     *         false if matching any part of value
     */
    public boolean isOnlyMatchPrefix() {
        return onlyMatchPrefix;
    }
}