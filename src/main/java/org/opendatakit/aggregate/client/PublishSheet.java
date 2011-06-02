/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client;

import java.util.Date;

import org.opendatakit.aggregate.client.services.admin.ExternServSummary;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * List all the external services to which forms are published.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class PublishSheet extends FlexTable {

	private static final String K_MAILTO = "mailto:";
	private static int PURGE_DATA = 0;
	private static int CREATED_BY = 1;
	private static int STATUS = 2;
	private static int TIME_PUBLISH_START = 3;
	private static int ACTION = 4;
	private static int TYPE = 5;
	private static int NAME = 6;
	
	private AggregateUI baseUI;
	
	public PublishSheet(AggregateUI baseUI) {
		super();
		this.baseUI = baseUI;
		this.setText(0, PURGE_DATA, " ");
		this.setText(0, CREATED_BY, "Created By");
		this.setText(0, STATUS, "Status");
		this.setText(0, TIME_PUBLISH_START, "Start Date");
		this.setText(0, ACTION, "Action");
		this.setText(0, TYPE, "Type");
		this.setText(0, NAME, "Name");
		this.addStyleName("dataTable");
		this.getRowFormatter().addStyleName(0, "titleBar");
	}
	
	private class ConfirmPurgePopup  extends PopupPanel implements ClickHandler {
		ExternServSummary e;
		Date earliest;

		ConfirmPurgePopup(ExternServSummary e, Date earliest, String bodyText) {
			super(false);
			this.e = e;
			this.earliest = earliest;
		    FlexTable layout = new FlexTable();
		    
		    layout.setWidget(0, 0, new HTML(bodyText));

		    Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Purge Data");
		    publishButton.addClickHandler(this);
		    layout.setWidget(0, 1, publishButton);
			
			Button closeButton = new Button("<img src=\"images/red_x.png\" />");
			closeButton.addStyleDependentName("close");
			closeButton.addStyleDependentName("negative");
			closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			layout.setWidget(0, 2, closeButton);
		    
		    setWidget(layout);
		}

		@Override
		public void onClick(ClickEvent event) {
			// OK -- we are to proceed.
			baseUI.formAdminSvc.purgePublishedData(e.getUri(), earliest, new AsyncCallback<Date>() {

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("Failed purge of published data: " + caught.getMessage());
					
				}

				@Override
				public void onSuccess(Date result) {
					Window.alert("Successful commencement of the purge of " +
							"\nall data published as of " + result.toString());
				}
			});
			hide();
		}
	}
	
	private class ButtonClickHandler implements ClickHandler {

		String formId;
		ExternServSummary e;
		
		ButtonClickHandler(String formId, ExternServSummary e) {
			this.formId = formId;
			this.e = e;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			Date earliest = null;
			switch ( e.getPublicationOption() ) {
			case UPLOAD_ONLY:
				if ( e.getUploadCompleted() ) {
					earliest = e.getTimeEstablished();
				} else {
					earliest = e.getTimeLastUploadCursor();
				}
				break;
			case UPLOAD_N_STREAM:
				if ( e.getUploadCompleted() ) {
					earliest = e.getTimeLastStreamingCursor();
					if ( earliest == null ) {
						earliest = e.getTimeEstablished();
					}
				} else {
					earliest = e.getTimeLastUploadCursor();
				}
				break;
			case STREAM_ONLY:
				earliest = e.getTimeLastStreamingCursor();
				if ( earliest == null ) {
					earliest = e.getTimeEstablished();
				}
				break;
			}
			
			StringBuilder b = new StringBuilder();
			if ( earliest == null ) {
				Window.alert("Data has not yet been published -- no data will be purged");
			} else {
				if ( e.getPublicationOption() != ExternalServicePublicationOption.UPLOAD_ONLY) {
					b.append("<p><b>Note:</b> Even though the chosen publishing action involves an ongoing streaming" +
							" of data to the external service, this purge action is a one-time event and is " +
							"not automatically ongoing.  You will need to periodically repeat this process.</p>");
				}
				b.append("Click to confirm purge of <b>" + formId + "</b> submissions older than " + 
							earliest.toString());
	
				// TODO: display pop-up with text from b...
				final ConfirmPurgePopup popup = new ConfirmPurgePopup(e, earliest, b.toString());
				popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						int left = ((Window.getClientWidth() - offsetWidth) / 2);
						int top = ((Window.getClientHeight() - offsetHeight) / 2);
						popup.setPopupPosition(left, top);
					}
				});
			}
		}
		
	}

	public void updatePublishPanel(String formId, ExternServSummary[] eSS) {
		if (eSS == null)
			return;
		while (this.getRowCount() > 1)
			this.removeRow(1);
		for (int i = 0; i < eSS.length; i++) {
			ExternServSummary e = eSS[i];
			Button b = new Button("Purge Published Data");
			b.addClickHandler(new ButtonClickHandler(formId, e));
			this.setWidget(i + 1, PURGE_DATA, b);
			String user = e.getUser();
			String displayName;
			if ( user.startsWith(K_MAILTO) ) {
				displayName =user.substring(K_MAILTO.length());
			} else if ( user.startsWith("uid:") ) {
				displayName = user.substring("uid:".length(),user.indexOf("|"));
			} else {
				displayName = user;
			}
			this.setText(i + 1, CREATED_BY, displayName);
			this.setText(i + 1, STATUS, e.getStatus().toString());
			this.setText(i + 1, TIME_PUBLISH_START, e.getTimeEstablished().toString());
			this.setText(i + 1, ACTION, e.getPublicationOption().getDescriptionOfOption());
			this.setText(i + 1, TYPE, e.getExternalServiceTypeName());
			this.setWidget(i + 1, NAME, new HTML(e.getName()));
		}
	}

}
