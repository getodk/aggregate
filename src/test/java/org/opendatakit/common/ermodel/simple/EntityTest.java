package org.opendatakit.common.ermodel.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

/**
 * Test for Entity
 * 
 * @author the.dylan.price@gmail.com
 *
 */
public class EntityTest
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
    public void testGetEntity() throws ODKEntityNotFoundException,
            ODKEntityPersistException
    {
        Entity john = d.relation.getEntity(d.johnsIdentifier);
        assertEquals(d.johnsName, john.getString(d.attrName));
        assertEquals(d.johnsAge, john.getInteger(d.attrAge).intValue());
    }

    @Test
    public void testSet() throws ODKEntityNotFoundException,
            ODKEntityPersistException
    {
        Entity john = d.relation.getEntity(d.johnsIdentifier);
        john.set(d.attrName, "John2");
        john.set(d.attrAge, 100);
        john.save();
        john = null;
        john = d.relation.getEntity(d.johnsIdentifier);
        assertEquals("John2", john.getString(d.attrName));
        assertEquals(100, john.getInteger(d.attrAge).intValue());
    }

    @Test
    public void testSetAsString() throws ODKEntityNotFoundException,
            ODKEntityPersistException
    {
        Entity john = d.relation.getEntity(d.johnsIdentifier);
        john.setAsString(d.attrAge, "100");
        john.save();
        john = null;
        john = d.relation.getEntity(d.johnsIdentifier);
        assertEquals(100, john.getInteger(d.attrAge).intValue());
    }

    @Test
    public void testDeleteEntity() throws ODKDatastoreException
    {
        Entity john = d.relation.getEntity(d.johnsIdentifier);
        john.delete();
        try
        {
            d.relation.getEntity(d.johnsIdentifier);
            fail("Should not be able to retrieve deleted entity");
        } catch (ODKEntityNotFoundException e)
        {
        }
    }

    @Test
    public void testSave() throws ODKEntityNotFoundException,
            ODKEntityPersistException
    {
        Entity john = d.relation.getEntity(d.johnsIdentifier);
        john.set(d.attrAge, 60);
        john.save();

        john = null;
        john = d.relation.getEntity(d.johnsIdentifier);
        assertEquals(d.johnsName, john.getString(d.attrName));
        assertEquals(60, john.getInteger(d.attrAge).intValue());
    }
}
