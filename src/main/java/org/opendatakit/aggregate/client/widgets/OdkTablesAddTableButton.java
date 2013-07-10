/*
 * Copyright (C) 2013 University of Washington
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

import org.opendatakit.aggregate.client.popups.OdkTablesAddNewTablePopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesAddTableButton extends AggregateButton implements
		ClickHandler {

	  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Add Table";
	  private static final String TOOLTIP_TXT = "Add a new table";
	  private static final String HELP_BALLOON_TXT = "Add a new table to the database.";

	  public OdkTablesAddTableButton() {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);

	    OdkTablesAddNewTablePopup popup = new OdkTablesAddNewTablePopup();
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
}
