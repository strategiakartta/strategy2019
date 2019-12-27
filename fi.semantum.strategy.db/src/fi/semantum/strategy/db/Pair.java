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

/**
 * A generic Pair (2-tuple) structure for containing two object instances of
 * chosen types.
 * 
 * @param <String> type of first element
 * @param <String> type of second element
 */
public final class Pair implements Comparable<Pair>, Serializable {
	
	private static final long serialVersionUID = -8575695603002574594L;
	
	public final String first;
    public final String second;
    public final int hash;

    public static Pair make(String first, String second) {
        return new Pair(first, second);
    }

    public Pair(String first, String second) {
        this.first = first;
        this.second = second;
        this.hash = makeHash();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj.getClass().equals(this.getClass())))
            return false;
        Pair other = (Pair) obj;
        if (other.first != first && (other.first == null || !other.first.equals(first)))
            return false;
        if (other.second != second && (other.second == null || !other.second.equals(second)))
            return false;
        return true;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
    @Override
    public String toString() {
        return "<"+first+", "+second+">";
    }
    
    private int makeHash() {
        return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode())*31;
    }

	@Override
	public int compareTo(Pair arg0) {
		return hash - arg0.hash;
	}
	
}
