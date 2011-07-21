package org.opendatakit.common.ermodel.simple;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EntityTest.class, RelationTest.class, QueryTest.class })
public class TestSuite extends junit.framework.TestSuite
{

}
