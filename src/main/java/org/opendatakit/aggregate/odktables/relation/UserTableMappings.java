package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.Attribute;
import org.opendatakit.common.persistence.Attribute.Attribute;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Cursors is a relation containing all the {@link InternalUserTableMapping} entities in
 * the datastore. Cursors defines a mapping of users to the tables they are
 * synchronized with and tracks their personal identifiers for those tables.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class UserTableMappings extends TypedEntityRelation<InternalUserTableMapping>
{
    // Field names
    /**
     * The name of the aggregateUserIdentifier field.
     */
    public static final String AGGREGATE_USER_IDENTIFIER = "AGGREGATE_USER_IDENTIFIER";

    /**
     * The name of the aggregateTableIdentifier field.
     */
    public static final String AGGREGATE_TABLE_IDENTIFIER = "AGGREGATE_TABLE_IDENTIFIER";

    /**
     * The name of the tableID field.
     */
    public static final String TABLE_ID = "TABLE_ID";

    // Relation name:
    /**
     * The name of the Cursor relation.
     */
    private static final String RELATION_NAME = "USER_TABLE_MAPPINGS";

    // The following defines the actual attributes that will be in the datastore:
    /**
     * The aggregateUserIdentifier field.
     */
    private static final Attribute aggregateUserIdentifier = new Attribute(
            AGGREGATE_USER_IDENTIFIER, Attribute.URI, false);
    /**
     * The aggregateTableIdentifier field.
     */
    private static final Attribute aggregateTableIdentifier = new Attribute(
            AGGREGATE_TABLE_IDENTIFIER, Attribute.URI, false);
    /**
     * The tableID field.
     */
    private static final Attribute tableID = new Attribute(TABLE_ID,
            Attribute.STRING, false);

    private static final List<Attribute> attributes;
    static
    {
        attributes = new ArrayList<Attribute>();
        attributes.add(aggregateUserIdentifier);
        attributes.add(aggregateTableIdentifier);
        attributes.add(tableID);
    }
    /**
     * The singleton instance of the Cursor relation.
     */
    private static UserTableMappings instance;

    /**
     * Constructs an instance which can be used to manipulate the Cursor
     * relation. If the Cursor relation does not already exist in the datastore
     * it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private UserTableMappings(CallingContext cc) throws ODKDatastoreException
    {
        super(Table.NAMESPACE, RELATION_NAME, attributes, cc);
    }

    @Override
    public InternalUserTableMapping initialize(Entity entity)
            throws ODKDatastoreException
    {
        return InternalUserTableMapping.fromEntity(entity);
    }

    /**
     * Returns the singleton instance of the Cursor relation.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Cursor relation. If the instance
     *         does not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static UserTableMappings getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || instance.getCC() != cc)
        {
            instance = new UserTableMappings(cc);
        }
        return instance;
    }
}
