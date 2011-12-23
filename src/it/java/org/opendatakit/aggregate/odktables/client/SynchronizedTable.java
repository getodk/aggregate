package org.opendatakit.aggregate.odktables.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;

/**
 * A table stored in a SynchronizedClient.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SynchronizedTable {
    private TableEntry entry;
    private int modificationNumber;
    private List<SynchronizedRow> rows;

    public SynchronizedTable(String tableName, String tableID,
	    String properties, List<Column> columns) {
	entry = new TableEntry(null, null, tableID, tableName, properties,
		columns, true);
	rows = new ArrayList<SynchronizedRow>();
    }

    public String getTableName() {
	return entry.getTableName();
    }

    public String getTableID() {
	return entry.getTableID();
    }

    public String getAggregateTableIdentifier() {
	return entry.getAggregateTableIdentifier();
    }

    public void setAggregateTableIdentifer(String value) {
	entry = new TableEntry(null, value, entry.getTableID(),
		entry.getTableName(), entry.getProperties(),
		entry.getColumns(), entry.isSynchronized());
    }

    public int getModificationNumber() {
	return this.modificationNumber;
    }

    public void setModificationNumber(int modificationNumber) {
	this.modificationNumber = modificationNumber;
    }

    public List<Column> getColumns() {
	return entry.getColumns();
    }

    public void insertRow(SynchronizedRow row) {
	int index = indexOf(rows, row.getRowID());
	if (index != -1) {
	    throw new IllegalArgumentException(String.format(
		    "Row with rowID: %s already exists in this table",
		    row.getRowID()));
	}
	rows.add(row);
    }

    public void updateRow(SynchronizedRow row) {
	int index = indexOf(rows, row.getRowID());
	if (index == -1) {
	    throw new IllegalArgumentException(String.format(
		    "Row with rowID: %s does not exist in this table",
		    row.getRowID()));
	} else {
	    rows.remove(index);
	    rows.add(index, row);
	}
    }

    public SynchronizedRow getRow(String rowID) {
	int index = indexOf(rows, rowID);
	if (index == -1) {
	    throw new IllegalArgumentException(String.format(
		    "Row with rowID: %s does not exist in this table", rowID));
	} else {
	    return rows.get(index);
	}
    }

    public SynchronizedRow getRowByIdentifier(String aggregateRowIdentifier) {
	int index = indexOfByIdentifier(rows, aggregateRowIdentifier);
	if (index == -1) {
	    throw new IllegalArgumentException(
		    String.format(
			    "Row with aggregateRowIdentifier: %s does not exist in this table",
			    aggregateRowIdentifier));
	} else {
	    return rows.get(index);
	}
    }

    public boolean hasRow(String rowID) {
	return indexOf(rows, rowID) != -1;
    }

    public boolean hasRowByIdentifier(String aggregateRowIdentifier) {
	return indexOfByIdentifier(rows, aggregateRowIdentifier) != -1;
    }

    public List<SynchronizedRow> getUnsynchronizedRows() {
	List<SynchronizedRow> unsynchedRows = new ArrayList<SynchronizedRow>();
	for (SynchronizedRow row : rows) {
	    if (row.getAggregateRowIdentifier() == null) {
		unsynchedRows.add(row);
	    }
	}
	return unsynchedRows;
    }

    /**
     * @return the index in rows of a row with the same rowID as the given
     *         rowID, or -1 if no such row is present
     */
    private int indexOf(List<SynchronizedRow> rows, String rowID) {
	int index = -1;
	for (int i = 0; i < rows.size(); i++) {
	    SynchronizedRow theRow = rows.get(i);
	    if (theRow.getRowID().equals(rowID)) {
		index = i;
	    }
	}
	return index;
    }

    private int indexOfByIdentifier(List<SynchronizedRow> rows,
	    String aggregateRowIdentifier) {
	int index = -1;
	for (int i = 0; i < rows.size(); i++) {
	    SynchronizedRow theRow = rows.get(i);
	    if (theRow.getAggregateRowIdentifier().equals(
		    aggregateRowIdentifier)) {
		index = i;
	    }
	}
	return index;
    }

    public List<SynchronizedRow> getRows() {
	return Collections.unmodifiableList(rows);
    }

    public List<SynchronizedRow> getRows(Collection<String> rowIDs) {
	List<SynchronizedRow> matchingRows = new ArrayList<SynchronizedRow>();
	for (SynchronizedRow row : rows) {
	    if (rowIDs.contains(row.getRowID())) {
		matchingRows.add(row);
	    }
	}
	return matchingRows;
    }
}
