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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.WebContent;
import fi.semantum.strategia.widget.TrafficValuation;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.CommentCallback;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Datatype;
import fi.semantum.strategy.db.EnumerationDatatype;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.MeterDescription;
import fi.semantum.strategy.db.MeterSpec;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;

/**
 * 
 * Class for manipulating Meter base objects
 */
public class UtilsMeters {
	
	public static Map<MapBase,List<Meter>> metersByParent(Database database, Collection<Meter> meters) {
		Map<MapBase,List<Meter>> result = new HashMap<MapBase,List<Meter>>();
		for(Meter m : meters) {
			MapBase owner = m.getOwner(database);
			List<Meter> exist = result.get(owner);
			if(exist == null) {
				exist = new ArrayList<Meter>();
				result.put(owner, exist);
			}
			exist.add(m);
		}
		return result;
	}
	
	public static void manageMeters(final Main main, final MapBase base) {

		String currentTime = main.getUIState().getTime(); 
		boolean showYears = currentTime.equals(Property.AIKAVALI_KAIKKI);

		final Database database = main.getDatabase();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        final Table table = new Table();
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.addStyleName(ValoTheme.TABLE_SMALL);
        table.addStyleName(ValoTheme.TABLE_COMPACT);

        table.addContainerProperty(Terminology.METER, Label.class, null);
        if(showYears)
        	table.addContainerProperty("Vuosi", String.class, null);
        
        table.setWidth("100%");
        table.setHeight("100%");
        table.setNullSelectionAllowed(true);

		table.setEditable(false);
		table.setColumnExpandRatio(Terminology.METER, 2.0f);
		if(showYears)
			table.setColumnExpandRatio("Vuosi", 0.0f);

        makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
        
        content.addComponent(table);
        content.setExpandRatio(table, 1.0f);

        abstract class MeterButtonListener implements Button.ClickListener {

			private static final long serialVersionUID = -6640950006518632633L;
			
			protected Meter getPossibleSelection() {
    			Object selection = table.getValue();
    			Collection<?> selected = (Collection<?>)selection;
    			if(selected.size() != 1) return null;
    			return (Meter)selected.iterator().next();
        	}
			
			@SuppressWarnings("unchecked")
			protected Collection<Meter> getSelection() {
    			return (Collection<Meter>)table.getValue();
			}

			protected  Map<MapBase,List<Meter>> getSelectionByParent(Database database) {
				return UtilsMeters.metersByParent(database, getSelection());
			}
        	
        }

		final Button removeMeters = new Button("Poista", new MeterButtonListener() {

			private static final long serialVersionUID = 2957964892664902859L;

			public void buttonClick(ClickEvent event) {
				
				for(Meter r : getSelection()) {
					MapBase owner = r.getOwner(database);
					if(owner != null)
						owner.removeMeter(r);
				}
				
				UtilsMeters.makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
				Updates.update(main, true);

			}

		});
		removeMeters.addStyleName(ValoTheme.BUTTON_TINY);

		final Button moveUp = new Button("Siirr‰ ylemm‰s", new MeterButtonListener() {

			private static final long serialVersionUID = 8434251773337788784L;

			public void buttonClick(ClickEvent event) {

				Map<MapBase,List<Meter>> sel = getSelectionByParent(database);
				if(sel == null) return;
				
				for(Map.Entry<MapBase, List<Meter>> entry : sel.entrySet()) {
					entry.getKey().moveMetersUp(entry.getValue());
				}
				
				UtilsMeters.makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
				Updates.update(main, true);

			}

		});
		moveUp.addStyleName(ValoTheme.BUTTON_TINY);

		final Button moveDown = new Button("Siirr‰ alemmas", new MeterButtonListener() {

			private static final long serialVersionUID = -5382367112305541842L;

			public void buttonClick(ClickEvent event) {

				for(Map.Entry<MapBase, List<Meter>> entry : getSelectionByParent(database).entrySet()) {
					entry.getKey().moveMetersDown(entry.getValue());
				}
				
				UtilsMeters.makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
				Updates.update(main, true);

			}

		});
		moveDown.addStyleName(ValoTheme.BUTTON_TINY);

		final Button modify = new Button("M‰‰rit‰");
		modify.addClickListener(new MeterButtonListener() {
			
			private static final long serialVersionUID = -7109999546516429095L;

			public void buttonClick(ClickEvent event) {

				Meter meter = getPossibleSelection();
				if(meter == null) return;
				
				UtilsMeters.editMeter(main, base, meter);

			}

		});
		modify.addStyleName(ValoTheme.BUTTON_TINY);
		
		final ComboBox indicatorSelect = new ComboBox();
		indicatorSelect.setWidth("100%");
		indicatorSelect.setNullSelectionAllowed(false);
		indicatorSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		indicatorSelect.setCaption("M‰‰ritt‰j‰");
		final StrategyMap map = database.getMap(base);
		
		// Indikaattorit
		for(Indicator i : map.getIndicators(database)) {
			MeterSpec spec = new MeterSpec(database, i);
			indicatorSelect.addItem(spec);
			indicatorSelect.select(spec);
		}
		// Enumeraatiot
		for(Datatype enu : Datatype.enumerate(database)) {
			if(enu instanceof EnumerationDatatype) {
				MeterSpec spec = new MeterSpec(database, enu);
				indicatorSelect.addItem(spec);
				indicatorSelect.select(spec);
			}
		}
		// Sis‰‰nrakennetut
		{
			MeterSpec spec = new MeterSpec(database, MeterSpec.IMPLEMENTATION);
			indicatorSelect.addItem(spec);
			indicatorSelect.select(spec);
		}
		
		indicatorSelect.setTextInputAllowed(false);

		final Button addMeter = new Button("Lis‰‰ p‰‰tasolle", new Button.ClickListener() {

			private static final long serialVersionUID = -5178621686299637238L;

			public void buttonClick(ClickEvent event) {
				
				MeterSpec spec = (MeterSpec)indicatorSelect.getValue();
				Object source = spec.getSource();
				if(source instanceof Indicator) {
					Indicator ind = (Indicator)source;
					UtilsMeters.addIndicatorMeter(main, base, ind, Property.AIKAVALI_KAIKKI);
				} else if (source instanceof EnumerationDatatype) {
					EnumerationDatatype dt = (EnumerationDatatype)source;
					Indicator ind = Indicator.create(database, "Uusi " + dt.getId(database), dt);
					UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultValue(), false, "", "Alkuarvo");
					UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultForecast(), true, "", "Alkuarvo");
					UtilsMeters.addIndicatorMeter(main, base, ind, Property.AIKAVALI_KAIKKI);
				}
				
				UtilsMeters.makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
				Updates.update(main, true);

			}

		});
		addMeter.addStyleName(ValoTheme.BUTTON_TINY);

		final Button addSubmeter = new Button("Lis‰‰ valitun alle", new MeterButtonListener() {

			private static final long serialVersionUID = -1250285092312682737L;

			public void buttonClick(ClickEvent event) {

				Meter meter = getPossibleSelection();
				if(meter == null) return;

				MeterSpec spec = (MeterSpec)indicatorSelect.getValue();
				Object source = spec.getSource();
				if(source instanceof Indicator) {
					Indicator ind = (Indicator)source;
					UtilsMeters.addIndicatorMeter(main, meter, ind, Property.AIKAVALI_KAIKKI);
				} else if (source instanceof EnumerationDatatype) {
					EnumerationDatatype dt = (EnumerationDatatype)source;
					Indicator ind = Indicator.create(database, "Uusi " + dt.getId(database), dt);
					UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultValue(), false, "", "Alkuarvo");
					UtilsIndicators.updateIndicator(main, ind, base, dt.getDefaultForecast(), true, "", "Alkuarvo");
					UtilsMeters.addIndicatorMeter(main, meter, ind, Property.AIKAVALI_KAIKKI);
				}
				
				UtilsMeters.makeMeterTable(main.getDatabase(), main.getUIState(), base, table);
				Updates.update(main, true);

			}

		});
		addSubmeter.addStyleName(ValoTheme.BUTTON_TINY);

		final Runnable setStates = new Runnable() {

			@Override
			public void run() {
				
				Object selection = table.getValue();
				Collection<?> selected = (Collection<?>)selection;
				if(!selected.isEmpty()) {
					removeMeters.setEnabled(true);
					moveUp.setEnabled(true);
					moveDown.setEnabled(true);
					if(selected.size() == 1) {
						modify.setEnabled(true);
						addSubmeter.setEnabled(true);
					} else {
						addSubmeter.setEnabled(false);
						modify.setEnabled(false);
					}
				} else {
					moveUp.setEnabled(false);
					moveDown.setEnabled(false);
					removeMeters.setEnabled(false);
					addSubmeter.setEnabled(false);
					modify.setEnabled(false);
				}
				
			}
			
		};
		
		table.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				setStates.run();
			}
			
		});
		
		setStates.run();
        
        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        hl2.setWidthUndefined();

        hl2.addComponent(modify);
        hl2.setComponentAlignment(modify, Alignment.TOP_LEFT);
        hl2.setExpandRatio(modify, 0.0f);

        hl2.addComponent(removeMeters);
        hl2.setComponentAlignment(removeMeters, Alignment.TOP_LEFT);
        hl2.setExpandRatio(removeMeters, 0.0f);

        hl2.addComponent(moveUp);
        hl2.setComponentAlignment(moveUp, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveUp, 0.0f);

        hl2.addComponent(moveDown);
        hl2.setComponentAlignment(moveDown, Alignment.TOP_LEFT);
        hl2.setExpandRatio(moveDown, 0.0f);

        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);
        hl3.setWidth("100%");
        
        hl3.addComponent(addMeter);
        hl3.setComponentAlignment(addMeter, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addMeter, 0.0f);

        hl3.addComponent(addSubmeter);
        hl3.setComponentAlignment(addSubmeter, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(addSubmeter, 0.0f);

        hl3.addComponent(indicatorSelect);
        hl3.setComponentAlignment(indicatorSelect, Alignment.BOTTOM_LEFT);
        hl3.setExpandRatio(indicatorSelect, 1.0f);

        content.addComponent(hl2);
        content.setComponentAlignment(hl2, Alignment.MIDDLE_CENTER);
        content.setExpandRatio(hl2, 0.0f);

        content.addComponent(hl3);
        content.setComponentAlignment(hl3, Alignment.BOTTOM_LEFT);
        content.setExpandRatio(hl3, 0.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        final Window dialog = Dialogs.makeDialog(main, "450px", "600px", "Hallitse mittareita", "Sulje", content, buttons);

	}

	public static void editMeter(final Main main, final Base base, final Meter meter) {

		Database database = main.getDatabase();

		final VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setHeightUndefined();
        content.setSpacing(true);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setMargin(false);

		final TextField tf = new TextField();
		tf.setCaption("Lyhytnimi");
		tf.setValue(meter.getId(database));
		tf.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf.setWidth("100%");
		
		hl.addComponent(tf);
		hl.setComponentAlignment(tf, Alignment.TOP_CENTER);
		hl.setExpandRatio(tf, 1.0f);
		
		final TextField tf1 = new TextField();
		tf1.setCaption("Teksti");
		tf1.setValue(meter.getText(database));
		tf1.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf1.setWidth("100%");
		
		hl.addComponent(tf1);
		hl.setComponentAlignment(tf1, Alignment.TOP_CENTER);
		hl.setExpandRatio(tf1, 2.0f);

		content.addComponent(hl);
		content.setComponentAlignment(hl, Alignment.TOP_CENTER);
		content.setExpandRatio(hl, 0.0f);

		final TextField tf2 = new TextField();
		tf2.setCaption("Voimassaolo");
		tf2.setValue(UtilsDB.getValidity(database, meter));
		tf2.addStyleName(ValoTheme.TEXTFIELD_TINY);
		tf2.setWidth("100%");
		
		content.addComponent(tf2);
		content.setComponentAlignment(tf2, Alignment.TOP_CENTER);
		content.setExpandRatio(tf2, 0.0f);

		final TextArea ta = new TextArea();
		ta.setCaption("M‰‰ritys");
		ta.setValue(meter.getText(database));
		ta.addStyleName(ValoTheme.TEXTAREA_TINY);
		ta.setHeight("100%");
		ta.setWidth("100%");

		content.addComponent(ta);
		content.setComponentAlignment(ta, Alignment.TOP_CENTER);
		content.setExpandRatio(ta, 1.0f);
        
        final TrafficValuation valuation = UtilsDataTypes.getTrafficValuation(database, meter);
        final Runnable onOK = valuation != null ? UtilsDataTypes.createTrafficValuationEditor(valuation, content, main, meter) : null;

        Indicator indicator = meter.getPossibleIndicator(database);
        if(indicator != null) {
	        final Label ta2 = UtilsIndicators.makeHistory(database, indicator, main.getUIState().forecastMeters);     
			content.addComponent(ta2);
	        content.setComponentAlignment(ta2, Alignment.MIDDLE_CENTER);
	        content.setExpandRatio(ta2, 1.0f);
        }

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);

        Button ok = new Button(Terminology.SAVE, new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1992235622970234624L;

            public void buttonClick(ClickEvent event) {
            	Account account = main.getAccountDefault();
            	if(onOK != null) onOK.run();
            	meter.modifyId(database, account, tf.getValue());
            	meter.modifyText(database, account, tf1.getValue());
            	Utils.modifyValidity(main, meter, tf2.getValue());
            	meter.modifyDescription(database, account, ta.getValue());
				Updates.update(main, true);
				UtilsMeters.manageMeters(main, main.getUIState().currentMapItem);
            }
            
        });
        buttons.addComponent(ok);

        Button close = new Button("Sulje");
        buttons.addComponent(close);
        
        final Window dialog = Dialogs.makeDialog(main, "500px", "800px", "M‰‰rit‰ mittaria", null, content, buttons);
        close.addClickListener(new Button.ClickListener() {
        	
			private static final long serialVersionUID = -8065367213523520602L;

			public void buttonClick(ClickEvent event) {
    			main.removeWindow(dialog);
				UtilsMeters.manageMeters(main, main.getUIState().currentMapItem);
            }
            
        });

	}
	
	public static void setUserMeter(final Main main, final Base base, final Meter m) {

		final Database database = main.getDatabase();

        final Window subwindow = new Window("Aseta mittarin arvo", new VerticalLayout());
        subwindow.setModal(true);
        subwindow.setWidth("350px");
        subwindow.setResizable(false);

        final VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        String caption = m.getCaption(database);
        if(caption != null && !caption.isEmpty()) {
            final Label header = new Label(caption);
            header.addStyleName(ValoTheme.LABEL_LARGE);
            winLayout.addComponent(header);
        }
        
		final Indicator indicator = m.getPossibleIndicator(database);
		if(indicator == null) return;
		
		Datatype dt = indicator.getDatatype(database);
		if(!(dt instanceof EnumerationDatatype)) return;

		final Label l = new Label(Terminology.DEFINITION_OR_GUIDE + ": " + indicator.getValueShortComment());

		AbstractField<?> forecastField = UtilsDataTypes.createDatatypeEditor(main, dt, base, indicator, true, new CommentCallback() {
			
			@Override
			public void runWithComment(String shortComment, String comment) {
				l.setValue(Terminology.DEFINITION_OR_GUIDE + ": " + indicator.getValueShortComment());
			}
			
			@Override
			public void canceled() {
			}
			
		});
		forecastField.setWidth("100%");
		forecastField.setCaption(Terminology.FORECAST);
		winLayout.addComponent(forecastField);
		
		AbstractField<?> currentField = UtilsDataTypes.createDatatypeEditor(main, dt, base, indicator, false, new CommentCallback() {
			
			@Override
			public void runWithComment(String shortComment, String comment) {
				l.setValue(Terminology.DEFINITION_OR_GUIDE + ": " + indicator.getValueShortComment());
			}
			
			@Override
			public void canceled() {
			}
			
		});
		currentField.setWidth("100%");
		currentField.setCaption(Terminology.ACTUALIZATION);
		winLayout.addComponent(currentField);
		
		winLayout.addComponent(l);
		
		l.setWidth("100%");
		winLayout.setComponentAlignment(l, Alignment.BOTTOM_CENTER);
        
        HorizontalLayout hl = new HorizontalLayout();
		winLayout.addComponent(hl);
		winLayout.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);

        Button ok = new Button("Sulje", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1364802814012491490L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(subwindow);
            }
            
        });
        
        Button define = new Button("M‰‰rit‰", new Button.ClickListener() {
        	
			private static final long serialVersionUID = 1364802814012491490L;

			public void buttonClick(ClickEvent event) {
				UtilsMeters.editMeter(main, base, m);
            }
            
        });

        hl.addComponent(ok);
		hl.setComponentAlignment(ok, Alignment.BOTTOM_LEFT);
        hl.addComponent(define);
		hl.setComponentAlignment(define, Alignment.BOTTOM_LEFT);

		main.addWindow(subwindow);
		
	}
	
	public static void updateMeters(final Main main, boolean canWrite) {

		if(main.getUIState().currentMapItem instanceof StrategyMap) return;

		final Database database = main.getDatabase();
		
		final MapBase base = main.getUIState().currentMapItem;
		
		List<MeterDescription> descs = makeMeterDescriptions(database, main.getUIState(), base, false);
		if(!descs.isEmpty() || canWrite) {

			HorizontalLayout meterHeader = new HorizontalLayout();
			meterHeader.setSpacing(true);

			Label header = new Label(Terminology.METERS);
			main.propertyCells.add(UtilsDB.excelRow(header.getValue()));
			header.setHeight("32px");
			header.addStyleName(ValoTheme.LABEL_HUGE);
			header.addStyleName(ValoTheme.LABEL_BOLD);
			meterHeader.addComponent(header);
			meterHeader.setComponentAlignment(header, Alignment.BOTTOM_CENTER);

			if(canWrite) {

				final Button editMeters = new Button();
				editMeters.setStyleName(ValoTheme.BUTTON_SMALL);
				editMeters.setIcon(new ThemeResource(WebContent.CHART_BAR_EDIT_PNG));
				editMeters.addClickListener(new ClickListener() {

					private static final long serialVersionUID = 7572381074458759054L;

					@Override
					public void buttonClick(ClickEvent event) {
						Utils.loseFocus(editMeters);
						manageMeters(main, main.getUIState().currentMapItem);
					}
				});

				meterHeader.addComponent(editMeters);
				meterHeader.setComponentAlignment(editMeters, Alignment.BOTTOM_CENTER);
			}

			main.properties.addComponent(meterHeader);
			main.properties.setComponentAlignment(meterHeader, Alignment.MIDDLE_CENTER);

			VerticalLayout meters = new VerticalLayout();				

			boolean showYears = main.getUIState().getTime().equals(Property.AIKAVALI_KAIKKI);

			Property time = Property.find(database, Property.AIKAVALI);

			int index = 0;
			for (final MeterDescription desc : descs) {
				
				ArrayList<String> excelRow = new ArrayList<String>();

				final Meter meter = desc.meter;

				final HorizontalLayout hl = new HorizontalLayout();
				hl.addStyleName((((index++)&1) == 0) ? "evenProperty" : "oddProperty");
				hl.setSpacing(true);
				
				Label l = new Label(desc.caption);
				excelRow.add(l.getValue().replace("%nbsp",""));
				l.setContentMode(ContentMode.HTML);
				l.setWidth("450px");
				l.addStyleName("propertyName");
				l.setData(desc);
				hl.addComponent(l);
				hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);

				String value = updateMeterValue(main, hl, base, meter, canWrite);
				excelRow.add(value);

				String shortComment = "";
				Indicator indicator = meter.getPossibleIndicator(database);
				if(indicator != null) shortComment = indicator.getValueShortComment();
				Label comment = new Label(shortComment);
				comment.setWidth("150px");
				hl.addComponent(comment);
				hl.setComponentAlignment(comment, Alignment.MIDDLE_LEFT);
				excelRow.add(comment.getValue());

				if(showYears) {

					HorizontalLayout hl2 = new HorizontalLayout();
					hl2.setWidth("70px");
					hl2.setHeight("100%");
					hl.addComponent(hl2);
					hl.setComponentAlignment(hl2, Alignment.MIDDLE_LEFT);

					String years = time.getPropertyValue(meter);
					if(years == null) years = Property.AIKAVALI_KAIKKI;

					final Label region = new Label(years);
					region.setWidthUndefined();

					excelRow.add(region.getValue());

					hl2.addComponent(region);
					hl2.setComponentAlignment(region, Alignment.MIDDLE_CENTER);

				}

				AbsoluteLayout image = new AbsoluteLayout();
				image.setWidth("32px");
				image.setHeight("32px");
				image.addStyleName("meterColor" + index);

				String color = UtilsMeters.getTrafficColor(meter, database);
				Styles styles = Page.getCurrent().getStyles();
		        styles.add(".fi_semantum_strategia div." + "meterColor"+ index + " { background: " + color + "; }");

				hl.addComponent(image);
				hl.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
				hl.setExpandRatio(image, 0.0f);
				
				meters.addComponent(hl);
				meters.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);

				ThemeResource res = desc.meter.isShowInMap() ? new ThemeResource(WebContent.ZOOM_PNG) : new ThemeResource(WebContent.ZOOM_OUT_PNG); 
				
				final Image show = new Image();
				show.setSource(res);
				show.setHeight("24px");
				show.setWidth("24px");
				if(canWrite) {
					show.setDescription("Klikkaamalla voit valita, n‰ytet‰‰nkˆ mittaria strategiakartassa");
					show.addClickListener(new MouseEvents.ClickListener() {
	
						private static final long serialVersionUID = 7156984656942915939L;
	
						@Override
						public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
							desc.meter.setShowInMap(!desc.meter.isShowInMap());
							Updates.update(main, true);
						}
	
					});
				}

				hl.addComponent(show);
				hl.setComponentAlignment(show, Alignment.MIDDLE_CENTER);

				if(Wiki.exists()) {
					final Image wiki = new Image();
					wiki.setSource(new ThemeResource(WebContent.TABLE_EDIT_PNG));
					wiki.setHeight("24px");
					wiki.setWidth("24px");
					wiki.setDescription("Klikkaamalla voit siirty‰ tausta-asiakirjaan");
					wiki.addClickListener(new MouseEvents.ClickListener() {
	
						private static final long serialVersionUID = 7156984656942915939L;
	
						@Override
						public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
							Wiki.openWiki(main, desc.meter);
						}
	
					});
	
					hl.addComponent(wiki);
					hl.setComponentAlignment(wiki, Alignment.MIDDLE_CENTER);
				}
				
				if(canWrite) {
					final Button principalButton = new Button();
					if(meter.isPrincipal) {
						principalButton.setCaption("Poista kokonaisarvio");
					} else {
						principalButton.setCaption("Aseta kokonaisarvioksi");
					}
					principalButton.setStyleName(ValoTheme.BUTTON_TINY);
					principalButton.addClickListener(new ClickListener() {

						private static final long serialVersionUID = 8247560202892661226L;

						@Override
						public void buttonClick(ClickEvent event) {
							if(meter.isPrincipal) {
								meter.isPrincipal = false;
							} else {
								for(Meter m : base.getMeters(database)) m.isPrincipal = false;
								meter.isPrincipal = true;
							}
							Updates.update(main, true);
							
						}

					});
					hl.addComponent(principalButton);
					hl.setComponentAlignment(principalButton, Alignment.MIDDLE_CENTER);
				}
				
				main.propertyCells.add(excelRow);

			}
			
			meters.addLayoutClickListener(new LayoutClickListener() {
				
				private static final long serialVersionUID = 3295743025581923380L;

				private String extractData(Component c) {
					if(c instanceof AbstractComponent) {
						Object data = ((AbstractComponent)c).getData();
						if(data instanceof MeterDescription) {
							MeterDescription desc = (MeterDescription)data;
							return UtilsMeters.getDescription(desc.meter, database, true);
						}
					}
					return null;
				}
				
				@Override
				public void layoutClick(LayoutClickEvent event) {
				
					String desc = extractData(event.getClickedComponent());
					if(desc == null) return;
					
					String content = "<div style=\"width: 700px; border: 2px solid; padding: 5px\">";
					content += "<div style=\"text-align: center; white-space:normal; font-size: 36px; padding: 10px\">" + desc + "</div>";
					content += "</div>";

					Notification n = new Notification(content, Notification.Type.HUMANIZED_MESSAGE);
					n.setHtmlContentAllowed(true);
					n.show(Page.getCurrent());
					
				}
				
			});
			
			main.properties.addComponent(meters);
			main.properties.setComponentAlignment(meters, Alignment.MIDDLE_CENTER);

		}

	}
	

	public static double value(Meter meter, Database database) {
		return value(meter, database, true);
	}

	public static  double value(Meter meter, Database database, boolean forecast) {

		if(meter.isUserDefined()) return meter.getUserValue();

		Indicator indicator = meter.getPossibleIndicator(database);
		if(indicator != null) {
			String color = UtilsMeters.getTrafficColor(meter, database, forecast);
			if(Datatype.GREEN.equals(color)) return 1.0;
			else if(Datatype.YELLOW.equals(color)) return 0.5;
			else return 0.0;
		}

		double result = 0.0;
		int contributions = 0;
		Collection<Meter> submeters = meter.getSubmeters(database);
		if(submeters.isEmpty()) return 0.0;
		
		for (Meter m : submeters) {
			result += UtilsMeters.value(m, database, forecast);
			contributions++;
		}
		
		if(contributions > 0)
			result /= contributions;
		
		return result;
		
	}


	public static String explain(Meter meter, Database database) {

		Indicator i = meter.getPossibleIndicator(database);
		if(i != null)
			return "" + i.getValue();
		else
			return "" + (int)(100.0 * UtilsMeters.value(meter, database)) + "%";

	}
	
	public static String describe(Meter meter, Database database, boolean forecast) {

		Indicator i = meter.getPossibleIndicator(database);
		if(i != null) {
			Object value = i.getValue(forecast);
			if(value == null) return "arvoa ei ole annettu";
			Datatype datatype = i.getDatatype(database);
			return datatype.format(value) + " " + i.getUnit();
		} else {
			if(meter.isUserDefined()) {
				if(meter.getUserValue() < 0.4) return "Ei toteutunut";
				else if(meter.getUserValue() > 0.6) return "Toteutunut";
				else return "Osittain toteutunut";
			} else {
				Collection<Meter> ms = meter.getSubmeters(database);
				double value = UtilsMeters.value(meter, database);
				int pct = (int)(100.0*value);
				if(ms.size() == 0) {
					return "" + pct + "% (ei mittareita)";
				} else if (ms.size() == 1) {
					return "" + pct + "% (1 mittari)";
				} else {
					return "" + pct + "% (" + ms.size() + " mittaria)";
				}
			}
			
		}

	}


	public static String getDescription(Meter meter, Database database, boolean forecast) {
		Indicator indicator = meter.getPossibleIndicator(database);
		if(indicator != null) {
			double value = UtilsMeters.value(meter, database, forecast);
			return "" + (int)(100.0*value) + "%";
		}
		return meter.getBaseDescription(database);
	}

	public static String getVerboseDescription(Meter meter, Database database, boolean forecast) {
		Indicator indicator = meter.getPossibleIndicator(database);
		if(indicator != null) {
			String result = indicator.getValueShortComment();
			if(result == null || result.isEmpty()) {
       			double value = UtilsMeters.value(meter, database, forecast);
       			return "" + (int)(100.0*value) + "%";
			}
			else return result;
		} else if (meter.description != null && !meter.description.isEmpty()) {
			return meter.description;
		}
		return meter.getBaseDescription(database);
	}
	
	public static String getTrafficColor(Meter meter, Database database) {
		return getTrafficColor(meter, database, true);
	}
	
	public static String getTrafficColor(Meter meter, Database database, boolean forecast) {
		Indicator indicator = meter.getPossibleIndicator(database);
		if(indicator != null) {
			
			Object value = indicator.getValue(forecast);
			if(value == null) return Datatype.RED;
			
			TrafficValuation trafficValuation = UtilsDataTypes.getTrafficValuation(database, meter);
			String trafficValue = trafficValuation.getTrafficValue(value);
			if(trafficValue != null) return trafficValue;
			
			return Datatype.RED;
			
		}
		
		return UtilsDB.trafficColor(UtilsMeters.value(meter, database, forecast));
	}
	
	public static List<MeterDescription> makeMeterDescriptions(Database database, UIState uistate, MapBase base, boolean filterShow) {
		List<MeterDescription> descs = new ArrayList<MeterDescription>();
		UtilsMeters.fillMeterDescriptions(database, uistate, base, "", "", filterShow, descs);
		return descs;
	}
	
	private static String makeIndent(String indent) {
		return "&nbsp;&nbsp;" + indent; 
	}
	
	private static void fillMeterDescriptions(Database database, UIState uistate, MapBase base, String indent, String numbering, boolean filterShow, List<MeterDescription> result) {
		
		List<Meter> meters = base.getMeters(database); 
		for(int i=0;i<meters.size();i++) {
			Meter meter = meters.get(i);
			if(!Utils.isActive(database, uistate, meter)) continue;
			if(filterShow && !meter.isShowInMap()) continue;
			String nr = numbering.isEmpty() ?  Integer.toString(i+1) : numbering + "." + Integer.toString(i+1);
			String desc = indent + nr + ". " + meter.getCaption(database);
			result.add(new MeterDescription(meter, nr, desc));
			UtilsMeters.fillMeterDescriptions(database, uistate, meter, UtilsMeters.makeIndent(indent), nr, filterShow, result);
		}

	}
	
	private static void makeMeterTable(Database database, UIState uistate, MapBase base, final Table table) {
		
		@SuppressWarnings("unchecked")
		Collection<Meter> selection = (Collection<Meter>)table.getValue();
		
		table.removeAllItems();
		List<MeterDescription> descs = makeMeterDescriptions(database, uistate, base, false);
		fillMeterTable(database, uistate, base, table, descs);
		
		for(Meter m : selection)
			table.select(m);

	}
	
	private static void fillMeterTable(Database database, UIState uistate, Base base, final Table table, List<MeterDescription> descs) {

		boolean showYears = uistate.getTime().equals(Property.AIKAVALI_KAIKKI);

		Property aika = Property.find(database, Property.AIKAVALI);
		
		for(int i=0;i<descs.size();i++) {
			MeterDescription desc = descs.get(i);
			Meter meter = desc.meter;
			Label text = new Label(desc.caption);
			text.setContentMode(ContentMode.HTML);
			String year = aika.getPropertyValue(meter);
			if(showYears)
				table.addItem(new Object[] { text, year }, meter);
			else
				table.addItem(new Object[] { text }, meter);
		}

	}
	
	
	public static String updateMeterValue(final Main main, HorizontalLayout hl, final Base base, final Meter meter, boolean canWrite) {
		
		final Database database = main.getDatabase();

		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setWidth("300px");
		hl2.setHeight("100%");
		hl.addComponent(hl2);
		hl.setComponentAlignment(hl2, Alignment.MIDDLE_LEFT);

		final Indicator i = meter.getPossibleIndicator(database);
		if(i != null) {
			
			Datatype datatype = i.getDatatype(database);
			if(datatype instanceof EnumerationDatatype) {

				EnumerationDatatype enu = (EnumerationDatatype)datatype;
				
				Object value = i.getValue();
				AbstractField<?> combo = UtilsDataTypes.createDatatypeEditor(main, enu, base, i, false, null);
				
				hl2.addComponent(combo);
				hl2.setComponentAlignment(combo, Alignment.MIDDLE_CENTER);

				return value != null ? value.toString() : "null";
				
			}
			
		}

		final Label label = new Label(UtilsMeters.describe(meter, database, main.getUIState().forecastMeters));
		label.setWidthUndefined();

		hl2.addComponent(label);
		hl2.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		hl2.setExpandRatio(label, 1.0f);
		
		return label.getValue();

	}

	
	public static Meter addIndicatorMeter(Main main, MapBase b, Indicator indicator, String year) {
		Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		
		return UtilsDB.addIndicatorMeter(database, account, b, indicator, year);
	}
	
	public static Meter getImplementationMeter(Database database, UIState uistate, OuterBox outerBox, String id, boolean forecast) {

		Property aika = Property.find(database, Property.AIKAVALI);

		double value = 0;
		int acceptedCounter = 0;
		for (InnerBox innerBox : outerBox.innerboxes) {
			String a = aika.getPropertyValue(innerBox);
			if (uistate.acceptTime(a)) {
				Meter m = UtilsMeters.getPrincipalMeter(database, uistate, innerBox, id, forecast);
				value += UtilsMeters.value(m, database, forecast);
				acceptedCounter++;
			}
		}
		if (acceptedCounter > 0) {
			value = value / (double) acceptedCounter;
			return Meter.transientMeter(id, outerBox.getMap(database).uuid, value);
		} else {
			return Meter.transientMeter(id, outerBox.getMap(database).uuid, 1.0);
		}

	}
	
	public static Meter getPrincipalMeter(Database database, UIState uistate, OuterBox outerBox, String id, boolean forecast) {
		for (Meter m : UtilsMeters.getMetersActive(database, uistate, outerBox)) {
			if (m.isPrincipal) {
				Meter result = Meter.transientMeter(id, outerBox.getMap(database).uuid, UtilsMeters.value(m, database, forecast));
				result.isPrincipal = true;
				return result;
			}
		}
		return UtilsMeters.getImplementationMeter(database, uistate, outerBox, id, forecast);
	}
	
	public static List<Meter> getImplementationMeters(Database database, UIState uistate, InnerBox innerBox, boolean forecast) {

		List<Meter> result = new ArrayList<Meter>();

		int counter = 1;

		StrategyMap map = innerBox.getMap(database);
		if(map == null) return Collections.emptyList();
		
		Property aika = Property.find(database, Property.AIKAVALI);

		if(map.linkGoalsAndSubmaps) {

			Collection<MapBase> impSet = UtilsDB.getDirectImplementors(database, innerBox, uistate.getTime());
			for(MapBase imp : impSet) {
				OuterBox t = (OuterBox)imp;
				for(InnerBox subInnerBox : t.innerboxes) {
					String a = aika.getPropertyValue(subInnerBox);
					if(uistate.acceptTime(a)) {
						String pid = subInnerBox.getId(database);
						if(pid.isEmpty())
							pid = subInnerBox.getText(database);
						if(pid.length() > 20)
							pid = pid.substring(0, 20);
						Meter m = UtilsMeters.getPrincipalMeter(database, uistate, subInnerBox, pid.isEmpty() ? "" + counter : pid, forecast);
						if(m.isTransient()) {
							m.description = subInnerBox.getText(database);
						}
						result.add(m);
						counter++;
					}
				}
			}
			
		} else {

			Collection<MapBase> impSet = UtilsDB.getDirectImplementors(database, innerBox, uistate.getTime());
			for(MapBase b : impSet) {
				String a = aika.getPropertyValue(b);
				if(uistate.acceptTime(a)) {
					if(b instanceof OuterBox) {
						OuterBox impOuterBox = (OuterBox)b;
						String tid = impOuterBox.getMap(database).getId(database);
						if(tid.isEmpty())
							tid = impOuterBox.getMap(database).getText(database);
						if(tid.length() > 20)
							tid = tid.substring(0, 20);
						result.add(UtilsMeters.getPrincipalMeter(database, uistate, impOuterBox, tid.isEmpty() ? "" + counter : tid, forecast));
						counter++;
					}
				}
			}
			
		}

		return result;

	}
	
	public static Meter getPossiblePrincipalMeterActive(Database database, UIState uistate, MapBase b) {
		for(Meter m : getMetersActive(database, uistate, b)) {
			if(m.isPrincipal) return m;
		}
		return null;
	}
	
	public static List<Meter> getMetersActive(Database database, UIState uistate, MapBase b) {
			
		ArrayList<Meter> pps = new ArrayList<Meter>();

		Property aika = Property.find(database, Property.AIKAVALI);

		for(Meter m : b.meters) {
			String a = aika.getPropertyValue(m);
			if(a != null) {
				if(uistate.acceptTime(a))
					pps.add(m);
			} else {
				pps.add(m);
			}
		}

		return pps;

	}
	
	
	public static Meter getPrincipalMeter(Database database, UIState uistate, InnerBox innerBox, String id, boolean forecast) {
		
		Meter pm = UtilsMeters.getPossiblePrincipalMeterActive(database, uistate, innerBox);
		if(pm != null) {
			return pm;
		}
		
		Collection<Meter> imps = UtilsMeters.getImplementationMeters(database, uistate, innerBox, forecast); 
		if(imps.size() == 1) {
			Meter m = imps.iterator().next();
			if(m.isPrincipal) {
				return m;
			}
		}
		if(imps.size() > 0) {
			double value = 0;
			for(Meter m : imps) {
				value += UtilsMeters.value(m, database, forecast);
			}
			value = value / (double)imps.size();
   			String id2 = "" + (int)(100.0*value) + "%";
			return Meter.transientMeter(id2, innerBox.getMap(database).uuid, value);
		} else {
			if(forecast)
				return Meter.transientMeter("100%", innerBox.getMap(database).uuid, 1.0);
			else
				return Meter.transientMeter("0%", innerBox.getMap(database).uuid, 0.0);
		}
	}
}
