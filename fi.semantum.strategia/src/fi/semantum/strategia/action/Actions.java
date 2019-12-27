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
package fi.semantum.strategia.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import elemental.events.KeyboardEvent.KeyCode;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.Utils;
import fi.semantum.strategia.UtilsComments;
import fi.semantum.strategia.Wiki;
import fi.semantum.strategia.widget.StandardVisRegistry;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.MapBox;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.StrategyMap.CharacterInfo;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class Actions {

	private static boolean isCurrentMap(Main main, StrategyMap map) {
		return map.equals(main.getUIState().currentMap);
	}
	
	public static void selectAction(final Main main, Double x, Double y, final MapBase container, final MapBase base) {

		Collection<Action> actions = listActions(main, x, y, container, base);
		
		Database database = main.getDatabase();

		StrategyMap baseMap = database.getMap(base);

		boolean generated = baseMap != null ? !baseMap.generators.isEmpty() : false;

		boolean forceDialogOpen = false;
		if(main.getUIState().commentLayoutOpen && !generated) {
			if(base instanceof StrategyMap || base instanceof MapBox || base instanceof Meter) {
				if(UtilsComments.accountHasCommentToolWriteRights(main)) {
					forceDialogOpen = true; //If comments should be displayed, always show the tooltip
				}
			}
		}

		if(actions.size() == 1 && !forceDialogOpen) {
			actions.iterator().next().run();
			return;
		}
		
		if(actions.size() == 0)
			return;
		
		VerticalLayout menu = new VerticalLayout();
		final PopupView openerButton = new PopupView("", menu);
		menu.setWidth("350px");
		
		openerButton.addPopupVisibilityListener(new PopupVisibilityListener() {
			
			private static final long serialVersionUID = -841548194737021404L;

			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(!event.isPopupVisible()) {
					main.background.schedule(new Runnable() {
						@Override
						public void run() {
							main.getUI().access(new Runnable() {
								@Override
								public void run() {
									main.menuActive = false;
								}
							});
						}
					}, 1L, TimeUnit.SECONDS);
				}
			}
			
		});
		
		String desc = base.getId(database);
		if(desc.isEmpty()) desc = base.getText(database);
		
		if(desc.length() > 100) desc = desc.substring(0, 99) + " ...";
		
		Label header = new Label(desc);
		header.addStyleName(ValoTheme.LABEL_LARGE);
		header.addStyleName("menuHeader");
		header.setWidth("350px");
		header.setHeightUndefined();
		menu.addComponent(header);
		menu.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

		Label header2 = new Label("valitse toiminto");
		header2.addStyleName(ValoTheme.LABEL_LIGHT);
		header2.addStyleName(ValoTheme.LABEL_SMALL);
		header2.setSizeUndefined();
		menu.addComponent(header2);
		menu.setComponentAlignment(header2, Alignment.MIDDLE_CENTER);

		Map<String, List<Action>> categories = new HashMap<String, List<Action>>();
		
		for(Action a : actions) {
			String category = a.category;
			if(category != null) {
				List<Action> acts = categories.get(category);
				if(acts == null) {
					acts = new ArrayList<Action>();
					categories.put(category, acts);
					
				}
				acts.add(a);
			} else {
				addAction(main, Collections.singleton(openerButton), menu, a.getCaption(), a);
			}
		}
		
		for(Map.Entry<String,List<Action>> entries : categories.entrySet()) {
			addCategory(main, openerButton, menu, x.intValue(), y.intValue(), entries.getKey(), entries.getValue());
		}
		
		openerButton.setHideOnMouseOut(false);
		openerButton.setPopupVisible(true);
		
		main.rootAbsoluteLayout.addComponent(openerButton, "left: " + x.intValue() + "px; top: " + y.intValue() + "px");
		main.menuActive = true;

	}

	public static Collection<Action> listActions(final Main main, Double x, Double y, final MapBase container, final MapBase base) {

		// Do not open a new menu if old one is already showing
		if(main.menuActive)
			return Collections.emptyList();
		
		boolean viewOnly = !main.getUIState().input;

		final Database database = main.getDatabase();

		List<Action> actions = new ArrayList<Action>();
		if(base instanceof Meter) return Collections.emptyList();
		
		StrategyMap baseMap = database.getMap(base);
		boolean generated = baseMap != null ? !baseMap.generators.isEmpty() : false;
		
		CharacterInfo ci = baseMap.getCharacterInfo(database);

		if(!generated) {
			
//			if(base instanceof StrategyMap) {
//				actions.add(new GenericSearch(main, base));
//				actions.add(new StrategianToteutus(main, base));
//				actions.add(new Valmiusasteet(main, base));
//				actions.add(new Tulostavoitteet(main, base));
//			}
			
			if(!viewOnly && Wiki.exists()) {
				actions.add(new ActionOpenWiki(main, base));
			}
			if(!viewOnly) actions.add(new ActionSwitchToProperties(main, base));
			
		}
		
//		if(main.getUIState().mapTabState != 0) {
//			actions.add(new ActionOpenInStrategyMap(main, base));
//		}

		if(base instanceof OuterBox) {
			
			OuterBox goal = (OuterBox)base;

			if(!generated) {
				if(!viewOnly && Wiki.exists()) {
					actions.add(new ActionOpenStrengthsWiki(main, base));
				}
			}
			
			final String desc = goal.getBoxDescription(database);

			if(Utils.canWrite(main, base) && !generated && !viewOnly) {

				StrategyMap map = database.getMap(base);
				if(map.linkGoalsToSubmaps(database)) {
					try {
						if(goal.getPossibleImplementationMap(database) == null) {
							actions.add(new ActionAddImplementationMap(main, goal));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				actions.add(new ActionAddInnerBox(desc, main, base));

				ActionMoveUp moveUp = new ActionMoveUp(main, (MapBox)base);
				if(moveUp.accept(goal)) actions.add(moveUp);
				ActionMoveDown moveDown = new ActionMoveDown(main, (MapBox)base);
				if(moveDown.accept(goal)) actions.add(moveDown);
				
				actions.add(new ActionRemoveOuterBox(desc, main, base));
				
				if(UtilsDB.getPossibleImplemented(database, base) == null)
					actions.add(new ActionSetToImplement(main, base));

			}
			
		}
		
		if(base instanceof InnerBox) {
			
			if(Utils.canWrite(main, base) && !generated && !viewOnly) {

				List<Meter> meters = base.getMeters(database);
				if(!ci.hasMeter) {
					if (meters.size() == 1 && meters.get(0).isPrincipal)
						actions.add(new ActionRemovePrincipalMeter(main, base));
				}
				
//				
//				if(!ci.hasMeter) {
//					if(meters.isEmpty())
//						actions.add(new ActionCreatePrincipalMeter(main, base));
//					else if (meters.size() == 1 && meters.get(0).isPrincipal)
//						actions.add(new ActionRemovePrincipalMeter(main, base));
//				}

				try {
					// We show this option only if there is no fixed connection between outer boxes and submaps
					if(baseMap != null && baseMap.getPossibleSubmapType(database) == null) {
						actions.add(new ActionDefineImplementors(main, base));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				actions.add(new ActionOpenResponsibilitiesDialog(main, base));

				final OuterBox t = database.getTavoite((InnerBox)base);
				int pos = t.findPainopiste((InnerBox)base);
				if(pos > 0) {
					actions.add(new ActionMoveUp(main, (InnerBox)base));
				}
				if(pos < t.innerboxes.length - 1) {
					actions.add(new ActionMoveDown(main, (InnerBox)base));
				}
				if(container != null) {
					actions.add(new ActionRemoveInnerBox(main, base, container));
				}
				
			}
			
		}

		if(main.getUIState().commentLayoutOpen && !generated) {
			if(base instanceof StrategyMap || base instanceof MapBox || base instanceof Meter) {
				if(UtilsComments.accountHasCommentToolWriteRights(main)) {
					actions.add(new ActionCreateNewChangeSuggestion(main, base));
				}
			}
		}
		
		if(base instanceof StrategyMap) {
			
			final StrategyMap map = (StrategyMap)base;

			if(isCurrentMap(main, map)) {

				if(!map.generators.isEmpty()) {
					actions.add(new ActionRefreshView(main, map));
				}
				
				if(Utils.canWrite(main, base) && !generated && !viewOnly && isCurrentMap(main, map)) {
					actions.add(new ActionInputMapElementsAsTable(main, base));
					if(!Utils.isImplementationMap(database, map)) {
						actions.add(new ActionAddOuterBox(main, map));
					}
					actions.add(new ActionAddLowerlevelMap(main, map));
					Base parent = map.getPossibleParent(database);
					if(parent == null) {
						actions.add(new ActionInsertRootMap(main, map));	
					}
					actions.add(new ActionRemoveMap(main, map));
					if(map.showVision) {
						actions.add(new ActionHideVision(main, map));
					} else {
						actions.add(new ActionShowVision(main, map));
					}
					if(main.getAccountDefault().isAdmin(database))
						actions.add(new ActionSelectMapType(main, map));
					actions.add(new ActionOpenResponsibilitiesDialog(main, base));
				}
				
//				if(main.getUIState().tabState == 1) {
//					actions.add(new RajaaAlle(main, map));
//				}

				if(main.getUIState().showTags) {
					actions.add(new ActionHideTags(main, base));
					if(base instanceof StrategyMap) {
						actions.add(new ActionChooseSubjectIdentifier(main, "Valitse aihetunnisteet", (StrategyMap)base));
					}
				} else {
					actions.add(new ActionChooseSubjectIdentifier(main, "Näytä aihetunnisteet", (StrategyMap)base));
				}
				
				if(main.getUIState().showDetailedMapMeters) {
					actions.add(new ActionHideDetailsMapMeters(main, base));
//					if(main.getUIState().useImplementationMeters) {
//						actions.add(new VaihdaKayttajanMittareihin(main, base));
//					} else {
//						actions.add(new VaihdaToteutusmittareihin(main, base));
//					}
				} else {
					actions.add(new ActionShowDetailedMapMeters(main, base));
				}

			} else {
				
				actions.add(new ActionOpenInStrategyMap(main, map));

			}

		}
		
		if(main.getUIState().mapTabState == 1) {
			actions.add(new ActionAddRequiredItem(main, base));
			actions.add(new ActionRemoveRequiredItem(main, base));
		}
		
		//if(actions.size() == 0) {
		boolean vis = StandardVisRegistry.getInstance().hasMapVis(database, main.getAccountDefault(), main.getUIState(), base);

		if(vis) {
			ActionOpenCustomMapVis openCustomMapVis = new ActionOpenCustomMapVis(main, base);
			if(!openCustomMapVis.getCaption().equals("Strategiakartta")) {
				actions.add(openCustomMapVis);
			}
		} 
//			else {
	//			return;
		//	}
		//}
			
			
//		if(actions.isEmpty()) {
//			StrategyMap map = ;
//			if(map != null) {
//				actions.add(new ActionOpenInStrategyMap(main, map));
//			}
//		}

			

		return actions;
			

	}
	
	private static void openSubmenu(Main main, Collection<PopupView> parents, int x, int y, List<Action> actions) {
		
		VerticalLayout content = new VerticalLayout();
		final PopupView subMenu = new PopupView("", content);
		
		content.setWidth("350px");
		
		for(Action a : actions) {
			ArrayList<PopupView> menus = new ArrayList<PopupView>(parents);
			menus.add(subMenu);
			addAction(main, menus, content, a.getCaption(), a);
		}
		
		subMenu.setHideOnMouseOut(false);
		subMenu.setPopupVisible(true);
		
		int middlePos = main.getUI().getPage().getBrowserWindowWidth() / 2;
		if(x > middlePos) {
			main.rootAbsoluteLayout.addComponent(subMenu, "left: " + (x-350) + "px; top: " + y + "px");
		} else {
			main.rootAbsoluteLayout.addComponent(subMenu, "left: " + (x+350) + "px; top: " + y + "px");
		}

	}
	
	private static void addAction(final Main main, final Collection<PopupView> menus, final VerticalLayout menu, String caption, final Action r) {

		Button b1 = new Button(caption);
		b1.addStyleName(ValoTheme.BUTTON_QUIET);
		b1.addStyleName(ValoTheme.BUTTON_TINY);
		b1.setWidth("100%");
		b1.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 7150528981216406326L;

			@Override
			public void buttonClick(ClickEvent event) {
				r.run();
				clearMenu(main, menus);
			}
			
		});
		if(Terminology.CANCEL.equals(caption)) {
			b1.setClickShortcut(KeyCode.ESC);
		}
		menu.addComponent(b1);

	}
	
	private static void clearMenu(Main main, Collection<PopupView> menus) {
		for(PopupView menu : menus)
			main.rootAbsoluteLayout.removeComponent(menu);
		main.menuActive = false;
	}
	
	private static void addCategory(final Main main, final PopupView openerButton, final VerticalLayout menu, final int x, final int y, String caption, final List<Action> actions) {

		Button b1 = new Button(caption);
		b1.addStyleName(ValoTheme.BUTTON_QUIET);
		b1.addStyleName(ValoTheme.BUTTON_TINY);
		b1.setWidth("100%");
		b1.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1044839440293207852L;

			@Override
			public void buttonClick(ClickEvent event) {
				openSubmenu(main, Collections.singleton(openerButton), x, y, actions);
			}
			
		});
		menu.addComponent(b1);

	}

	/*static ObjectType getFocusType(Database database, Strategiakartta map) {
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String)levelProperty.getPropertyValue(map));
		
		Property focusTypeProperty = Property.find(database, Property.FOCUS_TYPE);
		String focusTypeUUID = focusTypeProperty.getPropertyValue(level);

		if(focusTypeUUID == null) throw new IllegalStateException("No focus type for map " + map.getText(database) ); 
		
		return database.find(focusTypeUUID);

	}*/
	
}
