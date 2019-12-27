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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.CalendarBase;
import fi.semantum.strategy.db.CalendarEventBase;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.UtilsDB;

public class CalendarView {
	
	private VerticalLayout yearClock;
	private boolean editable = false;
	
	private Main main;
	
	public CalendarView(Main main) {
		this.main = main;
		yearClock = createYearClockLayout();
	}
	
	public void setEditable(boolean e) {
		this.editable = e;
	}
	
	public VerticalLayout getYearClock() {
		return yearClock;
	}
	
	private VerticalLayout createYearClockLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		return layout;
	}
	
	public void clear() {
		yearClock.removeAllComponents();
	}
	
	public void populate(CalendarBase forceYear) {
		Database database = main.getDatabase();
		clear();
		
		List<CalendarBase> calendarYears = CalendarBase.enumerate(database);
		
		CalendarBase activeCalendar = forceYear;
		if(activeCalendar == null) {
			for(CalendarBase year : calendarYears) {
				if(year.active) {
					activeCalendar = year;
					break;
				}
			}
		}
		if(activeCalendar != null) {
			Table summaryTable = new Table();
			VerticalLayout yearClockLayout = convertCalendarToYearClock(summaryTable, main, activeCalendar, editable);
			
			Button addEventButton = null;
			PopupView addEventPopupView = null;
			
			if(editable) {
				VerticalLayout content = new VerticalLayout();
				addEventPopupView = new PopupView("", content);
				addEventPopupView.setHideOnMouseOut(false);
				
				//Null base first - since the button creates new
				populateCreateEventContent(null, activeCalendar, content, addEventPopupView);
				
				addEventButton = createAddOrEditEventButton(addEventPopupView);
			}
			
			HorizontalLayout buttonToolbar = new HorizontalLayout();
			buttonToolbar.setHeight("40px");
			buttonToolbar.setWidth("100%");
			
			if(addEventButton != null && addEventPopupView != null) {
				buttonToolbar.addComponent(addEventPopupView);
				buttonToolbar.setExpandRatio(addEventPopupView, 0.0f);
				buttonToolbar.setComponentAlignment(addEventPopupView, Alignment.MIDDLE_RIGHT);
				buttonToolbar.addComponent(addEventButton);
				buttonToolbar.setExpandRatio(addEventButton, 0.5f);
				buttonToolbar.setComponentAlignment(addEventButton, Alignment.MIDDLE_RIGHT);
			}
			
			
			summaryTable.setWidth("70%");

			yearClock.addComponent(buttonToolbar);
			yearClock.setExpandRatio(buttonToolbar, 0.0f);
			yearClock.setComponentAlignment(buttonToolbar, Alignment.MIDDLE_CENTER);
			yearClock.addComponent(yearClockLayout);
			yearClock.setExpandRatio(yearClockLayout, 0.0f);
			yearClock.setComponentAlignment(yearClockLayout, Alignment.TOP_CENTER);
			yearClock.addComponent(summaryTable);
			yearClock.setExpandRatio(summaryTable, 1.0f);
			yearClock.setComponentAlignment(summaryTable, Alignment.TOP_CENTER);
			yearClock.setHeight("100%");
		} else {
			yearClock.setHeight("0px");
		}
		
	}
	
	private static final String CREATE_EVENT_INFO_STRING = SafeHtmlUtils.htmlEscape(
			"Tapahtuman kuvaukseen voi tehd‰ linkkej‰.\n"
			+ "Lis‰‰ linkkej‰ n‰in:\n"
			+ "<a href=\"https://minunsivu.fi\">Linkki</a>.\n"
			+ "T‰m‰ luo linkin joka n‰kyy k‰ytt‰jille Linkki-sanalla, ja ohjaa https://minunsivu.fi sivulle.\n\n"
			+ "Tapahtumien nimet ja kaudet eiv‰t saa olla liian pitki‰.\n\n"
			+ "Loppup‰iv‰ ei saa olla alkup‰iv‰‰ ennen.\n\n"
			+ "Jos tapahtumalla ei ole kellonaikaa, valitse pois Sis‰lt‰‰ kellonajan."
			).replaceAll("\n", "</br>");
	
	private void populateCreateEventContent(CalendarEventBase possibleBase, CalendarBase activeYear, VerticalLayout content, PopupView addEventPopupView) {
		Button close = createClosePopupViewButton(addEventPopupView);

		Label info = new Label();
		info.setWidth("400px");
		info.setHeight("300px");
		info.addStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		info.setContentMode(ContentMode.HTML);
		info.setSizeFull();
		
		info.setValue(CREATE_EVENT_INFO_STRING);
		info.setReadOnly(true);
		
		PopupView infoPopupView = new PopupView("", info);
		infoPopupView.setHideOnMouseOut(false);
		
		Button infoButton = new Button();
		infoButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8571918488695597548L;

			@Override
			public void buttonClick(ClickEvent e) {
				infoPopupView.setPopupVisible(true);
			}
			
		});
		infoButton.setIcon(FontAwesome.INFO);
		infoButton.setStyleName(ValoTheme.BUTTON_TINY);
		infoButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		
		CheckBox includesTimeAccuracy = new CheckBox();
		includesTimeAccuracy.setStyleName(ValoTheme.CHECKBOX_SMALL);
		includesTimeAccuracy.setDescription("Valitse sis‰lt‰‰kˆ tapahtuma kellonajan");
		includesTimeAccuracy.setCaption("Sis‰lt‰‰ kellonajan");

		DateField startDateField = new DateField();
		startDateField.setCaption("Tapahtuman alku");
		startDateField.setParseErrorMessage("Kent‰ss‰ on virheit‰");
		
		DateField endDateField = new DateField();
		endDateField.setCaption("Tapahtuman loppu");
		endDateField.setParseErrorMessage("Kent‰ss‰ on virheit‰");

		includesTimeAccuracy.setValue(false);
		includesTimeAccuracy.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -2484844033903438195L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(includesTimeAccuracy.getValue()) {
					startDateField.setDateFormat("yyyy-MM-dd HH:mm");
					startDateField.setResolution(Resolution.MINUTE);
					endDateField.setDateFormat("yyyy-MM-dd HH:mm");
					endDateField.setResolution(Resolution.MINUTE);
				} else {
					startDateField.setDateFormat("yyyy-MM-dd");
					startDateField.setResolution(Resolution.DAY);
					endDateField.setDateFormat("yyyy-MM-dd");
					endDateField.setResolution(Resolution.DAY);
				}
			}
			
		});
		
		TextField caption = new TextField();
		caption.setCaption("Nimi");

		TextField tagField = new TextField();
		tagField.setCaption("Kausi");
		
		TextArea descriptionField = new TextArea();
		descriptionField.setCaption("Kuvaus");
		
		ComboBox colorSelection = new ComboBox();
		colorSelection.setNullSelectionAllowed(false);
		colorSelection.setTextInputAllowed(false);
		colorSelection.setCaption("Tapahtuman v‰ri");
		colorSelection.setDescription("Valitse tapahtumalle v‰ri");
		for(CalendarEventBase.EventColor color : CalendarEventBase.EventColor.values()) {
			colorSelection.addItem(color);
			colorSelection.setItemCaption(color, Utils.translateColorLanguage(color));
		}
		
		Button create = new Button();
		Button delete = new Button();
		delete.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 935093684471458917L;

			@Override
			public void buttonClick(ClickEvent event) {
				possibleBase.remove(main.getDatabase());
				populate(activeYear);
				addEventPopupView.setVisible(false);
			}
			
		});
		
		addEventPopupView.addPopupVisibilityListener(new PopupVisibilityListener() {
			
			private static final long serialVersionUID = 8555493259464681081L;

			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(addEventPopupView.isPopupVisible()) {
					if(possibleBase == null) {
						startDateField.setValue(new Date());
						endDateField.setValue(new Date());
						caption.setValue("");
						tagField.setValue("");
						create.setCaption("Luo");
						colorSelection.setValue(CalendarEventBase.EventColor.NONE);
						includesTimeAccuracy.setValue(true); //Triggers refresh since value is false by default
						descriptionField.setValue("");
						delete.setEnabled(false);
						delete.setVisible(false);
					} else {
						descriptionField.setValue(possibleBase.description);
						startDateField.setValue(possibleBase.getStartDate());
						endDateField.setValue(possibleBase.getEndDate());
						caption.setValue(possibleBase.text);
						tagField.setValue(possibleBase.tag);
						create.setCaption("Muokkaa");
						delete.setEnabled(true);
						delete.setVisible(true);
						delete.setCaption("Poista");
						delete.setStyleName(ValoTheme.BUTTON_TINY);
						colorSelection.setValue(possibleBase.eventColor);
						//Default checkbox value is false - ensure it refreshes
						if(possibleBase.includesTime) {
							includesTimeAccuracy.setValue(possibleBase.includesTime);
						} else {
							//Force refresh of view
							includesTimeAccuracy.setValue(true);
							includesTimeAccuracy.setValue(possibleBase.includesTime);
						}
					}
				}
			}
			
		});
		
		create.setStyleName(ValoTheme.BUTTON_TINY);
		create.setIcon(FontAwesome.SAVE);
		create.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8179181240424937938L;

			@Override
			public void buttonClick(ClickEvent event) {
				String text = SafeHtmlUtils.htmlEscape(caption.getValue());
				String tag = SafeHtmlUtils.htmlEscape(tagField.getValue());
				Date startDate = startDateField.getValue();
				Date endDate = endDateField.getValue();
				Object colorO = colorSelection.getValue();
				boolean includesTime = includesTimeAccuracy.getValue();
				String detailedDescription = extractDescription(descriptionField.getValue());
				String errorMsg = validFields(text, tag, startDate, endDate);
				
				CalendarEventBase.EventColor color = colorO == null ? CalendarEventBase.EventColor.NONE : (CalendarEventBase.EventColor)colorO;
				
				if(errorMsg == null) {
					Account a = main.getAccountDefault();
					if(UtilsDB.canWrite(main.getDatabase(), a, activeYear) || a.isAdmin(main.getDatabase())){
						if(possibleBase == null) {
							//Create new
							CalendarEventBase ceb = CalendarEventBase.create(main.getDatabase(), text, startDate, endDate, color, includesTime, tag);
							ceb.description = detailedDescription;
							activeYear.addEvent(main.getDatabase(), ceb);
						} else {
							//Edit existing
							possibleBase.startDate = startDate;
							possibleBase.endDate = endDate;
							possibleBase.text = text;
							possibleBase.description = detailedDescription;
							possibleBase.eventColor = color;
							possibleBase.includesTime = includesTime;
							possibleBase.tag = tag;
						}
						Updates.update(main, true);
						populate(activeYear);
						addEventPopupView.setPopupVisible(false);
					} else {
						System.err.println("Non-admin user attempted to create event to calendar!");
					}
				} else {
					String err = "<div>" + errorMsg + "</div>";
					Label l = new Label();
					l.setValue(err);
					l.setContentMode(ContentMode.HTML);
					l.setWidth("300px");
					l.setHeight("150px");
					System.err.println(err);
					Dialogs.errorDialog(main, "Kentiss‰ on virheit‰!", l, "325px", "300px");
				}
			}
			
		});
		
		content.setHeight("700px");
		content.setWidth("800px");
		
		HorizontalLayout upperLayout = new HorizontalLayout();
		upperLayout.setWidth("800px");
		upperLayout.setHeight("100%");
		upperLayout.addComponent(infoPopupView);
		upperLayout.setExpandRatio(infoPopupView, 0.0f);
		upperLayout.setComponentAlignment(infoPopupView, Alignment.MIDDLE_LEFT);
		upperLayout.addComponent(infoButton);
		upperLayout.setExpandRatio(infoButton, 0.0f);
		upperLayout.setComponentAlignment(infoButton, Alignment.MIDDLE_LEFT);
		upperLayout.addComponent(includesTimeAccuracy);
		upperLayout.setExpandRatio(includesTimeAccuracy, 1.0f);
		upperLayout.setComponentAlignment(includesTimeAccuracy, Alignment.MIDDLE_CENTER);
		
		content.addComponent(upperLayout);
		content.setExpandRatio(upperLayout, 0.1f);
		content.setComponentAlignment(upperLayout, Alignment.MIDDLE_CENTER);
		
		HorizontalLayout dateLayout = new HorizontalLayout();
		dateLayout.setWidth("100%");
		startDateField.setWidth("260px");
		dateLayout.addComponent(startDateField);
		dateLayout.setComponentAlignment(startDateField, Alignment.TOP_LEFT);
		endDateField.setWidth("260px");
		dateLayout.addComponent(endDateField);
		dateLayout.setComponentAlignment(endDateField, Alignment.TOP_LEFT);
		colorSelection.setWidth("200px");
		dateLayout.addComponent(colorSelection);
		dateLayout.setComponentAlignment(colorSelection, Alignment.TOP_RIGHT);
		content.addComponent(dateLayout);
		content.setExpandRatio(dateLayout, 0.1f);
		
		HorizontalLayout textLayout = new HorizontalLayout();
		textLayout.setWidth("100%");
		caption.setWidth("550px");
		textLayout.addComponent(caption);
		textLayout.setExpandRatio(caption, 0.7f);
		textLayout.setComponentAlignment(caption, Alignment.TOP_LEFT);
		tagField.setWidth("200px");
		textLayout.addComponent(tagField);
		textLayout.setComponentAlignment(tagField, Alignment.TOP_RIGHT);
		textLayout.setExpandRatio(tagField, 0.3f);
		content.addComponent(textLayout);
		content.setExpandRatio(textLayout, 0.1f);
		
		descriptionField.setWidth("780px");
		descriptionField.setHeight("350px");
		content.addComponent(descriptionField);
		content.setExpandRatio(descriptionField, 0.5f);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setSizeFull();
		buttonToolbar.addComponent(close);
		buttonToolbar.setExpandRatio(close, 1.0f);
		buttonToolbar.setComponentAlignment(close, Alignment.MIDDLE_LEFT);

		buttonToolbar.addComponent(delete);
		buttonToolbar.setComponentAlignment(delete, Alignment.MIDDLE_CENTER);
		buttonToolbar.setExpandRatio(delete, 0.5f);

		buttonToolbar.addComponent(create);
		buttonToolbar.setExpandRatio(create, 1.0f);
		buttonToolbar.setComponentAlignment(create, Alignment.MIDDLE_RIGHT);
		
		content.addComponent(buttonToolbar);
		content.setComponentAlignment(buttonToolbar, Alignment.MIDDLE_CENTER);
		content.setExpandRatio(buttonToolbar, 0.1f);
	}
	
	/**
	 * Replace all link elements with links with target="_blank".
	 * Escape all other HTML elements.
	 * Link name and href are extracted from link elements, then those are also escaped.
	 * @param unescapedDescription
	 * @return
	 */
	private String extractDescription(String unescapedDescription) {
		return Utils.extractDescription(unescapedDescription);
	}

	
	/**
	 * Return errorMessage. If null, all fields were OK.
	 * @param a
	 * @param b
	 * @param a2
	 * @param b2
	 * @return
	 */
	private String validFields(String caption, String tag, Date startDate, Date endDate) {
		String error = "";
		if(caption == null || caption.equals("")) {
			error += "Nimi ei saa olla tyhj‰!</br>";
		} else if(caption.length() > 250) {
			error += "Nimi ei saa ylitt‰‰ 250 merkki‰!</br>";
		}
		
		if(tag != null && tag.length() > 80) {
			error += "Kauden pituus ei saa ylitt‰‰ 80 merkki‰!";
		}
		if(startDate == null) {
			error += "Aloitusp‰iv‰ ei saa olla tyhj‰!</br>";
		}
		
		if(endDate == null) {
			error += "Loppup‰iv‰ ei saa olla tyhj‰!</br>";
		}
		
		if(startDate != null && endDate != null) {
			if(startDate.after(endDate)) {
				error += "Aloitusp‰iv‰n t‰ytyy olla ennen loppup‰iv‰‰!</br>";
			}
		}
		
		if(error.equals("")) {
			return null;
		} else {
			return error;
		}
	}
	
	private Button createClosePopupViewButton(PopupView addEventPopupView) {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setCaption("Sulje");
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1869325312148875473L;

			@Override
			public void buttonClick(ClickEvent event) {
				addEventPopupView.setPopupVisible(false);
			}
			
		});
		return b;
	}
	
	private Button createAddOrEditEventButton(PopupView popup) {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setCaption("Uusi tapahtuma");
		b.setIcon(FontAwesome.CALENDAR_PLUS_O);
		b.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 464914310722844957L;

			@Override
			public void buttonClick(ClickEvent event) {
				popup.setPopupVisible(true);
			}
			
		});
		return b;
	}
	
	// Helper methods
	
	//Precision is month
	private int sliderValueForDate(String zone, Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.of(zone)).toLocalDate();
		int month = localDate.getMonthValue(); //Month from 1 to 12
		int year = localDate.getYear();
		int result = 12 * year;
		result += month;
		return result;
	}
	
	private int sliderValueYear(double sliderValue) {
		int fullYears = (int) (sliderValue / 12.0);
		return fullYears;
	}
	
	private int sliderValueMonth(int year, double sliderValue) {
		double rest = sliderValue - (1.0* (12*year));
		int month = (int)rest;
		return month;
	}
	
	public VerticalLayout convertCalendarToYearClock(Table summaryTable, Main main, CalendarBase calendarYear, boolean editable) {
		VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.setWidth("100%");
		rootLayout.setStyleName(CustomCSSStyleNames.STYLE_YEAR_CLOCK_LAYOUT);
		
		List<CalendarEventBase> events = calendarYear.getEvents(main.getDatabase());
		Collections.sort(events, CalendarEventBase.dateComparator);
		
		if(events.size() == 0) {
			summaryTable.setVisible(false);
			return rootLayout;
		}

		if(!editable) {
			populateTableWithEvents(summaryTable, events);
		} else {
			summaryTable.setVisible(false);
		}
		
		HorizontalLayout sliderToolbarLayout = new HorizontalLayout();
		sliderToolbarLayout.setWidth("50%");
		sliderToolbarLayout.setHeight("32px");
		sliderToolbarLayout.setSpacing(true);
		
		CalendarEventBase first = events.get(0);
		CalendarEventBase last = events.get(events.size() - 1);
		
		Account a = main.getAccountDefault();
		
		int min = sliderValueForDate(a.getZone(), first.getStartDate());
		int max = sliderValueForDate(a.getZone(), last.getEndDate());
		int currentDateValue = sliderValueForDate(a.getZone(), new Date());
		
		//Add 2 months, one after and one before
		Slider slider = new Slider(min - 1, max + 1);
		Button sliderPreviousMonthButton = createSliderNavigationButton(slider, true);
		Button sliderNextMonthButton = createSliderNavigationButton(slider, false);
		Label descriptionLabel = new Label();
		descriptionLabel.setContentMode(ContentMode.HTML);
		
		slider.setWidth("33%");
		slider.setHeight("24px");
		slider.setValidationVisible(false);
		VerticalLayout eventLayout = new VerticalLayout();
		
		//Attept to set the slider to the current date
		if(slider.getMin() <= currentDateValue && slider.getMax() >= currentDateValue) {
			setSliderValueSafe(slider, currentDateValue);
		}
		
		slider.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 8057216118156960405L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateEventLayout(main.getDatabase(), descriptionLabel, sliderPreviousMonthButton, sliderNextMonthButton, 
						a.getZone(), slider, eventLayout, events);
			}
			
		});

		//Update layout - either min or current date
		updateEventLayout(main.getDatabase(), descriptionLabel, sliderPreviousMonthButton, sliderNextMonthButton, 
				a.getZone(), slider, eventLayout, events);
		
		sliderToolbarLayout.addComponent(sliderPreviousMonthButton);
		sliderToolbarLayout.setComponentAlignment(sliderPreviousMonthButton, Alignment.TOP_RIGHT);
		sliderToolbarLayout.setExpandRatio(sliderPreviousMonthButton, 1.0f);
		
		sliderToolbarLayout.addComponent(descriptionLabel);
		sliderToolbarLayout.setComponentAlignment(descriptionLabel, Alignment.TOP_CENTER);
		sliderToolbarLayout.setExpandRatio(descriptionLabel, 1.0f);
		
		sliderToolbarLayout.addComponent(sliderNextMonthButton);
		sliderToolbarLayout.setComponentAlignment(sliderNextMonthButton, Alignment.TOP_LEFT);
		sliderToolbarLayout.setExpandRatio(sliderNextMonthButton, 1.0f);
		
		rootLayout.addComponent(sliderToolbarLayout);
		rootLayout.setComponentAlignment(sliderToolbarLayout, Alignment.TOP_CENTER);
		rootLayout.setExpandRatio(sliderToolbarLayout, 0.0f);
		
		rootLayout.addComponent(slider);
		rootLayout.setComponentAlignment(slider, Alignment.TOP_CENTER);
		rootLayout.setExpandRatio(slider, 0.0f);
		
		rootLayout.addComponent(eventLayout);
		rootLayout.setComponentAlignment(eventLayout, Alignment.TOP_CENTER);
		rootLayout.setExpandRatio(eventLayout, 1.0f);
		
		return rootLayout;
	}
	
	private void populateTableWithEvents(Table summaryTable, List<CalendarEventBase> events) {
		summaryTable.setSelectable(false);
		summaryTable.setMultiSelect(false);
		summaryTable.addStyleName(ValoTheme.TABLE_SMALL);
		summaryTable.addStyleName(ValoTheme.TABLE_SMALL);
		summaryTable.addStyleName(ValoTheme.TABLE_COMPACT);
		summaryTable.addStyleName(CustomCSSStyleNames.STYLE_YEAR_BLOCK_SUMMARY_TABLE);
		
		summaryTable.addContainerProperty("P‰iv‰m‰‰r‰", String.class, null, " P‰iv‰m‰‰r‰", FontAwesome.CALENDAR, Table.ALIGN_LEFT);
		summaryTable.addContainerProperty("Tapahtuma", String.class, null);
		summaryTable.addContainerProperty("Kausi", String.class, null);
		summaryTable.setColumnExpandRatio("P‰iv‰m‰‰r‰", 0.16f);
		summaryTable.setColumnExpandRatio("Tapahtuma", 0.64f);
		summaryTable.setColumnExpandRatio("Kausi", 0.25f);
		
		int tableIndex = 0;
		HashMap<String, CalendarEventBase> eventMap = new HashMap<>();
		for(CalendarEventBase event : events) {
			tableIndex++;
			Object itemId = summaryTable.addItem(new Object[] {
					summaryTableDateEntry(event.getStartDate()),
					event.text,
					event.tag
				},
				tableIndex);
			eventMap.put(itemId.toString(), event);
			
		}
		summaryTable.setPageLength(summaryTable.size());
		
		//Styling for rows
		summaryTable.setCellStyleGenerator(new Table.CellStyleGenerator() {

			private static final long serialVersionUID = 3802522232057974364L;

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if (propertyId == null) {
					CalendarEventBase event = eventMap.get(itemId.toString());
					if(event != null) {
						CalendarEventBase.EventColor eventColor = event.eventColor;
						if(eventColor != null && eventColor != CalendarEventBase.EventColor.NONE) {
							return eventColor.name().toLowerCase();
						} else {
							return null;
						}
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
		});
	}
	
	/**
	 * Safely attempt to set the slider value
	 * @param slider
	 * @param value
	 */
	private void setSliderValueSafe(Slider slider, double value) {
		try {
			slider.setValue(value);
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				slider.setValue(slider.getMin());
			} catch (Throwable e2) {
				e2.printStackTrace();
			}
		}
	}
	
	private Button createSliderNavigationButton(Slider slider, boolean back) {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		if(back) {
			b.setDescription("Edellinen kuukausi");
			b.setIcon(FontAwesome.ARROW_LEFT);
		} else {
			b.setDescription("Seuraava kuukausi");
			b.setIcon(FontAwesome.ARROW_RIGHT);
		}
		
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 5490911378580157254L;

			@Override
			public void buttonClick(ClickEvent event) {
				double curr = slider.getValue();
				if(back) {
					double min = slider.getMin();
					if(curr != min) {
						setSliderValueSafe(slider, curr - 1.0);
					}
				} else {
					double max = slider.getMax();
					if(curr != max) {
						setSliderValueSafe(slider, curr + 1.0);
					}
				}
			}
			
		});
		
		return b;
	}
	
	private AbstractLayout eventToLabel(Database database, String zone, final int year, final int month, CalendarEventBase event) {
		
		List<CalendarBase> calendars = CalendarBase.calendarsFromEvent(database, event);
		if(calendars.size() != 1) {
			System.err.println("Unexpected number of calendars found for event! Excepted 1, got " + calendars.size());
			return new HorizontalLayout();
		} else {
			CalendarBase calendarYear = calendars.get(0);
			
			AbsoluteLayout eventLayout = new AbsoluteLayout();
			eventLayout.setWidth("100%");
			eventLayout.setHeight("110px");
			
			Label eventLabel = new Label();
			eventLabel.addStyleName(CustomCSSStyleNames.STYLE_CALENDAR_EVENT_LABEL);
			eventLabel.setContentMode(ContentMode.HTML);
			eventLabel.setWidth("80%");
			
			String eventInfo = buildEventInfoString(zone, year, month, event);
			eventLabel.setValue(eventInfo);
			
			String eventLabelStyle = Utils.eventColorToLabelStyle(event.eventColor);
			eventLabel.addStyleName(eventLabelStyle);
			
			eventLayout.addComponent(eventLabel);
			
			if(editable) {
				VerticalLayout content = new VerticalLayout();
				PopupView editEventPopupView = new PopupView("", content);
				editEventPopupView.setHideOnMouseOut(false);
				
				//Null base first - since the button creates new
				populateCreateEventContent(event, calendarYear, content, editEventPopupView);
				
				Button editButton = createAddOrEditEventButton(editEventPopupView);
				editButton.setStyleName(ValoTheme.BUTTON_TINY);
				editButton.setIcon(FontAwesome.EDIT);
				editButton.setCaption("");
				
				eventLayout.addComponent(editEventPopupView);
				eventLayout.addComponent(editButton, "left: 10.2%; top: 5%;");
			} else {
				VerticalLayout content = new VerticalLayout();
				content.setMargin(true);
				content.addStyleName(eventLabelStyle);
				content.setWidth("780px");
				content.setHeight("350px");
				
				Label descriptionArea = new Label();
				descriptionArea.addStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
				descriptionArea.setContentMode(ContentMode.HTML);
				descriptionArea.setSizeFull();
				String htmlDescription = (event.description).replaceAll("\n", "</br>");
				descriptionArea.setValue(htmlDescription);
				descriptionArea.setReadOnly(true);
				content.addComponent(descriptionArea);
				
				PopupView pv = new PopupView("", content);
				pv.setHideOnMouseOut(false);
				
				Button descriptionButton = new Button();
				descriptionButton.addClickListener(new ClickListener() {

					private static final long serialVersionUID = -3765572139376894422L;

					@Override
					public void buttonClick(ClickEvent e) {
						pv.setPopupVisible(true);
					}
					
				});
				descriptionButton.setIcon(FontAwesome.INFO);
				descriptionButton.setStyleName(ValoTheme.BUTTON_TINY);
				descriptionButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
				eventLayout.addComponent(pv);
				eventLayout.addComponent(descriptionButton, "left: 10.2%; top: 5%;");
			}
			
			return eventLayout;
		}
	}
	
	private String summaryTableDateEntry(Date date) {
		if(date == null) return "P‰iv‰m‰‰r‰ ei lˆytynyt";
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		return dateFormat.format(date);
	}
	
	private String minimalDateString(Date date, String zone, int year, int month, boolean includesTime) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String result = "<large>" + dateFormat.format(date) + "</large>";
		if(includesTime) {
			DateFormat timeFormat = new SimpleDateFormat("HH:mm");
			result += "&#160;&#160;kl. " + timeFormat.format(date);
		}
		return result;
	}
	
	private String buildEventInfoString(String zone, final int year, final int month, CalendarEventBase event) {
		
		boolean includesTime = event.includesTime;
		String start = minimalDateString(event.getStartDate(), zone, year, month, includesTime);
		String end = minimalDateString(event.getEndDate(), zone, year, month, includesTime);

		StringBuilder eventInfoBuilder = new StringBuilder();
		eventInfoBuilder.append("<div style=\"text-align: center;\">");
		eventInfoBuilder.append(start);
		eventInfoBuilder.append("&#160;&#160;&#160;-&#160;&#160;&#160;");
		eventInfoBuilder.append(end);
		eventInfoBuilder.append("</div>");
		eventInfoBuilder.append("<div><small>" + event.tag + "</small></div>");
		eventInfoBuilder.append("<div style=\"text-align: center;\">");
		eventInfoBuilder.append("<medium>" + event.text + "</medium>");
		eventInfoBuilder.append("</div>");
		return eventInfoBuilder.toString();
	}
	
	private void setEventLayoutCaption(Label label, int year, int month) {
		String mS = "" + month;
		if(mS.length() == 1) {
			mS = "0" + mS;
		}
		label.setValue("<div style=\"text-align: center;\">" + mS + " - " + year + "</div>");
	}
	
	private void updateEventLayout(Database database,
			Label descriptionLabel, Button sliderPreviousMonthButton, Button sliderNextMonthButton,
			String zone, Slider slider,
			VerticalLayout eventLayouts, List<CalendarEventBase> events) {
		eventLayouts.removeAllComponents();
		
		double min = slider.getMin();
		double max = slider.getMax();
		double selection = slider.getValue();
		
		sliderPreviousMonthButton.setEnabled(min != selection);
		sliderNextMonthButton.setEnabled(max != selection);
		
		int tempYear = sliderValueYear(selection);
		int tempMonth = sliderValueMonth(tempYear, selection);
		
		if(tempMonth == 0) {
			int month = 12;
			int year = tempYear -1;
			//System.out.println("Selection: " + selection + ". Min/Max: " + min + "/" + max + ". Year/Month: " + year + "/" + month);
			handleEventLayoutUpdate(database, zone, slider,
					year, month, eventLayouts, events);
			setEventLayoutCaption(descriptionLabel, year, month); //Month from 1 to 12
		} else {
			int month = tempMonth;
			int year = tempYear;
			//System.out.println("Selection: " + selection + ". Min/Max: " + min + "/" + max + ". Year/Month: " + year + "/" + month);
			handleEventLayoutUpdate(database, zone, slider,
					year, month, eventLayouts, events);
			setEventLayoutCaption(descriptionLabel, year, month); //Month from 1 to 12
		}
	}
	
	private void handleEventLayoutUpdate(Database database, String zone, Slider slider,
			final int year, final int month, VerticalLayout eventLayouts,
			List<CalendarEventBase> events) {
		
		List<CalendarEventBase> visibleEvents = events
				.stream()
                .filter(e -> eventFitsCriteria(zone, e, year, month))
                .collect(Collectors.toList());
		
		eventLayouts.setMargin(new MarginInfo(true, false, true, false));
		if(visibleEvents.size() == 0) {
			Label l = new Label();
			l.setValue("<div style=\"text-align: center;\">T‰ll‰ kuukaudella ei ole tapahtumia</div>");
			l.setContentMode(ContentMode.HTML);
			eventLayouts.addComponent(l);
		} else {
			for(CalendarEventBase event : visibleEvents) {
				//System.out.println("Matching event: " + event.text);
				AbstractLayout eventLayout = eventToLabel(database, zone, year, month, event);
				eventLayouts.addComponent(eventLayout);
			}
		}
	}
	
	//Input date: 2018 05
	//Events match:
	//2017 12 - 2018 05 -> yes
	//2018 04 - 2018 04 -> no
	//2017 06 - 2018 04 -> no
	//2018 04 - 2018 06 -> yes
	//2018 05 - 2022 01 -> yes
	/**
	 * Check if an event should be shown
	 * @param zone
	 * @param event
	 * @param year
	 * @param month
	 * @return
	 */
	private static boolean eventFitsCriteria(String zone, CalendarEventBase event, int year, int month) {
		Date s = event.getStartDate();
		LocalDate localDateS = s.toInstant().atZone(ZoneId.of(zone)).toLocalDate();
		int sMonth = localDateS.getMonthValue();
		int sYear = localDateS.getYear();
		
		Date e = event.getEndDate();
		LocalDate localDateE = e.toInstant().atZone(ZoneId.of(zone)).toLocalDate();
		int eMonth = localDateE.getMonthValue();
		int eYear = localDateE.getYear();
		return yearAndMonthInRange(sYear, sMonth, year, month, eYear, eMonth);
	}
	
	private static boolean yearAndMonthInRange(int sYear, int sMonth, int year, int month, int eYear, int eMonth) {

		//Cannot match, start year is in the future or end year is in the past
		if(sYear > year || eYear < year) {
			//System.out.println("Case 1");
			return false;
		}
		
		//If start year is the same as the input year, then if start month is the same, show.
		//Otherwise, if the input month is between start and end months, also show.
		//End months in above case can be smaller if end year is larger than input year too!
		else if(sYear <= year) {
			if(sYear == year) {
				if(sMonth == month) {
					//System.out.println("Case 2");
					return true;
				}
				if(sMonth < month && (eMonth >= month || eYear > year)) {
					//System.out.println("Case 3");
					return true;
				}
			} else {
				//Start year is smaller than input year, just check if end if above or same
				if(eYear > year) {
					//System.out.println("Case 4");
					return true;
				}
				if(eYear == year && eMonth >= month) {
					//System.out.println("Case 5");
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Currently tests:
	 *  Case 1
		Case 2
		Case 3
		Case 4
		Case 5
		Case 6
	 */
	public static void testCriteria() {
		if(yearAndMonthInRange(2018, 1, 2018, 1, 2018, 1) != true){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2018, 1, 2018, 1, 2018, 2) != true) {
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2018, 1, 2018, 2, 2018, 1) != false) {
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2018, 1, 2018, 2, 2018, 3) != true) {
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 1, 2018, 1, 2018, 1) != true){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 1, 2017, 6, 2018, 1) != true){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 1, 2018, 1, 2017, 1) != false){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 1, 2018, 1, 2017, 5) != false){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 4, 2018, 1, 2019, 5) != true){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 5, 2018, 1, 2019, 4) != true){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 4, 2017, 3, 2017, 4) != false){
			System.err.println("ERROR");
		}
		if(yearAndMonthInRange(2017, 4, 2017, 3, 2017, 7) != false){
			System.err.println("ERROR");
		}
		System.out.println("Tests done");
	}
}
