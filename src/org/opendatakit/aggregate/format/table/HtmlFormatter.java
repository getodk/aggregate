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
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.HtmlLinkElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class HtmlFormatter extends TableFormatterBase implements SubmissionFormatter {

  private boolean checkboxes;
  
  public HtmlFormatter(FormDefinition formDefinition, String webServerUrl, PrintWriter printWriter, List<FormDataModel> selectedColumnNames, boolean includeCheckboxes) {
    super(formDefinition, printWriter, selectedColumnNames);
    checkboxes = includeCheckboxes;
    elemFormatter = new HtmlLinkElementFormatter(formDefinition, webServerUrl, true, true, true);
  }

  @Override
  public void processSubmissionSet(Collection<? extends SubmissionSet> submissions,
		  FormDataModel rootGroup) throws ODKDatastoreException {
    List<Row> formattedElements = new ArrayList<Row>();
    List<String> headers = headerFormatter.generateHeaders(formDefinition, rootGroup, propertyNames);

    // format row elements 
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter);
      formattedElements.add(row);
    }
    
    // format into html table
    output.append(HtmlUtil.wrapResultTableWithHtmlTags(checkboxes, headers, formattedElements));
    
    // TODO: consider reimplementing so we can stream out the html tags instead of writing them once.
    // TODO: consider reimplementing so there is only one loop of element formatting and table formatting
  }


  public void processSubmissionSetPublic(Collection<? extends SubmissionSet> submissions,
	FormDataModel rootElement) throws ODKDatastoreException {
	processSubmissionSet(submissions, rootElement);
  }

}
