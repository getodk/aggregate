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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.QueryResult;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.utils.WebUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

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
	
	/**
	 * Track the attributes that we are querying and sorting on...
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
	abstract class Tracker {
		final DataField attribute;
		
		Tracker(DataField attribute) {
			this.attribute = attribute;
		}
		
		DataField getAttribute() {
			return attribute;
		}
		
		public int simpleValueCompare(CommonFieldsBase b1, CommonFieldsBase b2) {
			Object value;
			switch ( attribute.getDataType() ) {
			default:
				throw new IllegalStateException("missing dataType implementation");
			case BINARY:
			case LONG_STRING:
				throw new IllegalStateException("should never filter on large objects (text or blob)");
			case STRING:
			case URI:
				value = b2.getStringField(attribute);
				break;
			case INTEGER:
				value = b2.getLongField(attribute);
				break;
			case DECIMAL:
				value = b2.getNumericField(attribute);
				break;
			case BOOLEAN:
				value = b2.getBooleanField(attribute);
				break;
			case DATETIME:
				value = b2.getDateField(attribute);
				break;
			}
			return compareField(b1, value);
		}

		<T extends Comparable<T>> int compareObjects(T b1, T b2) {
			if ( b1 == null ) {
				if ( b2 == null ) return 0;
				return 1; // nulls (==b2) appear last in ordering
			}
			if ( b2 == null ) return -1; // nulls (==b1) appear last in ordering 
			return b1.compareTo(b2);
		}
		
		int compareField(CommonFieldsBase record, Object value) {
			switch ( attribute.getDataType() ) {
			default:
				throw new IllegalStateException("missing dataType implementation");
			case BINARY:
			case LONG_STRING:
				throw new IllegalStateException("should never filter on large objects (text or blob)");
			case STRING:
			case URI:
				String eStr = record.getStringField(attribute);
				String vStr = (value == null) ? null : (String) value;
				return compareObjects(eStr, vStr);
			case INTEGER:
				Long eLong = record.getLongField(attribute);
				Long vLong;
				if ( value == null ) {
					vLong = null;
				} else if ( value instanceof Long ) {
					vLong = (Long) value;
				} else {
					vLong = Long.parseLong(value.toString());
				}
				return compareObjects(eLong, vLong);
			case DECIMAL:
				BigDecimal eDec = record.getNumericField(attribute);
				BigDecimal vDec;
				if ( value == null ) {
					vDec = null;
				} else if ( value instanceof BigDecimal ) {
					vDec = (BigDecimal) value;
				} else { 
					vDec = new BigDecimal(value.toString());
				}
				return compareObjects(eDec, vDec);
			case BOOLEAN:
				Boolean eBool = record.getBooleanField(attribute);
				Boolean vBool = (value == null) ? null : (Boolean) value;
				return compareObjects(eBool, vBool);
			case DATETIME:
				Date eDate = record.getDateField(attribute);
				Date vDate = (value == null) ? null : (Date) value;
				return compareObjects(eDate, vDate);
			}
		}

		abstract boolean passFilter(CommonFieldsBase record);
		
		abstract void setFilter(com.google.appengine.api.datastore.Query q);
	}
	
	class SimpleFilterTracker extends Tracker {
		final FilterOperation op;
		final Object value;
		
		SimpleFilterTracker( DataField attribute, FilterOperation op, Object value) {
			super(attribute);
			this.op = op;
			this.value = value;
		}
		
		@Override
		boolean passFilter(CommonFieldsBase record) {
			int result = compareField(record, value);
			switch ( op ) {
			case EQUAL:
				return result == 0;
			case LESS_THAN:
				return result < 0;
			case LESS_THAN_OR_EQUAL:
				return result <= 0;
			case GREATER_THAN:
				return result > 0;
			case GREATER_THAN_OR_EQUAL:
				return result >= 0;
			default:
				throw new IllegalStateException("missing a filter operation!");
			}
		}

		@Override
		void setFilter(com.google.appengine.api.datastore.Query q) {
			if ( attribute.getDataType() == DataType.DECIMAL ) {
				Double d = null;
				if ( value != null ) {
					if ( value instanceof BigDecimal ) {
						d = ((BigDecimal) value).doubleValue();
					} else {
						d = new BigDecimal(value.toString()).doubleValue();
					}
				}
				q.addFilter(attribute.getName(), operationMap.get(op), d);
			} else {
				q.addFilter(attribute.getName(), operationMap.get(op), value);
			}
		}
	}
	
	class ValueSetFilterTracker extends Tracker {
		final Collection<?> valueSet;
		
		ValueSetFilterTracker( DataField attribute, Collection<?> valueSet) {
			super(attribute);
			this.valueSet = valueSet;
		}

		@Override
		boolean passFilter(CommonFieldsBase record) {
			for ( Object o : valueSet ) {
				int result = compareField(record, o);
				if ( result == 0 ) return true;
			}
			return false;
		}

		@Override
		void setFilter(com.google.appengine.api.datastore.Query q) {
			if ( attribute.getDataType() == DataType.DECIMAL ) {
				Set<Double> dvSet = new HashSet<Double>();
				for ( Object value : valueSet ) {
					Double d = null;
					if ( value != null ) {
						if ( value instanceof BigDecimal ) {
							d = ((BigDecimal) value).doubleValue();
						} else {
							d = new BigDecimal(value.toString()).doubleValue();
						}
					}
					dvSet.add(d);
				}
				q.addFilter(attribute.getName(), FilterOperator.IN, dvSet);
			} else {
				q.addFilter(attribute.getName(), FilterOperator.IN, valueSet);
			}
		}
	}
	
	List<Tracker> filterList = new ArrayList<Tracker>();

	class SortTracker extends Tracker implements Comparator<CommonFieldsBase> {
		final Direction direction;
		
		SortTracker( DataField attribute, Direction direction ) {
			super(attribute);
			this.direction = direction;
		}

		@Override
		boolean passFilter(CommonFieldsBase record) {
			throw new IllegalStateException("not implemented");
		}
		
		@Override
		void setFilter(com.google.appengine.api.datastore.Query q) {
			throw new IllegalStateException("not implemented");
		}

		@Override
		public int compare(CommonFieldsBase o1, CommonFieldsBase o2) {
			if ( direction == Direction.ASCENDING ) {
				return simpleValueCompare(o1, o2);
			} else { 
				return -simpleValueCompare(o1, o2);
			}
		}
	}
	
	List<SortTracker> sortList = new ArrayList<SortTracker>();
	
	public QueryImpl(CommonFieldsBase relation, DatastoreImpl datastore, User user) {
		this.relation = relation;
		this.datastore = datastore;
		this.user = user;
	}

	@Override
	public void addFilter(DataField attribute, FilterOperation op, Object value) {
		// do everything locally except the first one (later)...
		filterList.add(new SimpleFilterTracker(attribute, op, value));
	}

	@Override
	public void addValueSetFilter(DataField attribute, Collection<?> valueSet) {
		// do everything locally except the first one (later)...
		filterList.add(new ValueSetFilterTracker(attribute, valueSet));
	}

	@Override
	public void addSort(DataField attribute, Direction direction) {
		// do the sort locally -- later...
		sortList.add(new SortTracker(attribute, direction));
	}

	private List<CommonFieldsBase> coreExecuteQuery(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException {
		List<CommonFieldsBase> odkEntities = new ArrayList<CommonFieldsBase>();

		DatastoreService ds = datastore.getDatastoreService();
		List<com.google.appengine.api.datastore.Entity> gaeEntities = null;
		
		if ( startCursor != null ) {
			DataField t = null;
			for ( DataField d : relation.getFieldList() ) {
				if ( d.getName().equals( startCursor.getAttributeName() ) ) {
					t = d;
					break;
				}
			}
			if ( t == null ) {
				throw new IllegalStateException("unable to find the matching attribute name");
			}
			Object value;
			String v = startCursor.getValue();
			switch ( t.getDataType() ) {
			case BINARY: throw new IllegalStateException("cannot sort on a binary field");
			case LONG_STRING: throw new IllegalStateException("cannot sort on a long text field");
			case URI:
			case STRING: {
				value = startCursor.getValue(); 
				break;
			}
			case INTEGER: {
				if ( v == null ) {
					value = null;
				} else {
					value = Long.valueOf(v);
				}
				break;
			}
			case DECIMAL: {
				if ( v == null ) {
					value = null;
				} else {
					value = new BigDecimal(v);
				}
				break;
			}
			case BOOLEAN: {
				if ( v == null ) {
					value = null;
				} else {
					value = WebUtils.parseBoolean(v);
				}
				break;
			}
			case DATETIME: {
				if ( v == null ) {
					value = null;
				} else {
					value = WebUtils.parseDate(v);
				}
				break;
			}
			default:
				throw new IllegalStateException("datatype not handled");
			}

			addFilter( t, startCursor.getOp(), value);
		}
		
		// regardless, always order by PK ascending...
		addSort( relation.primaryKey, Direction.ASCENDING);
		
		/**
		 * GAE 1.4.2 has changed the way it handles indices so that the actual
		 * query construction (prepareQuery) no longer throws a 
		 * DatastoreNeedIndexException, but, rather, that exception is thrown
		 * at the point where the cursor is accessed.
		 * 
		 * For now, just skip all multi-value querying and do the 
		 * filtering and sorting locally against the dataset returned
		 * by the first filter condition. 
		 */
		boolean filterAndSortLocally = true;
		
		if ( filterAndSortLocally) {
			gaeEntities = null;
			try {
				 Query hack = new com.google.appengine.api.datastore.Query(relation.getSchemaName() + "."
										+ relation.getTableName());
				 if ( filterList.size() > 0 ) {
					 Tracker t = filterList.get(0);
					 t.setFilter(hack);
					 // add any additional conditions on this same DataField
					 // e.g., for "between x and y" types of queries.
					 DataField f = t.getAttribute();
					 for ( int i = 1 ; i < filterList.size() ; ++i ) {
						 t = filterList.get(i);
						 if ( f.equals(t.getAttribute()) ) {
							 t.setFilter(hack);
						 }
					 }
				 }
				 PreparedQuery preparedHack = ds.prepare(hack);
				 gaeEntities = preparedHack.asList(FetchOptions.Builder.withDefaults());
			} catch ( Exception e ) {
				throw new ODKDatastoreException("Unable to complete request", e);
			}
		}

		try {
			// convert to odk entities
			EntityRowMapper m = new EntityRowMapper(relation, user);
			int idx = 0;
			for (com.google.appengine.api.datastore.Entity gaeEntity : gaeEntities) {
				CommonFieldsBase odkEntity;
				odkEntity = (CommonFieldsBase) m.mapRow(datastore, gaeEntity,
						idx++);
				if ( filterAndSortLocally ) {
					boolean passed = true;
					for ( Tracker t : filterList ) {
						if ( !t.passFilter(odkEntity) ) {
							passed = false;
							break;
						}
					}
					if ( passed ) {
						odkEntities.add(odkEntity);
					}
				} else {
					odkEntities.add(odkEntity);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
		if ( filterAndSortLocally ) {
			// OK. We have our list of results.  Now sort it...
			Collections.reverse(sortList); // stable sorts nest backwards...
			
			for ( SortTracker t : sortList ) {
				Collections.sort(odkEntities, t);
			}
			
			// process the list...
			List<CommonFieldsBase> finalEntities;
			if ( startCursor != null ) {
				finalEntities = new ArrayList<CommonFieldsBase>();
				boolean beforeUri = true;
				for ( CommonFieldsBase cb : odkEntities ) {
					if ( beforeUri ) {
						if ( startCursor.getUriLastReturnedValue().equals(cb.getUri()) ) {
							beforeUri = false;
						}
					} else {
						finalEntities.add(cb);
					}
				}
			} else {
				finalEntities = odkEntities;
			}
			
			if (fetchLimit != 0 ) {
				while ( finalEntities.size() > fetchLimit ) {
					finalEntities.remove(finalEntities.size()-1);
				}
			}
			odkEntities = finalEntities;
		}
		
		return odkEntities;
	}
	
	@Override
	public List<? extends CommonFieldsBase> executeQuery(int fetchLimit)
			throws ODKDatastoreException {
		return coreExecuteQuery(null, fetchLimit);
	}

	@Override
	public QueryResult executeQuery(
			QueryResumePoint startCursor, int fetchLimit)
			throws ODKDatastoreException {
		List<? extends CommonFieldsBase> results = coreExecuteQuery(startCursor, fetchLimit);
		if ( results.isEmpty() ) {
			return new QueryResult( startCursor, results, startCursor );
		}
		CommonFieldsBase cb = results.get(results.size()-1);
		SortTracker t = sortList.get(0);
		QueryResumePoint resumeCursor;
		String value;
		switch ( t.getAttribute().getDataType() ) {
		case BINARY: throw new IllegalStateException("cannot sort on a binary field");
		case LONG_STRING: throw new IllegalStateException("cannot sort on a long text field");
		case URI:
		case STRING: {
			value = cb.getStringField(t.getAttribute()); 
			break;
		}
		case INTEGER: {
			Long l = cb.getLongField(t.getAttribute());
			if ( l == null ) {
				value = null;
			} else {
				value = Long.toString(l);
			}
			break;
		}
		case DECIMAL: {
			BigDecimal bd = cb.getNumericField(t.getAttribute());
			if ( bd == null ) {
				value = null;
			} else {
				value = bd.toString();
			}
			break;
		}
		case BOOLEAN: {
			Boolean b = cb.getBooleanField(t.getAttribute());
			if ( b == null ) {
				value = null;
			} else {
				value = b.toString();
			}
			break;
		}
		case DATETIME: {
			Date d = cb.getDateField(t.getAttribute());
			if ( d == null ) {
				value = null;
			} else {
				value = WebUtils.iso8601Date(d);
			}
			break;
		}
		default:
			throw new IllegalStateException("datatype not handled");
		}
		
		if (t.direction == org.opendatakit.common.persistence.Query.Direction.ASCENDING ) {
			resumeCursor = new QueryResumePoint( t.getAttribute().toString(), FilterOperation.GREATER_THAN_OR_EQUAL, value, cb.getUri());
		} else {
			resumeCursor = new QueryResumePoint( t.getAttribute().toString(), FilterOperation.LESS_THAN_OR_EQUAL, value, cb.getUri());
		}
		return new QueryResult( startCursor, results, resumeCursor );
	}

	@Override
	public Set<EntityKey> executeForeignKeyQuery(
			CommonFieldsBase topLevelTable, DataField topLevelAuri )
			throws ODKDatastoreException {

		Set<EntityKey> keySet = new HashSet<EntityKey>();

		List<? extends CommonFieldsBase> entities = coreExecuteQuery(null, 0);
		for ( CommonFieldsBase entity : entities ) {
			keySet.add( new EntityKey( topLevelTable, entity.getStringField(topLevelAuri)));
		}
		return keySet;
	}

	@Override
	public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
		Set<Object> valueList = new HashSet<Object>();

		List<? extends CommonFieldsBase> entities = coreExecuteQuery(null, 0);
		for ( CommonFieldsBase entity : entities ) {
			switch ( dataField.getDataType() ) {
			case BINARY:
				throw new IllegalStateException("unsupported fetch of binary data");
			case BOOLEAN:
				valueList.add( entity.getBooleanField(dataField) );
				break;
			case DATETIME:
				valueList.add( entity.getDateField(dataField) );
				break;
			case DECIMAL:
				valueList.add( entity.getNumericField(dataField) );
				break;
			case INTEGER:
				valueList.add( entity.getLongField(dataField) );
				break;
			case LONG_STRING:
			case STRING:
			case URI:
				valueList.add( entity.getStringField(dataField) );
				break;
			}
		}
		return new ArrayList<Object>(valueList);
	}
}
