package org.opendatakit.aggregate.server;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.form.FormService {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -193679930586769386L;

  @Override
  public FormSummary[] getForms() {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      // ensure that Form table exists...
      QueryFormList formsList = new QueryFormList(false, cc);
      List<Form> forms = formsList.getForms();
      FormSummary[] formSummary = new FormSummary[forms.size()];

      int index = 0;
      for (Form form : forms) {
        formSummary[index++] = form.generateFormSummary();
      }
      return formSummary;

    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ODKIncompleteSubmissionData e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public ExternServSummary[] getExternalServices(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);


    try {
      Form form = Form.retrieveForm(formId, cc);
      List<ExternalService> esList = FormServiceCursor.getExternalServicesForForm(form, cc);

      ExternServSummary[] externServices;
      if (esList.size() > 0) {
        externServices = new ExternServSummary[esList.size()];

        for (int i = 0; i < esList.size(); i++) {
          externServices[i] = esList.get(i).transform();
        }

        return externServices;

      } else {
        return null;
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public ExportSummary[] getExports() {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveForm(PersistentResults.FORM_ID_PERSISTENT_RESULT, cc);

      QueryByDate query = new QueryByDate(form, new Date(), true, ServletConsts.FETCH_LIMIT, cc);

      // query.addFilter(PersistentResults.getRequestingUserKey(),
      // FilterOperation.EQUAL, cc.getCurrentUser().getUriUser());

      List<Submission> submissions = query.getResultSubmissions(cc);      
      
      ExportSummary [] exports = new ExportSummary[submissions.size()] ;
      
      int i=0;
      for (Submission sub : submissions) {
        PersistentResults export = new PersistentResults(sub);
        ExportSummary summary = new ExportSummary();
        
        summary.setFileType(export.getResultType());
        summary.setTimeRequested(export.getRequestDate());
        summary.setStatus(export.getStatus());
        summary.setTimeLastAction(export.getLastRetryDate());
        summary.setTimeCompleted(export.getCompletionDate());
       
        // TODO: fix this as it seems bad to switch the type of interaction midstream
        SubmissionValue blobSubmission = sub.getElementValue(PersistentResults.getResultFileKey());
        if(blobSubmission instanceof BlobSubmissionType) {
          BlobSubmissionType blob = (BlobSubmissionType) blobSubmission;
          SubmissionKey key = blob.getValue();
          summary.setResultFile(key.toString());
        }
        exports[i] = summary;
        i++;
      }

      return exports;
      
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return null;
  }

}
