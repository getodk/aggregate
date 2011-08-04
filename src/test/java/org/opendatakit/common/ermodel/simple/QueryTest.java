package org.opendatakit.common.ermodel.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Test for Query.
 * 
 * @author the.dylan.price@gmail.com
 *
 */
public class QueryTest
{
    private Data d;

    @Before
    public void setUp() throws ODKDatastoreException
    {
        d = new Data();
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        try
        {
            d.relation.dropRelation();
        } catch (ODKDatastoreException e)
        {
        }
    }

    @Test
    public void testGet() throws ODKDatastoreException
    {
        Entity john = d.relation.query().equal(d.attrName, d.johnsName).get();
        assertEquals(d.johnsName, john.getString(d.attrName));
        assertEquals(d.johnsAge, john.getInteger(d.attrAge).intValue());
    }

    @Test
    public void testExecute() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().execute();
        assertEquals(2, people.size());
    }

    // TODO: this fails but I think it's a problem in underlying query implementation
    @Ignore
    public void testExecuteWithLimit() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().execute(1);
        assertEquals(1, people.size());
    }

    @Test
    public void testInclude() throws ODKDatastoreException
    {
        List<String> names = Arrays.asList(new String[] { d.johnsName });
        List<Entity> people = d.relation.query().include(d.attrName, names)
                .execute();
        assertEquals(1, people.size());
        Entity john = people.get(0);
        assertEquals(d.johnsName, john.getString(d.attrName));
        assertEquals(d.johnsAge, john.getInteger(d.attrAge).intValue());
    }

    @Test
    public void testGetDistinct()
    {
        // Make sure our data is right
        assertEquals(d.johnsAge, d.joesAge);
        // Test getDistinct
        @SuppressWarnings("unchecked")
        List<Integer> ages = (List<Integer>) d.relation.query().getDistinct(
                d.attrAge);
        assertEquals(1, ages.size());
    }

    @Test
    public void testGreaterThan() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().greaterThan("AGE", 50)
                .execute();
        assertTrue(people.isEmpty());
    }

    @Test
    public void testGreaterThanOrEqual() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().greaterThanOrEqual("AGE", 50)
                .execute();
        assertEquals(2, people.size());
    }

    @Test
    public void testLessThan() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().lessThan("AGE", 50).execute();
        assertTrue(people.isEmpty());
    }

    @Test
    public void testLessThanOrEqual() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().lessThanOrEqual("AGE", 50)
                .execute();
        assertEquals(2, people.size());
    }

    @Test
    public void testSortAscending() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().sortAscending(d.attrName)
                .execute();
        Entity joe = people.get(0);
        Entity john = people.get(1);
        assertEquals(d.joesName, joe.getString(d.attrName));
        assertEquals(d.johnsName, john.getString(d.attrName));
    }

    @Test
    public void testSortDescending() throws ODKDatastoreException
    {
        List<Entity> people = d.relation.query().sortDescending(d.attrName)
                .execute();
        Entity john = people.get(0);
        Entity joe = people.get(1);
        assertEquals(d.joesName, joe.getString(d.attrName));
        assertEquals(d.johnsName, john.getString(d.attrName));
    }
}
