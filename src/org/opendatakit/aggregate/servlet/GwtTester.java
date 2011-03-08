package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.common.constants.BasicConsts;

public class GwtTester extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -150233761712061118L;
  
  /**
   * URI from base
   */
  public static final String ADDR = "www/gwttest";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Gwt Test";
  
  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
   CallingContext cc = ContextFactory.getCallingContext(this, req);

   // get parameter
   String flag = getParameter(req, "FLAG");
   
   if(flag == null) {
     flag = BasicConsts.EMPTY_STRING;
   }
   
   String formId = "test1";
   
   // generate html
   beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
   
   if(flag.equals("create")) {
     List<Filter> filters = new ArrayList<Filter>();
     filters.add(new RowFilter(Visibility.KEEP, new Column("Ro1Awesome", ""), FilterOperation.EQUAL, "captain", new Long(99)));

     List<ColumnFilterHeader> columns = new ArrayList<ColumnFilterHeader>();
     columns.add(new ColumnFilterHeader("ColAwesome1", ""));
     columns.add(new ColumnFilterHeader("ColAwesome2", ""));
     columns.add(new ColumnFilterHeader("ColAwesome3", ""));
     
     filters.add(new ColumnFilter(Visibility.KEEP, columns, new Long(5)));
     filters.add(new RowFilter(Visibility.REMOVE, new Column("Ro1Awesome", ""), FilterOperation.EQUAL, "captain1", new Long(1)));
     FilterGroup group = new FilterGroup("group1", formId, filters);
     try {
       SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
       filterGrp.persist(cc);      
       resp.getWriter().println("WORKED");
     } catch (Exception e) {
       resp.getWriter().println(e.getLocalizedMessage());
     }
   } else if(flag.equals("view")) {
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
     
     for(FilterGroup group : filterSet.getGroups()) {
       resp.getWriter().println("GROUP: " + group.getName());
       for(Filter filter : group.getFilters()) {
         if(filter instanceof RowFilter) {
           RowFilter rf = (RowFilter) filter;
           resp.getWriter().println("   RowFilter: " + rf.getColumn().getDisplayHeader());
         }else if(filter instanceof ColumnFilter) {
           ColumnFilter cf = (ColumnFilter) filter;
           resp.getWriter().println("   ColumnFilter: ");
           for(ColumnFilterHeader header : cf.getColumnFilterHeaders()) {
             resp.getWriter().println("   ColumnHeader: " + header.getColumn().getDisplayHeader());
           }
         }
       }
     }
     
   } else {
     resp.getWriter().println("NO parameters");
   }
   
   finishBasicHtmlResponse(resp);
  }


}
