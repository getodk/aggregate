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
package org.opendatakit.aggregate.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;

/**
 * When a form is uploaded, the names for the columns and tables representing the form
 * need to be determined.  This is done in a three-step process.  First, information about
 * the name is gathered (schema, prefix, qualifier, item) that will be used in 
 * constructing the full name, that data is stored in a {@link Name} object, and a 
 * unique placeholder string is returned to the caller.  Second, once all names have been 
 * entered, unique resolved names are determined for each {@link Name} in the set.  This
 * ensures that two data items, perhaps in different non-repeating subgroups, don't
 * accidentally get mapped to the same column in the table.  Third, a sweep by the 
 * caller then replaces the unique placeholders with the appropriate resolved name.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
final class NamingSet {
	private static final String DROP_CHARS = "AEIOUY";

	private final Map<String,Name> tablePlaceholders = new HashMap<String,Name>();
	private final Map<String,Map<String,Name>> columnPlaceholders = new HashMap<String,Map<String,Name>>();
	private final Set<String> uniqueTables = new HashSet<String>();
	
	public NamingSet() {}

	/**
	 * Record information about a table and obtain the placeholder string for it.
	 * 
	 * @param schema
	 * @param prefix
	 * @param qualifier
	 * @param itemName
	 * @return placeholder string for this table.
	 */
	public final String getTableName(String schema, String prefix, String qualifier, String itemName ) {
		String placeholder = UUID.randomUUID().toString();
		
		tablePlaceholders.put(placeholder, new Name(schema, prefix, qualifier, itemName));
		return placeholder;
	}
	
	/**
	 * Record information about a column in a table and obtain a placeholder string for it.
	 * @param tablePlaceholder
	 * @param qualifier
	 * @param itemName
	 * @return placeholder string for this column.
	 */
	public final String getColumnName(String tablePlaceholder, String qualifier, String itemName ) {
		String placeholder = UUID.randomUUID().toString();

		Map<String,Name> m = columnPlaceholders.get(tablePlaceholder);
		if ( m == null ) {
			m = new HashMap<String,Name>();
			columnPlaceholders.put(tablePlaceholder, m);
		}
		m.put(placeholder, new Name(null, "", qualifier, itemName));
		return placeholder;
	}
	
	/**
	 * Called prior to resolving column names if a column placeholder was 
	 * defined but actually doesn't exist (e.g., geopoint, binary or choice).
	 * 
	 * @param tablePlaceholder
	 * @param columnPlaceholder
	 */
	public final void removeColumnName( String tablePlaceholder, String columnPlaceholder) {
		Map<String,Name> m = columnPlaceholders.get(tablePlaceholder);
		if ( m != null ) {
			m.remove(columnPlaceholder);
		}
	}

	/**
	 * Step two -- resolve the table and column names in the form.
	 * 
	 * @param ds
	 * @param user
	 */
	public void resolveNames(Datastore ds, User user) {
		resolveTableNames(ds, user);
		resolveColumnNames(ds.getMaxLenColumnName());
	}
	
	/**
	 * Resolve the table names.  This requires asking the persistence
	 * layer if the table already exists.  We are creating tables for 
	 * a new form, so it should not stomp on any existing tables.
	 * 
	 * @param ds
	 * @param user
	 */
	private void resolveTableNames(Datastore ds, User user) {
		int maxLenTableName = ds.getMaxLenTableName();
		// resolve the table names
		
		// construct a map of qualifier -to- max item name length
		// for each different qualifier of table names.
		int maxTblPrefix = 0;
		int maxQualLen = 0;
		int maxItemName = 0;
		Map<String, Integer> qualMaxName = new HashMap<String, Integer>();
		for ( Map.Entry<String, Name> tbl : tablePlaceholders.entrySet() ) {
			Name nm = tbl.getValue();
			int len = nm.itemName.length();
			String qualifier = nm.qualifier;
			Integer curMax = qualMaxName.get(qualifier);
			if ( curMax == null || curMax < len ) {
				qualMaxName.put(qualifier, len);
			}
			if ( len > maxItemName ) {
				maxItemName = len;
			}
			if ( qualifier.length() > maxQualLen ) {
				maxQualLen = qualifier.length();
			}
			if ( nm.prefix.length() > maxTblPrefix ) {
				maxTblPrefix = nm.prefix.length();
			}
		}
		
		int fixTblPrefixLen = maxTblPrefix;
		int fixTblQualLen = maxQualLen;
		int dashCount = 0;
		if ( fixTblPrefixLen > 0 ) ++dashCount;
		if ( fixTblQualLen > 0 ) ++dashCount;
		int fixTblItemLen = maxLenTableName - fixTblPrefixLen - fixTblQualLen - dashCount;
		
		if ( maxTblPrefix + maxQualLen  + maxItemName + dashCount > maxLenTableName &&
				maxTblPrefix + maxQualLen + dashCount > (2*maxLenTableName) / 5 ) {
			// clip the table prefix and qualifier lengths so that 
			// the item name can be 60% of the resolved table name.
			
			// determine the space allocated to the item name...
			fixTblItemLen = (3*maxLenTableName) / 5; // 60%...
			if ( fixTblItemLen > maxItemName ) {
				// the actual item names are shorter than this -- adjust down...
				fixTblItemLen = maxItemName;
			}
			
			int remainder = maxLenTableName - fixTblItemLen;
			
			fixTblPrefixLen = remainder / 3;
			if ( fixTblPrefixLen > maxTblPrefix ) {
				// the table prefix is smaller -- adjust down... 
				fixTblPrefixLen = maxTblPrefix;
			}

			fixTblQualLen = remainder - fixTblPrefixLen;
			
			// and shorten the qualifier and table prefix to make room for '_'s
			if ( fixTblQualLen > 0 ) {
				if ( fixTblQualLen == 1 ) {
					fixTblPrefixLen--;
				} else {
					fixTblQualLen--;
				}
			}
			if ( fixTblPrefixLen > 0 ) {
				if ( fixTblPrefixLen == 1 ) {
					fixTblQualLen--;
				} else {
					fixTblPrefixLen--;
				}
			}
		}
		
		if ( fixTblPrefixLen + fixTblQualLen + fixTblItemLen + dashCount > maxLenTableName ) {
			throw new IllegalStateException("unexpectedly overlong thresholds");
		}
		
		// and go through the tables creating consistently 
		// munged table names by shrinking the qualifier and item
		// names a consistent amount across all the fields it qualifies.
		for ( Map.Entry<String, Name> tbl : tablePlaceholders.entrySet() ) {
			Name nm = tbl.getValue();
			nm.mungedPrefix = trimName(nm.prefix, fixTblPrefixLen);
			nm.mungedQualifier = trimName(nm.qualifier, fixTblQualLen);
			nm.mungedItemName = trimName(nm.itemName, fixTblItemLen);
			if ( nm.mungedPrefix.length() != 0 ) {
				nm.resolvedName = nm.mungedPrefix + "_";
			} else {
				nm.resolvedName = "";
			}
			if ( nm.mungedQualifier.length() != 0 ) {
				nm.resolvedName += nm.mungedQualifier + "_";
			}
			nm.resolvedName += nm.mungedItemName;

			if ( nm.resolvedName.length() > maxLenTableName ) {
				Logger.getLogger(Naming.class.getName()).severe("Munged resolved name still too long: " +
						nm.resolvedName);
				nm.resolvedName = trimName(nm.resolvedName, maxLenTableName);
			}
		}
		
		// we now have the set of candidate resolved names.
		// build a set of these to ensure no collisions either amongst
		// each other or with any existing tables in the datastore. 
		// If we find a collision, resolve by appending suffix count
		// and re-trimming as needed.
		for ( Map.Entry<String, Name> tbl : tablePlaceholders.entrySet() ) {
			Name nm = tbl.getValue();
			if ( uniqueTables.contains(nm.resolvedName) ||
					ds.hasRelation(nm.schema, nm.resolvedName, user)) {
				// collision
				int dupCounter = 2;
				String newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenTableName);
				while ( uniqueTables.contains(newName) ||
						ds.hasRelation(nm.schema, newName, user)) {
					newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenTableName);
				}
				nm.resolvedName = newName;
			}
			uniqueTables.add(nm.resolvedName);
		}
		// OK.  At this point, we have found good table names!
		// let's hope that the tables don't need to be split into phantoms...
	}
	
	/**
	 * Called after the fact when a table needs to be split into the 
	 * original table and a phantom.
	 * 
	 * @param schema
	 * @param originalTable
	 * @param ds
	 * @param user
	 * @return
	 */
	public String generateUniqueTableName(String schema, String originalTable, Datastore ds, User user) {
		int maxLenTableName = ds.getMaxLenTableName();
		if ( uniqueTables.contains(originalTable) || ds.hasRelation(schema, originalTable, user) ) {
			// collision
			int dupCounter = 2;
			String newName = trimName(originalTable + Integer.toString(dupCounter++), maxLenTableName);
			while ( uniqueTables.contains(newName) || ds.hasRelation(schema, newName, user) ) {
				newName = trimName(originalTable + Integer.toString(dupCounter++), maxLenTableName);
			}
			originalTable = newName;
		}
		uniqueTables.add(originalTable);
		return originalTable;
	}

	/**
	 * Resolve the column names in each table definition so they are unique
	 * within that table.
	 * 
	 * @param maxLenColumnName
	 */
	private void resolveColumnNames(int maxLenColumnName) {
		// resolve the column names within a table
		for ( Map.Entry<String, Map<String, Name>> colMap : columnPlaceholders.entrySet() ) {
			// We have the column map entry for a given table.
			
			// now, construct a map of qualifier -to- max item name length 
			// for each different qualifier of column names.
			Map<String, Integer> qualMaxName = new HashMap<String, Integer>();
			for ( Map.Entry<String, Name> col : colMap.getValue().entrySet() ) {
				int len = col.getValue().itemName.length();
				String qualifier = col.getValue().qualifier;
				Integer curMax = qualMaxName.get(qualifier);
				if ( curMax == null || curMax < len ) {
					qualMaxName.put(qualifier, len);
				}
			}
			
			// and go through the columns in the table creating consistently 
			// munged column names by shrinking the qualifier a consistent 
			// amount across all the fields it qualifies.
			for ( Map.Entry<String, Name> col : colMap.getValue().entrySet() ) {
				Name nm = col.getValue();
				String qualifier = nm.qualifier;
				int maxAll = qualMaxName.get(qualifier);
				// remainder available to the qualifier... (extra one is for '_')
				int remainder = maxLenColumnName - maxAll - 1;
				if ( remainder > 0 && qualifier.length() - 5 > remainder ) {
					// truncate qualifier to remainder...
					nm.mungedQualifier = trimName(nm.qualifier, remainder);
					nm.mungedItemName = nm.itemName;
				}
				else if ( qualifier.length() == 0 ) {
					// truncate the item name, since it is all we have...
					nm.mungedQualifier = nm.qualifier;
					nm.mungedItemName = trimName(nm.itemName, maxLenColumnName);
				} else {
					// we have to pare down both the qualifier and the item name.
					// trim the qualifier to be 1/3 the max length, and the 
					// item name to be 2/3 the max length...
					remainder = maxLenColumnName / 3;
					nm.mungedQualifier = trimName(nm.qualifier, remainder - 1);
					nm.mungedItemName = trimName(nm.itemName, maxLenColumnName - remainder);
				}
				
				if ( nm.mungedQualifier.length() == 0 ) {
					nm.resolvedName = nm.mungedItemName;
				} else {
					nm.resolvedName = nm.mungedQualifier + "_" + nm.mungedItemName;
				}
				
				if ( nm.resolvedName.length() > maxLenColumnName ) {
					Logger.getLogger(Naming.class.getName()).severe("Munged resolved name still too long: " +
							nm.resolvedName);
					nm.resolvedName = trimName(nm.resolvedName, maxLenColumnName);
				}
			}
			
			// we now have the set of candidate resolved names.
			// build a set of these to ensure no collisions.  If we find a collision,
			// resolve by appending suffix count and re-trimming as needed.
			Set<String> resolvedNames = new HashSet<String>();
			for ( Map.Entry<String, Name> col : colMap.getValue().entrySet() ) {
				Name nm = col.getValue();
				if ( resolvedNames.contains(nm.resolvedName) ) {
					// collision
					int dupCounter = 2;
					String newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenColumnName);
					while ( resolvedNames.contains(newName) ) {
						newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenColumnName);
					}
					nm.resolvedName = newName;
				}
				resolvedNames.add(nm.resolvedName);
			}
			// OK.  At this point, we have found good column names for this table!
		}
	}

	/**
	 * Attempt to shorten a name in a somewhat sensible way.
	 * 
	 * @param name
	 * @param len  goal length of returned string.  Returned string may be shorter.
	 * @return shortened name
	 */
	public String trimName( String name, int len) {

		int numCharToDrop = name.length() - len;
		
		if ( numCharToDrop > name.length() ) {
			return ""; // not much we can do...
		}
		
		if ( numCharToDrop > 0 ) {
			
			// find where the vowels are in the name...
			List<Integer> pos = new ArrayList<Integer>();
			for ( int i = 0 ; i < name.length() ; ++i ) {
				if ( DROP_CHARS.indexOf(name.charAt(i)) != -1 ) {
					pos.add(i);
				}
			}
			
			if ( pos.size() - 2 < numCharToDrop ) {
				// not enough vowels to drop while leaving first and last.
				// eliminate any repeated characters and underscores...
				for ( int i = 0 ; i < name.length()-1 ; ++i ) {
					if ( name.charAt(i) == name.charAt(i+1) ||
							name.charAt(i) == '_' ) {
						name = name.substring(0,i) + name.substring(i+1);
					}
				}

				// and relocate where the vowels are in the name...
				pos.clear();
				for ( int i = 0 ; i < name.length() ; ++i ) {
					if ( DROP_CHARS.indexOf(name.charAt(i)) != -1 ) {
						pos.add(i);
					}
				}
			}
			numCharToDrop = name.length() - len;

			// and drop vowels...
			if ( pos.size() > 2 ) {
				while ( pos.size() > 2 && numCharToDrop > 0 ) {
					// drop two at a time from the 2nd and n-1st positions.
					int idx = pos.get(1);
					pos.remove(1);
					for ( int i = 1 ; i < pos.size(); ++i ) {
						pos.set(i, pos.get(i)-1);
					}
					name = name.substring(0, idx) + name.substring(idx+1);
					int nextToLast = pos.size()-2;
					idx = pos.get(nextToLast);
					pos.remove(nextToLast);
					for ( int i = nextToLast ; i < pos.size(); ++i ) {
						pos.set(i, pos.get(i)-1);
					}
					name = name.substring(0, idx) + name.substring(idx+1);
					
					numCharToDrop = name.length() - len;
				}
			}

			if (numCharToDrop > 0 && name.length() > numCharToDrop) {
				// urgh!  we are still too long -- go to I18N formatting...
				// if we need to drop 100 characters, we actually need to drop 103
				// so we can have space for the digits -- e.g., A103N
				// 
				// Compute the number of characters we actually have to drop...
				int digits = (numCharToDrop >= 97) ? 3 : (numCharToDrop >= 8) ? 2 : 1;
				numCharToDrop += digits;
				// possibly increase numCharToDrop to make it even...
				// this won't affect digits...
				numCharToDrop = (2*numCharToDrop + 1)/2;

				int elideFirst = name.length()/2 - numCharToDrop/2;
				int elideLast = name.length()/2 + numCharToDrop/2;
				
				name = name.substring(0,elideFirst) + Integer.toString(numCharToDrop) +
						name.substring(elideLast+1);
			}
			numCharToDrop = name.length() - len;
			assert(numCharToDrop <= 0);
		}
		return name;
	}
	
	/**
	 * Step Three -- retrieve the actual name for the table placeholder.
	 * @param tablePlaceholder
	 * @return table name
	 */
	public String resolveTablePlaceholder(String tablePlaceholder) {
		return tablePlaceholders.get(tablePlaceholder).resolvedName;
	}

	/**
	 * Step Three -- retrieve the actual name for the column placeholder.
	 * 
	 * @param tablePlaceholder
	 * @param columnPlaceholder
	 * @return column name
	 */
	public String resolveColumnPlaceholder(String tablePlaceholder,
			String columnPlaceholder) {
		if ( columnPlaceholder == null ) {
			return null;
		} else {
			return columnPlaceholders.get(tablePlaceholder).get(columnPlaceholder).resolvedName;
		}
	}
}