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
package org.opendatakit.common.persistence.engine.pgres;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.exception.ODKTaskLockException;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

import com.google.appengine.api.datastore.Transaction;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskLockImpl implements TaskLock {

	private static final String PERSISTENCE_LAYER_PROBLEM = "Persistence layer failure";

	DatastoreImpl datastore;
	User user;

	TaskLockImpl(DatastoreImpl datastore, User user) {
		this.datastore = datastore;
		this.user = user;
	}

	private static final String K_BQ = "`";
	private static final String K_ARG = "?";
	
	private TaskLockTable doTransaction(TaskLockTable entity, long l ) throws ODKEntityNotFoundException, ODKTaskLockException {
		Object[] ol = new Object[entity.getFieldList().size()*2+3];
		int[] il = new int[entity.getFieldList().size()*2+3];
		int idx = 0;
		boolean first;
		
		StringBuilder b = new StringBuilder();
		String tableName = K_BQ + datastore.getDefaultSchemaName() + K_BQ + "." + K_BQ + TaskLockTable.TABLE_NAME + K_BQ;
		
		b.append("BEGIN WORK; ");
		b.append("LOCK TABLE "); b.append(tableName); b.append(" IN ACCESS EXCLUSIVE; ");
		// delete stale locks (don't care who's)
		b.append("DELETE FROM "); b.append(tableName); b.append(" WHERE ");
			b.append(K_BQ); b.append(TaskLockTable.STR_TIMESTAMP); b.append(K_BQ); b.append(" < NOW(); ");

		if ( !entity.isFromDatabase() ) {
			// insert a new record
			b.append("INSERT INTO "); b.append(tableName); b.append(" (");
			first = true;
			for ( DataField f : entity.getFieldList()) {
				if ( !first ) {
					b.append(",");
				}
				first = false;
				b.append(K_BQ); b.append(f.getName()) ; b.append(K_BQ);
			}
			first = true;
			b.append(") VALUES ( ");
			for ( DataField f : entity.getFieldList()) {
				if ( !first ) {
					b.append(",");
				}
				first = false;
				if ( f.equals(entity.timestamp) ) {
					b.append(" NOW() + "); b.append(K_ARG);
					ol[idx] = BigInteger.valueOf(l);
					il[idx] = java.sql.Types.BIGINT;
					idx++;
				} else {
					b.append(K_ARG);
					DatastoreImpl.buildArgumentList(ol, il, idx++, entity, f);
				}
			}
			b.append(");");
		} else {
			// update existing record
			b.append("UPDATE "); b.append(tableName); b.append(" SET ");
			first = true;
			for ( DataField f : entity.getFieldList()) {
				if ( f == entity.primaryKey ) continue;
				if ( !first ) {
					b.append(",");
				}
				first = false;
				b.append(K_BQ); b.append(f.getName()); b.append(K_BQ);
				b.append(" = ");
				if ( f.equals(entity.timestamp) ) {
					b.append(" NOW() + "); b.append(K_ARG);
					ol[idx] = BigInteger.valueOf(l);
					il[idx] = java.sql.Types.BIGINT;
					idx++;
				} else {
					b.append(K_ARG);
					DatastoreImpl.buildArgumentList(ol, il, idx++, entity, f);
				}
			}
			b.append(" WHERE "); 
			b.append(K_BQ); b.append(entity.primaryKey.getName()); b.append(K_BQ);
			b.append(" = "); b.append(K_ARG); b.append("; ");
			DatastoreImpl.buildArgumentList(ol, il, idx++, entity, entity.primaryKey);
		}
		b.append("DELETE FROM ");
		b.append(tableName);
		b.append(" WHERE ");

		b.append(K_BQ); b.append(TaskLockTable.STR_FORM_ID); b.append(K_BQ);
		  b.append(" = "); b.append(K_ARG); b.append(" AND ");
		DatastoreImpl.buildArgumentList(ol, il, idx++, entity, entity.formId);

		b.append(K_BQ); b.append(TaskLockTable.STR_TASK_TYPE); b.append(K_BQ);
		  b.append(" = "); b.append(K_ARG); b.append(" AND ");
		DatastoreImpl.buildArgumentList(ol, il, idx++, entity, entity.taskType);

		b.append(K_BQ); b.append(TaskLockTable.STR_TIMESTAMP); b.append(K_BQ);
		b.append(" > SELECT MIN(");
			b.append(K_BQ); b.append(TaskLockTable.STR_TIMESTAMP); b.append(K_BQ);
		b.append(") WHERE ");
		
		b.append(K_BQ); b.append(TaskLockTable.STR_FORM_ID); b.append(K_BQ);
		  b.append(" = "); b.append(K_ARG); b.append(" AND ");
		DatastoreImpl.buildArgumentList(ol, il, idx++, entity, entity.formId);

		b.append(K_BQ); b.append(TaskLockTable.STR_TASK_TYPE); b.append(K_BQ);
		  b.append(" = "); b.append(K_ARG); b.append("; ");
		DatastoreImpl.buildArgumentList(ol, il, idx++, entity, entity.taskType);
		
	    b.append("COMMIT WORK; ");
		String fullTransactionString = b.toString();
		TaskLockTable relation;
		try {
			datastore.getJdbcConnection().update(fullTransactionString, ol, il);
		
			relation = TaskLockTable.createRelation(datastore, user);
		} catch (Exception e) {
			throw new ODKTaskLockException(PERSISTENCE_LAYER_PROBLEM, e);
		}
		return (TaskLockTable) datastore.getEntity(relation, entity.getUri(), user);
	}
	
	@Override
	public boolean obtainLock(String lockId, String formId,
			TaskLockType taskType) {
		boolean result = false;
		try {
			TaskLockTable relation = TaskLockTable.createRelation(datastore, user);
			TaskLockTable entity = datastore.createEntityUsingRelation(relation, null, user);
			entity.setStringField(entity.primaryKey, lockId);
			entity.setFormId(formId);
			entity.setTaskType(taskType.name());
			entity = doTransaction( entity, taskType.getLockExpirationTimeout());
			result = true;
		} catch (ODKEntityNotFoundException e) {
			// didn't gain the lock...
		} catch (ODKTaskLockException e) {
			// unexpected failure...
			e.printStackTrace();
		} catch (ODKDatastoreException e) {
			// unexpected failure...
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean renewLock(String lockId, String formId, TaskLockType taskType) {
	    boolean result = false;
	    try {
		    TaskLockTable relation = TaskLockTable.createRelation(datastore, user);
		    TaskLockTable entity = datastore.getEntity(relation, lockId, user);
		    if ( !(entity.getFormId().equals(formId) && 
		    	   entity.getTaskType().equals(taskType.name())) ) {
		    	throw new IllegalArgumentException("formId or taskType don't match datastore values");
		    }
		    entity = doTransaction( entity, taskType.getLockExpirationTimeout());
		    result = true;
	    } catch ( IllegalArgumentException e ) {
	    	// unexpected failure...
	    	e.printStackTrace();
	    } catch (ODKEntityNotFoundException e) {
	    	// unexpected failure...
	    	e.printStackTrace();
		} catch (ODKDatastoreException e) {
	    	// unexpected failure...
	    	e.printStackTrace();
		} catch (ODKTaskLockException e) {
	    	// unexpected failure...
	    	e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean releaseLock(String lockId, String formId,
			TaskLockType taskType) {
		boolean result = false;
		try {
			TaskLockTable relation = TaskLockTable.createRelation(datastore, user);
			datastore.deleteEntity(new EntityKey(relation, lockId), user);
			result = true;
		} catch (ODKDatastoreException e) {
			// if we see a lot of these, we are running too long between renewals
			e.printStackTrace();
		}
		return result;
	}

	private static class TaskLockTable extends CommonFieldsBase {
		static final String TABLE_NAME = "_task_lock";

		static final String STR_TIMESTAMP = "TIMESTAMP";
		static final String STR_TASK_TYPE = "TASK_TYPE";
		static final String STR_FORM_ID = "FORM_ID";

		private static final DataField FORM_ID = new DataField(STR_FORM_ID,
				DataField.DataType.STRING, false, 4096L);
		private static final DataField TASK_TYPE = new DataField(STR_TASK_TYPE,
				DataField.DataType.STRING, false, 80L);
		private static final DataField TIMESTAMP = new DataField(STR_TIMESTAMP,
				DataField.DataType.DATETIME, true);

		DataField formId;
		DataField taskType;
		DataField timestamp;

		TaskLockTable(String schema) {
			super(schema, TABLE_NAME);
			fieldList.add(formId = new DataField(FORM_ID));
			fieldList.add(taskType = new DataField(TASK_TYPE));
			fieldList.add(timestamp = new DataField(TIMESTAMP));
		}

		TaskLockTable(TaskLockTable ref, User user) {
			super(ref, user);
			formId = ref.formId;
			taskType = ref.taskType;
			timestamp = ref.timestamp;
		}

		String getFormId() {
			return getStringField(formId);
		}

		void setFormId(String value) {
			if (!setStringField(formId, value)) {
				throw new IllegalStateException("overflow formId");
			}
		}

		String getTaskType() {
			return getStringField(taskType);
		}

		void setTaskType(String value) {
			if (!setStringField(taskType, value)) {
				throw new IllegalStateException("overflow taskType");
			}
		}

		@SuppressWarnings("unused")
		Date getTimestamp() {
			return getDateField(timestamp);
		}

		@SuppressWarnings("unused")
		void setTimestamp(Date value) {
			setDateField(timestamp, value);
		}

		@Override
		public CommonFieldsBase getEmptyRow(User user) {
			return new TaskLockTable(this, user);
		}

		static TaskLockTable relation = null;

		static synchronized final TaskLockTable createRelation(
				Datastore datastore, User user) throws ODKDatastoreException {
			if (relation == null) {
				TaskLockTable relationPrototype;
				relationPrototype = new TaskLockTable(datastore
						.getDefaultSchemaName());
				datastore.createRelation(relationPrototype, user);
				relation = relationPrototype;
			}
			return relation;
		}
	}
}
