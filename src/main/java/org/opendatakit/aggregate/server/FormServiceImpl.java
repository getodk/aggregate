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

package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.format.FormTableConsts;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.ResultFileInfo;
import org.opendatakit.aggregate.task.CsvGenerator;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.form.FormService {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -193679930586769386L;

  @Override
  public ArrayList<FormSummary> getForms() {

    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    ArrayList<FormSummary> formSummaries = new ArrayList<FormSummary>();

    try {
      // ensure that Form table exists...
      List<Form> forms = Form.getForms(false, cc);
      if (forms.size() == 0)
        return formSummaries;

      // get most recent form-deletion statuses
      Map<String, FormActionStatusTimestamp> formDeletionStatuses = MiscTasks
          .getFormDeletionStatusTimestampOfAllFormIds(cc);

      // get most recent purge-submissions statuses
      Map<String, FormActionStatusTimestamp> submissionPurgeStatuses = MiscTasks
          .getPurgeSubmissionsStatusTimestampOfAllFormIds(cc);

      for (Form form : forms) {
        FormSummary summary = form.generateFormSummary(cc);
        Date formLoadDate = summary.getCreationDate();
        formSummaries.add(summary);

        // the form could have been deleted and reloaded...
        // make sure that the action is after the creation date for
        // this instance of this form id.
        FormActionStatusTimestamp t;
        t = formDeletionStatuses.get(form.getFormId());
        if (t != null && t.getTimestamp().after(formLoadDate)) {
          summary.setMostRecentDeletionRequestStatus(t);
        }
        t = submissionPurgeStatuses.get(form.getFormId());
        if (t != null && t.getTimestamp().after(formLoadDate)) {
          summary.setMostRecentPurgeSubmissionsRequestStatus(t);
        }
      }
      return formSummaries;

    } catch (ODKDatastoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return formSummaries;
  }

  @Override
  public ArrayList<ExportSummary> getExports() {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    ArrayList<ExportSummary> exports = new ArrayList<ExportSummary>();
    try {
      List<PersistentResults> results = PersistentResults.getAvailablePersistentResults(cc);
      for (PersistentResults export : results) {
        ExportSummary summary = new ExportSummary();

        summary.setFileType(export.getResultType());
        summary.setTimeRequested(export.getRequestDate());
        summary.setStatus(export.getStatus());
        summary.setTimeLastAction(export.getLastRetryDate());
        summary.setTimeCompleted(export.getCompletionDate());

        // get info about the downloadable file.
        // null if no file yet....
        ResultFileInfo info = export.getResultFileInfo(cc);
        if (info != null) {
          String linkText = info.unrootedFilename;
          if (linkText == null || linkText.length() == 0) {
            linkText = FormTableConsts.DOWNLOAD_LINK_TEXT;
          }
          String url = HtmlUtil.createHref(info.downloadUrl, linkText);
          summary.setResultFile(url);
        }
        exports.add(summary);
      }

      return exports;

    } catch (ODKFormNotFoundException e) {
      return exports;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return exports;
  }

  @Override
  public Boolean createCsv(String formId, String filterUri) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // TODO: better error reporting -- form is being deleted. Disallow
      // exports.
      if (deletionTimestamp != null)
        return false;
      // create csv job
      Form form = Form.retrieveFormByFormId(formId, cc);
      if (form.getFormDefinition() == null)
        return false; // ill-formed definition
      PersistentResults r = new PersistentResults(ExportType.CSV, form, null, null, cc);
      r.persist(cc);

      // create csv task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      CsvGenerator generator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
      generator.createCsvTask(form, r.getSubmissionKey(), 1L, ccDaemon);
      return true;
    } catch (ODKFormNotFoundException e1) {
      e1.printStackTrace();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public KmlSettings getPossibleKmlSettings(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveFormByFormId(formId, cc);
      if (form.getFormDefinition() == null)
        return null; // ill-formed definition
      GenerateKmlSettings kmlSettings = new GenerateKmlSettings(form, false);
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
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(formId, cc);
      // TODO: better error reporting -- form is being deleted. Disallow
      // exports.
      if (deletionTimestamp != null)
        return false;

      Form form = Form.retrieveFormByFormId(formId, cc);
      if (form.getFormDefinition() == null)
        return false; // ill-formed definition

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
      params.put(KmlGenerator.TITLE_FIELD, (titleField == null) ? null : titleField
          .constructFormElementKey(form).toString());
      params.put(KmlGenerator.IMAGE_FIELD, (imageField == null) ? KmlGenerator.NONE : imageField
          .constructFormElementKey(form).toString());
      params.put(KmlGenerator.GEOPOINT_FIELD, (geopointField == null) ? null : geopointField
          .constructFormElementKey(form).toString());

      PersistentResults r = new PersistentResults(ExportType.KML, form, null, params, cc);
      r.persist(cc);

      KmlGenerator generator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      generator.createKmlTask(form, r.getSubmissionKey(), 1L, ccDaemon);
      return true;
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public KmlSettings getGpsCoordnates(String formId) {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      Form form = Form.retrieveFormByFormId(formId, cc);
      if (form.getFormDefinition() == null)
        return null; // ill-formed definition

      GenerateKmlSettings kmlSettings = new GenerateKmlSettings(form, true);
      return kmlSettings.generate();

    } catch (ODKFormNotFoundException e1) {
      return null;
    }
  }

  @Override
  public Boolean createCsvFromFilter(FilterGroup group) throws AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      FormActionStatusTimestamp deletionTimestamp = MiscTasks
          .getFormDeletionStatusTimestampOfFormId(group.getFormId(), cc);
      // TODO: better error reporting -- form is being deleted. Disallow
      // exports.
      if (deletionTimestamp != null)
        return false;

      // clear uri so a copy can be saved
      group.resetUriToDefault();

      // save the filter group
      SubmissionFilterGroup filterGrp = SubmissionFilterGroup.transform(group, cc);
      filterGrp.setName("FilterForExport");
      filterGrp.persist(cc);

      // create csv job
      Form form = Form.retrieveFormByFormId(filterGrp.getFormId(), cc);
      if (form.getFormDefinition() == null)
        return false; // ill-formed definition
      PersistentResults r = new PersistentResults(ExportType.CSV, form, filterGrp, null, cc);
      r.persist(cc);

      // create csv task
      CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
      ccDaemon.setAsDaemon(true);
      CsvGenerator generator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
      generator.createCsvTask(form, r.getSubmissionKey(), 1L, ccDaemon);
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
