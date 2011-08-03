package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.client.api.SimpleAPI;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Row;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.ODKTablesClientException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.common.ermodel.simple.AttributeType;

/**
 * Integration test for SimpleAPI. Only works when you have a running Aggregate
 * instance at localhost:8888, and assumes you start with an empty datastore.
 * 
 * @author the.dylan.price@gmail.com
 */
public class AggregateConnectionTest
{

    private SimpleAPI conn;
    private String userID;
    private String userName;
    private String tableID;
    private List<String> rowIds;
    private List<Row> rows;

    @Before
    public void setUp() throws ClientProtocolException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException, URISyntaxException, UserAlreadyExistsException,
            PermissionDeniedException
    {
        URI aggregateURI = new URI("http://localhost:8888/");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter admin aggregateUserIdentifier: ");
        String aggregateUserIdentifier = scanner.nextLine();

        conn = new SimpleAPI(aggregateURI, aggregateUserIdentifier);
        conn.createUser(userID, userName);

        conn = new SimpleAPI(aggregateURI, userID);

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
        row2.setValue("column1", "value2");
        row2.setValue("column2", "value2");
        rows.add(row2);
    }

    @Test
    public void testCreateUser() throws UserAlreadyExistsException,
            ClientProtocolException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        String userID = this.userID + "diff";
        String userName = this.userName + "diff";
        User user = conn.createUser(userID, userName);
        assertEquals(userID, user.getUserID());
        assertNotNull(user.getAggregateUserIdentifier());
    }

    @Test
    public void testListTablesEmpty() throws ClientProtocolException,
            IOException, AggregateInternalErrorException
    {
        List<TableEntry> entries = conn.listAllTables();
        assertEquals(0, entries.size());
    }

    @Test
    public void testCreateTable() throws ClientProtocolException, IOException,
            TableAlreadyExistsException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("column 1", AttributeType.STRING, true));
        conn.createTable(tableID, "Table 1", columns);
    }

    @Test(expected = TableAlreadyExistsException.class)
    public void testCreateTableTableAlreadyExists()
            throws ClientProtocolException, TableAlreadyExistsException,
            UserDoesNotExistException, IOException,
            AggregateInternalErrorException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("column 1", AttributeType.STRING, true));
        conn.createTable(tableID, "Table 1", columns);
    }

    @Test
    public void testInsertRows() throws ClientProtocolException, IOException,
            RowAlreadyExistsException, ODKTablesClientException
    {
        Map<String, String> rowIDtoIdentifier = conn.insertRows(tableID, rows);
        assertEquals(rowIds, rowIDtoIdentifier.keySet());
    }

    @Test
    public void testGetUser() throws ClientProtocolException,
            UserDoesNotExistException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        User user = conn.getUserByID(userID);
        assertEquals(userID, user.getUserID());
        assertEquals(userName, user.getUserName());
        String aggregateUserIdentifier = user.getAggregateUserIdentifier();
        assertNotNull(aggregateUserIdentifier);
        assertFalse(aggregateUserIdentifier.equals(""));
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testGetUserBadUser() throws ClientProtocolException,
            UserDoesNotExistException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        conn.getUserByID(userID + "diff");
    }

    @Test
    public void testListTables() throws ClientProtocolException, IOException,
            AggregateInternalErrorException
    {
        List<TableEntry> entries = conn.listAllTables();
        assertEquals(1, entries.size());
        for (TableEntry entry : entries)
        {
            assertTrue(tableID.equalsIgnoreCase(entry.getTableID()));
            assertEquals(userName, entry.getUser().getUserName());
        }
    }

    @Test
    public void testGetAllRows() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        List<Row> rows = conn.getAllRows(tableID);
        assertEquals(this.rows, rows);
    }

    @Test(expected = TableDoesNotExistException.class)
    public void testGetAllRowsBadTable() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        conn.getAllRows(tableID + "diff");
    }

    @Test
    public void testDeleteTable() throws ClientProtocolException, IOException,
            PermissionDeniedException, TableDoesNotExistException,
            AggregateInternalErrorException, UserDoesNotExistException
    {
        conn.deleteTable(tableID);
        try
        {
            conn.getAllRows(tableID);
            fail("Table should not exist!");
        } catch (TableDoesNotExistException e)
        {

        }
    }

    @Test
    public void testDeleteUser() throws ClientProtocolException, IOException,
            PermissionDeniedException, UserDoesNotExistException,
            AggregateInternalErrorException, CannotDeleteException
    {
        User user = conn.getUserByID(userID);
        conn.deleteUser(user.getAggregateUserIdentifier());
        try
        {
            conn.getUserByID(userID);
            fail("User should not exist!");
        } catch (UserDoesNotExistException e)
        {
        }
    }
}
