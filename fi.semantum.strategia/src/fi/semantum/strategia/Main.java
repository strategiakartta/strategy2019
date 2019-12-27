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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.action.Action;
import fi.semantum.strategia.action.Actions;
import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategia.configurations.WebContent;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.filter.FilterState;
import fi.semantum.strategia.filter.NodeFilter;
import fi.semantum.strategia.filter.SearchFilter;
import fi.semantum.strategia.widget.Browser;
import fi.semantum.strategia.widget.Browser.BrowserListener;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.BrowserNodeState;
import fi.semantum.strategy.db.DBConfiguration;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.MapVisitor;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Property;
import fi.semantum.strategy.db.ResultAgreementConfiguration;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.ResultAgreementToolAdminBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.TimeConfiguration;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;

@SuppressWarnings("serial")
@Theme("fi_semantum_strategia")
@PreserveOnRefresh
public class Main extends UI {

	private Database database;
	public Account account;
	public String wikiToken;
	public List<List<String>> propertyCells = new ArrayList<List<String>>();

	public static final int TOOLBAR_HEIGHT = 40;

	public ScheduledExecutorService background = Executors.newScheduledThreadPool(1);

	public UIState uiState;
	private Map<UIState, NodeFilter> filterMap;

	public static File getAppFile(String path) {
		File mf = new File(Main.class.getResource("Main.class").getFile());
		File base = mf.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
		return new File(base, path);
	}

	public static String getPossibleWikiPrefix(Database database) {
		if (DBConfiguration.getDATABASE_ID().equals(database.getDatabaseId())) {
			return Configuration.getWIKI_PREFIX();
		} else {
			String pref = Configuration.getWIKI_PREFIX();
			if(pref == null) return null;
			else return pref + database.getDatabaseId() + "_";
		}
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public UIState getUIState() {
		return uiState;
	}

	public boolean acceptTime(String t) {
		return this.uiState.acceptTime(t);
	}

	public UIState duplicateUIState() {
		stateCounter++;
		return this.uiState.duplicateI(stateCounter);
	}

	public Account getAccountDefault() {
		if (account != null)
			return account;
		else
			return database.guest;
	}

	class SearchTextField extends TextField {
		public boolean hasFocus = false;
	}
	
	public int stateCounter = 1;

	public Map<String, UIState> fragments = new HashMap<String, UIState>();

	public AbsoluteLayout rootAbsoluteLayout;
	public Button less;
	public Button reportAllButton;
	public Button backToMapButton;
	public Label reportStatus;
	public boolean menuActive = false;

	Button backToStartPage;
	Button commentToolDialogOpener;
	MainAccountToolbar accountToolbar;

	final Browser browser_ = new Browser(new BrowserNode[0], new BrowserLink[0], 100, 100);
	VerticalLayout browser;
	VerticalLayout wiki_;
	BrowserFrame wiki;
	String wikiPage;
	MapBase wikiBase;
	HorizontalSplitPanel hs;
	Panel propertiesPanel;
	public VerticalLayout properties;
	VerticalLayout tags;
	VerticalLayout gridPanelLayout;
	Button more;
	Button hori;
	ComboBox filter;
	
	ValueChangeListener timesListener;
	ComboBox times;

	ValueChangeListener activeDocumentAndMapSelectorListener;
	ComboBox activeDocumentAndMapSelector;

	// Don't clear this when clearAll is called!
	MainStartPage startPage;

	HorizontalLayout toolbarDetachableLayout;
	VerticalLayout mainToolbarRootLayout;
	HorizontalLayout mainToolbar;
	VerticalLayout mapAndTabs;
	MainCommentLayout commentLayout;
	SearchTextField search;
	
	public MainCommentLayout getCommentLayout() {
		return commentLayout;
	}

	TabSheet allChaptersTabSheet;
	TabSheet allOfficesTabSheet;
	SelectedTabChangeListener allOfficesTabSheetTabChangeListener;
	
	VerticalLayout activeHTMLView;
	Map<Integer, ChapterEditorWrapperUI> chapterEditors = new HashMap<>();
	MainMapEditorUI mapEditorUI;

	MainDocumentPrintWindow documentPrintWindow;

	boolean filterListenerActive = true;
	ValueChangeListener filterListener = new ValueChangeListener() {

		public void valueChange(ValueChangeEvent event) {

			if (!filterListenerActive)
				return;

			Object value = filter.getValue();
			if (value == null)
				return;

			String selected = value.toString();

			for (NodeFilter f : availableFilters) {
				if (f.toString().equals(selected)) {
					setCurrentFilter(f);
					f.refresh();
					switchToBrowser();
					Updates.updateJS(Main.this, false);
					return;
				}
			}

		}

	};

	public int treeSplitPixelPosition = 200; //px
	private int windowWidth;
	private int windowHeight;

	/**
	 * Available window width in pixels for non-comment tool view
	 */
	public int nonCommentToolWidth;
	
	/**
	 * Available window width in pixels for map, which takes into account the tree and comment tool
	 */
	public int availableStrategyMapWidth;

	public int getWindowHeightPixels() {
		return Main.this.windowHeight;
	}

	public int getFullWindowWidth() {
		return Main.this.windowWidth;
	}

	private void setWindowWidth(int newWidth, int newHeight) {
		windowWidth = newWidth;
		windowHeight = newHeight;
	}

	WebContent webContent;

	String backFragment = ""; // Initially empty, will change to be the last fragment
	String printDocumentFragment = "tulostaTulossopimus";
	String startPageFragment = "aloitusSivu";

	public void setFragment(UIState state, boolean update) {
		setFragment(state, false, update);
	}

	public void setFragment(UIState state, boolean setPositions, boolean update) {
		if (uiState.name.equals(state.name))
			return;
		backFragment = uiState.name;
		fragments.put(state.name, state);
		getPage().setUriFragment(state.name);
		uiState = state;
		if (update || setPositions)
			Updates.update(this, setPositions, false);
	}

	public void applyFragment(String uuid, boolean update) {
		if (uiState.name.equals(uuid))
			return;

		// If we're currently showing the print fragment and attempting to show a
		// non-print fragment, hide the print fragment
		if (uiState.name.equals(printDocumentFragment) && !uuid.equals(printDocumentFragment)) {
			hideResultAgreementPrintView();
		}

		backFragment = uiState.name;
		UIState state = fragments.get(uuid);
		if (state == null)
			return;

		uiState = state;

		if (update) {
			Updates.update(this, false);
		}
	}

	private String validatePathInfo(String pathInfo) {
		if (pathInfo.isEmpty())
			return DBConfiguration.getDATABASE_ID();
		if (pathInfo.contains("/"))
			return DBConfiguration.getDATABASE_ID();
		if (pathInfo.length() > 32)
			return DBConfiguration.getDATABASE_ID();
		for (int i = 0; i < pathInfo.length(); i++) {
			char c = pathInfo.charAt(i);
			if (!Character.isJavaIdentifierPart(c))
				return DBConfiguration.getDATABASE_ID();
		}
		return pathInfo;
	}

	// private String resolveDatabase(so) {
	// String pathInfo = request.getPathInfo();
	// if(pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
	// if(pathInfo.endsWith("/")) pathInfo = pathInfo.substring(0, pathInfo.length()
	// - 1);
	// return validatePathInfo(pathInfo);
	// }

	@Override
	protected void refresh(VaadinRequest request) {
		super.refresh(request);
	}

	public NodeFilter getCurrentFilter() {
		return this.filterMap.get(this.uiState);
	}

	public void setCurrentFilter(NodeFilter filter) {
		this.filterMap.put(this.uiState, filter);
		uiState.currentFilterName = filter.toString();
	}

	@Override
	protected void init(VaadinRequest request) {
		try {
			// Test emergency behaviour by throwing a nullpointer before anything is loaded:
			//if("".equals("")) {
			//	throw new NullPointerException("");
			//}
			//Init main application
			init_(request);

		// Show emergency rollback DB option in case of unexpected exception:
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				String databaseId = databaseFromRequest(request);
				Database emergencyDatabase = DatabaseLoader.emergencyLoad(databaseId);
				if(emergencyDatabase != null) {
					HorizontalLayout adminLayout = MainManageUsersWindow.createEmergencyAdminDatabaseBackupView(request, this, emergencyDatabase, e);
					
					Label l = new Label("Tietokanta on viallisessa tilassa - kirjaudu sisään System/Admin tunnuksena ja palaa takaisin toimivaan versioon!");
					this.setContent(l);
					Login.createEmergencyLoginWindow(this, emergencyDatabase, adminLayout);
				} else {
					throw new NullPointerException("Expected emergency database to be non-null!");
				}
			} catch(Throwable e2) {
				System.err.println("Attempt to bring up emergency admin view failed!");
				e2.printStackTrace();
				throw e2;
			}
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	private String databaseFromRequest(VaadinRequest request) {
		getPage().addUriFragmentChangedListener(new UriFragmentChangedListener() {
			public void uriFragmentChanged(UriFragmentChangedEvent source) {
				applyFragment(source.getUriFragment(), true);
			}
		});

		String pathInfo = request.getPathInfo();
		if (pathInfo.startsWith("/fi.semantum.strategia/"))
			pathInfo = pathInfo.substring("/fi.semantum.strategia/".length());
		if (pathInfo.startsWith("/"))
			pathInfo = pathInfo.substring(1);
		if (pathInfo.endsWith("/"))
			pathInfo = pathInfo.substring(0, pathInfo.length() - 1);

		return validatePathInfo(pathInfo);
	}
	
	private void init_(VaadinRequest request) {
		this.uiState = new UIState();
		filterMap = new HashMap<>();

		String databaseId = databaseFromRequest(request);
		database = DatabaseLoader.load(this, databaseId);
		
		this.uiState.setTime(TimeConfiguration.getInstance(database).getAllConfiguredYears());
		
		setWindowWidth(Page.getCurrent().getBrowserWindowWidth(), Page.getCurrent().getBrowserWindowHeight());
		Main.this.updateAvailableWindowWidthCache();

		// Web Content initialization for later use
		webContent = new WebContent();

		// This will set the login cookie
		if(Wiki.exists()) {
			Wiki.login(this);
		}
		
		// Make sure that the printing directory exists
		database.getOrCreateTag(Terminology.GOAL_OR_TARGET);

		database.getOrCreateTag(Terminology.TAG_FOCUS_POINT);

		// DON'T ENABLE IN ANY ACTUAL BUILD!!!!!!!");
		// account = Account.find(database, DBConfiguration.getADMIN_ACCOUNT_NAME());

		startPage = new MainStartPage(Main.this);
		startPage.show();

		// TODO: Remove:
		// startPage.hide(); //Open map directly
		// TODO: Remove:
		// handleToggleCommentTool(); //Open comment tool directly
	}

	/**
	 * Find all visible elements
	 * 
	 * @return
	 */
	public Set<String> getVisibleElements() {
		Set<String> result = new HashSet<>();

		if (allChaptersTabSheet != null && Main.this.getUIState().showSelectedDocuments) {
			int chapterIndex = allChaptersTabSheet
					.getTabPosition(allChaptersTabSheet.getTab(allChaptersTabSheet.getSelectedTab()));
			if (chapterIndex != -1) {
				ChapterEditorWrapperUI editor = Main.this.chapterEditors.get(chapterIndex);
				result.add(editor.getMarkdownToHTMLContent().uuid);
			}
		} else {
			addAllVisibleMapElements(result);
		}

		return result;
	}

	/**
	 * Appends all visible map elements to the provided result set.
	 * @param result
	 */
	private void addAllVisibleMapElements(Set<String> result) {
		StrategyMap currentMap = uiState.getCurrentMap();

		if (currentMap != null) {
			result.add(currentMap.uuid);
			for (OuterBox ob : currentMap.outerBoxes) {
				result.add(ob.uuid);
				
				Base targetObBase = MapBase.findTargetedMapBoxElement(database, ob);
				if(targetObBase != null) result.add(targetObBase.uuid);
				
				Base leafOuter = hasLeaf(ob);
				if (leafOuter != null) result.add(leafOuter.uuid);
				
				for (InnerBox ib : ob.innerboxes) {
					result.add(ib.uuid);
					
					Base targetIbBase = MapBase.findTargetedMapBoxElement(database, ib);
					if(targetIbBase != null) result.add(targetIbBase.uuid);
					
					Base leafInner = hasLeaf(ib);
					if (leafInner != null) result.add(leafInner.uuid);

				}
			}

		}

		Base mapItem = uiState.currentMapItem;
		StrategyMap currentMapPosition = uiState.currentMapPosition;

		result.add(currentMapPosition.uuid);
		result.add(mapItem.uuid);
	}

	/**
	 * Clear everything related to the Main and map view, except the start page!
	 */
	public void clearAll() {
		Main.this.setContent(null);
		Utils.clearAndNullify(backToStartPage);
		Utils.clearAndNullify(rootAbsoluteLayout);
		Utils.clearAndNullify(less);
		Utils.clearAndNullify(reportAllButton);
		Utils.clearAndNullify(backToMapButton);
		Utils.clearAndNullify(reportStatus);
		menuActive = false;

		Utils.clearAndNullify(commentToolDialogOpener);

		if (accountToolbar != null) {
			Utils.clearAndNullify(accountToolbar.manageUsers);
			Utils.clearAndNullify(accountToolbar.accountSettings);
			Utils.clearAndNullify(accountToolbar.login);
		}
		accountToolbar = null;

		Utils.clearAndNullify(browser);
		Utils.clearAndNullify(wiki_);
		Utils.clearAndNullify(wiki);
		wikiPage = null;
		wikiBase = null;
		Utils.clearAndNullify(hs);
		Utils.clearAndNullify(propertiesPanel);
		Utils.clearAndNullify(properties);
		Utils.clearAndNullify(tags);
		Utils.clearAndNullify(gridPanelLayout);
		Utils.clearAndNullify(more);
		Utils.clearAndNullify(hori);
		Utils.clearAndNullify(filter);
		Utils.clearAndNullify(times);
		Utils.clearAndNullify(activeDocumentAndMapSelector);

		Utils.clearAndNullify(mapAndTabs);
		if (commentLayout != null) {
			commentLayout.cleanup();
			commentLayout = null;
		}
		Utils.clearAndNullify(allChaptersTabSheet);
		Utils.clearAndNullify(allOfficesTabSheet);
		Utils.clearAndNullify(activeHTMLView);
		chapterEditors = new HashMap<>();
		if (mapEditorUI != null) {
			mapEditorUI.clearAll();
			mapEditorUI = null;
		}
		Utils.clearAndNullify(documentPrintWindow);
	}

	private void handleToggleCommentTool() {
		commentLayout.toggleOpenClose();
		commentToolDialogOpener.setCaption(commentLayout.buttonText());
		refreshAllChapterEditors();
		refreshCommentToolFullWindow();
		refreshAllSizes(); // Always refresh size after toggle!
	}
	
	public void pollDatabase() {
		
		if (database.checkChanges()) {
			String possibleCurrentMapUUID = uiState.startPageVisible ? database.getRoot().uuid : uiState.getCurrentMap().uuid;
			
			String possibleAccountUUID = Main.this.account == null ? null : Main.this.account.uuid;
			database = DatabaseLoader.load(Main.this, database.getDatabaseId());

			if (possibleAccountUUID != null) {
				for (Account candidate : Account.enumerate(database)) {
					if (candidate.uuid.equals(possibleAccountUUID)) {
						Main.this.account = candidate;
						break;
					}
				}
			}

			uiState.setCurrentMap((StrategyMap) database.find(possibleCurrentMapUUID));
			Updates.updateJS(Main.this, false);
		}

	}

	public void loadMainView() {
		clearAll();

		rootAbsoluteLayout = new AbsoluteLayout();
		rootAbsoluteLayout.setSizeFull();
		rootAbsoluteLayout.setId("rootAbsoluteLayout");
		Main.this.setSizeFull();
		Main.this.setContent(rootAbsoluteLayout);

		final VerticalLayout rootVerticalLayout = new VerticalLayout();
		rootVerticalLayout.setSizeFull();

		rootAbsoluteLayout.addComponent(rootVerticalLayout);

		if (uiState.getCurrentMap() == null)
			uiState.setCurrentMap(database.getRoot());

		uiState.currentMapPosition = uiState.getCurrentMap();

		uiState.currentMapItem = uiState.getCurrentMap();

		setPollInterval(10000);

		addPollListener(new PollListener() {

			@Override
			public void poll(PollEvent event) {
				pollDatabase();
			}

		});

		browser_.addListener(new BrowserListener() {

			@Override
			public void select(double x, double y, String uuid) {
				MapBase b = database.find(uuid);
				Actions.selectAction(Main.this, x, y, null, b);
			}

			@Override
			public void save(String name, Map<String, BrowserNodeState> states) {

				UIState state = getUIState().duplicateS(name);
				state.browserStates = states;

				account.uiStates.add(state);

				Updates.update(Main.this, true);

			}

		});

		Page.getCurrent().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {

			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event) {
				setWindowWidth(event.getWidth(), event.getHeight());
				applyResizedToolbar();
				refreshAllSizes();
			}
		});

		mapEditorUI = new MainMapEditorUI(Main.this);
		backToStartPage = MainStartPage.createNewBackToStartPageButton(Main.this);

		OnDemandFileDownloader dl = new OnDemandFileDownloader(new OnDemandStreamSource() {

			private static final long serialVersionUID = 981769438054780731L;

			File f;
			Date date = new Date();

			@Override
			public InputStream getStream() {

				String uuid = UUID.randomUUID().toString();
				File printing = database.getPrintingDirectory();
				f = new File(printing, uuid + ".xlsx");

				Workbook w = new XSSFWorkbook();
				Sheet sheet = w.createSheet("Sheet1");
				int row = 1;
				for (List<String> cells : propertyCells) {
					Row r = sheet.createRow(row++);
					for (int i = 0; i < cells.size(); i++) {
						String value = cells.get(i);
						r.createCell(i).setCellValue(value);
					}
				}

				try {
					FileOutputStream s = new FileOutputStream(f);
					w.write(s);
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				throw new IllegalStateException();

			}

			@Override
			public void onRequest() {
			}

			@Override
			public long getFileSize() {
				return f.length();
			}

			@Override
			public String getFileName() {
				return "Strategiakartta_" + UtilsDB.dateString(date) + ".xlsx";
			}

		});

		dl.getResource().setCacheTime(0);
		dl.extend(mapEditorUI.propertyExcelButton);

		search = new SearchTextField();
		search.addStyleName(ValoTheme.TEXTFIELD_TINY);
		search.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
		search.setInputPrompt("hae vapaasanahaulla valitun asian alta");
		search.setId("searchTextField");
		search.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {

				if (!search.hasFocus)
					return;

				String text = search.getValue().toLowerCase();
				try {

					Map<String, String> content = new HashMap<String, String>();
					List<String> hits = database.searchFileSystem(text + "*");
					
					//TODO: Search all desc, text, getText(db), and more for all DB objects.
					//
					for (String uuid : hits) {
						MapBase b = database.find(uuid);
						if (b != null) {
							String report = "";
							Map<String, String> map = b.searchMap(database);
							for (Map.Entry<String, String> e : map.entrySet()) {
								if (e.getValue().contains(text)) {
									if (!report.isEmpty())
										report += ", ";
									report += e.getKey();
								}
							}
							if (!report.isEmpty())
								content.put(uuid, report);
						}
					}
					
					// Prune implementations
					Set<String> keys = new HashSet<String>(content.keySet());
					for(String uuid : keys) {
						Base b = database.find(uuid);
						for(Base imp : b.getImplementedSet(database)) {
							if(keys.contains(imp.uuid))
								content.remove(uuid);
						}
					}
					

					setCurrentFilter(new SearchFilter(Main.this, content));

					Updates.updateJS(Main.this, false);

					switchToBrowser();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		search.addFocusListener(new FocusListener() {

			@Override
			public void focus(FocusEvent event) {
				search.hasFocus = true;
			}
		});
		search.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				search.hasFocus = false;
			}
		});

		commentLayout = new MainCommentLayout(Main.this);

		commentToolDialogOpener = new Button(commentLayout.buttonText());
		commentToolDialogOpener.setWidthUndefined();
		commentToolDialogOpener.setVisible(false);
		commentToolDialogOpener.addStyleName(ValoTheme.BUTTON_TINY);
		commentToolDialogOpener.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				handleToggleCommentTool();
			}

		});

		initTimesComboBox();
		accountToolbar = new MainAccountToolbar(Main.this);


		toolbarDetachableLayout = new HorizontalLayout();
		toolbarDetachableLayout.setSpacing(true);
		
		mainToolbarRootLayout = new VerticalLayout();
		mainToolbarRootLayout.setHeight(TOOLBAR_HEIGHT, Unit.PIXELS);
		mainToolbarRootLayout.setWidth("100%");
		mainToolbarRootLayout.addStyleName(CustomCSSStyleNames.STYLE_MAIN_TOOLBAR);
		
		mainToolbar = new HorizontalLayout();
		mainToolbar.addStyleName(CustomCSSStyleNames.STYLE_MAIN_TOOLBAR);
		mainToolbar.setWidth("100%");
		mainToolbar.setHeight(TOOLBAR_HEIGHT, Unit.PIXELS);
		mainToolbar.setSpacing(true);

		mainToolbar.addComponent(backToStartPage);
		mainToolbar.setComponentAlignment(backToStartPage, Alignment.MIDDLE_LEFT);
		mainToolbar.setExpandRatio(backToStartPage, 0.0f);

		// Initialize the active document/map selector
		createActiveDocumentAndMapSelector();

		mainToolbar.addComponent(activeDocumentAndMapSelector);
		mainToolbar.setComponentAlignment(activeDocumentAndMapSelector, Alignment.MIDDLE_LEFT);
		mainToolbar.setExpandRatio(activeDocumentAndMapSelector, 1.0f);

		mainToolbar.addComponent(times);
		mainToolbar.setComponentAlignment(times, Alignment.MIDDLE_RIGHT);
		mainToolbar.setExpandRatio(times, 0.0f);
		times.setWidth("80px");

		mainToolbar.addComponent(search);
		mainToolbar.setComponentAlignment(search, Alignment.MIDDLE_RIGHT);
		mainToolbar.setExpandRatio(search, 0.0f);
		
		toolbarDetachableLayout.addComponent(commentToolDialogOpener);
		toolbarDetachableLayout.setComponentAlignment(commentToolDialogOpener, Alignment.MIDDLE_RIGHT);
		toolbarDetachableLayout.setExpandRatio(commentToolDialogOpener, 0.0f);

		accountToolbar.attachToLayout(toolbarDetachableLayout);
		
		applyResizedToolbar();

		propertiesPanel = new Panel();
		propertiesPanel.setSizeFull();

		properties = new VerticalLayout();
		properties.setSpacing(true);
		properties.setMargin(true);

		propertiesPanel.setContent(properties);
		propertiesPanel.setVisible(false);

		tags = new VerticalLayout();
		tags.setSpacing(true);
		Updates.updateTags(this);

		{
			mapEditorUI.initJS();
		}

		wiki = new BrowserFrame();
		wiki.setVisible(Wiki.exists());
		wiki.setEnabled(Wiki.exists());
		
		if(Wiki.exists()) {
			wiki.setSource(new ExternalResource(Wiki.wikiAddress() + "/"));	
		}
		wiki.setWidth("100%");
		wiki.setHeight("100%");

		{

			wiki_ = new VerticalLayout();
			wiki_.setSizeFull();
			Button b = new Button("Palaa sovellukseen");
			b.setWidth("100%");
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					applyFragment(backFragment, true);
					String content = Wiki.get(wikiPage);
					if (content == null)
						return;
					int first = content.indexOf("<rev contentformat");
					if (first == -1)
						return;
					content = content.substring(first);
					int term = content.indexOf(">");
					content = content.substring(term + 1);
					int end = content.indexOf("</rev>");
					content = content.substring(0, end);
					if (wikiBase.modifyMarkup(getDatabase(), getAccountDefault(), content)) {
						Updates.update(Main.this, true);
					}
				}
			});
			wiki_.addComponent(b);
			wiki_.addComponent(wiki);
			wiki_.setVisible(false);

			wiki_.setExpandRatio(b, 0.0f);
			wiki_.setExpandRatio(wiki, 1.0f);

			mapEditorUI.strategyMapJSView.addComponent(wiki_);

		}

		hs = new HorizontalSplitPanel();
		hs.setSplitPosition(0, Unit.PIXELS);
		hs.setHeight("100%");
		hs.setWidth("100%");

		browser = new VerticalLayout();
		browser.setSizeFull();

		HorizontalLayout browserWidgets = new HorizontalLayout();
		browserWidgets.setWidth("100%");
		browserWidgets.setSpacing(true);

		hori = new Button();
		hori.setDescription("Näytä asiat taulukkona");
		hori.setEnabled(true);
		hori.setIcon(FontAwesome.ARROW_RIGHT);
		hori.addStyleName(ValoTheme.BUTTON_TINY);
		hori.addClickListener(new ClickListener() {

			boolean right = false;

			@Override
			public void buttonClick(ClickEvent event) {
				if (right) {
					hs.setSplitPosition(0, Unit.PIXELS);
					hori.setIcon(FontAwesome.ARROW_RIGHT);
					hori.setDescription("Näytä asiat taulukkona");
					right = false;
				} else {
					hs.setSplitPosition(windowWidth / 2, Unit.PIXELS);
					hori.setIcon(FontAwesome.ARROW_LEFT);
					hori.setDescription("Piilota taulukko");
					right = true;
				}
			}

		});

		more = new Button();
		more.setDescription("Laajenna näytettävien asioiden joukkoa");
		more.setIcon(FontAwesome.PLUS_SQUARE);
		more.addStyleName(ValoTheme.BUTTON_TINY);
		more.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				uiState.mapLevel++;
				Updates.updateJS(Main.this, false);
				if (uiState.mapLevel >= 2)
					less.setEnabled(true);
			}

		});

		less = new Button();
		less.setDescription("Supista näytettävien asioiden joukkoa");
		less.setIcon(FontAwesome.MINUS_SQUARE);
		less.addStyleName(ValoTheme.BUTTON_TINY);
		less.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (uiState.mapLevel > 1) {
					uiState.mapLevel--;
					Updates.updateJS(Main.this, false);
				}
				if (uiState.mapLevel <= 1)
					less.setEnabled(false);
			}

		});

		reportAllButton = new Button();
		reportAllButton.setCaption("Näkyvät tulokset");
		reportAllButton.addStyleName(ValoTheme.BUTTON_TINY);
		reportAllButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (uiState.reportAll) {
					reportAllButton.setCaption("Näkyvät tulokset");
					uiState.reportAll = false;
				} else {
					reportAllButton.setCaption("Kaikki tulokset");
					uiState.reportAll = true;
				}
				Updates.updateJS(Main.this, false);
			}

		});

		backToMapButton = new Button();
		backToMapButton.setCaption("Takaisin karttaan");
		backToMapButton.addStyleName(ValoTheme.BUTTON_TINY);
		backToMapButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				switchToMapAndDocumentsView();
			}

		});

		reportStatus = new Label("0 tulosta.");
		reportStatus.setWidth("100px");

		filter = new ComboBox();
		filter.setWidth("100%");
		filter.addStyleName(ValoTheme.COMBOBOX_SMALL);
		filter.setInvalidAllowed(false);
		filter.setNullSelectionAllowed(false);

		filter.addValueChangeListener(filterListener);

		browserWidgets.addComponent(backToMapButton);
		browserWidgets.setComponentAlignment(backToMapButton, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(backToMapButton, 0.0f);

		browserWidgets.addComponent(hori);
		browserWidgets.setComponentAlignment(hori, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(hori, 0.0f);

		browserWidgets.addComponent(more);
		browserWidgets.setComponentAlignment(more, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(more, 0.0f);

		browserWidgets.addComponent(less);
		browserWidgets.setComponentAlignment(less, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(less, 0.0f);

		browserWidgets.addComponent(reportAllButton);
		browserWidgets.setComponentAlignment(reportAllButton, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(reportAllButton, 0.0f);

		browserWidgets.addComponent(reportStatus);
		browserWidgets.setComponentAlignment(reportStatus, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(reportStatus, 0.0f);

		browserWidgets.addComponent(filter);
		browserWidgets.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
		browserWidgets.setExpandRatio(filter, 1.0f);

		browser.addComponent(browserWidgets);
		browser.addComponent(hs);

		browser.setExpandRatio(browserWidgets, 0.0f);
		browser.setExpandRatio(hs, 1.0f);

		browser.setVisible(false);

		mapEditorUI.strategyMapJSView.addComponent(browser);

		{
			gridPanelLayout = new VerticalLayout();
			gridPanelLayout.setMargin(false);
			gridPanelLayout.setSpacing(false);
			gridPanelLayout.setSizeFull();
			hs.addComponent(gridPanelLayout);
		}

		CssLayout browserLayout = new CssLayout();

		browserLayout.setSizeFull();

		browserLayout.addComponent(browser_);

		hs.addComponent(browserLayout);

		mapEditorUI.strategyMapJSView.addComponent(propertiesPanel);
		mapEditorUI.strategyMapJSView.setSizeFull();

		documentPrintWindow = new MainDocumentPrintWindow(Main.this);

		// Root layout and elements:

		rootVerticalLayout.setSizeFull();

		// Add toolbar that's always visible to the root top
		mainToolbarRootLayout.addComponent(mainToolbar);
		mainToolbarRootLayout.setExpandRatio(mainToolbar, 0.0f);
		mainToolbarRootLayout.setComponentAlignment(mainToolbar, Alignment.TOP_RIGHT);
		
		rootVerticalLayout.addComponent(mainToolbarRootLayout);
		rootVerticalLayout.setExpandRatio(mainToolbarRootLayout, 0.0f);
		rootVerticalLayout.setComponentAlignment(mainToolbarRootLayout, Alignment.TOP_RIGHT);

		// Separate the main view into map navigation and the comment tool
		HorizontalLayout mainView = new HorizontalLayout();
		mainView.setSizeFull();
		mainView.setId("mainView");
		rootVerticalLayout.addComponent(mainView);
		rootVerticalLayout.setExpandRatio(mainView, 1.0f);

		mapAndTabs = new VerticalLayout();
		mapAndTabs.setSizeFull();
		mapAndTabs.setId("mapAndTabs");

		VerticalLayout commentRootLayout = commentLayout.getRoot();

		// Add main view and comment tool to the main view that's under the root with
		// the main toolbar
		mainView.addComponent(mapAndTabs);
		mainView.setComponentAlignment(mapAndTabs, Alignment.TOP_LEFT);
		mainView.setExpandRatio(mapAndTabs, 1.0f);
		
		mainView.addComponent(commentRootLayout);
		mainView.setComponentAlignment(commentRootLayout, Alignment.TOP_RIGHT);
		mainView.setExpandRatio(commentRootLayout, 0.0f);

		//mainView.setExpandRatio(mapAndTabs, 0.0f);
		//mainView.setExpandRatio(commentRootLayout, 1.0f);

		// Initialize and populate view according to if a ResultsAgreementDocument is
		// present

		initResultAgreementView();
		//updateCurrentResultAgreementSelectionAndView();

		// Ground state
		fragments.put("", uiState);

		// Switch to current year, if possible
		String currentYear = Utils.getCurrentYear(Main.this);
		times.setValue(currentYear); // This fails silently if the year doesn't exist

		setCurrentItem(uiState.currentMapItem, (StrategyMap) uiState.currentMapItem);
		populateActiveDocumentAndMapSelector();
		forceSwitchToMapView();

		Login.refreshUserUI(Main.this);
	}

	
	private void applyResizedToolbar() {
		int w = getFullWindowWidth();
		
		if(w < 1500) {
			if(w < 850) {
				search.setWidth("200px");
			} else {
				search.setWidth("350px");
			}
			if(mainToolbarRootLayout.getComponentIndex(toolbarDetachableLayout) == -1) {
				mainToolbarRootLayout.setHeight(TOOLBAR_HEIGHT*2, Unit.PIXELS);
				mainToolbarRootLayout.addComponent(toolbarDetachableLayout, 0);
				mainToolbarRootLayout.setExpandRatio(toolbarDetachableLayout, 0.0f);
				mainToolbarRootLayout.setComponentAlignment(toolbarDetachableLayout, Alignment.MIDDLE_RIGHT);	
			}
		} else {
			search.setWidth("350px");
			
			if(mainToolbar.getComponentIndex(toolbarDetachableLayout) == -1) {
				mainToolbarRootLayout.setHeight(TOOLBAR_HEIGHT, Unit.PIXELS);
				mainToolbar.addComponent(toolbarDetachableLayout);
				mainToolbar.setExpandRatio(toolbarDetachableLayout, 0.0f);
				mainToolbar.setComponentAlignment(toolbarDetachableLayout, Alignment.MIDDLE_RIGHT);
			}
		}
	}
	
	public void refreshCommentToolFullWindow() {
		boolean visible = !(getUIState().commentLayoutOpen && getUIState().commentLayoutFullWindow);
		mapEditorUI.mapToolbar.setVisible(visible);
	}

	private void createActiveDocumentAndMapSelector() {
		activeDocumentAndMapSelector = new ComboBox();
		activeDocumentAndMapSelector.setNullSelectionAllowed(false);
		activeDocumentAndMapSelector.setStyleName(ValoTheme.COMBOBOX_TINY);
		activeDocumentAndMapSelector.setWidth("200px");

		activeDocumentAndMapSelectorListener = new ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = activeDocumentAndMapSelector.getValue();
				if (value != null) {
					if (value instanceof Base) {
						if (value instanceof ResultAgreementDocuments) {
							Main.this.getUIState().showSelectedDocuments = true;
							updateRootMapView(ResultAgreementDocuments.getInstance(getDatabase()));
						} else if (value instanceof StrategyMap) {
							Main.this.getUIState().showSelectedDocuments = false;
							updateRootMapView(null);
						} else {
							System.err.println("FATAL ERROR - expected type Map or Document in activeDocumentAndMapSelector, got neither!");
						}
						// Update UI
						refreshAllSizes();
					} else {
						System.err.println("FATAL ERROR - expected type Base in activeDocumentAndMapSelector!");
					}
				}
			}

		};

		activeDocumentAndMapSelector.addValueChangeListener(activeDocumentAndMapSelectorListener);

	}

	public void handleLoginUIRefresh() {
		if (account != null) {
			this.accountToolbar.login();
			this.updateCommentToolVisibility();
			Updates.update(this, false);

			this.mapEditorUI.saveState.setEnabled(true);
			this.refreshAllChapterEditors();
		} else {
			handleLogoutUIRefresh();
		}
	}

	public void handleLogoutUIRefresh() {
		this.accountToolbar.logout();
		this.commentLayout.cleanup();
		this.commentToolDialogOpener.setCaption(this.commentLayout.buttonText());
		this.updateCommentToolVisibility();
		// Updates.update(main, true);
		this.refreshAllChapterEditors();
		startPage.show();
	}

	/**
	 * Attempt to switch to the map chapter tab
	 */
	public void forceSwitchToMapView() {
		activeDocumentAndMapSelector.select(database.getRoot());
	}
	
	public void forceSwitchToResultAgreementDocuments() {
		activeDocumentAndMapSelector.select(UtilsDB.getPossibleLinkedResultAgreementDocuments(database, database.getRoot()));
	}
	
	public void repopulateOfficesTabSheet() {
		List<Office> offices = officesVisibleToUser();
		if (offices.size() != 0) {
			int tabCount = allOfficesTabSheet.getComponentCount();
			if(tabCount != offices.size()) {
				allOfficesTabSheet.removeSelectedTabChangeListener(allOfficesTabSheetTabChangeListener);
				allOfficesTabSheet.removeAllComponents();
				
				int i = 0;
				for (Office office : offices) {
					if(!office.isController) {
						HorizontalLayout empty = new HorizontalLayout();
						Tab addedTab = allOfficesTabSheet.addTab(empty, office.getText(database), null, i);
						if (addedTab != null) {
							String label = SafeHtmlUtils.htmlEscape(office.getText(database));
							addedTab.setCaption(
									"<div align=\"left\" style=\"line-height: 100%; margin-bottom: 2px;\"><font size=\"+1\">"
											+ label + "</font></div>");
							i++;
						}
					}
				}
				
				allOfficesTabSheet.addSelectedTabChangeListener(allOfficesTabSheetTabChangeListener);
				this.setCurrentOfficeFromTabSheet(0);
			}
		} else {
			allOfficesTabSheet.removeSelectedTabChangeListener(allOfficesTabSheetTabChangeListener);
			allOfficesTabSheet.removeAllComponents();
			this.uiState.currentOffice = null;
		}
	}
	
	private void initResultAgreementView() {
		allOfficesTabSheet = new TabSheet();
		allOfficesTabSheet.setWidth("100%");
		allOfficesTabSheet.setHeight("32px");
		allOfficesTabSheet.setTabCaptionsAsHtml(true);
		
		allOfficesTabSheetTabChangeListener = new SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Database db = database; // Ensure the DB stays the same during the entire event

				// Fetch current active RAD:
				ResultAgreementDocuments currentDocuments = ResultAgreementDocuments.getInstance(getDatabase());
				if (currentDocuments != null && !Main.this.chapterEditors.isEmpty()) {
					// Switch current office to the selected tab
					int index = event.getTabSheet()
							.getTabPosition(event.getTabSheet().getTab(event.getTabSheet().getSelectedTab()));
					setCurrentOfficeFromTabSheet(index);

					// Find the active office and document
					Office currentOffice = Main.this.getUIState().getCurrentOffice();

					for (int chapterIndex : currentDocuments.getSharedChapterIndeces(db)) {
						TextChapter chapter = currentDocuments.getPossibleChapterByOffice(db, chapterIndex,
								currentOffice);
						ChapterEditorWrapperUI editor = Main.this.chapterEditors.get(chapterIndex);
						editor.switchChapter(chapter);
					}

					for (int chapterIndex : currentDocuments.getOfficeChapterIndeces(db)) {
						TextChapter chapter = currentDocuments.getPossibleChapterByOffice(db, chapterIndex,
								currentOffice);
						ChapterEditorWrapperUI editor = Main.this.chapterEditors.get(chapterIndex);
						if(editor != null) {
							editor.switchChapter(chapter);
						} else {
							System.err.println("Found no MainChapterEditor from map, for index " + chapterIndex + " in initResultAgreementView!");
						}
					}

				} else {
					if(!Main.this.chapterEditors.isEmpty()) {
						System.err.println("No chapter editors found in initResultAgreementView!");
					} else {
						System.err.println("Selected document was null in initResultAgreementView!");
					}
				}
			}
		};
		
		repopulateOfficesTabSheet();
		
		activeHTMLView = new VerticalLayout();
		activeHTMLView.setSpacing(true);

		allChaptersTabSheet = new TabSheet();
		allChaptersTabSheet.setSizeFull();
		allChaptersTabSheet.setTabCaptionsAsHtml(true);

		allChaptersTabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				int chapterIndex = event.getTabSheet()
						.getTabPosition(event.getTabSheet().getTab(event.getTabSheet().getSelectedTab()));
				ChapterEditorWrapperUI editor = chapterEditors.get(chapterIndex);
				if (editor != null) {
					editor.updateUI(true);
				}
				// Hide the offices tab if in map view - not needed and takes up space
				ResultAgreementDocuments currentDocument = ResultAgreementDocuments.getInstance(getDatabase());

				if (currentDocument != null) {
					allOfficesTabSheet.setVisible(true);
				} else {
					allOfficesTabSheet.setVisible(false);
				}
			}
		});

		commentLayout.attachTabSheetListeners(allOfficesTabSheet, allChaptersTabSheet);

	}

	public void populateActiveDocumentAndMapSelector() {
		this.commentLayout.setUpdateOngoing(true);

		try {
			ResultAgreementDocuments selectableDocument = ResultAgreementDocuments.getInstance(getDatabase());
			StrategyMap map = database.getRoot();

			activeDocumentAndMapSelector.removeAllItems();

			// By default cannot read
			boolean user_can_read_document = false;
			if(selectableDocument != null) {
				if(this.getAccountDefault().isAdmin(database) || ResultAgreementToolAdminBase.canRead(database, this.getAccountDefault())) {
					user_can_read_document = true;
				} else {
					user_can_read_document = Utils.canRead(this, selectableDocument);
					
					if(user_can_read_document && officesVisibleToUser().isEmpty()) {
						user_can_read_document = false; //Nothing to view
					}
				}
			}
			
			if (! user_can_read_document) {
				activeDocumentAndMapSelector.addItem(map);
				activeDocumentAndMapSelector.setItemCaption(map, "Karttanäkymä");
				activeDocumentAndMapSelector.select(map);
				// Switch to hide office and chapter sheets and show map in root
				updateRootMapView(null);
			} else {
				activeDocumentAndMapSelector.addItem(map);
				activeDocumentAndMapSelector.setItemCaption(map, "Karttanäkymä");
				activeDocumentAndMapSelector.addItem(selectableDocument);
				activeDocumentAndMapSelector.setItemCaption(selectableDocument,
						"Tulossopimus");
				activeDocumentAndMapSelector.select(map);
				updateRootMapView(selectableDocument);
			}

			if (this.uiState.commentLayoutOpen) {
				this.commentLayout.refresh();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			this.commentLayout.setUpdateOngoing(false);
		}
	}

	private void clearMapAndTabsRoot() {
		if (chapterEditors != null)
			chapterEditors.clear();
		if (allChaptersTabSheet != null)
			allChaptersTabSheet.removeAllComponents();

		try {
			mapAndTabs.removeComponent(allOfficesTabSheet);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			mapAndTabs.removeComponent(allChaptersTabSheet);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			mapAndTabs.removeComponent(mapEditorUI.root);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void updateRootMapView(ResultAgreementDocuments currentDocument) {
		clearMapAndTabsRoot();
		if (currentDocument == null || !Main.this.getUIState().showSelectedDocuments) {
			times.setEnabled(true);
			mapAndTabs.addComponent(mapEditorUI.root);
			mapAndTabs.setExpandRatio(mapEditorUI.root, 1.0f);
		} else {
			times.setEnabled(false);
			repopulateOfficesTabSheet();
			
			mapAndTabs.addComponent(allOfficesTabSheet);
			mapAndTabs.setExpandRatio(allOfficesTabSheet, 0.0f);
			mapAndTabs.addComponent(allChaptersTabSheet);
			mapAndTabs.setExpandRatio(allChaptersTabSheet, 1.0f);

			repopulateEditorsAndChapterTabs(currentDocument);
			
			// Switch back to what was shown earlier, if possible:
			if(uiState.currentOffice != null) {
				List<Office> offices = officesVisibleToUser();
				int i = offices.indexOf(uiState.currentOffice);
				if(i != -1 && allOfficesTabSheet.getComponentCount() > i) {
					allOfficesTabSheet.setSelectedTab(i);
				}
			}
		}
	}

	private void repopulateEditorsAndChapterTabs(ResultAgreementDocuments currentDocument) {
		Map<Integer, TextChapter> activeSharedTextChapters = currentDocument.getSharedChapters(database);
		Map<Integer, List<TextChapter>> activeOfficeTextChaptersList = currentDocument.getOfficeChapters(database);
		Set<Integer> indexSet = currentDocument.getAllIndeces(database);
		Integer[] expectedIndexes = ResultAgreementConfiguration.getInstance(database).getAllIndeces();
		chapterEditors.clear();
		allChaptersTabSheet.removeAllComponents();
		
		for (int i : expectedIndexes) {
			boolean is_shared = false;
			
			Tab addedTab = null;
			if (!indexSet.contains(i) || allChaptersTabSheet.getComponentCount() < i) {
				// Failure state - cannot add!
				System.err.println("Missing chapter in index " + i + " for resultagreement " + currentDocument.uuid);
			} else {
				if (activeSharedTextChapters.containsKey(i)) {
					is_shared = true;
					TextChapter chapter = activeSharedTextChapters.get(i);
					ChapterEditorWrapperUI editor = new ChapterEditorWrapperUI(Main.this, chapter);
					chapterEditors.put(chapter.chapterIndex, editor);
					addedTab = allChaptersTabSheet.addTab(editor.getRoot(), "" + i, null, i);
				} else if (activeOfficeTextChaptersList.containsKey(i)) {
					List<TextChapter> chapterForIndexList = activeOfficeTextChaptersList.get(i);

					if (chapterForIndexList.size() != 0) {
						Office office = uiState.getCurrentOffice();
						if(office != null) {
							TextChapter chapter = currentDocument.getPossibleChapterByOffice(database, i, office);
							if(chapter != null) {
								ChapterEditorWrapperUI editor = new ChapterEditorWrapperUI(Main.this, chapter);
								chapterEditors.put(chapter.chapterIndex, editor);
								addedTab = allChaptersTabSheet.addTab(editor.getRoot(), "" + i, null, i);
							} else {
								System.err.println("Chapter was null for office " + office.text + ". in repopulateEditorsAndChapterTabs!");
							}
						} else {
							System.err.println("Selected Office was null during repopulateEditorsAndChapterTabs!");
						}

					} else {
						Label empty = new Label("Viraston tietoa ei löydetty");
						addedTab = allChaptersTabSheet.addTab(empty, "" + i, null, i);
					}
				} else {
					addedTab = null;
					throw (new NullPointerException("Incorrectly defined indeces for chapters!"));
				}
			}

			if (addedTab == null) {
				Label l = new Label();
				l.setValue("Kappaletta ei määritelty");
				addedTab = allChaptersTabSheet.addTab(l, "" + allChaptersTabSheet.getComponentCount(), null,
						allChaptersTabSheet.getComponentCount());
			}

			ResultAgreementConfiguration resultAgreementConfiguration = ResultAgreementConfiguration.getInstance(database);
			
			// Style the tab
			if (addedTab != null) {
				String label = resultAgreementConfiguration.getChapterLabel(i);
				String desc = resultAgreementConfiguration.getChapterDescription(i);

				addedTab.setCaption(
						"<div align=\"left\" style=\"line-height: 100%; margin-top: 2px; margin-bottom: 2px;\">"
								+ formatLabel(label, is_shared)
								+ "<br /><font size=\"-2\">"
								+ formatDesc(desc)
								+ "</div></font>");
			} else {
				System.err.println("Expected non-null tab to be added to chapter tab, got null! CurrentDocument: "
						+ currentDocument.uuid);
			}

		}
	}
	
	private static String formatLabel(String label, boolean is_shared) {
		return is_shared ? (label + " " + ResultAgreementConfiguration.SHARED_LABEL_APPENDIX) : label;
	}
	
	/**
	 * Tabs look strange if the bottom contains oddly formatted value.
	 * Split the string with <br/> to make it fit
	 * Will always contain exactly one <br/> element regardless of size, to format tabs to look right
	 * @param desc
	 * @return
	 */
	private static String formatDesc(String desc) {
		// Make description fit
		if(desc.length() > 30) {
			List<Integer> spaces = new ArrayList<>();
			for(int i = 0; i < desc.length(); i++) {
				if(desc.charAt(i) == ' ') {
					spaces.add(i);
				}
			}
			
			// Find space closest to the text's middle point and replace with <br/>, if able
			// Otherwise cut text short with dots
			if(spaces.size() > 0) {
				int middlepoint = (desc.length() / 2);
				Optional<Integer> c = spaces.stream().min(
						Comparator.comparingInt(i -> Math.abs(i - middlepoint)));
				if(c.isPresent()) {
					Integer i = c.get();
					String before = desc.substring(0, i);
					String after = desc.substring(i);
					desc = before + "<br/>" + after;
				} else {
					desc = desc.substring(0, 27);
					desc += "...";
				}
			} else {
				desc = desc.substring(0, 27);
				desc += "...";
			}
		}
		
		if(desc.contains("<br/>")) {
			return desc;
		} else {
			return desc + "<br><br/>";
		}
	}
	
	public void refreshAllSizes() {
		if (uiState.startPageVisible) {
			this.startPage.refreshAllSizes();
		} else {
			
			Main.this.updateAvailableWindowWidthCache();
			updateMainLayoutWidth();

			Collection<ChapterEditorWrapperUI> editors = this.chapterEditors.values();
			for (ChapterEditorWrapperUI editor : editors) {
				editor.updateSizes();
			}

			this.commentLayout.refreshSize();
			this.mapEditorUI.refreshSplitPanelSize();
		}
	}

	public void updateMainLayoutWidth() {
		if (Main.this.mapAndTabs != null) {
			Main.this.mapAndTabs.setWidth((float) Main.this.nonCommentToolWidth, Unit.PIXELS);
		}
		mapEditorUI.panelHorizontalSplitLayout.setWidth(nonCommentToolWidth + "px");
	}

	public void refreshEditorServerSideOnly() {
		updateCurrentOffice();
		Collection<ChapterEditorWrapperUI> editors = this.chapterEditors.values();
		for (ChapterEditorWrapperUI editor : editors) {
			editor.updateServerSideOnly();
		}
	}

	public void refreshAllChapterEditors() {
		refreshEditorServerSideOnly();

		Collection<ChapterEditorWrapperUI> editors = this.chapterEditors.values();
		for (ChapterEditorWrapperUI editor : editors) {
			editor.updateUI(true);
		}
	}

	public void updateCurrentOffice() {
		if (this.uiState.currentOffice != null) {
			List<Office> candidates = officesVisibleToUser();
			for (Office candidate : candidates) {
				if (candidate.uuid.equals(this.uiState.currentOffice.uuid)) {
					this.uiState.currentOffice = candidate;
					break;
				}
			}
		}
	}

	/**
	 * Helper function to filter our AccessRight for the ResultAgreementDocuments view.
	 * Only show in tabs the offices the current user is allowed to view.
	 * @return
	 */
	private List<Office> officesVisibleToUser() {
		Database database = this.getDatabase();
		Account account = this.getAccountDefault();
		if(account.isAdmin(database) || ResultAgreementToolAdminBase.canRead(database, account)) {
			return Office.enumerate(database, true, false);
		}
		
		Office o = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		if(o != null) {
			List<Office> offices = new ArrayList<Office>();
			offices.add(o);
			return offices;
		} else {
			return Collections.emptyList();
		}
	}
	
	public void setCurrentOfficeFromTabSheet(int index) {
		List<Office> offices = officesVisibleToUser();
		if (offices.size() != 0 && offices.size() > index) {
			Office o = offices.get(index);
			this.uiState.setCurrentOffice(o);
		} else {
			System.err.println("Got index " + index + ", but Offices found was: " + offices.size() + " in setCurrentOfficeFromTabSheet!");
		}
	}

	private void initTimesComboBox() {
		timesListener = new ValueChangeListener() {

			private static final long serialVersionUID = 3685328582513913459L;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) times.getValue();
				if (value == null)
					return;

				UIState state = Main.this.uiState.duplicateI(stateCounter++);
				state.time = value;
				Main.this.setFragment(state, true);
				return;

			}

		};

		times = new ComboBox();
		times.setWidth("130px");
		times.addStyleName(ValoTheme.COMBOBOX_SMALL);
		times.addItem(Property.AIKAVALI_KAIKKI);

		ArrayList<Integer> years = TimeConfiguration.getInstance(database).getAllConfiguredYears();

		for (Integer y : years) {
			times.addItem(y.toString());
		}

		if (years.size() != 0) {
			String value = years.get(0).toString();
			times.select(value);
			uiState.time = value;
		}

		times.setInvalidAllowed(false);
		times.setNullSelectionAllowed(false);

		times.addValueChangeListener(timesListener);
	}

	List<NodeFilter> availableFilters = new ArrayList<NodeFilter>();

	void setTabState(UIState uiState, int state) {

		mapEditorUI.panel.setVisible(false);
		browser.setVisible(false);
		propertiesPanel.setVisible(false);
		wiki_.setVisible(false);
		mapEditorUI.pdf.setVisible(false);
		mapEditorUI.propertyExcelButton.setVisible(false);

		uiState.mapTabState = state;

		if (uiState.mapTabState == UIState.MAP) {
			mapEditorUI.panel.setVisible(true);
			mapEditorUI.pdf.setVisible(true);
		} else if (uiState.mapTabState == UIState.BROWSER) {
			browser.setVisible(true);
		} else if (uiState.mapTabState == UIState.WIKI) {
			wiki_.setVisible(true);
		} else if (uiState.mapTabState == UIState.PROPERTIES) {
			propertiesPanel.setVisible(true);
			mapEditorUI.propertyExcelButton.setVisible(true);
		}

	}

	public void updateCommentToolVisibility() {
		commentToolDialogOpener.setVisible(UtilsComments.accountHasCommentToolReadRights(Main.this));
	}

	public void switchToMapAndDocumentsView() {
		setTabState(uiState, UIState.MAP);
		Updates.update(this, false);
	}

	public void switchToBrowser() {
		setTabState(uiState, UIState.BROWSER);
		Updates.update(this, false);
	}

	void switchToWiki() {
		setTabState(uiState, UIState.WIKI);
		Updates.update(this, false);
	}

	public void switchToProperties() {
		setTabState(uiState, UIState.PROPERTIES);
		Updates.update(this, false);
	}

	public class TreeVisitor1 implements MapVisitor {

		FilterState filterState = new FilterState(Main.this, windowWidth, windowHeight);
		LinkedList<MapBase> path = new LinkedList<MapBase>();

		@Override
		public void visit(MapBase b) {

			path.add(b);

			Collection<MapBase> bases = getCurrentFilter().traverse(path, filterState);
			for (MapBase b2 : bases) {
				b2.accept(this);
			}

			getCurrentFilter().accept(path, filterState);
			
			path.removeLast();

		}

	}

	public void setCurrentItem(MapBase item, StrategyMap position) {

		UIState state = uiState.duplicateI(stateCounter++);
		state.currentMapItem = item;
		state.currentMapPosition = position;
		setFragment(state, true);

	}

	public void addRequiredItem(MapBase item) {

		UIState state = uiState.duplicateI(stateCounter++);
		if (state.requiredMapItems == null)
			state.requiredMapItems = new HashSet<MapBase>();
		state.requiredMapItems.add(item);
		setFragment(state, true);

	}

	public void removeRequiredItem(MapBase item) {

		UIState state = uiState.duplicateI(stateCounter++);
		if (state.requiredMapItems == null)
			state.requiredMapItems = new HashSet<MapBase>();
		state.requiredMapItems.remove(item);
		setFragment(state, true);

	}

	public String dialogWidth() {
		return dialogWidth(0.7);
	}

	public String dialogWidth(double ratio) {
		return (ratio * windowWidth) + "px";
	}

	public String dialogHeight() {
		return dialogHeight(0.8);
	}

	public String dialogHeight(double ratio) {
		return (ratio * windowHeight) + "px";
	}

	public boolean canWrite(Base b) {
		Account account = getAccountDefault();
		return UtilsDB.canWrite(database, account, b);
	}

	public MapBase hasLeaf(MapBase b) {
		return b.hasLeaf(database, getUIState().getTime());
	}

	public boolean canWriteWithSelectedTime() {
		String currentTime = getUIState().getTime();
		return TimeConfiguration.getInstance(database).canWrite(currentTime);
	}

	public void updateAvailableWindowWidthCache() {
		
		if (Main.this.uiState.commentLayoutOpen) {
			nonCommentToolWidth = (int)(this.commentLayout.getMapViewWidthRatio() * ((double) Main.this.windowWidth));
			double mapWidth = nonCommentToolWidth - treeSplitPixelPosition;
			availableStrategyMapWidth = (int) mapWidth;
		} else {
			nonCommentToolWidth = Main.this.windowWidth;;
			double mapWidth = nonCommentToolWidth - treeSplitPixelPosition;
			availableStrategyMapWidth = (int) mapWidth;
		}
		
//		System.err.println("updateAvailableWindowWidthCache");
//		System.err.println("-windowWidth: " + windowWidth);
//		System.err.println("-availableLeftSideWidth: " + availableLeftSideWidth);
//		System.err.println("-availableStrategyMapWidth: " + availableStrategyMapWidth);
//		System.err.println("-treeSplitPixelPosition: " + treeSplitPixelPosition);
		
	}

	/**
	 * Only possible to call from button in chapter text editor view
	 */
	public void showResultAgreementPrintViewOptionsDialog() {
		// Open options window:
		documentPrintWindow.clearOptions();
		documentPrintWindow.createAndOpenNewPrintOptionsDialog();
	}

	public void showGuideViewDialog() {
		MainDocumentGuideWindow.createAndOpen(this);
	}

	/**
	 * 
	 */
	public void showPrintViewWithSelectedOptions() {
		// Actual print window:
		this.uiState.showDocumentPrintWindow = true;
		setFragment(uiState.duplicateS(printDocumentFragment), false, false);

		applyPrintSize();

		documentPrintWindow.refreshHTMLWithOptions();
		Main.this.setContent(documentPrintWindow);
		JavaScript.getCurrent().execute("printPageAfterRefresh()");
	}

	/**
	 * Only possible to call from usage of back button
	 */
	public void hideResultAgreementPrintView() {
		this.uiState.showDocumentPrintWindow = false;

		applyDefaultSize();

		Main.this.setContent(rootAbsoluteLayout);
	}

	private void applyDefaultSize() {
		com.vaadin.ui.JavaScript.getCurrent()
				.execute("document.body.style.overflow = \"\";" + "document.body.style.height  = \"\"");
		UI.getCurrent().setSizeFull();
	}

	private void applyPrintSize() {
		com.vaadin.ui.JavaScript.getCurrent()
				.execute("document.body.style.overflow = \"auto\";" + "document.body.style.height  = \"auto\"");
		UI.getCurrent().setSizeUndefined();
	}
	
	public Action tryToGetNavigationAction(MapBase b) {
		
		Collection<Action> actions = Actions.listActions(this, 0.0, 0.0, null, b);
		Action singleNavigation = null;
		for(Action a : actions) {
			if(a.isNavigation()) {
				if(singleNavigation != null)
					return null;
				else
					singleNavigation = a;
			}
		}
		
		return singleNavigation;
		
	}
	
	public void tryToNavigate(MapBase b) {
		
		Action action = tryToGetNavigationAction(b);
		if(action != null)
			action.run();

	}

}