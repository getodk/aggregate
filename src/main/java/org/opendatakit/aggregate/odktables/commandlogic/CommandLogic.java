package org.opendatakit.aggregate.odktables.commandlogic;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.common.CheckUserExists;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.common.GetUserByAggregateIdentifier;
import org.opendatakit.aggregate.odktables.command.common.GetUserByID;
import org.opendatakit.aggregate.odktables.command.common.QueryForTables;
import org.opendatakit.aggregate.odktables.command.common.SetTablePermissions;
import org.opendatakit.aggregate.odktables.command.common.SetUserManagementPermissions;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.command.synchronize.CloneSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.CreateSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.DeleteSynchronizedTable;
import org.opendatakit.aggregate.odktables.command.synchronize.InsertSynchronizedRows;
import org.opendatakit.aggregate.odktables.command.synchronize.RemoveTableSynchronization;
import org.opendatakit.aggregate.odktables.command.synchronize.Synchronize;
import org.opendatakit.aggregate.odktables.command.synchronize.UpdateSynchronizedRows;
import org.opendatakit.aggregate.odktables.commandlogic.common.CheckUserExistsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.CreateUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserByAggregateIdentifierLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserByIDLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.QueryForTablesLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetTablePermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetUserManagementPermissionsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.CreateTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.InsertRowsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.simple.QueryForRowsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.CloneSynchronizedTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.CreateSynchronizedTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.DeleteSynchronizedTableLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.InsertSynchronizedRowsLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.RemoveTableSynchronizationLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.SynchronizeLogic;
import org.opendatakit.aggregate.odktables.commandlogic.synchronize.UpdateSynchronizedRowsLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * CommandLogic encapsulates the logic associated with a specific Command. That
 * is, CommandLogic actually 'does' the command.
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
     * @throws AggregateInternalErrorException
     *             if there was an internal error which the CommandLogic could
     *             not handle, but should generally be recoverable on the client
     *             side.
     * @throws SnafuException
     *             if there was an unrecoverable error which likely left the
     *             datastore in a corrupted state.
     * @return the result of executing the command
     */
    public abstract CommandResult<T> execute(CallingContext cc)
            throws AggregateInternalErrorException, SnafuException;

    /**
     * There should be one CommandType for each implementation of the Command
     * interface. This is just to support switching in
     * {@link CommandConverter#newCommandLogic(Command)}.
     */
    private enum CommandType
    {
        // Common
        CHECK_USER_EXISTS,
        CREATE_USER,
        DELETE_USER,
        GET_USER_BY_ID,
        GET_USER_BY_AGGREGATE_IDENTIFIER,
        QUERY_FOR_TABLES,
        SET_TABLE_PERMISSIONS,
        SET_USER_MANAGEMENT_PERMISSIONS,

        // Simple
        CREATE_TABLE,
        DELETE_TABLE,
        INSERT_ROWS,
        QUERY_FOR_ROWS,

        // Synchronized
        CLONE_SYNCHRONIZED_TABLE,
        CREATE_SYNCHRONIZED_TABLE,
        DELETE_SYNCHRONIZED_TABLE,
        INSERT_SYNCHRONIZED_ROWS,
        REMOVE_TABLE_SYNCHRONIZATION,
        SYNCHRONIZE,
        UPDATE_SYNCHRONIZED_ROWS,
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
        commandClassMap.put(CheckUserExists.class,
                CommandType.CHECK_USER_EXISTS);
        commandClassMap.put(CreateUser.class, CommandType.CREATE_USER);
        commandClassMap.put(DeleteUser.class, CommandType.DELETE_USER);
        commandClassMap.put(GetUserByID.class, CommandType.GET_USER_BY_ID);
        commandClassMap.put(GetUserByAggregateIdentifier.class,
                CommandType.GET_USER_BY_AGGREGATE_IDENTIFIER);
        commandClassMap.put(QueryForTables.class, CommandType.QUERY_FOR_TABLES);
        commandClassMap.put(SetTablePermissions.class,
                CommandType.SET_TABLE_PERMISSIONS);
        commandClassMap.put(SetUserManagementPermissions.class,
                CommandType.SET_USER_MANAGEMENT_PERMISSIONS);

        // Simple
        commandClassMap.put(CreateTable.class, CommandType.CREATE_TABLE);
        commandClassMap.put(DeleteTable.class, CommandType.DELETE_TABLE);
        commandClassMap.put(InsertRows.class, CommandType.INSERT_ROWS);
        commandClassMap.put(QueryForRows.class, CommandType.QUERY_FOR_ROWS);

        // Synchronized
        commandClassMap.put(CloneSynchronizedTable.class,
                CommandType.CLONE_SYNCHRONIZED_TABLE);
        commandClassMap.put(CreateSynchronizedTable.class,
                CommandType.CREATE_SYNCHRONIZED_TABLE);
        commandClassMap.put(DeleteSynchronizedTable.class,
                CommandType.DELETE_SYNCHRONIZED_TABLE);
        commandClassMap.put(InsertSynchronizedRows.class,
                CommandType.INSERT_SYNCHRONIZED_ROWS);
        commandClassMap.put(RemoveTableSynchronization.class,
                CommandType.REMOVE_TABLE_SYNCHRONIZATION);
        commandClassMap.put(Synchronize.class, CommandType.SYNCHRONIZE);
        commandClassMap.put(UpdateSynchronizedRows.class,
                CommandType.UPDATE_SYNCHRONIZED_ROWS);
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
        case CHECK_USER_EXISTS:
            return new CheckUserExistsLogic((CheckUserExists) command);
        case CREATE_USER:
            return new CreateUserLogic((CreateUser) command);
        case DELETE_USER:
            return new DeleteUserLogic((DeleteUser) command);
        case GET_USER_BY_ID:
            return new GetUserByIDLogic((GetUserByID) command);
        case GET_USER_BY_AGGREGATE_IDENTIFIER:
            return new GetUserByAggregateIdentifierLogic(
                    (GetUserByAggregateIdentifier) command);
        case QUERY_FOR_TABLES:
            return new QueryForTablesLogic((QueryForTables) command);
        case SET_TABLE_PERMISSIONS:
            return new SetTablePermissionsLogic((SetTablePermissions) command);
        case SET_USER_MANAGEMENT_PERMISSIONS:
            return new SetUserManagementPermissionsLogic(
                    (SetUserManagementPermissions) command);

            // Simple
        case CREATE_TABLE:
            return new CreateTableLogic((CreateTable) command);
        case DELETE_TABLE:
            return new DeleteTableLogic((DeleteTable) command);
        case INSERT_ROWS:
            return new InsertRowsLogic((InsertRows) command);
        case QUERY_FOR_ROWS:
            return new QueryForRowsLogic((QueryForRows) command);

            // Synchronized
        case CLONE_SYNCHRONIZED_TABLE:
            return new CloneSynchronizedTableLogic(
                    (CloneSynchronizedTable) command);
        case CREATE_SYNCHRONIZED_TABLE:
            return new CreateSynchronizedTableLogic(
                    (CreateSynchronizedTable) command);
        case DELETE_SYNCHRONIZED_TABLE:
            return new DeleteSynchronizedTableLogic(
                    (DeleteSynchronizedTable) command);
        case INSERT_SYNCHRONIZED_ROWS:
            return new InsertSynchronizedRowsLogic(
                    (InsertSynchronizedRows) command);
        case REMOVE_TABLE_SYNCHRONIZATION:
            return new RemoveTableSynchronizationLogic(
                    (RemoveTableSynchronization) command);
        case SYNCHRONIZE:
            return new SynchronizeLogic((Synchronize) command);
        case UPDATE_SYNCHRONIZED_ROWS:
            return new UpdateSynchronizedRowsLogic(
                    (UpdateSynchronizedRows) command);

        default:
            throw new IllegalArgumentException("No such command: " + command);
        }
    }
}
