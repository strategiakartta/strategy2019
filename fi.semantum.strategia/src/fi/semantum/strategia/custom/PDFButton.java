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
package fi.semantum.strategia.custom;

import com.vaadin.annotations.JavaScript;
import com.vaadin.shared.MouseEventDetails;

import fi.semantum.strategia.widgetset.client.pdfbutton.PDFButtonServerRpc;

@JavaScript(value = {
		"app://VAADIN/js/pdfbutton.nocache.js"
	})
public class PDFButton extends com.vaadin.ui.Button {
	
	private static final long serialVersionUID = 2043810264218016506L;
	
	public String svg;
	
	private PDFButtonServerRpc rpc = new PDFButtonServerRpc() {

		private static final long serialVersionUID = 458905634306442336L;

        @Override
        public void click(MouseEventDetails mouseEventDetails) {
            fireClick(mouseEventDetails);
        }

        @Override
        public void disableOnClick() throws RuntimeException {
            setEnabled(false);
            // Makes sure the enabled=false state is noticed at once - otherwise
            // a following setEnabled(true) call might have no effect. see
            // ticket #10030
            getUI().getConnectorTracker().getDiffState(PDFButton.this)
                    .put("enabled", false);
        }

		@Override
		public void setSVG(String text) {
			PDFButton.this.svg = text;
		}

	};  

	public PDFButton() {
		registerRpc(rpc);
	}
	
}
