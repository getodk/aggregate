package org.opendatakit.aggregate.odktables;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.opendatakit.aggregate.odktables.client.SimpleAPITest;
import org.opendatakit.aggregate.odktables.client.ScriptFileTests;

/**
 * Requires that Aggregate is running and ODKTablesTestSuite.properties is set
 * up correctly.
 * 
 * @author the.dylan.price@gmail.com
 */

@RunWith(Suite.class)
@SuiteClasses({ SimpleAPITest.class, ScriptFileTests.class })
public class ODKTablesTestSuite
{

}
