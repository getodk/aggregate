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
import java.util.List;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public abstract class TableFormatterBase implements SubmissionFormatter {

  protected final IForm form;
  protected final PrintWriter output;
  protected ElementFormatter elemFormatter;
  protected HeaderFormatter headerFormatter;
  protected List<FormElementModel> propertyNames;

  public TableFormatterBase(IForm xform, PrintWriter printWriter,
                            List<FormElementModel> selectedColumnNames) {
    form = xform;
    output = printWriter;
    propertyNames = selectedColumnNames;
    headerFormatter = new BasicHeaderFormatter(true, true, true);
  }

  @Override
  public final void processSubmissions(List<Submission> submissions, CallingContext cc) throws ODKDatastoreException {
    beforeProcessSubmissions(cc);
    processSubmissionSegment(submissions, cc);
    afterProcessSubmissions(cc);
  }

  @Override
  public final void beforeProcessSubmissions(CallingContext cc) {
    beforeProcessSubmissionSet(form.getTopLevelGroupElement(), cc);
  }

  @Override
  public final void processSubmissionSegment(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {
    processSubmissionSetSegment(submissions, form.getTopLevelGroupElement(), cc);
  }

  @Override
  public final void afterProcessSubmissions(CallingContext cc) {
    afterProcessSubmissionSet(form.getTopLevelGroupElement(), cc);
  }

  protected abstract void beforeProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc);

  protected abstract void processSubmissionSetSegment(Collection<? extends SubmissionSet> submissions,
                                                      FormElementModel rootGroup, CallingContext cc) throws ODKDatastoreException;

  protected abstract void afterProcessSubmissionSet(FormElementModel rootGroup, CallingContext cc);

  public final void processSubmissionSet(Collection<? extends SubmissionSet> submissions,
                                         FormElementModel rootGroup, CallingContext cc) throws ODKDatastoreException {
    beforeProcessSubmissionSet(rootGroup, cc);
    processSubmissionSetSegment(submissions, rootGroup, cc);
    afterProcessSubmissionSet(rootGroup, cc);
  }
}