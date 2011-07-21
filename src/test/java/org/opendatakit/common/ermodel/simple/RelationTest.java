package org.opendatakit.common.ermodel.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

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
    public void testGetAttributes() throws ODKDatastoreException
    {
        List<Attribute> actualAttributes = d.relation.getAttributes();
        Comparator<Attribute> attrComparator = new Comparator<Attribute>()
        {
            public int compare(Attribute o1, Attribute o2)
            {
                return o1.getName().compareTo(o2.getName());
            }

        };
        Collections.sort(d.attributes, attrComparator);
        Collections.sort(actualAttributes, attrComparator);
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
