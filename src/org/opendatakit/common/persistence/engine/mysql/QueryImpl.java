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
package org.opendatakit.common.persistence.engine.mysql;

import java.util.ArrayList;
import java.util.Collection;
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
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.springframework.jdbc.core.RowMapper;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryImpl implements Query {

	private static final String K_SELECT = "SELECT ";
	private static final String K_SELECT_DISTINCT = "SELECT DISTINCT ";
	private static final String K_BQ = "`";
	private static final String K_CS = ", ";
	private static final String K_FROM = " FROM ";
	private static final String K_WHERE = " WHERE ";
	private static final String K_AND = " AND ";
	private static final String K_IN_OPEN = " IN (";
	private static final String K_IN_CLOSE = ")";
	private static final String K_BIND_VALUE = " ? ";
	private static final String K_ORDER_BY = " ORDER BY ";

	private static Map<FilterOperation, String> operationMap = new HashMap<FilterOperation, String>();
	private static Map<Direction, String> directionMap = new HashMap<Direction, String>();

	static {
		operationMap.put(FilterOperation.EQUAL, " = ");
		operationMap.put(FilterOperation.GREATER_THAN, " > ");
		operationMap.put(FilterOperation.GREATER_THAN_OR_EQUAL, " >= ");
		operationMap.put(FilterOperation.LESS_THAN, " < ");
		operationMap.put(FilterOperation.LESS_THAN_OR_EQUAL, " <= ");

		directionMap.put(Direction.ASCENDING, " ASC ");
		directionMap.put(Direction.DESCENDING, " DESC ");
	}

	private final CommonFieldsBase relation;
	private final DatastoreImpl dataStoreImpl;
	private final User user;

	private final StringBuilder queryBindBuilder = new StringBuilder();
	private final List<Object> bindValues = new ArrayList<Object>();
	private final StringBuilder querySortBuilder = new StringBuilder();

	public QueryImpl(CommonFieldsBase tableDefn, DatastoreImpl dataStoreImpl, User user) {
		this.relation = tableDefn;
		this.dataStoreImpl = dataStoreImpl;
		this.user = user;
	}

	private String generateQuery() {
		// generate the query
		StringBuilder baseQueryBuilder = new StringBuilder();

		baseQueryBuilder.append(K_SELECT);

		boolean first = true;
		for (DataField f : relation.getFieldList()) {
			if (!first) {
				baseQueryBuilder.append(K_CS);
			}
			first = false;
			baseQueryBuilder.append(K_BQ);
			baseQueryBuilder.append(f.getName());
			baseQueryBuilder.append(K_BQ);
		}
		baseQueryBuilder.append(K_FROM);
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(relation.getSchemaName());
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(".");
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(relation.getTableName());
		baseQueryBuilder.append(K_BQ);
		
		return baseQueryBuilder.toString();
	}

	private String generateDistinctFieldValueQuery(DataField dataField) {
		if (!relation.getFieldList().contains(dataField)) {
			throw new IllegalStateException(
					"Attempting to retrieve non-existent data field " 
						+ dataField.getName() + " from "
						+ relation.getSchemaName() + "."
						+ relation.getTableName());
		}

		StringBuilder baseQueryBuilder = new StringBuilder();
		// generate the query
		baseQueryBuilder.append(K_SELECT_DISTINCT);
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(dataField.getName());
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(K_FROM);
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(relation.getSchemaName());
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(".");
		baseQueryBuilder.append(K_BQ);
		baseQueryBuilder.append(relation.getTableName());
		baseQueryBuilder.append(K_BQ);
		
		return baseQueryBuilder.toString();
	}

	@Override
	public void addFilter(DataField attributeName, FilterOperation op,
			Object value) {
		if (queryBindBuilder.length() == 0) {
			queryBindBuilder.append(K_WHERE);
		} else {
			queryBindBuilder.append(K_AND);
		}
		queryBindBuilder.append(K_BQ);
		queryBindBuilder.append(attributeName.getName());
		queryBindBuilder.append(K_BQ);
		queryBindBuilder.append(operationMap.get(op));
		queryBindBuilder.append(K_BIND_VALUE);
		bindValues.add(value);
	}

	@Override
	public void addValueSetFilter(DataField attributeName, Collection<?> valueSet) {
		if ( queryBindBuilder.length() == 0 ) {
			queryBindBuilder.append(K_WHERE);
		} else {
			queryBindBuilder.append(K_AND);
		}
		queryBindBuilder.append(K_BQ);
		queryBindBuilder.append(attributeName.getName());
		queryBindBuilder.append(K_BQ);
		queryBindBuilder.append(K_IN_OPEN);
		boolean first = true;
		for ( Object o : valueSet ) {
			if ( !first ) {
				queryBindBuilder.append(K_CS);
			}
			first = false;
			queryBindBuilder.append(K_BIND_VALUE);
			bindValues.add(o);
		}
		queryBindBuilder.append(K_IN_CLOSE);
	}

	@Override
	public void addSort(DataField attributeName, Direction direction) {
		if (querySortBuilder.length() == 0) {
			querySortBuilder.append(K_ORDER_BY);
		} else {
			querySortBuilder.append(K_CS);
		}
		querySortBuilder.append(K_BQ);
		querySortBuilder.append(attributeName.getName());
		querySortBuilder.append(K_BQ);
		querySortBuilder.append(directionMap.get(direction));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends CommonFieldsBase> executeQuery(int fetchLimit)
			throws ODKDatastoreException {
		String query = generateQuery() 
				+ queryBindBuilder.toString()
				+ querySortBuilder.toString() + ";";
		RowMapper rowMapper = null;
		rowMapper = new RelationRowMapper(relation, user);

		try {
			return (List<? extends CommonFieldsBase>) dataStoreImpl
				.getJdbcConnection()
				.query(query, bindValues.toArray(), rowMapper);
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
	}

	@Override
	public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
		
		String query = generateDistinctFieldValueQuery(dataField) 
				+ queryBindBuilder.toString()
				+ querySortBuilder.toString() + ";";

		List<?> keys = null;
		try {
			keys = dataStoreImpl.getJdbcConnection().queryForList(query,
				bindValues.toArray(), String.class);
		} catch ( Exception e ) {
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
		return keys;
	}

	@Override
	public Set<EntityKey> executeTopLevelKeyQuery(
			CommonFieldsBase topLevelTable)
			throws ODKDatastoreException {

		DataField topLevelAuri = null;
		if ( relation instanceof TopLevelDynamicBase ) {
			topLevelAuri = ((TopLevelDynamicBase) relation).primaryKey;
		} else if ( relation instanceof DynamicAssociationBase ) {
			topLevelAuri = ((DynamicAssociationBase) relation).topLevelAuri;
		} else if ( relation instanceof DynamicDocumentBase ) {
			topLevelAuri = ((DynamicDocumentBase) relation).topLevelAuri;
		} else if ( relation instanceof DynamicBase ) {
			topLevelAuri = ((DynamicBase) relation).topLevelAuri;
		} else {
			throw new IllegalStateException("unexpected persistence backing object type");
		}

		List<?> keys = executeDistinctValueForDataField(topLevelAuri);

		Set<EntityKey> keySet = new HashSet<EntityKey>();
		for (Object o : keys) {
			String key = (String) o;
			keySet.add(new EntityKey(topLevelTable, key));
		}
		return keySet;
	}
}
