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

package org.opendatakit.aggregate.query.submission;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.aggregate.server.UITrans;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.datamodel.ODKEnumeratedElementException;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryByUIFilterGroup extends QueryBase {
  private static final String MISSING_ARGS = "Missing either Form or FilterGroup making it impossible to query";
  private final CompletionFlag completionFlag;
  private final TopLevelDynamicBase tbl;
  private int fetchLimit;
  private QueryResumePoint cursor;

  public QueryByUIFilterGroup(IForm form, FilterGroup filterGroup, CompletionFlag completionFlag, CallingContext cc) {
    super(form);

    if (filterGroup == null || form == null) {
      throw new IllegalArgumentException(MISSING_ARGS);
    }

    this.completionFlag = completionFlag;

    this.tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel()
        .getBackingObjectPrototype();

    this.query = cc.getDatastore().createQuery(tbl, "QueryByUIFilterGroup.constructor", cc.getCurrentUser());

    boolean isForwardCursor = ((filterGroup.getCursor() == null) ?
        true :
        filterGroup.getCursor().getIsForwardCursor());

    switch (completionFlag) {
      case ONLY_COMPLETE_SUBMISSIONS:
        // order by the completion date and filter against isComplete == true
        if (isForwardCursor) {
          query.addSort(tbl.markedAsCompleteDate, Query.Direction.ASCENDING);
        } else {
          query.addSort(tbl.markedAsCompleteDate, Query.Direction.DESCENDING);
        }
        query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
        query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, true);
        break;
      case ONLY_INCOMPLETE_SUBMISSIONS:
        // we expect incomplete submissions to be a small fraction of
        // total submissions -- so filter by these, with subsidiary
        // filtering by lastUpdateDate.
        query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, false);
        query.addSort(tbl.isComplete, Query.Direction.ASCENDING); // gae optimization

        // order by the last update date and filter against isComplete == false
        if (isForwardCursor) {
          query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
        } else {
          query.addSort(tbl.lastUpdateDate, Query.Direction.DESCENDING);
        }
        query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
        break;
      case ALL_SUBMISSIONS:
        // order by the last update date
        if (isForwardCursor) {
          query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
        } else {
          query.addSort(tbl.lastUpdateDate, Query.Direction.DESCENDING);
        }
        query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
        break;
      default:
        throw new IllegalStateException("unhandled case");
    }

    fetchLimit = filterGroup.getQueryFetchLimit();

    UIQueryResumePoint uiCursor = filterGroup.getCursor();
    if (uiCursor != null) {
      cursor = QueryResumePoint.transform(uiCursor);
    } else {
      cursor = null;
    }

    for (Filter filter : filterGroup.getFilters()) {
      if (filter instanceof RowFilter) {
        RowFilter rf = (RowFilter) filter;
        Column column = rf.getColumn();

        FormElementKey decodeKey = new FormElementKey(column.getColumnEncoding());
        FormElementModel fem = FormElementModel.retrieveFormElementModel(form, decodeKey);
        FilterOperation op = UITrans.convertFilterOperation(rf.getOperation(), rf.getVisibility());

        Object compareValue = getCompareValue(fem, rf.getInput());

        switch (fem.getElementType()) {
          case BOOLEAN:
            super.addFilter(fem, op, compareValue);
            break;
          case JRDATETIME:
            if (fem.getFormDataModel() == null && fem.getElementName().contains("meta-"))
              super.addFilter(fem, op, compareValue);
            else if (fem.getFormDataModel() != null)
              super.addFilterChildren(fem, column.getChildColumnCode(), op, compareValue);
            else
              throw new IllegalArgumentException("Unrecognized dateTime field");
            break;
          case JRDATE:
            super.addFilterChildren(fem, column.getChildColumnCode(), op, compareValue);
            break;
          case JRTIME:
            super.addFilterChildren(fem, column.getChildColumnCode(), op, compareValue);
            break;
          case INTEGER:
            super.addFilter(fem, op, compareValue);
            break;
          case DECIMAL:
            super.addFilter(fem, op, compareValue);
            break;
          case SELECT1:
            super.addFilter(fem, op, compareValue);
            break;
          case STRING:
            super.addFilter(fem, op, compareValue);
            break;
          case GEOPOINT:
            super.addFilterChildren(fem, column.getChildColumnCode(), op, compareValue);
            break;
        }
      }
    }

  }

  public Object getCompareValue(FormElementModel fem, String value) {
    switch (fem.getElementType()) {
      case BOOLEAN:
        return WebUtils.parseBoolean(value);
      case JRDATETIME:
        return WebUtils.parseDate(value);
      case JRDATE:
        return WebUtils.parseDate(value);
      case JRTIME:
        return WebUtils.parseDate(value);
      case INTEGER:
        return Long.valueOf(value);
      case DECIMAL:
        return new BigDecimal(value);
      case SELECT1:
        return value;
      case STRING:
        return value;
      case GEOPOINT:
        return new BigDecimal(value);
      default:
        throw new IllegalArgumentException("Can't get the compare value for FormElementModel type " + fem.getElementType());
    }
  }

  public void addFilterByPrimaryDate(Query.FilterOperation operation, Date dateToFilter) {

    switch (completionFlag) {
      case ONLY_COMPLETE_SUBMISSIONS:
        query.addFilter(tbl.markedAsCompleteDate, operation, dateToFilter);
        break;
      case ONLY_INCOMPLETE_SUBMISSIONS:
        query.addFilter(tbl.lastUpdateDate, operation, dateToFilter);
        break;
      case ALL_SUBMISSIONS:
        query.addFilter(tbl.lastUpdateDate, operation, dateToFilter);
        break;
      default:
        throw new IllegalStateException("unhandled case");
    }

  }

  public List<Submission> getResultSubmissions(CallingContext cc) throws ODKDatastoreException {

    List<Submission> retrievedSubmissions = new ArrayList<Submission>();

    // retrieve submissions
    QueryResult results = getQueryResult(cursor, fetchLimit);
    List<? extends CommonFieldsBase> submissionEntities = results.getResultList();

    // create a row for each submission
    for (int count = 0; count < submissionEntities.size(); count++) {
      CommonFieldsBase subEntity = submissionEntities.get(count);
      try {
        Submission sub = new Submission((TopLevelDynamicBase) subEntity, getForm(), cc);
        retrievedSubmissions.add(sub);
      } catch (ODKDatastoreException e) {
        Logger logger = LoggerFactory.getLogger(QueryByUIFilterGroup.class);
        e.printStackTrace();
        logger.error("Unable to reconstruct submission for " +
            subEntity.getSchemaName() + "." + subEntity.getTableName() + " uri " + subEntity.getUri());

        if ((e instanceof ODKEntityNotFoundException) ||
            (e instanceof ODKEnumeratedElementException)) {
          // see if we should throw an error or skip processing...
          Boolean skip = ServerPreferencesProperties.getSkipMalformedSubmissions(cc);
          if (skip) {
            continue;
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    }

    // advance cursor...
    cursor = results.getResumeCursor();
    return retrievedSubmissions;
  }

  public List<TopLevelDynamicBase> getTopLevelSubmissionObjects(CallingContext cc) throws ODKDatastoreException {

    List<TopLevelDynamicBase> topLevelEntities = new ArrayList<TopLevelDynamicBase>();

    // retrieve submissions
    QueryResult results = getQueryResult(cursor, fetchLimit);
    List<? extends CommonFieldsBase> submissionEntities = results.getResultList();

    // create a row for each submission
    for (int count = 0; count < submissionEntities.size(); count++) {
      CommonFieldsBase subEntity = submissionEntities.get(count);
      topLevelEntities.add((TopLevelDynamicBase) subEntity);
    }

    // advance cursor...
    cursor = results.getResumeCursor();
    return topLevelEntities;
  }

  public void populateSubmissions(SubmissionUISummary summary, List<FormElementModel> filteredElements, ElementFormatter elemFormatter, List<FormElementNamespace> elementTypes, CallingContext cc) throws ODKDatastoreException {

    // retrieve submissions
    QueryResult results = getQueryResult(cursor, fetchLimit);
    FormElementModel fem = getForm().getTopLevelGroupElement();

    QueryResumePoint startCursor = results.getStartCursor();
    QueryResumePoint resumeCursor = results.getResumeCursor();
    QueryResumePoint backwardCursor = results.getBackwardCursor();

    // The SubmissionUISummary holds data as presented to the
    // UI layer.  Therefore, if we are paging backward, we need
    // to invert the sense of the query results.
    //
    // Determine whether we are going forward or backward...
    boolean isForwardCursor = true;
    if (startCursor != null) {
      isForwardCursor = startCursor.isForwardCursor();
    }

    if (isForwardCursor) {
      // everything matches across the UI and query layer...
      summary.setHasPriorResults(results.hasPriorResults());
      summary.setHasMoreResults(results.hasMoreResults());
      if (startCursor != null) {
        summary.setStartCursor(startCursor.transform());
      } else {
        summary.setStartCursor(null);
      }

      if (resumeCursor != null) {
        summary.setResumeCursor(resumeCursor.transform());
      } else {
        summary.setResumeCursor(null);
      }

      if (backwardCursor != null) {
        summary.setBackwardCursor(backwardCursor.transform());
      } else {
        summary.setBackwardCursor(null);
      }
    } else {
      // we are moving backward; the UI is inverted w.r.t. query.

      // SubmissionUISummary.hasPriorResults is query hasMoreResults
      // SubmissionUISummary.hasMoreResults is query hasPriorResults
      summary.setHasPriorResults(results.hasMoreResults());
      summary.setHasMoreResults(results.hasPriorResults());

      // SubmissionUISummary.startCursor is unchanged
      if (startCursor != null) {
        summary.setStartCursor(startCursor.transform());
      } else {
        summary.setStartCursor(null);
      }

      // SubmissionUISummary.resumeCursor is query backwardCursor
      if (backwardCursor != null) {
        summary.setResumeCursor(backwardCursor.transform());
      } else {
        summary.setResumeCursor(null);
      }

      // SubmissionUISummary.backwardCursor is query resumeCursor
      if (resumeCursor != null) {
        summary.setBackwardCursor(resumeCursor.transform());
      } else {
        summary.setBackwardCursor(null);
      }

    }

    List<SubmissionUI> submissionList = new ArrayList<SubmissionUI>();

    // create a row for each submission
    for (CommonFieldsBase subEntity : results.getResultList()) {
      try {
        Submission sub = new Submission((TopLevelDynamicBase) subEntity, getForm(), cc);
        Row row = sub.getFormattedValuesAsRow(elementTypes, filteredElements, elemFormatter, false,
            cc);

        SubmissionKey subKey = sub.constructSubmissionKey(fem);
        submissionList.add(new SubmissionUI(row.getFormattedValues(), subKey.toString()));
      } catch (ODKDatastoreException e) {
        Logger logger = LoggerFactory.getLogger(QueryByUIFilterGroup.class);
        e.printStackTrace();
        logger.error("Unable to reconstruct submission for " +
            subEntity.getSchemaName() + "." + subEntity.getTableName() + " uri " + subEntity.getUri());

        if ((e instanceof ODKEntityNotFoundException) ||
            (e instanceof ODKEnumeratedElementException)) {
          // see if we should throw an error or skip processing...
          Boolean skip = ServerPreferencesProperties.getSkipMalformedSubmissions(cc);
          if (skip) {
            continue;
          } else {
            throw e;
          }
        } else {
          throw e;
        }
      }
    }
    if (!isForwardCursor) {
      // query has the results in the reverse order.
      // invert them to get them properly ordered.
      Collections.reverse(submissionList);
    }
    summary.getSubmissions().addAll(submissionList);
  }

  public enum CompletionFlag {
    ONLY_COMPLETE_SUBMISSIONS,
    ONLY_INCOMPLETE_SUBMISSIONS,
    ALL_SUBMISSIONS
  }

}
