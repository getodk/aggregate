package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.client.exception.ODKTablesClientException;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
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

    private AggregateConnection conn;
    private String userId;
    private String userName;
    private String tableId;
    private List<String> rowIds;
    private List<Row> rows;

    @Before
    public void setUp() throws URISyntaxException
    {
        conn = new AggregateConnection(new URI("http://the-dylan-price.appspot.com/"));
        userId = "user1";
        userName = "Dylan Price";
        tableId = "table1";

        rowIds = new ArrayList<String>();
        rows = new ArrayList<Row>();
        Row row1 = new Row("1");
        rowIds.add("1");
        row1.setColumn("COL_1", "value1");
        rows.add(row1);
        Row row2 = new Row("2");
        rowIds.add("2");
        row2.setColumn("COL_1", "value2");
        rows.add(row2);
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testCreateTableUserDoesNotExist()
            throws ClientProtocolException, TableAlreadyExistsException,
            UserDoesNotExistException, IOException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        conn.createTable(userId, tableId, "Table 1", columns);
    }

    @Test
    public void testCreateUser() throws UserAlreadyExistsException,
            ClientProtocolException, IOException
    {
        String createdUserId = conn.createUser(userId, userName);
        assertEquals(userId, createdUserId);
    }

    @Test
    public void testListTablesEmpty() throws ClientProtocolException,
            IOException
    {
        TableList tableList = conn.listTables();
        assertEquals(0, tableList.size());
    }

    @Test
    public void testCreateTable() throws ClientProtocolException, IOException,
            TableAlreadyExistsException, UserDoesNotExistException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        String createdTableId = conn.createTable(userId, tableId, "Table 1",
                columns);
        assertEquals(tableId, createdTableId);
    }

    @Test(expected = TableAlreadyExistsException.class)
    public void testCreateTableTableAlreadyExists()
            throws ClientProtocolException, TableAlreadyExistsException,
            UserDoesNotExistException, IOException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        conn.createTable(userId, tableId, "Table 1", columns);
    }

    @Test
    public void testInsertRows() throws ClientProtocolException, IOException,
            RowAlreadyExistsException, ODKTablesClientException
    {
        List<String> insertedRowIds = conn.insertRows(userId, tableId, rows);
        assertEquals(rowIds, insertedRowIds);
    }

    @Test(expected = TableDoesNotExistException.class)
    public void testInsertRowsBadUser() throws ClientProtocolException,
            RowAlreadyExistsException, ODKTablesClientException, IOException
    {
        conn.insertRows(userId + "adifferentuser", tableId, rows);
    }

    @Test
    public void testGetUser() throws ClientProtocolException,
            UserDoesNotExistException, IOException
    {
        User user = conn.getUser(userId);
        assertEquals(userId, user.getUserId());
        assertEquals(userName, user.getUserName());
        String userUri = user.getUserUri();
        assertNotNull(userUri);
        assertFalse(userUri.equals(""));
        assertTrue(userUri.startsWith("uuid"));
        assertEquals(41, userUri.length());
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testGetUserBadUser() throws ClientProtocolException,
            UserDoesNotExistException, IOException
    {
        conn.getUser(userId + "adifferentuser");
    }

    @Test
    public void testListTables() throws ClientProtocolException, IOException
    {
        TableList tableList = conn.listTables();
        assertEquals(1, tableList.size());
        for (TableEntry entry : tableList)
        {
            assertTrue(tableId.equalsIgnoreCase(entry.getTableId()));
            assertEquals("Dylan Price", entry.getUserName());
        }
    }

    @Test
    public void testQueryForRows() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException
    {
        String userUri = conn.getUser(userId).getUserUri();
        List<Row> rows = conn.getRows(userUri, tableId);
        assertEquals(this.rows, rows);
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testQueryForRowsBadUser() throws ClientProtocolException,
            TableDoesNotExistException, UserDoesNotExistException, IOException
    {
        String userUri = conn.getUser(userId).getUserUri() + "baduri";
        conn.getRows(userUri, tableId);
    }

    @Test(expected = TableDoesNotExistException.class)
    public void testQueryForRowsBadTable() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException
    {
        String userUri = conn.getUser(userId).getUserUri();
        conn.getRows(userUri, tableId + "badtable");
    }

    @Test
    public void testDeleteTable() throws ClientProtocolException, IOException
    {
        String deletedTableId = conn.deleteTable(userId, tableId);
        assertEquals(tableId, deletedTableId);
    }

    @Test
    public void testDeleteUser() throws ClientProtocolException, IOException
    {
        String deletedUserId = conn.deleteUser(userId);
        assertEquals(userId, deletedUserId);
    }
}
