package org.opendatakit.aggregate.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilterServiceImpl extends RemoteServiceServlet implements FilterService {

  /**
   * identifier for serialization
   */
  private static final long serialVersionUID = 6350939191805868959L;
 

  @Override
  public FilterSet getFilterSet(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    FilterSet filterSet = new FilterSet();
    
    try {
      List<SubmissionFilterGroup> filterGroupList = SubmissionFilterGroup.getFilterGroupList(formId, cc);
      for(SubmissionFilterGroup group : filterGroupList) {
        filterSet.addFilterGroup(group.transform());
      }
    } catch (Exception e) {
      // TODO: send exception over service
      e.printStackTrace();
    }
    return filterSet;
  }
  
  @Override
  public Boolean updateFilterGroup(FilterGroup group) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.persist(cc);      
      return Boolean.TRUE;
    } catch (Exception e) {
      return Boolean.FALSE;
    }
  }

  @Override
  public Boolean deleteFilterGroup(FilterGroup group) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    
    try {
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.delete(cc);      
      return Boolean.TRUE;
    } catch (Exception e) {
      return Boolean.FALSE;
    }
  }
}
