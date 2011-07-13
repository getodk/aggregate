package org.opendatakit.aggregate.odktables.command.result;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.CommandResultTest;
import org.opendatakit.aggregate.odktables.client.exception.ODKTablesClientException;
import org.opendatakit.aggregate.odktables.client.exception.RowAlreadyExistsException;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.commandresult.simple.InsertRowsResult;

public class InsertRowsResultTest extends CommandResultTest<InsertRowsResult>
{

    private String tableId;
    private List<String> rowIds;
    private FailureReason reason;
    private String failedRowId;

    @Before
    public void setUp()
    {
        tableId = "1";

        rowIds = new ArrayList<String>();
        rowIds.add("1");
        rowIds.add("2");
        rowIds.add("3");

        reason = FailureReason.ROW_ALREADY_EXISTS;

        failedRowId = "3";
        super.setUp();
    }

    @Override
    protected InsertRowsResult createSuccessfulResult()
    {
        return InsertRowsResult.success(tableId, rowIds);
    }

    @Override
    protected InsertRowsResult createFailureResult()
    {
        return InsertRowsResult.failure(tableId, failedRowId);
    }

    @Test
    public void testGetInsertedRowIdsSuccess() throws RowAlreadyExistsException, ODKTablesClientException
    {
        assertEquals(rowIds, success.getInsertedRowIds());
    }

    @Test(expected = RowAlreadyExistsException.class)
    public void testGetInsertedRowIdsFailure() throws RowAlreadyExistsException, ODKTablesClientException
    {
        failure.getInsertedRowIds();
    }
}
