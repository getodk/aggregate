package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entities.User;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Users represents all the odktables API users who own tables in the Aggregate
 * datastore.
 * </p>
 * 
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
 * @author the.dylan.price@gmail.com
 */
public class Users extends TypedEntityRelation<User>
{
    // Field names
    /**
     * The name of the userId field.
     */
    public static final String USER_ID = "USER_ID";

    /**
     * The name of the userName field.
     */
    public static final String USER_NAME = "USER_NAME";

    // Relation name
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
    }

    public User initialize(Entity entity) throws ODKDatastoreException
    {
        return new User(entity, super.getCC());
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
        if (instance == null || instance.getCC() != cc)
        {
            instance = new Users(cc);
        }
        return instance;
    }
}
