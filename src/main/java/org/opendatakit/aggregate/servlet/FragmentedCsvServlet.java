/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.table.FragmentedCsvFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDateRange;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to generate a CSV file for download, in parts!
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FragmentedCsvServlet extends ServletUtilBase {

  private static final long serialVersionUID = 9161862118534323521L;

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Download CSV Dataset Range";

  private static final String WEBSAFE_CURSOR_SEPARATOR = " and ";
  /**
   * URI from base
   */
  public static final String ADDR = "view/csvFragment";

  private static final int DEFAULT_NUM_ENTRIES = 1000;

  private void emitInfoPage(HttpServletRequest req, HttpServletResponse resp,
      String errorDescription, int status, CallingContext cc) throws IOException {
    beginBasicHtmlResponse(TITLE_INFO, resp, cc); // header info
    String requestPath = cc.getServerURL() + BasicConsts.FORWARDSLASH + ADDR;

    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, errorDescription));
    out.write(HtmlConsts.TABLE_OPEN);
    out.write(HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.TABLE_ROW,
        HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, "Parameter")
            + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, "Description"))
        + HtmlUtil.wrapWithHtmlTags(
            HtmlConsts.TABLE_ROW,
            HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, ServletConsts.FORM_ID)
                + HtmlUtil
                    .wrapWithHtmlTags(
                        HtmlConsts.TABLE_DATA,
                        "Required for accessing all data associated with a form.  This is a path rooted at the Form Identity displayed in the forms list."))
        + HtmlUtil.wrapWithHtmlTags(
            HtmlConsts.TABLE_ROW,
            HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, ServletConsts.NUM_ENTRIES)
                + HtmlUtil
                    .wrapWithHtmlTags(
                        HtmlConsts.TABLE_DATA,
                        "Optional.  The number of rows of data to return in a result csv.  If you are having transmission issues, you may need to reduce the number of records you fetch.  The default is 1000."))
        + HtmlUtil.wrapWithHtmlTags(
            HtmlConsts.TABLE_ROW,
            HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, ServletConsts.CURSOR)
                + HtmlUtil
                    .wrapWithHtmlTags(
                        HtmlConsts.TABLE_DATA,
                        "Optional.  Required for accessing subsequent blocks of data.  Supplied as the <cursor> value from the previous web request.")));
    out.write(HtmlConsts.TABLE_CLOSE);

    String formIdentity = "widgets";
    out.write(HtmlUtil
        .wrapWithHtmlTags(
            HtmlConsts.P,
            "To download a csv fragment for the non-repeating elements of a form, append the Form Identifier and the number of entries to fetch to this url, e.g., "));
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.FORM_ID
        + "=" + formIdentity + "&" + ServletConsts.NUM_ENTRIES + "=1000"));

    out.write(HtmlUtil
        .wrapWithHtmlTags(
            HtmlConsts.P,
            "The "
                + ServletConsts.FORM_ID
                + " parameter supports an xpath-like specification of repeat groups within a form (e.g., widgets/widgets/repeat_a) and primary key restrictions on the last or next-to-last element in the path."));
    out.write(HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.UL,
        HtmlUtil.wrapWithHtmlTags(
            HtmlConsts.LI,
            HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.FORM_ID
                + "=widgets/widgets/repeat_a")
                + " returns all repeat_a rows.")
            + HtmlUtil.wrapWithHtmlTags(
                HtmlConsts.LI,
                HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.FORM_ID
                    + "=widgets/widgets[@key=\"aaaa\"]/repeat_a")
                    + " returns all repeat_a rows for the widgets record identified by key \"aaaa\".")
            + HtmlUtil.wrapWithHtmlTags(
                HtmlConsts.LI,
                HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.FORM_ID
                    + "=widgets/widgets/repeat_a[@key=\"bbb\"]")
                    + " returns the repeat_a row identified by key \"bbb\".")));
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P,
        "The data returned is a text/xml document as follows:"));
    out.write(HtmlUtil
        .wrapWithHtmlTags(
            HtmlConsts.PRE,
            "&lt;entries&gt;\n"
                + "  &lt;cursor&gt;...&lt;/cursor&gt; &lt;!-- only present if additional records may be fetched --&gt;\n"
                + "  &lt;header&gt;...&lt;/header&gt; &lt;!-- csv -- property names --&gt;\n"
                + "  &lt;result&gt;...&lt;/result&gt; &lt;!-- csv -- values -- repeats 0 or more times --&gt;\n"
                + "&lt;/entries&gt;\n"));
    out.write(HtmlUtil
        .wrapWithHtmlTags(
            HtmlConsts.P,
            "The returned form data includes an additional property (as the right-most column): KEY.  The KEY value is the URL for this item on the Aggregate server."));
    out.write(HtmlUtil
        .wrapWithHtmlTags(
            HtmlConsts.P,
            "The returned repeated group data within a form includes two additional properties (as the next-to-right-most and right-most columns): PARENT_KEY and KEY.  The PARENT_KEY value is the URL for the parent item of this repeated group on the Aggregate server; the KEY value is the URL for this repeated group item on the Aggregate server."));

    resp.setStatus(status);
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP Get request that responds with CSV
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // required common parameters
    // form or form element identity
    // -- a forward-slash separated list of identity and group or repeat names
    // that identifies the Form or FormElement to retrieve. It is a form
    // if the path has one or two entries, otherwise it is a repeat group.
    String submissionKeyString = getParameter(req, ServletConsts.FORM_ID);
    if (submissionKeyString == null || submissionKeyString.length() == 0) {
      emitInfoPage(req, resp, ErrorConsts.INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST, cc);
      return;
    }
    SubmissionKey submissionKey = new SubmissionKey(submissionKeyString);
    List<SubmissionKeyPart> submissionKeyParts = submissionKey.splitSubmissionKey();

    // optional common parameters
    // for client-side simplicity, if these have "" values, treat them as null

    // cursor -- tracks where we resume our record fetch (if missing, we start
    // over)
    String websafeCursorString = getParameter(req, ServletConsts.CURSOR);
    Date dateCode = null;
    String uriAfter = null;
    if (websafeCursorString == null || websafeCursorString.length() == 0) {
      websafeCursorString = null;
      dateCode = BasicConsts.EPOCH;
    } else {
      int idx = websafeCursorString.indexOf(WEBSAFE_CURSOR_SEPARATOR);
      if (idx == -1) {
        emitInfoPage(req, resp, ErrorConsts.INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST, cc);
        return;
      }
      String dateString = websafeCursorString.substring(0, idx);
      uriAfter = websafeCursorString.substring(idx + WEBSAFE_CURSOR_SEPARATOR.length());
      try {
        dateCode = new Date(Long.valueOf(dateString));
      } catch (NumberFormatException e) {
        emitInfoPage(req, resp, ErrorConsts.INVALID_PARAMS, HttpServletResponse.SC_BAD_REQUEST, cc);
        return;
      }
    }

    // number of records to fetch
    String numEntriesStr = getParameter(req, ServletConsts.NUM_ENTRIES);
    if (numEntriesStr != null && numEntriesStr.length() == 0) {
      numEntriesStr = null;
    }

    try {
      int numEntriesToFetch = 0;
      try {
        if (numEntriesStr == null) {
          numEntriesToFetch = DEFAULT_NUM_ENTRIES;
        } else {
          numEntriesToFetch = Integer.parseInt(numEntriesStr);
        }
      } catch (NumberFormatException e) {
        throw new ODKParseException("Invalid number of entries parameter", e);
      }

      if (submissionKeyParts.size() >= 2 && submissionKeyParts.get(1).getAuri() != null
          && numEntriesToFetch > 0) {
        IForm form = FormFactory.retrieveFormByFormId(submissionKeyParts.get(0).getElementName(), cc);
        if (!form.hasValidFormDefinition()) {
          errorRetreivingData(resp);
          return; // ill-formed definition
        }
        Submission sub = Submission.fetchSubmission(submissionKeyParts, cc);
        FormElementModel m = form.getFormElementModel(submissionKeyParts);
        SubmissionElement elem = sub.resolveSubmissionKey(submissionKeyParts);
        List<SubmissionSet> submissions = new ArrayList<SubmissionSet>();
        if (elem instanceof RepeatSubmissionType) {
          RepeatSubmissionType r = (RepeatSubmissionType) elem;
          submissions.addAll(r.getSubmissionSets());
        } else if (elem instanceof SubmissionSet) {
          submissions.add((SubmissionSet) elem);
        }

        websafeCursorString = null;
        resp.setContentType("text/xml; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        FragmentedCsvFormatter fmt = new FragmentedCsvFormatter(form, submissionKeyParts,
            cc.getServerURL(), websafeCursorString, out);
        fmt.processSubmissionSet(submissions, m, cc);
      } else if (((submissionKeyParts.size() == 2 && submissionKeyParts.get(1).getAuri() == null) || (submissionKeyParts
          .size() == 1)) && numEntriesToFetch > 0) {
        // top-level form has no parent...
        // top-level form can be referenced either by just "form-identity" or by
        // "form-identity/top-level-tag"
        // reworked from formxmlservlet.java
        IForm form = FormFactory.retrieveFormByFormId(submissionKeyParts.get(0).getElementName(), cc);

        QueryByDateRange query = new QueryByDateRange(form, numEntriesToFetch, dateCode, uriAfter, cc);
        List<Submission> submissions = query.getResultSubmissions(cc);

        if (!submissions.isEmpty()) {
          QueryResumePoint resumeCursor = query.getResumeCursor();
          Date resumeDate = WebUtils.parseDate(resumeCursor.getValue());
          websafeCursorString = Long.toString(resumeDate.getTime())
              + " and " + resumeCursor.getUriLastReturnedValue();
        } else {
          websafeCursorString = null;
        }

        resp.setContentType("text/xml; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        FragmentedCsvFormatter fmt = new FragmentedCsvFormatter(form, submissionKeyParts,
            cc.getServerURL(), websafeCursorString, out);
        fmt.processSubmissions(submissions, cc);

      } else {
        emitInfoPage(req, resp, "Parameters are not correctly specified.",
            HttpServletResponse.SC_BAD_REQUEST, cc);
      }
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
    } catch (ODKParseException e) {
      e.printStackTrace();
      emitInfoPage(req, resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      errorRetreivingData(resp);
    } catch (ODKIncompleteSubmissionData e) {
      e.printStackTrace();
      errorRetreivingData(resp);
    }
  }
}
