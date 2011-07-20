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
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.commandlogic.simple.CreateTableLogic;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Test case for CreateTableLogic. Depends on the test case for
 * DeleteTableLogic, but that test case also depends on this one. Also depends
 * on CreateUserLogic.
 * 
 * @author dylan-price
 */
public class CreateTableLogicTest
{

    private String userId;
    private String tableId;
    private CreateTableLogic createTableLogic;
    private CallingContext cc;

    @Before
    public void setUp() throws ODKDatastoreException, UserAlreadyExistsException
    {
        userId = "user1";
        tableId = "table1";
        cc = TestContextFactory.getCallingContext();

        // Make sure that we have cleared the db.
        tearDown();

        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        CreateTable createTable = new CreateTable(userId, tableId, "Table 1",
                columns);

        createTableLogic = new CreateTableLogic(createTable);
        TestUtils.createUser(userId, "The User", cc);
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        TestUtils.deleteTable(userId, tableId, cc);
        TestUtils.deleteUser(userId, cc);
    }

    @Test
    public void testExecuteSuccess() throws ODKDatastoreException,
            TableAlreadyExistsException, UserDoesNotExistException
    {
        CreateTableResult result = createTableLogic.execute(cc);
        assertTrue(result.successful());
        assertEquals(tableId, result.getCreatedTableId());
    }

    @Test(expected = TableAlreadyExistsException.class)
    public void testExecuteFailure() throws TableAlreadyExistsException,
            ODKDatastoreException, UserDoesNotExistException
    {
        createTableLogic.execute(cc);
        CreateTableResult result = createTableLogic.execute(cc);
        assertFalse(result.successful());
        assertEquals(tableId, result.getCreatedTableId());
    }
}
