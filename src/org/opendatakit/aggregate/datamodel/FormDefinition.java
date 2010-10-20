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
package org.opendatakit.aggregate.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

/**
 * Describes everything about the structure of a given xform as extracted
 * by javarosa during parsing of the xform and as backed by the FormDataModel
 * within the persistence layer.
 * Note that the metadata associated with the xform will be stored
 * separately in the form information table.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class FormDefinition {
	public static final String domainRoot = "test.test";
	
	public static final Map<String, FormDefinition> formDefinitions = new HashMap<String, FormDefinition>();
	
	/** map from uri to FormDataModel; with navigable parent/child structure */
	public final Map<String, FormDataModel> uriMap = new HashMap<String, FormDataModel>();
	/** list of all tables (form, repeat group and auxillary) */
	public final List<FormDataModel> tableList = new ArrayList<FormDataModel>();
	/** list of non-repeat groups in xform */
	public final List<FormDataModel> groupList = new ArrayList<FormDataModel>();
	/** list of structured fields in xform */
	public final List<FormDataModel> geopointList = new ArrayList<FormDataModel>();
	/** map from fully qualified tableName to CFB definition */
	public final Map<String, CommonFieldsBase> backingTableMap;

	FormDataModel longStringRefTextModel = null;
	LongStringRefText longStringRefTextTable = null;
	FormDataModel refTextModel = null;
	RefText refTextTable = null;
	FormDataModel formName = null;
	FormDataModel topLevelGroup = null;
	
	private final String qualifiedTopLevelTable;
	private final String formId;

	private static FormDataModel fdm = null;
	
	public static final FormDataModel getFormDataModel(Datastore datastore, User user) throws ODKDatastoreException {
		if ( fdm == null ) {
			fdm = new FormDataModel(datastore.getDefaultSchemaName());
			datastore.createRelation(fdm, user);
		}
		return fdm;
	}
	
	public static final String extractWellFormedFormId(String submissionKey, Realm realm) {
		int firstSlash = submissionKey.indexOf('/');
		String formId = submissionKey;
		if ( firstSlash != -1 ) {
			// strip off the group path of the key
			formId = submissionKey.substring(0, firstSlash);
		}
		if ( formId.indexOf(':') == -1 ) {
			return realm.getRootDomain() + ":" + formId;
		}
		return formId;
	}
	
	public static final FormDefinition getFormDefinition(String formId, Datastore datastore, User user) {

		if ( formId.indexOf('/') != -1 ) {
			throw new IllegalArgumentException("formId is not well formed: " + formId);
		}
		if (formId.indexOf(':') == -1) {
			throw new IllegalArgumentException("formId is not well formed: " + formId);
		}
		
		FormDefinition fd = formDefinitions.get(formId);
		if ( fd == null ) {
			List<? extends CommonFieldsBase> fdmList = null;
			try {
				FormDataModel fdm = FormDefinition.getFormDataModel(datastore, user);
				Query query = datastore.createQuery(fdm, user);
				query.addFilter(fdm.uriFormId, FilterOperation.EQUAL, formId);
				fdmList = query.executeQuery(0);
			} catch (ODKDatastoreException e) {
				return null;
			}
			if ( fdmList == null || fdmList.size() == 0 ) {
				return null;
			}
			
			fd = new FormDefinition(formId, fdmList);
			if ( fd != null ) {
				try {
					// update the form data model with the actual dimensions
					// of its columns -- or create the tables from scratch...
					for ( Map.Entry<String, CommonFieldsBase> e : fd.backingTableMap.entrySet() ) {
						
						datastore.createRelation(e.getValue(), user);
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
					formDefinitions.put(formId, fd);
				}
			}
		}
		return fd;
	}

	public FormDefinition(String formId, List<?> formDataModelList) {
		this.formId = formId;
		
		// map of tableName to map of columnName, FDM record
		Map<String, Map<String, FormDataModel >> eeMap = new HashMap< String, Map<String, FormDataModel>>();
			
		for ( Object o : formDataModelList ) {
			FormDataModel m = (FormDataModel) o;
			uriMap.put(m.getUri(), m);
			String table = m.getPersistAsQualifiedTableName();
			String column = m.getPersistAsColumn();
			if ( column != null && table == null ) {
				throw new IllegalStateException("Form id: " + m.getUriFormId() +
					" - Unexpected null persist-as table name when persist-as column name is: "
					+ column );
			}
			if ( column == null ) {
				FormDataModel.ElementType type = m.getElementType();
				if ( table == null ) {
					// should be structured field (e.g., geopoint) or form name.
					switch ( type ) {
					case FORM_NAME:
						if ( formName != null ) {
							throw new IllegalStateException("Multiple formName entries!");
						}
						formName = m;
						break;
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
		for ( FormDataModel m : uriMap.values() ) {
			String uriParent = m.getParentAuri();
			if ( uriParent != null ) {
				FormDataModel p = uriMap.get(uriParent);
				m.setParent(p);
				p.setChild(m.getOrdinalNumber(), m);
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
		backingTableMap = new HashMap<String, CommonFieldsBase>();
		for ( FormDataModel m : tableList ) {
			String tableName = (String) m.getPersistAsQualifiedTableName();
			
			CommonFieldsBase b = backingTableMap.get(tableName);
			if ( b != null ) {
				throw new IllegalStateException("Backing table already linked back: " + tableName);
			}

			switch ( m.getElementType()) {
			case SELECT1: // obsoleting this...
				b = new SelectChoice(m.getPersistAsSchema(),m.getPersistAsTable());
				m.setBackingObject(b);
				break;
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
				longStringRefTextModel = m;
				longStringRefTextTable = new LongStringRefText(m.getPersistAsSchema(),m.getPersistAsTable());
				b = longStringRefTextTable;
				m.setBackingObject(b);
				break;
			case REF_TEXT:
				if ( refTextTable != null ) {
					throw new IllegalStateException("multiple ref text tables defined!");
				}
				refTextModel = m;
				refTextTable = new RefText(m.getPersistAsSchema(),m.getPersistAsTable());
				b = refTextTable;
				m.setBackingObject(b);
				break;
			default:
				throw new IllegalStateException("Unexpectedly no column but has table for type " + m.getElementType().toString());
			}
			backingTableMap.put(tableName, b);
		}
		
		// and now handle the tables in the main form...
		for ( Map.Entry<String, Map<String, FormDataModel>> e : eeMap.entrySet() ) {
			String tableName = e.getKey();
			CommonFieldsBase b = backingTableMap.get(tableName);
			Collection<FormDataModel> c = e.getValue().values();
			for ( FormDataModel m : c) {
				if ( b == null ) {
					b = new InstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
					backingTableMap.put(tableName, b);
				}
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
				
				DataField dfd = new DataField(m.getPersistAsColumn(), dataType, true);
				b.getFieldList().add( dfd );
				m.setBackingKey(dfd);
				m.setBackingObject(b);
			}
		}

		// set the backing object for the geopointList
		// geopoint values are all stored within the same table...
		// it is fine to grab the backing object of the first child...
		for ( FormDataModel m : geopointList ) {
			if ( m.getPersistAsTable() == null ) {
				throw new IllegalStateException("geopoints should identify their backing table");
			}
			String tableName = m.getPersistAsQualifiedTableName();
			CommonFieldsBase b = backingTableMap.get(tableName);
			if ( b == null ) {
				b = new InstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
				backingTableMap.put(tableName, b);
			}
			m.setBackingObject(b);
		}

		// set the backing object for the groupList
		for ( FormDataModel m : groupList ) {
			if ( m.getPersistAsTable() == null ) {
				throw new IllegalStateException("groups and repeats should identify their backing table");
			}
			String tableName = m.getPersistAsQualifiedTableName();
			CommonFieldsBase b = backingTableMap.get(tableName);
			if ( b == null ) {
				b = new InstanceData(m.getPersistAsSchema(), m.getPersistAsTable());
				backingTableMap.put(tableName, b);
			}
			m.setBackingObject(b);
		}

		if ( formName == null ) {
			throw new IllegalStateException("No form name defined");
		}
		
		
		if ( (formName.getChildren().size() != 2) || 
			 (formName.getChildren().get(1).getElementType() != ElementType.LONG_STRING_REF_TEXT)) {
			throw new IllegalStateException("Expected only one group and one long string element at top level");
		}

		topLevelGroup = formName.getChildren().get(0);
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
	}
	
	/**
	 * Get the top-level group for this form.
	 * 
	 * @return
	 */
	public final FormDataModel getTopLevelGroup() {
		return topLevelGroup;
	}
	
	public final FormDataModel getElementByName(String name) {
		String[] path = name.split("/");
		FormDataModel m = topLevelGroup;
		boolean first = true;
		for ( String p : path ) {
			if ( first ) {
				first = false;
				// first entry can be form id...
				if ( formId.equals(p) ) continue; 
			}

			m = getElementByNameHelper(m, p);
			if ( m == null ) return null;
		}
		return m;		
	}
	
	private final FormDataModel getElementByNameHelper(FormDataModel group, String name) {
		if ( group.getElementName() != null && group.getElementName().equals(name)) {
			return group;
		}
		for ( FormDataModel m : group.getChildren() ) {
			// depth first traversal...
			FormDataModel tmp = getElementByNameHelper( m, name);
			if ( tmp != null ) return tmp;
		}
		return null;
	}
	
	/**
	 * Return the first-level repeat groups within this group.
	 * 
	 * @param group
	 * @return first-level list of groups within this group that are repeating.
	 */
	public final List<FormDataModel> getImmediateRepeatGroups(FormDataModel group) {
		List<FormDataModel> l = new ArrayList<FormDataModel>();

		// You could optimize this quite a bit, but the nesting of the groups 
		// is likely to be fairly shallow.  Leave as-is until it is proven
		// that this is a performance bottleneck.
		
		for ( FormDataModel m : groupList ) {
			if ( m == group ) continue;
			if ( m.getElementType() == ElementType.REPEAT ) {
				// OK -- verify that this is nested within 'group' and that
				// there are no intervening repeat groups in the chain up to 'group'
				// it is OK if parent ends up being the form name, as  that means 
				// the iterated-over group 'm' was not contained in 'group'
				FormDataModel parent = m.getParent();
				while ( parent != null && parent != group ) {
					if ( parent.getElementType() == ElementType.REPEAT) break;
					parent = m.getParent();
				}
				if ( parent == group ) {
					// OK. we found a first-level repeating group within this group
					l.add(m);
				}
			}
		}
		
		return l;
	}
	
	public final String getQualifiedTopLevelTable() {
		return qualifiedTopLevelTable;
	}
	
	public CommonFieldsBase getQualifiedTable(String qualifiedTableName) {
		return backingTableMap.get(qualifiedTableName);
	}

	public RefText getRefTextTable() {
		return refTextTable;
	}
	
	public LongStringRefText getLongStringRefTextTable() {
		return longStringRefTextTable;
	}

	public String getFormId() {
		return formId;
	}
	
	public String getFormName() {
		return formName.getElementName();
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

	public FormDataModel getElementForTable(CommonFieldsBase table) {
		for ( FormDataModel m : tableList ) {
			if ( table.sameTable(m.getBackingObjectPrototype()) ) return m;
		}
		return null;
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
	        RefText eElem = datastore.createEntityUsingRelation(refTextTable, topLevelTableAuri, user);
	        eElem.setValue(subString);
	        
			LongStringRefText t = datastore.createEntityUsingRelation(longStringRefTextTable, topLevelTableAuri, user);
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