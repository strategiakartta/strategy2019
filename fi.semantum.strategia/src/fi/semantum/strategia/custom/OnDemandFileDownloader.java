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

import java.io.IOException;

import com.vaadin.server.DownloadStream;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;

/**
 * Adapted from https://vaadin.com/wiki/-/wiki/Main/Letting+the+user+download+a+file
 * This version also sets the content length and handles session locking properly.
 * 
 * This specializes {@link FileDownloader} in a way, such that both the file name and content can be determined
 * on-demand, i.e. when the user has clicked the component.
 *
 */
public class OnDemandFileDownloader extends FileDownloader {

	/**
	 * Provide both the {@link StreamSource} and the filename in an on-demand way.
	 */
	public interface OnDemandStreamSource extends StreamSource {
		public String getFileName();
		public long getFileSize();
		public void onRequest();
	}

	private static final long serialVersionUID = 1L;
	private final OnDemandStreamSource onDemandStreamSource;
	
	public OnDemandFileDownloader (OnDemandStreamSource onDemandStreamSource) {
		super(new StreamResource(onDemandStreamSource, "") {
			private static final long serialVersionUID = -7386418918429322891L;

			// Inject the content length
			@Override
			public DownloadStream getStream() {
				DownloadStream ds = super.getStream();
				long size = ((OnDemandStreamSource)getStreamSource()).getFileSize();
				if (size > 0) {
					ds.setParameter("Content-Length", String.valueOf(size));
				}
				return ds;
			}
		});
		this.onDemandStreamSource = onDemandStreamSource;
	}
	
	@Override
	public boolean handleConnectorRequest (VaadinRequest request, VaadinResponse response, String path)
			throws IOException {
		VaadinSession session = getSession();
		
		session.lock();
		try {
			onDemandStreamSource.onRequest();
			getResource().setFilename(onDemandStreamSource.getFileName());
		} finally {
			session.unlock();
		}
		return super.handleConnectorRequest(request, response, path);
	}

	public StreamResource getResource () {
		return (StreamResource) this.getResource("dl");
	}

}