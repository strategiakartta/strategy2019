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

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategy.db.MdHtmlContent;

/**
 * Re-useable user account toolbar for use in multiple places.
 * Simply create a new instance and attached buttons where needed.
 */
public class MainAccountToolbar {
	
	private Main main;
	
	public Button openFAQ;
	public Button manageUsers;
	public Button accountSettings;
	public Button login;
	
	private void openFAQPopup() {
		MdHtmlContent content = MdHtmlContent.getOrCreateByID(main.getDatabase(), MdHtmlContent.FAQ);
		ChapterEditorWrapperUI contentEditor = new ChapterEditorWrapperUI(main, content);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setHeight("100%");
		layout.addComponent(contentEditor.getRoot());
		layout.setComponentAlignment(contentEditor.getRoot(), Alignment.MIDDLE_CENTER);
		
		HorizontalLayout buttons = new HorizontalLayout();
		
		String width = "25cm";
		if(main.account != null && main.account.isAdmin(main.getDatabase())) {
			width = "98%";
		}
		
		Dialogs.makeDialog(main, width, "98%", "", "Sulje", layout, buttons);
	}
	
	public MainAccountToolbar(Main main) {
		this.main = main;
		init();
	}
	
	public void login() {
		if(main.account != null) {
			manageUsers.setVisible(main.account.isAdmin(main.getDatabase()));
			accountSettings.setVisible(true);
			openFAQ.setVisible(true);
			login.setCaption("Kirjaudu ulos " + main.account.getId(main.getDatabase()));
		} else {
			logout();
		}
	}
	
	public void logout() {
		manageUsers.setVisible(false);
		accountSettings.setVisible(false);
		openFAQ.setVisible(false);
		login.setCaption("Kirjaudu");
		login.setCaption("Kirjaudu");
	}
	
	private void init() {

		openFAQ = new Button();
		openFAQ.setIcon(FontAwesome.QUESTION_CIRCLE);
		openFAQ.setCaption("ukk");
		openFAQ.setWidthUndefined();
		openFAQ.setVisible(false);
		openFAQ.addStyleName(ValoTheme.BUTTON_TINY);
		openFAQ.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1649120878082829500L;

			@Override
			public void buttonClick(ClickEvent event) {
				openFAQPopup();
			}
			
		});
		
		manageUsers = new Button("Hallinnoi");
		manageUsers.setWidthUndefined();
		manageUsers.setIcon(FontAwesome.GEAR);
		manageUsers.setVisible(false);
		manageUsers.addStyleName(ValoTheme.BUTTON_TINY);
		manageUsers.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -1649120878082829500L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (main.account != null) {
					if (main.account.isAdmin(main.getDatabase())) {
						MainManageUsersWindow.createAndOpen(main);
					}
				}
			}

		});

		accountSettings = new Button("Käyttäjätili");
		accountSettings.setIcon(FontAwesome.USER);
		accountSettings.setWidthUndefined();
		accountSettings.setVisible(false);
		accountSettings.addStyleName(ValoTheme.BUTTON_TINY);
		accountSettings.addClickListener(new ClickListener() {

			private static final long serialVersionUID = -5508211629252569877L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (main.account != null) {
					Utils.modifyAccount(main);
				}
			}

		});

		//Another Login button also exists in the StartPage
		login = new Button("Kirjaudu");
		login.setWidthUndefined();
		login.addStyleName(ValoTheme.BUTTON_TINY);
		login.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 4974892627256251266L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (main.account != null) {
					Login.logout(main);
				} else {
					Login.login(main);
				}
			}

		});
	}
	
	public void attachToLayout(HorizontalLayout layout) {
		layout.addComponent(this.openFAQ);
		layout.setComponentAlignment(this.openFAQ, Alignment.MIDDLE_RIGHT);
		layout.setExpandRatio(this.openFAQ, 0.0f);
		
		layout.addComponent(this.manageUsers);
		layout.setComponentAlignment(this.manageUsers, Alignment.MIDDLE_RIGHT);
		layout.setExpandRatio(this.manageUsers, 0.0f);

		layout.addComponent(this.accountSettings);
		layout.setComponentAlignment(this.accountSettings, Alignment.MIDDLE_RIGHT);
		layout.setExpandRatio(this.accountSettings, 0.0f);

		layout.addComponent(this.login);
		layout.setComponentAlignment(this.login, Alignment.MIDDLE_RIGHT);
		layout.setExpandRatio(this.login, 0.0f);
	}
}
