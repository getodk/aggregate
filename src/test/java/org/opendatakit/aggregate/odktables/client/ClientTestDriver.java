package org.opendatakit.aggregate.odktables.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.opendatakit.aggregate.odktables.client.entity.Column;
import org.opendatakit.aggregate.odktables.client.entity.SynchronizedRow;
import org.opendatakit.common.ermodel.simple.AttributeType;

public class ClientTestDriver
{

    private SynchronizedAPI conn;
    private Map<String, SynchronizedClient> clients;
    private Scanner input;
    private PrintWriter output;

    public ClientTestDriver(SynchronizedAPI conn, Reader r, Writer w)
    {
        this.conn = conn;
        clients = new HashMap<String, SynchronizedClient>();
        input = new Scanner(r);
        input.useDelimiter("");
        output = new PrintWriter(w);
    }

    public void runTests()
    {
        while (input.hasNextLine())
        {
            String line = input.nextLine();
            if (line.length() == 0 || line.charAt(0) == '#')
            {
                // echo blank and comment lines
                output.println(line);
            } else
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
        output.flush();
    }

    private void executeCommand(String command, List<String> arguments)
    {
        try
        {
            if (command.equals("createUser"))
            {
                createUser(arguments);
            } else if (command.equals("createSynchronizedTable"))
            {
                createSynchronizedTable(arguments);
            } else if (command.equals("insertSynchronizedRows"))
            {
                insertSynchronizedRows(arguments);
            } else if (command.equals("updateSynchronizedRows"))
            {
                updateSynchronizedRows(arguments);
            } else if (command.equals("printTable"))
            {
                printTable(arguments);
            }
        } catch (Exception e)
        {
            output.println("Exception: " + e.toString());
        }
    }

    private void createUser(List<String> arguments)
    {
        if (arguments.size() != 1)
            throw new RuntimeException("Bad arguments to createUser: "
                    + arguments);

        String clientName = arguments.get(0);
        createUser(clientName);
    }

    private void createUser(String clientName)
    {
        SynchronizedClient client = new SynchronizedClient(clientName);
        clients.put(clientName, client);
        // conn.createUser
    }

    private void createSynchronizedTable(List<String> arguments)
    {
        if (arguments.size() != 2)
            throw new RuntimeException(
                    "Bad arguments to createSynchronizedTable: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        List<Column> columns = new ArrayList<Column>();
        String inputLine;
        while((inputLine = input.findInLine("    \\w+ [A-Z_]+ (false)|(true) *")) != null)
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
            List<Column> columns)
    {
        SynchronizedTable table = new SynchronizedTable(tableName, tableName,
                columns);
        SynchronizedClient client = clients.get(clientName);
        client.addTable(table);
        // Modification modification = conn.createSynchronizedTable
        table.setModificationNumber(modification.getModificationNumber);
    }

    private void insertSynchronizedRows(List<String> arguments)
    {
        if (arguments.size() != 2)
            throw new RuntimeException("Bad arguments to insertSynchronizedRows: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        Map<String, SynchronizedRow> rows = parseRows();
        insertSynchronizedRows(clientName, tableName, rows);
    }

    private void insertSynchronizedRows(String clientName, String tableName,
            Map<String, SynchronizedRow> rows)
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTables(tableName);
        // Modification mod = client.insertSynchronizedRows(
        for (SynchronizedRow updatedRow : mod.getRows())
        {
            SynchronizedRow row = rows.get(updatedRow.getRowID());

            String aggregateRowIdentifier = updatedRow.getAggregateRowIdentifier();
            String revisionTag = updatedRow.getRevisionTag();

            row.setAggregateRowIdentifier(aggregateRowIdentifier);
            row.setRevisionTag(revisionTag);
            
            table.insertRow(row);
        }
    }

    private void updateSynchronizedRows(List<String> arguments)
    {
        if (arguments.size() != 2)
            throw new RuntimeException("Bad arguments to insertSynchronizedRows: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        Map<String, SynchronizedRow> rows = parseRows();
        updateSynchronizedRows(clientName, tableName, rows);

    }

    private void updateSynchronizedRows(String clientName, String tableName,
            Map<String, SynchronizedRow> rows)
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTables(tableName);

        List<SynchronizedRow> existingRows = table.getRows(rowMap.keySet());
        for (SynchronizedRow existingRow : existingRows)
        {
           SynchronizedRow row = rows.get(existingRow.getRowID());
           row.setAggregateRowIdentifier(existingRow.getAggregateRowIdentifier());
           row.setRevisionTag(existingRow.getRevisionTag());
        }
        // Modification mod = client.updateSynchronizedRows(
        for (SynchronizedRow updatedRow : mod.getRows())
        {
            SynchronizedRow row = rows.get(updatedRow.getRowID());
            row.setRevisionTag(updatedRow.getRevisionTag());
            table.updateRow(row);
        }
    }

    /**
     * this.input should be sitting on the beginning of the line with the first row.
     * e.g.
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
        while((inputLine = input.findInLine("    \\w+ *")) != null)
        {
            StringTokenizer st = new StringTokenizer(inputLine);

            String rowID = st.nextToken();
            SynchronizedRow row = new SynchronizedRow();
            row.setRowID(rowID);
            input.nextLine();

            String inputLine2;
            while((inputLine2 = input.findInLine("    \\w+ \\w+ *")) != null);
            {
                StringTokenizer st = new StringTokenizer(inputLine2);

                String column = st.nextToken();
                String value = st.nextToken();
                row.setValue(column, value);

                input.nextLine();
            } 
            rows.put(rowID, row);
        }
        return rows;
    }

    private void printTable(List<String> arguments)
    {
        if (arguments.size() != 2)
            throw new RuntimeException("Bad arguments to printTable: " + arguments);

        String clientName = arguments.get(0);
        String tableName = arguments.get(1);
        printTable(clientName, tableName);

    }

    private void printTable(String clientName, String tableName)
    {
        SynchronizedClient client = clients.get(clientName);
        SynchronizedTable table = client.getTable(tableName);
        output.println(String.format("%s %s\n%s %s", clientName, tableName, table.getAggregateTableIdentifier(), table.getModificationNumber()));
        for (SynchronizedRow row : table.getRows())
        {
            output.println(String.format("    %s", row.getRowID()));
            for (Entry<String, String> entry : row.getColumnValuePairs().entrySet())
            {
                output.println(String.format("        %s %s", entry.getKey(), entry.getValue()));
            }
        }
    }

    public static void main(String args[])
    {
        try
        {
            if (args.length != 3)
            {
                printUsage();
                return;
            }
            else
            {
                ClientTestDriver td;

                URI aggregateURI = new URI(args[0]);
                String userID = args[1];
                SynchronizedAPI conn = new SynchronizedAPI(aggregateURI, userID);

                String fileName = args[2];
                File tests = new File(fileName);

                if (tests.exists() || tests.canRead())
                {
                    td = new ClientTestDriver(conn, new FileReader(tests),
                            new OutputStreamWriter(System.out));
                } else
                {
                    System.err.println("Cannot read from " + tests.toString());
                    printUsage();
                    return;
                }

                td.runTests();
            }


        } catch (IOException e)
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
