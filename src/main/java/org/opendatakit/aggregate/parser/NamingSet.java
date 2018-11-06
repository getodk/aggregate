/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When a form is uploaded, the names for the columns and tables representing
 * the form need to be determined. This is done in a three-step process. First,
 * information about the name is gathered (schema, prefix, qualifier, item) that
 * will be used in constructing the full name, that data is stored in a
 * {@link Name} object, and a unique placeholder string is returned to the
 * caller. Second, once all names have been entered, unique resolved names are
 * determined for each {@link Name} in the set. This ensures that two data
 * items, perhaps in different non-repeating subgroups, don't accidentally get
 * mapped to the same column in the table. Third, a sweep by the caller then
 * replaces the unique placeholders with the appropriate resolved name.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
final class NamingSet {
  private static final String DROP_CHARS = "AEIOUY";
  private static final Logger logger = LoggerFactory.getLogger(NamingSet.class);
  private final Map<String, Name> tablePlaceholders = new TreeMap<String, Name>();
  private final Map<String, Map<String, Name>> columnPlaceholders = new TreeMap<String, Map<String, Name>>();
  private final Set<String> uniqueTables = new TreeSet<String>();
  private final Map<String, Integer> tableIndexCounters = new TreeMap<String, Integer>();
  private StringBuilder dbg = null;
  private int idxResolveNames = 0;
  private int baseCounter = 0;

  public NamingSet() {
  }

  private final String genPlaceholder() {
    String holder = String.format("%1$04d%2$s", ++baseCounter, UUID.randomUUID().toString());
    return holder;
  }

  private void dumpIndent(int indent) {
    while (indent > 0) {
      dbg.append(" ");
      --indent;
    }
  }

  private void dumpName(Name n, int indent) {
    dbg.append("{\n");
    dumpIndent(indent);
    dbg.append(" \"schema\": " + ((n.schema == null) ? "null" : ("\"" + n.schema + "\"")) + ",\n");
    dumpIndent(indent);
    dbg.append(" \"prefix\": " + ((n.prefix == null) ? "null" : ("\"" + n.prefix + "\"")) + ",\n");
    dumpIndent(indent);
    dbg.append(" \"qualifier\": " + ((n.qualifier == null) ? "null" : ("\"" + n.qualifier + "\""))
        + ",\n");
    dumpIndent(indent);
    dbg.append(" \"itemName\": " + ((n.itemName == null) ? "null" : ("\"" + n.itemName + "\""))
        + ",\n");
    dumpIndent(indent);
    dbg.append(" \"mungedPrefix\": "
        + ((n.mungedPrefix == null) ? "null" : ("\"" + n.mungedPrefix + "\"")) + ",\n");
    dumpIndent(indent);
    dbg.append(" \"mungedQualifier\": "
        + ((n.mungedQualifier == null) ? "null" : ("\"" + n.mungedQualifier + "\"")) + ",\n");
    dumpIndent(indent);
    dbg.append(" \"mungedItemName\": "
        + ((n.mungedItemName == null) ? "null" : ("\"" + n.mungedItemName + "\"")) + ",\n");
    dumpIndent(indent);
    dbg.append(" \"resolvedName\": "
        + ((n.resolvedName == null) ? "null" : ("\"" + n.resolvedName + "\"")) + "\n");
    dumpIndent(indent);
    dbg.append("}");
  }

  private synchronized void dumpTables(String header, int indent) {
    if (logger.isDebugEnabled()) {
      dumpTablesInternal(header, indent, false);
    }
  }

  private synchronized void dumpTablesInternal(String header, int indent, boolean asError) {
    dbg = new StringBuilder();

    dumpIndent(indent);
    dbg.append("\"" + header + "\": {\n");

    dumpIndent(indent + 2);
    dbg.append("\"tablePlaceholders\": {\n");
    boolean first = true;
    for (Map.Entry<String, Name> tp : tablePlaceholders.entrySet()) {
      if (!first) {
        dbg.append(",\n");
      }
      first = false;
      dumpIndent(indent + 4);
      dbg.append("\"" + tp.getKey() + "\" : ");
      dumpName(tp.getValue(), indent + 6);
    }
    dbg.append("\n");
    dumpIndent(indent + 2);
    dbg.append("},\n");

    dumpIndent(indent + 2);
    dbg.append("\"columnPlaceholders\": {\n");
    first = true;
    for (Map.Entry<String, Map<String, Name>> cp : columnPlaceholders.entrySet()) {
      if (!first) {
        dbg.append(",\n");
      }
      first = false;
      dumpIndent(indent + 4);
      dbg.append("\"" + cp.getKey() + "\" : {");
      boolean nestedFirst = true;
      for (Map.Entry<String, Name> cpp : cp.getValue().entrySet()) {
        if (!nestedFirst) {
          dbg.append(",\n");
        }
        nestedFirst = false;
        dumpIndent(indent + 6);
        dbg.append("\"" + cpp.getKey() + "\" : ");
        dumpName(cpp.getValue(), indent + 8);
      }
      dbg.append("\n");
      dumpIndent(indent + 4);
      dbg.append("}");
    }
    dbg.append("\n");
    dumpIndent(indent + 2);
    dbg.append("}\n");
    dumpIndent(indent);
    dbg.append("},\n");
    if (asError) {
      logger.error(dbg.toString());
    } else {
      logger.debug(dbg.toString());
    }
  }

  /**
   * Record information about a table and obtain the placeholder string for it.
   *
   * @param schema
   * @param prefix
   * @param qualifier
   * @param itemName
   * @return placeholder string for this table.
   */
  public final String getTableName(String schema, String prefix, String qualifier, String itemName) {
    String placeholder = genPlaceholder();

    tablePlaceholders.put(placeholder, new Name(schema, prefix, qualifier, itemName));
    return placeholder;
  }

  /**
   * Record information about a column in a table and obtain a placeholder
   * string for it.
   *
   * @param tablePlaceholder
   * @param qualifier
   * @param itemName
   * @return placeholder string for this column.
   */
  public final String getColumnName(String tablePlaceholder, String qualifier, String itemName) {
    String placeholder = genPlaceholder();

    Map<String, Name> m = columnPlaceholders.get(tablePlaceholder);
    if (m == null) {
      m = new TreeMap<String, Name>();
      columnPlaceholders.put(tablePlaceholder, m);
    }
    m.put(placeholder, new Name(null, "", qualifier, itemName));
    return placeholder;
  }

  /**
   * Called prior to resolving column names if a column placeholder was defined
   * but actually doesn't exist (e.g., geopoint, binary or choice).
   *
   * @param tablePlaceholder
   * @param columnPlaceholder
   */
  public final void removeColumnName(String tablePlaceholder, String columnPlaceholder) {
    Map<String, Name> m = columnPlaceholders.get(tablePlaceholder);
    if (m != null) {
      m.remove(columnPlaceholder);
    }
  }

  /**
   * Step two -- resolve the table and column names in the form.
   *
   * @param ds
   * @param user
   * @throws ODKDatastoreException
   */
  public void resolveNames(Datastore ds, User user) throws ODKDatastoreException {
    resolveTableNames(ds, user);
    resolveColumnNames(ds.getMaxLenColumnName());
    dumpTables("resolve" + Integer.valueOf(++idxResolveNames), 0);
  }

  /**
   * Resolve the table names. This requires asking the persistence layer if the
   * table already exists. We are creating tables for a new form, so it should
   * not stomp on any existing tables.
   *
   * @param ds
   * @param user
   * @throws ODKDatastoreException
   */
  private void resolveTableNames(Datastore ds, User user) throws ODKDatastoreException {
    int maxLenTableName = ds.getMaxLenTableName();
    // resolve the table names

    // construct a map of qualifier -to- max item name length
    // for each different qualifier of table names.
    int maxTblPrefix = 0;
    int maxQualLen = 0;
    int maxItemName = 0;
    Map<String, Integer> qualMaxName = new TreeMap<String, Integer>();
    for (Map.Entry<String, Name> tbl : tablePlaceholders.entrySet()) {
      Name nm = tbl.getValue();
      int len = nm.itemName.length();
      String qualifier = nm.qualifier;
      Integer curMax = qualMaxName.get(qualifier);
      if (curMax == null || curMax.compareTo(len) < 0) {
        qualMaxName.put(qualifier, len);
      }
      if (len > maxItemName) {
        maxItemName = len;
      }
      if (qualifier.length() > maxQualLen) {
        maxQualLen = qualifier.length();
      }
      if (nm.prefix.length() > maxTblPrefix) {
        maxTblPrefix = nm.prefix.length();
      }
    }

    int fixTblPrefixLen = maxTblPrefix;
    int fixTblQualLen = maxQualLen;
    int dashCount = 0;
    if (fixTblPrefixLen > 0)
      ++dashCount;
    if (fixTblQualLen > 0)
      ++dashCount;
    int fixTblItemLen = maxLenTableName - fixTblPrefixLen - fixTblQualLen - dashCount;

    if (maxTblPrefix + maxQualLen + maxItemName + dashCount > maxLenTableName
        && maxTblPrefix + maxQualLen + dashCount > (2 * maxLenTableName) / 5) {
      // clip the table prefix and qualifier lengths so that
      // the item name can be 60% of the resolved table name.

      // determine the space allocated to the item name...
      fixTblItemLen = (3 * maxLenTableName) / 5; // 60%...
      if (fixTblItemLen > maxItemName) {
        // the actual item names are shorter than this -- adjust down...
        fixTblItemLen = maxItemName;
      }

      int remainder = maxLenTableName - fixTblItemLen;

      fixTblPrefixLen = remainder / 3;
      if (fixTblPrefixLen > maxTblPrefix) {
        // the table prefix is smaller -- adjust down...
        fixTblPrefixLen = maxTblPrefix;
      }

      fixTblQualLen = remainder - fixTblPrefixLen;

      // and shorten the qualifier and table prefix to make room for '_'s
      if (fixTblQualLen > 0) {
        if (fixTblQualLen == 1) {
          fixTblPrefixLen--;
        } else {
          fixTblQualLen--;
        }
      }
      if (fixTblPrefixLen > 0) {
        if (fixTblPrefixLen == 1) {
          fixTblQualLen--;
        } else {
          fixTblPrefixLen--;
        }
      }
    }

    if (fixTblPrefixLen + fixTblQualLen + fixTblItemLen + dashCount > maxLenTableName) {
      throw new IllegalStateException("unexpectedly overlong thresholds");
    }

    // and go through the tables creating consistently
    // munged table names by shrinking the qualifier and item
    // names a consistent amount across all the fields it qualifies.
    for (Map.Entry<String, Name> tbl : tablePlaceholders.entrySet()) {
      Name nm = tbl.getValue();
      nm.mungedPrefix = trimName(nm.prefix, fixTblPrefixLen);
      nm.mungedQualifier = trimName(nm.qualifier, fixTblQualLen);
      nm.mungedItemName = trimName(nm.itemName, fixTblItemLen);
      if (nm.mungedPrefix.length() != 0) {
        nm.resolvedName = nm.mungedPrefix + "_";
      } else {
        nm.resolvedName = "";
      }
      if (nm.mungedQualifier.length() != 0) {
        nm.resolvedName += nm.mungedQualifier + "_";
      }
      nm.resolvedName += nm.mungedItemName;

      if (nm.resolvedName.length() > maxLenTableName) {
        logger.error("Munged resolved name still too long: " + nm.resolvedName);
        nm.resolvedName = trimName(nm.resolvedName, maxLenTableName);
      }
    }

    // we now have the set of candidate resolved names.
    // build a set of these to ensure no collisions either amongst
    // each other or with any existing tables in the datastore.
    // If we find a collision, resolve by appending suffix count
    // and re-trimming as needed.
    for (Map.Entry<String, Name> tbl : tablePlaceholders.entrySet()) {
      Name nm = tbl.getValue();
      if (uniqueTables.contains(nm.resolvedName)
          || ds.hasRelation(nm.schema, nm.resolvedName, user)) {
        // collision
        int dupCounter = 2;
        String newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenTableName);
        while (uniqueTables.contains(newName) || ds.hasRelation(nm.schema, newName, user)) {
          newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenTableName);
        }
        nm.resolvedName = newName;
      }
      uniqueTables.add(nm.resolvedName);
    }
    // OK. At this point, we have found good table names!
    // let's hope that the tables don't need to be split into phantoms...
  }

  /**
   * Strip off any trailing digits to recover the raw name of a table.
   *
   * @param originalTable
   * @return
   */
  private String getStrippedName(String originalTable) {
    int idxNonNumber = originalTable.length() - 1;
    while (idxNonNumber >= 0 && Character.isDigit(originalTable.charAt(idxNonNumber))) {
      --idxNonNumber;
    }
    String strippedName = originalTable;
    if (idxNonNumber != 0) {
      strippedName = originalTable.substring(0, idxNonNumber + 1);
    }
    return strippedName;
  }

  /**
   * Called after the fact when a table needs to be split into the original
   * table and a phantom.
   *
   * @param schema
   * @param originalTable
   * @param ds
   * @param user
   * @return
   * @throws ODKDatastoreException
   */
  public String generateUniqueTableName(String schema, String originalTable, CallingContext cc)
      throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    int maxLenTableName = ds.getMaxLenTableName();
    if (uniqueTables.contains(originalTable) || ds.hasRelation(schema, originalTable, user)) {
      int dupCounter = 2;
      String strippedName = getStrippedName(originalTable);
      if (tableIndexCounters.containsKey(strippedName)) {
        dupCounter = tableIndexCounters.get(strippedName);
      }
      // collision
      String newName = trimName(strippedName + Integer.toString(dupCounter++), maxLenTableName);
      while (uniqueTables.contains(newName) || ds.hasRelation(schema, newName, user)) {
        newName = trimName(strippedName + Integer.toString(dupCounter++), maxLenTableName);
      }
      tableIndexCounters.put(strippedName, dupCounter);
      originalTable = newName;
    }
    uniqueTables.add(originalTable);
    return originalTable;
  }

  /**
   * Resolve the column names in each table definition so they are unique within
   * that table.
   *
   * @param maxLenColumnName
   */
  private void resolveColumnNames(int maxLenColumnName) {
    try {
      // resolve the column names within a table
      for (Map.Entry<String, Map<String, Name>> colMap : columnPlaceholders.entrySet()) {
        // We have the column map entry for a given table.

        // now, construct a map of qualifier -to- max item name length
        // for each different qualifier of column names.
        Map<String, Integer> qualMaxName = new TreeMap<String, Integer>();
        for (Map.Entry<String, Name> col : colMap.getValue().entrySet()) {
          int len = col.getValue().itemName.length();
          String qualifier = col.getValue().qualifier;
          Integer curMax = qualMaxName.get(qualifier);
          if (curMax == null || curMax.compareTo(len) < 0) {
            qualMaxName.put(qualifier, len);
          }
        }

        // and go through the columns in the table creating consistently
        // munged column names by shrinking the qualifier a consistent
        // amount across all the fields it qualifies.
        for (Map.Entry<String, Name> col : colMap.getValue().entrySet()) {
          Name nm = col.getValue();
          String qualifier = nm.qualifier;
          int maxAll = qualMaxName.get(qualifier);
          // remainder available to the qualifier... (extra one is for '_')
          int remainder = maxLenColumnName - maxAll - 1;
          if (qualifier.length() == 0) {
            // truncate the item name, since it is all we have...
            nm.mungedQualifier = nm.qualifier;
            nm.mungedItemName = trimName(nm.itemName, maxLenColumnName);
          } else if (qualifier.length() <= remainder) {
            // (qualifier + max item name) is short enough to avoid truncation
            nm.mungedQualifier = nm.qualifier;
            nm.mungedItemName = nm.itemName;
          } else if ((remainder >= 3) &&
              (qualifier.length() <= 11 ||
                  remainder >= Integer.toString(qualifier.length() - 2).length() + 2)) {
            // truncate qualifier to remainder...
            // In the worst case, we have enough room
            // to encode the qualifier with an I18N style encoding.
            nm.mungedQualifier = trimName(nm.qualifier, remainder);
            nm.mungedItemName = nm.itemName;
          } else {
            // we have to pare down both the qualifier and the item name.
            // trim the qualifier to be 1/3 the max length, and the
            // item name to be 2/3 the max length...
            remainder = maxLenColumnName / 3;
            nm.mungedQualifier = trimName(nm.qualifier, remainder - 1);
            nm.mungedItemName = trimName(nm.itemName, maxLenColumnName - remainder);
          }

          if (nm.mungedQualifier.length() == 0) {
            nm.resolvedName = nm.mungedItemName;
          } else {
            nm.resolvedName = nm.mungedQualifier + "_" + nm.mungedItemName;
          }

          if (nm.resolvedName.length() > maxLenColumnName) {
            logger.error("Munged resolved name still too long: " + nm.resolvedName);
            nm.resolvedName = trimName(nm.resolvedName, maxLenColumnName);
          }
        }

        // we now have the set of candidate resolved names.
        // build a set of these to ensure no collisions. If we find a collision,
        // resolve by appending suffix count and re-trimming as needed.
        Set<String> resolvedNames = new TreeSet<String>();
        for (Map.Entry<String, Name> col : colMap.getValue().entrySet()) {
          Name nm = col.getValue();
          if (resolvedNames.contains(nm.resolvedName)) {
            // collision
            int dupCounter = 2;
            String newName = trimName(nm.resolvedName + Integer.toString(dupCounter++),
                maxLenColumnName);
            while (resolvedNames.contains(newName)) {
              newName = trimName(nm.resolvedName + Integer.toString(dupCounter++), maxLenColumnName);
            }
            nm.resolvedName = newName;
          }
          resolvedNames.add(nm.resolvedName);
        }
        // OK. At this point, we have found good column names for this table!
      }
    } catch (IndexOutOfBoundsException e) {
      logger.error("Index out of bounds exception");
      dumpTablesInternal("idxOutOfBounds", 0, true);
      throw e;
    }
  }

  /**
   * Attempt to shorten a name in a somewhat sensible way.
   *
   * @param name
   * @param len
   *          goal length of returned string. Returned string may be shorter.
   * @return shortened name
   */
  public String trimName(String name, int len) {

    String originalName = name;
    int numCharToDrop = name.length() - len;

    if (numCharToDrop > name.length()) {
      return ""; // not much we can do...
    }

    if (numCharToDrop > 0) {

      // find where the vowels are in the name...
      List<Integer> pos = new ArrayList<Integer>();
      for (int i = 0; i < name.length(); ++i) {
        if (DROP_CHARS.indexOf(name.charAt(i)) != -1) {
          pos.add(i);
        }
      }

      if (pos.size() - 2 < numCharToDrop) {
        // not enough vowels to drop while leaving first and last.
        // eliminate any repeated characters and underscores...
        for (int i = 0; i < name.length() - 1; ++i) {
          if (name.charAt(i) == name.charAt(i + 1) || name.charAt(i) == '_') {
            name = name.substring(0, i) + name.substring(i + 1);
          }
        }

        // and relocate where the vowels are in the name...
        pos.clear();
        for (int i = 0; i < name.length(); ++i) {
          if (DROP_CHARS.indexOf(name.charAt(i)) != -1) {
            pos.add(i);
          }
        }
      }
      numCharToDrop = name.length() - len;

      // and drop vowels...
      if (pos.size() > 2) {
        while (pos.size() > 2 && numCharToDrop > 0) {
          // drop two at a time from the 2nd and n-1st positions.
          int idx = pos.get(1);
          pos.remove(1);
          for (int i = 1; i < pos.size(); ++i) {
            pos.set(i, pos.get(i) - 1);
          }
          name = name.substring(0, idx) + name.substring(idx + 1);
          int nextToLast = pos.size() - 2;
          idx = pos.get(nextToLast);
          pos.remove(nextToLast);
          for (int i = nextToLast; i < pos.size(); ++i) {
            pos.set(i, pos.get(i) - 1);
          }
          name = name.substring(0, idx) + name.substring(idx + 1);

          numCharToDrop = name.length() - len;
        }
      }

      if (name.length() > len) {
        numCharToDrop = originalName.length() - len;
        // urgh! we are still too long -- go to I18N formatting...
        // if we need to drop 100 characters, we actually need to drop 103
        // so we can have space for the digits -- e.g., A103N
        //
        // Compute the number of characters we actually have to drop...
        int digits = (numCharToDrop >= 98) ? 3 : (numCharToDrop >= 9) ? 2 : 1;
        numCharToDrop += digits;

        if (numCharToDrop + 2 <= originalName.length()) {
          int oddCorrector = 1 - (originalName.length() % 2);
          int elideFirst = (originalName.length() + oddCorrector - numCharToDrop) / 2;
          int elideLast = (originalName.length() + oddCorrector + numCharToDrop) / 2;

          name = originalName.substring(0, elideFirst) + Integer.toString(numCharToDrop)
              + originalName.substring(elideLast);
        }
      }

      if (name.length() > len) {
        // OK. We are completely hosed.
        // There is no room for I18N
        name = originalName.substring(0, len);
      }
    }
    return name;
  }

  /**
   * Step Three -- retrieve the actual name for the table placeholder.
   *
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
  public String resolveColumnPlaceholder(String tablePlaceholder, String columnPlaceholder) {
    if (columnPlaceholder == null) {
      return null;
    } else {
      return columnPlaceholders.get(tablePlaceholder).get(columnPlaceholder).resolvedName;
    }
  }
}
