package org.opendatakit.aggregate.odktables;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.opendatakit.aggregate.odktables.client.ColumnTest;
import org.opendatakit.aggregate.odktables.client.RowTest;
import org.opendatakit.aggregate.odktables.command.CreateTableTest;
import org.opendatakit.aggregate.odktables.command.logic.CreateTableLogicTest;
import org.opendatakit.aggregate.odktables.command.logic.DeleteTableLogicTest;
import org.opendatakit.aggregate.odktables.command.logic.InsertRowsLogicTest;
import org.opendatakit.aggregate.odktables.command.result.CreateTableResultTest;
import org.opendatakit.aggregate.odktables.command.result.DeleteTableResultTest;
import org.opendatakit.aggregate.odktables.command.result.InsertRowsResultTest;
import org.opendatakit.aggregate.odktables.relation.TableIndexTest;
import org.opendatakit.aggregate.odktables.relation.TableTest;
import org.opendatakit.aggregate.odktables.relation.UsersTest;

@RunWith(Suite.class)
@SuiteClasses({ TableIndexTest.class, TableTest.class, UsersTest.class,
        ColumnTest.class, RowTest.class, CommandConverterTest.class,
        CommandLogicTest.class, CreateTableTest.class,
        CreateTableLogicTest.class, CreateTableResultTest.class, //InsertRowsTest.class,
        InsertRowsLogicTest.class, InsertRowsResultTest.class, // DeleteTableTest.class,
        DeleteTableLogicTest.class, DeleteTableResultTest.class })
public class TestSuite
{

}
