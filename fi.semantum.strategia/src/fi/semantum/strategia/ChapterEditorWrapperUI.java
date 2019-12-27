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
import java.util.List;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategia.widget.TextChapterCommentsJS;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.MarkdownToHTMLContent;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.ResultAgreementToolAdminBase;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.TextChapter;
import fi.semantum.strategy.db.UtilsDB;

public class ChapterEditorWrapperUI {

	public static final String EXTRA_SMALL_FONT = "extra-small-font";
	
	private HorizontalLayout mainLayout;
	private VerticalLayout left;
	protected Main main;
	
	//Reload the info in these when chapter changes:
	private MarkdownToHTMLContent chapter;
	
	private VerticalLayout reviewHTML;
	private Button updateButton;
	private Button discardChangesButton;
	private Button previewButton;
	private Button toggleShowFullDocumentButton;
	private Button openInPDFViewButton;
	private Button openGuideViewButton;
	private TextArea editor;
	private Button hideEditorViewButton;
	private TextChapterCommentsJS js;
	
	private boolean includeShowAll = false;
	private boolean includeAdvancedToolBar = false;
	private boolean includeHideEditor = false;

	public ChapterEditorWrapperUI(Main main_, MarkdownToHTMLContent content) {
		this.main = main_;
		if(content instanceof TextChapter) {
			includeShowAll = true;
			includeAdvancedToolBar = true;
			includeHideEditor = true;
		}
		init(content);
		updateUI(true);
	}
	
	public HorizontalLayout getRoot() {
		return this.mainLayout;
	}
	
	public MarkdownToHTMLContent getMarkdownToHTMLContent() {
		return this.chapter;
	}
	
	/**
	 * Add a new chapter to this MainChapterEditor and reload the view
	 * @param chapter
	 */
	public void switchChapter(TextChapter chapter) {
		this.chapter = chapter;
		dispose();
		updateUI(true);
	}
	
	
	/**
	 * Helper function for creating a new customlayout of the HTML String
	 * @param html
	 * @return
	 * @throws IOException
	 */
	private static CustomLayout htmlToCustomLayout(String html) throws IOException {
		CustomLayout htmlLayout = new CustomLayout(new ByteArrayInputStream(
				(html)
				.getBytes(StandardCharsets.UTF_8)
				));
		htmlLayout.setHeight("100%");
		return(htmlLayout);
	}
	
	/**
	 * Only update HTML if the UI state is set to show the current chapter, otherwise reload from full HTML
	 * @param html
	 */
	private void addStylingAndRefreshHTMLView(){
		String updatedHTML = "";
		
		if(this.chapter instanceof TextChapter) {
			if(this.main.uiState.showFullDocumentHTML) {
				String html = findFullHTML();
				updatedHTML = applyA4PageStyling(html);
			} else {
				String html = findChapterSpecificHTML();
				updatedHTML = applyA4PageStyling(html);
			}
		} else {
			updatedHTML = applyA4PageStyling(this.chapter.html);
		}
		
		refreshHTMLView_(updatedHTML);
	}
	
	private void addStylingAndRefreshHTMLViewFromEditor() {
		String unsafeClientsideContent = this.editor.getValue();
		Integer chapterToOverride = null;
		String updatedHTML = "";
		if(this.chapter instanceof TextChapter) {
			TextChapter textChapter = ((TextChapter)this.chapter);
			chapterToOverride = textChapter.chapterIndex;
			if(this.main.uiState.showFullDocumentHTML) {
				String html = fullHTMLReplaceIndex(chapterToOverride, UtilsDB.markdownToHTML(unsafeClientsideContent, true));
				updatedHTML = applyA4PageStyling(html);
			} else {
				String html = UtilsDB.markdownToHTML(unsafeClientsideContent, true);
				updatedHTML = applyA4PageStyling(html);
			}
		} else {
			//Show it as is without replacements
			String html = UtilsDB.markdownToHTML(unsafeClientsideContent, false);
			updatedHTML = applyA4PageStyling(html);
		}
		refreshHTMLView_(updatedHTML);
	}
	
	/**
	 * Update the current active HTML view
	 */
	private void refreshHTMLView_(String htmlWithoutTagReplacement) {
		try {
			reviewHTML.removeAllComponents();
			//Office office = UtilsDB.getPossibleRelatedOffice(main.getDatabase(), this.chapter);
			String html = Utils.htmlTagReplacement(htmlWithoutTagReplacement, main);
			CustomLayout htmlLayout = htmlToCustomLayout(html);
			reviewHTML.addComponent(htmlLayout);
			reviewHTML.setExpandRatio(htmlLayout, 1.0f);

			if(chapter instanceof TextChapter && main.getUIState().commentLayoutOpen && !main.getUIState().showFullDocumentHTML) {
				js = new TextChapterCommentsJS(main, (TextChapter)chapter);
				Panel jsPanel = jsToPanel();
				reviewHTML.addComponent(jsPanel);
				reviewHTML.setExpandRatio(jsPanel, 0.0f);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Panel jsToPanel() {
		Panel panel = new Panel();
		panel.addStyleName(ValoTheme.PANEL_BORDERLESS);
		panel.setSizeFull();
		panel.setId("textChapterJSPanel" + this.chapter.uuid);
		VerticalLayout panelLayout = new VerticalLayout();
		panelLayout.addComponent(js);

		panel.setContent(panelLayout);
		return panel;
	}
	
	/**
	 * Optional dispose code, to be used with switchChapter
	 */
	private void dispose() {
		//TODO: Add dispose code, if it's needed
	}
	
	public void updateServerSideOnly() {
		updateCachedChapter();
	}
	
	/**
	 * Reload elements
	 */
	public void updateUI(boolean forceTextContentReset) {
		if(forceTextContentReset) {
			updateServerSideOnly();
			updateEditorFromChapterContent();
		}
		updateSizes();
		updateAccordingToAccount();
		updateHideEditorButtonGraphics();
		updateShowFullDocumentButtonGraphics();
	}
	
	// Change the chapter being shown if possible
	private void updateCachedChapter(){
		if(this.chapter != null) {
			List<TextChapter> candidates = TextChapter.enumerate(this.main.getDatabase());
			for(TextChapter candidate : candidates) {
				if(candidate.uuid.equals(this.chapter.uuid)) {
					//System.out.println("Updated cached chapter!");
					this.chapter = candidate;
					break;
				}
			}
		}
	}
	
	public void updateSizes() {
		int availableWidth = this.main.nonCommentToolWidth;
		int editorWidth = (availableWidth)/5 * 2;
		//int editorHeight = ((Page.getCurrent().getBrowserWindowHeight())/10)*8;
		
		if(this.main.uiState.showEditor) {
			left.setWidth(editorWidth, Unit.PIXELS);
		} else {
			left.setWidth("100%");
		}
	}
	
	/**
	 * Initialize the editor, the html view, etc.
	 */
	private void init(MarkdownToHTMLContent content) {		
		this.chapter = content;
		
		//View consists of left and right
		mainLayout = new HorizontalLayout();
		mainLayout.setMargin(new MarginInfo(true, false, false, true));
		mainLayout.setSpacing(false);
		
		left = new VerticalLayout();
		left.setMargin(new MarginInfo(false, true, false, false));
		left.setSpacing(false);
		
		HorizontalLayout rightHorizontalLayout = new HorizontalLayout();
		rightHorizontalLayout.setMargin(new MarginInfo(false, false, false, false));
		rightHorizontalLayout.setSpacing(false);
		left.setSpacing(true);
		
		VerticalLayout rightButtonLayout = new VerticalLayout();
		rightButtonLayout.setSpacing(true);
		rightButtonLayout.setMargin(new MarginInfo(false, false, false, true));
		
		reviewHTML = new VerticalLayout();
		
		//Initialize preview HTML element
		//Full document HTML is found from main
		
		//Prepare the text editor:
		editor = new TextArea(null, chapter.getUnformattedContent());
		editor.setWordwrap(true);
		editor.setWidth("100%");
		editor.setHeight("100%");
		
		//Sizing:
		mainLayout.setHeight("100%");
		left.setHeight("100%");
		rightHorizontalLayout.setHeight("100%");
		
		//Update the cached values and update the main view
		//possiblyRefreshHTMLViewWithChapterHTML(Utils.applyA4PageStyling(chapter.getHTML()));
		addStylingAndRefreshHTMLView();
		
		//Prepare button toolbar and left view:
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		this.updateButton = createUpdateMarkdownButton(editor);
		this.discardChangesButton = createDiscardChangesButton(editor);
		this.previewButton = createPreviewButton(editor);
		this.hideEditorViewButton = createHideEditorViewButton();
		
		buttonToolbar.setWidth("100%");
		buttonToolbar.addComponent(hideEditorViewButton);
		buttonToolbar.addComponent(updateButton);
		buttonToolbar.addComponent(discardChangesButton);
		buttonToolbar.addComponent(previewButton);
		
		buttonToolbar.addStyleName(EXTRA_SMALL_FONT);
		hideEditorViewButton.addStyleName(EXTRA_SMALL_FONT);
		updateButton.addStyleName(EXTRA_SMALL_FONT);
		discardChangesButton.addStyleName(EXTRA_SMALL_FONT);
		previewButton.addStyleName(EXTRA_SMALL_FONT);
		
		buttonToolbar.setComponentAlignment(hideEditorViewButton, Alignment.MIDDLE_LEFT);
		buttonToolbar.setComponentAlignment(updateButton, Alignment.MIDDLE_RIGHT);
		buttonToolbar.setComponentAlignment(discardChangesButton, Alignment.MIDDLE_RIGHT);
		buttonToolbar.setComponentAlignment(previewButton, Alignment.MIDDLE_RIGHT);

		left.addComponent(buttonToolbar);
		left.addComponent(editor);
		left.setExpandRatio(buttonToolbar, 0.0f);
		left.setExpandRatio(editor, 1.0f);
		
		//Add components to right:
		toggleShowFullDocumentButton = createToggleShowFullDocumentHTML();
		toggleShowFullDocumentButton.addStyleName(EXTRA_SMALL_FONT);
		
		openInPDFViewButton = createOpenInPDFViewButton();
		openInPDFViewButton.addStyleName(EXTRA_SMALL_FONT);
		openGuideViewButton = createGuideViewButton();
		openGuideViewButton.addStyleName(EXTRA_SMALL_FONT);
		
		reviewHTML.setHeight("100%");
		
		rightHorizontalLayout.addComponent(reviewHTML);
		rightHorizontalLayout.setExpandRatio(reviewHTML, 1.0f);
		
		//Add button layout to right layout:
		rightButtonLayout.addComponent(openGuideViewButton);
		rightButtonLayout.setExpandRatio(openGuideViewButton, 0.0f);
		rightButtonLayout.setComponentAlignment(openGuideViewButton, Alignment.TOP_LEFT);
		rightButtonLayout.addComponent(toggleShowFullDocumentButton);
		rightButtonLayout.setExpandRatio(toggleShowFullDocumentButton, 0.0f);
		rightButtonLayout.setComponentAlignment(toggleShowFullDocumentButton, Alignment.TOP_LEFT);
		rightButtonLayout.addComponent(openInPDFViewButton);
		rightButtonLayout.setExpandRatio(openInPDFViewButton, 0.0f);
		rightButtonLayout.setComponentAlignment(openInPDFViewButton, Alignment.TOP_LEFT);
		rightHorizontalLayout.addComponent(rightButtonLayout);
		rightHorizontalLayout.setExpandRatio(rightButtonLayout, 0.0f);
		
		//Add left and right views to main
		mainLayout.addComponent(left);
		mainLayout.addComponent(rightHorizontalLayout);
		mainLayout.setComponentAlignment(left, Alignment.TOP_LEFT);
		mainLayout.setComponentAlignment(rightHorizontalLayout, Alignment.TOP_RIGHT);
	}
	
	
	private String getToggleButtonText() {
		if(main.getUIState().showFullDocumentHTML) {
			return Terminology.ONLY_CHAPTER;
		} else {
			return Terminology.ALL;
		}
	}
	
	private Resource getToggleButtonIcon() {
		if(main.getUIState().showFullDocumentHTML) {
			return FontAwesome.MINUS_SQUARE;
		} else {
			return FontAwesome.PLUS_SQUARE;
		}
	}
	
	
	private void updateHideEditorButtonGraphics() {
		this.hideEditorViewButton.setCaption(getHideEditorButtonText());
		this.hideEditorViewButton.setIcon(getHideEditorButtonIcon());
	}
	
	private String getHideEditorButtonText() {
		if(main.getUIState().showEditor) {
			return "(" + Terminology.CLOSE + ")";
		} else {
			return "(" + Terminology.OPEN + ")";
		}
	}
	
	private Resource getHideEditorButtonIcon() {
		if(main.getUIState().showEditor) {
			return FontAwesome.MINUS_SQUARE;
		} else {
			return FontAwesome.PLUS_SQUARE;
		}
	}
	
	private Button createGuideViewButton() {
		Button button = new Button();
		button.setCaption("Apu");
		button.setStyleName(ValoTheme.BUTTON_TINY);
		button.setIcon(FontAwesome.QUESTION_CIRCLE);
		button.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1256739770459600230L;

			@Override
			public void buttonClick(ClickEvent event) {
				main.showGuideViewDialog();
			}
			
		});
		return button;
	}
	
	private Button createOpenInPDFViewButton() {
		Button button = new Button();
		button.setCaption("Tulosta");
		button.setDescription("Avaa PDF n‰kym‰ss‰");
		button.setIcon(FontAwesome.PRINT);
		button.addStyleName(ValoTheme.BUTTON_TINY);
		button.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1256739770459600230L;

			@Override
			public void buttonClick(ClickEvent event) {
				main.showResultAgreementPrintViewOptionsDialog();
			}

		});
		button.setVisible(includeAdvancedToolBar);
		return(button);
	}
	
	private Button createToggleShowFullDocumentHTML() {
		Button button = new Button();
		button.setCaption(getToggleButtonText());
		button.setDescription("N‰yt‰ tai piiloita koko tulossopimuksen teksti.");
		button.setIcon(getToggleButtonIcon());
		button.addStyleName(ValoTheme.BUTTON_TINY);
		button.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -1256739770459600230L;

			@Override
			public void buttonClick(ClickEvent event) {
				boolean old = main.getUIState().showFullDocumentHTML;
				main.getUIState().showFullDocumentHTML = !old;
				addStylingAndRefreshHTMLView();
				updateShowFullDocumentButtonGraphics();
			}

		});
		button.setVisible(includeShowAll);
		return(button);
	}
	
	
	private Button createHideEditorViewButton() {
		Button button = new Button();
		String uniqueButtonIdentifier = "";
		button.setData(uniqueButtonIdentifier);
		button.setDescription("N‰yt‰ tai piilota luvun muutosn‰kym‰.");
		button.addStyleName(ValoTheme.BUTTON_TINY);
		
		button.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1646729650708656100L;

			@Override
			public void buttonClick(ClickEvent event) {
				boolean showEditorNewValue = !ChapterEditorWrapperUI.this.main.uiState.showEditor;
				ChapterEditorWrapperUI.this.main.uiState.showEditor = showEditorNewValue;
				updateUI(false);
			}

		});
		button.setVisible(includeHideEditor);
		return(button);
	}
	
	/**
	 * This will replace the content of the editor with the markdown of the chapter
	 * @param editor
	 * @return
	 */
	private Button createDiscardChangesButton(TextArea editor) {
		Button discardChangesButton = new Button();
		discardChangesButton.setCaption("Hylk‰‰");
		discardChangesButton.setDescription("Hylk‰‰ muutokset. Palauttaa vanhan tekstin.");
		discardChangesButton.setIcon(FontAwesome.REMOVE);
		discardChangesButton.addStyleName(ValoTheme.BUTTON_TINY);
		discardChangesButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4530761622213776107L;

			@Override
			public void buttonClick(ClickEvent event) {
				updateEditorFromChapterContent();
			}

		});
		return(discardChangesButton);
	}
	
	/**
	 * This will update the shared HTML view with the current contents of the editor
	 * @param editor
	 * @return
	 */
	private Button createPreviewButton(TextArea editor) {
		Button previewButton = new Button();
		previewButton.setCaption("Tarkasta");
		previewButton.setDescription("Tarkasta Muotoilu. P‰ivitt‰‰ n‰kym‰n.");
		previewButton.setIcon(FontAwesome.LONG_ARROW_RIGHT);
		previewButton.addStyleName(ValoTheme.BUTTON_TINY);
		previewButton.addClickListener(new ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -2814741085016153138L;

			@Override
			public void buttonClick(ClickEvent event) {
				updateHTMLFromEditor(true);
			}

		});
		return(previewButton);
	}
	
	/**
	 * This will update the current chapter's markdown and attempt to refresh the shared HTML view.
	 * @param editor
	 * @return
	 */
	private Button createUpdateMarkdownButton(TextArea editor) {
		Button updateButton = new Button();
		updateButton.setCaption("Tallenna");
		updateButton.setDescription("Tallenna Muutokset. P‰ivitt‰‰ luvun tekstin.");
		updateButton.setIcon(FontAwesome.SAVE);
		updateButton.addStyleName(ValoTheme.BUTTON_TINY);
		updateButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -3658624103021558109L;

			@Override
			public void buttonClick(ClickEvent event) {
				ResultAgreementDocuments doc = ResultAgreementDocuments.getInstance(main.getDatabase());
				if(doc != null) {
					if(Utils.canWrite(main, doc)) {
						updateChapterContentFromEditor();
					}
				} else {
					System.err.println("FATAL ERROR - No document selected, but update markdown button clicked!");
				}
			}
		});
		return(updateButton);
	}
	
	/**
	 * Helper function for updating the HTML view with the contents of the editor
	 * @param force - updates the current button toggler and uistate to only show the current chapter
	 */
	private void updateHTMLFromEditor(boolean force) {
		addStylingAndRefreshHTMLViewFromEditor();
	}
	
	
	private void updateShowFullDocumentButtonGraphics() {
		this.toggleShowFullDocumentButton.setCaption(getToggleButtonText());
		this.toggleShowFullDocumentButton.setIcon(getToggleButtonIcon());
	}
	
	
	/**
	 * Helper function for changing the editor's text by the chapter's markdown content and refresh HTML
	 */
	private void updateEditorFromChapterContent() {
		String markdown = chapter.getUnformattedContent();
		editor.setValue(markdown);
		addStylingAndRefreshHTMLView();
	}
	
	/**
	 * Helper function for updating the chapter's internal markdown with the contents of the editor,
	 * and refreshing the HTML view.
	 */
	private void updateChapterContentFromEditor() {
		String unsafeClientsideContent = editor.getValue();
		//Only update DB if necessary
		if(!unsafeClientsideContent.equals(chapter.getUnformattedContent())){
			boolean ok = false;
			if(this.chapter instanceof TextChapter) {
				ok = ((TextChapter)chapter).attemptSetContent(unsafeClientsideContent);
			} else {
				ok = chapter.attemptSetContent(unsafeClientsideContent);
			}
			
			if(ok) {
				Updates.update(main, true);
				addStylingAndRefreshHTMLView();
			} else {
				//TODO: Show to user that save failed
				System.err.println("Failed to validate markdown - database not updated with new value!");
			}
		}
	}
	
	private boolean canWrite() {
		Database database = main.getDatabase();
		
		if(main.getAccountDefault().isAdmin(database)) {
			return true;
		}
		
		// AccessRight check ends here not non-TextChapters. Read-Only mode
		if(! (this.chapter instanceof TextChapter)) {
			return false;
		}
		
		ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(database);
		if(docs == null) {
			return false; //Cannot write if docs is null
		}
		
		// System admins and ResultAgreementDocument write-admins can edit all
		if(ResultAgreementToolAdminBase.canWrite(database, main.getAccountDefault())) {
			return true;
		}
		
		// User does not have any write access to this document anyway - read only
		if(! Utils.canWrite(main, docs)) {
			return false;
		}
		
		Account account = main.getAccountDefault();
		Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		
		// Accounts can only edit their own office's ResultAgreementDocument, unless they are admins
		// Thus, a null office means they are not allowed to edit anything,
		// despite having "write" right to the current ResultAgreementDocuments
		if(office == null) {
			return false;
		}
		
		
		// Shared chapters should only be able to be edited by ResultAgreementDocument admins
		
		TextChapter chapter = (TextChapter)this.chapter;
		
		Office chapterOffice = UtilsDB.getTextChaptersPossibleRelatedOffice(database, chapter);
		if(chapterOffice != null && office.uuid.equals(chapterOffice.uuid)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Hide/Show and enable components based on account and access right
	 */
	private void updateAccordingToAccount() {

		boolean writeOK = canWrite();
		
		//This button is unique and is always visible and enabled if account has write permissions.
		//The other fields are enabled and visible only if state shows the editor
		left.setEnabled(writeOK);
		left.setVisible(writeOK);
		
		hideEditorViewButton.setEnabled(includeHideEditor && writeOK);
		hideEditorViewButton.setVisible(includeHideEditor && writeOK);
		
		boolean showEditor = this.main.uiState.showEditor;
		boolean visible = writeOK && showEditor; //Only show if account has write permissions and the UI state is set to show
		
		updateButton.setEnabled(visible);
		updateButton.setVisible(visible);
		
		discardChangesButton.setEnabled(visible);
		discardChangesButton.setVisible(visible);
		
		previewButton.setEnabled(visible);
		previewButton.setVisible(visible);
		
		editor.setEnabled(visible);
		editor.setVisible(visible);
		
		openGuideViewButton.setEnabled(includeAdvancedToolBar && visible);
		openGuideViewButton.setVisible(includeAdvancedToolBar && visible);
	}
	
	private String fullHTMLReplaceIndex(int index, String replacementHTML) {
		Office office = this.main.uiState.getCurrentOffice();
		if(office != null) {
			ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
			if(docs != null) {
				List<String> chapterHtmls = docs.pureChapterHTMLs(main.getDatabase(), office);
				String resultHTML = "";
				for(int i = 0; i < chapterHtmls.size(); i++) {
					if(i == index) {
						resultHTML += replacementHTML;
					} else {
						resultHTML += chapterHtmls.get(i);
					}
				}
				return(resultHTML);
			} else {
				return("Error - No connected ResultAgreementDocuments!");
			}
		} else {
			return("Virastoa ei valittuna. Dokumentin teksti‰ ei voida n‰ytt‰‰. ID: 602");
		}
	}
	
	private String findFullHTML() {
		Office office = this.main.uiState.getCurrentOffice();
		if(office != null) {
			ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
			if(docs != null) {
				String html = docs.toHTML(main.getDatabase(), office);
				return(html);
			} else {
				return("Error - No connected ResultAgreementDocuments!");
			}
		} else {
			return("Virastoa ei valittuna. Dokumentin teksti‰ ei voida n‰ytt‰‰. ID: 601");
		}
	}
	
	private String findChapterSpecificHTML() {
		Office office = this.main.uiState.getCurrentOffice();
		if(office != null) {
			try {
				ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
				if(docs != null) {
					if(this.chapter instanceof TextChapter) {
						String html = docs.chapterHTML(main.getDatabase(), ((TextChapter)this.chapter).chapterIndex, office.uuid);
						return(html);
					} else {
						System.err.println("FATAL ERROR: Attempted to find the chapter HTML for a non-TextChapter object in editor UI!");
						return "(virhe - kappaleen teksti‰ ei voitu hakea!)";
					}
				} else {
					return("Error - No connected ResultAgreementDocuments!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "Failed to find HTML for Chapter!";
			}
		} else {
			return("Virastoa ei valittuna. Dokumentin teksti‰ ei voida n‰ytt‰‰. ID: 600");
		}
	}
	
	public static String applyA4PageStyling(String html) {
		int height = ((Page.getCurrent().getBrowserWindowHeight())/10)*8; //overflow: auto;
		return "<div class=\""+ CustomCSSStyleNames.STYLE_A4_PAGE + "\" style=\"min-height: "+height+"px; margin-top: 0px; height: "+height+"px;\">" + html + "</div>";
	}
	
}
