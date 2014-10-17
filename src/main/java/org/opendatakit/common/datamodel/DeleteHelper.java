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

import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

public class DeleteHelper {

  private DeleteHelper() {}
  
  /**
   * Delete the list of objects. Deletion is in reverse order from 
   * the ordering of the keys in the supplied list (the supplied 
   * list is easier to construct in retrieval order, and we generally
   * want to delete using a reverse-retrieval order).
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
      // we have a failure. Go through, deleting each in turn. If
      // the object does not exist, ignore the error. Otherwise, 
      // abort at the first error found. This ensures that we are 
      // able to recreate the deletion action when we restart or 
      // resolve whatever this deletion problem is.
      HashSet<CommonFieldsBase> relations = new HashSet<CommonFieldsBase>();
      HashSet<CommonFieldsBase> nonRelations = new HashSet<CommonFieldsBase>();
      for ( EntityKey key : keys ) {
        try {
          CommonFieldsBase b = key.getRelation();
          // test that the table exists. We should not be dropping
          // tables prematurely, but perhaps someone ran MySQL
          // Workbench and did something foolish?
          // 
          // Cache the relations that exist, so we don't need to 
          // hit the database each time.
          if ( nonRelations.contains(b) ) {
            continue;
          }
          if ( !relations.contains(b) ) {
            if ( cc.getDatastore().hasRelation(b.getSchemaName(), b.getTableName(), cc.getCurrentUser()) ) {
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
        } catch ( ODKEntityNotFoundException ex ) {
          // ignore this... if we are retrying we expect these...
        } catch ( ODKDatastoreException ex ) {
          // and log this... these are things to clean up manually...
          LogFactory.getLog(BinaryContentManipulator.class).warn(
              "Datastore failure while deleting " + key.getRelation().getSchemaName() +
              "." + key.getRelation().getTableName() + " " + CommonFieldsBase.URI_COLUMN_NAME +
              " = " + key.getKey());
          ex.printStackTrace();
          throw ex;
        } catch ( Exception ex ) {
          LogFactory.getLog(BinaryContentManipulator.class).warn(
              "Unexpected exception while deleting " + key.getRelation().getSchemaName() +
              "." + key.getRelation().getTableName() + " " + CommonFieldsBase.URI_COLUMN_NAME +
              " = " + key.getKey());
          ex.printStackTrace();
          throw new ODKDatastoreException("Unexpected exception", ex);
        }
      }
    }
  }
}
