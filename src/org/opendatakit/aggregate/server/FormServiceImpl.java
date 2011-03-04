package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.form.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.query.QueryFormList;
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

    CallingContext cc = ContextFactory.getCallingContext(this);

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

    CallingContext cc = ContextFactory.getCallingContext(this);

    try {
      Form form = Form.retrieveForm(formId, cc);
      List<ExternalService> esList = FormServiceCursor.getExternalServicesForForm(form, cc);

      ExternServSummary [] externServices;
      if(esList.size() > 0) {
        externServices = new ExternServSummary[esList.size()];
        
        for(int i = 0; i < esList.size(); i++) {
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

}
