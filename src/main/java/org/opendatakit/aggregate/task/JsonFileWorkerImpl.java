/*
 * Copyright (C) 2012 University of Washington
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.BinaryOption;
import org.opendatakit.aggregate.constants.common.ExportStatus;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.filter.SubmissionFilterGroup;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.structure.JsonFormatterWithFilters;
import org.opendatakit.aggregate.query.submission.QueryBase;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common worker implementation for the generation of json files.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class JsonFileWorkerImpl {

  private final Logger logger = LoggerFactory.getLogger(CsvWorkerImpl.class);
  private final IForm form;
  private final SubmissionKey persistentResultsKey;
  private final Long attemptCount;
  private final CallingContext cc;

  public JsonFileWorkerImpl(IForm form, SubmissionKey persistentResultsKey, Long attemptCount,
                            CallingContext cc) {
    this.form = form;
    this.persistentResultsKey = persistentResultsKey;
    this.attemptCount = attemptCount;
    this.cc = cc;
    if (attemptCount == null) {
      throw new IllegalArgumentException("attempt count cannot be null");
    }
  }

  public void generateJsonFile() {
    logger.info("Beginning JSON File generation: " + persistentResultsKey.toString() +
        " form " + form.getFormId());

    try {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      PrintWriter pw = new PrintWriter(new OutputStreamWriter(stream, HtmlConsts.UTF8_ENCODE));

      PersistentResults r = new PersistentResults(persistentResultsKey, cc);
      String filterGroupUri = r.getFilterGroupUri();

      // placeholder for clean-up...
      SubmissionFilterGroup subFilterGroup = null;

      // create CSV
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
      formatter = new JsonFormatterWithFilters(pw, form, filterGroup, BinaryOption.PROVIDE_LINKS, true, cc.getServerURL());

      logger.info("after setup of JSON file generation for " + form.getFormId());
      formatter.beforeProcessSubmissions(cc);
      List<Submission> submissions;
      int count = 0;
      for (; ; ) {
        count++;
        logger.info("iteration " + Integer.toString(count) + " before issuing query for " + form.getFormId());
        submissions = query.getResultSubmissions(cc);
        if (submissions.isEmpty()) break;
        logger.info("iteration " + Integer.toString(count) + " before emitting JSON for " + form.getFormId());
        formatter.processSubmissionSegment(submissions, cc);
      }
      logger.info("wrapping up JSON generation for " + form.getFormId());
      formatter.afterProcessSubmissions(cc);

      // output file
      pw.close();
      byte[] outputFile = stream.toByteArray();

      // refetch because this might have taken a while...
      r = new PersistentResults(persistentResultsKey, cc);
      if (attemptCount.equals(r.getAttemptCount())) {
        logger.info("saving JSON into PersistentResults table for " + form.getFormId());
        r.setResultFile(outputFile, HtmlConsts.RESP_TYPE_JSON,
            form.getViewableFormNameSuitableAsFileName() + ServletConsts.JSON_FILENAME_APPEND, false, cc);
        r.setStatus(ExportStatus.AVAILABLE);
        r.setCompletionDate(new Date());
        if (subFilterGroup != null) {
          subFilterGroup.delete(cc);
        }
        r.persist(cc);
      } else {
        logger.warn("stale JSON activity - do not save file in PersistentResults table for " + form.getFormId());
      }
    } catch (Exception e) {
      failureRecovery(e);
    }
  }

  private void failureRecovery(Exception e) {
    // four possible exceptions:
    // ODKFormNotFoundException, ODKDatastoreException,
    // ODKIncompleteSubmissionData, Exception
    logger.error("Exception caught: " + e.toString() + " for " + form.getFormId());
    e.printStackTrace();
    try {
      PersistentResults r = new PersistentResults(persistentResultsKey, cc);
      if (attemptCount.equals(r.getAttemptCount())) {
        logger.info("Exception recovery during JSON generation - mark as failed for " + form.getFormId());
        r.deleteResultFile(cc);
        r.setStatus(ExportStatus.FAILED);
        r.persist(cc);
      } else {
        logger.warn("Exception recovery during JSON generation - skipped - not the active attempt! for " + form.getFormId());
      }
    } catch (Exception ex) {
      // something is hosed -- don't attempt to continue.
      // watchdog: find this once lastRetryDate is late
      logger.error("Exception during exception recovery: " + ex.toString() + " for " + form.getFormId());
    }
  }

}
