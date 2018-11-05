/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.common.datamodel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteHelper {

  public static final Logger logger = LoggerFactory
      .getLogger(DeleteHelper.class);

  private DeleteHelper() {
  }

  /**
   * Delete the list of objects. Deletion is in reverse order from the ordering
   * of the keys in the supplied list (the supplied list is easier to construct
   * in retrieval order, and we generally want to delete using a
   * reverse-retrieval order).
   *
   * @param keys
   * @param cc
   * @throws ODKDatastoreException
   */
  public static void deleteEntities(List<EntityKey> keys, CallingContext cc)
      throws ODKDatastoreException {
    // reverse the list of entities so that we delete them
    // in the reverse order...
    Collections.reverse(keys);

    try {
      // try to do it the fast way...
      cc.getDatastore().deleteEntities(keys, cc.getCurrentUser());
    } catch (Exception e) {
      logger.warn("Datastore failure while performing straight-forward delete of entities - attempting them individually");
      // we have a failure. Go through, deleting each in turn. If
      // the object does not exist, ignore the error. Otherwise,
      // abort at the first error found. This ensures that we are
      // able to recreate the deletion action when we restart or
      // resolve whatever this deletion problem is.
      HashSet<CommonFieldsBase> relations = new HashSet<CommonFieldsBase>();
      HashSet<CommonFieldsBase> nonRelations = new HashSet<CommonFieldsBase>();
      for (EntityKey key : keys) {
        try {
          CommonFieldsBase b = key.getRelation();
          // test that the table exists. We should not be dropping
          // tables prematurely, but perhaps someone ran MySQL
          // Workbench and did something foolish?
          //
          // Cache the relations that exist, so we don't need to
          // hit the database each time.
          if (nonRelations.contains(b)) {
            continue;
          }
          if (!relations.contains(b)) {
            if (cc.getDatastore().hasRelation(b.getSchemaName(), b.getTableName(),
                cc.getCurrentUser())) {
              relations.add(b);
            } else {
              nonRelations.add(b);
              continue;
            }
          }
          // we know the table exists, so verify that the
          // row exists by fetching it; if it exists, then
          // delete it, otherwise silently skip it.
          cc.getDatastore().getEntity(key.getRelation(), key.getKey(), cc.getCurrentUser());
          cc.getDatastore().deleteEntity(key, cc.getCurrentUser());
        } catch (ODKEntityNotFoundException ex) {
          // ignore this... if we are retrying we expect these...
        } catch (ODKDatastoreException ex) {
          // and log this... these are things to clean up manually...
          LoggerFactory.getLogger(DeleteHelper.class).warn(
              "Datastore failure while deleting " + key.getRelation().getSchemaName() + "."
                  + key.getRelation().getTableName() + " " + CommonFieldsBase.URI_COLUMN_NAME
                  + " = " + key.getKey());
          ex.printStackTrace();
          throw ex;
        } catch (Exception ex) {
          LoggerFactory.getLogger(DeleteHelper.class).warn(
              "Unexpected exception while deleting " + key.getRelation().getSchemaName() + "."
                  + key.getRelation().getTableName() + " " + CommonFieldsBase.URI_COLUMN_NAME
                  + " = " + key.getKey());
          ex.printStackTrace();
          throw new ODKDatastoreException("Unexpected exception", ex);
        }
      }
    }
  }

  public static void deleteDamagedSubmission(TopLevelDynamicBase tle,
                                             Set<DynamicCommonFieldsBase> backingObjects, CallingContext cc) throws ODKDatastoreException {

    Logger logger = LoggerFactory.getLogger(DeleteHelper.class);

    Set<DynamicDocumentBase> documents = new TreeSet<DynamicDocumentBase>(
        DynamicCommonFieldsBase.sameTableName);
    Set<DynamicAssociationBase> associations = new TreeSet<DynamicAssociationBase>(
        DynamicCommonFieldsBase.sameTableName);
    Set<DynamicBase> groups = new TreeSet<DynamicBase>(DynamicCommonFieldsBase.sameTableName);

    for (DynamicCommonFieldsBase dcb : backingObjects) {
      if (dcb instanceof DynamicDocumentBase) {
        documents.add((DynamicDocumentBase) dcb);
      } else if (dcb instanceof DynamicAssociationBase) {
        associations.add((DynamicAssociationBase) dcb);
      } else if (!(dcb instanceof TopLevelDynamicBase)) {
        groups.add((DynamicBase) dcb);
      }
    }

    logger.info("deleteDamagedSubmissions begin purging records for badTopLevelEntity: "
        + tle.getUri());

    // delete documents
    for (DynamicDocumentBase dd : documents) {
      Query q = cc.getDatastore().createQuery(dd, "purge(damaged) - documents",
          cc.getCurrentUser());
      q.addFilter(dd.topLevelAuri, FilterOperation.EQUAL, tle.getUri());
      List<? extends CommonFieldsBase> results = q.executeQuery();
      for (CommonFieldsBase c : results) {
        cc.getDatastore().deleteEntity(c.getEntityKey(), cc.getCurrentUser());
      }
    }

    // delete associations
    for (DynamicAssociationBase dd : associations) {
      Query q = cc.getDatastore().createQuery(dd, "purge(damaged) - associations",
          cc.getCurrentUser());
      q.addFilter(dd.topLevelAuri, FilterOperation.EQUAL, tle.getUri());
      List<? extends CommonFieldsBase> results = q.executeQuery();
      for (CommonFieldsBase c : results) {
        cc.getDatastore().deleteEntity(c.getEntityKey(), cc.getCurrentUser());
      }
    }

    // delete groups or choices
    for (DynamicBase dd : groups) {
      Query q = cc.getDatastore().createQuery(dd, "purge(damaged) - groups", cc.getCurrentUser());
      q.addFilter(dd.topLevelAuri, FilterOperation.EQUAL, tle.getUri());
      List<? extends CommonFieldsBase> results = q.executeQuery();
      for (CommonFieldsBase c : results) {
        cc.getDatastore().deleteEntity(c.getEntityKey(), cc.getCurrentUser());
      }
    }

    // delete top level entity
    cc.getDatastore().deleteEntity(tle.getEntityKey(), cc.getCurrentUser());
    logger.info("deleteDamagedSubmissions end purging records for badTopLevelEntity: "
        + tle.getUri());
  }
}
