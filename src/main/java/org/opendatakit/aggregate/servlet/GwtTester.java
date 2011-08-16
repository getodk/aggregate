package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.UIDisplayType;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.ResultFileInfo;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

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

    if (flag == null) {
      flag = BasicConsts.EMPTY_STRING;
    }

    String formId = "test1";

    // generate html
    beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info

    if (flag.equals("create")) {
      ArrayList<Filter> filters = new ArrayList<Filter>();
      filters.add(new RowFilter(Visibility.DISPLAY, new Column("Ro1Awesome", "", new Long(1)),
          FilterOperation.EQUAL, "captain", new Long(99)));

      ArrayList<ColumnFilterHeader> columns = new ArrayList<ColumnFilterHeader>();
      columns.add(new ColumnFilterHeader("ColAwesome1", "", new Long(1)));
      columns.add(new ColumnFilterHeader("ColAwesome2", "", new Long(1)));
      columns.add(new ColumnFilterHeader("ColAwesome3", "", new Long(1)));

      filters.add(new ColumnFilter(Visibility.DISPLAY, columns, new Long(5)));
      filters.add(new RowFilter(Visibility.HIDE, new Column("Ro1Awesome", "", UIDisplayType.TEXT),
          FilterOperation.EQUAL, "captain1", new Long(1)));
      FilterGroup group = new FilterGroup("group100", formId, filters);
      try {
        SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
        filterGrp.persist(cc);
        resp.getWriter().println("WORKED");
      } catch (Exception e) {
        resp.getWriter().println(e.getLocalizedMessage());
      }
    } else if (flag.equals("view")) {
      FilterSet filterSet = new FilterSet();

      try {
        List<SubmissionFilterGroup> filterGroupList = SubmissionFilterGroup.getFilterGroupList(
            formId, cc);
        for (SubmissionFilterGroup group : filterGroupList) {
          filterSet.addFilterGroup(group.transform());
        }
      } catch (Exception e) {
        // TODO: send exception over service
        e.printStackTrace();
      }

      for (FilterGroup group : filterSet.getGroups()) {
        resp.getWriter().println("GROUP: " + group.getName());
        for (Filter filter : group.getFilters()) {
          if (filter instanceof RowFilter) {
            RowFilter rf = (RowFilter) filter;
            resp.getWriter().println("   RowFilter: " + rf.getColumn().getDisplayHeader());
          } else if (filter instanceof ColumnFilter) {
            ColumnFilter cf = (ColumnFilter) filter;
            resp.getWriter().println("   ColumnFilter: ");
            for (ColumnFilterHeader header : cf.getColumnFilterHeaders()) {
              resp.getWriter().println("   ColumnHeader: " + header.getColumn().getDisplayHeader());
            }
          }
        }
      }

    } else if (flag.equals("query")) {

      SubmissionUISummary summary = new SubmissionUISummary();
      try {

        // Form form = Form.retrieveForm("widgets", cc);
        Form form = Form.retrieveForm("LocationThings", cc);
        QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, null, 1000, cc);
        List<Submission> submissions = query.getResultSubmissions(cc);

        GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(null, summary, form);
        headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
        List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();

        ElementFormatter elemFormatter = new BasicElementFormatter(true, true, true);

        // format row elements
        for (SubmissionSet sub : submissions) {
          Row row = sub.getFormattedValuesAsRow(filteredElements, elemFormatter, false, cc);
          try {
            summary.addSubmission(new SubmissionUI(row.getFormattedValues()));
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKDatastoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      resp.getWriter().println("Done with Query");

    } else if (flag.equals("export")) {

      try {
    	List<PersistentResults> results = PersistentResults.getAvailablePersistentResults(cc);

        ExportSummary[] exports = new ExportSummary[results.size()];

        int i = 0;
        for (PersistentResults export : results) {
          ExportSummary summary = new ExportSummary();

          summary.setFileType(export.getResultType());
          summary.setTimeRequested(export.getRequestDate());
          summary.setStatus(export.getStatus());
          summary.setTimeLastAction(export.getLastRetryDate());
          summary.setTimeCompleted(export.getCompletionDate());
          ResultFileInfo info = export.getResultFileInfo(cc);
          if (info != null) {
        	  summary.setResultFile(HtmlUtil.createHref(info.downloadUrl, "Download"));
          }
          exports[i] = summary;
          i++;
          
        }
        resp.getWriter().println("Done with Export");
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
      }

    } else if (flag.equals("createfusion")) {
      
      try {
        Form form = Form.retrieveForm("LocationThings", cc);
        FusionTable fusion = new FusionTable(form, ExternalServicePublicationOption.UPLOAD_N_STREAM, cc);;
        String uri =  fusion.getFormServiceCursor().getUri(); 
        String scope = FusionTableConsts.FUSION_SCOPE;
        String requestUrl = generateAuthorizationUrl(resp, cc, uri, scope);
        System.out.println(requestUrl);
        resp.getWriter().println(HtmlUtil.createHref(requestUrl, "Authorize FusionTable"));
      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKDatastoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
    } else if (flag.equals("createspreadsheet")) {
      
      try {
        Form form = Form.retrieveForm("LocationThings", cc);
        GoogleSpreadsheet spreadsheet = new GoogleSpreadsheet(form, "TESTWORKBOOK", ExternalServicePublicationOption.UPLOAD_N_STREAM, cc);
        String uri =  spreadsheet.getFormServiceCursor().getUri();
        String scope = SpreadsheetConsts.DOCS_SCOPE + BasicConsts.SPACE + SpreadsheetConsts.SPREADSHEETS_SCOPE;
        String requestUrl = generateAuthorizationUrl(resp, cc, uri, scope);            
        resp.getWriter().println(HtmlUtil.createHref(requestUrl, "Authorize Spreadsheet"));
      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKDatastoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
    } else {
      resp.getWriter().println("NO parameters");
    }

    finishBasicHtmlResponse(resp);
  }

  private String generateAuthorizationUrl(HttpServletResponse resp, CallingContext cc, String uri, String scope)
      throws IOException {
    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
    oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
    oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
    oauthParameters.setScope(scope);
    
    GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
    try 
    {
       oauthHelper.getUnauthorizedRequestToken(oauthParameters);
    } 
    catch (OAuthException e) 
    {
         e.printStackTrace();
         resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
             ErrorConsts.OAUTH_SERVER_REJECTED_ONE_TIME_USE_TOKEN);
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put(UIConsts.FSC_URI_PARAM, uri);
    params.put(ServletConsts.OAUTH_TOKEN_SECRET_PARAMETER, oauthParameters.getOAuthTokenSecret());
    String serverAddr = cc.getServerURL() + BasicConsts.FORWARDSLASH + OAuthServlet.ADDR; 
    String callbackUrl = HtmlUtil.createLinkWithProperties(serverAddr, params);
    
    oauthParameters.setOAuthCallback(callbackUrl);
    String requestUrl = oauthHelper.createUserAuthorizationUrl(oauthParameters);
    return requestUrl;
  }

}
