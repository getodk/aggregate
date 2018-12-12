/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format.table;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.LinkElementFormatter;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

public class CsvFormatterWithFilters implements SubmissionFormatter {

  private final IForm form;
  private final PrintWriter output;
  private ElementFormatter elemFormatter;
  private List<FormElementModel> propertyNames;
  private List<String> headers;
  private List<FormElementNamespace> namespaces;

  public CsvFormatterWithFilters(IForm xform, String webServerUrl, PrintWriter printWriter,
                                 FilterGroup filterGroup) {
    form = xform;
    output = printWriter;

    headers = new ArrayList<String>();
    SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());

    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    propertyNames = headerGenerator.getIncludedElements();
    namespaces = headerGenerator.includedFormElementNamespaces();

    for (Column col : summary.getHeaders()) {
      headers.add(col.getDisplayHeader());
    }
    elemFormatter = new LinkElementFormatter(webServerUrl, FormMultipleValueServlet.ADDR, true,
        true, true, false);
  }

  @Override
  public final void beforeProcessSubmissions(CallingContext cc) {
    // format headers
    appendCsvRow(headers.iterator());
  }

  @Override
  public final void processSubmissionSegment(List<Submission> submissions,
                                             CallingContext cc) throws ODKDatastoreException {
    // format row elements
    for (Submission sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(namespaces, propertyNames, elemFormatter, false, cc);
      appendCsvRow(row.getFormattedValues().iterator());
    }
  }

  @Override
  public final void afterProcessSubmissions(CallingContext cc) {
  }

  @Override
  public final void processSubmissions(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }

  /**
   * Helper function used to append the comma separated value row
   *
   * @param itr string values to be separated by commas
   */
  private void appendCsvRow(Iterator<String> itr) {
    output.append(BasicConsts.EMPTY_STRING);
    while (itr.hasNext()) {
      String value = itr.next();
      if (value != null) {
        // escape double quotes with another double quote per RFC 4180
        value = value.replaceAll(BasicConsts.QUOTE, BasicConsts.QUOTE_QUOTE);
        output.append(BasicConsts.QUOTE).append(value).append(BasicConsts.QUOTE);
      }
      if (itr.hasNext()) {
        output.append(FormatConsts.CSV_DELIMITER);
      } else {
        output.append(BasicConsts.NEW_LINE);
      }
    }
  }

}
