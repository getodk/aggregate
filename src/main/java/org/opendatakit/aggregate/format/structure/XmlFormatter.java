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
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.XmlAttributeFormatter;
import org.opendatakit.aggregate.format.element.XmlElementFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.HtmlUtil;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Xml formatter for a given submission.  Should roughly recreate the submission
 * xml as sent up from collect.
 *  
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class XmlFormatter implements SubmissionFormatter, RepeatCallbackFormatter {

  private Form form;
  private XmlAttributeFormatter attributeFormatter;
  private XmlElementFormatter elemFormatter;

  private List<FormElementModel> propertyNames;

  private PrintWriter output;

  public XmlFormatter(PrintWriter printWriter,
      List<FormElementModel> selectedColumnNames, Form form, CallingContext cc) {
    output = printWriter;
    this.form = form;
    propertyNames = selectedColumnNames;
    attributeFormatter = new XmlAttributeFormatter();
    elemFormatter = new XmlElementFormatter(this);
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {

    // format row elements
    for (Submission sub : submissions) {
      FormElementModel fem = sub.getFormElementModel();
	  Row attributeRow = new Row(sub.constructSubmissionKey(null));
	  // 
	  // add what could be considered the form's metadata...
	  // 
	  attributeRow.addFormattedValue("id=\"" + form.getFormId() + "\"");
	  if ( form.isEncryptedForm()) {
		  attributeRow.addFormattedValue("encrypted=\"yes\"");
	  }
	  sub.getFormattedNamespaceValuesForRow(attributeRow, Collections.singletonList(FormElementNamespace.METADATA), attributeFormatter, false, cc);
	  
	  output.append("<");
	  output.append(fem.getElementName());
	  Iterator<String> itrAttributes = attributeRow.getFormattedValues().iterator();
      while (itrAttributes.hasNext()) {
    	output.append(" ");
        output.append(itrAttributes.next());
      }
      output.append(">");
      Row dataRow = new Row(sub.constructSubmissionKey(null));
	  sub.getFormattedNamespaceValuesForRow(dataRow, Collections.singletonList(FormElementNamespace.VALUES), elemFormatter, false, cc);
	  Iterator<String> itr = dataRow.getFormattedValues().iterator();
      while (itr.hasNext()) {
        output.append(itr.next());
      }
      output.append(HtmlUtil.createEndTag(fem.getElementName()));
    }
  }
  
  public void processRepeatedSubmssionSetsIntoRow(List<SubmissionSet> repeats,
      FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {

	StringBuilder b = new StringBuilder();
    // format repeat row elements
    for (SubmissionSet repeat : repeats) {
      b.append(HtmlUtil.createBeginTag(repeatElement.getElementName()));
      Row repeatRow = repeat.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      Iterator<String> itr = repeatRow.getFormattedValues().iterator();
      while (itr.hasNext()) {
        b.append(itr.next());
      }
      b.append(HtmlUtil.createEndTag(repeatElement.getElementName()));
    }
    row.addFormattedValue(b.toString());
  }
    
}
