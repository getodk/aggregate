package org.opendatakit.aggregate.odktables.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opendatakit.aggregate.odktables.Command;
import org.opendatakit.aggregate.odktables.CommandConverter;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.CreateUser;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.DeleteUser;
import org.opendatakit.aggregate.odktables.command.GetUser;
import org.opendatakit.aggregate.odktables.command.InsertRows;
import org.opendatakit.aggregate.odktables.command.QueryForRows;
import org.opendatakit.aggregate.odktables.command.QueryForTables;
import org.opendatakit.aggregate.odktables.command.result.CreateTableResult;
import org.opendatakit.aggregate.odktables.command.result.CreateUserResult;
import org.opendatakit.aggregate.odktables.command.result.DeleteTableResult;
import org.opendatakit.aggregate.odktables.command.result.DeleteUserResult;
import org.opendatakit.aggregate.odktables.command.result.GetUserResult;
import org.opendatakit.aggregate.odktables.command.result.InsertRowsResult;
import org.opendatakit.aggregate.odktables.command.result.QueryForRowsResult;
import org.opendatakit.aggregate.odktables.command.result.QueryForTablesResult;

/**
 * <p>
 * AggregateConnection represents a connection to a running instance of ODK
 * Aggregate (see <a href="http://opendatakit.org">http://opendatakit.org</a>).
 * AggregateConnection can be used as a simple table management API for
 * Aggregate.
 * <p>
 * 
 * <p>
 * Understanding the basics:
 * </p>
 * 
 * <p>
 * A Table in Aggregate (as accessed through this API only) is defined by a
 * unique identifier, a name, and a set of columns which define names and types.
 * Each Table's identifier includes a userId and a tableId. For a given userId,
 * every tableId must be unique.
 * </p>
 * 
 * <p>
 * Important note: internally Aggregate converts all tableIds, userIds, and
 * column names to upper case. So make sure that these values are still unique
 * under these constraints.
 * </p>
 * 
 * <p>
 * The basic permissions system for the tables works like this:
 * <ul>
 * <li>Only the owner of a table may perform insert/update/delete operations on
 * it. This is enforced by keeping the userIds associated with tables secret so
 * that only a client in possession of the userId for a table may
 * insert/update/delete it.</li>
 * <li>Anyone can read data from anyone else. That is, anyone can see a list of
 * all the tables currently stored in Aggregate and download data from them.
 * This is allowed by uniquely identifying users through a second mechanism--the
 * userUri. Thus any table can be uniquely identified for read operations using
 * userUri + tableId, and insert/update/delete may only be performed using
 * userId + tableId.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * AggregateConnection is currently not thread safe.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class AggregateConnection
{

    private final URI aggregateURI;
    private final HttpClient client;

    /**
     * Constructs a new AggregateConnection.
     * 
     * @param aggregateURI
     *            the base URI of a running ODK Aggregate instance (e.g.
     *            'http://aggregate.opendatakit.org').
     */
    public AggregateConnection(URI aggregateURI)
    {
        this.client = new DefaultHttpClient();
        this.aggregateURI = aggregateURI;
    }

    /**
     * Creates a new user on the Aggregate instance represented by this
     * AggregateConnection.
     * 
     * @param userId
     *            the unique identifier of the user to create. This must consist
     *            of only letters, numbers, and underscores, and must not be
     *            empty or null. Note that internally Aggregate will convert
     *            this to upper case and it still must be unique after that
     *            conversion.
     * @param userName
     *            the human readable name of the user. Must not be empty of
     *            null.
     * @return the userId of the successfully created user
     * @throws UserAlreadyExistsException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String createUser(String userId, String userName)
            throws UserAlreadyExistsException, ClientProtocolException,
            IOException
    {
        CreateUser command = new CreateUser(userId, userName);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        CreateUserResult result = CommandConverter.getInstance()
                .deserializeResult(reader, CreateUserResult.class);
        reader.close();
        return result.getCreatedUserId();
    }

    /**
     * Retrieves the User associated with the given userId from the Aggregate
     * instance represented by this AggregateConnection.
     * 
     * @param userId
     *            the unique identifier of the user to retrieve the uri of
     * @return the User associated with userId
     * @throws ClientProtocolException
     * @throws IOException
     * @throws UserDoesNotExistException
     */
    public User getUser(String userId) throws ClientProtocolException,
            IOException, UserDoesNotExistException
    {
        GetUser command = new GetUser(userId);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        GetUserResult result = CommandConverter.getInstance()
                .deserializeResult(reader, GetUserResult.class);
        reader.close();
        String userUri = result.getUserUri();
        String userName = result.getUserName();
        User user = new User(userId, userUri, userName);
        return user;
    }

    /**
     * Deletes the user with the given userId from the Aggregate instance.
     * 
     * 
     * @param userId
     *            the unique identifier of the user to delete.
     * @return the userId of the successfully deleted user. Note that this
     *         command will succeed even if the user does not exist.
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String deleteUser(String userId) throws ClientProtocolException,
            IOException
    {
        DeleteUser command = new DeleteUser(userId);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        DeleteUserResult result = CommandConverter.getInstance()
                .deserializeResult(reader, DeleteUserResult.class);
        reader.close();
        return result.getDeletedUserId();
    }

    /**
     * Creates a new table on the Aggregate instance represented by this
     * AggregateConnection.
     * 
     * @param userId
     *            the unique identifier of the user who will own the table. This
     *            user must previously have been created by calling
     *            {@link #createUser}. The userId must consist of only letters,
     *            numbers, and underscores and must not be emtpy or null.
     * @param tableId
     *            the unique identifier of the table to create. This must
     *            consist of only letters, numbers, and underscores. Note that
     *            different case tableIds are not unique, e.g. 'table1' is
     *            equivalent to 'Table1' and 'TABLE1'. Must not be empty or
     *            null. Note that internally Aggregate will convert this to
     *            upper case and it still must be unique after that conversion.
     * @param tableName
     *            the name of the table to create. The only restrictions are
     *            that it must not be empty or null.
     * @param columns
     *            a list of the columns the new table will have. Must not be
     *            empty or null. Note that internally Aggregate will convert all
     *            column names to upper case and they must not clash after that
     *            conversion.
     * @throws IOException
     * @throws ClientProtocolException
     * @return the tableId of the newly created table
     * @throws UserDoesNotExistException
     *             if the user with the given userId does not exist
     * @throws TableAlreadyExistsException
     *             if a table with tableId already exists
     */
    public String createTable(String userId, String tableId, String tableName,
            List<Column> columns) throws ClientProtocolException, IOException,
            TableAlreadyExistsException, UserDoesNotExistException
    {
        CreateTable command = new CreateTable(userId, tableId, tableName,
                columns);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        CreateTableResult result = CommandConverter.getInstance()
                .deserializeResult(reader, CreateTableResult.class);
        reader.close();
        return result.getCreatedTableId();
    }

    /**
     * Lists all the tables that are currently stored on the Aggregate instance
     * represented by this AggregateConnection.
     * 
     * @return a TableList representing all the tables in the Aggregate instance
     * @throws ClientProtocolException
     * @throws IOException
     */
    public TableList listTables() throws ClientProtocolException, IOException
    {
        QueryForTables command = new QueryForTables();
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        QueryForTablesResult result = CommandConverter.getInstance()
                .deserializeResult(reader, QueryForTablesResult.class);
        reader.close();
        return result.getTableList();
    }

    /**
     * Returns a list of populated rows representing all the rows in the table
     * owned by the user with the given userUri and tableId.
     * 
     * @param userUri
     *            the public unique identifier of the user
     * @param tableId
     *            the unique identifier of the table
     * @return a populated list of all the rows in the table
     * @throws IOException
     * @throws ClientProtocolException
     * @throws TableDoesNotExistException
     * @throws UserDoesNotExistException
     */
    public List<Row> getRows(String userUri, String tableId)
            throws ClientProtocolException, IOException,
            TableDoesNotExistException, UserDoesNotExistException
    {
        QueryForRows command = new QueryForRows(userUri, tableId);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        QueryForRowsResult result = CommandConverter.getInstance()
                .deserializeResult(reader, QueryForRowsResult.class);
        reader.close();
        return result.getRows();
    }

    /**
     * Inserts the given rows into the table with the given userId and tableId.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. This
     *            user must previously have been created by calling
     *            {@link #createUser}. The userId must consist of only letters,
     *            numbers, and underscores and must not be emtpy or null.
     * @param tableId
     *            the unique identifier of the table to insert rows into. Must
     *            not be null or empty.
     * @param rows
     *            a list of rows to insert into the table. Must not be null or
     *            empty, and each row must be a new row in the table (i.e. no
     *            row with a matching rowId can exist).
     * @throws IOException
     * @throws ClientProtocolException
     * @return a list of row ids that were successfully inserted
     * @throws RowAlreadyExistsException
     * @throws TableDoesNotExistException
     */
    public List<String> insertRows(String userId, String tableId, List<Row> rows)
            throws ClientProtocolException, IOException,
            RowAlreadyExistsException, TableDoesNotExistException
    {
        InsertRows command = new InsertRows(userId, tableId, rows);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        InsertRowsResult result = CommandConverter.getInstance()
                .deserializeResult(reader, InsertRowsResult.class);
        reader.close();
        return result.getInsertedRowIds();
    }

    /**
     * Deletes the table with the given userId and tableId.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. This
     *            user must previously have been created by calling
     *            {@link #createUser}. The userId must consist of only letters,
     *            numbers, and underscores and must not be emtpy or null.
     * @param tableId
     *            the unique identifier of the table to delete. Must be non-null
     *            and non-empty.
     * @throws IOException
     * @throws ClientProtocolException
     * @return the table id of the successfully deleted table
     */
    public String deleteTable(String userId, String tableId)
            throws ClientProtocolException, IOException
    {
        DeleteTable command = new DeleteTable(userId, tableId);
        HttpResponse response = sendCommand(command);
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        DeleteTableResult result = CommandConverter.getInstance()
                .deserializeResult(reader, DeleteTableResult.class);
        reader.close();
        return result.getDeletedTableId();
    }

    /**
     * Sends the given command to the Aggregate instance represented by this
     * AggregateConnection.
     * 
     * @param command
     *            the command to send to the Aggregate instance.
     * @return the response from the command.
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse sendCommand(Command command)
            throws ClientProtocolException, IOException
    {
        URI uri = aggregateURI.resolve(command.getMethodPath());

        String json = CommandConverter.getInstance().serializeCommand(command);

        HttpPost post = new HttpPost(uri);
        HttpEntity entity = new StringEntity(json);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);

        return response;
    }
}
