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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDataModel.DDRelationName;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.BooleanMetadataType;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.ChoiceSubmissionType;
import org.opendatakit.aggregate.submission.type.DateTimeMetadataType;
import org.opendatakit.aggregate.submission.type.DecimalSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPointSubmissionType;
import org.opendatakit.aggregate.submission.type.LongMetadataType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringMetadataType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.aggregate.submission.type.jr.JRDateTimeType;
import org.opendatakit.aggregate.submission.type.jr.JRDateType;
import org.opendatakit.aggregate.submission.type.jr.JRTimeType;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Groups a set of submission values together so they can be stored in a
 * databstore entity
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class SubmissionSet implements Comparable<SubmissionSet>, SubmissionElement {

	/**
	 * Submission set fields may be split across multiple backing tables due to
	 * limitations in the storage capacities of the underlying persistence
	 * layer. These manifest as phantom tables and subordinate structures in the
	 * data model. Abstract that all away at this level.
	 */

	/**
	 * dbEntities holds the map of all backing objects used to store this submission
	 * set record.  It excludes the binary and choice tables, which are independent
	 * elements.  Everything is broken if we have two or more backing objects residing in
	 * the same table because DDRelationName is the key into this map, and that would be
	 * the same for all records held in a given table.
	 */
	private final Map<DDRelationName, DynamicCommonFieldsBase> dbEntities =
					new HashMap<DDRelationName, DynamicCommonFieldsBase>();

	/**
	 * set in which this set is contained.
	 */

	private final SubmissionSet enclosingSet;

	/**
	 * key that uniquely identifies the submission
	 */
	private final EntityKey key;

	/**
	 * The definition of this form (for access to lst).
	 */
	private final IForm form;

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
	private final EntityKey topLevelTableKey;

	/**
	 * Map of propteryName to submission values that make up the data contained
	 * in this submission set. OrdinalNumbering is determined by the
	 * FormDataModel.getChildren() list.
	 */
	private final Map<FormElementModel, SubmissionValue> elementsToValues =
					new HashMap<FormElementModel, SubmissionValue>();

	/**
	 * Construct an empty repeating group.
	 *
	 * @param enclosingSet
	 * @param ordinalNumber
	 * @param group
	 * @param formDefinition
	 * @param topLevelTableKey
	 * @param cc - the CallingContext for this request
	 * @throws ODKDatastoreException
	 */
	public SubmissionSet(SubmissionSet enclosingSet, Long ordinalNumber,
			FormElementModel group, IForm form,
			EntityKey topLevelTableKey, CallingContext cc)
			throws ODKDatastoreException {
		this.form = form;
		this.group = group;
		this.enclosingSet = enclosingSet;
		Datastore datastore = cc.getDatastore();
		User user = cc.getCurrentUser();
		DynamicBase tlg = (DynamicBase) datastore.createEntityUsingRelation(
				group.getFormDataModel().getBackingObjectPrototype(), user);
		tlg.setTopLevelAuri(topLevelTableKey.getKey());
		tlg.setOrdinalNumber(ordinalNumber);
		if ( enclosingSet != null ) {
			tlg.setParentAuri(enclosingSet.getKey().getKey());
		}
		this.key = tlg.getEntityKey();
		this.topLevelTableKey = topLevelTableKey;
		dbEntities.put(group.getFormDataModel().getDDRelationName(), tlg);
		recursivelyCreateEntities(group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, cc);
	}

	public SubmissionSet(Long modelVersion, Long uiVersion,
			IForm form,
			CallingContext cc)
			throws ODKDatastoreException {
		this(modelVersion, uiVersion, null, form, cc);
	}

	public SubmissionSet(Long modelVersion, Long uiVersion, String uriTopLevelGroup,
			IForm form,
			CallingContext cc)
			throws ODKDatastoreException {
		this.form = form;
		this.group = form.getTopLevelGroupElement();
		this.enclosingSet = null;
		Datastore datastore = cc.getDatastore();
		User user = cc.getCurrentUser();
		// this is a top level table...
		TopLevelDynamicBase tlg = (TopLevelDynamicBase) datastore.createEntityUsingRelation(
				group.getFormDataModel().getBackingObjectPrototype(), user);
		if ( uriTopLevelGroup != null ) {
			tlg.setStringField(tlg.primaryKey, uriTopLevelGroup);
		}
		tlg.setModelVersion(modelVersion);
		tlg.setUiVersion(uiVersion);
		this.key = tlg.getEntityKey();
		this.topLevelTableKey = key;
		// persist and recursively construct it...
		dbEntities.put(group.getFormDataModel().getDDRelationName(), tlg);
		recursivelyCreateEntities(group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, cc);
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
				&& ((m.getElementType() == ElementType.PHANTOM) ||
					(m.getElementType() == ElementType.GEOPOINT) ||
					(m.getElementType() == ElementType.GROUP));
	}

	private void recursivelyCreateEntities(FormDataModel groupDataModel,
			Datastore datastore, User user) {
		DynamicCommonFieldsBase groupRelation = (DynamicCommonFieldsBase) groupDataModel.getBackingObjectPrototype();
		for (FormDataModel m : groupDataModel.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				// backing object should never be null, so this should never throw NullPointerException...
				DynamicCommonFieldsBase mRelation = (DynamicCommonFieldsBase) m.getBackingObjectPrototype();
				DynamicCommonFieldsBase mBackingObject = dbEntities.get(m.getDDRelationName());
				if (groupRelation.equals(mRelation)) {
					// same backing object prototype as parent.  This should already be in the map.
					if ( mBackingObject == null ) {
						throw new IllegalStateException("unexpected null value");
					}
				}
				else
				if (mBackingObject == null) {
					// prototypes aren't the same.
					// create a new backing instance.
					DynamicBase row = (DynamicBase) datastore
							.createEntityUsingRelation(mRelation, user);
					row.setTopLevelAuri(topLevelTableKey.getKey());
					row.setParentAuri(dbEntities.get(groupDataModel.getDDRelationName()).getUri());
					row.setOrdinalNumber(1L); // these are always the
					// one-and-only record in the order, as this
					// is a phantom table...
					dbEntities.put(m.getDDRelationName(), row);
				}

				// and ensure that we create any nested rows...
				recursivelyCreateEntities(m, datastore, user);
			}
		}
	}

	/**
	 * Construct a submission set from the datastore.
	 * @param enclosingSet - the enclosing submission set.
	 * @param row - the base record for this submission set.
	 * @param group - the form group mapped to the base record.
	 * @param formDefinition - the definition of the form.
	 * @param cc - the CallingContext of this request.
	 * @throws ODKDatastoreException
	 */
	public SubmissionSet(SubmissionSet enclosingSet, DynamicCommonFieldsBase row,
			FormElementModel group, IForm form,
			CallingContext cc)
			throws ODKDatastoreException {
		this.form = form;
		this.group = group;
		this.enclosingSet = enclosingSet;
		this.key = row.getEntityKey();
		Datastore datastore = cc.getDatastore();
		User user = cc.getCurrentUser();

		if (!key.getRelation().sameTable(group.getFormDataModel().getBackingObjectPrototype())) {
			throw new IllegalArgumentException(
					"self-key and group backing object do not match");
		}
		if (row instanceof TopLevelDynamicBase) {
			this.topLevelTableKey = key;
		} else {
			DynamicBase entity = (DynamicBase) row;
			// we aren't the top level record so manufacture the
			// entity key of the top level record from the relation
			// for that record and the up-pointer in our record
			// that holds the AURI for that top level record.
			this.topLevelTableKey = new EntityKey(form.getTopLevelGroupElement()
			      .getFormDataModel().getBackingObjectPrototype(), entity
					.getTopLevelAuri());
		}
		dbEntities.put(group.getFormDataModel().getDDRelationName(), row);
		recursivelyGetEntities(row.getUri(), group.getFormDataModel(), datastore, user);
		buildSubmissionFields(group, cc);
	}

	private void recursivelyGetEntities(String uriParent, FormDataModel groupDataModel,
			Datastore datastore, User user) throws ODKDatastoreException {
		DynamicCommonFieldsBase groupRelation = (DynamicCommonFieldsBase) groupDataModel.getBackingObjectPrototype();
		for (FormDataModel m : groupDataModel.getChildren()) {
			if (isPhantomOfSubmissionSet(m)) {
				// backing object should never be null, so this should never throw NullPointerException...
				DynamicCommonFieldsBase mRelation = (DynamicCommonFieldsBase) m.getBackingObjectPrototype();
				DynamicCommonFieldsBase mBackingObject = dbEntities.get(m.getDDRelationName());
				String nestedGroupUriParent;
				if (groupRelation.equals(mRelation)) {
					// same backing object prototype as parent.
					// record the parent's actual backing object
					// as the backing object for this phantom.
					if ( mBackingObject == null ) {
						throw new IllegalStateException("the parent's backing object should be there");
					}
					nestedGroupUriParent = uriParent;
				}
				else
				{
					if (mBackingObject == null) {
						DynamicBase mBaseRelation = (DynamicBase) mRelation;
						Query query = datastore.createQuery(mBaseRelation, "SubmissionSet.recursivelyGetEntities", user);
						query.addFilter(mBaseRelation.parentAuri, FilterOperation.EQUAL,
								uriParent);
						List<? extends CommonFieldsBase> rows = query.executeQuery();
						if (rows.size() != 1) {
							throw new IllegalStateException(
									"Expected exactly one match in phantom reconstruction!");
						}
						mBackingObject = (DynamicBase) rows.get(0);
						dbEntities.put(m.getDDRelationName(), mBackingObject);
					}
					nestedGroupUriParent = mBackingObject.getUri();
				}
				// and ensure that we create the other datastores...
				recursivelyGetEntities(nestedGroupUriParent, m, datastore, user);
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
	private void buildSubmissionFields(FormElementModel group, CallingContext cc)
			throws ODKDatastoreException {
		DynamicCommonFieldsBase groupRowGroup = getGroupBackingObject();
		for (FormElementModel m : group.getChildren()) {
			SubmissionField<?> submissionField;
			if ( m.isMetadata() ) {
				// all metadata is at the top-level (submission) level...
				TopLevelDynamicBase tl = (TopLevelDynamicBase) groupRowGroup;
				switch ( m.getType() ) {
				case META_INSTANCE_ID:
					submissionField = new StringMetadataType(groupRowGroup, m,
															tl.primaryKey);
					elementsToValues.put(m, submissionField);
					break;
				case META_UI_VERSION:
					submissionField = new LongMetadataType(groupRowGroup, m,
															tl.uiVersion);
					elementsToValues.put(m, submissionField);
					break;
				case META_MODEL_VERSION:
					submissionField = new LongMetadataType(groupRowGroup, m,
															tl.modelVersion);
					elementsToValues.put(m, submissionField);
					break;
				case META_SUBMISSION_DATE:
					submissionField = new DateTimeMetadataType(groupRowGroup, m,
															tl.submissionDate);
					elementsToValues.put(m, submissionField);
					break;
				case META_IS_COMPLETE:
					submissionField = new BooleanMetadataType(groupRowGroup, m,
															tl.isComplete);
					elementsToValues.put(m, submissionField);
					break;
				case META_DATE_MARKED_AS_COMPLETE:
					submissionField = new DateTimeMetadataType(groupRowGroup, m,
															tl.markedAsCompleteDate);
					elementsToValues.put(m, submissionField);
					break;
				default:
					throw new IllegalStateException("unhandled metadata type");
				}
			} else {
				DynamicCommonFieldsBase rowGroup = getGroupBackingObject(m);
				switch (m.getFormDataModel().getElementType()) {
				case STRING:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new StringSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case JRDATETIME:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new JRDateTimeType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case JRDATE:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new JRDateType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case JRTIME:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new JRTimeType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case INTEGER:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new LongSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case DECIMAL:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new DecimalSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case GEOPOINT:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new GeoPointSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case BOOLEAN:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					submissionField = new BooleanSubmissionType(rowGroup, m);
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case GROUP:
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					// groups are not manifest unless they repeat...
					// just recurse to build out the fields under them...
					buildSubmissionFields(m, cc);
					break;
				// additional supporting tables
				case PHANTOM: // if a relation needs to be divided in order to fit
					if ( rowGroup == null ) {
						throw new IllegalStateException("Unexpectedly null backingObject");
					}
					// phantoms are not manifest...
					// just recurse to build out the fields under them...
					buildSubmissionFields(m, cc);
					break;
				case BINARY: // identifies BinaryContent table
					submissionField = new BlobSubmissionType(m, groupRowGroup.getUri(),
							topLevelTableKey, constructSubmissionKey(m));
					// pass in row we occur under (to access parentAuri)
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case SELECT1: // identifies SelectChoice table
					submissionField = new ChoiceSubmissionType(m, groupRowGroup.getUri(),
							topLevelTableKey); // pass
					// in row we occur under to access parentAuri
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case SELECTN: // identifies SelectChoice table
					submissionField = new ChoiceSubmissionType(m, groupRowGroup.getUri(),
							topLevelTableKey); // pass
					// in row we occur under to access parentAuri
					submissionField.getValueFromEntity(cc);
					elementsToValues.put(m, submissionField);
					break;
				case REPEAT:
					RepeatSubmissionType repeatNode = new RepeatSubmissionType(
							this, m,  groupRowGroup.getUri(), form);
					repeatNode.getValueFromEntity(cc);
					elementsToValues.put(m, repeatNode);
					break;
				case BINARY_CONTENT_REF_BLOB: // association between
					// BINARY and REF_BLOB
				case REF_BLOB: // the table of the actual byte[] data (xxxBLOB)
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
			b.append(form.getFormId());
			b.append("[@version=null and @uiVersion=null]");
			b.append(BasicConsts.FORWARDSLASH);
			b.append(group.getElementName());
			b.append("[@key=");
			b.append(getGroupBackingObject().getUri());
			b.append("]");
		} else {
			// it is derived from DynamicBase...
			DynamicBase entity = (DynamicBase) getGroupBackingObject();
			b.append(enclosingSet.getFullyQualifiedElementName(null));
			b.append(BasicConsts.FORWARDSLASH);
			b.append(group.getGroupQualifiedElementName());
			b.append("[@ordinal=");
			b.append(entity.getOrdinalNumber());
			b.append("]");
		}

		if (element != null) {
			b.append(BasicConsts.FORWARDSLASH);
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
		if (! p.getElementName().equals(group.getGroupQualifiedElementName())) {
		   if ( FormDataModel.isFieldStoredWithinDataTable(group.getFormDataModel().getElementType()) ||
		        !p.getElementName().equals(group.getElementName()) ) {
    			throw new IllegalArgumentException("group name: " + group.getElementName()
    					+ " does not match submission key element name: " +
    					p.getElementName() );
		   }
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
			if ( ordinal == null || !p.getOrdinalNumber().equals(ordinal) ) {
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
				} else {
					if ( i+2 != parts.size() ) {
						throw new IllegalStateException("submission key parts remaining at leaf node");
					}
					return v;
				}
			}
		}

		// backward compatibility...
      for ( Map.Entry<FormElementModel,SubmissionValue> entry : elementsToValues.entrySet() ) {
        if ( elementName.equals(entry.getKey().getElementName()) ) {
            SubmissionValue v = entry.getValue();
            if ( v instanceof SubmissionSet ) {
               return ((SubmissionSet) v).resolveSubmissionKeyBeginningAt(i+1,parts);
            } else if ( v instanceof SubmissionRepeat ) {
               return ((SubmissionRepeat) v).resolveSubmissionKeyBeginningAt(i+1,parts);
            } else if ( v instanceof ChoiceSubmissionType ) {
               return ((ChoiceSubmissionType) v).resolveSubmissionKeyBeginningAt(i+1,parts);
            } else if ( v instanceof BlobSubmissionType ) {
               return ((BlobSubmissionType) v).resolveSubmissionKeyBeginningAt(i+1,parts);
            } else {
               if ( i+2 != parts.size() ) {
                  throw new IllegalStateException("submission key parts remaining at leaf node");
               }
               return v;
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
			if ( m.isMetadata() ) {
				SubmissionValue v = elementsToValues.get(m);
				valueList.add(v);
			} else {
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

	public List<FormElementModel> getFormElements() {
		List<FormElementModel> keyList = new ArrayList<FormElementModel>();
		recursivelyGetFormElements(group, keyList);
		return keyList;
	}


	private void recursivelyGetFormElements(FormElementModel group,
			List<FormElementModel> keyList) {
		for (FormElementModel m : group.getChildren()) {
			if ( m.isMetadata() ) {
				keyList.add(m);
			} else {
				if (isPhantomOfSubmissionSet(m.getFormDataModel())) {
					if ( m.getFormDataModel().getElementType() != ElementType.GEOPOINT ) {
						recursivelyGetFormElements(m, keyList);
					} else {
						keyList.add(m); // geopoints are structured fields
					}
				} else {
					keyList.add(m);
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

	public String getFormId() {
	  return form.getFormId();
	}

	protected DynamicCommonFieldsBase getGroupBackingObject(FormElementModel m) {
		return dbEntities.get(m.getFormDataModel().getDDRelationName());
	}

	protected DynamicCommonFieldsBase getGroupBackingObject() {
		return getGroupBackingObject(group);
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
			ElementFormatter elemFormatter, CallingContext cc) throws ODKDatastoreException {
		SubmissionValue value = elementsToValues.get(propertyName);
		if (value != null) {
			value.formatValue(elemFormatter, row, getOrdinalNumAsStr(), cc);
		}
	}

	protected void populateFormattedValuesInRow(Row row,
			List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter, CallingContext cc) throws ODKDatastoreException {
		if (propertyNames == null) {
			List<SubmissionValue> values = getSubmissionValues();
			for (SubmissionValue value : values) {
				value.formatValue(elemFormatter, row, getOrdinalNumAsStr(), cc);
			}
		} else {
			for (FormElementModel element : propertyNames) {
				populateFormattedValueInRow(row, element, elemFormatter, cc);
			}
		}
	}

	/**
	 * Format this submission's properties into a Row object.
	 *
	 * @param propertyNames - if null, all properties are emitted.  Otherwise, a list of those to emit.
	 * @param elemFormatter
	 * @param includeParentUid - true if the parentUid should be included
	 * @return
	 * @throws ODKDatastoreException
	 */
	public Row getFormattedValuesAsRow(List<FormElementModel> propertyNames,
			ElementFormatter elemFormatter, boolean includeParentUid, CallingContext cc) throws ODKDatastoreException {
		Row row = new Row(constructSubmissionKey(null));
		if(includeParentUid && !(this instanceof Submission)) {
		  elemFormatter.formatUid(enclosingSet.getKey().getKey(), enclosingSet.getPropertyName(), row);
		}
		populateFormattedValuesInRow(row, propertyNames, elemFormatter, cc);
		return row;
	}

	public SubmissionSet getEnclosingSet() {
		return enclosingSet;
	}

	public void printSubmission(PrintWriter out) {

		List<SubmissionValue> values = getSubmissionValues();
		for (SubmissionValue value : values) {
			out.println(value.toString());
		}
	}

	public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc)
			throws ODKOverQuotaException, ODKDatastoreException {
		for (SubmissionValue value : getSubmissionValues()) {
			value.recursivelyAddEntityKeys(keyList, cc);
		}
		for ( DynamicCommonFieldsBase e : dbEntities.values() ) {
			keyList.add(e.getEntityKey());
		}
	}

	@Override
	public boolean depthFirstTraversal( SubmissionVisitor visitor ) {
	  if ( visitor.traverse(this) ) return true;

     for (SubmissionValue value : getSubmissionValues()) {
       if ( value != null && value.depthFirstTraversal(visitor) ) return true;
     }

     return false;
	}

	/**
	 * Remove the element from the submission.
	 * Only implemented for binary attachments at this time.
	 * For use when manually marking a form as complete.
	 *
	 * @param m
	 */
	public void removeElementValue(FormElementModel m) {
	  if ( m.isMetadata() ) {
	    throw new IllegalStateException("cannot remove metadata");
	  }

	  if ( m.getElementType() != FormElementModel.ElementType.BINARY ) {
	    throw new IllegalStateException("only removal of binary objects supported");
	  }

	  elementsToValues.remove(m);
	}

	public void persist(CallingContext cc)
			throws ODKEntityPersistException, ODKOverQuotaException {
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
				entry.getValue().persist(cc);
				break;
			}
		}

		// persist phantom tables excluding ourselves...
		Set<CommonFieldsBase> others = new HashSet<CommonFieldsBase>();
		others.addAll(dbEntities.values());
		others.remove(getGroupBackingObject());
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		ds.putEntities(others, user);

		// and finally, persist us...
		ds.putEntity(getGroupBackingObject(), user);
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
