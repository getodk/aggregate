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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.popups.HelpBalloon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RemoveFilterGroupButton extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Delete filter group";

	private static final String HELP_BALLOON_TXT = "This will delete the filter group from the database.";

	private FilterSubTab parentSubTab;

	public RemoveFilterGroupButton(FilterSubTab parentSubTab) {
		super("Delete", TOOLTIP_TEXT);
		this.parentSubTab = parentSubTab;
		addStyleDependentName("negative");
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		final FilterGroup filterGroup = parentSubTab.getDisplayedFilterGroup();

		// Set up the callback object.
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				AggregateUI.getUI().clearError();
				parentSubTab.removeFilterGroupWithinForm();
			}
		};

		// Save the filter on the server
		SecureGWT.getFilterService().deleteFilterGroup(filterGroup, callback);

	}
}