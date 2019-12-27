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

import java.util.List;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.CalendarBase;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MdHtmlContent;

/**
 * Main start page. Main class redirects here after DB initializations.
 */
public class MainStartPage {
	
	private Main main;
	private Button openMapViewButton;
	
	private Label leftfiller;
	private Link linkToTargetSiteLink;
	private Label htmlContent;

	private CalendarView startPageCalendarView;
	private Button editStartPageButton;
	
	private MainAccountToolbar accountToolbar;
	
	private VerticalLayout root;
	
	public MainStartPage(Main main) {
		this.main = main;
		init();
	}
	
	private void init() {
		root = new VerticalLayout();
		root.setSizeFull();
		root.setSpacing(true);
		
		accountToolbar = new MainAccountToolbar(main);
		openMapViewButton = createOpenMapViewButton();
		startPageCalendarView = new CalendarView(main);
		startPageCalendarView.setEditable(false);
		
		editStartPageButton = createNewOpenPopupButton("Muokkaa p‰‰sivua", FontAwesome.EDIT, "Muokkaa p‰‰sivun sis‰ltˆ‰");
		
		final HorizontalLayout mainToolbar = new HorizontalLayout();
		mainToolbar.addStyleName(CustomCSSStyleNames.STYLE_MAIN_TOOLBAR);
		mainToolbar.setWidth("100%");
		mainToolbar.setHeight(Main.TOOLBAR_HEIGHT, Unit.PIXELS);
		mainToolbar.setSpacing(true);
		
		linkToTargetSiteLink = createLinkToTargetSiteLink();
		leftfiller = new Label();
		mainToolbar.addComponent(leftfiller);
		mainToolbar.setComponentAlignment(leftfiller, Alignment.TOP_LEFT);
		mainToolbar.setExpandRatio(leftfiller, 1.0f);
		
		mainToolbar.addComponent(linkToTargetSiteLink);
		mainToolbar.setComponentAlignment(linkToTargetSiteLink, Alignment.TOP_LEFT);
		mainToolbar.setExpandRatio(linkToTargetSiteLink, 1.0f);
		
		mainToolbar.addComponent(editStartPageButton);
		mainToolbar.setComponentAlignment(editStartPageButton, Alignment.MIDDLE_RIGHT);
		mainToolbar.setExpandRatio(editStartPageButton, 0.0f);
		
		mainToolbar.addComponent(openMapViewButton);
		mainToolbar.setExpandRatio(openMapViewButton, 0.0f);
		mainToolbar.setComponentAlignment(openMapViewButton, Alignment.MIDDLE_RIGHT);
		
		accountToolbar.attachToLayout(mainToolbar);
		
		VerticalLayout textLayout = new VerticalLayout();
		textLayout.addStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		textLayout.setSizeFull();
		htmlContent = createHTMLContent();
		
		textLayout.addComponent(htmlContent);
		textLayout.setComponentAlignment(htmlContent, Alignment.TOP_CENTER);
		textLayout.setExpandRatio(htmlContent, 0.0f);
		
		textLayout.addComponent(startPageCalendarView.getYearClock());
		textLayout.setComponentAlignment(startPageCalendarView.getYearClock(), Alignment.TOP_CENTER);
		textLayout.setExpandRatio(startPageCalendarView.getYearClock(), 1.0f);
		
		root.addComponent(mainToolbar);
		root.setExpandRatio(mainToolbar, 0.0f);
		root.setComponentAlignment(mainToolbar, Alignment.TOP_RIGHT);
		root.addComponent(textLayout);
		root.setExpandRatio(textLayout, 1.0f);
		root.setComponentAlignment(textLayout, Alignment.TOP_CENTER);
	}
	
	private Link createLinkToTargetSiteLink() {
		Link l = new Link();
		l.setStyleName(CustomCSSStyleNames.STYLE_START_PAGE_EXTERNAL_LINK);
		return l;
	}
	
	private static String applyHTMLStyling(String html) {
		return "<div class=\""+ CustomCSSStyleNames.STYLE_START_PAGE+ "\">" + html + "</div>";
	}
	
	private Label createHTMLContent() {
		Label l = new Label();
		l.setContentMode(ContentMode.HTML);
		l.setWidth("90%");
		return l;
	}
	
	public void hide() {
		if(this.main.uiState.startPageVisible) {
			loadMapView();
		}
		this.main.uiState.startPageVisible = false;
	}
	
	/**
	 * Open the map view
	 */
	private void loadMapView() {
		if(main.account != null) {
			this.main.uiState.startPageVisible = false;
			this.main.setContent(null);
			this.main.loadMainView();
		} else {
			System.err.println("Tried to load map view with a non-logged in Account (null)!");
		}
	}
	
	/**
	 * Open the StartPage view
	 */
	public void show() {
		refreshUI();
		main.clearAll();
		main.setContent(root);
		main.uiState.startPageVisible = true;
		Login.refreshUserUI(main);
		handleLoginUIRefresh();
	}
	
	private void refreshUI(){
		MdHtmlContent startPageMHC = MdHtmlContent.getOrCreateByID(main.getDatabase(), MdHtmlContent.STARTPAGE);
		htmlContent.setValue(applyHTMLStyling(startPageMHC.getHTML()));
		MdHtmlContent targetWebsiteLink = MdHtmlContent.getOrCreateByID(main.getDatabase(), MdHtmlContent.TARGET_WEBSITE_LINK);
		if(targetWebsiteLink == null || targetWebsiteLink.unformattedContent.equals("")) {
			linkToTargetSiteLink.setVisible(false);
			leftfiller.setVisible(true);
		} else {
			linkToTargetSiteLink.setResource(new ExternalResource(targetWebsiteLink.unformattedContent));
			linkToTargetSiteLink.setCaption(targetWebsiteLink.description);
			linkToTargetSiteLink.setVisible(true);
			leftfiller.setVisible(false);
		}
		this.startPageCalendarView.populate(null); //Populate finds the active year
	}
	
	public void refreshAllSizes() {
		
	}
	
	public void handleLoginUIRefresh() {
		if(main.account != null) {
			accountToolbar.login();
			openMapViewButton.setVisible(true);
			openMapViewButton.setEnabled(true);
			refreshAdminView();
		} else {
			handleLogoutUIRefresh();
		}
	}
	
	public void refreshAdminView() {
		if(main.getAccountDefault().isAdmin(main.getDatabase())) {
			editStartPageButton.setVisible(true);
			editStartPageButton.setEnabled(true);
		} else {
			editStartPageButton.setVisible(false);
			editStartPageButton.setEnabled(false);
		}
	}
	
	public void handleLogoutUIRefresh() {
		accountToolbar.logout();
		openMapViewButton.setVisible(false);
		openMapViewButton.setEnabled(false);
		refreshAdminView();
	}
	
	private Button createOpenMapViewButton() {
		Button b = new Button();
		b.setCaption("Avaa kartta");
		b.setVisible(false);
		b.setEnabled(false);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4951511535894330443L;

			@Override
			public void buttonClick(ClickEvent event) {
				b.setEnabled(false);
				try {
					loadMapView();
				} catch(Throwable e) {
					e.printStackTrace();
				}
				b.setEnabled(true);
			}
			
		});
		return b;
	}
	
	public static Button createNewBackToStartPageButton(Main main) {
		Button b = new Button();
		b.setCaption("Palaa aloitussivuun");
		b.setIcon(FontAwesome.ARROW_CIRCLE_LEFT);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4951511535894330443L;

			@Override
			public void buttonClick(ClickEvent event) {
				b.setEnabled(false);
				try {
					main.startPage.show();
				} catch(Throwable e) {
					e.printStackTrace();
				}
				b.setEnabled(true);
			}
			
		});
		return b;
	}

	public static void createAndOpenEditStartPageWindow(final Main main) {

		final Database database = main.getDatabase();
		
		HorizontalLayout root = new HorizontalLayout();
		root.setSizeFull();
		
        VerticalLayout startPageEditLayout = new VerticalLayout();
        startPageEditLayout.setSizeFull();
        startPageEditLayout.setSpacing(true);
        
        VerticalLayout linkEditLayout = new VerticalLayout();
        linkEditLayout.setSizeFull();
        linkEditLayout.setSpacing(true);
        
        VerticalLayout yearClockEditLayout = new VerticalLayout();
        yearClockEditLayout.setSizeFull();
        yearClockEditLayout.setSpacing(true);
        yearClockEditLayout.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
        
		TabSheet sheet = new TabSheet();
		root.addComponent(sheet);
        sheet.setSizeFull();

        sheet.addTab(yearClockEditLayout, "Vuosikello");
        sheet.addTab(startPageEditLayout, "P‰‰sivu");
        sheet.addTab(linkEditLayout, "Linkit");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        createAndAttachStartPageEditLayout(main, startPageEditLayout);
        createAndAttachLinkEditLayout(main, linkEditLayout);
        createAndAttachYearClockEditLayout(main, yearClockEditLayout);
        
        Window w = Dialogs.makeDialog(main, main.getFullWindowWidth()+"px", main.getWindowHeightPixels()+"px", "Muokkaa etusivua", "Sulje", root, buttons);
        w.addCloseListener(new CloseListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 8746069131400742092L;

			@Override
			public void windowClose(CloseEvent e) {
				main.startPage.refreshUI();
			}
        	
        });
	}
	
	private static void createAndAttachStartPageEditLayout(Main main, VerticalLayout startPageEditLayout) {
		MdHtmlContent startPageMHC = MdHtmlContent.getOrCreateByID(main.getDatabase(), MdHtmlContent.STARTPAGE);
		ChapterEditorWrapperUI contentEditor = new ChapterEditorWrapperUI(main, startPageMHC);
		startPageEditLayout.addComponent(contentEditor.getRoot());
	}
	
	private static void createAndAttachLinkEditLayout(Main main, VerticalLayout linkEditLayout){
		MdHtmlContent linkPageEditor = MdHtmlContent.getOrCreateByID(main.getDatabase(), MdHtmlContent.TARGET_WEBSITE_LINK);
		TextField contentEditor = new TextField();
		contentEditor.setWidth("500px");
		contentEditor.setCaption("Linkki:");
		contentEditor.setValue(linkPageEditor.unformattedContent);
		
		TextField name = new TextField();
		name.setCaption("Nimi:");
		name.setWidth("500px");
		name.setValue(linkPageEditor.description);
		
		Button save = new Button();
		save.setStyleName(ValoTheme.BUTTON_TINY);
		save.setIcon(FontAwesome.SAVE);
		save.setCaption("Tallenna");
		
		save.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -3138666678567603374L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(main.getDatabase())) {
					String newValue = contentEditor.getValue();
					String newName = name.getValue();
					
					//If either content of link is old, update
					if(!newName.equals(linkPageEditor.description) || !newValue.equals(linkPageEditor.unformattedContent)) {
						linkPageEditor.attemptSetContent(newValue);
						linkPageEditor.description = newName;
						Updates.update(main, true);
					}
				} else {
					System.err.println("Non admin user attempted to write to link edit layout!");
				}
			}
			
		});
		
		linkEditLayout.addComponent(name);
		linkEditLayout.setExpandRatio(name, 0.0f);
		linkEditLayout.setComponentAlignment(name, Alignment.TOP_CENTER);
		linkEditLayout.addComponent(contentEditor);
		linkEditLayout.setExpandRatio(contentEditor, 0.0f);
		linkEditLayout.setComponentAlignment(contentEditor, Alignment.TOP_CENTER);
		linkEditLayout.addComponent(save);
		linkEditLayout.setExpandRatio(save, 1.0f);
		linkEditLayout.setComponentAlignment(save, Alignment.TOP_CENTER);
    }
	
	private static void createAndAttachYearClockEditLayout(Main main, VerticalLayout yearClockEditLayout){
		CalendarView settingsCalendarView = new CalendarView(main);
		settingsCalendarView.setEditable(true);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setSpacing(true);
		buttonToolbar.setHeight("40px");
		buttonToolbar.setWidth("100%");
		
		//Create new:
    	Label isActiveLabel = new Label();
		ComboBox yearClockSelection = new ComboBox();
		Button selectAsMainYearClock = new Button();
		Button openCreateNewButton = new Button();
		Button remove = new Button();
		
		PopupView createNewCalendarPopUp = createNewCalendarPopupView(main, yearClockSelection);
    	
    	openCreateNewButton.setIcon(FontAwesome.PLUS);
    	openCreateNewButton.setCaption("Luo uusi vuosikello");
    	openCreateNewButton.setStyleName(ValoTheme.BUTTON_TINY);
    	openCreateNewButton.addClickListener(new ClickListener() {
    		
			private static final long serialVersionUID = 9051007311822585672L;

			@Override
			public void buttonClick(ClickEvent event) {
				createNewCalendarPopUp.setPopupVisible(true);
			}
    		
    	});
    	
		//Edit existing:
    	isActiveLabel.setStyleName(ValoTheme.LABEL_TINY);
		
		yearClockSelection.addStyleName(ValoTheme.COMBOBOX_TINY);
		yearClockSelection.setWidth("250px");
		yearClockSelection.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1372313437327028348L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object selection = yearClockSelection.getValue();
				if(selection != null) {
					CalendarBase selectedYC = null;
					try {
						selectedYC = (CalendarBase)selection;
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
					refreshSettingsSelectedCalendarYearView(main.getDatabase(), selectedYC, remove, selectAsMainYearClock, isActiveLabel);
					settingsCalendarView.populate(selectedYC);
				} else {
					settingsCalendarView.clear();
					refreshSettingsSelectedCalendarYearView(main.getDatabase(), null, remove, selectAsMainYearClock, isActiveLabel);
				}
			}
			
		});
		
		remove.setCaption("Poista valittu");
		remove.setIcon(FontAwesome.TRASH);
		remove.setStyleName(ValoTheme.BUTTON_TINY);
		remove.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -609542083820258206L;

			@Override
			public void buttonClick(ClickEvent event) {
				Object selection = yearClockSelection.getValue();
				if(selection != null) {
					CalendarBase selectedYC = null;
					try {
						selectedYC = (CalendarBase)selection;
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
					if(selectedYC != null) {
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								try {
									Object selection = yearClockSelection.getValue();
									CalendarBase selectedYC = (CalendarBase)selection;
									
									Database database = main.getDatabase();
									selectedYC.remove(database);
									Updates.update(main, true);
									populateComboBoxWithCalendars(database, yearClockSelection);
									yearClockSelection.select(null);
									settingsCalendarView.clear();
								} catch (Throwable e) {
									e.printStackTrace();
									Dialogs.errorDialog(main, "Virhe vuosikellon poistossa", new Label(""), "600px", "100px");
								}
							}
							
						};
						
						Dialogs.confirmDialog(main, "Haluatko poistaa vuosikellon pysyv‰sti?", "Poista vuosikello pysyv‰sti", "Peruuta", runnable);
					} else {
						System.err.println("Failed to find CalendarYear from selection object!");
						Dialogs.errorDialog(main, "Vuosikelloa ei lˆydetty!", new Label(""), "600px", "100px");
					}
				} else {
					System.err.println("Null selection in year clock selection!");
					Dialogs.errorDialog(main, "Vuosikelloa ei valittuna!", new Label(""), "600px", "100px");
				}
			}
			
		});
		
		selectAsMainYearClock.setCaption("Merkitse p‰‰sivun vuosikelloksi");
		selectAsMainYearClock.setIcon(FontAwesome.SAVE);
		selectAsMainYearClock.setStyleName(ValoTheme.BUTTON_TINY);
		selectAsMainYearClock.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 3495588802400043099L;

			@Override
			public void buttonClick(ClickEvent event) {
				Object selection = yearClockSelection.getValue();
				if(selection != null) {
					CalendarBase selectedYC = null;
					try {
						selectedYC = (CalendarBase)selection;
					} catch (ClassCastException e) {
						e.printStackTrace();
					}
					if(selectedYC != null) {
						Database database = main.getDatabase();
						List<CalendarBase> allYears = CalendarBase.enumerate(database);
						for(CalendarBase year : allYears) {
							if(year.uuid.equals(selectedYC.uuid)) {
								year.active = true;
							} else {
								year.active = false;
							}
						}
						Updates.update(main, true);
						CalendarBase newSelection = populateComboBoxWithCalendars(database, yearClockSelection);
						settingsCalendarView.populate(newSelection);
						refreshSettingsSelectedCalendarYearView(main.getDatabase(), newSelection, remove, selectAsMainYearClock, isActiveLabel);
					} else {
						System.err.println("Failed to find CalendarYear from selection object!");
						Dialogs.errorDialog(main, "Vuosikelloa ei lˆydetty!", new Label(""), "600px", "100px");
					}
				} else {
					System.err.println("Null selection in year clock selection!");
					Dialogs.errorDialog(main, "Vuosikelloa ei valittuna!", new Label(""), "600px", "100px");
				}
			}
			
		});
		
    	
		CalendarBase selection = populateComboBoxWithCalendars(main.getDatabase(), yearClockSelection);
		settingsCalendarView.populate(selection);
		refreshSettingsSelectedCalendarYearView(main.getDatabase(), selection, remove, selectAsMainYearClock, isActiveLabel);
		
		//Add all to layout:
		buttonToolbar.addComponent(createNewCalendarPopUp);
		buttonToolbar.setExpandRatio(createNewCalendarPopUp, 0.0f);
    	
		buttonToolbar.addComponent(openCreateNewButton);
		buttonToolbar.setExpandRatio(openCreateNewButton, 0.0f);
    	
		buttonToolbar.addComponent(yearClockSelection);
		buttonToolbar.setExpandRatio(yearClockSelection, 0.0f);
		
		buttonToolbar.addComponent(remove);
		buttonToolbar.setExpandRatio(remove, 0.0f);
		
		buttonToolbar.addComponent(selectAsMainYearClock);
		buttonToolbar.setExpandRatio(selectAsMainYearClock, 0.0f);
		
		buttonToolbar.addComponent(isActiveLabel);
		buttonToolbar.setExpandRatio(isActiveLabel, 1.0f);
		
		yearClockEditLayout.setMargin(true);
		yearClockEditLayout.addComponent(buttonToolbar);
		yearClockEditLayout.setExpandRatio(buttonToolbar, 0.0f);
		yearClockEditLayout.addComponent(settingsCalendarView.getYearClock());
		yearClockEditLayout.setComponentAlignment(settingsCalendarView.getYearClock(), Alignment.TOP_CENTER);
		yearClockEditLayout.setExpandRatio(settingsCalendarView.getYearClock(), 1.0f);
    }
	
	private static void refreshSettingsSelectedCalendarYearView(Database database,
			CalendarBase calendarYear,
			Button removeButton,
			Button selectAsMainYearClockButton,
			Label isActiveLabel) {
		if(calendarYear != null) {
			removeButton.setEnabled(true);
			selectAsMainYearClockButton.setEnabled(!calendarYear.active);
			
			if(calendarYear.active) {
				isActiveLabel.setIcon(FontAwesome.CHECK_SQUARE_O);
				isActiveLabel.setCaption("Valittu vuosikello on p‰‰sivun aktiivinen vuosikello");
			} else {
				isActiveLabel.setIcon(FontAwesome.SQUARE_O);
				isActiveLabel.setCaption("Valittu vuosikello ei ole p‰‰sivun aktiivinen vuosikello.");
			}
		} else {
			removeButton.setEnabled(false);
			selectAsMainYearClockButton.setEnabled(false);
			isActiveLabel.setCaption("");
			isActiveLabel.setIcon(null);
		}
	}
	
	private static PopupView createNewCalendarPopupView(Main main, ComboBox existingYearsComboBox) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setSizeFull();
		
		PopupView popupView = new PopupView("", layout);
		popupView.setHideOnMouseOut(false);

    	TextField yearClockIdentifier = new TextField();
    	yearClockIdentifier.setWidth("100%");
		yearClockIdentifier.setCaption("Uuden vuosikellon nimi:");
    	yearClockIdentifier.setValue("");
    	
    	Button createButton = new Button();
    	
		popupView.addPopupVisibilityListener(new PopupVisibilityListener() {

			private static final long serialVersionUID = -3791852800092999277L;

			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(event.isPopupVisible()) {
					yearClockIdentifier.setValue("");
				}
			}
			
		});

    	createButton.setStyleName(ValoTheme.BUTTON_TINY);
    	createButton.setCaption("Luo uusi vuosikello");
    	createButton.setIcon(FontAwesome.PLUS);
    	createButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -181468310230998603L;

			@Override
			public void buttonClick(ClickEvent event) {
				String text = yearClockIdentifier.getValue();
				CalendarBase.create(main.getDatabase(), text);
				Updates.update(main, true);
				populateComboBoxWithCalendars(main.getDatabase(), existingYearsComboBox);
				popupView.setPopupVisible(false);
			}
    		
    	});
    	
    	Button close = new Button();
    	close.setStyleName(ValoTheme.BUTTON_TINY);
    	close.setCaption("Sulje");
    	close.setIcon(FontAwesome.CLOSE);
    	close.addClickListener(new ClickListener() {
    		
			private static final long serialVersionUID = -5346991398410812925L;

			@Override
			public void buttonClick(ClickEvent event) {
				popupView.setPopupVisible(false);
			}
    		
    	});
    	
    	layout.addComponent(yearClockIdentifier);
    	layout.addComponent(buttonToolbar);
    	layout.setExpandRatio(yearClockIdentifier, 0.0f);
    	layout.setExpandRatio(buttonToolbar, 0.0f);
    	layout.setComponentAlignment(yearClockIdentifier, Alignment.TOP_LEFT);
    	
    	buttonToolbar.addComponent(close);
    	buttonToolbar.setComponentAlignment(close, Alignment.MIDDLE_LEFT);
    	buttonToolbar.addComponent(createButton);
    	buttonToolbar.setExpandRatio(close, 0.0f);
    	buttonToolbar.setComponentAlignment(createButton, Alignment.MIDDLE_RIGHT);
    	buttonToolbar.setExpandRatio(createButton, 0.0f);
    	
		layout.setWidth("400px");
		layout.setHeight("150px");
    	return popupView;
	}
	
	private static CalendarBase populateComboBoxWithCalendars(Database database, ComboBox target) {
		target.removeAllItems();
		List<CalendarBase> alreadyExistingYears = CalendarBase.enumerate(database);
		CalendarBase active = null;
		for(CalendarBase yc : alreadyExistingYears) {
			if(yc.active) {
				active = yc;
			}
			String checkBox = yc.active ? "[X]" : "[ ]";
			target.addItem(yc);
			target.setItemCaption(yc, checkBox + " " + " | " + yc.text);
		}
		
		target.select(active);
		return active;
	}
	
	public Button createNewOpenPopupButton(String caption, Resource icon, String description) {
		Button b = new Button();
		b.setIcon(icon);
		b.setCaption(caption);
		b.setDescription(description);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 6353327872513838654L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(main.getDatabase())) {
					createAndOpenEditStartPageWindow(main);
				}
			}

		});
		
		return b;
	}

}
