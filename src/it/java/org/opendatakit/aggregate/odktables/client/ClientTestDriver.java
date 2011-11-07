package org.opendatakit.aggregate.odktables.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.http.client.ClientProtocolException;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.aggregate.odktables.client.api.SynchronizeAPI;
import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.Filter;
import org.opendatakit.aggregate.odktables.client.entity.FilterOperation;
import org.opendatakit.aggregate.odktables.client.entity.Modification;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.ColumnDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.OutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.client.exception.RowOutOfSynchException;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.TableDoesNotExistException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.common.ermodel.simple.AttributeType;

/**
 * <p>
 * Test driver which reads in a test script, runs the API commands against
 * Aggregate, and outputs the results. Used for testing the SynchronizeAPI.
 * </p>
 * 
 * <pre>
 * The commands are:
 * TODO: document script file
 * 
 * <i>createUser (userName)</i>
 * 
 * <i>deleteUser (userName)</i>
 * 
 * <i>setTablePermissions (requestingUserName) (userName) (tableName) (read) (write) (delete)</i>
 * 
 * <i>listAllTables (userName)</i>
 * 
 * <i>createSynchronizedTable (userName) (tableName)
 *        (columnName) (columnType) (nullable)
 *        ...</i>
 * 
 * <i>cloneSynchronizedTable (userName) (tableName)</i>
 * 
 * <i>cloneSynchronizedTableWithFilters (userName) (tableName)
 * 		  (columnName) (filterType) (value)
 * 		  ...</i>
 * 
 * <i>removeTableSynchronization (userName) (tableName)</i>
 * 
 * <i>deleteSynchronizedTable (userName) (tableName)</i>
 * 
 * <i>insertSynchronizedRows (userName) (tableName)
 *        (rowID)
 *            (columnName) (value)
 *            ...
 *        ...</i>
 * 
 * <i>updateSynchronizedRows (userName) (tableName)
 *        (rowID)
 *            (columnName) (value)
 *            ...
 *        ...</i>
 * 
 * <i>synchronize (userName) (tableName)</i>
 * 
 * <i>printTable (userName) (tableName)</i>
 * </pre>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class ClientTestDriver
{

    private String adminUserID;
    private SynchronizeAPI conn;
    // Map from userID to the client
    private Map<String, SynchronizedClient> clients;
    // Map from userID to a list of Aggregate tables that they are allowed to
    // read
    private Map<String, List<TableEntry>> aggregateTables;
    private Scanner input;
    private PrintWriter output;

    public ClientTestDriver(URI aggregateURI, String adminUserID, Reader r,
            Writer w) throws ClientProtocolException,
            AggregateInternalErrorException, IOException
    {
        this.adminUserID = adminUserID;
        clients = new HashMap<String, SynchronizedClient>();
        aggregateTables = new HashMap<String, List<TableEntry>>();
        input = new Scanner(r);
        input.useDelimiter("");
        output = new PrintWriter(w);

        try
        {
            conn = new SynchronizeAPI(aggregateURI, adminUserID);
        } catch (UserDoesNotExistException e)
        {
            output.println(String
                    .format("User with userID '%s' does not exist. Please go to "
                            + "the 'ODK Tables Admin' tab in Aggregate and create "
                            + "the admin user.", adminUserID));
            output.flush();
            System.exit(1);
        } catch (Exception e)
        {
            output.println("Exception: " + e.toString());
            output.flush();
            System.exit(1);
        }
    }

    public void runTests()
    {
        try
        {
            while (input.hasNextLine())
            {
                String line = input.nextLine();
                output.println(line);
                if (line.length() != 0 && !line.matches("^(\\s|\\t|#).*"))
                {
                    StringTokenizer st = new StringTokenizer(line);
                    String command = st.nextToken();
                    List<String> lineArguments = new ArrayList<String>();
                    while (st.hasMoreTokens())
                    {
                        lineArguments.add(st.nextToken());
                    }
                    executeCommand(command, lineArguments);
                }
            }
        } catch (Exception e)
        {
            output.println(String.format("Exception: %s", e.toString()));
        }
        output.flush();
    }

    private void executeCommand(String command, List<String> arguments)
    {
        try
        {
            if (command.equals("createUser"))
            {
                createUser(arguments);
            } else if (command.equals("deleteUser"))
            {
                deleteUser(arguments);
            } else if (command.equals("setTablePermissions"))
            {
                setTablePermissions(arguments);
            } else if (command.equals("listAllTables"))
            {
                listAllTables(arguments);
            } else if (command.equals("createSynchronizedTable"))
            {
                createSynchronizedTable(arguments);
            } else if (command.equals("cloneSynchronizedTable"))
            {
                cloneSynchronizedTable(arguments);
            } else if (command.equals("cloneSynchronizedTableWithFilters"))
            {
                cloneSynchronizedTableWithFilters(arguments);
            } else if (command.equals("removeTableSynchronization"))
            {
                removeTableSynchronization(arguments);
            } else if (command.equals("deleteSynchronizedTable"))
            {
                deleteSynchronizedTable(arguments);
            } else if (command.equals("insertSynchronizedRows"))
            {
                insertSynchronizedRows(arguments);
            } else if (command.equals("updateSynchronizedRows"))
            {
                updateSynchronizedRows(arguments);
            } else if (command.equals("synchronize"))
            {
                synchronize(arguments);
            } else if (command.equals("printTable"))
            {
                printTable(arguments);
            } else
            {
                throw new IllegalArgumentException("Unrecognized command: "
                        + command);
            }
        } catch (Exception e)
        {
            output.println("Exception: " + e.toString());
        }
    }

    private void createUser(List<String> arguments)
            throws ClientProtocolException, UserAlreadyExistsException,
            PermissionDeniedException, AggregateInternalErrorException,
            IOException, UserDoesNotExistException
    {
        if (arguments.size() != 1)
            throw new RuntimeException("Bad arguments to createUser: "
                    + arguments);

        String clientName = arguments.get(0);
        createUser(clientName);
    }

    private void createUser(String clientName) throws ClientProtocolException,
            UserAlreadyExistsException, PermissionDeniedException,
            AggregateInternalErrorException, IOException,
            UserDoesNotExistException
    {
        SynchronizedClient client = new SynchronizedClient(clientName);
        clients.put(clientName, client);
        conn.setUserID(adminUserID);
        User user = conn.createUser(clientName, clientName);
        client.setAggregateUserIdentifier(user.getAggregateUserIdentifier());
    }

    private void deleteUser(List<String> arguments)
            throws PermissionDeniedException, UserDoesNotExistException,
            CannotDeleteException, ClientProtocolException,
            AggregateInternalErrorException, IOException
    {
        if (arguments.size() != 1)
            throw new IllegalArgumentException("Bad arguments to deleteUser: "
                    + arguments);

        String clientName = arguments.get(0);
        deleteUser(clientName);
    }

    private void deleteUser(String clientName)
            throws PermissionDeniedException, UserDoesNotExistException,
            CannotDeleteException, ClientProtocolException,
            AggregateInternalErrorException, IOException
    {
        conn.setUserID(adminUserID);
        User user = conn.getUserByID(clientName);
        conn.deleteUser(user.getAggregateUserIdentifier());
    }

    private void setTablePermissions(List<String> arguments)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, PermissionDeniedException,
            TableDoesNotExistException, IOException
    {
        if (arguments.size() != 6)
            throw new IllegalArgumentException(
                    "Bad arguments to setTablePermissions: " + arguments);

        String clientName = arguments.get(0);
        String userName = arguments.get(1);
        String tableName = arguments.get(2);
        boolean read = Boolean.parseBoolean(arguments.get(3));
        boolean write = Boolean.parseBoolean(arguments.get(4));
        boolean delete = Boolean.parseBoolean(arguments.get(5));

        setTablePermissions(clientName, userName, tableName, read, write,
                delete);
    }

    private void setTablePermissions(String clientName, String userName,
            String tableName, boolean read, boolean write, boolean delete)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException, PermissionDeniedException,
            TableDoesNotExistException
    {
        updateTables(clientName);

        conn.setUserID(clientName);
        String aggregateUserIdentifier = clients.get(userName)
                .getAggregateUserIdentifier();
        conn.setTablePermissions(aggregateUserIdentifier, tableName, read,
                write, delete);
    }

    private void listAllTables(List<String> arguments)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException
    {
        if (arguments.size() != 1)
            throw new IllegalArgumentException(
                    "Bad arguments to listAllTables: " + arguments);

        String clientName = arguments.get(0);

        listAllTables(clientName);
    }

    private void listAllTables(String clientName)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException
    {
        conn.setUserID(clientName);
        List<TableEntry> entries = conn.listAllTables();
        for (TableEntry entry : entries)
        {
            output.println(String.format("tableName: %s, userName: %s",
                    entry.getTableName(), entry.getUser().getUserName()));
        }
    }

    private void createSynchronizedTable(List<String> arguments)
            throws ClientProtocolException, PermissionDeniedException,
            TableAlreadyExistsException, AggregateInternalErrorException,
            IOException, UserDoesNotExistException
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to createSynchronizedTable: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        List<Column> columns = new ArrayList<Column>();
        String inputLine;
        while ((inputLine = input
                .findInLine("    \\w+ [A-Z_]+ (false)|(true) *")) != null)
        {
            StringTokenizer st = new StringTokenizer(inputLine);

            String name = st.nextToken();
            AttributeType type = AttributeType.valueOf(st.nextToken());
            boolean nullable = Boolean.parseBoolean(st.nextToken());

            Column column = new Column(name, type, nullable);
            columns.add(column);

            // advance scanner
            input.nextLine();
        }
        createSynchronizedTable(clientName, tableName, columns);
    }

    private void createSynchronizedTable(String clientName, String tableName,
            List<Column> columns) throws ClientProtocolException,
            PermissionDeniedException, TableAlreadyExistsException,
            AggregateInternalErrorException, IOException,
            UserDoesNotExistException
    {
        SynchronizedTable table = new SynchronizedTable(tableName, tableName,
                columns);
        SynchronizedClient client = clients.get(clientName);
        client.addTable(table);
        conn.setUserID(clientName);
        Modification modification = conn.createSynchronizedTable(tableName,
                tableName, columns);
        table.setModificationNumber(modification.getModificationNumber());
    }

    private void cloneSynchronizedTable(List<String> arguments)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, TableDoesNotExistException,
            PermissionDeniedException, TableAlreadyExistsException, IOException
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to cloneSynchronizedTable: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        cloneSynchronizedTable(clientName, tableName, null);
    }

    private void cloneSynchronizedTableWithFilters(List<String> arguments)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, TableDoesNotExistException,
            PermissionDeniedException, TableAlreadyExistsException, IOException
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to cloneSynchronizedTableWithFilters: "
                            + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        List<Filter> filters = parseFilters();
        cloneSynchronizedTable(clientName, tableName, filters);
    }

    private void cloneSynchronizedTable(String clientName, String tableName,
            List<Filter> filters) throws ClientProtocolException,
            AggregateInternalErrorException, UserDoesNotExistException,
            IOException, TableDoesNotExistException, PermissionDeniedException,
            TableAlreadyExistsException
    {
        updateTables(clientName);
        TableEntry table = getTableEntry(clientName, tableName);
        SynchronizedTable ownerTable = getOwnerTable(table);

        conn.setUserID(clientName);
        Modification mod;
        if (filters == null || filters.isEmpty())
        {
            mod = conn.cloneSynchronizedTable(
                    table.getAggregateTableIdentifier(), tableName);
        } else
        {
            mod = conn.cloneSynchronizedTable(
                    table.getAggregateTableIdentifier(), tableName, filters);
        }

        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable clientTable = new SynchronizedTable(
                table.getTableName(), table.getTableName(), table.getColumns());
        clientTable.setModificationNumber(mod.getModificationNumber());

        List<SynchronizedRow> rows = mod.getRows();
        for (SynchronizedRow row : rows)
        {
            SynchronizedRow ownerRow = ownerTable.getRowByIdentifier(row
                    .getAggregateRowIdentifier());
            row.setRowID(ownerRow.getRowID());
            clientTable.insertRow(row);
        }
        client.addTable(clientTable);
    }

    private void removeTableSynchronization(List<String> arguments)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, TableDoesNotExistException, IOException
    {
        if (arguments.size() != 2)
            throw new IllegalArgumentException(
                    "Bad arguments to removeTableSynchronization: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        removeTableSynchronization(clientName, tableName);
    }

    private void removeTableSynchronization(String clientName, String tableName)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException, TableDoesNotExistException
    {
        conn.setUserID(clientName);
        conn.removeTableSynchronization(tableName);

        SynchronizedClient client = clients.get(clientName);
        client.removeTable(tableName);
    }

    private void deleteSynchronizedTable(List<String> arguments)
            throws ClientProtocolException, PermissionDeniedException,
            TableDoesNotExistException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException
    {
        if (arguments.size() != 2)
            throw new IllegalArgumentException(
                    "Bad arguments to deleteSynchronizedTable: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        deleteSynchronizedTable(clientName, tableName);
    }

    private void deleteSynchronizedTable(String clientName, String tableName)
            throws ClientProtocolException, PermissionDeniedException,
            TableDoesNotExistException, AggregateInternalErrorException,
            IOException, UserDoesNotExistException
    {
        conn.setUserID(clientName);
        conn.deleteSynchronizedTable(tableName);

        SynchronizedClient client = clients.get(clientName);
        client.removeTable(tableName);
    }

    private void insertSynchronizedRows(List<String> arguments)
            throws ClientProtocolException, OutOfSynchException,
            TableDoesNotExistException, PermissionDeniedException,
            AggregateInternalErrorException, ColumnDoesNotExistException,
            IOException, UserDoesNotExistException
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to insertSynchronizedRows: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        Map<String, SynchronizedRow> rows = parseRows();
        insertSynchronizedRows(clientName, tableName, rows);
    }

    private void insertSynchronizedRows(String clientName, String tableName,
            Map<String, SynchronizedRow> rows) throws ClientProtocolException,
            OutOfSynchException, TableDoesNotExistException,
            PermissionDeniedException, AggregateInternalErrorException,
            ColumnDoesNotExistException, IOException, UserDoesNotExistException
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTable(tableName);
        conn.setUserID(clientName);
        Modification mod;
        if (table != null)
        {
            mod = conn.insertSynchronizedRows(table.getTableID(),
                    table.getModificationNumber(),
                    new ArrayList<SynchronizedRow>(rows.values()));
        } else
        {
            mod = conn.insertSynchronizedRows(tableName, 0,
                    new ArrayList<SynchronizedRow>());
        }
        table.setModificationNumber(mod.getModificationNumber());
        for (SynchronizedRow updatedRow : mod.getRows())
        {
            SynchronizedRow row = rows.get(updatedRow.getRowID());

            String aggregateRowIdentifier = updatedRow
                    .getAggregateRowIdentifier();
            String revisionTag = updatedRow.getRevisionTag();

            row.setAggregateRowIdentifier(aggregateRowIdentifier);
            row.setRevisionTag(revisionTag);

            table.insertRow(row);
        }
    }

    private void updateSynchronizedRows(List<String> arguments)
            throws ClientProtocolException, PermissionDeniedException,
            OutOfSynchException, TableDoesNotExistException,
            RowOutOfSynchException, AggregateInternalErrorException,
            ColumnDoesNotExistException, IOException, UserDoesNotExistException
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to insertSynchronizedRows: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        Map<String, SynchronizedRow> rows = parseRows();
        updateSynchronizedRows(clientName, tableName, rows);
    }

    private void updateSynchronizedRows(String clientName, String tableName,
            Map<String, SynchronizedRow> rows) throws ClientProtocolException,
            PermissionDeniedException, OutOfSynchException,
            TableDoesNotExistException, RowOutOfSynchException,
            AggregateInternalErrorException, ColumnDoesNotExistException,
            IOException, UserDoesNotExistException
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTable(tableName);

        // special case to test updating non-existent table
        if (table == null)
        {
            conn.updateSynchronizedRows(tableName, 0,
                    new ArrayList<SynchronizedRow>());
        }

        List<SynchronizedRow> existingRows = table.getRows(rows.keySet());
        for (SynchronizedRow existingRow : existingRows)
        {
            SynchronizedRow row = rows.get(existingRow.getRowID());
            row.setAggregateRowIdentifier(existingRow
                    .getAggregateRowIdentifier());
            row.setRevisionTag(existingRow.getRevisionTag());
        }
        conn.setUserID(clientName);
        Modification mod = conn.updateSynchronizedRows(table.getTableName(),
                table.getModificationNumber(), new ArrayList<SynchronizedRow>(
                        rows.values()));
        table.setModificationNumber(mod.getModificationNumber());
        for (SynchronizedRow updatedRow : mod.getRows())
        {
            SynchronizedRow row = null;
            String aggregateRowIdentifier = updatedRow
                    .getAggregateRowIdentifier();
            for (SynchronizedRow testRow : rows.values())
            {
                if (testRow.getAggregateRowIdentifier().equals(
                        aggregateRowIdentifier))
                    row = testRow;
            }
            if (row == null)
                throw new RuntimeException(
                        "Could not find row with aggregateRowIdentifier: "
                                + aggregateRowIdentifier);

            row.setRevisionTag(updatedRow.getRevisionTag());
            table.updateRow(row);
        }
    }

    private void synchronize(List<String> arguments)
            throws ClientProtocolException, PermissionDeniedException,
            TableDoesNotExistException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException
    {
        if (arguments.size() != 2)
            throw new IllegalArgumentException("Bad arguments to synchronize: "
                    + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        synchronize(clientName, tableName);
    }

    private void synchronize(String clientName, String tableName)
            throws ClientProtocolException, PermissionDeniedException,
            TableDoesNotExistException, AggregateInternalErrorException,
            IOException, UserDoesNotExistException
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTable(tableName);

        updateTables(clientName);
        TableEntry entry = getTableEntry(clientName, tableName);
        SynchronizedTable ownerTable = getOwnerTable(entry);

        conn.setUserID(clientName);
        Modification mod = conn.synchronize(tableName,
                table.getModificationNumber());

        table.setModificationNumber(mod.getModificationNumber());
        for (SynchronizedRow row : mod.getRows())
        {
            String aggregateRowIdentifier = row.getAggregateRowIdentifier();
            if (table.hasRowByIdentifier(aggregateRowIdentifier))
            {
                // existing row
                SynchronizedRow existingRow = table
                        .getRowByIdentifier(aggregateRowIdentifier);
                row.setRowID(existingRow.getRowID());
                table.updateRow(row);
            } else
            {
                // new row
                SynchronizedRow ownerRow = ownerTable
                        .getRowByIdentifier(aggregateRowIdentifier);
                row.setRowID(ownerRow.getRowID());
                table.insertRow(row);
            }
        }
    }

    private void printTable(List<String> arguments)
    {
        if (arguments.size() != 2)
            throw new RuntimeException("Bad arguments to printTable: "
                    + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        printTable(clientName, tableName);

    }

    private void printTable(String clientName, String tableName)
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTable(tableName);
        output.println(String.format("modification: %s",
                table.getModificationNumber()));
        List<SynchronizedRow> rows = new ArrayList<SynchronizedRow>(
                table.getRows());
        Collections.sort(rows, TestUtils.rowComparator);
        for (SynchronizedRow row : rows)
        {
            output.println(String.format("    %s", row.getRowID()));
            Map<String, String> columnValuePairs = new TreeMap<String, String>(
                    row.getColumnValuePairs());
            for (Entry<String, String> entry : columnValuePairs.entrySet())
            {
                output.println(String.format("        %s %s", entry.getKey(),
                        entry.getValue()));
            }
        }
    }

    /**
     * this.input should be sitting on the beginning of the line with the first
     * row. e.g.
     * 
     * <pre>
     *  updateSynchronizedRows user1 people
     * # this.input points at the first space
     * # |
     * # V
     *      row1
     *          name DylanUpdate
     *          age 23
     *          weight 175
     * </pre>
     * 
     * @return a map from rowIDs to rows.
     */
    private Map<String, SynchronizedRow> parseRows()
    {
        Map<String, SynchronizedRow> rows = new HashMap<String, SynchronizedRow>();
        String inputLine;
        while ((inputLine = input.findInLine("    \\w+ *")) != null)
        {
            StringTokenizer st = new StringTokenizer(inputLine);

            String rowID = st.nextToken();
            SynchronizedRow row = new SynchronizedRow();
            row.setRowID(rowID);
            input.nextLine();

            String inputLine2;
            while ((inputLine2 = input.findInLine("    \\w+ [^ \\t\\s]+ *")) != null)
            {
                StringTokenizer st2 = new StringTokenizer(inputLine2);

                String column = st2.nextToken();
                String value = st2.nextToken();
                row.setValue(column, value);

                input.nextLine();
            }
            rows.put(rowID, row);
        }
        return rows;
    }

    /**
     * this.input should be sitting on the beginning of the line with the first
     * filter. e.g.
     * 
     * <pre>
     *  cloneSynchronizedTable user1 people
     * # this.input points at the first space
     * # |
     * # V
     *      columnName filterType value
     * </pre>
     * 
     * @return a list of filters
     */
    private List<Filter> parseFilters()
    {
        List<Filter> filters = new ArrayList<Filter>();
        String inputLine;
        while ((inputLine = input.findInLine("    \\w+ \\w+ [^ \\t\\s]+ *")) != null)
        {
            StringTokenizer st = new StringTokenizer(inputLine);

            String columnName = st.nextToken();
            FilterOperation op = FilterOperation.valueOf(st.nextToken());
            String value = st.nextToken();
            Filter filter = new Filter(columnName, op, value);

            input.nextLine();

            filters.add(filter);
        }
        return filters;
    }

    /**
     * Updates this.tables.get(clientName) to be the latest list of table
     * entries that the client has permission to read.
     */
    private void updateTables(String clientName)
            throws ClientProtocolException, AggregateInternalErrorException,
            UserDoesNotExistException, IOException
    {
        conn.setUserID(clientName);
        List<TableEntry> entries = conn.listAllTables();
        aggregateTables.put(clientName, entries);
    }

    private TableEntry getTableEntry(String clientName, String tableName)
            throws TableDoesNotExistException
    {
        TableEntry table = null;
        for (TableEntry entry : this.aggregateTables.get(clientName))
        {
            String entryName = entry.getTableName();
            if (entryName.equals(tableName))
                table = entry;
        }
        if (table == null)
            throw new TableDoesNotExistException(tableName);

        return table;
    }

    private SynchronizedTable getOwnerTable(TableEntry table)
            throws TableDoesNotExistException
    {
        User owner = table.getUser();
        SynchronizedClient ownerClient = clients.get(owner.getUserName());
        SynchronizedTable ownerTable = ownerClient.getTable(table
                .getTableName());
        return ownerTable;
    }

    public static void main(String args[])
    {
        try
        {
            if (args.length != 3)
            {
                printUsage();
                return;
            } else
            {
                ClientTestDriver td;

                URI aggregateURI = new URI(args[0]);
                String userID = args[1];

                String fileName = args[2];
                File tests = new File(fileName);

                if (tests.exists() || tests.canRead())
                {
                    td = new ClientTestDriver(aggregateURI, userID,
                            new FileReader(tests), new OutputStreamWriter(
                                    System.out));
                } else
                {
                    System.err.println("Cannot read from " + tests.toString());
                    printUsage();
                    return;
                }

                td.runTests();
            }

        } catch (Exception e)
        {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
    }

    private static void printUsage()
    {
        System.err.println("Usage:");
        System.err
                .println("java org.opendatakit.odktables.client.ClientTestDriver <aggregateURI> <userID> <name of input script>");
    }

}
