package org.opendatakit.aggregate.odktables.commandlogic.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;
import org.opendatakit.aggregate.odktables.relation.Rows;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * InsertRowsLogic encapsulates the logic necessary to validate and execute a
 * InsertRows Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRowsLogic extends CommandLogic<InsertRows>
{

    private InsertRows insertRows;

    public InsertRowsLogic(InsertRows insertRows)
    {
        this.insertRows = insertRows;
    }

    @Override
    public InsertRowsResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableEntries index = TableEntries.getInstance(cc);
        String convertedUserId = convertUserId(insertRows.getUserId());
        String convertedTableId = convertTableId(insertRows.getTableId());
        if (!index.tableExists(convertedUserId, convertedTableId))
            return InsertRowsResult.failure(insertRows.getTableId());
        Rows table = index.getTable(convertedUserId, convertedTableId);
        List<Row> rows = insertRows.getRows();

        List<String> rowIds = new ArrayList<String>();
        List<Entity> entities = new ArrayList<Entity>();
        for (Row row : rows)
        {
            String rowId = row.getRowId();
            String rowURI = createRowURI(insertRows.getTableId(), rowId);

            // If row exists, then abort and return failure
            if (table.containsEntity(rowURI, cc))
                return InsertRowsResult.failure(insertRows.getTableId(), rowId);

            // The row does not already exist, so create the entity for it
            Entity entity = table.newEntity(rowURI, cc);
            for (Entry<String, String> entry : row.getColumnValuePairs()
                    .entrySet())
            {
                entity.setField(convertColumnName(entry.getKey()), entry
                        .getValue());
            }
            entities.add(entity);
            rowIds.add(rowId);
        }

        table.putEntities(entities, cc);

        return InsertRowsResult.success(insertRows.getTableId(), rowIds);
    }
}
