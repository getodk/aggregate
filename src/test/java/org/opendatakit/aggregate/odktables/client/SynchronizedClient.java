package org.opendatakit.aggregate.odktables.client;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedClient
{
    
    private String clientName;
    // tableID to SynchronizedTable
    private Map<String, SynchronizedTable> tables;
    
    public SynchronizedClient(String clientName)
    {
        this.clientName = clientName;
        tables = new HashMap<String, SynchronizedTable>();
    }
    
    public void addTable(SynchronizedTable table)
    {
       tables.put(table.getTableID(), table);
    }

    public void getTable(String tableID)
    {
        return tables.get(tableID);
    }
    
    public List<SynchronizedTable> getTables()
    {
        return tables.valueSet();
    }

}
