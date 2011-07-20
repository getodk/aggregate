package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;

public abstract class CommandResultTest<T extends CommandResult<?>>
{

    protected T success;
    protected T failure;
    private boolean shouldTestFailure;

    public void setUp()
    {
        success = createSuccessfulResult();
        failure = createFailureResult();
        shouldTestFailure = true;
    }

    protected abstract T createSuccessfulResult();

    protected abstract T createFailureResult();

    protected void dontTestFailure()
    {
        shouldTestFailure = false;
    }

    @Test
    public void testSuccessful()
    {
        assertTrue(success.successful());
        if (shouldTestFailure)
            assertFalse(failure.successful());
    }

    @Test
    public void testGetReason()
    {
        assertNull(success.getReason());
        if (shouldTestFailure)
            assertNotNull(failure.getReason());
    }

    @Test
    public void testEqualsReflexive()
    {
        assertEquals(success, success);
        if (shouldTestFailure)
            assertEquals(failure, failure);
    }

    @Test
    public void testEqualsSameData()
    {
        assertEquals(success, createSuccessfulResult());
        if (shouldTestFailure)
            assertEquals(failure, createFailureResult());
    }

    @Test
    public void testNotEquals()
    {
        assertFalse(success.equals(failure));
    }

    @Test
    public void testHashCodeConsistent()
    {
        assertEquals(success.hashCode(), success.hashCode());
        if (shouldTestFailure)
            assertEquals(failure.hashCode(), failure.hashCode());
    }

    @Test
    public void testHashCodeSameData()
    {
        assertEquals(success.hashCode(), createSuccessfulResult().hashCode());
        if (shouldTestFailure)
            assertEquals(failure.hashCode(), createFailureResult().hashCode());
    }
}
