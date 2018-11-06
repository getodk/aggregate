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
import java.util.Collection;
import java.util.List;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.HtmlLinkElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class HtmlFormatter extends TableFormatterBase implements SubmissionFormatter {

  List<Row> formattedElements = new ArrayList<Row>();
  List<String> headers = null;

  private boolean checkboxes;

  public HtmlFormatter(IForm form, String webServerUrl, PrintWriter printWriter, List<FormElementModel> selectedColumnNames, boolean includeCheckboxes) {
    super(form, printWriter, selectedColumnNames);
    checkboxes = includeCheckboxes;
    elemFormatter = new HtmlLinkElementFormatter(webServerUrl, true, true, true);
  }

  @Override
  protected void beforeProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc) {
    formattedElements.clear();
    headers = headerFormatter.generateHeaders(form, rootGroup, propertyNames);
  }

  @Override
  protected void processSubmissionSetSegment(Collection<? extends SubmissionSet> submissions,
                                             FormElementModel rootGroup, CallingContext cc) throws ODKDatastoreException {
    // format row elements 
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      formattedElements.add(row);
    }
  }

  @Override
  protected void afterProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc) {
    // format into html table
    output.append(HtmlUtil.wrapResultTableWithHtmlTags(checkboxes, ServletConsts.RECORD_KEY, headers, formattedElements));
    formattedElements.clear();
    // TODO: consider reimplementing so we can stream out the html tags instead of writing them once.
    // TODO: consider reimplementing so there is only one loop of element formatting and table formatting
  }


  public void processSubmissionSetPublic(Collection<? extends SubmissionSet> submissions,
                                         FormElementModel formElementModel, CallingContext cc) throws ODKDatastoreException {
    processSubmissionSet(submissions, formElementModel, cc);
  }

}
