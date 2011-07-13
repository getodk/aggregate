package org.opendatakit.aggregate.odktables.client.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;

/**
 * SynchronizedAPI contains API calls for using Aggregate as a synchronization
 * service for tables.
 */
public class SynchronizeAPI extends CommonAPI
{

    /**
     * Constructs a new instance of SynchronizedAPI, using the supplied user
     * identification for API calls which require it.
     * 
     * @param aggregateURI
     *            the URI of a running ODK Aggregate instance
     * @param userID
     *            the ID of the user to use for API calls
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     *             or if it does not exist
     * @throws UserDoesNotExistException
     *             if no user with userID exists in Aggregate
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             initial communication to fail
     */
    public SynchronizeAPI(URI aggregateURI, String userID)
    {
        super(aggregateURI);
        throw new NotImplementedException();
    }

    /**
     * Creates a new synchronized table.
     * 
     * @param tableID
     *            the client's unique identifier for the table
     * @return the initial Modification of the newly created table (calling
     *         getRows() on the Modification will return an empty list).
     * @throws TableAlreadyExistsException
     *             if the caller has already created a synchronized table with
     *             the given tableID.
     */
    public Modification createSynchronizedTable(String tableID)
    {
        throw new NotImplementedException();
    }

    /**
     * Clones an existing synchronized table.
     * 
     * @param tableUUID
     *            the universally unique identifier of the table
     * @param tableID
     *            the unique identifier that the caller will use to identify the
     *            table
     * @return the current Modification of the table. The list returned by
     *         getRows() will be populated with rowUUID, revisionNumber, and
     *         data for the row. Make sure that all of this data is stored as it
     *         will be required for other API calls (see {@link SynchronizedAPI
     *         the top of this file} for a summary of client requirements for
     *         synchronized API usage).
     * @throws TableAlreadyExistsException
     *             if the caller has already registered a table with tableID
     * @throws TableDoesNotExistException
     *             if no table with UUID tableUUID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the table
     */
    public Modification cloneSynchronizedTable(String tableUUID, String tableID)
    {
        throw new NotImplementedException();
    }

    /**
     * Removes the caller from synchronization with a table. The caller must
     * call {@link #cloneSynchronizedTable} to interact with the table again.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @throws TableDoesNotExistException
     *             if the caller does not have a table registered with tableID
     */
    public void removeTableSynchronization(String tableID)
    {
        throw new NotImplementedException();
    }

    /**
     * Completely deletes a synchronized table from Aggregate. All future
     * requests for the table will error for all users.
     * 
     * @param tableUUID
     *            the universally unique identifier of the table
     * @throws TableDoesNotExistException
     *             if no such table with tableUUID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have delete
     *             permission on the table
     */
    public void deleteSynchronizedTable(String tableUUID)
    {
        throw new NotImplementedException();
    }

    /**
     * Inserts new rows into a synchronized table. This is a valid call only if
     * the caller's table is up to date with the latest Modification of the
     * table in Aggregate.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @param modificationNumber
     *            the current modificationNumber of the caller's copy of the
     *            table
     * @param newRows
     *            a list of rows to insert. These should be populated with
     *            rowIDs and data.
     * @return a Modification whose modificationNumber represents the latest
     *         modification of the table in Aggregate. Calling getRows() on the
     *         Modification will return a list of rows where each row is
     *         populated with rowID, rowUUID, and revisionNumber. Make sure that
     *         all of this data is stored as it will be required for other API
     *         calls (see {@link SynchronizedAPI the top of this file} for a
     *         summary of client requirements for synchronized API usage).
     * @throws OutOfSynchException
     *             if the given modificationNumber does not match the
     *             modificationNumber of the table in Aggregate. In this case
     *             the caller should call {@link #synchronize}, then attempt to
     *             insert again.
     * @throws TableDoesNotExistException
     *             if the caller has no table with tableID registered for
     *             synchronization
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the table
     */
    public Modification insertSynchronizedRows(String tableID, int modificationNumber, List<Row> newRows)
    {
        throw new NotImplementedException();
    }

    /**
     * Updates existing rows of a synchronized table in Aggregate. This is a
     * valid call only if the client is up to date with the latest Modification
     * of the table in Aggregate.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @param modificationNumber
     *            the current modificationNumber of the caller's copy of the
     *            table
     * @param changedRows
     *            a list of synchronized rows which are populated rowUUIDs,
     *            revisionNumbers,and data. These rows must already exist in
     *            Aggregate's copy of the table
     * @return a Modificaton whose modificationNumber represents the latest
     *         modification of the table in Aggregate. Calling getRows() on the
     *         Modification will return a list of rows where each row is
     *         populated with rowUUID and revisionNumber. Make sure that all of
     *         this data is stored as it will be required for other API calls
     *         (see {@link SynchronizedAPI the top of this file} for a summary
     *         of client requirements for synchronized API usage).
     * @throws OutOfSynchException
     *             if the given modificationNumber does not match the
     *             modificationNumber of the table in Aggregate. In this case
     *             the caller should call {@link #synchronize}, then attempt to
     *             insert again.
     * @throws TableDoesNotExistException
     *             if the caller has no table with tableID registered for
     *             synchronization
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the table
     */
    public Modification updateSynchronizedRows(String tableID, int modificationNumber, List<SynchronizedRow> changedRows)
    {
        throw new NotImplementedException();
    }

    /**
     * Retrieves the latest data from a table so that the caller can stay
     * synchronized with Aggregate's copy of the table.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @param modificationNumber
     *            the current modificationNumber of the caller's copy of the
     *            table. The caller must be up to date with this
     *            modificationNumber in Aggregate.
     * @return a Modification whose modificationNumber represents the latest
     *         modification of the table in Aggregate. Calling getRows() on the
     *         Modification will return a list of rows where each row is
     *         populated with rowUUID, revisionNumber, and data. Make sure that
     *         all of this data is stored as it will be required for other API
     *         calls (see {@link SynchronizedAPI the top of this file} for a
     *         summary of client requirements for synchronized API usage).
     * @throws TableDoesNotExistException
     *             if the caller has no table with tableID registered for
     *             synchronization
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the table
     */
    public Modification synchronize(String tableID, int modificationNumber)
    {
        throw new NotImplementedException();
    }
}
