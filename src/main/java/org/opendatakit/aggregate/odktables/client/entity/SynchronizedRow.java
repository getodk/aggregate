package org.opendatakit.aggregate.odktables.client.entity;

public class SynchronizedRow extends Row
{
    private final String revisionNumber;

    public SynchronizedRow()
    {
        super();
        this.revisionNumber = null;
    }

    public String getRevisionNumber()
    {
        return this.revisionNumber;
    }

    public void setRevisionNumber(String revisionNumber)
    {
        this.revisionNumber = revisionNumber;
    }

}
