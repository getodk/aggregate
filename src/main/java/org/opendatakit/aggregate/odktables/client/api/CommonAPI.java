package org.opendatakit.aggregate.odktables.client.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.Http404Exception;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.command.common.CheckUserExists;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.common.GetUserByAggregateIdentifier;
import org.opendatakit.aggregate.odktables.command.common.GetUserByID;
import org.opendatakit.aggregate.odktables.command.common.QueryForTables;
import org.opendatakit.aggregate.odktables.command.common.SetTablePermissions;
import org.opendatakit.aggregate.odktables.command.common.SetUserManagementPermissions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.aggregate.odktables.commandresult.common.CheckUserExistsResult;
import org.opendatakit.aggregate.odktables.commandresult.common.CreateUserResult;
import org.opendatakit.aggregate.odktables.commandresult.common.DeleteUserResult;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserByAggregateIdentifierResult;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserByIDResult;
import org.opendatakit.aggregate.odktables.commandresult.common.QueryForTablesResult;
import org.opendatakit.aggregate.odktables.commandresult.common.SetTablePermissionsResult;
import org.opendatakit.aggregate.odktables.commandresult.common.SetUserManagementPermissionsResult;
import org.opendatakit.common.utils.Check;

/**
 * CommonAPI contains API calls that are common to both SimpleAPI and
 * SynchronizedAPI.
 */
public class CommonAPI
{

    protected String requestingUserID;
    private final URI aggregateURI;
    private final HttpClient client;

    /**
     * Constructs a new instance of CommonAPI, using the supplied user
     * identification for API calls.
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
    public CommonAPI(URI aggregateURI, String userID)
            throws ClientProtocolException, IOException,
            UserDoesNotExistException, AggregateInternalErrorException
    {
        Check.notNull(aggregateURI, "aggregateURI");
        Check.notNullOrEmpty(userID, "userID");
        
        this.aggregateURI = aggregateURI;
        this.requestingUserID = userID;
        this.client = new DefaultHttpClient();
        
        checkUserExists(userID);
    }

    /**
     * Sets the userID to use for API calls
     * 
     * @param userID
     *            the ID of the user to use for API calls
     * @throws UserDoesNotExistException
     *             if no user with userID exists in Aggregate
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws ClientProtocolException
     */
    public void setUserID(String userID) throws ClientProtocolException,
            AggregateInternalErrorException, UserDoesNotExistException,
            IOException
    {
        checkUserExists(userID);
        this.requestingUserID = userID;
    }

    private void checkUserExists(String userID) throws ClientProtocolException,
            AggregateInternalErrorException, IOException,
            UserDoesNotExistException
    {
        CheckUserExists checkUserExists = new CheckUserExists(userID);
        CheckUserExistsResult result = sendCommand(checkUserExists,
                CheckUserExistsResult.class);
        if (!result.getUserExists())
            throw new UserDoesNotExistException("null and userID " + userID);
    }

    /**
     * Creates a new user of the odktables API.
     * 
     * @param userID
     *            a universally unique identifier for the new user. This should
     *            be kept private.
     * @param userName
     *            the human readable name for the new user.
     * @return the newly created User.
     * @throws UserAlreadyExistsException
     *             if a user with userID already exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the Users table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     * @throws ClientProtocolException
     */
    public User createUser(String userID, String userName)
            throws ClientProtocolException, IOException,
            UserAlreadyExistsException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        CreateUser createUser = new CreateUser(requestingUserID, userName,
                userID);
        CreateUserResult result = sendCommand(createUser,
                CreateUserResult.class);
        return result.getCreatedUser();
    }

    /**
     * Retrieves the User with userID.
     * 
     * @param userID
     *            the unique private identifier of the user
     * @return the User with userID.
     * @throws ClientProtocolException
     * @throws UserDoesNotExistException
     *             if no user with userID exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the Users table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public User getUserByID(String userID) throws ClientProtocolException,
            IOException, PermissionDeniedException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        GetUserByID getUserByID = new GetUserByID(requestingUserID, userID);
        GetUserByIDResult result = sendCommand(getUserByID,
                GetUserByIDResult.class);
        return result.getUser();
    }

    /**
     * Retrieves the User with aggregateUserIdentifier.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user
     * @return the User with aggregateUserIdentifier. Note that trying to
     *         retrieve the userID from the returned User will throw an
     *         exception.
     * @throws ClientProtocolException
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have read
     *             permission on the Users table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public User getUserByAggregateIdentifier(String aggregateUserIdentifier)
            throws ClientProtocolException, IOException,
            PermissionDeniedException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        GetUserByAggregateIdentifier getUserByAggregateIdentifier = new GetUserByAggregateIdentifier(
                requestingUserID, aggregateUserIdentifier);
        GetUserByAggregateIdentifierResult result = sendCommand(
                getUserByAggregateIdentifier,
                GetUserByAggregateIdentifierResult.class);
        return result.getUser();
    }

    /**
     * Deletes the user with the given aggregateUserIdentifier, and all
     * associated permissions. Note that the user with given
     * aggregateUserIdentifier must have no links to any tables in Aggregate. A
     * user may delete themselves.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user to delete
     * @throws ClientProtocolException
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws PermissionDeniedException
     *             if the userID used to make the call does not have delete
     *             permission on the Users table
     * @throws CannotDeleteException
     *             if the user still owns one or more tables or is tracking one
     *             or more synchronized tables
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void deleteUser(String aggregateUserIdentifier)
            throws PermissionDeniedException, UserDoesNotExistException,
            CannotDeleteException, ClientProtocolException, IOException,
            AggregateInternalErrorException
    {
        DeleteUser deleteUser = new DeleteUser(requestingUserID,
                aggregateUserIdentifier);
        DeleteUserResult result = sendCommand(deleteUser,
                DeleteUserResult.class);
        result.getDeletedAggregateUserIdentifier();
    }

    /**
     * Sets permissions for the given user on the given table.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user
     * @param aggregateTableIdentifier
     *            the unique identifier of the table
     * @param read
     *            true if the user is allowed to read from the table
     * @param write
     *            true if the user is allowed to write to the table
     * @param delete
     *            true if the user is allowed to delete rows from the table or
     *            delete the table
     * @throws ClientProtocolException
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws TableDoesNotExistException
     *             if no table with aggregateTableIdentifier exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the table.
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void setTablePermissions(String aggregateUserIdentifier,
            String aggregateTableIdentifier, boolean read, boolean write,
            boolean delete) throws ClientProtocolException, IOException,
            PermissionDeniedException, UserDoesNotExistException,
            TableDoesNotExistException, AggregateInternalErrorException

    {
        SetTablePermissions setTablePermissions = new SetTablePermissions(
                requestingUserID, aggregateTableIdentifier,
                aggregateUserIdentifier, read, write, delete);
        SetTablePermissionsResult result = sendCommand(setTablePermissions,
                SetTablePermissionsResult.class);
        result.checkResult();
    }

    /**
     * Sets the permissions for a given user to create, edit, and delete other
     * users.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user
     * @param allowed
     *            true if the user should be allowed to create, edit, and delete
     *            other users.
     * @throws ClientProtocolException
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws PermissionDeniedException
     *             if the userID used to make the API call does not have user
     *             management permissions.
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void setUserManagementPermissions(String aggregateUserIdentifier,
            boolean allowed) throws ClientProtocolException, IOException,
            PermissionDeniedException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        SetUserManagementPermissions command = new SetUserManagementPermissions(
                requestingUserID, aggregateUserIdentifier, allowed);
        SetUserManagementPermissionsResult result = sendCommand(command,
                SetUserManagementPermissionsResult.class);
        result.checkResult();
    }

    /**
     * @return a list of information on all tables the user who made the API
     *         call has permisison to read
     * @throws ClientProtocolException
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public List<TableEntry> listAllTables() throws ClientProtocolException,
            IOException, AggregateInternalErrorException
    {
        QueryForTables command = new QueryForTables(requestingUserID);
        QueryForTablesResult result = sendCommand(command,
                QueryForTablesResult.class);
        return result.getEntries();
    }

    /**
     * Sends the given command to the Aggregate instance represented by this
     * AggregateConnection.
     * 
     * @param command
     *            the command to send to the Aggregate instance.
     * @return the result from the command.
     * @throws ClientProtocolException
     * @throws IOException
     * @throws AggregateInternalErrorException
     */
    protected <T extends CommandResult<?>> T sendCommand(Command command,
            Class<T> commandResultClass) throws ClientProtocolException,
            IOException, AggregateInternalErrorException
    {
        URI uri = aggregateURI.resolve("/odktables" + command.getMethodPath());
        String json = CommandConverter.getInstance().serializeCommand(command);
        HttpPost post = new HttpPost(uri);
        HttpEntity entity = new StringEntity(json);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() / 100 == 5)
        {
            response.getEntity().consumeContent();
            throw new AggregateInternalErrorException(status.getReasonPhrase());
        }
        if (status.getStatusCode() == 404)
        {
            response.getEntity().consumeContent();
            throw new Http404Exception(status.getReasonPhrase());
        }
        Reader reader = new InputStreamReader(response.getEntity().getContent());
        T result = CommandConverter.getInstance().deserializeResult(reader,
                commandResultClass);
        reader.close();
        return result;
    }

}
