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
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UIUtils;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;

public final class SaveFilterGroupButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "Save";
  private static final String TOOLTIP_TXT = "Save a new filter group";
  private static final String HELP_BALLOON_TXT = "Save the current filters applied as a filter group.";

  private FilterSubTab parentSubTab;

  public SaveFilterGroupButton(FilterSubTab parentSubTab) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.parentSubTab = parentSubTab;
    addStyleDependentName("positive");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    final FilterGroup filterGroup = parentSubTab.getDisplayedFilterGroup();
    ArrayList<Filter> filters = filterGroup.getFilters();

    if (filters == null || (filters.size() <= 0 && !filterGroup.getIncludeMetadata())) {
      Window.alert(UIConsts.ERROR_NO_FILTERS);
      return;
    }

    // if default filter group, prompt user for name
    if (UIConsts.FILTER_NONE.equals(filterGroup.getName())) {
      String newFilterName;
      try {
        newFilterName = UIUtils.promptForFilterName(parentSubTab.getListOfPossibleFilterGroups());
      } catch (Exception e) {
        return; // user pressed cancel
      }
      filterGroup.setName(newFilterName);
    }

    // Set up the callback object.
    AsyncCallback<String> callback = new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(String uri) {
        if (uri != null) {
          filterGroup.setUri(uri);
        }
        parentSubTab.updateAfterSave(filterGroup);
      }
    };

    // Save the filter on the server
    SecureGWT.getFilterService().updateFilterGroup(filterGroup, callback);

  }
}