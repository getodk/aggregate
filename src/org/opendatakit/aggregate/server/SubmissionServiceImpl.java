package org.opendatakit.aggregate.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.UiElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.common.constants.BasicConsts;
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
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    SubmissionUISummary summary = new SubmissionUISummary();
    try {
      String formId = filterGroup.getFormId();
      Form form = Form.retrieveForm(formId, cc);
      QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup, 1000, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);

      getSubmissions(filterGroup, cc, summary, form, submissions);
      
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

  private void getSubmissions(FilterGroup filterGroup, CallingContext cc,
      SubmissionUISummary summary, Form form, List<Submission> submissions)
      throws ODKDatastoreException {
    GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
    headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
    List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
    ElementFormatter elemFormatter = new UiElementFormatter(headerGenerator.getGeopointIncludes());

    
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
  }

  @Override
  public SubmissionUISummary getSubmissions(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);   
    
    SubmissionUISummary summary = new SubmissionUISummary();
    try {
      Form form = Form.retrieveForm(formId, cc);
      QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
          ServletConsts.FETCH_LIMIT, cc);
      List<Submission> submissions = query.getResultSubmissions(cc);

      getSubmissions(null, cc, summary, form, submissions);
      
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
