package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.CreateUser;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.DeleteUser;
import org.opendatakit.aggregate.odktables.command.GetUser;
import org.opendatakit.aggregate.odktables.command.InsertRows;
import org.opendatakit.aggregate.odktables.command.QueryForRows;
import org.opendatakit.aggregate.odktables.command.QueryForTables;
import org.opendatakit.aggregate.odktables.command.result.CreateTableResult;
import org.opendatakit.aggregate.odktables.command.result.CreateUserResult;
import org.opendatakit.aggregate.odktables.command.result.DeleteTableResult;
import org.opendatakit.aggregate.odktables.command.result.DeleteUserResult;
import org.opendatakit.aggregate.odktables.command.result.GetUserResult;
import org.opendatakit.aggregate.odktables.command.result.InsertRowsResult;
import org.opendatakit.aggregate.odktables.command.result.QueryForRowsResult;
import org.opendatakit.aggregate.odktables.command.result.QueryForTablesResult;

public class CommandConverterTest
{

    private CommandConverter ccv;
    private Map<Command, String> commandToJson;
    private Map<CommandResult<?>, String> commandResultToJson;
    private Map<Class<? extends Command>, String> commandClassToMethodPath;

    @Before
    public void setUp()
    {
        ccv = CommandConverter.getInstance();

        // Command Json
        commandToJson = new HashMap<Command, String>();
        commandToJson.put(TestUtils.createTable, "{\"userId\":\"1\",\"tableId\":\"1\",\"tableName\":\"Table 1\",\"columns\":[{\"name\":\"col1\",\"type\":\"STRING\",\"nullable\":false}]}");
        commandToJson.put(TestUtils.createUser, "{\"userId\":\"1\",\"userName\":\"The User\"}");
        commandToJson.put(TestUtils.deleteTable, "{\"userId\":\"1\",\"tableId\":\"1\"}");
        commandToJson.put(TestUtils.deleteUser, "{\"userId\":\"1\"}");
        commandToJson.put(TestUtils.getUserUri, "{\"userId\":\"1\"}");
        commandToJson.put(TestUtils.insertRows, "{\"userId\":\"1\",\"tableId\":\"1\",\"rows\":[{\"rowId\":\"1\",\"values\":{\"weight\":\"175.25\",\"age\":\"22\",\"name\":\"john\"}},{\"rowId\":\"2\",\"values\":{\"weight\":\"164.26\",\"age\":\"36\",\"name\":\"james\"}}]}");
        commandToJson.put(TestUtils.queryForRows, "{\"userUri\":\"4ac1471c-a4b6-4835-8814-1e99fa7c47c3\",\"tableId\":\"1\"}");
        commandToJson.put(TestUtils.queryForTables, "{}");

        // Success CommandResult Json
        commandResultToJson = new HashMap<CommandResult<?>, String>();
        commandResultToJson.put(CreateTableResult.success(TestUtils.userId, TestUtils.tableId), "{\"userId\":\"1\",\"tableId\":\"1\",\"successful\":true}");
        commandResultToJson.put(CreateUserResult.success(TestUtils.userId), "{\"userId\":\"1\",\"successful\":true}");
        commandResultToJson.put(DeleteTableResult.success(TestUtils.tableId), "{\"tableId\":\"1\",\"successful\":true}");
        commandResultToJson.put(DeleteUserResult.success(TestUtils.userId), "{\"userId\":\"1\",\"successful\":true}");
        commandResultToJson.put(GetUserResult.success(TestUtils.userId, TestUtils.userUri, TestUtils.userName), "{\"userId\":\"1\",\"userUri\":\"4ac1471c-a4b6-4835-8814-1e99fa7c47c3\",\"userName\":\"The User\",\"successful\":true}");
        commandResultToJson.put(InsertRowsResult.success(TestUtils.tableId, TestUtils.rowIds), "{\"tableId\":\"1\",\"rowIds\":[\"1\",\"2\"],\"successful\":true}");
        commandResultToJson.put(QueryForRowsResult.success(TestUtils.userUri, TestUtils.tableId, TestUtils.rows), "{\"rows\":[{\"rowId\":\"1\",\"values\":{\"weight\":\"175.25\",\"age\":\"22\",\"name\":\"john\"}},{\"rowId\":\"2\",\"values\":{\"weight\":\"164.26\",\"age\":\"36\",\"name\":\"james\"}}],\"tableId\":\"1\",\"userUri\":\"4ac1471c-a4b6-4835-8814-1e99fa7c47c3\",\"successful\":true}");
        commandResultToJson.put(QueryForTablesResult.success(TestUtils.tableList), "{\"tableList\":{\"tableEntries\":[{\"userUri\":\"4ac1471c-a4b6-4835-8814-1e99fa7c47c3\",\"userName\":\"The User\",\"tableId\":\"1\",\"tableName\":\"The Table\"}]},\"successful\":true}");
 
        // Failure CommandResult Json
        commandResultToJson.put(CreateTableResult.failure(TestUtils.userId, TestUtils.tableId, FailureReason.TABLE_ALREADY_EXISTS), "{\"userId\":\"1\",\"tableId\":\"1\",\"successful\":false,\"reason\":\"TABLE_ALREADY_EXISTS\"}");
        commandResultToJson.put(CreateUserResult.failure(TestUtils.userId, FailureReason.USER_ALREADY_EXISTS), "{\"userId\":\"1\",\"successful\":false,\"reason\":\"USER_ALREADY_EXISTS\"}");
        commandResultToJson.put(GetUserResult.failure(TestUtils.userId, FailureReason.USER_DOES_NOT_EXIST), "{\"userId\":\"1\",\"successful\":false,\"reason\":\"USER_DOES_NOT_EXIST\"}");
        commandResultToJson.put(InsertRowsResult.failure(TestUtils.tableId, TestUtils.rowId), "{\"tableId\":\"1\",\"failedRowId\":\"1\",\"successful\":false,\"reason\":\"ROW_ALREADY_EXISTS\"}");
        commandResultToJson.put(QueryForRowsResult.failure(TestUtils.userUri, TestUtils.tableId, FailureReason.TABLE_DOES_NOT_EXIST), "{\"tableId\":\"1\",\"userUri\":\"4ac1471c-a4b6-4835-8814-1e99fa7c47c3\",\"successful\":false,\"reason\":\"TABLE_DOES_NOT_EXIST\"}");
        
        commandClassToMethodPath = new HashMap<Class<? extends Command>, String>();
        commandClassToMethodPath.put(CreateTable.class, CreateTable.methodPath());
        commandClassToMethodPath.put(CreateUser.class, CreateUser.methodPath());
        commandClassToMethodPath.put(DeleteTable.class, DeleteTable.methodPath());
        commandClassToMethodPath.put(DeleteUser.class, DeleteUser.methodPath());
        commandClassToMethodPath.put(GetUser.class, GetUser.methodPath());
        commandClassToMethodPath.put(InsertRows.class, InsertRows.methodPath());
        commandClassToMethodPath.put(QueryForRows.class, QueryForRows.methodPath());
        commandClassToMethodPath.put(QueryForTables.class, QueryForTables.methodPath());
    }

    @Test
    public void testSerializeCommand()
    {
        for (Entry<Command, String> entry : commandToJson.entrySet())
        {
            Command command = entry.getKey();
            String commandJson = entry.getValue();
            String serialized = ccv.serializeCommand(command);
            assertEquals("Failed for " + command.getClass().getSimpleName(), commandJson, serialized);
        }
    }

    @Test
    public void testDeserializeCommand()
    {
        for (Entry<Command, String> entry : commandToJson.entrySet())
        {
            Command command = entry.getKey();
            Class<? extends Command> commandClass = command.getClass();
            String commandJson = entry.getValue();
            Command deserialized = ccv.deserializeCommand(commandJson,
                    commandClass);
            assertEquals("Failed for " + commandClass.getSimpleName(), command, deserialized);
        }
    }

    @Test
    public void testSerializeResult()
    {
        for (Entry<CommandResult<?>, String> entry : commandResultToJson.entrySet())
        {
            CommandResult<?> result = entry.getKey();
            String resultJson = entry.getValue();
            String serialized = ccv.serializeResult(result);
            assertEquals("Failed for " + result.getClass().getSimpleName(), resultJson, serialized);
        }
   }

    @Test
    public void testDeserializeResult()
    {
        for (Entry<CommandResult<?>, String> entry : commandResultToJson.entrySet())
        {
            CommandResult<?> result = entry.getKey();
            @SuppressWarnings("rawtypes")
            Class<? extends CommandResult> resultClass = result.getClass();
            String resultJson = entry.getValue();
            CommandResult<?> deserialized = ccv.deserializeResult(resultJson,
                    resultClass);
            assertEquals("Failed for " + result.getClass().getSimpleName(), result, deserialized);
        }
   }

    @Test
    public void testGetCommandClass()
    {
        for (Entry<Class<? extends Command>, String> entry : commandClassToMethodPath.entrySet())
        {
            assertEquals("Failed for " + entry.getKey().getSimpleName(), entry.getKey(), ccv.getCommandClass(entry.getValue()));
        }
    }
}
