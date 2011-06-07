package org.opendatakit.aggregate.odktables.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.client.Column;
import org.opendatakit.common.persistence.DataField.DataType;

public class CreateTableTest
{

    private CreateTable eq1;
    private CreateTable eq2;
    private CreateTable notEqDifferentTableId;
    private CreateTable notEqDifferentTableName;
    private CreateTable notEqDifferentColumns;

    @Before
    public void setUp()
    {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));

        eq1 = new CreateTable("user1", "table1", "Table 1", columns);
        eq2 = new CreateTable("user1", "table1", "Table 1", columns);

        notEqDifferentTableId = new CreateTable("user1", "table2", "Table 1",
                columns);
        notEqDifferentTableName = new CreateTable("user1", "table1",
                "Not equals", columns);
        columns.add(new Column("COL_2", DataType.DECIMAL, true));
        notEqDifferentColumns = new CreateTable("user1", "table1", "Table 1",
                columns);
    }

    @Test
    public void testCtrCrazyParameters()
    {
        // Test all values of CreateTable ctr spec
        String userId = "12___dskfj__ASDFASD223904";
        String tableId = "12___dskfj__ASDFASD223904";
        String tableName = "this can be '''###%#^%&%__--++@^@^@ anything!";
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("COL_1", DataType.STRING, true));
        CreateTable table = null;
        try
        {
            table = new CreateTable(userId, tableId, tableName, columns);
        } catch (Exception e)
        {
            fail("Constructor threw an exception");
        }
        assertEquals(tableId, table.getTableId());
        assertEquals(tableName, table.getTableName());
        assertEquals(columns, table.getColumns());
    }

    @Test
    public void testEqualsReflexive()
    {
        assertEquals(eq1, eq1);
    }

    @Test
    public void testEqualsSameData()
    {
        assertEquals(eq1, eq2);
        assertEquals(eq2, eq1);
    }

    @Test
    public void testNotEqualsDifferentTableId()
    {
        assertFalse(eq1.equals(notEqDifferentTableId));
    }

    @Test
    public void testNotEqualsDifferentTableName()
    {
        assertFalse(eq1.equals(notEqDifferentTableName));
    }

    @Test
    public void testNotEqualsDifferentColumns()
    {
        assertFalse(eq1.equals(notEqDifferentColumns));
    }

    @Test
    public void testHashCodeSameObject()
    {
        assertEquals(eq1.hashCode(), eq1.hashCode());
    }

    @Test
    public void testHashCodeSameData()
    {
        assertEquals(eq1.hashCode(), eq2.hashCode());
    }

    @Test
    public void testHashCodeDifferentData()
    {
        assertFalse(eq1.hashCode() == notEqDifferentTableId.hashCode());
        assertFalse(eq1.hashCode() == notEqDifferentTableName.hashCode());
        assertFalse(eq1.hashCode() == notEqDifferentColumns.hashCode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutability()
    {
        List<Column> columns = eq1.getColumns();
        columns.remove(0);
        columns.add(new Column("new_col", DataType.INTEGER, false));
    }
}
