package org.opendatakit.aggregate.odktables.command;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.command.common.CreateUser;
import org.opendatakit.aggregate.odktables.command.common.DeleteUser;
import org.opendatakit.aggregate.odktables.command.common.GetUser;
import org.opendatakit.aggregate.odktables.command.simple.CreateTable;
import org.opendatakit.aggregate.odktables.command.simple.DeleteTable;
import org.opendatakit.aggregate.odktables.command.simple.InsertRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForRows;
import org.opendatakit.aggregate.odktables.command.simple.QueryForTables;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * CommandConverter handles the serialization and deserialization of Command and
 * CommandResult objects. It also provides a way to retrieve the class of a
 * command implementation given its method name (e.g. '/odktables/createTable').
 * 
 * @author the.dylan.price@gmail.com
 */
public class CommandConverter
{
    /**
     * The singleton instance.
     */
    private static CommandConverter instance;

    /**
     * Map from method name to Command class, e.g. '/odktables/createTable' to
     * CreateTable.class
     */
    private final Map<String, Class<? extends Command>> commandMap;

    /**
     * The Google Json serializer.
     */
    private Gson gson;

    /**
     * Constructs a new CommandConverter.
     */
    private CommandConverter()
    {
        commandMap = new HashMap<String, Class<? extends Command>>();
        commandMap.put(CreateTable.methodPath(), CreateTable.class);
        commandMap.put(InsertRows.methodPath(), InsertRows.class);
        commandMap.put(DeleteTable.methodPath(), DeleteTable.class);
        commandMap.put(CreateUser.methodPath(), CreateUser.class);
        commandMap.put(DeleteUser.methodPath(), DeleteUser.class);
        commandMap.put(GetUser.methodPath(), GetUser.class);
        commandMap.put(QueryForTables.methodPath(), QueryForTables.class);
        commandMap.put(QueryForRows.methodPath(), QueryForRows.class);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    /**
     * Converts the given command into its serialized form which can be sent
     * over the wire.
     * 
     * @param command
     *            the command to serialize.
     * @return the serialized form of command.
     */
    public String serializeCommand(Command command)
    {
        return gson.toJson(command);
    }

    /**
     * Converts the given result into its serialized form which can be sent over
     * the wire.
     * 
     * @param result
     *            the result to serialize
     * @return the serialized form of the result
     */
    public <T extends Command> String serializeResult(CommandResult<T> result)
    {
        return gson.toJson(result);
    }

    /**
     * Deserializes from contents of the given reader into a Command of the
     * given type. Reads from the reader until the Command can be parsed, but
     * does not close it.
     * 
     * @param reader
     *            a Reader ready to stream the serialized form of a Command. The
     *            reader will still be open after the call.
     * @param type
     *            the type of Command to deserialize the contents of the reader
     *            into.
     * @return a Command of class 'type', representing the deserialized contents
     *         of the characters read from the reader.
     */
    public <T extends Command> T deserializeCommand(Reader reader, Class<T> type)
    {
        return gson.fromJson(reader, type);
    }

    /**
     * Deserializes the contents of the given String into a Command of the given
     * type.
     * 
     * @param serialized
     *            a String containing the serialized form of a Command.
     * @param type
     *            the type of Command to deserialize the contents of serialized
     *            into.
     * @return a Command of class 'type', representing the deserialized contents
     *         of serialized.
     */
    public <T extends Command> T deserializeCommand(String serialized,
            Class<T> type)
    {
        return gson.fromJson(serialized, type);
    }

    /**
     * Deserializes from the contents of the given reader into a CommandResult
     * of the given type. Reads from the reader until the Command can be parsed
     * but does not close it.
     * 
     * @param reader
     *            a Reader ready to stream the serialized form of a
     *            CommandResult. The reader will still be open after the call.
     * @param type
     *            the type of CommandResult to deserialize the contents of the
     *            reader into.
     * @return a CommandResult of class 'type', representing the deserialized
     *         contents of characters read from the reader.
     */
    public <T extends CommandResult<?>> T deserializeResult(Reader reader,
            Class<T> type)
    {
        return gson.fromJson(reader, type);
    }

    /**
     * Deserializes the contents of the given String into a CommandResult of the
     * given type.
     * 
     * @param serialized
     *            a String containing the serialized form of a CommandResult
     * @param type
     *            the type of CommandResult to deserialize the String into
     * @return a CommandResult of class 'type' representing the deserialized
     *         contents of the string
     */
    public <T extends CommandResult<?>> T deserializeResult(String serialized,
            Class<T> type)
    {
        return gson.fromJson(serialized, type);
    }

    /**
     * Returns the class implementing the Command interface which corresponds to
     * the given methodName.
     * 
     * @param methodName
     *            the name of a method corresponding to a command, e.g.
     *            '/odktables/createTable'. Returns null if no method with the
     *            given methodName exists.
     * @return the class of an implementation of the Command interface which
     *         corresponds to the given methodName.
     */
    public Class<? extends Command> getCommandClass(String methodName)
    {
        return commandMap.get(methodName);
    }

    /**
     * @return the singleton CommandConverter instance.
     */
    public static CommandConverter getInstance()
    {
        if (instance == null)
            instance = new CommandConverter();
        return instance;
    }
}
