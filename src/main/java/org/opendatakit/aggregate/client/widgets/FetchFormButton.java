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
import org.opendatakit.aggregate.client.table.FormNFilterSelectionTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class FetchFormButton extends AbstractButtonBase implements ClickHandler {

  private static final String TOOLTIP_TEXT = "Display the form and filter";
  
  private FormNFilterSelectionTable formNFilter;

  public FetchFormButton(FormNFilterSelectionTable formNFilter) {
    super("Fetch Form with Filter", TOOLTIP_TEXT);
    this.formNFilter = formNFilter;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    formNFilter.fetchClicked();
    AggregateUI.getUI().getTimer().refreshNow();
  }
}
