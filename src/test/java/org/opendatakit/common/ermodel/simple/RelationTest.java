package org.opendatakit.common.ermodel.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Test for Relation
 * 
 * @author the.dylan.price@gmail.com
 *
 */
public class RelationTest
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
    public void testCreateRelation() throws ODKDatastoreException
    {
        tearDown();
        d.relation = new Relation(d.namespace, d.personRelationName,
                d.attributes, d.cc);
    }

    @Test
    public void testCreateRelationAlreadyExists() throws ODKDatastoreException
    {
        d.relation = new Relation(d.namespace, d.personRelationName,
                d.attributes, d.cc);
        assertEquals(2, d.relation.getAttributes().size());
    }

    @Test
    public void testGetRelation() throws ODKDatastoreException
    {
        Relation.getRelation(d.namespace, d.personRelationName, d.cc);
    }

    @Test
    public void testDropRelation() throws ODKDatastoreException
    {
        d.relation.dropRelation();
        try
        {
            Relation.getRelation(d.namespace, d.personRelationName, d.cc);
            fail("Should not be able to get deleted relation.");
        } catch (ODKDatastoreException e)
        {

        }
    }

    @Test
    public void testCreateEntity() throws ODKDatastoreException
    {
        d.relation.newEntity();
    }

    @Test
    public void testGetAttribute()
    {
        Attribute name = d.relation.getAttribute(d.attrName);
        Attribute age = d.relation.getAttribute(d.attrAge);
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(name);
        attributes.add(age);
        Collections.sort(attributes, d.attrComparator);
        Collections.sort(d.attributes, d.attrComparator);

    }

    @Test
    public void testGetAttributes() throws ODKDatastoreException
    {
        List<Attribute> actualAttributes = d.relation.getAttributes();
        Collections.sort(d.attributes, d.attrComparator);
        Collections.sort(actualAttributes, d.attrComparator);
        assertEquals(d.attributes, actualAttributes);
    }

    @Test
    public void testNewEntity()
    {
        d.relation.newEntity();
    }

    @Test
    public void testQuery()
    {
        d.relation.query();
    }
}
