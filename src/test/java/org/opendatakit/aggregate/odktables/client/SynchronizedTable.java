package org.opendatakit.aggregate.odktables.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;

public class SynchronizedTable
{
    private TableEntry entry;
    private List<Column> columns;
    private List<SynchronizedRow> rows;

    public SynchronizedTable(String tableName, String tableID, List<Column> columns)
    {
        entry = new TableEntry(null, null, tableID, tableName, true);
        this.columns = new ArrayList<Column>(columns);
        rows = new ArrayList<SynchronizedRow>();
    }

    public String getTableName()
    {
        return entry.getTableName();
    }

    public String getTableID()
    {
        return entry.getTableID();
    }

    public String getAggregateTableIdentifier()
    {
        return entry.getAggregateTableIdentifier();
    }

    public void setAggregateTableIdentifer(String value)
    {
        entry = new TableEntry(null, value, entry.getTableID(),
                entry.getTableName(), entry.isSynchronized());
    }

    public void insertRow(SynchronizedRow row)
    {
        int index = indexOf(rows, row);
        if (index != -1)
        {
            throw new IllegalArgumentException(String.format(
                    "Row with rowID: %s already exists in this table",
                    row.getRowID()));
        }
        rows.add(row);
    }

    public void updateRow(SynchronizedRow row)
    {
        int index = indexOf(rows, row);
        if (index == -1)
        {
            throw new IllegalArgumentException(String.format(
                    "Row with rowID: %s does not exist in this table",
                    row.getRowID()));
        } else
        {
            rows.remove(index);
            rows.add(index, row);
        }
    }
    
    public List<SynchronizedRow> getUnsynchronizedRows()
    {
       List<SynchronizedRow> unsynchedRows = new ArrayList<SynchronizedRow>();
       for (SynchronizedRow row : rows)
       {
           if (row.getAggregateRowIdentifier() == null)
           {
               unsynchedRows.add(row);
           }
       }
       return unsynchedRows;
    }

    /**
     * @return the index in rows of a row with the same rowID as the given row,
     *         or -1 if no such row is present
     */
    private int indexOf(List<SynchronizedRow> rows, SynchronizedRow row)
    {
        int index = -1;
        for (int i = 0; i < rows.size(); i++)
        {
            SynchronizedRow theRow = rows.get(i);
            if (theRow.getRowID().equals(row.getRowID()))
            {
                index = i;
            }
        }
        return index;
    }

    public List<SynchronizedRow> getRows()
    {
        return Collections.unmodifiableList(rows);
    }
    
    public List<SynchronizedRow> getRows(Collection<String> rowIDs)
    {
        List<SynchronizedRow> matchingRows = new ArrayList<SynchronizedRow>();
        for (SynchronizedRow row : rows)
        {
            if (rowIDs.contains(row.getRowID()))
            {
                matchingRows.add(row);
            }
        }
        return matchingRows;
    }
}
