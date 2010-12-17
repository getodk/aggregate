/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.BinaryContent;
import org.opendatakit.aggregate.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.InstanceData;
import org.opendatakit.aggregate.datamodel.LongStringRefText;
import org.opendatakit.aggregate.datamodel.RefBlob;
import org.opendatakit.aggregate.datamodel.RefText;
import org.opendatakit.aggregate.datamodel.SelectChoice;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.datamodel.TopLevelInstanceData;
import org.opendatakit.aggregate.datamodel.VersionedBinaryContent;
import org.opendatakit.aggregate.datamodel.VersionedBinaryContentRefBlob;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BooleanSubmissionType;
import org.opendatakit.aggregate.submission.type.LongSubmissionType;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Describes everything about the structure of a given xform as extracted
 * by javarosa during parsing of the xform and as backed by the FormDataModel
 * within the persistence layer.
 * Note that the metadata associated with the xform will be stored
 * separately in the form information table.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class FormDefinition {
	
	private static final Map<XFormParameters, FormDefinition> formDefinitions = new HashMap<XFormParameters, FormDefinition>();
	
	/** map from uri to FormDataModel; with navigable parent/child structure */
	public final Map<String, FormDataModel> uriMap = new HashMap<String, FormDataModel>();
	/** list of all tables (form, repeat group and auxillary) */
	private final List<FormDataModel> tableList = new ArrayList<FormDataModel>();
	/** list of non-repeat groups in xform */
	private final List<FormDataModel> groupList = new ArrayList<FormDataModel>();
	/** list of structured fields in xform */
	private final List<FormDataModel> geopointList = new ArrayList<FormDataModel>();
	/** map from fully qualified tableName to CFB definition */
	private final Map<String, DynamicCommonFieldsBase> backingTableMap;

	private LongStringRefText longStringRefTextTable = null;
	private RefText refTextTable = null;
	private FormDataModel topLevelGroup = null;
	private FormElementModel topLevelGroupElement = null;
	
	private final String qualifiedTopLevelTable;
	private final XFormParameters xformParameters;
	
	/**
	 * Append to the list the FormDataModel entries needed to represent this
	 * dynamic table.  Useful for generating the FormInfo data model from its 
	 * base tables.
	 * 
	 * @param list
	 * @param definitionKey
	 * @param form
	 * @param topLevel
	 * @param parent
	 * @param ordinal
	 * @param datastore
	 * @param user
	 * @return the ordinal of the last field defined.
	 * @throws ODKDatastoreException
	 */
	static final Long buildTableFormDataModel( List<FormDataModel> list, 
													DynamicCommonFieldsBase form, 
													DynamicCommonFieldsBase topLevel, 
													DynamicCommonFieldsBase parent,
													Long ordinal,
													Datastore datastore, User user ) throws ODKDatastoreException {
		FormDataModel fdm = FormDataModel.createRelation(datastore, user);
		FormDataModel d;
		
		if ( topLevel == null || 
			 ((form != fdm) && !( topLevel instanceof TopLevelDynamicBase )) ) {
			throw new IllegalStateException("topLevel entity must be present and a TopLevelDynamicBase!");
		}
		// we are making use of the fact that the PK in the 
		// FormDataModel is the PK within the relation model.
		final EntityKey k = new EntityKey( fdm, topLevel.getUri());
		final String parentURI = (parent == null) ? null : parent.getUri();
		
		// define the table...
		d = datastore.createEntityUsingRelation(fdm, user);
		list.add(d);
		// reset the PK to be the PK of the table we are representing
		d.setStringField(fdm.primaryKey, form.getUri());
		d.setLongField(fdm.ordinalNumber, ordinal);
		d.setStringField(fdm.parentAuri, parentURI);
		d.setStringField(fdm.topLevelAuri, topLevel.getUri());
		d.setStringField(fdm.elementName, form.getTableName());
		d.setStringField(fdm.elementType,
				(parent == topLevel) ?
				FormDataModel.ElementType.GROUP.toString() :
				FormDataModel.ElementType.REPEAT.toString() );
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, form.getTableName());
		d.setStringField(fdm.persistAsSchema, form.getSchemaName());
		
		// enforce defined ordinal positions based upon alphabetical sort
		List<DataField> sortedFields = new ArrayList<DataField>();
		sortedFields.addAll(form.getFieldList());
		Collections.sort(sortedFields, new Comparator<DataField>() {
			@Override
			public int compare(DataField o1, DataField o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		// loop through the fields...
		Long l = 0L;
		for ( DataField f : sortedFields ) {
			if ( f.getName().startsWith("_")) continue; // ignore metadata
			++l;
			
			// this field should be in the fdm model...
			d = datastore.createEntityUsingRelation(fdm, user);
			list.add(d);
			d.setStringField(fdm.primaryKey, form.getUri() + "-" + Long.toString(l));
			d.setLongField(fdm.ordinalNumber, l);
			d.setStringField(fdm.parentAuri, form.getUri());
			d.setStringField(fdm.topLevelAuri, k.getKey());
			d.setStringField(fdm.elementName, f.getName());
			switch ( f.getDataType() ) {
			case STRING:
				d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
				break;
			case INTEGER:
				d.setStringField(fdm.elementType, FormDataModel.ElementType.INTEGER.toString());
				break;
			case DECIMAL:
				d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL.toString());
				break;
			case BOOLEAN:
				d.setStringField(fdm.elementType, FormDataModel.ElementType.BOOLEAN.toString());
				break;
			case DATETIME:
				d.setStringField(fdm.elementType, FormDataModel.ElementType.JRDATETIME.toString());
				break;
			case URI:
			case BINARY: // this data type is hidden under BINARY content structure...
			case LONG_STRING: // this data type does not appear in model...
				default:
					throw new IllegalStateException("Unexpected DataType");
			}
			d.setStringField(fdm.persistAsColumn, f.getName());
			d.setStringField(fdm.persistAsTable, form.getTableName());
			d.setStringField(fdm.persistAsSchema, form.getSchemaName());
		}
		
		return l;
	}
	
	static final void buildBinaryContentFormDataModel( List<FormDataModel> list, 
			String binaryContentElementName,
			String binaryContentUri,
			String binaryContentTableName,
			String versionedBinaryUri,
			String versionedBinaryContentTableName,
			String versionedRefBlobUri,
			String versionedBinaryContentRefBlobTableName,
			String refBlobUri,
			String refBlobTableName,
			TopLevelDynamicBase topLevel, 
			DynamicCommonFieldsBase parent,
			Long ordinal,
			Datastore datastore, User user ) throws ODKDatastoreException {
		
		FormDataModel fdm = FormDataModel.createRelation(datastore, user);
		FormDataModel d;
		
		// we are making use of the fact that the PK in the 
		// FormDataModel is the PK within the relation model.
		final EntityKey k = new EntityKey( fdm, topLevel.getUri());
		final String topLevelURI = topLevel.getUri();
		final String parentURI = parent.getUri(); // there better be a parent!!!
		
		// record for binary content...
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, binaryContentUri);
		list.add(d);
		final String bcURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, ordinal);
		d.setStringField(fdm.parentAuri, parentURI);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, binaryContentElementName);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.BINARY.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, binaryContentTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for versioned binary content...
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, versionedBinaryUri);
		list.add(d);
		final String vbcURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, bcURI);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, binaryContentElementName);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, versionedBinaryContentTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for binary content ref blob..
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, versionedRefBlobUri);
		list.add(d);
		final String bcbURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, vbcURI);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, binaryContentElementName);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY_CONTENT_REF_BLOB.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, versionedBinaryContentRefBlobTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for ref blob...
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, refBlobUri);
		list.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, bcbURI);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, binaryContentElementName);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_BLOB.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, refBlobTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
	}
	
	
	static final void buildLongStringFormDataModel( List<FormDataModel> list, 
			String longStringRefTextUri,
			String longStringRefTextTableName,
			String refTextUri,
			String refTextTableName,
			TopLevelDynamicBase topLevel, 
			Long ordinal,
			Datastore datastore, User user ) throws ODKDatastoreException {
		
		FormDataModel fdm = FormDataModel.createRelation(datastore, user);
		FormDataModel d;
		
		// we are making use of the fact that the PK in the 
		// FormDataModel is the PK within the relation model.
		final EntityKey k = new EntityKey( fdm, topLevel.getUri());
		final String topLevelURI = topLevel.getUri();
		
		// record for long string ref text...
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, longStringRefTextUri);
		list.add(d);
		final String lst = d.getUri();
		d.setLongField(fdm.ordinalNumber, ordinal);
		d.setStringField(fdm.parentAuri, topLevelURI);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.LONG_STRING_REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, longStringRefTextTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for ref text...
		d = datastore.createEntityUsingRelation(fdm, user);
		d.setStringField(fdm.primaryKey, refTextUri);
		list.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, lst);
		d.setStringField(fdm.topLevelAuri, topLevelURI);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, refTextTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
	}
	
	static final void assertModel(XFormParameters p, List<FormDataModel> model, Datastore datastore, User user) throws ODKDatastoreException {
		FormDataModel fdm = FormDataModel.createRelation(datastore, user);
		if ( model == null || model.size() == 0 ) {
			throw new IllegalArgumentException("should never be null");
		}
		for ( FormDataModel m : model ) {
			try {
				datastore.getEntity(fdm, m.getUri(), user);
			} catch ( ODKEntityNotFoundException e ) {
				datastore.putEntity(m, user);
			}
		}
		
		// and if the model is all stored, then...
		String definitionUri = model.get(0).getTopLevelAuri();
		String formUri = CommonFieldsBase.newMD5HashUri(p.formId);
		
		SubmissionAssociationTable saRelation = SubmissionAssociationTable.createRelation(datastore, user);
		SubmissionAssociationTable sa = datastore.createEntityUsingRelation(saRelation, user);
		
		sa.setStringField(saRelation.primaryKey, definitionUri );
		sa.setDomAuri(formUri); // md5 of submissionFormId
		sa.setSubAuri(formUri); // md5 of rootElementFormId
		sa.setSubmissionFormId(p.formId);
		sa.setSubmissionModelVersion(p.modelVersion);
		sa.setSubmissionUiVersion(p.uiVersion);
		sa.setIsPersistenceModelComplete(true);
		sa.setIsSubmissionAllowed(true);
		sa.setUriSubmissionDataModel(definitionUri); // in general, this is arbitrary.  Fixed for FormInfo...
		
		try {
			datastore.getEntity(saRelation, definitionUri, user);
		} catch ( ODKEntityNotFoundException e ) {
			datastore.putEntity(sa, user);
		}
	}
	
	static final Submission assertFormInfoRecord(XFormParameters thisFormVersion, String thisFormName, String thisFormDescription, String formIdMd5Uri, Datastore datastore, User user) throws ODKDatastoreException {
		FormInfoTable fiRelation = Form.getFormInfoRelation(datastore);
		FormDefinition formInfoDefinition = Form.getFormInfoDefinition(datastore);
		return assertFormInfoRecord(fiRelation, formInfoDefinition, thisFormVersion, thisFormName, thisFormDescription, formIdMd5Uri, datastore, user); 
	}

	/**
	 * Use the variant of this method without the first two arguments for every form definition
	 * except the FormInfo table itself.  This variant is provided to support bootstrapping only.
	 * 
	 * @param fiRelation
	 * @param formInfoDefinition
	 * @param thisFormVersion
	 * @param thisFormName
	 * @param thisFormDescription
	 * @param thisFormIdMd5Uri
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	static final Submission assertFormInfoRecord(FormInfoTable fiRelation, FormDefinition formInfoDefinition, XFormParameters thisFormVersion, String thisFormName, String thisFormDescription, String thisFormIdMd5Uri, Datastore datastore, User user) throws ODKDatastoreException {
		// Now create a record in the FormInfo table for the PersistentResults table itself...
		TopLevelDynamicBase fi = null;
		try {
			fi = datastore.getEntity(fiRelation, thisFormIdMd5Uri, user);
		} catch ( ODKEntityNotFoundException e ) {
			// we must have failed before persisting a FormInfo record
			// or this must be our first time through...
			Submission formInfo = new Submission(thisFormVersion.modelVersion, thisFormVersion.uiVersion,
					thisFormIdMd5Uri, formInfoDefinition, datastore, user);
			((StringSubmissionType) formInfo.getElementValue(FormInfo.formId)).setValueFromString(thisFormVersion.formId);
			// default description...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiDescriptionTable);
				SubmissionSet sDescription = new SubmissionSet(formInfo, 1L, FormInfo.fiDescriptionTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((StringSubmissionType) sDescription.getElementValue(FormInfo.formName)).setValueFromString(thisFormName);
				((StringSubmissionType) sDescription.getElementValue(FormInfo.description)).setValueFromString(thisFormDescription);
				r.addSubmissionSet(sDescription);
			}
			// fileset...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiFilesetTable);
				SubmissionSet sFileset = new SubmissionSet(formInfo, 1L, FormInfo.fiFilesetTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((LongSubmissionType) sFileset.getElementValue(FormInfo.rootElementModelVersion)).setValueFromString(thisFormVersion.modelVersion.toString());
				((LongSubmissionType) sFileset.getElementValue(FormInfo.rootElementUiVersion)).setValueFromString(thisFormVersion.uiVersion.toString());
				((BooleanSubmissionType) sFileset.getElementValue(FormInfo.isFilesetComplete)).setValueFromString("yes");
				((BooleanSubmissionType) sFileset.getElementValue(FormInfo.isDownloadAllowed)).setValueFromString("yes");
				r.addSubmissionSet(sFileset);
			}
			// submission...
			{
				RepeatSubmissionType r = (RepeatSubmissionType) formInfo.getElementValue(FormInfo.fiSubmissionTable);
				SubmissionSet sSubmission = new SubmissionSet(formInfo, 1L, FormInfo.fiSubmissionTable, formInfoDefinition, formInfo.getKey(), datastore, user);
				((StringSubmissionType) sSubmission.getElementValue(FormInfo.submissionFormId)).setValueFromString(thisFormVersion.formId);
				((LongSubmissionType) sSubmission.getElementValue(FormInfo.submissionModelVersion)).setValueFromString(thisFormVersion.modelVersion.toString());
				((LongSubmissionType) sSubmission.getElementValue(FormInfo.submissionUiVersion)).setValueFromString(thisFormVersion.uiVersion.toString());
				r.addSubmissionSet(sSubmission);
			}
			formInfo.persist(datastore, user);
			
			fi = datastore.getEntity(fiRelation, thisFormIdMd5Uri, user);
		}
		
		// and retrieve cleanly... 
	    Submission formInfo = new Submission(fi, formInfoDefinition, datastore, user);
	    return formInfo;
	}

	static final FormElementModel findElement(FormElementModel group, DataField backingKey) {
		for ( FormElementModel m : group.getChildren()) {
			if ( m.isMetadata() ) continue;
			if ( m.getFormDataModel().getBackingKey() == backingKey ) return m;
		}
		return null;
	}

	public static final FormDefinition getFormDefinition(XFormParameters p, Datastore datastore, User user) {

		if ( p.formId.indexOf('/') != -1 ) {
			throw new IllegalArgumentException("formId is not well formed: " + p.formId);
		}
		
		FormDefinition fd = formDefinitions.get(p);
		if ( fd == null ) {
			List<? extends CommonFieldsBase> fdmList = null;
			try {
				// changes here should be paralleled in the FormParserForJavaRosa
			    SubmissionAssociationTable saRelation = SubmissionAssociationTable.createRelation(datastore, user);
			    String submissionFormIdUri = CommonFieldsBase.newMD5HashUri(p.formId); // key under which submission is located...
			    Query q = datastore.createQuery(saRelation, user);
			    q.addFilter( saRelation.domAuri, Query.FilterOperation.EQUAL, submissionFormIdUri);
			    List<? extends CommonFieldsBase> l = q.executeQuery(0);
			    SubmissionAssociationTable sa = null;
			    String fdmSubmissionUri = CommonFieldsBase.newUri();
			    for ( CommonFieldsBase b : l ) {
			    	SubmissionAssociationTable t = (SubmissionAssociationTable) b;
			    	if ( t.getXFormParameters().equals(p) ) {
			    		sa = t;
			    		fdmSubmissionUri = sa.getUriSubmissionDataModel();
			    		break;
			    	}
			    }
			    if ( sa == null ) return null;
			    // OK.  Found an sa record -- use it to find the fdm entries...
			    FormDataModel fdm = FormDataModel.createRelation(datastore, user);
				Query query = datastore.createQuery(fdm, user);
				query.addFilter(fdm.topLevelAuri, FilterOperation.EQUAL, fdmSubmissionUri);
				fdmList = query.executeQuery(0);
			} catch (ODKDatastoreException e) {
				return null;
			}
			if ( fdmList == null || fdmList.size() == 0 ) {
				return null;
			}
			
			fd = new FormDefinition(p, fdmList);
			
			if ( fd != null ) {
				try {
					// update the form data model with the actual dimensions
					// of its columns -- or create the tables from scratch...
					for ( Map.Entry<String, DynamicCommonFieldsBase> e : fd.backingTableMap.entrySet() ) {
						
						datastore.assertRelation(e.getValue(), user);
					}
					
					// and compute the set of element URIs that 
					// have text within the longStringText table.
					
					// this should be a small number, so we should be able to cache
					// it effectively.  It will simplify the follow-on query construction.
					LongStringRefText lsr = fd.getLongStringRefTextTable();
					
					Query queryLsr = datastore.createQuery(lsr, user);
					List<?> fdmsWithLongStrings = queryLsr.executeDistinctValueForDataField( lsr.uriFormDataModel );
					fd.tagLongStringElements(fdmsWithLongStrings);
					
				} catch (ODKDatastoreException e1) {
					e1.printStackTrace();
					fd = null;
				}

				// errors might have cleared the fd...
				if ( fd != null ) {
					// remember details about this form
					formDefinitions.put(p, fd);
				}
			}
		}
		return fd;
	}

    static final void forgetFormId(XFormParameters p) {
		formDefinitions.remove(p);
	}

	public FormDefinition(XFormParameters xformParameters, List<?> formDataModelList) {
		this.xformParameters = xformParameters;
		
		// map of tableName to map of columnName, FDM record
		Map<String, Map<String, FormDataModel >> eeMap = new HashMap< String, Map<String, FormDataModel>>();
			
		for ( Object o : formDataModelList ) {
			FormDataModel m = (FormDataModel) o;
			uriMap.put(m.getUri(), m);
			String table = m.getPersistAsQualifiedTableName();
			String column = m.getPersistAsColumn();
			if ( column != null && table == null ) {
				throw new IllegalStateException("Fdm uri: " + m.getUri() +
					" - Unexpected null persist-as table name when persist-as column name is: "
					+ column );
			}
			if ( column == null ) {
				FormDataModel.ElementType type = m.getElementType();
				if ( table == null ) {
					// should be structured field (e.g., geopoint) or form name.
					switch ( type ) {
					case GEOPOINT:
						geopointList.add(m);
						break;
					default:
						throw new IllegalStateException("Unexpectedly no column and no table for type " + type.toString());
					}
				} else {
					// should be either a structured field (e.g., geopoint),
					// group or repeat element or 
					// one of the auxiliary table types.
					// assume it is for now; will throw an exception later...
					switch ( type ) {
					case GEOPOINT:
						geopointList.add(m);
						break;
					case GROUP:
					case REPEAT:
					case PHANTOM:
						groupList.add(m);
						break;
					default:
						tableList.add(m);
						break;
					}
				}
			} else {
				// a field or structured field part
				Map<String, FormDataModel> mfdm = eeMap.get(table);
				if ( mfdm == null ) {
					mfdm = new HashMap<String, FormDataModel>();
					eeMap.put(table, mfdm);
				}
				mfdm.put(column, m);
			}
		}
		
		// stitch up data model's parent and child links...
		// everything has a parent except the top-level group and 
		// long string text ref tables, which refer to the 
		// key into the form_info table...
		int nullParentCount = 0;
		for ( FormDataModel m : uriMap.values() ) {
			String uriParent = m.getParentAuri();
			if ( uriParent == null ) {
				throw new IllegalStateException("Every record in FormDataModel should have a parent key");
			}
			
			FormDataModel p = uriMap.get(uriParent);
			if ( p != null ) {
				m.setParent(p);
				p.setChild(m.getOrdinalNumber(), m);
			} else if ( m.getElementType() != ElementType.LONG_STRING_REF_TEXT ) {
				if ( m.getElementType() != ElementType.GROUP ) {
					throw new IllegalStateException("Expected upward references only from GROUP elements");
				}
				if ( ++nullParentCount > 1 ) {
					throw new IllegalStateException("Expected at most one top level group");
				}
				topLevelGroup = m;
			}
		}

		// ensure there are no nulls in the children array.
		// nulls would indicate a skipped ordinal position.
		for ( FormDataModel m : uriMap.values() ) {
			m.validateChildren();
		}
		
		// OK.  we have the list of tables, map of fqn's, 
		// form name, non-repeat groups, geopoints, and 
		// fully linked map of parent and children.
		
		// Now construct the descriptions of the tables
		// that represent this form.
		backingTableMap = new HashMap<String, DynamicCommonFieldsBase>();
		for ( FormDataModel m : tableList ) {
			String tableName = (String) m.getPersistAsQualifiedTableName();
			
			DynamicCommonFieldsBase b = backingTableMap.get(tableName);
			if ( b != null ) {
				throw new IllegalStateException("Backing table already linked back: " + tableName);
			}

			switch ( m.getElementType()) {
			case SELECTN:
				b = new SelectChoice(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
			case BINARY:
				b = new BinaryContent(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
			case VERSIONED_BINARY:
				b = new VersionedBinaryContent(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
			case VERSIONED_BINARY_CONTENT_REF_BLOB:
				b = new VersionedBinaryContentRefBlob(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
			case REF_BLOB:
				b = new RefBlob(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
			case LONG_STRING_REF_TEXT:
				if ( longStringRefTextTable != null ) {
					throw new IllegalStateException("multiple long string ref text tables defined!");
				}
				longStringRefTextTable = new LongStringRefText(m.getPersistAsSchema(),m.getPersistAsTable());
				b = longStringRefTextTable;
				m.setBackingObject(b);
				break;
			case REF_TEXT:
				if ( refTextTable != null ) {
					throw new IllegalStateException("multiple ref text tables defined!");
				}
				refTextTable = new RefText(m.getPersistAsSchema(),m.getPersistAsTable());
				b = refTextTable;
				m.setBackingObject(b);
				break;
			default:
				throw new IllegalStateException("Unexpectedly no column but has table for type " + m.getElementType().toString());
			}
			backingTableMap.put(tableName, b);
		}
		

		boolean isWellKnownForm = false;
		// set the backing objects for the tables identified in the groupList
		if ( xformParameters.formId.equals(Form.URI_FORM_ID_VALUE_FORM_INFO) ) {
			// it is the FormInfo table -- pre-populate the backingTableMap
			// with the table relations we know...
			FormInfo.populateBackingTableMap(backingTableMap);
			isWellKnownForm = true;
		} else if ( xformParameters.formId.equals(PersistentResults.FORM_ID_PERSISTENT_RESULT)) {
			// it is the PersistentResults table - pre-populate the backingTableMap
			PersistentResults.populateBackingTableMap(backingTableMap);
			isWellKnownForm = true;
		} else if ( xformParameters.formId.equals(MiscTasks.FORM_ID_MISC_TASKS)) {
			// it is the PersistentResults table - pre-populate the backingTableMap
			MiscTasks.populateBackingTableMap(backingTableMap);
			isWellKnownForm = true;
		}
		
		for ( FormDataModel m : groupList ) {
			if ( m.getPersistAsTable() == null ) {
				throw new IllegalStateException("groups, phantoms and repeats should identify their backing table");
			}
			String tableName = m.getPersistAsQualifiedTableName();
			DynamicCommonFieldsBase b = backingTableMap.get(tableName);
			if ( b == null ) {
				if ( isWellKnownForm ) {
					throw new IllegalStateException("Well-known forms expect all backing tables to be defined");
				}
				/*
				 * Determine if the given group is equivalent to the top level group.  This
				 * occurs when a given group's elements can be collapsed into the top level group
				 * within the persistence layer (the top level group's backing object then holds
				 * the data elements defined within it and within the given group).
				 * When this collapse happens, the group and the parent group share
				 * the same qualified table name.  Phantom and Repeat elements are automatically
				 * not equivalent to the top level group.
				 */
				boolean equivalentToTopLevelGroup = true;
				FormDataModel current = m;
				while ( current != null ) {
					if ( (current.getElementType() == ElementType.REPEAT) ||
						 (current.getElementType() == ElementType.PHANTOM)) {
						// automatically not equivalent
						equivalentToTopLevelGroup = false;
						break;
					}
					FormDataModel parent = current.getParent();
					if ( parent != null && 
						 ( !current.getPersistAsQualifiedTableName().equals(parent.getPersistAsQualifiedTableName())) ) {
						// backing tables are different -- not equivalent!
						equivalentToTopLevelGroup = false;
						break;
					}
					current = parent;
				}

				if ( equivalentToTopLevelGroup ) {
					b = new TopLevelInstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
				} else {
					b = new InstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
				}
				backingTableMap.put(tableName, b);
			}
			m.setBackingObject(b);
		}

		// set the backing object for the geopointList.
		// Geopoint value fields are all stored within the same table...
		// if the backing table was not yet defined by the groupList loop
		// above, then the backing table will never be equivalent to 
		// a top-level group.
		for ( FormDataModel m : geopointList ) {
			if ( m.getPersistAsTable() == null ) {
				throw new IllegalStateException("geopoints should identify their backing table");
			}
			String tableName = m.getPersistAsQualifiedTableName();
			DynamicCommonFieldsBase b = backingTableMap.get(tableName);
			if ( b == null ) {
				if ( isWellKnownForm ) {
					throw new IllegalStateException("Well-known forms expect all backing tables to be defined");
				}
				b = new InstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
				backingTableMap.put(tableName, b);
			}
			m.setBackingObject(b);
		}

		// and now handle the primitive data elements in the main form...
		// all the backing tables must have been created at this point, 
		// so it is a logic error if we find one that isn't.
		for ( Map.Entry<String, Map<String, FormDataModel>> e : eeMap.entrySet() ) {
			String tableName = e.getKey();
			DynamicCommonFieldsBase b = backingTableMap.get(tableName);
			Collection<FormDataModel> c = e.getValue().values();
			
			// we should have created all the backing tables in the previous
			// two loops.  If not, it is a logic error.
			if ( b == null ) {
				throw new IllegalStateException("Backing table is not yet defined!");
			}

			for ( FormDataModel m : c) {
				DataField.DataType dataType = DataField.DataType.STRING;
				switch ( m.getElementType() ) {
				case STRING:
					dataType = DataField.DataType.STRING;
					break;
				case JRDATETIME:
				case JRDATE:
				case JRTIME:
					dataType = DataField.DataType.DATETIME;
					break;
				case INTEGER:
					dataType = DataField.DataType.INTEGER;
					break;
				case DECIMAL:
					dataType = DataField.DataType.DECIMAL;
					break;
				case BOOLEAN:
					dataType = DataField.DataType.BOOLEAN;
					break;
				default:
					String name = m.getElementName();
					if ( name == null ) name = "--blank--";
					throw new IllegalStateException("Element: " + name + "uri: " + m.getUri() + "Unexpected data type: " + m.getElementType().toString());
				}
				
				DataField dfd = null;
				if ( isWellKnownForm ) {
					for ( DataField f : b.getFieldList() ) {
						if ( m.getPersistAsColumn().equals(f.getName())) {
							dfd = f;
							break;
						}
					}
					if ( dfd == null ) {
						throw new IllegalStateException("Unable to locate data field in a well-known form");
					}
					if ( !dfd.getDataType().equals(dataType) ) {
						throw new IllegalStateException("Data type of data field " + dfd.getName() + " is different than expected");
					}
				} else {
					dfd = new DataField(m.getPersistAsColumn(), dataType, true);
					b.addDataField(dfd);
				}
				m.setBackingKey(dfd);
				m.setBackingObject(b);
			}
		}
		
		if ( topLevelGroup == null ) {
			throw new IllegalStateException("Top level group could not be found");
		}

		if ( topLevelGroup.getElementType() != ElementType.GROUP ) {
			throw new IllegalStateException("Top level group is a non-group!");
		}

		qualifiedTopLevelTable = topLevelGroup.getPersistAsQualifiedTableName();
		
		if ( refTextTable == null ) {
			throw new IllegalStateException("No ref text table declared!");
		}

		if ( longStringRefTextTable == null ) {
			throw new IllegalStateException("No long string ref text table declared!");
		}
		
		topLevelGroupElement = FormElementModel.buildFormElementModelTree(topLevelGroup);
	}
	
	/**
	 * Get the top-level group for this form.
	 * 
	 * @return
	 */
	public final FormDataModel getTopLevelGroup() {
		return topLevelGroup;
	}
	
	public final FormElementModel getTopLevelGroupElement() {
		return topLevelGroupElement;
	}
	
	public final FormElementModel getElementByName(String name) {
		String[] path = name.split("/");
		FormElementModel m = topLevelGroupElement;
		boolean first = true;
		for ( String p : path ) {
			if ( first ) {
				first = false;
				// first entry can be form id...
				if ( xformParameters.formId.equals(p) ) continue; 
			}

			m = getElementByNameHelper(m, p);
			if ( m == null ) return null;
		}
		return m;		
	}
	
	private final FormElementModel getElementByNameHelper(FormElementModel group, String name) {
		if ( group.getElementName() != null && group.getElementName().equals(name)) {
			return group;
		}
		for ( FormElementModel m : group.getChildren() ) {
			// depth first traversal...
			FormElementModel tmp = getElementByNameHelper( m, name);
			if ( tmp != null ) return tmp;
		}
		return null;
	}
	
	public final String getQualifiedTopLevelTable() {
		return qualifiedTopLevelTable;
	}
	
	public CommonFieldsBase getQualifiedTable(String qualifiedTableName) {
		return backingTableMap.get(qualifiedTableName);
	}
	
	public Collection<? extends CommonFieldsBase> getBackingTableSet() {
		return backingTableMap.values();
	}

	public RefText getRefTextTable() {
		return refTextTable;
	}
	
	public LongStringRefText getLongStringRefTextTable() {
		return longStringRefTextTable;
	}

	public String getFormId() {
		return xformParameters.formId;
	}
	
	public Long getModelVersion() {
		return xformParameters.modelVersion;
	}
	
	public Long getUiVersion() {
		return xformParameters.uiVersion;
	}
	
	public String getTopLevelAuri() {
		return topLevelGroup.getTopLevelAuri();
	}
	
	public void tagLongStringElements(List<?> fdmsWithLongStrings) {
		for ( Object o : fdmsWithLongStrings ) {
			String uri = (String) o;
			FormDataModel m = uriMap.get(uri);
			if ( m == null ) {
				throw new IllegalArgumentException("Unexpected failure to map uri to column set: " + uri);
			}
			m.setMayHaveExtendedStringData(true);
		}
	}

	public void setLongString(String text, String parentKey, String uriFormDataModel, EntityKey topLevelTableAuri, Datastore datastore,
			User user) throws ODKEntityPersistException {
		
		long textLimit = refTextTable.value.getMaxCharLen();
		// TODO: create the parts...
		long i = 1;
	    for(long index = 0; index < text.length(); index = index + textLimit) {
	    	long endCopy = index + textLimit;
	    	if ( endCopy > text.length() ) endCopy = text.length();
	    	String subString = text.substring((int) index, (int) endCopy);
	        RefText eElem = datastore.createEntityUsingRelation(refTextTable, user);
	        eElem.setTopLevelAuri(topLevelTableAuri.getKey());
	        eElem.setValue(subString);
	        
			LongStringRefText t = datastore.createEntityUsingRelation(longStringRefTextTable, user);
			t.setTopLevelAuri(topLevelTableAuri.getKey());
			t.setDomAuri(parentKey);
			t.setSubAuri(eElem.getUri());
			t.setPart(i++);
			t.setUriFormDataModel(uriFormDataModel);
			
			datastore.putEntity(eElem, user);
			datastore.putEntity(t, user);
	    }
	}

	public String getLongString(String parentKey, String uriFormDataModel, Datastore datastore,
			User user) throws ODKDatastoreException {
		
		StringBuilder b = new StringBuilder();
		
		Query q = datastore.createQuery(longStringRefTextTable, user);
		q.addFilter(longStringRefTextTable.domAuri, FilterOperation.EQUAL, parentKey);
		q.addFilter(longStringRefTextTable.uriFormDataModel, FilterOperation.EQUAL, uriFormDataModel);
		q.addSort(longStringRefTextTable.part, Direction.ASCENDING);
		
		List<? extends CommonFieldsBase> elements = q.executeQuery(ServletConsts.FETCH_LIMIT);
		for (CommonFieldsBase cb : elements ) {
			LongStringRefText e = (LongStringRefText) cb;
			RefText eElem = datastore.getEntity(refTextTable, e.getSubAuri(), user);
			b.append(eElem.getValue());
		}
		return b.toString();
	}

	public void recursivelyAddLongStringTextEntityKeys(List<EntityKey> keyList, String parentKey, String uriFormDataModel,
			Datastore datastore, User user) throws ODKDatastoreException {

		Query q = datastore.createQuery(longStringRefTextTable, user);
		q.addFilter(longStringRefTextTable.domAuri, FilterOperation.EQUAL, parentKey);
		q.addFilter(longStringRefTextTable.uriFormDataModel, FilterOperation.EQUAL, uriFormDataModel);
		q.addSort(longStringRefTextTable.part, Direction.ASCENDING);
		
		List<? extends CommonFieldsBase> elements = q.executeQuery(ServletConsts.FETCH_LIMIT);
		for (CommonFieldsBase cb : elements ) {
			LongStringRefText e = (LongStringRefText) cb;
			keyList.add(new EntityKey(e, e.getUri()));
			keyList.add(new EntityKey(refTextTable, e.getSubAuri()));
		}
	}

	public String getElementKey(String keyString) {
		// TODO pick apart an "odkId" to return the key within... steal code from 0.9.3
		throw new IllegalStateException("unimplemented");
	}
}