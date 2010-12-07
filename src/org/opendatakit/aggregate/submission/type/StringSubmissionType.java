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

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.LongStringRefText;
import org.opendatakit.aggregate.datamodel.RefText;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Data Storage Converter for String Type
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class StringSubmissionType extends SubmissionFieldBase<String> {

	  /**
	   * Backing object holding the value of the submission field
	   */
	protected DynamicCommonFieldsBase backingObject;

	private String fullValue = null;
	private FormDefinition formDefinition;
	private EntityKey topLevelTableKey;
	private Datastore datastore;
	private User user;
	private List<LongStringRefText> lsts = new ArrayList<LongStringRefText>();
	private List<RefText> refs = new ArrayList<RefText>();

	public String getValue() {
		return fullValue;
	}
	
	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            Name of submission element
	 */
	public StringSubmissionType(DynamicCommonFieldsBase backingObject, FormElementModel m, FormDefinition formDefinition, EntityKey topLevelTableKey, Datastore datastore, User user) {
		super(m);
		this.backingObject = backingObject;
		this.formDefinition = formDefinition;
		this.topLevelTableKey = topLevelTableKey;
		this.datastore = datastore;
		this.user = user;
	}

	/**
	 * Format value for output
	 * 
	 * @param elemFormatter
	 *            the element formatter that will convert the value to the
	 *            proper format for output
	 */
	@Override
	public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue)
			throws ODKDatastoreException {
		elemFormatter.formatString(getValue(), element.getGroupQualifiedElementName() + ordinalValue, row);
	}

	/**
	 * Set the string value
	 * 
	 * @param value
	 *            string form of the value
	 * @throws ODKEntityPersistException 
	 */
	@Override
	public void setValueFromString(String value) throws ODKEntityPersistException {
		fullValue = value;
		if ( !backingObject.setStringField(element.getFormDataModel().getBackingKey(), value)) {
			formDefinition.setLongString(value, backingObject.getUri(), element.getFormDataModel().getUri(), topLevelTableKey, datastore, user);
		}
	}

	/**
	 * Get submission field value from database entity
	 * 
	 * @param dbEntity
	 *            entity to obtain value
	 * @throws ODKDatastoreException 
	 */
	@Override
	public void getValueFromEntity(CommonFieldsBase dbEntity,
			String uriAssociatedRow, EntityKey topLevelTableKey,
			Datastore datastore, User user, boolean fetchElement)
			throws ODKDatastoreException {
		
		String value = (String) dbEntity.getStringField(element.getFormDataModel().getBackingKey());
		if (element.getFormDataModel().isPossibleLongStringField(dbEntity, element.getFormDataModel().getBackingKey())) {
			String longValue = formDefinition.getLongString(uriAssociatedRow, element.getFormDataModel().getUri(), datastore, user);
			if ( longValue != null ) {
				value = longValue;
			}
		}
		fullValue = value;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StringSubmissionType)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return true;
	}

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList) {
		for ( CommonFieldsBase b : lsts ) {
			keyList.add(new EntityKey( b, b.getUri()));
		}
		for ( CommonFieldsBase b : refs ) {
			keyList.add(new EntityKey( b, b.getUri()));
		}
	}
	
	@Override
	public void persist(Datastore datastore, User user) throws ODKEntityPersistException {
		datastore.putEntities(refs, user);
		datastore.putEntities(lsts, user);
	}
}
