package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.DataField.DataType;

public class ColumnTest
{

    private Column eq;
    private Column eqSameData;
    private Column eqDiffCaseName;
    private Column notEqDiffName;
    private Column notEqDiffType;
    private Column notEqDiffNullable;

    @Before
    public void setUp()
    {
        eq = new Column("col1", DataType.STRING, false);
        eqSameData = new Column("col1", DataType.STRING, false);
        eqDiffCaseName = new Column("COL1", DataType.STRING, false);
        notEqDiffName = new Column("differentname", DataType.STRING, false);
        notEqDiffType = new Column("COL1", DataType.INTEGER, false);
        notEqDiffNullable = new Column("COL1", DataType.STRING, true);
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
    public void testEqualsDifferentCaseName()
    {
        assertEquals(eq, eqDiffCaseName);
    }

    @Test
    public void testNotEqualsDifferentName()
    {
        assertFalse(eq.equals(notEqDiffName));
    }

    @Test
    public void testNotEqualsDifferentType()
    {
        assertFalse(eq.equals(notEqDiffType));
    }

    @Test
    public void testNotEqualsDifferentNullable()
    {
        assertFalse(eq.equals(notEqDiffNullable));
    }

    @Test
    public void testHashCodeSameObject()
    {
        assertEquals(eq.hashCode(), eq.hashCode());
    }

    @Test
    public void testHashCodeSameData()
    {
        assertEquals(eq.hashCode(), eqSameData.hashCode());
    }

    @Test
    public void testHashCodeDifferent()
    {
        assertFalse(eq.hashCode() == notEqDiffName.hashCode());
        assertFalse(eq.hashCode() == notEqDiffType.hashCode());
        assertFalse(eq.hashCode() == notEqDiffNullable.hashCode());
    }
}
