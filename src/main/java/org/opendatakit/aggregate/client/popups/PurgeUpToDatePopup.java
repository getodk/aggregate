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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.Date;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.datepicker.client.DatePicker;

public class PurgeUpToDatePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Purge Data";
  private static final String TOOLTIP_TXT = "Delete submission data through the given date";
  private static final String HELP_BALLOON_TXT = "This confirms that you want to delete the submission data.";

  private FormSummary summary;
  private Date selectedDate = new Date(); // today...
  private DatePicker picker;
  private AggregateButton confirm;

  public PurgeUpToDatePopup(FormSummary formSummary) {
    super();

    this.summary = formSummary;

    confirm = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    confirm.addClickHandler(new PurgeHandler());
    confirm.setEnabled(false);

    picker = new DatePicker();
    picker.addValueChangeHandler(new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        if ( selectedDate != null ) {
          picker.removeStyleFromDates("datePickerDayIsSelectedAndHighlighted", selectedDate);
        }
        selectedDate = event.getValue();
        if ( selectedDate != null ) {
          picker.addTransientStyleToDates("datePickerDayIsSelectedAndHighlighted", selectedDate);
        }
        confirm.setEnabled(selectedDate != null);
      }});

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML(new SafeHtmlBuilder()
        .appendEscaped("Purge submissions data for:")
        .appendHtmlConstant("<br/>")
        .appendHtmlConstant("<b>" + formSummary.getTitle() + " [" + formSummary.getId() + "]</b>")
        .appendHtmlConstant("<br>")
        .appendEscaped("up to the chosen GMT date.")
        .appendHtmlConstant("<br>")
        .appendEscaped("Incomplete submissions will")
        .appendHtmlConstant("<br>")
        .appendEscaped("not be deleted.")
        .toSafeHtml()));
    layout.setWidget(0, 1, picker);
    layout.setWidget(0, 2, confirm);
    layout.setWidget(0, 3, new ClosePopupButton(this));
    setWidget(layout);
  }

  private class PurgeHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {

      // set time to one millisecond past midnight at the start of the chosen day
      @SuppressWarnings("deprecation")
      Date theDate = new Date( Date.UTC(selectedDate.getYear(),
                                selectedDate.getMonth(), selectedDate.getDate(),
                                0, 0, 0));

      if ( theDate.getTime() > System.currentTimeMillis() ) {
        // if in the future -- reset to current time...
        theDate = new Date();
      }
      ConfirmPurgeUpToDatePopup popup = new ConfirmPurgeUpToDatePopup( summary, theDate );
      popup.setPopupPositionAndShow(popup.getPositionCallBack());
    }
  }
}