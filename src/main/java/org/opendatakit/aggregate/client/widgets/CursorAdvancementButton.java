package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SubTabInterface;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;

import com.google.gwt.event.dom.client.ClickEvent;

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

    if(forward) {
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
    if(filterTab != null) {
      filterTab.update();
    }
  }
}
