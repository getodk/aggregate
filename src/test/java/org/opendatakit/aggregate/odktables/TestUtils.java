package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendatakit.aggregate.odktables.client.Column;
import org.opendatakit.aggregate.odktables.client.Row;
import org.opendatakit.aggregate.odktables.client.TableList;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.CreateUser;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.DeleteUser;
import org.opendatakit.aggregate.odktables.command.GetUser;
import org.opendatakit.aggregate.odktables.command.InsertRows;
import org.opendatakit.aggregate.odktables.command.QueryForRows;
import org.opendatakit.aggregate.odktables.command.QueryForTables;
import org.opendatakit.aggregate.odktables.command.logic.CreateUserLogic;
import org.opendatakit.aggregate.odktables.command.logic.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.command.logic.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.command.result.CreateUserResult;
import org.opendatakit.aggregate.odktables.command.result.DeleteUserResult;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class TestUtils
{

    public static final String userId = "1";
    public static final String userUri = "4ac1471c-a4b6-4835-8814-1e99fa7c47c3";
    public static final String userName = "The User";
    public static final String tableId = "1";
    public static final String tableName = "The Table";
    public static final String rowId = "1";
    public static final Column column = new Column("col1", DataType.STRING,
            false);
    public static final DataField field = new DataField("col1",
            DataType.STRING, false);

    public static final List<Column> columns;
    static
    {
        columns = new ArrayList<Column>();
        columns.add(column);
    }

    public static final List<String> rowIds = new ArrayList<String>();
    public static final List<Row> rows = new ArrayList<Row>();
    public static final Row john;
    public static final Row james;
    static
    {
        john = new Row("1");
        rowIds.add("1");
        john.setColumn("name", "john");
        john.setColumn("age", "22");
        john.setColumn("weight", "175.25");
        rows.add(john);

        james = new Row("2");
        rowIds.add("2");
        james.setColumn("name", "james");
        james.setColumn("age", "36");
        james.setColumn("weight", "164.26");
        rows.add(james);
    }

    public static final TableList tableList;
    static
    {
        tableList = new TableList();
        tableList.addEntry(userUri, userName, tableId, tableName);
    }

    public static final CreateTable createTable = new CreateTable(userId,
            tableId, "Table 1", columns);
    public static final CreateUser createUser = new CreateUser(userId, userName);
    public static final DeleteTable deleteTable = new DeleteTable(userId,
            tableId);
    public static final DeleteUser deleteUser = new DeleteUser(userId);
    public static final GetUser getUserUri = new GetUser(userId);
    public static final InsertRows insertRows = new InsertRows(userId, tableId,
            rows);
    public static final QueryForRows queryForRows = new QueryForRows(userUri,
            tableId);
    public static final QueryForTables queryForTables = new QueryForTables();

    /**
     * Asserts that the given lists of DataFields are equal, that is that each
     * pair of fields in the lists have the same name and same DataType.
     * 
     * @param expected
     * @param actual
     */
    public static void assertFieldListsAreEqual(List<DataField> expected,
            List<DataField> actual)
    {
        assertEquals(expected.size(), actual.size());
        Iterator<DataField> expectedIter = expected.iterator();
        Iterator<DataField> actualIter = actual.iterator();
        while (expectedIter.hasNext() && actualIter.hasNext())
        {
            DataField expectedNext = expectedIter.next();
            DataField actualNext = actualIter.next();
            assertEquals(expectedNext.getName(), actualNext.getName());
            assertEquals(expectedNext.getDataType(), actualNext.getDataType());
            assertEquals(expectedNext.getNullable(), actualNext.getNullable());
        }
    }

    /**
     * Deletes the table represented by the given tableId.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table, in the
     *            format is would be coming from a client (i.e. not the
     *            datastore's internal representation of the userId).
     * @param tableId
     *            the unique identifier of the table to delete, in the format it
     *            would be coming from a client (i.e. not the datastore's
     *            internal representation of the tableId).
     * @param cc
     *            the context
     * @throws ODKDatastoreException
     */
    public static void deleteTable(String userId, String tableId,
            CallingContext cc) throws ODKDatastoreException
    {
        DeleteTable deleteTable = new DeleteTable(userId, tableId);
        DeleteTableLogic deleteTableLogic = new DeleteTableLogic(deleteTable);
        deleteTableLogic.execute(cc);
    }

    public static void createUser(String userId, String userName,
            CallingContext cc) throws ODKDatastoreException,
            UserAlreadyExistsException
    {
        CreateUser createUser = new CreateUser(userId, userName);
        CreateUserLogic createUserLogic = new CreateUserLogic(createUser);
        CreateUserResult result = createUserLogic.execute(cc);
        assertEquals(userId, result.getCreatedUserId());
    }

    public static void deleteUser(String userId, CallingContext cc)
            throws ODKDatastoreException
    {
        DeleteUser deleteUser = new DeleteUser(userId);
        DeleteUserLogic deleteUserLogic = new DeleteUserLogic(deleteUser);
        DeleteUserResult result = deleteUserLogic.execute(cc);
        assertEquals(userId, result.getDeletedUserId());
    }
}
