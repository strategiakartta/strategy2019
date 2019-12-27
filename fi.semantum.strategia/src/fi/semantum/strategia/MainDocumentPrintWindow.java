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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UtilsDB;

public class MainDocumentPrintWindow extends CustomLayout {
	
	private static final long serialVersionUID = 4103833454679973767L;
	public static final String DOCUMENT_PRINT_BODY_ID = "documentPrintBody";
	public static final String PRINT_OPTIONS_WINDOW_ID = "printOptionsWindow";
	private Main main;
	private PrintOptions printOptions;
	
	public MainDocumentPrintWindow(Main main) {
        super();
        this.main = main;
        this.printOptions = new PrintOptions();
        this.setId(DOCUMENT_PRINT_BODY_ID);
        setSizeUndefined();
        setWidth("100%");
        addStyleName("customDocumentLayout");
    }
	
	static class PrintOptions {
		
		public static enum Option {
			DEFAULT_DOCUMENT,
			ADVANCED
		}
		
		public static String hightlightColorUILanguage(HighlightColors c) {
			switch(c) {
			case LIGHT_GREY: return "Harmaa (vaalea)";
			case GREY: return "Harmaa";
			case DARK_GREY: return "Harmaa (tumma)";
			case LIGHT_YELLOW: return "Keltainen (vaalea)";
			case LIGHT_GREEN: return "Vihre‰ (vaalea)";
			case LIGHT_RED: return "Punainen (vaalea)";
			case YELLOW: return "Keltainen";
			case GREEN: return "Vihre‰";
			case RED: return "Punainen";
			default: return "(ei m‰‰ritelty)";
		}
		}
		
		public static String highlightColorCode(HighlightColors c) {
			switch(c) {
				case LIGHT_GREY: return "dfdfdf";
				case GREY: return "bfbfbf";
				case DARK_GREY: return "999999";
				case LIGHT_YELLOW: return "ffff99";
				case LIGHT_GREEN: return "99ff99";
				case LIGHT_RED: return "ff9999";
				case YELLOW: return "ffff55";
				case GREEN: return "55ff55";
				case RED: return "ff5555";
				default: return "dfdfdf";
			}
		}
		
		public static enum HighlightColors {
			YELLOW,
			LIGHT_YELLOW,
			GREEN,
			LIGHT_GREEN,
			RED,
			LIGHT_RED,
			LIGHT_GREY,
			GREY,
			DARK_GREY
		}
		
		Option option = Option.DEFAULT_DOCUMENT;
		List<String> customTags = new ArrayList<>();
		boolean appendTags = false;
		boolean highlightChangeSuggestionTargets = false;
		HighlightColors highlightColor = HighlightColors.LIGHT_GREY;
		
		public void setMainOption(Option option) {
			this.option = option;
		}
		
		public void addCustomTag(String tag) {
			if(!customTags.contains(tag)) {
				customTags.add(tag);
			}
		}
		
		public void setHighLightColor(HighlightColors h) {
			this.highlightColor = h;
		}
		
		public void setChangeSuggestionHighlights(boolean highlight) {
			this.highlightChangeSuggestionTargets = highlight;
		}
		
		public void setAppendTagsAfterDocument(boolean append) {
			this.appendTags = append;
		}
		
		public void removePossibleCustomTag(String tag) {
			customTags.remove(tag);
		}
		
		public PrintOptions() {
			
		}
		
	}
	
	/**
	 * Clear any print options that have been set
	 */
	public void clearOptions() {
		this.printOptions = new PrintOptions();
	}
	
	/**
	 * Create and open a pre-window for the main document print window that sets the options for this MainDocumentPrintWindow
	 * Then call the proper print window opener logic through main, refreshing the UI
	 */
	public void createAndOpenNewPrintOptionsDialog() {
		final Database database = main.getDatabase();
		
		HorizontalLayout root = new HorizontalLayout();
		root.setWidth("100%");
		
        VerticalLayout optionsLayout = new VerticalLayout();
        optionsLayout.setWidth("100%");
        optionsLayout.setSpacing(true);
        optionsLayout.setMargin(true);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
		Button printButton = new Button();
		printButton.setStyleName(ValoTheme.BUTTON_SMALL);
		printButton.setIcon(FontAwesome.PRINT);
		
		printButton.setCaption("Tulosta");
		buttons.addComponent(printButton);
        
        createAndAttachOptionsLayout(database, optionsLayout);
        
        Window window = Dialogs.makeDialog(main, "600px", "600px", "Tulostusvaihtoehdot", "Sulje", root, buttons);
        window.setId(PRINT_OPTIONS_WINDOW_ID);
        window.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
        
		printButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 8185117840647056627L;

			@Override
			public void buttonClick(ClickEvent event) {
				window.detach();
				window.close();
				main.showPrintViewWithSelectedOptions();
			}
			
		});
		root.addComponent(optionsLayout);
	}
	
	/**
	 * 
	 * @return
	 */
	private VerticalLayout createAdvancedOptionsLayout() {
		VerticalLayout advancedOptionsLayout = new VerticalLayout();
		advancedOptionsLayout.setWidth("100%");
		
		List<String> availableTags = Utils.getAvailableTransformationTags();
		
		VerticalLayout customTagsLayout = new VerticalLayout();
		customTagsLayout.setWidth("100%");
		customTagsLayout.setVisible(false);
		customTagsLayout.setSpacing(true);
		
		Label l = new Label();
		l.setValue("Valitse yksi tai useampi tunniste: ");
		customTagsLayout.addComponent(l);
		
		CheckBox appendTagsCheckBox = new CheckBox();
		//TODO: Enable this if needed for adding custom tags with parameters and values
		appendTagsCheckBox.setVisible(false);
		//^disabled for now
		
		appendTagsCheckBox.setValue(false);
		appendTagsCheckBox.setCaption("Lis‰‰ kartan tunnisteita dokumentit loppuun");
		appendTagsCheckBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1401468695822332575L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean selected = appendTagsCheckBox.getValue();
				customTagsLayout.setVisible(selected);
				printOptions.setAppendTagsAfterDocument(selected);
			}
			
		});

		ComboBox colorPickerComboBox = createColorPickerComboBox();
		colorPickerComboBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object selection = colorPickerComboBox.getValue();
				if(selection != null && selection instanceof PrintOptions.HighlightColors) {
					printOptions.setHighLightColor((PrintOptions.HighlightColors)selection);
				} else {
					System.err.println("Null color selection or incorrect item class!");
				}
			}
			
		});

		colorPickerComboBox.select(PrintOptions.HighlightColors.LIGHT_GREY);
		
		CheckBox addChangeSuggestionHighlightsCheckBox = new CheckBox();
		addChangeSuggestionHighlightsCheckBox.setValue(false);
		addChangeSuggestionHighlightsCheckBox.setDescription("V‰rj‰‰ muutosehdotukset");
		addChangeSuggestionHighlightsCheckBox.setCaption("Lis‰‰ muutosehdotusv‰rj‰ys");
		addChangeSuggestionHighlightsCheckBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -2311579983508657887L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean selected = addChangeSuggestionHighlightsCheckBox.getValue();
				printOptions.setChangeSuggestionHighlights(selected);
				colorPickerComboBox.setVisible(selected);
			}
			
		});
		colorPickerComboBox.setVisible(addChangeSuggestionHighlightsCheckBox.getValue());
		
		HorizontalLayout colorPickerLayout = new HorizontalLayout();
		colorPickerLayout.setWidth("100%");
		colorPickerLayout.setHeight("40px");
		
		colorPickerLayout.addComponent(addChangeSuggestionHighlightsCheckBox);
		colorPickerLayout.setComponentAlignment(addChangeSuggestionHighlightsCheckBox, Alignment.MIDDLE_LEFT);
		colorPickerLayout.addComponent(colorPickerComboBox);
		colorPickerLayout.setComponentAlignment(colorPickerComboBox, Alignment.MIDDLE_LEFT);
		
		advancedOptionsLayout.addComponent(colorPickerLayout);
		advancedOptionsLayout.addComponent(appendTagsCheckBox);
		
		for(String tag : availableTags) {
			CheckBox checkBox = new CheckBox();
			checkBox.setValue(false);
			checkBox.setCaption(tag);
			checkBox.addValueChangeListener(new ValueChangeListener() {

				private static final long serialVersionUID = -3205154444730484190L;

				@Override
				public void valueChange(ValueChangeEvent event) {
					boolean selected = checkBox.getValue();
					if(selected) {
						printOptions.addCustomTag(checkBox.getCaption());
					} else {
						printOptions.removePossibleCustomTag(checkBox.getCaption());
					}
				}
				
			});
			customTagsLayout.addComponent(checkBox);
		}
		
		advancedOptionsLayout.addComponent(customTagsLayout);
		customTagsLayout.setMargin(new MarginInfo(false, false, false, true));
		
		return advancedOptionsLayout;
	}
	
	private ComboBox createColorPickerComboBox() {
		ComboBox combo = new ComboBox();
		combo.setStyleName(ValoTheme.COMBOBOX_TINY);
		combo.setNullSelectionAllowed(false);
		for(PrintOptions.HighlightColors c : PrintOptions.HighlightColors.values()) {
			combo.addItem(c);
			combo.setItemCaption(c, PrintOptions.hightlightColorUILanguage(c));
		}
		return combo;
	}
	
	private void createAndAttachOptionsLayout(Database database, VerticalLayout optionsLayout) {
		VerticalLayout advancedOptionsLayout = createAdvancedOptionsLayout();
		
		ComboBox comboBox = new ComboBox();
		comboBox.setStyleName(ValoTheme.COMBOBOX_SMALL);
		comboBox.setWidth("300px");
		comboBox.setNullSelectionAllowed(false);
		comboBox.addItem(PrintOptions.Option.DEFAULT_DOCUMENT);
		comboBox.setItemCaption(PrintOptions.Option.DEFAULT_DOCUMENT, "Dokumentti sellaisenaan");
		comboBox.addItem(PrintOptions.Option.ADVANCED);
		comboBox.setItemCaption(PrintOptions.Option.ADVANCED, "Lis‰‰ vaihtoehtoja...");
		
		comboBox.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 4552494877329711614L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				PrintOptions.Option value = (PrintOptions.Option)comboBox.getValue();
				printOptions.setMainOption(value);
				
				if(value == PrintOptions.Option.ADVANCED) {
					advancedOptionsLayout.setVisible(true);
				} else {
					advancedOptionsLayout.setVisible(false);
				}
			}
			
		});
		
		comboBox.select(PrintOptions.Option.DEFAULT_DOCUMENT);
		
		optionsLayout.addComponent(comboBox);
		optionsLayout.setExpandRatio(comboBox, 0.0f);
		optionsLayout.setComponentAlignment(comboBox, Alignment.TOP_CENTER);
		optionsLayout.addComponent(advancedOptionsLayout);
		optionsLayout.setExpandRatio(advancedOptionsLayout, 1.0f);
		optionsLayout.setComponentAlignment(advancedOptionsLayout, Alignment.TOP_CENTER);
	}
	
	private void htmlToFullWindowLayout(String html) throws IOException {
		this.removeAllComponents();
		this.initTemplateContentsFromInputStream(new ByteArrayInputStream(
				html.getBytes(StandardCharsets.UTF_8)
			));
	}
	
	private static String applyPrintDocumentStyle(List<String> htmls) {
		String result = "<body class=\"document\" type=\"text/css\">";
		for(String html : htmls) {
			result += "<div class=\"a4page\">" + html + "</div>";
		}
		result += "</body>";
		return result;
	}
	
	/**
	 * Update the HTML shown in the Window
	 */
	public void refreshHTMLWithOptions() {
		try {
			List<String> unstyledHTMLs = getCurrentSelectedDocumentWithOfficeChaptersHTMLsWithOptions();
			String html = applyPrintDocumentStyle(unstyledHTMLs);
			htmlToFullWindowLayout(html);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private List<String> getCurrentSelectedDocumentWithOfficeChaptersHTMLsWithOptions() {
		Database database = main.getDatabase();
		Account account = main.getAccountDefault();
		
		Office office = main.uiState.getCurrentOffice();
		if (office != null) {
			ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(database);
			if (docs != null) {
				List<String> htmls = new ArrayList<>();

				for(TextChapter chapter : docs.getOfficeAndSharedChapters(database, office)) {
					List<String> innerHtmlsWithChangeSuggestions = UtilsDB.subChapterHtmlsTargetedByChangeSuggestions(database, account, chapter);
					String tempHtmlWithoutTagReplacements = chapter.getHTML();
					
					String finalHTMLWithoutTagReplacements = tempHtmlWithoutTagReplacements;
					if(printOptions.highlightChangeSuggestionTargets) {
						Document jsoupDoc = Jsoup.parse(tempHtmlWithoutTagReplacements);
						for(String innerHtmlToPaint : innerHtmlsWithChangeSuggestions) {
							for(Element elem : jsoupDoc.getAllElements()) {
								if(elem.html().equals(innerHtmlToPaint)) {
									String colorCode = PrintOptions.highlightColorCode(printOptions.highlightColor);
									elem.attr("style", "background:#" + colorCode + ";"); //Highlight with only slightly light grey
								}
							}
						}
						//Get only the inner HTML of the body for appending
						finalHTMLWithoutTagReplacements = jsoupDoc.body().html();
					}
					
					//Replace tags:
					String html = Utils.htmlTagReplacement(finalHTMLWithoutTagReplacements, main);
					htmls.add(html);
				}
				return (htmls);
			} else {
				return Collections.emptyList();
			}
		} else {
			return Collections.emptyList();
		}
	}
	

}
