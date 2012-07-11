package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.FilterGroup;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public final class MetadataCheckBox extends AggregateCheckBox implements
    ValueChangeHandler<Boolean> {

  private static final String LABEL = "<span id=\"filter_header\">Display Metadata</span>";
  private static final String TOOLTIP_TXT = "Display or hide metadata";
  private static final String HELP_BALLOON_TXT = "When checked, it will show the metadata columns.  "
      + "When not checked, it will hide these columns.";

  private final FilterSubTab filterSubTab;

  public MetadataCheckBox(FilterGroup group, FilterSubTab filterSubTab) {
    super(LABEL, true, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.filterSubTab = filterSubTab;
    setValue(group.getIncludeMetadata());
    setEnabled(true);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);

    filterSubTab.setDisplayMetaData(event.getValue());
    filterSubTab.update();
  }

}