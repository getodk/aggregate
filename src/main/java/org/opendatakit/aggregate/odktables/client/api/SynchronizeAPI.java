package org.opendatakit.aggregate.odktables.client.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.OutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.RowOutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.command.synchronize.RemoveTableSynchronization;
import org.opendatakit.aggregate.odktables.command.synchronize.Synchronize;
import org.opendatakit.aggregate.odktables.command.synchronize.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CloneSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.CreateSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.DeleteSynchronizedTableResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.InsertSynchronizedRowsResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.RemoveTableSynchronizationResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.SynchronizeResult;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.UpdateSynchronizedRowsResult;

/**
 * <p>
 * SynchronizedAPI contains API calls for using Aggregate as a synchronization
 * service for tables.
 * </p>
 * 
 * <p>
 * Clients are required to store the following information in order to use the
 * api:
 * </p>
 * <p>
 * For each table: the client's tableID and the modificationNumber from the last
 * synchronization with Aggregate.
 * </p>
 * <p>
 * For each row of a table: Aggregate's aggregateRowIdentifier, the
 * revisionNumber from the last synchronization with Aggregate, and the data
 * that is contained in the row.
 * </p>
 * 
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
     * @throws ClientProtocolException
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
            throws ClientProtocolException, UserDoesNotExistException,
            IOException
    {
        super(aggregateURI, userID);
    }

    /**
     * Creates a new synchronized table.
     * 
     * @param tableID
     *            the client's unique identifier for the table
     * @param tableName
     *            the human readable name of the table
     * @param columns
     *            a list of columns defining the columns the table should have
     * @return the initial Modification of the newly created table (calling
     *         getRows() on the Modification will return an empty list).
     * @throws ClientProtocolException
     * @throws TableAlreadyExistsException
     *             if the caller has already created a synchronized table with
     *             the given tableID.
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws PermissionDeniedException
     */
    public Modification createSynchronizedTable(String tableID,
            String tableName, List<Column> columns)
            throws ClientProtocolException, IOException,
            PermissionDeniedException, TableAlreadyExistsException
    {
        CreateSynchronizedTable command = new CreateSynchronizedTable(
                requestingUserID, tableName, tableID, columns);
        CreateSynchronizedTableResult result = sendCommand(command,
                CreateSynchronizedTableResult.class);
        return result.getModification();
    }

    /**
     * Clones an existing synchronized table.
     * 
     * @param aggregateTableIdentifier
     *            the universally unique identifier of the table
     * @param tableID
     *            the unique identifier that the caller will use to identify the
     *            table
     * @return the current Modification of the table. The list returned by
     *         getRows() will be populated with aggregateRowIdentifier,
     *         revisionNumber, and data for the row. Make sure that all of this
     *         data is stored as it will be required for other API calls (see
     *         {@link SynchronizedAPI the top of this file} for a summary of
     *         client requirements for synchronized API usage).
     * @throws ClientProtocolException
     * @throws TableAlreadyExistsException
     *             if the caller has already registered a table with tableID
     * @throws TableDoesNotExistException
     *             if no table with Aggregate Identifier
     *             aggregateTableIdentifier exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public Modification cloneSynchronizedTable(String aggregateTableIdentifier,
            String tableID) throws ClientProtocolException, IOException,
            PermissionDeniedException, TableDoesNotExistException,
            TableAlreadyExistsException
    {
        CloneSynchronizedTable command = new CloneSynchronizedTable(
                requestingUserID, tableID, aggregateTableIdentifier);
        CloneSynchronizedTableResult result = sendCommand(command,
                CloneSynchronizedTableResult.class);
        return result.getModification();
    }

    /**
     * Removes the caller from synchronization with a table. The caller must
     * call {@link #cloneSynchronizedTable} to interact with the table again.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if the caller does not have a table registered with tableID
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void removeTableSynchronization(String tableID)
            throws ClientProtocolException, IOException,
            TableDoesNotExistException
    {
        RemoveTableSynchronization command = new RemoveTableSynchronization(
                requestingUserID, tableID);
        RemoveTableSynchronizationResult result = sendCommand(command,
                RemoveTableSynchronizationResult.class);
        result.checkResults();
    }

    /**
     * Completely deletes a synchronized table from Aggregate. All future
     * requests for the table will error for all users.
     * 
     * @param tableID
     *            the client's identifier for the table
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if no such table with tableID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have delete
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void deleteSynchronizedTable(String tableID)
            throws ClientProtocolException, IOException,
            PermissionDeniedException, TableDoesNotExistException
    {
        DeleteSynchronizedTable command = new DeleteSynchronizedTable(
                requestingUserID, tableID);
        DeleteSynchronizedTableResult result = sendCommand(command,
                DeleteSynchronizedTableResult.class);
        result.checkResults();
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
     *         populated with rowID, aggregateRowIdentifier, and revisionNumber.
     *         Make sure that all of this data is stored as it will be required
     *         for other API calls (see {@link SynchronizedAPI the top of this
     *         file} for a summary of client requirements for synchronized API
     *         usage).
     * @throws ClientProtocolException
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
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public Modification insertSynchronizedRows(String tableID,
            int modificationNumber, List<SynchronizedRow> newRows)
            throws ClientProtocolException, IOException, OutOfSynchException,
            TableDoesNotExistException, PermissionDeniedException
    {
        InsertSynchronizedRows command = new InsertSynchronizedRows(
                requestingUserID, tableID, modificationNumber, newRows);
        InsertSynchronizedRowsResult result = sendCommand(command,
                InsertSynchronizedRowsResult.class);
        return result.getModification();
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
     *            a list of synchronized rows which are populated
     *            aggregateRowIdentifiers, revisionNumbers,and data. These rows
     *            must already exist in Aggregate's copy of the table
     * @return a Modificaton whose modificationNumber represents the latest
     *         modification of the table in Aggregate. Calling getRows() on the
     *         Modification will return a list of rows where each row is
     *         populated with aggregateRowIdentifier and revisionNumber. Make
     *         sure that all of this data is stored as it will be required for
     *         other API calls (see {@link SynchronizedAPI the top of this file}
     *         for a summary of client requirements for synchronized API usage).
     * @throws ClientProtocolException
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
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws RowOutOfSynchException
     */
    public Modification updateSynchronizedRows(String tableID,
            int modificationNumber, List<SynchronizedRow> changedRows)
            throws ClientProtocolException, IOException,
            PermissionDeniedException, OutOfSynchException,
            TableDoesNotExistException, RowOutOfSynchException
    {
        UpdateSynchronizedRows command = new UpdateSynchronizedRows(
                requestingUserID, changedRows, tableID, modificationNumber);
        UpdateSynchronizedRowsResult result = sendCommand(command,
                UpdateSynchronizedRowsResult.class);
        return result.getModification();
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
     *         populated with aggregateRowIdentifier, revisionNumber, and data.
     *         Make sure that all of this data is stored as it will be required
     *         for other API calls (see {@link SynchronizedAPI the top of this
     *         file} for a summary of client requirements for synchronized API
     *         usage).
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if the caller has no table with tableID registered for
     *             synchronization
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public Modification synchronize(String tableID, int modificationNumber)
            throws ClientProtocolException, IOException,
            PermissionDeniedException, TableDoesNotExistException
    {
        Synchronize command = new Synchronize(requestingUserID, tableID,
                modificationNumber);
        SynchronizeResult result = sendCommand(command, SynchronizeResult.class);
        return result.getModification();
    }
}
