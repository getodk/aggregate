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

package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.popups.ConfirmPublishDeletePopup;
import org.opendatakit.aggregate.client.popups.HelpBalloon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Delete the publishing of data to an external service.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class DeletePublishButton extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Remove published data";

	private static final String HELP_BALLOON_TXT = "This confirms that you want to delete the published " +
			"data.";

	private ExternServSummary publisher;

	public DeletePublishButton(ExternServSummary publisher) {
		super("<img src=\"images/red_x.png\" /> Delete", TOOLTIP_TEXT);
		this.publisher = publisher;
		addStyleDependentName("negative");
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		// TODO: display pop-up with text from b...
		final ConfirmPublishDeletePopup popup = new ConfirmPublishDeletePopup(publisher);
		popup.setPopupPositionAndShow(popup.getPositionCallBack());
	}
}