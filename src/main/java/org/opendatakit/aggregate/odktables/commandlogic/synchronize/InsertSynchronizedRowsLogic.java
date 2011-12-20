package org.opendatakit.aggregate.odktables.commandlogic.synchronize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.synchronize.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogicFunctions;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.synchronize.InsertSynchronizedRowsResult;
import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.aggregate.odktables.entity.InternalTableEntry;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.UserTableMappings;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * InsertSynchronizedRowsLogic encapsulates the logic necessary to validate and
 * execute a InsertSynchronizedRows command.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InsertSynchronizedRowsLogic extends
	CommandLogic<InsertSynchronizedRows> {

    private final InsertSynchronizedRows insertSynchronizedRows;

    public InsertSynchronizedRowsLogic(
	    InsertSynchronizedRows insertSynchronizedRows) {
	this.insertSynchronizedRows = insertSynchronizedRows;
    }

    @Override
    public InsertSynchronizedRowsResult execute(CallingContext cc)
	    throws AggregateInternalErrorException {
	Modification clientModification;
	try {
	    // get relation instances
	    Users users = Users.getInstance(cc);
	    TableEntries entries = TableEntries.getInstance(cc);
	    UserTableMappings mappings = UserTableMappings.getInstance(cc);
	    Columns columns = Columns.getInstance(cc);

	    // get request data
	    String requestingUserID = insertSynchronizedRows
		    .getRequestingUserID();
	    String tableID = insertSynchronizedRows.getTableID();
	    int clientModificationNumber = insertSynchronizedRows
		    .getModificationNumber();
	    List<SynchronizedRow> newRows = insertSynchronizedRows.getNewRows();

	    // retrieve request user
	    InternalUser requestUser = users.getByID(requestingUserID);

	    // retrieve mapping from user's tableID to aggregateTableIdentifier
	    InternalUserTableMapping mapping;
	    try {
		mapping = mappings
			.query("InsertSynchronizedRowsLogic.execute")
			.equal(UserTableMappings.AGGREGATE_USER_IDENTIFIER,
				requestUser.getAggregateIdentifier())
			.equal(UserTableMappings.TABLE_ID, tableID).get();
	    } catch (ODKDatastoreException e) {
		return InsertSynchronizedRowsResult.failure(tableID,
			FailureReason.TABLE_DOES_NOT_EXIST);
	    }

	    String aggregateTableIdentifier = mapping
		    .getAggregateTableIdentifier();

	    // in order to insert rows the user must have write permission on
	    // the table
	    if (!requestUser.hasPerm(aggregateTableIdentifier,
		    Permissions.WRITE)) {
		return InsertSynchronizedRowsResult.failure(tableID,
			FailureReason.PERMISSION_DENIED);
	    }

	    InternalTableEntry entry = entries
		    .getEntity(aggregateTableIdentifier);

	    // make sure that the modification number of the user's table is up
	    // to date with aggregate
	    if (entry.getModificationNumber() != clientModificationNumber) {
		return InsertSynchronizedRowsResult.failure(tableID,
			FailureReason.OUT_OF_SYNCH);
	    }

	    // set new modification number
	    int newModificationNumber = clientModificationNumber + 1;
	    CommandLogicFunctions.updateModificationNumber(entry,
		    aggregateTableIdentifier, newModificationNumber, cc);

	    // get columns of table
	    List<InternalColumn> cols = columns
		    .query("InsertSynchronizedRows.execute")
		    .equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
			    aggregateTableIdentifier).execute();

	    // Insert new rows and create modification
	    try {
		clientModification = insertNewRows(newRows,
			aggregateTableIdentifier, newModificationNumber, cols,
			cc);
	    } catch (ColumnDoesNotExistException e) {
		// revert modification number and return failure
		CommandLogicFunctions.updateModificationNumber(entry,
			aggregateTableIdentifier, clientModificationNumber, cc);
		return InsertSynchronizedRowsResult.failure(tableID,
			e.getBadColumnName());
	    }
	} catch (ODKDatastoreException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	} catch (ODKTaskLockException e) {
	    throw new AggregateInternalErrorException(e.getMessage());
	}

	return InsertSynchronizedRowsResult.success(clientModification);
    }

    private Modification insertNewRows(List<SynchronizedRow> newRows,
	    String aggregateTableIdentifier, int newModificationNumber,
	    List<InternalColumn> cols, CallingContext cc)
	    throws ODKDatastoreException, ColumnDoesNotExistException {
	List<SynchronizedRow> insertedRows = new ArrayList<SynchronizedRow>();
	List<TypedEntity> entitiesToSave = new ArrayList<TypedEntity>();
	for (SynchronizedRow clientRow : newRows) {
	    // Convert client's row into an internal row and save to datastore
	    InternalRow row = new InternalRow(aggregateTableIdentifier, cc);
	    for (Entry<String, String> rowEntry : clientRow
		    .getColumnValuePairs().entrySet()) {
		InternalColumn col;
		col = InternalColumn.search(cols, rowEntry.getKey());
		if (col == null) {
		    throw new ColumnDoesNotExistException(null,
			    rowEntry.getKey());
		}
		row.setValue(col.getAggregateIdentifier(), rowEntry.getValue());
	    }
	    entitiesToSave.add(row);

	    // Add row to this modification
	    String aggregateRowIdentifier = row.getAggregateIdentifier();
	    InternalModification modification = new InternalModification(
		    aggregateTableIdentifier, newModificationNumber,
		    aggregateRowIdentifier, cc);
	    entitiesToSave.add(modification);

	    // Create the row that will be sent back to the client
	    SynchronizedRow insertedRow = new SynchronizedRow();
	    insertedRow.setRowID(clientRow.getRowID());
	    insertedRow.setAggregateRowIdentifier(aggregateRowIdentifier);
	    insertedRow.setRevisionTag(row.getRevisionTag());
	    insertedRows.add(insertedRow);
	}
	Modification clientModification = new Modification(
		newModificationNumber, insertedRows);

	// save entities
	boolean success = CommandLogicFunctions.saveEntities(entitiesToSave);
	if (!success)
	    throw new SnafuException("Could not save entities: "
		    + entitiesToSave);

	return clientModification;
    }
}