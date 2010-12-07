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

package org.opendatakit.aggregate.submission;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.ChoiceSubmissionType;
import org.opendatakit.aggregate.submission.type.DecimalSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPointSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.aggregate.submission.type.jr.JRDateTimeType;
import org.opendatakit.aggregate.submission.type.jr.JRDateType;
import org.opendatakit.aggregate.submission.type.jr.JRTimeType;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.DynamicBase;
import org.opendatakit.common.persistence.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Groups a set of submission values together so they can be stored in a
 * databstore entity
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionSet implements Comparable<SubmissionSet>, SubmissionElement {
	protected static final String K_SL = "/";

	/**
	 * Submission set fields may be split across multiple backing tables due to
	 * limitations in the storage capacities of the underlying persistence
	 * layer. These manifest as phantom tables and subordinate structures in the
	 * data model. Abstract that all away at this level.
	 */
	private final Map<FormDataModel, DynamicCommonFieldsBase> dbEntities = new HashMap<FormDataModel, DynamicCommonFieldsBase>();

	/**
	 * set in which this set is contained.
	 */

	protected final SubmissionSet enclosingSet;

	/**
	 * key that uniquely identifies the submission
	 */
	protected final EntityKey key;

	/**
	 * The definition of this form (for access to lst).
	 */
	protected final FormDefinition formDefinition;

	/**
	 * Identifier for this submission set (all other entries in dbEntities are
	 * under this set)
	 */
	protected final FormElementModel group;

	/**
	 * Identifier for the parent for persistence co-location.
	 * 
	 * TODO: does this have special treatment if a repeat group?
	 */
	protected final EntityKey topLevelTableKey;

	/**
	 * Map of propteryName to submission values that make up the data contained
	 * in this submission set. OrdinalNumbering is determined by the
	 * FormDataModel.getChildren() list.
	 */
	protected final Map<FormElementModel, SubmissionValue> elementsToValues = new HashMap<FormElementModel, SubmissionValue>();

	/**
	 * Construct an empty submission for a repeating group within a form
	 * 
	 * @param formOdkIdentifier
	 *            the ODK id of the form
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public SubmissionSet(SubmissionSet enclosingSet, Long ordinalNumber,
			FormElementModel group, FormDefinition formDefinition,
			EntityKey topLevelTableKey, Datastore datastore, User user)
			throws ODKDatastoreException {
		this.formDefinition = formDefinition;
		this.group = group;
		this.enclosingSet = enclosingSet;
		DynamicBase tlg = (DynamicBase) datastore.createEntityUsingRelation(
				group.getFormDataModel().getBackingObjectPrototype(), topLevelTableKey, user);
		tlg.setOrdinalNumber(ordinalNumber);
		if ( enclosingSet != null ) {
			tlg.setParentAuri(enclosingSet.getKey().getKey());
		}
		this.key = new EntityKey(tlg, tlg.getUri());
		if (topLevelTableKey == null) {
			this.topLevelTableKey = key;
		} else {
			this.topLevelTableKey = topLevelTableKey;
		}
		dbEntities.put(group.getFormDataModel(), tlg);
		recursivelyCreateEntities(group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, datastore, user, false);
	}

	public SubmissionSet(Long modelVersion, Long uiVersion, 
			FormDefinition formDefinition,
			Datastore datastore, User user)
			throws ODKDatastoreException {
		this(modelVersion, uiVersion, null, formDefinition, datastore, user);
	}

	public SubmissionSet(Long modelVersion, Long uiVersion, String uriTopLevelGroup, 
			FormDefinition formDefinition,
			Datastore datastore, User user)
			throws ODKDatastoreException {
		this.formDefinition = formDefinition;
		this.group = formDefinition.getTopLevelGroupElement();
		this.enclosingSet = null;
		// this is a top level table...
		TopLevelDynamicBase tlg = (TopLevelDynamicBase) datastore.createEntityUsingRelation(
				group.getFormDataModel().getBackingObjectPrototype(), null, user);
		if ( uriTopLevelGroup != null ) {
			tlg.setStringField(tlg.primaryKey, uriTopLevelGroup);
		}
		tlg.setModelVersion(modelVersion);
		tlg.setUiVersion(uiVersion);
		this.key = new EntityKey(tlg, tlg.getUri());
		this.topLevelTableKey = key;
		// persist and recursively construct it...
		dbEntities.put(group.getFormDataModel(), tlg);
		recursivelyCreateEntities(group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, datastore, user, false);
	}

	/**
	 * Submission sets may be split over multiple database records by either
	 * inserting a nested phantom table, moving a geopoint to a different table,
	 * or moving an entire non-repeating group to a different table.
	 * 
	 * @param m
	 * @return true if this element may identify a new table or if its children
	 *         may have a new table within them.
	 */
	private boolean isPhantomOfSubmissionSet(FormDataModel m) {
		return (m.getPersistAsColumn() == null)
				&& ((m.getElementType() == ElementType.PHANTOM)
						|| (m.getElementType() == ElementType.GEOPOINT) || (m
						.getElementType() == ElementType.GROUP));
	}

	private void recursivelyCreateEntities(FormDataModel groupDataModel,
			Datastore datastore, User user) {
		for (FormDataModel m : groupDataModel.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				if (m.getBackingObjectPrototype() == null
						|| groupDataModel.getBackingObjectPrototype().equals(
								m.getBackingObjectPrototype())) {
					// same backing object prototype as parent.
					// record the parent's actual backing object
					// as the backing object for this phantom.
					dbEntities.put(m, dbEntities.get(groupDataModel));
				}
				else
				if (dbEntities.get(m) == null) {
					// prototypes aren't the same.
					// create a new backing instance.
					DynamicBase row = (DynamicBase) datastore
							.createEntityUsingRelation(m
									.getBackingObjectPrototype(),
									topLevelTableKey, user);
					row.setParentAuri(dbEntities.get(groupDataModel).getUri());
					row.setOrdinalNumber(1L); // these are always the
					// one-and-only record in the
					// order...
					dbEntities.put(m, row);
				}

				// and ensure that we create any nested rows...
				recursivelyCreateEntities(m, datastore, user);
			}
		}
	}

	/**
	 * Construct a submission set from the data store
	 * 
	 * @param entity
	 *            submission entity that contains the data
	 * @param form
	 *            TODO
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public SubmissionSet(SubmissionSet enclosingSet, DynamicCommonFieldsBase row,
			FormElementModel group, FormDefinition formDefinition,
			Datastore datastore, User user)
			throws ODKDatastoreException {
		this.formDefinition = formDefinition;
		this.group = group;
		this.enclosingSet = enclosingSet;
		this.key = new EntityKey(row, row.getUri());
		if (!key.getRelation().sameTable(group.getFormDataModel().getBackingObjectPrototype())) {
			throw new IllegalArgumentException(
					"self-key and group backing object do not match");
		}
		if (row instanceof TopLevelDynamicBase) {
			this.topLevelTableKey = key;
		} else {
			DynamicBase entity = (DynamicBase) row;
			this.topLevelTableKey = new EntityKey(formDefinition
					.getTopLevelGroup().getBackingObjectPrototype(), entity
					.getTopLevelAuri());
		}
		dbEntities.put(group.getFormDataModel(), row);
		recursivelyGetEntities(row.getUri(), group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, datastore, user, true);
	}

	private void recursivelyGetEntities(String uriParent, FormDataModel groupDataModel,
			Datastore datastore, User user) throws ODKDatastoreException {
		for (FormDataModel m : groupDataModel.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				if (m.getBackingObjectPrototype() == null
						|| groupDataModel.getBackingObjectPrototype().equals(
								m.getBackingObjectPrototype())) {
					// same backing object prototype as parent.
					// record the parent's actual backing object
					// as the backing object for this phantom.
					dbEntities.put(m, dbEntities.get(groupDataModel));
				}
				else
				{
					DynamicCommonFieldsBase row = dbEntities.get(m);
					if (row == null) {
						Query query = datastore.createQuery(m
								.getBackingObjectPrototype(), user);
						query.addFilter(m.parentAuri, FilterOperation.EQUAL,
								uriParent);
						List<? extends CommonFieldsBase> rows = query
								.executeQuery(ServletConsts.FETCH_LIMIT);
						if (rows.size() != 1) {
							throw new IllegalStateException(
									"Expected exactly one match in phantom reconstruction!");
						}
						row = (DynamicBase) rows.get(0);
						dbEntities.put(m, row);
					}
					// and ensure that we create the other datastores...
					recursivelyGetEntities(row.getUri(), m, datastore, user);
				}
			}
		}
	}

	/**
	 * Recursively use form definition to recreate the submission
	 * 
	 * @param form
	 *            persistence manager used to retrieve form elements
	 * @param element
	 *            current element to recreate
	 * @throws ODKDatastoreException
	 */
	private void buildSubmissionFields(FormElementModel group,
			Datastore datastore, User user, boolean fetchElement)
			throws ODKDatastoreException {
		DynamicCommonFieldsBase rowGroup = getGroupBackingObject();
		for (FormElementModel m : group.getChildren()) {
			SubmissionField<?> submissionField;
			if ( !m.isMetadata() ) {
				switch (m.getFormDataModel().getElementType()) {
				case STRING:
					submissionField = new StringSubmissionType(rowGroup, m,
							formDefinition, topLevelTableKey, datastore, user);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case JRDATETIME:
					submissionField = new JRDateTimeType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case JRDATE:
					submissionField = new JRDateType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case JRTIME:
					submissionField = new JRTimeType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case INTEGER:
					submissionField = new LongSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case DECIMAL:
					submissionField = new DecimalSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case GEOPOINT:
					// geopoints may be moved to subordinate table...
					DynamicCommonFieldsBase geopoint = dbEntities.get(m.getFormDataModel());
					submissionField = new GeoPointSubmissionType(geopoint, m);
					submissionField.getValueFromEntity(geopoint, geopoint.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case BOOLEAN:
					submissionField = new BooleanSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(rowGroup, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case GROUP:
					// groups are not manifest unless they repeat...
					// just recurse to build out the fields under them...
					buildSubmissionFields(m, datastore, user, fetchElement);
					break;
				// additional supporting tables
				case PHANTOM: // if a relation needs to be divided in order to fit
					// phantoms are not manifest...
					// just recurse to build out the fields under them...
					buildSubmissionFields(m, datastore, user, fetchElement);
					break;
				case BINARY: // identifies BinaryContent table
					submissionField = new BlobSubmissionType(m, rowGroup.getUri(),
							topLevelTableKey, formDefinition, 
							constructSubmissionKey(m), datastore, user);
					// pass in row we occur under (to access parentAuri)
					submissionField.getValueFromEntity(null, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case SELECT1: // identifies SelectChoice table
					submissionField = new ChoiceSubmissionType(m,
							rowGroup.getUri(), topLevelTableKey, datastore, user); // pass
					// in
					// row
					// we
					// occur
					// under
					// (to
					// access
					// parentAuri)
					submissionField.getValueFromEntity(null, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case SELECTN: // identifies SelectChoice table
					submissionField = new ChoiceSubmissionType(m,
							rowGroup.getUri(), topLevelTableKey, datastore, user); // pass
					// in
					// row
					// we
					// occur
					// under
					// (to
					// access
					// parentAuri)
					submissionField.getValueFromEntity(null, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, submissionField);
					break;
				case REPEAT:
					RepeatSubmissionType repeatNode = new RepeatSubmissionType(
							this, m, formDefinition);
					repeatNode.getValueFromEntity(null, rowGroup.getUri(),
							topLevelTableKey, datastore, user, fetchElement);
					elementsToValues.put(m, repeatNode);
					break;
				case VERSIONED_BINARY_CONTENT_REF_BLOB: // association between
					// BINARY and REF_BLOB
				case REF_BLOB: // the table of the actual byte[] data (xxxBLOB)
				case LONG_STRING_REF_TEXT: // association between any field and
					// REF_TEXT
				case REF_TEXT: // the table of extended string values (xxxTEXT)
				default:
					throw new IllegalStateException(
							"Traversed element type is unexpected: "
									+ m.getElementType().toString());
				}
			}
		}
	}

	public SubmissionValue getElementValue(FormElementModel element) {
		return elementsToValues.get(element);
	}

	private final String getFullyQualifiedElementName(FormElementModel element) {

		StringBuilder b = new StringBuilder();
		if (group.getParent() == null) {
			b.append(formDefinition.getFormId());
			b.append(K_SL);
			b.append(group.getElementName());
			b.append("[@key=");
			b.append(getGroupBackingObject().getUri());
			b.append("]");
		} else {
			// it is derived from DynamicBase...
			DynamicBase entity = (DynamicBase) getGroupBackingObject();
			b.append(enclosingSet.getFullyQualifiedElementName(null));
			b.append(K_SL);
			b.append(group.getElementName());
			b.append("[@ordinal=");
			b.append(entity.getOrdinalNumber());
			b.append("]");
		}

		if (element != null) {
			b.append(K_SL);
			b.append(element.getGroupQualifiedElementName());
		}

		return b.toString();
	}

	/**
	 * Keys are of the form
	 * <code>formId/topLevelGroupName[@key=PK]/repeatGroupA/.../thisGroup[@key=PK]/element</code>
	 * 
	 * @param element
	 *            may be null; must be in this SubmissionSet
	 * @return submissionKey specifying the key to the top-level submission, the
	 *         key for this submissionSet, and the name of the element (if not
	 *         null)
	 */
	public SubmissionKey constructSubmissionKey(FormElementModel element) {
		return new SubmissionKey(getFullyQualifiedElementName(element));
	}

	public SubmissionElement resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		SubmissionKeyPart p = parts.get(i);
		if (! p.getElementName().equals(group.getElementName())) {
			throw new IllegalArgumentException("group name: " + group.getElementName()
					+ " does not match submission key element name: " + 
					p.getElementName() );
		}
		if ( p.getAuri() == null && p.getOrdinalNumber() == null) {
			throw new IllegalArgumentException("no auri or ordinal supplied in submission key");
		}
		if ( p.getAuri() != null && !p.getAuri().equals(getGroupBackingObject().getUri()) ) {
			throw new IllegalArgumentException("the auri of this group does not match!");
		}
		
		if ( p.getOrdinalNumber() != null) {
			Long ordinal = null;
			try {
				DynamicBase entity = (DynamicBase) getGroupBackingObject();
				ordinal = entity.getOrdinalNumber();
			} catch ( Exception e ) {
				throw new IllegalArgumentException("inproper use of ordinal qualifier");
			}
			if ( p.getOrdinalNumber() != ordinal ) {
				throw new IllegalArgumentException("the ordinal of this group does not match!");
			}
		}
		
		if ( i+1 == parts.size() ) return this;
		String elementName = parts.get(i+1).getElementName();
		for ( Map.Entry<FormElementModel,SubmissionValue> entry : elementsToValues.entrySet() ) {
			if ( elementName.equals(entry.getKey().getGroupQualifiedElementName()) ) {
				SubmissionValue v = entry.getValue();
				if ( v instanceof SubmissionSet ) {
					return ((SubmissionSet) v).resolveSubmissionKeyBeginningAt(i+1,parts);
				} else if ( v instanceof SubmissionRepeat ) {
					return ((SubmissionRepeat) v).resolveSubmissionKeyBeginningAt(i+1,parts);
				} else if ( v instanceof ChoiceSubmissionType ) {
					return ((ChoiceSubmissionType) v).resolveSubmissionKeyBeginningAt(i+1,parts);
				} else if ( v instanceof BlobSubmissionType ) {
					return ((BlobSubmissionType) v).resolveSubmissionKeyBeginningAt(i+1,parts);
				}
			}
		}
		return null;
	}

	public List<SubmissionValue> findElementValue(FormElementModel element) {

		List<SubmissionValue> values = new ArrayList<SubmissionValue>();
		SubmissionValue v = getElementValue(element);
		if (v != null) {
			// simple case -- within this list...
			values.add(v);
			return values;
		}

		// complex case -- nested...
		// build the list of FEMs from the element up to this group.
		List<FormElementModel> elements = new ArrayList<FormElementModel>();
		FormElementModel current = element;
		while (current != null && current != group) {
			elements.add(current);
			current = current.getParent();
		}
		// work back down the elements list to find the actual element...
		for (int i = elements.size() - 1; i >= 0; --i) {
			current = elements.get(i);
			if (isPhantomOfSubmissionSet(current.getFormDataModel()))
				continue;

			v = getElementValue(current);
			if (v instanceof RepeatSubmissionType) {
				return ((RepeatSubmissionType) v).findElementValue(element);
			} else {
				throw new IllegalStateException(
						"unexpected type in recursive search");
			}
		}
		throw new IllegalStateException("unexpected exit in recursive search");
	}


	/**
	 * Construct value list in the order in which the values should appear.
	 * 
	 * @return list of populated submission values
	 */
	public List<SubmissionValue> getSubmissionValues() {
		List<SubmissionValue> valueList = new ArrayList<SubmissionValue>();
		recursivelyGetSubmissionValues(group, valueList);
		return valueList;
	}

	private void recursivelyGetSubmissionValues(FormElementModel group,
			List<SubmissionValue> valueList) {
		for (FormElementModel m : group.getChildren()) {
			if ( !m.isMetadata() ) {
				if (isPhantomOfSubmissionSet(m.getFormDataModel())) {
					if ( m.getFormDataModel().getElementType() != ElementType.GEOPOINT ) {
						recursivelyGetSubmissionValues(m, valueList);
					} else {
						SubmissionValue v = elementsToValues.get(m);
						valueList.add(v);
					}
				} else {
					SubmissionValue v = elementsToValues.get(m);
					valueList.add(v);
				}
			}
		}
	}

	/**
	 * Get a map of the submission values with the field/element name as the key
	 * 
	 * @return map of submission values
	 */
	public Map<FormElementModel, SubmissionValue> getSubmissionValuesMap() {
		return elementsToValues;
	}

	/**
	 * Get the datastore key that uniquely identifies the submission
	 * 
	 * @return datastore key
	 */
	public EntityKey getKey() {
		return key;
	}

	public FormDefinition getFormDefinition() {
		return formDefinition;
	}

	protected DynamicCommonFieldsBase getGroupBackingObject() {
		return dbEntities.get(group.getFormDataModel());
	}
	
	public Date getCreationDate() {
		return getGroupBackingObject().getCreationDate();
	}

	public Date getLastUpdateDate() {
		return getGroupBackingObject().getLastUpdateDate();
	}

	public String getCreatorUriUser() {
		return getGroupBackingObject().getCreatorUriUser();
	}

	public String getLastUpdateUriUser() {
		return getGroupBackingObject().getLastUpdateUriUser();
	}

	protected void populateFormattedValueInRow(Row row,
			FormElementModel propertyName,
			ElementFormatter elemFormatter) throws ODKDatastoreException {
		SubmissionValue value = elementsToValues.get(propertyName);
		if (value != null) {
			value.formatValue(elemFormatter, row, getOrdinalNumAsStr());
		}
	}

	protected void populateFormattedValuesInRow(Row row,
			List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter) throws ODKDatastoreException {
		if (propertyNames == null) {
			List<SubmissionValue> values = getSubmissionValues();
			for (SubmissionValue value : values) {
				value.formatValue(elemFormatter, row, getOrdinalNumAsStr());
			}
		} else {
			for (FormElementModel element : propertyNames) {
				populateFormattedValueInRow(row, element, elemFormatter);
			}
		}
	}

	/**
	 * Format the submission set for output
	 * 
	 * @param propertyNames
	 *            if null includes all properties, otherwise will only include
	 *            property listed
	 * @param elemFormatter
	 *            formatter to use to properly format the values
	 * @param includeParentUid TODO
	 * @return TODO
	 * @throws ODKDatastoreException
	 */
	public Row getFormattedValuesAsRow(List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter, boolean includeParentUid) throws ODKDatastoreException {
		Row row = new Row(constructSubmissionKey(null));
		if(includeParentUid && !(this instanceof Submission)) {
		  elemFormatter.formatUid(enclosingSet.getKey().getKey(), enclosingSet.getPropertyName(), row);
		}
		populateFormattedValuesInRow(row, propertyNames, elemFormatter);
		return row;
	}

	public void printSubmission(PrintWriter out) {

		List<SubmissionValue> values = getSubmissionValues();
		for (SubmissionValue value : values) {
			out.println(value.toString());
		}
	}

	public void recursivelyAddEntityKeys(List<EntityKey> keyList)
			throws ODKDatastoreException {
		for (SubmissionValue value : getSubmissionValues()) {
			value.recursivelyAddEntityKeys(keyList);
		}
		for ( Map.Entry<FormDataModel, DynamicCommonFieldsBase> e : dbEntities.entrySet() ) {
			keyList.add( new EntityKey( e.getValue(), e.getValue().getUri()));
		}
	}

	public void persist(Datastore datastore, User user)
			throws ODKEntityPersistException {
		// persist everything underneath us...
		for (Map.Entry<FormElementModel, SubmissionValue> entry : elementsToValues
				.entrySet()) {
			FormElementModel m = entry.getKey();
			// isPhantomOfGroup() handles groups, phantoms and geopoints...
			// we need to manually propagate persist(...) for
			// select1, selectn -- ChoiceSubmissionType is maintained in
			// external tables
			// binary -- maintained in 3 external tables
			// string -- in case they are long strings
			// repeat -- nesting abstraction for a list of submission sets
			switch (m.getElementType()) {
			case SELECT1:
			case SELECTN:
			case BINARY:
			case STRING:
			case REPEAT:
				entry.getValue().persist(datastore, user);
				break;
			}
		}

		// and persist us...
		datastore.putEntities(dbEntities.values(), user);
	}

	public int compareTo(SubmissionSet obj) {
		return key.compareTo(obj.key);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		List<SubmissionValue> values = getSubmissionValues();
		for (SubmissionValue value : values) {
			b.append(value.toString());
			b.append(BasicConsts.NEW_LINE);
		}
		return b.toString();
	}

	@Override
	public String getPropertyName() {
		return group.getElementName();
	}
	
	public FormElementModel getFormElementModel() {
	  return group;
	}
	
	private String getOrdinalNumAsStr() {
	  if(group.getElementType() == FormElementModel.ElementType.REPEAT) {
	    DynamicBase entity = (DynamicBase) getGroupBackingObject();
	    return entity.getOrdinalNumber().toString();
	  } else {
	    return BasicConsts.EMPTY_STRING;
	  }
	}

}
