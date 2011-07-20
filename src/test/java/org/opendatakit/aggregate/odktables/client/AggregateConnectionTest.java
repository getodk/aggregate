package org.opendatakit.aggregate.odktables.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.client.api.CommonAPI;
import org.opendatakit.aggregate.odktables.client.api.SimpleAPI;
import org.opendatakit.aggregate.odktables.client.api.SynchronizeAPI;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.OutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.RowOutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.common.persistence.DataField.DataType;

/**
 * Test case for AggregateConnection, but not really a unit test. Only works
 * when you have a running Aggregate instance at localhost:8888, and assumes you
 * start with an empty datastore.
 * 
 * Also, some tests are dependent on state created by previous tests, so the
 * order of the tests must be kept the same.
 * 
 * @author the.dylan.price@gmail.com
 */
public class AggregateConnectionTest
{

    private SimpleAPI conn;
    private SynchronizeAPI conn2;
    private String userID;
    private String userName;
    private String tableID;
    private List<String> rowIds;
    private List<Row> rows;

    @Before
    public void setUp()
    {
        userID = "user1";

        userName = "Dylan Price";
        tableID = "table1";

        rowIds = new ArrayList<String>();
        rows = new ArrayList<Row>();
        Row row1 = new Row();
        rowIds.add("1");
        row1.setRowID("1");
        row1.setValue("column1", "value1");
        row1.setValue("column2", "value1");
        rows.add(row1);
        Row row2 = new Row();
        rowIds.add("2");
        row2.setRowID("2");
        row1.setValue("column1", "value2");
        row2.setValue("column2", "value2");
        rows.add(row2);
    }

    @Test
    public void test() throws URISyntaxException, ClientProtocolException,
            UserDoesNotExistException, IOException, UserAlreadyExistsException,
            PermissionDeniedException, TableAlreadyExistsException,
            TableDoesNotExistException, CannotDeleteException,
            AggregateInternalErrorException, OutOfSynchException,
            RowOutOfSynchException
    {
        URI aggregateURI = new URI("http://localhost:8888");
        System.out.print("Please enter adminID: ");
        Scanner input = new Scanner(System.in);
        String adminID = input.nextLine();
        CommonAPI api = new CommonAPI(aggregateURI, adminID);
        try
        {
            api.createUser(userID, "Dylan Price");
        } catch (UserAlreadyExistsException e)
        {
        }

        conn = new SimpleAPI(aggregateURI, userID);

        List<Column> columns = new ArrayList<Column>();
        Column column1 = new Column("column1", DataType.STRING, false);
        Column column2 = new Column("column2", DataType.STRING, false);
        columns.add(column1);
        columns.add(column2);

        try
        {
            conn.deleteTable(tableID);
        } catch (TableDoesNotExistException e)
        {
        }

        conn.createTable(tableID, "Table 1", columns);

        conn.insertRows(tableID, rows);

        List<TableEntry> entries = conn.listAllTables();
        TableEntry entry = entries.get(0);
        conn.getAllRows(entry.getTableID());

        conn.deleteTable(tableID);

        conn2 = new SynchronizeAPI(aggregateURI, userID);

        Modification mod0 = conn2.createSynchronizedTable(tableID, "Table 1",
                columns);

        List<SynchronizedRow> synchRows = new ArrayList<SynchronizedRow>();
        for (int i = 0; i < 10; i++)
        {
            SynchronizedRow row = new SynchronizedRow();
            row.setRowID("" + i);
            row.setValue("column1", "value" + i);
            row.setValue("column2", "value" + i);
            synchRows.add(row);
        }
        Modification mod1 = conn2.insertSynchronizedRows(tableID,
                mod0.getModificationNumber(), synchRows);

        synchRows = mod1.getRows();
        for (SynchronizedRow row : synchRows)
        {
            row.setValue("column1", "changedValue");
            row.setValue("column2", "changedValue");
        }

        Modification mod2 = conn2.updateSynchronizedRows(tableID,
                mod1.getModificationNumber(), synchRows);

        Modification currentMod = conn2.synchronize(tableID,
                mod0.getModificationNumber());

        conn2.removeTableSynchronization(tableID);
        entries = conn2.listAllTables();
        entry = entries.get(0);
        conn2.cloneSynchronizedTable(entry.getAggregateTableIdentifier(),
                tableID);

        conn2.deleteSynchronizedTable(tableID);

        User user = conn2.getUserByID(userID);
        user = conn2.getUserByAggregateIdentifier(user
                .getAggregateUserIdentifier());
        conn2.deleteUser(user.getAggregateUserIdentifier());
    }

    //    @Test(expected = UserDoesNotExistException.class)
    //    public void testCreateTableUserDoesNotExist()
    //            throws ClientProtocolException, TableAlreadyExistsException,
    //            UserDoesNotExistException, IOException
    //    {
    //        List<InternalColumn> columns = new ArrayList<InternalColumn>();
    //        columns.add(new InternalColumn("COL_1", DataType.STRING, true));
    //        conn.createTable(userID, tableId, "Table 1", columns);
    //    }
    //
    //    @Test
    //    public void testCreateUser() throws UserAlreadyExistsException,
    //            ClientProtocolException, IOException
    //    {
    //        String createdUserId = conn.createUser(userID, userName);
    //        assertEquals(userID, createdUserId);
    //    }
    //
    //    @Test
    //    public void testListTablesEmpty() throws ClientProtocolException,
    //            IOException
    //    {
    //        TableList tableList = conn.listTables();
    //        assertEquals(0, tableList.size());
    //    }
    //
    //    @Test
    //    public void testCreateTable() throws ClientProtocolException, IOException,
    //            TableAlreadyExistsException, UserDoesNotExistException
    //    {
    //        List<InternalColumn> columns = new ArrayList<InternalColumn>();
    //        columns.add(new InternalColumn("COL_1", DataType.STRING, true));
    //        String createdTableId = conn.createTable(userID, tableId, "Table 1",
    //                columns);
    //        assertEquals(tableId, createdTableId);
    //    }
    //
    //    @Test(expected = TableAlreadyExistsException.class)
    //    public void testCreateTableTableAlreadyExists()
    //            throws ClientProtocolException, TableAlreadyExistsException,
    //            UserDoesNotExistException, IOException
    //    {
    //        List<InternalColumn> columns = new ArrayList<InternalColumn>();
    //        columns.add(new InternalColumn("COL_1", DataType.STRING, true));
    //        conn.createTable(userID, tableId, "Table 1", columns);
    //    }
    //
    //    @Test
    //    public void testInsertRows() throws ClientProtocolException, IOException,
    //            RowAlreadyExistsException, ODKTablesClientException
    //    {
    //        List<String> insertedRowIds = conn.insertRows(userID, tableId, rows);
    //        assertEquals(rowIds, insertedRowIds);
    //    }
    //
    //    @Test(expected = TableDoesNotExistException.class)
    //    public void testInsertRowsBadUser() throws ClientProtocolException,
    //            RowAlreadyExistsException, ODKTablesClientException, IOException
    //    {
    //        conn.insertRows(userID + "adifferentuser", tableId, rows);
    //    }
    //
    //    @Test
    //    public void testGetUser() throws ClientProtocolException,
    //            UserDoesNotExistException, IOException
    //    {
    //        InternalUser user = conn.getUser(userID);
    //        assertEquals(userID, user.getUserId());
    //        assertEquals(userName, user.getUserName());
    //        String userUri = user.getUserUri();
    //        assertNotNull(userUri);
    //        assertFalse(userUri.equals(""));
    //        assertTrue(userUri.startsWith("uuid"));
    //        assertEquals(41, userUri.length());
    //    }
    //
    //    @Test(expected = UserDoesNotExistException.class)
    //    public void testGetUserBadUser() throws ClientProtocolException,
    //            UserDoesNotExistException, IOException
    //    {
    //        conn.getUser(userID + "adifferentuser");
    //    }
    //
    //    @Test
    //    public void testListTables() throws ClientProtocolException, IOException
    //    {
    //        TableList tableList = conn.listTables();
    //        assertEquals(1, tableList.size());
    //        for (InternalTableEntry entry : tableList)
    //        {
    //            assertTrue(tableId.equalsIgnoreCase(entry.getTableId()));
    //            assertEquals("Dylan Price", entry.getUserName());
    //        }
    //    }
    //
    //    @Test
    //    public void testQueryForRows() throws ClientProtocolException,
    //            UserDoesNotExistException, IOException, TableDoesNotExistException
    //    {
    //        String userUri = conn.getUser(userID).getUserUri();
    //        List<Row> rows = conn.getRows(userUri, tableId);
    //        assertEquals(this.rows, rows);
    //    }
    //
    //    @Test(expected = UserDoesNotExistException.class)
    //    public void testQueryForRowsBadUser() throws ClientProtocolException,
    //            TableDoesNotExistException, UserDoesNotExistException, IOException
    //    {
    //        String userUri = conn.getUser(userID).getUserUri() + "baduri";
    //        conn.getRows(userUri, tableId);
    //    }
    //
    //    @Test(expected = TableDoesNotExistException.class)
    //    public void testQueryForRowsBadTable() throws ClientProtocolException,
    //            UserDoesNotExistException, IOException, TableDoesNotExistException
    //    {
    //        String userUri = conn.getUser(userID).getUserUri();
    //        conn.getRows(userUri, tableId + "badtable");
    //    }
    //
    //    @Test
    //    public void testDeleteTable() throws ClientProtocolException, IOException
    //    {
    //        String deletedTableId = conn.deleteTable(userID, tableId);
    //        assertEquals(tableId, deletedTableId);
    //    }
    //
    //    @Test
    //    public void testDeleteUser() throws ClientProtocolException, IOException
    //    {
    //        String deletedUserId = conn.deleteUser(userID);
    //        assertEquals(userID, deletedUserId);
    //    }
}
