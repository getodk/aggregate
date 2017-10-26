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

package org.opendatakit.aggregate.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.GeoPointConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData.Reason;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.SubmissionAssociationTable;
import org.opendatakit.aggregate.form.XFormParameters;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Parses an XML definition of an XForm based on java rosa types
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * @author chrislrobert@gmail.com
 *
 */
public class FormParserForJavaRosa extends BaseFormParserForJavaRosa {

  private static final Log log = LogFactory.getLog(FormParserForJavaRosa.class.getName());

  private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 15 * 60 * 1000L;

  // arbitrary limit on the table-creation process, to prevent infinite loops
  // that exhaust memory
  private static final int MAX_FORM_CREATION_ATTEMPTS = 100;

  private String fdmSubmissionUri;
  private int elementCount = 0;
  private int phantomCount = 0;

  private final Map<FormDataModel, Integer> fieldLengths = new HashMap<FormDataModel, Integer>();

  /**
   * Constructor that parses and xform from the input stream supplied and
   * creates the proper ODK Aggregate Form definition.
   *
   * @param formName
   * @param formXmlData
   * @param inputXml
   * @param fileName
   * @param uploadedFormItems
   * @param warnings
   *          -- the builder that will hold all the non-fatal form-creation
   *          warnings
   * @param cc
   * @throws ODKFormAlreadyExistsException
   * @throws ODKIncompleteSubmissionData
   * @throws ODKDatastoreException
   * @throws ODKParseException
   */
  public FormParserForJavaRosa(String formName, MultiPartFormItem formXmlData, String inputXml,
      String fileName, MultiPartFormData uploadedFormItems, StringBuilder warnings,
      CallingContext cc) throws ODKFormAlreadyExistsException, ODKIncompleteSubmissionData,
      ODKDatastoreException, ODKParseException {
    super(inputXml, formName, false);

    if (formXmlData == null) {
      throw new ODKIncompleteSubmissionData(Reason.MISSING_XML);
    }

    // Construct the base table prefix candidate from the
    // submissionElementDefn.formId.
    String persistenceStoreFormId = submissionElementDefn.formId;
    if (persistenceStoreFormId.indexOf(':') != -1) {
      // this is likely an xmlns-style URI (http://..../)
      // remove the scheme://domain.org/ from this name, as it is likely
      // to be common across all forms. Use the remainder as the base
      // table
      // prefix candidate.
      persistenceStoreFormId = submissionElementDefn.formId.substring(submissionElementDefn.formId
          .indexOf(':') + 1);
      int idxSlashAfterDomain = persistenceStoreFormId.indexOf('/', 2);
      if (idxSlashAfterDomain != -1) {
        // remove the domain from the xmlns -- we'll use the string
        // after the
        // domain for the tablespace.
        persistenceStoreFormId = persistenceStoreFormId.substring(idxSlashAfterDomain + 1);
      }
    }
    // First, replace all slash substitutions with underscores.
    // Then replace all non-alphanumerics with underscores.
    // Then trim any leading underscores.
    persistenceStoreFormId = persistenceStoreFormId.replace(
        ParserConsts.FORWARD_SLASH_SUBSTITUTION, "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll(
        "[^\\p{Digit}\\p{Lu}\\p{Ll}\\p{Lo}]", "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll("^_*", "");

    initHelper(uploadedFormItems, formXmlData, inputXml, persistenceStoreFormId, warnings, cc);
  }

  enum AuxType {
    NONE, BC_REF, REF_BLOB, GEO_LAT, GEO_LNG, GEO_ALT, GEO_ACC, LONG_STRING_REF, REF_TEXT
  };

  private String generatePhantomKey(String uriSubmissionFormModel) {
    return String.format("elem+%1$s(%2$08d-phantom:%3$08d)", uriSubmissionFormModel, elementCount,
        ++phantomCount);
  }

  private void setPrimaryKey(FormDataModel m, String uriSubmissionFormModel, AuxType aux) {
    String pkString;
    if (aux != AuxType.NONE) {
      pkString = String.format("elem+%1$s(%2$08d-%3$s)", uriSubmissionFormModel, elementCount, aux
          .toString().toLowerCase());
    } else {
      ++elementCount;
      pkString = String.format("elem+%1$s(%2$08d)", uriSubmissionFormModel, elementCount);
    }
    m.setStringField(m.primaryKey, pkString);
  }

  private void initHelper(MultiPartFormData uploadedFormItems, MultiPartFormItem xformXmlData,
      String inputXml, String persistenceStoreFormId, StringBuilder warnings, CallingContext cc)
      throws ODKDatastoreException, ODKFormAlreadyExistsException, ODKParseException,
      ODKIncompleteSubmissionData {

    // ///////////////////
    // Step 0: ensure that form is not in the process of being deleted
    // we can't create or update a FormInfo record if there is a pending
    // deletion for this same id.
    {
      FormActionStatusTimestamp formDeletionStatus;
      formDeletionStatus = MiscTasks.getFormDeletionStatusTimestampOfFormId(rootElementDefn.formId,
          cc);
      if (formDeletionStatus != null) {
        throw new ODKFormAlreadyExistsException(
            "This form and its data have not yet been fully deleted from the server. Please wait a few minutes and retry.");
      }
      if (!submissionElementDefn.formId.equals(rootElementDefn.formId)) {
        formDeletionStatus = MiscTasks.getFormDeletionStatusTimestampOfFormId(
            submissionElementDefn.formId, cc);
        if (formDeletionStatus != null) {
          throw new ODKFormAlreadyExistsException(
              "This form and its data have not yet been fully deleted from the server. Please wait a few minutes and retry.");
        }
      }
    }

    // gain single-access lock record in database...
    String lockedResourceName = rootElementDefn.formId;
    String creationLockId = UUID.randomUUID().toString();
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    int i = 0;
    boolean locked = false;
    while (!locked) {
      if ((++i) % 10 == 0) {
        log.warn("excessive wait count for form creation lock. Count: " + i);
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
          // we remain in the loop even if we get kicked out.
        }
      } else if (i != 1) {
        try {
          Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
        } catch (InterruptedException e) {
          // we remain in the loop even if we get kicked out.
        }
      }
      try {
        TaskLock formCreationTaskLock = ds.createTaskLock(user);
        if (formCreationTaskLock.obtainLock(creationLockId, lockedResourceName,
            TaskLockType.CREATE_FORM)) {
          locked = true;
        }
        formCreationTaskLock = null;
      } catch (ODKTaskLockException e) {
        e.printStackTrace();
      }
    }

    // we hold the lock while we create the form here...
    try {
      guardedInitHelper(uploadedFormItems, xformXmlData, inputXml, persistenceStoreFormId,
          warnings, cc);
    } finally {
      // release the form creation serialization lock
      try {
        for (i = 0; i < 10; i++) {
          TaskLock formCreationTaskLock = ds.createTaskLock(user);
          if (formCreationTaskLock.releaseLock(creationLockId, lockedResourceName,
              TaskLockType.CREATE_FORM)) {
            break;
          }
          formCreationTaskLock = null;
          try {
            Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
          } catch (InterruptedException e) {
            // just move on, this retry mechanism
            // is to make things nice
          }
        }
      } catch (ODKTaskLockException e) {
        e.printStackTrace();
      }
    }
  }

  public static void updateFormXmlVersion(IForm thisForm, String incomingFormXml,
      Long modelVersion, CallingContext cc) throws ODKDatastoreException {
    String revisedXml = xmlWithTimestampComment(xmlWithoutTimestampComment(incomingFormXml),
        cc.getServerURL());
    // update the uiVersion and the form definition file...
    thisForm.setFormXml(thisForm.getFormFilename(cc), revisedXml, modelVersion, cc);
  }

  /**
   * Return the string by which we uniquely identify a table in the datastore.
   * This is the schema name concatenated with the table name. Used during table
   * creation to track the mapping from datastore tables to CommonFieldsBase
   * objects.
   *
   * @param tbl
   * @return
   */
  private String tableKey(CommonFieldsBase tbl) {
    return tbl.getSchemaName() + "." + tbl.getTableName();
  }

  private void guardedInitHelper(MultiPartFormData uploadedFormItems,
      MultiPartFormItem xformXmlData, String incomingFormXml, String persistenceStoreFormId,
      StringBuilder warnings, CallingContext cc) throws ODKDatastoreException,
      ODKFormAlreadyExistsException, ODKParseException, ODKIncompleteSubmissionData {
    // ///////////////
    // Step 1: create or fetch the Form (FormInfo) submission
    //
    // This allows us to delete the form if upload goes bad...
    // form downloads are immediately enabled unless the upload specifies
    // that they shouldn't be.
    String isIncompleteFlag = uploadedFormItems
        .getSimpleFormField(ServletConsts.TRANSFER_IS_INCOMPLETE);
    boolean isDownloadEnabled = (isIncompleteFlag == null || isIncompleteFlag.trim().length() == 0);

    /* true if newly created */
    boolean newlyCreatedXForm = false;
    /* true if we are modifying this form definition. */
    boolean updateForm;
    /* true if the form definition changes, but is compatible */
    @SuppressWarnings("unused")
    boolean differentForm = false;
    IForm formInfo = null;
    /*
     * originationGraceTime: if a previously loaded form was last updated prior
     * to the originationGraceTime, then require a version change if the new
     * form is not identical (is changed).
     */
    Date originationGraceTime = new Date(System.currentTimeMillis()
        - FIFTEEN_MINUTES_IN_MILLISECONDS);
    /*
     * originationTime: the time of the form's first upload to the system
     */
    Date originationTime;
    try {
      formInfo = FormFactory.retrieveFormByFormId(rootElementDefn.formId, cc);

      // formId matches...
      Boolean thisIsEncryptedForm = formInfo.isEncryptedForm();
      if (thisIsEncryptedForm == null) {
        thisIsEncryptedForm = false;
      }

      if (isFileEncryptedForm != thisIsEncryptedForm) {
        // they either both need to be encrypted, or both need to not be
        // encrypted...
        throw new ODKFormAlreadyExistsException(
            "Form encryption status cannot be altered. Form Id must be changed.");
      }
      // isEncryptedForm matches...

      XFormParameters thisRootElementDefn = formInfo.getRootElementDefn();
      String thisTitle = formInfo.getViewableName();
      String thisMd5Hash = formInfo.getMd5HashFormXml(cc);
      String md5Hash = CommonFieldsBase.newMD5HashUri(incomingFormXml);

      boolean same = thisRootElementDefn.equals(rootElementDefn)
          && (thisMd5Hash == null || md5Hash.equals(thisMd5Hash));

      if (same) {
        // version matches
        if (thisMd5Hash == null) {
          // IForm record does not have any attached form definition XML
          // attach it, set the title, and flag the form as updating
          // NOTE: this is an error path and not a normal flow
          updateFormXmlVersion(formInfo, incomingFormXml, rootElementDefn.modelVersion, cc);
          formInfo.setViewableName(title);
          updateForm = true;
          originationTime = new Date();
        } else {
          // The md5Hash of the form file being uploaded matches that
          // of a fully populated IForm record.
          // Do not allow changing the title...
          if (!title.equals(thisTitle)) {
            throw new ODKFormAlreadyExistsException(
                "Form title cannot be changed without updating the form version");
          }
          updateForm = false;
          String existingFormXml = formInfo.getFormXml(cc);
          // get the upload time of the existing form definition
          originationTime = FormParserForJavaRosa.xmlTimestamp(existingFormXml);
        }
      } else {
        String existingFormXml = formInfo.getFormXml(cc);
        // get the upload time of the existing form definition
        originationTime = FormParserForJavaRosa.xmlTimestamp(existingFormXml);

        if (FormParserForJavaRosa.xmlWithoutTimestampComment(incomingFormXml).equals(
            FormParserForJavaRosa.xmlWithoutTimestampComment(existingFormXml))) {
          // (version and file match).
          // The text of the form file being uploaded matches that of a
          // fully-populated IForm record once the ODK Aggregate
          // TimestampComment is removed.

          // Do not allow changing the title...
          if (!title.equals(thisTitle)) {
            throw new ODKFormAlreadyExistsException(
                "Form title cannot be changed without updating the form version.");
          }
          updateForm = false;

        } else {
          // file is different...

          // determine if the form is storage-equivalent and if version is
          // increasing...
          DifferenceResult diffresult = FormParserForJavaRosa.compareXml(this, existingFormXml,
              formInfo.getViewableName(), originationTime.after(originationGraceTime));
          if (diffresult == DifferenceResult.XFORMS_DIFFERENT) {
            // form is not storage-compatible
            throw new ODKFormAlreadyExistsException();
          }
          if (diffresult == DifferenceResult.XFORMS_MISSING_VERSION) {
            throw new ODKFormAlreadyExistsException(
                "Form definition file has changed but does not specify a form version.  Update the form version and resubmit.");
          }
          if (diffresult == DifferenceResult.XFORMS_EARLIER_VERSION) {
            throw new ODKFormAlreadyExistsException(
                "Form version is not lexically greater than existing form version.  Update the form version and resubmit.");
          }

          // update the title and form definition file as needed...
          if (!thisTitle.equals(title)) {
            formInfo.setViewableName(title);
          }

          updateFormXmlVersion(formInfo, incomingFormXml, rootElementDefn.modelVersion, cc);

          // mark this as a different form...
          differentForm = true;
          updateForm = true;
          originationTime = new Date();
        }
      }
    } catch (ODKFormNotFoundException e) {
      // form is not found -- create it
      formInfo = FormFactory.createFormId(incomingFormXml, rootElementDefn, isFileEncryptedForm,
          isDownloadEnabled, title, cc);
      updateForm = false;
      newlyCreatedXForm = true;
      originationTime = new Date();
    }

    // and upload all the media files associated with the form.
    // Allow updates if the form version has changed (updateForm is true)
    // or if the originationTime is after the originationGraceTime
    // e.g., the form version was changed within the last 15 minutes.

    boolean allowUpdates = updateForm || originationTime.after(originationGraceTime);

    // If an update is attempted and we don't allow updates,
    // throw an ODKFormAlreadyExistsException
    // NOTE: we store new files during this process, in the
    // expectation that the user simply forgot to update the
    // version and will do so shortly and upload that revised
    // form.
    Set<Map.Entry<String, MultiPartFormItem>> fileSet = uploadedFormItems.getFileNameEntrySet();
    for (Map.Entry<String, MultiPartFormItem> itm : fileSet) {
      if (itm.getValue() == xformXmlData)
        continue;// ignore the xform -- stored above.

      // update the images if the form version changed, otherwise throw an
      // error.
      if (formInfo.setXFormMediaFile(itm.getValue(), allowUpdates, cc)) {
        // needed update
        if (!allowUpdates) {
          // but we didn't update the form...
          throw new ODKFormAlreadyExistsException(
              "Form media file(s) have changed.  Please update the form version and resubmit.");
        }
      }
    }
    // NOTE: because of caching, we only update the form definition file at
    // intervals of no more than every 3 seconds. So if you upload a
    // media file, then immediately upload an altered version, we don't
    // necessarily increment the uiVersion.

    // Determine the information about the submission...
    formInfo.setIsComplete(true);
    formInfo.persist(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    FormDefinition fdDefined = null;
    try {
      fdDefined = FormDefinition.getFormDefinition(submissionElementDefn.formId, cc);
    } catch (IllegalStateException e) {
      e.printStackTrace();
      throw new ODKFormAlreadyExistsException(
          "Internal error: the form already exists but has a bad form definition.  Delete it.");
    }
    if (fdDefined != null) {
      // get most recent form-deletion statuses
      if (newlyCreatedXForm) {
        throw new ODKFormAlreadyExistsException(
            "Internal error: Completely new file has pre-existing form definition");
      }
      // we're done -- updated the file and media; form definition doesn't need
      // updating.
      return;
    }

    // we don't have an existing form definition
    // -- create a submission association table entry mapping to what will
    // be
    // the model.
    // -- then create the model and iterate on manifesting it in the
    // database.
    SubmissionAssociationTable sa = SubmissionAssociationTable.assertSubmissionAssociation(formInfo
        .getKey().getKey(), submissionElementDefn.formId, cc);
    fdmSubmissionUri = sa.getUriSubmissionDataModel();

    // so we have the formInfo record, but no data model backing it.
    // Find the submission associated with this form...

    final List<FormDataModel> fdmList = new ArrayList<FormDataModel>();

    // List of successfully asserted relations.
    // Use a HashMap<String,CommonFieldsBase>(). This allows us to
    // use the tableKey(CommonFieldsBase) (schema name + table name)
    // to identify the successfully asserted relations, rather than
    // the object identity of the CommonFieldsBase objects, which is
    // inappropriate for our use.
    final HashMap<String, CommonFieldsBase> assertedRelations = new HashMap<String, CommonFieldsBase>();

    try {
      // ////////////////////////////////////////////////
      // Step 2: Now build up the parse tree for the form...
      //
      final FormDataModel fdm = FormDataModel.assertRelation(cc);

      // we haven't actually constructed the fdm record yet, so use the
      // relation when creating the entity key...
      final EntityKey k = new EntityKey(fdm, fdmSubmissionUri);

      NamingSet opaque = new NamingSet();

      // construct the data model with table and column placeholders.
      // assumes that the root is a non-repeating group element.
      final String tableNamePlaceholder = opaque.getTableName(fdm.getSchemaName(),
          persistenceStoreFormId, "", "CORE");

      constructDataModel(opaque, k, fdmList, fdm, k.getKey(), 1, persistenceStoreFormId, "",
          tableNamePlaceholder, submissionElement, warnings, cc);

      // find a good set of names...
      // this also ensures that the table names don't overlap existing
      // tables
      // in the datastore.
      opaque.resolveNames(ds, user);

      // debug output
      // for ( FormDataModel m : fdmList ) {
      // m.print(System.out);
      // }

      // and revise the data model with those names...
      for (FormDataModel m : fdmList) {
        String tablePlaceholder = m.getPersistAsTable();
        if (tablePlaceholder == null)
          continue;

        String columnPlaceholder = m.getPersistAsColumn();

        String tableName = opaque.resolveTablePlaceholder(tablePlaceholder);
        String columnName = opaque.resolveColumnPlaceholder(tablePlaceholder, columnPlaceholder);

        m.setPersistAsColumn(columnName);
        m.setPersistAsTable(tableName);
      }

      // ///////////////////////////////////////////
      // Step 3: create the backing tables...
      //
      // OK. At this point, the construction gets a bit ugly.
      // We need to handle the possibility that the table
      // needs to be split into phantom tables.
      // That happens if the table exceeds the maximum row
      // size for the persistence layer.

      // we do this by constructing the form definition from the fdmList
      // and then testing for successful creation of each table it
      // defines.
      // If that table cannot be created, we subdivide it, rearranging
      // the structure of the fdmList. Repeat until no errors.
      // Very error prone!!!
      //
      FormDefinition fd = null;
      try {
        int nAttempts = 0;
        for (;;) {
          // place a limit on this process
          if (++nAttempts > MAX_FORM_CREATION_ATTEMPTS) {
            log.error("Aborting form-creation due to fail-safe limit ("
                + MAX_FORM_CREATION_ATTEMPTS + " attempts)!");
            throw new ODKParseException("Unable to create form data tables after "
                + MAX_FORM_CREATION_ATTEMPTS + " attempts.");
          }

          fd = new FormDefinition(sa, submissionElementDefn.formId, fdmList, cc);

          List<CommonFieldsBase> badTables = new ArrayList<CommonFieldsBase>();

          for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
            try {
              // patch up tbl with desired lengths of string
              // fields...
              for (FormDataModel m : fdmList) {
                if (m.getElementType().equals(ElementType.GEOTRACE) ||
                    m.getElementType().equals(ElementType.GEOSHAPE) ||
                    m.getElementType().equals(ElementType.STRING)) {
                  DataField f = m.getBackingKey();
                  Integer i = fieldLengths.get(m);
                  if (f != null && i != null) {
                    f.setMaxCharLen(new Long(i));
                  }
                }
              }

              // CommonFieldsBase objects are re-constructed with each
              // call to new FormDefinition(...). We need to ensure the
              // datastore contains the table that each of these objects
              // refers to.
              //
              // Optimization:
              //
              // If assertedRelations contains a hit for tableKey(tbl),
              // then we can assume that the table exists in the datastore
              // and just update the CommonFieldsBase object in the
              // assertedRelations map.
              //
              // Otherwise, we need to see if we can create it.
              //
              // Later on, before we do any more work, we need to
              // sweep through and call ds.assertRelation() to ensure
              // that all the CommonFieldsBase objects are fully
              // initialized (because we don't really know what is
              // done in the persistence layers during the
              // ds.assertRelation() call).
              //
              if (assertedRelations.containsKey(tableKey(tbl))) {
                assertedRelations.put(tableKey(tbl), tbl);
              } else {
                ds.assertRelation(tbl, user);
                assertedRelations.put(tableKey(tbl), tbl);
              }
            } catch (Exception e1) {
              // assume it is because the table is too wide...
              log.warn("Create failed -- assuming phantom table required " + tableKey(tbl) + " Exception: " + e1.toString());
              // we expect the following dropRelation to fail,
              // as the most likely state of the system is
              // that the table was unable to be created.
              try {
                ds.dropRelation(tbl, user);
              } catch (Exception e2) {
                // no-op
              }
              if ((tbl instanceof DynamicBase) || (tbl instanceof TopLevelDynamicBase)) {
                /* we know how to subdivide these -- we can recover from this */
                badTables.add(tbl);
              } else {
                /* there must be something amiss with the database... */
                throw e1;
              }
            }
          }

          for (CommonFieldsBase tbl : badTables) {
            // dang. We need to create phantom tables...
            orderlyDivideTable(fdmList, FormDataModel.assertRelation(cc), tbl, opaque, cc);
          }

          if (badTables.isEmpty()) {
            // OK. We created everything and have no re-work.
            //
            // Since this might be the N'th time through this
            // loop, we may have incompletely initialized the
            // CommonFieldsBase entries in the assertedRelations map.
            //
            // Go through that now, asserting each relation.
            // This ensures that all those entries are
            // properly initialized.
            //
            // Since this was once successful, it should still be.
            // If it isn't then any database error thrown is
            // not recoverable.
            for (CommonFieldsBase tbl : assertedRelations.values()) {
              ds.assertRelation(tbl, user);
            }
            break;
          }

          /*
           * reset the derived fields so that the FormDefinition construction
           * will work.
           */
          for (FormDataModel m : fdmList) {
            m.resetDerivedFields();
          }
        }
      } catch (Exception e) {
        /*
         * either something is amiss in the database or there was some sort of
         * internal error. Try to drop all the successfully created database
         * tables.
         */
        try {
          log.warn("Aborting form-creation do to exception: " + e.toString() +
              ". Datastore exceptions are expected in the following stack trace; other exceptions may indicate a problem:");
          e.printStackTrace();

          /* if everything were OK, assertedRelations should be empty... */
          if (!assertedRelations.isEmpty()) {
            log.error("assertedRelations not fully unwound!");
            Iterator<Entry<String, CommonFieldsBase>> iter = assertedRelations.entrySet()
                .iterator();
            while (iter.hasNext()) {
              Entry<String, CommonFieldsBase> entry = iter.next();
              CommonFieldsBase tbl = entry.getValue();
              try {
                log.error("--dropping " + entry.getKey());
                ds.dropRelation(tbl, user);
              } catch (Exception e3) {
                log.error("--Exception while dropping " + entry.getKey() + " exception: "
                    + e3.toString());
                // do nothing...
                e3.printStackTrace();
              }
              // we tried our best... twice.
              // Remove the definition whether
              // or not we were successful.
              // No point in ever trying again.
              iter.remove();
            }
          }

          // scorched earth -- get all the tables and try to drop them all...
          if ( fd != null ) {
            for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
              try {
                ds.dropRelation(tbl, user);
                assertedRelations.remove(tableKey(tbl));
              } catch (Exception e3) {
                // the above may fail because the table was never created...
                // do nothing...
                log.warn("If the following stack trace is not a complaint about a table not existing, it is likely a problem!");
                e3.printStackTrace();
              }
            }
          }
        } catch (Exception e4) {
          // just log error... popping out to original exception
          log.error("dropping of relations unexpectedly failed with exception: " + e4.toString());
          e4.printStackTrace();
        }
        throw new ODKParseException("Error processing new form: " + e.toString());
      }
      // TODO: if the above gets killed, how do we clean up?
    } catch (ODKParseException e) {
      formInfo.deleteForm(cc);
      throw e;
    } catch (ODKDatastoreException e) {
      formInfo.deleteForm(cc);
      throw e;
    }

    // ////////////////////////////////////////////
    // Step 4: record the data model...
    //
    // if we get here, we were able to create the tables -- record the
    // form description....
    ds.putEntities(fdmList, user);

    // TODO: if above write fails, how do we clean this up?

    // and update the complete flag to indicate that upload was fully
    // successful.
    sa.setIsPersistenceModelComplete(true);
    ds.putEntity(sa, user);
    // And wait until the data is propagated across all server instances.
    //
    // Rather than relying on MemCache, we insert this delay here so that
    // any caller that is creating a form can know that the form definition
    // has been propagated across the front-ends (subject to fast/slow
    // clocks).
    // This assumes that server clock rates never cause drifts of more than
    // the
    // network transmission latency between the requester and the server
    // over
    // the PersistConsts.MAX_SETTLE_MILLISECONDS time period.
    //
    // After this delay interval, the caller can be confident that the form
    // is visible by whatever server receives the caller's next request
    // (and this is also true during unit tests).
    try {
      Thread.sleep(PersistConsts.MAX_SETTLE_MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * The creation of the tbl relation has failed. We need to split it into
   * multiple sub-tables and try again.
   *
   * @param fdmList
   * @param fdmRelation
   * @param tbl
   * @param newPhantomTableName
   */
  private void orderlyDivideTable(List<FormDataModel> fdmList, FormDataModel fdmRelation,
      CommonFieldsBase tbl, NamingSet opaque, CallingContext cc) {

    log.info("Attempting to divide " + tbl.getTableName());
    // Find out how many columns it has...
    int nCol = tbl.getFieldList().size();
    if (tbl instanceof TopLevelDynamicBase) {
      nCol = nCol - TopLevelDynamicBase.ADDITIONAL_COLUMN_COUNT - 1;
    } else if (tbl instanceof DynamicBase) {
      nCol = nCol - DynamicBase.ADDITIONAL_COLUMN_COUNT - 1;
    }

    if (nCol < 2) {
      log.error("Too few columns to subdivide! " + tbl.getTableName());
      throw new IllegalStateException("Too few columns to subdivide instance table! "
          + tbl.getSchemaName() + "." + tbl.getTableName());
    }

    // because of how groups are assigned arbitrarily to tables, we can have a
    // table
    // that contains N groups but not the parent of those groups. So in order to
    // re-subdivide such a table, we need to scan the entire fdmList and build a
    // set of
    // parents of the elements contained in the table, provided those parents
    // are
    // each fully resident within the table.
    Set<FormDataModel> tblContentParents = new HashSet<FormDataModel>();
    for (FormDataModel m : fdmList) {
      if (!tbl.equals(m.getBackingObjectPrototype())) {
        // this isn't in the table being split.
        continue;
      }
      FormDataModel parentTable = m;
      while (parentTable.getParent() != null) {
        FormDataModel parent = parentTable.getParent();
        if (!tbl.equals(parent.getBackingObjectPrototype())) {
          // the parent is not within the tbl
          // so parentTable dominates
          break;
        }
        // and now check that the parent has not been
        // spread across multiple data tables already.
        // If it has, we want to keep the previous parent.
        boolean fragmented = false;
        for (FormDataModel child : parent.getChildren()) {
          if (!tbl.equals(child.getBackingObjectPrototype())
              && FormDataModel.isFieldStoredWithinDataTable(child.getElementType())) {
            // the child isn't in the table being split
            // and the child is one that should be stored
            // in a data table, so this parent is already
            // fragmented across multiple tables.
            fragmented = true;
            break;
          }
        }
        if (fragmented) {
          // stick with the current parentTable
          break;
        }
        // daisy-chain up to parent
        // we must have had an element or a subordinate group...
        parentTable = parent;
      }
      tblContentParents.add(parentTable);
    }

    // OK. We have all the elements that can be further split or reallocated
    // in tblContentParents we should have found something...
    if (tblContentParents.size() == 0) {
      log.error("Unable to locate model for backing table! " + tbl.getTableName());
      throw new IllegalStateException("Unable to locate model for backing table");
    }

    // go through the tblContentParents dividing them into groups and
    // raw elements.
    List<FormDataModel> topElementChange = new ArrayList<FormDataModel>();
    List<FormDataModel> groups = new ArrayList<FormDataModel>();
    for (;;) {
      for (FormDataModel m : tblContentParents) {
        // geopoints, phantoms and groups don't have backing keys
        if (m.getBackingKey() != null) {
          topElementChange.add(m);
        } else {
          groups.add(m);
        }
      }

      // order the list of groups from high to low...
      Collections.sort(groups, new Comparator<FormDataModel>() {
        @Override
        public int compare(FormDataModel o1, FormDataModel o2) {
          int c1 = recursivelyCountChildrenInSameTable(o1);
          int c2 = recursivelyCountChildrenInSameTable(o2);
          if (c1 > c2)
            return -1;
          if (c1 < c2)
            return 1;
          return 0;
        }
      });

      int firstGroupSize = (groups.size() == 0) ? 0 : recursivelyCountChildrenInSameTable(groups
          .get(0));
      if (groups.size() != 0
          && ((groups.size() + topElementChange.size() == 1) || firstGroupSize > (3 * nCol) / 4)) {
        // the parent group is dominated by a very large subgroup
        // switch to split that subgroup.
        FormDataModel parentTable = groups.get(0);
        groups.clear();
        topElementChange.clear();
        tblContentParents.clear();
        for (FormDataModel m : parentTable.getChildren()) {
          if (!tbl.equals(m.getBackingObjectPrototype())) {
            // this isn't in the table...
            continue;
          }
          tblContentParents.add(m);
        }
        // and then repeat this loop to assign the elements of this
        // group to the groups and topElementChange lists.
        // Note that we don't have to patch up the parentTable we are
        // moving off of, because the tbl will continue to exist. We
        // just need to move some of its contents to a second table,
        // either by moving a nested group or geopoint off, or by
        // creating a phantom table.
      } else {
        // OK either the groups are not dominated by a large group so
        // we can potentially reallocate them across the new and
        // existing tables or we have more than one raw element so we
        // can push some of the raw elements into phantom tables.
        // Proceed to try each of these in turn.
        break;
      }
    }

    // Table in which to move fields...
    String newTable;
    try {
      newTable = opaque.generateUniqueTableName(tbl.getSchemaName(), tbl.getTableName(), cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      log.error("Unable to interrogate database for new table to split backing table! "
          + tbl.getTableName());
      throw new IllegalStateException("unable to interrogate database");
    }

    // Try to move a set of groups into the new table such that the new
    // table is about 50% of the size of the original table.
    if (groups.size() > 0) {
      // list is already ordered from high to low...
      // go through the list moving the larger groups into tables
      // until close to half of the elements are moved...
      log.info("Multiple groups -- splitting to different tables");
      int cleaveCount = 0;
      for (FormDataModel m : groups) {
        int groupSize = recursivelyCountChildrenInSameTable(m);
        if (cleaveCount + groupSize > (3 * nCol) / 4) {
          // this group is too big to add into this particular split
          // see if there is a smaller group...
          continue;
        }
        recursivelyReassignChildren(m, tbl, newTable);
        cleaveCount += groupSize;
        // and if we have cleaved over half, (divide and conquer), retry
        // it with the database.
        if (cleaveCount > (nCol / 2)) {
          log.info("Cleaved along groups. New table: " + newTable + " columnCount: " + cleaveCount);
          return;
        }
      }
      // and otherwise, if we did cleave anything off, try anyway...
      // the next time through, we won't have any groups and will need
      // to create phantom tables, so it is worth trying for this here
      // now...
      if (cleaveCount > 0)
        log.info("Cleaved along groups. New table: " + newTable + " columnCount: " + cleaveCount);
      return;
    }

    log.info("Unable to cleave along groups; attempting phantom table! " + tbl.getTableName());

    // Urgh! we don't have any nested groups we can cleave off.
    // Create a phantom table. We need to preserve the parent-child
    // relationship and the ordinal ordering even for the
    // external tables like choices and binary objects.
    //
    // To do that, we want to move contiguous child elements into a phantom
    // table.
    // To do that, we need to identify the parents of the elements in the table,
    // even if those parents are already split across tables. Then, we assume
    // that the
    // parent with the greatest number of elements also has the greatest number
    // of
    // contiguous elements.

    // for each topElementChange, tally its presence under its parent.
    Map<FormDataModel, Integer> distinctParents = new HashMap<FormDataModel, Integer>();
    for (FormDataModel m : topElementChange) {
      FormDataModel parent = m.getParent();
      Integer a = distinctParents.get(parent);
      if (a == null) {
        distinctParents.put(parent, 1);
      } else {
        distinctParents.put(parent, a + 1);
      }
    }
    // scan the set of tallies to find the maximum tally.
    // That will be the parentTable that we will be splitting
    // with a phantom table.
    int max = 0;
    FormDataModel parentTable = null;
    for (Map.Entry<FormDataModel, Integer> e : distinctParents.entrySet()) {
      if (e.getValue() > max) {
        parentTable = e.getKey();
      }
    }

    // OK. We have the parent table.
    //
    // The children array is ordered by ordinal number.
    // Find the longest contiguous span to cleave off
    // or partially cleave off.
    //
    // This improves the split outcomes for forms with
    // many multiple choice elements, repeat groups or
    // media attachments.
    int idxStart;
    List<FormDataModel> children = parentTable.getChildren();
    // spanCount tracks, for a given key=firstIndexOfSpan,
    // the count of contiguous values.
    Map<Integer, Integer> spanCount = new HashMap<Integer, Integer>();
    int firstIndexOfSpan = 0;
    for (idxStart = 0; idxStart < children.size(); ++idxStart) {
      FormDataModel m = children.get(idxStart);
      if (!FormDataModel.isFieldStoredWithinDataTable(m.getElementType())) {
        continue;
      }
      if (!tbl.equals(m.getBackingObjectPrototype())) {
        // the contiguous span is broken...
        firstIndexOfSpan = idxStart + 1; // the next element...
        continue;
      }
      int elements = recursivelyCountChildrenInSameTable(m);
      if (elements == 0) {
        // the contiguous span is broken...
        firstIndexOfSpan = idxStart + 1; // the next element...
      } else {
        Integer v = spanCount.get(firstIndexOfSpan);
        if (v == null) {
          spanCount.put(firstIndexOfSpan, 1);
        } else {
          spanCount.put(firstIndexOfSpan, v + elements);
        }
      }
    }
    // find the longest span
    int maxSpanCount = 0;
    idxStart = -1;
    for (Map.Entry<Integer, Integer> spanEntry : spanCount.entrySet()) {
      if (spanEntry.getValue() > maxSpanCount) {
        idxStart = spanEntry.getKey();
        maxSpanCount = spanEntry.getValue();
      }
    }

    // now move up to half the desired original table columns of
    // this span into the phantom table. Typically, we will just
    // move all of this span into the phantom table because of
    // question groups, multiple choice or media questions breaking
    // up the continuity before the 50% mark.
    String phantomURI = generatePhantomKey(fdmSubmissionUri);
    int desiredOriginalTableColCount = (nCol / 2);

    if (idxStart == -1) {
      log.error("Failed to split at half the eligible records to move to phantom table "
          + tbl.getTableName());
      throw new IllegalStateException(
          "Failed to split at half the eligible records to move to phantom table!");
    }

    {
      // the contiguous elements after idxStart should be moved
      // to be "under" the phantom table.
      FormDataModel firstToMove = children.get(idxStart);
      long remainingOrdinalNumber = firstToMove.getOrdinalNumber();
      final long startingOrdinal = remainingOrdinalNumber;
      // data record...
      FormDataModel d = cc.getDatastore().createEntityUsingRelation(fdmRelation,
          cc.getCurrentUser());
      fdmList.add(d);
      d.setStringField(fdmRelation.primaryKey, phantomURI);
      d.setOrdinalNumber(remainingOrdinalNumber);
      d.setUriSubmissionDataModel(fdmSubmissionUri);
      d.setParentUriFormDataModel(parentTable.getUri());
      d.setElementName(null);
      d.setElementType(FormDataModel.ElementType.PHANTOM);
      d.setPersistAsColumn(null);
      d.setPersistAsTable(newTable);
      d.setPersistAsSchema(fdmRelation.getSchemaName());

      // OK -- update ordinals of the elements being moved...
      long ordinalNumber = 0L;
      int records = 0;
      for (; idxStart < children.size(); ++idxStart) {
        if (records >= desiredOriginalTableColCount) {
          // we have moved the desired number of columns to
          // the new table -- stop!
          break;
        }
        FormDataModel m = children.get(idxStart);
        if (!FormDataModel.isFieldStoredWithinDataTable(m.getElementType())) {
          m.setParentUriFormDataModel(phantomURI);
          m.setOrdinalNumber(++ordinalNumber);
          continue;
        }
        if (!tbl.equals(m.getBackingObjectPrototype())) {
          // we need to stop because this item is
          // already moved out elsewhere and
          // phantom tables are always contiguous...
          break;
        }
        int elements = recursivelyCountChildrenInSameTable(m);
        if (elements == 0) {
          // stop also if this element is already
          // elsewhere.
          break;
        }
        m.setParentUriFormDataModel(phantomURI);
        m.setOrdinalNumber(++ordinalNumber);
        recursivelyReassignChildren(m, tbl, newTable);
        records += recursivelyCountChildrenInSameTable(m);
      }
      // and update the remaining ordinals in the original set...
      for (; idxStart < children.size(); ++idxStart) {
        FormDataModel m = children.get(idxStart);
        m.setOrdinalNumber(++remainingOrdinalNumber);
      }
      log.info("Created phantom for " + tbl.getTableName() + " beginning at "
          + Long.toString(startingOrdinal) + " with a total of " + records + " cleaved");

      if (log.isDebugEnabled()) {
        log.debug("Dump after phantom-split of form list");
        for (FormDataModel m : fdmList) {
          m.print(System.err);
        }
      }
    }
  }

  private int recursivelyCountChildrenInSameTable(FormDataModel parent) {

    int count = 0;
    for (FormDataModel m : parent.getChildren()) {
      if (parent.getPersistAsTable().equals(m.getPersistAsTable())
          && parent.getPersistAsSchema().equals(m.getPersistAsSchema())) {
        count += recursivelyCountChildrenInSameTable(m);
      }
    }
    if (parent.getPersistAsColumn() != null) {
      count++;
    }
    return count;
  }

  private void recursivelyReassignChildren(FormDataModel biggest, CommonFieldsBase tbl,
      String newPhantomTableName) {

    if (!tbl.equals(biggest.getBackingObjectPrototype()))
      return;

    biggest.setPersistAsTable(newPhantomTableName);

    for (FormDataModel m : biggest.getChildren()) {
      recursivelyReassignChildren(m, tbl, newPhantomTableName);
    }

  }

  /**
   * Used to recursively process the xform definition tree to create the form
   * data model.
   *
   * @param treeElement
   *          java rosa tree element
   *
   * @param parentKey
   *          key from the parent form for proper entity group usage in gae
   *
   * @param parent
   *          parent form element
   *
   * @throws ODKEntityPersistException
   * @throws ODKParseException
   *
   */

  private void constructDataModel(final NamingSet opaque, final EntityKey k,
      final List<FormDataModel> dmList, final FormDataModel fdm, String parent, int ordinal,
      String tablePrefix, String nrGroupPrefix, String tableName, TreeElement treeElement,
      StringBuilder warnings, CallingContext cc) throws ODKEntityPersistException,
      ODKParseException {

    // for debugging: printTreeElementInfo(treeElement);

    FormDataModel d;

    FormDataModel.ElementType et;
    String persistAsTable = tableName;
    String originalPersistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix,
        treeElement.getName());
    String persistAsColumn = originalPersistAsColumn;

    switch (treeElement.getDataType()) {
    case org.javarosa.core.model.Constants.DATATYPE_TEXT:
      /**
       * Text question type.
       */
      et = FormDataModel.ElementType.STRING;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_INTEGER:
      /**
       * Numeric question type. These are numbers without decimal points
       */
      et = FormDataModel.ElementType.INTEGER;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DECIMAL:
      /**
       * Decimal question type. These are numbers with decimals
       */
      et = FormDataModel.ElementType.DECIMAL;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DATE:
      /**
       * Date question type. This has only date component without time.
       */
      et = FormDataModel.ElementType.JRDATE;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_TIME:
      /**
       * Time question type. This has only time element without date
       */
      et = FormDataModel.ElementType.JRTIME;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DATE_TIME:
      /**
       * Date and Time question type. This has both the date and time components
       */
      et = FormDataModel.ElementType.JRDATETIME;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_CHOICE:
      /**
       * This is a question with alist of options where not more than one option
       * can be selected at a time.
       */
      et = FormDataModel.ElementType.STRING;
      // et = FormDataModel.ElementType.SELECT1;
      // persistAsColumn = null;
      // persistAsTable = opaque.getTableName(fdm.getSchemaName(),
      // tablePrefix, nrGroupPrefix, treeElement.getName());
      break;
    case org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST:
      /**
       * This is a question with alist of options where more than one option can
       * be selected at a time.
       */
      et = FormDataModel.ElementType.SELECTN;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null;
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName());
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BOOLEAN:
      /**
       * Question with true and false answers.
       */
      et = FormDataModel.ElementType.BOOLEAN;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_GEOPOINT:
      /**
       * Question with location answer.
       */
      et = FormDataModel.ElementType.GEOPOINT;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null; // structured field
      break;
    case org.javarosa.core.model.Constants.DATATYPE_GEOTRACE:
      /**
       * Question with location trace.
       */
      et = FormDataModel.ElementType.GEOTRACE;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_GEOSHAPE:
      /**
       * Question with location trace.
       */
      et = FormDataModel.ElementType.GEOSHAPE;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BARCODE:
      /**
       * Question with barcode string answer.
       */
      et = FormDataModel.ElementType.STRING;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BINARY:
      /**
       * Question with external binary answer.
       */
      et = FormDataModel.ElementType.BINARY;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null;
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_BN");
      break;

    case org.javarosa.core.model.Constants.DATATYPE_NULL: /*
                                                           * for nodes that have
                                                           * no data, or data
                                                           * type otherwise
                                                           * unknown
                                                           */
      if (treeElement.isRepeatable()) {
        // repeatable group...
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        persistAsColumn = null;
        et = FormDataModel.ElementType.REPEAT;
        persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
            treeElement.getName());
      } else if (treeElement.getNumChildren() == 0 && dmList.size() != 0) {
        // assume fields that don't have children are string fields.
        // but exclude the top-level group, as somebody might define an
        // empty
        // form.
        // the developer likely has not set a type for the field.
        et = FormDataModel.ElementType.STRING;
        log.warn("Element " + getTreeElementPath(treeElement) + " does not have a type");
        warnings.append("<tr><td>");
        warnings.append(getTreeElementPath(treeElement));
        warnings.append("</td></tr>");
      } else {
        /* one or more children -- this is a non-repeating group */
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        persistAsColumn = null;
        et = FormDataModel.ElementType.GROUP;
      }
      break;

    default:
    case org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED:
      et = FormDataModel.ElementType.STRING;
      break;
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    // data record...
    d = ds.createEntityUsingRelation(fdm, user);
    setPrimaryKey(d, fdmSubmissionUri, AuxType.NONE);
    dmList.add(d);
    final String groupURI = d.getUri();
    d.setOrdinalNumber(Long.valueOf(ordinal));
    d.setUriSubmissionDataModel(k.getKey());
    d.setParentUriFormDataModel(parent);
    d.setElementName(treeElement.getName());
    d.setElementType(et);
    d.setPersistAsColumn(persistAsColumn);
    d.setPersistAsTable(persistAsTable);
    d.setPersistAsSchema(fdm.getSchemaName());

    // and patch up the tree elements that have multiple fields...
    switch (et) {
    case BINARY_CONTENT_REF_BLOB:
    case BOOLEAN:
    case DECIMAL:
    case INTEGER:
    case JRDATE:
    case JRDATETIME:
    case JRTIME:
    case PHANTOM:
    case REF_BLOB:
    case SELECT1:
    case SELECTN:
      // This case keeps lint messages down...
      break;
    case GEOTRACE:
    case GEOSHAPE:
    case STRING:
      // track the preferred string lengths of the string fields
      Integer len = getNodesetStringLength(treeElement);
      if (len != null) {
        fieldLengths.put(d, len);
      }
      break;
    case BINARY:
      // binary elements have two additional tables associated with them
      // -- the _REF and _BLB tables (in addition to _BIN above).
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_REF");

      // record for VersionedBinaryContentRefBlob..
      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.BC_REF);
      dmList.add(d);
      final String bcbURI = d.getUri();
      d.setOrdinalNumber(1L);
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.BINARY_CONTENT_REF_BLOB);
      d.setPersistAsColumn(null);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_BLB");

      // record for RefBlob...
      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.REF_BLOB);
      dmList.add(d);
      d.setOrdinalNumber(1L);
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(bcbURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.REF_BLOB);
      d.setPersistAsColumn(null);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());
      break;

    case GEOPOINT:
      // geopoints are stored as 4 fields (_LAT, _LNG, _ALT, _ACC) in the
      // persistence layer.
      // the geopoint attribute itself has no column, but is a placeholder
      // within
      // the data model for the expansion set of these 4 fields.

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LAT");

      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.GEO_LAT);
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LNG");

      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.GEO_LNG);
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ALT");

      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.GEO_ALT);
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ACC");

      d = ds.createEntityUsingRelation(fdm, user);
      setPrimaryKey(d, fdmSubmissionUri, AuxType.GEO_ACC);
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());
      break;

    case GROUP:
      // non-repeating group - this modifies the group prefix,
      // and all children are emitted.
      if (!parent.equals(k.getKey())) {
        // incorporate the group name only if it isn't the top-level
        // group.
        if (nrGroupPrefix.length() == 0) {
          nrGroupPrefix = treeElement.getName();
        } else {
          nrGroupPrefix = nrGroupPrefix + "_" + treeElement.getName();
        }
      }
      // OK -- group with at least one element -- assume no value...
      // TreeElement list has the begin and end tags for the nested
      // groups.
      // Swallow the end tag by looking to see if the prior and current
      // field names are the same.
      TreeElement prior = null;
      int trueOrdinal = 0;
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
        TreeElement current = (TreeElement) treeElement.getChildAt(i);
        // TODO: make this pay attention to namespace of the tag...
        if ((prior != null) && (prior.getName().equals(current.getName()))) {
          // it is the end-group tag... seems to happen with two
          // adjacent repeat
          // groups
          log.info("repeating tag at " + i + " skipping " + current.getName());
          prior = current;
        } else {
          constructDataModel(opaque, k, dmList, fdm, groupURI, ++trueOrdinal, tablePrefix,
              nrGroupPrefix, persistAsTable, current, warnings, cc);
          prior = current;
        }
      }
      break;

    case REPEAT:
      // repeating group - clears group prefix
      // and all children are emitted.
      // TreeElement list has the begin and end tags for the nested
      // groups.
      // Swallow the end tag by looking to see if the prior and current
      // field names are the same.
      prior = null;
      trueOrdinal = 0;
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
        TreeElement current = (TreeElement) treeElement.getChildAt(i);
        // TODO: make this pay attention to namespace of the tag...
        if ((prior != null) && (prior.getName().equals(current.getName()))) {
          // it is the end-group tag... seems to happen with two
          // adjacent repeat
          // groups
          log.info("repeating tag at " + i + " skipping " + current.getName());
          prior = current;
        } else {
          constructDataModel(opaque, k, dmList, fdm, groupURI, ++trueOrdinal, tablePrefix, "",
              persistAsTable, current, warnings, cc);
          prior = current;
        }
      }
      break;
    }
  }

}