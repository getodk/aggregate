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

package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.ArrayList;

public final class FilterSet implements Serializable {

  private static final long serialVersionUID = -7646690488192856868L;
  private ArrayList<FilterGroup> groups = new ArrayList<FilterGroup>();
  private String formId;

  public FilterSet() {
  }

  public FilterSet(String formId) {
    this.formId = formId;
  }

  public ArrayList<FilterGroup> getGroups() {
    return groups;
  }

  /**
   * This should add a new filter group to the database
   *
   * @param group the new group
   */
  public void addFilterGroup(FilterGroup group) {
    groups.add(group);
  }

  /**
   * This should remove the filter group altogether
   *
   * @param group the group to be removed
   */
  public void removeFilterGroup(FilterGroup group) {
    groups.remove(group);
  }

  public String getFormId() {
    return formId;
  }
}
