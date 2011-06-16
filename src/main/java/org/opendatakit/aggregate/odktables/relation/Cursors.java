package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entities.Cursor;
import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Cursor defines a mapping of users to the tables they are synchronized with
 * and tracks their personal identifiers for those tables.
 * </p>
 * 
 * <p>
 * Cursor is a set of (userUri, tableUri, tableId) tuples, aka 'entities' where
 * <ul>
 * <li>userUri: the globally unique identifier of a user who is using the
 * synchronized features for the table with tableUri</li>
 * <li>tableUri: the globally unique identifier of a table</li>
 * <li>tableId: the identifier which the user with userUri uses to identify the
 * table with tableUri</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Cursors extends TypedEntityRelation<Cursor>
{
    // Field names
    /**
     * The name of the userUri field.
     */
    public static final String USER_URI = "USER_URI";

    /**
     * The name of the tableUri field.
     */
    public static final String TABLE_URI = "TABLE_URI";

    /**
     * The name of the tableId field.
     */
    public static final String TABLE_ID = "TABLE_ID";

    // Relation name:
    /**
     * The name of the Cursor relation.
     */
    private static final String RELATION_NAME = "CURSOR";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The userUri field.
     */
    private static final DataField userUri = new DataField(USER_URI,
            DataType.URI, false);
    /**
     * The tableUri field.
     */
    private static final DataField tableUri = new DataField(TABLE_URI,
            DataType.URI, false);
    /**
     * The tableId field.
     */
    private static final DataField tableId = new DataField(TABLE_ID,
            DataType.STRING, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(userUri);
        fields.add(tableUri);
        fields.add(tableId);
    }
    /**
     * The singleton instance of the Cursor relation.
     */
    private static Cursors instance;

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
    private Cursors(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    @Override
    public Cursor initialize(Entity entity) throws ODKDatastoreException
    {
        return new Cursor(entity, getCC());
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
    public static Cursors getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || AbstractRelationAdapter.getCC() != cc)
        {
            instance = new Cursors(cc);
        }
        return instance;
    }
}
