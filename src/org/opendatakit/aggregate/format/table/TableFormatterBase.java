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
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class TableFormatterBase implements SubmissionFormatter {

  protected ElementFormatter elemFormatter;
  protected HeaderFormatter headerFormatter;
  protected List<FormElementModel> propertyNames;
  protected final Form form;
  protected final PrintWriter output;

  public TableFormatterBase(Form xform, PrintWriter printWriter,
      List<FormElementModel> selectedColumnNames) {
    form = xform;
    output = printWriter;
    propertyNames = selectedColumnNames;
    headerFormatter = new BasicHeaderFormatter(true, true, true);
  }

  @Override
  public void processSubmissions(List<Submission> submissions) throws ODKDatastoreException {
    processSubmissionSet(submissions, form.getFormDefinition().getTopLevelGroupElement());
  }

  protected abstract void processSubmissionSet(Collection<? extends SubmissionSet> submissions,
		  FormElementModel rootGroup) throws ODKDatastoreException;
  
}