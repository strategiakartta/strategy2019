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
package fi.semantum.strategia.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;
import elemental.json.impl.JreJsonObject;
import fi.semantum.strategy.db.BrowserNodeState;

@JavaScript(value = {
		"app://VAADIN/js/d3.nocache.js",
		"app://VAADIN/js/map.nocache.js"
	})
public class Browser extends AbstractJavaScriptComponent {

	private static final long serialVersionUID = 400460883943268636L;

	/**
	 * Defines the functions available to the map.nocache.js and d3.nocache.js files
	 *
	 */
	public interface BrowserListener {
		void select(double x, double y, String uuid);
		void save(String name, Map<String,BrowserNodeState> states);
	}
	
	private ArrayList<BrowserListener> listeners = new ArrayList<BrowserListener>();
	
	public Browser(BrowserNode[] nodes, BrowserLink[] links, int width, int height) {
		/**
		 * This function is called from map.nocache.js as:
		 * rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid);
		 * where arguments.get(0).asNumber() is the pageX, arguments.get(1).asNumber() is pageY and arguments.get(2).asString() is the uuid
		 */
		addFunction("select", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8281255257252572037L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(BrowserListener listener : listeners) listener.select(arguments.get(0).asNumber(), arguments.get(1).asNumber(), arguments.get(2).asString());
			}
			
		});
		/**
		 * This save function is called from map.nocache.js as:
		 * singletonBrowserRootFn.save(name, nodeMap);
		 * where arguments.getString(0) is the name and the nodeMap object is arguments.get(1).
		 */
		addFunction("save", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -237728055512115104L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				JreJsonObject o = arguments.get(1);
				Map<String, BrowserNodeState> states = new HashMap<String, BrowserNodeState>();
				for(String uuid : o.keys()) {
					JreJsonObject o2 = (JreJsonObject)o.get(uuid);
					BrowserNodeState ns = new BrowserNodeState();
					ns.x = o2.get("x").asNumber();
					ns.y = o2.get("y").asNumber();
					ns.fixed = o2.get("fixed").asBoolean();
					states.put(uuid, ns);
				}
				for(BrowserListener listener : listeners) listener.save(arguments.getString(0), states);
			}
			
		});
		update(nodes, links, width, height, true);
	}
	
	public void addListener(BrowserListener listener) {
		listeners.add(listener);
	}

	@Override
	public BrowserState getState() {
		return (BrowserState) super.getState();
	}
	
	public void update(BrowserNode[] nodes, BrowserLink[] links, int width, int height, boolean setPositions) {
		BrowserState state = getState();
		state.w = width;
		state.h = height;
		state.nodes = nodes;
		state.links = links;
		state.setPositions = setPositions;
	}

}
