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
package fi.semantum.strategia.configurations;

import java.io.File;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;

/**
 * Keep up-to-date with files found under ./WebContent/
 * @author Miro Eklund
 *
 */
public class WebContent {
	
	private static final String IMAGES_PATH = "/WEB-INF/images";
	private static final String OFFICE_IMAGES_PATH = IMAGES_PATH + "/offices";
	
	public static final String ADDONS_SCSS = "addons.scss";
	public static final String CHART_BAR_EDIT_PNG = "chart_bar_edit.png";
	public static final String CHART_LINE_EDIT_PNG = "chart_line_edit.png";
	public static final String CURSOR_PNG = "cursor.png";
	public static final String FAVICON_ICO = "favicon.ico";
	public static final String FI_SEMANTUM_STRATEGIA_SCSS = "fi_semantum_strategia.scss";
	public static final String PAGE_COPY_PNG = "page_copy.png";
	public static final String PAGE_WHITE_EXCEL_PNG = "page_white_excel.png";
	public static final String PRINTSTYLES_CSS = "printstyles.css";
	public static final String SHEETS_OF_PAPER_A4_CSS = "sheets-of-paper-a4.css";
	public static final String SHEETS_OF_PAPER_CSS = "sheets-of-paper.css";
	public static final String STYLES_CSS = "styles.css";
	public static final String STYLES_SCSS = "styles.scss";
	public static final String TABLE_EDIT_PNG = "table_edit.png";
	public static final String TAG_BLUE_EDIT_PNG = "tag_blue_edit.png";
	public static final String ZOOM_OUT_PNG = "zoom_out.png";
	public static final String ZOOM_PNG = "zoom.png";
	
	public FileResource redResource;
	public FileResource greenResource;
	public FileResource blackResource;
	public FileResource mapMagnify;
	
	
	public WebContent() {
		// Find the application directory
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
		
		redResource = new FileResource(new File(basepath + IMAGES_PATH + "/bullet_red.png"));
		greenResource = new FileResource(new File(basepath + IMAGES_PATH + "/bullet_green.png"));
		blackResource = new FileResource(new File(basepath + IMAGES_PATH + "/bullet_black.png"));
		mapMagnify = new FileResource(new File(basepath + IMAGES_PATH + "/map_magnify.png"));
	}
	
}
