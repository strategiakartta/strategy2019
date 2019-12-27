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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategy.db.Account;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Terminology;
import fi.semantum.strategy.db.UtilsDB;

public class Login {

	
	private Window subwindow;
	private VerticalLayout winLayout;
	private TextField tf;
	private PasswordField pf;
	private Label l;
	private Button ok;
	private Button close;
	
	private Login() {
        subwindow = new Window("", new VerticalLayout());
        subwindow.setCaption("Anna k‰ytt‰j‰tunnus ja salasana");
        subwindow.setModal(true);
        subwindow.setWidth("400px");
        subwindow.setHeight("295px");
        subwindow.setResizable(false);

        winLayout = (VerticalLayout) subwindow.getContent();
        winLayout.setMargin(true);
        winLayout.setSpacing(true);

        tf = new TextField();
        tf.setWidth("100%");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        tf.setCaption("K‰ytt‰j‰tunnus:");
        tf.setId("loginUsernameField");
        winLayout.addComponent(tf);

        pf = new PasswordField();
        pf.setCaption("Salasana:");
        pf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        pf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
        pf.setWidth("100%");
        pf.setId("loginPasswordField");
        winLayout.addComponent(pf);

        l = new Label("V‰‰r‰ k‰ytt‰j‰tunnus tai salasana");
        l.addStyleName(ValoTheme.LABEL_FAILURE);
        l.addStyleName(ValoTheme.LABEL_TINY);
        l.setVisible(false);
        winLayout.addComponent(l);
        
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        
        ok = new Button("Kirjaudu");
        hl.addComponent(ok);
        
        close = new Button(Terminology.CANCEL);
        hl.addComponent(close);
        
        winLayout.addComponent(hl);
        winLayout.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
        
        tf.setCursorPosition(tf.getValue().length());
	}
	
	private void connectFieldListeners(ValueChangeListener passwordValueListener,
			Button.ClickListener okButtonClick,
			Button.ClickListener closeButtonClick) {
        
		if(passwordValueListener != null) {
			this.pf.addValueChangeListener(passwordValueListener);
		}
		
		if(okButtonClick != null) {
			this.ok.addClickListener(okButtonClick);
		}
		
		if(closeButtonClick != null) {
			this.close.addClickListener(closeButtonClick);
		}
	}
	
	/**
	 * Emergency login for corrupted database rollback
	 * @param main
	 */
	public static void createEmergencyLoginWindow(final Main main, Database emergencyDatabase, AbstractLayout showContentOnSuccess) {
		Login loginInstance = new Login();

        Button.ClickListener okButtonClick = new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5148036024457593062L;

			public void buttonClick(ClickEvent event) {
				String hash = UtilsDB.hash(loginInstance.pf.getValue());
				if(DatabaseLoader.emergencyLoginOK(emergencyDatabase, loginInstance.tf.getValue(), hash)) {
					main.setContent(showContentOnSuccess);
					main.removeWindow(loginInstance.subwindow);
				} else {
					loginInstance.l.setVisible(true);
				}
            }
            
        };
        
        ValueChangeListener passwordValueListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -2708082203576343391L;

        	@Override
			public void valueChange(ValueChangeEvent event) {
        		loginInstance.l.setVisible(false);
			}
		};

        
        loginInstance.connectFieldListeners(passwordValueListener, okButtonClick, null);
        loginInstance.close.setVisible(false);
        loginInstance.subwindow.setClosable(false);
        loginInstance.subwindow.setCaption("Kirjaudu sis‰‰n System/Admin k‰ytt‰j‰n‰");
        
		main.addWindow(loginInstance.subwindow);
	}
	
	/**
	 * Regular login for main application
	 * @param main
	 */
	public static void login(final Main main) {
		Login loginInstance = new Login();
		
		ValueChangeListener passwordValueListener = new ValueChangeListener() {
			
			private static final long serialVersionUID = -2708082203576343391L;

        	@Override
			public void valueChange(ValueChangeEvent event) {
        		doLogin(main, loginInstance.subwindow, loginInstance.l, loginInstance.tf.getValue(), loginInstance.pf.getValue());
			}
		};

        Button.ClickListener okButtonClick = new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5148036024457593062L;

			public void buttonClick(ClickEvent event) {
        		doLogin(main, loginInstance.subwindow, loginInstance.l, loginInstance.tf.getValue(), loginInstance.pf.getValue());
            }
            
        };
        
        Button.ClickListener closeButtonClick = new Button.ClickListener() {
        	
			private static final long serialVersionUID = -5719853213838228457L;

			public void buttonClick(ClickEvent event) {
            	main.removeWindow(loginInstance.subwindow);
            }
            
        };
        
        loginInstance.connectFieldListeners(passwordValueListener, okButtonClick, closeButtonClick);

		main.addWindow(loginInstance.subwindow);
	}

	static void doLogin(Main main, Window subwindow, Label l, String usr, String pass) {

		Database database = main.getDatabase();
    	String hash = UtilsDB.hash(pass);
    	Account acc = Account.find(database, usr);
    	doLogin(main, subwindow, l, hash, acc);
	}

	static void doLogin(Main main, Window subwindow, Label l, String hash, Account acc) {

		Database database = main.getDatabase();
    	if(acc != null) {
    		if(hash.equals(acc.getHash())) {
    			main.removeWindow(subwindow);
    			loginAccountRefreshUI(main, database, acc);
    			return;
    		}
    	}
    	l.setVisible(true);

	}
	
	/**
	 * If all hashes and checked, etc. tries to login the user
	 * @param main
	 * @param database
	 * @param acc
	 */
	static void loginAccountRefreshUI(Main main, Database database, Account acc) {
    	main.account = acc;
    	loginRefreshUI(main, database);
	}
	
	/**
	 * Login as Acc and refresh view
	 * @param main
	 * @param database
	 * @param acc
	 */
	static void loginRefreshUI(Main main, Database database) {
    	if(main.getUIState().startPageVisible) {
    		main.startPage.handleLoginUIRefresh();
    	} else {
	    	main.handleLoginUIRefresh();
    	}
	}
	
	/**
	 * Refresh view according to if user is logged in or not
	 * @param main
	 * @param database
	 */
	static void refreshUserUI(Main main) {
		Account acc = main.account;
		if(acc == null) {
			logoutRefreshUI(main);
		} else {
			loginRefreshUI(main, main.getDatabase());
		}
	}
	
	/**
	 * 
	 * @param main
	 */
	static void logoutRefreshUI(Main main) {
    	if(main.getUIState().startPageVisible) {
    		main.startPage.handleLogoutUIRefresh();
    	} else {
			main.handleLogoutUIRefresh();
    	}
	}
	
	/**
	 * Logout user and refresh as a logged out user
	 * @param main
	 */
	static void logout(Main main) {
		main.account = null;
		logoutRefreshUI(main);
	}
	
}
