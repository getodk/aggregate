package org.opendatakit.aggregate.odktables.client.exception;

/**
 * An exception for when an attempt to update a table is made but the caller's
 * version is out of synch with the one in Aggregate.
 * 
 * @author the.dylan.price@gmail.com
 */
public class OutOfSynchException extends ODKTablesClientException
{
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = -9027773843183177336L;

    public OutOfSynchException()
    {
        super("Table out of synch with Aggregate!.");

    }
}
