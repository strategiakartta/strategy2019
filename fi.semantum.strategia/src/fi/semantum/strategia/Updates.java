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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main.TreeVisitor1;
import fi.semantum.strategia.configurations.WebContent;
import fi.semantum.strategia.contrib.ContribVisUtils;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.contrib.MapVisFactory;
import fi.semantum.strategia.custom.OnDemandFileDownloader;
import fi.semantum.strategia.custom.OnDemandFileDownloader.OnDemandStreamSource;
import fi.semantum.strategia.filter.FilterState;
import fi.semantum.strategia.filter.FilterState.ReportCell;
import fi.semantum.strategia.filter.GenericImplementationFilter;
import fi.semantum.strategia.filter.NodeFilter;
import fi.semantum.strategia.filter.QueryFilter;
import fi.semantum.strategia.filter.TulostavoiteToimenpideFilter;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategia.widget.StandardVisRegistry;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;


public class Updates {

	private static final String BASE_MAP = "map";
	private static final String BASE_MAP_2 = "map2";
	
	/**
	 * Update database if needSave = true
	 * @param main
	 * @param needSave
	 */
	public static void update(Main main, boolean needSave) {
		update(main, false, needSave);
	}

	public static void update(Main main, boolean setPositions, boolean needSave) {

		if(!main.getUIState().startPageVisible) {
			updateStates(main);
			updateFilter(main);
		
			if(main.getUIState().mapTabState == UIState.PROPERTIES)
				updateProperties(main);
			
			boolean needComments = main.getUIState().commentLayoutOpen;
			if(needComments) {
				updateCommentTool(main);
			}
			
			updateJS(main, setPositions, needSave);
			
			main.setTabState(main.getUIState(), main.getUIState().mapTabState);
		} else {
			if (needSave)
				main.getDatabase().save();
		}
		
	}

	private static void updateStates(Main main) {
		ComboBox combo = main.mapEditorUI.states;
		combo.removeAllItems();
		combo.setInputPrompt("valitse tallennettu näkymä");

		for(UIState state : main.getAccountDefault().uiStates) {
			combo.addItem(state.name);
		}
	}

	private static void updateFilter(Main main) {
		List<NodeFilter> availableFilters = main.availableFilters;
		UIState uiState = main.getUIState();
		ComboBox filter = main.filter;
		
		main.availableFilters.clear();
		main.filter.removeAllItems();

		NodeFilter current = main.getCurrentFilter();
		if(current != null)
			availableFilters.add(current);
		
		List<String> cols = new ArrayList<String>();
		cols.add(Terminology.STRATEGIC_GOAL_OR_TARGET);
		cols.add(Terminology.FOCUS_POINT);
		cols.add("$" + Terminology.MAP);
		cols.add(Terminology.RESULT_GOAL_OR_TARGET);
		cols.add(Terminology.METER);
		cols.add(Terminology.GOAL_OR_TARGET_LEVEL);
		cols.add(Terminology.GOAL_OR_TARGET_LEVEL_DEFINITION);
		cols.add("$" + Terminology.METER);

		availableFilters.add(new GenericImplementationFilter(main, uiState.currentMapItem, cols));
//		availableFilters.add(new TulostavoiteToimenpideFilter(main, uiState.currentItem, uiState.currentPosition));

		if (uiState.currentMapItem instanceof StrategyMap) {

//			availableFilters.add(new MeterFilter(main,
//					uiState.currentItem, uiState.currentPosition));
//			availableFilters.add(new TulostavoiteFilter(main,
//					uiState.currentItem, uiState.currentPosition));

		} else if (uiState.currentMapItem instanceof OuterBox) {

//			availableFilters.add(new MeterFilter(main,
//					uiState.currentItem, uiState.currentPosition));
//			availableFilters.add(new TulostavoiteFilter(main,
//					uiState.currentItem, uiState.currentPosition));

		} else if (uiState.currentMapItem instanceof InnerBox) {

//			availableFilters.add(new MeterFilter(main,
//					uiState.currentItem, uiState.currentPosition));
//			availableFilters.add(new TulostavoiteFilter(main,
//					uiState.currentItem, uiState.currentPosition));

		}

//		availableFilters.add(new ChangeFilter(main));

//		if(account != null)
//			availableFilters.add(new AccountFilter(main, account));
		
		for (NodeFilter f : availableFilters) {
			filter.addItem(f.toString());
		}

		String filterName = uiState.currentFilterName;
		if(filterName == null)
			filterName = "";

		NodeFilter newFilter = null;
		
		main.filterListenerActive = false;
		for(NodeFilter f : availableFilters) {
			if(f.toString().equals(filterName))
				newFilter = f;
		}
		
		if(newFilter == null) {
			newFilter = new TulostavoiteToimenpideFilter(main, uiState.currentMap);
			filter.select(filter.getNullSelectionItemId());
			filter.setInputPrompt("valitse hakuehto");
		} else {
			filter.select(newFilter.toString());
		}

		main.filterListenerActive = true;

		main.setCurrentFilter(newFilter);
		
		ComboBox times = main.times;
		if(times != null) {
			Object value = times.getValue();
			if(value != null) {
				String v = value.toString();
				if(!v.equals(uiState.getTime())){
					//System.out.println("Times ComboBox not up-to-date - refresh!");
					try {
						main.times.removeValueChangeListener(main.timesListener);
						main.times.select(uiState.getTime());
					} finally {
						main.times.addValueChangeListener(main.timesListener);
					}
				}
			}
		}
	}
	
	private static void updateProperties(final Main main) {

		final MapBase base = main.getUIState().currentMapItem; 
		
		boolean canWrite = main.canWrite(base);
		
		main.propertyCells = new ArrayList<List<String>>();
				
		main.properties.removeAllComponents();
		main.properties.setId(BASE_MAP);

		if (base == null)
			return;

		Utils.updateProperties(main, base, canWrite);
		
		UtilsIndicators.updateIndicators(main, base, canWrite);
		
		UtilsMeters.updateMeters(main, canWrite);
		
		UtilsTags.updateRelatedTags(main, canWrite);
		
		UtilsTags.updateMonitoredTags(main, canWrite);
		
		final Button palaa = new Button("Palaa takaisin", new Button.ClickListener() {

			private static final long serialVersionUID = -6097534468072862126L;

			public void buttonClick(ClickEvent event) {
				main.applyFragment(main.backFragment, true);
			}

		});
		
		main.properties.addComponent(palaa);
		main.properties.setComponentAlignment(palaa, Alignment.BOTTOM_CENTER);

	}
	
	public static class TreeItem {
		
		public String label;
		public String uuid;
		
		public TreeItem(Database database, Base base) {
			this.label = base.getText(database);
			this.uuid = base.uuid;
		}
		
		@Override
		public String toString() {
			return label;
		}
		
	}
	
	public static void updateTreeForMap(Main main, TreeItem parentItem) {
		
		Database database = main.getDatabase();
		
		StrategyMap parent = database.find(parentItem.uuid);

		for(Linkki l : ContribVisUtils.getSubmapLinks(database, main.getAccountDefault(), main.getUIState(), parent, false)) {

			StrategyMap child = database.find(l.uuid);

			TreeItem officeItem = new TreeItem(database, child);

			main.mapEditorUI.tree.addItem(new Object[] {
					officeItem
			}, officeItem);
			main.mapEditorUI.tree.setParent(officeItem, parentItem);
			
			updateTreeForMap(main, officeItem);
			
		}

		UIState uistate = main.getUIState();
		if(uistate.treeExpansion.contains(parentItem.uuid))
			main.mapEditorUI.tree.setCollapsed(parentItem, false);
		
	}
	
	public static void updateTree(Main main) {
		
		Database database = main.getDatabase();
		UIState uistate = main.getUIState();
		if(uistate.treeExpansion == null)
			uistate.treeExpansion = new HashSet<String>();
		
		main.mapEditorUI.tree.removeAllItems();

		StrategyMap root = database.getRoot();
		
		// Create the tree nodes
		TreeItem rootItem = new TreeItem(database, root);
		main.mapEditorUI.tree.addItem(new Object[] {
				rootItem	
				}, rootItem);

		for(OuterBox ob : database.getRoot().outerBoxes) {
			
			TreeItem obItem = new TreeItem(database, ob);
			main.mapEditorUI.tree.addItem(new Object[] {
					obItem	
					}, obItem);
			main.mapEditorUI.tree.setParent(obItem, rootItem);

			for(InnerBox ib : ob.innerboxes) {

				TreeItem ibItem = new TreeItem(database, ib);

				main.mapEditorUI.tree.addItem(new Object[] {
						ibItem	
						}, ibItem);
				main.mapEditorUI.tree.setParent(ibItem, obItem);
				main.mapEditorUI.tree.setChildrenAllowed(ibItem, false);

			}
			
			if(uistate.treeExpansion.contains(obItem.uuid))
				main.mapEditorUI.tree.setCollapsed(obItem, false);

		}
		
		updateTreeForMap(main, rootItem);
		
//		for(Linkki l : ContribVisUtils.getSubmapLinks(database, main.getAccountDefault(), main.getUIState(), root, false)) {
//			
//			StrategyMap office = database.find(l.uuid);
//
//			TreeItem officeItem = new TreeItem(database, office);
//
//			main.mapEditorUI.tree.addItem(new Object[] {
//						officeItem
//					}, officeItem);
//			main.mapEditorUI.tree.setParent(officeItem, rootItem);
//			
////			for(OuterBox ob : office.outerBoxes) {
////				
////				try {
////					StrategyMap map = ob.getPossibleImplementationMap(database);
////				} catch (Exception e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////
////				TreeItem obItem = new TreeItem(database, ob);
////
////				main.mapEditorUI.tree.addItem(new Object[] {
////						obItem	
////						}, obItem);
////				main.mapEditorUI.tree.setParent(obItem, officeItem);
////
////				for(InnerBox ib : ob.innerboxes) {
////
////					TreeItem ibItem = new TreeItem(database, ib);
////
////					main.mapEditorUI.tree.addItem(new Object[] {
////							ibItem	
////							}, ibItem);
////					main.mapEditorUI.tree.setParent(ibItem, obItem);
////					main.mapEditorUI.tree.setChildrenAllowed(ibItem, false);
////
////				}
////
////				if(uistate.treeExpansion.contains(obItem.uuid))
////					main.mapEditorUI.tree.setCollapsed(obItem, false);
////
////			}
////
////			if(uistate.treeExpansion.contains(officeItem.uuid))
////				main.mapEditorUI.tree.setCollapsed(officeItem, false);
//			
//		}
//		
//		if(uistate.treeExpansion.contains(rootItem.uuid))
//			main.mapEditorUI.tree.setCollapsed(rootItem, false);
//
		
	}
	
	public static void updateMap(Main main) {
		if(main.getUIState().currentMap == null) return;
		MainMapEditorUI mapEditor = main.mapEditorUI;
		if(mapEditor == null) return;
		
		updateTree(main);
		
		int usedWidthPX = main.availableStrategyMapWidth;
		
		main.getUIState().currentMap.prepare(main.getDatabase());
		
		MapBase visSource = main.getUIState().currentMap;
		if(main.getUIState().customVis != null) visSource = main.getUIState().customVis; 
		
		MapVis vis = Utils.mapBaseToMapVis(
				main.getDatabase(),
				main.getAccountDefault(),
				main.getUIState(),
				visSource,
				MapVisFactory.PRIORITY_MAP_NAVIGATION);
		if(vis == null) {
			System.err.println("Failed to find MapVis for source " + visSource.uuid + " in updateMap!");
		} else {
			vis.elementId = BASE_MAP;
			vis.showNavigation = true;
			vis.fixRows();
		}
		
		mapEditor.js.setWidth(usedWidthPX + "px");
		mapEditor.js.update(vis, usedWidthPX, Utils.canWrite(main, main.getUIState().currentMap), main.getUIState());
		
		if(main.getUIState().referenceMap != null) {
			vis = StandardVisRegistry.getInstance().getBestMapVis(
					main.getDatabase(),
					main.getAccountDefault(),
					main.getUIState(),
					main.getUIState().referenceMap,
					MapVisFactory.PRIORITY_MAP_NAVIGATION);
			
			vis.elementId = BASE_MAP_2;
			vis.showNavigation = false;
			vis.fixRows();
			mapEditor.js2Container.setVisible(true);
			if(main.mapEditorUI.mapDialog != null) {
				mapEditor.js2.update(vis,
						(int)main.mapEditorUI.mapDialog.getWidth(),
						Utils.canWrite(main, main.getUIState().referenceMap),
						main.getUIState());
			} else {
				mapEditor.js2.update(vis, usedWidthPX, Utils.canWrite(main, main.getUIState().referenceMap), main.getUIState());
			}
		} else{
			mapEditor.js2.update(null, usedWidthPX, false, null);
			mapEditor.js2Container.setVisible(false);
		}

	}
	
	public static void updateBrowser(Main main, boolean setPositions) {

		final Database database = main.getDatabase();
		
		if(main.getUIState().requiredMapItems == null)
			main.getUIState().requiredMapItems = new HashSet<MapBase>();

		TreeVisitor1 treeVisitor1 = main.new TreeVisitor1();
		main.getCurrentFilter().reset(treeVisitor1.filterState);
		treeVisitor1.visit(database.getRoot());
		
		long s1 = System.nanoTime();
		
		// Filter reject
		List<List<MapBase>> preAccept = new ArrayList<List<MapBase>>();
		preAccept.addAll(treeVisitor1.filterState.getAcceptedNodes());
		
		long s2 = System.nanoTime();
		
		for(List<MapBase> path : preAccept)
			if(main.getCurrentFilter().reject(path))
				treeVisitor1.filterState.reject(path);

		// Remove internals
		preAccept = new ArrayList<List<MapBase>>(treeVisitor1.filterState.getAcceptedNodes());
		Set<MapBase> internals = new HashSet<MapBase>();
		for(List<MapBase> path : preAccept) {
			for(int i=0;i<path.size()-1;i++)
				internals.add(path.get(i));
		}
		for(List<MapBase> path : preAccept) {
			if(path.size() == 0) continue;
			Base last = path.get(path.size()-1);
			if(internals.contains(last)) 
				treeVisitor1.filterState.reject(path);
		}

		long s3 = System.nanoTime();
//		System.err.println("reject: " + (1e-6*(s3-s2) + "ms."));

		treeVisitor1.filterState.process(main.getCurrentFilter(), setPositions);

		ArrayList<BrowserNode> ns = new ArrayList<BrowserNode>();
		ns.addAll(treeVisitor1.filterState.nodes);
		
		BrowserNode[] nodes = ns.toArray(new BrowserNode[ns.size()]);
		Collections.sort(treeVisitor1.filterState.links, new Comparator<BrowserLink>() {

			@Override
			public int compare(BrowserLink o1, BrowserLink o2) {
				
				int result = Double.compare(o2.weight, o1.weight);
				if(result != 0) return result;
				
				result = Integer.compare(o2.source, o1.source);
				if(result != 0) return result;
				
				return Integer.compare(o2.target, o1.target);
				
			}
			
		});
		
		main.browser_.update(nodes, treeVisitor1.filterState.links
				.toArray(new BrowserLink[treeVisitor1.filterState.links.size()]),
				main.availableStrategyMapWidth + main.treeSplitPixelPosition, main.getWindowHeightPixels(), setPositions);

		updateQueryGrid(main, treeVisitor1.filterState);

	}

	public static void updateJS(Main main, boolean needSave) {
		updateJS(main, false, needSave);
	}

	public static void updateJS(Main main, boolean setPositions, boolean needSave) {

		boolean needMapAndDocuments = main.getUIState().mapTabState == UIState.MAP;
		boolean needBrowser = main.getUIState().mapTabState == UIState.BROWSER;
		
		boolean needChapters = main.getUIState().showSelectedDocuments;
		
		updateJS(main, needMapAndDocuments, needBrowser, needChapters, setPositions, needSave);
		
	}

	public static void updateJS(Main main, boolean needMapAndDocuments, boolean needBrowser, boolean chaptersVisible, boolean setPositions, boolean needSave) {
		
		if (needSave)
			main.getDatabase().save();

		if (needMapAndDocuments)
			updateMap(main);
			
		if (needBrowser || setPositions)
			updateBrowser(main, setPositions);
		
		if(chaptersVisible)
			updateChapters(main);

	}
	
	private static void updateCommentTool(Main main) {
		main.commentLayout.refreshServerSideOnly();
	}
	
	private static void updateChapters(Main main) {
		main.refreshAllChapterEditors();
	}
	
	private static void updateQueryGrid(final Main main, final FilterState state) {
		
		main.gridPanelLayout.removeAllComponents();
		main.gridPanelLayout.setMargin(false);
		
		final List<String> keys = state.reportColumns;
		if(keys.isEmpty()) {
			Label l = new Label(Terminology.QUERY_DID_NOT_PRODUCED_ANY_RESULTS);
			l.addStyleName(ValoTheme.LABEL_BOLD);
			l.addStyleName(ValoTheme.LABEL_HUGE);
			main.gridPanelLayout.addComponent(l);
			return;
		}
		
		final IndexedContainer container = new IndexedContainer();
		
		for(String key : keys) {
			container.addContainerProperty(key, String.class, "");
		}
		
		rows: for(Map<String,ReportCell> map : state.report) {
			Object item = container.addItem();
			for(String key : keys)
				if(map.get(key) == null)
					continue rows;
			
			for(Map.Entry<String,ReportCell> entry : map.entrySet()) {
				@SuppressWarnings("unchecked")
				com.vaadin.data.Property<String> p = container.getContainerProperty(item, entry.getKey());
				p.setValue(entry.getValue().get());
			}
			
		}
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		
		final TextField filter = new TextField();
		filter.addStyleName(ValoTheme.TEXTFIELD_TINY);
		filter.setInputPrompt("rajaa hakutuloksia - kirjoitetun sanan tulee löytyä rivin teksteistä");
		filter.setWidth("100%");
		
		final Button clipboard = new Button();
		clipboard.setStyleName(ValoTheme.BUTTON_SMALL);
		clipboard.setIcon(new ThemeResource(WebContent.PAGE_WHITE_EXCEL_PNG));
		
		hl.addComponent(filter);
		hl.setExpandRatio(filter, 1.0f);
		hl.setComponentAlignment(filter, Alignment.BOTTOM_CENTER);
		hl.addComponent(clipboard);
		hl.setComponentAlignment(clipboard, Alignment.BOTTOM_CENTER);
		hl.setExpandRatio(clipboard, 0.0f);
		
		main.gridPanelLayout.addComponent(hl);
		main.gridPanelLayout.setExpandRatio(hl, 0f);
		
		filter.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 3033918399018888150L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				container.removeAllContainerFilters();
				container.addContainerFilter(new QueryFilter(filter.getValue(), true, false));
			}
		});
		
		AbsoluteLayout abs = new AbsoluteLayout();
		abs.setSizeFull();
		
		final Grid queryGrid = new Grid(container);
		queryGrid.setSelectionMode(SelectionMode.NONE);
		queryGrid.setHeightMode(HeightMode.CSS);
		queryGrid.setHeight("100%");
		queryGrid.setWidth("100%");
		
		for(String key : keys) {
			Column col = queryGrid.getColumn(key);
			col.setExpandRatio(1);
		}
		
		abs.addComponent(queryGrid);
		
		OnDemandFileDownloader dl = new OnDemandFileDownloader(new OnDemandStreamSource() {
			
			private static final long serialVersionUID = 981769438054780731L;

			File f; 
			Date date = new Date();

			@Override
			public InputStream getStream() {
				
				String uuid = UUID.randomUUID().toString();
				File printing = main.getDatabase().getPrintingDirectory();
				f = new File(printing, uuid+".xlsx"); 
				
				Workbook w = new XSSFWorkbook();
				Sheet sheet = w.createSheet("Sheet1");
				Row header = sheet.createRow(0);
				for(int i=0;i<keys.size();i++) {
					Cell cell = header.createCell(i);
					cell.setCellValue(keys.get(i));
				}

				int row = 1;
				rows: for(Map<String,ReportCell> map : state.report) {
					for(String key : keys)
						if(map.get(key) == null)
							continue rows;
					
					Row r = sheet.createRow(row++);
					int column = 0;
					for(int i=0;i<keys.size();i++) {
						Cell cell = r.createCell(column++);
						ReportCell rc = map.get(keys.get(i));
						cell.setCellValue(rc.getLong());
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
				// TODO Auto-generated method stub
				
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
		dl.extend(clipboard);

		main.gridPanelLayout.addComponent(abs);
		main.gridPanelLayout.setExpandRatio(abs, 1f);
		
	}
	
	public static void updateTags(final Main main) {

		final Database database = main.getDatabase();

		main.tags.removeAllComponents();
		main.tags.setMargin(true);

		ArrayList<Tag> sorted = new ArrayList<Tag>(Tag.enumerate(database)); 
		Collections.sort(sorted, new Comparator<Tag>() {

			@Override
			public int compare(Tag arg0, Tag arg1) {
				return arg0.getId(database).compareTo(arg1.getId(database));
			}
			
		});
		
		for (final Tag t : sorted) {

			final HorizontalLayout hl = new HorizontalLayout();
			hl.setSpacing(true);
			Label l = new Label(t.getId(database));
			l.setSizeUndefined();
			l.addStyleName(ValoTheme.LABEL_HUGE);

			hl.addComponent(l);
			hl.setComponentAlignment(l, Alignment.BOTTOM_LEFT);

			final Image select = new Image("", new ThemeResource(WebContent.CURSOR_PNG));
			select.setHeight("24px");
			select.setWidth("24px");
			select.setDescription("Valitse");
			select.addClickListener(new MouseEvents.ClickListener() {

				private static final long serialVersionUID = 3734678948272593793L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					main.setCurrentItem(t, main.getUIState().currentMapPosition);
					Utils.loseFocus(select);
				}
				
			});
			hl.addComponent(select);
			hl.setComponentAlignment(select, Alignment.BOTTOM_LEFT);
			
			final Image edit = new Image("", new ThemeResource(WebContent.TABLE_EDIT_PNG));
			edit.setHeight("24px");
			edit.setWidth("24px");
			edit.setDescription("Muokkaa");
			edit.addClickListener(new MouseEvents.ClickListener() {

				private static final long serialVersionUID = -3792353723974454702L;

				@Override
				public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
					Utils.editTextAndId(main, "Muokkaa aihetunnistetta", t);
					updateTags(main);
				}
				
			});
			hl.addComponent(edit);
			hl.setComponentAlignment(edit, Alignment.BOTTOM_LEFT);

			main.tags.addComponent(hl);
			main.tags.setComponentAlignment(hl, Alignment.MIDDLE_CENTER);

			Label l2 = new Label(t.getText(database));
			l2.addStyleName(ValoTheme.LABEL_LIGHT);
			l2.setSizeUndefined();
			main.tags.addComponent(l2);
			main.tags.setComponentAlignment(l2, Alignment.MIDDLE_CENTER);

		}

	}
	
}
