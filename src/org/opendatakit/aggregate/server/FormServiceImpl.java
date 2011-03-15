package org.opendatakit.aggregate.server;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.SubmissionAssociationTable;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.servlet.KmlServlet;
import org.opendatakit.aggregate.servlet.KmlSettingsServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.KmlGenerator;
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

      ExportSummary[] exports = new ExportSummary[submissions.size()];

      int i = 0;
      for (Submission sub : submissions) {
        PersistentResults export = new PersistentResults(sub);
        ExportSummary summary = new ExportSummary();

        summary.setFileType(export.getResultType());
        summary.setTimeRequested(export.getRequestDate());
        summary.setStatus(export.getStatus());
        summary.setTimeLastAction(export.getLastRetryDate());
        summary.setTimeCompleted(export.getCompletionDate());

        // TODO: fix this as it seems bad to switch the type of interaction
        // midstream
        SubmissionValue blobSubmission = sub.getElementValue(PersistentResults.getResultFileKey());
        if (blobSubmission instanceof BlobSubmissionType) {
          BlobSubmissionType blob = (BlobSubmissionType) blobSubmission;
          SubmissionKey key = blob.getValue();
          summary.setResultFile(key.toString());
        }
        exports[i] = summary;
        i++;
      }

      return exports;

    } catch (ODKFormNotFoundException e) {
      return null;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Boolean createCsv(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      // create csv job
      Form form = Form.retrieveForm(formId, cc);
      PersistentResults r = new PersistentResults(ExportType.CSV, form, null, cc);
      r.persist(cc);

      // create csv task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      CsvGenerator generator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
      generator.createCsvTask(form, r.getSubmissionKey(), 1L, ccDaemon);

    } catch (ODKFormNotFoundException e1) {
      return false;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public KmlSettings getPossibleKmlSettings(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveForm(formId, cc);
      GenerateKmlSettings kmlSettings = new GenerateKmlSettings(form);
      return kmlSettings.generate();

    } catch (ODKFormNotFoundException e1) {
      return null;
    }
  }

  @Override
  public Boolean createKml(String formId, String geopointKey, String titleKey, String binaryKey) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (formId == null || geopointKey == null) {
      return false;
    }

    try {
      Form form = Form.retrieveForm(formId, cc);

      FormElementModel titleField = null;
      if (titleKey != null) {
        FormElementKey titleFEMKey = new FormElementKey(titleKey);
        titleField = FormElementModel.retrieveFormElementModel(form, titleFEMKey);
      }
      
      FormElementModel geopointField = null;
      if (geopointKey != null) {
        FormElementKey geopointFEMKey = new FormElementKey(geopointKey);
        geopointField = FormElementModel.retrieveFormElementModel(form, geopointFEMKey);
      }
      
      FormElementModel imageField = null;
      if (binaryKey != null) {
        FormElementKey imageFEMKey = new FormElementKey(binaryKey);
        imageField = FormElementModel.retrieveFormElementModel(form, imageFEMKey);
      }

      Map<String, String> params = new HashMap<String, String>();
      params.put(KmlServlet.TITLE_FIELD, (titleField == null) ? null : titleField
          .constructFormElementKey(form).toString());
      params.put(KmlServlet.IMAGE_FIELD, (imageField == null) ? KmlSettingsServlet.NONE
          : imageField.constructFormElementKey(form).toString());
      params.put(KmlServlet.GEOPOINT_FIELD, (geopointField == null) ? null : geopointField
          .constructFormElementKey(form).toString());

      PersistentResults r = new PersistentResults(ExportType.KML, form, params, cc);
      r.persist(cc);

      KmlGenerator generator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      generator.createKmlTask(form, r.getSubmissionKey(), 1L, ccDaemon);

    } catch (ODKFormNotFoundException e) {
      return false;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Boolean setFormDownloadable(String formId, Boolean downloadable) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveForm(formId, cc);
      form.setDownloadEnabled(downloadable);
      form.persist(cc);
      return true;
    } catch (ODKFormNotFoundException e1) {
      return false;
    } catch (ODKDatastoreException e) {
      return false;
    }
  }

  @Override
  public Boolean setFormAcceptSubmissions(String formId, Boolean acceptSubmissions) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveForm(formId, cc);
      form.setSubmissionEnabled(acceptSubmissions);
      form.persist(cc);
      return true;
    } catch (ODKFormNotFoundException e1) {
      return false;
    } catch (ODKDatastoreException e) {
      return false;
    }
  }

  @Override  
  public String createFusionTable(String formId, ExternalServiceOption esType) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveForm(formId, cc);
    //  FusionTable fusion = FusionTable.createFusionTable(form, authToken, esType, cc);
      return null;
    } catch (ODKFormNotFoundException e1) {
      return null;
    } 
    
  }
}
