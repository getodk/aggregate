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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.datamodel.DynamicBase;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryRepeats {

  private final Form form;

  private final FormElementModel repeatGroup;

  private final String parentKey;

  public QueryRepeats(Form form, String submissionKey,
      String submissionParentKey) throws ODKFormNotFoundException,
      ODKEntityNotFoundException {
    this.form = form;
    this.parentKey = submissionParentKey;
    // TODO: kindId should be concatenation of enclosing element names...
    this.repeatGroup = form.getFormDefinition().getElementByName(submissionKey);
  }

  public Collection<? extends SubmissionSet> getRepeatSubmissionSet(CallingContext cc) throws ODKIncompleteSubmissionData, ODKDatastoreException {
    List<SubmissionSet> submissionSets = new ArrayList<SubmissionSet>();

    TopLevelDynamicBase topLevelRelation = (TopLevelDynamicBase) form.getTopLevelGroupElement().getFormDataModel().getBackingObjectPrototype();

    // TODO: this doesn't work with PHANTOM or GROUP splits...
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser(); 
    // get the key to the top level relation where this repeat group has the given parent key
    DynamicBase backingObject = ((DynamicBase) repeatGroup.getFormDataModel().getBackingObjectPrototype());
    Query topLevelKeyQuery = ds.createQuery(backingObject, user);
    topLevelKeyQuery.addFilter(backingObject.parentAuri, Query.FilterOperation.EQUAL, parentKey);
    Set<EntityKey> submissionKeys = topLevelKeyQuery.executeForeignKeyQuery(topLevelRelation, backingObject.topLevelAuri);
    if ( submissionKeys.size() != 1 ) {
    	throw new IllegalStateException("unexpectedly found the same parent key in two different top-level tables!");
    }
    EntityKey k = submissionKeys.iterator().next();
    // fetch the top-level relation and recreate the entire submission...
    TopLevelDynamicBase d = (TopLevelDynamicBase) ds.getEntity(k.getRelation(), k.getKey(), user);
	Submission s = new Submission(d, form.getFormDefinition(), cc);
	// find the repeat group elements.
	List<SubmissionValue> vList = s.findElementValue(repeatGroup);
	for ( SubmissionValue v : vList ) {
		RepeatSubmissionType r = (RepeatSubmissionType) v;
		if ( r.getEnclosingSet().getKey().getKey().equals(parentKey) ) {
			// ok -- it is a repeat that meets the parentKey filter criteria
			// add all the submission sets under that repeat.
			submissionSets.addAll(r.getSubmissionSets());
		}
	}
    return submissionSets;
  }
}
