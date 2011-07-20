package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalUserTableMapping;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
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

    // The following defines the actual fields that will be in the datastore:
    /**
     * The aggregateUserIdentifier field.
     */
    private static final DataField aggregateUserIdentifier = new DataField(
            AGGREGATE_USER_IDENTIFIER, DataType.URI, false);
    /**
     * The aggregateTableIdentifier field.
     */
    private static final DataField aggregateTableIdentifier = new DataField(
            AGGREGATE_TABLE_IDENTIFIER, DataType.URI, false);
    /**
     * The tableID field.
     */
    private static final DataField tableID = new DataField(TABLE_ID,
            DataType.STRING, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(aggregateUserIdentifier);
        fields.add(aggregateTableIdentifier);
        fields.add(tableID);
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
        super(Table.NAMESPACE, RELATION_NAME, fields, cc);
    }

    @Override
    public InternalUserTableMapping initialize(Entity entity)
            throws ODKDatastoreException
    {
        return new InternalUserTableMapping(entity, getCC());
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
