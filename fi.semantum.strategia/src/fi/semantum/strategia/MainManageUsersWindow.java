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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.configurations.CustomCSSStyleNames;
import fi.semantum.strategy.db.AccessRight;
import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.AccountGroup;
import fi.semantum.strategy.db.CommentToolAdminBase;
import fi.semantum.strategy.db.CommentToolBase;
import fi.semantum.strategy.db.DBConfiguration;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.DatabaseManager;
import fi.semantum.strategy.db.Linkki;
import fi.semantum.strategy.db.MapRight;
import fi.semantum.strategy.db.Office;
import fi.semantum.strategy.db.ResultAgreementConfiguration;
import fi.semantum.strategy.db.ResultAgreementDocuments;
import fi.semantum.strategy.db.ResultAgreementToolAdminBase;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.TimeConfiguration;
import fi.semantum.strategy.db.TimeInterval;
import fi.semantum.strategy.db.UtilsDB;

public class MainManageUsersWindow {
	
	public static void createAndOpen(final Main main) {

		final Database database = main.getDatabase();
		
		HorizontalLayout root = new HorizontalLayout();
		root.setSizeFull();
		
        VerticalLayout accountLayout = new VerticalLayout();
        accountLayout.setSizeFull();
        accountLayout.setSpacing(true);
        
        VerticalLayout accountGroupLayout = new VerticalLayout();
        accountGroupLayout.setSizeFull();
        accountGroupLayout.setSpacing(true);
        
        VerticalLayout mapsSettingsLayout = new VerticalLayout();
        mapsSettingsLayout.setSizeFull();
        mapsSettingsLayout.setSpacing(true);
        
        VerticalLayout resultAgreementLayout = new VerticalLayout();
        resultAgreementLayout.setSizeFull();
        resultAgreementLayout.setSpacing(true);
        
        VerticalLayout backupLayout = new VerticalLayout();
        backupLayout.setSizeFull();
        backupLayout.setSpacing(true);

        VerticalLayout officeLayout = new VerticalLayout();
        officeLayout.setSizeFull();
        officeLayout.setSpacing(true);
        
		TabSheet sheet = new TabSheet();
		root.addComponent(sheet);
        sheet.setSizeFull();
        
        sheet.addTab(accountLayout, "K‰ytt‰j‰t");
        sheet.addTab(accountGroupLayout, "Ryhm‰t");
        sheet.addTab(officeLayout, "Virastot / Osastot");
        sheet.addTab(mapsSettingsLayout, "Kartat");
        sheet.addTab(resultAgreementLayout, "Tulossopimukset");
        sheet.addTab(backupLayout, "Tietokannat");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false);
        
        //Initial tab is at 0:
        createAndAttachAccountLayout(main, database, accountLayout);
        
        sheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {

			private static final long serialVersionUID = 8211904117926300238L;

			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				int index = event.getTabSheet().getTabPosition(event.getTabSheet().getTab(event.getTabSheet().getSelectedTab()));
				switch(index) {
				case 0: {
					createAndAttachAccountLayout(main, database, accountLayout); //K‰ytt‰j‰t
					break;
				}
				case 1: {
					createAndAttachAccountGroupLayout(main, database, accountGroupLayout); //Ryhm‰t
					break;
				}
				case 2: {
					createAndAttachOfficeLayout(main, database, officeLayout); //Virastot / Osastot
					break;
				}
				case 3: {
					createAndAttachMapSettings(main, database, mapsSettingsLayout); //Kartat
					break;
				}
				case 4: {
					createAndAttachResultAgreementSettings(main, database, resultAgreementLayout); //Tulossopimukset
					break;
				}
				case 5: {
					createAndAttachBackupLayout(main, database, backupLayout); //Tietokannat
					break;
				}
				}
			}
        	
        });
        
        Dialogs.makeDialog(main, main.dialogWidth(), main.dialogHeight(0.9), "Hallinnoi strategiakarttaa", "Sulje", root, buttons);

	}
	
	private static void populateOfficeTable(Database database, Map<String,Office> officeMap, Table officeTable) {
		List<Office> offices = Office.enumerate(database, true, true);
		int tableIndex = 0;
		for(Office office : offices) {
			tableIndex++;
			officeMap.put("" + tableIndex, office);
			
			AccountGroup possibleGroup = UtilsDB.getPossibleAccountGroupByOffice(database, office);
			officeTable.addItem(new Object[] {
					office.getText(database),
					possibleGroup != null ? possibleGroup.text : "(ei k‰ytt‰j‰ ryhm‰‰)"
				},
				tableIndex);
		}
	}

	private static class BackupTreeItem {
		
		public String label;
		public File file;
		
		public BackupTreeItem(String label, File file) {
			this.label = label;
			this.file = file;
		}
		
		@Override
		public String toString() {
			return label;
		}
		
	}
	
	/**
	 * Meant for cases where database is corrupted and unreadable. Allow admin users to login and rollback to a working version.
	 * @return
	 */
	public static HorizontalLayout createEmergencyAdminDatabaseBackupView(VaadinRequest request, Main main, Database database, Throwable originalError) {
		Path currentDatabasePath = database.getDatabaseFile().toPath();
		
		Label errorLabel = new Label();
		String errorMessage = "<h1>Virheen Syy:</h1>";
		for(StackTraceElement ste : originalError.getStackTrace()) {
			errorMessage += "</br>" + ste.toString();
		}
		errorLabel.setValue(errorMessage);
		errorLabel.setContentMode(ContentMode.HTML);
		
		Tree tree = initializeDatabaseTree(database.getDatabaseDirectory().toPath());
		tree.setWidth("500px");
		
		Button apply = new Button("Palaa valittuun tietokantaan");
		apply.setEnabled(false);
		apply.setStyleName(ValoTheme.BUTTON_TINY);
		apply.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -2011067363997787687L;

			@Override
			public void buttonClick(ClickEvent event) {
				BackupTreeItem item = (BackupTreeItem)tree.getValue();
				if(item != null && item.file != null) {
					try {
						System.out.println("Restoring previous database with emergency mechanism");
						restoreSelectedDatabase(item.file.toPath(), currentDatabasePath);
						
						//Refresh entire session:
						UI.getCurrent().getSession().close();
						UI.getCurrent().getSession().getSession().invalidate();
						Page.getCurrent().reload();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 8702025633899260432L;

			@Override
			public void itemClick(ItemClickEvent event) {
				
				BackupTreeItem item = (BackupTreeItem)event.getItemId();
				if(item.file != null) {
					apply.setEnabled(true);
				} else {
					apply.setEnabled(false);
				}
				
			}
			
		});
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		hl.setSpacing(true);
		hl.setMargin(true);
		hl.setSizeFull();
		
		VerticalLayout leftLayout = new VerticalLayout();
		
		hl.setSizeFull();
		leftLayout.setSpacing(true);
		leftLayout.setSizeFull();
		
		leftLayout.addComponent(apply);
		leftLayout.setExpandRatio(apply, 0.0f);
		leftLayout.addComponent(tree);
		leftLayout.setExpandRatio(tree, 1.0f);
		
		hl.addComponent(leftLayout);
		hl.setExpandRatio(leftLayout, 0.4f);
		hl.addComponent(errorLabel);
		hl.setExpandRatio(errorLabel, 0.6f);
		return hl;
	}
	
	/**
	 * database.getDatabaseDirectory().toPath()
	 * @param databasePath
	 * @return
	 */
	private static Tree initializeDatabaseTree(Path databasePath) {
		Tree tree = new Tree();
		tree.addStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		
		BackupTreeItem today = new BackupTreeItem("T‰n‰‰n", null);
		tree.addItem(today);

		for(Path file : DatabaseManager.collectBackupFiles(databasePath)) {
			String label;
			try {
				label = UtilsDB.clockDateFormat.format(DatabaseManager.backupFileDate(file));
			} catch (NullPointerException e) {
				label = file.toString();
				System.err.println("Failed to parse database file name - incorrect format! Showing as full path name instead!");
				System.err.println(file + " caused " + e.toString());
			}
			BackupTreeItem fileItem = new BackupTreeItem(label, file.toFile());
			tree.addItem(fileItem);
			tree.setParent(fileItem, today);
			tree.setChildrenAllowed(fileItem, false);

		}
		
		for(Path hourly : DatabaseManager.collectHourlyDirectories(databasePath)) {

			String l = UtilsDB.dayDateFormat.format(DatabaseManager.hourlyDirectoryDate(hourly));

			BackupTreeItem item = new BackupTreeItem(l, null);
			tree.addItem(item);
			
			for(Path file : DatabaseManager.collectBackupFiles(hourly)) {

				String label = UtilsDB.clockDateFormat.format(DatabaseManager.backupFileDate(file));

				BackupTreeItem fileItem = new BackupTreeItem(label, file.toFile());
				tree.addItem(fileItem);
				tree.setParent(fileItem, item);
				tree.setChildrenAllowed(fileItem, false);

			}

		}
		
		return tree;
	}
	
	private static void restoreSelectedDatabase(Path selectedDatabasePath, Path currentDatabasePath) throws IOException {
		// Restore selected
		java.nio.file.Files.copy(
				selectedDatabasePath,
				currentDatabasePath, 
				java.nio.file.StandardCopyOption.REPLACE_EXISTING,
				java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
				java.nio.file.LinkOption.NOFOLLOW_LINKS );
	}
	
	private static void createAndAttachBackupLayout(Main main, Database database, VerticalLayout targetRoot) {

		targetRoot.removeAllComponents();

		HorizontalLayout hl = new HorizontalLayout();
		HorizontalLayout advancedToolbar = new HorizontalLayout();
		
		final Button apply = new Button("Palauta nykyinen tietokanta valittuun versioon");
		apply.setStyleName(ValoTheme.BUTTON_TINY);
		apply.setEnabled(false);
		apply.setHeightUndefined();
		hl.addComponent(apply);
		hl.setExpandRatio(apply, 0);
		
		Label advancedToolbarLabel = new Label();
		advancedToolbarLabel.setValue("Uusi tietokanta:");
		
		advancedToolbar.addComponent(advancedToolbarLabel);
		advancedToolbar.setExpandRatio(advancedToolbarLabel, 0.0f);
		advancedToolbar.setComponentAlignment(advancedToolbarLabel, Alignment.BOTTOM_LEFT);
		
		final TextField name = new TextField();
		name.setCaption("Anna uuden tietokannan nimi:");
		name.setStyleName(ValoTheme.TEXTFIELD_TINY);
		name.setWidth("500px");
		advancedToolbar.addComponent(name);
		
		final Button copy = new Button("Kopioi nykyinen tietokanta uuteen tietokantaan");
		copy.setStyleName(ValoTheme.BUTTON_TINY);
		copy.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = 3854697418811685539L;

			@Override
			public void buttonClick(ClickEvent event) {
				
				String databaseName = name.getValue();
				if(!DatabaseManager.isDatabase(databaseName)) {
					DatabaseManager.copyDatabase(database.getDatabaseId(), databaseName);
				}
				
			}
			
		});

		name.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -9160910144689419696L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				String databaseName = name.getValue();
				if(DatabaseManager.isDatabase(databaseName)) {
					copy.setEnabled(false);
				} else {
					copy.setEnabled(true);
				}
				
			}
		});

		copy.setEnabled(false);
		copy.setHeightUndefined();
		advancedToolbar.addComponent(copy);
		advancedToolbar.setComponentAlignment(copy, Alignment.BOTTOM_LEFT);
		
		advancedToolbar.setMargin(new MarginInfo(true, false, false, true));
		advancedToolbar.setSpacing(true);
		targetRoot.addComponent(advancedToolbar);
		targetRoot.setExpandRatio(advancedToolbar, 0.0f);
		
		hl.setSpacing(true);
		targetRoot.addComponent(hl);
		targetRoot.setExpandRatio(hl, 0.0f);
		
		Tree tree = initializeDatabaseTree(database.getDatabaseDirectory().toPath());
		
		apply.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -2011067363997787687L;

			@Override
			public void buttonClick(ClickEvent event) {
				BackupTreeItem item = (BackupTreeItem)tree.getValue();
				if(item != null && item.file != null) {
					try {
						Path databasePath = database.getDatabaseFile().toPath();
						// Backup current
						database.backupCurrentDatabase();
						//Restore selection
						restoreSelectedDatabase(item.file.toPath(), databasePath);
						database.getDatabaseFile().setLastModified(database.now().getTime());
						main.pollDatabase();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});

		
		tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 8702025633899260432L;

			@Override
			public void itemClick(ItemClickEvent event) {
				
				BackupTreeItem item = (BackupTreeItem)event.getItemId();
				if(item.file != null) {
					apply.setEnabled(true);
				} else {
					apply.setEnabled(false);
				}
				
			}
			
		});
		
		tree.setSizeFull();
		
		targetRoot.addComponent(tree);
		targetRoot.setExpandRatio(tree, 1);
		
	}

	private static void createAndAttachOfficeLayout(Main main, Database database, VerticalLayout targetRoot) {
		targetRoot.removeAllComponents();
		
		final Map<String,Office> officeMap = new HashMap<String,Office>();
		
		final Table officeTable = new Table();
		officeTable.setSelectable(true);
		officeTable.setMultiSelect(true);
		officeTable.addStyleName(ValoTheme.TABLE_SMALL);
		officeTable.addStyleName(ValoTheme.TABLE_SMALL);
		officeTable.addStyleName(ValoTheme.TABLE_COMPACT);
		
		officeTable.addContainerProperty("Virasto / Osasto", String.class, null);
		officeTable.addContainerProperty("K‰ytt‰j‰ryhm‰", String.class, null);
		
		populateOfficeTable(database, officeMap, officeTable);
		
		officeTable.setWidth("100%");
		officeTable.setHeight("100%");;
		officeTable.setSelectable(true);
		officeTable.setMultiSelect(false);
		
		final Button removeOfficeButton = new Button("Poista valittu virasto kokonaan, sek‰ sen tulossopimus (ei poista kartta elementtej‰).", new Button.ClickListener() {

			private static final long serialVersionUID = -5359199320445328801L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					Object selection = officeTable.getValue();
					if(selection != null) {
						Office officeToRemove = officeMap.get(selection.toString());
						if(officeToRemove != null && !officeToRemove.isController) {
							UtilsDB.removeAllAssociatedObjectsByOffice(database, officeToRemove);
							officeToRemove.remove(database);
							
							//Update table:
							officeMap.clear();
							officeTable.removeAllItems();
							populateOfficeTable(database, officeMap, officeTable);
							Updates.update(main, true);
						} else {
							System.err.println("Attempted to remove null or controller office in removeOfficeButton call!");
						}
					}
				}
			}

		});
		
		removeOfficeButton.setDescription("Karttaan j‰‰ mahdolliset alikartat, ja kommentit jotka ovat tehty t‰ll‰ virastolla pysyv‰t kannassa.");
		
		removeOfficeButton.setEnabled(officeTable.getValue() != null);
		
		officeTable.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 4548562933276733885L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(officeTable.getValue() != null) {
					Office officeToRemove = officeMap.get(officeTable.getValue().toString());
					if(officeToRemove != null && !officeToRemove.isController) {
						removeOfficeButton.setEnabled(true);
					} else {
						removeOfficeButton.setEnabled(false);
					}
				} else {
					removeOfficeButton.setEnabled(false);
				}
			}
			
		});
		
		removeOfficeButton.setIcon(FontAwesome.TRASH);
		removeOfficeButton.addStyleName(ValoTheme.BUTTON_TINY);
		
		HorizontalLayout buttonToolbar = new HorizontalLayout();
		buttonToolbar.addComponent(removeOfficeButton);
		
		targetRoot.addComponent(officeTable);
		targetRoot.setExpandRatio(officeTable, 1.0f);
		targetRoot.addComponent(buttonToolbar);
		targetRoot.setExpandRatio(buttonToolbar, 0.0f);
	}
	
	private static void updateOfficesListSelect(Database database, ListSelect possibleOfficeSelection) {
		List<Office> offices = Office.enumerate(database, true, true);
		for(Office office : offices) {
			possibleOfficeSelection.addItem(office.getText(database));
		}
	}
	
	private static void createAndAttachAccountLayout(Main main, Database database, VerticalLayout targetRoot) {
		targetRoot.removeAllComponents();
		
        HorizontalLayout topHorizontalLayout = new HorizontalLayout();
        topHorizontalLayout.setSpacing(true);
        topHorizontalLayout.setWidth("100%");
		
		final ComboBox users = new ComboBox();
		users.setWidth("100%");
		users.setNullSelectionAllowed(false);
        users.addStyleName(ValoTheme.COMBOBOX_TINY);
        users.setCaption("Valitse k‰ytt‰j‰:");
        
		final Map<String,Account> accountMap = new HashMap<String,Account>();
        makeAccountCombo(main, accountMap, users);

		for(Account a : Account.enumerate(database)) {
			accountMap.put(a.text, a);
			users.addItem(a.text);
			users.select(a.text);
		}


        final Table accountGroupsTable = new Table();
        accountGroupsTable.setSelectable(true);
        accountGroupsTable.setMultiSelect(true);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_SMALL);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_SMALL);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_COMPACT);

        final TextField newAccountNameField = new TextField();
        
        Validator nameValidator = new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				if(s.isEmpty())
					throw new InvalidValueException("Nimi ei saa olla tyhj‰");
				if(accountMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo k‰ytˆss‰");
			}
			
		};
		
		final ComboBox newGroupSelect = new ComboBox();
		newGroupSelect.setWidth("100%");
		newGroupSelect.setNullSelectionAllowed(false);
		newGroupSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		newGroupSelect.setCaption("Valitse ryhm‰:");
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Define the layout and popup view for editing which office the account belong to
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        VerticalLayout associatedOfficeLayout = new VerticalLayout();
        associatedOfficeLayout.setWidth("300px");
        associatedOfficeLayout.setHeight("300px");
        
        ListSelect possibleOfficeSelection = new ListSelect();
        possibleOfficeSelection.setSizeFull();
        possibleOfficeSelection.setNullSelectionAllowed(true);
        possibleOfficeSelection.setMultiSelect(false);
        
        PopupView changeAssociatedOfficePopupView = new PopupView("", associatedOfficeLayout);
        changeAssociatedOfficePopupView.setHideOnMouseOut(false);
        
        Button closeAssociatedOfficePopupViewButton = new Button("Sulje", new Button.ClickListener() {

			private static final long serialVersionUID = 738934367900825318L;

			public void buttonClick(ClickEvent event) {
				changeAssociatedOfficePopupView.setPopupVisible(false);
			}

		});
        
        
        Button changeAssociatedOfficeButton = new Button("Tallenna valinta", new Button.ClickListener() {

			private static final long serialVersionUID = 5550419086384568977L;

			public void buttonClick(ClickEvent event) {
				Object officeSelection = possibleOfficeSelection.getValue();
				Object userSelection = users.getValue();
				
				if(officeSelection != null) {
					String officeText = officeSelection.toString();
					
					List<Office> availableOffices = Office.enumerate(database, true, true);
					Office selectedOffice = null;
					for(Office possibleOffice : availableOffices) {
						if(possibleOffice.getText(database).equals(officeText)) {
							selectedOffice = possibleOffice;
							break;
							
						}
					}
					
					if(selectedOffice != null && userSelection != null) {
						Account selectedAccount = accountMap.get(userSelection);
						System.out.println("Changing associated office for account [" + selectedAccount.text + "] to [" + officeText + "]");
						UtilsDB.associateAccountWithOffice(database, selectedOffice, selectedAccount);
					} else {
						System.err.println("Office or account is null, cannot change the associated Office for Account!");
					}
				} else {
					//Remove from all offices
					if(userSelection != null) {
						Account selectedAccount = accountMap.get(userSelection);
						System.out.println("Removing all associtated offices from account " + selectedAccount.text);
						UtilsDB.disassociateAccountFromAllOffices(database, selectedAccount);
					}
				}
				Updates.update(main, true);
				updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
				changeAssociatedOfficePopupView.setPopupVisible(false);
			}

		});
        changeAssociatedOfficeButton.setStyleName(ValoTheme.BUTTON_TINY);
        changeAssociatedOfficeButton.setIcon(FontAwesome.SAVE);
        
        closeAssociatedOfficePopupViewButton.setStyleName(ValoTheme.BUTTON_TINY);
        closeAssociatedOfficePopupViewButton.setIcon(FontAwesome.CLOSE);
        
        HorizontalLayout associatedOfficeLayoutButtons = new HorizontalLayout();
        associatedOfficeLayoutButtons.setSizeFull();
        associatedOfficeLayoutButtons.addComponent(closeAssociatedOfficePopupViewButton);
        associatedOfficeLayoutButtons.setExpandRatio(closeAssociatedOfficePopupViewButton, 0.2f);
        associatedOfficeLayoutButtons.setComponentAlignment(closeAssociatedOfficePopupViewButton, Alignment.BOTTOM_LEFT);
        associatedOfficeLayoutButtons.addComponent(changeAssociatedOfficeButton);
        associatedOfficeLayoutButtons.setExpandRatio(changeAssociatedOfficeButton, 0.2f);
        associatedOfficeLayoutButtons.setComponentAlignment(changeAssociatedOfficeButton, Alignment.BOTTOM_RIGHT);
        
        associatedOfficeLayout.addComponent(possibleOfficeSelection);
        associatedOfficeLayout.setExpandRatio(possibleOfficeSelection, 0.85f);
        associatedOfficeLayout.addComponent(associatedOfficeLayoutButtons);
        associatedOfficeLayout.setExpandRatio(associatedOfficeLayoutButtons, 0.15f);

        final Button openAssociatedOfficePopupViewButton = new Button("Muokkaa Osastoa", new Button.ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = -1305479534262982230L;

			public void buttonClick(ClickEvent event) {
				updateOfficesListSelect(database, possibleOfficeSelection);
				changeAssociatedOfficePopupView.setPopupVisible(true);
			}

		});
        openAssociatedOfficePopupViewButton.setIcon(FontAwesome.EDIT);
        openAssociatedOfficePopupViewButton.setDescription("Muokkaa k‰ytt‰j‰n osastoa");
        openAssociatedOfficePopupViewButton.addStyleName(ValoTheme.BUTTON_TINY);
        changeAssociatedOfficeButton.addStyleName(ValoTheme.BUTTON_TINY);
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////////        
        // End the definition of the Office changing popup view
        /////////////////////////////////////////////////////////////////////////////////////////////////////////     
		
		final Button createNewUserButton = new Button("Luo", new Button.ClickListener() {

			private static final long serialVersionUID = -6053708137324681886L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					if(!newAccountNameField.isValid()) return;
					
					String pass = Long.toString(Math.abs( UUID.randomUUID().getLeastSignificantBits()), 36);
					
					Account.create(database, newAccountNameField.getValue(), "", UtilsDB.hash(pass), database.guestGroup);
	
					Updates.update(main, true);
	
			        makeAccountCombo(main, accountMap, users);
			        updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
	
			        Dialogs.infoDialog(main, "Uusi k‰ytt‰j‰ '" + newAccountNameField.getValue() + "' luotu", "K‰ytt‰j‰n salasana on " + pass + "", null);
				}

			}

		});
		createNewUserButton.setIcon(FontAwesome.USER_PLUS);
		createNewUserButton.addStyleName(ValoTheme.BUTTON_TINY);

		final Button removeUserButton = new Button("Poista", new Button.ClickListener() {

			private static final long serialVersionUID = -5359199320445328801L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					Object selection = users.getValue();
					Account state = accountMap.get(selection);
					
					// Default admin cannot be removed
					if(DBConfiguration.getADMIN_ACCOUNT_NAME().equals(state.text)) return;
					
					state.remove(database);
	
					Updates.update(main, true);
	
					makeAccountCombo(main, accountMap, users);
					updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
				}
			}

		});
		
		removeUserButton.setIcon(FontAwesome.TRASH);
		removeUserButton.addStyleName(ValoTheme.BUTTON_TINY);
		
		newAccountNameField.setWidth("100%");
		newAccountNameField.addStyleName(ValoTheme.TEXTFIELD_TINY);
		newAccountNameField.setCaption("Luo uusi k‰ytt‰j‰ nimell‰:");
		newAccountNameField.setValue(Utils.findFreshUserName(nameValidator));
		newAccountNameField.setCursorPosition(newAccountNameField.getValue().length());
		newAccountNameField.setValidationVisible(true);
		newAccountNameField.setInvalidCommitted(true);
		newAccountNameField.setImmediate(true);
		newAccountNameField.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				newAccountNameField.setValue(event.getText());
				try {
					newAccountNameField.validate();
				} catch (InvalidValueException e) {
					createNewUserButton.setEnabled(false);
					return;
				}
				createNewUserButton.setEnabled(true);
			}
			
		});
		newAccountNameField.addValidator(nameValidator);
		if(!newAccountNameField.isValid()) createNewUserButton.setEnabled(false);
		
		topHorizontalLayout.addComponent(users);
		topHorizontalLayout.setExpandRatio(users, 1.0f);
		topHorizontalLayout.setComponentAlignment(users, Alignment.BOTTOM_CENTER);

		topHorizontalLayout.addComponent(changeAssociatedOfficePopupView);
		topHorizontalLayout.addComponent(openAssociatedOfficePopupViewButton);
		topHorizontalLayout.setExpandRatio(openAssociatedOfficePopupViewButton, 0.0f);
		topHorizontalLayout.setComponentAlignment(openAssociatedOfficePopupViewButton, Alignment.BOTTOM_CENTER);
		
		topHorizontalLayout.addComponent(removeUserButton);
		topHorizontalLayout.setExpandRatio(removeUserButton, 0.0f);
		topHorizontalLayout.setComponentAlignment(removeUserButton, Alignment.BOTTOM_CENTER);
		
		topHorizontalLayout.addComponent(newAccountNameField);
		topHorizontalLayout.setExpandRatio(newAccountNameField, 1.0f);
		topHorizontalLayout.setComponentAlignment(newAccountNameField, Alignment.BOTTOM_CENTER);
		
		topHorizontalLayout.addComponent(createNewUserButton);
		topHorizontalLayout.setExpandRatio(createNewUserButton, 0.0f);
		topHorizontalLayout.setComponentAlignment(createNewUserButton, Alignment.BOTTOM_CENTER);

		targetRoot.addComponent(topHorizontalLayout);
		targetRoot.setExpandRatio(topHorizontalLayout, 0.0f);

		accountGroupsTable.addContainerProperty(USER_GROUPS, String.class, null);
        
		accountGroupsTable.setWidth("100%");
		accountGroupsTable.setHeight("100%");
		accountGroupsTable.setNullSelectionAllowed(true);
		accountGroupsTable.setMultiSelect(false);

		updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
		
        targetRoot.addComponent(accountGroupsTable);
        targetRoot.setExpandRatio(accountGroupsTable, 1.0f);
        
		final Button removeFromGroupButton = new Button("Poista valitusta ryhm‰st‰", new Button.ClickListener() {

			private static final long serialVersionUID = 4699670345358079045L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					Object user = users.getValue();
					Account selectedAccount = accountMap.get(user);
					if(selectedAccount != null) {
						Object selection = accountGroupsTable.getValue();
						if(selection != null) {
							String groupName = (String)accountGroupsTable.getContainerProperty((Integer)selection, USER_GROUPS).getValue();
							if(groupName != null) {
								for(AccountGroup group : AccountGroup.enumerate(database)) {
									if(group.text.equals(groupName)){
										System.out.println("Found matching group to remove " + selectedAccount.text + " from. Removing user to group " + group.text);
										selectedAccount.denyAccountGroup(database, group);
										Updates.update(main, true);
										updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
										break;
									}
								}
							} else {
								System.err.println("Cannot remove null group!");
							}
						} else {
							System.err.println("Selection for group removal is null!");
						}
					}
				}
			}

		});
		removeFromGroupButton.setEnabled(false);
		removeFromGroupButton.setIcon(FontAwesome.TRASH);
		
		removeFromGroupButton.addStyleName(ValoTheme.BUTTON_TINY);
        users.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 5036991262418844060L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				users.removeValueChangeListener(this);
		        makeAccountCombo(main, accountMap, users);
		        updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);
		        users.addValueChangeListener(this);
			}
			
		});
		
		final Button addtoGroupButton = new Button("Lis‰‰ ryhm‰‰n", new Button.ClickListener() {

			private static final long serialVersionUID = -4841787792917761055L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					Object selectedUser = users.getValue();
					Account selectedAccount = accountMap.get(selectedUser);
					if(selectedAccount != null) {
						Object newGroup = newGroupSelect.getValue();
						if(newGroup != null) {
							System.out.println("Should update group here!");
							for(AccountGroup group : AccountGroup.enumerate(database)) {
								if(group.uuid.equals(newGroup.toString())){
									System.out.println("Found matching group to add. Adding user to group " + group.text);
									selectedAccount.addAccountGroup(database, group);
									Updates.update(main, true);
									updateSelectedAccountView(database, users, accountMap, accountGroupsTable, newGroupSelect);	
									break;
								}
							}
						}
					}
				}
			}

		});
		
		addtoGroupButton.setIcon(FontAwesome.USERS);
		addtoGroupButton.addStyleName(ValoTheme.BUTTON_TINY);

		accountGroupsTable.addValueChangeListener(new ValueChangeListener() {
			
			private static final long serialVersionUID = 6439090862804667322L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object selection = accountGroupsTable.getValue();
				removeFromGroupButton.setEnabled(selection != null);
			}
			
		});
        
        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setSpacing(true);
        hl2.setWidth("100%");

        hl2.addComponent(removeFromGroupButton);
        hl2.setComponentAlignment(removeFromGroupButton, Alignment.BOTTOM_LEFT);
        hl2.setExpandRatio(removeFromGroupButton, 0.0f);

        hl2.addComponent(addtoGroupButton);
        hl2.setComponentAlignment(addtoGroupButton, Alignment.BOTTOM_RIGHT);
        hl2.setExpandRatio(addtoGroupButton, 0.0f);

        hl2.addComponent(newGroupSelect);
        hl2.setComponentAlignment(newGroupSelect, Alignment.BOTTOM_RIGHT);
        hl2.setExpandRatio(newGroupSelect, 1.0f);

        targetRoot.addComponent(hl2);
        targetRoot.setComponentAlignment(hl2, Alignment.BOTTOM_LEFT);
        targetRoot.setExpandRatio(hl2, 0.0f);
	}
	
	private static void updateGroupSelection(Set<AccountGroup> alreadyExistingGroups, Database database, ComboBox newGroupSelect) {
		newGroupSelect.removeAllItems();
		
		for(AccountGroup a : AccountGroup.enumerate(database)) {
			//Remove already existing groups
			if(!alreadyExistingGroups.contains(a)) {
				newGroupSelect.addItem(a.uuid);
				newGroupSelect.setItemCaption(a.uuid, a.getText(database));
				newGroupSelect.select(a.uuid);
			}
		}
	}
	
	private static void createAndAttachAccountGroupLayout(Main main, Database database, VerticalLayout targetRoot) {
		targetRoot.removeAllComponents();
		
		HorizontalLayout topHorizontalLayout = new HorizontalLayout();
		topHorizontalLayout.setSpacing(true);
		topHorizontalLayout.setWidth("100%");

		final ComboBox groups = new ComboBox();
		groups.setWidth("100%");
		groups.setNullSelectionAllowed(false);
		groups.addStyleName(ValoTheme.COMBOBOX_TINY);
        groups.setCaption("Valitse ryhm‰:");
        
		final Map<String,AccountGroup> accountGroupMap = new HashMap<String,AccountGroup>();
		makeAccountGroupCombo(main, accountGroupMap, groups);
		
		for(AccountGroup a : AccountGroup.enumerate(database)) {
			accountGroupMap.put(a.text, a);
			groups.addItem(a.text);
			groups.select(a.text);
		}
		
        final TextField newAccountGroupNameField = new TextField();
        
        Validator nameValidator = new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				if(s.isEmpty())
					throw new InvalidValueException("Nimi ei saa olla tyhj‰");
				if(accountGroupMap.containsKey(s))
					throw new InvalidValueException("Nimi on jo k‰ytˆss‰");
			}
			
		};
		
        final Table accountGroupsTable = new Table();
        accountGroupsTable.setSelectable(true);
        accountGroupsTable.setMultiSelect(true);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_SMALL);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_SMALL);
        accountGroupsTable.addStyleName(ValoTheme.TABLE_COMPACT);

		groups.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 5036991262418844060L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				groups.removeValueChangeListener(this);
		        makeAccountGroupCombo(main, accountGroupMap, groups);
		        updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);
		        groups.addValueChangeListener(this);
			}
			
		});
        
        
		final Button createNewAccountGroupButton = new Button("Luo", new Button.ClickListener() {

			private static final long serialVersionUID = -6053708137324681886L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					if(!newAccountGroupNameField.isValid()) return;
					AccountGroup.create(database, newAccountGroupNameField.getValue());
					
					Updates.update(main, true);
	
			        makeAccountGroupCombo(main, accountGroupMap, groups);
			        updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);
	
			        Dialogs.infoDialog(main, "Uusi ryhm‰ '" + newAccountGroupNameField.getValue() + "' luotu", "", null);
				}

			}

		});
		
		createNewAccountGroupButton.setIcon(FontAwesome.USER_PLUS);
		createNewAccountGroupButton.addStyleName(ValoTheme.BUTTON_TINY);

		
		final Button removeAccountGroupButton = new Button("Poista Ryhm‰", new Button.ClickListener() {

			private static final long serialVersionUID = -5359199320445328801L;

			public void buttonClick(ClickEvent event) {
				if(main.getAccountDefault().isAdmin(database)) {
					Object selection = groups.getValue();
					AccountGroup selectedGroup = accountGroupMap.get(selection);
					
					// Default admin group cannot be removed
					if(Configuration.getADMIN_GROUP_NAME().equals(selectedGroup.text)) {
						System.err.println("Cannot remove default admin group!");
						Dialogs.infoDialog(main, "Ryhm‰‰ ei voi poistaa!", "", null);
						return;
					}
					
					UtilsDB.removeAccountGroupAndAssociations(database, selectedGroup);
	
					Updates.update(main, true);
	
					makeAccountGroupCombo(main, accountGroupMap, groups);
					updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);
				}
			}

		});
		
		removeAccountGroupButton.setIcon(FontAwesome.TRASH);
		removeAccountGroupButton.addStyleName(ValoTheme.BUTTON_TINY);
		
		
		newAccountGroupNameField.setWidth("100%");
		newAccountGroupNameField.addStyleName(ValoTheme.TEXTFIELD_TINY);
		newAccountGroupNameField.setCaption("Luo uusi ryhm‰ nimell‰:");
		newAccountGroupNameField.setValue(Utils.findFreshGroupName(nameValidator));
		newAccountGroupNameField.setCursorPosition(newAccountGroupNameField.getValue().length());
		newAccountGroupNameField.setValidationVisible(true);
		newAccountGroupNameField.setInvalidCommitted(true);
		newAccountGroupNameField.setImmediate(true);
		newAccountGroupNameField.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				newAccountGroupNameField.setValue(event.getText());
				try {
					newAccountGroupNameField.validate();
				} catch (InvalidValueException e) {
					createNewAccountGroupButton.setEnabled(false);
					return;
				}
				createNewAccountGroupButton.setEnabled(true);
			}
			
		});
		newAccountGroupNameField.addValidator(nameValidator);
		if(!newAccountGroupNameField.isValid()) createNewAccountGroupButton.setEnabled(false);
		
		
		topHorizontalLayout.addComponent(groups);
		topHorizontalLayout.setExpandRatio(groups, 1.0f);
		topHorizontalLayout.setComponentAlignment(groups, Alignment.BOTTOM_CENTER);

		topHorizontalLayout.addComponent(removeAccountGroupButton);
		topHorizontalLayout.setExpandRatio(removeAccountGroupButton, 0.0f);
		topHorizontalLayout.setComponentAlignment(removeAccountGroupButton, Alignment.BOTTOM_CENTER);
		
		topHorizontalLayout.addComponent(newAccountGroupNameField);
		topHorizontalLayout.setExpandRatio(newAccountGroupNameField, 1.0f);
		topHorizontalLayout.setComponentAlignment(newAccountGroupNameField, Alignment.BOTTOM_CENTER);
		
		topHorizontalLayout.addComponent(createNewAccountGroupButton);
		topHorizontalLayout.setExpandRatio(createNewAccountGroupButton, 0.0f);
		topHorizontalLayout.setComponentAlignment(createNewAccountGroupButton, Alignment.BOTTOM_CENTER);

		targetRoot.addComponent(topHorizontalLayout);
		targetRoot.setExpandRatio(topHorizontalLayout, 0.0f);
		
		accountGroupsTable.addContainerProperty(MAP_OR_OBJECT_ID, String.class, null);
		accountGroupsTable.addContainerProperty("Oikeus", String.class, null);
		accountGroupsTable.addContainerProperty("Tyyppi / Laajuus", String.class, null);
		
		accountGroupsTable.setWidth("100%");
		accountGroupsTable.setHeight("100%");
		accountGroupsTable.setNullSelectionAllowed(true);
		accountGroupsTable.setMultiSelect(true);

		updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);
		
        targetRoot.addComponent(accountGroupsTable);
        targetRoot.setExpandRatio(accountGroupsTable, 1.0f);
        
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Remote and add AccessRights (related to maps)
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
		final Button removeRights = new Button("Poista valitut oikeus", new Button.ClickListener() {

			private static final long serialVersionUID = 4699670345358079045L;

			public void buttonClick(ClickEvent event) {
				Object group = groups.getValue();
				AccountGroup selectedGroup = accountGroupMap.get(group);
				Object selection = accountGroupsTable.getValue();
				Collection<?> allSelections = (Collection<?>)selection;
				List<AccessRight> toRemove = new ArrayList<AccessRight>();
				
				for(Object s : allSelections) {
					Integer index = (Integer)s;
					AccessRight rightToRemove = Utils.getOrderedRightsWithNullSeparators(selectedGroup.rights).get(index-1);
					if(rightToRemove != null) {
						toRemove.add(rightToRemove);
					} else {
						System.err.println("Added null right to list to be removed, ignoring.");
					}
				}
				
				for(AccessRight r : toRemove) {
					System.out.println("Removing accessright " + r.base.text + " from accountgroup " + selectedGroup.text);
					selectedGroup.removeRight(r);
					Utils.removePossibleDocumentAndMapRights(database, selectedGroup, r);
				}
				
				Updates.update(main, true);
				updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);

			}

		});
		
		removeRights.setEnabled(false);
		removeRights.addStyleName(ValoTheme.BUTTON_TINY);
		removeRights.setIcon(FontAwesome.TRASH);
		
		accountGroupsTable.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1188285609779556446L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object selection = accountGroupsTable.getValue();
				Collection<?> sel = (Collection<?>)selection;
				if(sel.isEmpty()) removeRights.setEnabled(false);
				else removeRights.setEnabled(true);
			}
        	
        });
		
		final ComboBox typeSelect = new ComboBox();
		typeSelect.setWidth("140px");
		typeSelect.setNullSelectionAllowed(false);
		typeSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		typeSelect.setCaption("Tyyppi:");
		typeSelect.addItem(KARTTA_OIKEUS);
		typeSelect.addItem(KARTTA_ELEMENTTI_OIKEUS);
		typeSelect.addItem(KOMMENTTI_OIKEUS);
		typeSelect.addItem(RESULTAGREEMENT_RIGHT);
		
		final ComboBox resultAgreementAdminSelect = new ComboBox();
		resultAgreementAdminSelect.setWidth("100%");
		resultAgreementAdminSelect.setNullSelectionAllowed(false);
		resultAgreementAdminSelect.setCaption("Laajuus:");
		resultAgreementAdminSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		resultAgreementAdminSelect.addItem(RESULTAGREEMENT_TOOL_ADMIN);
		resultAgreementAdminSelect.addItem(NOTHING);
		resultAgreementAdminSelect.select(NOTHING);
		
		final ComboBox commentToolSelect = new ComboBox();
		commentToolSelect.setWidth("100%");
		commentToolSelect.setNullSelectionAllowed(false);
		commentToolSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		commentToolSelect.setCaption("Kommenttityˆkalu:");
		commentToolSelect.addItem(COMMENT_TOOL_ADMIN);
		commentToolSelect.addItem(COMMENT_TOOL_USER);
		commentToolSelect.select(COMMENT_TOOL_USER);
		
		final ComboBox mapSelect = new ComboBox();
		mapSelect.setWidth("100%");
		mapSelect.setNullSelectionAllowed(false);
		mapSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		mapSelect.setCaption("Valitse kartta:");

		final String EDIT = "Muokkaus";
		final String READ = "Luku";
		
		final ComboBox rightSelect = new ComboBox();
		rightSelect.setWidth("70px");
		rightSelect.setNullSelectionAllowed(false);
		rightSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		rightSelect.setCaption("Oikeus:");
		rightSelect.addItem(EDIT);
		rightSelect.addItem(READ);
		rightSelect.select(READ);

		final ComboBox propagateSelect = new ComboBox();
		propagateSelect.setWidth("130px");
		propagateSelect.setNullSelectionAllowed(false);
		propagateSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
		propagateSelect.setCaption("Laajuus:");
		propagateSelect.addItem(VALITTU_KARTTA);
		propagateSelect.addItem(ALATASON_KARTAT);
		propagateSelect.select(VALITTU_KARTTA);

		final Button addRight = new Button("Lis‰‰ oikeus", new Button.ClickListener() {

			private static final long serialVersionUID = -4841787792917761055L;

			public void buttonClick(ClickEvent event) {
				
				Object group = groups.getValue();
				AccountGroup selectedGroup = accountGroupMap.get(group);
				
				Object typeO = typeSelect.getValue();
				String type = typeO != null ? typeO.toString() : "";
				
				if(type.equals(KARTTA_OIKEUS) || type.equals(KARTTA_ELEMENTTI_OIKEUS)) {
					String mapUUID = (String)mapSelect.getValue();
					StrategyMap map = database.find(mapUUID);
					String right = (String)rightSelect.getValue();
					String propagate = (String)propagateSelect.getValue();
	
					MapRight r = new MapRight(map, right.equals(EDIT), propagate.equals(ALATASON_KARTAT));
					Utils.addRightToAllLinkedElements(database, selectedGroup, r);
					
				} else if(type.equals(KOMMENTTI_OIKEUS)) {
					Object commentToolUserTypeO = commentToolSelect.getValue();
					String commentToolUserType = commentToolUserTypeO.toString();
					
					String right = (String)rightSelect.getValue();
					AccessRight r = null;
					if(commentToolUserType.equals(COMMENT_TOOL_ADMIN)) {
						r = new AccessRight(CommentToolAdminBase.fetchOrCreateSingleton(database), right.equals(EDIT));
					} else {
						r = new AccessRight(CommentToolBase.fetchOrCreateSingleton(database), right.equals(EDIT));
					}
					if(r != null) {
						selectedGroup.addRightIfMissing(r);
					}
				} else if(type.equals(RESULTAGREEMENT_RIGHT)) {
					String right = (String)rightSelect.getValue();
					Object adminRight = resultAgreementAdminSelect.getValue();
					
					if(adminRight != null && adminRight.equals(RESULTAGREEMENT_TOOL_ADMIN)) {
						AccessRight r = new AccessRight(ResultAgreementToolAdminBase.fetchOrCreateSingleton(database), right.equals(EDIT));
						selectedGroup.addRightIfMissing(r);
					} else {
						ResultAgreementDocuments doc = ResultAgreementDocuments.getInstance(database);
						if(doc != null) {
							AccessRight r = new AccessRight(doc, right.equals(EDIT));
							if(r != null) {
								selectedGroup.addRightIfMissing(r);
							}
						}
						
					}
				}
				else {
					System.err.println("Unexpected type! " + type);
				}
				
				Updates.update(main, true);
				updateSelectedAccountGroupView(database, groups, accountGroupMap, accountGroupsTable);

			}

		});
		addRight.addStyleName(ValoTheme.BUTTON_TINY);
		addRight.setIcon(FontAwesome.KEY);
		
		typeSelect.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1188285609779556446L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object selection = typeSelect.getValue();
				if(selection != null) {
					if(selection.toString().equals(KARTTA_OIKEUS) || selection.toString().equals(KARTTA_ELEMENTTI_OIKEUS)) {
						if(selection.toString().equals(KARTTA_OIKEUS)) {
							//Show only root and sub-maps
							populateMapSelect(database, mapSelect, false);
						} else {
							//Show all
							populateMapSelect(database, mapSelect, true);
						}
						mapSelect.setVisible(true);
						mapSelect.setEnabled(true);
						propagateSelect.setVisible(true);
						propagateSelect.setEnabled(true);
						commentToolSelect.setVisible(false);
						commentToolSelect.setEnabled(false);
						resultAgreementAdminSelect.setVisible(false);
						resultAgreementAdminSelect.setEnabled(false);
						rightSelect.setWidth("70px");
					} else if(selection.toString().equals(KOMMENTTI_OIKEUS)){
						mapSelect.setVisible(false);
						mapSelect.setEnabled(false);
						propagateSelect.setVisible(false);
						propagateSelect.setEnabled(false);
						commentToolSelect.setVisible(true);
						commentToolSelect.setEnabled(true);
						resultAgreementAdminSelect.setVisible(false);
						resultAgreementAdminSelect.setVisible(false);
						rightSelect.setWidth("140px");
					} else if(selection.toString().equals(RESULTAGREEMENT_RIGHT)) {
						mapSelect.setVisible(false);
						mapSelect.setEnabled(false);
						propagateSelect.setVisible(false);
						propagateSelect.setEnabled(false);
						commentToolSelect.setVisible(false);
						commentToolSelect.setEnabled(false);
						resultAgreementAdminSelect.setVisible(true);
						resultAgreementAdminSelect.setEnabled(true);
						rightSelect.setWidth("140px");
					} else {
						System.err.println("Selection for right type did not match any expected type!");
					}
				} else {
					System.err.println("Selection for right type is null!");
				}
			}
        	
        });

		//Select maps to be the default right
		typeSelect.select(KARTTA_OIKEUS);
		
        HorizontalLayout bottomToolbar = new HorizontalLayout();
        bottomToolbar.setSpacing(true);
        bottomToolbar.setWidth("100%");

        bottomToolbar.addComponent(removeRights);
        bottomToolbar.setComponentAlignment(removeRights, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(removeRights, 0.0f);

        bottomToolbar.addComponent(typeSelect);
        bottomToolbar.setComponentAlignment(typeSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(typeSelect, 0.0f);
        
        bottomToolbar.addComponent(mapSelect);
        bottomToolbar.setComponentAlignment(mapSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(mapSelect, 1.0f);

        bottomToolbar.addComponent(resultAgreementAdminSelect);
        bottomToolbar.setComponentAlignment(resultAgreementAdminSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(resultAgreementAdminSelect, 1.0f);
        
        bottomToolbar.addComponent(commentToolSelect);
        bottomToolbar.setComponentAlignment(commentToolSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(commentToolSelect, 1.0f);
        
        bottomToolbar.addComponent(rightSelect);
        bottomToolbar.setComponentAlignment(rightSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(rightSelect, 0.0f);

        bottomToolbar.addComponent(propagateSelect);
        bottomToolbar.setComponentAlignment(propagateSelect, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(propagateSelect, 0.0f);
        
        bottomToolbar.addComponent(addRight);
        bottomToolbar.setComponentAlignment(addRight, Alignment.BOTTOM_LEFT);
        bottomToolbar.setExpandRatio(addRight, 0.0f);
        
        targetRoot.addComponent(bottomToolbar);
        targetRoot.setComponentAlignment(bottomToolbar, Alignment.BOTTOM_LEFT);
        targetRoot.setExpandRatio(bottomToolbar, 0.0f);
        
	}
	
	/**
	 * 
	 * @param database
	 * @param mapSelect
	 * @param showAll
	 */
	private static void populateMapSelect(Database database, ComboBox mapSelect, boolean showAll) {
		mapSelect.removeAllItems();
		HashMap<String, String> uuidToTextMap = new HashMap<String, String>();
		
		//Find the maps:
		if(showAll) {
			for(StrategyMap a : StrategyMap.enumerate(database)) {
				uuidToTextMap.put(a.uuid, a.getText(database));
			}
		} else {
			StrategyMap root = database.getRoot();
			if(root != null) {
				uuidToTextMap.put(root.uuid, root.getText(database));
				for(Linkki l : root.alikartat) {
					StrategyMap underMap = database.find(l.uuid);
					if(underMap != null) {
						uuidToTextMap.put(underMap.uuid, underMap.getText(database));
					}
				}
			}
		}
		
		//Populate combobox:
		for(String key : uuidToTextMap.keySet()) {
			mapSelect.addItem(key);
			mapSelect.setItemCaption(key, uuidToTextMap.get(key));
		}
		
		if(!uuidToTextMap.keySet().isEmpty()) {
			mapSelect.select(uuidToTextMap.keySet().iterator().next());
		}
	}
	
	private static void addChapterLabelEditEntry(VerticalLayout parentRoot, Main main, VerticalLayout root, ResultAgreementConfiguration rac, int i) {
		HorizontalLayout entry = new HorizontalLayout();
		entry.setWidth("100%");
		entry.setSpacing(true);
		entry.setMargin(new MarginInfo(true, true, false, true));
		
		Button save = new Button();
		
		Label index = new Label();
		index.setStyleName(ValoTheme.LABEL_TINY);
		index.setWidth("60px");
		index.setValue("Luku " + i + ":");
		
		String old_label = rac.getChapterLabel(i);
		
		TextField label = new TextField();
		label.setDescription("Luvun lyhytnimi. Enint‰‰n " + ResultAgreementConfiguration.MAX_LABEL_LENGTH + " merkki‰. Yhteisiin lukuihin lis‰t‰‰n aina loppuun: " + ResultAgreementConfiguration.SHARED_LABEL_APPENDIX);
		label.setCaptionAsHtml(true);
		label.setWidth("100%");
		label.setValue(old_label);
		
		String old_desc = rac.getChapterDescription(i);
		
		TextField desc = new TextField();
		desc.setDescription("Luvun kuvaus. Enint‰‰n " + ResultAgreementConfiguration.MAX_DESC_LENGTH + " merkki‰.");
		desc.setCaptionAsHtml(true);
		desc.setWidth("100%");
		desc.setValue(old_desc);
		
		CheckBox isSharedCheckBox = new CheckBox();
		isSharedCheckBox.setValue(rac.isSharedIndex(i));
		isSharedCheckBox.setStyleName(ValoTheme.CHECKBOX_SMALL);
		isSharedCheckBox.setCaption("Yhteinen");
		
		save.setCaption("Tallenna");
		save.setIcon(FontAwesome.SAVE);
		save.setStyleName(ValoTheme.BUTTON_TINY);
		save.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -2488205648825530191L;

			@Override
			public void buttonClick(ClickEvent event) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						String label_value = SafeHtmlUtils.htmlEscape(label.getValue());
						String desc_value = SafeHtmlUtils.htmlEscape(desc.getValue());
						boolean is_shared =  isSharedCheckBox.getValue();
						
						boolean ok = rac.renameLabel(i, label_value);
						boolean ok2 = rac.renameDescription(i, desc_value);
						
						if(!ok || !ok2) {
							Dialogs.errorDialog(main, "Liian pitki‰ nimi‰! Suurin sallittu pituus: "
									+ ResultAgreementConfiguration.MAX_LABEL_LENGTH 
									+ " ja " +
									ResultAgreementConfiguration.MAX_DESC_LENGTH + ".", new Label(), "500px", "125px");
						}
						
						if(rac.isSharedIndex(i) != is_shared) {
							ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
							UtilsDB.synchronizeChapterChange(main.getDatabase(), docs, i, is_shared);
						}
						
						Updates.update(main, true);
						
						ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(main.getDatabase());
						label.setValue(rac.getChapterLabel(i));
						desc.setValue(rac.getChapterDescription(i));
						isSharedCheckBox.setValue(rac.isSharedIndex(i));
					}
					
				};
				
				boolean is_shared =  isSharedCheckBox.getValue();
				if(rac.isSharedIndex(i) != is_shared) {
					String tip = is_shared ? "yhteinen kaikille virastoille" : "virastokohtainen";
					Dialogs.confirmDialog(main, "Olet muokkaamassa luvun \"" + rac.getChapterLabel(i) + "\" (luku "+ i +") m‰‰rityst‰. Luvusta on tulossa " + tip
						+ ". Kaikki kappaleisiin m‰‰ritellyt tekstit katoavat tietokannasta pysyv‰sti.", "Hyv‰ksy muutokset", "Peruuta", runnable,
						"600px", "250px");
				} else {
					runnable.run();
				}

			}
			
		});
		
		Button delete = new Button();
		delete.setIcon(FontAwesome.TRASH);
		delete.setCaption("Poista");
		delete.setStyleName(ValoTheme.BUTTON_TINY);
		delete.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 983180220225273480L;

			@Override
			public void buttonClick(ClickEvent event) {
				ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(main.getDatabase());
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
						boolean delete_ok = UtilsDB.deleteResultAgreementChapterPermanently(main.getDatabase(), docs, i);
						if(delete_ok) {
							Updates.update(main, true);
							createAndAttachResultAgreementSettings(main, main.getDatabase(), parentRoot);
						} else {
							Dialogs.errorDialog(main, "Kappaleen poistossa tapahtui virhe!", new Label(), "450px", "150px");
						}
					}
					
				};
				
				Dialogs.confirmDialog(main, "Poistetaanko luvut \"" + rac.getChapterLabel(i) + "\" (luku "+ i +") kokonaan kaikista tulossopimuksista?"
						+ " Kaikki kappaleisiin m‰‰ritellyt tekstit katoavat tietokannasta pysyv‰sti.", "Poista Pysyv‰sti", "Peruuta", runnable,
						"600px", "250px");
			}
			
		});
		
		entry.addComponent(index);
		entry.setExpandRatio(index, 0.0f);
		entry.setComponentAlignment(index, Alignment.MIDDLE_CENTER);
		
		entry.addComponent(label);
		entry.setExpandRatio(label, 0.5f);
		
		entry.addComponent(desc);
		entry.setExpandRatio(desc, 1.0f);
		
		entry.addComponent(isSharedCheckBox);
		entry.setExpandRatio(isSharedCheckBox, 0.0f);
		entry.setComponentAlignment(isSharedCheckBox, Alignment.MIDDLE_CENTER);
		
		entry.addComponent(save);
		entry.setExpandRatio(save, 0.0f);
		entry.setComponentAlignment(save, Alignment.MIDDLE_CENTER);
		
		entry.addComponent(delete);
		entry.setExpandRatio(delete, 0.0f);
		entry.setComponentAlignment(delete, Alignment.MIDDLE_CENTER);
		
		root.addComponent(entry);
		root.setExpandRatio(entry, 0.0f);
		root.setComponentAlignment(entry, Alignment.TOP_CENTER);
	}
	
	private static void createAndAttachResultAgreementSettings(Main main, Database database, VerticalLayout targetRoot) {
		ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
		
		targetRoot.removeAllComponents();
		targetRoot.setStyleName(CustomCSSStyleNames.STYLE_OVERFLOW_AUTO);
		
		if(docs != null) {
			VerticalLayout chapterLabelDescriptionEditLayout = new VerticalLayout();
			chapterLabelDescriptionEditLayout.setWidth("100%");
			
			ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(database);
			for(int i : rac.getAllIndeces()) {
				addChapterLabelEditEntry(targetRoot, main, chapterLabelDescriptionEditLayout, rac, i);
			}
			
			HorizontalLayout addNewChapterLayout = new HorizontalLayout();
			addNewChapterLayout.setWidth("100%");
			addNewChapterLayout.setHeight("32px");
			addNewChapterLayout.setMargin(new MarginInfo(true, false, false, true));
			
			Button addNewButton = new Button();
			addNewButton.setStyleName(ValoTheme.BUTTON_TINY);
			addNewButton.setDescription("Lis‰‰ uusi luku viimeisen‰ lukuna");
			addNewButton.setCaption("Lis‰‰ uusi luku");
			addNewButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -5140699801698038978L;

				@Override
				public void buttonClick(ClickEvent event) {
					ResultAgreementDocuments docs = ResultAgreementDocuments.getInstance(main.getDatabase());
					boolean ok = UtilsDB.appendNewSharedChapter(main.getDatabase(), docs);
					if(ok) {
						Updates.update(main, true);
						createAndAttachResultAgreementSettings(main, main.getDatabase(), targetRoot);
					} else {
						Dialogs.errorDialog(main, "Kappaleen lis‰yksess‰ tapahtui virhe!", new Label(), "450px", "150px");
					}
				}
			});
			
			addNewChapterLayout.addComponent(addNewButton);
			
			targetRoot.addComponent(addNewChapterLayout);
			targetRoot.setExpandRatio(addNewChapterLayout, 0.0f);
			targetRoot.addComponent(chapterLabelDescriptionEditLayout);
			targetRoot.setExpandRatio(chapterLabelDescriptionEditLayout, 1.0f);
		} else {
			targetRoot.addComponent(new Label("Tulossopimusta ei lˆytynyt tietokannasta"));
		}
	}
	
	private static void createAndAttachMapSettings(Main main, Database database, VerticalLayout targetRoot) {
		targetRoot.removeAllComponents();
		
		final VerticalLayout yearLayout = new VerticalLayout();

        final Panel p = new Panel();
        p.setWidth("100%");
        p.setHeight("100%");
        p.setContent(yearLayout);
        
        final TimeConfiguration tc = TimeConfiguration.getInstance(database); 
        
        final TextField tf2 = new TextField();
        tf2.setWidth("200px");
        tf2.addStyleName(ValoTheme.TEXTFIELD_TINY);
        tf2.setCaption("Strategiakartan m‰‰ritysaika:");
		tf2.setValue(tc.getRange());
		tf2.setValidationVisible(true);
		tf2.setInvalidCommitted(true);
		tf2.setImmediate(true);
		tf2.addTextChangeListener(new TextChangeListener() {
			
			private static final long serialVersionUID = -8274588731607481635L;

			@Override
			public void textChange(TextChangeEvent event) {
				tf2.setValue(event.getText());
				try {
					tf2.validate();
					boolean ok = tc.setRange(event.getText());
					if(! ok) {
						//Unexpected error while setting the range
						VerticalLayout content_ = new VerticalLayout();
						Dialogs.errorDialog(main, "M‰‰ritysaikaa ei voitu asentaa!", content_, "300px", "100px");
					} else {
						updateYears(main, database, yearLayout);
						Updates.update(main, true);
					}
				} catch (InvalidValueException e) {
					return;
				}
			}
			
		});
		tf2.addValidator(new Validator() {

			private static final long serialVersionUID = -4779239111120669168L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				String s = (String)value;
				TimeInterval ti = TimeInterval.parse(s);
				int start = ti.startYear;
				int end = ti.endYear;
				
				ArrayList<Integer> all_years = TimeConfiguration.getInstance(database).getAllConfiguredYears();
				if(all_years.size() == 0) {
					throw new InvalidValueException("Strategiakarttalle ei ole m‰‰ritelty vuosia.");
				} else {
					Integer lowest = all_years.get(0);
					Integer highest = all_years.get(all_years.size() - 1);
					for(Integer y : all_years) {
						if(y < lowest) {
							lowest = y;
						} else if(y > highest) {
							highest = y;
						}
					}
					
					if(end-start > 100)
						throw new InvalidValueException("Strategiakartta ei tue yli " + 100 + " vuoden tarkasteluja.");
					
				}
			}
			
		});

		targetRoot.addComponent(tf2);
		targetRoot.setComponentAlignment(tf2, Alignment.TOP_LEFT);
		targetRoot.setExpandRatio(tf2, 0.0f);
        
        updateYears(main, database, yearLayout);
        
        targetRoot.addComponent(p);
        targetRoot.setComponentAlignment(p, Alignment.TOP_LEFT);
        targetRoot.setExpandRatio(p, 1.0f);
	}

	private static void makeAccountCombo(Main main, final Map<String,Account> accountMap, ComboBox users) {
		
		final Database database = main.getDatabase();

		accountMap.clear();
		String exist = (String)users.getValue();
		users.removeAllItems();
		for(Account a : Account.enumerate(database)) {
			accountMap.put(a.text, a);
			users.addItem(a.text);
			if(exist != null) {
				if(a.text.equals(exist))
					users.select(a.text);
			} else {
				users.select(a.text);
			}
		}
		
	}
	
	private static void updateSelectedAccountGroupView(Database database, ComboBox groups, final Map<String,AccountGroup> accountGroupMap, final Table accountGroupsTable) {
		accountGroupsTable.removeAllItems();
		Object activeUserSelection = groups.getValue();
		AccountGroup accountGroup = accountGroupMap.get(activeUserSelection);

		if(accountGroup != null) {
			int tableIndex = 0;			
			accountGroupsTable.setCaption(accountGroup.text);

			//Add AccessRights according to a pre-defined order
			for(AccessRight r : Utils.getOrderedRightsWithNullSeparators(accountGroup.rights)) {
				if(r == null) { //This is a separator, add separator line
					tableIndex++;
					accountGroupsTable.addItem(new Object[] {
							"-------------",
							"-------------",
							"-------------"
						},
						tableIndex);
				} else if(r instanceof MapRight) { //Map instance, include useful information related to maps
					MapRight mapR = (MapRight)r;
					tableIndex++;
					accountGroupsTable.addItem(new Object[] {
								mapR.getMap().text,
								r.write ? "Muokkaus" : "Luku",
								"(Kartta) " + (mapR.recurse ? ALATASON_KARTAT : VALITTU_KARTTA)
							},
							tableIndex);
				} else { //Regular access right, add as is
					tableIndex++;
					accountGroupsTable.addItem(new Object[] {
							UtilsDB.baseToAccessRightString(database, r.base),
							r.write ? "Muokkaus" : "Luku",
							"(Yleinen oikeus)"
					},
							tableIndex);
					}
			}
			
		} else {
			accountGroupsTable.setCaption("(Ryhm‰‰ ei valittu)");
			System.err.println("Can't update view for null account group selection!");
		}
	}
	
	private static void makeAccountGroupCombo(Main main, final Map<String,AccountGroup> accountGroupMap, ComboBox groups) {
		
		final Database database = main.getDatabase();

		accountGroupMap.clear();
		String exist = (String)groups.getValue();
		groups.removeAllItems();
		for(AccountGroup group : AccountGroup.enumerate(database)) {
			accountGroupMap.put(group.text, group);
			groups.addItem(group.text);
			if(exist != null) {
				if(group.text.equals(exist))
					groups.select(group.text);
			} else {
				groups.select(group.text);
			}
		}
		
	}
	
	private static void updateSelectedAccountView(Database database, ComboBox users,
			final Map<String,Account> accountMap, final Table accountGroupsTable,
			ComboBox newGroupSelect) {
		
		accountGroupsTable.removeAllItems();
		Object activeUserSelection = users.getValue();
		Account account = accountMap.get(activeUserSelection);

		if(account != null) {
			int tableIndex = 0;
			Office possibleAssociatedOffice = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
			
			if(possibleAssociatedOffice != null) {
				AccountGroup officeGroup = UtilsDB.getPossibleAccountGroupByOffice(database, possibleAssociatedOffice);
				String groupText = officeGroup != null ? officeGroup.text : "-";
				accountGroupsTable.setCaptionAsHtml(true);
				String aT = SafeHtmlUtils.htmlEscape(account.text);
				String oT = SafeHtmlUtils.htmlEscape(possibleAssociatedOffice.getText(database));
				String gT = SafeHtmlUtils.htmlEscape(groupText);
				accountGroupsTable.setCaption("<div>K‰ytt‰j‰ [" + aT +"] on virastossa [" + oT + "]."
						+ "</br>Virasto on ryhm‰ss‰ [" + gT + "].</div>");
			} else {
				accountGroupsTable.setCaption("(k‰ytt‰j‰ ei kuulu virastoon)");
			}
			
			updateGroupSelection(account.getAccountGroups(database), database, newGroupSelect);
			
			for(AccountGroup group : account.getAccountGroups(database)) {
				tableIndex++;
				accountGroupsTable.addItem(new Object[] {
							group.text
						},
						tableIndex
						);
			}
		} else {
			accountGroupsTable.setCaption("(K‰ytt‰j‰‰ ei valittu)");
			System.err.println("Can't update view for null account selection!");
		}
	}
	
	private static void updateYears(Main main, final Database database, final VerticalLayout yearLayout) {

		yearLayout.removeAllComponents();
		
		final TimeConfiguration tc = TimeConfiguration.getInstance(database);
        TimeInterval ti = TimeInterval.parse(tc.getRange());
        for(int i=ti.startYear;i<=ti.endYear;i++) {
        	final int year = i;
        	HorizontalLayout hl = new HorizontalLayout();
        	hl.setSpacing(true);
        	String caption = Integer.toString(i) + (tc.isFrozen(i) ? " Muutokset estetty" : " Muokattavissa");
        	Label l = new Label(caption);
        	l.setWidth("250px");
        	l.setHeight("100%");
        	hl.addComponent(l);
        	Button b = new Button(tc.isFrozen(i) ? "Avaa muokattavaksi" : "Est‰ muutokset");
        	b.addStyleName(ValoTheme.BUTTON_TINY);
        	b.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = 556680407448842136L;

				@Override
				public void buttonClick(ClickEvent event) {
					if(main.getAccountDefault().isAdmin(database)) {
						if(tc.isFrozen(year)) {
							tc.unfreeze(year);
						} else {
							tc.freeze(year);
						}
						updateYears(main, database, yearLayout);
					}
				}
				
			});
        	b.setWidth("200px");
        	hl.addComponent(b);
        	//hl.setWidth("100%");
        	yearLayout.addComponent(hl);
        }
		
	}
	
	private static final String RESULTAGREEMENT_TOOL_ADMIN = "Tulossopimusn‰kym‰n yll‰pit‰j‰";
	private static final String NOTHING = "Virastokohtainen n‰kyvyys";
	
	private static final String COMMENT_TOOL_ADMIN = "Kommenttityˆkalun yll‰pit‰j‰";
	private static final String COMMENT_TOOL_USER = "Kommenttityˆkalun k‰ytt‰j‰";
	
	public static final String ALATASON_KARTAT = "Alatason kartat";
	public static final String VALITTU_KARTTA = "Valittu kartta";
	
	private static final String USER_GROUPS = "K‰ytt‰j‰n Ryhm‰t";
	private static final String MAP_OR_OBJECT_ID = "Kartta / Olio";
	
	private static final String KARTTA_OIKEUS = "Kartta";
	private static final String KARTTA_ELEMENTTI_OIKEUS = "Karttaelementti";
	private static final String KOMMENTTI_OIKEUS = "Kommenttityˆkalu";
	private static final String RESULTAGREEMENT_RIGHT = "Tulossopimukset";
	
}
