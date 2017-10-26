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

import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class OdkTablesTableNameBox extends AggregateTextBox implements
		ValueChangeHandler<String> {

	  private static final String TOOLTIP_TXT = "Display Name for this Table";
	  private static final String HELP_BALLOON_TXT = "Enter the user-friendly display name for this table";

	  private final OdkTablesAddNewTablePopup popup;

	  public OdkTablesTableNameBox(OdkTablesAddNewTablePopup popup) {
	    super(TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.popup = popup;

	    setEnabled(true);
	  }
	}
