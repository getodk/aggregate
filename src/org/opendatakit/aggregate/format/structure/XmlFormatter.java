package org.opendatakit.aggregate.format.structure;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.XmlElementFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.HtmlUtil;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class XmlFormatter implements SubmissionFormatter, RepeatCallbackFormatter {

  private XmlElementFormatter elemFormatter;

  private List<FormElementModel> propertyNames;

  private PrintWriter output;

  public XmlFormatter(PrintWriter printWriter,
      List<FormElementModel> selectedColumnNames, Form form, CallingContext cc) {
    output = printWriter;
    propertyNames = selectedColumnNames;
    elemFormatter = new XmlElementFormatter();
  }

  @Override
  public void processSubmissions(List<Submission> submissions, CallingContext cc)
      throws ODKDatastoreException {

    // format row elements
    for (Submission sub : submissions) {
      FormElementModel fem = sub.getFormElementModel();
      output.append(HtmlUtil.createBeginTag(fem.getElementName()));
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      Iterator<String> itr = row.getFormattedValues().iterator();
      while (itr.hasNext()) {
        output.append(itr.next());
      }
      output.append(HtmlUtil.createEndTag(fem.getElementName()));
    }
  }
  
  public void processRepeatedSubmssionSetsIntoRow(List<SubmissionSet> repeats,
      FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {

    output.append(HtmlUtil.createBeginTag(repeatElement.getElementName()));

    // format repeat row elements
    for (SubmissionSet repeat : repeats) {
      Row repeatRow = repeat.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      Iterator<String> itr = repeatRow.getFormattedValues().iterator();
      while (itr.hasNext()) {
        output.append(itr.next());
      }
    }

    output.append(HtmlUtil.createEndTag(repeatElement.getElementName()));

  }
    
}
