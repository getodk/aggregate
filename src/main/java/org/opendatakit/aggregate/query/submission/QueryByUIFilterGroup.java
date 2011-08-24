package org.opendatakit.aggregate.query.submission;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.server.UITrans;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

public class QueryByUIFilterGroup extends QueryBase {

  private TopLevelDynamicBase tbl;

  public QueryByUIFilterGroup(Form form, FilterGroup filterGroup, int maxFetchLimit,
      CallingContext cc) throws ODKFormNotFoundException {
    super(form, maxFetchLimit);

    tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel()
        .getBackingObjectPrototype();

    query = cc.getDatastore().createQuery(tbl, cc.getCurrentUser());
    query.addSort(tbl.lastUpdateDate, Query.Direction.ASCENDING);
    
    if(filterGroup == null) {
      return;
    }
    
    for (Filter filter : filterGroup.getFilters()) {
      if(filter instanceof RowFilter) {
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
    List<? extends CommonFieldsBase> submissionEntities = getSubmissionEntities();

    // create a row for each submission
    for (int count = 0; count < submissionEntities.size(); count++) {
      CommonFieldsBase subEntity = submissionEntities.get(count);
      retrievedSubmissions.add(new Submission((TopLevelDynamicBase) subEntity, form
          .getFormDefinition(), cc));
    }
    return retrievedSubmissions;
  }
}
