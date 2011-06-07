package org.opendatakit.aggregate.odktables.command.logic;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.client.Column;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.result.CreateTableResult;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * CreateTableLogic encapsulates the logic necessary to validate and execute a
 * CreateTable Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class CreateTableLogic extends CommandLogic<CreateTable>
{

    private final CreateTable createTable;

    public CreateTableLogic(CreateTable createTable)
    {
        this.createTable = createTable;
    }

    @Override
    public CreateTableResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableIndex index = TableIndex.getInstance(cc);
        Users users = Users.getInstance(cc);

        String tableId = createTable.getTableId();
        String convertedTableId = convertTableId(tableId);
        String userId = createTable.getUserId();
        String convertedUserId = convertUserId(userId);

        if (index.tableExists(convertedUserId, convertedTableId))
        {
            return CreateTableResult.failure(userId, tableId,
                    FailureReason.TABLE_ALREADY_EXISTS);
        }
        if (!users.userExists(convertedUserId))
        {
            return CreateTableResult.failure(userId, tableId,
                    FailureReason.USER_DOES_NOT_EXIST);
        }

        List<DataField> fields = new ArrayList<DataField>();
        for (Column column : createTable.getColumns())
            fields.add(columnToDataField(column));

        index.createTable(convertedUserId, convertedTableId,
                createTable.getTableName(), fields);

        return CreateTableResult.success(userId, tableId);
    }

    @Override
    public String toString()
    {
        return createTable.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof CreateTableLogic))
            return false;
        CreateTableLogic o = (CreateTableLogic) obj;
        return o.createTable.equals(this.createTable);
    }

    @Override
    public int hashCode()
    {
        return 36 * createTable.hashCode();
    }
}
