package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.client.Column;
import org.opendatakit.aggregate.odktables.command.CreateTable;
import org.opendatakit.aggregate.odktables.command.DeleteTable;
import org.opendatakit.aggregate.odktables.command.InsertRows;
import org.opendatakit.aggregate.odktables.command.logic.CreateTableLogic;
import org.opendatakit.aggregate.odktables.command.logic.DeleteTableLogic;
import org.opendatakit.aggregate.odktables.command.logic.InsertRowsLogic;
import org.opendatakit.common.persistence.DataField;

public class CommandLogicTest
{

    private String userId;
    private String tableId;
    private String rowId;
    private Column column;
    private DataField field;
    private String tableIdContainsTBL_;
    private String rowIdContainsROW_;

    private CreateTable createTable;
    private InsertRows insertRows;
    private DeleteTable deleteTable;

    @Before
    public void setUp()
    {
        userId = TestUtils.userId;
        tableId = TestUtils.tableId;
        rowId = TestUtils.rowId;
        column = TestUtils.column;
        field = TestUtils.field;
        tableIdContainsTBL_ = "TABL_";
        rowIdContainsROW_ = "ROW_";

        createTable = TestUtils.createTable;
        insertRows = TestUtils.insertRows;
        deleteTable = TestUtils.deleteTable;
    }

    @Test
    public void testNewCommandLogic()
    {
        CommandLogic<?> createTableLogic = CommandLogic
                .newInstance(createTable);
        assertTrue(createTableLogic instanceof CreateTableLogic);

        CommandLogic<?> insertRowsLogic = CommandLogic.newInstance(insertRows);
        assertTrue(insertRowsLogic instanceof InsertRowsLogic);

        CommandLogic<?> deleteTableLogic = CommandLogic
                .newInstance(deleteTable);
        assertTrue(deleteTableLogic instanceof DeleteTableLogic);
    }

    @Test
    public void testConvertUserId()
    {
        String convertedUserId = CommandLogic.convertUserId(userId);
        assertEquals(userId, CommandLogic.unconvertUserId(convertedUserId));
    }

    @Test
    public void testConvertUserIdWithLetters()
    {
        String userId = "hello1";
        String convertedUserId = CommandLogic.convertUserId(userId);
        assertTrue(userId.equalsIgnoreCase(CommandLogic
                .unconvertUserId(convertedUserId)));
    }

    @Test
    public void testConvertTableId()
    {
        String convertedTableId = CommandLogic.convertTableId(tableId);
        assertEquals(tableId, CommandLogic.unconvertTableId(convertedTableId));
    }

    @Test
    public void testRowURI()
    {
        String rowURI = CommandLogic.createRowURI(tableId, rowId);
        assertEquals(tableId, CommandLogic.getTableId(rowURI));
        assertEquals(rowId, CommandLogic.getRowId(rowURI));
    }

    @Test
    public void testColumnToDataField()
    {
        DataField convertedField = CommandLogic.columnToDataField(column);
        assertTrue(field.getName().equalsIgnoreCase(convertedField.getName()));
        assertEquals(field.getDataType(), convertedField.getDataType());
        assertEquals(field.getNullable(), convertedField.getNullable());
    }

    /* Whitebox tests */
    @Test
    public void testConvertTableIdContainsTBL_()
    {
        String convertedTableId = CommandLogic
                .convertTableId(tableIdContainsTBL_);
        assertEquals(tableIdContainsTBL_,
                CommandLogic.unconvertTableId(convertedTableId));
    }

    @Test
    public void testRowURIContainsROW_()
    {
        String rowURI = CommandLogic.createRowURI(tableId, rowIdContainsROW_);
        assertEquals(tableId, CommandLogic.getTableId(rowURI));
        assertEquals(rowIdContainsROW_, CommandLogic.getRowId(rowURI));
    }

}
