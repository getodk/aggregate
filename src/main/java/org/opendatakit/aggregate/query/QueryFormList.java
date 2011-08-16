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
package org.opendatakit.aggregate.query;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryFormList {

  private List<Form> forms;
  
  /**
   * Private constructor for common work to both constructors
   * 
   * @param requestingUser user that is requesting the forms to be queried
   * @param datastore   datastore reference
   */
  
  private QueryFormList() {
    forms = new ArrayList<Form>();    
  }

  /**
   * Constructor that queries the database for available forms
   * 
   * @param requestingUser user that is requesting the forms to be queried
   * @param checkAuthorization true if authorization rules should be used to filter form list, false otherwise
   * @param datastore  datastore reference

   * @throws ODKDatastoreException
 * @throws ODKIncompleteSubmissionData 
   */
  public QueryFormList(boolean checkAuthorization, CallingContext cc) throws ODKDatastoreException, ODKIncompleteSubmissionData{
    this();
    
    Query formQuery = cc.getDatastore().createQuery(Form.getFormInfoRelation(cc), cc.getCurrentUser());
    List<? extends CommonFieldsBase> formEntities = formQuery.executeQuery(ServletConsts.FETCH_LIMIT);
    for (CommonFieldsBase formEntity : formEntities) {
      Form form = new Form((TopLevelDynamicBase) formEntity, cc);
      if ( form.getFormId().equals(Form.URI_FORM_ID_VALUE_FORM_INFO)) continue;
      addIfAuthorized(form, checkAuthorization, cc);
    }
  }
  
  /**
   * Constructor that queries the database for the forms referenced by the formkeys. List will only contain forms that are specified in arguments
   * 
   * @param submissionKeys
   * @param requestingUser user that is requesting the forms to be queried
   * @param checkAuthorization true if authorization rules should be used to filter form list, false otherwise
   * @param datastore  datastore reference
 * @throws ODKDatastoreException 
   */
  public QueryFormList(List<SubmissionKey> submissionKeys, boolean checkAuthorization, CallingContext cc) throws ODKDatastoreException {
    this();
    
    for (SubmissionKey submissionKey : submissionKeys) {
      try {
		List<SubmissionKeyPart> parts = submissionKey.splitSubmissionKey();
		if ( parts.size() != 2 ) {
			throw new ODKIncompleteSubmissionData();
		}
		if ( !parts.get(0).getElementName().equals(Form.URI_FORM_ID_VALUE_FORM_INFO) ) {
			throw new ODKIncompleteSubmissionData();
		}

        Form form = new Form(parts.get(1).getAuri(), cc);
        addIfAuthorized(form, checkAuthorization, cc);
      } catch (Exception e) {
        // TODO: determine how to better handle error
      }
    }
  }

  /**
   * Get the resulting Forms from the query
   * @return list of Forms
   */
  public List<Form> getForms() {
    return forms;
  }
  
  /**
   * Apply's authorization logic to determine whether form should be included.
   * 
   * @param form possible form to be included in result list
   * @param checkAuthorization true if authorization rules should be used to filter form list, false otherwise
   */
  private void addIfAuthorized(Form form, boolean checkAuthorization, CallingContext cc) {
    // TODO: improve with groups management, etc
    if(form.getCreationUser().equals(cc.getCurrentUser())) {
      forms.add(form);
    } else {
      forms.add(form);
    }
  }
}
