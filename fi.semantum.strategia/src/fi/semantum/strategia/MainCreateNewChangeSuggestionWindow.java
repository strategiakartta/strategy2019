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
import java.util.Collections;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.ChangeSuggestion;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UtilsDB;
import fi.semantum.strategy.db.UtilsDBComments;

public class MainCreateNewChangeSuggestionWindow extends Window {

	private static final long serialVersionUID = -5920944215082302811L;
	
	private Main main;
	private VerticalLayout root;
	private Button save;
	private Label info;
	
	/**
	 * Can be null, if is, then creates new ChangeSuggestion
	 */
	private String possibleExistingChangeSuggestionUUID;
	
	private TextField shortDescriptionTextEditor;
	private TextArea newSuggestionTextEditor;
	private TextArea motivationTextEditor;
	private TextArea targetStringTextEditor;
	private ComboBox officeAndChapterTargetSelectionComboBox;
	
	/**
	 * This can be called at any time to select a target
	 * @param targetBase
	 */
	public void setTargetBase(Base targetBase) {
		if(targetBase != null) {
			//Add this option to the combobox of UUID's.
			Database database = main.getDatabase();
			//Ensure the ID is unique in the combobox list by remove an existing copy of it
			if(officeAndChapterTargetSelectionComboBox.containsId(targetBase.uuid)) {
				officeAndChapterTargetSelectionComboBox.removeItem(targetBase.uuid);
			}
			
			officeAndChapterTargetSelectionComboBox.addItem(targetBase.uuid);
			
			String caption = "";
			if(targetBase instanceof MapBase) {
				caption = database.getType((MapBase)targetBase) + ": " + UtilsDB.mapBaseText(database, (MapBase)targetBase);
			} else {
				caption = UtilsDBComments.getPossibleBaseSummary(database, targetBase, false, false, " - ");
			}
			officeAndChapterTargetSelectionComboBox.setItemCaption(targetBase.uuid, caption);
			officeAndChapterTargetSelectionComboBox.select(targetBase.uuid);
			
			//Update the "Mit‰ muokataan" field here too, but only if there is not value in the field
			if(targetStringTextEditor.getValue().isEmpty()) {
				targetStringTextEditor.setValue(targetBase.getText(database));
			}
		} else {
			officeAndChapterTargetSelectionComboBox.select(null);
		}
	}

	private List<TextChapter> fetchAndSortTextChapterOptions() {
		Database database = main.getDatabase();
		ResultAgreementDocuments doc = ResultAgreementDocuments.getInstance(database);
		if(doc != null) {
			List<TextChapter> chapters = doc.getAllChapters(database);
			Collections.sort(chapters, TextChapter.chapterIndexComparator);
			return chapters;
		} else {
			System.err.println("Null ResultAgreementDocuments - cannot create TextChapter options");
			return new ArrayList<TextChapter>();
		}
	}

	private void populateOfficeAndChapterTargetSelectionComboBox() {
		List<TextChapter> options = fetchAndSortTextChapterOptions();
		
		boolean removeOk = officeAndChapterTargetSelectionComboBox.removeAllItems();
		if(!removeOk) {
			System.err.println("Failed to remove items from officeAndChapter selection combo box for ChangeSuggestion!");
		}
		
		officeAndChapterTargetSelectionComboBox.select(null);
		Database database = main.getDatabase();
		
		for(TextChapter chapter : options) {
			officeAndChapterTargetSelectionComboBox.addItem(chapter.uuid);
			String caption = UtilsDBComments.textChapterToSummary(database, chapter);
			officeAndChapterTargetSelectionComboBox.setItemCaption(chapter.uuid, caption);
		}
	}
	
	@Override
	public void close() {
		super.close();
		cleanup();
	}
	
	public MainCreateNewChangeSuggestionWindow(Main main_) {
        super("Muutosehdotus"); // Set window caption
        this.main = main_;

        // Some basic content for the window
        root = new VerticalLayout();
        
        root.setMargin(true);
        root.setSpacing(true);
        
        setContent(root);
        setClosable(true);

        info = new Label();
        info.setCaption("Ohje");
        info.setStyleName(ValoTheme.BUTTON_TINY);
        info.setIcon(FontAwesome.QUESTION);
        info.setDescription("Sopimukseen tehd‰‰n muutokset muutosehdotuksella.</br>"
        		+ "Muutosehdotus ja siihen liittyv‰ kommentointi on kaikkien k‰ytt‰jien n‰ht‰vill‰.</br>"
        		+ "Muutosehdotuksen tulee aina olla organisaation virallinen ehdotus muutokseksi.</br>"
        		+ "Muutosehdotus selkeytt‰‰ sopimusvaiheen valmistelua ja samalla se mahdollistaa muutosehdotusten"
        		+ "n‰kyvyyden kaikille valmistelijoille samanaikaisesti.</br>"
        		+ "Vain tulosohjaajat voivat merkit‰ muutosehdotuksen k‰sitellyksi, jolloin muutosehdotus joko hyl‰t‰‰n, sit‰ t‰ydennet‰‰n tai se"
        		+ "hyv‰ksyt‰‰n sellaisenaan.</br>"
        		+ "Mik‰li kaikki muutosehdotuksen k‰sittelyss‰ mukana olevat organisaatiot ovat ehdotuksesta samaa mielt‰ hyv‰ksyt‰‰n "
        		+ "ehdotus ja tulosohjaaja korjaa sopimusta ehdotuksen mukaan.</br>"
        		+ "Mik‰li muutosehdotus ei tule hyv‰ksytyksi se voidaan k‰sitell‰ edelleen tulosneuvotteluissa ylimm‰n johdon v‰lill‰.</br>"
        		+ "Muutosehdotus voi myˆs siirty‰ seuraaville tulossopimuskausille.");
        
        save = new Button();
        save.setStyleName(ValoTheme.BUTTON_TINY);
        save.setIcon(FontAwesome.SAVE);
        
        save.addClickListener(new ClickListener() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 2652776348069466696L;

			public void buttonClick(ClickEvent event) {
				if(UtilsComments.accountHasCommentToolWriteRights(main)) {
	            	String sanitizedMotivation = replaceNewlines(SafeHtmlUtils.htmlEscape(motivationTextEditor.getValue()));
	            	String sanitizedNewSuggestion = replaceNewlines(SafeHtmlUtils.htmlEscape(newSuggestionTextEditor.getValue()));
	            	String sanitizedTargetString = replaceNewlines(SafeHtmlUtils.htmlEscape(targetStringTextEditor.getValue()));
	            	
	            	String sanitizedShortDescription = SafeHtmlUtils.htmlEscape(shortDescriptionTextEditor.getValue());
	            	
	            	String possibleTargetBaseUUID = null;
	            	Object item = officeAndChapterTargetSelectionComboBox.getValue();
	            	if(item != null) {
	            		possibleTargetBaseUUID = item.toString();
	            	}
	            	
	            	if(validateAndNotify(sanitizedTargetString, sanitizedMotivation, sanitizedNewSuggestion, sanitizedShortDescription)) {
		            	if(possibleExistingChangeSuggestionUUID != null) {
		            		//Edit existing
		            		updateChangeSuggestion(main, possibleExistingChangeSuggestionUUID, possibleTargetBaseUUID, sanitizedMotivation, sanitizedTargetString, sanitizedNewSuggestion, sanitizedShortDescription);
		            	} else {
		            		//Create new
		            		UtilsComments.possiblyCreateChangeSuggestion(main,
		            				sanitizedShortDescription,
		            				possibleTargetBaseUUID,
		            				sanitizedTargetString,
		            				sanitizedNewSuggestion,
		            				sanitizedMotivation);
		            	}
		            	
		            	Updates.update(main, true);
		            	main.commentLayout.refresh();
		            	close();
	            	} else {
	            		VerticalLayout layout = new VerticalLayout();
	            		layout.setHeight("50px");
	            		layout.setWidth("200px");
	            		layout.addComponent(new Label(""));
	    		        Dialogs.errorDialog(main, "Muutosehdotuksen kentiss‰ on virheit‰"
	    		        		+ " Tarkista etteiv‰t kent‰t ole tyhji‰ tai tiivistelm‰ liian pitk‰.", layout, "700px", "100px");
	            	}
				} else {
					System.err.println("Account does not have right to write a new change suggestion! Create button clicked in dialog!");
				}
            }
        });
        
        Page.getCurrent().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8370821642096096388L;

			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event) {
				resize(main);
			}
		});
        
		initializeFields();
		
		root.addComponent(shortDescriptionTextEditor);
		root.addComponent(officeAndChapterTargetSelectionComboBox);
		root.addComponent(targetStringTextEditor);
		root.addComponent(newSuggestionTextEditor);
		root.addComponent(motivationTextEditor);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.setWidth("100%");
		
		buttonToolbar.addComponent(info);
		buttonToolbar.addComponent(save);
		buttonToolbar.setComponentAlignment(info, Alignment.TOP_LEFT);
		buttonToolbar.setComponentAlignment(save, Alignment.TOP_RIGHT);
		buttonToolbar.setExpandRatio(info, 1.0f);
		buttonToolbar.setExpandRatio(save, 0.0f);
		
        root.addComponent(buttonToolbar);
        
        root.setExpandRatio(shortDescriptionTextEditor, 0.0f);
        root.setExpandRatio(officeAndChapterTargetSelectionComboBox, 0.0f);
        root.setExpandRatio(targetStringTextEditor, 0.25f);
        root.setExpandRatio(newSuggestionTextEditor, 0.25f);
        root.setExpandRatio(motivationTextEditor, 0.25f);
        root.setExpandRatio(buttonToolbar, 0.0f);
        
        root.setComponentAlignment(shortDescriptionTextEditor, Alignment.TOP_CENTER);
        root.setComponentAlignment(officeAndChapterTargetSelectionComboBox, Alignment.TOP_CENTER);
        root.setComponentAlignment(targetStringTextEditor, Alignment.TOP_CENTER);
        root.setComponentAlignment(newSuggestionTextEditor, Alignment.TOP_CENTER);
        root.setComponentAlignment(motivationTextEditor, Alignment.TOP_CENTER);
        
        resize(main);
    }
	
	private String replaceNewlines(String input) {
		if(input == null) return null;
		input = input.replaceAll("\r\n", "</br>");
		return input.replaceAll("\n", "</br>");
	}
	
	private String replaceBR(String input) {
		if(input == null) return null;
		
		return input.replaceAll("</br>", "\n");
	}
	
	/**
	 * From client to server
	 * @param main
	 * @param uuid
	 * @param sanitizedMotivation
	 * @param sanitizedTargetString
	 * @param sanitizedNewSuggestion
	 * @param sanitizedShortDescription
	 */
	private static void updateChangeSuggestion(Main main, String uuid, String possibleTargetBaseUUID,  String sanitizedMotivation,
			String sanitizedTargetString, String sanitizedNewSuggestion, String sanitizedShortDescription) {
		Database database = main.getDatabase();
		ChangeSuggestion target = database.find(uuid);
		if(target != null) {
			target.updateChangeSuggestion(main.getAccountDefault(),
					database,
					sanitizedShortDescription,
					possibleTargetBaseUUID,
					sanitizedTargetString,
					sanitizedNewSuggestion,
					sanitizedMotivation);
			Updates.update(main, true);
		} else {
			System.err.println("Target ChangeSuggestion is null, expected non-null!");
		}
	}
	
	private void resize(Main main) {
		int width = main.getFullWindowWidth()/2;
		root.setWidth(width, Unit.PIXELS);
        root.setHeight(main.getWindowHeightPixels()/10*9 - Main.TOOLBAR_HEIGHT, Unit.PIXELS);
        setPositionY(Main.TOOLBAR_HEIGHT);
        setPositionX(width-Main.TOOLBAR_HEIGHT);
	}
	
	private boolean validateAndNotify(String targetString, String motivation, String newSuggestion, String shortDescription) {
		boolean mOK = validateMotivation(motivation);
		boolean sOK = validateSuggestion(newSuggestion);
		boolean dOK = validateShortDescription(shortDescription);
		boolean tOK = validateTargetString(targetString);
		return tOK && mOK && sOK && dOK;
	}
	
	private boolean validateTargetString(String targetString) {
		if(targetString == null || targetString.equals("")) {
			return false;
		}
		return true;
	}
	
	private boolean validateMotivation(String motivation) {
		if(motivation == null) {
			return false;
		}
		return true;
	}
	
	private boolean validateSuggestion(String newSuggestion){
		if(newSuggestion == null || newSuggestion.equals("")) {
			return false;
		}
		
		return true;
	}
	
	private boolean validateShortDescription(String shortDescription){
		if(shortDescription == null || shortDescription.equals("")) {
			return false;
		}
		if(shortDescription.length() > Configuration.getMAX_SHORT_DESCRIPTION_LENGTH()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Enter this mode to edit existing changeSuggestion
	 */
	public void editExistingMode(ChangeSuggestion changeSuggestion) {
		this.possibleExistingChangeSuggestionUUID = changeSuggestion.uuid;
		populateRoot();
		String targetUUID = changeSuggestion.getPossibleBaseTargetUUID();
		if(targetUUID != null) {
			Base b = main.getDatabase().find(targetUUID);
			setTargetBase(b);
		}
		super.setCaption("Muokkaa muutosehdotusta");
		save.setCaption("Tallenna");
	}
	
	/**
	 * Enter this mode to create new ChangeSuggestion
	 */
	public void createNewMode() {
		this.possibleExistingChangeSuggestionUUID = null;
		
		super.setCaption("Luo uusi muutosehdotus");
		save.setCaption("Luo uusi");
		populateRoot();
	}
	
	private void initializeFields() {
		shortDescriptionTextEditor = new TextField();
		shortDescriptionTextEditor.setHeight("36px");
		shortDescriptionTextEditor.setWidth("100%");
		shortDescriptionTextEditor.setCaption(Terminology.CHANGE_SUGGESTION_SHORT_DESCRIPTION);
		
		shortDescriptionTextEditor.setDescription("T‰m‰ on muutosehdotuksen otsikko, joka n‰kyy muutosehdotukset luetteloissa.</br>"
				+ "Kirjoita tekij‰, kappale johon muutos kohdistuu ja otsikko ja n‰iden j‰lkeen lyhyt kuvaus siit‰ miss‰ muutoksessa on kyse.</br>"
				+ "Esimerkiksi LVM KOO: 4.1 Toiminta-ajatus ja visio, t‰ydennet‰‰n teksti‰</br>tai</br>"
				+ "V‰yl‰: 4,2 Viraston rooli yhteiskunnallisten vaikuttavuustavoitteiden saavuttamisessa ehdotetaan poistettavaksi osa.");
		
		shortDescriptionTextEditor.setRequiredError("Tiivistelm‰ ei saa olla tyhj‰, eik‰ yli " + Configuration.getMAX_SHORT_DESCRIPTION_LENGTH() + " merkki‰ pitk‰.");
		shortDescriptionTextEditor.setRequired(true);
		
		targetStringTextEditor = new TextArea();
		targetStringTextEditor.setCaption(Terminology.WHAT_TO_EDIT);
		targetStringTextEditor.setRequiredError("T‰m‰ kentt‰ ei saa olla tyhj‰");
		targetStringTextEditor.setRequired(true);
		targetStringTextEditor.setDescription("Kopioi t‰h‰n se teksti sopimuksesta jota haluat muuttaa.");
		targetStringTextEditor.setSizeFull();
		
		newSuggestionTextEditor = new TextArea();
		newSuggestionTextEditor.setCaption(Terminology.HOW_TO_EDIT);
		newSuggestionTextEditor.setDescription("Kirjoita t‰h‰n sopimuksen teksti jollaiseksi se tulisi muuttaa");
		newSuggestionTextEditor.setSizeFull();
		newSuggestionTextEditor.setRequiredError("T‰m‰ kentt‰ ei saa olla tyhj‰");
		newSuggestionTextEditor.setRequired(true);
		
		motivationTextEditor = new TextArea();
		motivationTextEditor.setCaption(Terminology.WHY_EDIT);
		motivationTextEditor.setDescription("Kuvaa t‰h‰n perustelut jonka takia sopimuksen teksti‰ tulisi muuttaa");
		motivationTextEditor.setSizeFull();
		motivationTextEditor.setRequired(false);
		
		officeAndChapterTargetSelectionComboBox = new ComboBox();
		officeAndChapterTargetSelectionComboBox.setPageLength(0); //Show all items
		officeAndChapterTargetSelectionComboBox.setTextInputAllowed(false);
		officeAndChapterTargetSelectionComboBox.setCaption(Terminology.CHANGE_SUGGESTION_TARGET);
		officeAndChapterTargetSelectionComboBox.setDescription("Valitse mihin muutosehdotus kohdistuu");
		officeAndChapterTargetSelectionComboBox.setHeight("36px");
		officeAndChapterTargetSelectionComboBox.setWidth("100%");
		officeAndChapterTargetSelectionComboBox.setStyleName(ValoTheme.COMBOBOX_TINY);
		officeAndChapterTargetSelectionComboBox.addStyleName(CustomCSSStyleNames.STYLE_CHANGE_SUGGESTION_OPINION_STATE_COMBO_BOX);		
		officeAndChapterTargetSelectionComboBox.setNullSelectionAllowed(true);
	}
	
	/**
	 * 
	 */
	private void populateRoot() {
		setTargetBase(null); //Base targets null at first - must be explicitly changed later to target something valid
		populateOfficeAndChapterTargetSelectionComboBox();
		updateFieldsFromChangeSuggestion();
		
		Account account = main.getAccountDefault();
		Office o = UtilsDB.getPossibleAssociatedOfficeFromAccount(main.getDatabase(), account);
		save.setEnabled(o != null);
		if(o == null) {
			save.setDescription("T‰ll‰ k‰ytt‰j‰ll‰ ei ole virastoa, joten muutosehdotusta ei voi tallentaa.");
		} else {
			save.setDescription("");
		}
	}
	
	/**
	 * From server to client
	 */
	private void updateFieldsFromChangeSuggestion() {
		if(this.possibleExistingChangeSuggestionUUID != null) {
			ChangeSuggestion target = main.getDatabase().find(this.possibleExistingChangeSuggestionUUID);
			if(target != null) {
				shortDescriptionTextEditor.setValue(target.getDescription());
				targetStringTextEditor.setValue(replaceBR(target.getPossibleTargetString()));
				newSuggestionTextEditor.setValue(replaceBR(target.getNewSuggestion()));
				motivationTextEditor.setValue(replaceBR(target.getMotivation()));
			} else {
				System.err.println("Target ChangeSuggestion is null, expected non-null!");
			}

		}
	}
	
	public void cleanup() {
		officeAndChapterTargetSelectionComboBox.removeAllItems();
		possibleExistingChangeSuggestionUUID = null;
		shortDescriptionTextEditor.setValue("");
		targetStringTextEditor.setValue("");
		newSuggestionTextEditor.setValue("");
		motivationTextEditor.setValue("");
	}
}

