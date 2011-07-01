package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Users is a relation containing all the {@link User} entities stored in the
 * datastore. Thus Users keeps track of all the registered users of the
 * odktables API.
 * </p>
 * 
 * <p>
 * Users automatically starts out with one user--the anonymous user. You can
 * retrieve this user through the {@link #getAnonymousUser} method.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Users extends TypedEntityRelation<User>
{
    // Field names
    /**
     * The name of the userID field.
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

    /**
     * The ID of the anonymous user.
     */
    private static final String ANON_ID = "-1";

    /**
     * The name of the anonymous user.
     */
    private static final String ANON_NAME = "Anonymous User";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The field for the user id.
     */
    private static final DataField userID = new DataField(USER_ID,
            DataType.STRING, false);
    /**
     * The field for the user name.
     */
    private static final DataField userName = new DataField(USER_NAME,
            DataType.STRING, false);

    private static final List<DataField> fields;
    static
    {
        userID.setIndexable(IndexType.HASH);

        fields = new ArrayList<DataField>();
        fields.add(userID);
        fields.add(userName);
    }

    /**
     * The singleton instance of the Users.
     */
    private static Users instance;

    /**
     * The singleton instance of the anonymous user.
     */
    private static User anonInstance;

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

    public User getAnonymousUser() throws ODKDatastoreException
    {
        return Users.anonInstance;
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
        // Create anonymous user if they don't already exist
        if (anonInstance == null)
        {
            try
            {
                anonInstance = instance.query().equal(USER_ID, ANON_ID).get();
            } catch (ODKDatastoreException e)
            {
                anonInstance = new User(ANON_ID, ANON_NAME, cc);
                anonInstance.save();
            }
        }
        return instance;
    }
}
