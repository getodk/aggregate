package org.opendatakit.aggregate.odktables.client.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;
import org.opendatakit.aggregate.odktables.commandresult.simple.QueryForRowsResult;

/**
 * SimpleAPI contains API calls for using Aggregate as a simple table storage
 * service.
 */
public class SimpleAPI extends CommonAPI
{

    /**
     * Constructs a new instance of SimpleAPI, using the supplied user
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
    public SimpleAPI(URI aggregateURI, String userID)
            throws ClientProtocolException, UserDoesNotExistException,
            IOException, AggregateInternalErrorException
    {
        super(aggregateURI, userID);
    }

    /**
     * Creates a new simple table. The userID used to make the API call will
     * become the owner of the table, and automatically be granted all
     * permissions.
     * 
     * @param tableID
     *            the identifier which you will use to track the table on the
     *            client side
     * @param tableName
     *            the human readable name of the table
     * @param columns
     *            a list of columns defining the columns the table should have
     * @return the aggregateTableIdentifier of the table, which is universally
     *         unique.
     * @throws ClientProtocolException
     * @throws TableAlreadyExistsException
     *             if you have already created a table with tableID
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws UserDoesNotExistException
     */
    public String createTable(String tableID, String tableName,
            List<Column> columns) throws ClientProtocolException, IOException,
            TableAlreadyExistsException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        CreateTable command = new CreateTable(requestingUserID, tableName,
                tableID, columns);
        CreateTableResult result = sendCommand(command, CreateTableResult.class);
        return result.getCreatedTableId();
    }

    /**
     * Deletes a table.
     * 
     * @param tableID
     *            the caller's identifier for the table
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if the table does not exist
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have delete
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void deleteTable(String tableID) throws ClientProtocolException,
            IOException, PermissionDeniedException, TableDoesNotExistException,
            AggregateInternalErrorException
    {
        DeleteTable command = new DeleteTable(requestingUserID, tableID);
        DeleteTableResult result = sendCommand(command, DeleteTableResult.class);
        result.checkResults();
    }

    /**
     * @return a list of all rows in the table with tableID
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if no table with tableID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws UserDoesNotExistException
     */
    public List<Row> getAllRows(String tableID) throws ClientProtocolException,
            IOException, TableDoesNotExistException, UserDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        QueryForRows command = new QueryForRows(requestingUserID, tableID);
        QueryForRowsResult result = sendCommand(command,
                QueryForRowsResult.class);
        return result.getRows();
    }

    /**
     * Inserts the given rows into the given table.
     * 
     * @param tableID
     *            the caller's identifier for a table
     * @param rows
     *            the rows to insert into the table. The rowIDs of these rows
     *            should not have previously been inserted into the table.
     * @return a map of the rowIDs inserted to the aggregateRowIdentifiers
     *         generated by Aggregate. You must store these
     *         aggregateRowIdentifiers in order to identify the rows to
     *         Aggregate in the future.
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     *             if no table with tableID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public Map<String, String> insertRows(String tableID, List<Row> rows)
            throws ClientProtocolException, IOException,
            TableDoesNotExistException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        InsertRows command = new InsertRows(requestingUserID, rows, tableID);
        InsertRowsResult result = sendCommand(command, InsertRowsResult.class);
        return result.getMapOfInsertedRowIDsToAggregateRowIdentifiers();
    }
}
