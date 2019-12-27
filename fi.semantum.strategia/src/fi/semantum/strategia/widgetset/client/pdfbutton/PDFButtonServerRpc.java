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

import com.vaadin.shared.ui.button.ButtonServerRpc;

public interface PDFButtonServerRpc extends ButtonServerRpc {

	public void setSVG(String text);

}
