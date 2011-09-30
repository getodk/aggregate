package org.opendatakit.aggregate.query.submission;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.server.UITrans;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

public class QueryByUIFilterGroup extends QueryBase {

  public enum CompletionFlag {
     ONLY_COMPLETE_SUBMISSIONS,
     ONLY_INCOMPLETE_SUBMISSIONS,
     ALL_SUBMISSIONS
  };
  
  private static final String MISSING_ARGS = "Missing either Form or FilterGroup making it impossible to query";

  private final TopLevelDynamicBase tbl;

  private int fetchLimit;
  
  private final QueryResumePoint cursor;
  
  public QueryByUIFilterGroup(Form form, FilterGroup filterGroup, CompletionFlag completionFlag, CallingContext cc) {
    super(form);

    if (filterGroup == null || form == null) {
      throw new IllegalArgumentException(MISSING_ARGS);
    }
    
    tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel()
        .getBackingObjectPrototype();

    query = cc.getDatastore().createQuery(tbl, "QueryByUIFilterGroup.constructor", cc.getCurrentUser());
    switch ( completionFlag ) {
    case ONLY_COMPLETE_SUBMISSIONS:
      // order by the completion date and filter against isComplete == true
      query.addSort(tbl.markedAsCompleteDate, Query.Direction.ASCENDING);
      query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
      query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, true);
      break;
    case ONLY_INCOMPLETE_SUBMISSIONS:
      // order by the last update date and filter against isComplete == false
      query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
      query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
      query.addFilter(tbl.isComplete, Query.FilterOperation.EQUAL, false);
      break;
    case ALL_SUBMISSIONS:
      // order by the last update date
      query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
      query.addFilter(tbl.lastUpdateDate, Query.FilterOperation.GREATER_THAN, BasicConsts.EPOCH);
      break;
    default:
        throw new IllegalStateException("unhandled case");
    }
    
    fetchLimit = filterGroup.getQueryFetchLimit();
    
    UIQueryResumePoint uiCursor = filterGroup.getCursor();
    if(uiCursor != null) {
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
        FilterOperation op = UITrans.convertFilterOperation(rf.getOperation());

        String value = rf.getInput();
        Object compareValue = null;
        switch (fem.getElementType()) {
        case BOOLEAN:
          compareValue = WebUtils.parseBoolean(value);
          break;
        case JRDATETIME:
          compareValue = WebUtils.parseDate(value);
          break;
        case JRDATE:
          compareValue = WebUtils.parseDate(value);
          break;
        case JRTIME:
          compareValue = WebUtils.parseDate(value);
          break;
        case INTEGER:
          compareValue = Long.valueOf(value);
          break;
        case DECIMAL:
          compareValue = new BigDecimal(value);
          break;
        case SELECT1:
        case STRING:
          compareValue = value;
          break;
        case GEOPOINT:
          compareValue = new BigDecimal(value);
          super.addFilterGeoPoint(fem, column.getGeopointColumnCode(), op, compareValue);
          continue;
        default:
          // e.g., SELECTN
          // can't apply a filter to this type
          continue;
        }

        super.addFilter(fem, op, compareValue);
      }
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
      Submission sub = new Submission((TopLevelDynamicBase) subEntity, getForm()
          .getFormDefinition(), cc);
      retrievedSubmissions.add(sub);
    }
    return retrievedSubmissions;
  }
  
  public void populateSubmissions(SubmissionUISummary summary,
      List<FormElementModel> filteredElements, ElementFormatter elemFormatter,
      List<FormElementNamespace> elementTypes, CallingContext cc) throws ODKDatastoreException {

    // retrieve submissions
    QueryResult results = getQueryResult(cursor, fetchLimit);
    FormDefinition formDef = getForm().getFormDefinition();
    FormElementModel fem = getForm().getTopLevelGroupElement();
    
    QueryResumePoint startCursor = results.getStartCursor();
    QueryResumePoint resumeCursor = results.getResumeCursor();
    QueryResumePoint backwardCursor = results.getBackwardCursor();
    
    if(startCursor != null) {
      summary.setStartCursor(startCursor.transform());
    } else {
      summary.setStartCursor(null);
    }
    
    if(resumeCursor != null) {
      summary.setResumeCursor(resumeCursor.transform());
    } else {
      summary.setResumeCursor(null);
    }
    
    if(backwardCursor != null) {
      summary.setBackwardCursor(backwardCursor.transform());
    } else {
      summary.setBackwardCursor(null);      
    }
    
    // create a row for each submission
    for (CommonFieldsBase subEntity : results.getResultList()) {
      Submission sub = new Submission((TopLevelDynamicBase) subEntity, formDef, cc);
      Row row = sub.getFormattedValuesAsRow(elementTypes, filteredElements, elemFormatter, false,
          cc);

      SubmissionKey subKey = sub.constructSubmissionKey(fem);
      summary.addSubmission(new SubmissionUI(row.getFormattedValues(), subKey.toString()));
    }
  }

}
