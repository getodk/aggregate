/**
 * Copyright (C) 2012 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.format.structure;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.JsonElementFormatter;
import org.opendatakit.aggregate.server.GenerateHeaderInfo;
import org.opendatakit.aggregate.submission.Submission;
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
public class JsonFormatterWithFilters implements SubmissionFormatter, RepeatCallbackFormatter {

  private boolean first = true;

  private JsonElementFormatter elemFormatter;

  private List<FormElementModel> propertyNames;

  private PrintWriter output;

  public JsonFormatterWithFilters(PrintWriter printWriter, IForm form, FilterGroup filterGroup,
                                  BinaryOption binaryOption, boolean expandMultipleChoiceAsArray, String webServerUrl) {
    output = printWriter;

    if (binaryOption == BinaryOption.EMBED_BINARY) {
      elemFormatter = new JsonElementFormatter(true, true, true, expandMultipleChoiceAsArray, this);
    } else {
      elemFormatter = new JsonElementFormatter(webServerUrl, true, true, true,
          expandMultipleChoiceAsArray, this);
    }

    SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    propertyNames = headerGenerator.getIncludedElements();
  }

  @Override
  public void beforeProcessSubmissions(CallingContext cc) {
    output.append(BasicConsts.LEFT_BRACKET);
    first = true;
  }

  @Override
  public void processSubmissionSegment(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    // format row elements
    for (Submission sub : submissions) {
      if (!first) {
        output.append(FormatConsts.JSON_VALUE_DELIMITER);
      }
      first = false;
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      appendJsonObject(row.getFormattedValues().iterator());
    }
  }

  @Override
  public void afterProcessSubmissions(CallingContext cc) {
    output.append(BasicConsts.RIGHT_BRACKET);
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }

  @Override
  public void processRepeatedSubmssionSetsIntoRow(List<SubmissionSet> repeats,
                                                  FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {

    StringBuilder jsonString = new StringBuilder();
    jsonString.append(BasicConsts.QUOTE);
    jsonString.append(repeatElement.getElementName());
    jsonString.append(BasicConsts.QUOTE + BasicConsts.COLON);
    jsonString.append(BasicConsts.LEFT_BRACKET);
    boolean firstRepeatGroup = true;
    // format row elements
    for (int i = 0; i < repeats.size(); ++i) {
      if (!firstRepeatGroup) {
        jsonString.append(FormatConsts.JSON_VALUE_DELIMITER);
      }
      firstRepeatGroup = false;
      SubmissionSet repeat = repeats.get(i);
      Row repeatRow = repeat.getFormattedValuesAsRow(null, elemFormatter, false, cc);
      Iterator<String> itr = repeatRow.getFormattedValues().iterator();
      boolean firstElement = true;
      jsonString.append(BasicConsts.LEFT_BRACE);
      while (itr.hasNext()) {
        if (!firstElement) {
          jsonString.append(FormatConsts.JSON_VALUE_DELIMITER);
        }
        firstElement = false;
        jsonString.append(itr.next());
      }
      jsonString.append(BasicConsts.RIGHT_BRACE);
    }
    jsonString.append(BasicConsts.RIGHT_BRACKET);
    row.addFormattedValue(jsonString.toString());
  }

  /**
   * Helper function used to convert row to a JSON object and append to the
   * stream
   *
   * @param itr
   *          string values to be separated by commas
   */
  private void appendJsonObject(Iterator<String> itr) {
    output.append(BasicConsts.LEFT_BRACE);
    boolean firstElement = true;
    while (itr.hasNext()) {
      if (!firstElement) {
        output.append(FormatConsts.JSON_VALUE_DELIMITER);
      }
      firstElement = false;
      output.append(itr.next());
    }
    output.append(BasicConsts.RIGHT_BRACE);
  }
}
