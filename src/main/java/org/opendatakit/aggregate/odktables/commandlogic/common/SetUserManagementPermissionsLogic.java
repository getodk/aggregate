package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.common.SetUserManagementPermissions;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.common.SetUserManagementPermissionsResult;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SetUsersPermissionsLogic encapsulates the logic necessary to validate and
 * execute a SetUsersPermissions command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SetUserManagementPermissionsLogic extends
	CommandLogic<SetUserManagementPermissions> {

    private final SetUserManagementPermissions setUsersPermissions;

    public SetUserManagementPermissionsLogic(
	    SetUserManagementPermissions setUsersPermissions) {
	this.setUsersPermissions = setUsersPermissions;
    }

    @Override
    public SetUserManagementPermissionsResult execute(CallingContext cc)
	    throws AggregateInternalErrorException {
	try {
	    // get relation instances
	    Users users = Users.getInstance(cc);
	    Permissions permissions = Permissions.getInstance(cc);

	    // get request data
	    String requestingUserID = this.setUsersPermissions
		    .getRequestingUserID();
	    String aggregateUserIdentifier = this.setUsersPermissions
		    .getAggregateUserIdentifier();

	    // retrieve request user
	    InternalUser requestingUser = users
		    .query("SetUserManagementPermissionsLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    // To set user management permissions the user must have write
	    // permission on the users table
	    if (!requestingUser.hasPerm(users.getAggregateIdentifier(),
		    Permissions.WRITE)) {
		return SetUserManagementPermissionsResult.failure(
			aggregateUserIdentifier,
			FailureReason.PERMISSION_DENIED);
	    }

	    try {
		setUserManagementPermissions(users, permissions,
			aggregateUserIdentifier,
			setUsersPermissions.getAllowed());
	    } catch (UserDoesNotExistException e) {
		return SetUserManagementPermissionsResult.failure(
			aggregateUserIdentifier,
			FailureReason.USER_DOES_NOT_EXIST);
	    }
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}

	return SetUserManagementPermissionsResult.success();
    }

    public static void setUserManagementPermissions(Users users,
	    Permissions permissions, String aggregateUserIdentifier,
	    boolean allowed) throws ODKDatastoreException,
	    UserDoesNotExistException {
	// check if the user we are setting permissions for exists
	try {
	    users.getEntity(aggregateUserIdentifier);
	} catch (ODKDatastoreException e) {
	    throw new UserDoesNotExistException(null);
	}

	// see if permission exists, if not create it
	InternalPermission perm;
	try {
	    perm = permissions
		    .query("SetUserManagementPermissionsLogic.setUserManagementPermissions")
		    .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
			    users.getAggregateIdentifier())
		    .equal(Permissions.AGGREGATE_USER_IDENTIFIER,
			    aggregateUserIdentifier).get();
	    perm.setRead(allowed);
	    perm.setWrite(allowed);
	    perm.setDelete(allowed);
	} catch (ODKDatastoreException e) {
	    perm = new InternalPermission(users.getAggregateIdentifier(),
		    aggregateUserIdentifier, allowed, allowed, allowed,
		    users.getCC());
	}
	perm.save();
    }
}