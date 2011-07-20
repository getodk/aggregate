package org.opendatakit.aggregate.odktables.command.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.exception.ODKTablesClientException;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.commandlogic.simple.CreateTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.InsertRowsLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Test case for InsertRowsLogic. Depends on CreateTableLogicTest.
 * 
 * @author the.dylan.price@gmail.com
 */
public class InsertRowsLogicTest
{

    private String userId;
    private String tableId;
    private InsertRowsLogic insertRowsLogic;
    private List<String> rowIds;
    private CallingContext cc;

    @Before
    public void setUp() throws ODKDatastoreException, UserAlreadyExistsException
    {
        userId = "user1";
        tableId = "table1";
        cc = TestContextFactory.getCallingContext();

        // Make sure that we have cleared the db
        tearDown();

        TestUtils.createUser(userId, "The User", cc);
        
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        CreateTable createTable = new CreateTable(userId, tableId, "Table 1",
                columns);

        CreateTableLogic createTableLogic = new CreateTableLogic(createTable);
        createTableLogic.execute(cc);

        List<Row> rows = new ArrayList<Row>();
        Row row = new Row("1");
        row.setColumn("COL_1", "value");
        rows.add(row);
        InsertRows insertRows = new InsertRows(userId, tableId, rows);
        insertRowsLogic = new InsertRowsLogic(insertRows);

        rowIds = new ArrayList<String>();
        rowIds.add("1");
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        TestUtils.deleteTable(userId, tableId, cc);
        TestUtils.deleteUser(userId, cc);
    }

    @Test
    public void testExecuteSuccess() throws ODKDatastoreException,
            RowAlreadyExistsException, ODKTablesClientException
    {
        InsertRowsResult result = insertRowsLogic.execute(cc);
        assertTrue(result.successful());
        assertEquals(rowIds, result.getInsertedRowIds());
    }

    @Test
    public void testExecuteFailure() throws ODKDatastoreException
    {
        insertRowsLogic.execute(cc);
        InsertRowsResult result = insertRowsLogic.execute(cc);
        assertFalse(result.successful());
    }
}
