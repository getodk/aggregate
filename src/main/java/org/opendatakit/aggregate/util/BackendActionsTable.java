/*
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.aggregate.util;

import java.util.Date;

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.task.Watchdog;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Implements the mechanisms used in GAE to launch Watchdog during periods of 
 * website activity ( triggerWatchdog ) and from within the Watchdog implementation
 * when there are background tasks requiring supervision ( scheduleFutureWatchdog ).
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class BackendActionsTable extends CommonFieldsBase {

	private static final String WATCHDOG_SCHEDULING_ROW_ID = "rid:watchdog_scheduling";
	
	private static final String TABLE_NAME = "_backend_actions";
	
	private static final DataField LAST_REVISION_DATE = new DataField(
			"LAST_REVISION", DataField.DataType.DATETIME, true );

	private static long lastWatchdogSchedulingTime = 0L;
	
	/**
	 * Construct a relation prototype.  Only called via {@link #assertRelation(Datastore, User)}
	 * 
	 * @param schemaName
	 */
	protected BackendActionsTable(String schemaName) {
		super(schemaName, TABLE_NAME);
		fieldList.add(LAST_REVISION_DATE);
	}
	
	/**
	 * Construct an empty entity.  Only called via {@link #getEmptyRow(User)}
	 * 
	 * @param ref
	 * @param user
	 */
	protected BackendActionsTable(BackendActionsTable ref, User user) {
		super(ref, user);
	}

	// Only called from within the persistence layer.
	@Override
	public CommonFieldsBase getEmptyRow(User user) {
		BackendActionsTable t = new BackendActionsTable(this, user);
		return t;
	}

	private Date getLastRevisionDate() {
		return getDateField(LAST_REVISION_DATE);
	}
	
	private void setLastRevisionDate(Date value) {
		setDateField(LAST_REVISION_DATE, value);
	}

	private static BackendActionsTable relation = null;
	
	/**
	 * This is private because this table implements a singleton pattern.
	 * 
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	private static BackendActionsTable assertRelation(Datastore datastore, User user) throws ODKDatastoreException {
		if ( relation == null ) {
			BackendActionsTable relationPrototype;
			relationPrototype = new BackendActionsTable(datastore.getDefaultSchemaName());
			datastore.assertRelation(relationPrototype, user);
			relation = relationPrototype;
		}
		return relation;
	}
	
	/**
	 * This retrieves the singleton record.
	 * 
	 * @param uri
	 * @param datastore
	 * @param user
	 * @return
	 * @throws ODKDatastoreException
	 */
	private static final BackendActionsTable getSingletonRecord(String uri, Datastore datastore, User user) throws ODKDatastoreException {
		BackendActionsTable prototype = assertRelation(datastore, user);
		BackendActionsTable record = null;
		try {
			record = datastore.getEntity(prototype, uri, user);
		} catch ( ODKEntityNotFoundException e ) {
			record = datastore.createEntityUsingRelation(prototype, user);
			record.setStringField(prototype.primaryKey, uri);
			record.setLastRevisionDate(new Date(0)); // NOTE: Defaults differently than SecurityRevisionsTable
			datastore.putEntity(record, user);
		}
		return record;
	}
   
   private static final long getLastWatchdogSchedulingRevisionDate(Datastore datastore, User user) throws ODKDatastoreException {
      BackendActionsTable t = getSingletonRecord( WATCHDOG_SCHEDULING_ROW_ID, datastore, user );
      return t.getLastRevisionDate().getTime();
   }
   
   private static final void setLastWatchdogSchedulingRevisionDate(long futureMilliseconds, Datastore datastore, User user) throws ODKDatastoreException {
      BackendActionsTable t = getSingletonRecord( WATCHDOG_SCHEDULING_ROW_ID, datastore, user );
      t.setLastRevisionDate(new Date(System.currentTimeMillis()+futureMilliseconds));
      datastore.putEntity(t, user);
   }
   
   /**
    * Schedule a watchdog to run the specified number of milliseconds in the future.
    * 
    * @param futureMilliseconds
    * @param cc
    */
   public static final synchronized void scheduleFutureWatchdog(long futureMilliseconds, CallingContext cc) {
     boolean wasDaemon = cc.getAsDeamon();
     try {
       cc.setAsDaemon(true);
       Datastore ds = cc.getDatastore();
       User user = cc.getCurrentUser();
       
       Watchdog dog = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
       dog.onUsage(0L, cc);
       BackendActionsTable.setLastWatchdogSchedulingRevisionDate(futureMilliseconds, ds, user);
     } catch (ODKDatastoreException e) {
       e.printStackTrace();
     } finally {
       cc.setAsDaemon(wasDaemon);
     }
   }
   
   /**
    * Check whether a watchdog should be spun up (immediately).  This is done
    * every Watchdog.WATCHDOG_RETRY_INTERVAL_MILLISECONDS.  Note that if the 
    * watchdog determines that there is work pending, it will schedule itself
    * to run in  
    * 
    * @param cc
    */
   public static final synchronized void triggerWatchdog(CallingContext cc) {
     boolean wasDaemon = cc.getAsDeamon();
     if ( lastWatchdogSchedulingTime + Watchdog.WATCHDOG_RETRY_INTERVAL_MILLISECONDS < System.currentTimeMillis() ) {
       try {
         cc.setAsDaemon(true);
         Datastore ds = cc.getDatastore();
         User user = cc.getCurrentUser();
         
         // check to see that another process hasn't already updated this value...
         lastWatchdogSchedulingTime = BackendActionsTable.getLastWatchdogSchedulingRevisionDate(ds, user);
  
         if ( lastWatchdogSchedulingTime + Watchdog.WATCHDOG_RETRY_INTERVAL_MILLISECONDS < System.currentTimeMillis() ) { 
           // OK -- trigger a watchdog process and update the value now...
           // NOTE: duplicate running watchdogs are possible.
           Watchdog dog = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
           dog.onUsage(0L, cc);
           BackendActionsTable.setLastWatchdogSchedulingRevisionDate(0L, ds, user);
         }
       } catch (ODKDatastoreException e) {
         e.printStackTrace();
       } finally {
         cc.setAsDaemon(wasDaemon);
       }
     }
   }
}
