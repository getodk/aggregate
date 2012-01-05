package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.UpdateTableProperties;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.common.UpdateTablePropertiesResult;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class UpdateTablePropertiesLogic extends
	CommandLogic<UpdateTableProperties> {

    private final UpdateTableProperties updateTableProperties;

    public UpdateTablePropertiesLogic(
	    UpdateTableProperties updateTableProperties) {
	this.updateTableProperties = updateTableProperties;
    }

    @Override
    public UpdateTablePropertiesResult execute(CallingContext cc)
	    throws AggregateInternalErrorException, SnafuException {
	try {
	    // get relation instances
	    TableEntries entries = TableEntries.getInstance(cc);
	    Users users = Users.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);

	    // get request data
	    String requestingUserID = updateTableProperties
		    .getRequestingUserID();
	    String tableID = updateTableProperties.getTableID();
	    String properties = updateTableProperties.getProperties();

	    // retrieve request user
	    InternalUser requestUser = users
		    .query("UpdateTablePropertiesLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    // retrieve mapping from user's tableID to aggregateTableIdentifier
	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("UpdateTablePropertiesLogic.execute")
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				requestUser.getAggregateIdentifier())
			.equal(UserTableMappings.TABLE_ID, tableID).get();
	    } catch (ODKDatastoreException e) {
		return UpdateTablePropertiesResult
			.failureTableDoesNotExist(tableID);
	    }

	    String aggregateTableIdentifier = mapping
		    .getAggregateTableIdentifier();

	    // check that the user has write permission on the table
	    if (!requestUser.hasPerm(aggregateTableIdentifier,
		    Permissions.WRITE)) {
		return UpdateTablePropertiesResult.failurePermissionDenied();
	    }

	    // update properties
	    InternalTableEntry entry = entries
		    .getEntity(aggregateTableIdentifier);
	    entry.setProperties(properties);
	    entry.save();
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}
	return UpdateTablePropertiesResult.success();
    }

}
