package org.opendatakit.aggregate.odktables;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.odktables.client.Column;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.CreateUser;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.DeleteUser;
import org.opendatakit.aggregate.odktables.command.GetUser;
import org.opendatakit.aggregate.odktables.command.InsertRows;
import org.opendatakit.aggregate.odktables.command.QueryForRows;
import org.opendatakit.aggregate.odktables.command.QueryForTables;
import org.opendatakit.aggregate.odktables.command.logic.CreateTableLogic;
import org.opendatakit.aggregate.odktables.command.logic.CreateUserLogic;
import org.opendatakit.aggregate.odktables.command.logic.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.command.logic.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.command.logic.GetUserLogic;
import org.opendatakit.aggregate.odktables.command.logic.InsertRowsLogic;
import org.opendatakit.aggregate.odktables.command.logic.QueryForRowsLogic;
import org.opendatakit.aggregate.odktables.command.logic.QueryForTablesLogic;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.common.persistence.DataField;
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
 * <p>
 * CommandLogic is also responsible for converting data formats (e.g. table and
 * row ids, column names) back and forth between the client and the datastore.
 * Any subclass of CommandLogic should make sure to convert and unconvert these
 * values appropriately using the utility methods in this class.
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

    /**
     * Converts a tableId coming from the client into a form accepted by the
     * datastore. Note that you should always ignore case when comparing
     * tableIds.
     */
    protected static String convertTableId(String tableId)
    {
        return "TBL_" + tableId.toUpperCase();
    }

    /**
     * Converts a tableId coming from the datastore into the form that the
     * client expects. Note that you should always ignore case when comparing
     * tableIds.
     */
    protected static String unconvertTableId(String tableId)
    {
        return tableId.replaceFirst("^TBL_", "");
    }

    /**
     * Converts a userId coming from the client into a form accepted by the
     * datastore. Note that you should always ignore case when comparing
     * userIds.
     */
    protected static String convertUserId(String userId)
    {
        return "USR_" + userId.toUpperCase();
    }

    /**
     * Converts a userId coming from the datastore into the form that the client
     * expects. Note that you should always ignore case when comparing userIds.
     */
    protected static String unconvertUserId(String userId)
    {
        return userId.replaceFirst("^USR_", "");
    }

    /**
     * Converts a rowId coming from the client into a form accepted by the
     * datastore. Note that this does not affect the uniqueness of the rowId.
     */
    private static String convertRowId(String rowId)
    {
        return "ROW_" + rowId;
    }

    /**
     * Converts a rowId coming from the datastore into the form that the client
     * expects. Note that this does not affect the uniqueness of the tableId.
     */
    private static String unconvertRowId(String rowId)
    {
        return rowId.replaceFirst("^ROW_", "");
    }

    /**
     * Converts a column name coming from the client into a form accepted by the
     * datastore by making it upper case. This conversion has no reversability,
     * therefore code dealing with column names should always ignore case.
     */
    protected static String convertColumnName(String name)
    {
        return name.toUpperCase();
    }

    /**
     * Takes a tableId and rowId coming from the client and creates the URI for
     * the row which can be used by the datastore. Note that this does not
     * affect the uniqueness of the ids.
     * 
     * @param tableId
     *            the unique identifier of the table. This should be of the
     *            unconverted form that would be given by a client.
     * @param rowId
     *            the unique identifier of the row. This should be of the
     *            unconverted form that would be given by a client.
     */
    protected static String createRowURI(String tableId, String rowId)
    {
        return String.format("%s:%s:%s", TableIndex.TABLE_NAMESPACE,
                convertTableId(tableId), convertRowId(rowId));
    }

    /**
     * Takes a rowURI coming from the datastore and parses out the rowId.
     * 
     * @param rowURI
     *            the row URI coming from the datastore
     * @return the rowId which the client knows how to deal with
     */
    protected static String getRowId(String rowURI)
    {
        return unconvertRowId(rowURI.split(":")[2]);
    }

    /**
     * Takes a rowURI coming from the datastore and parses out the tableId.
     * 
     * @param rowURI
     *            the row URI coming from the datastore
     * @return the tableId which the client knows how to deal with
     */
    protected static String getTableId(String rowURI)
    {
        return unconvertTableId(rowURI.split(":")[1]);
    }

    /**
     * Converts the given Column into a DataField.
     * 
     * @param column
     *            the column to convert
     * @return a DataField with the same name, type, and nullable flag as the
     *         column
     */
    protected static DataField columnToDataField(Column column)
    {
        return new DataField(convertColumnName(column.getName()), column
                .getType(), column.isNullable());
    }
}
