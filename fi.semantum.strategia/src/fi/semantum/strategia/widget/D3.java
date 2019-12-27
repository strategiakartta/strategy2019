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

import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategy.db.UIState;

@JavaScript(value = {
		"app://VAADIN/js/d3.nocache.js",
		"app://VAADIN/js/map.nocache.js"
	})
public class D3 extends AbstractJavaScriptComponent {

	private static final long serialVersionUID = -7783740979205083211L;

	/**
	 * Defines the functions available for d3.nocache.js and map.nocache.js
	 *
	 */
	public interface D3Listener {
		void navigate(String map);
		void select(double x, double y);
		void selectMeter(int outerBox, int innerBox, int index, String link);
		void drill(int outerBox);
		void columns(int outerBox, int innerBox, boolean increase);
		void navi(double x, double y, int outerBox);
		void navi2(double x, double y, int outerBox, int innerBox);
		void navi3(double x, double y, int outerBox, int innerBox, int innerBoxExtra);
		void navi4(double x, double y, String uuid);
		void editHeader();
		void editVision();
		void editOuterBox(int index);
		void editInnerBox(int outerBox, int innerBox);
		void removeOuterBox(int index);
		void displayInfo(int outerBox, int innerBox);
		void displayMeter(int outerBox, int innerBox);
		void createChangeSuggestion(int outerBox, int innerBox);
		void openRelatedChangeSuggestions(int outerBox, boolean recursive);
	}
	
	private ArrayList<D3Listener> listeners = new ArrayList<D3Listener>();
	
	private static String asString(JsonArray arguments, int index) {
		return arguments.get(index).asString();
	}

	private static Integer asInteger(JsonArray arguments, int index) {
		return Integer.parseInt(arguments.get(index).asString());
	}

	private static Boolean asBoolean(JsonArray arguments, int index) {
		return Boolean.parseBoolean(arguments.get(index).asString());
	}

	private static Double asDouble(JsonArray arguments, int index) {
		return Double.parseDouble(arguments.get(index).asString());
	}
	
	public D3() {
		addFunction("navigate", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners) listener.navigate(arguments.get(0).asString());
			}
			
		});
		addFunction("select", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners) listener.select(asDouble(arguments, 0), asDouble(arguments, 1));
			}
			
		});
		addFunction("drill", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.drill(asInteger(arguments, 0));
			}
			
		});
		addFunction("columns", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.columns(asInteger(arguments, 0), asInteger(arguments, 1), asBoolean(arguments, 2));
			}
			
		});
		addFunction("navi", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi(asDouble(arguments, 0), asDouble(arguments, 1), asInteger(arguments, 2));
			}
			
		});
		addFunction("navi2", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi2(asDouble(arguments, 0), asDouble(arguments, 1), asInteger(arguments, 2), asInteger(arguments, 3));
			}
			
		});
		addFunction("navi3", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi3(asDouble(arguments, 0), asDouble(arguments, 1), asInteger(arguments, 2), asInteger(arguments, 3), asInteger(arguments, 4));
			}
			
		});
		addFunction("navi4", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.navi4(asDouble(arguments, 0), asDouble(arguments, 1), asString(arguments, 2));
			}
			
		});
		addFunction("editHeader", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editHeader();
			}
			
		});
		addFunction("editVision", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editVision();
			}
			
		});
		addFunction("selectMeter", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.selectMeter(asInteger(arguments, 0),asInteger(arguments, 1),asInteger(arguments, 2),arguments.get(3).asString());
			}
			
		});
		addFunction("editOuterBox", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editOuterBox(asInteger(arguments, 0));
			}
			
		});
		addFunction("removeOuterBox", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.removeOuterBox(asInteger(arguments, 0));
			}
			
		});
		addFunction("editInnerBox", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.editInnerBox(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		addFunction("displayInfo", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8172758366004562840L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.displayInfo(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		addFunction("displayMeter", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8685767075489512422L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.displayMeter(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		addFunction("createChangeSuggestion", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8685767075489512422L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.createChangeSuggestion(asInteger(arguments, 0), asInteger(arguments, 1));
			}
			
		});
		
		addFunction("openRelatedChangeSuggestions", new JavaScriptFunction() {
			
			private static final long serialVersionUID = -8685767075489512422L;

			@Override
			public void call(JsonArray arguments) throws JSONException {
				for(D3Listener listener : listeners)
					listener.openRelatedChangeSuggestions(asInteger(arguments, 0), asBoolean(arguments, 1));
			}
			
		});
	}
	
	public void addListener(D3Listener listener) {
		listeners.add(listener);
	}
	
	@Override
	public D3State getState() {
		return (D3State) super.getState();
	}
	
	public void update(final MapVis model, int width, boolean logged, UIState uistate) {
		boolean edit =  false;
		boolean shiftLessMoreColumns = false;
		if(uistate != null) {
			edit = uistate.input;
			shiftLessMoreColumns = uistate.showDetailedMapMeters;
		}
		
		getState().setModel(model);
		if(model != null) {
			model.width = width-30;
			getState().setLogged(logged);
			getState().setEdit(edit);
			getState().setShiftLessMoreColumns(shiftLessMoreColumns);
		}
	}
	
	public MapVis getModel() {
		return getState().model;
	}

}
