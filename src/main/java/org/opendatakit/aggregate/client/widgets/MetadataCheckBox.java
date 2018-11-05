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

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.FilterGroup;

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