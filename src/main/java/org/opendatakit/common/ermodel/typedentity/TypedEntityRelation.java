package org.opendatakit.common.ermodel.typedentity;

import java.util.List;

import org.opendatakit.common.ermodel.ExtendedAbstractRelation;
import org.opendatakit.common.ermodel.Entity;
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
 * org.opendatakit.aggregate.odktables.entities pacakges for an example.
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
 * Person newPerson = new Person("John Smith", cc);
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

    protected TypedEntityRelation(String tableName, List<DataField> fields,
            CallingContext cc) throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(tableName, fields, cc);
    }

    protected TypedEntityRelation(String namespace, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(namespace, tableName,
                fields, cc);
    }

    protected TypedEntityRelation(TableNamespace type, String tableName,
            List<DataField> fields, CallingContext cc)
            throws ODKDatastoreException
    {
        this.relation = new ExtendedAbstractRelation(type, tableName, fields,
                cc);
    }

    public abstract T initialize(Entity entity) throws ODKDatastoreException;

    public T get(String uri) throws ODKDatastoreException
    {
        return initialize(this.relation.getEntity(uri, getCC()));
    }

    public TypedEntityQuery<T> query()
    {
        return new TypedEntityQuery<T>(this);
    }

    protected Entity newEntity(CallingContext cc)
    {
        return relation.newEntity(cc);
    }

    protected DataField getDataField(String fieldName)
    {
        return relation.getDataField(fieldName);
    }

    protected CallingContext getCC()
    {
        return relation.getCC();
    }

    protected Query createQuery()
    {
        return relation.createQuery();
    }

    protected List<Entity> executeQuery(Query query, int limit)
    {
        return relation.executeQuery(query, limit);
    }
}
