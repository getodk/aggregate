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
package org.opendatakit.aggregate.query.submission;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.engine.EngineUtils;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryByDateRange extends QueryBase {

  final int fetchLimit;
  final QueryResumePoint startCursor;
  QueryResumePoint resumeCursor = null;
  
  public QueryByDateRange(IForm form, int maxFetchLimit, Date startDate, Date endDate, String uriLast, CallingContext cc) {
    super(form);
    this.fetchLimit = maxFetchLimit;
   
    TopLevelDynamicBase tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();
    
    // Query by markedAsCompleteDate, filtering by isCompleted.
    // Submissions may be partially uploaded and are marked completed once they 
    // are fully uploaded.  We want the query to be aware of that and to not 
    // report anything that is not yet fully loaded.
    query = cc.getDatastore().createQuery(tbl, "QueryByDateRange.constructor", cc.getCurrentUser());
    query.addSort(tbl.markedAsCompleteDate, Query.Direction.ASCENDING);
    query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.LESS_THAN, endDate);
    query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.GREATER_THAN_OR_EQUAL, startDate);
    query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, Boolean.TRUE);

    this.startCursor = (uriLast != null) ? new QueryResumePoint( tbl.markedAsCompleteDate.getName(),
        EngineUtils.getAttributeValueAsString(startDate, tbl.markedAsCompleteDate), uriLast, true) : null;
  }

  public QueryByDateRange(IForm form, int maxFetchLimit, Date startDate, String uriLast, CallingContext cc) {
    this(form, maxFetchLimit, startDate, new Date(System.currentTimeMillis() - PersistConsts.MAX_SETTLE_MILLISECONDS), uriLast, cc);
  }

  /**
   * Fetch the record with the most recent markedAsCompleteDate, excluding
   * records that arrived within the MAX_SETTLE of now. 
   * @param form
   * @param cc
   */
  public QueryByDateRange(IForm form, CallingContext cc) {
    super(form);
    this.fetchLimit = 1;
    this.startCursor = null;
    
    TopLevelDynamicBase tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();
    
    // Query by lastUpdateDate, filtering by isCompleted.
    // Submissions may be partially uploaded and are marked completed once they 
    // are fully uploaded.  We want the query to be aware of that and to not 
    // report anything that is not yet fully loaded.
    query = cc.getDatastore().createQuery(tbl, "QueryByDateRange.constructor", cc.getCurrentUser());
    query.addSort(tbl.markedAsCompleteDate, Query.Direction.DESCENDING);
    query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.LESS_THAN, new Date(System.currentTimeMillis() - PersistConsts.MAX_SETTLE_MILLISECONDS));
    query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, Boolean.TRUE);
  }
  
  @Override
  public List<Submission> getResultSubmissions(CallingContext cc) throws ODKIncompleteSubmissionData,
      ODKDatastoreException {

    List<Submission> retrievedSubmissions = new ArrayList<Submission>();

    QueryResult result = query.executeQuery(startCursor, fetchLimit);
    
    resumeCursor = result.getResumeCursor();
    
    // retrieve submissions
    List<? extends CommonFieldsBase> submissionEntities = result.getResultList();

    // create a row for each submission
    for (int count = 0; count < submissionEntities.size(); count++) {
    CommonFieldsBase subEntity = submissionEntities.get(count);
      retrievedSubmissions.add(new Submission((TopLevelDynamicBase) subEntity, getForm(), cc));
    }
    return retrievedSubmissions;
  }
  
  public QueryResumePoint getResumeCursor() {
    return resumeCursor;
  }

}
