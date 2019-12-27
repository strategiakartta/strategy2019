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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.action.ActionAddOuterBox;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.map.transformation.MapTransformationUtils;
import fi.semantum.strategia.widget.StandardVisRegistry;
import fi.semantum.strategy.db.AccessRight;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.AccountGroup;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.CalendarEventBase.EventColor;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.CommentToolAdminBase;
import fi.semantum.strategy.db.CommentToolBase;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.MapBox;
import fi.semantum.strategy.db.MapRight;
import fi.semantum.strategy.db.ObjectType;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Pair;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.ResultAgreementToolAdminBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.TimeConfiguration;
import fi.semantum.strategy.db.TimeInterval;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;
import fi.semantum.strategy.db.UtilsDBComments;

public class Utils {
	
	public static MapVis mapBaseToMapVis(Database database, Account account, UIState state,
			MapBase base,
			int lowestAcceptablePriority) {
		MapVis vis = StandardVisRegistry.getInstance().getBestMapVis(database, account, state, base, lowestAcceptablePriority);
		return vis;
	}
	
	public static List<MapVis> mapBaseAndParentsToMapVis(Database database, Account account, UIState state,
			MapBase base,
			int lowestAcceptablePriority){
		List<MapVis> result = new ArrayList<>();
		Base parent = base.getOwner(database);
		if(parent != null && parent instanceof MapBase) {
			List<MapVis> parentMapVises = mapBaseAndParentsToMapVis(database, account, state, (MapBase)parent, lowestAcceptablePriority);
			result.addAll(parentMapVises);
		}
		
		MapVis vis = mapBaseToMapVis(database, account, state, base, lowestAcceptablePriority);
		if(vis != null) {
			result.add(vis);
		} else {
			System.err.println("MapVis for base " + base.uuid + " was null!");
		}
		return result;
	}
	
	
	public static List<String> changeSuggestionTargetPathAsList(Main main, ChangeSuggestion cs) {
		List<String> result = new ArrayList<>();
		String uuid = cs.getPossibleBaseTargetUUID();
		if(uuid != null) {
			Base base = main.getDatabase().find(uuid);
			if(base instanceof MapBase) {
				List<MapVis> mapVises = Utils.mapBaseAndParentsToMapVis(
						main.getDatabase(),
						main.getAccountDefault(),
						main.getUIState(),
						(MapBase)base,
						MapVisFactory.PRORITY_ALL);
				for(MapVis vis : mapVises) {
					result.add(vis.text);
				}
			} else {
				if(base instanceof TextChapter) {
					TextChapter chapter = (TextChapter)base;
					result.add(UtilsDBComments.textChapterToSummary(main.getDatabase(), chapter));
				}
			}
		} else {
			result.add("(muutosehdotuksella ei ole kohdetta)");
		}
		return result;
	}
	
	/**
	 * Call the UtilsDB replacement with the current map transformations
	 * @param html
	 * @param main
	 * @param office
	 * @return
	 */
	public static String htmlTagReplacement(String html, Main main) {
		Database database = main.getDatabase();
		return MapTransformationUtils.htmlTagReplacement(html, database);
	}
	
	public static List<String> getAvailableTransformationTags() {
		return MapTransformationUtils.getAvailableTags();
	}
	
	public static List<HTMLMapTransform> getAvailableMapTransforms(){
		return MapTransformationUtils.getAvailableMapTransforms();
	}
	
	public static TreeTable emptyMapNavigationTreeTable() {
		TreeTable treeTable = new TreeTable();
		treeTable.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		
		treeTable.addStyleName(ValoTheme.TABLE_SMALL);
		treeTable.addStyleName(ValoTheme.TABLE_SMALL);
		treeTable.addStyleName(ValoTheme.TABLE_COMPACT);
		treeTable.setReadOnly(true);

		treeTable.setPageLength(0);
		
		return treeTable;
	}
	
	/**
	 * Clear and remove all components, and nullify
	 * @param c
	 */
	public static void clearAndNullify(Component c) {
		if(c != null) {
			if(c instanceof AbstractLayout) {
				if(c != null) {
					((AbstractLayout)c).removeAllComponents();
				}	
			}
			c = null;
		}
	}
	
	
	public static void modifyAccount(final Main main) {

		final Database database = main.getDatabase();
		
		FormLayout content = new FormLayout();
		content.setSizeFull();

		final Label l = new Label(main.account.getId(database));
        l.setCaption("Käyttäjän nimi:");
        l.setWidth("100%");
        content.addComponent(l);
		
        Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, main.account);
		final Label officeLabel = new Label(office != null ? office.getText(database) : "(ei virastoa)");
		officeLabel.setCaption("Käyttäjän virasto:");
		officeLabel.setWidth("100%");
        content.addComponent(officeLabel);
        
        final TextField tf = new TextField();
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.setCaption("Käyttäjän nimi:");
        tf.setId("loginUsernameField");
        tf.setValue(main.account.getText(database));
        content.addComponent(tf);

        final TextField tf2 = new TextField();
        tf2.setWidth("100%");
        tf2.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf2.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf2.setCaption("Sähköpostiosoite:");
        tf2.setId("loginUsernameField");
        tf2.setValue(main.account.getEmail());
        content.addComponent(tf2);

        final PasswordField pf = new PasswordField();
        pf.setCaption("Vanha salasana:");
        pf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf.setWidth("100%");
        pf.setId("loginPasswordField");
        content.addComponent(pf);
        
        final PasswordField pf2 = new PasswordField();
        pf2.setCaption("Uusi salasana:");
        pf2.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf2.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf2.setWidth("100%");
        pf2.setId("loginPasswordField");
        content.addComponent(pf2);

        final PasswordField pf3 = new PasswordField();
        pf3.setCaption("Uusi salasana uudestaan:");
        pf3.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf3.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf3.setWidth("100%");
        pf3.setId("loginPasswordField");
        content.addComponent(pf3);

        final Label err = new Label("Väärä käyttäjätunnus tai salasana");
        err.addStyleName(ValoTheme.LABEL_FAILURE);
        err.addStyleName(ValoTheme.LABEL_TINY);
        err.setVisible(false);
        content.addComponent(err);
		
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);

        Button apply = new Button("Tee muutokset");
        apply.setStyleName(ValoTheme.BUTTON_TINY);
        apply.setIcon(FontAwesome.SAVE);
        
        buttons.addComponent(apply);
        
        final Window dialog = Dialogs.makeDialog(main, "450px", "550px", "Käyttäjätilin asetukset", "Poistu", content, buttons);
        apply.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	
            	String valueHash = UtilsDB.hash(pf.getValue());
            	if(!valueHash.equals(main.account.getHash())) {
            		err.setValue("Väärä salasana");
            		err.setVisible(true);
            		return;
            	}
            	
            	if(pf2.isEmpty()) {
            		err.setValue("Tyhjä salasana ei kelpaa");
            		err.setVisible(true);
            		return;
            	}
            	
            	if(!pf2.getValue().equals(pf3.getValue())) {
            		err.setValue("Uudet salasanat eivät täsmää");
            		err.setVisible(true);
            		return;
            	}
            	
            	main.account.text = tf.getValue();
            	main.account.email = tf2.getValue();
            	main.account.hash = UtilsDB.hash(pf2.getValue());

            	Updates.update(main, true);
            	
            	main.removeWindow(dialog);
            	
            }
            
        });

	}

	public static String findFreshGroupName(Validator validator) {
		String defaultGroupName = "Uusi ryhmä";
		return findFreshName_(validator, defaultGroupName);
	}
	
	public static String findFreshUserName(Validator validator) {
		String defaultGroupName = "Uusi käyttäjä";
		return findFreshName_(validator, defaultGroupName);		
	}
	
	private static String findFreshName_(Validator validator, String suggestion) {
		String proposal = suggestion;
		try {
			
			validator.validate(proposal);
			return proposal;
			
		} catch (InvalidValueException e) {

			int counter = 2;
			while(true) {
				proposal = suggestion + " " + counter++;
				try {
					validator.validate(proposal);
					return proposal;
				} catch (InvalidValueException e2) {
				}
			}
			
		}
	}

	public static void saveCurrentState(final Main main) {
        
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setSpacing(true);
        hl1.setWidth("100%");
        
		final Map<String,UIState> stateMap = new HashMap<String,UIState>();
		for(UIState s : main.account.uiStates)
			stateMap.put(s.name, s);

        final TextField tf = new TextField();
        
		final Button save = new Button(Terminology.SAVE_VIEW_LABEL, new Button.ClickListener() {

			private static final long serialVersionUID = 2449606920686729881L;

			public void buttonClick(ClickEvent event) {

				if(!tf.isValid()) return;
				
				String name = tf.getValue();

				Page.getCurrent().getJavaScript().execute("doSaveBrowserState('" + name + "');");
				
			}

		});
        
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setCaption(Terminology.SAVE_CURRENT_VIEW_WITH_NAME_LABEL);
		tf.setValue("Uusi näkymä");
		tf.setCursorPosition(tf.getValue().length());
		tf.setValidationVisible(true);
		tf.setInvalidCommitted(true);
		tf.setImmediate(true);
		tf.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				tf.setValue(event.getText());
				try {
					tf.validate();
				} catch (InvalidValueException e) {
					save.setEnabled(false);
					return;
				}
				save.setEnabled(true);
			}
			
		});
		tf.addValidator(new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				if(s.isEmpty())
					throw new InvalidValueException("Nimi ei saa olla tyhjä");
				if(stateMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo käytössä");
			}
			
		});
		if(!tf.isValid()) save.setEnabled(false);
		
		hl1.addComponent(tf);
		hl1.setExpandRatio(tf, 1.0f);
		
		hl1.addComponent(save);
		hl1.setExpandRatio(save, 0.0f);
		hl1.setComponentAlignment(save, Alignment.BOTTOM_CENTER);
		
        content.addComponent(hl1);
        content.setExpandRatio(hl1, 0.0f);

        final ListSelect table = new ListSelect();
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);
        table.setMultiSelect(true);
        table.setCaption("Tallennetut näkymät");
        for(UIState state : main.account.uiStates) {
        	table.addItem(state.name);
        }
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);
        
		final Button remove = new Button("Poista valitut näkymät");
		
        table.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					remove.setEnabled(true);
				} else {
					remove.setEnabled(false);
				}
				
			}
			
		});
        
        remove.setEnabled(false);

        content.addComponent(remove);
        content.setComponentAlignment(remove, Alignment.MIDDLE_LEFT);
        content.setExpandRatio(remove, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        final Window dialog = Dialogs.makeDialog(main, "Näkymien hallinta", "Sulje",  content, buttons);
        
        remove.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -4680588998085550908L;

			public void buttonClick(ClickEvent event) {

				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				
				if(!selected.isEmpty()) {
					
					for(Object o : selected) {
						UIState state = stateMap.get(o);
						if(state != null)
							main.account.uiStates.remove(state);
					}
					Updates.update(main, true);
				}
				
				main.removeWindow(dialog);

			}

		});

	}
	
	/**
	 * Helper method for a new combobox with common properties of a MapType combobox
	 * Populates combobox with the strategymap available levels
	 * @return
	 */
	private static ComboBox createMapTypeComboBox(Database database) {
		ComboBox combo = new ComboBox();
		combo.setCaption("Kartan tyyppi:");
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		
		Collection<Base> subs = StrategyMap.availableLevels(database);
		for(Base b : subs) {
			combo.addItem(b.uuid);
			combo.setItemCaption(b.uuid, b.getText(database));
			combo.select(b.uuid);
		}

		return combo;
	}
	
	/**
	 * Helper method for a new TextField with common properties of a MapName textfield
	 * @return
	 */
	private static TextField createMapNameTextField() {
		TextField tf2 = new TextField();
		tf2.setCaption("Kartan nimi:");
		tf2.setValue(Terminology.NEW_MAP);
		tf2.setWidth("100%");
		return tf2;
	}
	
	public static void addMap(final Main main, final StrategyMap parent) {

		final Database database = main.getDatabase();
		
		FormLayout content = new FormLayout();
		content.setSizeFull();

		final TextField tf2 = createMapNameTextField();
		content.addComponent(tf2);
		
		final ComboBox combo = createMapTypeComboBox(database);
		content.addComponent(combo);
		
		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button(Terminology.ADD);
		ok.setStyleName(ValoTheme.BUTTON_SMALL);
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "525px", "350px", Terminology.ADD_LOWER_LEVEL_MAP_LABEL, Terminology.CANCEL, content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String typeUUID = (String)combo.getValue();

				try {
					
					Base subType = parent.getPossibleSubmapType(database);
					if(subType != null && subType.uuid.contentEquals(typeUUID)) {
						
						// Here we are instantiating a submap with the defined outer box submap type.
						// Perform same action as if asking to create a new outer box.
						main.removeWindow(dialog);
						new ActionAddOuterBox(main, parent).run();
						return;
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}

				// A normal submap with no link to boxes (yet)
					
				String name = tf2.getValue();

				Base type = database.find(typeUUID);
				database.newMap(parent, "", name, type);

				main.removeWindow(dialog);

				Updates.updateJS(main, true);

			}
		});
		
	}
	
	public static boolean ensureImplementationMap(final Main main, OuterBox goal) throws Exception {
		Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		String currentTime = main.getUIState().getTime();
		return goal.ensureImplementationMap(database, account, currentTime);
	}
	
	public static boolean isImplementationMap(Database database, StrategyMap map) {
		for(Base imp : map.getImplementedSet(database)) {
			if(imp instanceof OuterBox) return true;
		}
		return false;
	}
	
	public static void addImplementationMap(final Main main, final OuterBox goal) {

		final Database database = main.getDatabase();

		try {
			Base subType = goal.getPossibleSubmapType(database);
			if(subType != null) {
				Utils.ensureImplementationMap(main, goal);
				Updates.updateJS(main, true);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		FormLayout content = new FormLayout();
		content.setSizeFull();
		
		final ComboBox combo = createMapTypeComboBox(database);
		content.addComponent(combo);

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button(Terminology.ADD);
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "340px", Terminology.ADD_LOWER_LEVEL_MAP_LABEL, Terminology.CANCEL, content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				StrategyMap parent = database.getMap(goal);
				
				StrategyMap newMap = database.newMap(parent, "", "", type);
				newMap.addRelation(Relation.find(database, Relation.IMPLEMENTS), goal);
				
				for(InnerBox pp : goal.innerboxes) {
					Utils.createOuterBoxCopy(main, newMap, pp);
				}
				
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
		});
		
	}

	public static void boxMoveUp(Main main, MapBox box) {
		Database database = main.getDatabase();
		box.moveUp(database);
		Updates.updateJS(main, true);
	}
	
	public static void boxMoveDown(Main main, MapBox box) {
		Database database = main.getDatabase();
		box.moveDown(database);
		Updates.updateJS(main, true);
	}
	
	/**
	 * 
	 * @param main
	 * @param map
	 * @param goal
	 * @param uuid
	 * @return
	 */
	public static InnerBox createInnerBox(Main main, StrategyMap map, OuterBox goal, String uuid) {
		Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		return InnerBox.create(database, main.getUIState().getTime(), map, goal, uuid, "", null);
	}
	
	public static InnerBox createInnerBoxCopy(Main main, StrategyMap map, OuterBox goal, Base ref) {
		Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		return UtilsDB.createInnerBoxCopy(database, account, main.getUIState().getTime(), map, goal, ref);
	}
	
	public static OuterBox createOuterBoxCopy(Main main, StrategyMap map, Base ref) {
		Database database = main.getDatabase();
		String currentTime = main.getUIState().getTime();
		Account account = main.getAccountDefault();
		return UtilsDB.createOuterBoxCopy(database, account, map, ref, currentTime);

	}
	
	public static void insertRootMap(final Main main, final StrategyMap currentRoot) {

		final Database database = main.getDatabase();

		FormLayout content = new FormLayout();
		content.setSizeFull();

		final TextField tf = new TextField();
		tf.setCaption("Kartan tunniste:");
		tf.setValue("tunniste");
		tf.setWidth("100%");
		content.addComponent(tf);

		final TextField tf2 = createMapNameTextField();
		content.addComponent(tf2);
		
		final ComboBox combo = createMapTypeComboBox(database);
		content.addComponent(combo);

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button(Terminology.ADD);
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "340px", Terminology.ADD_LOWER_LEVEL_MAP_LABEL, Terminology.CANCEL, content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String id = tf.getValue();
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				StrategyMap uusi = database.newMap(null, id, name, type);
				uusi.addAlikartta(currentRoot);
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
		});
		
	}
	
	public static void print(final Main main) {

		VerticalLayout content = new VerticalLayout();

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
		
        final Button csv = new Button("Lataa CSV");
        csv.setStyleName(ValoTheme.BUTTON_SMALL);
        csv.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8882662527329536686L;

			@Override
			public void buttonClick(ClickEvent event) {
				csv.setEnabled(false);
			}
			
		});
        
		final Button pdf = new Button("Lataa PDF");
		pdf.setStyleName(ValoTheme.BUTTON_SMALL);
		pdf.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 5350320436995864012L;

			@Override
			public void buttonClick(ClickEvent event) {
				pdf.setEnabled(false);
			}
			
		});
		buttons.addComponent(csv);
		buttons.addComponent(pdf);
		
		final String svgText = main.mapEditorUI.pdf.svg;
		
		final OnDemandFileDownloader pdfDL = new OnDemandFileDownloader(new OnDemandStreamSource() {
			
			private static final long serialVersionUID = -3866190768030858133L;
			File temp;
			Date date = new Date();
			
			@Override
			public InputStream getStream() {
				
				String uuid = UUID.randomUUID().toString(); 
				
				File printing = main.getDatabase().getPrintingDirectory();
				
				temp = new File(printing, uuid + ".pdf");

				if(svgText != null) {

					File htmlFile = new File(printing, uuid + ".html");
					File script = new File(printing, uuid + ".js");
					
		        	try {
		        		
		        		String html = PhantomJSDriver.printHtml(svgText, DatabaseLoader.bootstrapFile("print.html").getParentFile().getAbsolutePath());
						Files.write(htmlFile.toPath(), html.getBytes(Charset.forName("UTF-8")));
		        		
						String browserUrl = htmlFile.toURI().toURL().toString();
		        		
			        	String printCommand = PhantomJSDriver.printCommand(browserUrl, temp.getAbsolutePath());
			        	
						Files.write(script.toPath(), printCommand.getBytes());
						
						PhantomJSDriver.execute(script);
						
						return new FileInputStream(temp);

		        	} catch (IOException e) {
						e.printStackTrace();
					}

				}
				
				throw new IllegalStateException();
				
			}
			
			@Override
			public void onRequest() {
			}
			
			@Override
			public long getFileSize() {
				return temp.length();
			}
			
			@Override
			public String getFileName() {
				return "Strategiakartta_" + UtilsDB.dateString(date) + ".pdf";
			}
			
		});
		
		final OnDemandFileDownloader csvDL = new OnDemandFileDownloader(new OnDemandStreamSource() {

			private static final long serialVersionUID = -1242999161589361930L;
			File temp;
			Date date = new Date();
			
			@Override
			public InputStream getStream() {
				String uuid = UUID.randomUUID().toString(); 
				File printing = main.getDatabase().getPrintingDirectory();
				temp = new File(printing, uuid + ".csv");

	        	String csv = MapTransformationUtils.mapToCSV(main.getDatabase(),
	        			main.getUIState().currentMap,
	        			main.getUIState().getTime(),
	        			",");
	        	
		        try {
		        	BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
		    	    bw.write(csv);
		    	    bw.close();
		    	    
		        	return new FileInputStream(temp);
		        } catch (IOException e) {
					e.printStackTrace();
				}
				
				throw new IllegalStateException();
			}

			@Override
			public String getFileName() {
				return UtilsDB.dateString(date) + ".csv";
			}

			@Override
			public long getFileSize() {
				return temp.length();
			}

			@Override
			public void onRequest() {
			}
			
		});
		
		csvDL.getResource().setCacheTime(0);
		csvDL.extend(csv);
		pdfDL.getResource().setCacheTime(0);
		pdfDL.extend(pdf);

		
		final Window dialog = Dialogs.makeDialog(main, "480px", "140px", "Missä muodossa haluat ladata kartan?", "Sulje", content, buttons);
		
	}

	public static void addView(final Main main, final StrategyMap map) {

		final Database database = main.getDatabase();

		FormLayout content = new FormLayout();
		content.setSizeFull();

		final TextField tf = new TextField();
		tf.setCaption("Näkymän tunniste:");
		tf.setValue("tunniste");
		tf.setWidth("100%");
		content.addComponent(tf);

		final TextField tf2 = new TextField();
		tf2.setCaption("Näkymän nimi:");
		tf2.setValue("Uusi näkymä");
		tf2.setWidth("100%");
		content.addComponent(tf2);

		final ComboBox combo = createMapTypeComboBox(database);
		content.addComponent(combo);
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));

		final ComboBox combo2 = new ComboBox();
		combo2.setCaption("Aihetunniste:");
		combo2.setNullSelectionAllowed(false);
		combo2.setWidth("100%");
		content.addComponent(combo2);
		
		for(Tag t : Tag.enumerate(database)) {
			combo2.addItem(t.uuid);
			combo2.setItemCaption(t.uuid, t.getId(database));
			combo2.select(t.uuid);
		}

		HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(true);
		
		Button ok = new Button(Terminology.ADD);
		buttons.addComponent(ok);

		final Window dialog = Dialogs.makeDialog(main, "450px", "380px", Terminology.ADD_VIEW_LABEL, Terminology.CANCEL, content, buttons);
		ok.addClickListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1422158448876521843L;

			public void buttonClick(ClickEvent event) {
				
				String id = tf.getValue();
				String name = tf2.getValue();
				String typeUUID = (String)combo.getValue();
				Base type = database.find(typeUUID);
				
				String tagUUID = (String)combo2.getValue();
				Tag tag = (Tag)database.find(tagUUID);
				
				StrategyMap newMap = database.newMap(main.uiState.currentMap, id, name + " (näkymä)", type);
				newMap.generators.add(tag);
				
				Updates.updateJS(main, true);
				main.removeWindow(dialog);

			}
			
		});
		
	}
	
	public static class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
	} 
	
	public static String extractDescription(String unescapedDescription) {
		Document doc = Jsoup.parse(unescapedDescription);
		
		Map<String, Tuple<String, String>> linkMap = new HashMap<>();
		
		for(Element element : doc.body().getAllElements()) {
			String prefix = element.tagName();
			if(prefix.equals("a")) {
				String target = SafeHtmlUtils.htmlEscape(element.toString());
				String linkName = element.html();
				String link = element.attr("href");
				if(link != null) {
					if(linkName == null) linkName = link;
					linkMap.put(target, new Tuple<String, String>(SafeHtmlUtils.htmlEscape(linkName), SafeHtmlUtils.htmlEscape(link)));
				}
			}
		}
		
		String escaped = SafeHtmlUtils.htmlEscape(unescapedDescription);
		for(String key : linkMap.keySet()) {
			Tuple<String, String> t = linkMap.get(key);
			String linkName = t.x;
			String linkTarget = t.y;
			String link = "<a href=\"" + linkTarget + "\" target=\"_blank\">" + linkName + "</a>";
			escaped = escaped.replaceAll(key, link);
		}
		return escaped;
	}
	
	public static boolean setProperty(Property property, Main main, Database database, Base b, String value) {
		Account account;
		if(main != null) {
			account = main.getAccountDefault();
		} else {
			account = database.getDefaultAdminAccount();
		}
		return property.set(main != null, database, account, b, value);
	}
	
	public static void editTextAndId(final Main main, String title, final MapBase container) {
		
		final Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		
        final Window subwindow = new Window(title, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("500px");
        subwindow.setResizable(false);

        VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        final TextField tf = new TextField();
        tf.setCaption("Lyhytnimi:");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setValue(container.getId(database));
        tf.setWidth("100%");
        winLayout.addComponent(tf);

        final TextArea ta = new TextArea();
        ta.setCaption("Teksti:");
        ta.setValue(container.getText(database));
        ta.setWidth("100%");
        ta.setHeight("290px");
        winLayout.addComponent(ta);

        Button save = new Button(Terminology.SAVE, new Button.ClickListener() {
        	
			private static final long serialVersionUID = 6641880870005364983L;

			public void buttonClick(ClickEvent event) {
            	String idValue = tf.getValue();
            	String value = ta.getValue();
            	main.removeWindow(subwindow);
            	
            	if(container instanceof StrategyMap) {
            		boolean ok = UtilsDB.renameStrategyMapAndRelatedObjects(database, (StrategyMap)container, account, idValue, value);
            		if(!ok) {
            			Label l = new Label("Tietokannassa löytyy käyttäjäryhmiä, joihin kartta ei voi kytkeytyä!"
            							+ " Voit poistaa käyttäjäryhmät nimeltä " + value + ", tai antaa kartalle uuden nimen!");
            			l.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
            			Dialogs.errorDialog(main, "Kartan nimeä ei voitu muokata!", l, "600px", "200px");
            		}
            	} else {
            		container.modifyId(database, account, idValue);
            		container.modifyText(database, account, value);
            	}
            	
            	Collection<String> tags = UtilsTags.extractTags(value);
            	database.assertTags(tags);
            	ArrayList<Tag> tagObjects = new ArrayList<Tag>();
            	for(String s : tags)
            		tagObjects.add(database.getOrCreateTag(s));
            	container.assertRelatedTags(database, tagObjects);
            	
        		Updates.update(main, true);
        		Property emails = Property.find(database, Property.EMAIL);
        		String addr = emails.getPropertyValue(container);
        		if(addr != null && !addr.isEmpty()) {
        			String[] addrs = addr.split(",");
        			if(addrs.length > 0) {
        				try {
							Email.send(addrs, "Muutos strategiakartassa: " + container.getId(database), "Käyttäjä " + main.account.getId(database) + " on muuttanut strategiakarttaa.<br/><br/>Lyhytnimi: " + container.getId(database) + "<br/><br/>Teksti: " + container.getText(database));
						} catch (MessagingException e) {
							e.printStackTrace();
						}
        			}
        		}
            }
            
        });
        
        Button discard = new Button(Terminology.CANCEL_CHANGES_LABEL, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -784522457615993823L;

            public void buttonClick(ClickEvent event) {
            	Updates.update(main, true);
            	main.removeWindow(subwindow);
            }
            
        });

    	HorizontalLayout hl2 = new HorizontalLayout();
    	hl2.setSpacing(true);
    	hl2.addComponent(save);
    	hl2.addComponent(discard);
    	winLayout.addComponent(hl2);
    	winLayout.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
		main.addWindow(subwindow);
        
		ta.setCursorPosition(ta.getValue().length());

	}

	public static Button tagButton(final Database database, String prefix, String tag, final int index) {

    	Tag t = database.getOrCreateTag(tag);

        Styles styles = Page.getCurrent().getStyles();

        styles.add(".fi_semantum_strategia div.v-button." + prefix + "tag"+ index + " { opacity: 1.0; box-shadow:none; color: #000000; background-image: linear-gradient(" + t.color + " 0%, " + t.color + " 100%); }");
    	
    	Button tagButton = new Button();
    	tagButton.setEnabled(false);
    	tagButton.setCaption(tag);
    	tagButton.addStyleName(prefix + "tag" + index);
    	
    	return tagButton;

	}
	
	public static void fillTagEditor(final Database database, final AbstractLayout layout, final List<String> tags, final boolean allowRemove) {

		layout.removeAllComponents();
		
        for(int i=0;i<tags.size();i++) {

        	String tag = tags.get(i);
        	
        	HorizontalLayout hl = new HorizontalLayout();

        	if(allowRemove) {
        		final int index = i;
            	Button b = new Button();
    	        b.addStyleName(ValoTheme.BUTTON_BORDERLESS);
            	b.setIcon(FontAwesome.TIMES_CIRCLE);
            	b.addClickListener(new ClickListener() {
            		
    				private static final long serialVersionUID = -4473258383318654850L;

    				@Override
    				public void buttonClick(ClickEvent event) {
    					tags.remove(index);
    					fillTagEditor(database, layout, tags, allowRemove);
    				}
    			});
            	hl.addComponent(b);
        	}
        	
        	Button tagButton = tagButton(database, "dialog", tag, i);
        	hl.addComponent(tagButton);
        	hl.setComponentAlignment(tagButton, Alignment.MIDDLE_LEFT);

        	layout.addComponent(hl);

        }

	}

	
	static class TagCombo extends ComboBox {
		
		private static final long serialVersionUID = 5930055496801663683L;
		
		String customFilterString;
		
		@Override
		public void changeVariables(Object source, Map<String, Object> variables) {
			super.changeVariables(source, variables);
			customFilterString = (String) variables.get("filter");
		}
		
	}
	
	public static void editTags(final Main main, String title, final MapBase container) {
		
		final Database database = main.getDatabase();

        final Window subwindow = new Window(title, new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("360px");
        subwindow.setResizable(true);

        VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        // Add some content; a label and a close-button
        final List<String> tags = new ArrayList<String>();
        for(Tag t : container.getRelatedTags(database))
        	tags.add(t.getId(database));

        final CssLayout vl = new CssLayout();
        vl.setCaption("Käytössä olevat aihetunnisteet:");
        fillTagEditor(database, vl, tags, Utils.canWrite(main, container));
        winLayout.addComponent(vl);

    	HorizontalLayout hl = new HorizontalLayout();
    	hl.setWidth("100%");
    	hl.setSpacing(true);
    	
    	final TagCombo combo= new TagCombo();
    	final CustomLazyContainer comboContainer = new CustomLazyContainer(database, combo, Tag.enumerate(database));
    	combo.setWidth("100%");
    	combo.setCaption("Uusi aihetunniste:");
    	combo.setInputPrompt("valitse listasta tai kirjoita");
    	combo.setFilteringMode(FilteringMode.STARTSWITH);
    	combo.setTextInputAllowed(true);
    	combo.setImmediate(true);
    	combo.setNullSelectionAllowed(false);
    	combo.setInvalidAllowed(true);
    	combo.setInvalidCommitted(true);
    	combo.setItemCaptionMode(ItemCaptionMode.PROPERTY);
    	combo.setItemCaptionPropertyId("id"); //should set
    	combo.setContainerDataSource(comboContainer);
    	
        hl.addComponent(combo);
    	hl.setExpandRatio(combo, 1.0f);
        
        Button add = new Button(Terminology.ADD, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -2848576385076605664L;

            public void buttonClick(ClickEvent event) {
            	String filter = (String)combo.getValue();
				if(filter != null && filter.length() > 0) {
					Tag t = database.getOrCreateTag(filter);
					if(tags.contains(t.getId(database))) return;
					tags.add(t.getId(database));
					fillTagEditor(database, vl, tags, main.account != null);
					combo.clear();
				}
            }
        });
        hl.addComponent(add);
    	hl.setComponentAlignment(add, Alignment.BOTTOM_LEFT);
    	hl.setExpandRatio(add, 0.0f);

        winLayout.addComponent(hl);

        Button close = new Button(Terminology.SAVE, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -451523776456589591L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            	List<Tag> newTags = new ArrayList<Tag>();
            	for(String s : tags)
            		newTags.add(database.getOrCreateTag(s));
            	container.setRelatedTags(database, newTags);
            	Updates.update(main, true);
            }
        });
        Button discard = new Button(Terminology.CANCEL_CHANGES_LABEL, new Button.ClickListener() {
        	
			private static final long serialVersionUID = -2387057110951581993L;

            public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
        });

    	HorizontalLayout hl2 = new HorizontalLayout();
    	hl2.setSpacing(true);
    	hl2.addComponent(close);
    	hl2.addComponent(discard);
    	winLayout.addComponent(hl2);
    	winLayout.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
    	
		main.addWindow(subwindow);
		
	}
	
	public static void loseFocus(Component parent) {
		while (parent != null) {
			if(parent instanceof Component.Focusable) {
				((Component.Focusable) parent).focus();
				break;
			} else {
				parent = parent.getParent();
			}
		}			
	}
	
//	public static boolean getManyImplementor(Database database, ObjectType type) {
//
//		Property p = Property.find(database, Property.MANY_IMPLEMENTOR);
//		return Boolean.parseBoolean(p.getPropertyValue(type));
//
//	}

//	public static ObjectType getOwnGoalType(Database database, Strategiakartta map) {
//		
//		Property levelProperty = Property.find(database, Property.LEVEL);
//		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
//		
//		Property goalTypeProperty = Property.find(database, Property.OWN_GOAL_TYPE);
//		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);
//		
//		return database.find(goalTypeUUID);
//
//	}
//
//	public static ObjectType getGoalType(Database database, Strategiakartta map) {
//		
//		Property levelProperty = Property.find(database, Property.LEVEL);
//		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
//		
//		Property goalTypeProperty = Property.find(database, Property.GOAL_TYPE);
//		String goalTypeUUID = goalTypeProperty.getPropertyValue(level);
//		
//		return database.find(goalTypeUUID);
//
//	}

	public static String describeDate(Date d) {
		return describeDate(new Date(), d);
	}

	public static String describeDate(Date now, Date d) {

		long diff = now.getTime()-d.getTime();
		String date = "";
		if(diff < 1000*60) {
			date = "Hetki sitten";
		} else if(diff < 1000*60*60) {
			date = "" + (diff/(1000*60)) + " min sitten";
		} else if(diff < 1000*60*60*24) {
			date = "" + (diff/(1000*60*60)) + " h sitten";
		} else if(diff < 1000*60*60*24*7) {
			date = "" + (diff/(1000*60*60*24)) + " päivää sitten";
		} else {
			date = UtilsDB.simpleDateFormat.format(d);
		}
		return date;
		
	}
	
	public static void modifyValidity(Main main, Base base, String newValidity) {
		Account account = main.getAccountDefault();
		Database database = main.getDatabase();
		UtilsDB.modifyValidity(database, account, base, newValidity);
	}
	
	/**
	 * Check if a provided new value is acceptable for Property p and Base base
	 * @param main
	 * @param p
	 * @param base
	 * @param value
	 * @return
	 */
	private static boolean propertyValueIsCorrect(Main main, Property p, MapBase base, Object value) {
		if(value == null) {
			return false;
		}
		
		final Property timeP = Property.find(main.getDatabase(), Property.AIKAVALI);
		if(p.uuid.equals(timeP.uuid)){
			TimeInterval ti = TimeInterval.parse(value.toString());
			String base_current_time = timeP.getPropertyValue(base);
			Database database = main.getDatabase();
			List<String> yearsS = UtilsDB.allAllowedYearsForBase(database, base, base_current_time);
			if(yearsS.contains(Property.AIKAVALI_KAIKKI)) {
				List<Integer> yearsI = TimeConfiguration.getInstance(database).getAllConfiguredYears();
				if(yearsI.size() == 0) {
					return false;
				}
				
				try {
					return ti.startYear >= yearsI.get(0) && 
							ti.endYear <= yearsI.get(yearsI.size() - 1);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return false;
				}
			} else {
				if(yearsS.size() == 0) {
					return false; //No years can be accepted if no years defined
				} else {
					try {
						return ti.startYear >= Integer.parseInt(yearsS.get(0)) && 
								ti.endYear <= Integer.parseInt(yearsS.get(yearsS.size() - 1));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return false;
					}
				}
			}
		}

		final Property ttlP = Property.find(main.getDatabase(), Property.TTL);
		
		if(p.uuid.equals(ttlP.uuid)) {
			try {
				Integer i = Integer.parseInt(value.toString());
				return i >= 0;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void updateProperties(final Main main, final Base base, boolean canWrite) {

		final Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		
		String headerText = main.getUIState().currentMapItem.getCaption(database);
		main.propertyCells.add(UtilsDB.excelRow(headerText));
		Label header = new Label(headerText);
		header.setWidth("800px");
		header.addStyleName("propertyHeader");
		header.addStyleName(ValoTheme.LABEL_HUGE);
		header.addStyleName(ValoTheme.LABEL_BOLD);
		main.properties.addComponent(header);
		main.properties.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

		ArrayList<Pair> sorted = new ArrayList<Pair>(main.getUIState().currentMapItem.properties);
		Collections.sort(sorted, new Comparator<Pair>() {

			@Override
			public int compare(Pair arg0, Pair arg1) {

				final Property p0 = database.find(arg0.first);
				final Property p1 = database.find(arg1.first);
				return p0.getId(database).compareTo(p1.getId(database));

			}

		});

		Property typeProperty = Property.find(database, Property.TYPE);

		for (Pair pair : sorted) {

			// Skip type
			if(typeProperty.uuid.equals(pair.first)) continue;

			final Property p = database.find(pair.first);
			String value = pair.second;
			final HorizontalLayout hl = new HorizontalLayout();
			hl.setSpacing(true);
			String label = p.getText(database);
			main.propertyCells.add(UtilsDB.excelRow(label, value));
			Label l = new Label(label);
			l.setWidth("450px");
			l.addStyleName("propertyName");
			hl.addComponent(l);
			List<String> enumeration = p.getEnumeration(database);
			if (enumeration.isEmpty()) {
				final TextField tf = new TextField();
				tf.setValue(value);
				tf.setWidth("350px");
				hl.addComponent(tf);
				hl.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
				tf.setReadOnly(p.readOnly);
				tf.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = 7729833503749464603L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						String newValue = tf.getValue();

						if(propertyValueIsCorrect(main, p, main.getUIState().currentMapItem, newValue)) {
							Utils.loseFocus(hl);
							if(p.set(main != null, database, account, main.getUIState().currentMapItem, newValue)) {
								Updates.update(main, true);
							}
						} else {
							tf.setValue(value); //Reroll to accepted value
							Label content_ = new Label();
							String error_text = p.getText(database) + " ominaisuudelle annettua arvoa ei hyväksytty!";
							Dialogs.errorDialog(main, error_text, content_, "700px", "100px");
						}
						

					}
				});
				tf.setReadOnly(!canWrite);
			} else {
				final ComboBox combo = new ComboBox();
				combo.setWidth("350px");
				combo.setInvalidAllowed(false);
				combo.setNullSelectionAllowed(false);
				for (String e : enumeration) {
					combo.addItem(e);
				}
				combo.select(p.getEnumerationValue(database, value));
				combo.setPageLength(0);
				combo.addValueChangeListener(new ValueChangeListener() {

					private static final long serialVersionUID = 3511164709346832901L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						Utils.loseFocus(hl);
						if(p.set(main != null, database, account, main.getUIState().currentMapItem, combo.getValue().toString()))
							Updates.update(main, true);
					}
				});
				combo.setReadOnly(!canWrite);
				hl.addComponent(combo);
				hl.setComponentAlignment(combo, Alignment.MIDDLE_LEFT);
			}
			hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
			main.properties.addComponent(hl);
			main.properties.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
		}

	}
	
	public static ResultAgreementDocuments getPossibleRootDocumentFromChapter(Database database, TextChapter chapter) {
		Collection<Base> resultB = chapter.getRelatedObjects(database, Relation.find(database, Relation.CONTAINS_CHAPTER_INVERSE));

		if(resultB.size() == 1) {
			return (ResultAgreementDocuments)resultB.iterator().next();
		} else if(resultB.size() == 0) {
			return null;
		} else {
			System.err.println("Incorrect number of documents for text chapter found! Expected 1 or 0, got " + resultB.size() + ". Chapter uuid: " + chapter.uuid);
			return null;
		}
	}

	
	/**
	 * Add read access to the Base element for entire account group that matches the office
	 * @param main
	 * @param comment
	 * @param office
	 * return if addition succeeded
	 */
	public static boolean addReadRightToOffice(Main main, Base b, Office office) {
		boolean ok = UtilsDB.addReadRightToOffice(main.getDatabase(), b, office);
		Updates.update(main, true);
		return(ok);
	}
	
	/**
	 * Get a list of ordered rights with NULL separators to indicate where special rights, map rights and the rest of rights end.
	 * @param rights
	 * @return
	 */
	public static List<AccessRight> getOrderedRightsWithNullSeparators(List<AccessRight> rights){
		List<AccessRight> result = new ArrayList<>(rights.size()+2);
		
		List<AccessRight> specialRights = new ArrayList<>();
		List<AccessRight> mapRights = new ArrayList<>();
		List<AccessRight> rest = new ArrayList<>();
		
		for(AccessRight right : rights) {
			if(right instanceof MapRight) {
				mapRights.add(right);
			} else {
				if(isSpecialRight(right)) {
					specialRights.add(right);
				} else {
					rest.add(right);
				}
			}
		}
		
		result.addAll(specialRights);
		result.add(null); //A separator AccessRight
		result.addAll(mapRights);
		result.add(null); //A separator AccessRight
		result.addAll(rest);
		return result;
	}
	
	private static boolean isSpecialRight(AccessRight right) {
		Base base = right.base;
		if(base != null) {
			if(base instanceof CommentToolAdminBase) return true;
			if(base instanceof CommentToolBase) return true;
			if(base instanceof ResultAgreementDocuments) return true;
			if(base instanceof ResultAgreementToolAdminBase) return true;
		}
		return false;
	}
	
	public static void removePossibleDocumentAndMapRights(Database database, AccountGroup group, AccessRight right) {
		if(right.base instanceof ResultAgreementDocuments) {
			StrategyMap map = UtilsDB.getPossibleLinkedMap(database, (ResultAgreementDocuments)right.base);
			List<ResultAgreementDocuments> allDocs = UtilsDB.getAllLinkedResultAgreementDocuments(database, map);
			if(allDocs != null) {
				for(ResultAgreementDocuments b : allDocs) {
					group.removeRightIfExists(b, right.write);
				}
			}
			if(map != null) {
				group.removeRightIfExists(map, right.write);
			}
		} else if(right.base instanceof StrategyMap) {
			List<ResultAgreementDocuments> allDocs = UtilsDB.getAllLinkedResultAgreementDocuments(database, (StrategyMap)right.base);
			if(allDocs != null) {
				for(ResultAgreementDocuments b : allDocs) {
					group.removeRightIfExists(b, right.write);
				}
			}
		} else {
			//Don't do anything - the AccessRight was not a Doc or a Map and thus we don't need to remove any linked rights
		}
	}
	
	public static void addRightToAllLinkedElements(Database database, AccountGroup group, AccessRight right) {
		group.addRightIfMissing(right);
		addRightToRelatedElements(database, group, Relation.find(database, Relation.LINKED_TO), right);
	}
	
	private static void addRightToRelatedElements(Database database, AccountGroup group, Relation relation, AccessRight right) {
		Collection<Base> relatedElement = right.base.getRelatedObjects(database, relation);
		for(Base b : relatedElement) {
			System.out.println("Adding AccessRight to group " + group.text + " to " + b.text);
			group.addRightIfMissing(new AccessRight(b, right.write));
		}
	}
	
	public static String getCurrentYear(Main main) {
		String zone = main.getAccountDefault().zone;
		Date date = new Date();
		LocalDate localDate = date.toInstant().atZone(ZoneId.of(zone)).toLocalDate();
		Integer year = localDate.getYear();
		return year.toString();
	}
	
	public static String eventColorToLabelStyle(EventColor color) {
		return "yearClockEventLabel-" + color.name().toLowerCase();
	}
	
	public static String translateColorLanguage(EventColor color) {
		switch(color) {
			case RED: return "Punainen";
			case GREEN: return "Vihreä";
			case BLUE: return "Sininen";
			case GREY: return "Harmaa";
			case PURPLE: return "Lila";
			case ORANGE: return "Oranssi";
			case YELLOW: return "Keltainen";
			case NONE: return "Ei väriä";
			default: return "(väriä ei löydy)";
		}
	}
	
	
	public static boolean isActive(Database database, UIState uistate, Base b) {
		return UtilsDB.isActive(database, uistate.getTime(), b);
	}
	
	public static boolean canRead(Main main, List<MapBase> path) {
		MapBase last = path.get(path.size()-1);
		for(AccountGroup group : UtilsDB.getAllAssociatedAccountGroups(main.getDatabase(), main.getAccountDefault())) {
			if(group.canRead(main.getDatabase(), last)) return true;
		}
		return false;
	}
	
	public static boolean canRead(Main main, Base b) {
		return UtilsDB.canRead(main.getDatabase(), main.getAccountDefault(), b);
	}

	/**
	 * Check if the currently logged in user is allowed to read the Base b
	 * @param main
	 * @param b
	 * @return
	 */
	public static boolean canWrite(Main main, Base b) {
		for(AccountGroup group : UtilsDB.getAllAssociatedAccountGroups(main.getDatabase(), main.getAccountDefault())) {
			if(group.canWrite(main.getDatabase(), b)) return true;
		}
		return false;
	}

	public static StrategyMap resolveMap(Database database, Base b) {
		if(b instanceof StrategyMap) {
			return (StrategyMap)b;
		} else if (b instanceof OuterBox) {
			OuterBox ob = (OuterBox)b;
			try {
				StrategyMap map = ob.getPossibleImplementationMap(database);
				if(map != null) return map;
			} catch (Exception e) {
			}
		} else if (b instanceof InnerBox) {
			InnerBox ib = (InnerBox)b;
			OuterBox ob = ib.getPossibleImplementationGoal(database);
			if(ob != null) return resolveMap(database, ob);
		}
		return null;
	}
	
}






