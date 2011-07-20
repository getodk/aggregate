package org.opendatakit.aggregate.odktables.client.entity;

public class SynchronizedRow extends Row
{
    private String revisionTag;

    public SynchronizedRow()
    {
        super();
        this.revisionTag = null;
    }

    public String getRevisionTag()
    {
        return this.revisionTag;
    }

    public void setRevisionTag(String revisionTag)
    {
        this.revisionTag = revisionTag;
    }

}
