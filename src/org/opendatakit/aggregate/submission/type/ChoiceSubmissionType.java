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
package org.opendatakit.aggregate.submission.type;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.SelectChoice;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class ChoiceSubmissionType extends SubmissionFieldBase<List<String>> {

	List<String> values = new ArrayList<String>();
	
	List<SelectChoice> choices = new ArrayList<SelectChoice>();
	
	private final String parentKey;
	private final EntityKey topLevelTableKey;
	private final Datastore datastore;
	private final User user;
	
	public ChoiceSubmissionType(FormElementModel element, String parentKey, EntityKey topLevelTableKey, Datastore datastore, User user) {
		super(element);
		this.parentKey = parentKey;
		this.topLevelTableKey = topLevelTableKey;
		this.datastore = datastore;
		this.user = user;
	}

	@Override
	public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue)
			throws ODKDatastoreException {
		elemFormatter.formatChoices(values, element.getGroupQualifiedElementName()+ ordinalValue, row);
	}

	@Override
	public List<String> getValue() {
		return values;
	}

	@Override
	public void getValueFromEntity(Datastore datastore, User user) throws ODKDatastoreException {
		
		SelectChoice sel = (SelectChoice) element.getFormDataModel().getBackingObjectPrototype();
		Query q = datastore.createQuery(element.getFormDataModel().getBackingObjectPrototype(), user);
		q.addFilter(sel.parentAuri, FilterOperation.EQUAL, parentKey);
		q.addSort(sel.ordinalNumber, Direction.ASCENDING);

		List<? extends CommonFieldsBase> choiceHits = q.executeQuery(0);
		choices.clear();
		values.clear();
		for ( CommonFieldsBase cb : choiceHits ) {
			SelectChoice choice = (SelectChoice) cb;
			choices.add(choice);
			values.add(choice.getValue());
		}
	}

	@Override
	public void setValueFromString(String concatenatedValues) throws ODKConversionException, ODKDatastoreException {
		
		// clear the old values and underlying data records...
		values.clear();
		for ( SelectChoice c: choices ) {
			datastore.deleteEntity(new EntityKey(c, c.getUri()), user);
		}
		choices.clear();
		
		if ( concatenatedValues != null ) {
			String[] valueArray = concatenatedValues.split(" ");
			int i = 1;
			for ( String v : valueArray ) {
				SelectChoice c = (SelectChoice) datastore.createEntityUsingRelation(element.getFormDataModel().getBackingObjectPrototype(), user);
				c.setTopLevelAuri(topLevelTableKey.getKey());
				c.setParentAuri(parentKey);
				c.setOrdinalNumber(Long.valueOf(i++));
				c.setValue(v);
				choices.add(c);
				values.add(v);
			}
			datastore.putEntities(choices, user);
		}
	}

	@Override
	public void recursivelyAddEntityKeys(List<EntityKey> keyList) {
		for ( SelectChoice s : choices ) {
			keyList.add( new EntityKey( s, s.getUri()));
		}
	}
	
	@Override
	public void persist(Datastore datastore, User user) throws ODKEntityPersistException {
		datastore.putEntities(choices, user);
	}

	public SubmissionValue resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		// TODO: indexing into the list would require creating a 
		// virtual StringSubmissionType for each choice value.
		// for now, don't go to the trouble of that...
		// NOTE: a virtual StringSubmissionType would allow the
		// value to be of arbitrary length.
		return this;
	}
}
