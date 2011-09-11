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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.LinkElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class CsvFormatter extends TableFormatterBase implements SubmissionFormatter {

  public CsvFormatter(Form xform, String webServerUrl, PrintWriter printWriter,
      List<FormElementModel> selectedColumnNames) {
    super(xform, printWriter, selectedColumnNames);
    elemFormatter = new LinkElementFormatter(webServerUrl, FormMultipleValueServlet.ADDR, true, true, true);
    headerFormatter = new BasicHeaderFormatter(true, true, true);
  }

  @Override
  protected void processSubmissionSet(Collection<? extends SubmissionSet> submissions,
		  FormElementModel rootGroup, CallingContext cc) throws ODKDatastoreException {

    List<String> headers = headerFormatter.generateHeaders(form, rootGroup, propertyNames);
    // format headers
    appendCsvRow(headers.iterator());

    // format row elements
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      appendCsvRow(row.getFormattedValues().iterator());
    }
  }

  /**
   * Helper function used to append the comma separated value row
   * 
   * @param itr
   *          string values to be separated by commas
   * 
   */
  private void appendCsvRow(Iterator<String> itr) {
    output.append(BasicConsts.EMPTY_STRING);
    while (itr.hasNext()) {
      output.append(BasicConsts.QUOTE + itr.next() + BasicConsts.QUOTE);
      if (itr.hasNext()) {
        output.append(FormatConsts.CSV_DELIMITER);
      } else {
        output.append(BasicConsts.NEW_LINE);
      }
    }
  }

}
