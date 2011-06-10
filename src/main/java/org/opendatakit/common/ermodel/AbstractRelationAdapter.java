package org.opendatakit.common.ermodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * TODO: Mitch, take a look at these and see if you think.
 */
public abstract class AbstractRelationAdapter extends AbstractRelation
{

    // Bring down constructors of AbstractRelation

    protected AbstractRelationAdapter(String tableName, List<DataField> fields,
            CallingContext cc) throws ODKDatastoreException
    {
        super(tableName, fields, cc);
    }

    protected AbstractRelationAdapter(String namespace, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, tableName, fields, cc);
    }

    protected AbstractRelationAdapter(TableNamespace type, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        super(type, tableName, fields, cc);
    }

    // New methods

    /**
     * Returns a list of Entity objects where 'fieldName op value' is true.
     */
    public List<Entity> getEntities(String fieldName, FilterOperation op,
            Object value, CallingContext cc) throws ODKDatastoreException
    {
        return super.getEntities(getDataField(fieldName), op, value, cc);
    }

    /**
     * Returns all Entity objects e such that:
     * valueSet.contains(e.getField(fieldName)) is true
     */
    public List<Entity> getEntities(String fieldName, Collection<?> valueSet,
            CallingContext cc) throws ODKDatastoreException
    {
        DataField dataField = getDataField(fieldName);

        if (!prototype.getFieldList().contains(dataField))
        {
            throw new IllegalArgumentException("Unrecognized data field: "
                    + dataField.getName());
        }

        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();

        Query q = ds.createQuery(prototype, user);
        q.addValueSetFilter(dataField, valueSet);

        List<? extends CommonFieldsBase> list = q.executeQuery(0);
        List<Entity> eList = new ArrayList<Entity>();
        for (CommonFieldsBase b : list)
        {
            eList.add(new EntityImpl((RelationImpl) b));
        }

        return eList;
    }

    /**
     * Returns a list of entity objects corresponding to the collection of uris.
     */
    public List<Entity> getEntities(Collection<String> uris, CallingContext cc)
            throws ODKEntityNotFoundException
    {
        List<Entity> entities = new ArrayList<Entity>();

        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();

        for (String uri : uris)
        {
            Entity entity = new EntityImpl(ds.getEntity(prototype, uri, user));
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Returns a list of all entity objects for this relation.
     */
    public List<Entity> getAllEntities(CallingContext cc)
            throws ODKDatastoreException
    {
        Query q = createQuery(cc);
        List<Entity> entities = executeQuery(q);
        return entities;
    }

    /**
     * Returns true if this relation contains an entity with the given uri.
     */
    public boolean containsEntity(String uri, CallingContext cc)
    {
        try
        {
            getEntity(uri, cc);
        } catch (ODKEntityNotFoundException e)
        {
            return false;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Create a new query for this relation.
     */
    public Query createQuery(CallingContext cc)
    {
        Datastore ds = cc.getDatastore();
        User user = cc.getCurrentUser();

        Query q = ds.createQuery(prototype, user);

        return q;
    }

    /**
     * Execute the query which was created with {@link #createQuery}.
     */
    public List<Entity> executeQuery(Query q) throws ODKDatastoreException
    {
        List<? extends CommonFieldsBase> list = q.executeQuery(0);
        List<Entity> eList = new ArrayList<Entity>();
        for (CommonFieldsBase b : list)
        {
            eList.add(new EntityImpl((RelationImpl) b));
        }

        return eList;
    }
}
