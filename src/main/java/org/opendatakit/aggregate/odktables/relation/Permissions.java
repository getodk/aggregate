package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entities.Permission;
import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Permissions defines the access permissions on all Tables.
 * </p>
 * 
 * <p>
 * Permissions is a set of (tableUri, userUri, read, write, delete) tuples, aka
 * 'entities' where
 * <ul>
 * <li>tableUri: the globally unique identifer of a table</li>
 * <li>userUri: the globally unique identifier of a user who should have access
 * to the table</li>
 * <li>read: true if the user with userUri should be able to read the table with
 * tableUri</li>
 * <li>write: true if the user with userUri should be able to write the table
 * with tableUri</li>
 * <li>delete: true if the user with userUri should be able to delete the table
 * with tableUri</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Permissions extends TypedEntityRelation<Permission>
{
    // Field names
    /**
     * The name of the tableUri field.
     */
    public static final String TABLE_URI = "TABLE_URI";

    /**
     * The name of the userUri field.
     */
    public static final String USER_URI = "USER_URI";

    /**
     * The name of the read field.
     */
    public static final String READ = "READ";

    /**
     * The name of the write field.
     */
    public static final String WRITE = "WRITE";

    /**
     * The name of the delete field.
     */
    public static final String DELETE = "DELETE";

    // Relation name
    /**
     * The name of the Permissions relation.
     */
    private static final String RELATION_NAME = "PERMISSIONS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The tableUri field.
     */
    private static final DataField tableUri = new DataField(TABLE_URI,
            DataType.URI, false);
    /**
     * The userUri field.
     */
    private static final DataField userUri = new DataField(USER_URI,
            DataType.URI, false);
    /**
     * The read field.
     */
    private static final DataField read = new DataField(READ, DataType.BOOLEAN,
            false);
    /**
     * The write field.
     */
    private static final DataField write = new DataField(WRITE,
            DataType.BOOLEAN, false);
    /**
     * The delete field.
     */
    private static final DataField delete = new DataField(DELETE,
            DataType.BOOLEAN, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(tableUri);
        fields.add(userUri);
        fields.add(read);
        fields.add(write);
        fields.add(delete);
    }
    /**
     * The singleton instance of the Permissions relation.
     */
    private static Permissions instance;

    /**
     * Constructs an instance which can be used to manipulate the Permissions
     * relation. If the Permissions relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Permissions(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    @Override
    public Permission initialize(Entity entity) throws ODKDatastoreException
    {
        return new Permission(entity, getCC());
    }

    /**
     * Returns the singleton instance of the Permissions relation.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Permissions relation. If the
     *         instance does not exist or if the CallingContext has changed
     *         since it was constructed, then constructs and returns a new
     *         instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Permissions getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || AbstractRelationAdapter.getCC() != cc)
        {
            instance = new Permissions(cc);
        }
        return instance;
    }
}
