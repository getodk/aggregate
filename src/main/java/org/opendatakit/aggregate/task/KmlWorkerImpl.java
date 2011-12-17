/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.task;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.ExportStatus;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.structure.KmlFormatterWithFilters;
import org.opendatakit.aggregate.query.submission.QueryBase;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Common worker implementation for the generation of kml files.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class KmlWorkerImpl {

  private final IForm form;
  private final SubmissionKey persistentResultsKey;
  private final Long attemptCount;
  private final FormElementModel titleField;
  private final FormElementModel geopointField;
  private final FormElementModel imageField;
  private final CallingContext cc;

  public KmlWorkerImpl(IForm form, SubmissionKey persistentResultsKey, long attemptCount,
      FormElementModel titleField, FormElementModel geopointField, FormElementModel imageField,
      CallingContext cc) {
    this.form = form;
    this.persistentResultsKey = persistentResultsKey;
    this.attemptCount = attemptCount;
    this.titleField = titleField;
    this.geopointField = geopointField;
    this.imageField = imageField;
    this.cc = cc;
  }

  public void generateKml() {
    Log logger = LogFactory.getLog(KmlWorkerImpl.class);
    logger.info("Beginning KML generation: " + persistentResultsKey.toString() +
                " form " + form.getFormId());


    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(stream);

      PersistentResults r = new PersistentResults(persistentResultsKey, cc);
      String filterGroupUri = r.getFilterGroupUri();

      // placeholder for clean-up...
      SubmissionFilterGroup subFilterGroup = null;

      // create KML
      QueryBase query;
      SubmissionFormatter formatter;
      FilterGroup filterGroup;
      
      // figure out the filterGroup...
      if (filterGroupUri == null) {
        filterGroup = new FilterGroup(UIConsts.FILTER_NONE, form.getFormId(), null);
      } else {
        subFilterGroup = SubmissionFilterGroup.getFilterGroup(filterGroupUri, cc);
        filterGroup = subFilterGroup.transform();
      }
      filterGroup.setQueryFetchLimit(ServletConsts.EXPORT_CURSOR_CHUNK_SIZE);

      query = new QueryByUIFilterGroup(form, filterGroup, CompletionFlag.ONLY_COMPLETE_SUBMISSIONS, cc);
      formatter = new KmlFormatterWithFilters(form, cc.getServerURL(), geopointField,
          titleField, imageField, pw, filterGroup, cc);
      
      formatter.beforeProcessSubmissions(cc);
      List<Submission> submissions;
      for (;;) {
        submissions = query.getResultSubmissions(cc);
        if ( submissions.isEmpty()) break;
        formatter.processSubmissionSegment(submissions, cc);
      }
      formatter.afterProcessSubmissions(cc);

      // output file
      pw.close();
      byte[] outputFile = stream.toByteArray();

      // refetch because this might have taken a while...
      r = new PersistentResults(persistentResultsKey, cc);
      if (attemptCount.equals(r.getAttemptCount())) {
        r.setResultFile(outputFile, HtmlConsts.RESP_TYPE_KML, Long.valueOf(outputFile.length),
            form.getViewableFormNameSuitableAsFileName() + ServletConsts.KML_FILENAME_APPEND, cc);
        r.setStatus(ExportStatus.AVAILABLE);
        r.setCompletionDate(new Date());
        if(subFilterGroup != null) {
          subFilterGroup.delete(cc);
        }
        r.persist(cc);
      }
    } catch (Exception e) {
      failureRecovery(e);
    }
  }

  private void failureRecovery(Exception e) {
    // three exceptions possible:
    // ODKFormNotFoundException, ODKDatastoreException, Exception
    e.printStackTrace();
    try {
      PersistentResults r = new PersistentResults(persistentResultsKey, cc);
      if (attemptCount.equals(r.getAttemptCount())) {
        r.deleteResultFile(cc);
        r.setStatus(ExportStatus.FAILED);
        r.persist(cc);
      }
    } catch (Exception ex) {
      // something is hosed -- don't attempt to continue.
      // TODO: watchdog: find this once lastRetryDate is way late?
    }
  }

}
