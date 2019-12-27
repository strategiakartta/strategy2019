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

import java.util.Collection;
import java.util.List;

import org.vaadin.elements.ElementIntegration;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.colorpicker.ColorChangeEvent;
import com.vaadin.ui.components.colorpicker.ColorChangeListener;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Dialogs;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Updates;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.ObjectType;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UtilsDBMapTypes;

public class ManageMapsDialog {

	private CheckBox linkCheckbox;
	private CheckBox tavoiteSubmapCheckbox;
	private CheckBox hasVisionCheckbox;
	private CheckBox hasMeterCheckbox;
	private CheckBox allowsOwnOuterBoxCheckbox;
	private CheckBox definesOfficesCheckBox;
	private ComboBox tavoiteSubmapSelection;
	private ColorPicker backgroundButton;
	private ColorPicker foregroundButton;
	private TextField idTextField;
	private TextField goalTextField;
	private TextField focusTextField;
	private ComboBox amountOfLevelsComboBox;
	private ComboBox list;
	private Label example;
	private ValueChangeListener listener;
	private ValueChangeListener idTextFieldListener;
	private ValueChangeListener goalTextListener;
	private ValueChangeListener focusTextListener;
	private ValueChangeListener amountOfLevelsValueChangeListener;
	private ColorChangeListener foregroundListener;
	private ColorChangeListener backgroundListener;
	private ValueChangeListener linkCheckboxListener;
	private ValueChangeListener tavoiteSubmapCheckboxListener;
	private ValueChangeListener tavoiteSubmapSelectionListener;
	private ValueChangeListener hasVisionCheckboxListener;
	private ValueChangeListener hasMeterCheckboxListener;
	private ValueChangeListener allowsOwnOuterBoxCheckboxListener;
	private ValueChangeListener definesOfficesCheckBoxListener;
	private StrategyMap returnMap;
	private Base currentSelection;
	
	private String currentText;
	private String focusColor;
	private String focusTextColor;
	private String goalDescription;
	private String focusDescription;
	private String tavoiteSubmapType = "";
	private int amountOfLevels;
	private boolean hasVision;
	private boolean hasMeter;
	private boolean allowsOwnOuterBox;
	private boolean linkWithParent = true;
	private boolean tavoiteSubmap = false;
	private boolean definesOffices = false;
	final Database database;
	
	ManageMapsDialog(final Main main, StrategyMap returnMap, Base initialSelection) {
		
		this.database = main.getDatabase();

		this.returnMap = returnMap;
		currentSelection = initialSelection;
		currentText = currentSelection.getText(database);
		
        VerticalLayout content = new VerticalLayout();
        content.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
        content.setSizeFull();
        content.setSpacing(true);

        list = new ComboBox();
        list.setCaption("Valitse karttatyyppi");
        list.setWidth("100%");
        list.setNullSelectionAllowed(false);
        list.addStyleName(ValoTheme.TABLE_SMALL);
        list.addStyleName(ValoTheme.TABLE_COMPACT);
        list.setTextInputAllowed(false);

        final Button apply = new Button("Tee muutokset");
        apply.setStyleName(ValoTheme.BUTTON_SMALL);
        final Button add = new Button("Lis‰‰ tyyppi", new Button.ClickListener() {

			private static final long serialVersionUID = 7600747402942335542L;

			public void buttonClick(ClickEvent event) {
				
				MapTypes.createMapType(main, database, "Uusi karttatyyppi", true, false);
				Updates.updateJS(main, true);
				initializeAndUpdate(database);

            }
            
        });
        add.setStyleName(ValoTheme.BUTTON_SMALL);
        final Button vastuut = new Button(Relation.RESPONSIBILITY_MODEL, new Button.ClickListener() {

			private static final long serialVersionUID = -6379201679421629753L;

			public void buttonClick(ClickEvent event) {
				if(currentSelection != null && currentSelection instanceof ObjectType) {
					new ResponsibilityModelsDialog(main, (ObjectType)currentSelection);
				} else {
					System.err.println("Expected type ObjectType when defining responsibilities!");
				}

            }
            
        });
        vastuut.setStyleName(ValoTheme.BUTTON_SMALL);
        
        final Button close = new Button("Sulje");
        close.setStyleName(ValoTheme.BUTTON_SMALL);
        
		listener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				String uuid = (String)list.getValue();
				currentSelection = database.find(uuid);
				initializeAndUpdate(database);
				fetchAndUpdate();
			}
			
	    };
        
        content.addComponent(list);
        content.setExpandRatio(list, 0.0f);

        idTextField = new TextField();
        idTextField.setCaption("Karttatyypin nimi");
        idTextField.setWidth("100%");
        idTextFieldListener = new ValueChangeListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1202030133490478331L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fetchAndUpdate();
			}
			
	    };
	    idTextField.addValueChangeListener(idTextFieldListener);

	    idTextField.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        content.addComponent(idTextField);
        content.setExpandRatio(idTextField, 0.0f);

        amountOfLevelsComboBox = new ComboBox();
        amountOfLevelsComboBox.addItem(2);
        amountOfLevelsComboBox.addItem(3);
        amountOfLevelsComboBox.setNullSelectionAllowed(false);
        amountOfLevelsComboBox.select(2);
        amountOfLevelsComboBox.setCaption("Valitse sis‰kk‰isten laatikkotasojen m‰‰r‰");
        amountOfLevelsComboBox.setWidth("100%");
        amountOfLevelsValueChangeListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fetchAndUpdate();
			}
			
	    };
	    amountOfLevelsComboBox.addValueChangeListener(amountOfLevelsValueChangeListener);

	    amountOfLevelsComboBox.setStyleName(ValoTheme.COMBOBOX_SMALL);
        content.addComponent(amountOfLevelsComboBox);
        content.setExpandRatio(amountOfLevelsComboBox, 0.0f);
        
        goalTextField = new TextField();
        goalTextField.setCaption("Valitse m‰‰re ulommille laatikoille");
        goalTextField.setWidth("100%");
		goalTextListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fetchAndUpdate();
			}
			
	    };
	    goalTextField.addValueChangeListener(goalTextListener);

	    goalTextField.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        content.addComponent(goalTextField);
        content.setExpandRatio(goalTextField, 0.0f);

        
        
        
        focusTextField = new TextField();
        focusTextField.setCaption("Valitse m‰‰re uusille sis‰laatikoille");
        focusTextField.setWidth("100%");
		focusTextListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -8623322108689254211L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fetchAndUpdate();
			}
			
	    };
	    focusTextField.addValueChangeListener(focusTextListener);

	    focusTextField.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        content.addComponent(focusTextField);
        content.setExpandRatio(focusTextField, 0.0f);
        
        HorizontalLayout tavoite = new HorizontalLayout();
        tavoite.setWidth("100%");
        tavoite.setSpacing(true);
        tavoite.setMargin(false);
        
        foregroundButton = new ColorPicker("Valitse tekstin v‰ri");
        foregroundButton.setCaption("Valitse tekstin v‰ri");
        foregroundButton.setWidth("100%");
        foregroundListener = new ColorChangeListener() {
        	
			private static final long serialVersionUID = 2195620409108279819L;

			@Override
            public void colorChanged(ColorChangeEvent event) {
                fetchAndUpdate();
            }
        };
        foregroundButton.addColorChangeListener(foregroundListener);
        foregroundButton.setStyleName(ValoTheme.BUTTON_SMALL);
        tavoite.addComponent(foregroundButton);
        tavoite.setExpandRatio(foregroundButton, 1f);

        backgroundButton = new ColorPicker("Valitse taustav‰ri");
        backgroundButton.setCaption("Valitse taustav‰ri");
        backgroundButton.setWidth("100%");
	    backgroundListener = new ColorChangeListener() {
        	
			private static final long serialVersionUID = 5960028504533354038L;

			@Override
            public void colorChanged(ColorChangeEvent event) {
                fetchAndUpdate();
            }
        };
        backgroundButton.addColorChangeListener(backgroundListener);
        backgroundButton.setStyleName(ValoTheme.BUTTON_SMALL);
        tavoite.addComponent(backgroundButton);
        tavoite.setExpandRatio(backgroundButton, 1f);
        
        content.addComponent(tavoite);
        content.setExpandRatio(tavoite, 0.0f);

        hasVisionCheckbox = new CheckBox("Kartta m‰‰ritt‰‰ vision");
        hasVisionCheckbox.setWidth("100%");
        hasVisionCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
                fetchAndUpdate();
			}
			
        };
        hasVisionCheckbox.addValueChangeListener(hasVisionCheckboxListener);
        
        hasVisionCheckbox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        content.addComponent(hasVisionCheckbox);
        content.setExpandRatio(hasVisionCheckbox, 0.0f);

        hasMeterCheckbox = new CheckBox("Sisemmill‰ laatikoilla on mittarit");
        hasMeterCheckbox.setWidth("100%");
        hasMeterCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
                fetchAndUpdate();
			}
			
        };
        hasMeterCheckbox.addValueChangeListener(hasMeterCheckboxListener);
        
        hasMeterCheckbox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        content.addComponent(hasMeterCheckbox);
        content.setExpandRatio(hasMeterCheckbox, 0.0f);

        allowsOwnOuterBoxCheckbox = new CheckBox("Kartta sallii omat ulommat laatikot");
        allowsOwnOuterBoxCheckbox.setWidth("100%");
        allowsOwnOuterBoxCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
			}
			
        };
        allowsOwnOuterBoxCheckbox.addValueChangeListener(allowsOwnOuterBoxCheckboxListener);
        
        allowsOwnOuterBoxCheckbox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        content.addComponent(allowsOwnOuterBoxCheckbox);
        content.setExpandRatio(allowsOwnOuterBoxCheckbox, 0.0f);

        
        ////////////////////////////////////////////////////////////////////////////
        // Does the map type define an office (start)
        definesOfficesCheckBox = new CheckBox();
        definesOfficesCheckBox.setWidth("100%");
        updateDefinesOfficesCheckBoxEnabled(database);
        
        //Create confirm popup:
        VerticalLayout popupContent = new VerticalLayout();
        popupContent.setHeight("250px");
        popupContent.setWidth("260px");
        HorizontalLayout buttonToolbar = new HorizontalLayout();       
        buttonToolbar.setWidth("100%");
        PopupView confirmDefinesOfficeSelectionPopupView = new PopupView("", popupContent);
        confirmDefinesOfficeSelectionPopupView.setHideOnMouseOut(false);
        
        Button accept = new Button();
        accept.setCaption("Hyv‰ksy");
        accept.setStyleName(ValoTheme.BUTTON_TINY);
        accept.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 486174445979886077L;

			@Override
			public void buttonClick(ClickEvent event) {
				definesOffices = !definesOfficesCheckBox.getValue();
				definesOfficesCheckBox.removeValueChangeListener(definesOfficesCheckBoxListener);
				definesOfficesCheckBox.setValue(definesOffices);
				definesOfficesCheckBox.addValueChangeListener(definesOfficesCheckBoxListener);
				confirmDefinesOfficeSelectionPopupView.setPopupVisible(false);
			}
        	
        });
        
        Button closePopup = new Button();
        closePopup.setCaption("Hylk‰‰");
        closePopup.setStyleName(ValoTheme.BUTTON_TINY);
        closePopup.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 2407626945912975380L;

			@Override
			public void buttonClick(ClickEvent event) {
				confirmDefinesOfficeSelectionPopupView.setPopupVisible(false);
			}
        	
        });
        
        buttonToolbar.addComponent(accept);
        buttonToolbar.setComponentAlignment(accept, Alignment.TOP_LEFT);
        buttonToolbar.addComponent(closePopup);
        buttonToolbar.setComponentAlignment(closePopup, Alignment.TOP_RIGHT);
        Label l = new Label("Vahvista muokkaus. Vain yksi karttatyyppi voi m‰‰ritt‰‰ tulossopimuksia."
        		+ "Olet muokkaamassa nykyist‰ valintaa. Muutos tallennetaan kun painat \"Tee muutokset\" nappia t‰m‰n j‰lkeen.");
        popupContent.addComponent(l);
        popupContent.setExpandRatio(l, 0.7f);
        popupContent.setComponentAlignment(l, Alignment.TOP_CENTER);
        popupContent.addComponent(buttonToolbar);
        popupContent.setExpandRatio(buttonToolbar, 0.3f);
        popupContent.setComponentAlignment(buttonToolbar, Alignment.MIDDLE_CENTER);
        
        definesOfficesCheckBoxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				//No matter what state the checkbox is in, set it to the old value without re-triggering listener:
				if(main.getAccountDefault().isAdmin(database)) {
					definesOfficesCheckBox.removeValueChangeListener(definesOfficesCheckBoxListener);
					definesOfficesCheckBox.setValue(!definesOfficesCheckBox.getValue());
					confirmDefinesOfficeSelectionPopupView.setPopupVisible(true);
					definesOfficesCheckBox.addValueChangeListener(definesOfficesCheckBoxListener);
				} else {
					definesOfficesCheckBox.removeValueChangeListener(definesOfficesCheckBoxListener);
					definesOfficesCheckBox.setValue(!definesOfficesCheckBox.getValue());
					definesOfficesCheckBox.addValueChangeListener(definesOfficesCheckBoxListener);
					
					Dialogs.errorDialog(main, "Sinulla ei ole oikeutta muokata viraston m‰‰rityst‰."
							+ "", new Label("Ota yhteys yll‰pit‰j‰‰n (admin)."), "500px", "250px");
				}
			}
			
        };
        definesOfficesCheckBox.addValueChangeListener(definesOfficesCheckBoxListener);
        
        definesOfficesCheckBox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        HorizontalLayout definesOfficeLayout = new HorizontalLayout();
        definesOfficeLayout.addComponent(confirmDefinesOfficeSelectionPopupView);
        definesOfficeLayout.setExpandRatio(confirmDefinesOfficeSelectionPopupView, 0.0f);
        definesOfficeLayout.addComponent(definesOfficesCheckBox);
        definesOfficeLayout.setExpandRatio(definesOfficesCheckBox, 1.0f);
        content.addComponent(definesOfficeLayout);
        content.setExpandRatio(definesOfficeLayout, 0.0f);
        
        // Does the map type define an office (end)
        ////////////////////////////////////////////////////////////////////////////
        
        linkCheckbox = new CheckBox("Kartta linkittyy yl‰tason karttaan");
        linkCheckbox.setWidth("100%");
        linkCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
			}
			
        };
        linkCheckbox.addValueChangeListener(linkCheckboxListener);
        
        linkCheckbox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        content.addComponent(linkCheckbox);
        content.setExpandRatio(linkCheckbox, 0.0f);

        tavoiteSubmapCheckbox = new CheckBox("Alikartat kytkeytyv‰t ulompiin laatikoihin");
        tavoiteSubmapCheckbox.setWidth("100%");
        tavoiteSubmapCheckboxListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				fetchAndUpdate();
			}
			
        };
        tavoiteSubmapCheckbox.addValueChangeListener(tavoiteSubmapCheckboxListener);

        tavoiteSubmapCheckbox.setStyleName(ValoTheme.CHECKBOX_SMALL);
        content.addComponent(tavoiteSubmapCheckbox);
        content.setExpandRatio(tavoiteSubmapCheckbox, 0.0f);

        tavoiteSubmapSelection = new ComboBox();
        tavoiteSubmapSelection.setWidth("100%");
		Collection<Base> subs = StrategyMap.availableLevels(database);
		for(Base b : subs) {
			tavoiteSubmapSelection.addItem(b.uuid);
			tavoiteSubmapSelection.setItemCaption(b.uuid, b.getText(database));
			tavoiteSubmapSelection.select(b.uuid);
		}
		
        tavoiteSubmapSelectionListener = new ValueChangeListener() {
        	
			private static final long serialVersionUID = 5967732351802123391L;

			@Override
			public void valueChange(ValueChangeEvent event) {
			}
			
        };
        tavoiteSubmapSelection.addValueChangeListener(tavoiteSubmapSelectionListener);
        
        tavoiteSubmapSelection.setStyleName(ValoTheme.COMBOBOX_SMALL);
        content.addComponent(tavoiteSubmapSelection);
        content.setExpandRatio(tavoiteSubmapSelection, 0.0f);
        
        
        example = new Label("<div style=\"width:100%;vertical-align:middle;\">Sisemm‰n laatikon teksti</div>");
        example.setContentMode(ContentMode.HTML);
        example.setSizeFull();
        content.addComponent(example);
        content.setExpandRatio(example, 1.0f);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        buttons.addComponent(apply);
        buttons.addComponent(add);
        buttons.addComponent(vastuut);
        buttons.addComponent(close);
        
		list.addValueChangeListener(listener);

        initializeAndUpdate(database);
        fetchAndUpdate();

        
		final Window dialog = Dialogs.makeDialog(main, "550px", "650px", "Hallinnoi karttatyyppej‰", null, content, buttons);
		close.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7345507553621833858L;

			public void buttonClick(ClickEvent event) {
				main.removeWindow(dialog);
				if(ManageMapsDialog.this.returnMap != null)
					MapTypes.selectMapType(main, ManageMapsDialog.this.returnMap);
            }
            
        });
		apply.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = 7345507553621833858L;

			public void buttonClick(ClickEvent event) {
				// Update fetches values and recomputes the view
				fetchAndUpdate();
				apply(main);
				if(ManageMapsDialog.this.returnMap != null)
					ManageMapsDialog.this.returnMap.prepare(main.getDatabase());
            }
            
        });
        
	}
	
	private void fetchFieldsFromWidgets() {
		
		currentText = idTextField.getValue();
		tavoiteSubmap = tavoiteSubmapCheckbox.getValue();
		tavoiteSubmapType = (String)tavoiteSubmapSelection.getValue();
		linkWithParent = linkCheckbox.getValue();
		amountOfLevels = (Integer)amountOfLevelsComboBox.getValue();
		goalDescription = goalTextField.getValue();
		focusDescription = focusTextField.getValue();
        focusTextColor = toCSS(foregroundButton.getColor());
		hasVision = hasVisionCheckbox.getValue();
		hasMeter = hasMeterCheckbox.getValue();
		allowsOwnOuterBox = allowsOwnOuterBoxCheckbox.getValue();
        focusColor = toCSS(backgroundButton.getColor());

	}
	
	private void updateDefinesOfficesCheckBoxEnabled(Database database) {
		boolean anotherObjectTypeDefinesOffices = false;
		
		if(this.currentSelection instanceof ObjectType) {
	        Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);
	        List<ObjectType> types = ObjectType.enumerate(database);
	        
	        for(ObjectType t : types) {
				String definesOffice = definesOfficesP.getPropertyValue(t);
				if(definesOffice != null && definesOffice.equals("true")) {
					ObjectType currentSelectedType = (ObjectType)this.currentSelection;
					if(!currentSelectedType.uuid.equals(t.uuid)) {
						anotherObjectTypeDefinesOffices = true;
					}
					break;
				}
			}
		}
        if(anotherObjectTypeDefinesOffices) {
        	definesOfficesCheckBox.setCaption("(Kartta ei voi m‰‰ritt‰‰ tulossopimusta - toinen karttatyyppi m‰‰ritt‰‰ sen jo)");
        	definesOfficesCheckBox.setDescription("Toinen karttatyyppi m‰‰ritt‰‰ viraston ja tulossopimuksen. Kaksi karttatyyppi‰ eiv‰t voi m‰‰ritt‰‰ t‰t‰.");
        	definesOfficesCheckBox.setEnabled(false);
        } else {
        	definesOfficesCheckBox.setCaption("Kartta m‰‰ritt‰‰ tulossopimuksen");
        	definesOfficesCheckBox.setDescription("M‰‰ritt‰‰kˆ kartta viraston ja tulossopimuksen kyseiselle virastolle?"
            		+ "Vain yksi karttatyyppi voi m‰‰ritt‰‰ t‰m‰n!");
        	definesOfficesCheckBox.setEnabled(true);
        }
	}
	
	void initializeAndUpdate(Database database) {

		Collection<Base> available = StrategyMap.availableLevels(database);
		if(currentSelection == null) {
			if(!available.isEmpty())
				currentSelection = available.iterator().next();
			else
				currentSelection = null;
		}

		list.removeValueChangeListener(listener);
		list.removeAllItems();
		for(Base b : available) {
			list.addItem(b.uuid);
			if(currentSelection != null) {
				if(currentSelection.uuid == b.uuid)
					list.select(b.uuid);
			}
			list.setItemCaption(b.uuid, b.text);
		}
		list.addValueChangeListener(listener);
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Property amountOfLevelsP = Property.find(database, Property.AMOUNT_OF_LEVELS);
		Property hasVisionP = Property.find(database, Property.HAS_VISION);
		Property hasMeterP = Property.find(database, Property.HAS_METER);
		Property allowsOwnOuterBoxP = Property.find(database, Property.ALLOWS_OWN_OUTER_BOX);
		

		//Property.HAS_METER
		//Property.ALLOWS_OWN_OUTER_BOX


		Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);
		
		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		if(currentSelection != null) {
			currentText = currentSelection.getText(database);
			focusDescription = characterDescriptionP.getPropertyValue(currentSelection);
			goalDescription = goalDescriptionP.getPropertyValue(currentSelection);
			focusColor = characterColorP.getPropertyValue(currentSelection);
			if(focusColor == null) focusColor = "#CA6446";
			focusTextColor = characterTextColorP.getPropertyValue(currentSelection);

			String amountOfLevelsValue = amountOfLevelsP.getPropertyValue(currentSelection);
			amountOfLevels = StrategyMap.parsePossibleInteger(amountOfLevelsValue, 3);

			String hasVisionValue = hasVisionP.getPropertyValue(currentSelection);
			if(hasVisionValue == null) hasVision = true;
			else if("false".equals(hasVisionValue)) hasVision = false;
			else hasVision = true;

			String hasMeterValue = hasMeterP.getPropertyValue(currentSelection);
			if(hasMeterValue == null) hasMeter = false;
			else if("true".equals(hasMeterValue)) hasMeter = true;
			else hasMeter = false;

			String allowsOwnOuterBoxValue = allowsOwnOuterBoxP.getPropertyValue(currentSelection);
			if(allowsOwnOuterBoxValue == null) allowsOwnOuterBox = false;
			else if("true".equals(allowsOwnOuterBoxValue)) allowsOwnOuterBox = true;
			else allowsOwnOuterBox = false;

			String linkValue = linkWithParentP.getPropertyValue(currentSelection);
			if(linkValue == null) linkWithParent = true;
			else if("false".equals(linkValue)) linkWithParent = false;
			else linkWithParent = true;
			
			String goalSubmapValue = goalSubmapP.getPropertyValue(currentSelection);
			if(goalSubmapValue == null) tavoiteSubmap = false;
			else if("true".equals(goalSubmapValue)) tavoiteSubmap = true;
			else tavoiteSubmap = false;

			String definesOfficeValue = definesOfficesP.getPropertyValue(currentSelection);
			if(definesOfficeValue == null) definesOffices = false;
			else if("true".equals(definesOfficeValue)) definesOffices = true;
			else definesOffices = false;
			
			Collection<Base> subTypes = currentSelection.getRelatedObjects(database, goalSubmapTypeR);
			if(subTypes.size() == 1) {
				tavoiteSubmapType = subTypes.iterator().next().uuid;
			} else {
				tavoiteSubmapType = "";
			}
		}		
		
		updateValuesToWidgets();
		
	}
	
	public String parseCSSDigit(int value) {
		String result = Integer.toHexString(value);
		if(result.length() == 1) result = "0" + result;
		return result;
	}
	
	public String toCSS(Color color) {
        return "#" + parseCSSDigit(color.getRed()) + parseCSSDigit(color.getGreen()) + parseCSSDigit(color.getBlue());
	}
	
	public Color toColor(String css) {
		if(css.length() == 4) {
			return new Color(Integer.parseInt(css.substring(1,2), 16), Integer.parseInt(css.substring(2,3), 16), Integer.parseInt(css.substring(3,4), 16));
		} else if (css.length() == 7) {
			return new Color(Integer.parseInt(css.substring(1,3), 16), Integer.parseInt(css.substring(3,5), 16), Integer.parseInt(css.substring(5,7), 16));
		} else throw new IllegalArgumentException("Invalid css color: '" + css + "'");
	}

	private void fetchAndUpdate() {

        fetchFieldsFromWidgets();
        updateValuesToWidgets();
        
	}

    private void updateValuesToWidgets() {

		Collection<Base> available = StrategyMap.availableLevels(database);
		
		list.removeValueChangeListener(listener);
		list.removeAllItems();
		for(Base b : available) {
			list.addItem(b.uuid);
			if(currentSelection != null) {
				if(currentSelection.uuid == b.uuid)
					list.select(b.uuid);
			}
			list.setItemCaption(b.uuid, b.text);
		}
		list.addValueChangeListener(listener);
		
		if(focusColor != null) {
			Color back = toColor(focusColor);
			backgroundButton.removeColorChangeListener(backgroundListener);
			backgroundButton.setColor(back);
			backgroundButton.addColorChangeListener(backgroundListener);
		}
		
		if(focusTextColor != null) {
			Color fore = toColor(focusTextColor);
			foregroundButton.removeColorChangeListener(foregroundListener);
			foregroundButton.setColor(fore);
			foregroundButton.addColorChangeListener(foregroundListener);
		}

		if(focusColor != null && focusTextColor != null)
			ElementIntegration.getRoot(example).setAttribute("style", "color:" + focusTextColor + ";background-color:" + focusColor + ";width:100%;height:100%;text-align:center;vertical-align:middle;line-height:100px;font-size:26px;");

		if(currentText != null) {
	        idTextField.removeValueChangeListener(idTextFieldListener);
	        idTextField.setValue(currentText);
	        idTextField.addValueChangeListener(idTextFieldListener);
		}

		if(goalDescription != null) {
	        goalTextField.removeValueChangeListener(goalTextListener);
	        goalTextField.setValue(goalDescription);
	        goalTextField.addValueChangeListener(goalTextListener);
		}

		if(focusDescription != null) {
	        focusTextField.removeValueChangeListener(focusTextListener);
	        focusTextField.setValue(focusDescription);
	        focusTextField.addValueChangeListener(focusTextListener);
		}
	
		linkCheckbox.removeValueChangeListener(linkCheckboxListener);
		linkCheckbox.setValue(linkWithParent);
		linkCheckbox.addValueChangeListener(linkCheckboxListener);

		hasVisionCheckbox.removeValueChangeListener(hasVisionCheckboxListener);
		hasVisionCheckbox.setValue(hasVision);
		hasVisionCheckbox.addValueChangeListener(hasVisionCheckboxListener);

		hasMeterCheckbox.removeValueChangeListener(hasMeterCheckboxListener);
		hasMeterCheckbox.setValue(hasMeter);
		hasMeterCheckbox.addValueChangeListener(hasMeterCheckboxListener);

		allowsOwnOuterBoxCheckbox.removeValueChangeListener(allowsOwnOuterBoxCheckboxListener);
		allowsOwnOuterBoxCheckbox.setValue(allowsOwnOuterBox);
		allowsOwnOuterBoxCheckbox.addValueChangeListener(allowsOwnOuterBoxCheckboxListener);

		definesOfficesCheckBox.removeValueChangeListener(definesOfficesCheckBoxListener);
		updateDefinesOfficesCheckBoxEnabled(database);
		definesOfficesCheckBox.setValue(definesOffices);
		definesOfficesCheckBox.addValueChangeListener(definesOfficesCheckBoxListener);
		
		tavoiteSubmapCheckbox.removeValueChangeListener(tavoiteSubmapCheckboxListener);
		tavoiteSubmapCheckbox.setValue(tavoiteSubmap);
		tavoiteSubmapCheckbox.addValueChangeListener(tavoiteSubmapCheckboxListener);


		if(tavoiteSubmap) {
			tavoiteSubmapSelection.removeValueChangeListener(tavoiteSubmapSelectionListener);
			if(!tavoiteSubmapType.isEmpty())
				tavoiteSubmapSelection.select(tavoiteSubmapType);
			tavoiteSubmapSelection.addValueChangeListener(tavoiteSubmapSelectionListener);
			tavoiteSubmapSelection.setVisible(true);
		} else {
			tavoiteSubmapSelection.setVisible(false);
		}

		amountOfLevelsComboBox.removeValueChangeListener(amountOfLevelsValueChangeListener);
		amountOfLevelsComboBox.select(amountOfLevels);
		amountOfLevelsComboBox.addValueChangeListener(amountOfLevelsValueChangeListener);

	}
	
	public void apply(Main main) {
		
		Database database = main.getDatabase();
		
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Property amountOfLevelsR = Property.find(database, Property.AMOUNT_OF_LEVELS);
		Property hasVisionP = Property.find(database, Property.HAS_VISION);
		Property hasMeterP = Property.find(database, Property.HAS_METER);
		Property allowsOwnOuterBoxP = Property.find(database, Property.ALLOWS_OWN_OUTER_BOX);
		Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);
		
		Relation goalSubmapTypeP = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		if(currentSelection != null) {

			if(currentSelection instanceof ObjectType) {
				((ObjectType)currentSelection).modifyId(main.getDatabase(), main.getAccountDefault(), currentText);
				((ObjectType)currentSelection).modifyText(main.getDatabase(), main.getAccountDefault(), currentText);
			} else if(currentSelection instanceof MapBase) {
				((MapBase)currentSelection).modifyId(main.getDatabase(), main.getAccountDefault(), currentText);
				((MapBase)currentSelection).modifyText(main.getDatabase(), main.getAccountDefault(), currentText);
			} else {
				System.err.println("UNEXPECTED ERROR FROM USER ACTION: Editing an unexpected element in apply of ManageMapsDialog! Class: " + currentSelection.getClass().getName());
			}
			Utils.setProperty(goalDescriptionP, main, database, currentSelection, goalDescription);
			Utils.setProperty(characterDescriptionP, main, database, currentSelection, focusDescription);
			Utils.setProperty(characterColorP, main, database, currentSelection, focusColor);
			Utils.setProperty(characterTextColorP, main, database, currentSelection, focusTextColor);
			Utils.setProperty(linkWithParentP, main, database, currentSelection, linkWithParent ? "true" : "false");
			Utils.setProperty(goalSubmapP, main, database, currentSelection, tavoiteSubmap ? "true" : "false");
			Utils.setProperty(amountOfLevelsR, main, database, currentSelection, Integer.toString(amountOfLevels));
			Utils.setProperty(hasVisionP, main, database, currentSelection, hasVision ? "true" : "false");
			Utils.setProperty(hasMeterP, main, database, currentSelection, hasMeter ? "true" : "false");
			Utils.setProperty(allowsOwnOuterBoxP, main, database, currentSelection, allowsOwnOuterBox ? "true" : "false");
			Account account = main.getAccountDefault();
			boolean isAdmin = account.isAdmin(database);

			String oldValue = definesOfficesP.getPropertyValue(currentSelection);
			boolean oldDidDefineOffices = oldValue != null && oldValue.equals("true");
			if(definesOffices) {
				if(oldDidDefineOffices) {
					//Tried to update to define offices, but was already true. Do nothing!
				} else {
					//Otherwise, update if admin account
					if(isAdmin) {
						boolean success = UtilsDBMapTypes.updateOfficeMapTypeSelection(database, main != null, account, currentSelection);
						if(!success) {
							Dialogs.errorDialog(main, "Tulossopimus kytkent‰‰ ei voitu tehd‰ karttatyypille!", new Label(""));
						}
					} else {
						System.err.println("Non-admin account " + account.text + " tried to edit defines offices!");
						Dialogs.errorDialog(main, "Sinulla ei ole oikeutta muokata viraston m‰‰rityst‰."
								+ "", new Label("Ota yhteys yll‰pit‰j‰‰n (admin)."), "500px", "250px");
					}
				}
			} else {
				//If set to not define offices, then do cleanup only if old value did define offices.
				if(oldDidDefineOffices) {
					if(isAdmin) {
						UtilsDBMapTypes.removeAllNonControllerOffices(database);
						Utils.setProperty(definesOfficesP, main, main.getDatabase(), currentSelection, "false");
					} else {
						System.err.println("Non-admin account " + account.text + " tried to edit defines offices!");
						Dialogs.errorDialog(main, "Sinulla ei ole oikeutta muokata viraston m‰‰rityst‰."
								+ "", new Label("Ota yhteys yll‰pit‰j‰‰n (admin)."), "500px", "250px");
					}
				} else {
					//Otherwise property is null or false - make sure it's false
					Utils.setProperty(definesOfficesP, main, main.getDatabase(), currentSelection, "false");
				}
			}
			
			currentSelection.denyRelation(database, goalSubmapTypeP);

			if(tavoiteSubmap) {
				Base b = database.find(tavoiteSubmapType);
				if(b != null) {
					currentSelection.addRelation(goalSubmapTypeP, b);
				}
			}

			Updates.updateJS(main, true);
			initializeAndUpdate(database);
		}	
		
	}
	
	public static ManageMapsDialog create(Main main, StrategyMap returnMap, Base initialSelection) {
		return new ManageMapsDialog(main, returnMap, initialSelection);
	}
	
}
