package org.opendatakit.aggregate.odktables.client;

import java.util.ArrayList;
import java.util.List;

public class SynchronizedClient
{
    
    private String clientName;
    private List<SynchronizedTable> tables;
    
    public SynchronizedClient(String clientName)
    {
        this.clientName = clientName;
        tables = new ArrayList<SynchronizedTable>();
    }
    
    public void addTable(SynchronizedTable table)
    {
       tables.add(table); 
    }
    
    public List<SynchronizedTable> getTables()
    {
        return tables;
    }

}
