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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryByDate extends QueryBase {

  private boolean backward;

  public QueryByDate(Form form, Date lastDate,
      boolean backwardDirection, boolean secondaryOrderingByPrimaryKey, Boolean onlyCompleteSubmissions, int maxFetchLimit, CallingContext cc) {
    super(form);

    backward = backwardDirection;

    TopLevelDynamicBase tbl = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();
    
    query = cc.getDatastore().createQuery(tbl, "QueryByDate.constructor", cc.getCurrentUser());
    if (backward) {
    	query.addSort(tbl.markedAsCompleteDate, Query.Direction.DESCENDING);
    	query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.LESS_THAN, lastDate);
    } else {
    	query.addSort(tbl.markedAsCompleteDate, Query.Direction.ASCENDING);
    	query.addFilter(tbl.markedAsCompleteDate, Query.FilterOperation.GREATER_THAN, lastDate);
    }
    if (secondaryOrderingByPrimaryKey) {
    	query.addSort(tbl.primaryKey, Query.Direction.ASCENDING);
    }
    if ( onlyCompleteSubmissions ) {
    	query.addFilter(tbl.isComplete, FilterOperation.EQUAL, true);
    }
  }

  public QueryByDate(Form form, Date lastDate,
      boolean backwardDirection, int maxFetchLimit, CallingContext cc) throws ODKFormNotFoundException {
	  this(form, lastDate, backwardDirection, true, true, maxFetchLimit, cc);
  }
  
  @Override
  public List<Submission> getResultSubmissions(CallingContext cc) throws ODKDatastoreException {

    List<Submission> retrievedSubmissions = new ArrayList<Submission>();

    // retrieve submissions
    List<? extends CommonFieldsBase> submissionEntities = getSubmissionEntities();

    // create a row for each submission
    int count = 0;
    while (count < submissionEntities.size()) {
      CommonFieldsBase subEntity;
      if (backward) {
        subEntity = submissionEntities.get(submissionEntities.size() - 1 - count);
      } else {
        subEntity = submissionEntities.get(count);
      }

      retrievedSubmissions.add(new Submission((TopLevelDynamicBase) subEntity, getForm().getFormDefinition(), cc));
      count++;

    }
    return retrievedSubmissions;
  }

}
