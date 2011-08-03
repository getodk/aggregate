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

    private Map<String, SynchronizedClient> clients;
    private Scanner input;
    private PrintWriter output;

    public ClientTestDriver(Reader r, Writer w)
    {
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
            if (command.equals("createClient"))
            {
                createClient(arguments);
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

    private void createClient(List<String> arguments)
    {
        if (arguments.size() != 1)
            throw new RuntimeException("Bad arguments to createClient: "
                    + arguments);

        String clientName = arguments.get(0);
        createClient(clientName);
    }

    private void createClient(String clientName)
    {
        SynchronizedClient client = new SynchronizedClient(clientName);
        clients.put(clientName, client);
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
    }

    private void insertSynchronizedRows(List<String> arguments)
    {

    }

    private void insertSynchronizedRows(String clientName, String tableName,
            List<SynchronizedRow> rows)
    {

    }

    private void updateSynchronizedRows(List<String> arguments)
    {

    }

    private void updateSynchronizedRows(String clientName, String tableName,
            List<SynchronizedRow> rows)
    {

    }

    private void printTable(List<String> arguments)
    {

    }

    private void printTable(String clientName, String tableName)
    {

    }

    public static void main(String args[])
    {
        try
        {
            if (args.length > 1)
            {
                printUsage();
                return;
            }

            ClientTestDriver td;

            if (args.length == 0)
            {
                td = new ClientTestDriver(new InputStreamReader(System.in),
                        new OutputStreamWriter(System.out));
            } else
            {

                String fileName = args[0];
                File tests = new File(fileName);

                if (tests.exists() || tests.canRead())
                {
                    td = new ClientTestDriver(new FileReader(tests),
                            new OutputStreamWriter(System.out));
                } else
                {
                    System.err.println("Cannot read from " + tests.toString());
                    printUsage();
                    return;
                }
            }

            td.runTests();

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
                .println("to read from a file: java org.opendatakit.odktables.client.ClientTestDriver <name of input script>");
        System.err
                .println("to read from standard in: java org.opendatakit.odktables.client.ClientTestDriver");
    }

}
