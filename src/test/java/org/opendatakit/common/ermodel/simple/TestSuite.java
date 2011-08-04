package org.opendatakit.common.ermodel.simple;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for org.opendatakit.common.ermodel.simple package.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
@RunWith(Suite.class)
@SuiteClasses({ EntityTest.class, RelationTest.class, QueryTest.class })
public class TestSuite extends junit.framework.TestSuite
{

}
