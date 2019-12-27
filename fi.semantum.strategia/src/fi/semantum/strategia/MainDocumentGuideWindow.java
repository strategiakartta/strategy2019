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
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.CalendarEventBase;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.UtilsDB.HTMLMapTransform;

public class MainDocumentGuideWindow {
	
	public static void createAndOpen(final Main main) {

		final Database database = main.getDatabase();
		
		HorizontalLayout root = new HorizontalLayout();
		root.setWidth("100%");
        VerticalLayout guideLayout = new VerticalLayout();
        guideLayout.setWidth("100%");
        guideLayout.setSpacing(true);
        guideLayout.setMargin(true);
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        createAndAttachGuideLayout(main, database, guideLayout);
        root.addComponent(guideLayout);
        root.setExpandRatio(guideLayout, 0.0f);
        
        Window w = Dialogs.makeDialog(main, main.dialogWidth(0.8), main.dialogHeight(0.8), "Dokumentin ohjeet", "Sulje", root, buttons);
        w.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
	}
	
	private static void createAndAttachGuideLayout(final Main main, final Database database, VerticalLayout guideLayout) {
		TabSheet sheet = new TabSheet();
		guideLayout.addComponent(sheet);
        sheet.setSizeFull();
        
        VerticalLayout tagGuideLayout = new VerticalLayout();
        tagGuideLayout.setWidth("100%");
        tagGuideLayout.setSpacing(true);
        tagGuideLayout.setMargin(true);
        
        VerticalLayout markdownGuideLayout = new VerticalLayout();
        markdownGuideLayout.setWidth("100%");
        markdownGuideLayout.setSpacing(true);
        markdownGuideLayout.setMargin(true);
        
        sheet.addTab(tagGuideLayout, "Kartan Tunnisteet");
        sheet.addTab(markdownGuideLayout, "Markdown Ohje");
        
		createAndAttachTagGuideLayout(main, database, tagGuideLayout);
		createAndAttachMarkdownGuideLayout(main, database, markdownGuideLayout);
	}
	
	
	
	
	private static void createAndAttachMarkdownGuideLayout(final Main main, final Database database, VerticalLayout layout) {
		final Table table = new Table();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.addStyleName(ValoTheme.TABLE_SMALL);
		table.addStyleName(ValoTheme.TABLE_SMALL);
		table.addStyleName(ValoTheme.TABLE_COMPACT);
		table.addStyleName(CustomCSSStyleNames.STYLE_YEAR_BLOCK_SUMMARY_TABLE);
		table.setSortEnabled(false);
		table.addContainerProperty("Markdown esimerkki", Label.class, null);
		table.addContainerProperty("Selitys", String.class, null);
		table.setColumnExpandRatio("Markdown esimerkki", 0.60f);
		table.setColumnExpandRatio("Selitys", 0.40f);
		table.setWidth("100%");
		table.setHeight("100%");
		table.setSelectable(false);

		int tableIndex = 0;
		
		String tableExample = "| Header1 | Header2 | Header3 |\n" + 
				"| ---- | ---- | ---- |\n" + 
				"| <cols /> 50% | 30% | 20%|\n" + 
				"| ContentCell1 |  ContentCell2 | ContentCell3 |";
		String tableGuide = "Esimerkkitaulukko markdownissa. Sarakkeiden leveys on vaihdettavissa lisäämättä <cols /> tyylinen rivi taulukkon.";
		
		tableIndex = addMarkdownGuideEntry(
				table,
				tableIndex,
				tableExample,
				tableGuide);
		
		String centeredExample = "<p style=\"text-align: center;\">This is centered</p>";
		String centeredGuide = "Tekstin tyyliä saa lisäämällä HTML style elementin tekstin ympärille. Esimerkkinä keskitetty teksti.";
		tableIndex = addMarkdownGuideEntry(
				table,
				tableIndex,
				centeredExample,
				centeredGuide);
		
		String colorExample = "<p style=\"color:blue;\">This is blue</p>";
		String colorGuide = "Myös väriä saa muokattua. Kokeile myös muita HTML tyylejä.";
		tableIndex = addMarkdownGuideEntry(
				table,
				tableIndex,
				colorExample,
				colorGuide);
		
		table.setPageLength(table.size());
		
		layout.addComponent(table);
		layout.setExpandRatio(table, 1.0f);		
	}
	
	private static int addMarkdownGuideEntry(Table table, int tableIndex, String exampleText, String guideText) {
		Label l = new Label();
		l.setContentMode(ContentMode.HTML);
		
		exampleText = exampleText.replaceAll("<", "&lt;");
		exampleText = exampleText.replaceAll(">", "&gt;");
		exampleText = exampleText.replaceAll("\n", "</br>");
		
		l.setValue(exampleText);
		table.addItem(new Object[] {
				l,
				guideText
			},
			tableIndex);
		return tableIndex + 1;
	}
	
	private static void createAndAttachTagGuideLayout(final Main main, final Database database, VerticalLayout layout) {
		List<HTMLMapTransform> mapTransforms = Utils.getAvailableMapTransforms();
		
		final Table table = new Table();
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.addStyleName(ValoTheme.TABLE_SMALL);
		table.addStyleName(ValoTheme.TABLE_SMALL);
		table.addStyleName(ValoTheme.TABLE_COMPACT);
		table.addStyleName(CustomCSSStyleNames.STYLE_YEAR_BLOCK_SUMMARY_TABLE);
		table.setSortEnabled(false);
		table.addContainerProperty("Tunnisteet", String.class, null);
		table.addContainerProperty("Parametrit", String.class, null);
		table.addContainerProperty("Parametrien käyttöohjeet", Label.class, null);
		table.setColumnExpandRatio("Tunnisteet", 0.18f);
		table.setColumnExpandRatio("Parametrit", 0.18f);
		table.setColumnExpandRatio("Parametrien käyttöohjeet", 0.64f);
		table.setWidth("100%");
		table.setHeight("100%");
		table.setSelectable(false);

		int tableIndex = 0;
		for(HTMLMapTransform t : mapTransforms) {			
			Map<String, String> parameterGuide = t.possibleParametersGuide(database);
			tableIndex++;
			table.addItem(new Object[] {
					"<" + t.tag() + " />",
					parameterGuide.size() == 0 ? "(ei parametreja)" : "",
					new Label()
				},
				tableIndex);
			for(String key : parameterGuide.keySet()) {
				tableIndex++;
				Label label = new Label();
				label.setContentMode(ContentMode.HTML);
				label.setValue("<div style=\"margin-top: 3px; margin-bottom: 3px;\">" + parameterGuide.get(key).replaceAll("\n", "</br>") + "</div>");
				table.addItem(new Object[] {
						"",
						key,
						label
					},
					tableIndex);
			}
		}
		
		table.setPageLength(table.size());
		
		//Styling for rows
		table.setCellStyleGenerator(new Table.CellStyleGenerator() {

			private static final long serialVersionUID = 3802522232057974364L;

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				if (propertyId == null) {
					try {
						Item item = table.getItem(itemId);
						Property p = item.getItemProperty("Tunnisteet");
						Object t = p.getValue();
						String s = (String)t;
						if(!s.equals("")) {
							return CalendarEventBase.EventColor.BLUE.name().toLowerCase();
						} else {
							return null;
						}
					} catch (NullPointerException | ClassCastException e) {
						return null;
					}
				} else {
					return null;
				}
			}
		});
		
		
		layout.addComponent(table);
		layout.setExpandRatio(table, 1.0f);
		Label exampleLabel = new Label();
		exampleLabel.setCaption("Esimerkkejä:");
		exampleLabel.setValue("<tunniste />, <tunniste parametri1=\"arvo\">, <tunniste par1=\"a1\" par2=\"a2\"/>");
		layout.addComponent(exampleLabel);
		layout.setExpandRatio(exampleLabel, 1.0f);
		layout.setComponentAlignment(exampleLabel, Alignment.BOTTOM_CENTER);
	}
	
}
