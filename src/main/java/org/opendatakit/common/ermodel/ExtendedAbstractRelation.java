package org.opendatakit.common.ermodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Additional methods for AbstractRelation. To take advantage of the additional
 * functionality, subclass this when you create your relation. See below for the
 * suggested pattern in defining a relation.
 * </p>
 * 
 * <p>
 * <b>Additions to AbstractRelation</b>: Keeps track of an instance of the
 * CallingContext so subclasses do not need to require it in all methods, and
 * adds a public getter for the CallingContext. Adds {@link #createQuery} and
 * {@link #executeQuery} for query functionality. If you need to query a large
 * set of data, please add QueryResult support.
 * </p>
 * 
 * <p>
 * <b>Defining a relation:</b> A relation has a name and a set of DataFields
 * which define the entities that it will contain. The common pattern for
 * defining a relation is best explained with an example. Let's say we are
 * defining a relation called <i>People</i> whose entities have only one field,
 * <i>name</i>. <i>name</i> is of type DataField.DataType.STRING and is not
 * allowed to be null. The class definition of <i>People</i> would look like
 * this:
 * 
 * <pre>
 *  class People extends ExtendedAbstractRelation
 *  {
 *      public static final String NAME = "NAME"
 *      
 *      private static final String RELATION_NAME = "PEOPLE";
 *      
 *      private static final DataField name = new DataField(NAME, DataField.DataType.STRING, false);
 *      private static final List&lt;DataField&gt; fields;
 *      static
 *      {
 *          fields = new ArrayList&lt;DataField&gt;();
 *          fields.add(name);
 *      }
 *      
 *      private static People instance;
 *      
 *      private People(CallingContext cc) throws ODKDatastoreException
 *      {
 *          super(RELATION_NAME, fields, cc); 
 *      }
 *      
 *      public static People getInstance(CallingContext cc)
 *      {
 *          if (instance == null || instance.getCC() != cc)
 *          {
 *              instance = new People(cc);
 *          }
 *          return instance;
 *      }
 *  }
 * </pre>
 * 
 * To use People,
 * 
 * <pre>
 * CallingContext cc;
 * People people = People.getInstance(cc);
 * 
 * // Create John Smith
 * Entity newPerson = people.newEntity(cc)
 * newPerson.setField(People.NAME, "John Smith");
 * newPerson.persist(cc);
 * 
 * // Retrieve John Smith and change name
 * Query query = people.createQuery();
 * query.addFilter(people.getDataField(People.NAME), FilterOperation.EQUAL,
 *         &quot;John Smith&quot;);
 * List&lt;Entity&gt; results = people.executeQuery(query);
 * Entity john = results.get(0);
 * john.setField(People.NAME, &quot;Jane Smith&quot;);
 * john.persist(cc);
 * </pre>
 * 
 * @author the.dylan.price@gmail.com
 */
public class ExtendedAbstractRelation extends AbstractRelation {

  /**
   * The CallingContext of the Aggregate instance.
   */
  private CallingContext cc;

  /**
   * {@link AbstractRelation#AbstractRelation(String, List, CallingContext)}
   */
  public ExtendedAbstractRelation(String tableName, List<DataField> fields, CallingContext cc)
      throws ODKDatastoreException {
    super(tableName, fields, cc);
    this.cc = cc;
  }

  /**
   * {@link AbstractRelation#AbstractRelation(String, String, List, CallingContext)}
   */
  public ExtendedAbstractRelation(String namespace, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(namespace, tableName, fields, cc);
    this.cc = cc;
  }

  /**
   * {@link AbstractRelation#AbstractRelation(TableNamespace, String, List, CallingContext)}
   */
  public ExtendedAbstractRelation(TableNamespace type, String tableName, List<DataField> fields,
      CallingContext cc) throws ODKDatastoreException {
    super(type, tableName, fields, cc);
    this.cc = cc;
  }

  /**
   * @return the CallingContext saved in the instance of this relation.
   */
  public CallingContext getCC() {
    return this.cc;
  }

  /**
   * Creates an empty query which can be used to query this relation.
   * 
   * @return an empty Query.
   */
  public Query createQuery(String loggingContextTag) {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    Query emptyQuery = ds.createQuery(super.prototype, loggingContextTag, user);
    return emptyQuery;
  }

  /**
   * Executes the given query which was most likely created with
   * {@link #createQuery()}. Prefer this to {@link Query#executeQuery()} because
   * that method does not return a list of Entity objects.
   * 
   * @param query
   *          the Query to execute
   * @return a list of all the entities which are the results of the query
   */
  public List<Entity> executeQuery(Query query) {
    try {
      List<? extends CommonFieldsBase> list = query.executeQuery();
      List<Entity> entities = new ArrayList<Entity>();
      for (CommonFieldsBase b : list) {
        entities.add(new EntityImpl((RelationImpl) b));
      }
      return entities;
    } catch (ODKDatastoreException e) {
      return Collections.emptyList();
    }
  }
}