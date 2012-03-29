package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.QueryForRowsResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * QueryForRowsLogic encapsulates the logic necessary to validate and execute a
 * QueryForRows Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class QueryForRowsLogic extends CommandLogic<QueryForRows> {

    private QueryForRows queryForRows;

    public QueryForRowsLogic(QueryForRows queryForRows) {
	this.queryForRows = queryForRows;
    }

    @Override
    public QueryForRowsResult execute(CallingContext cc)
	    throws AggregateInternalErrorException {
	List<Row> clientRows;
	try {
	    TableEntries entries = TableEntries.getInstance(cc);
	    Users users = Users.getInstance(cc);
	    Columns columns = Columns.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);

	    String requestingUserID = queryForRows.getRequestingUserID();
	    String tableID = queryForRows.getTableID();

	    InternalUser requestingUser = users
		    .query("QueryForRowsLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    String aggregateRequestingUserIdentifier = requestingUser
		    .getAggregateIdentifier();

	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("QueryForRowsLogic.execute")
			.equal(UserTableMappings.TABLE_ID, tableID)
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				aggregateRequestingUserIdentifier).get();
	    } catch (ODKDatastoreException e) {
		return QueryForRowsResult.failure(tableID,
			FailureReason.TABLE_DOES_NOT_EXIST);
	    }

	    String aggregateTableIdentifier = mapping
		    .getAggregateTableIdentifier();

	    if (!requestingUser.hasPerm(aggregateTableIdentifier,
		    Permissions.READ)) {
		return QueryForRowsResult.failure(tableID,
			FailureReason.PERMISSION_DENIED);
	    }

	    entries.getEntity(aggregateTableIdentifier);

	    Table table = Table.getInstance(aggregateTableIdentifier, cc);
	    List<InternalRow> rows = table.query("QueryForRowsLogic.execute")
		    .execute();

	    @SuppressWarnings("unchecked")
	    List<String> columnNames = (List<String>) columns
		    .query("QueryForRowsLogic.execute")
		    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier)
		    .getDistinct(Columns.COLUMN_NAME);
	    clientRows = new ArrayList<Row>();

	    for (InternalRow row : rows) {
		Row clientRow = new Row();
		clientRow.setAggregateRowIdentifier(row
			.getAggregateIdentifier());
		for (String columnName : columnNames) {
		    InternalColumn column = columns
			    .query("QueryForRowsLogic.execute")
			    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
				    aggregateTableIdentifier)
			    .equal(Columns.COLUMN_NAME, columnName).get();
		    clientRow.setValue(columnName,
			    row.getValue(column.getAggregateIdentifier()));
		}
		clientRows.add(clientRow);
	    }
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}

	return QueryForRowsResult.success(clientRows);
    }
}
