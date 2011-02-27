package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
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
  public FormSummary [] getForms() {

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

}
