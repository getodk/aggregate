package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.synchronize.RemoveTableSynchronization;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.RemoveTableSynchronizationResult;
import org.opendatakit.aggregate.odktables.entity.InternalFilter;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Filters;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * RemoveTableSynchronizationLogic encapsulates the logic necessary to validate
 * and execute a RemoveTableSynchronization command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class RemoveTableSynchronizationLogic extends
	CommandLogic<RemoveTableSynchronization> {

    private final RemoveTableSynchronization removeTableSynchronization;

    public RemoveTableSynchronizationLogic(
	    RemoveTableSynchronization removeTableSynchronization) {
	this.removeTableSynchronization = removeTableSynchronization;
    }

    @Override
    public RemoveTableSynchronizationResult execute(CallingContext cc)
	    throws AggregateInternalErrorException {
	try {
	    // get relation instances
	    Users users = Users.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);
	    Filters filters = Filters.getInstance(cc);

	    // get request data
	    String requestingUserID = removeTableSynchronization
		    .getRequestingUserID();
	    String tableID = removeTableSynchronization.getTableID();

	    // retrieve request user
	    InternalUser requestUser = users
		    .query("RemoveTableSynchronizationLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    // get mapping from user's tableID to the aggregateTableIdentifier
	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("RemoveTableSynchronizationLogic.execute")
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				requestUser.getAggregateIdentifier())
			.equal(UserTableMappings.TABLE_ID, tableID).get();
	    } catch (ODKDatastoreException e) {
		return RemoveTableSynchronizationResult.failure(tableID,
			FailureReason.TABLE_DOES_NOT_EXIST);
	    }

	    Collection<TypedEntity> entitiesToDelete = new ArrayList<TypedEntity>();

	    // add the mapping
	    entitiesToDelete.add(mapping);

	    // get any filters the user has
	    List<InternalFilter> clientFilters = filters
		    .query("RemoveTableSynchronizationLogic.execute")
		    .equal(Filters.AGGREGATE_USER_IDENTIFIER,
			    requestUser.getAggregateIdentifier())
		    .equal(Filters.AGGREGATE_TABLE_IDENTIFIER,
			    mapping.getAggregateTableIdentifier()).execute();
	    entitiesToDelete.addAll(clientFilters);

	    // delete the entities
	    CommandLogicFunctions.deleteEntities(entitiesToDelete);
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}

	return RemoveTableSynchronizationResult.success();
    }
}