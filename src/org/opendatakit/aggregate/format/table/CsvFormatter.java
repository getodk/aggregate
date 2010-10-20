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

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.element.HtmlLinkElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class CsvFormatter extends TableFormatterBase implements SubmissionFormatter {

  public CsvFormatter(FormDefinition xform, String webServerUrl, PrintWriter printWriter,
      List<FormDataModel> selectedColumnNames) {
    super(xform, printWriter, selectedColumnNames);
    elemFormatter = new HtmlLinkElementFormatter(xform, webServerUrl, true, true, true);
    headerFormatter = new BasicHeaderFormatter(true, true, true);
  }

  @Override
  protected void processSubmissionSet(Collection<? extends SubmissionSet> submissions,
		  FormDataModel rootGroup) throws ODKDatastoreException {

    List<String> headers = headerFormatter.generateHeaders(formDefinition, rootGroup, propertyNames);
    // format headers
    appendCsvRow(headers.iterator());

    // format row elements
    for (SubmissionSet sub : submissions) {
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter);
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
