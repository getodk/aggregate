/**
 * Copyright (C) 2010 University of Washington
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DynamicAssociationBase;
import org.opendatakit.common.persistence.DynamicBase;
import org.opendatakit.common.persistence.DynamicDocumentBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryImpl implements org.opendatakit.common.persistence.Query {

	private static Map<FilterOperation, FilterOperator> operationMap = new HashMap<FilterOperation, FilterOperator>();

	static {
		operationMap.put(FilterOperation.EQUAL, FilterOperator.EQUAL);
		operationMap.put(FilterOperation.GREATER_THAN,
				FilterOperator.GREATER_THAN);
		operationMap.put(FilterOperation.GREATER_THAN_OR_EQUAL,
				FilterOperator.GREATER_THAN_OR_EQUAL);
		operationMap.put(FilterOperation.LESS_THAN, FilterOperator.LESS_THAN);
		operationMap.put(FilterOperation.LESS_THAN_OR_EQUAL,
				FilterOperator.LESS_THAN_OR_EQUAL);
	}

	private final CommonFieldsBase relation;
	private final DatastoreImpl datastore;
	private final User user;
	private final Query query;


	public QueryImpl(CommonFieldsBase relation, DatastoreImpl datastore, User user) {
		this.relation = relation;
		this.datastore = datastore;
		this.user = user;
		query = new Query(relation.getSchemaName() + "."
				+ relation.getTableName());
	}

	@Override
	public void addFilter(DataField attribute, FilterOperation op, Object value) {
		query.addFilter(attribute.getName(), operationMap.get(op), value);
	}

	@Override
	public void addValueSetFilter(DataField attribute, Set<?> valueSet) {
		query.addFilter(attribute.getName(), FilterOperator.IN, valueSet);
	}

	@Override
	public void addSort(DataField attribute, Direction direction) {
		if (direction.equals(Direction.ASCENDING)) {
			query.addSort(attribute.getName(), SortDirection.ASCENDING);
		} else {
			query.addSort(attribute.getName(), SortDirection.DESCENDING);
		}
	}

	@Override
	public List<? extends CommonFieldsBase> executeQuery(int fetchLimit)
			throws ODKDatastoreException {
		DatastoreService ds = datastore.getDatastoreService();
		PreparedQuery preparedQuery = ds.prepare(query);
		List<com.google.appengine.api.datastore.Entity> gaeEntities;
		if ( fetchLimit == 0 ) {
			gaeEntities = preparedQuery.asList(FetchOptions.Builder.withDefaults());
		} else {
			gaeEntities = preparedQuery.asList(FetchOptions.Builder.withLimit(fetchLimit));
		}
		List<CommonFieldsBase> odkEntities = new ArrayList<CommonFieldsBase>();

		try {
			// convert to odk entities
			EntityRowMapper m = new EntityRowMapper(relation, user);
			int idx = 0;
			for (com.google.appengine.api.datastore.Entity gaeEntity : gaeEntities) {
				CommonFieldsBase odkEntity;
				odkEntity = (CommonFieldsBase) m.mapRow(datastore, gaeEntity,
						idx++);
				odkEntities.add(odkEntity);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}

		return odkEntities;
	}

	@Override
	public Set<EntityKey> executeTopLevelKeyQuery(
			CommonFieldsBase topLevelTable, int fetchLimit)
			throws ODKDatastoreException {

		DatastoreService ds = datastore.getDatastoreService();
		PreparedQuery preparedQuery = ds.prepare(query);
		List<com.google.appengine.api.datastore.Entity> gaeEntities;
		if ( fetchLimit == 0 ) {
			gaeEntities = preparedQuery.asList(FetchOptions.Builder.withDefaults());
		} else {
			gaeEntities = preparedQuery.asList(FetchOptions.Builder.withLimit(fetchLimit));
		}
		DataField topLevelAuri = null;
		if ( relation instanceof DynamicAssociationBase ) {
			topLevelAuri = ((DynamicAssociationBase) relation).topLevelAuri;
		} else if ( relation instanceof DynamicDocumentBase ) {
			topLevelAuri = ((DynamicDocumentBase) relation).topLevelAuri;
		} else if ( relation instanceof DynamicBase ) {
			topLevelAuri = ((DynamicBase) relation).topLevelAuri;
		} else {
			throw new IllegalStateException("unexpected persistence backing object type");
		}

		Set<EntityKey> keySet = new HashSet<EntityKey>();
		try {
			// convert to odk entities
			EntityRowMapper m = new EntityRowMapper(relation, user);
			int idx = 0;
			for (com.google.appengine.api.datastore.Entity gaeEntity : gaeEntities) {
				CommonFieldsBase odkEntity;
				odkEntity = (CommonFieldsBase) m.mapRow(datastore, gaeEntity,
						idx++);
				keySet.add( new EntityKey( topLevelTable, odkEntity.getStringField(topLevelAuri)));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
		return keySet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
		// TODO Auto-generated method stub

		DatastoreService ds = datastore.getDatastoreService();
		PreparedQuery preparedQuery = ds.prepare(query);
		List<com.google.appengine.api.datastore.Entity> gaeEntities;
		gaeEntities = preparedQuery.asList(FetchOptions.Builder.withDefaults());
		Set valueList = new HashSet();
		try {
			// convert to odk entities
			EntityRowMapper m = new EntityRowMapper(relation, user);
			int idx = 0;
			for ( com.google.appengine.api.datastore.Entity gaeEntity : gaeEntities) {
				CommonFieldsBase odkEntity;
					odkEntity = (CommonFieldsBase) m.mapRow(datastore, gaeEntity, idx++);
				switch ( dataField.getDataType() ) {
				case BINARY:
					throw new IllegalStateException("unsupported fetch of binary data");
				case BOOLEAN:
					valueList.add( odkEntity.getBooleanField(dataField) );
					break;
				case DATETIME:
					valueList.add( odkEntity.getDateField(dataField) );
					break;
				case DECIMAL:
					valueList.add( odkEntity.getNumericField(dataField) );
					break;
				case INTEGER:
					valueList.add( odkEntity.getLongField(dataField) );
					break;
				case LONG_STRING:
				case STRING:
				case URI:
					valueList.add( odkEntity.getStringField(dataField) );
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
		return new ArrayList(valueList);
	}
}
