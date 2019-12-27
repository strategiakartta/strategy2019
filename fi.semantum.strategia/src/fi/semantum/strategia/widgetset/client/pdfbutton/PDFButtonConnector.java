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
package fi.semantum.strategia.widgetset.client.pdfbutton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.Connect;

import fi.semantum.strategia.custom.PDFButton;

@Connect(PDFButton.class)
public class PDFButtonConnector extends ButtonConnector {

	private static final long serialVersionUID = -1721081498304205311L;

	@Override
    public void onClick(ClickEvent event) {
        if (getState().disableOnClick) {
            // Simulate getting disabled from the server without waiting for the
            // round trip. The server-side RPC handler takes care of updating
            // the server-side state in a similar way to ensure subsequent
            // changes are properly propagated. Changing state on client is not
            // generally supported.
            getState().enabled = false;
            super.updateEnabledState(false);
            getRpcProxy(PDFButtonServerRpc.class).disableOnClick();
        }

        // Add mouse details
        MouseEventDetails details = MouseEventDetailsBuilder
                .buildMouseEventDetails(event.getNativeEvent(), getWidget()
                        .getElement());
        
        String text = extractSVG1();

        getRpcProxy(PDFButtonServerRpc.class).setSVG(text);
        getRpcProxy(PDFButtonServerRpc.class).click(details);

    }
	
	public static native String extractSVG1() /*-{
		return $wnd.extractSVG();
	}-*/;
	
}

