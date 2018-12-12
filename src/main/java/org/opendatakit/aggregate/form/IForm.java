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

package org.opendatakit.aggregate.form;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Persistable definition of the XForm that defines how to store submissions to
 * the datastore. Includes form elements that specify how to properly convert
 * the data to/from the datastore.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public interface IForm {
  Long MAX_FORM_ID_LENGTH = PersistConsts.GUARANTEED_SEARCHABLE_LEN;

  void persist(CallingContext cc) throws ODKDatastoreException;


  void deleteForm(CallingContext cc) throws ODKDatastoreException;

  EntityKey getEntityKey();

  FormElementModel getTopLevelGroupElement();

  XFormParameters getRootElementDefn();

  String getMajorMinorVersionString();

  String getOpenRosaVersionString();

  String getXFormFileHash(CallingContext cc) throws ODKDatastoreException;

  boolean hasValidFormDefinition();

  String getFormId();

  boolean hasManifestFileset(CallingContext cc) throws ODKDatastoreException;

  BinaryContentManipulator getManifestFileset();

  String getViewableName();

  void setViewableName(String title);

  String getViewableFormNameSuitableAsFileName();

  String getDescription();

  String getDescriptionUrl();

  Date getCreationDate();

  Date getLastUpdateDate();

  String getCreationUser();

  BinaryContentManipulator getXformDefinition();

  String getFormFilename(CallingContext cc) throws ODKDatastoreException;

  String getFormXml(CallingContext cc) throws ODKDatastoreException;

  BlobSubmissionOutcome setFormXml(String formFilename, String formXml, Long modelVersion, CallingContext cc) throws ODKDatastoreException;

  String getMd5HashFormXml(CallingContext cc) throws ODKDatastoreException;

  Boolean isEncryptedForm();

  Boolean getDownloadEnabled();

  void setDownloadEnabled(Boolean downloadEnabled);

  Boolean getSubmissionEnabled();

  void setSubmissionEnabled(Boolean submissionEnabled);

  FormElementModel getFormElementModel(List<SubmissionKeyPart> submissionKeyParts);

  Set<DynamicCommonFieldsBase> getAllBackingObjects();

  List<FormElementModel> getRepeatGroupsInModel();

  FormSummary generateFormSummary(CallingContext cc) throws ODKDatastoreException;

  void setIsComplete(Boolean value);

  EntityKey getKey();

  boolean setXFormMediaFile(MultiPartFormItem item, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException;

  String getUri();

  boolean isValid();
}
