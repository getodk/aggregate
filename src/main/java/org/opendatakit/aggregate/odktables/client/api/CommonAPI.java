package org.opendatakit.aggregate.odktables.client.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;

/**
 * CommonAPI contains API calls that are common to both SimpleAPI and
 * SynchronizedAPI.
 */
public class CommonAPI
{

    /**
     * Constructs a new instance of CommonAPI, using the anonymous user for API
     * calls.
     * 
     * @param aggregateURI
     *            the URI of a running ODK Aggregate instance
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     *             or if it does not exist
     */
    public CommonAPI(URI aggregateURI)
    {
        throw new NotImplementedException();
    }

    /**
     * Constructs a new instance of CommonAPI, using the supplied user
     * identification for API calls.
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
    public CommonAPI(URI aggregateURI, String userID)
    {
        throw new NotImplementedException();
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
     * @throws PermissonDeniedException
     *             if the userID used to make the API call does not have write
     *             permission on the Users table
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public User createUser(String userID, String userName)
    {
        throw new NotImplementedException();
    }

    /**
     * Retrieves the User with userID.
     * 
     * @param userID
     *            the unique private identifier of the user
     * @return the User with userID.
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
    public User getUserByID(String userID)
    {
        throw new NotImplementedException();
    }

    /**
     * Retrieves the User with aggregateUserIdentifier.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user
     * @return the User with aggregateUserIdentifier. Note that trying to
     *         retrieve the userID from the returned User will throw an
     *         exception.
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
    {
        throw new NotImplementedException();
    }

    /**
     * Deletes the user with the given aggregateUserIdentifier, and all
     * associated permissions. Note that the user with given
     * aggregateUserIdentifier must have no links to any tables in Aggregate. A
     * user may delete themselves.
     * 
     * @param aggregateUserIdentifier
     *            the unique public identifier of the user to delete
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws PermissonDeniedException
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
    {
        throw new NotImplementedException();
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
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws TableDoesNotExistException
     *             if no table with aggregateTableIdentifier exists
     * @throws PermissonDeniedException
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
            boolean delete)
    {
        throw new NotImplementedException();
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
     * @throws UserDoesNotExistException
     *             if no user with aggregateUserIdentifier exists
     * @throws PermissonDeniedException
     *             if the userID used to make the API call does not have user
     *             management permissions.
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public void setUserManagementPermissions(String aggregateUserIdentifier,
            boolean allowed)
    {
        throw new NotImplementedException();
    }

    /**
     * @return a list of information on all tables the user who made the API
     *         call has permisison to read
     * @throws AggregateInternalErrorException
     *             if Aggregate encounters an internal error that causes the
     *             call to fail
     * @throws IOException
     *             if there is a problem communicating with the Aggregate server
     */
    public List<TableEntry> listAllTables()
    {
        throw new NotImplementedException();
    }
}
