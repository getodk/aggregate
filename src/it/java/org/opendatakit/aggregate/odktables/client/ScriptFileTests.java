package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.aggregate.odktables.client.LabelledParameterized.Labels;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;

/**
 * <p>
 * Taken from CSE 331 class taught by Michael Ernst at University of Washington.
 * Modified by Dylan Price <the.dylan.price@gmail.com> to use my test driver.
 * </p>
 * 
 * <p>
 * Finds all files ending in .test, runs them using the ClientTestDriver and
 * saves the output in a .expected file. Compares the .expected to the already
 * existing .actual file and fails if there are differences.
 * </p>
 */
@RunWith(LabelledParameterized.class)
public class ScriptFileTests
{

    //static fields and methods used during setup of the parameterized runner
    private static FileFilter testFileFilter = new FileFilter()
    {
        public boolean accept(File file)
        {
            return file.getName().endsWith(".test");
        }
    };
    private static List<String> testScriptNames = null; // not yet calculated
    private static List<File> testScriptFiles = null; // not yet calculated

    //used by the actual test instance
    private final File testScriptFile;

    /**
     * This method searches for and creates file handles for each script test.
     * It only searches the immediate directory where the ScriptFileTests.class
     * classfile is located.
     */
    public static void calculateTestFiles()
    {
        if (ScriptFileTests.testScriptFiles != null
                || ScriptFileTests.testScriptNames != null)
        {
            //already initialized
            return;
        }

        ScriptFileTests.testScriptNames = new LinkedList<String>();
        ScriptFileTests.testScriptFiles = new LinkedList<File>();
        try
        {
            // getResource() cannot be null: this file itself is ScriptFileTests
            // getParentFile() cannot be null: ScriptFileTests has a package
            File myDirectory = new File(ScriptFileTests.class.getResource(
                    "ScriptFileTests.class").toURI()).getParentFile();
            String testsPath = myDirectory.getAbsolutePath() + File.separator
                    + "tests";
            myDirectory = new File(testsPath);
            for (File f : myDirectory.listFiles(ScriptFileTests.testFileFilter))
            {
                testScriptNames.add(f.getName());
                testScriptFiles.add(f);
            }

        } catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is called in the constructor of Parameterized.
     * 
     * @return List of argument arrays that should be invoked on the
     *         ScriptFileTests constructor by the Parameterized test runner.
     *         Since that runner's constructor has one parameter, the array only
     *         has one element.
     */
    @Parameters
    public static List<Object[]> getTestFiles()
    {
        ScriptFileTests.calculateTestFiles();

        if (ScriptFileTests.testScriptFiles == null)
            throw new IllegalStateException(
                    "Did not initialise any files to test!");

        //we have to wrap testScriptFiles here so Parameterized.class receives a list of arg array.
        List<Object[]> filesToTest = new ArrayList<Object[]>(
                testScriptFiles.size());
        for (File f : ScriptFileTests.testScriptFiles)
        {
            filesToTest.add(new Object[] { f });
        }

        return filesToTest;
    }

    /**
     * This method is called in the constructor of LabelledParameterized. Since
     * getTestFiles (and thus calculateTestFiles()) should have already been
     * called by the Parameterized constructor, the test script names should
     * already have been computed.
     * 
     * @return List of labels to be used as names for each of the parameterized
     *         tests. These names are the same as the script file used to run
     *         the test.
     */
    @Labels
    public static List<String> getTestLabels()
    {
        if (ScriptFileTests.testScriptNames == null)
            throw new IllegalStateException(
                    "Must initialize list of test names before creating tests.");

        return ScriptFileTests.testScriptNames;
    }

    /**
     * This constructor is reflectively called by the Parameterized runner. It
     * creates a script file test instance, representing one script file to be
     * tested.
     */
    public ScriptFileTests(File testScriptFile)
    {
        this.testScriptFile = testScriptFile;
    }

    /**
     * Reads in the contents of a file
     * 
     * @throws FileNotFoundException
     *             , IOException
     * @requires that the specified File exists && File ends with a newline
     * @returns the contents of that file
     */
    private String fileContents(File f) throws IOException
    {
        if (f == null)
            throw new IllegalArgumentException("No file specified");

        BufferedReader br = new BufferedReader(new FileReader(f));

        StringBuilder result = new StringBuilder();
        String line = null;

        //read line reads up to *any* newline character
        while ((line = br.readLine()) != null)
        {
            result.append(line);
            result.append('\n');
        }

        br.close();
        return result.toString();
    }

    /**
     * @throws IOException
     * @throws URISyntaxException
     * @throws AggregateInternalErrorException
     * @throws UserDoesNotExistException
     * @requires there exists a test file indicated by testScriptFile
     * 
     * @effects runs the test in filename, and output its results to a file in
     *          the same directory with name filename+".actual"; if that file
     *          already exists, it will be overwritten.
     * @returns the contents of the output file
     */
    private String runScriptFile() throws IOException, URISyntaxException,
            UserDoesNotExistException, AggregateInternalErrorException
    {
        if (testScriptFile == null)
            throw new RuntimeException("No file specified");

        File actual = fileWithSuffix("actual");

        Properties props = TestUtils.getTestProperties();
        URI aggregateURI = new URI(props.getProperty("aggregateURI",
                "http://localhost:8888/"));
        String adminUserID = props.getProperty("adminUserID", "bob");

        Reader r = new FileReader(testScriptFile);
        Writer w = new PrintWriter(actual);

        ClientTestDriver driver = new ClientTestDriver(aggregateURI,
                adminUserID, r, w);
        driver.runTests();

        return fileContents(actual);
    }

    /**
     * @param newSuffix
     * @return a File with the same name as testScriptFile, except that the test
     *         suffix is replaced by the given suffix
     */
    private File fileWithSuffix(String newSuffix)
    {
        File parent = testScriptFile.getParentFile();
        String driverName = testScriptFile.getName();
        String baseName = driverName.substring(0,
                driverName.length() - "test".length());

        return new File(parent, baseName + newSuffix);
    }

    /**
     * The only test that is run: run a script file and test its output.
     * 
     * @throws IOException
     * @throws URISyntaxException
     * @throws AggregateInternalErrorException
     * @throws UserDoesNotExistException
     */
    @Test
    public void checkAgainstExpectedOutput() throws IOException,
            UserDoesNotExistException, AggregateInternalErrorException,
            URISyntaxException
    {
        File expected = fileWithSuffix("expected");
        assertEquals(testScriptFile.getName(), fileContents(expected),
                runScriptFile());
    }
}
