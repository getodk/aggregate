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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * This entity defines the mapping between a submission element and a backing
 * object and data field. It is xform-specific. When processed by the
 * FormDefinition class, it will have weak pointers to itself within the
 * DataField object of the backing keys.
 * <p>
 * The element tags of a form and phantom sub-tables for large forms are
 * maintained in the FormDataModel table. All tags are recorded, even group tags
 * that are not repeat groups. Thus, once the javarosa xform definition has been
 * parsed, it is no longer needed for any subsequent processing.
 * <p>
 * Each form element records:
 * <ol>
 * <li>the uri of the submission data model (submission form id, version, and
 * uiVersion) to which it belongs (URI_SUBMISSION_DATA_MODEL)</li>
 * <li>the enclosing form element, if any (PARENT_URI_FORM_DATA_MODEL)</li>
 * <li>the one-based position of this element within the enclosing form element
 * (ORDINAL_NUMBER)</li>
 * <li>the type of this element (ELEMENT_TYPE)</li>
 * <li>the name of the element (ELEMENT_NAME - null if this is a phantom
 * sub-table)</li>
 * <li>the column name in which it is stored (PERSIST_AS_COLUMN_NAME - null if
 * it is not a column)</li>
 * <li>the table name in which it is stored (PERSIST_AS_TABLE_NAME)</li>
 * <li>the schema name in which it is stored (PERSIST_AS_SCHEMA_NAME)</li>
 * </ol>
 * If this is a data element, the (column, table, schema) are all non-null.
 * Otherwise, for a repeat, group, geopoint, phantom, multiple-choice or binary
 * element, the column element will be null, but the (table, schema) will be
 * non-null.
 * <p>
 * Repeat groups are their own tables. If a dataset has many columns, it will be
 * split across many tables due to limitations in the underlying data store
 * (e.g., MySql has a 65536-byte row-size limit). These phantom sub-tables are
 * represented as form elements with null element names and null columns.
 * Structured types, such as geopoints, are represented as a record to mark the
 * structure field (with a null column name) plus one data element underneath
 * that marker for each value in the structured type (e.g., lat, long, alt,
 * acc).
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class FormDataModel extends CommonFieldsBase {

  private static final Log logger = LogFactory.getLog(FormDataModel.class.getName());

  public static final Long MAX_ELEMENT_NAME_LENGTH = PersistConsts.GUARANTEED_SEARCHABLE_LEN;

  /* xform element types */
  public static enum ElementType {
    // xform tag types
    STRING, JRDATETIME, JRDATE, JRTIME, INTEGER, DECIMAL, GEOPOINT, GEOTRACE, GEOSHAPE,
     
    BINARY, // identifies BinaryContent table
    BOOLEAN, SELECT1, // identifies SelectChoice table
    SELECTN, // identifies SelectChoice table
    REPEAT, GROUP,
    // additional supporting tables
    PHANTOM, // if a relation needs to be divided in order to fit
    BINARY_CONTENT_REF_BLOB, // association between BINARY and REF_BLOB
    REF_BLOB, // the table of the actual byte[] data (xxxBLOB)
  };

  private static final String TABLE_NAME = "_form_data_model";

  public static final DataField URI_SUBMISSION_DATA_MODEL = new DataField(
      "URI_SUBMISSION_DATA_MODEL", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN)
      .setIndexable(IndexType.HASH);
  private static final DataField PARENT_URI_FORM_DATA_MODEL = new DataField(
      "PARENT_URI_FORM_DATA_MODEL", DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN);
  /** ordinal (1st, 2nd, ... ) of this item in the form element */
  private static final DataField ORDINAL_NUMBER = new DataField("ORDINAL_NUMBER",
      DataField.DataType.INTEGER, false);
  private static final DataField ELEMENT_TYPE = new DataField("ELEMENT_TYPE",
      DataField.DataType.STRING, false, PersistConsts.URI_STRING_LEN);
  private static final DataField ELEMENT_NAME = new DataField("ELEMENT_NAME",
      DataField.DataType.STRING, true, MAX_ELEMENT_NAME_LENGTH);
  private static final DataField PERSIST_AS_COLUMN_NAME = new DataField("PERSIST_AS_COLUMN_NAME",
      DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
  private static final DataField PERSIST_AS_TABLE_NAME = new DataField("PERSIST_AS_TABLE_NAME",
      DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);
  private static final DataField PERSIST_AS_SCHEMA_NAME = new DataField("PERSIST_AS_SCHEMA_NAME",
      DataField.DataType.STRING, true, PersistConsts.URI_STRING_LEN);

  /**
   * Predicate function for determining whether a given field is one that is
   * expected to be stored within a data table, vs. in a special auxiliary
   * table. Returns true if it is stored in a data table.
   * 
   * This predicate function is used to determine if a parent FormDataModel is
   * divided across multiple data tables.
   * 
   * @param t
   * @return
   */
  public static boolean isFieldStoredWithinDataTable(ElementType t) {
    switch (t) {
    case STRING:
    case JRDATETIME:
    case JRDATE:
    case JRTIME:
    case INTEGER:
    case DECIMAL:
    case GEOPOINT:
    case GEOTRACE:
    case GEOSHAPE:
    case BOOLEAN:
    case SELECT1: // identifies SelectChoice table
    case GROUP:
    case PHANTOM: // if a relation needs to be divided in order to fit
      return true;
    default:
      return false;
    }
  }

  /**
   * Class wrapping the persisted object name. Used when dealing with backing
   * object maps.
   * 
   * @author mitchellsundt@gmail.com
   * 
   */
  public final class DDRelationName {

    private DDRelationName() {
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof DDRelationName))
        return false;
      DDRelationName ref = (DDRelationName) obj;
      return toString().equals(ref.toString());
    }

    @Override
    public int hashCode() {
      return FormDataModel.this.getPersistAsTable().hashCode() + 103
          * FormDataModel.this.getPersistAsSchema().hashCode();
    }

    @Override
    public String toString() {
      return FormDataModel.this.getPersistAsSchema() + "." + FormDataModel.this.getPersistAsTable();
    }
  };

  // linked up value...
  private WeakReference<FormDataModel> parent = null;
  private final List<FormDataModel> children = new ArrayList<FormDataModel>();
  private CommonFieldsBase backingObject = null;
  private DataField backingKey = null;

  /**
   * Reset the linked up values so FormDefinition can construct a new model.
   * 
   * Called by the FormParserForJavaRosa to reset the FDM prior to trying once
   * again to create the relations it describes.
   */
  public void resetDerivedFields() {
    parent = null;
    children.clear();
    backingObject = null;
    backingKey = null;
  }

  /**
   * Constructor to create the relation prototype. Only called via
   * {@link #assertRelation(CallingContext)}
   * 
   * Note that the backing relation is not created by this constructor.
   * 
   * @param schemaName
   */
  FormDataModel(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(URI_SUBMISSION_DATA_MODEL);
    fieldList.add(PARENT_URI_FORM_DATA_MODEL);
    fieldList.add(ORDINAL_NUMBER);
    fieldList.add(ELEMENT_TYPE);
    fieldList.add(ELEMENT_NAME);
    fieldList.add(PERSIST_AS_COLUMN_NAME);
    fieldList.add(PERSIST_AS_TABLE_NAME);
    fieldList.add(PERSIST_AS_SCHEMA_NAME);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * Note that the backing relation is not created by this constructor.
   * 
   * @param ref
   * @param user
   */
  private FormDataModel(FormDataModel ref, User user) {
    super(ref, user);
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
    return getStringField(URI_SUBMISSION_DATA_MODEL);
  }

  public final void setUriSubmissionDataModel(String value) {
    if (!setStringField(URI_SUBMISSION_DATA_MODEL, value)) {
      String str = "overflow on uriSubmissionDataModel";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public final String getParentUriFormDataModel() {
    return getStringField(PARENT_URI_FORM_DATA_MODEL);
  }

  public final void setParentUriFormDataModel(String value) {
    if (!setStringField(PARENT_URI_FORM_DATA_MODEL, value)) {
      String str = "overflow on parentUriFormDataModel";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public final Long getOrdinalNumber() {
    return getLongField(ORDINAL_NUMBER);
  }

  public final void setOrdinalNumber(Long value) {
    setLongField(ORDINAL_NUMBER, value);
  }

  public final ElementType getElementType() {
    String type = getStringField(ELEMENT_TYPE);
    ElementType et = null;
    try {
      et = ElementType.valueOf(type);
    } catch (Exception e) {
      logger.error("Unrecognized element type: " + type);
      print(System.err);
      e.printStackTrace();
    }
    return et;
  }

  public final void setElementType(ElementType type) {
    if (!setStringField(ELEMENT_TYPE, type.toString())) {
      String str = "overflow on elementType";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public final String getElementName() {
    return getStringField(ELEMENT_NAME);
  }

  public final void setElementName(String name) {
    if (!setStringField(ELEMENT_NAME, name)) {
      String str = "overflow on elementName";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  /**
   * Constructs the colon-separate qualified name for an element. This is the
   * element name prefixed with the enclosing group(s) names up until the first
   * enclosing repeat group or the top-level group. The enclosing repeat group
   * or top-level group name is not part of the constructed qualified name.
   * <p>
   * For many uses, the SubmissionKey is likely more appropriate.
   * 
   * @return the colon-separated qualified name for this element.
   */
  public final String getGroupQualifiedElementName() {
    return getGroupQualifiedElementNameCommon(false);
  }

  public final String getGroupQualifiedXpathElementName() {
    return getGroupQualifiedElementNameCommon(true);
  }

  private final String getGroupQualifiedElementNameCommon(boolean xpath) {
    String groupPrefix;
    // find our "real" parent (one that is not a phantom)
    FormDataModel pReal = getParent();
    while (pReal != null && pReal.getElementType() == ElementType.PHANTOM) {
      pReal = pReal.getParent();
    }
    if (pReal == null) {
      // Should not happen except when this is the top-level group
      // Recursion should end with pReal.getParent() == null (below)
      groupPrefix = "";
    } else if (pReal.getElementType() == ElementType.REPEAT) {
      groupPrefix = "";
    } else if (pReal.getParent() == null) {
      if (xpath) {
        groupPrefix = BasicConsts.FORWARDSLASH +
            pReal.getGroupQualifiedElementNameCommon(xpath) + BasicConsts.FORWARDSLASH;
      } else {
        groupPrefix = "";
      }
    } else {
      if (xpath) {
        groupPrefix = pReal.getGroupQualifiedElementNameCommon(xpath) + BasicConsts.FORWARDSLASH;
      } else {
        groupPrefix = pReal.getGroupQualifiedElementNameCommon(xpath) + BasicConsts.COLON;
      }
    }

    switch (getElementType()) {
    // xform tag types
    case STRING:
    case JRDATETIME:
    case JRDATE:
    case JRTIME:
    case INTEGER:
    case DECIMAL:
    case GEOPOINT:
    case GEOTRACE:
    case GEOSHAPE:
    case BINARY: // identifies BinaryContent table
    case BOOLEAN:
    case SELECT1: // identifies SelectChoice table
    case SELECTN: // identifies SelectChoice table
    case REPEAT:
    case GROUP:
      return groupPrefix + getElementName();
    case PHANTOM: // if a relation needs to be divided in order to fit
      return getParent().getGroupQualifiedElementNameCommon(xpath);
    case BINARY_CONTENT_REF_BLOB: // association between VERSIONED_BINARY and
                                  // REF_BLOB
    case REF_BLOB: // the table of the actual byte[] data (xxxBLOB)
    default:
      throw new IllegalStateException("unexpected request for unreferencable element type");
    }
  }

  public final String getPersistAsColumn() {
    return getStringField(PERSIST_AS_COLUMN_NAME);
  }

  public final void setPersistAsColumn(String value) {
    if (!setStringField(PERSIST_AS_COLUMN_NAME, value)) {
      String str = "overflow on persistAsColumn";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public final String getPersistAsTable() {
    return getStringField(PERSIST_AS_TABLE_NAME);
  }

  public final void setPersistAsTable(String value) {
    if (!setStringField(PERSIST_AS_TABLE_NAME, value)) {
      String str = "overflow on persistAsTable";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public final String getPersistAsSchema() {
    return getStringField(PERSIST_AS_SCHEMA_NAME);
  }

  public final void setPersistAsSchema(String value) {
    if (!setStringField(PERSIST_AS_SCHEMA_NAME, value)) {
      String str = "overflow on persistAsSchema";
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
  }

  public String getPersistAsQualifiedTableName() {
    String table = getPersistAsTable();
    if (table == null)
      return null;
    return getPersistAsSchema() + "." + table;
  }

  public final FormDataModel findElementByName(String elementName) {
    if (elementName == null) {
      throw new IllegalArgumentException("null elementName passed in!");
    }

    for (FormDataModel m : children) {
      if (m.getElementName() == null) {
        // phantom...
        FormDataModel t = m.findElementByName(elementName);
        if (t != null)
          return t;
      } else if (m.getElementName().equals(elementName)) {
        return m;
      }
    }
    return null;
  }

  public final void setParent(FormDataModel p) {
    parent = new WeakReference<FormDataModel>(p);
  }

  public final FormDataModel getParent() {
    if (parent == null)
      return null;
    return parent.get();
  }

  public final void setChild(Long ordinal, FormDataModel child) {
    // ordinal is in range 1..n so convert it to 0..n-1
    // rounding error is a non-issue.
    int i = (int) (ordinal - 1L);
    // grow array to at least (i+1) in size (so [i] is valid index)
    while (children.size() <= i) {
      children.add(null);
    }
    // test that we aren't overwriting the ordinal
    FormDataModel c = children.get(i);
    if (c != null) {
      String str = "Form id " + getUri() + " Child already defined for ordinal "
          + ordinal.toString();
      logger.error(str);
      print(System.err);
      throw new IllegalStateException(str);
    }
    // save child...
    children.set(i, child);
  }

  public final void validateChildren() {
    int i = 1;
    for (FormDataModel m : children) {
      if (m == null) {
        String str = "missing ordinal position " + Integer.toString(i);
        logger.error(str);
        print(System.err);
        throw new IllegalStateException(str);
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

  private static FormDataModel relation = null;

  public static synchronized final FormDataModel assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
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
    if (ppk == null) {
      ppk = "";
    }
    out.format("FDM(%d,%s)  fdmSubmissionUri %s\n", getOrdinalNumber().intValue(), ppk,
        getUriSubmissionDataModel());
    out.format("            PK=%s\n", getUri());
    String en = getElementName();
    if (en == null) {
      en = "";
    }
    out.format("  elementName %s\n", en);
    out.format("  elementType %s\n", getElementType().toString());
    if (getPersistAsColumn() != null) {
      out.format("                persistAsColumn %s\n", getPersistAsColumn());
      out.format("                persistAsTable %s\n", getPersistAsTable());
      out.format("                persistAsScheme %s\n", getPersistAsSchema());
    } else if (getPersistAsTable() != null) {
      out.format("                persistAsTable %s\n", getPersistAsTable());
      out.format("                persistAsScheme %s\n", getPersistAsSchema());

    } else {
      out.format("                persistAsScheme %s\n", getPersistAsSchema());
    }
  }
}
