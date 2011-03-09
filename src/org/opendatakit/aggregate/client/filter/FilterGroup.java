package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.common.UIConsts;

public class FilterGroup implements Serializable {

  private static final long serialVersionUID = 3317433416889397657L;

  private String uri; // unique identifier
  private String name;
  private String formId;
  private List<Filter> filters;

  public FilterGroup() {

  }

  public FilterGroup(String groupName, String formId, List<Filter> filters) {
    this.uri = UIConsts.URI_DEFAULT;
    this.name = groupName;
    this.formId = formId;
    this.filters = new ArrayList<Filter>();
    this.filters.addAll(filters);
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public FilterGroup(String uri) {
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

  public List<Filter> getFilters() {
    return filters;
  }

  public String getUri() {
    return uri;
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


}
