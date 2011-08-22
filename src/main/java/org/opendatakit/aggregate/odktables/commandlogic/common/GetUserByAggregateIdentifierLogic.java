package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.GetUserByAggregateIdentifier;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.GetUserByAggregateIdentifierResult;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserByAggregate IdentifierLogic encapsulates the logic necessary to
 * validate and execute a GetUserByAggregate Identifier command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class GetUserByAggregateIdentifierLogic extends
        CommandLogic<GetUserByAggregateIdentifier>
{

    private final GetUserByAggregateIdentifier getUserByAggregateIdentifier;

    public GetUserByAggregateIdentifierLogic(
            GetUserByAggregateIdentifier getUserByAggregateIdentifier)
    {
        this.getUserByAggregateIdentifier = getUserByAggregateIdentifier;
    }

    @Override
    public GetUserByAggregateIdentifierResult execute(CallingContext cc)
            throws AggregateInternalErrorException
    {
        User retrievedUser;
        try
        {
            Users users = Users.getInstance(cc);
    
            String aggregateUserIdentifier = this.getUserByAggregateIdentifier
                    .getAggregateUserIdentifier();
            String requestingUserID = this.getUserByAggregateIdentifier
                    .getRequestingUserID();
            String usersTable = users.getAggregateIdentifier();
    
            InternalUser requestUser = users.query()
                    .equal(Users.USER_ID, requestingUserID).get();
            if (!requestUser.hasPerm(usersTable, Permissions.READ))
            {
                return GetUserByAggregateIdentifierResult.failure(
                        aggregateUserIdentifier, FailureReason.PERMISSION_DENIED);
            }
    
            InternalUser user = null;
            try
            {
                user = users.getEntity(aggregateUserIdentifier);
            } catch (ODKDatastoreException e)
            {
                return GetUserByAggregateIdentifierResult.failure(
                        aggregateUserIdentifier, FailureReason.USER_DOES_NOT_EXIST);
            }
    
            // set userID to null since we don't want people finding it out
            retrievedUser = new User(null,
                    user.getAggregateIdentifier(), user.getName());
        }
        catch (ODKDatastoreException e)
        {
           throw new AggregateInternalErrorException(e.getMessage()); 
        }

        return GetUserByAggregateIdentifierResult.success(retrievedUser);
    }
}