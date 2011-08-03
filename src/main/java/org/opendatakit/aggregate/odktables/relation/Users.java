package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Users is a set of (userUri, userId, userName) tuples, aka 'entities' where
 * <ul>
 * <li>userUri = the public unique identifier of the user</li>
 * <li>userId = the private unique identifier of the user, known only to the
 * user</li>
 * <li>userName = the human readable name of the user</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Thus Users represents all the odktables users who own tables in the Aggregate
 * datastore.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Users extends AbstractRelationAdapter
{
    // Field names:
    /**
     * The name of the user id field.
     */
    public static final String USER_ID = "USER_ID";
    /**
     * The name of the user name field.
     */
    public static final String USER_NAME = "USER_NAME";

    // Relation name and User namespace:
    /**
     * The name of the Users relation.
     */
    private static final String RELATION_NAME = "USERS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The field for the user id.
     */
    private static final DataField userId = new DataField(USER_ID,
            DataType.STRING, false);
    /**
     * The field for the user name.
     */
    private static final DataField userName = new DataField(USER_NAME,
            DataType.STRING, false);

    private static final List<DataField> fields;
    static
    {
        userId.setIndexable(IndexType.HASH);

        fields = new ArrayList<DataField>();
        fields.add(userId);
        fields.add(userName);
    }

    /**
     * The singleton instance of the Users.
     */
    private static Users instance;
    /**
     * The CallingContext of the Aggregate instance.
     */
    private static CallingContext cc;

    /**
     * Constructs an instance which can be used to manipulate the Users
     * relation. If the Users relation does not already exist in the datastore
     * it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Users(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
        Users.cc = cc;
    }

    /**
     * Creates a User with the given userId and userName.
     * 
     * @param userId
     *            the unique identifier for the user. It must consist of only
     *            uppercase letters, numbers, and underscores and must start
     *            with an uppercase letter. There must not be another user with
     *            this userId. Must be non-null and non-empty.
     * @param userName
     *            the name for the User. The userName does not have to be
     *            unique. Must be non-null and non-empty.
     * @throws ODKDatastoreException
     *             if there was a problem communicating with the datastore
     * @throws IllegalArgumentException
     *             if any arguments are null or empty of if the userId is badly
     *             formed
     * @throws RuntimeException
     *             if a User with the given userId already exists
     */
    public void createUser(String userId, String userName)
            throws ODKDatastoreException
    {
        if (userId == null || userId.isEmpty() || userName == null
                || userName.isEmpty())
        {
            throw new IllegalArgumentException(
                    "received null or empty argument");
        }
        if (!userId.matches(Relation.VALID_UPPER_CASE_NAME_REGEX))
        {
            throw new IllegalArgumentException("badly formed userId '" + userId
                    + "'. Check that it consists of only uppercase "
                    + "letters, numbers, and underscores and that "
                    + "it starts with an uppercase letter.");
        }

        Entity user = retrieveEntity(userId);
        if (user != null)
        {
            throw new RuntimeException("User with userId: '" + userId
                    + "' already exists!");
        }

        // Add user to index
        Entity entry = newEntity(cc);
        entry.setField(USER_ID, userId);
        entry.setField(USER_NAME, userName);
        entry.persist(cc);
    }

    /**
     * Retrieves the user entity of the user with the given userId.
     * 
     * @param userId
     *            the unique identifier of the user. Must be non-null and
     *            non-empty.
     * @return the entity with the given userId
     * @throws ODKDatastoreException
     */
    public Entity getEntity(String userId) throws ODKDatastoreException
    {
        Entity user = retrieveEntity(userId);
        if (user == null)
            throw new IllegalArgumentException("No user with userId '" + userId
                    + "' exists!");
        return user;
    }

    /**
     * @param userId
     *            the unique identifier of the user to test for. Must be
     *            non-null and non-empty.
     * @return true if a User with the given userId exists in the datastore,
     *         false otherwise.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public boolean userExists(String userId) throws ODKDatastoreException
    {
        return retrieveEntity(userId) != null;
    }

    /**
     * Deletes the User with the given userId. This user must previously have
     * been created through {@link #createUser}.
     * 
     * @param userId
     *            the unique identifier of the user to delete. Must be non-null
     *            and non-empty. If no user with this userId exists, then
     *            calling this method does nothing.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public void deleteUser(String userId) throws ODKDatastoreException
    {
        Entity user = retrieveEntity(userId);
        if (user == null)
            return;
        user.remove(cc);
    }

    /**
     * Retrieves the entity with the given userId from the datastore.
     * 
     * @param userId
     *            the value of the userId field of the desired Entity.
     * @return the Entity with the given userId, or null if no such Entity
     *         exists.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore.
     */
    private Entity retrieveEntity(String userId) throws ODKDatastoreException
    {
        if (userId == null || userId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "received null or empty argument");
        }

        List<Entity> entities = getEntities(USER_ID, FilterOperation.EQUAL,
                userId, cc);
        if (entities == null || entities.isEmpty())
        {
            return null;
        }
        if (entities.size() > 1)
        {
            throw new RuntimeException(
                    "More than one entry for the given userId '" + userId + "'");
        }

        Entity entity = entities.get(0);
        return entity;
    }

    /**
     * Returns the singleton instance of the Users.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Users. If the instance does not
     *         exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Users getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || Users.cc != cc)
        {
            instance = new Users(cc);
        }
        return instance;
    }
}
