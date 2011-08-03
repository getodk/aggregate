package org.opendatakit.aggregate.odktables.relation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.TestUtils;
import org.opendatakit.aggregate.odktables.relation.Table;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

/**
 * Test case for Table. Table does not really have any functionality to test so
 * this is mostly a sanity check against AbstractRelation.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class TableTest
{
    private static final String NAMESPACE = "TEST";
    private static final String RELATION_NAME = "TABLE1";
    private static final String COLUMN_1 = "COL1";
    private static final String VALUE_1 = "value1";
    private static final CallingContext cc = TestContextFactory
            .getCallingContext();
    private static final List<DataField> tableFields;
    static
    {
        tableFields = new ArrayList<DataField>();
        tableFields.add(new DataField(COLUMN_1, DataField.DataType.STRING,
                false));
    }

    private static String uri;

    @Before
    public void setUp() throws ODKDatastoreException
    {
        // Create relation
        Table relation = new Table(NAMESPACE, RELATION_NAME, tableFields, cc);

        // Create and persist an entity
        uri = NAMESPACE + ":1";
        Entity entity = relation.newEntity(uri, cc);
        entity.setField(COLUMN_1, VALUE_1);
        entity.persist(cc);
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        Table relation = new Table(NAMESPACE, RELATION_NAME, tableFields, cc);
        relation.dropRelation(cc);
    }

    @Test
    public void testRetrieveEntity() throws ODKDatastoreException
    {
        // Retrieve relation
        Table relation = new Table(NAMESPACE, RELATION_NAME, tableFields, cc);
        Entity entity = relation.getEntity(uri, cc);
        assertEquals(VALUE_1, entity.getField(COLUMN_1));
    }

    @Test
    public void testQueryEntity() throws ODKDatastoreException
    {
        // Retrieve relation
        Table relation = new Table(NAMESPACE, RELATION_NAME, tableFields, cc);

        DataField col1 = relation.getDataField(COLUMN_1);

        // Query entity
        List<Entity> entities = relation.getEntities(col1,
                FilterOperation.EQUAL, VALUE_1, cc);
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(VALUE_1, entity.getField(COLUMN_1));
    }

    @Test
    public void testGetDataFields() throws ODKDatastoreException
    {
        Table relation = new Table(NAMESPACE, RELATION_NAME, tableFields, cc);
        TestUtils.assertFieldListsAreEqual(tableFields,
                relation.getDataFields());
    }
}
