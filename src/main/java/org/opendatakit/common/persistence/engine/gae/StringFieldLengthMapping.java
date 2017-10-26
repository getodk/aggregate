/**
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.common.persistence.engine.gae;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * This class manages accesses to the _STRING_FIELD_LENGTHS_ table.
 * That table has a PK of the gaeEntityKind of a table managed by 
 * the persistence layer.  The LENGTH_MAPPING column is a Text object
 * that contains a space-separated list of (field-name,length) pairs,
 * with an extra space at the end.  It is parsed to return the lengths
 * of the various fields.  
 * 
 * Provides equivalent functionality to DESCRIBE in RDBMS.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class StringFieldLengthMapping {

  private static final String TABLE_NAME = "_STRING_FIELD_LENGTHS_";
  private static final String LENGTH_MAPPING = "LENGTH_MAPPING";

  private Log logger = LogFactory.getLog(StringFieldLengthMapping.class);
  
  public StringFieldLengthMapping() {
  }

  /**
   * Called when a table is dropped to remove the lengths record
   * from the datastore.
   * 
   * @param gaeEntityKind -- entity kind whose column lengths are to be forgotten.
   * @param dam
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public synchronized void removeStringFieldLengths(String gaeEntityKind, 
      DatastoreAccessMetrics dam, DatastoreImpl datastore, User user) throws ODKDatastoreException {
    
    logger.info(gaeEntityKind);
    Key key = KeyFactory.createKey(TABLE_NAME, gaeEntityKind);
    try {
      datastore.getDatastoreService().delete(key);
    } catch ( Exception e ) {
      throw new ODKDatastoreException(e);
    }
    dam.recordDeleteUsage(TABLE_NAME);
  }
 
  /**
   * Internal routine to serialize and save the column lengths map.
   * 
   * @param e
   * @param lenMap
   * @param dam
   * @param datastore
   * @param user
   */
  private void putEntity(Entity e, Map<String, Long> lenMap, DatastoreAccessMetrics dam, DatastoreImpl datastore, User user ) {
    logger.info(e.getKey().getName());

    // build up the string that will be saved in BigTables
    StringBuilder b = new StringBuilder();
    for ( Map.Entry<String, Long> entry : lenMap.entrySet() ) {
      String k = entry.getKey();
      Long v = entry.getValue();
      b.append(k).append(" ").append(v).append(" ");
    }
    e.setProperty(LENGTH_MAPPING, new Text(b.toString()));
    
    datastore.getDatastoreService().put(e);
    dam.recordPutUsage(TABLE_NAME);
  }
  
  /**
   * Called when asserting the existence of a table.
   * Upon completion, the maximum lengths of all STRING
   * and URI columns will have been set.
   * 
   * @param gaeEntityKind -- entity kind being asserted 
   * @param relation
   * @param dam
   * @param datastore
   * @param user
   * @throws ODKDatastoreException
   */
  public synchronized void assertStringFieldLengths(String gaeEntityKind,
      CommonFieldsBase relation, DatastoreAccessMetrics dam, DatastoreImpl datastore, User user) throws ODKDatastoreException {

    Map<String,Long> lenMap = new HashMap<String, Long>();
    
    logger.info(gaeEntityKind);
    Key key = KeyFactory.createKey(TABLE_NAME, gaeEntityKind);
    
    try {
      dam.recordGetUsage(TABLE_NAME);
      Entity e = datastore.getDatastoreService().get(key);
      // we have a record of this -- get the mapping value...
      Object o = e.getProperty(LENGTH_MAPPING);
      if ( !(o instanceof Text) ) {
        throw new ODKDatastoreException("Expected Text object but found " + o.getClass().getName());
      }
      Text t = (Text) o;
      
      // for convenience, the last element of the split array
      // will be an extraneous empty string.  Confirm this!
      String[] splits = t.getValue().split(" ");
      if ( splits.length % 2 != 0 ) {
        throw new ODKDatastoreException("Unexpected non-even-element-count " +
                splits.length + " for string lengths map: " + t.getValue());
      }
      
      for ( int i = 0 ; i < splits.length ; i = i + 2 ) {
        String k = splits[i];
        String vStr = splits[i+1];
        
        try {
          Long v = Long.valueOf(vStr);
          lenMap.put(k, v);
        } catch ( NumberFormatException ex ) {
          throw new ODKDatastoreException("column name: " + k +
              " length: " + vStr + " could not be parsed: " + ex.toString());
        }
      }
      
      // we now have the lenMap as saved in the database.
      // Update the relation with the values from this list
      // and detect any new or changed values.
      // 
      // For migrations, we trust any incoming value over 
      // any the saved value.  Track whether the saved
      // value should be updated with additions, but don't
      // change it if there is a change that alters an 
      // existing (specified) length.
      boolean changed = false;
      for ( DataField f : relation.getFieldList() ) {
        if ( f.getDataType() == DataType.STRING ||
             f.getDataType() == DataType.URI ) {
          // string field -- get what GAE thinks
          // the length should be...
          Long len = lenMap.get(f.getName());
          Long reqLen = f.getMaxCharLen();
          if ( len == null ) {
            // GAE doesn't know -- we need to update GAE
            changed = true;
            if ( reqLen == null ) {
              // we're using the default length -- set field too...
              reqLen = PersistConsts.DEFAULT_MAX_STRING_LENGTH;
              f.setMaxCharLen(reqLen);
            }
            lenMap.put(f.getName(), reqLen);
          } else {
            if ( reqLen == null ) {
              // GAE knows the length -- set field now...
              f.setMaxCharLen(len);
            } else if (reqLen != len ) {
              // length differs -- update GAE
              changed = true;
              lenMap.put(f.getName(), reqLen);
            }
          }
        }
      }

      if ( changed ) {
        // we changed something in lenMap -- update GAE
        putEntity(e, lenMap, dam, datastore, user );
      }
      
    } catch ( EntityNotFoundException ex) {
      // no record of this entity...
      // ensure all default lengths are imposed.
      // build up length map.
      for ( DataField f : relation.getFieldList() ) {
        if ( (f.getDataType() == DataType.STRING ||
            f.getDataType() == DataType.URI) ) {
          Long len = f.getMaxCharLen();
          if ( len == null ) {
            len = PersistConsts.DEFAULT_MAX_STRING_LENGTH;
            f.setMaxCharLen(len);
          }
          lenMap.put(f.getName(), f.getMaxCharLen());
        }
      }
      
      // define a new entity...
      Entity e = new Entity(TABLE_NAME, gaeEntityKind);
      // set and insert it into GAE...
      putEntity(e, lenMap, dam, datastore, user);
    }
    
    // And verify that the invariant condition holds...
    // Everything should have a length
    for ( DataField f : relation.getFieldList() ) {
      if ( (f.getDataType() == DataType.STRING ||
          f.getDataType() == DataType.URI) &&
          f.getMaxCharLen() == null ) {
        throw new ODKDatastoreException("Failed to set all lengths!");
      } 
    }
  }
}