package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.BasicElementFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmissionServiceImpl extends RemoteServiceServlet implements
org.opendatakit.aggregate.client.submission.SubmissionService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -7997978505247614945L;

  @Override
  public SubmissionUISummary getSubmissions(FilterGroup filterGroup) {
    
    CallingContext cc = ContextFactory.getCallingContext(this);    
    
    SubmissionUISummary summary = new SubmissionUISummary();
    try {
      String formId = filterGroup.getFormId();
      Form form = Form.retrieveForm(formId, cc);
      QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup, 1000, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);

      GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
      headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
      List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
      
      ElementFormatter elemFormatter = new BasicElementFormatter(true, true, true);
      
      // format row elements
      for (SubmissionSet sub : submissions) {
        Row row = sub.getFormattedValuesAsRow(filteredElements, elemFormatter, false, cc);
        try {
          summary.addSubmission(new SubmissionUI(row.getFormattedValues()));
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
    } catch (ODKFormNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    
    
    return summary;
  }

}
