package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.DeleteUserResult;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteUserLogic encapsulates the logic necessary to validate and execute a
 * DeleteUser Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteUserLogic extends CommandLogic<DeleteUser>
{

    private DeleteUser deleteUser;

    public DeleteUserLogic(DeleteUser deleteUser)
    {
        this.deleteUser = deleteUser;
    }

    @Override
    public DeleteUserResult execute(CallingContext cc)
            throws AggregateInternalErrorException
    {
        String aggregateUserIdentifier;
        try
        {
            // get relation instances
            Users users = Users.getInstance(cc);
            TableEntries tables = TableEntries.getInstance(cc);
            Permissions permissions = Permissions.getInstance(cc);

            // get request data
            aggregateUserIdentifier = this.deleteUser
                    .getAggregateUserIdentifier();
            String requestingUserID = this.deleteUser.getRequestingUserID();
            String usersTable = users.getAggregateIdentifier();

            // retrieve requesting user
            InternalUser requestingUser = users.query("DeleteUserLogic.execute")
                    .equal(Users.USER_ID, requestingUserID).get();

            // check if request user has permission to delete the user
            if (!requestingUser.hasPerm(usersTable, Permissions.DELETE))
            {
                return DeleteUserResult.failure(aggregateUserIdentifier,
                        FailureReason.PERMISSION_DENIED);
            }

            // delete user
            try
            {
                deleteUser(users, permissions, tables, aggregateUserIdentifier);
            } catch (UserDoesNotExistException e)
            {
                return DeleteUserResult.failure(aggregateUserIdentifier,
                        FailureReason.USER_DOES_NOT_EXIST);
            } catch (CannotDeleteException e)
            {
                return DeleteUserResult.failure(aggregateUserIdentifier,
                        FailureReason.CANNOT_DELETE);
            }
        } catch (ODKDatastoreException e)
        {
            throw new AggregateInternalErrorException(e.getMessage());
        }

        return DeleteUserResult.success(aggregateUserIdentifier);
    }

    public static void deleteUser(Users users, Permissions permissions,
            TableEntries tables, String aggregateUserIdentifier)
            throws UserDoesNotExistException, CannotDeleteException,
            ODKDatastoreException
    {
        List<TypedEntity> entitiesToDelete = new ArrayList<TypedEntity>();
        InternalUser user = null;
        try
        {
            user = users.getEntity(aggregateUserIdentifier);
        } catch (ODKDatastoreException e)
        {
            // user does not exist
            throw new UserDoesNotExistException(aggregateUserIdentifier);
        }

        if (tables
                .query("DeleteUserLogic.deleteUser")
                .equal(TableEntries.AGGREGATE_OWNER_IDENTIFIER,
                        aggregateUserIdentifier).exists())
        {
            // user still has some tables
            throw new CannotDeleteException(aggregateUserIdentifier);
        }

        List<InternalPermission> perms;
        try
        {
            perms = permissions
                    .query("DeleteUserLogic.deleteUser")
                    .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
                            aggregateUserIdentifier).execute();
        } catch (ODKDatastoreException e)
        {
            // TODO: retry delete?
            throw e;
        }

        for (InternalPermission perm : perms)
        {
            entitiesToDelete.add(perm);
        }
        entitiesToDelete.add(user);

        boolean success = CommandLogicFunctions
                .deleteEntities(entitiesToDelete);
        if (!success)
            throw new SnafuException("Could not delete entities: "
                    + entitiesToDelete);
    }
}
