package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class PaginationNumTextBox extends AggregateTextBox implements ValueChangeHandler<String> {

  private static final int MAX_NUM_LEN = 4;
  private static final String MUST_BE_A_NUMBER = "Number of Submissions to Display must be a NUMBER!";
  private static final String TOOLTIP_TXT = "Number of Submissions to Display per Page";
  private static final String HELP_BALLOON_TXT = "Number of Submissions to Display per Page, navigation buttons 'previous' and 'next' buttons allow movement between pages";

  private final FilterSubTab filterSubTab;

  public PaginationNumTextBox(FilterSubTab filterSubTab) {
    super(TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.filterSubTab = filterSubTab;

    int queryLimit = filterSubTab.getQueryFetchLimit();

    setVisibleLength(MAX_NUM_LEN);
    setMaxLength(MAX_NUM_LEN);
    setValue(Integer.toString(queryLimit));
    setEnabled(true);
  }

  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    super.onValueChange(event);

    String querySize = event.getValue();

    try {
      int querySizeLimit = Integer.parseInt(querySize);
      filterSubTab.setQueryFetchLimit(querySizeLimit);
      filterSubTab.update();
    } catch (NumberFormatException e) {
      AggregateUI.getUI().reportError(new Throwable(MUST_BE_A_NUMBER));
    }
  }

}
