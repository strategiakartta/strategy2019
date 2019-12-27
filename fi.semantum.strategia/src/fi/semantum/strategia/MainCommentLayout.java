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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategia.widget.comment.tool.CommentToolModel;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.ChangeSuggestion.ChangeSuggestionState;
import fi.semantum.strategy.db.ChangeSuggestionOpinion;
import fi.semantum.strategy.db.Comment;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.GathersOpinions;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Opinion.OpinionState;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDBComments;
/**
 * 
 * @author Miro Eklund
 *
 */
public class MainCommentLayout {
	
	public static final String LEFT_CHANGE_SUGGESTION_BUTTON_TOOLBAR_WIDTH = "40px";
	
	private Main main;
	private VerticalLayout root;
	//DB objects, must be updated when DB refreshes:
	private ChangeSuggestion currentSelectedChangeSuggestion;
	private CommentToolModel commentToolModel;
	
	//Rest
	private VerticalLayout currentOpenedChangeSuggestionLayout;
	private GridLayout opinionGridlayout;
	
	//All components related to a specific (opened) ChangeSuggestion:
	private Label currentChangeSuggestionMetadataLabel;
	private Label currentChangeSuggestionMotivationLabel;
	private Label currentChangeSuggestionSuggestionLabel;
	private Label currentChangeSuggestionTargetStringLabel;
	private VerticalLayout currentChangeSuggestionVisualLayout;
	private AbstractLayout opinionStates;
	
	private Button backButton;
	private Button changeCommentOrderButton;
	private PopupView createCommentTextAreaPopupView;
	private Button addCommentButton;
	
	private Button newChangeSuggestionButton;
	private Button fullScreenButton;
	private CheckBox filterModeCheckBox;
	private Set<String> cachedVisibleElementsOverride = new HashSet<>();
	/**
	 * The ChangeSuggestions must target string must be found in this cached String in filter mode to be shown. If this is null, ignore it.
	 */
	private String cachedTargetedStringFilter = null;
	
	private TabSheet navigationTabSheet;
	private VerticalLayout changeSuggestionNavigationWrapper;
	private VerticalLayout active;
	private VerticalLayout onhold;
	private VerticalLayout closed;

	public static final String COMMENT_ROOT_ID = "commentToolContainerRoot"; //This must be the same in comment.nocache.js
	
	private static final double commentViewWidthRatio = 0.35; //35%
	private static final double mapViewWidthRatio = 1.0 - commentViewWidthRatio;
	
	private MainCreateNewChangeSuggestionWindow subWindow;
	
	public double getCommentViewWidthRatio() {
		if(this.main.uiState.commentLayoutFullWindow) {
			return 1.0;
		} else {
			return 0.35;
		}
	}
	
	public double getMapViewWidthRatio() {
		if(this.main.uiState.commentLayoutFullWindow) {
			return 0.0;
		} else {
			return mapViewWidthRatio;
		}
	}

	public MainCommentLayout(Main main) {
		this.main = main;
		subWindow = new MainCreateNewChangeSuggestionWindow(main);

		init();
		refresh();
	}
	
	private AtomicBoolean updateOngoing = new AtomicBoolean(false);
	
	public void setUpdateOngoing(boolean value) {
		updateOngoing.set(value);
	}
	
	public void attachTabSheetListeners(TabSheet officeTabSheet, TabSheet chaptersTabSheet) {
		officeTabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = 863339109940833618L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				possiblyPopulateTabsView();
			}
		});
		
		
		chaptersTabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = 6791293702598153709L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				possiblyPopulateTabsView();
			}
		});
	}
	
	/**
	 * Attempts to populate ChangeSuggestion main tabs views with ChangeSuggestions.
	 * Only refreshes if filter mode is active
	 */
	public void possiblyPopulateTabsView() {
		if(!updateOngoing.get() && main.getUIState().commentLayoutOpen && main.getUIState().commentLayoutFilterMode) {
			populateTabsView(true);
		}
	}
	
	/**
	 * 
	 * @param chapter
	 * @param target
	 * @param index
	 */
	public void openRelatedChangeSuggestions(Set<Base> bases, String target) {
		//Clear selection and return to main navigation:
		if(this.currentSelectedChangeSuggestion != null) {
			switchCurrentSelectedChangeSuggestionView(null);
		}
		
		cachedVisibleElementsOverride.clear();
		cachedTargetedStringFilter = target;
		
		for(Base b : bases) {
			cachedVisibleElementsOverride.add(b.uuid);
		}

		//Update UI:
		if(!filterModeCheckBox.getValue()) {
			filterModeCheckBox.setValue(true); //Triggers listener redraw
		} else {
			//Force redraw, UI element already up-to-date
			toggleFilterMode(true);
		}
		
		cachedTargetedStringFilter = null; //Reset the target after refresh is completed
	}
	
	/**
	 *
	 * @param bases
	 */
	public void openRelatedChangeSuggestions(Set<Base> bases) {
		openRelatedChangeSuggestions(bases, null);
	}
	
	private void init() {
		root = new VerticalLayout();
		root.setStyleName(CustomCSSStyleNames.STYLE_COMMENT_TOOL);
		root.setHeight("100%");
		
		//Initialize some buttons and fields used later:
		backButton = createBackButton();
		changeCommentOrderButton = createChangeCommentOrderButton();
		createCommentTextAreaPopupView = createCommentTextAreaPopupView();
		addCommentButton = createAddCommentButton(createCommentTextAreaPopupView);
		
		updateOrderButtonIcon();
		
		fullScreenButton = createFullScreenButton();
		refreshFullScreenButtonUI();
		
		filterModeCheckBox = createFilterModeCheckBox();
			
		newChangeSuggestionButton = createNewChangeSuggestionButton();
		
		currentChangeSuggestionMetadataLabel = new Label();
		currentChangeSuggestionMetadataLabel.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_METADATA);
		currentChangeSuggestionMetadataLabel.setContentMode(ContentMode.HTML);
		currentChangeSuggestionMetadataLabel.setWidth("100%");
		
		currentChangeSuggestionMotivationLabel = new Label();
		currentChangeSuggestionMotivationLabel.setWidth("100%");
		currentChangeSuggestionMotivationLabel.setContentMode(ContentMode.HTML);
		
		currentChangeSuggestionSuggestionLabel = new Label();
		currentChangeSuggestionSuggestionLabel.setWidth("100%");
		currentChangeSuggestionSuggestionLabel.setContentMode(ContentMode.HTML);
		
		currentChangeSuggestionTargetStringLabel = new Label();
		currentChangeSuggestionTargetStringLabel.setWidth("100%");
		currentChangeSuggestionTargetStringLabel.setContentMode(ContentMode.HTML);
		
		currentChangeSuggestionVisualLayout = new VerticalLayout();
		currentChangeSuggestionVisualLayout.setWidth("100%");
		
		opinionStates = new CssLayout();
		
		//Rest:
		commentToolModel = new CommentToolModel();
		
		currentOpenedChangeSuggestionLayout = new VerticalLayout();
		
		root.addComponent(currentOpenedChangeSuggestionLayout);
		root.setComponentAlignment(currentOpenedChangeSuggestionLayout, Alignment.TOP_CENTER);
		
		navigationTabSheet = createChangeSuggestionNavigation();
		
		changeSuggestionNavigationWrapper = new VerticalLayout();
		changeSuggestionNavigationWrapper.setSizeFull();
		
		VerticalLayout buttonVerticalRootLayout = new VerticalLayout();
		buttonVerticalRootLayout.setWidth("100%");
		buttonVerticalRootLayout.setMargin(new MarginInfo(true, true, false, true));
		buttonVerticalRootLayout.setSpacing(true);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setWidth("100%");
		buttonToolbar.setSpacing(true);
		
		buttonToolbar.addComponent(newChangeSuggestionButton);
		buttonToolbar.setComponentAlignment(newChangeSuggestionButton, Alignment.TOP_LEFT);
		buttonToolbar.setExpandRatio(newChangeSuggestionButton, 0.0f);
		
		buttonToolbar.addComponent(fullScreenButton);
		buttonToolbar.setComponentAlignment(fullScreenButton, Alignment.TOP_RIGHT);
		buttonToolbar.setExpandRatio(fullScreenButton, 0.0f);
		
		buttonVerticalRootLayout.addComponent(buttonToolbar);
		buttonVerticalRootLayout.setComponentAlignment(buttonToolbar, Alignment.MIDDLE_LEFT);
		buttonVerticalRootLayout.setExpandRatio(buttonToolbar, 0.0f);
		
		buttonVerticalRootLayout.addComponent(filterModeCheckBox);
		buttonVerticalRootLayout.setComponentAlignment(filterModeCheckBox, Alignment.MIDDLE_LEFT);
		buttonVerticalRootLayout.setExpandRatio(filterModeCheckBox, 0.0f);
				
		changeSuggestionNavigationWrapper.addComponent(buttonVerticalRootLayout);
		changeSuggestionNavigationWrapper.setComponentAlignment(buttonVerticalRootLayout, Alignment.TOP_LEFT);
		changeSuggestionNavigationWrapper.setExpandRatio(buttonVerticalRootLayout, 0.0f);
		
		changeSuggestionNavigationWrapper.addComponent(navigationTabSheet);
		changeSuggestionNavigationWrapper.setComponentAlignment(navigationTabSheet, Alignment.TOP_CENTER);
		changeSuggestionNavigationWrapper.setExpandRatio(navigationTabSheet, 1.0f);
		
		root.addComponent(changeSuggestionNavigationWrapper);
		root.setComponentAlignment(changeSuggestionNavigationWrapper, Alignment.TOP_CENTER);
		
		root.setExpandRatio(currentOpenedChangeSuggestionLayout, 1.0f);
		root.setExpandRatio(changeSuggestionNavigationWrapper, 1.0f);
	}
	
	private TabSheet createChangeSuggestionNavigation() {
		TabSheet navigation = new TabSheet();
		navigation.setSizeFull();
		navigation.setTabCaptionsAsHtml(true);
		navigation.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		
		active = new VerticalLayout();
		onhold = new VerticalLayout();
		closed = new VerticalLayout();
		active.setHeightUndefined();
		onhold.setHeightUndefined();
		closed.setHeightUndefined();
		
		navigation.addTab(active, "Avoimet");
		navigation.addTab(onhold, "Siirretään neuvotteluihin");
		navigation.addTab(closed, "Suljetut");
		navigation.setVisible(true);
		
		navigation.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 863339109940833618L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				int index = event.getTabSheet().getTabPosition(event.getTabSheet().getTab(event.getTabSheet().getSelectedTab()));
				handleTabChangeEvent(index);
			}
		});
		
		return navigation;
	}
	
	private void handleTabChangeEvent(int index) {
		//TODO: Can be used to handle a tab change event.
	}

	public VerticalLayout getRoot() {
		return this.root;
	}
	
	public void toggleOpenClose() {
		if(UtilsComments.accountHasCommentToolReadRights(main)) {
			main.uiState.commentLayoutOpen = !main.uiState.commentLayoutOpen;
			if(main.uiState.commentLayoutOpen) {
				open();
			} else {
				close();
			}
			
			//Update map JS with comment layout open settings
			Updates.updateJS(main, false, false);
			
			refresh();
		}
	}
	
	private void switchCurrentSelectedChangeSuggestionView(ChangeSuggestion cs) {
		if(cs != null) {
			if(cs.isActive()) {
				Database database = main.getDatabase();
				Account account = main.getAccountDefault();
				if(!UtilsDB.seenByAccount(database, account, cs)) {
					UtilsDB.setSeenByAccount(database, account, cs);
					Updates.update(main, true);
				}
			}
			
			this.currentSelectedChangeSuggestion = cs;
			refreshServerSideOnly();
			
			VerticalLayout csRoot = changeSuggestionToFullView();
			Panel officeOpinionsLayout = createOpinionsLayout();
			//This is the view containing office opinions, official statements and comments:
			csRoot.addComponent(officeOpinionsLayout);
			csRoot.setExpandRatio(officeOpinionsLayout, 1.0f);
			
			this.currentOpenedChangeSuggestionLayout.addComponent(csRoot);
		} else {
			this.currentSelectedChangeSuggestion = null;
			this.currentOpenedChangeSuggestionLayout.removeAllComponents();
			refreshServerSideOnly();
			populateTabsView();
		}
		updateChangeSuggestionLayoutVisibility();
	}
	
	/**
	 * Should the changesuggestion be shown or the main navigation that lists all available changesuggestions?
	 */
	private void updateChangeSuggestionLayoutVisibility() {
		boolean showChangeSuggestion = this.currentSelectedChangeSuggestion != null;
		this.currentOpenedChangeSuggestionLayout.setVisible(showChangeSuggestion);
		this.changeSuggestionNavigationWrapper.setVisible(!showChangeSuggestion);
		resizeInternal();
	}
	
	public void refreshSize() {
		if(main.uiState.commentLayoutOpen) {
			int a = this.main.nonCommentToolWidth;
			int width = this.main.getFullWindowWidth() - a;
			
			this.root.setWidth((float)width, Unit.PIXELS);
			resizeInternal();
		}
	}

	private void updateCurrentSelectedChangeSuggestion() {
		if(this.currentSelectedChangeSuggestion != null) {
			List<ChangeSuggestion> suggestions = UtilsDBComments.getPossibleLinkedChangeSuggestions(main.getDatabase(), main.getDatabase().getRoot());
			for(ChangeSuggestion candidate : suggestions) {
				if(candidate.uuid.equals(this.currentSelectedChangeSuggestion.uuid)) {
					currentSelectedChangeSuggestion = candidate;
				}
			}
		}
	}
	
	public void refreshServerSideOnly() {
		if(main.uiState.commentLayoutOpen) {
			updateCurrentSelectedChangeSuggestion();
			commentToolModel.updateFromServer(this.main);
		}
	}
	
	/**
	 * Refresh both serverside objects and client UI
	 */
	public void refresh() {
		refreshServerSideOnly();
		refreshCommentToolMainUI(true, true, true);
	}
	
	public void refresh(boolean needServerSide, boolean needOpinionGrid, boolean needChangeSuggestionFullView, boolean needTabs) {
		if(needServerSide) refreshServerSideOnly();
		refreshCommentToolMainUI(needOpinionGrid, needChangeSuggestionFullView, needTabs);
	}
	
	private void refreshCommentToolMainUI(boolean needOpinionGrid, boolean needChangeSuggestionFullView, boolean needTabs){
		this.root.setVisible(this.main.uiState.commentLayoutOpen);
		if(main.uiState.commentLayoutOpen) {
			if(needOpinionGrid) updateOpinionGridLayout();
			if(needChangeSuggestionFullView) updateChangeSuggestionFullView();
			if(needTabs) populateTabsView();
			updateChangeSuggestionLayoutVisibility();
			refreshFullScreenButtonUI();
		}
		refreshSize();
	}

	private void populateTabsView(){
		populateTabsView(false);
	}
	
	private void populateTabsView(boolean resetCache) {
		//Don't update tabs if the current change suggestion is non-null, since the tabs aren't visible
		if(this.currentSelectedChangeSuggestion != null) {
			return;
		}
		//System.out.println("Updating tabs view!");

		List<ChangeSuggestion> activeCS = commentToolModel.visibleChangeSuggestionsActive;
		List<ChangeSuggestion> onHoldCS = commentToolModel.visibleChangeSuggestionsOnHold;
		List<ChangeSuggestion> closedCS = commentToolModel.visibleChangeSuggestionsClosed;
		
		//Apply custom sorting here if enabled
		selectedPage = 0;

		if(resetCache) {
			cachedVisibleElementsOverride.clear();
			cachedTargetedStringFilter = null;
		}
		
		boolean filter = main.getUIState().commentLayoutFilterMode;
		if(filter) {
			final Set<String> visibleBases;
			if(cachedVisibleElementsOverride != null && !cachedVisibleElementsOverride.isEmpty()) {
				visibleBases = cachedVisibleElementsOverride;
			} else {
				visibleBases = main.getVisibleElements();
			}
			
			List<ChangeSuggestion> filteredActiveCS = activeCS.stream().filter(cs -> showChangeSuggestion(cs, filter, cachedTargetedStringFilter, visibleBases)).collect(Collectors.toList());
			List<ChangeSuggestion> filteredOnHoldCS = onHoldCS.stream().filter(cs -> showChangeSuggestion(cs, filter, cachedTargetedStringFilter, visibleBases)).collect(Collectors.toList());
			List<ChangeSuggestion> filteredClosedCS = closedCS.stream().filter(cs -> showChangeSuggestion(cs, filter, cachedTargetedStringFilter, visibleBases)).collect(Collectors.toList());
			
			cleanAndRepopulateTabsViews(filteredActiveCS, filteredOnHoldCS, filteredClosedCS);
		} else {
			cleanAndRepopulateTabsViews(activeCS, onHoldCS, closedCS);
		}
	}
	
	/**
	 * Force re-fresh the tabsview
	 * @param activeCS
	 * @param onholdCS
	 * @param closedCS
	 */
	private void cleanAndRepopulateTabsViews(List<ChangeSuggestion> activeCS, List<ChangeSuggestion> onholdCS, List<ChangeSuggestion> closedCS) {
		this.active.removeAllComponents();
		this.closed.removeAllComponents();
		this.onhold.removeAllComponents();
		
		boolean filter = main.getUIState().commentLayoutFilterMode;
		String suodatettu = filter ? " (Suodatettu näkymä)" : "";
		navigationTabSheet.setCaption("<div style=\"font-size: 80%;\">Muutosehdotuksia " + SafeHtmlUtils.htmlEscape(this.commentToolModel.getResultsFrom() + suodatettu) + "</div>");
		navigationTabSheet.setCaptionAsHtml(true);
		
		createSingleTabsView(this.active, activeCS);
		createSingleTabsView(this.onhold, onholdCS);
		createSingleTabsView(this.closed, closedCS);
		
	}
	
	private int selectedPage = 0;
	
	private void createSingleTabsView(VerticalLayout layout, List<ChangeSuggestion> changeSuggestions) {
		int allCount = changeSuggestions.size();
		int maxPerPage = Configuration.getMAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE();
		int maxPageIndex = (allCount / maxPerPage);

		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setWidth("100%");
		buttonToolbar.setHeight("28px");
		
		Button previous = new Button();
		Button next = new Button();
		buttonToolbar.addComponent(previous);
		buttonToolbar.addComponent(next);
		buttonToolbar.setComponentAlignment(previous, Alignment.TOP_CENTER);
		buttonToolbar.setComponentAlignment(next, Alignment.TOP_CENTER);
		
		//previous.setHeight("28px");
		previous.setStyleName(ValoTheme.BUTTON_TINY);
		previous.setCaption("Näytä edelliset");
		previous.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 5327265407796138407L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(selectedPage > 0) {
					selectedPage--;
				}
				previous.setEnabled(selectedPage > 0);
				next.setEnabled(selectedPage < maxPageIndex);
				updateLayoutBySelectedPage(buttonToolbar, layout, changeSuggestions);
			}
			
		});
		
		//next.setHeight("28px");
		next.setCaption("Näytä seuraavat");
		next.setStyleName(ValoTheme.BUTTON_TINY);
		next.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -6317288825701097686L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(selectedPage < maxPageIndex) {
					selectedPage++;
				}
				next.setEnabled(selectedPage < maxPageIndex);
				previous.setEnabled(selectedPage > 0);
				updateLayoutBySelectedPage(buttonToolbar, layout, changeSuggestions);
			}
			
		});
		
		buttonToolbar.setVisible(allCount > maxPerPage);
		previous.setVisible(allCount > maxPerPage);
		next.setVisible(allCount > maxPerPage);
		
		if(selectedPage <= 0) {
			previous.setEnabled(false);
		}
		if(selectedPage >= maxPageIndex) {
			next.setEnabled(false);
		}

		updateLayoutBySelectedPage(buttonToolbar, layout, changeSuggestions);
	}
	
	private void updateLayoutBySelectedPage(HorizontalLayout buttonToolbar, VerticalLayout layout, List<ChangeSuggestion> changeSuggestions) {
		layout.removeAllComponents();
		
		int allCount = changeSuggestions.size();
		int maxPerPage = Configuration.getMAX_VISIBLE_CHANGE_SUGGESTIONS_PER_PAGE();
		int startIndex = selectedPage * maxPerPage;
		int endIndex = startIndex + maxPerPage;
		
		layout.addComponent(buttonToolbar);
		layout.setComponentAlignment(buttonToolbar, Alignment.TOP_CENTER);
		
		for(int i = startIndex; i < endIndex; i++) {
			if(i < allCount) {
				ChangeSuggestion changeSuggestion = changeSuggestions.get(i);
				Component c = changeSuggestionToNavigationElement(changeSuggestion);
				layout.addComponent(c);
			} 
		}
	}
	
	/**
	 * 
	 * @param cs
	 * @param filter
	 * @param possibleTargetStringFull
	 * @param visibleBases
	 * @return
	 */
	private boolean showChangeSuggestion(ChangeSuggestion cs, boolean filter, String possibleTargetStringFull, Set<String> visibleBases) {
		boolean show = true;
		if(filter) {
			show = false; //If a filter is on, then only show if target matches something in list of visible elements
			if(cs.getPossibleBaseTargetUUID() != null) {
				for(String uuid : visibleBases) {
					if(uuid.equals(cs.getPossibleBaseTargetUUID())) {
						//ChangeSuggestion targets an element that is visible in filtered mode - make sure possible targeted string also matches
						if(possibleTargetStringFull != null) {
							//Only those ChangeSuggestions with a target that is contained within the full target should be shown
							if(cs.getPossibleTargetString() != null && possibleTargetStringFull.contains(cs.getPossibleTargetString())) {
								show = true;
							}
						} else {
							//Possible target string is null, show all that come this far
							show = true;
						}
						break;
					}
				}
			}
		}
		
		return show;
	}
	
	private void toggleFilterMode(boolean newMode) {
		main.getUIState().commentLayoutFilterMode = newMode;
		//If we exist filter mode, then clear the cached visible override elements
		if(!newMode) {
			cachedVisibleElementsOverride.clear();
		}
		populateTabsView();
		refreshSize();
	}
	
	private CheckBox createFilterModeCheckBox() {
		CheckBox b = new CheckBox();
		b.setCaption("Näkymän suodatin");
		b.setDescription("Kytke muutosehdotusnäkymän suodatin päälle tai pois. Jos suodatin on käytössä, "
				+ "näkymässä näytetään vain avattuun kappaleeseen tai karttaelementtiin liittyvät muutosehdotukset.");
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -139987815773345348L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				toggleFilterMode(b.getValue());
			}
			
		});
		b.setValue(main.getUIState().commentLayoutFilterMode);
		return b;
	}
	
	private Button createChangeCommentOrderButton() {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setCaption("Muokkaa järjestystä");
		b.setDescription("Muokkaa kommenttien järjestystä");
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 3440545692333506410L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleCommentOrder();
			}
			
		});
		return b;
	}

	private void toggleCommentOrder() {
		boolean isAscending = !main.getUIState().commentsOrderAscending;
		main.getUIState().commentsOrderAscending = isAscending;
		updateOrderButtonIcon();
		updateOpinionGridLayout();
		refreshSize();
	}
	
	private void updateOrderButtonIcon() {
		if(main.getUIState().commentsOrderAscending) {
			changeCommentOrderButton.setIcon(FontAwesome.SORT_NUMERIC_ASC);
		} else {
			changeCommentOrderButton.setIcon(FontAwesome.SORT_NUMERIC_DESC);
		}
	}
	
	private Button createBackButton() {
		Button b = new Button();
		b.setWidth(LEFT_CHANGE_SUGGESTION_BUTTON_TOOLBAR_WIDTH);
		b.setCaption("");
		b.setIcon(FontAwesome.ARROW_CIRCLE_LEFT);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setDescription("Takaisin päänavigointiin");
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 3847040341030723031L;

			@Override
			public void buttonClick(ClickEvent event) {
				switchCurrentSelectedChangeSuggestionView(null);
			}

		});
		return b;
	}
	
	private Button createFullScreenButton() {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -5755708405782693916L;

			@Override
			public void buttonClick(ClickEvent event) {
				toggleFullScreen();
				refreshFullScreenButtonUI();
			}

		});
		return b;
	}
	
	private Button createNewChangeSuggestionButton() {
		Button b = new Button();
		b.setCaption("Uusi muutosehdotus");
		b.setIcon(FontAwesome.COMMENT);
		b.setStyleName(ValoTheme.BUTTON_TINY);
		
		//This will stay the same during the entire session (unless System account changes groups and access rights):
		final boolean canWrite = UtilsComments.accountHasCommentToolWriteRights(main);
		
		if(canWrite) {
			b.setEnabled(true);
			b.addClickListener(new ClickListener() {
	
				private static final long serialVersionUID = 5514381947317911504L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					if(UtilsComments.accountHasCommentToolWriteRights(main)) {
						openCreateNewChangeSuggestionDialog();
					} else {
						System.err.println("Account does not have write rights to comment tool - cannot open create new change suggestion dialog!");
					}
				}
	
			});
		} else {
			b.setEnabled(false);
			b.setDescription("Sinulla ei ole oikeutta luoda uusia muutosehdotuksia");
		}
		return b;
	}
	
	private void refreshFullScreenButtonUI() {
		if(!this.main.uiState.commentLayoutFullWindow) {
			fullScreenButton.setCaption("Suurenna");
			fullScreenButton.setIcon(FontAwesome.EXPAND);
		} else {
			fullScreenButton.setCaption("Pienennä");
			fullScreenButton.setIcon(FontAwesome.MINUS);
		}
	}
	
	private void toggleFullScreen() {
		boolean showFull = !main.uiState.commentLayoutFullWindow;
		main.uiState.commentLayoutFullWindow = showFull;
		main.refreshCommentToolFullWindow();
		main.refreshAllSizes();
	}
	
	
	/**
	 * Ensure width and height is correct for the comment tool and all elements.
	 * Essential to ensure comments and changesuggestionopinions are correct size.
	 */
	private void resizeInternal() {
		if(this.currentSelectedChangeSuggestion != null) {
			int actualHeight = this.main.getWindowHeightPixels() - Main.TOOLBAR_HEIGHT;
			int actualWidth = this.main.getFullWindowWidth() - this.main.nonCommentToolWidth - 28; //A bit of room for the right scrollbar
			
			if(actualHeight < 0) actualHeight = 0;
			if(actualWidth < 0) actualWidth = 0;
			
			this.currentOpenedChangeSuggestionLayout.setHeight(actualHeight, Unit.PIXELS);

			if(this.opinionGridlayout != null) {
				int columns = opinionGridlayout.getColumns();
				int newWidth = actualWidth / columns;
				for(int column = 0; column < columns; column++) {
					for(int row = 0; row < opinionGridlayout.getRows(); row++) {
						Component possibleComponent = opinionGridlayout.getComponent(column, row);
						if(possibleComponent != null) {
							possibleComponent.setWidth(newWidth, Unit.PIXELS);
						}
					}
				}
			}
		}
	}

	private Component changeSuggestionToNavigationElement(ChangeSuggestion cs) {
		Database database = main.getDatabase();
		
		Account account = main.account;
		boolean notSeenByAccount = account == null ? false : !UtilsDB.seenByAccount(database, account, cs);
		
		VerticalLayout changeSuggestioNavigationLayout = new VerticalLayout();
		changeSuggestioNavigationLayout.addLayoutClickListener(new LayoutClickListener() {

			private static final long serialVersionUID = -2700126390810303973L;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				MainCommentLayout.this.currentOpenedChangeSuggestionLayout.removeAllComponents();
				switchCurrentSelectedChangeSuggestionView(cs);
			}
			
		});
		
		Label summaryLabel = new Label();
		summaryLabel.setWidth("100%");
		summaryLabel.setContentMode(ContentMode.HTML);
		Label creatorLabel = new Label();
		creatorLabel.setWidth("100%");
		creatorLabel.setContentMode(ContentMode.HTML);
		
		HorizontalLayout opinionsLayout = new HorizontalLayout();
		Map<String, OpinionState> opinions = cs.getOpinionsAsMap(database);
		
		SortedSet<String> sortedKeys = new TreeSet<>();
		sortedKeys.addAll(opinions.keySet());

		HorizontalLayout topLayout  = new HorizontalLayout();
		topLayout.setWidth("100%");
		Label label = new Label("");
		label.setStyleName(ValoTheme.LABEL_TINY);
		if(notSeenByAccount && cs.isActive()) {
			label.setIcon(FontAwesome.EXCLAMATION);
		}
		topLayout.addComponent(label);
		topLayout.setComponentAlignment(label, Alignment.TOP_LEFT);
		topLayout.setExpandRatio(label, 0.20f);
		topLayout.addComponent(opinionsLayout);
		topLayout.setComponentAlignment(opinionsLayout, Alignment.TOP_LEFT);
		topLayout.setExpandRatio(opinionsLayout, 0.8f);
		
		
		for(String key : sortedKeys) {
			OpinionState opinionEnum = opinions.get(key);
			Office office = Office.possiblyFindByName(main.getDatabase(), key);
			Button b = createOpinionStateButton(office, opinionEnum, true);
			b.setEnabled(false);
			opinionsLayout.addComponent(b);
			opinionsLayout.setComponentAlignment(b, Alignment.MIDDLE_CENTER);
		}

		changeSuggestioNavigationLayout.addComponent(topLayout);
		changeSuggestioNavigationLayout.setComponentAlignment(topLayout, Alignment.MIDDLE_CENTER);
		
		String creatorPrefix = "";
		Office office = UtilsDB.getPossibleAssociatedOffice(database, cs);
		if(office != null) {
			creatorPrefix += "<div style=\"font-size: 90%; margin-left:3px; float:left;\">Virastolta: " + office.getText(database) + "</div>";
		}
		creatorPrefix += "<div style=\"font-size: 90%; margin-right:3px; float:right;\">Luotu: " + cs.showCreationTime("yyyy-MM-dd HH:mm") + "</div></br>";
		
		creatorLabel.setValue(creatorPrefix);
		
		String summary = "";
		if(cs.getPossibleBaseTargetUUID() != null) {
			Base base = database.find(cs.getPossibleBaseTargetUUID());
			if(base != null) {
				if(base instanceof TextChapter) {
					summary = UtilsDBComments.textChapterToSummary(database, (TextChapter)base);
				} else {
					if(base instanceof MapBase) {
						MapBase mb = (MapBase)base;
						summary = database.getType(mb) + ": " + UtilsDB.mapBaseText(database, mb);
					} else {
						System.err.println("Change suggestion target is non-map non-text chapter. Using old summary logic. Type found: " + base.clientIdentity());
						summary = UtilsDBComments.getChangeSuggestionTargetBaseSummary(database, cs, false, false, " - ");
					}
				}
			} else {
				summary = "(Kohdetta ei löydettä. Se on voitu poistaa)";
			}
		}

		String summaryPrefix = "<div style=\"font-size:90%;\">" + summary + "</div>";
		
		summaryLabel.setValue(summaryPrefix + "<div style=\"font-size: 110%;\">" + cs.getDescription() + "</div>");
		changeSuggestioNavigationLayout.addStyleName(CustomCSSStyleNames.STYLE_CS_NAVIGATION_ELEMENT);
		
		changeSuggestioNavigationLayout.addComponent(creatorLabel);
		changeSuggestioNavigationLayout.addComponent(summaryLabel);
		return changeSuggestioNavigationLayout;
	}
	
	private void open() {
		this.root.setVisible(true);
	}
	
	private void close() {
		this.root.setVisible(false);
	}
	
	
	public void cleanup() {
		this.main.uiState.commentLayoutOpen = false;
		this.main.uiState.commentLayoutFilterMode = false;
		this.main.uiState.commentLayoutFullWindow = false;
		
		this.close();
		this.commentToolModel.updateFromServer(this.main);
		if(this.active != null) {
			this.active.removeAllComponents();
		}
		if(this.closed != null) {
			this.closed.removeAllComponents();
		}
		
		if(this.onhold != null) {
			this.onhold.removeAllComponents();
		}
		
		if(this.opinionGridlayout != null)  {
			this.opinionGridlayout.removeAllComponents();
		}
		if(this.currentOpenedChangeSuggestionLayout != null) {
			this.currentOpenedChangeSuggestionLayout.removeAllComponents();
		}
		if(subWindow != null) {
			try {
				subWindow.close();
				subWindow.cleanup();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		this.currentSelectedChangeSuggestion = null;
		clearChangeSuggestionFullView();
		this.main.refreshAllSizes();
	}
	
	public String buttonText() {
		if(this.main.uiState.commentLayoutOpen) {
			return "Kommentit (Sulje)";
		} else {
			return "Kommentit (Avaa)";
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// The ChangeSuggestion object's view - including opinion states, info, buttons, etc.
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Create a change suggestion view together with all navigation elements, opinion states, etc.
	 * @param changeSuggestion
	 * @return
	 */
	private VerticalLayout changeSuggestionToFullView() {
		//Start by adding the close view button.
		ChangeSuggestion changeSuggestion = this.currentSelectedChangeSuggestion;
		Database database = this.main.getDatabase();
		
		VerticalLayout csRoot = new VerticalLayout();
		csRoot.setHeight("100%");
		csRoot.setWidth("100%");
		
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setHeight("80px");
		
		top.setMargin(new MarginInfo(true, true, false, true));
		
		VerticalLayout leftButtonToolbar = new VerticalLayout();
		leftButtonToolbar.setHeight("100%");
		leftButtonToolbar.setWidth(LEFT_CHANGE_SUGGESTION_BUTTON_TOOLBAR_WIDTH);
		leftButtonToolbar.setSpacing(true);
		
		leftButtonToolbar.addComponent(backButton);
		leftButtonToolbar.setComponentAlignment(backButton, Alignment.TOP_LEFT);
		leftButtonToolbar.setExpandRatio(backButton, 0.0f);

		top.addComponent(leftButtonToolbar);
		top.setComponentAlignment(leftButtonToolbar, Alignment.TOP_LEFT);
		top.setExpandRatio(leftButtonToolbar, 0.0f);
		
		top.addComponent(currentChangeSuggestionMetadataLabel);
		top.setComponentAlignment(currentChangeSuggestionMetadataLabel, Alignment.TOP_CENTER);
		top.setExpandRatio(currentChangeSuggestionMetadataLabel, 1.0f);
		
		VerticalLayout rightButtonToolbar = new VerticalLayout();
		rightButtonToolbar.setHeight("100%");
		rightButtonToolbar.setWidth("40px");
		rightButtonToolbar.setSpacing(true);
		
		VerticalLayout changeSuggestionInfoView = createChangeSuggestionInfoView(changeSuggestion);
		changeSuggestionInfoView.setMargin(new MarginInfo(false, false, true, false));
		
		Account account = this.main.account;
		Office possibleAccountAssociatedOffice = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		
		//Only accounts that can write to comments are shown the buttons for comments, etc.
		if(UtilsComments.accountHasCommentToolWriteRights(main)) {
			
			//It's not enough that an account can write to a changeSuggestion - this allows them to update the Opinion of their Office.
			//Only system and resultControllers accounts can change the state of a ChangeSuggestion.
			boolean isCommentToolAdmin = UtilsComments.accountHasCommentToolAdminWriteRights(main);
			boolean isActive = changeSuggestion.isActive();
			boolean canWriteToChangeSuggestion = UtilsComments.accountCanWriteToChangeSuggestion(main, changeSuggestion);
			
			Office ownerOffice = UtilsDB.getPossibleAssociatedOffice(database, changeSuggestion);
			boolean isInAssociatedOffice = ownerOffice != null && possibleAccountAssociatedOffice != null && ownerOffice.uuid.equals(possibleAccountAssociatedOffice.uuid);
			
			if(isCommentToolAdmin || (canWriteToChangeSuggestion && isInAssociatedOffice)) {
				Button editChangeSuggestionButton = createEditSelectedChangeSuggestionButton();
				editChangeSuggestionButton.setEnabled((canWriteToChangeSuggestion || isCommentToolAdmin) && isActive);
				rightButtonToolbar.addComponent(editChangeSuggestionButton);
				rightButtonToolbar.setComponentAlignment(editChangeSuggestionButton, Alignment.TOP_RIGHT);
			}
			
			if(isCommentToolAdmin) {
				AbstractSelect selectionBox = createChangeSuggestionStateListBox();
				PopupView changeStatePopupView = createChangeStateOfChangeSuggestionPopupView(changeSuggestion, selectionBox);
				Button openChangeStateView = createUpdateChangeSuggestionStateButton(changeSuggestion, changeStatePopupView, selectionBox);
				rightButtonToolbar.addComponent(changeStatePopupView);
				rightButtonToolbar.addComponent(openChangeStateView);
				rightButtonToolbar.setComponentAlignment(openChangeStateView, Alignment.TOP_RIGHT);
			} else {
				//System.out.println("Account " + account.text + ", doesn't have write access to CS " + changeSuggestion.uuid);
			}

			if(canWriteToChangeSuggestion || isCommentToolAdmin) {
				ListSelect selectionBox = new ListSelect();
				PopupView requiredOfficesPopupview = createAddNewRequiredOfficePopupView(selectionBox);
				Button openRequiredOfficesButton = createRequireNewOfficeOpinionButton(requiredOfficesPopupview, selectionBox);
				rightButtonToolbar.addComponent(requiredOfficesPopupview);
				rightButtonToolbar.addComponent(openRequiredOfficesButton);
				rightButtonToolbar.setComponentAlignment(openRequiredOfficesButton, Alignment.TOP_RIGHT);
				
				requiredOfficesPopupview.setEnabled(isActive);
				openRequiredOfficesButton.setEnabled(isActive);
			} else {
				System.err.println("Account " + account.text + ", is not part of the comment tool admins");
			}
			
			createCommentTextAreaPopupView.setEnabled(isActive);
			addCommentButton.setEnabled(isActive);
		}
		
		top.addComponent(rightButtonToolbar);
		top.setComponentAlignment(rightButtonToolbar, Alignment.TOP_RIGHT);
		top.setExpandRatio(rightButtonToolbar, 0.0f);
		
		csRoot.addComponent(top);
		csRoot.setExpandRatio(top, 0.0f);
		csRoot.setComponentAlignment(top, Alignment.TOP_CENTER);
		
		csRoot.addComponent(opinionStates);
		csRoot.setComponentAlignment(opinionStates, Alignment.TOP_CENTER);
		csRoot.setExpandRatio(opinionStates, 0.0f);
		
		csRoot.addComponent(changeSuggestionInfoView);
		csRoot.setExpandRatio(changeSuggestionInfoView, 0.0f);
		
		updateChangeSuggestionFullView();
		return csRoot;
	}
	
	private void updateChangeSuggestionFullView() {
		clearChangeSuggestionFullView();
		ChangeSuggestion cs = this.currentSelectedChangeSuggestion;
		if(cs != null) {
			Database database = main.getDatabase();
			createStateViewFromOpinionGatherer(opinionStates, cs);
			
			String motivation = cs.getMotivation() == null ? "" : cs.getMotivation();
			String newSuggestion = cs.getNewSuggestion() == null ? "" : cs.getNewSuggestion();
			
			currentChangeSuggestionMotivationLabel.setValue("<h3><bold>" + Terminology.WHY_EDIT + "</bold></h3><div>" + motivation + "</div>");
			currentChangeSuggestionSuggestionLabel.setValue("<h3><bold>" + Terminology.HOW_TO_EDIT + "</bold></h3><div>" + newSuggestion + "</div>");
			String possibleTargetStringCaption = cs.getPossibleTargetString();
			if(possibleTargetStringCaption == null || possibleTargetStringCaption.equals("")) {
				possibleTargetStringCaption = "(muutosehdotuksella ei ole tekstikohdetta)";
			}
			currentChangeSuggestionTargetStringLabel.setValue("<h3><bold>" + Terminology.WHAT_TO_EDIT + "</bold></h3><div>" + possibleTargetStringCaption + "</div>");
			
			
			// Create the change suggestion table tree here:

			String uuid = cs.getPossibleBaseTargetUUID();
			List<String> targetBaseSummaryList = UtilsDB.changeSuggestionTargetPathAsList(database, cs);
					
			Label l = new Label();
			l.setContentMode(ContentMode.HTML);
			l.setWidth("100%");
			l.setValue("<h3><bold>" + Terminology.CHANGE_SUGGESTION_TARGET + "</bold></h3>");
			
			currentChangeSuggestionVisualLayout.removeAllComponents();
			currentChangeSuggestionVisualLayout.addComponent(l);
			currentChangeSuggestionVisualLayout.setMargin(new MarginInfo(true, false, false, false));
			
			TreeTable tree = Utils.emptyMapNavigationTreeTable();
			tree.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_TABLE);
			tree.addContainerProperty("", String.class, null);
			tree.setColumnExpandRatio("", 1.0f);
			tree.setWidth("100%");
			tree.setSelectable(uuid != null);
			
			tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
				
				private static final long serialVersionUID = -2569833291361138964L;

				@Override
				public void itemClick(ItemClickEvent event) {
					if(event.isDoubleClick()) {
						if(uuid != null) {
							Base b = database.find(uuid);
							navigateToBaseElement(database, b, 0);
						}
					}
				}
				
			});
			
			for(int i = 0; i < targetBaseSummaryList.size(); i++) {
				String baseInfo = targetBaseSummaryList.get(i);
				String caption = SafeHtmlUtils.htmlEscape(baseInfo);
				
				tree.addItem(new Object[] {
					caption	
					}, i);
				
				if(i != 0) {
					tree.setParent(i, i - 1);
				}
				
				if(i == (targetBaseSummaryList.size() - 1)) {
					tree.setChildrenAllowed(i, false);
				}
				tree.setCollapsed(i, true);
			}
			
			tree.addExpandListener(new ExpandListener() {

				private static final long serialVersionUID = -3555885111551307312L;

				@Override
				public void nodeExpand(ExpandEvent event) {
					for(int i = 0; i < targetBaseSummaryList.size(); i++) {
						tree.setCollapsed(i, false);	
					}
				}
				
			});
			
			currentChangeSuggestionVisualLayout.addComponent(tree);

			Office office = UtilsDB.getPossibleAssociatedOffice(database, cs);
			String officeText = "ei osastotietoa";
			if(office != null) {
				officeText = office.getText(database);
			}
			
			currentChangeSuggestionMetadataLabel.setValue("<div><bold>Osastolta [" + officeText +"]"
					+ " - Tila [" + ChangeSuggestion.changeSuggestionStateEnumToUIText(cs.getState()) + "]</bold></div>"
					+ "Luotu: " + cs.showCreationTime("yyyy-MM-dd HH:mm")
					+ "</br>" + cs.getDescription());
		} 
	}
	
	private void navigateToBaseElement(Database database, Base b, int tries) {
		tries++;
		if(b != null) {
			if(b instanceof MapBase) {
				main.forceSwitchToMapView();
				StrategyMap map = Utils.resolveMap(database, (MapBase)b);
				if(map != null) {
					UIState s = main.duplicateUIState();
					s.currentMap = map;
					s.customVis = null;
					main.setFragment(s, true);
				} else {
					Base parent = ((MapBase)b).getOwner(database);
					if(parent != null && tries < 4) {
						navigateToBaseElement(database, parent, tries);
					} else {
						System.err.println("Parent wall null or tried 4 times already!");
					}
				}
			} else {
				if(b instanceof TextChapter) {
					TextChapter chapter = (TextChapter)b;
					Office office = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
					if(office != null) {
						main.getUIState().currentOffice = office;
						main.forceSwitchToResultAgreementDocuments();
						if(main.allChaptersTabSheet.getComponentCount() > chapter.chapterIndex) {
							main.allChaptersTabSheet.setSelectedTab(chapter.chapterIndex);
						} else {
							System.err.println("Not enough chapter tabs to make switch to index " + chapter.chapterIndex);
						}
					} else {
						System.err.println("TextChapter had no associated Office - cannot switch views! " + chapter.uuid);
					}
				} else {
					System.err.println("Unhandled element type in navigateToBaseElement! " + b.getClass());
				}
			}
		}
	}
	
	private void clearChangeSuggestionFullView() {
		if(opinionStates != null) opinionStates.removeAllComponents();
		if(currentChangeSuggestionMotivationLabel != null) currentChangeSuggestionMotivationLabel.setValue("");
		if(currentChangeSuggestionSuggestionLabel != null) currentChangeSuggestionSuggestionLabel.setValue("");
		if(currentChangeSuggestionTargetStringLabel != null) currentChangeSuggestionTargetStringLabel.setValue("");
		if(currentChangeSuggestionVisualLayout != null) currentChangeSuggestionVisualLayout.removeAllComponents();
		if(currentChangeSuggestionMetadataLabel != null) currentChangeSuggestionMetadataLabel.setValue("");
	}
	
	/**
	 * Create the view that shows the original changesuggestion. Only contains the information of the change suggestion.
	 * @param changeSuggestion
	 * @return
	 */
	private VerticalLayout createChangeSuggestionInfoView(ChangeSuggestion changeSuggestion) {
		VerticalLayout changeSuggestionLayout = new VerticalLayout();
		changeSuggestionLayout.setStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION);
		
		changeSuggestionLayout.addComponent(currentChangeSuggestionVisualLayout);
		changeSuggestionLayout.setComponentAlignment(currentChangeSuggestionVisualLayout, Alignment.TOP_LEFT);
		changeSuggestionLayout.setExpandRatio(currentChangeSuggestionVisualLayout, 0.0f);
		
		changeSuggestionLayout.addComponent(currentChangeSuggestionTargetStringLabel);
		changeSuggestionLayout.setComponentAlignment(currentChangeSuggestionTargetStringLabel, Alignment.TOP_LEFT);
		changeSuggestionLayout.setExpandRatio(currentChangeSuggestionTargetStringLabel, 0.0f);
		
		changeSuggestionLayout.addComponent(currentChangeSuggestionSuggestionLabel);
		changeSuggestionLayout.setComponentAlignment(currentChangeSuggestionSuggestionLabel, Alignment.TOP_LEFT);
		changeSuggestionLayout.setExpandRatio(currentChangeSuggestionSuggestionLabel, 0.0f);
		
		changeSuggestionLayout.addComponent(currentChangeSuggestionMotivationLabel);
		changeSuggestionLayout.setComponentAlignment(currentChangeSuggestionMotivationLabel, Alignment.TOP_LEFT);
		changeSuggestionLayout.setExpandRatio(currentChangeSuggestionMotivationLabel, 0.0f);
		
		return(changeSuggestionLayout);
	}
	
	
	/**
	 * Create the view that contains office's official statements (changesuggestionopinions) and comments.
	 */
	private Panel createOpinionsLayout() {
		Panel rootPanel = new Panel();
		if(opinionGridlayout != null) {
			opinionGridlayout.removeAllComponents();
		}
		opinionGridlayout = new GridLayout();
		
		updateOpinionGridLayout();
		
		rootPanel.setSizeFull();
		rootPanel.setContent(opinionGridlayout);
		return rootPanel;
	}

	private void createStateViewFromOpinionGatherer(AbstractLayout layout, GathersOpinions opinionGatherer) {
		Account account = this.main.account;
		if(account != null) {
			layout.removeAllComponents();
			final boolean accountHasWriteRights = UtilsComments.accountHasCommentToolWriteRights(main);
			
			//Do not allow Offices to edit their own ChangeSuggestionOpinion state - it should always be "OK" (an office should always accept their own suggestion!)
			//For original ChangeSuggestions, offices are allowed to change their opinions.
			boolean isChangeSuggestionOpinion = opinionGatherer instanceof ChangeSuggestionOpinion;
			Office possibleOwnerOffice = UtilsDB.getPossibleAssociatedOffice(this.main.getDatabase(), opinionGatherer);
	
			layout.setWidth("100%");
			layout.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION);
			
			Map<String, OpinionState> opinions = opinionGatherer.getOpinionsAsMap(this.main.getDatabase());
			
			SortedSet<String> sortedKeys = new TreeSet<>();
			sortedKeys.addAll(opinions.keySet());
			Office possibleAccountOffice = UtilsDB.getPossibleAssociatedOfficeFromAccount(main.getDatabase(), account);
			
			boolean isActive = this.currentSelectedChangeSuggestion != null && this.currentSelectedChangeSuggestion.isActive();
			
			for(String key : sortedKeys) {
				OpinionState opinionEnum = opinions.get(key);
				Office office = Office.possiblyFindByName(main.getDatabase(), key);
				if(office == null) {
					Button b = createOpinionStateButton(null, opinionEnum, isChangeSuggestionOpinion);
					b.setEnabled(false);
					layout.addComponent(b);
				} else {
					//Found the matching office that gave this opinion
					Button b = createOpinionStateButton(office, opinionEnum, isChangeSuggestionOpinion);
					if(!isActive){
						//Only active change suggestion opinions can have their official statements and opinions updated
						b.setEnabled(false);
					} else if(isChangeSuggestionOpinion && possibleOwnerOffice != null && office.uuid.equals(possibleOwnerOffice.uuid)) {
						if(possibleAccountOffice != null && possibleAccountOffice.uuid.equals(office.uuid)) {
							//If we have a change suggestion opinion and the owner office is the office currently viewed,
							//we should allow the official statement to be edited.
							PopupView editStatementPopup = createEditOfficialStatementPopupView((ChangeSuggestionOpinion)opinionGatherer);
							layout.addComponent(editStatementPopup);
							b.setIcon(FontAwesome.EDIT);
							b.setStyleName(ValoTheme.BUTTON_TINY);
							b.addStyleName(CustomCSSStyleNames.STYLE_WHITE);
							b.setDescription(Terminology.EDIT_OFFICIAL_STATEMENT);
							b.setVisible(true); //Force visibility to be always true
							
							b.addClickListener(new ClickListener() {
								
								/**
								 * 
								 */
								private static final long serialVersionUID = 4256565858358910130L;

								@Override
								public void buttonClick(ClickEvent event) {
									if(UtilsDB.canWrite(MainCommentLayout.this.main.getDatabase(), account, opinionGatherer)) {
										editStatementPopup.setPopupVisible(true);
									} else {
										System.err.println("Account " + account.uuid + " attempted to edit official statement an opinion state that it cannot write!");	
									}
								}
		
							});
							b.setEnabled(accountHasWriteRights);
						} else {
							//If the current opinion being viewed wasn't the same as the logged in account's,
							//then we cannot edit the official statement or otherwise change the opinion in any way.
							b.setEnabled(false);
						}

					} else {
						Office possibleOffice = UtilsDB.getPossibleAssociatedOfficeFromAccount(this.main.getDatabase(), account);
						boolean accountBelongsToOffice = possibleOffice != null && possibleOffice.uuid.equals(office.uuid);
						
						b.setEnabled(accountHasWriteRights && accountBelongsToOffice);
						
						if(accountHasWriteRights && accountBelongsToOffice) {
							AbstractSelect selectionBox = createOpinionStateListBox();
							PopupView popup = createOpinionStatePopUp(b, selectionBox, opinionGatherer, possibleOffice);
							layout.addComponent(popup);
							
							b.addClickListener(new ClickListener() {
		
								private static final long serialVersionUID = -3369001098299731375L;

								@Override
								public void buttonClick(ClickEvent event) {
									if(UtilsDB.canRead(MainCommentLayout.this.main.getDatabase(), account, opinionGatherer)) {
										//Read access is enough to edit the office opinion.
										//Write access means it is allowed to edit the official statement.
										popup.setPopupVisible(true);
										//System.out.println("Is allowed to edit opinion for their office.");
									} else {
										System.err.println("Account " + account.uuid + " attempted to edit an opinion state that it cannot read!");	
									}
								}
		
							});
						}
					}
					
					layout.addComponent(b);
				}
	
			}
		}
	}

	private AbstractSelect createOpinionStateListBox() {
		ListSelect box = new ListSelect();
		box.setStyleName(ValoTheme.COMBOBOX_TINY);
		box.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION_STATE_COMBO_BOX);
		
		List<String> options = new ArrayList<>();
		for(OpinionState optionEnum : OpinionState.values()) {
			options.add(enumToUIText(optionEnum));
		}
		
		box.addItems(options);
		box.setRows(options.size());
		box.setNullSelectionAllowed(false);
		return box;
	}
	
	private PopupView createOpinionStatePopUp(Button button, AbstractSelect selectionBox, GathersOpinions opinionGatherer, Office office) {
		PopupView popup = new PopupView("", selectionBox);
		
		selectionBox.addValueChangeListener(new ValueChangeListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1843138652378318009L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object item = event.getProperty().getValue();
				OpinionState oldOpinion = opinionGatherer.getOpinionsAsMap(MainCommentLayout.this.main.getDatabase()).get(office.getText(main.getDatabase()));
				//If account has explicit read access to the object gathering opinions, they can change their office's opinion
				if(UtilsDB.canRead(main.getDatabase(), main.getAccountDefault(), opinionGatherer) && !enumToUIText(oldOpinion).equals(item.toString())) {
					//System.out.println("Changing value to: " + item);
					OpinionState newOpinion = uiTextToEnum(item.toString());
					if(newOpinion != null) {
						updateOpinionAndRefresh(button, opinionGatherer, office, newOpinion);
					}
					popup.setPopupVisible(false);
				} else {
					//Value is unchanged
				}
				
			}
			
		});
		
		popup.addPopupVisibilityListener(event -> {
		    if (event.isPopupVisible()) {
		    	if(opinionGatherer == null || office == null) {
		    		System.err.print("OpinionGatherer or Office were null during popup event visibility trigger!");
		    	} else {
			    	OpinionState opinionEnum = opinionGatherer.getOpinionsAsMap(this.main.getDatabase()).get(office.getText(main.getDatabase()));
			    	if(opinionEnum != null) {
			    		selectionBox.select(enumToUIText(opinionEnum));
			    	} else {
			    		System.err.println("Expected to find opinion enum for popup combobox, got null! OpinionGatherer: " + opinionGatherer.uuid + ", Office: " + office.uuid);
			    	}
		    	}
		    }});
		
		
		return popup;
	}
	
	private void updateOpinionAndRefresh(Button button, GathersOpinions opinionGatherer, Office office, OpinionState newOpinion) {
		opinionGatherer.updateOpinion(main.getDatabase(), office, newOpinion);
		refreshOpinionStateButton(button, newOpinion, office, opinionGatherer instanceof ChangeSuggestionOpinion);
		Updates.update(main, true);
	}
	
	private void refreshOpinionStateButton(Button button, OpinionState opinion, Office office, boolean isChangeSuggestionOpinion) {
		updateButtonStyle(button, opinion, false);
		String officeText = "Virhe - osaston tietoa ei löydetty!";
		if(office != null) {
			officeText = office.getText(main.getDatabase());
			button.setCaption(office.getShortId(main.getDatabase()));
		} else {
			button.setCaption("");
		}
		
		button.setDescription(enumToAppHelpUIText(officeText, opinion, isChangeSuggestionOpinion) + legends);
		button.setVisible(true);
	}
	
	private boolean addNewComment(ChangeSuggestion changeSuggestion, String text) {
		Database database = main.getDatabase();
		List<ChangeSuggestion> suggestions = UtilsDBComments.getPossibleLinkedChangeSuggestions(database, database.getRoot());
		for(ChangeSuggestion candidate : suggestions) {
			if(candidate.uuid.equals(changeSuggestion.uuid)) {
				Comment comment = UtilsComments.possiblyCreateComment(main, text, candidate);
				boolean ok = comment != null;
				if(ok) {
					Updates.update(main, true);
					refresh();
				}
				return ok;
			}
		}
		return false;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Add new offices so their opinion counts
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private PopupView createAddNewRequiredOfficePopupView(ListSelect selectionBox) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setMargin(true);
		
		layout.setWidth("300px");
		layout.setHeight("300px");
		
		PopupView popup = new PopupView("", layout);
		popup.setHideOnMouseOut(false);
		
		Label label = new Label("Valitse lisättävä Virasto / Osasto");
		
		selectionBox.setStyleName(ValoTheme.COMBOBOX_TINY);
		selectionBox.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION_STATE_COMBO_BOX);
		
		selectionBox.setNullSelectionAllowed(false);
		selectionBox.setSizeFull();
		
		Button closePopupButton = new Button();
		closePopupButton.setIcon(FontAwesome.CLOSE);
		closePopupButton.setCaption("Sulje");
		closePopupButton.setStyleName(ValoTheme.BUTTON_TINY);
		closePopupButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4131353218265704115L;

			@Override
			public void buttonClick(ClickEvent event) {
				popup.setPopupVisible(false);
			}

		});
		
		Button addOfficeButton = new Button();
		addOfficeButton.setIcon(FontAwesome.CHECK);
		addOfficeButton.setCaption("Vaadi kantaa");
		addOfficeButton.setStyleName(ValoTheme.BUTTON_TINY);
		addOfficeButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -3345322250973073748L;

			@Override
			public void buttonClick(ClickEvent event) {
				String possibleOfficeName = selectionBox.getValue().toString();
				List<Office> missingOffices = findMissingOffices();
				for(Office office : missingOffices) {
					if(office.getText(main.getDatabase()).equals(possibleOfficeName)) {
						requireNewOfficeOpinion(MainCommentLayout.this.currentSelectedChangeSuggestion, office);
						Updates.update(main, true);
						refresh();
						break;
					}	
				}
				popup.setPopupVisible(false);
			}

		});
		
		layout.addComponent(label);
		layout.setComponentAlignment(label, Alignment.TOP_CENTER);
		layout.setExpandRatio(label, 0.1f);
		
		layout.addComponent(selectionBox);
		layout.setComponentAlignment(selectionBox, Alignment.TOP_CENTER);
		layout.setExpandRatio(selectionBox, 0.7f);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addComponent(closePopupButton);
		buttonLayout.setComponentAlignment(closePopupButton, Alignment.BOTTOM_LEFT);
		buttonLayout.addComponent(addOfficeButton);
		buttonLayout.setComponentAlignment(addOfficeButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setWidth("100%");
		
		layout.addComponent(buttonLayout);
		layout.setExpandRatio(buttonLayout, 0.2f);
		
		return popup;
	}

	private Button createEditSelectedChangeSuggestionButton() {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setIcon(FontAwesome.EDIT);
		b.setDescription("Muokkaa muutosehdotusta");
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 4508139960019895700L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(currentSelectedChangeSuggestion != null && UtilsComments.accountCanWriteToChangeSuggestion(main, currentSelectedChangeSuggestion)) {
					openEditChangeSuggestionDialog(currentSelectedChangeSuggestion);
				} else {
					System.err.println("User that cannot write to change suggestion tried to edit it!");
				}
			}

		});
		
		return b;
	}
	
	private Button createRequireNewOfficeOpinionButton(PopupView popupView, ListSelect selectionBox) {
		Button b = new Button();
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.setIcon(FontAwesome.BRIEFCASE);
		b.setDescription("Vaadi uuden Viraston/Osaston kantaa");
		b.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4508139960019895700L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(currentSelectedChangeSuggestion != null && UtilsComments.accountCanWriteToChangeSuggestion(main, currentSelectedChangeSuggestion)) {
					List<String> options = new ArrayList<>();
					
					List<Office> missingOffices = findMissingOffices();
					for(Office office : missingOffices) {
						//System.out.println("Adding office " + office.text + " to selection list!");
						options.add(office.getText(main.getDatabase()));
					}
					
					selectionBox.removeAllItems();
					selectionBox.addItems(options);
					selectionBox.setRows(options.size()+1);
					
					popupView.setPopupVisible(true);
				} else {
					System.err.println("Account cannot write to ChangeSuggestion!");
				}
			}

		});
		
		return b;
	}
	
	private List<Office> findMissingOffices(){
		List<Office> allOffices = Office.enumerate(main.getDatabase(), false, true);
		Set<Office> currentOffices = UtilsDBComments.findRelatedOffices(main.getDatabase(), currentSelectedChangeSuggestion);
		allOffices.removeAll(currentOffices);
		return allOffices;
	}
	
	private boolean requireNewOfficeOpinion(ChangeSuggestion changeSuggestion, Office office) {
		ChangeSuggestionOpinion newOpinion = UtilsDBComments.possiblyCreateChangeSuggestionOpinionAndGiveAccessRight(main.getDatabase(), changeSuggestion, office,
				main.getDatabase().getDefaultAdminAccountGroup());
		newOpinion.setOfficialStatement("(virallista kantaa ei annettu)");
		return false;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Add new offices so that their opinion counts, ends here
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private PopupView createCommentTextAreaPopupView() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		TextArea textArea = new TextArea();
		textArea.setWidth("600px");
		textArea.setHeight("300px");
		
		PopupView popup = new PopupView("", layout);
		popup.setHideOnMouseOut(false);
		
		Label label = new Label("Uusi kommentti");
		
		Button closePopupButton = new Button();
		closePopupButton.setIcon(FontAwesome.CLOSE);
		closePopupButton.setCaption("Sulje");
		closePopupButton.setStyleName(ValoTheme.BUTTON_TINY);
		closePopupButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -6870325127760066095L;

			@Override
			public void buttonClick(ClickEvent event) {
				textArea.setValue("");
				popup.setPopupVisible(false);
			}

		});
		
		Button sendCommentButton = new Button();
		sendCommentButton.setIcon(FontAwesome.SEND);
		sendCommentButton.setCaption("Kommentoi");
		sendCommentButton.setStyleName(ValoTheme.BUTTON_TINY);
		sendCommentButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 2149270705006544112L;

			@Override
			public void buttonClick(ClickEvent event) {
				String sanitizedString = SafeHtmlUtils.htmlEscape(textArea.getValue());
				String check_if_empty = sanitizedString.replaceAll(" ", "");
				check_if_empty = check_if_empty.replaceAll("\t", "");
				check_if_empty = check_if_empty.replaceAll("\n", "");
				if(!check_if_empty.equals("")) {
					boolean ok = addNewComment(currentSelectedChangeSuggestion, sanitizedString);
					popup.setPopupVisible(false);
					if(ok) {
						textArea.setValue("");
					} else {
	            		VerticalLayout layout = new VerticalLayout();
	            		layout.setHeight("100px");
	            		layout.setWidth("200px");
	    		        Dialogs.errorDialog(main, "Kommentin luonnissa tapahtui virhe!", layout, "800px", "100px");
					}
				} else {
					popup.setPopupVisible(false);
					textArea.setValue("");
					
            		VerticalLayout layout = new VerticalLayout();
            		layout.setHeight("100px");
            		layout.setWidth("200px");
    		        Dialogs.errorDialog(main, "Kommenttia ei voitu tallentaa (tyhjä kommentti)", layout, "800px", "100px");
				}
			}

		});
		
		layout.addComponent(label);
		layout.setExpandRatio(label, 0.0f);
		layout.addComponent(textArea);
		layout.setExpandRatio(textArea, 1.0f);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addComponent(closePopupButton);
		buttonLayout.setComponentAlignment(closePopupButton, Alignment.BOTTOM_LEFT);
		buttonLayout.addComponent(sendCommentButton);
		buttonLayout.setComponentAlignment(sendCommentButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setWidth("100%");
		
		layout.addComponent(buttonLayout);
		layout.setExpandRatio(buttonLayout, 0.0f);
		
		return popup;
	}
	
	private Button createAddCommentButton(PopupView popupView) {
		Button b = new Button();
		b.setIcon(FontAwesome.COMMENT_O);
		b.setCaption("Luo uusi kommentti");
		b.setDescription("Kommentoi muutosehdotusta");
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1151306327177655917L;

			@Override
			public void buttonClick(ClickEvent event) {
				if(currentSelectedChangeSuggestion != null && UtilsComments.accountCanWriteToChangeSuggestion(main, currentSelectedChangeSuggestion)) {
					popupView.setPopupVisible(true);
				} else {
					System.err.println("Account cannot write to selected change suggestion, but createAddCommentButton button called!");
				}
			}

		});
		
		return b;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Translate serverside enumerations related to change suggestions and states to UI and back
	////////////////////////////////////////////////////////////////////////////////////
	
	public static final String opinionmissingText = "Kantaa ei otettu";
	public static final String requiresmoredetailsText = "Vaatii selvennystä";
	public static final String okText = "Hyväksytty";
	public static final String notokText = "Hylätty";
	
	public static final String legends = "</br></br>Väriselitykset:</br>Harmaa: " + opinionmissingText
			+ "</br>Punainen: " + notokText 
			+ "</br>Keltainen: " + requiresmoredetailsText
			+ "</br>Vihreä: " + okText;
	/*
	 * "</br></br>Harmaa: virasto ei ole kommentoinut, kommenttia odotetaan"
			+ "</br>Punainen: virasto hylkää muutosehdotuksen"
			+ "</br>Keltainen: virasto odottaa tarkennusta muutosehdotukseen"
			+ "</br>Vihreä: virasto hyväksyy muutosehdotuksen";
	 */
	
	/**
	 * Helper function, turn String shown to end-user back to serverside enum
	 * @param uiText
	 * @return
	 */
	private static OpinionState uiTextToEnum(String uiText) {
		switch(uiText) {
			case okText: return OpinionState.OK;
			case requiresmoredetailsText: return OpinionState.EXPLANATION_NEEDED;
			case opinionmissingText: return OpinionState.NO_OPINION_GIVEN;
			case notokText: return OpinionState.NOT_OK;
			default: return null;
		}
	}
	
	/**
	 * Helper function, turn serverside enum to String shown to end-user
	 * @param opinionEnum
	 * @return
	 */
	private static String enumToUIText(OpinionState opinionEnum) {
		switch(opinionEnum) {
			case OK: return okText;
			case EXPLANATION_NEEDED: return requiresmoredetailsText;
			case NO_OPINION_GIVEN: return opinionmissingText;
			case NOT_OK: return notokText;
			default: return "";
		}
	}
	
	private static String enumToAppHelpUIText(String name, OpinionState opinionEnum, boolean isChangeSuggestionOpinion) {
		
		switch(opinionEnum) {
			case OK: {
				if(!isChangeSuggestionOpinion) {
					return name + " hyväksyy muutosehdotuksen";
				} else {
					return name + " hyväksyy tämän kannan";
				}
			}
			case EXPLANATION_NEEDED: {
				if(!isChangeSuggestionOpinion) {
					return name + " odottaa tarkennusta muutosehdotukseen";
				} else {
					return name + " odottaa tarkennusta kantaan";
				}
			}
			case NO_OPINION_GIVEN: {
				if(!isChangeSuggestionOpinion) {
					return name + " ei ole kommentoinut muutosehdotusta, kommenttia odotetaan";
				} else {
					return name + " ei ole kommentoinut kantaa";
				}
			}
			case NOT_OK: {
				if(!isChangeSuggestionOpinion) {
					return name + " hylkää muutosehdotuksen";
				} else {
					return name + " hylkää tämän kannan";
				}
			}
			default: {
				return "";
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// End of enum translations
	////////////////////////////////////////////////////////////////////////////////////
	
	private void updateButtonStyle(Button b, OpinionState newOpinion, boolean iconOnly) {
		switch(newOpinion) {
			case OK: {
				if(iconOnly) {
					b.setStyleName(CustomCSSStyleNames.STYLE_ICON_GREEN);
				} else {
					b.setStyleName(CustomCSSStyleNames.STYLE_GREEN);
				}
				break;
			}
			case EXPLANATION_NEEDED: {
				if(iconOnly) {
					b.setStyleName(CustomCSSStyleNames.STYLE_ICON_YELLOW);
				} else {
					b.setStyleName(CustomCSSStyleNames.STYLE_YELLOW);
				}
				break;
			}
			case NO_OPINION_GIVEN: {
				if(iconOnly) {
					b.setStyleName(CustomCSSStyleNames.STYLE_ICON_GREY);
				} else {
					b.setStyleName(CustomCSSStyleNames.STYLE_GREY);
				}
				break;
			}
			case NOT_OK: {
				if(iconOnly) {
					b.setStyleName(CustomCSSStyleNames.STYLE_ICON_RED);
				} else {
					b.setStyleName(CustomCSSStyleNames.STYLE_RED);
				}
				break;
			}
			default: {
				break;
			}
		}
		b.addStyleName(ValoTheme.BUTTON_TINY);
	}
	
	private Button createOpinionStateButton(Office office, OpinionState opinionEnum, boolean isChangeSuggestionOpinion) {
		Button b = new Button();
		b.addStyleName(ValoTheme.BUTTON_TINY);
		b.setHeight("24px");
		b.setEnabled(false); //By default not enabled!
		refreshOpinionStateButton(b, opinionEnum, office, isChangeSuggestionOpinion);
		return b;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Edit Official Statement for an opinion
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private PopupView createEditOfficialStatementPopupView(ChangeSuggestionOpinion cso) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);
		
		TextArea textArea = new TextArea();
		textArea.setWidth("400px");
		textArea.setHeight("400px");
		
		PopupView popup = new PopupView("", layout);
		popup.setHideOnMouseOut(false);
		
		popup.addPopupVisibilityListener(event -> {
		    if (event.isPopupVisible()) {
		    	textArea.setValue(cso.text);
		    }});
		
		Label label = new Label("Muokkaa virallista kantaa");
		
		Button closePopupButton = new Button();
		closePopupButton.setIcon(FontAwesome.CLOSE);
		closePopupButton.setCaption("Sulje");
		closePopupButton.setStyleName(ValoTheme.BUTTON_TINY);
		closePopupButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 5333076284868590479L;

			@Override
			public void buttonClick(ClickEvent event) {
				popup.setPopupVisible(false);
			}

		});
		
		Button saveStatementButton = new Button();
		saveStatementButton.setIcon(FontAwesome.SEND);
		saveStatementButton.setCaption("Muokkaa kannanottoa");
		saveStatementButton.setStyleName(ValoTheme.BUTTON_TINY);
		saveStatementButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -247361644904549430L;

			@Override
			public void buttonClick(ClickEvent event) {
				String sanitizedString = SafeHtmlUtils.htmlEscape(textArea.getValue());
				sanitizedString = sanitizedString.replaceAll("&quot;", "\"");
				updateOfficialStatement(sanitizedString);
				refresh();
				popup.setPopupVisible(false);
				textArea.setValue("");
			}

		});
		
		layout.addComponent(label);
		layout.setExpandRatio(label, 0.0f);
		layout.addComponent(textArea);
		layout.setExpandRatio(textArea, 1.0f);
		
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.addComponent(closePopupButton);
		buttonLayout.setComponentAlignment(closePopupButton, Alignment.BOTTOM_LEFT);
		buttonLayout.addComponent(saveStatementButton);
		buttonLayout.setComponentAlignment(saveStatementButton, Alignment.BOTTOM_RIGHT);
		buttonLayout.setWidth("100%");
		
		layout.addComponent(buttonLayout);
		layout.setExpandRatio(buttonLayout, 0.0f);
		
		return popup;
	}
	
	private void updateOfficialStatement(String newStatement) {
		if(this.currentSelectedChangeSuggestion != null) {
			if(this.currentSelectedChangeSuggestion.isActive()) {
				if(this.main.account != null) {
					Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(this.main.getDatabase(), this.main.account);
					if(office != null) {
						UtilsComments.updateOfficialStatement(this.main, this.currentSelectedChangeSuggestion, office, newStatement);
						Updates.update(main, true);
					} else {
						System.err.println("Failed to find the office associated with the account " + this.main.account.text);
					}
				} else {
					System.err.println("Null account tried to update official statement!");
				}
			} else {
				System.err.println("Tried to update a change suggestion that isn't active!");
			}
		} else {
			System.err.println("FATAL ERROR - Tried to update official statement but current selected change suggestionw as null");
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Edit Official Statement for an opinion ends here
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Completely refreshes the gridlayout from the database
	 */
	private void updateOpinionGridLayout() {
		if(opinionGridlayout != null && this.currentSelectedChangeSuggestion != null) {
			opinionGridlayout.removeAllComponents();
			
			HorizontalLayout buttonToolbar = new HorizontalLayout();
			buttonToolbar.setMargin(true);
			buttonToolbar.setSpacing(true);
			buttonToolbar.setHeight("30px");
			
			buttonToolbar.addComponent(changeCommentOrderButton);
			buttonToolbar.setExpandRatio(changeCommentOrderButton, 0.0f);
			buttonToolbar.setComponentAlignment(changeCommentOrderButton, Alignment.TOP_LEFT);
			buttonToolbar.addComponent(createCommentTextAreaPopupView);
			buttonToolbar.setExpandRatio(createCommentTextAreaPopupView, 0.0f);
			buttonToolbar.setComponentAlignment(createCommentTextAreaPopupView, Alignment.TOP_LEFT);
			buttonToolbar.addComponent(addCommentButton);
			buttonToolbar.setExpandRatio(addCommentButton, 1.0f);
			buttonToolbar.setComponentAlignment(addCommentButton, Alignment.TOP_LEFT);
			
			List<Base> relatedObjects = UtilsDBComments.findContainsObjects(main.getDatabase(), this.currentSelectedChangeSuggestion);
			List<Base> relatedCommentsAsBase = new ArrayList<>();

			for(Base b : relatedObjects) {
				if(b instanceof Comment) {
					relatedCommentsAsBase.add(b); //Add as Base, later converted
				}
			}
			
			opinionGridlayout.setColumns(1);
			opinionGridlayout.setRows(relatedCommentsAsBase.size()+1);
			opinionGridlayout.setHideEmptyRowsAndColumns(false);
			
			boolean descending = !main.getUIState().commentsOrderAscending;
			UtilsDB.sortBaseByCreationTime(relatedCommentsAsBase, descending);
			
			opinionGridlayout.addComponent(buttonToolbar);
			int row = 1;
			for(Base b : relatedCommentsAsBase) {
				Comment comment = (Comment)b;
				Component commentComponent = createCommentTextLabel(comment);
				opinionGridlayout.addComponent(commentComponent, 0, row);
				row++;
			}
		}
	}
	
	/**
	 * Admins can always delete.
	 * Account that created can delete, assuming ChangeSuggestion is OPEN
	 * and less than X hours have passed since comment was created.
	 * @param database
	 * @param comment
	 * @return
	 */
	private boolean commentDeletionIsAllowed(Database database, Comment comment) {
		ChangeSuggestion cs = this.currentSelectedChangeSuggestion;
		if(cs != null && main.account != null) {
			if(main.account.isAdmin(database)) {
				return true;
			} else {
				if(cs.getState().equals(ChangeSuggestionState.OPEN_ACTIVE)) {
					boolean isFromAccount = UtilsDBComments.baseIsFromAccount(database, comment, main.account);
					if(isFromAccount) {
						Long creationTime = comment.creationTime;
						Long now = Calendar.getInstance().getTime().getTime();
						Long diff = now - creationTime;
						int hours_passed = (int) (diff / (60 * 60 * 1000));
						
						return hours_passed < Configuration.getMAX_COMMENT_DELETION_HOURS();
					}
				}
			}
		}
		
		return false;
	}
	
	private Component createCommentTextLabel(Comment comment) {
		Database database = this.main.getDatabase();
		
		HorizontalLayout commentLayout = new HorizontalLayout();
		commentLayout.setWidth("100%");
		
		String time = comment.showCreationTime("yyyy-MM-dd HH:mm");
		
		Account account = UtilsDBComments.getPossibleAssociatedAccount(database, comment);
		Office office = UtilsDB.getPossibleAssociatedOffice(database, comment);
		String accountText = account != null ? account.text : "-";
		
		String officeText = office != null ? "<b>" + office.getText(database) + "</b>" : "Käyttäjältä: <b>" + accountText + "</b>";
		
		String commentText = comment.text;
		commentText = commentText.replaceAll("\n", "</br>");
		
		Label label = new Label();
		label.addStyleName(CustomCSSStyleNames.STYLE_COMMENT);
		label.setContentMode(ContentMode.HTML);
		label.setValue("<div style=\"width:100%;vertical-align:middle;text-align:center;font-size:12px;\">" + officeText + "</div>" +
				"<div style=\"width:100%;vertical-align:middle;text-align:center;font-size:11px;\">" + time + "</div>" +
				"<div style=\"font-size:14px;\">" + commentText + "</div>");
		label.setWidth("100%");
		
		label.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION_SECTION);
		
		commentLayout.addComponent(label);
		commentLayout.setExpandRatio(label, 1.0f);
		commentLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		
		if(commentDeletionIsAllowed(database, comment)) {
			HorizontalLayout popupContent = new HorizontalLayout();
			popupContent.setSpacing(true);
			popupContent.setMargin(true);
			popupContent.setWidth("275px");
			popupContent.setHeight("50px");
			
			PopupView confirmDeletionPopup = new PopupView("", popupContent);
			confirmDeletionPopup.setHideOnMouseOut(false);
			
			Button confirm = new Button();
			confirm.setCaption("Poista Kommentti");
			confirm.setIcon(FontAwesome.TRASH);
			confirm.setStyleName(ValoTheme.BUTTON_TINY);
			
			confirm.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -1687349456782465958L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmDeletionPopup.setPopupVisible(false);
					
					if(commentDeletionIsAllowed(database, comment)) {
						UtilsDBComments.deleteComment(database, comment, MainCommentLayout.this.currentSelectedChangeSuggestion, main.account);
						Updates.update(main, true);
					} else {
						Updates.update(main, false);
						VerticalLayout layout = new VerticalLayout();
	            		layout.setHeight("100px");
	            		layout.setWidth("200px");
	    		        Dialogs.errorDialog(main, "Kommenttia ei voitu poistaa!", layout, "400px", "100px");
					}

					//Update view here
					updateOpinionGridLayout();
					resizeInternal();
				}
				
			});
			
			Button cancel = new Button();
			cancel.setCaption("Sulje");
			cancel.setIcon(FontAwesome.CLOSE);
			cancel.setStyleName(ValoTheme.BUTTON_TINY);
			cancel.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 2178626610672569898L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmDeletionPopup.setPopupVisible(false);
				}
				
			});
			
			popupContent.addComponent(cancel);
			popupContent.setExpandRatio(cancel, 0.0f);
			popupContent.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
			popupContent.addComponent(confirm);
			popupContent.setExpandRatio(confirm, 1.0f);
			popupContent.setComponentAlignment(confirm, Alignment.MIDDLE_LEFT);
			
			Button deleteButton = new Button();
			deleteButton.setCaption("");
			deleteButton.setIcon(FontAwesome.TRASH);
			deleteButton.setStyleName(ValoTheme.BUTTON_TINY);
			deleteButton.setDescription("Poista kommentti."
					+ "</br>Kommentin poisto vain mahdollista avoimille muutosehdotuksille."
					+ "</br>Vain kommentin tekijä voi poistaa sen, "
					+ Configuration.getMAX_COMMENT_DELETION_HOURS() + " tuntia luonnin jälkeen.");

			deleteButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 499788149019139091L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmDeletionPopup.setPopupVisible(true);
				}

			});

			commentLayout.addComponent(confirmDeletionPopup);
			commentLayout.setExpandRatio(confirmDeletionPopup, 0.0f);
			commentLayout.setComponentAlignment(confirmDeletionPopup, Alignment.TOP_RIGHT);
			commentLayout.addComponent(deleteButton);
			commentLayout.setExpandRatio(deleteButton, 0.0f);
			commentLayout.setComponentAlignment(deleteButton, Alignment.TOP_RIGHT);
		}
		
		return commentLayout;
	}

	/**
	 * @param cs
	 * @return
	 */
	private PopupView createChangeStateOfChangeSuggestionPopupView(ChangeSuggestion cs, AbstractSelect selectionBox) {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("300px");
		vl.setSpacing(true);
		
		Label l = new Label();
		l.setStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION);
		l.setValue("<h3>Muokkaa muutosehdotuksen tilaa</h3>" + 
				"<div>Nykyinen tila:</br>"+ ChangeSuggestion.changeSuggestionStateEnumToUIText(cs.getState()) + "</div>");
		l.setContentMode(ContentMode.HTML);
		l.setWidth("300px");
		l.setSizeFull();
		
		PopupView popupView = new PopupView("", vl);
		popupView.setHideOnMouseOut(false);
		
		
		Button closeStateChangePopup = new Button();
		closeStateChangePopup.setIcon(FontAwesome.CLOSE);
		closeStateChangePopup.setCaption("Sulje");
		closeStateChangePopup.setStyleName(ValoTheme.BUTTON_TINY);
		
		closeStateChangePopup.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8650857891966631150L;

			@Override
			public void buttonClick(ClickEvent event) {
				popupView.setPopupVisible(false);
			}

		});
		
		Button showDeleteEntirelyPopupViewButton = null;
		HorizontalLayout popupContent = new HorizontalLayout();
		final PopupView confirmChangeSuggestionDeletionPopup = new PopupView("", popupContent);
		
		if(main.account != null && main.account.isAdmin(main.getDatabase())) {
			popupContent.setSpacing(true);
			popupContent.setMargin(true);
			popupContent.setWidth("400px");
			popupContent.setHeight("50px");
			confirmChangeSuggestionDeletionPopup.setHideOnMouseOut(false);
			
			Button confirm = new Button();
			confirm.setCaption("Poista muutosehdotus pysyvästi");
			confirm.setIcon(FontAwesome.TRASH);
			confirm.setStyleName(ValoTheme.BUTTON_TINY);
			
			confirm.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -1687349456782465958L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmChangeSuggestionDeletionPopup.setPopupVisible(false);
					
					if(main.account != null && main.account.isAdmin(main.getDatabase())) {
						cs.remove(main.getDatabase());
						Updates.update(main, true);
					} else {
						Updates.update(main, false);
						VerticalLayout layout = new VerticalLayout();
	            		layout.setHeight("100px");
	            		layout.setWidth("200px");
	    		        Dialogs.errorDialog(main, "Muutosehdotusta ei voitu poistaa!", layout, "400px", "100px");
	    		    }

					//Update view here
					switchCurrentSelectedChangeSuggestionView(null);
				}
				
			});
			
			Button cancel = new Button();
			cancel.setCaption("Sulje");
			cancel.setIcon(FontAwesome.CLOSE);
			cancel.setStyleName(ValoTheme.BUTTON_TINY);
			cancel.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 2178626610672569898L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmChangeSuggestionDeletionPopup.setPopupVisible(false);
				}
				
			});
			
			popupContent.addComponent(cancel);
			popupContent.setExpandRatio(cancel, 0.0f);
			popupContent.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
			popupContent.addComponent(confirm);
			popupContent.setExpandRatio(confirm, 1.0f);
			popupContent.setComponentAlignment(confirm, Alignment.MIDDLE_LEFT);
			
			showDeleteEntirelyPopupViewButton = new Button();
			showDeleteEntirelyPopupViewButton.setCaption("Poista");
			showDeleteEntirelyPopupViewButton.setIcon(FontAwesome.TRASH);
			showDeleteEntirelyPopupViewButton.setStyleName(ValoTheme.BUTTON_TINY);
			showDeleteEntirelyPopupViewButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -6258624941869553816L;

				@Override
				public void buttonClick(ClickEvent event) {
					confirmChangeSuggestionDeletionPopup.setPopupVisible(true);
				}
				
			});
			
		}
		
		selectionBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 4231761797236459699L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Database database = main.getDatabase();
				Account account = main.account;
				if(account != null && UtilsDB.canWrite(database, account, cs)) {
					Object item = event.getProperty().getValue();
					if(!ChangeSuggestion.changeSuggestionStateEnumToUIText(cs.getState()).equals(item.toString())) {
						ChangeSuggestionState newState = ChangeSuggestion.uiTextToChangeSuggestionState(item.toString());
						if(newState != null) {
							//System.out.println("Changing state from " + cs.state.name() + " to " + newState.name());
							cs.updateState(account, database, newState);
							Updates.update(main, true);
							refresh();
						}
						popupView.setPopupVisible(false);
						
						if(confirmChangeSuggestionDeletionPopup != null) {
							confirmChangeSuggestionDeletionPopup.setPopupVisible(false);
						}
					} else {
						//Value is unchanged
					}
				} else {
					System.err.println("Illegal account tried to edit the state of ChangeSuggestion " + cs.uuid);
				}
				
			}
			
		});

		
		vl.addComponent(l);
		vl.setExpandRatio(l, 0.0f);
		vl.setComponentAlignment(l, Alignment.TOP_CENTER);
		
		vl.addComponent(selectionBox);
		vl.setExpandRatio(selectionBox, 1.0f);
		vl.setComponentAlignment(selectionBox, Alignment.TOP_CENTER);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setSpacing(true);
		buttonToolbar.setMargin(new MarginInfo(false, true, false, true));
		buttonToolbar.setSizeFull();
		
		buttonToolbar.addComponent(closeStateChangePopup);
		buttonToolbar.setExpandRatio(closeStateChangePopup, 0.0f);
		buttonToolbar.setComponentAlignment(closeStateChangePopup, Alignment.BOTTOM_LEFT);
		
		if(showDeleteEntirelyPopupViewButton != null && confirmChangeSuggestionDeletionPopup != null) {
			buttonToolbar.addComponent(confirmChangeSuggestionDeletionPopup);
			buttonToolbar.setExpandRatio(confirmChangeSuggestionDeletionPopup, 0.0f);
			buttonToolbar.setComponentAlignment(confirmChangeSuggestionDeletionPopup, Alignment.BOTTOM_LEFT);
			
			buttonToolbar.addComponent(showDeleteEntirelyPopupViewButton);
			buttonToolbar.setExpandRatio(showDeleteEntirelyPopupViewButton, 1.0f);
			buttonToolbar.setComponentAlignment(showDeleteEntirelyPopupViewButton, Alignment.BOTTOM_CENTER);
		}
		
		vl.addComponent(buttonToolbar);
		vl.setExpandRatio(buttonToolbar, 0.0f);
		vl.setComponentAlignment(buttonToolbar, Alignment.BOTTOM_LEFT);
		return popupView;
	}
	
	private Button createUpdateChangeSuggestionStateButton(ChangeSuggestion cs, PopupView popupView, AbstractSelect selectionBox) {
		Button b = new Button();
		b.setIcon(FontAwesome.LIST);
		b.setDescription("Muokkaa tilaa");
		b.setStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 6353327872513838654L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectionBox.select(ChangeSuggestion.changeSuggestionStateEnumToUIText(cs.getState()));
				popupView.setPopupVisible(true);
			}

		});
		
		return b;
	}
	
	private AbstractSelect createChangeSuggestionStateListBox() {
		ListSelect box = new ListSelect();
		box.setStyleName(ValoTheme.COMBOBOX_TINY);
		box.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION_STATE_COMBO_BOX);
		
		List<String> options = new ArrayList<>();
		for(ChangeSuggestionState state : ChangeSuggestionState.values()) {
			options.add(ChangeSuggestion.changeSuggestionStateEnumToUIText(state));
		}
		
		box.addItems(options);
		box.setRows(options.size());
		box.setNullSelectionAllowed(false);
		return box;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// The ChangeSuggestion object's view ends here.
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// New Change Suggestions and their editing - Dialogues
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Open a new ChangeSuggestion dialog - doesn't target a specific map or chapter (base)
	 */
	public void openCreateNewChangeSuggestionDialog() {
		initializeAndOpenChangeSuggestionDialog(null);
	}
	
	public void openCreateNewChangeSuggestionDialog(Base b) {
		MainCreateNewChangeSuggestionWindow subWindow = initializeAndOpenChangeSuggestionDialog(null);
		subWindow.setTargetBase(b);
	}
	
	/**
	 * Open a new ChangeSuggestion dialog - for editing an existing ChangeSuggestion
	 * @param changeSuggestion
	 */
	private void openEditChangeSuggestionDialog(ChangeSuggestion changeSuggestion) {
		initializeAndOpenChangeSuggestionDialog(changeSuggestion);
	}
	
	public MainCreateNewChangeSuggestionWindow initializeAndOpenChangeSuggestionDialog(ChangeSuggestion possibleChangeSuggestion) {
		if(possibleChangeSuggestion != null) {
			subWindow.editExistingMode(possibleChangeSuggestion);
		} else {
			subWindow.createNewMode();
		}
        UI.getCurrent().addWindow(subWindow);
        return subWindow;
	}
}
