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

import com.google.gwt.event.dom.client.ClickEvent;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SubTabInterface;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;

public final class CursorAdvancementButton extends AggregateButton {

  private static final String NEXT_BUTTON_TXT = "Next";
  private static final String NEXT_TOOLTIP_TXT = "Advance to the 'Next' set of submissions";
  private static final String NEXT_HELP_BALLOON_TXT = "The user inteface limits the number of submission shown on a particular screen. When this button is pressed Aggregate will advance to the 'Next' set of submissions to display";

  private static final String PREV_BUTTON_TXT = "Previous";
  private static final String PREV_TOOLTIP_TXT = "Return to the 'Previous' set of submissions";
  private static final String PREV_HELP_BALLOON_TXT = "The user inteface limits the number of submission shown on a particular screen. When this button is pressed Aggregate will return to the 'Previous' set of submissions that were displayed";


  private final UIQueryResumePoint cursor;
  private final FilterGroup filterGroup;

  public CursorAdvancementButton(SubmissionUISummary summary, FilterGroup filterGroup, boolean forward) {
    super(forward ? NEXT_BUTTON_TXT : PREV_BUTTON_TXT, forward ? NEXT_TOOLTIP_TXT : PREV_TOOLTIP_TXT, forward ? NEXT_HELP_BALLOON_TXT : PREV_HELP_BALLOON_TXT);
    this.filterGroup = filterGroup;

    if (forward) {
      this.cursor = summary.getResumeCursor();
      setEnabled(summary.hasMoreResults());
    } else {
      this.cursor = summary.getBackwardCursor();
      setEnabled(summary.hasPriorResults());
    }
  }

  public void onClick(ClickEvent event) {
    super.onClick(event);

    // set the request to move to the next cursor and update
    filterGroup.setCursor(cursor);
    SubTabInterface filterTab = AggregateUI.getUI().getSubTab(SubTabs.FILTER);
    if (filterTab != null) {
      filterTab.update();
    }
  }
}
