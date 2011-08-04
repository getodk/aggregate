package org.opendatakit.aggregate.odktables;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.opendatakit.aggregate.odktables.client.AggregateConnectionTest;
import org.opendatakit.aggregate.odktables.client.ScriptFileTests;

/**
 * Requires that Aggregate is running and ODKTablesTestSuite.properties is set
 * up correctly.
 * 
 * @author the.dylan.price@gmail.com
 */

@RunWith(Suite.class)
@SuiteClasses({ AggregateConnectionTest.class, ScriptFileTests.class })
public class ODKTablesTestSuite
{

}
