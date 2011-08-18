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
package org.opendatakit.common.persistence.engine.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.ITaskLockType;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskLockImpl implements TaskLock {

	private static final String PERSISTENCE_LAYER_PROBLEM = "Persistence layer failure";

	final DatastoreImpl datastore;
	final User user;

	TaskLockImpl(DatastoreImpl datastore, User user) {
		this.datastore = datastore;
		this.user = user;
	}

	private static final String K_BQ = "`";
		
	private TaskLockTable doTransaction(TaskLockTable entity, long l ) throws ODKEntityNotFoundException, ODKTaskLockException {
		boolean first;

		final List<String> stmts = new ArrayList<String>();
		
		StringBuilder b = new StringBuilder();
		String tableName = K_BQ + datastore.getDefaultSchemaName() + K_BQ + "." + K_BQ + TaskLockTable.TABLE_NAME + K_BQ;
//		String tableName= TaskLockTable.TABLE_NAME;
		
		b.append("SET @present = NOW()"); stmts.add(b.toString()); b.setLength(0);
		b.append("SET @uriUser = '"); b.append(StringEscapeUtils.escapeSql(user.getUriUser())); b.append("'"); stmts.add(b.toString()); b.setLength(0);
		b.append("SET @uriLock = '"); b.append(StringEscapeUtils.escapeSql(entity.getUri())); b.append("'"); stmts.add(b.toString()); b.setLength(0);
		b.append("SET @formId = '"); b.append(StringEscapeUtils.escapeSql(entity.getFormId())); b.append("'"); stmts.add(b.toString()); b.setLength(0);
		b.append("SET @taskType = '"); b.append(StringEscapeUtils.escapeSql(entity.getTaskType())); b.append("'"); stmts.add(b.toString()); b.setLength(0);
		b.append("SET @lifetimeMicroseconds = "); b.append(1000L*l); stmts.add(b.toString()); b.setLength(0);
		b.append("LOCK TABLES "); b.append(tableName); b.append(" WRITE "); 
		b.append(", "); b.append(tableName); b.append(" AS t1 WRITE ");
		b.append(", "); b.append(tableName); b.append(" AS t3 READ ");
		b.append(", "); b.append(tableName); b.append(" AS t4 WRITE "); stmts.add(b.toString()); b.setLength(0);

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
				if ( f.equals(entity.creationDate) ||
					 f.equals(entity.lastUpdateDate) ) {
					b.append("@present");
				} else if ( f.equals(entity.creatorUriUser) ||
							f.equals(entity.lastUpdateUriUser) ) {
					b.append("@uriUser");
				} else if ( f.equals(entity.formId) ) {
					b.append("@formId");
				} else if ( f.equals(entity.taskType) ) {
					b.append("@taskType");
				} else if ( f.equals(entity.primaryKey) ) {
					b.append("@uriLock");
				} else if ( f.equals(entity.expirationDateTime) ) {
					b.append(" TIMESTAMPADD(MICROSECOND, @lifetimeMicroseconds, @present)");
				} else {
					throw new IllegalStateException("unexpected case " + f.getName());
				}
			}
			b.append(")"); stmts.add(b.toString()); b.setLength(0);
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
				if ( f.equals(entity.creationDate) ||
						 f.equals(entity.lastUpdateDate) ) {
					b.append("@present");
				} else if ( f.equals(entity.creatorUriUser) ||
							f.equals(entity.lastUpdateUriUser) ) {
					b.append("@uriUser");
				} else if ( f.equals(entity.formId) ) {
					b.append("@formId");
				} else if ( f.equals(entity.taskType) ) {
					b.append("@taskType");
				} else if ( f.equals(entity.primaryKey) ) {
					b.append("@uriLock");
				} else if ( f.equals(entity.expirationDateTime) ) {
					b.append(" TIMESTAMPADD(MICROSECOND, @lifetimeMicroseconds, @present)");
				} else {
					throw new IllegalStateException("unexpected case " + f.getName());
				}
			}
			b.append(" WHERE "); 
			b.append(K_BQ); b.append(entity.primaryKey.getName()); b.append(K_BQ);
			b.append(" = "); b.append("@uriLock");  stmts.add(b.toString()); b.setLength(0);
		}
		// delete stale locks (don't care who's)
		b.append("DELETE t1 FROM "); b.append(tableName); b.append(" AS t1 WHERE t1.");
			b.append(K_BQ); b.append(entity.expirationDateTime.getName()); b.append(K_BQ); b.append(" <= @present"); stmts.add(b.toString()); b.setLength(0);
		// determine the time of the oldest lock for this resource and task type... 
		b.append("SET @minExpiration = @present"); stmts.add(b.toString()); b.setLength(0);
		b.append("SELECT @minExpiration:=MIN(t3.");
		b.append(K_BQ); b.append(entity.expirationDateTime.getName()); b.append(K_BQ);
		b.append(") FROM "); b.append(tableName); b.append(" AS t3 WHERE t3.");
		b.append(K_BQ); b.append(entity.formId.getName()); b.append(K_BQ); b.append(" = @formId AND t3.");
		b.append(K_BQ); b.append(entity.taskType.getName()); b.append(K_BQ); b.append(" = @taskType"); stmts.add(b.toString()); b.setLength(0);
		// delete all locks except the oldest one for this resource and task type...
		// whatever lock exists identifies the owner of the resource.
		b.append("DELETE t4 FROM ");
		b.append(tableName);
		b.append(" AS t4 WHERE t4.");

		b.append(K_BQ); b.append(entity.formId.getName()); b.append(K_BQ);
		  b.append(" = @formId AND t4.");

		b.append(K_BQ); b.append(entity.taskType.getName()); b.append(K_BQ);
		  b.append(" = @taskType AND TIMESTAMPDIFF(MICROSECOND, t4.");
		
		b.append(K_BQ); b.append(entity.expirationDateTime.getName()); b.append(K_BQ);
		b.append(",@minExpiration) > 0"); stmts.add(b.toString()); b.setLength(0);
		
		TaskLockTable relation;
		try {
			
			JdbcTemplate jdbc = datastore.getJdbcConnection();
			jdbc.execute( new ConnectionCallback<Object>() {

				@Override
				public Object doInConnection(Connection conn)
						throws SQLException, DataAccessException {
					boolean oldValue = conn.getAutoCommit();
					try {
						conn.setAutoCommit(false);
						conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
						Statement stmt = conn.createStatement();
						for ( String s : stmts ) {
							// for debugging: LogFactory.getLog(TaskLockImpl.class).info(s);
							stmt.execute(s);
						}
						conn.commit();
					} catch (Exception e ) {
						e.printStackTrace();
						conn.rollback();
					} finally {
						Statement stmt = conn.createStatement();
						LogFactory.getLog(TaskLockImpl.class).info("UNLOCK TABLES");
						stmt.execute("UNLOCK TABLES");
						conn.commit();
					}
					conn.setAutoCommit(oldValue);
					return null;
				}
				
			});
		
			relation = TaskLockTable.assertRelation(datastore, user);
		} catch (Exception e) {
			throw new ODKTaskLockException(PERSISTENCE_LAYER_PROBLEM, e);
		}
		return (TaskLockTable) datastore.getEntity(relation, entity.getUri(), user);
	}
	
	@Override
	public boolean obtainLock(String lockId, String formId,
			ITaskLockType taskType) {
		boolean result = false;
		try {
			TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
			TaskLockTable entity = datastore.createEntityUsingRelation(relation, user);
			entity.setStringField(entity.primaryKey, lockId);
			entity.setFormId(formId);
			entity.setTaskType(taskType.getName());
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
	public boolean renewLock(String lockId, String formId, ITaskLockType taskType) {
	    boolean result = false;
	    try {
		    TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
		    TaskLockTable entity = datastore.getEntity(relation, lockId, user);
		    if ( !(entity.getFormId().equals(formId) && 
		    	   entity.getTaskType().equals(taskType.getName())) ) {
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
			ITaskLockType taskType) {
		boolean result = false;
		try {
			TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
			datastore.deleteEntity(new EntityKey(relation, lockId), user);
			result = true;
		} catch (ODKDatastoreException e) {
			// if we see a lot of these, we are running too long between renewals
			LogFactory.getLog(TaskLockImpl.class).info("delete of taskLock threw exception!");
			e.printStackTrace();
		}
		return result;
	}

	private static class TaskLockTable extends CommonFieldsBase {
		static final String TABLE_NAME = "_task_lock";

		private static final DataField FORM_ID = new DataField("FORM_ID",
				DataField.DataType.STRING, false, 4096L);
		private static final DataField TASK_TYPE = new DataField("TASK_TYPE",
				DataField.DataType.STRING, false, 80L);
		private static final DataField EXPIRATION_DATETIME = new DataField("EXPIRATION_DATETIME",
				DataField.DataType.DATETIME, true);

		DataField formId;
		DataField taskType;
		DataField expirationDateTime;

		TaskLockTable(String schema) {
			super(schema, TABLE_NAME);
			fieldList.add(formId = new DataField(FORM_ID));
			fieldList.add(taskType = new DataField(TASK_TYPE));
			fieldList.add(expirationDateTime = new DataField(EXPIRATION_DATETIME));
		}

		TaskLockTable(TaskLockTable ref, User user) {
			super(ref, user);
			formId = ref.formId;
			taskType = ref.taskType;
			expirationDateTime = ref.expirationDateTime;
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
		Date getExpirationDateTime() {
			return getDateField(expirationDateTime);
		}

		@SuppressWarnings("unused")
		void setExpirationDateTime(Date value) {
			setDateField(expirationDateTime, value);
		}

		@Override
		public CommonFieldsBase getEmptyRow(User user) {
			return new TaskLockTable(this, user);
		}

		static TaskLockTable relation = null;

		static synchronized final TaskLockTable assertRelation(
				Datastore datastore, User user) throws ODKDatastoreException {
			if (relation == null) {
				TaskLockTable relationPrototype;
				relationPrototype = new TaskLockTable(datastore
						.getDefaultSchemaName());
				datastore.assertRelation(relationPrototype, user);
				relation = relationPrototype;
			}
			return relation;
		}
	}
}
