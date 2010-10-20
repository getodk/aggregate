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

import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
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
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Groups a set of submission values together so they can be stored in a
 * databstore entity
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionSet implements Comparable<SubmissionSet> {
	protected static final String K_SL = "/";

	/**
	 * Submission set fields may be split across multiple backing tables due to
	 * limitations in the storage capacities of the underlying persistence
	 * layer. These manifest as phantom tables and subordinate structures in the
	 * data model. Abstract that all away at this level.
	 */
	protected final Map<FormDataModel, InstanceDataBase> dbEntities = new HashMap<FormDataModel, InstanceDataBase>();

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
	protected final FormDataModel group;

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
	protected final Map<FormDataModel, SubmissionValue> elementsToValues = new HashMap<FormDataModel, SubmissionValue>();

	/**
	 * Construct an empty submission for the ODK ID form
	 * 
	 * @param formOdkIdentifier
	 *            the ODK id of the form
	 * @param datastore
	 *            TODO
	 * @throws ODKDatastoreException
	 */
	public SubmissionSet(SubmissionSet enclosingSet, Long ordinalNumber,
			FormDataModel group, FormDefinition formDefinition,
			EntityKey colocationKey, Datastore datastore, User user)
			throws ODKDatastoreException {
		this.formDefinition = formDefinition;
		this.group = group;
		this.enclosingSet = enclosingSet;
		InstanceDataBase tlg = (InstanceDataBase) datastore.createEntityUsingRelation(
				group.getBackingObjectPrototype(), colocationKey, user);
		tlg.setOrdinalNumber(ordinalNumber);
		this.key = new EntityKey(tlg, tlg.getUri());
		if (colocationKey == null) {
			this.topLevelTableKey = key;
		} else {
			this.topLevelTableKey = colocationKey;
		}
		dbEntities.put(group, tlg);
		recursivelyCreateEntities(group, datastore, user);
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

	private void recursivelyCreateEntities(FormDataModel group,
			Datastore datastore, User user) {
		for (FormDataModel m : group.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				if (m.getBackingObjectPrototype() == null
						|| group.getBackingObjectPrototype().equals(
								m.getBackingObjectPrototype())) {
					// same backing object prototype as parent.
					// record the parent's actual backing object
					// as the backing object for this phantom.
					dbEntities.put(m, dbEntities.get(group));
				}
				else
				if (dbEntities.get(m) == null) {
					// prototypes aren't the same.
					// create a new backing instance.
					InstanceDataBase row = (InstanceDataBase) datastore
							.createEntityUsingRelation(m
									.getBackingObjectPrototype(),
									topLevelTableKey, user);
					row.setParentAuri(dbEntities.get(group).getUri());
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
	public SubmissionSet(SubmissionSet enclosingSet, InstanceDataBase row,
			FormDataModel group, FormDefinition formDefinition,
			Datastore datastore, User user)
			throws ODKDatastoreException {
		this.formDefinition = formDefinition;
		this.group = group;
		this.enclosingSet = enclosingSet;
		this.key = new EntityKey(row, row.getUri());
		if (!key.getRelation().sameTable(group.getBackingObjectPrototype())) {
			throw new IllegalArgumentException(
					"self-key and group backing object do not match");
		}
		if (row.getTopLevelAuri() == null) {
			this.topLevelTableKey = key;
		} else {
			this.topLevelTableKey = new EntityKey(formDefinition
					.getTopLevelGroup().getBackingObjectPrototype(), row
					.getTopLevelAuri());
		}
		dbEntities.put(group, row);
		recursivelyGetEntities(row.getUri(), group, datastore, user);
		buildSubmissionFields(group, datastore, user, true);
	}

	private void recursivelyGetEntities(String uriParent, FormDataModel group,
			Datastore datastore, User user) throws ODKDatastoreException {
		for (FormDataModel m : group.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				if (m.getBackingObjectPrototype() == null
						|| group.getBackingObjectPrototype().equals(
								m.getBackingObjectPrototype())) {
					// same backing object prototype as parent.
					// record the parent's actual backing object
					// as the backing object for this phantom.
					dbEntities.put(m, dbEntities.get(group));
				}
				else
				{
					InstanceDataBase row = dbEntities.get(m);
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
						row = (InstanceDataBase) rows.get(0);
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
	private void buildSubmissionFields(FormDataModel group,
			Datastore datastore, User user, boolean fetchElement)
			throws ODKDatastoreException {
		InstanceDataBase rowGroup = dbEntities.get(group);
		for (FormDataModel m : group.getChildren()) {
			SubmissionField<?> submissionField;
			switch (m.getElementType()) {
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
				submissionField = new GeoPointSubmissionType(m);
				// geopoints may be moved to subordinate table...
				InstanceDataBase geopoint = dbEntities.get(m);
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
						constructSubmissionKey(m), datastore, user); // pass
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

	public SubmissionValue getElementValue(FormDataModel element) {
		return elementsToValues.get(element);
	}

	private final String getFullyQualifiedElementName(FormDataModel element) {

		StringBuilder b = new StringBuilder();
		if (group.getParent() == null) {
			throw new IllegalStateException("unexpectedly missing FORM_NAME");
		} else if (group.getParent().getElementType() == ElementType.FORM_NAME) {
			b.append(formDefinition.getFormId());
		} else {
			b.append(getFullyQualifiedElementName(null));
		}
		b.append(K_SL);
		b.append(group.getElementName());
		b.append("[@key=");
		b.append(dbEntities.get(group).getUri());
		b.append("]");

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
	public SubmissionKey constructSubmissionKey(FormDataModel element) {
		return new SubmissionKey(getFullyQualifiedElementName(element));
	}

	public SubmissionValue resolveSubmissionKeyBeginningAt(int i,
			List<SubmissionKeyPart> parts) {
		SubmissionKeyPart p = parts.get(i);
		if (! p.getElementName().equals(group.getElementName())) {
			throw new IllegalArgumentException("group name: " + group.getElementName()
					+ " does not match submission key element name: " + 
					p.getElementName() );
		}
		if ( p.getAuri() == null ) {
			throw new IllegalArgumentException("no auri supplied in submission key");
		}
		if ( !p.getAuri().equals(dbEntities.get(group).getUri()) ) {
			throw new IllegalArgumentException("the auri of this group does not match!");
		}
		String elementName = parts.get(i+1).getElementName();
		for ( Map.Entry<FormDataModel,SubmissionValue> entry : elementsToValues.entrySet() ) {
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

	public List<SubmissionValue> findElementValue(FormDataModel element) {

		List<SubmissionValue> values = new ArrayList<SubmissionValue>();
		SubmissionValue v = getElementValue(element);
		if (v != null) {
			// simple case -- within this list...
			values.add(v);
			return values;
		}

		// complex case -- nested...
		List<FormDataModel> elements = new ArrayList<FormDataModel>();
		FormDataModel current = element;
		while (current != null && current != group) {
			elements.add(current);
			current = current.getParent();
		}
		for (int i = elements.size() - 1; i >= 0; --i) {
			current = elements.get(i);
			if (isPhantomOfSubmissionSet(current))
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

	private void recursivelyGetSubmissionValues(FormDataModel group,
			List<SubmissionValue> valueList) {
		for (FormDataModel m : group.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				if ( m.getElementType() != ElementType.GEOPOINT ) {
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

	/**
	 * Get a map of the submission values with the field/element name as the key
	 * 
	 * @return map of submission values
	 */
	public Map<FormDataModel, SubmissionValue> getSubmissionValuesMap() {
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

	public EntityKey getColocationKey() {
		return topLevelTableKey;
	}

	public Date getCreationDate() {
		return dbEntities.get(group).getCreationDate();
	}

	public Date getLastUpdateDate() {
		return dbEntities.get(group).getLastUpdateDate();
	}

	public String getCreatorUriUser() {
		return dbEntities.get(group).getCreatorUriUser();
	}

	public String getLastUpdateUriUser() {
		return dbEntities.get(group).getLastUpdateUriUser();
	}

	/**
	 * Format the submission set for output
	 * 
	 * @param propertyNames
	 *            if null includes all properties, otherwise will only include
	 *            property listed
	 * @param elemFormatter
	 *            formatter to use to properly format the values
	 * @return TODO
	 * @throws ODKDatastoreException
	 */
	public Row getFormattedValuesAsRow(List<FormDataModel> elements,
			ElementFormatter elemFormatter) throws ODKDatastoreException {
		Row row = new Row(key);
		if ( this instanceof Submission ) {
			// we are a Submission -- emit the creation date
			elemFormatter.formatDate(getCreationDate(), 
					FormatConsts.SUBMISSION_DATE_HEADER, row);
			elemFormatter.formatString(getKey().getKey(),
					FormatConsts.SUBMISSION_ID_HEADER, row);
		}
		if (elements == null) {
			List<SubmissionValue> values = getSubmissionValues();
			for (SubmissionValue value : values) {
				value.formatValue(elemFormatter, row);
			}
		} else {
			for (FormDataModel element : elements) {
				SubmissionValue value = elementsToValues.get(element);
				if (value != null) {
					value.formatValue(elemFormatter, row);
				}
			}
		}
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
	}

	public void persist(Datastore datastore, User user)
			throws ODKEntityPersistException {
		// persist everything underneath us...
		for (Map.Entry<FormDataModel, SubmissionValue> entry : elementsToValues
				.entrySet()) {
			FormDataModel m = entry.getKey();
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

}
