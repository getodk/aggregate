package org.opendatakit.aggregate.odktables.commandlogic;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.common.GetUser;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForTables;
import org.opendatakit.aggregate.odktables.commandlogic.common.CreateUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.GetUserLogic;
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
        CREATE_TABLE, INSERT_ROWS, DELETE_TABLE, CREATE_USER, DELETE_USER, GET_USER, QUERY_FOR_TABLES, QUERY_FOR_ROWS;
    }

    /**
     * Map from Command class to CommandType, e.g. CreateTable.class to
     * CommandType.CREATE_TABLE.
     */
    private static final Map<Class<? extends Command>, CommandType> commandClassMap;
    static
    {
        commandClassMap = new HashMap<Class<? extends Command>, CommandType>();
        commandClassMap.put(CreateTable.class, CommandType.CREATE_TABLE);
        commandClassMap.put(InsertRows.class, CommandType.INSERT_ROWS);
        commandClassMap.put(DeleteTable.class, CommandType.DELETE_TABLE);
        commandClassMap.put(CreateUser.class, CommandType.CREATE_USER);
        commandClassMap.put(DeleteUser.class, CommandType.DELETE_USER);
        commandClassMap.put(GetUser.class, CommandType.GET_USER);
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
        case CREATE_TABLE:
            return new CreateTableLogic((CreateTable) command);
        case INSERT_ROWS:
            return new InsertRowsLogic((InsertRows) command);
        case DELETE_TABLE:
            return new DeleteTableLogic((DeleteTable) command);
        case CREATE_USER:
            return new CreateUserLogic((CreateUser) command);
        case DELETE_USER:
            return new DeleteUserLogic((DeleteUser) command);
        case GET_USER:
            return new GetUserLogic((GetUser) command);
        case QUERY_FOR_TABLES:
            return new QueryForTablesLogic((QueryForTables) command);
        case QUERY_FOR_ROWS:
            return new QueryForRowsLogic((QueryForRows) command);
        default:
            throw new IllegalArgumentException("No such command: " + command);
        }
    }
}
