/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.datamodel;

import java.util.Date;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.security.User;


/**
 * All instance data for an xform is stored in tables derived from 
 * InstanceDataBase or TopLevelInstanceDataBase tables.  The 
 * TopLevelInstanceDataBase table holds the metadata about the 
 * submission, whereas the repeat groups (the InstanceDataBase tables)
 * do not.
 * <p>
 * This common base class can be used by internal tables that should
 * be xform-like to share much of the processing of xform tables.
 * E.g., Xform tables can be viewed through the web interface.
 * <p>
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public abstract class TopLevelDynamicBase extends DynamicCommonFieldsBase {

  /* top level dynamic */
  public static final int ADDITIONAL_COLUMN_COUNT = 5 + CommonFieldsBase.AUDIT_COLUMN_COUNT;

  public static final String FIELD_NAME_MARKED_AS_COMPLETE_DATE = "_MARKED_AS_COMPLETE_DATE";

  /** (data model) version from submission */
  private static final DataField MODEL_VERSION = new DataField("_MODEL_VERSION", DataField.DataType.INTEGER, true);
  /** uiVersion from submission */
  private static final DataField UI_VERSION = new DataField("_UI_VERSION", DataField.DataType.INTEGER, true);
  /** whether or not the submission is complete.
   *
   * Because submissions may be uploaded across multiple transport requests, we need
   * a flag to say whether the submission has been fully uploaded w.r.t. the transport.
   *
   * Only completed submissions appear in reports and are forwarded to external services
   *
   * Note that the metadata block of a submission is available for implementing workflow
   * state transitions, if you want to do that.
   */
  private static final DataField IS_COMPLETE = new DataField("_IS_COMPLETE", DataField.DataType.BOOLEAN, true);

  private static final DataField MARKED_AS_COMPLETE_DATE = new DataField(FIELD_NAME_MARKED_AS_COMPLETE_DATE, DataField.DataType.DATETIME, true).setIndexable(IndexType.ORDERED);

  private static final DataField SUBMISSION_DATE = new DataField("_SUBMISSION_DATE", DataField.DataType.DATETIME, true);

  public final DataField modelVersion;
  public final DataField uiVersion;
  public final DataField isComplete;
  public final DataField markedAsCompleteDate;
  public final DataField submissionDate;

  /**
   * Construct a relation prototype.
   *
   * @param databaseSchema
   * @param tableName
   */
  protected TopLevelDynamicBase(String databaseSchema, String tableName) {
    super(databaseSchema, tableName);
    fieldList.add(modelVersion = new DataField(MODEL_VERSION));
    fieldList.add(uiVersion = new DataField(UI_VERSION));
    fieldList.add(isComplete = new DataField(IS_COMPLETE));
    fieldList.add(submissionDate = new DataField(SUBMISSION_DATE));
    fieldList.add(markedAsCompleteDate = new DataField(MARKED_AS_COMPLETE_DATE));
  }

  /**
   * Construct an empty entity.
   *
   * @param ref
   * @param user
   */
  protected TopLevelDynamicBase(TopLevelDynamicBase ref, User user) {
    super(ref, user);
    modelVersion = ref.modelVersion;
    uiVersion = ref.uiVersion;
    isComplete = ref.isComplete;
    submissionDate = ref.submissionDate;
    markedAsCompleteDate = ref.markedAsCompleteDate;
  }

  public final void setModelVersion(Long value) {
    setLongField(modelVersion, value);
  }

  public final void setUiVersion(Long value) {
    setLongField(uiVersion, value);
  }

  public final Boolean getIsComplete() {
    return getBooleanField(isComplete);
  }

  public final void setIsComplete(Boolean value) {
    setBooleanField(isComplete, value);
  }

  public final void setSubmissionDate(Date value) {
    setDateField(submissionDate, value);
  }

  public final Date getMarkedAsCompleteDate() {
    return getDateField(markedAsCompleteDate);
  }

  public final void setMarkedAsCompleteDate(Date value) {
    setDateField(markedAsCompleteDate, value);
  }
}
