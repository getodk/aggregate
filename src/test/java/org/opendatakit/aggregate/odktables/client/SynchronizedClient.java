package org.opendatakit.aggregate.odktables.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulates a client of the odktables API with functionality for storing
 * synchronized tables.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SynchronizedClient
{

    private String clientName;
    private String aggregateUserIdentifier;
    // tableID to SynchronizedTable
    private Map<String, SynchronizedTable> tables;

    public SynchronizedClient(String clientName)
    {
        this.clientName = clientName;
        this.aggregateUserIdentifier = null;
        tables = new HashMap<String, SynchronizedTable>();
    }

    public String getClientName()
    {
        return clientName;
    }

    public void setAggregateUserIdentifier(String value)
    {
        this.aggregateUserIdentifier = value;
    }

    public String getAggregateUserIdentifier()
    {
        return this.aggregateUserIdentifier;
    }

    public void addTable(SynchronizedTable table)
    {
        tables.put(table.getTableName(), table);
    }

    public void removeTable(String tableID)
    {
        tables.remove(tableID);
    }

    public SynchronizedTable getTable(String tableID)
    {
        return tables.get(tableID);
    }

    public Collection<SynchronizedTable> getTables()
    {
        return tables.values();
    }

}
