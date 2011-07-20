package org.opendatakit.aggregate.odktables.command.logic;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.commandlogic.simple.CreateTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class DeleteTableLogicTest
{

    private String userId;
    private String tableId;
    private DeleteTableLogic deleteTableLogic;
    private CallingContext cc;

    @Before
    public void setUp() throws ODKDatastoreException,
            UserAlreadyExistsException
    {
        userId = "user1";
        tableId = "table1";
        cc = TestContextFactory.getCallingContext();
        // Make sure we have cleared the db
        tearDown();

        TestUtils.createUser(userId, "The User", cc);

        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        CreateTable createTable = new CreateTable(userId, tableId, "Table 1",
                columns);

        CreateTableLogic createTableLogic = new CreateTableLogic(createTable);
        createTableLogic.execute(cc);

        DeleteTable deleteTable = new DeleteTable(userId, tableId);
        deleteTableLogic = new DeleteTableLogic(deleteTable);
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        TestUtils.deleteUser(userId, cc);
    }

    @Test
    public void testExecuteSuccess() throws ODKDatastoreException
    {
        DeleteTableResult result = deleteTableLogic.execute(cc);
        assertTrue(result.successful());
    }
}
