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

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * This entity defines the mapping between a submission element and a 
 * backing object and data field.  It is xform-specific.  When processed
 * by the FormDefinition class, it will have weak pointers to itself within
 * the DataField object of the backing keys.
 * <p>
 * The element tags of a form and phantom sub-tables for large forms
 * are maintained in the FormDataModel table.  All tags are recorded,
 * even group tags that are not repeat groups.  Thus, once the 
 * javarosa xform definition has been parsed, it is no longer needed
 * for any subsequent processing.
 * <p>
 * Each form element records:
 * <ol><li>the uri of the submission data model (submission form id,
 *  version, and uiVersion) to which it belongs (URI_SUBMISSION_DATA_MODEL)</li>
 * <li>the enclosing form element, if any (PARENT_URI_FORM_DATA_MODEL)</li>
 * <li>the one-based position of this element within the enclosing form element (ORDINAL_NUMBER)</li>
 * <li>the type of this element (ELEMENT_TYPE)</li>
 * <li>the name of the element (ELEMENT_NAME - null if this is a phantom sub-table)</li>
 * <li>the column name in which it is stored (PERSIST_AS_COLUMN_NAME - null if it is not a column)</li>
 * <li>the table name in which it is stored (PERSIST_AS_TABLE_NAME)</li>
 * <li>the schema name in which it is stored (PERSIST_AS_SCHEMA_NAME)</li>
 * </ol>
 * If this is a data element, the (column, table, schema) are all non-null.
 * Otherwise, for a repeat, group, geopoint, phantom, multiple-choice or binary element,
 * the column element will be null, but the (table, schema) will be non-null. 
 * <p>
 * Repeat groups are their own tables.  If a dataset
 * has many columns, it will be split across many tables due to limitations
 * in the underlying data store (e.g., MySql has a 65536-byte row-size limit).
 * These phantom sub-tables are represented as form elements with null element
 * names and null columns.  Structured types, such as geopoints, are represented
 * as a record to mark the structure field (with a null column name) plus one 
 * data element underneath that marker for each value in the structured type
 * (e.g., lat, long, alt, acc).   
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class FormDataModel extends CommonFieldsBase {

	/* xform element types */
	public static enum ElementType {
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

	private static final DataField URI_SUBMISSION_DATA_MODEL = new DataField("URI_SUBMISSION_DATA_MODEL", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
	private static final DataField PARENT_URI_FORM_DATA_MODEL = new DataField("PARENT_URI_FORM_DATA_MODEL", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN);
	/** ordinal (1st, 2nd, ... ) of this item in the form element */
	private static final DataField ORDINAL_NUMBER = new DataField("ORDINAL_NUMBER", DataField.DataType.INTEGER, false);
	private static final DataField ELEMENT_TYPE = new DataField("ELEMENT_TYPE", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN);
	private static final DataField ELEMENT_NAME = new DataField("ELEMENT_NAME", DataField.DataType.STRING, true, PersistConsts.MAX_SIMPLE_STRING_LEN);
	private static final DataField PERSIST_AS_COLUMN_NAME = new DataField("PERSIST_AS_COLUMN_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
	private static final DataField PERSIST_AS_TABLE_NAME = new DataField("PERSIST_AS_TABLE_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
	private static final DataField PERSIST_AS_SCHEMA_NAME = new DataField("PERSIST_AS_SCHEMA_NAME", DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);

	/**
	 * Class wrapping the persisted object name.
	 * Used when dealing with backing object maps.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	public final class DDRelationName {

		private DDRelationName() {
		}
		
		@Override
		public boolean equals(Object obj) {
			if ( !(obj instanceof DDRelationName) ) return false;
			DDRelationName ref = (DDRelationName) obj;
			return toString().equals(ref.toString());
		}

		@Override
		public int hashCode() {
			return FormDataModel.this.getPersistAsTable().hashCode() +
					103*FormDataModel.this.getPersistAsSchema().hashCode();
		}

		@Override
		public String toString() {
			return FormDataModel.this.getPersistAsSchema() + "." +
					FormDataModel.this.getPersistAsTable();
		}
	};
	
	public final DataField uriSubmissionDataModel;
	public final DataField parentUriFormDataModel;
	public final DataField ordinalNumber;
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
	 * Reset the linked up values so FormDefinition can construct a new model.
	 * 
	 * Called by the FormParserForJavaRosa to reset the FDM prior to 
	 * trying once again to create the relations it describes.
	 */
	public void resetDerivedFields() {
		parent = null;
		children.clear();
		backingObject = null;
		backingKey = null;
		mayHaveExtendedStringData = false;
	}
	
	/**
	 * Constructor to create the relation prototype.
	 * 
	 * Note that the backing relation is not created by this constructor.
	 * See the {@link #createRelation(Datastore, User)} method.
	 * 
	 * @param schemaName
	 */
	FormDataModel(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(uriSubmissionDataModel = new DataField(URI_SUBMISSION_DATA_MODEL));
		fieldList.add(parentUriFormDataModel = new DataField(PARENT_URI_FORM_DATA_MODEL));
		fieldList.add(ordinalNumber = new DataField(ORDINAL_NUMBER));
		fieldList.add(elementType = new DataField(ELEMENT_TYPE));
		fieldList.add(elementName = new DataField(ELEMENT_NAME));
		fieldList.add(persistAsColumn = new DataField(PERSIST_AS_COLUMN_NAME));
		fieldList.add(persistAsTable = new DataField(PERSIST_AS_TABLE_NAME));
		fieldList.add(persistAsSchema = new DataField(PERSIST_AS_SCHEMA_NAME));
	}

	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	private FormDataModel(FormDataModel ref, User user) {
		super(ref, user);

		uriSubmissionDataModel = ref.uriSubmissionDataModel;
		parentUriFormDataModel = ref.parentUriFormDataModel;
		ordinalNumber = ref.ordinalNumber;
		elementType = ref.elementType;
		elementName = ref.elementName;
		persistAsColumn = ref.persistAsColumn;
		persistAsTable = ref.persistAsTable;
		persistAsSchema = ref.persistAsSchema;
	}

	// Only called from within the persistence layer.
	@Override
	public FormDataModel getEmptyRow(User user) {
		return new FormDataModel(this, user);
	}
	
	public final DDRelationName getDDRelationName() {
		return new DDRelationName();
	}
	
	public final String getUriSubmissionDataModel() {
		return getStringField(uriSubmissionDataModel);
	}
	
	public final void setUriSubmissionDataModel(String value) {
		if ( ! setStringField(uriSubmissionDataModel, value) ) {
			throw new IllegalStateException("overflow on uriSubmissionDataModel");
		}
	}
	
	public final String getParentUriFormDataModel() {
		return getStringField(parentUriFormDataModel);
	}
	
	public final void setParentUriFormDataModel(String value) {
		if ( ! setStringField(parentUriFormDataModel, value) ) {
			throw new IllegalStateException("overflow on parentUriFormDataModel");
		}
	}

	public final Long getOrdinalNumber() {
		return getLongField(ordinalNumber);
	}

	public final void setOrdinalNumber(Long value) {
		setLongField(ordinalNumber, value);
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
		String groupPrefix;
		// find our "real" parent (one that is not a phantom)
		FormDataModel pReal = getParent();
		while ( pReal != null && pReal.getElementType() == ElementType.PHANTOM ) {
			pReal = pReal.getParent();
		}
		if ( pReal == null ) {
			groupPrefix = "";
		} else if ( pReal.getElementType() == ElementType.REPEAT ) {
			groupPrefix = "";
		} else if ( pReal.getParent() == null ) {
			groupPrefix = "";
		} else {
			groupPrefix = pReal.getGroupQualifiedElementName() + ":";
		}
		
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
		case REPEAT:
		case GROUP:
		case VERSIONED_BINARY: // association between BINARY and VERSIONED_BINARY_CONTENT_REF_BLOB
			// this shares the element name of the binary content record, so leave it...
			return groupPrefix + getElementName();
		case PHANTOM: // if a relation needs to be divided in order to fit
			return getParent().getGroupQualifiedElementName();
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

	private static FormDataModel relation = null;
	
	public static synchronized final FormDataModel createRelation(CallingContext cc) throws ODKDatastoreException {
		if ( relation == null ) {
			FormDataModel relationPrototype;
			Datastore ds = cc.getDatastore();
			User user = cc.getUserService().getDaemonAccountUser();
			relationPrototype = new FormDataModel(ds.getDefaultSchemaName());
			ds.assertRelation(relationPrototype, user); // may throw exception...
		    // at this point, the prototype has become fully populated
			relation = relationPrototype; // set static variable only upon success...
		}
		return relation;
	}
	
	public void print(PrintStream out) {
		String ppk = getParentUriFormDataModel();
		if ( ppk == null ) {
			ppk = "";
		}
		out.format("FDM(%d,%s)  fdmSubmissionUri %s\n",
				getOrdinalNumber().intValue(), ppk, getUriSubmissionDataModel());
		String tpk = getUriSubmissionDataModel();
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
