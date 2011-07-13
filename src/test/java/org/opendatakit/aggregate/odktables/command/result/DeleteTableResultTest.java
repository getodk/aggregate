package org.opendatakit.aggregate.odktables.command.result;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.CommandResultTest;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.commandresult.simple.DeleteTableResult;

public class DeleteTableResultTest extends CommandResultTest<DeleteTableResult>
{

    private String tableId;

    @Before
    public void setUp()
    {
        tableId = "1";
        super.setUp();
        super.dontTestFailure();
    }

    @Override
    protected DeleteTableResult createSuccessfulResult()
    {
        return DeleteTableResult.success(tableId);
    }

    @Override
    protected DeleteTableResult createFailureResult()
    {
        return null;
    }

    @Test
    public void testGetCreatedTableIdOnSuccess()
            throws TableAlreadyExistsException
    {
        assertEquals(tableId, success.getDeletedTableId());
    }
}
