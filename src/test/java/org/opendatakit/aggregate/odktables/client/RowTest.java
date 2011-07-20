package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class RowTest
{

    private InternalRow eq;
    private InternalRow eqSameData;
    private InternalRow notEqDiffRowId;
    private InternalRow notEqDiffValues;
    private InternalRow notEqDiffColumns;

    @Before
    public void setUp()
    {
        eq = new InternalRow("1");
        eq.setColumn("col1", "value1");

        eqSameData = new InternalRow("1");
        eqSameData.setColumn("col1", "value1");

        notEqDiffRowId = new InternalRow("2");
        notEqDiffRowId.setColumn("col1", "value1");

        notEqDiffValues = new InternalRow("1");
        notEqDiffValues.setColumn("col1", "value2");

        notEqDiffColumns = new InternalRow("1");
        notEqDiffColumns.setColumn("col2", "value1");
    }

    @Test
    public void testCtr()
    {
        InternalRow row = null;
        try
        {
            row = new InternalRow("1");
        } catch (Exception e)
        {
            fail("Constructor threw an exception!.");
        }
        assertEquals("1", row.getRowId());
    }

    @Test
    public void testGetValue()
    {
        assertEquals("value1", eq.getValue("col1"));
        assertNull(eq.getValue("col2"));
    }

    @Test
    public void testSetValue()
    {
        eq.setColumn("col1", "value2");
        assertEquals("value2", eq.getValue("col1"));
    }

    @Test
    public void testGetColumnValuePairs()
    {
        Map<String, String> columnValuePairs = eq.getColumnValuePairs();
        assertEquals(1, columnValuePairs.size());
        assertEquals("value1", columnValuePairs.get("col1"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRepExposure()
    {
        Map<String, String> columnValuePairs = eq.getColumnValuePairs();
        columnValuePairs.put("col2", "value2");
    }

    @Test
    public void testEqualsReflexive()
    {
        assertEquals(eq, eq);
    }

    @Test
    public void testEqualsSameData()
    {
        assertEquals(eq, eqSameData);
    }

    @Test
    public void testNotEqualsDifferentRowId()
    {
        assertFalse(eq.equals(notEqDiffRowId));
    }

    @Test
    public void testNotEqualsDifferentValues()
    {
        assertFalse(eq.equals(notEqDiffValues));
    }

    @Test
    public void testNotEqualsDifferentColumns()
    {
        assertFalse(eq.equals(notEqDiffColumns));
    }

    @Test
    public void testHashCodeConsistent()
    {
        assertEquals(eq.hashCode(), eq.hashCode());
    }

    @Test
    public void testHashCodeSameData()
    {
        assertEquals(eq.hashCode(), eqSameData.hashCode());
    }

    @Test
    public void testHashCodeDifferentRowId()
    {
        assertFalse(eq.hashCode() == notEqDiffRowId.hashCode());
    }

    @Test
    public void testHashCodeDifferentValues()
    {
        assertFalse(eq.hashCode() == notEqDiffValues.hashCode());
    }

    @Test
    public void testHashCodeDifferentColumns()
    {
        assertFalse(eq.hashCode() == notEqDiffColumns.hashCode());
    }
}
