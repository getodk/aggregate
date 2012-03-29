package org.opendatakit.aggregate.odktables.commandlogic.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.UpdateColumnProperties;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.common.UpdateColumnPropertiesResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class UpdateColumnPropertiesLogic extends
	CommandLogic<UpdateColumnProperties> {

    private final UpdateColumnProperties updateColumnProperties;

    public UpdateColumnPropertiesLogic(
	    UpdateColumnProperties updateColumnProperties) {
	this.updateColumnProperties = updateColumnProperties;
    }

    @Override
    public UpdateColumnPropertiesResult execute(CallingContext cc)
	    throws AggregateInternalErrorException, SnafuException {
	try {
	    // get relation instances
	    Columns columns = Columns.getInstance(cc);
	    Users users = Users.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);

	    // get request data
	    String requestingUserID = updateColumnProperties
		    .getRequestingUserID();
	    String tableID = updateColumnProperties.getTableID();
	    Map<String, String> columnsToProperties = updateColumnProperties
		    .getColumnsToProperties();

	    // retrieve request user
	    InternalUser requestUser = users
		    .query("UpdateColumnPropertiesLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    // retrieve mapping from user's tableID to aggregateTableIdentifier
	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("UpdateColumnPropertiesLogic.execute")
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				requestUser.getAggregateIdentifier())
			.equal(UserTableMappings.TABLE_ID, tableID).get();
	    } catch (ODKDatastoreException e) {
		return UpdateColumnPropertiesResult
			.failureTableDoesNotExist(tableID);
	    }

	    String aggregateTableIdentifier = mapping
		    .getAggregateTableIdentifier();

	    // check that the user has write permission on the table
	    if (!requestUser.hasPerm(aggregateTableIdentifier,
		    Permissions.WRITE)) {
		return UpdateColumnPropertiesResult.failurePermissionDenied();
	    }

	    // retrieve the columns for the table
	    List<InternalColumn> internalColumns = columns
		    .query("UpdateColumnProperties.execute")
		    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier).execute();
	    List<TypedEntity> columnsToSave = new ArrayList<TypedEntity>();
	    for (Entry<String, String> entry : columnsToProperties.entrySet()) {
		String columnName = entry.getKey();
		InternalColumn column = InternalColumn.search(internalColumns,
			columnName);
		if (column == null) {
		    return UpdateColumnPropertiesResult
			    .failureColumnDoesNotExist(tableID, columnName);
		} else {
		    column.setProperties(entry.getValue());
		    columnsToSave.add(column);
		}
	    }

	    CommandLogicFunctions.saveEntities(columnsToSave);

	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}
	return UpdateColumnPropertiesResult.success();
    }

}
