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

import org.opendatakit.aggregate.constants.common.UIConsts;

public final class FilterGroup implements Serializable {

  private static final long serialVersionUID = 3317433416889397657L;

  private String uri; // unique identifier
  private String name;
  private String formId;
  private ArrayList<Filter> filters;

  private Boolean includeMetadata;
  
  public FilterGroup() {
    includeMetadata = false;
  }

  public FilterGroup(String groupName, String formId, ArrayList<Filter> filtersToApply) {
    this();
    this.uri = UIConsts.URI_DEFAULT;
    this.name = groupName;
    this.formId = formId;
    
    if(filtersToApply == null) {
      this.filters = new ArrayList<Filter>();
    } else {
      this.filters = filtersToApply;
    }
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public FilterGroup(String uri) {
    this();
    this.uri = uri;
    this.filters = new ArrayList<Filter>();
  }

  public String getName() {
    return name;
  }
  
  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getIncludeMetadata() {
    return includeMetadata;
  }

  public void setIncludeMetadata(Boolean metadata) {
    this.includeMetadata = metadata;
  }

  public ArrayList<Filter> getFilters() {
    return filters;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
  
  /**
   * This should add the filter to the group
   * 
   * @param filter
   *          the filter to be added
   */
  public void addFilter(Filter filter) {
    filters.add(filter);
  }

  /**
   * This should remove the filter from the group
   * 
   * @param filter
   *          the filter to be removed
   */
  public void removeFilter(Filter filter) {
	  filters.remove(filter);
  }

  /**
   * This should apply the filter to the data but not impact the filter group
   * i.e. user is previewing a new filter
   * 
   * @param filter
   *          the new filter to preview
   */
  public void maskAddFilter(Filter filter) {

  }

  /**
   * This should remove the filter to the data but not impact the filter group
   * i.e. user is removing a filter to see what happens
   * 
   * @param filter
   *          the filter to remove and preview
   */
  public void maskRemoveFilter(Filter filter) {

  }

  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FilterGroup)) {
      return false;
    }

    FilterGroup other = (FilterGroup) obj;
    return (name == null ? (other.name == null) : (name.equals(other.name)))
        && (formId == null ? (other.formId == null) : (formId.equals(other.formId)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 101;
    if (name != null)
      hashCode += name.hashCode();
    if (formId != null)
      hashCode += formId.hashCode();
    if (uri != null)
      hashCode += uri.hashCode();
    return hashCode;
  }

}
