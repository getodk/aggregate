package org.opendatakit.aggregate.odktables;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Properties;

import org.opendatakit.aggregate.odktables.client.ScriptFileTests;
import org.opendatakit.aggregate.odktables.client.entity.Row;

/**
 * Utilities for testing org.opendatakit.aggregate.odktables package.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TestUtils
{
    public static final Comparator<Row> rowComparator = new Comparator<Row>()
    {
        @Override
        public int compare(Row row1, Row row2)
        {
            if (row1.getRowID() != null && row2.getRowID() != null)
                return row1.getRowID().compareTo(row2.getRowID());
            else if (row1.getAggregateRowIdentifier() != null
                    && row2.getAggregateRowIdentifier() != null)
                return row1.getAggregateRowIdentifier().compareTo(
                        row2.getAggregateRowIdentifier());
            else
                return 0;
        }

    };

    /**
     * Retrieves the properties found in ODKTablesTestSuite.properties in the
     * same directory as ODKTablesTestSuite.class
     * 
     * @return the properties, loaded up if the file was found, empty otherwise
     */
    public static Properties getTestProperties()
    {
        Properties props = new Properties();
        try
        {
            String className = ODKTablesTestSuite.class.getSimpleName();
            File myDirectory = new File(ScriptFileTests.class.getResource(
                    className + ".class").toURI()).getParentFile();
            String propertiesFile = myDirectory.getAbsolutePath()
                    + File.pathSeparator + className + ".properties";
            Reader reader = new FileReader(propertiesFile);
            props.load(reader);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        return props;
    }
}
