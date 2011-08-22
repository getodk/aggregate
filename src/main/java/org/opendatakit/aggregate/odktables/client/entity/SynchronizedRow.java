package org.opendatakit.aggregate.odktables.client.entity;

/**
 * <p>
 * A SynchronizedRow is a row in a synchronized table, that is, a table created
 * using the SynchronizeAPI.
 * </p>
 * 
 * <p>
 * A SynchronizedRow is the same as a normal Row, except that in addition to the
 * attributes of a Row it has:
 * <ul>
 * <li>revisionTag: a random tag that is updated every time the row is changed.
 * This is used to ensure the client is able to keep their row in synch with
 * Aggregate's</li>
 * </ul>
 * </p>
 * 
 * <p>
 * SynchronizedRow is mutable and currently not threadsafe.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((revisionTag == null) ? 0 : revisionTag.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof SynchronizedRow))
            return false;
        SynchronizedRow other = (SynchronizedRow) obj;
        if (revisionTag == null)
        {
            if (other.revisionTag != null)
                return false;
        } else if (!revisionTag.equals(other.revisionTag))
            return false;
        return true;
    }

}
