/*
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.aggregate.format.structure;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.XmlMediaAttachmentFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Constructs the list of media attachment name+url entries for a given submission.
 * Used in the new briefcase download mechanism.
 *
 * @author mitchellsundt@gmail.com
 */
public class XmlAttachmentFormatter implements SubmissionFormatter, RepeatCallbackFormatter {

  private XmlMediaAttachmentFormatter attachmentFormatter;

  private PrintWriter output;

  public XmlAttachmentFormatter(PrintWriter printWriter,
                                IForm form, CallingContext cc) {
    output = printWriter;
    attachmentFormatter = new XmlMediaAttachmentFormatter(this);
  }


  @Override
  public void beforeProcessSubmissions(CallingContext cc) {
  }

  @Override
  public void processSubmissionSegment(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    // format row elements
    for (Submission sub : submissions) {
      Row dataRow = new Row(sub.constructSubmissionKey(null));
      sub.getFormattedNamespaceValuesForRow(dataRow, Collections.singletonList(FormElementNamespace.VALUES), attachmentFormatter, false, cc);
      Iterator<String> itr = dataRow.getFormattedValues().iterator();
      while (itr.hasNext()) {
        output.append(itr.next());
      }
    }
  }

  @Override
  public void afterProcessSubmissions(CallingContext cc) {
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }

  public void processRepeatedSubmssionSetsIntoRow(List<SubmissionSet> repeats,
                                                  FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {

    // format repeat row elements
    for (SubmissionSet repeat : repeats) {
      Row repeatRow = repeat.getFormattedValuesAsRow(null, attachmentFormatter, false, cc);
      Iterator<String> itr = repeatRow.getFormattedValues().iterator();
      while (itr.hasNext()) {
        output.append(itr.next());
      }
    }
  }
}
