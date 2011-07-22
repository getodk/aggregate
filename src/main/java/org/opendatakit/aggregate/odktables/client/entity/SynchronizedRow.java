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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String
                .format("SynchronizedRow [rowID=%s, aggregateRowIdentifier=%s, revisionTag=%s]",
                        getRowID(), getAggregateRowIdentifier(), revisionTag);
    }

}
