package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public final class MetadataCheckBox extends AggregateCheckBox implements
    ValueChangeHandler<Boolean> {

  private static final String LABEL = "Display Metadata";
  private static final String TOOLTIP_TXT = "Display or hide metadata";
  private static final String HELP_BALLOON_TXT = "When checked, it will show the metadata columns.  "
      + "When not checked, it will hide these columns.";

  private final FilterSubTab filterSubTab;

  public MetadataCheckBox(FilterSubTab filterSubTab) {
    super(LABEL, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.filterSubTab = filterSubTab;

    Boolean inlcudeMetaData = filterSubTab.getDisplayMetaData();

    setValue(inlcudeMetaData);
    setEnabled(true);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);

    filterSubTab.setDisplayMetaData(event.getValue());
    filterSubTab.update();
  }

}