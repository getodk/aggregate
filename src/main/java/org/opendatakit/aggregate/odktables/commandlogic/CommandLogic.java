package org.opendatakit.aggregate.odktables.commandlogic;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.common.GetTablePermissions;
import org.opendatakit.aggregate.odktables.command.common.GetUserByID;
import org.opendatakit.aggregate.odktables.command.common.GetUserByUUID;
import org.opendatakit.aggregate.odktables.command.common.GetUserPermissions;
import org.opendatakit.aggregate.odktables.command.common.SetPermissions;
import org.opendatakit.aggregate.odktables.command.common.SetPermissionsPermissions;
import org.opendatakit.aggregate.odktables.command.common.SetUsersPermissions;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.common.CreateUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetTablePermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserByIDLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserByUUIDLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserPermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetPermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetPermissionsPermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetUsersPermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.CreateTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.InsertRowsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.QueryForRowsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.QueryForTablesLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * CommandLogic encapsulates the logic associated with a specific Command. That
 * is, CommandLogic implements the logic necessary to validate and execute a
 * Command.
 * </p>
 * 
 * <p>
 * Extensions of this abstract class should provide a one-argument constructor
 * where the argument's type is of the corresponding Command class. E.g. for the
 * Command 'CreateTable', the CommandLogic 'CreateTableLogic' contains a
 * constructor with this signature:
 * </p>
 * 
 * <pre>
 * public CreateTableLogic(CreateTable createTable)
 * </pre>
 * 
 * <p>
 * That way the CreateTableLogic can easily be constructed from a serialized
 * CreateTable and it can access the data it needs to validate and execute the
 * Command.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public abstract class CommandLogic<T extends Command>
{
    /**
     * <p>
     * Executes the logic necessary to run the command.
     * </p>
     * <p>
     * Modifies the datastore according to the purpose of this command (i.e.
     * creates the table, inserts the rows, etc.).
     * </p>
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was an exception from the datastore which this
     *             CommandLogic could not handle
     * @throws E
     *             if there was a problem executing the Command
     * @return the result of successfully executing the command
     */
    public abstract CommandResult<T> execute(CallingContext cc)
            throws ODKDatastoreException;

    /**
     * There should be one CommandType for each implementation of the Command
     * interface. This is just to support fast switching in
     * {@link CommandConverter#newCommandLogic(Command)}.
     */
    private enum CommandType
    {
        // Common
        CREATE_USER,
        DELETE_USER,
        GET_TABLE_PERMISSIONS,
        GET_USER_BY_ID,
        GET_USER_BY_UUID,
        GET_USER_PERMISSIONS,
        SET_PERMISSIONS,
        SET_PERMISSIONS_PERMISSIONS,
        SET_USERS_PERMISSIONS,

        // Simple
        CREATE_TABLE,
        DELETE_TABLE,
        INSERT_ROWS,
        QUERY_FOR_ROWS,
        QUERY_FOR_TABLES,
    }

    /**
     * Map from Command class to CommandType, e.g. CreateTable.class to
     * CommandType.CREATE_TABLE.
     */
    private static final Map<Class<? extends Command>, CommandType> commandClassMap;
    static
    {
        commandClassMap = new HashMap<Class<? extends Command>, CommandType>();

        // Common
        commandClassMap.put(CreateUser.class, CommandType.CREATE_USER);
        commandClassMap.put(DeleteUser.class, CommandType.DELETE_USER);
        commandClassMap.put(GetTablePermissions.class,
                CommandType.GET_TABLE_PERMISSIONS);
        commandClassMap.put(GetUserByID.class, CommandType.GET_USER_BY_ID);
        commandClassMap.put(GetUserByUUID.class, CommandType.GET_USER_BY_UUID);
        commandClassMap.put(GetUserPermissions.class,
                CommandType.GET_USER_PERMISSIONS);
        commandClassMap.put(SetPermissions.class, CommandType.SET_PERMISSIONS);
        commandClassMap.put(SetPermissionsPermissions.class,
                CommandType.SET_PERMISSIONS_PERMISSIONS);
        commandClassMap.put(SetUsersPermissions.class,
                CommandType.SET_USERS_PERMISSIONS);

        // Simple
        commandClassMap.put(CreateTable.class, CommandType.CREATE_TABLE);
        commandClassMap.put(DeleteTable.class, CommandType.DELETE_TABLE);
        commandClassMap.put(InsertRows.class, CommandType.INSERT_ROWS);
        commandClassMap.put(QueryForTables.class, CommandType.QUERY_FOR_TABLES);
        commandClassMap.put(QueryForRows.class, CommandType.QUERY_FOR_ROWS);
    }

    /**
     * Constructs and returns a new CommandLogic corresponding to the given
     * command.
     * 
     * @param command
     *            the command to create the CommandLogic for.
     * @return a new CommandLogic constructed using the given command.
     */
    public static CommandLogic<? extends Command> newInstance(Command command)
    {
        Class<?> commandClass = command.getClass();
        CommandType commandType = commandClassMap.get(commandClass);

        if (commandType == null)
            throw new IllegalArgumentException("No such command: " + command);

        switch (commandType)
        {
        // Common
        case CREATE_USER:
            return new CreateUserLogic((CreateUser) command);
        case DELETE_USER:
            return new DeleteUserLogic((DeleteUser) command);
        case GET_TABLE_PERMISSIONS:
            return new GetTablePermissionsLogic((GetTablePermissions) command);
        case GET_USER_BY_ID:
            return new GetUserByIDLogic((GetUserByID) command);
        case GET_USER_BY_UUID:
            return new GetUserByUUIDLogic((GetUserByUUID) command);
        case GET_USER_PERMISSIONS:
            return new GetUserPermissionsLogic((GetUserPermissions) command);
        case SET_PERMISSIONS:
            return new SetPermissionsLogic((SetPermissions) command);
        case SET_PERMISSIONS_PERMISSIONS:
            return new SetPermissionsPermissionsLogic(
                    (SetPermissionsPermissions) command);
        case SET_USERS_PERMISSIONS:
            return new SetUsersPermissionsLogic((SetUsersPermissions) command);

            // Simple
        case CREATE_TABLE:
            return new CreateTableLogic((CreateTable) command);
        case DELETE_TABLE:
            return new DeleteTableLogic((DeleteTable) command);
        case INSERT_ROWS:
            return new InsertRowsLogic((InsertRows) command);
        case QUERY_FOR_TABLES:
            return new QueryForTablesLogic((QueryForTables) command);
        case QUERY_FOR_ROWS:
            return new QueryForRowsLogic((QueryForRows) command);

        default:
            throw new IllegalArgumentException("No such command: " + command);
        }
    }
}
