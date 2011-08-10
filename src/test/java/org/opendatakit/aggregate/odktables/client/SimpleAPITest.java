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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.TestUtils;
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
 * Integration test for SimpleAPI and CommonAPI. Only works when you have a
 * running Aggregate instance and assumes you start with an empty datastore.
 * 
 * @author the.dylan.price@gmail.com
 */
public class SimpleAPITest
{

    private static String adminID;

    private static final String requestUserID = "user1";
    private static final String requestUserName = "Dylan Price";

    private SimpleAPI conn;
    private String userID;
    private String userName;
    private String tableID;
    private List<String> rowIds;
    private List<Row> rows;
    private String column1Name;
    private String column2Name;

    @BeforeClass
    public static void beforeClass() throws ClientProtocolException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException, UserAlreadyExistsException, PermissionDeniedException,
            URISyntaxException
    {
        Properties props = TestUtils.getTestProperties();
        URI aggregateURI = new URI(props.getProperty("aggregateURI",
                "http://localhost:8888/"));
        adminID = props.getProperty("adminUserID", "bob");

        SimpleAPI conn = new SimpleAPI(aggregateURI, adminID);
        try
        {
            conn.createUser(requestUserID, requestUserName);
        } catch (UserAlreadyExistsException e)
        {

        }
        User user = conn.getUserByID(requestUserID);
        conn.setUserManagementPermissions(user.getAggregateUserIdentifier(),
                true);
    }

    @AfterClass
    public static void afterClass() throws ClientProtocolException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException, PermissionDeniedException, CannotDeleteException,
            URISyntaxException
    {
        URI aggregateURI = new URI("http://localhost:8888/");

        SimpleAPI conn = new SimpleAPI(aggregateURI, adminID);
        try
        {
            User user = conn.getUserByID(requestUserID);
            conn.deleteUser(user.getAggregateUserIdentifier());
        } catch (UserDoesNotExistException e)
        {

        }
    }

    @Before
    public void setUp() throws ClientProtocolException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException, URISyntaxException, UserAlreadyExistsException,
            PermissionDeniedException
    {
        URI aggregateURI = new URI("http://localhost:8888/");

        conn = new SimpleAPI(aggregateURI, requestUserID);

        userID = requestUserID + "diff";
        userName = requestUserName + "diff";

        tableID = "table1";

        rowIds = new ArrayList<String>();
        rows = new ArrayList<Row>();

        column1Name = "column 1";
        column2Name = "column 2";

        Row row1 = new Row();
        rowIds.add("1");
        row1.setRowID("1");
        row1.setValue(column1Name, "value1");
        row1.setValue(column2Name, "value1");
        rows.add(row1);

        Row row2 = new Row();
        rowIds.add("2");
        row2.setRowID("2");
        row2.setValue(column1Name, "value2");
        row2.setValue(column2Name, "value2");
        rows.add(row2);

    }

    @Test
    public void testCreateUser() throws UserAlreadyExistsException,
            ClientProtocolException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        User user = conn.createUser(userID, userName);
        assertEquals(userID, user.getUserID());
        assertNotNull(user.getAggregateUserIdentifier());
    }

    @Test(expected = PermissionDeniedException.class)
    public void testCreateUserNoPermission() throws ClientProtocolException,
            AggregateInternalErrorException, UserDoesNotExistException,
            IOException, UserAlreadyExistsException, PermissionDeniedException
    {
        conn.setUserID(userID);
        conn.createUser(userID + "2", userName + "2");
    }

    @Test
    public void testListTablesEmptyExceptForUsersTable()
            throws ClientProtocolException, IOException,
            AggregateInternalErrorException
    {
        List<TableEntry> entries = conn.listAllTables();
        assertEquals(1, entries.size());
        System.out.println(entries);
    }

    @Test
    public void testCreateTable() throws ClientProtocolException, IOException,
            TableAlreadyExistsException, UserDoesNotExistException,
            AggregateInternalErrorException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column(column1Name, AttributeType.STRING, false));
        columns.add(new Column(column2Name, AttributeType.STRING, false));
        conn.createTable(tableID, "Table 1", columns);
    }

    @Test(expected = TableAlreadyExistsException.class)
    public void testCreateTableTableAlreadyExists()
            throws ClientProtocolException, TableAlreadyExistsException,
            UserDoesNotExistException, IOException,
            AggregateInternalErrorException
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column(column1Name, AttributeType.STRING, true));
        columns.add(new Column(column2Name, AttributeType.STRING, false));
        conn.createTable(tableID, "Table 1", columns);
    }

    @Test
    public void testInsertRows() throws ClientProtocolException, IOException,
            RowAlreadyExistsException, ODKTablesClientException
    {
        Map<String, String> rowIDtoIdentifier = conn.insertRows(tableID, rows);
        List<String> actualRowIds = new ArrayList<String>(
                rowIDtoIdentifier.keySet());
        Collections.sort(rowIds);
        Collections.sort(actualRowIds);
        assertEquals(rowIds, actualRowIds);
    }

    @Test
    public void testGetUserByID() throws ClientProtocolException,
            UserDoesNotExistException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        User user = conn.getUserByID(requestUserID);
        assertEquals(requestUserID, user.getUserID());
        assertEquals(requestUserName, user.getUserName());
        String aggregateUserIdentifier = user.getAggregateUserIdentifier();
        assertNotNull(aggregateUserIdentifier);
        assertFalse(aggregateUserIdentifier.equals(""));
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testGetUserByIDBadUser() throws ClientProtocolException,
            UserDoesNotExistException, IOException, PermissionDeniedException,
            AggregateInternalErrorException
    {
        conn.getUserByID(userID + "diff");
    }

    @Test(expected = PermissionDeniedException.class)
    public void testGetUserByIDNoPermission() throws ClientProtocolException,
            PermissionDeniedException, UserDoesNotExistException,
            AggregateInternalErrorException, IOException
    {
        conn.setUserID(userID);
        conn.getUserByID(requestUserID);
    }

    @Test
    public void testGetUserByAggregateUserIdentifer()
            throws ClientProtocolException, PermissionDeniedException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException
    {
        User user = conn.getUserByID(requestUserID);
        String aggregateUserIdentifier = user.getAggregateUserIdentifier();
        User sameUser = conn
                .getUserByAggregateIdentifier(aggregateUserIdentifier);
        assertEquals(user.getAggregateUserIdentifier(),
                sameUser.getAggregateUserIdentifier());
        assertEquals(user.getUserName(), sameUser.getUserName());
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testGetUserByAggregateIdentifierBadUser()
            throws ClientProtocolException, PermissionDeniedException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException
    {
        conn.getUserByAggregateIdentifier("nosuchuser");
    }

    @Test
    public void testListTables() throws ClientProtocolException, IOException,
            AggregateInternalErrorException
    {
        List<TableEntry> entries = conn.listAllTables();
        assertEquals(2, entries.size());
        boolean containedTable = false;
        for (TableEntry entry : entries)
        {
            if (entry.getUser().getUserName().equals(requestUserName))
            {
                assertTrue(tableID.equalsIgnoreCase(entry.getTableID()));
                containedTable = true;
            }
        }
        assertTrue(containedTable);
    }

    @Test
    public void testGetAllRows() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        List<Row> rows = conn.getAllRows(tableID);
        assertEquals(2, rows.size());
        Row expected = this.rows.get(0);
        Row actual = rows.get(0);
        if (!expected.getColumnValuePairs()
                .equals(actual.getColumnValuePairs()))
            actual = rows.get(1);

        assertEquals(expected.getColumnValuePairs(),
                actual.getColumnValuePairs());
    }

    @Test(expected = TableDoesNotExistException.class)
    public void testGetAllRowsBadTable() throws ClientProtocolException,
            UserDoesNotExistException, IOException, TableDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException
    {
        conn.getAllRows(tableID + "diff");
    }

    @Test(expected = PermissionDeniedException.class)
    public void testSetUserManagementPermissionsNoPermission()
            throws ClientProtocolException, PermissionDeniedException,
            UserDoesNotExistException, AggregateInternalErrorException,
            IOException
    {
        conn.setUserID(userID);
        User user = conn.getUserByID(userID);
        conn.setUserManagementPermissions(user.getAggregateUserIdentifier(),
                true);
    }

    @Test
    public void testSetUserManagementPermissions()
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException, PermissionDeniedException
    {
        conn.setUserID(adminID);
        User user = conn.getUserByID(userID);
        conn.setUserManagementPermissions(user.getAggregateUserIdentifier(),
                true);
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
        //        try
        //        {
        // TODO: if you delete yourself this invalidates the connection.
        // conn.getUserByID(userID);
        //fail("User should not exist!");
        //} catch (UserDoesNotExistException e)
        //        {
        //        }
    }
}
