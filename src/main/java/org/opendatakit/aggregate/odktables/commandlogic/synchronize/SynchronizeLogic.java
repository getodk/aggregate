package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.synchronize.Synchronize;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.SynchronizeResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalFilter;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Filters;
import org.opendatakit.aggregate.odktables.relation.Modifications;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityQuery;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * SynchronizeLogic encapsulates the logic necessary to validate and execute a
 * Synchronize command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class SynchronizeLogic extends CommandLogic<Synchronize> {

    private final Synchronize synchronize;

    public SynchronizeLogic(Synchronize synchronize) {
	this.synchronize = synchronize;
    }

    @Override
    public SynchronizeResult execute(CallingContext cc)
	    throws AggregateInternalErrorException {
	Modification modification;
	try {
	    // get relation instances
	    Users users = Users.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);
	    TableEntries entries = TableEntries.getInstance(cc);
	    Modifications modifications = Modifications.getInstance(cc);
	    Columns columns = Columns.getInstance(cc);
	    Filters filters = Filters.getInstance(cc);

	    // get request data
	    String requestingUserID = synchronize.getRequestingUserID();
	    String tableID = synchronize.getTableID();
	    int clientModificationNumber = synchronize.getModificationNumber();

	    // retrieve request user
	    InternalUser requestUser = users.query("SynchronizeLogic.execute")
		    .equal(Users.USER_ID, requestingUserID).get();

	    // retrieve mapping from user's tableID to aggregateTableIdentifer
	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("SynchronizeLogic.execute")
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				requestUser.getAggregateIdentifier())
			.equal(UserTableMappings.TABLE_ID, tableID).get();
	    } catch (ODKDatastoreException e) {
		return SynchronizeResult.failure(tableID,
			FailureReason.TABLE_DOES_NOT_EXIST);
	    }

	    String aggregateTableIdentifier = mapping
		    .getAggregateTableIdentifier();

	    // in order to get the latest rows the user must have read
	    // permission on the table
	    if (!requestUser
		    .hasPerm(aggregateTableIdentifier, Permissions.READ)) {
		return SynchronizeResult.failure(tableID,
			FailureReason.PERMISSION_DENIED);
	    }

	    // Get current modification number
	    InternalTableEntry entry = entries
		    .getEntity(aggregateTableIdentifier);
	    int currentModificationNumber = entry.getModificationNumber();

	    // Get latest modifications
	    List<InternalModification> latestModifications = modifications
		    .query("SynchronizeLogic.execute")
		    .equal(Modifications.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier)
		    .greaterThan(Modifications.MODIFICATION_NUMBER,
			    clientModificationNumber).execute();
	    Set<String> aggregateRowIdentifiers = new TreeSet<String>();

	    for (InternalModification mod : latestModifications) {
		aggregateRowIdentifiers.add(mod.getAggregateRowIdentifier());
	    }

	    // Retrieve filters
	    List<InternalFilter> clientFilters = filters
		    .query("SynchronizeTableLogic.execute")
		    .equal(Filters.AGGREGATE_USER_IDENTIFIER,
			    requestUser.getAggregateIdentifier())
		    .equal(Filters.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier).execute();

	    // Retrieve rows
	    Table table = Table.getInstance(aggregateTableIdentifier, cc);
	    List<InternalColumn> cols = columns
		    .query("SynchronizeLogic.execute")
		    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier).execute();

	    List<InternalRow> rows = new ArrayList<InternalRow>();
	    if (!aggregateRowIdentifiers.isEmpty()) {
		TypedEntityQuery<InternalRow> query = table
			.query("SynchronizeTableLogic.execute");
		query.include(CommonFieldsBase.URI_COLUMN_NAME,
			aggregateRowIdentifiers).execute();

		for (InternalFilter filter : clientFilters) {
		    InternalColumn col = InternalColumn.search(cols,
			    filter.getColumnName());
		    String columnName = Table.convertIdentifier(col
			    .getAggregateIdentifier());
		    Object value = CommandLogicFunctions.convert(table
			    .getAttribute(columnName).getType(), filter
			    .getValue());
		    query.addFilter(columnName, filter.getFilterOperation(),
			    value);
		}
		rows = query.execute();
	    }

	    // Convert rows to SynchronizedRow
	    List<SynchronizedRow> latestRows = CommandLogicFunctions.convert(
		    rows, cols);

	    modification = new Modification(currentModificationNumber,
		    latestRows);
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}

	return SynchronizeResult.success(modification);
    }
}