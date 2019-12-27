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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.splitpanel.AbstractSplitPanelRpc;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.ResizeEvent;
import com.vaadin.ui.Window.ResizeListener;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Updates.TreeItem;
import fi.semantum.strategia.action.ActionCreatePrincipalMeter;
import fi.semantum.strategia.action.Actions;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategia.contrib.MapVis;
import fi.semantum.strategia.custom.PDFButton;
import fi.semantum.strategia.widget.D3;
import fi.semantum.strategia.widget.D3.D3Listener;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Datatype;
import fi.semantum.strategy.db.EnumerationDatatype;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.MeterDescription;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.Relation;
import fi.semantum.strategy.db.ResponsibilityInstance;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UIState;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDBComments;

public class MainMapEditorUI {
	
	private Main main;
	
	public ComboBox states;
	public Button saveState;
	
	public PDFButton pdf;
	
	//Mode label and mode button belong together
	public Label modeLabel;
	public Button mode;
	
	//Duplicate buttons and mapDialog belong together
	public Button duplicate;
	public Button duplicate2;
	public Window mapDialog = null;
	
	public Button meterMode;
	
	public Button propertyExcelButton;
	
	public VerticalLayout root;
	public AbsoluteLayout strategyMapJSView;
	public HorizontalLayout mapToolbar;

	public HSP panelHorizontalSplitLayout;
	public Panel panel;
	public VerticalLayout js2Container;

	//public HorizontalSplitPanel hs;

	TreeTable tree;
	
	final D3 js = new D3();
	final D3 js2 = new D3();
	final D3 js3 = new D3();
	
	ValueChangeListener statesListener;
	
	public MainMapEditorUI(Main main) {
		this.main = main;
		statesListener = new ValueChangeListener() {

			private static final long serialVersionUID = 7604300573989401911L;

			public void valueChange(ValueChangeEvent event) {

				Object value = states.getValue();
				if (value == null)
					return;

				String selected = value.toString();
				for (UIState state : main.account.uiStates) {
					if (state.name.equals(selected)) {
						main.setFragment(state.duplicateI(main.stateCounter++), true, true);
						return;
					}
				}

			}

		};
		
		init();
	}
	
	private void init() {
		initStatesComboBox();
		initSaveStateButton();
		initMeterModeButton();
		initPrintMapAsPDFButton();
		initModeLabel();
		initModeButton();
		initDuplicateButtons();
		initPropertyExcelButton();
		initMapToolbar();
		initRoot();
		
	}
	
	public class HSP extends HorizontalSplitPanel {

		private static final long serialVersionUID = -2878496061027332477L;

		public HSP() {
			
			registerRpc(new AbstractSplitPanelRpc() {

				private static final long serialVersionUID = 8388408508522418565L;

				@Override
		        public void setSplitterPosition(float position) {
					int pixelPosition = (int)(main.nonCommentToolWidth * 0.01 * position);
					main.treeSplitPixelPosition = pixelPosition;
		        	
		        	main.updateAvailableWindowWidthCache();
		        	main.updateMainLayoutWidth();
		        	main.commentLayout.refreshSize();
		        	
		        	Updates.update(main, false);
		        }

				@Override
				public void splitterClick(MouseEventDetails mouseDetails) {
				}
		        
			});
			
		}
		
		public void forceSplitPercentageRedraw() {
			float full = main.nonCommentToolWidth;
			float p = (float)100.0 * ((float)main.treeSplitPixelPosition / full);
			this.setSplitPosition(p, Unit.PERCENTAGE);
			
			int w2 = main.nonCommentToolWidth;
			if(w2 < 850) {
				main.mapEditorUI.states.setWidth("125px");
			} else {
				main.mapEditorUI.states.setWidth("250px");
			}
			
			Updates.updateJS(main, false);
		}

	}
	
	public void refreshSplitPanelSize() {
		this.panelHorizontalSplitLayout.forceSplitPercentageRedraw();
	}
	
	/**
	 * Should be called only after initRoot has been called
	 */
	public void initJS() {
		panel = new Panel();
		panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.setSizeFull();
		panel.setId("mapContainer1");

		tree = Utils.emptyMapNavigationTreeTable();
		tree.addStyleName(CustomCSSStyleNames.MAP_TREE_TABLE_BROWSER);
		tree.addContainerProperty("", TreeItem.class, null);
		tree.setWidth("100%");
		tree.setColumnExpandRatio("", 1.0f);
		tree.setSelectable(false);
		tree.setReadOnly(true);
		
		tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 3490657603974713652L;
			
			@Override
			public void itemClick(ItemClickEvent event) {
				
				Database database = main.getDatabase();
				TreeItem item = (TreeItem)event.getItemId();
				if(item != null && event.isDoubleClick()) {
					MapBase b = database.find(item.uuid);
					if(b != null) {
						main.tryToNavigate(b);
					}
					
				}
				
			}
			
		});
		
		tree.setCellStyleGenerator(new CellStyleGenerator() {
			
			private static final long serialVersionUID = -5060393331217336564L;

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				Database database = main.getDatabase();
				MapBase b = database.find(((TreeItem)itemId).uuid);
				if(b != null) {
					if(b.equals(main.getUIState().getCurrentMap()))
						return "current-map";
					else if(main.tryToGetNavigationAction(b) != null)
						return "can-navigate";
				}
				return "";
			}
			
		});
		
		tree.addExpandListener(new ExpandListener() {
			
			private static final long serialVersionUID = -1886285917703592023L;

			@Override
			public void nodeExpand(ExpandEvent event) {
				TreeItem item = (TreeItem)event.getItemId();
				if(main.getUIState().treeExpansion == null)
					main.getUIState().treeExpansion = new HashSet<String>();
				main.getUIState().treeExpansion.add(item.uuid);
			}
			
		});
		
		tree.addCollapseListener(new CollapseListener() {
			
			private static final long serialVersionUID = -5569652975400056618L;

			@Override
			public void nodeCollapse(CollapseEvent event) {
				TreeItem item = (TreeItem)event.getItemId();
				if(main.getUIState().treeExpansion == null)
					main.getUIState().treeExpansion = new HashSet<String>();
				main.getUIState().treeExpansion.remove(item.uuid);
			}
			
		});

		
		panelHorizontalSplitLayout = new HSP();
		
		panelHorizontalSplitLayout.addComponent(tree);
		
		panelHorizontalSplitLayout.addComponent(js);
		panelHorizontalSplitLayout.setHeight("100%");
		panelHorizontalSplitLayout.setWidth("100%");

		panelHorizontalSplitLayout.setSplitPosition(10, Unit.PERCENTAGE);
		
		js2Container = new VerticalLayout();
		js2Container.setHeight("100%");
		js2Container.addComponent(new Label("<hr />", ContentMode.HTML));
		js2Container.addComponent(js2);

		panel.setContent(panelHorizontalSplitLayout);
		this.strategyMapJSView.addComponent(panel);
	}
	
	private void initRoot() {
		
		js.addListener(new MapListener(this.main, false));
		js2.addListener(new MapListener(this.main, true));
		
		root = new VerticalLayout();
		root.setSizeFull();
		root.setSpacing(true);
		
		root.addComponent(mapToolbar);
		root.setExpandRatio(mapToolbar, 0.0f);
		root.setComponentAlignment(mapToolbar, Alignment.TOP_RIGHT);
		
		strategyMapJSView = new AbsoluteLayout();
		root.addComponent(strategyMapJSView);
		root.setExpandRatio(strategyMapJSView, 1.0f);
		root.setComponentAlignment(strategyMapJSView, Alignment.TOP_LEFT);
		
	}
	
	private void initMapToolbar() {
		mapToolbar = new HorizontalLayout();
		mapToolbar.setSpacing(true);
		mapToolbar.setMargin(new MarginInfo(true, false, false, false));
		
		mapToolbar.addComponent(propertyExcelButton);
		mapToolbar.setComponentAlignment(propertyExcelButton, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(propertyExcelButton, 0.0f);
		
		mapToolbar.addComponent(states);
		mapToolbar.setComponentAlignment(states, Alignment.MIDDLE_LEFT);
		mapToolbar.setExpandRatio(states, 0.0f);

		mapToolbar.addComponent(saveState);
		mapToolbar.setComponentAlignment(saveState, Alignment.MIDDLE_LEFT);
		mapToolbar.setExpandRatio(saveState, 0.0f);

		mapToolbar.addComponent(pdf);
		mapToolbar.setComponentAlignment(pdf, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(pdf, 0.0f);

		mapToolbar.addComponent(modeLabel);
		mapToolbar.setComponentAlignment(modeLabel, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(modeLabel, 0.0f);

		mapToolbar.addComponent(mode);
		mapToolbar.setComponentAlignment(mode, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(mode, 0.0f);

		mapToolbar.addComponent(meterMode);
		mapToolbar.setComponentAlignment(meterMode, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(meterMode, 0.0f);

		mapToolbar.addComponent(duplicate);
		mapToolbar.setComponentAlignment(duplicate, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(duplicate, 0.0f);

		mapToolbar.addComponent(duplicate2);
		mapToolbar.setComponentAlignment(duplicate2, Alignment.MIDDLE_RIGHT);
		mapToolbar.setExpandRatio(duplicate2, 0.0f);
	}
	
	private void initStatesComboBox(){
		states = new ComboBox();
		states.addStyleName(ValoTheme.COMBOBOX_TINY);
		states.setInvalidAllowed(false);
		states.setNullSelectionAllowed(false);

		states.addValueChangeListener(statesListener);
	}
	
	private void initSaveStateButton(){
		saveState = new Button();
		saveState.setEnabled(false);
		saveState.setDescription(Terminology.SAVE_CURRENT_VIEW_LABEL);
		saveState.setIcon(FontAwesome.BOOKMARK);
		saveState.addStyleName(ValoTheme.BUTTON_TINY);
		saveState.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 3951104780741054290L;

			@Override
			public void buttonClick(ClickEvent event) {
				Utils.saveCurrentState(main);
			}

		});
	}
	
	private void initMeterModeButton() {
		meterMode = new Button();
		meterMode.setDescription(Terminology.CHANGE_TO_ACTUALIZATION_METERS);
		meterMode.setCaption(Terminology.FORECAST);
		meterMode.addStyleName(ValoTheme.BUTTON_TINY);

		meterMode.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -236479771847125234L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				if(Terminology.CHANGE_TO_ACTUALIZATION_METERS.equals(meterMode.getDescription())) {
					
					meterMode.setDescription(Terminology.CHANGE_TO_PREDICTION_METERS);
					meterMode.setCaption(Terminology.ACTUALIZATION);

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					s.setActualMeters();
					main.setFragment(s, true);
					
				} else {

					meterMode.setDescription(Terminology.CHANGE_TO_ACTUALIZATION_METERS);
					meterMode.setCaption(Terminology.FORECAST);

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					s.setForecastMeters();
					main.setFragment(s, true);
					
				}

			}

		});
	}
	
	private void initPrintMapAsPDFButton() {
		pdf = new PDFButton();
		pdf.setDescription(Terminology.EXPORT_PDF_BUTTON_LABEL);
		pdf.setIcon(FontAwesome.PRINT);
		pdf.addStyleName(ValoTheme.BUTTON_TINY);
	
		pdf.addClickListener(new ClickListener() {
	
			/**
			 * 
			 */
			private static final long serialVersionUID = -4647793733659133104L;

			@Override
			public void buttonClick(ClickEvent event) {
				Utils.print(main);
			}
		});
	}

	private void initModeLabel() {
		modeLabel = new Label(Terminology.VIEW_MODE_LABEL);
		modeLabel.setWidth("95px");
		modeLabel.addStyleName("viewMode");
	}
	
	private void initModeButton() {
		mode = new Button();
		mode.setDescription(Terminology.MOVE_TO_EDIT_MODE_LABEL);
		mode.setIcon(FontAwesome.EYE);
		mode.addStyleName(ValoTheme.BUTTON_TINY);

		mode.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4758767308232235387L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				if(Terminology.MOVE_TO_EDIT_MODE_LABEL.equals(mode.getDescription())) {
					
					mode.setDescription("Siirry katselutilaan");
					mode.setIcon(FontAwesome.PENCIL);
					modeLabel.setValue(Terminology.EDIT_MODE_LABEL);
					modeLabel.removeStyleName("viewMode");
					modeLabel.addStyleName("editMode");

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					s.input = true;
					main.setFragment(s, true);
				} else {
					
					mode.setDescription(Terminology.MOVE_TO_EDIT_MODE_LABEL);
					mode.setIcon(FontAwesome.EYE);
					modeLabel.setValue(Terminology.VIEW_MODE_LABEL);
					modeLabel.removeStyleName("editMode");
					modeLabel.addStyleName("viewMode");

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					s.input = false;
					main.setFragment(s, true);
					
				}

			}

		});
	}
	
	private void initDuplicateButtons() {
		duplicate = new Button("Avaa ikkunassa");
		duplicate.setDescription("Avaa kopio kartasta uudessa ikkunassa.");
		duplicate2 = new Button("Avaa alas");
		duplicate2.setDescription("Avaa kopio kartasta ala-ikkunaan.");
		
		duplicate.setWidthUndefined();
		duplicate.addStyleName(ValoTheme.BUTTON_TINY);
		duplicate.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 7867372063880336836L;

			@Override
			public void buttonClick(ClickEvent event) {

				MapVis model = js2.getModel();
				if (model == null) {

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					s.referenceMap = s.currentMap;

					mapDialog = new Window(s.referenceMap.getText(main.getDatabase()), new VerticalLayout());
					mapDialog.setWidth(main.dialogWidth());
					mapDialog.setHeight(main.dialogHeight());
					mapDialog.setResizable(true);
					mapDialog.setContent(js2Container);
					mapDialog.setVisible(true);
					mapDialog.setResizeLazy(false);
					mapDialog.addCloseListener(new CloseListener() {

						private static final long serialVersionUID = -4752688162905939407L;

						@Override
						public void windowClose(CloseEvent e) {

							duplicate.setCaption("Avaa ikkunassa");
							duplicate2.setVisible(true);

							UIState s = main.uiState.duplicateI(main.stateCounter++);
							mapDialog.close();
							mapDialog = null;
							s.referenceMap = null;
							main.setFragment(s, true);

						}

					});
					mapDialog.addResizeListener(new ResizeListener() {

						private static final long serialVersionUID = 8368824147403936588L;

						@Override
						public void windowResized(ResizeEvent e) {
							Updates.updateJS(main, false);
						}

					});

					main.setFragment(s, true);

					main.addWindow(mapDialog);

					duplicate.setCaption("Sulje referenssi");
					duplicate2.setVisible(false);

				} else {

					UIState s = main.uiState.duplicateI(main.stateCounter++);
					if (mapDialog != null) {
						mapDialog.close();
						mapDialog = null;
					}

					panelHorizontalSplitLayout.removeComponent(js2Container);

					s.referenceMap = null;
					main.setFragment(s, true);

					duplicate.setCaption("Avaa ikkunassa");
					duplicate2.setVisible(true);

				}

			}

		});

		duplicate2.setWidthUndefined();
		duplicate2.addStyleName(ValoTheme.BUTTON_TINY);
		duplicate2.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 964165018890512478L;

			@Override
			public void buttonClick(ClickEvent event) {

				MapVis model = js2.getModel();
				assert (model == null);

				UIState s = main.uiState.duplicateI(main.stateCounter++);
				s.referenceMap = s.currentMap;
				main.setFragment(s, true);

				panelHorizontalSplitLayout.addComponent(js2Container);

				duplicate.setCaption("Sulje referenssi");
				duplicate2.setVisible(false);

			}

		});
	}
	
	private void initPropertyExcelButton() {
		propertyExcelButton = new Button();
		propertyExcelButton.setDescription(Terminology.SAVE_DATA_AS_EXCEL_LABEL);
		propertyExcelButton.setIcon(FontAwesome.PRINT);
		propertyExcelButton.addStyleName(ValoTheme.BUTTON_TINY);
	}
	
	public void clearAll() {
		Utils.clearAndNullify(states);
		Utils.clearAndNullify(saveState);
		
		Utils.clearAndNullify(pdf);
		
		Utils.clearAndNullify(modeLabel);
		Utils.clearAndNullify(mode);
		
		Utils.clearAndNullify(duplicate);
		Utils.clearAndNullify(duplicate2);
		Utils.clearAndNullify(mapDialog);
		
		Utils.clearAndNullify(meterMode);
		
		Utils.clearAndNullify(propertyExcelButton);
		
		Utils.clearAndNullify(root);
		Utils.clearAndNullify(strategyMapJSView);
		Utils.clearAndNullify(mapToolbar);

		Utils.clearAndNullify(panelHorizontalSplitLayout);
		Utils.clearAndNullify(panel);
		Utils.clearAndNullify(js2Container);

		Utils.clearAndNullify(js);
		Utils.clearAndNullify(js2);
		Utils.clearAndNullify(js3);
	}
	
	/**
	 * 
	 * @author Antti Villberg
	 *
	 */
	static class MapListener implements D3Listener {

		final private Main main;
		final private boolean isReference;

		public MapListener(Main main, boolean isReference) {
			this.main = main;
			this.isReference = isReference;
		}

		private StrategyMap getMap() {
			if (isReference) {
				return main.getUIState().referenceMap;
			} else {
				return main.getUIState().currentMap;
			}
		}

		private OuterBox getGoal(int index) {
			StrategyMap map = getMap();
			if (index == -1) return null;
			if (index == map.outerBoxes.length)
				return map.voimavarat;
			else
				return map.outerBoxes[index];
		}
		
		private InnerBox getInnerBox(int outerBox, int innerBox) {
			return getGoal(outerBox).innerboxes[innerBox];
		}

		@Override
		public void select(double x, double y) {
			Actions.selectAction(main, x, y, null, getMap());
		}

		@Override
		public void navigate(String kartta) {
			Database database = main.getDatabase();
			StrategyMap k = database.find(kartta);
			if (k != null) {
				UIState s = main.getUIState().duplicateI(main.stateCounter++);
				s.setCurrentMap(k);
				main.setFragment(s, true);
			}
			if(main.commentLayout != null) {
				main.commentLayout.possiblyPopulateTabsView();
			}
		}

		@Override
		public void columns(int outerBox, int innerBox, boolean increase) {
			if(innerBox == -1) {
				
				if(outerBox == -1) {
					
					StrategyMap map = getMap();
					if(increase)
						map.columns++;
					else 
						if(map.columns > 1)
							map.columns--;
					
				} else {

					OuterBox goal = getGoal(outerBox);
					if(increase)
						goal.columns++;
					else 
						if(goal.columns > 1)
							goal.columns--;
					
				}
				
			} else {
				InnerBox inner = getInnerBox(outerBox, innerBox);
				if(increase)
					inner.columns++;
				else 
					if(inner.columns > 1)
						inner.columns--;
			}
			
			try {
				Updates.update(main, true);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void drill(int outerBox) {
			OuterBox goal = getGoal(outerBox);
			try {
				StrategyMap k = goal.getPossibleImplementationMap(main.getDatabase());
				if (k != null) {
					UIState s = main.getUIState().duplicateI(main.stateCounter++);
					s.currentMap = k;
					s.customVis = null;
					main.setFragment(s, true);
				}
				if(main.commentLayout != null) {
					main.commentLayout.possiblyPopulateTabsView();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void navi(double x, double y, int outerBox) {
			Actions.selectAction(main, x, y, getMap(), getGoal(outerBox));
		}

		@Override
		public void navi2(double x, double y, int outerBox, int innerBox) {
			Actions.selectAction(main, x, y, getGoal(outerBox), getGoal(outerBox).innerboxes[innerBox]);
		}
		
		@Override
		public void navi3(double x, double y, int outerBox, int innerBox, int innerBoxExtra) {
			InnerBox clickedInnerbox = getGoal(outerBox).innerboxes[innerBox];

			Collection<MapBase> extra = UtilsDB.getDirectImplementors(main.getDatabase(), clickedInnerbox, main.getUIState().getTime());
			if(extra.size() == 1) {
				MapBase b = extra.iterator().next();
				if(b instanceof OuterBox) {
					OuterBox container = (OuterBox)b;
					InnerBox base = container.innerboxes[innerBoxExtra];
					if(base != null) {
						Actions.selectAction(main, x, y, container, base);
					} 
				}
			}
		}

		@Override
		public void navi4(double x, double y, String uuid) {
			MapBase b = main.getDatabase().find(uuid);
			if(b == null) return;
			MapBase container = main.getDatabase().getDefaultParent(b);
			Actions.selectAction(main, x, y, container, b);
		}

		@Override
		public void editHeader() {
			Utils.editTextAndId(main, Terminology.EDIT_STRATEGY_MAP_NAME_LABEL, getMap());
		}

		@Override
		public void editVision() {

			StrategyMap map = getMap();

			final Window subwindow = new Window(Terminology.EDIT_VISION_LABEL, new VerticalLayout());
			subwindow.setModal(true);
			subwindow.setWidth("400px");
			subwindow.setHeight("500px");
			subwindow.setResizable(false);

			VerticalLayout winLayout = (VerticalLayout) subwindow.getContent();
			winLayout.setMargin(true);
			winLayout.setSpacing(true);

			final TextArea tf = new TextArea();
			tf.setValue(map.vision);
			tf.setWidth("360px");
			tf.setHeight("390px");
			winLayout.addComponent(tf);

			Button save = new Button(Terminology.SAVE, new Button.ClickListener() {

				private static final long serialVersionUID = -4001921326477368915L;

				public void buttonClick(ClickEvent event) {
					String value = tf.getValue();
					main.removeWindow(subwindow);
					getMap().vision = value;
					Updates.updateJS(main, true);
				}
			});

			Button discard = new Button(Terminology.CANCEL_CHANGES_LABEL, new Button.ClickListener() {

				private static final long serialVersionUID = -784522457615993823L;

				public void buttonClick(ClickEvent event) {
					Updates.updateJS(main, true);
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

			tf.setCursorPosition(tf.getValue().length());

		}

		private void editMeter(Base b, Meter m) {

			Database database = main.getDatabase();

			boolean canWrite = main.canWrite(getMap());

			Indicator indicator = m.getPossibleIndicator(database);
			if (canWrite && indicator != null && main.getUIState().input) {

				Datatype dt = indicator.getDatatype(database);
				if (dt instanceof EnumerationDatatype) {
					UtilsMeters.setUserMeter(main, b, m);
					return;
				}

			}

			String id = m.getCaption(database);
			String exp = UtilsMeters.describe(m, database, main.getUIState().forecastMeters);
			Indicator i = m.getPossibleIndicator(database);
			if (i != null) {
				String shortComment = i.getValueShortComment();
				if (!shortComment.isEmpty())
					exp += ", " + shortComment;
			}

			String content = "<div style=\"width: 800px; border: 2px solid; padding: 5px\">";
			/*
			 * content +=
			 * "<div style=\"text-align: center; white-space:normal; font-size: 22px; padding: 5px\">"
			 * + id + "</div>";
			 */
			content += "<div style=\"text-align: center; white-space:normal; font-size: 24px; padding: 15px\">" + exp
					+ "</div>";
			content += "</div>";

			Notification n = new Notification(content, Notification.Type.HUMANIZED_MESSAGE);
			n.setHtmlContentAllowed(true);
			n.show(Page.getCurrent());

		}

		@Override
		public void selectMeter(int outerBox, int innerBox, int index, String link) {

			MapBase b = null;
			if (innerBox == -1) {
				b = getGoal(outerBox);
			} else {
				b = getInnerBox(outerBox, innerBox);
			}

			if (b == null)
				return;

			Database database = main.getDatabase();

			if (main.getUIState().useImplementationMeters) {

				boolean forecast = main.getUIState().forecastMeters;

				Base linked = database.find(link);
				if (linked != null) {
					UIState s = main.getUIState().duplicateI(main.stateCounter++);
					s.currentMap = (StrategyMap) linked;
					s.customVis = null;
					main.setFragment(s, true);
					if(main.commentLayout != null) {
						main.commentLayout.possiblyPopulateTabsView();
					}
					return;
				}

				boolean canWrite = main.canWrite(getMap());
				if (canWrite) {
					if (b instanceof InnerBox) {
						InnerBox p = (InnerBox) b;
						List<Meter> descs = UtilsMeters.getImplementationMeters(main.getDatabase(), main.getUIState(), p, forecast);
						if (descs.isEmpty()) {
							Meter m = UtilsMeters.getPrincipalMeter(main.getDatabase(), main.getUIState(), p, "", forecast);
							if (m != null)
								descs = Collections.singletonList(m);
						}
						if (index < descs.size()) {
							Meter m = descs.get(index);
							if (!m.isTransient()) {
								editMeter(b, m);
							}
						}
					}
				}

			} else {

				List<MeterDescription> descs = UtilsMeters.makeMeterDescriptions(main.getDatabase(), main.getUIState(), b, true);
				Meter m = descs.get(index).meter;
				editMeter(b, m);

			}

		}

		@Override
		public void editOuterBox(final int outerBox) {

			Database database = main.getDatabase();

			OuterBox t = getGoal(outerBox);
			if (t.isCopy(database))
				return;

			Utils.editTextAndId(main, Terminology.EDIT_GOAL_DEFINITION_LABEL, t);

		}

		@Override
		public void editInnerBox(final int outerBox, final int innerBox) {

			Database database = main.getDatabase();

			InnerBox p = getGoal(outerBox).innerboxes[innerBox];
			if (p.isCopy(database))
				return;

			Utils.editTextAndId(main, Terminology.EDIT_FOCUS_DEFINITION_LABEL, p);

		}

		@Override
		public void removeOuterBox(int index) {
			getMap().removeOuterBox(index);
			Updates.updateJS(main, true);
		}

		
		private static void populateResponsibilityTable(Database database, InnerBox p, PopupView popup, Map<String, String> rowFieldMap, Table table) {
			Map<String, String> resp = UtilsDB.getResponsibilityMap(database, p);
			if (resp.isEmpty()) {
				popup.setPopupVisible(false);
				return;
			}
			
			int tableIndex = 0;
			for(String field : resp.keySet()) {
				tableIndex++;
				rowFieldMap.put("" + tableIndex, field);
				
				String value = resp.get(field);
				
				table.addItem(new Object[] {
						field,
						value
					},
					tableIndex);
			}
			
			table.setPageLength(table.size());
		}
		
		@Override
		public void displayInfo(final int outerBox, final int innerBox) {
			Database database = main.getDatabase();

			InnerBox p = getGoal(outerBox).innerboxes[innerBox];

			Map<String, String> resp = UtilsDB.getResponsibilityMap(database, p);
			if (resp.isEmpty())
				return;

			Map<String, String> rowFieldMap = new TreeMap<>();
			
			VerticalLayout vl = new VerticalLayout();
			vl.addStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
			
			PopupView popup = new PopupView("", vl);
			popup.setSizeFull();
			vl.setWidth("900px");
			vl.setHeight("250px");
			
			final Table table = new Table();
			table.addStyleName(ValoTheme.TABLE_SMALL);
			table.addStyleName(ValoTheme.TABLE_SMALL);
			table.addStyleName(ValoTheme.TABLE_COMPACT);
			table.setSortEnabled(false);
			table.setReadOnly(!main.getUIState().input);
			
			table.addContainerProperty(Relation.RESPONSIBILITY_INSTANCE, String.class, null);
			table.addContainerProperty("Kentän tiedot", String.class, null);
			
			final Button deleteResponsibilityEntry = new Button("Poista valittu vastuu", new Button.ClickListener() {

				private static final long serialVersionUID = 5241336987482956456L;

				public void buttonClick(ClickEvent event) {
					if(main.getUIState().input && main.getAccountDefault().isAdmin(database)) {
						Object selection = table.getValue();
						if(selection != null) {
							ResponsibilityInstance instance = (Relation.find(database, Relation.RESPONSIBILITY_INSTANCE)).getPossibleRelationObject(database, p);
							if(instance == null) {
								System.err.println("Cannot delete the chosen Responsibility field - instance is null for InnerBox " + p.uuid);
							} else {
								String field = rowFieldMap.get(selection.toString());
								String value = instance.removeField(field);
								System.out.println("Removed Responsibility field " + field + " with value " + value + " from InnerBox " + p.getText(database));
								
								Updates.update(main, true);
								
								rowFieldMap.clear();
								table.removeAllItems();
								populateResponsibilityTable(database, p, popup, rowFieldMap, table);
							}
						}
					}
				}

			});
			
			table.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = -90939774992959971L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					Object selection = table.getValue();
					if(selection != null) {
						deleteResponsibilityEntry.setEnabled(true);
					} else {
						deleteResponsibilityEntry.setEnabled(false);
					}
				}

			});
			
			deleteResponsibilityEntry.setEnabled(false);
			deleteResponsibilityEntry.setIcon(FontAwesome.TRASH);
			deleteResponsibilityEntry.addStyleName(ValoTheme.BUTTON_TINY);
			deleteResponsibilityEntry.setVisible(main.getUIState().input);
			
			populateResponsibilityTable(database, p, popup, rowFieldMap, table);
			
			table.setWidth("100%");
			table.setSelectable(true);
			table.setMultiSelect(false);
			
			HorizontalLayout buttonToolbar = new HorizontalLayout();
			buttonToolbar.setSpacing(true);
			
			Button close = new Button();
			close.setCaption("Sulje");
			close.setStyleName(ValoTheme.BUTTON_TINY);
			close.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -3303924457956586548L;

				@Override
				public void buttonClick(ClickEvent event) {
					popup.setPopupVisible(false);
				}
				
			});
			
			buttonToolbar.addComponent(deleteResponsibilityEntry);
			buttonToolbar.setExpandRatio(deleteResponsibilityEntry, 0.0f);
			buttonToolbar.addComponent(close);
			buttonToolbar.setExpandRatio(close, 1.0f);
			
			vl.addComponent(table);
			vl.setExpandRatio(table, 0.0f);
			vl.addComponent(buttonToolbar);
			vl.setExpandRatio(buttonToolbar, 1.0f);
			vl.setComponentAlignment(buttonToolbar, Alignment.BOTTOM_CENTER);

			String cssPosition = "left:50%;right:50%";
			main.rootAbsoluteLayout.addComponent(popup, cssPosition);
			
			popup.setVisible(true);
			popup.setHideOnMouseOut(false);
			popup.setPopupVisible(true);
			
			popup.addPopupVisibilityListener(new PopupVisibilityListener() {

				private static final long serialVersionUID = 6317015271973380337L;

				@Override
				public void popupVisibilityChange(PopupVisibilityEvent event) {
					if(! popup.isPopupVisible()) {
						main.rootAbsoluteLayout.removeComponent(popup);
					}
				}
				
			});
		}

		@Override
		public void displayMeter(final int outerBox, final int innerBox) {

			InnerBox p = getGoal(outerBox).innerboxes[innerBox];

			// The unique leaf box
			//MapBase leaf = main.hasLeaf(p);

			for (Meter m : UtilsMeters.getMetersActive(main.getDatabase(), main.getUIState(), p)) {
				if (m.isPrincipal) {
					UtilsMeters.setUserMeter(main, p, m);
					return;
				}
			}

			ActionCreatePrincipalMeter.perform(main, p);

			for (Meter m : UtilsMeters.getMetersActive(main.getDatabase(), main.getUIState(), p)) {
				if (m.isPrincipal) {
					UtilsMeters.setUserMeter(main, p, m);
					return;
				}
			}

		}
		
		/**
		 * This function is not connected to js yet. Instead, the context-menu actions is used to create new ChangeSuggestions.
		 */
		@Override
		public void createChangeSuggestion(final int outerBox, final int innerBox) {
			Base leaf = null;
			if(innerBox == -1) {
				OuterBox p = getGoal(outerBox);
				leaf = main.hasLeaf(p);
			} else {
				InnerBox p = getGoal(outerBox).innerboxes[innerBox];
				leaf = main.hasLeaf(p);
			}
			if(leaf != null) {
				if(main.commentLayout != null) {
					main.commentLayout.openCreateNewChangeSuggestionDialog(leaf);
				} else {
					System.err.println("CommentLayout is null - Cannot create new ChangeSuggestion!");
				}
			} else {
				System.err.println("Leaf was null for createChangeSuggestion");
			}
		}
		
		private Set<Base> findTargetChangeSuggestionsForLeaf(final int outerBox, final boolean openRecursive){
			Set<Base> leafs = new HashSet<>();
			Database db = main.getDatabase();
			OuterBox ob = getGoal(outerBox);
			if(ob != null) {
				Base target = MapBase.findTargetedMapBoxElement(db, ob);
				if(target != null) {
					leafs.add(target);
					if(openRecursive) {
						leafs.addAll(UtilsDBComments.findAllSubLeafBases(db, main.getUIState(), ob));
					}
				} else {
					System.err.println("Target was null in findTargetChangeSuggestionsForLeaf!");
				}
			}
			return leafs;
		}
		
		@Override
		public void openRelatedChangeSuggestions(final int outerBox, final boolean openRecursive) {
			Set<Base> leafs = findTargetChangeSuggestionsForLeaf(outerBox, openRecursive);
			
			if(!leafs.isEmpty()) {
				if(main.commentLayout != null) {
					main.commentLayout.openRelatedChangeSuggestions(leafs);
				} else {
					System.err.println("CommentLayout is null - Cannot filter openRelatedChangeSuggestions!");
				}
			} else {
				System.err.println("Leaf was empty for openRelatedChangeSuggestions");
			}
		}

	};

	
}
