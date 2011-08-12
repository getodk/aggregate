package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Users is a relation containing all the {@link InternalUser} entities stored
 * in the datastore. Thus Users keeps track of all the registered users of the
 * odktables API.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Users extends TypedEntityRelation<InternalUser>
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

    // The following defines the actual attributes that will be in the datastore:
    /**
     * The field for the user id.
     */
    private static final Attribute userID = new Attribute(USER_ID,
            AttributeType.STRING, false);
    /**
     * The field for the user name.
     */
    private static final Attribute userName = new Attribute(USER_NAME,
            AttributeType.STRING, false);

    private static final List<Attribute> attributes;
    static
    {
        attributes = new ArrayList<Attribute>();
        attributes.add(userID);
        attributes.add(userName);
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
        super(Table.NAMESPACE, RELATION_NAME, attributes, cc);
    }

    public InternalUser initialize(Entity entity) throws ODKDatastoreException
    {
        return InternalUser.fromEntity(entity, getCC());
    }

    public InternalUser getByID(String userID) throws ODKDatastoreException
    {
        return query().equal(USER_ID, userID).get();
    }

    public String getAggregateIdentifier()
    {
        return RELATION_NAME;
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
