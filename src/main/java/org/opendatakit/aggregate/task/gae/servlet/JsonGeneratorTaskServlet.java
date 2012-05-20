package org.opendatakit.aggregate.task.gae.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.JsonFileWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

public class JsonGeneratorTaskServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -2571463127331034693L;

  /**
   * URI from base
   */
  public static final String ADDR = "gae/JsonFileGeneratorTask";
  
  
  /**
   * Handler for HTTP Get request to create the JSON file
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
   CallingContext cc = ContextFactory.getCallingContext(this, req);
   cc.setAsDaemon(true);

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String persistentResultsString = getParameter(req, ServletConsts.PERSISTENT_RESULTS_KEY);
    if ( persistentResultsString == null ) {
      errorBadParam(resp);
      return;
    }
    SubmissionKey persistentResultsKey = new SubmissionKey(persistentResultsString);
    String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if ( attemptCountString == null ) {
      errorBadParam(resp);
      return;
    }
    Long attemptCount = Long.valueOf(attemptCountString);

    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    IForm form = null;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      return;
    }
    
    if ( !form.hasValidFormDefinition() ) {
     errorRetreivingData(resp);
     return; // ill-formed definition
    }

    JsonFileWorkerImpl impl = new JsonFileWorkerImpl(form, persistentResultsKey, attemptCount, cc);   
    impl.generateJsonFile();
  }
}
