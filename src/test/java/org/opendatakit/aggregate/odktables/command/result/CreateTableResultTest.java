package org.opendatakit.aggregate.odktables.command.result;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.CommandResultTest;
import org.opendatakit.aggregate.odktables.client.exception.TableAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.CreateTableResult;

public class CreateTableResultTest extends CommandResultTest<CreateTableResult>
{

    private String userId;
    private String tableId;

    @Before
    public void setUp()
    {
        userId = "1";
        tableId = "1";
        super.setUp();
    }

    @Override
    protected CreateTableResult createSuccessfulResult()
    {
        return CreateTableResult.success(userId, tableId);
    }

    @Override
    protected CreateTableResult createFailureResult()
    {
        return CreateTableResult.failure(userId, tableId,
                FailureReason.TABLE_ALREADY_EXISTS);
    }

    @Test
    public void testGetCreatedTableIdOnSuccess()
            throws TableAlreadyExistsException, UserDoesNotExistException
    {
        assertEquals(tableId, success.getCreatedTableId());
    }

    @Test(expected = TableAlreadyExistsException.class)
    public void testGetCreatedTableIdOnFailureTableAlreadyExists()
            throws TableAlreadyExistsException, UserDoesNotExistException
    {
        failure.getCreatedTableId();
    }

    @Test(expected = UserDoesNotExistException.class)
    public void testGetCreatedTableIdOnFailureUserDoesNotExist()
            throws TableAlreadyExistsException, UserDoesNotExistException
    {
        CreateTableResult.failure(userId, tableId,
                FailureReason.USER_DOES_NOT_EXIST).getCreatedTableId();
    }
}
