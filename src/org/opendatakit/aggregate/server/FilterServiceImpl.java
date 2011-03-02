package org.opendatakit.aggregate.server;

import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterSet;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilterServiceImpl extends RemoteServiceServlet implements FilterService {

  /**
   * identifier for serialization
   */
  private static final long serialVersionUID = 6350939191805868959L;

  @Override
  public FilterGroup addFilter(Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterGroup removeFilter(Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public FilterGroup maskAddFilter(Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public FilterGroup maskRemoveFilter(Filter filter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterSet createFilterGroup(FilterGroup group) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterSet removeFilterGroup(FilterGroup group) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public FilterSet maskAddFilterGroup(FilterGroup group) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FilterSet maskRemoveFilterGroup(FilterGroup group) {
    // TODO Auto-generated method stub
    return null;
  }

}
