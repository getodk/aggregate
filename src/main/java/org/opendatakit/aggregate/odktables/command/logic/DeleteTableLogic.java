package org.opendatakit.aggregate.odktables.command.logic;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.result.DeleteTableResult;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteTableLogic encapsulates the logic necessary to validate and execute a
 * DeleteTable Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteTableLogic extends CommandLogic<DeleteTable>
{

    private final DeleteTable deleteTable;

    public DeleteTableLogic(DeleteTable deleteTable)
    {
        this.deleteTable = deleteTable;
    }

    @Override
    public DeleteTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableIndex index = TableIndex.getInstance(cc);
        String convertedUserId = convertUserId(deleteTable.getUserId());
        String convertedTableId = convertTableId(deleteTable.getTableId());
        index.deleteTable(convertedUserId, convertedTableId);
        return DeleteTableResult.success(deleteTable.getTableId());
    }
}
