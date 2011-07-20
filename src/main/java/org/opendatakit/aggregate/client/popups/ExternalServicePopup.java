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

package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecutePublishButton;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class ExternalServicePopup extends PopupPanel {

  public static final String TYPE_SPREAD_SHEET = "Google Spreadsheet";
  public static final String TYPE_FUSION_TABLE = "Google Fusion Table";

  private String formId;
  private TextBox name;
  private ListBox service;
  private ListBox esOptions;
  
  public ExternalServicePopup(String formId) {
    super(false);
    
    this.formId = formId;

    service = new ListBox();
    service.addItem(TYPE_FUSION_TABLE);
    service.addItem(TYPE_SPREAD_SHEET);
    service.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (service.getItemText(service.getSelectedIndex()).equals(TYPE_FUSION_TABLE)) {
          name.setText("Spreadsheet Name");
          name.setEnabled(false);
        } else { // .equals(TYPE_SPREAD_SHEET)
          name.setEnabled(true);
        }
      }
    });

    name = new TextBox();
    name.setText("Spreadsheet Name");
    name.setEnabled(false);
    name.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        name.setText("");
      }
    });
    
    esOptions = new ListBox();
    for (ExternalServicePublicationOption eso : ExternalServicePublicationOption.values()) {
      esOptions.addItem(eso.toString());
    }
    
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML("Form: " + formId + " "));    
    layout.setWidget(0, 1, service);
    layout.setWidget(0, 2, name);


    layout.setWidget(0, 3, esOptions);
    layout.setWidget(0, 4, new ExecutePublishButton(this));
    layout.setWidget(0, 5, new ClosePopupButton(this));

    setWidget(layout);
  }

  public String getFormId() {
    return formId;
  }

  public String getName() {
    return name.getText();
  }

  public String getService() {
    return service.getItemText(service.getSelectedIndex());
  }

  public ExternalServicePublicationOption getEsOptions() {
    String selectedOption = esOptions.getItemText(esOptions.getSelectedIndex());
    for (ExternalServicePublicationOption selected : ExternalServicePublicationOption.values()) {
      if (selected.toString().equals(selectedOption))
        return selected;
    }
    return null;
  }
  
  
}
