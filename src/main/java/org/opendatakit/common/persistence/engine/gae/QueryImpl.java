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
import org.opendatakit.common.persistence.engine.EngineUtils;
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

		public Comparator<Object> getComparator() {
			return new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					int sense = ( direction == Direction.ASCENDING ) ? 1 : -1;
					switch ( attribute.getDataType() ) {
					default:
						throw new IllegalStateException("missing dataType implementation");
					case BINARY:
					case LONG_STRING:
						throw new IllegalStateException("should never filter on large objects (text or blob)");
					case STRING:
					case URI:
						String s1 = (o1 == null) ? null : (String) o1;
						String s2 = (o2 == null) ? null : (String) o2;
						return sense * compareObjects(s1, s2);
					case INTEGER:
						Long l1 = (o1 == null) ? null : (Long) o1;
						Long l2 = (o2 == null) ? null : (Long) o2;
						return sense * compareObjects(l1, l2);
					case DECIMAL:
						BigDecimal bd1 = (o1 == null) ? null : (BigDecimal) o1;
						BigDecimal bd2 = (o2 == null) ? null : (BigDecimal) o2;
						return sense * compareObjects(bd1, bd2);
					case BOOLEAN:
						Boolean b1 = (o1 == null) ? null : (Boolean) o1;
						Boolean b2 = (o2 == null) ? null : (Boolean) o2;
						return sense * compareObjects(b1, b2);
					case DATETIME:
						Date d1 = (o1 == null) ? null : (Date) o1;
						Date d2 = (o2 == null) ? null : (Date) o2;
						return sense * compareObjects(d1, d2);
					}
				}
			};
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

	private class CoreResult {
		final List<CommonFieldsBase> results;
		final boolean hasMoreResults;
		
		CoreResult(List<CommonFieldsBase> results, boolean hasMoreResults ) {
			this.results = results;
			this.hasMoreResults = hasMoreResults;
		}
	}
	
	private CoreResult coreExecuteQuery(QueryResumePoint startCursor, int fetchLimit) throws ODKDatastoreException {
		DatastoreService ds = datastore.getDatastoreService();
		
		// get the dominant sort definition
		 if ( sortList.size() == 0 ) {
			 throw new IllegalStateException("expected at least one sort criteria");
		 }
		 
		 // this is the dominant sort:
		 SortTracker dominantSort = sortList.get(0);
		 DataField dominantSortAttr = dominantSort.getAttribute();
		
		// if we don't have any sort on the PK, add one
		// direction of PK sort matches that of dominant sort
		boolean isUriSortAlreadyPresent = false;
		for ( SortTracker st : sortList ) {
			if ( st.attribute.equals(relation.primaryKey) ) {
				isUriSortAlreadyPresent = true;
				break;
			}
		}
		
		if ( !isUriSortAlreadyPresent ) {
			// direction of PK sort matches that of dominant sort
			// 
			// NOTE: if a PK sort is already defined, it is up to the
			// caller to alter its sense in a new query when using
			// a resume point to fetch the records preceding
			// that resume point.
			addSort( relation.primaryKey, dominantSort.direction);
		}

		// we allow the same query to be executed multiple times with different
		// start cursors (resume points).  To do that, we maintain the additional
		// start cursor filter separately from the caller-specified list of 
		// query filters.
		SimpleFilterTracker startCursorFilter = null;
		
		if ( startCursor != null ) {
			DataField matchingStartCursorAttr = null;
			for ( DataField d : relation.getFieldList() ) {
				if ( d.getName().equals( startCursor.getAttributeName() ) ) {
					matchingStartCursorAttr = d;
					break;
				}
			}
			if ( matchingStartCursorAttr == null ) {
				throw new IllegalStateException("unable to find the matching attribute name " +
						"for dominant sort attribute in start cursor: " + startCursor.getAttributeName());
			}
			
			if ( !matchingStartCursorAttr.equals(dominantSortAttr)) {
				// the dominant sort column is different 
				// -- the start cursor is not appropriate for this query.
				throw new IllegalStateException("start cursor is inappropriate for query");
			}

			Object value = EngineUtils.getDominantSortAttributeValueFromString(startCursor.getValue(), dominantSortAttr);
			
			startCursorFilter = new SimpleFilterTracker( dominantSortAttr,
					dominantSort.direction.equals(Direction.ASCENDING) ?
							FilterOperation.GREATER_THAN_OR_EQUAL :
							FilterOperation.LESS_THAN_OR_EQUAL, value); 
		}
		
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

		Iterable<com.google.appengine.api.datastore.Entity> it = null;
		try {
			 Query hack = new com.google.appengine.api.datastore.Query(relation.getSchemaName() + "."
									+ relation.getTableName());
			 
			 // apply the dominant column sort (will be by PK if no user-supplied sort).
			 SortDirection sd = dominantSort.direction.equals(Direction.ASCENDING) ? SortDirection.ASCENDING : SortDirection.DESCENDING;
			 hack.addSort(dominantSortAttr.getName(), sd);
			 // apply the startCursor filter
			 if ( startCursorFilter != null ) {
				 startCursorFilter.setFilter(hack);
			 }
			 // add any filter conditions on the dominant sort attribute.
			 // e.g., for "between x and y" types of queries.
			 for ( Tracker t : filterList ) {
				 if ( dominantSortAttr.equals(t.getAttribute()) ) {
					 t.setFilter(hack);
				 }
			 }

			 // Since we are filtering locally, we need to grab a chunk of values
			 // in the expectation that most will fail the filter.
			 PreparedQuery preparedHack = ds.prepare(hack);
			 int chunkSize = (fetchLimit == 0) ? 4096 : Math.max(fetchLimit+4, 4096);
			 it = preparedHack.asIterable(FetchOptions.Builder.withDefaults().chunkSize(chunkSize).prefetchSize(chunkSize));
			 
		} catch ( Exception e ) {
			throw new ODKDatastoreException("Unable to complete request", e);
		}

		List<CommonFieldsBase> odkEntities = new ArrayList<CommonFieldsBase>();

		try {
			// convert to odk entities
			EntityRowMapper m = new EntityRowMapper(relation, user);
			int idx = 0; // for logging and debugging only...
			CommonFieldsBase odkLastEntity = null;
			// since the subordinate sorts rearrange the data sharing the same
			// dominantSort attribute value, we must gather all matching start
			// values then all values up to the fetchLimit, then all matching 
			// end values and then one more record to determine if there are 
			// additional records.
			boolean possiblyBeforeStartCursor = (startCursor != null);
			int sizeQuestionableFirstMatches = 0;
			
			for (com.google.appengine.api.datastore.Entity gaeEntity : it) {
				CommonFieldsBase odkEntity =
					(CommonFieldsBase) m.mapRow(datastore, gaeEntity, idx++);
				boolean passed = true;
				for ( Tracker t : filterList ) {
					if ( !t.passFilter(odkEntity) ) {
						passed = false;
						break;
					}
				}
				if ( passed ) {
					if ( fetchLimit == 0 ) {
						// grab everything until we have nothing
						odkEntities.add(odkEntity);
						odkLastEntity = odkEntity;
					} else if ( possiblyBeforeStartCursor ) {
						// we are just starting to process the result set.
						// This candidate may be positioned before or after the last record
						// returned from the previous set (depending upon how the subordinate
						// sorts rearrange the result set).
						
						if ( odkLastEntity == null ) {
							// bootstrap... this is the very first entity
							odkEntities.add(odkEntity);
							odkLastEntity = odkEntity;
						} else {
							// see if the dominantSort attribute values of the current entity and the prior one match.
							boolean matchingDominantAttr = EngineUtils.hasMatchingDominantSortAttribute( odkLastEntity, odkEntity, dominantSortAttr);
							
							if ( !matchingDominantAttr ) {
								// we have our first for-sure element (it has a dominantSort
								// attribute value that is different from the value of the start cursor).
								// remember the number of questionable elements
								possiblyBeforeStartCursor = false;
								sizeQuestionableFirstMatches = odkEntities.size();
							}
							
							// add the match...
							odkEntities.add(odkEntity);
							odkLastEntity = odkEntity;
						}
					} else if ( odkEntities.size() <= fetchLimit+sizeQuestionableFirstMatches+1 ) {
						// get at least fetchLimit+1 in order to determine 
						// if there are more records.
						odkEntities.add(odkEntity);
						odkLastEntity = odkEntity;
					} else {
						// see if we have gotten enough and can do the subordinate sorting.
						// Because of the possible existence of subordinate sorts (there 
						// will always be subordinate sorts unless the dominant sort is 
						// against the PK), we must read all data matching the last value
						// of the dominantSort attribute.  The subordinate sorts will 
						// rearrange those values so we cannot just truncate after N of them.
						// This is also the reason we need sizeQuestionableFirstMatches
						// and its associated logic.
						//
						boolean matchingDominantAttr = EngineUtils.hasMatchingDominantSortAttribute( odkLastEntity, odkEntity, dominantSortAttr);
						if ( matchingDominantAttr ) {
							odkEntities.add(odkEntity);
						} else {
							// we don't need to gather any more records -- exit loop...
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ODKDatastoreException(e);
		}
		
		// OK. We have our list of results.  Now sort it...
		// Stable sorts nest backwards, so we do this by 
		// applying the sorts in reverse order from their definitions.
		//
		// NOTE: since executions may be called repeatedly on the same
		// query, we do not want to in-place reverse or otherwise 
		// alter the sortList.
		for ( int i = sortList.size()-1; i >= 0 ; --i ) {
			Collections.sort(odkEntities, sortList.get(i));
		}
		
		// process the list...
		boolean hasMoreResults = false;
		List<CommonFieldsBase> finalEntities = new ArrayList<CommonFieldsBase>();
		boolean beforeUri = (startCursor != null);
		for ( CommonFieldsBase cb : odkEntities ) {
			if ( beforeUri ) {
				if ( startCursor.getUriLastReturnedValue().equals(cb.getUri()) ) {
					beforeUri = false;
				}
			} else if ( fetchLimit == 0 || finalEntities.size() < fetchLimit ) {
				finalEntities.add(cb);
			} else {
				hasMoreResults = true;
				break;
			}
		}

		return new CoreResult(finalEntities, hasMoreResults);
	}

	@Override
	public QueryResult executeQuery(
			QueryResumePoint startCursor, int fetchLimit)
			throws ODKDatastoreException {
		CoreResult r = coreExecuteQuery(startCursor, fetchLimit);
		
		// quick exit -- empty set
		if ( r.results.isEmpty() ) {
			return new QueryResult( startCursor, r.results, null, startCursor, false );
		}
		// otherwise, we need to get the values of the dominantAttr and uri of the last field.
		DataField dominantSortAttr = sortList.get(0).getAttribute();

		// otherwise, we need to get the values of the dominantAttr and uri of the last field.
		CommonFieldsBase cb;
		String value;
		// determine the resume cursor...
		cb = r.results.get(r.results.size()-1);
		value = EngineUtils.getDominantSortAttributeValueAsString(cb, dominantSortAttr);
		QueryResumePoint resumeCursor = new QueryResumePoint( dominantSortAttr.getName(), value, cb.getUri());
		// determine the backward cursor...
		cb = r.results.get(0);
		value = EngineUtils.getDominantSortAttributeValueAsString(cb, dominantSortAttr);
		QueryResumePoint backwardCursor = new QueryResumePoint( dominantSortAttr.getName(), value, cb.getUri());
		
		return new QueryResult( startCursor, r.results, backwardCursor, resumeCursor, r.hasMoreResults );
	}
	
	@Override
	public List<? extends CommonFieldsBase> executeQuery()
			throws ODKDatastoreException {

		// Ensure at least one dominant sort is applied to the result set. 
		// This allows the methods that return all matches to leverage the
		// core query execution logic.
		
		if ( sortList.isEmpty() ) {
			if ( filterList.isEmpty() ) {
				// use primary key, as we know that is never null.
				addSort( relation.primaryKey, Direction.ASCENDING );
			} else {
				// we want to sort by whatever the first filter 
				// criteria is.  The callers of the fetch-all 
				// methods should be applying the filters in an
				// order that maximally excludes records so we
				// want to pass the first filter down to the GAE
				// layer.  Get that to happen by sorting along that
				// filter dimension (which will pass down to GAE the
				// sort directive and the filters for that column).
				Tracker t = filterList.get(0);
				addSort( t.getAttribute(), Direction.ASCENDING );
			}
		}
		CoreResult result = coreExecuteQuery(null, 0);
		return result.results;
	}

	@Override
	public Set<EntityKey> executeForeignKeyQuery(
			CommonFieldsBase topLevelTable, DataField topLevelAuri )
			throws ODKDatastoreException {

		List<?> keys = executeDistinctValueForDataField(topLevelAuri);

		Set<EntityKey> keySet = new HashSet<EntityKey>();
		for (Object o : keys) {
			String key = (String) o;
			// we don't have the top level records themselves.  Construct the entity keys
			// from the supplied relation and the value of the AURI fields in the records
			// we do have.
			keySet.add(new EntityKey(topLevelTable, key));
		}
		return keySet;
	}

	@Override
	public List<?> executeDistinctValueForDataField(DataField dataField) throws ODKDatastoreException {
		Set<Object> uniqueValueSet = new HashSet<Object>();
		
		// use a cursor, since we have to bring everything into memory...
		// this means we need to have at least one sort criteria in place.
		if ( sortList.isEmpty() ) {
			if ( filterList.isEmpty() ) {
				// use primary key, as we know that is never null.
				addSort( relation.primaryKey, Direction.ASCENDING );
			} else {
				// we want to sort by whatever the first filter 
				// criteria is.  The callers of the fetch-all 
				// methods should be applying the filters in an
				// order that maximally excludes records so we
				// want to pass the first filter down to the GAE
				// layer.  Get that to happen by sorting along that
				// filter dimension (which will pass down to GAE the
				// sort directive and the filters for that column).
				Tracker t = filterList.get(0);
				addSort( t.getAttribute(), Direction.ASCENDING );
			}
		}
		QueryResumePoint startCursor = null;
		
		for (;;) {
			QueryResult r = executeQuery(startCursor, 4096);
			startCursor = r.getResumeCursor();
			
			if ( r.getResultList().isEmpty() ) break;
			
			for ( CommonFieldsBase entity : r.getResultList() ) {
				switch ( dataField.getDataType() ) {
				case BINARY:
				case LONG_STRING:
					throw new IllegalStateException("unsupported fetch of binary data");
				case BOOLEAN:
					uniqueValueSet.add( entity.getBooleanField(dataField) );
					break;
				case DATETIME:
					uniqueValueSet.add( entity.getDateField(dataField) );
					break;
				case DECIMAL:
					uniqueValueSet.add( entity.getNumericField(dataField) );
					break;
				case INTEGER:
					uniqueValueSet.add( entity.getLongField(dataField) );
					break;
				case STRING:
				case URI:
					uniqueValueSet.add( entity.getStringField(dataField) );
					break;
				}
			}
		}
		
		List<Object> values = new ArrayList<Object>(uniqueValueSet);
		
		// and apply the sorting, if any, appropriate to this dataField
		for ( int i = sortList.size()-1 ; i >= 0 ; --i ) {
			SortTracker t = sortList.get(i);
			if ( t.getAttribute().equals(dataField) ) {
				Collections.sort(values, t.getComparator());
			}
		}
		return values;
	}
}
