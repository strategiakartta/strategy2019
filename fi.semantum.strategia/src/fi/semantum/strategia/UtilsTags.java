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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.WebContent;
import fi.semantum.strategia.contrib.ContribVisUtils;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.Tag;
import fi.semantum.strategy.db.UIState;

/**
 * 
 * Class with static methods for manipulating Tag elements
 */
public class UtilsTags {

	/**
	 * @param main
	 * @param tag
	 * @param base
	 * @return
	 */
	public static double getCoverage(Database database, UIState uistate, Tag tag, MapBase base) {
		return tag.getCoverage(database, uistate.getTime(), base);
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 */
	public static Collection<String> extractTags(String text) {

		Set<String> result = new HashSet<String>();

		int pos = 0;
		while(true) {
			int start = text.indexOf('#', pos)+1;
			if(start == 0) break;
			pos = text.indexOf(' ', start+1);
			if(pos == -1) {
				result.add(text.substring(start));
				break;
			} else {
				result.add(text.substring(start, pos));
			}
		}

		return result;

	}

	/**
	 * 
	 * @param main
	 * @param canWrite
	 */
	public static void updateRelatedTags(final Main main, boolean canWrite) {

		final Database database = main.getDatabase();

		final MapBase base = main.getUIState().currentMapItem;

		Collection<Tag> tags = base.getRelatedTags(database);
		if(!tags.isEmpty() || canWrite) {

			HorizontalLayout tagHeader = new HorizontalLayout();
			tagHeader.setSpacing(true);

			Label header2 = new Label("Aihetunnisteet");
			header2.setHeight("32px");
			header2.addStyleName(ValoTheme.LABEL_HUGE);
			header2.addStyleName(ValoTheme.LABEL_BOLD);
			tagHeader.addComponent(header2);
			tagHeader.setComponentAlignment(header2, Alignment.BOTTOM_CENTER);

			if(canWrite) {
				final Button editButton = new Button();
				editButton.setIcon(new ThemeResource(WebContent.TAG_BLUE_EDIT_PNG));
				editButton.setStyleName(ValoTheme.BUTTON_SMALL);
				editButton.addClickListener(new ClickListener() {

					private static final long serialVersionUID = -6140867347404571880L;

					@Override
					public void buttonClick(ClickEvent event) {
						Utils.editTags(main, "Muokkaa aihetunnisteita", main.getUIState().currentMapItem);
					}

				});
				tagHeader.addComponent(editButton);
				tagHeader.setComponentAlignment(editButton, Alignment.BOTTOM_CENTER);
			}

			main.properties.addComponent(tagHeader);
			main.properties.setComponentAlignment(tagHeader, Alignment.MIDDLE_CENTER);

			HorizontalLayout divider = new HorizontalLayout();
			main.properties.addComponent(divider);
			main.properties.setComponentAlignment(divider, Alignment.MIDDLE_CENTER);

			VerticalLayout left = new VerticalLayout();
			left.setSpacing(true);
			left.setWidth("400px");
			left.setMargin(true);
			divider.addComponent(left);
			VerticalLayout right = new VerticalLayout();
			right.setSpacing(true);
			right.setWidth("400px");
			right.setMargin(true);
			divider.addComponent(right);

			Set<Tag> monitoredTags = getMonitoredTags(database, base);

			int i = 0;
			for(final Tag tag : tags) {

				final boolean monitor = base.hasMonitorTag(database, tag);
				String tagId = tag.getId(database);

				HorizontalLayout hl = new HorizontalLayout();
				hl.setSpacing(true);
				hl.setHeight("37px");

				Button tagButton = Utils.tagButton(database, "list", tagId, i++);
				left.addComponent(tagButton);
				left.setComponentAlignment(tagButton, Alignment.MIDDLE_RIGHT);

				if(canWrite) {
					Button b = new Button();
					b.addStyleName(ValoTheme.BUTTON_BORDERLESS);
					b.setIcon(FontAwesome.TIMES_CIRCLE);
					b.addClickListener(new ClickListener() {

						private static final long serialVersionUID = -4473258383318654850L;

						@Override
						public void buttonClick(ClickEvent event) {
							base.removeRelatedTags(database, tag);
							Utils.loseFocus(main.properties);
							Updates.update(main, true);
						}

					});
					hl.addComponent(b);
					hl.setComponentAlignment(b, Alignment.MIDDLE_LEFT);
				}

				if(base instanceof StrategyMap) {

					Button tagButton2 = new Button();
					tagButton2.setCaption(monitor ? "Seurataan toteutuksessa" : "Ei seurata toteutuksessa");
					tagButton2.addStyleName(monitor ? "greenButton" : "redButton");
					tagButton2.addStyleName(ValoTheme.BUTTON_SMALL);
					tagButton2.setWidth("200px");
					if(canWrite) {
						tagButton2.addClickListener(new ClickListener() {

							private static final long serialVersionUID = -1769769368034323594L;

							@Override
							public void buttonClick(ClickEvent event) {
								if(monitor) {
									base.removeMonitorTags(database, tag);
								} else {
									base.assertMonitorTags(database, tag);
								}
								Utils.loseFocus(main.properties);
								Updates.update(main, true);
							}

						});
						tagButton2.setEnabled(true);
					} else {
						tagButton2.setEnabled(false);
					}

					hl.addComponent(tagButton2);
					hl.setComponentAlignment(tagButton2, Alignment.MIDDLE_LEFT);

				} else {

					if(monitoredTags.contains(tag)) {
						Label l = new Label(" toteuttaa seurattavaa aihetta ");
						hl.addComponent(l);
						hl.setComponentAlignment(l, Alignment.MIDDLE_LEFT);
					}

				}

				right.addComponent(hl);
				right.setComponentAlignment(hl, Alignment.MIDDLE_LEFT);

			}

		}

	}

	/**
	 * 
	 * @param database
	 * @param base
	 * @return
	 */
	public static Set<Tag> getMonitoredTags(Database database, MapBase base) {

		Set<Tag> monitoredTags = new HashSet<Tag>();
		StrategyMap map = database.getMap(base);
		for(MapBase b : map.getOwners(database)) {
			monitoredTags.addAll(b.getMonitorTags(database));
		}
		return monitoredTags;

	}

	/**
	 * 
	 * @param main
	 * @param canWrite
	 */
	public static void updateMonitoredTags(final Main main, boolean canWrite) {

		final Database database = main.getDatabase();

		final MapBase base = main.getUIState().currentMapItem;

		if(!(base instanceof OuterBox  || base instanceof InnerBox)) return;

		Set<Tag> monitoredTags = getMonitoredTags(database, base);
		monitoredTags.removeAll(base.getRelatedTags(database));
		Map<String,MapBase> implementors = new HashMap<String,MapBase>();
		for(MapBase impl : ContribVisUtils.getImplementationSet(database, base)) {
			for(Tag i : impl.getRelatedTags(database)) {
				if(monitoredTags.contains(i)) {
					implementors.put(i.uuid, impl);
				}
			}
		}

		if(!monitoredTags.isEmpty() || canWrite) {

			HorizontalLayout tagHeader = new HorizontalLayout();
			tagHeader.setSpacing(true);

			Label header2 = new Label("Seurattavat aihetunnisteet");
			header2.setHeight("32px");
			header2.addStyleName(ValoTheme.LABEL_HUGE);
			header2.addStyleName(ValoTheme.LABEL_BOLD);
			tagHeader.addComponent(header2);
			tagHeader.setComponentAlignment(header2, Alignment.BOTTOM_CENTER);

			main.properties.addComponent(tagHeader);
			main.properties.setComponentAlignment(tagHeader, Alignment.MIDDLE_CENTER);

			HorizontalLayout divider = new HorizontalLayout();
			main.properties.addComponent(divider);
			main.properties.setComponentAlignment(divider, Alignment.MIDDLE_CENTER);

			VerticalLayout left = new VerticalLayout();
			left.setSpacing(true);
			left.setWidth("400px");
			left.setMargin(true);
			divider.addComponent(left);
			VerticalLayout right = new VerticalLayout();
			right.setSpacing(true);
			right.setWidth("400px");
			right.setMargin(true);
			divider.addComponent(right);

			int i = 0;
			for(final Tag tag : monitoredTags) {

				String tagId = tag.getId(database);

				HorizontalLayout hl = new HorizontalLayout();
				hl.setSpacing(true);
				hl.setHeight("37px");

				Button tagButton = Utils.tagButton(database, "inferred", tagId, i++);
				left.addComponent(tagButton);
				left.setComponentAlignment(tagButton, Alignment.MIDDLE_RIGHT);

				Base implementor = implementors.get(tag.uuid);
				if(implementor != null) {

					String desc = implementor.getId(database);
					if(desc.isEmpty()) desc = implementor.getText(database);

					Label tagLabel = new Label("Toteutetaan ylemmll tasolla: " + desc);
					hl.addComponent(tagLabel);
					hl.setComponentAlignment(tagLabel, Alignment.MIDDLE_LEFT);

				} else {

					Button tagButton2 = new Button();
					tagButton2.setCaption("Merkitse toteuttajaksi");
					tagButton2.addStyleName("redButton");
					tagButton2.addStyleName(ValoTheme.BUTTON_SMALL);
					tagButton2.setWidth("200px");
					if(canWrite) {
						tagButton2.addClickListener(new ClickListener() {

							private static final long serialVersionUID = -1769769368034323594L;

							@Override
							public void buttonClick(ClickEvent event) {
								base.assertRelatedTags(database, tag);
								Utils.loseFocus(main.properties);
								Updates.update(main, true);
							}

						});
						tagButton2.setEnabled(true);
					} else {
						tagButton2.setEnabled(false);
					}
					hl.addComponent(tagButton2);
					hl.setComponentAlignment(tagButton2, Alignment.MIDDLE_LEFT);

				}

				right.addComponent(hl);
				right.setComponentAlignment(hl, Alignment.MIDDLE_LEFT);

			}

		}

	}



}
