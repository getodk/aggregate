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

import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.security.User;

/**
 * This entity defines the mapping between a submission element and a 
 * backing object and data field.  It is xform-specific, but is present
 * because the DataField object has reverse-pointers to the submission.
 * <p>
 * The element tags of a form and phantom sub-tables for large forms
 * are maintained in the FormDataModel table.  All tags are recorded,
 * even group tags that are not repeat groups.  Thus, once the 
 * javarosa xform definition has been parsed, it is no longer needed
 * for any subsequent processing.
 * <p>
 * Each form element records:
 * <ol><li>the uri of the form in which it belongs</li>
 * <li>the enclosing form element, if any</li>
 * <li>the position of this element within the enclosing form element</li>
 * <li>the type of this element</li>
 * <li>the name of the element (null if this is a phantom sub-table)</li>
 * <li>the column name in which it is stored (null if it is not a column)</li>
 * <li>the table name in which it is stored (null if it is not a table)</li>
 * </ol>
 * The table in which a column appears is found by following the chain of 
 * enclosing uri form elements upward until the first non-null table name
 * is encountered.  Thus, for non-repeat groups, they would have null
 * column and table names, and their columns would be stored under the 
 * enclosing table name.  Repeat groups are their own tables.  If a dataset
 * has many columns, it will be split across many tables due to limitations
 * in the underlying data store (e.g., MySql has a 65536-byte row-size limit).
 * These phantom sub-tables are represented as form elements with null element
 * names.  Structured types, such as geopoints, are stored in phantom columns.
 * The geopoint field has null column and table name.  It has form elements 
 * enclosed within it that have non-null column names, and a type indicating
 * the portion of the structured field contained in that column
 * (e.g., lat, long, alt, acc). 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public final class FormDataModel extends InstanceDataBase {

	/* xform element types */
	public static enum ElementType {
		// top level tag -- holds the form name
		FORM_NAME,
		// xform tag types
		STRING,
		JRDATETIME,
		JRDATE,
		JRTIME,
		INTEGER,
		DECIMAL,
		GEOPOINT,
		BINARY,  // identifies BinaryContent table
		BOOLEAN,
		SELECT1, // identifies SelectChoice table
		SELECTN, // identifies SelectChoice table
		REPEAT,
		GROUP,
		// additional supporting tables
		PHANTOM, // if a relation needs to be divided in order to fit
		VERSIONED_BINARY, // association between BINARY and VERSIONED_BINARY_CONTENT_REF_BLOB
		VERSIONED_BINARY_CONTENT_REF_BLOB, // association between VERSIONED_BINARY and REF_BLOB
		REF_BLOB, // the table of the actual byte[] data (xxxBLOB)
		LONG_STRING_REF_TEXT, // association between any field and REF_TEXT
		REF_TEXT, // the table of extended string values (xxxTEXT)
	};
	
	// GEOPOINT structured field has the following ordinal interpretations...
	public static final int GEOPOINT_LATITUDE_ORDINAL_NUMBER = 1;
	public static final int GEOPOINT_LONGITUDE_ORDINAL_NUMBER = 2;
	public static final int GEOPOINT_ALTITUDE_ORDINAL_NUMBER = 3;
	public static final int GEOPOINT_ACCURACY_ORDINAL_NUMBER = 4;
	
	private static final String TABLE_NAME = "_form_data_model";
	private static final String FORM_DATA_MODEL_REF_TEXT = "_form_data_model_ref_text";
	private static final String FORM_DATA_MODEL_LONG_STRING_REF_TEXT = "_form_data_model_long_string_ref_text";

	private static final DataField URI_FORM_ID = new DataField("URI_FORM_ID", DataField.DataType.URI, false, PersistConsts.MAX_SIMPLE_STRING_LEN);
	private static final DataField ELEMENT_TYPE = new DataField("ELEMENT_TYPE", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN);
	private static final DataField ELEMENT_NAME = new DataField("ELEMENT_NAME", DataField.DataType.STRING, true, PersistConsts.MAX_SIMPLE_STRING_LEN);
	private static final DataField PERSIST_AS_COLUMN_NAME = new DataField("PERSIST_AS_COLUMN_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
	private static final DataField PERSIST_AS_TABLE_NAME = new DataField("PERSIST_AS_TABLE_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
	private static final DataField PERSIST_AS_SCHEMA_NAME = new DataField("PERSIST_AS_SCHEMA_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);

	// special values for bootstrapping
	public static final String URI_FORM_ID_VALUE_FORM_DATA_MODEL = "aggregate.opendatakit.org:FormElement"; 

	public final DataField uriFormId;
	public final DataField elementType;
	public final DataField elementName;
	public final DataField persistAsColumn;
	public final DataField persistAsTable;
	public final DataField persistAsSchema;
	
	// linked up value...
	private WeakReference<FormDataModel> parent = null;
	private final List<FormDataModel> children = new ArrayList<FormDataModel>();
	private CommonFieldsBase backingObject = null;
	private DataField backingKey = null;
	private boolean mayHaveExtendedStringData = false;
	
	/**
	 * Constructor used by e.g., FormDefinition when initially starting up.
	 * 
	 * Note that the backing relation is not created by this constructor.
	 * The Datastore.createRelation(formDataModel, User) method should
	 * be called to complete the initialization of the object and 
	 * create the backing relation in the database.
	 * 
	 * @param schemaName
	 */
	FormDataModel(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(uriFormId = new DataField(URI_FORM_ID));
		fieldList.add(elementType = new DataField(ELEMENT_TYPE));
		fieldList.add(elementName = new DataField(ELEMENT_NAME));
		fieldList.add(persistAsColumn = new DataField(PERSIST_AS_COLUMN_NAME));
		fieldList.add(persistAsTable = new DataField(PERSIST_AS_TABLE_NAME));
		fieldList.add(persistAsSchema = new DataField(PERSIST_AS_SCHEMA_NAME));
	}

	/**
	 * Copy constructor for use by {@link #getEmptyRow(Class)}   
	 * This does not populate any fields related to the values of this row. 
	 *
	 * @param d
	 */
	public FormDataModel(FormDataModel d) {
		super(d);

		uriFormId = d.uriFormId;
		elementType = d.elementType;
		elementName = d.elementName;
		persistAsColumn = d.persistAsColumn;
		persistAsTable = d.persistAsTable;
		persistAsSchema = d.persistAsSchema;
	}

	public final String getUriFormId() {
		return getStringField(uriFormId);
	}

	public final ElementType getElementType() {
		String type = getStringField(elementType);
		ElementType et = null;
		try {
			et = ElementType.valueOf(type);
		} catch ( Exception e ) {
			Logger.getLogger(FormDataModel.class.getName()).severe("Unrecognized element type: " + type);
			e.printStackTrace();
		}
		return et;
	}
	
	public final String getElementName() {
		return getStringField(elementName);
	}
	
	/**
	 * Constructs the colon-separate qualified name for an element.  This is the
	 * element name prefixed with the enclosing group(s) names up until the first
	 * enclosing repeat group or the top-level group.  The enclosing repeat group
	 * or top-level group name is not part of the constructed qualified name.
	 * <p>
	 * For many uses, the SubmissionKey is likely more appropriate.
	 *  
	 * @return the colon-separated qualified name for this element.
	 */
	public final String getGroupQualifiedElementName() {
		switch ( getElementType() ) {
		// xform tag types
		case STRING:
		case JRDATETIME:
		case JRDATE:
		case JRTIME:
		case INTEGER:
		case DECIMAL:
		case GEOPOINT:
		case BINARY:  // identifies BinaryContent table
		case BOOLEAN:
		case SELECT1: // identifies SelectChoice table
		case SELECTN: // identifies SelectChoice table
			return getParent().getGroupQualifiedElementName() + ":" + getElementName();
		case PHANTOM: // if a relation needs to be divided in order to fit
			return getParent().getGroupQualifiedElementName();
		case REPEAT:
			return "";
		case GROUP:
			if ( getParent().getElementType() == ElementType.FORM_NAME ) {
				return "";
			} else {
				return getParent().getGroupQualifiedElementName() + ":" + getElementName();
			}
		case VERSIONED_BINARY: // association between BINARY and VERSIONED_BINARY_CONTENT_REF_BLOB
			// this shares the element name of the binary content record, so leave it...
			return getParent().getGroupQualifiedElementName();
			// top level tag -- holds the form name
		case FORM_NAME:
		case VERSIONED_BINARY_CONTENT_REF_BLOB: // association between VERSIONED_BINARY and REF_BLOB
		case REF_BLOB: // the table of the actual byte[] data (xxxBLOB)
		case LONG_STRING_REF_TEXT: // association between any field and REF_TEXT
		case REF_TEXT: // the table of extended string values (xxxTEXT)
		default:
			throw new IllegalStateException("unexpected request for unreferencable element type");
		}
	}

	public final String getPersistAsColumn() {
		return getStringField(persistAsColumn);
	}

	public final String getPersistAsTable() {
		return getStringField(persistAsTable);
	}
	
	public final String getPersistAsSchema() {
		return getStringField(persistAsSchema);
	}
	
	public String getPersistAsQualifiedTableName() {
		String table = getPersistAsTable();
		if ( table == null ) return null;
		return getPersistAsSchema() + "." + table;
	}

	public final FormDataModel findElementByName(String elementName) {
		if ( elementName == null ) {
			throw new IllegalArgumentException("null elementName passed in!");
		}
		
		for ( FormDataModel m : children ) {
			if ( m.getElementName() == null ) {
				// phantom...
				FormDataModel t = m.findElementByName(elementName);
				if ( t != null ) return t;
			} else if ( m.getElementName().equals(elementName) ) {
				return m;
			}
		}
		return null;
	}

	public final void setParent(FormDataModel p) {
		parent = new WeakReference<FormDataModel>(p);
	}
	
	public final FormDataModel getParent() {
		if ( parent == null ) return null;
		return parent.get();
	}
	
	public final void setChild(Long ordinal, FormDataModel child) {
		// ordinal is in range 1..n so convert it to 0..n-1
		// rounding error is a non-issue.
		int i = (int) (ordinal - 1L);
		// grow array to at least (i+1) in size (so [i] is valid index)
		while ( children.size() <= i ) {
			children.add(null);
		}
		// test that we aren't overwriting the ordinal
		FormDataModel c = children.get(i);
		if ( c != null ) {
			throw new IllegalStateException("Form id " + getUri() + " Child already defined for ordinal " + ordinal.toString());
		}
		// save child...
		children.set(i, child);
	}
	
	public final void validateChildren() {
		int i = 1;
		for ( FormDataModel m : children ) {
			if ( m == null ) {
				throw new IllegalStateException("missing ordinal position " + Integer.toString(i));
			}
			++i;
		}
	}
	
	public final List<FormDataModel> getChildren() {
		return children;
	}
	
	public final CommonFieldsBase getBackingObjectPrototype() {
		return backingObject;
	}

	public final void setBackingObject(CommonFieldsBase backingObject) {
		this.backingObject = backingObject;
	}

	public final DataField getBackingKey() {
		return backingKey;
	}

	public final void setBackingKey(DataField backingKey) {
		this.backingKey = backingKey;
	}
	public boolean isMayHaveExtendedStringData() {
		return mayHaveExtendedStringData;
	}
	public void setMayHaveExtendedStringData(boolean mayHaveExtendedStringData) {
		this.mayHaveExtendedStringData = mayHaveExtendedStringData;
	}
	
	/**
	 * Determine whether the given field is possibly a long string field.
	 * String values are assumed to be stored as individual characters
	 * and are expected to fit up to f.getMaxCharLen() characters regardless
	 * of the UTF-8 multi-byte content of the string.  So a long string is
	 * identified as a string field that contains a value that is exactly 
	 * the length of the storage area.
	 *  
	 * @param entity
	 * @param f
	 * @return
	 */
	public final boolean isPossibleLongStringField(CommonFieldsBase entity, DataField f) {
		String value = entity.getStringField(f);
		if ( value != null && isMayHaveExtendedStringData() ) {
			int outcome = f.getMaxCharLen().compareTo(Long.valueOf(value.length()));
			if ( outcome == 0 ) {
				// this may be extended...
				return true;
			} else if ( outcome < 0 ) {
				throw new IllegalStateException("Unexpected -- stored value is longer than max char len! " +
						getPersistAsSchema() + " " + getPersistAsTable() + " " + f.getName());
			} else {
				return false;
			}
		}
		return false;
	}

	public static final List<FormDataModel> constructFormDataModel(Datastore ds, FormDataModel fdm, String schemaName, User user) {

		List<FormDataModel> idDefn = new ArrayList<FormDataModel>();
		
		FormDataModel d;
		
		// data record...
		d = ds.createEntityUsingRelation(fdm, null, user);
		idDefn.add(d);
		final String topLevelURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, null);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "FormDataModel");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.FORM_NAME.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, null);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		final EntityKey k = new EntityKey( d, d.getUri());

		// data record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String groupURI = d.getUri();
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, topLevelURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "data");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.GROUP.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		
		// uriFormId record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "uriFormId");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.uriFormId.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		// elementName record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 2L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "elementName");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.elementName.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		// elementType record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 3L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "elementType");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.elementType.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		// persistAsColumn record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 4L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "persistAsColumn");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.persistAsColumn.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		// persistAsTable record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 5L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "persistAsTable");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.persistAsSchema.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		// persistAsSchema record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 6L);
		d.setStringField(fdm.parentAuri, groupURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, "persistAsSchema");
		d.setStringField(fdm.elementType, FormDataModel.ElementType.STRING.toString());
		d.setStringField(fdm.persistAsColumn, fdm.persistAsSchema.getName());
		d.setStringField(fdm.persistAsTable, fdm.getTableName());
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for long string ref text...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		final String lst = d.getUri();
		d.setLongField(fdm.ordinalNumber, 2L);
		d.setStringField(fdm.parentAuri, topLevelURI);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.LONG_STRING_REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_DATA_MODEL_LONG_STRING_REF_TEXT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// record for ref text...
		d = ds.createEntityUsingRelation(fdm, k, user);
		idDefn.add(d);
		d.setLongField(fdm.ordinalNumber, 1L);
		d.setStringField(fdm.parentAuri, lst);
		d.setStringField(fdm.uriFormId, URI_FORM_ID_VALUE_FORM_DATA_MODEL);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, FORM_DATA_MODEL_REF_TEXT);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		return idDefn;
	}
	
	public void print(PrintStream out) {
		String ppk = getParentAuri();
		if ( ppk == null ) {
			ppk = "";
		}
		out.format("FDM(%d,%s)  uriFormId %s\n",
				getOrdinalNumber().intValue(), ppk, getUriFormId());
		String tpk = getTopLevelAuri();
		if ( tpk == null ) {
			tpk = "";
		}
		out.format("            PK=%s  topLevelAuri=%s\n",
				getUri(), tpk);
		String en = getElementName();
		if ( en == null ) {
			en = "";
		}
		out.format("  elementName %s\n", en ); 
		out.format("  elementType %s\n", getElementType().toString() );
		if ( getPersistAsColumn() != null ) {
			out.format("                persistAsColumn %s\n", getPersistAsColumn() ); 
			out.format("                persistAsTable %s\n", getPersistAsTable() ); 
			out.format("                persistAsScheme %s\n", getPersistAsSchema() ); 
		} else if ( getPersistAsTable() != null ) {
			out.format("                persistAsTable %s\n", getPersistAsTable() ); 
			out.format("                persistAsScheme %s\n", getPersistAsSchema() ); 
			
		} else {
			out.format("                persistAsScheme %s\n", getPersistAsSchema() ); 
		}
	}
}
