package org.opendatakit.aggregate.odktables.client.entity;

import java.util.Collections;
import java.util.List;

public class Modification
{
    private final int modificationNumber;
    private final List<SynchronizedRow> rows;

    public Modification(int modificationNumber, List<SynchronizedRow> rows)
    {
        this.modificationNumber = modificationNumber;
        this.rows = rows;
    }

    public int getModificationNumber()
    {
        return this.modificationNumber;
    }

    public List<SynchronizedRow> getRows()
    {
        return Collections.unmodifiableList(this.rows);
    }
}
