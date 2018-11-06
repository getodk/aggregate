/*
 * Copyright (C) 2011 University of Washington.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.parser.FormParserForJavaRosa;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.util.BackendActionsTable;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for managing Form objects.
 * Does caching of the forms so as to minimize the number of database accesses.
 *
 * @author mitchellsundt@gmail.com
 */
public class FormFactory {

  private static final Logger logger = LoggerFactory.getLogger(FormFactory.class);
  private static final List<IForm> cache = new LinkedList<IForm>();
  private static long cacheTimestamp = 0L;

  private FormFactory() {
  }

  /**
   * Return the list of forms in the database.
   * If topLevelAuri is null, return all forms. Otherwise, return the form with the matching URI.
   * This is the main interface to the cache of form objects.  The cache is refreshed as a whole
   * every PersistConsts.MAX_SETTLE_MILLISECONDS.
   */
  private static synchronized final List<IForm> internalGetForms(String topLevelAuri, CallingContext cc) throws ODKDatastoreException {

    List<IForm> forms = new ArrayList<IForm>();
    if (cacheTimestamp + PersistConsts.MAX_SETTLE_MILLISECONDS > System.currentTimeMillis()) {
      // TODO: This cache should reside in MemCache.  Right now, different running
      // servers might see different Form definitions for up to the settle time.
      //
      // Since the datastore is treated as having a settle time of MAX_SETTLE_MILLISECONDS,
      // we should rely on the cache for that time interval.  Without MemCache-style
      // support, this is somewhat problematic since different server instances might
      // see different versions of the same Form.
      //
      logger.info("FormCache: using cached list of Forms");
    } else {
      // we have a fairly stale list of forms -- interrogate the database
      // for what is really there and update the cache.
      Map<String, IForm> oldForms = new HashMap<String, IForm>();
      for (IForm f : cache) {
        oldForms.put(f.getUri(), f);
      }
      cache.clear();
      logger.info("FormCache: fetching new list of Forms");

      Datastore ds = cc.getDatastore();
      User user = cc.getCurrentUser();

      FormInfoTable relation = FormInfoTable.assertRelation(cc);
      // ensure that Form table exists...
      Query formQuery = ds.createQuery(relation, "Form.getForms", user);
      List<? extends CommonFieldsBase> infoRows = formQuery.executeQuery();

      for (CommonFieldsBase cb : infoRows) {
        FormInfoTable infoRow = (FormInfoTable) cb;
        IForm f = oldForms.get(infoRow.getUri());
        // rely on the fact that a persist updates the last-update-date of the
        // top-level FormInfoTable even if only subordinate values are updated.
        Date infoDate = infoRow.getLastUpdateDate();
        Date oldDate = (f == null) ? null : f.getLastUpdateDate();
        if (f != null && f.hasValidFormDefinition() &&
            (infoRow.getCreationDate().equals(f.getCreationDate())) &&
            ((infoDate == null && oldDate == null) ||
                (infoDate != null && oldDate != null && infoDate.equals(oldDate)))) {
          cache.add(f);
        } else {
          logger.info("FormCache: refreshing form definition from database: " + infoRow.getStringField(FormInfoTable.FORM_ID));
          // pull and update from the datastore
          f = new Form(infoRow, cc);
          cache.add(f);
        }
      }

      for (IForm form : cache)
        if (!form.isValid()) {
          logger.error("Possible corruption: Form with URI " + form.getUri() + " is not valid");
          cache.remove(form);
        }

      // sort by form title then by form id
      Collections.sort(forms, new Comparator<IForm>() {

        @Override
        public int compare(IForm o1, IForm o2) {
          int ref = o1.getViewableName().compareToIgnoreCase(o2.getViewableName());
          if (ref != 0) return ref;
          return o1.getFormId().compareToIgnoreCase(o2.getFormId());
        }
      });

      // update cacheTimestamp -- note that if the datastore is very slow, this will
      // space out the updates because the cacheTimestamp is established after all
      // the datastore accesses.
      cacheTimestamp = System.currentTimeMillis();

      // test to see if we need to trigger the watchdog
      BackendActionsTable.triggerWatchdog(cc);
    }

    for (IForm v : cache) {
      if (topLevelAuri == null || v.getUri().equals(topLevelAuri)) {
        forms.add(v);
      }
    }
    return forms;
  }

  public static synchronized void clearForm(IForm match) {
    // NOTE: delays refresh of the forms list by the settle time.
    cache.remove(match);
    cacheTimestamp = System.currentTimeMillis();
  }

  /**
   * Common private static method through which all Form objects are obtained.
   * This provides a cache of the form data.  If known, the top-level object's
   * row object is passed in.  This is a database access optimization (minimize
   * GAE billing).
   */
  private static IForm getForm(String topLevelAuri, CallingContext cc) throws ODKDatastoreException {
    List<IForm> forms = internalGetForms(topLevelAuri, cc);

    if (forms.isEmpty())
      throw new ODKEntityNotFoundException("Could not retrieve form uri: " + topLevelAuri);
    IForm f = forms.get(0);
    // TODO: check authorization?
    return f;
  }

  public static final List<IForm> getForms(boolean checkAuthorization, CallingContext cc) throws ODKDatastoreException {
    List<IForm> forms = internalGetForms(null, cc);
    // TODO: check authorization
    return forms;
  }

  public static final String extractWellFormedFormId(String submissionKey) {
    int firstSlash = submissionKey.indexOf('/');
    String formId = submissionKey;
    if (firstSlash != -1) {
      // strip off the group path of the key
      formId = submissionKey.substring(0, firstSlash);
    }
    return formId;
  }

  public static IForm retrieveFormByFormId(String formId, CallingContext cc) throws ODKFormNotFoundException, ODKDatastoreException {

    if (formId == null) {
      return null;
    }
    try {
      String formUri = CommonFieldsBase.newMD5HashUri(formId);
      IForm form = getForm(formUri, cc);
      if (!formId.equals(form.getFormId())) {
        throw new IllegalStateException("more than one FormInfo entry for the given form id: "
            + formId);
      }
      return form;
    } catch (ODKOverQuotaException e) { // datastore exception
      throw e;
    } catch (ODKEntityNotFoundException e) { // datastore exception
      throw new ODKFormNotFoundException(e);
    } catch (ODKDatastoreException e) {
      throw e;
    } catch (Exception e) {
      throw new ODKFormNotFoundException(e);
    }
  }

  public static IForm retrieveForm(List<SubmissionKeyPart> parts, CallingContext cc) throws ODKDatastoreException, ODKFormNotFoundException {

    if (!FormInfo.validFormKey(parts)) {
      return null;
    }

    try {
      String formUri = parts.get(1).getAuri();
      IForm form = getForm(formUri, cc);
      return form;
    } catch (ODKOverQuotaException e) { // datastore exception
      throw e;
    } catch (ODKEntityNotFoundException e) { // datastore exception
      throw new ODKFormNotFoundException(e);
    } catch (ODKDatastoreException e) {
      throw e;
    } catch (Exception e) {
      throw new ODKFormNotFoundException(e);
    }
  }

  public static IForm createFormId(String incomingFormXml, XFormParameters rootElementDefn, boolean isEncryptedForm, boolean isDownloadEnabled, String title, CallingContext cc) throws ODKDatastoreException {
    IForm thisForm = null;

    String formUri = CommonFieldsBase.newMD5HashUri(rootElementDefn.formId);
    try {
      thisForm = getForm(formUri, cc); // this SHOULD throw an exception!!!
    } catch (ODKEntityNotFoundException e) {
      thisForm = new Form(rootElementDefn, isEncryptedForm, isDownloadEnabled, title, cc);
      FormParserForJavaRosa.updateFormXmlVersion(thisForm, incomingFormXml, rootElementDefn.modelVersion, cc);
    }
    return thisForm;
  }
}

