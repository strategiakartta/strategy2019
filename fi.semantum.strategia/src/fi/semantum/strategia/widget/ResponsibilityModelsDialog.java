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
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.ObjectType;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.ResponsibilityModel;
import fi.semantum.strategy.db.Terminology;

public class ResponsibilityModelsDialog {

	private static String FIELD = "Kent‰t";
	
	private Main main;
	private ObjectType mapType;
	private ComboBox models;
	private TextField nameField;
	private Table table;
	private boolean isListening = false;

	private ResponsibilityModel currentSelection;
	private List<String> currentFields;
	
	private ValueChangeListener comboListener;
	private ValueChangeListener nameListener;
	private ValueChangeListener tableListener;

	ResponsibilityModelsDialog(final Main main, ObjectType mapType) {

		this.main = main;
		this.mapType = mapType;
		
		initialize();
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);

		HorizontalLayout buttons1 = new HorizontalLayout();
		buttons1.setSpacing(true);
		buttons1.setMargin(false);

		HorizontalLayout buttons2 = new HorizontalLayout();
		buttons2.setSpacing(true);
		buttons2.setMargin(false);

		models = new ComboBox();
		models.setWidth("100%");
		models.addStyleName(ValoTheme.COMBOBOX_SMALL);
		models.setCaption("Valitse vastuumalli:");
		
		nameField = new TextField();
		nameField.setWidth("100%");
		nameField.setCaption("Mallin nimi");
		
		comboListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1936269579541506442L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(!isListening) return;
				currentSelection = (ResponsibilityModel)models.getValue();
				currentFields = currentSelection.getFields();
				table.setEditable(false);
				refresh();
			}
			
		};
		
		models.addValueChangeListener(comboListener);
		
		nameListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1936269579541506442L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(!isListening) return;
				Database database = main.getDatabase();
				Account account = main.getAccountDefault();
				String fieldValue = nameField.getValue() == null ? "" : nameField.getValue();
				if(currentSelection != null) {
					currentSelection.modifyText(database, account, fieldValue);
					Updates.update(main, true);
					refresh();
				} else {
					System.err.println("currentSelection was null in nameListener ValueChangeListener");
				}
			}
			
		}; 
		
		nameField.addValueChangeListener(nameListener);
		
		content.addComponent(models);
		content.setExpandRatio(models, 0.0f);

		content.addComponent(nameField);
		content.setExpandRatio(nameField, 0.0f);

		table = new Table();
		table.setSizeFull();
		table.setNullSelectionAllowed(false);
		table.setMultiSelect(false);
		table.setEditable(false);
		table.setSelectable(true);
		
		table.setTableFieldFactory(new TableFieldFactory () {
			
			private static final long serialVersionUID = 8565113301704552778L;

			public Field createField(Container container, Object itemId,
		            Object propertyId, Component uiContext) {
		        TextField field = new TextField((String) propertyId);
		        field.setWidth("100%");
		        field.addValueChangeListener(tableListener);
		        return field;
		    }
			
		});
		
		tableListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -5314205090826130399L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(!isListening) return;
				tableValueChange();
			}
			
		};
		
		table.addContainerProperty(FIELD, String.class, null);

		content.addComponent(table);
		content.setExpandRatio(table, 1.0f);
		
		content.addComponent(buttons1);
		content.setExpandRatio(buttons1, 0.0f);

		refresh();

		final Button add = new Button("Lis‰‰ vastuumalli");
		final Button edit = new Button("Muokkaa kentti‰");

		final Button save = new Button("Valitse vastuumalli");
		final Button close = new Button("Poistu");
		
		buttons1.addComponent(add);
		buttons1.addComponent(edit);

		buttons2.addComponent(save);
		buttons2.addComponent(close);
		
		edit.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 7827032177626629386L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				isListening = false;
				
				if(edit.getCaption().equals("Muokkaa kentti‰")) {
					
					edit.setCaption(Terminology.SAVE_FIELDS);
					table.setEditable(true);
					
				} else {
					
					edit.setCaption("Muokkaa kentti‰");
					
					if(currentSelection != null) {
						pruneCurrentFields();
						currentSelection.setFields(currentFields);
						Updates.update(main, true);
					}
					
					table.setEditable(false);
					
				}
				
				isListening = true;

				refresh();
				
			}
		});
		
		add.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 4880293081824569375L;

			@Override
			public void buttonClick(ClickEvent event) {
				new ResponsibilityModel(main.getDatabase(), Terminology.NEW_RESPONSIBILITY_MODEL);
				Updates.update(main, true);
				refresh();
			}
		});

		final Window dialog = Dialogs.makeDialog(main, "450px", "650px", Terminology.EDIT_RESPONSIBILITY_MODEL, null, content, buttons2);
		
		save.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -903277368165079909L;

			@Override
			public void buttonClick(ClickEvent event) {
				Database database = main.getDatabase();
				Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
				ResponsibilityModelsDialog.this.mapType.denyRelation(database, modelRelation);
				if(currentSelection != null)
					ResponsibilityModelsDialog.this.mapType.addRelation(modelRelation, currentSelection);
				Updates.update(main, true);
				main.removeWindow(dialog);
			}
		});

		close.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -7204678564018834037L;

			@Override
			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
			}
		});

	}
	
	private void initialize() {
		
		Database database = main.getDatabase();
		
		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, mapType);
		
		this.currentSelection = model;
		
		if(currentSelection != null) {
			currentFields = currentSelection.getFields();
		} else {
			currentFields = new ArrayList<String>();
		}
		
	}

	private void refresh() {
		
		isListening = false;
		
		Database database = main.getDatabase();

		models.removeAllItems();

		for(ResponsibilityModel model : ResponsibilityModel.enumerate(database)) {
			models.addItem(model);
			models.setItemCaption(model, model.getText(database));
		}
		
		if(currentSelection != null) {
			models.select(currentSelection);
			String name = currentSelection.getText(database);
			nameField.setValue(name);
		}
		
		table.removeAllItems();

		int itemId = 0;
		
		if(currentSelection != null) {
			
			pruneCurrentFields();
			
			for(String field : currentFields) {
				table.addItem(new Object[] { field }, itemId++);
			}
			
		}

		if(table.isEditable()) {
			table.addItem(new Object[] { "" }, itemId++);
		}

		isListening = true;

	}
	
	private void pruneCurrentFields() {

		ArrayList<String> pruned = new ArrayList<String>();
		for(String field : currentFields) 
			if(!field.isEmpty())
				pruned.add(field);
		
		currentFields = pruned;
		

	}
	
	private List<String> currentFields() {

		ArrayList<String> result = new ArrayList<String>();
		Container container = table.getContainerDataSource();
		for(int i=0;i<container.size();i++) {
			Item item = container.getItem(i);
			com.vaadin.data.Property p = item.getItemProperty(FIELD);
			result.add(p.getValue().toString());
		}
		return result;

	}
	
	private void tableValueChange() {

		isListening = false;
		
		this.currentFields = currentFields(); 
		
		refresh();
		
		isListening = true;

	}

}
