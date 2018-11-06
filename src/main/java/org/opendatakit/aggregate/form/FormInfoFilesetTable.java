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
package org.opendatakit.aggregate.form;

import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class FormInfoFilesetTable extends DynamicBase {
  public static final DataField ROOT_ELEMENT_MODEL_VERSION = new DataField("ROOT_ELEMENT_MODEL_VERSION",
      DataField.DataType.INTEGER, true);

  // Additional DataField -- the xform fileset (single-value binary content)
  public static final DataField IS_ENCRYPTED_FORM = new DataField("IS_ENCRYPTED_FORM",
      DataField.DataType.BOOLEAN, true);
  public static final DataField IS_DOWNLOAD_ALLOWED = new DataField("IS_DOWNLOAD_ALLOWED",
      DataField.DataType.BOOLEAN, true);
  public static final DataField LANGUAGE_CODE = new DataField("LANGUAGE_CODE",
      DataField.DataType.STRING, true, 8L);

  // Additional DataField -- the manifest fileset (multivalued binary content)
  public static final DataField FORM_NAME = new DataField("FORM_NAME",
      DataField.DataType.STRING, true, PersistConsts.GUARANTEED_SEARCHABLE_LEN);
  public static final DataField DESCRIPTION = new DataField("DESCRIPTION",
      DataField.DataType.STRING, true, 8192L);
  public static final DataField DESCRIPTION_URL = new DataField("DESCRIPTION_URL",
      DataField.DataType.STRING, true, 2048L);

  // DataFields in the fileset table
  public static final String URI_FORM_ID_VALUE_FORM_INFO_FILESET = "aggregate.opendatakit.org:FormInfoFileset";
  static final String TABLE_NAME = "_form_info_fileset";
  private static final String FORM_INFO_XFORM_REF_BLOB = "_form_info_xform_blb";
  private static final String FORM_INFO_XFORM_BINARY_CONTENT_REF_BLOB = "_form_info_xform_ref";
  private static final String FORM_INFO_XFORM_BINARY_CONTENT = "_form_info_xform_bin";
  private static final String FORM_INFO_MANIFEST_REF_BLOB = "_form_info_manifest_blb";
  private static final String FORM_INFO_MANIFEST_BINARY_CONTENT_REF_BLOB = "_form_info_manifest_ref";
  private static final String FORM_INFO_MANIFEST_BINARY_CONTENT = "_form_info_manifest_bin";
  private static FormInfoFilesetTable relation = null;
  private static BinaryContent xformBinaryRelation = null;
  private static BinaryContentRefBlob xformBinaryRefBlobRelation = null;
  private static RefBlob xformRefBlobRelation = null;
  private static BinaryContent manifestBinaryRelation = null;
  private static BinaryContentRefBlob manifestBinaryRefBlobRelation = null;
  private static RefBlob manifestRefBlobRelation = null;

  private FormInfoFilesetTable(String databaseSchema) {
    super(databaseSchema, TABLE_NAME);
    fieldList.add(ROOT_ELEMENT_MODEL_VERSION);
    fieldList.add(IS_ENCRYPTED_FORM);
    fieldList.add(IS_DOWNLOAD_ALLOWED);
    fieldList.add(LANGUAGE_CODE);
    fieldList.add(FORM_NAME);
    fieldList.add(DESCRIPTION);
    fieldList.add(DESCRIPTION_URL);

    fieldValueMap.put(primaryKey, FormInfoFilesetTable.URI_FORM_ID_VALUE_FORM_INFO_FILESET);
  }
  private FormInfoFilesetTable(FormInfoFilesetTable ref, User user) {
    super(ref, user);
  }

  static synchronized final FormInfoFilesetTable assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      FormInfoFilesetTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new FormInfoFilesetTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      BinaryContent xformBc = new BinaryContent(ds.getDefaultSchemaName(), FORM_INFO_XFORM_BINARY_CONTENT);
      ds.assertRelation(xformBc, user);
      BinaryContentRefBlob xformBref = new BinaryContentRefBlob(ds.getDefaultSchemaName(), FORM_INFO_XFORM_BINARY_CONTENT_REF_BLOB);
      ds.assertRelation(xformBref, user);
      RefBlob xformRef = new RefBlob(ds.getDefaultSchemaName(), FORM_INFO_XFORM_REF_BLOB);
      ds.assertRelation(xformRef, user);

      BinaryContent manifestBc = new BinaryContent(ds.getDefaultSchemaName(), FORM_INFO_MANIFEST_BINARY_CONTENT);
      ds.assertRelation(manifestBc, user);
      BinaryContentRefBlob manifestBref = new BinaryContentRefBlob(ds.getDefaultSchemaName(), FORM_INFO_MANIFEST_BINARY_CONTENT_REF_BLOB);
      ds.assertRelation(manifestBref, user);
      RefBlob manifestRef = new RefBlob(ds.getDefaultSchemaName(), FORM_INFO_MANIFEST_REF_BLOB);
      ds.assertRelation(manifestRef, user);

      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
      xformBinaryRelation = xformBc;
      xformBinaryRefBlobRelation = xformBref;
      xformRefBlobRelation = xformRef;

      manifestBinaryRelation = manifestBc;
      manifestBinaryRefBlobRelation = manifestBref;
      manifestRefBlobRelation = manifestRef;
    }
    return relation;
  }

  static final BinaryContentManipulator assertXformManipulator(String topLevelAuri, String uri, CallingContext cc) throws ODKDatastoreException {
    // make sure the relations are defined...
    assertRelation(cc);
    return new BinaryContentManipulator(uri, topLevelAuri, xformBinaryRelation, xformBinaryRefBlobRelation, xformRefBlobRelation);
  }

  static final BinaryContentManipulator assertManifestManipulator(String topLevelAuri, String uri, CallingContext cc) throws ODKDatastoreException {
    // make sure the relations are defined...
    assertRelation(cc);
    return new BinaryContentManipulator(uri, topLevelAuri, manifestBinaryRelation, manifestBinaryRefBlobRelation, manifestRefBlobRelation);
  }

  @Override
  public FormInfoFilesetTable getEmptyRow(User user) {
    return new FormInfoFilesetTable(this, user);
  }

}
