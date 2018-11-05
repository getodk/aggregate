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

import java.util.List;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public abstract class QueryBase {

  private final IForm form;
  protected Query query;


  protected QueryBase(IForm form) {
    this.form = form;
  }

  /**
   * CAUTION: the attribute must be in the top-level record!
   *
   * @param attribute
   * @param op
   * @param value
   */
  public void addFilter(FormElementModel attribute, FilterOperation op,
                        Object value) {
    if (attribute.isMetadata()) {
      DataField metaField;
      TopLevelDynamicBase tlb = ((TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype());
      switch (attribute.getType()) {
        case META_INSTANCE_ID:
          metaField = tlb.primaryKey;
          break;
        case META_IS_COMPLETE:
          metaField = tlb.isComplete;
          break;
        case META_MODEL_VERSION:
          metaField = tlb.modelVersion;
          break;
        case META_SUBMISSION_DATE:
          metaField = tlb.submissionDate;
          break;
        case META_UI_VERSION:
          metaField = tlb.uiVersion;
          break;
        case META_DATE_MARKED_AS_COMPLETE:
          metaField = tlb.markedAsCompleteDate;
        default:
          throw new IllegalStateException("unknown Metadata type");
      }
      query.addFilter(metaField, op, value);
    } else {
      query.addFilter(attribute.getFormDataModel().getBackingKey(), op, value);
    }
  }

  public void addFilterGeoPoint(FormElementModel attr, Long ordinal, FilterOperation op,
                                Object value) {

    List<FormDataModel> geoList = attr.getFormDataModel().getChildren();

    for (FormDataModel m : geoList) {
      if (m.getOrdinalNumber().equals(Long.valueOf(ordinal))) {
        query.addFilter(m.getBackingKey(), op, value);
      }
    }
  }

  public abstract List<Submission> getResultSubmissions(CallingContext cc) throws ODKIncompleteSubmissionData, ODKDatastoreException;


  public final IForm getForm() {
    return form;
  }

  /**
   * Generates a QueryResultthat contains all the submission data
   * of the form specified by the ODK ID
   *
   * @return
   * @throws ODKDatastoreException
   */
  protected QueryResult getQueryResult(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException {
    return query.executeQuery(startCursor, fetchLimit);


  }


}
