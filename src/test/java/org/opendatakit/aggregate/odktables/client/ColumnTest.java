package org.opendatakit.aggregate.odktables.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.DataField.DataType;

public class ColumnTest
{

    private InternalColumn eq;
    private InternalColumn eqSameData;
    private InternalColumn eqDiffCaseName;
    private InternalColumn notEqDiffName;
    private InternalColumn notEqDiffType;
    private InternalColumn notEqDiffNullable;

    @Before
    public void setUp()
    {
        eq = new InternalColumn("col1", DataType.STRING, false);
        eqSameData = new InternalColumn("col1", DataType.STRING, false);
        eqDiffCaseName = new InternalColumn("COL1", DataType.STRING, false);
        notEqDiffName = new InternalColumn("differentname", DataType.STRING, false);
        notEqDiffType = new InternalColumn("COL1", DataType.INTEGER, false);
        notEqDiffNullable = new InternalColumn("COL1", DataType.STRING, true);
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
