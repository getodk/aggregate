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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
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
public abstract class QueryBase {

  protected Query query;
  protected final Form form;
  
  private boolean moreRecords;
  private int fetchLimit;
  
  private int numOfRecords;
  
  protected QueryBase(Form form, int maxFetchLimit) throws ODKFormNotFoundException {
    fetchLimit = maxFetchLimit;
    numOfRecords = 0;
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
	if ( attribute.isMetadata() ) {
		DataField metaField;
		TopLevelDynamicBase tlb = ((TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype());
		switch ( attribute.getType() ) {
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

    for ( FormDataModel m : geoList ) {
       if ( m.getOrdinalNumber().equals(Long.valueOf(ordinal)) ) {
         query.addFilter(m.getBackingKey(), op, value);
       } 
    }
  }

  public abstract List<Submission> getResultSubmissions(CallingContext cc) throws ODKIncompleteSubmissionData, ODKDatastoreException;

  public boolean moreRecordsAvailable() {
    return moreRecords;
  }
  
  public final Form getForm(){
    return form;
  }
  
  /**
   * Generates a result table that contains all the submission data 
   * of the form specified by the ODK ID
   * 
   * @return
   *    a result table containing submission data
 * @throws ODKDatastoreException 
   *
   * @throws ODKIncompleteSubmissionData 
   */
  protected List<? extends CommonFieldsBase> getSubmissionEntities() throws ODKDatastoreException {

    // retrieve submissions
    List<? extends CommonFieldsBase> submissionEntities = null;
    submissionEntities = query.executeQuery(fetchLimit + 1);
    numOfRecords = submissionEntities.size();
    if(submissionEntities.size() > fetchLimit) {
      moreRecords = true;
      submissionEntities.remove(fetchLimit);
    }    
    return submissionEntities;
  }
  

  public int getNumRecords() {
    return numOfRecords;
  }
  
}
