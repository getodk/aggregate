package org.opendatakit.aggregate.query.submission;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.server.UITrans;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class QueryByUIFilterGroup extends QueryBase {

  private TopLevelDynamicBase tbl;

  public QueryByUIFilterGroup(Form form, FilterGroup filterGroup, int maxFetchLimit,
      CallingContext cc) throws ODKFormNotFoundException {
    super(form, maxFetchLimit, cc);

    tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel()
        .getBackingObjectPrototype();

    query = cc.getDatastore().createQuery(tbl, cc.getCurrentUser());

    for (Filter filter : filterGroup.getFilters()) {
      addFilterToQuery(filter);
    }

  }

  private void addFilterToQuery(Filter filter) {

    for (DataField field : tbl.getFieldList()) {
      if (field.getName().equals(filter.getCol())) {
        // TODO: ask mitch
        query.addFilter(field, UITrans.convertFilterOperation(filter.getOperation()),
            filter.getInput());
        return;
      }
    }
    // if we didn't find the field give up
  }

  public List<Submission> getResultSubmissions() throws ODKDatastoreException {

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
