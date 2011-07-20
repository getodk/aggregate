package org.opendatakit.aggregate.odktables.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Test case for TableIndex. Depends on the Users functioning correctly.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableIndexTest
{
    private String userId;
    private String tableId;
    private String tableName;
    private List<DataField> tableFields;
    private String serialFields;
    private CallingContext cc;
    private TableIndex index;

    @Before
    public void setUp() throws ODKDatastoreException
    {
        userId = "USER1";
        tableId = "TABLE1";
        tableName = "Table 1";
        tableFields = new ArrayList<DataField>();
        tableFields.add(new DataField("FIELD_1", DataType.STRING, false));
        tableFields.add(new DataField("FIELD_2", DataType.INTEGER, false));
        serialFields = "{FIELD_1:STRING,FIELD_2:INTEGER,}";
        cc = TestContextFactory.getCallingContext();
        index = TableIndex.getInstance(cc);
        Users.getInstance(cc).createUser(userId, "The User");
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        index.deleteTable(userId, tableId);
        index.dropRelation(cc);
        Users.getInstance(cc).dropRelation(cc);
    }

    @Test
    public void testSerializeFields()
    {
        String serialized = TableIndex.serializeFields(tableFields);
        assertEquals(serialFields, serialized);
    }

    @Test
    public void testSerializeFieldsEmptyList()
    {
        String serialized = TableIndex
                .serializeFields(new ArrayList<DataField>());
        assertEquals("{}", serialized);
    }

    @Test
    public void testDeserializeFields()
    {
        List<DataField> deserialized = TableIndex
                .deserializeFields(serialFields);
        TestUtils.assertFieldListsAreEqual(tableFields, deserialized);
    }

    @Test
    public void testDeserializeFieldsEmptyList()
    {
        List<DataField> deserialized = TableIndex.deserializeFields("{}");
        assertTrue(deserialized.isEmpty());
    }

    @Test
    public void testCreateTable() throws ODKDatastoreException
    {
        index.createTable(userId, tableId, tableName, tableFields);
        List<Entity> entities = index.getEntities(TableIndex.TABLE_ID,
                FilterOperation.EQUAL, tableId, cc);
        assertEquals(1, entities.size());
        Entity entity = entities.get(0);
        assertEntityFields(tableId, tableName, tableFields, entity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTableLowerCaseTableId() throws ODKDatastoreException
    {
        String tableId = "lower_case_table_id";
        String tableName = "Lower Case Table Id";
        index.createTable(userId, tableId, tableName, tableFields);
        List<Entity> entities = index.getEntities(TableIndex.TABLE_ID,
                FilterOperation.EQUAL, tableId.toUpperCase(), cc);
        assertEquals(1, entities.size());
        Entity entity = entities.get(0);
        assertEntityFields(tableId.toUpperCase(), tableName, tableFields,
                entity);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testCreateTableBadTableId() throws ODKDatastoreException
    {
        String tableId = "1BAD_TABLE_NAME";
        String tableName = "Table 2";
        index.createTable(userId, tableId, tableName, tableFields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTableUserDoesNotExist() throws ODKDatastoreException
    {
        index.createTable("DOESNOTEXIST", tableId, tableName, tableFields);
    }

    @Test
    public void testGetTable() throws ODKDatastoreException
    {
        index.createTable(userId, tableId, tableName, tableFields);
        Table table = index.getTable(userId, tableId);
        assertDataFields(table, tableFields);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTableLowerCaseTableId() throws ODKDatastoreException
    {
        String tableId = "lower_case_table_id";
        String tableName = "Lower Case Table Id";
        index.createTable(userId, tableId, tableName, tableFields);
        Table table = index.getTable(userId, tableId);
        assertDataFields(table, tableFields);
    }

    @Test(expected = java.lang.RuntimeException.class)
    public void testGetTableDoesNotExist() throws ODKDatastoreException
    {
        index.getTable(userId, tableId);
    }

    @Test
    public void testDeleteTable() throws ODKDatastoreException
    {
        index.createTable(userId, tableId, tableName, tableFields);
        index.deleteTable(userId, tableId);
        List<Entity> entities = index.getEntities(TableIndex.TABLE_ID,
                FilterOperation.EQUAL, tableId, cc);
        assertEquals(0, entities.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteTableLowerCaseTableId() throws ODKDatastoreException
    {
        String tableId = "lower_case_table_id";
        String tableName = "Lower Case Table Id";
        index.createTable(userId, tableId, tableName, tableFields);
        index.deleteTable(userId, tableId);
        List<Entity> entities = index.getEntities(TableIndex.TABLE_ID,
                FilterOperation.EQUAL, tableId, cc);
        assertEquals(0, entities.size());
    }

    @Test
    public void testDeleteTableDoesNotExist() throws ODKDatastoreException
    {
        index.deleteTable(userId, tableId);
        List<Entity> entities = index.getEntities(TableIndex.TABLE_ID,
                FilterOperation.EQUAL, tableId, cc);
        assertEquals(0, entities.size());
    }

    @Test
    public void testTableExistsTrue() throws ODKDatastoreException
    {
        index.createTable(userId, tableId, tableName, tableFields);
        assertTrue(index.tableExists(userId, tableId));
    }

    @Test
    public void testTableExistsFalse() throws ODKDatastoreException
    {
        assertFalse(index.tableExists(userId, tableId));
    }

    /**
     * Asserts that the given TableIndex entity has the correct values for each
     * of its fields.
     */
    public void assertEntityFields(String tableId, String tableName,
            List<DataField> tableFields, Entity entity)
    {
        assertEquals(tableId, entity.getField(TableIndex.TABLE_ID));
        assertEquals(tableName, entity.getField(TableIndex.TABLE_NAME));
        assertEquals(TableIndex.serializeFields(tableFields),
                entity.getField(TableIndex.TABLE_FIELDS));
    }

    /**
     * Asserts that the given Table has the given expectedFields.
     */
    public void assertDataFields(Table table, List<DataField> expectedFields)
    {
        for (DataField field : expectedFields)
        {
            assertEquals(field.getName(), table.getDataField(field.getName())
                    .getName());
            assertEquals(field.getDataType(),
                    table.getDataField(field.getName()).getDataType());
            assertEquals(field.getNullable(),
                    table.getDataField(field.getName()).getNullable());
        }
    }
}
