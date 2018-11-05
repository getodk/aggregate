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

package org.opendatakit.aggregate.client.table;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.widgets.CursorAdvancementButton;

public class SubmissionPaginationNavBar extends SimplePanel {


  private final FlexTable controls;

  public SubmissionPaginationNavBar() {

    getElement().setId("filter_submission_pagination");

    controls = new FlexTable();
    controls.addStyleName("form_title_bar");
    controls.getElement().setAttribute("align", "center");

    add(controls);
  }


  public void update(FilterGroup group, SubmissionUISummary summary) {
    // create previous button
    controls.setWidget(0, 0, new CursorAdvancementButton(summary, group, false));

    controls.setHTML(0, 1, new SafeHtmlBuilder().appendHtmlConstant("<h2 id=\"form_name\">").appendEscaped(summary.getFormTitle()).appendHtmlConstant("</h2>").toSafeHtml());

    // create next button
    controls.setWidget(0, 2, new CursorAdvancementButton(summary, group, true));
  }
}
