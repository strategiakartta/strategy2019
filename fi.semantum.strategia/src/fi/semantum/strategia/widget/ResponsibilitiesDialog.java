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

import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.ResponsibilityInstance;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class ResponsibilitiesDialog {

	private Main main;
	private MapBase base;
	private ComboBox modelsSelection;
	private TextArea nameField;
	private boolean isListening = false;
	
	private ValueChangeListener comboListener;
	private String currentField = "";

	private VerticalLayout rootContent;
	private VerticalLayout responsibilityContent;
	private HorizontalLayout buttons1;
	private Button save;
	private Button delete;
	
	public ResponsibilitiesDialog(final Main main, MapBase b) {
		rootContent = new VerticalLayout();
		rootContent.setSizeFull();
		rootContent.setSpacing(true);
		
		this.main = main;
		this.base = b;
		
		responsibilityContent = new VerticalLayout();
		responsibilityContent.setSizeFull();
		responsibilityContent.setSpacing(true);

		buttons1 = new HorizontalLayout();
		buttons1.setSpacing(true);
		buttons1.setMargin(false);

		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setWidth("100%");
		buttonToolbar.setSpacing(true);
		buttonToolbar.setMargin(false);

		modelsSelection = new ComboBox();
		modelsSelection.setWidth("100%");
		modelsSelection.setNullSelectionAllowed(false);
		modelsSelection.addStyleName(ValoTheme.COMBOBOX_SMALL);
		modelsSelection.setCaption("Valitse kentt‰:");
		
		nameField = new TextArea();
		nameField.setSizeFull();
		nameField.setCaption("Kent‰n tiedot:");
		
		comboListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = 1936269579541506442L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(!isListening) return;
				currentField = (String)modelsSelection.getValue();
				refresh();
			}
			
		};
		
		modelsSelection.addValueChangeListener(comboListener);
		
		responsibilityContent.addComponent(modelsSelection);
		responsibilityContent.setExpandRatio(modelsSelection, 0.0f);

		responsibilityContent.addComponent(nameField);
		responsibilityContent.setExpandRatio(nameField, 1.0f);

		save = new Button(Terminology.SAVE_FIELD);
		save.setStyleName(ValoTheme.BUTTON_TINY);
		final Button close = new Button("Sulje");
		close.setStyleName(ValoTheme.BUTTON_TINY);
		
		delete = new Button("Poista kentt‰");
		delete.setStyleName(ValoTheme.BUTTON_TINY);
		delete.setIcon(FontAwesome.TRASH);
		delete.setEnabled(false);
		
		refresh();
		
		buttonToolbar.addComponent(delete);
		buttonToolbar.setComponentAlignment(delete, Alignment.BOTTOM_LEFT);
		buttonToolbar.setExpandRatio(delete, 1.0f);
		buttonToolbar.addComponent(save);
		buttonToolbar.setComponentAlignment(save, Alignment.BOTTOM_RIGHT);
		buttonToolbar.addComponent(close);
		buttonToolbar.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

		final Window dialog = Dialogs.makeDialog(main, "450px", "650px", "M‰‰rit‰ vastuut", null, rootContent, buttonToolbar);
		
		delete.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -7100537010900970857L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(currentField != null) {
					if(main.getUIState().input) {
						Database database = main.getDatabase();
						Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
						ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, base);
						
						instance.removeField(currentField);
						Updates.update(main, true);
						refresh();
					} else {
						delete.setEnabled(false);
					}
				} else {
					delete.setEnabled(false);
				}
			}
			
		});
		
		save.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -5543987996724055368L;

			@Override
			public void buttonClick(ClickEvent event) {

				Database database = main.getDatabase();
				Account account = main.getAccountDefault();
				Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
				ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, base);
				if(instance == null) {
					base.denyRelation(database, instanceRelation);
					instance = new ResponsibilityInstance(main.getDatabase());
					Property aika = Property.find(database, Property.AIKAVALI);
					aika.set(main != null, database, account, instance, Property.AIKAVALI_KAIKKI);
					base.addRelation(instanceRelation, instance);
				}
				instance.setValue(currentField, nameField.getValue());
				Updates.update(main, true);
				refresh();
				
			}
			
		});

		close.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -5543987996724055368L;

			@Override
			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
			}
		});

	}

	private void refresh() {
		isListening = false;
		
		Database database = main.getDatabase();
		Set<String> fields = UtilsDB.getResponsibilityFields(database, base);
		
		rootContent.removeAllComponents();
		if(fields.isEmpty()) {
			Label guideLabel = new Label();
			guideLabel.setContentMode(ContentMode.HTML);
			guideLabel.setValue("<div>Vastuumalleja ei ole m‰‰ritelty.</div></br>"
					+ "<div>Luodaksesi uusia vastuumalleja:</div>"
					+ "<div>1) (ole kartan muokkaustilassa)</div>"
					+ "<div>2) Valitse -> \"Kartan tyyppi\"</div>"
					+ "<div>3) -> \"Muokkaa\"</div>"
					+ "<div>4) -> \"Vastuumalli\"</div>"
					+ "<div>5) -> \"Lis‰‰ vastuumalli\"</div>");
			save.setEnabled(false);
			
			rootContent.addComponent(guideLabel);
			rootContent.setExpandRatio(guideLabel, 1.0f);
			rootContent.addComponent(buttons1);
			rootContent.setExpandRatio(buttons1, 0.0f);
			return;
		} else {
			save.setEnabled(true);
			rootContent.addComponent(responsibilityContent);
			rootContent.setExpandRatio(responsibilityContent, 1.0f);
			rootContent.addComponent(buttons1);
			rootContent.setExpandRatio(buttons1, 0.0f);
			
			Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
			ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, base);
			
			modelsSelection.removeAllItems();
	
			for(String field : fields) {
				modelsSelection.addItem(field);
			}
			
			if(currentField.isEmpty()) {
				currentField = fields.iterator().next();
			}
			
			modelsSelection.select(currentField);
			
			
			if(instance == null) {
				delete.setEnabled(false);
			} else {
				if(currentField != null) {
					delete.setEnabled(instance.containsField(currentField));
				} else {
					delete.setEnabled(false);
				}
			}
			
			if(instance != null) {
				String currentValue = instance.getValue(currentField);
				if(currentValue == null) currentValue = "";
				nameField.setValue(currentValue);
			} else {
				nameField.setValue("");
			}
	
			isListening = true;
		}
	}
	
}
