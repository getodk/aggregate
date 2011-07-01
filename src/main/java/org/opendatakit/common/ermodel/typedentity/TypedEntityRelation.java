package org.opendatakit.common.ermodel.typedentity;

import java.util.List;

import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.ermodel.TableNamespace;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Extends the concept of Relation to allow dealing with typed entities. For
 * example, querying a relation called <i>People</i> will no longer return a
 * list of plain Entity objects but will return a list of Person objects. This
 * is more work, but it makes it easier for the clients using your relation.
 * </p>
 * 
 * <p>
 * TypedEntityRelation is immutable as long as subclasses follow the patterns
 * described below and do not expose their fields or relation name.
 * </p>
 * 
 * <p>
 * <b>Defining a TypedEntityRelation:</b> Changing a normal relation into a
 * TypedEntityRelation is simple. Building off of the <i>People</i> example in
 * {@link ExtendedAbstractRelation}, you would make these changes:
 * 
 * <pre>
 *  class People extends TypedEntityRelation&lt;Person&gt;
 *  {
 *      ... existing code ...
 *      
 *      public Person initialize(Entity entity)
 *      {
 *          return new Person(entity, getCC());
 *      }
 *      
 *  }
 *  
 *  class Person extends TypedEntity
 *  {
 *      public Person(String name, CallingContext cc)
 *      {
 *          super(People.getInstance(cc));
 *          setName(name);
 *      }
 *      
 *      public Person(Entity entity, CallingContext cc)
 *      {
 *          super(People.getInstance(cc), entity);
 *      }
 *      
 *      public String getName()
 *      {
 *          return super.getEntity().getField(People.NAME);
 *      }
 *      
 *      public void setName(String name)
 *      {
 *          super.getEntity().setField(People.NAME, name); 
 *      }
 *  }
 * </pre>
 * 
 * <p>
 * One advantage of doing things this way is that you get nicer semantics for
 * querying and dealing with your relations and entities. Other advantages that
 * are not as obvious from this example are that (1) you can define any
 * arbitrary methods on your entities in a clean way, and (2) when you have more
 * than one relation it makes it easier to define relationships between your
 * relations. See the org.opendatakit.aggregate.odktables.relation and
 * org.opendatakit.aggregate.odktables.entities packages for an example.
 * </p>
 * 
 * <p>
 * Now, the same example usage as in {@link ExtendedAbstractRelation}, but
 * better!
 * </p>
 * 
 * <pre>
 * CallingContext cc;
 * People people = People.getInstance(cc);
 * 
 * // Create John Smith
 * Person newPerson = new Person(&quot;John Smith&quot;, cc);
 * newPerson.save();
 * 
 * // Retrieve John Smith and change name
 * Person john = people.query().equal(People.NAME, &quot;John Smith&quot;).get();
 * john.setName(&quot;Jane Smith&quot;);
 * john.save();
 * </pre>
 * 
 * <pre>
 * 
 * 
 * @author the.dylan.price@gmail.com
 * 
 * @param <T>
 * the type of entity which will be stored in this relation.
 */
public abstract class TypedEntityRelation<T extends TypedEntity>
{

    private ExtendedAbstractRelation relation;

    /**
     * {@link ExtendedAbstractRelation#ExtendedAbstractRelation(String, List, CallingContext)}
     */
    protected TypedEntityRelation(String tableName, List<DataField> fields,
            CallingContext cc) throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(tableName, fields, cc);
    }

    /**
     * {@link ExtendedAbstractRelation#ExtendedAbstractRelation(String, String, List, CallingContext)}
     */
    protected TypedEntityRelation(String namespace, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(namespace, tableName,
                fields, cc);
    }

    /**
     * {@link ExtendedAbstractRelation#ExtendedAbstractRelation(TableNamespace, String, List, CallingContext)}
     */
    protected TypedEntityRelation(TableNamespace type, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(type, tableName, fields,
                cc);
    }

    /**
     * Initializes a typed entity using the given generic entity. You probably
     * will not need to call this.
     * 
     * @param entity
     *            a generic entity from this relation
     * @return a typed entity based on the given generic entity
     * @throws ODKDatastoreException
     */
    public abstract T initialize(Entity entity) throws ODKDatastoreException;

    /**
     * Retrieve a typed entity by uri.
     * 
     * @param uri
     *            the global unique identifier of an entity in this relation.
     *            Must be a valid uri and an entity with the uri must exist in
     *            this relation.
     * @return the entity with the given uri
     * @throws ODKDatastoreException
     * @throws ODKEntityNotFoundException
     *             if no such entity exists in the datastore
     */
    public T get(String uri) throws ODKDatastoreException
    {
        return initialize(this.relation.getEntity(uri, getCC()));
    }

    /**
     * Creates a new empty query.
     * 
     * @return a new empty query over this relation, that is a query with no
     *         filter or sort criteria.
     */
    public TypedEntityQuery<T> query()
    {
        return new TypedEntityQuery<T>(this);
    }

    /**
     * {@link org.opendatakit.common.ermodel.Relation#newEntity(CallingContext)}
     */
    protected Entity newEntity(CallingContext cc)
    {
        return relation.newEntity(cc);
    }

    /**
     * {@link org.opendatakit.common.ermodel.Relation#getDataField(String)}
     */
    protected DataField getDataField(String fieldName)
    {
        return relation.getDataField(fieldName);
    }

    /**
     * @return the CallingContext this relation was retrieved with.
     */
    protected CallingContext getCC()
    {
        return relation.getCC();
    }

    /**
     * {@link org.opendatakit.common.ermodel.ExtendedAbstractRelation#createQuery()}
     */
    protected Query createQuery()
    {
        return relation.createQuery();
    }

    /**
     * {@link org.opendatakit.common.ermodel.ExtendedAbstractRelation#executeQuery(Query, int)}
     */
    protected List<Entity> executeQuery(Query query, int limit)
    {
        return relation.executeQuery(query, limit);
    }
}
