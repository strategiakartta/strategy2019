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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;

import fi.semantum.strategia.configurations.DefaultInitialActiveYears;
import fi.semantum.strategia.configurations.Configuration;
import fi.semantum.strategia.widget.StandardVisRegistry;
import fi.semantum.strategy.db.DBConfiguration;

@VaadinServletConfiguration(productionMode = false, ui = Main.class, widgetset = "fi.semantum.strategia.widgetset.Fi_semantum_strategiaWidgetset")
public class Servlet extends VaadinServlet {

	private static final long serialVersionUID = 8413467755686759773L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
	IOException {
		super.service(request, response);
	}


	@Override
	protected void servletInitialized() throws ServletException {
		//Ensure environment variables are read and application configured:
		DBConfiguration.init();
		Configuration.init();
		DefaultInitialActiveYears.init();
		StandardVisRegistry.init();
		
		super.servletInitialized();
		
		getService().addSessionInitListener(new SessionInitListener() {

			private static final long serialVersionUID = 8234186352463637883L;

			@Override
			public void sessionInit(SessionInitEvent event) {
				event.getSession().addBootstrapListener( new BootstrapListener() {

					private static final long serialVersionUID = 7535841016176448694L;

					@Override
					public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
					}

					@Override
					public void modifyBootstrapPage(BootstrapPageResponse response) {
					}
				});
			}
		});
	}

}

