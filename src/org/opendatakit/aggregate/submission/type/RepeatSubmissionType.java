/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Data Storage type for a repeat type. Store a list of datastore keys to
 * submission sets in an entity
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class RepeatSubmissionType implements SubmissionRepeat {

	/**
	 * ODK identifier that uniquely identifies the form
	 */
	private FormDefinition formDefinition;

	/**
	 * Enclosing submission set
	 */
	private final SubmissionSet enclosingSet;

	/**
	 * Identifier for repeat
	 */
	private FormDataModel repeatGroup;

	/**
	 * List of submission sets that are a part of this submission set Ordered by
	 * OrdinalNumber...
	 */
	private List<SubmissionSet> submissionSets = new ArrayList<SubmissionSet>();

	public RepeatSubmissionType(SubmissionSet enclosingSet,
			FormDataModel repeatGroup, FormDefinition formDefinition) {
		this.enclosingSet = enclosingSet;
		this.formDefinition = formDefinition;
		this.repeatGroup = repeatGroup;
	}

	public SubmissionSet getEnclosingSet() {
		return enclosingSet;
	}
	
	public void addSubmissionSet(SubmissionSet submissionSet) {
		submissionSets.add(submissionSet);
	}

	public List<SubmissionSet> getSubmissionSets() {
		return submissionSets;
	}

	public int getNumberRepeats() {
		return submissionSets.size();
	}

	/**
	 * @return submissionKey that defines all the repeats for this particular repeat group.
	 */
	public SubmissionKey constructSubmissionKey() {
		return enclosingSet.constructSubmissionKey(repeatGroup);
	}
	
	/**
	 * Format value for output
	 * 
	 * @param elemFormatter
	 *            the element formatter that will convert the value to the
	 *            proper format for output
	 */
	@Override
	public void formatValue(ElementFormatter elemFormatter, Row row)
			throws ODKDatastoreException {
		elemFormatter.formatRepeats(this, repeatGroup.getElementName(), row);
	}

	@Override
	public void getValueFromEntity(CommonFieldsBase dbEntity,
			String uriAssociatedRow, EntityKey topLevelTableKey,
			Datastore datastore, User user, boolean fetchElement) throws ODKDatastoreException {

		Query q = datastore.createQuery(repeatGroup.getBackingObjectPrototype(), user);
		q.addFilter(repeatGroup.getBackingObjectPrototype().parentAuri,
				FilterOperation.EQUAL, uriAssociatedRow);

		List<? extends CommonFieldsBase> repeatGroupList = q
				.executeQuery(ServletConsts.FETCH_LIMIT);
		for (CommonFieldsBase cb : repeatGroupList) {
			InstanceDataBase d = (InstanceDataBase) cb;
			SubmissionSet set = new SubmissionSet(enclosingSet, d, repeatGroup,
					formDefinition, datastore, user);
			submissionSets.add(set);
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RepeatSubmissionType)) {
			return false;
		}

		RepeatSubmissionType other = (RepeatSubmissionType) obj;
		return formDefinition.equals(other.formDefinition)
				&& repeatGroup.equals(other.repeatGroup)
				&& submissionSets.equals(other.submissionSets);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 13;

		hashCode += formDefinition.hashCode();
		hashCode += repeatGroup.hashCode();
		hashCode += submissionSets.hashCode();

		return hashCode;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = enclosingSet.constructSubmissionKey(repeatGroup) + "\n";
		for (SubmissionSet set : submissionSets) {
			str += FormatConsts.TO_STRING_DELIMITER + set.toString();
		}
		return str;
	}

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList)
			throws ODKDatastoreException {
		for (SubmissionSet s : submissionSets) {
			s.recursivelyAddEntityKeys(keyList);
		}
	}

	@Override
	public void persist(Datastore datastore, User user)
			throws ODKEntityPersistException {
		for (SubmissionSet s : submissionSets) {
			s.persist(datastore, user);
		}
	}

	@Override
	public FormDataModel getElement() {
		return repeatGroup;
	}

	/**
	 * Get Property Name
	 * 
	 * @return property name
	 */
	public String getPropertyName() {
		return repeatGroup.getElementName();
	}

	public List<SubmissionValue> findElementValue(FormDataModel element) {
		// TODO Auto-generated method stub
		List<SubmissionValue> values = new ArrayList<SubmissionValue>();

		for (SubmissionSet s : submissionSets) {
			values.addAll(s.findElementValue(element));
		}
		return values;
	}

	@Override
	public SubmissionValue resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		SubmissionKeyPart p = parts.get(i);
		String auri = p.getAuri();
		if ( auri == null ) {
			throw new IllegalArgumentException("submission key repeat part does not have auri");
		}
		for (SubmissionSet s : submissionSets) {
			if ( s.getKey().getKey().equals(auri)) {
				return s.resolveSubmissionKeyBeginningAt(i, parts);
			}
		}
		return null;
	}

}
